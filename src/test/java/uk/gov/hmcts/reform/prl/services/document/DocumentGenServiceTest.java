package uk.gov.hmcts.reform.prl.services.document;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.*;
import uk.gov.hmcts.reform.prl.models.*;
import uk.gov.hmcts.reform.prl.models.complextypes.*;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.OtherPersonConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_DOCUMENT_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_DOCUMENT_WELSH_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGenServiceTest {

    @Mock
    private DgsService dgsService;

    @Mock
    DocumentLanguageService documentLanguageService;

    @Mock
    OrganisationService organisationService;

    @InjectMocks
    DocumentGenService documentGenService;

    private GeneratedDocumentInfo generatedDocumentInfo;

    public static final String authToken = "Bearer TestAuthToken";

    CaseData c100CaseData;
    CaseData c100CaseDataC1A;
    CaseData fl401CaseData;
    PartyDetails partyDetails;

    @Before
    public void setUp() {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<ContactInformation> contactInformationList = Collections.singletonList(ContactInformation.builder()
                                                                                        .addressLine1("29, SEATON DRIVE")
                                                                                        .addressLine2("test line")
                                                                                        .townCity("NORTHAMPTON")
                                                                                        .postCode("NN3 9SS")
                                                                                        .build());

        Organisations organisations = Organisations.builder()
            .organisationIdentifier("79ZRSOU")
            .name("Civil - Organisation 2")
            .contactInformation(contactInformationList)
            .build();


        PartyDetails partyDetailsWithOrganisations = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .isAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .isEmailAddressConfidential(Yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("79ZRSOU")
                              .organisationName("Civil - Organisation 2")
                              .build())
            .organisations(organisations)
            .build();

        Element<PartyDetails> applicants = Element.<PartyDetails>builder().value(partyDetailsWithOrganisations).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(applicants);

        ApplicantConfidentialityDetails applicantConfidentialityDetails = ApplicantConfidentialityDetails.builder()
            .phoneNumber("1234567890")
            .firstName("UserFirst")
            .lastName("UserLast")
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .email("test@confidential.com")
            .build();

        Element<ApplicantConfidentialityDetails> applicantConfidential = Element.<ApplicantConfidentialityDetails>builder()
            .value(applicantConfidentialityDetails).build();
        List<Element<ApplicantConfidentialityDetails>> applicantConfidentialList = Collections.singletonList(
            applicantConfidential);

        OtherPersonConfidentialityDetails otherPersonConfidentialityDetails = uk.gov.hmcts.reform.prl.models.complextypes.confidentiality
            .OtherPersonConfidentialityDetails.builder()
            .isPersonIdentityConfidential(Yes)
            .firstName("test1")
            .lastName("last1")
            .relationshipToChildDetails("uncle")
            .address(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .build();

        Element<OtherPersonConfidentialityDetails> otherPerson = Element.<OtherPersonConfidentialityDetails>builder()
            .value(otherPersonConfidentialityDetails).build();
        List<Element<OtherPersonConfidentialityDetails>> otherPersonList = Collections.singletonList(otherPerson);


        ChildConfidentialityDetails childConfidentialityDetails = ChildConfidentialityDetails.builder()
            .firstName("ChildFirst")
            .lastName("ChildLast")
            .otherPerson(otherPersonList)
            .build();

        Element<ChildConfidentialityDetails> childConfidential = Element.<ChildConfidentialityDetails>builder()
            .value(childConfidentialityDetails).build();
        List<Element<ChildConfidentialityDetails>> childConfidentialList = Collections.singletonList(childConfidential);


        c100CaseData = CaseData.builder()
            .id(123456789123L)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .allegationsOfHarmYesNo(Yes)
            .applicants(listOfApplicants)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .allegationsOfHarmYesNo(No)
            .applicantsConfidentialDetails(applicantConfidentialList)
            .childrenConfidentialDetails(childConfidentialList)
            .build();

        c100CaseDataC1A = CaseData.builder()
            .id(123456789123L)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .allegationsOfHarmYesNo(Yes)
            .applicants(listOfApplicants)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .allegationsOfHarmYesNo(Yes)
            .applicantsConfidentialDetails(applicantConfidentialList)
            .childrenConfidentialDetails(childConfidentialList)
            .build();

        ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
            .keepChildrenInfoConfidential(Yes)
            .childFullName("child")
            .childsAge("12")
            .isRespondentResponsibleForChild(YesOrNo.Yes)
            .build();

        Home homefull = Home.builder()
            .address(Address.builder().addressLine1("123").build())
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(YesOrNo.No)
            .doAnyChildrenLiveAtAddress(YesOrNo.Yes)
            .children(List.of(Element.<ChildrenLiveAtAddress>builder().value(childrenLiveAtAddress).build()))
            .isPropertyRented(YesOrNo.No)
            .isThereMortgageOnProperty(YesOrNo.No)
            .isPropertyAdapted(YesOrNo.No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .build();


        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.occupationOrder);

        TypeOfApplicationOrders orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        LinkToCA linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

        fl401CaseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetailsWithOrganisations)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .home(homefull)
            .build();
    }

    @Test
    public void generateDocsForC100Test() throws Exception {

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);

        Map<String, Object> stringObjectMap = documentGenService.generateDocuments(authToken, c100CaseData);

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertFalse(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));
        assertFalse(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A));

        verify(dgsService, times(2)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(2)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void generateDocsForFL401TestWithOrganisation() throws Exception {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(
            fl401CaseData);

        Map<String, Object> stringObjectMap = documentGenService.generateDocuments(authToken, fl401CaseData);

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));

        verify(dgsService, times(2)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(2)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }


    @Test
    public void generateDraftDocsForC100Test() throws Exception {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(c100CaseData);

        Map<String, Object> stringObjectMap = documentGenService.generateDraftDocuments(authToken, c100CaseData);

        assertTrue(stringObjectMap.containsKey(DRAFT_DOCUMENT_FIELD));
        assertTrue(stringObjectMap.containsKey(DRAFT_DOCUMENT_WELSH_FIELD));

        verify(dgsService, times(1)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(1)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void generateDocsForC100TestWithC1A() throws Exception {

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(
            c100CaseDataC1A);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class))).thenReturn(
            c100CaseDataC1A);

        Map<String, Object> stringObjectMap = documentGenService.generateDocuments(authToken, c100CaseDataC1A);

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C1A));

        verify(dgsService, times(3)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(3)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }

    @Test
    public void generateDocsForFL401Test() throws Exception {
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        doReturn(generatedDocumentInfo).when(dgsService).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        doReturn(generatedDocumentInfo).when(dgsService).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        when(organisationService.getApplicantOrganisationDetailsForFL401(Mockito.any(CaseData.class))).thenReturn(
            fl401CaseData);

        Map<String, Object> stringObjectMap = documentGenService.generateDocuments(authToken, fl401CaseData);

        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_C8));
        assertTrue(stringObjectMap.containsKey(DOCUMENT_FIELD_FINAL));

        verify(dgsService, times(2)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(2)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verifyNoMoreInteractions(dgsService);
    }
}



