package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.RequestUpdateCallbackService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ServiceRequestUpdateCallbackController {

    private final RequestUpdateCallbackService requestUpdateCallbackService;


    @PostMapping(path = "/service-request-update", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Ways to pay will call this API and send the status of payment with other details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed.", response = CallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")})
    public void serviceRequestUpdate(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestBody ServiceRequestUpdateDto serviceRequestUpdateDto
    ) {
        try {
            requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);
        }catch(Exception ex) {
            log.info(
                "Payment callback is unsuccessfull for the CaseID: {}",
                serviceRequestUpdateDto.getCcdCaseNumber()
            );
        }
    }
}
