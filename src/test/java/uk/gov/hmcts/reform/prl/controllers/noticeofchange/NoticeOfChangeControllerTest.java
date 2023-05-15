package uk.gov.hmcts.reform.prl.controllers.noticeofchange;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NoticeOfChangeControllerTest {

    @InjectMocks
    NoticeOfChangeController noticeOfChangeController;

    @Mock
    NoticeOfChangePartiesService noticeOfChangePartiesService;

    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;
    CallbackRequest callbackRequest;
    String auth = "authorisation";

    @Before
    public void setup() {
        caseDataMap = new HashMap<>();
        caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    @Test
    public void testAboutToSubmitNoCRequest() throws Exception {
        noticeOfChangeController.aboutToSubmitNoCRequest(auth, callbackRequest);
        verify(noticeOfChangePartiesService, times(1)).applyDecision(Mockito.any(CallbackRequest.class), Mockito.anyString());
    }

    @Test
    public void testSubmittedNoCRequest() throws Exception {
        noticeOfChangeController.submittedNoCRequest(auth, callbackRequest);
        verify(noticeOfChangePartiesService, times(1)).nocRequestSubmitted(Mockito.any(CallbackRequest.class), Mockito.anyString());
    }
}
