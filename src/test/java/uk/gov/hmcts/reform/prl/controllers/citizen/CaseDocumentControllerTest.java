package uk.gov.hmcts.reform.prl.controllers.citizen;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseDocumentControllerTest {

    @InjectMocks
    private CaseDocumentController caseDocumentController;

    @Mock
    private DocumentGenService documentGenService;

    private GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest;

    @Before
    public void setUp() {

        generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(Map.of("fileName", "test.docx"))
            .build();
    }
}
