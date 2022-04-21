package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.OrderDetails;
import uk.gov.hmcts.reform.prl.enums.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;
import javax.ws.rs.core.HttpHeaders;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequiredArgsConstructor
public class ManageOrdersController {

    @Autowired
    private ObjectMapper objectMapper;



    @Autowired
    private final UserService userService;

    @Autowired
    private ManageOrderService manageOrderService;



    @Autowired
    private ManageOrderEmailService manageOrderEmailService;

    @PostMapping(path = "/populate-preview-order", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to show preview order in next screen for upload order")
    public CallbackResponse populatePreviewOrderWhenOrderUploaded(
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData1 = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        CaseData caseData = objectMapper.convertValue(
            CaseData.builder()
                .previewOrderDoc(caseData1.getAppointmentOfGuardian())
                .build(),
            CaseData.class
        );
        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }

    @PostMapping(path = "/fetch-child-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to fetch child details ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Child details are fetched"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public CallbackResponse fetchChildDetails(
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        CaseData caseDataInput = manageOrderService.getUpdatedCaseData(caseData);

        return CallbackResponse.builder()
            .data(caseDataInput)
            .build();
    }

    @PostMapping(path = "/populate-header", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to populate the header")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Child details are fetched"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse populateHeader(
        @RequestBody CallbackRequest callbackRequest
    ) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(manageOrderService.populateHeader(caseData))
            .build();
    }

    @PostMapping(path = "/case-order-email-notification", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Send Email Notification on Case order")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse sendEmailNotificationOnClosingOrder(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest
    ) {

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        manageOrderEmailService.sendEmail(caseDetails);
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping(path = "/save-order-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Save order details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse saveOrderDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {

        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        OrderDetails.builder().orderType(caseData.getSelectedOrder()).orderDocument(caseData.getPreviewOrderDoc())
            .otherDetails(OtherOrderDetails.builder().createdBy(caseData.getCreatedDate()).build());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

}
