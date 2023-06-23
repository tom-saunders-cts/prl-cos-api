package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AmendCourtServiceTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AmendCourtService amendCourtService;

    @Mock
    private CaseUtils caseUtils;

    @Mock
    private LocationRefDataService locationRefDataService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;

    @Mock
    private C100IssueCaseService c100IssueCaseService;

    @Mock
    private CourtSealFinderService courtSealFinderService;

    private CaseData caseData;
    private CallbackRequest callbackRequest;
    private Map<String, Object> caseDataMap;
    private CourtVenue courtVenue;

    @Before
    public void setUp() {
        caseDataMap = new HashMap<>();
        courtVenue = CourtVenue.builder().build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder().id(123L).state(State.CASE_ISSUED.getValue()).data(caseDataMap).build()).build();
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .courtList(DynamicList.builder().value(DynamicListElement.builder().code(":test@test.com").build()).build())
            .build();
        when(locationRefDataService.getCourtDetailsFromEpimmsId(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.of(courtVenue));
        when(c100IssueCaseService.getFactCourtId(courtVenue)).thenReturn("");
        when(courtSealFinderService.getCourtSeal(Mockito.anyString())).thenReturn("");
    }

    @Test
    public void testC100EmailNotification() throws Exception {
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        when(locationRefDataService.getCourtDetailsFromEpimmsId(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.of(courtVenue));
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        amendCourtService.handleAmendCourtSubmission("", callbackRequest, caseDataMap);
        verifyNoInteractions(emailService);
    }

    @Test
    public void testC100EmailNotificationWithEmail() throws Exception {
        caseData = caseData.toBuilder().courtEmailAddress("").build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        when(locationRefDataService.getCourtDetailsFromEpimmsId(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.empty());
        when(emailService.getCaseData(Mockito.any(CaseDetails.class))).thenReturn(caseData);
        amendCourtService.handleAmendCourtSubmission("", callbackRequest, caseDataMap);
        verify(emailService, times(1)).send(Mockito.anyString(),
                                            Mockito.any(),
                                            Mockito.any(), Mockito.any()
        );
    }
}
