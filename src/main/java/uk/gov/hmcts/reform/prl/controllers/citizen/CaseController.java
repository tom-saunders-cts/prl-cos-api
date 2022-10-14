package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class CaseController {

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CaseService caseService;

    @GetMapping(path = "/{caseId}", produces = APPLICATION_JSON)
    @Operation(description = "Frontend to fetch the data")
    public CaseData getCase(
        @PathVariable("caseId") String caseId,
        @RequestHeader(value = "Authorization", required = false) @Parameter(hidden = true) String userToken,
        @RequestHeader("serviceAuthorization") String s2sToken
    ) {
        CaseDetails caseDetails = coreCaseDataApi.getCase(userToken, s2sToken, caseId);
        caseDetails.getData().put("state", caseDetails.getState());
        return objectMapper.convertValue(
            caseDetails.getData(),
            CaseData.class
        );
    }

    @PostMapping(value = "{caseId}/{eventId}/update-case", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Updating casedata")
    public CaseData updateCase(
        @Valid @NotNull @RequestBody CaseData caseData,
        @PathVariable("caseId") String caseId,
        @PathVariable("eventId") String eventId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader("serviceAuthorization") String s2sToken,
        @RequestHeader("accessCode") String accessCode
    ) {
        if ("linkCase".equalsIgnoreCase(eventId)) {
            caseService.linkCitizenToCase(authorisation, s2sToken, accessCode, caseId);
            return objectMapper.convertValue(
                coreCaseDataApi.getCase(authorisation, s2sToken, caseId).getData(),
                CaseData.class
            );
        } else {
            return objectMapper.convertValue(caseService.updateCase(
                caseData,
                authorisation,
                s2sToken,
                caseId,
                eventId
            ).getData(), CaseData.class);
        }
    }

    @GetMapping(path = "/citizen/{role}/retrieve-cases/{userId}", produces = APPLICATION_JSON)
    public List<CaseData> retrieveCases(
        @PathVariable("role") String role,
        @PathVariable("userId") String userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader("serviceAuthorization") String s2sToken
    ) {
        return caseService.retrieveCases(authorisation, s2sToken, role, userId);
    }

    @GetMapping(path = "/cases", produces = APPLICATION_JSON)
    public List<CaseData> retrieveCitizenCases(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader("serviceAuthorization") String s2sToken
    ) {
        return caseService.retrieveCases(authorisation, s2sToken);
    }

    @PostMapping(path = "/citizen/link", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Linking case to citizen account with access code")
    public void linkCitizenToCase(@RequestHeader("caseId") String caseId,
                                  @RequestHeader("accessCode") String accessCode,
                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                  @RequestHeader("serviceAuthorization") String s2sToken) {
        caseService.linkCitizenToCase(authorisation, s2sToken, accessCode, caseId);
    }

    @GetMapping(path = "/validate-access-code", produces = APPLICATION_JSON)
    @Operation(description = "Frontend to fetch the data")
    public String validateAccessCode(@RequestHeader(value = "Authorization", required = true) String authorisation,
                                     @RequestHeader(value = "serviceAuthorization", required = true) String s2sToken,
                                     @RequestHeader(value = "caseId", required = true) String caseId,
                                     @RequestHeader(value = "accessCode", required = true) String accessCode) {
        return caseService.validateAccessCode(authorisation, s2sToken, caseId, accessCode);
    }
}
