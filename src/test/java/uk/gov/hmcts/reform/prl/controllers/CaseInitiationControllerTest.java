package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.services.EventService;

@RunWith(MockitoJUnitRunner.class)
public class CaseInitiationControllerTest {

    private final String auth = "testAuth";

    @InjectMocks
    CaseInitiationController caseInitiationController;

    @Mock
    EventService eventPublisher;

    @Test
    public void testHandleSubmitted() {


    }

}

