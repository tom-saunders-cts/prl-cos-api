package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.complextypes.StatementOfTruth;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.DateOfSubmission;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATE_AND_TIME_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_OF_SUBMISSION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL_401_STMT_OF_TRUTH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TESTING_SUPPORT_LD_FLAG_ENABLED;
import static uk.gov.hmcts.reform.prl.enums.Event.TS_SOLICITOR_APPLICATION;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestingSupportService {

    @Autowired
    private final ObjectMapper objectMapper;
    @Autowired
    private final EventService eventPublisher;
    @Autowired
    @Qualifier("allTabsService")
    private final AllTabServiceImpl tabService;
    @Autowired
    private final UserService userService;
    @Autowired
    private final DocumentGenService dgsService;
    @Autowired
    private final CaseWorkerEmailService caseWorkerEmailService;
    @Autowired
    private final AllTabServiceImpl allTabsService;
    private final LaunchDarklyClient launchDarklyClient;
    private final AuthorisationService authorisationService;
    private final RequestUpdateCallbackService requestUpdateCallbackService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    private static final String VALID_C100_DRAFT_INPUT_JSON = "C100_Dummy_Draft_CaseDetails.json";

    private static final String VALID_FL401_DRAFT_INPUT_JSON = "FL401_Dummy_Draft_CaseDetails.json";

    private static final String VALID_C100_GATEKEEPING_INPUT_JSON = "C100_Dummy_Gatekeeping_CaseDetails.json";

    private static final String VALID_FL401_GATEKEEPING_INPUT_JSON = "FL401_Dummy_Gatekeeping_CaseDetails.json";

    public Map<String, Object> initiateCaseCreation(String authorisation, CallbackRequest callbackRequest) throws Exception {
        if (isAuthorized(authorisation)) {
            String requestBody;
            CaseDetails initialCaseDetails = callbackRequest.getCaseDetails();
            CaseData initialCaseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            Map<String, Object> caseDataUpdated = new HashMap<>();
            boolean adminCreateApplication = false;
            if (TS_SOLICITOR_APPLICATION.getId().equalsIgnoreCase(callbackRequest.getEventId())) {
                if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(initialCaseData.getCaseTypeOfApplication())) {
                    requestBody = ResourceLoader.loadJson(VALID_C100_DRAFT_INPUT_JSON);
                } else {
                    requestBody = ResourceLoader.loadJson(VALID_FL401_DRAFT_INPUT_JSON);
                }
            } else {
                if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(initialCaseData.getCaseTypeOfApplication())) {
                    requestBody = ResourceLoader.loadJson(VALID_C100_GATEKEEPING_INPUT_JSON);
                } else {
                    requestBody = ResourceLoader.loadJson(VALID_FL401_GATEKEEPING_INPUT_JSON);
                }
                adminCreateApplication = true;
            }
            CaseDetails dummyCaseDetails = objectMapper.readValue(requestBody, CaseDetails.class);
            if (dummyCaseDetails != null) {
                CaseDetails updatedCaseDetails = dummyCaseDetails.toBuilder()
                    .id(initialCaseDetails.getId())
                    .createdDate(initialCaseDetails.getCreatedDate())
                    .lastModified(initialCaseDetails.getLastModified())
                    .build();
                caseDataUpdated = updatedCaseDetails.getData();
                CaseData updatedCaseData = CaseUtils.getCaseData(updatedCaseDetails, objectMapper);
                if (adminCreateApplication) {
                    caseDataUpdated.putAll(updateDateInCase(initialCaseData.getCaseTypeOfApplication(), updatedCaseData));
                    try {
                        caseDataUpdated.putAll(dgsService.generateDocumentsForTestingSupport(
                            authorisation,
                            updatedCaseData
                        ));
                    } catch (Exception e) {
                        log.error("Error regenerating the document", e);
                    }
                }
            }

            return caseDataUpdated;
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private Map<String, Object> updateDateInCase(String caseTypeOfApplication, CaseData dummyCaseData) {
        Map<String, Object> objectMap = new HashMap<>();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String dateSubmitted = DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime);
        objectMap.put(DATE_SUBMITTED_FIELD, dateSubmitted);
        objectMap.put(
            CASE_DATE_AND_TIME_SUBMITTED_FIELD,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime)
        );
        objectMap.put(ISSUE_DATE_FIELD, LocalDate.now());
        objectMap.put(
            DATE_OF_SUBMISSION,
            DateOfSubmission.builder().dateOfSubmission(CommonUtils.getIsoDateToSpecificFormat(
                dateSubmitted,
                CommonUtils.DATE_OF_SUBMISSION_FORMAT
            ).replace("-", " ")).build()
        );
        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseTypeOfApplication)
            && null != dummyCaseData.getFl401StmtOfTruth()) {
            StatementOfTruth statementOfTruth = dummyCaseData.getFl401StmtOfTruth().toBuilder().date(LocalDate.now()).build();
            objectMap.put(FL_401_STMT_OF_TRUTH, statementOfTruth);
        }
        return objectMap;
    }

    public Map<String, Object> submittedCaseCreation(CallbackRequest callbackRequest, String authorisation) {
        if (isAuthorized(authorisation)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            eventPublisher.publishEvent(new CaseDataChanged(caseData));
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            caseData = caseData.toBuilder()
                .c8Document(objectMapper.convertValue(caseDataUpdated.get(DOCUMENT_FIELD_C8), Document.class))
                .c1ADocument(objectMapper.convertValue(caseDataUpdated.get(DOCUMENT_FIELD_C1A), Document.class))
                .c8WelshDocument(objectMapper.convertValue(
                    caseDataUpdated.get(DOCUMENT_FIELD_C8_WELSH),
                    Document.class
                ))
                .finalDocument(objectMapper.convertValue(caseDataUpdated.get(DOCUMENT_FIELD_FINAL), Document.class))
                .finalWelshDocument(objectMapper.convertValue(
                    caseDataUpdated.get(DOCUMENT_FIELD_FINAL_WELSH),
                    Document.class
                ))
                .c1AWelshDocument(objectMapper.convertValue(
                    caseDataUpdated.get(DOCUMENT_FIELD_C1A_WELSH),
                    Document.class
                ))
                .build();
            tabService.updateAllTabsIncludingConfTab(caseData);
            caseWorkerEmailService.sendEmailToGateKeeper(callbackRequest.getCaseDetails());
            Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);
            caseDataUpdated.putAll(allTabsFields);

            return caseDataUpdated;
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    public Map<String, Object> confirmDummyPayment(CallbackRequest callbackRequest, String authorisation) {
        if (isAuthorized(authorisation)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            log.info("caseData got here ::" + caseData);
            requestUpdateCallbackService.processCallback(ServiceRequestUpdateDto
                                                             .builder()
                                                             .ccdCaseNumber(String.valueOf(caseData.getId()))
                                                             .serviceRequestStatus("Paid")
                                                             .build());
            return coreCaseDataApi.getCase(
                authorisation,
                authTokenGenerator.generate(),
                String.valueOf(caseData.getId())
            ).getData();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private boolean isAuthorized(String authorisation) {
        log.info("test");
        log.info("launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED) "
                     + launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED));
        log.info("authorisationService.authoriseUser(authorisation) "
                     + authorisationService.authoriseUser(authorisation));
        return Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation));
    }
}
