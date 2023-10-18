package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.DraftAnOrderService;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EditAndApproveDraftOrderController {
    private final ObjectMapper objectMapper;
    private final DraftAnOrderService draftAnOrderService;
    private final ManageOrderService manageOrderService;
    private final HearingDataService hearingDataService;
    private final ManageOrderEmailService manageOrderEmailService;
    private final AuthorisationService authorisationService;
    private final HearingService hearingService;
    private final CoreCaseDataService coreCaseDataService;

    @PostMapping(path = "/populate-draft-order-dropdown", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Populate draft order dropdown")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback to populate draft order dropdown"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse generateDraftOrderDropDown(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            if (caseData.getDraftOrderCollection() != null
                && !caseData.getDraftOrderCollection().isEmpty()) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(draftAnOrderService.getDraftOrderDynamicList(caseData, callbackRequest.getEventId())).build();
            } else {
                return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of("There are no draft orders")).build();
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/judge-or-admin-populate-draft-order",
        consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Remove dynamic list from the caseData")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback to populate draft order dropdown"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateJudgeOrAdminDraftOrder(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(draftAnOrderService.populateDraftOrderDocument(
                    caseData)).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }

    }

    @PostMapping(path = "/judge-or-admin-edit-approve/mid-event", consumes = APPLICATION_JSON,
        produces = APPLICATION_JSON)
    @Operation(description = "Callback to generate draft order collection")
    public AboutToStartOrSubmitCallbackResponse prepareDraftOrderCollection(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            Map<String, Object> caseDataUpdated = draftAnOrderService.judgeOrAdminEditApproveDraftOrderMidEvent(
                authorisation,
                callbackRequest
            );
            log.info("/judge-or-admin-edit-approve/mid-event caseDataUpdated {}", caseDataUpdated);
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/judge-or-admin-edit-approve/about-to-submit",
        consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Remove dynamic list from the caseData")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback to populate draft order dropdown"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse saveServeOrderDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            String isHearingTaskNeeded = null;
            manageOrderService.resetChildOptions(callbackRequest);
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            log.info("Serve order multiselect {}", caseDataUpdated.get("serveOrderDynamicList"));
            caseDataUpdated.putAll(draftAnOrderService.judgeOrAdminEditApproveDraftOrderAboutToSubmit(
                authorisation,
                callbackRequest
            ));
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            manageOrderService.setMarkedToServeEmailNotification(caseData, caseDataUpdated);
            //PRL-4216 - save server order additional documents if any
            manageOrderService.saveAdditionalOrderDocuments(authorisation, caseData, caseDataUpdated);

            CaseUtils.setCaseState(callbackRequest, caseDataUpdated);

            ManageOrderService.cleanUpSelectedManageOrderOptions(caseDataUpdated);
            //Added this for hearing WA task creation
            if (CollectionUtils.isNotEmpty(caseData.getManageOrders().getOrdersHearingDetails())) {
                isHearingTaskNeeded = manageOrderService.checkIfHearingTaskNeeded(caseData.getManageOrders().getOrdersHearingDetails());
            }
            caseDataUpdated.put("isHearingTaskNeeded", isHearingTaskNeeded);
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/judge-or-admin-populate-draft-order-custom-fields", consumes = APPLICATION_JSON,
        produces = APPLICATION_JSON)
    @Operation(description = "Remove dynamic list from the caseData")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback to populate draft order dropdown"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateJudgeOrAdminDraftOrderCustomFields(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws  Exception {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            DraftOrder selectedOrder = draftAnOrderService.getSelectedDraftOrderDetails(caseData);
            if (selectedOrder != null && (CreateSelectOrderOptionsEnum.blankOrderOrDirections.equals(selectedOrder.getOrderType()))
            ) {
                caseData = draftAnOrderService.updateCustomFieldsWithApplicantRespondentDetails(callbackRequest, caseData);
                caseDataUpdated.putAll(draftAnOrderService.getDraftOrderInfo(authorisation, caseData));
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(caseDataUpdated).build();
            }
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(draftAnOrderService.populateDraftOrderCustomFields(caseData, authorisation)).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }

    }

    @PostMapping(path = "/judge-or-admin-populate-draft-order-common-fields", consumes = APPLICATION_JSON,
        produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate common fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated common fields"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateCommonFields(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            Map<String, Object> response = draftAnOrderService.populateCommonDraftOrderFields(authorisation, caseData);
            return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(response).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/judge-or-admin-edit-approve/serve-order/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse editAndServeOrderMidEvent(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            log.info("/judge-or-admin-edit-approve/serve-order/mid-event callbackRequest {}", callbackRequest);
            return AboutToStartOrSubmitCallbackResponse.builder().data(manageOrderService.checkOnlyC47aOrderSelectedToServe(
                callbackRequest)).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/pre-populate-standard-direction-order-other-fields", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate standard direction order fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Populated Headers"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse populateSdoOtherFields(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            if (DraftAnOrderService.checkStandingOrderOptionsSelected(caseData)) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(draftAnOrderService.populateStandardDirectionOrder(authorisation, caseData)).build();
            } else {
                List<String> errorList = new ArrayList<>();
                errorList.add(
                    "Please select at least one options from below");
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errorList)
                    .build();
            }
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/judge-or-admin-edit-approve/submitted", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Send Email Notification on Case order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse sendEmailNotificationToRecipientsServeOrder(
        @RequestHeader(javax.ws.rs.core.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            if (Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId()
                .equalsIgnoreCase(callbackRequest.getEventId())) {
                CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

                if (Yes.equals(caseData.getManageOrders().getMarkedToServeEmailNotification())) {
                    manageOrderEmailService.sendEmailWhenOrderIsServed(authorisation, caseData, caseDataUpdated);
                }

                caseDataUpdated.put(STATE, caseData.getState());

                //Cleanup
                /*ManageOrderService.cleanUpSelectedManageOrderOptions(caseDataUpdated);*/

                coreCaseDataService.triggerEvent(
                        JURISDICTION,
                        CASE_TYPE,
                        caseData.getId(),
                        "internal-update-all-tabs",
                        caseDataUpdated
                );
            }

            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
