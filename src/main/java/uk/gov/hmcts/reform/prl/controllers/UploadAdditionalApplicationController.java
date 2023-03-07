package uk.gov.hmcts.reform.prl.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UploadAdditionalApplicationService;
import uk.gov.hmcts.reform.prl.utils.ApplicantsListGenerator;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UploadAdditionalApplicationController {

    public static final String TEMPORARY_OTHER_APPLICATIONS_BUNDLE = "temporaryOtherApplicationsBundle";
    public static final String TEMPORARY_C_2_DOCUMENT = "temporaryC2Document";
    public static final String ADDITIONAL_APPLICANTS_LIST = "additionalApplicantsList";
    public static final String TYPE_OF_C_2_APPLICATION = "typeOfC2Application";
    public static final String ADDITIONAL_APPLICATIONS_APPLYING_FOR = "additionalApplicationsApplyingFor";
    private final ApplicantsListGenerator applicantsListGenerator;

    private final ObjectMapper objectMapper;

    @Autowired
    private final UploadAdditionalApplicationService uploadAdditionalApplicationService;


    @PostMapping(path = "/pre-populate-applicants", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to Generate applicants")
    public AboutToStartOrSubmitCallbackResponse prePopulateApplicants(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put(ADDITIONAL_APPLICANTS_LIST, applicantsListGenerator.buildApplicantsList(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }


    @PostMapping(path = "/upload-additional-application/about-to-submit", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to create additional application bundle ")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Bundle created"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse createUploadAdditionalApplicationBundle(@RequestHeader("Authorization")
                                                                                        @Parameter(hidden = true) String authorisation,
                                                                                        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<Element<AdditionalApplicationsBundle>> additionalApplicationElements =
            uploadAdditionalApplicationService.getAdditionalApplicationElements(
                authorisation,
                caseData
            );
        additionalApplicationElements.sort(Comparator.comparing(
            m -> m.getValue().getUploadedDateTime(),
            Comparator.reverseOrder()
        ));
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("additionalApplicationsBundle", additionalApplicationElements);

        cleanUpUploadAdditionalApplicationData(caseDataUpdated);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    private static void cleanUpUploadAdditionalApplicationData(Map<String, Object> caseDataUpdated) {
        if (caseDataUpdated.containsKey(TEMPORARY_OTHER_APPLICATIONS_BUNDLE)) {
            caseDataUpdated.remove(TEMPORARY_OTHER_APPLICATIONS_BUNDLE);
        }
        if (caseDataUpdated.containsKey(TEMPORARY_C_2_DOCUMENT)) {
            caseDataUpdated.remove(TEMPORARY_C_2_DOCUMENT);
        }
        if (caseDataUpdated.containsKey(ADDITIONAL_APPLICANTS_LIST)) {
            caseDataUpdated.remove(ADDITIONAL_APPLICANTS_LIST);
        }
        if (caseDataUpdated.containsKey(TYPE_OF_C_2_APPLICATION)) {
            caseDataUpdated.remove(TYPE_OF_C_2_APPLICATION);
        }
        if (caseDataUpdated.containsKey(ADDITIONAL_APPLICATIONS_APPLYING_FOR)) {
            caseDataUpdated.remove(ADDITIONAL_APPLICATIONS_APPLYING_FOR);
        }
    }


}