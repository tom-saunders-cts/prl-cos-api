package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.prl.config.cafcass.PostcodeLookupConfiguration;
import uk.gov.hmcts.reform.prl.exception.cafcass.PostcodeValidationException;
import uk.gov.hmcts.reform.prl.models.cafcass.PostcodeResponse;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class PostcodeLookupService {
    private static final Logger LOG = LoggerFactory.getLogger(PostcodeLookupService.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    PostcodeLookupConfiguration configuration;

    public boolean isValidNationalPostCode(String postcode, String countrCode) {
        PostcodeResponse response = fetchCountryFromPostCode(postcode.toUpperCase(Locale.UK));

        boolean isValidPostCode =
                response != null
                        && response.getResults() != null
                        && !response.getResults().isEmpty();

        if (isValidPostCode) {
            return !response.getResults().stream()
                    .filter(eachObj -> null != eachObj.getDpa()
                            && eachObj.getDpa().getCountryCode().equalsIgnoreCase(countrCode))
                    .map(eachObj -> eachObj.getDpa().getBuildingNumber())
                    .collect(Collectors.toList()).isEmpty();
        }

        return isValidPostCode;
    }

    public PostcodeResponse fetchCountryFromPostCode(String postcode) {
        PostcodeResponse results = null;
        try {
            Map<String, String> params = new HashMap<>();
            params.put("postcode", StringUtils.deleteWhitespace(postcode));
            String url = configuration.getUrl();
            String key = configuration.getAccessKey();
            params.put("key", key);
            if (StringUtils.isEmpty(url)) {
                throw new PostcodeValidationException("Postcode URL is null");
            }
            if (StringUtils.isEmpty(key)) {
                throw new PostcodeValidationException("Postcode API Key is null");
            }
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url + "/postcode");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            HttpEntity<String> response =
                    restTemplate.exchange(
                            builder.toUriString(),
                            HttpMethod.GET,
                            new HttpEntity(headers),
                            String.class);

            HttpStatus responseStatus = ((ResponseEntity) response).getStatusCode();

            if (responseStatus.value() == org.apache.http.HttpStatus.SC_OK) {
                results = objectMapper.readValue(response.getBody(), PostcodeResponse.class);

                return results;
            } else if (responseStatus.value() == org.apache.http.HttpStatus.SC_NOT_FOUND) {
                LOG.info("Postcode " + postcode + " not found");
            } else {
                LOG.info("Postcode lookup failed with status {}", responseStatus.value());
            }

        } catch (Exception e) {
            LOG.error("Postcode Lookup Failed - ", e.getMessage());
            throw new PostcodeValidationException(e.getMessage(), e);
        }

        return results;
    }
}