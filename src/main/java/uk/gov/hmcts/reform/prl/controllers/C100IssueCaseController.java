package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.C100JsonMapper;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class C100IssueCaseController {

    private final CaseWorkerEmailService caseWorkerEmailService;
    private final ObjectMapper objectMapper;
    private final AllTabServiceImpl allTabsService;
    private final DocumentGenService documentGenService;
    private final SendgridService sendgridService;
    private final C100JsonMapper c100JsonMapper;
    private final LocationRefDataService locationRefDataService;

    @PostMapping(path = "/issue-and-send-to-local-court", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Issue and send to local court")
    public AboutToStartOrSubmitCallbackResponse issueAndSendToLocalCourt(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest) throws Exception {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        if (YesOrNo.No.equals(caseData.getConsentOrder())) {
            requireNonNull(caseData);
            sendgridService.sendEmail(c100JsonMapper.map(caseData));
        }
        caseData = caseData.toBuilder().issueDate(LocalDate.now()).build();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        if (null != caseData.getCourtList() && null != caseData.getCourtList().getValue()) {
            String[] idEmail = caseData.getCourtList().getValue().getCode().split(":");
            String baseLocation = Arrays.stream(idEmail).toArray()[0].toString();
            String key = locationRefDataService.getCourtDetailsFromEpimmsId(baseLocation, authorisation);
            String[] venueDetails = key.split("-");
            String region = Arrays.stream(venueDetails).toArray()[1].toString();
            String courtName = Arrays.stream(venueDetails).toArray()[2].toString();
            String regionName = Arrays.stream(venueDetails).toArray()[4].toString();
            String baseLocationName = Arrays.stream(venueDetails).toArray()[5].toString();
            caseDataUpdated.put("courtName", courtName);
            caseDataUpdated.put("caseManagementLocation", CaseManagementLocation.builder()
                .region(region).baseLocation(baseLocation).regionName(regionName)
                .baseLocationName(baseLocationName).build());
            String courtEmail = "";
            if (idEmail.length > 1) {
                courtEmail = Arrays.stream(idEmail).toArray()[1].toString();
            }
            caseDataUpdated.put("localCourtAdmin", List.of(Element.<LocalCourtAdminEmail>builder().id(UUID.randomUUID())
                                                               .value(LocalCourtAdminEmail.builder().email(courtEmail)
                                                                          .build()).build()));
            caseData.setCourtName(caseDataUpdated.get("courtName").toString());
        }
        caseData.setIssueDate();
        // Generate All Docs and set to casedataupdated.
        caseDataUpdated.putAll(documentGenService.generateDocuments(authorisation, caseData));
        // Refreshing the page in the same event. Hence no external event call needed.
        // Getting the tab fields and add it to the casedetails..
        Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);
        caseDataUpdated.put("issueDate", LocalDate.now());
        caseDataUpdated.putAll(allTabsFields);
        try {
            caseWorkerEmailService.sendEmailToCourtAdmin(callbackRequest.getCaseDetails().toBuilder().data(caseDataUpdated).build());
        } catch (Exception ex) {
            log.error("Email notification could not be sent", ex);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
