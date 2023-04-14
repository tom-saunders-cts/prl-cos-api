package uk.gov.hmcts.reform.prl.controllers.gatekeeping;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.services.AddCaseNoteService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.ListOnNoticeService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LIST_ON_NOTICE_REASONS_SELECTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REASONS_SELECTED_FOR_LIST_ON_NOTICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SELECTED_AND_ADDITIONAL_REASONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBJECT;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class ListOnNoticeController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ListOnNoticeService listOnNoticeService;

    @Autowired
    private AddCaseNoteService addCaseNoteService;

    @Autowired
    RefDataUserService refDataUserService;

    @Autowired
    AllocatedJudgeService allocatedJudgeService;

    @Autowired
    @Qualifier("caseSummaryTab")
    private CaseSummaryTabService caseSummaryTabService;

    @Autowired
    private UserService userService;

    @PostMapping(path = "/listOnNotice/reasonUpdation/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = " mid-event for updating the reason")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse listOnNoticeMidEvent(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("*** mid event triggered for List ON Notice : {}", caseData.getId());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        String reasonsSelectedForListOnNotice =
            listOnNoticeService.getReasonsSelected(caseDataUpdated.get(LIST_ON_NOTICE_REASONS_SELECTED),caseData.getId());
        if (null != reasonsSelectedForListOnNotice && reasonsSelectedForListOnNotice != "") {
            caseDataUpdated.put(SELECTED_AND_ADDITIONAL_REASONS,reasonsSelectedForListOnNotice);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/pre-populate-list-on-notice", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate list on notice")
    public AboutToStartOrSubmitCallbackResponse prePopulateListOnNotice(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        String caseReferenceNumber = String.valueOf(callbackRequest.getCaseDetails().getId());
        log.info("Inside Prepopulate prePopulate for the case id {}", caseReferenceNumber);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        //populate legal advisor list
        List<DynamicListElement> legalAdviserList = refDataUserService.getLegalAdvisorList();
        caseDataUpdated.put("legalAdviserList", DynamicList.builder().value(DynamicListElement.EMPTY).listItems(legalAdviserList)
            .build());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/listOnNotice", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "List On Notice submission flow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List ON notice submission is success"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse listOnNoticeSubmission(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        Long id = callbackRequest.getCaseDetails().getId();
        log.info("List on Notice Submission flow - case id : {}", id);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        String selectedAndAdditonalReasons = (String) caseDataUpdated.get(SELECTED_AND_ADDITIONAL_REASONS);
        if (null != selectedAndAdditonalReasons && selectedAndAdditonalReasons != "") {
            caseDataUpdated.put(CASE_NOTE, selectedAndAdditonalReasons);
            caseDataUpdated.put(SUBJECT, REASONS_SELECTED_FOR_LIST_ON_NOTICE);
        }
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        updateCaseNotes(caseData,caseDataUpdated,authorisation,selectedAndAdditonalReasons);
        AllocatedJudge allocatedJudge = allocatedJudgeService.getAllocatedJudgeDetails(caseDataUpdated,
                                                                                       caseData.getLegalAdviserList(), refDataUserService);
        caseData = caseData.toBuilder().allocatedJudge(allocatedJudge).build();
        caseDataUpdated.putAll(caseSummaryTabService.updateTab(caseData));
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    private void updateCaseNotes(CaseData caseData, Map<String, Object> caseDataUpdated, String authorisation, String selectedAndAdditonalReasons) {
        if (null != selectedAndAdditonalReasons && selectedAndAdditonalReasons != "") {
            caseDataUpdated.put(CASE_NOTES, addCaseNoteService.addCaseNoteDetails(caseData, userService.getUserDetails(authorisation)));
            addCaseNoteService.clearFields(caseDataUpdated);
        }
    }
}
