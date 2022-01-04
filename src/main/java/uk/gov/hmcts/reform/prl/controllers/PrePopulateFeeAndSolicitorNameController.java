package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrePopulateFeeAndSolicitorNameController {

    @Autowired
    private FeeService feeService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(path = "/getSolicitorAndFeeDetails", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @ApiOperation(value = "Callback to get Solicitor name and fee amount. ")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "User name received."),
        @ApiResponse(code = 400, message = "Bad Request")})
    public CallbackResponse prePoppulateSolicitorAndFees(@RequestHeader("Authorization") String authorisation,
                                                         @RequestBody CallbackRequest callbackRequest) {
        List<String> errorList = new ArrayList<>();

        UserDetails userDetails = userService.getUserDetails(authorisation);
        FeeResponse feeResponse = null;
        try {
            feeResponse = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
        } catch (Exception e) {
            errorList.add(e.getMessage());
            return CallbackResponse.builder()
                .errors(errorList)
                .build();
        }

        CaseData caseData = objectMapper.convertValue(
            CaseData.builder()
                //   .id(1639057496134831)
                .solicitorName(userDetails.getFullName())
                .feeAmount(feeResponse.getAmount().toString()).build(),
            CaseData.class
        );

        return CallbackResponse.builder()
            .data(caseData)
            .build();

    }
}
