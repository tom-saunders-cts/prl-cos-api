package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.RequestUpdateCallbackService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceRequestUpdateCallbackControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ServiceRequestUpdateCallbackController serviceRequestUpdateCallbackController;

    @Mock
    private RequestUpdateCallbackService requestUpdateCallbackService;

    @Mock
    private FeeService feesService;

    @Mock
    AuthorisationService authorisationService;

    @Mock
    private FeeResponse feeResponse;

    @Mock
    private ServiceRequestUpdateDto serviceRequestUpdateDto;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String serviceAuthToken = "Bearer TestServiceAuthToken";

    @Before
    public void setUp() {

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .serviceRequestStatus("paid").ccdCaseNumber("123456").build();

        feeResponse = FeeResponse.builder()
            .code("FEE0325")
            .build();
    }

    @Test
    public void testServiceRequestCallBackDetails() throws Exception {

        FeeType feeType = null;

        when(authorisationService.authorise(any())).thenReturn(Boolean.TRUE);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        serviceRequestUpdateCallbackController.serviceRequestUpdate(authToken,serviceAuthToken,serviceRequestUpdateDto);

        verify(requestUpdateCallbackService).processCallback(serviceRequestUpdateDto);
        verifyNoMoreInteractions(requestUpdateCallbackService);

    }

    @Test
    public void testFeeServiceFeeCodeDetails() throws Exception {
        FeeType feeType = null;

        CallbackRequest callbackRequest = CallbackRequest.builder().build();

        when(authorisationService.authorise(any())).thenReturn(Boolean.TRUE);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        serviceRequestUpdateCallbackController.serviceRequestUpdate(authToken, serviceAuthToken,serviceRequestUpdateDto);

        verifyNoMoreInteractions(feesService);

    }

}
