package uk.gov.hmcts.reform.prl.services.courtnav;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CourtNavCaseServiceTest {

    private final String authToken = "Bearer abc";
    private final String s2sToken = "s2s token";
    private final String randomUserId = "e3ceb507-0137-43a9-8bd3-85dd23720648";
    private static final String randomAlphaNumeric = "Abc123EFGH";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private IdamClient idamClient;

    @Mock
    private CaseUtils caseUtils;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private DocumentGenService documentGenService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    CourtNavCaseService courtNavCaseService;


    private CaseData caseData;

    public MultipartFile file;

    @Before
    public void setup() {
        caseData = CaseData.builder().id(1234567891234567L).applicantCaseName("xyz").build();
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(randomUserId).build());
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(coreCaseDataApi.startForCaseworker(any(), any(), any(), any(), any(), any())
        ).thenReturn(StartEventResponse.builder().eventId("courtnav-case-creation").token("eventToken").build());
        file = new MockMultipartFile(
            "file",
            "private-law.pdf",
            MediaType.TEXT_PLAIN_VALUE,
            "FL401 case".getBytes()
        );
    }

    @Test
    public void shouldStartAndSubmitEventWithEventData() throws Exception {
        Map<String, Object> tempMap = new HashMap<>();
        courtNavCaseService.createCourtNavCase("Bearer abc", caseData);
        verify(coreCaseDataApi).startForCaseworker(authToken, s2sToken,
                                                   randomUserId, PrlAppsConstants.JURISDICTION,
                                                   PrlAppsConstants.CASE_TYPE, "courtnav-case-creation"
        );
    }

    @Ignore
    @Test
    public void shouldUploadDocumentWhenAllFieldsAreCorrect() {
        uk.gov.hmcts.reform.prl.models.documents.Document tempDoc = uk.gov.hmcts.reform.prl.models.documents
            .Document.builder()
            .documentFileName("private-law.pdf")
            .documentUrl(randomAlphaNumeric)
            .documentBinaryUrl(randomAlphaNumeric)
            .build();
        Document document = testDocument();
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken("eventToken")
            .event(Event.builder()
                       .id("courtnav-document-upload")
                       .build())
            .data(Map.of("FL401", tempDoc))
            .build();
        CaseDetails tempCaseDetails = CaseDetails.builder().data(Map.of("id", "1234567891234567")).state(
            "SUBMITTED_PAID").createdDate(
            LocalDateTime.now()).lastModified(LocalDateTime.now()).id(1234567891234567L).build();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        when(coreCaseDataApi.getCase(authToken, s2sToken, "1234567891234567")).thenReturn(tempCaseDetails);
        //when(caseUtils.).thenReturn(caseData);
        when(coreCaseDataApi.startEventForCaseWorker(any(), any(), any(), any(), any(), any(), any())
        ).thenReturn(StartEventResponse.builder().eventId("courtnav-document-upload").token("eventToken").build());
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(randomUserId).build());
        when(coreCaseDataApi.submitEventForCaseWorker(
            authToken,
            s2sToken,
            randomUserId, PrlAppsConstants.JURISDICTION,
            PrlAppsConstants.CASE_TYPE,
            "1234567891234567",
            true,
            caseDataContent
             )
        ).thenReturn(CaseDetails.builder().id(1234567891234567L).data(Map.of(
            "typeOfDocument",
            "fl401Doc1"
        )).build());
        courtNavCaseService.uploadDocument("Bearer abc", file, "FL401",
                                           "1234567891234567"
        );
        verify(coreCaseDataApi).startEventForCaseWorker(
            "Bearer abc",
            "s2s token",
            "e3ceb507-0137-43a9-8bd3-85dd23720648",
            PrlAppsConstants.JURISDICTION,
            PrlAppsConstants.CASE_TYPE,
            "1234567891234567",
            "courtnav-document-upload"
        );
        verify(coreCaseDataApi).submitEventForCaseWorker(
            "Bearer abc",
            "s2s token",
            "e3ceb507-0137-43a9-8bd3-85dd23720648",
            PrlAppsConstants.JURISDICTION,
            PrlAppsConstants.CASE_TYPE,
            "1234567891234567",
            true,
            caseDataContent
        );
    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotUploadDocumentWhenInvalidDocumentTypeOfDocumentIsRequested() {
        courtNavCaseService.uploadDocument("Bearer abc", file, "InvalidTypeOfDocument",
                                           "1234567891234567"
        );
    }

    @Test(expected = ResponseStatusException.class)
    public void shouldNotUploadDocumentWhenInvalidDocumentFormatIsRequested() {
        courtNavCaseService.uploadDocument("Bearer abc", file, "InvalidTypeOfDocument",
                                           "1234567891234567"
        );
    }

    @Test(expected = ResponseStatusException.class)
    public void shouldThrowExceptionWhenInvalidTypeOfDocumentIsPassed() {
        file = new MockMultipartFile(
            "file",
            "private-law.json",
            MediaType.TEXT_PLAIN_VALUE,
            "FL401 case".getBytes()
        );
        courtNavCaseService.uploadDocument("Bearer abc", file, "FL401",
                                           "1234567891234567"
        );
    }

    public static Document testDocument() {
        Document.Link binaryLink = new Document.Link();
        binaryLink.href = randomAlphaNumeric;
        Document.Link selfLink = new Document.Link();
        selfLink.href = randomAlphaNumeric;

        Document.Links links = new Document.Links();
        links.binary = binaryLink;
        links.self = selfLink;

        Document document = Document.builder().build();
        document.links = links;
        document.originalDocumentName = randomAlphaNumeric;

        return document;
    }

}
