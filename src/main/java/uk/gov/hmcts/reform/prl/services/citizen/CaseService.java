package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.UpdateCaseData;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.caseflags.request.CitizenPartyFlagsRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.FlagDetailRequest;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetailsMeta;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT_WITH_HWF;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getPartyDetailsMeta;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseService {

    public static final String LINK_CASE = "linkCase";
    public static final String INVALID = "Invalid";
    public static final String VALID = "Valid";
    public static final String LINKED = "Linked";
    public static final String YES = "Yes";
    public static final String CASE_INVITES = "caseInvites";
    @Autowired
    private final CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private final CaseRepository caseRepository;

    private final IdamClient idamClient;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final SystemUserService systemUserService;

    @Autowired
    private final CaseDataMapper caseDataMapper;

    private final CcdCoreCaseDataService coreCaseDataService;

    private final NoticeOfChangePartiesService noticeOfChangePartiesService;
    private static final String INVALID_CLIENT = "Invalid Client";

    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    public CaseDetails updateCase(CaseData caseData, String authToken, String s2sToken,
                                  String caseId, String eventId, String accessCode) throws JsonProcessingException {
        if (LINK_CASE.equalsIgnoreCase(eventId) && null != accessCode) {
            linkCitizenToCase(authToken, s2sToken, caseId, accessCode);
            return caseRepository.getCase(authToken, caseId);
        }
        if (CITIZEN_CASE_SUBMIT.getValue().equalsIgnoreCase(eventId)
            || CITIZEN_CASE_SUBMIT_WITH_HWF.getValue().equalsIgnoreCase(eventId)) {
            UserDetails userDetails = idamClient.getUserDetails(authToken);
            UserInfo userInfo = UserInfo
                .builder()
                .idamId(userDetails.getId())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname().orElse(null))
                .emailAddress(userDetails.getEmail())
                .build();

            CaseData updatedCaseData = caseDataMapper
                .buildUpdatedCaseData(caseData.toBuilder().userInfo(wrapElements(userInfo))
                                          .courtName(C100_DEFAULT_COURT_NAME)
                                          .build());
            return caseRepository.updateCase(authToken, caseId, updatedCaseData, CaseEvent.fromValue(eventId));
        }

        return caseRepository.updateCase(authToken, caseId, caseData, CaseEvent.fromValue(eventId));
    }

    public CaseDetails updateCaseDetails(String authToken,
                                         String caseId, String eventId, UpdateCaseData updateCaseData) {

        CaseDetails caseDetails = caseRepository.getCase(authToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        PartyDetails partyDetails = updateCaseData.getPartyDetails();
        PartyEnum partyType = updateCaseData.getPartyType();
        if (null != partyDetails.getUser()) {
            if (C100_CASE_TYPE.equalsIgnoreCase(updateCaseData.getCaseTypeOfApplication())) {
                updatingPartyDetailsCa(caseData, partyDetails, partyType);
            } else {
                caseData = getFlCaseData(caseData, partyDetails, partyType);
            }
            caseData = generateAnswersForNoc(caseData);
            return caseRepository.updateCase(authToken, caseId, caseData, CaseEvent.fromValue(eventId));
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private CaseData generateAnswersForNoc(CaseData caseData) {
        Map<String, Object> caseDataMap = caseData.toMap(objectMapper);
        if (isNotEmpty(caseDataMap)) {
            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                caseDataMap.putAll(noticeOfChangePartiesService.generate(caseData, CARESPONDENT));
                caseDataMap.putAll(noticeOfChangePartiesService.generate(caseData, CAAPPLICANT));
            } else {
                caseDataMap.putAll(noticeOfChangePartiesService.generate(caseData, DARESPONDENT));
                caseDataMap.putAll(noticeOfChangePartiesService.generate(caseData, DAAPPLICANT));
            }
        }
        caseData = objectMapper.convertValue(caseDataMap, CaseData.class);
        return caseData;
    }

    private static CaseData getFlCaseData(CaseData caseData, PartyDetails partyDetails, PartyEnum partyType) {
        if (PartyEnum.applicant.equals(partyType)) {
            if (partyDetails.getUser().getIdamId().equalsIgnoreCase(caseData.getApplicantsFL401().getUser().getIdamId())) {
                caseData = caseData.toBuilder().applicantsFL401(partyDetails).build();
            }
        } else {
            if (partyDetails.getUser().getIdamId().equalsIgnoreCase(caseData.getRespondentsFL401().getUser().getIdamId())) {
                caseData = caseData.toBuilder().respondentsFL401(partyDetails).build();
            }
        }
        return caseData;
    }

    private static void updatingPartyDetailsCa(CaseData caseData, PartyDetails partyDetails, PartyEnum partyType) {
        if (PartyEnum.applicant.equals(partyType)) {
            List<Element<PartyDetails>> applicants = caseData.getApplicants();
            applicants.stream()
                .filter(party -> Objects.equals(
                    party.getValue().getUser().getIdamId(),
                    partyDetails.getUser().getIdamId()
                ))
                .findFirst()
                .ifPresent(party ->
                               applicants.set(applicants.indexOf(party), element(party.getId(), partyDetails))
                );
        } else if (PartyEnum.respondent.equals(partyType)) {
            List<Element<PartyDetails>> respondents = caseData.getRespondents();
            respondents.stream()
                .filter(party -> Objects.equals(
                    party.getValue().getUser().getIdamId(),
                    partyDetails.getUser().getIdamId()
                ))
                .findFirst()
                .ifPresent(party ->
                               respondents.set(respondents.indexOf(party), element(party.getId(), partyDetails))
                );
        }
    }


    public List<CaseData> retrieveCases(String authToken, String s2sToken) {

        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("sortDirection", "desc");
        searchCriteria.put("page", "1");

        return searchCasesLinkedToUser(authToken, s2sToken, searchCriteria);
    }

    private List<CaseData> searchCasesLinkedToUser(String authToken, String s2sToken,
                                                   Map<String, String> searchCriteria) {

        UserDetails userDetails = idamClient.getUserDetails(authToken);
        log.info("userDetails is :: " + userDetails.getEmail());
        List<CaseDetails> caseDetails = new ArrayList<>();
        caseDetails.addAll(performSearch(authToken, userDetails, searchCriteria, s2sToken));
        log.info("caseDetails count :: " + caseDetails.size());
        return caseDetails
            .stream()
            .map(caseDetail -> CaseUtils.getCaseData(caseDetail, objectMapper))
            .collect(Collectors.toList());
    }

    private List<CaseDetails> performSearch(String authToken, UserDetails user, Map<String, String> searchCriteria,
                                            String serviceAuthToken) {
        List<CaseDetails> result;

        result = coreCaseDataApi.searchForCitizen(
            authToken,
            serviceAuthToken,
            user.getId(),
            JURISDICTION,
            CASE_TYPE,
            searchCriteria
        );
        log.info("result size is: " + result.size());
        return result;
    }

    public void linkCitizenToCase(String authorisation, String s2sToken, String caseId, String accessCode) {
        String anonymousUserToken = systemUserService.getSysUserToken();
        CaseData currentCaseData = objectMapper.convertValue(
            coreCaseDataApi.getCase(anonymousUserToken, s2sToken, caseId).getData(),
            CaseData.class
        );

        if (VALID.equalsIgnoreCase(findAccessCodeStatus(accessCode, currentCaseData))) {
            UUID partyId = null;
            YesOrNo isApplicant = YesOrNo.Yes;

            String systemUpdateUserId = systemUserService.getUserId(anonymousUserToken);
            EventRequestData eventRequestData = coreCaseDataService.eventRequest(
                CaseEvent.LINK_CITIZEN,
                systemUpdateUserId
            );
            StartEventResponse startEventResponse =
                coreCaseDataService.startUpdate(
                    anonymousUserToken,
                    eventRequestData,
                    caseId,
                    true
                );

            CaseData caseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(
                startEventResponse,
                objectMapper
            );
            Map<String, Object> caseDataUpdated = new HashMap<>();
            UserDetails userDetails = idamClient.getUserDetails(authorisation);
            String userId = userDetails.getId();
            String emailId = userDetails.getEmail();

            for (Element<CaseInvite> invite : caseData.getCaseInvites()) {
                if (accessCode.equals(invite.getValue().getAccessCode())) {
                    partyId = invite.getValue().getPartyId();
                    isApplicant = invite.getValue().getIsApplicant();
                    invite.getValue().setHasLinked(YES);
                    invite.getValue().setInvitedUserId(userId);
                }
            }
            caseDataUpdated.put(CASE_INVITES, caseData.getCaseInvites());

            processUserDetailsForCase(userId, emailId, caseData, partyId, isApplicant, caseDataUpdated);
            caseRepository.linkDefendant(
                authorisation,
                anonymousUserToken,
                caseId,
                eventRequestData,
                startEventResponse,
                caseDataUpdated
            );
        }
    }

    private void processUserDetailsForCase(String userId, String emailId, CaseData caseData, UUID partyId,
                                           YesOrNo isApplicant, Map<String, Object> caseDataUpdated) {
        //Assumption is for C100 case PartyDetails will be part of list
        // and will always contain the partyId
        // whereas FL401 will have only one party details without any partyId
        if (partyId != null) {
            getValuesFromPartyDetails(caseData, partyId, isApplicant, userId, emailId, caseDataUpdated);
        } else {
            if (YesOrNo.Yes.equals(isApplicant)) {
                User user = caseData.getApplicantsFL401().getUser().toBuilder().email(emailId)
                    .idamId(userId).build();
                caseData.getApplicantsFL401().setUser(user);
                caseDataUpdated.put(FL401_APPLICANTS, caseData.getApplicantsFL401());
            } else {
                User user = caseData.getRespondentsFL401().getUser().toBuilder().email(emailId)
                    .idamId(userId).build();
                caseData.getRespondentsFL401().setUser(user);
                caseDataUpdated.put(FL401_RESPONDENTS, caseData.getRespondentsFL401());

            }
        }
    }

    private void getValuesFromPartyDetails(CaseData caseData, UUID partyId, YesOrNo isApplicant, String userId,
                                           String emailId, Map<String, Object> caseDataUpdated) {
        if (YesOrNo.Yes.equals(isApplicant)) {
            for (Element<PartyDetails> partyDetails : caseData.getApplicants()) {
                if (partyId.equals(partyDetails.getId())) {
                    User user = partyDetails.getValue().getUser().toBuilder().email(emailId)
                        .idamId(userId).build();
                    partyDetails.getValue().setUser(user);
                }
            }

            caseDataUpdated.put(C100_APPLICANTS, caseData.getApplicants());
        } else {
            for (Element<PartyDetails> partyDetails : caseData.getRespondents()) {
                if (partyId.equals(partyDetails.getId())) {
                    User user = partyDetails.getValue().getUser().toBuilder().email(emailId)
                        .idamId(userId).build();
                    partyDetails.getValue().setUser(user);
                }
            }
            caseDataUpdated.put(C100_RESPONDENTS, caseData.getRespondents());
        }
    }

    public String validateAccessCode(String authorisation, String s2sToken, String caseId, String accessCode) {
        CaseData caseData = objectMapper.convertValue(
            coreCaseDataApi.getCase(authorisation, s2sToken, caseId).getData(),
            CaseData.class
        );
        if (null == caseData) {
            return INVALID;
        }
        return findAccessCodeStatus(accessCode, caseData);
    }

    private String findAccessCodeStatus(String accessCode, CaseData caseData) {
        String accessCodeStatus = INVALID;
        if (null == caseData.getCaseInvites() || caseData.getCaseInvites().isEmpty()) {
            return accessCodeStatus;
        }
        List<CaseInvite> matchingCaseInvite = caseData.getCaseInvites()
            .stream()
            .map(Element::getValue)
            .filter(x -> accessCode.equals(x.getAccessCode()))
            .collect(Collectors.toList());

        if (!matchingCaseInvite.isEmpty()) {
            accessCodeStatus = VALID;
            for (CaseInvite caseInvite : matchingCaseInvite) {
                if (YES.equals(caseInvite.getHasLinked())) {
                    accessCodeStatus = LINKED;
                }
            }
        }
        return accessCodeStatus;
    }

    public CaseDetails getCase(String authToken, String caseId) {
        return caseRepository.getCase(authToken, caseId);
    }

    public CaseDetails createCase(CaseData caseData, String authToken) {
        return caseRepository.createCase(authToken, caseData);
    }

    public CaseDetails withdrawCase(CaseData caseData, String caseId, String authToken) {

        WithdrawApplication withDrawApplicationData = caseData.getWithDrawApplicationData();
        Optional<YesOrNo> withdrawApplication = ofNullable(withDrawApplicationData.getWithDrawApplication());
        CaseDetails caseDetails = getCase(authToken, caseId);
        CaseData updatedCaseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder().id(caseDetails.getId()).build();

        if ((withdrawApplication.isPresent() && Yes.equals(withdrawApplication.get()))) {
            updatedCaseData = updatedCaseData.toBuilder()
                .withDrawApplicationData(withDrawApplicationData)
                .build();
        }

        return caseRepository.updateCase(authToken, caseId, updatedCaseData, CaseEvent.CITIZEN_CASE_WITHDRAW);
    }

    public Flags getPartyCaseFlags(String authToken, String caseId, String partyId) {
        CaseDetails caseDetails = getCase(authToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        Optional<PartyDetailsMeta> partyDetailsMeta = getPartyDetailsMeta(
            partyId,
            caseData.getCaseTypeOfApplication(),
            caseData
        );

        if (partyDetailsMeta.isPresent()
            && partyDetailsMeta.get().getPartyDetails() != null
            && !StringUtils.isEmpty(partyDetailsMeta.get().getPartyDetails().getLabelForDynamicList())) {
            Optional<String> partyExternalCaseFlagField = getPartyExternalCaseFlagField(
                caseData.getCaseTypeOfApplication(),
                partyDetailsMeta.get().getPartyType(),
                partyDetailsMeta.get().getPartyIndex()
            );

            if (partyExternalCaseFlagField.isPresent()) {
                return objectMapper.convertValue(
                    caseDetails.getData().get(partyExternalCaseFlagField.get()),
                    Flags.class
                );
            }
        }

        return null;
    }

    public ResponseEntity<Object> updateCitizenRAflags(
        String caseId, String eventId, String authToken, CitizenPartyFlagsRequest citizenPartyFlagsRequest) {
        log.info("Inside updateCitizenRAflags caseId {}", caseId);
        log.info("Inside updateCitizenRAflags eventId {}", eventId);
        log.info("Inside updateCitizenRAflags citizenPartyFlagsRequest {}", citizenPartyFlagsRequest);

        if (StringUtils.isEmpty(citizenPartyFlagsRequest.getPartyIdamId()) || ObjectUtils.isEmpty(
            citizenPartyFlagsRequest.getPartyExternalFlags())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("bad request");
        }

        String systemUpdateUserId = systemUserService.getUserId(authToken);
        CaseEvent caseEvent = CaseEvent.fromValue(eventId);
        EventRequestData eventRequestData = coreCaseDataService.eventRequest(
            caseEvent,
            systemUpdateUserId
        );

        StartEventResponse startEventResponse =
            coreCaseDataService.startUpdate(
                authToken,
                eventRequestData,
                caseId,
                false
            );

        CaseData caseData = CaseUtils.getCaseData(startEventResponse.getCaseDetails(), objectMapper);
        Optional<PartyDetailsMeta> partyDetailsMeta = getPartyDetailsMeta(
            citizenPartyFlagsRequest.getPartyIdamId(),
            caseData.getCaseTypeOfApplication(),
            caseData
        );

        if (!partyDetailsMeta.isPresent()
            || null == partyDetailsMeta.get().getPartyDetails()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("party details not found");
        }

        Map<String, Object> updatedCaseData = startEventResponse.getCaseDetails().getData();
        Optional<String> partyExternalCaseFlagField = getPartyExternalCaseFlagField(
            caseData.getCaseTypeOfApplication(),
            partyDetailsMeta.get().getPartyType(),
            partyDetailsMeta.get().getPartyIndex()
        );

        if (!partyExternalCaseFlagField.isPresent() || !updatedCaseData.containsKey(partyExternalCaseFlagField.get()) || ObjectUtils.isEmpty(
            updatedCaseData.get(
                partyExternalCaseFlagField.get()))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("party external flag details not found");
        }
        log.info("partyExternalCaseFlagField {}", partyExternalCaseFlagField.get());

        Flags flags = objectMapper.convertValue(
            updatedCaseData.get(partyExternalCaseFlagField.get()),
            Flags.class
        );
        log.info("Existing external Party flags {}", flags);
        flags = flags.toBuilder()
            .details(convertFlags(citizenPartyFlagsRequest.getPartyExternalFlags().getDetails()))
            .build();
        log.info("Updated external Party flags {}", flags);
        updatedCaseData.put(partyExternalCaseFlagField.get(), flags);

        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContent(
            startEventResponse,
            updatedCaseData
        );
        coreCaseDataService.submitUpdate(
            authToken,
            eventRequestData,
            caseDataContent,
            caseId,
            false
        );
        return ResponseEntity.status(HttpStatus.OK).body("party flags updated");
    }

    private List<Element<FlagDetail>> convertFlags(List<FlagDetailRequest> details) {
        List<Element<FlagDetail>> flagDetails = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH);

        for (FlagDetailRequest detail : details) {
            if (null != detail.getDateTimeCreated()) {

                LocalDateTime createdDateTime
                    = LocalDateTime
                    .parse(detail.getDateTimeCreated().format(dateTimeFormatter));
                detail.setDateTimeCreated(createdDateTime);
            }

            if (null != detail.getDateTimeModified()) {
                LocalDateTime modifiedDateTime
                    = LocalDateTime
                    .parse(detail.getDateTimeModified().format(dateTimeFormatter));

                detail.setDateTimeModified(modifiedDateTime);
            }

            FlagDetail flagDetail = FlagDetail.builder().name(detail.getName())
                .name_cy(detail.getName_cy())
                .subTypeValue(detail.getSubTypeValue())
                .subTypeValue_cy(detail.getSubTypeValue_cy())
                .subTypeKey(detail.getSubTypeKey())
                .otherDescription(detail.getOtherDescription())
                .otherDescription_cy(detail.getOtherDescription_cy())
                .flagComment(detail.getFlagComment())
                .flagComment_cy(detail.getFlagComment_cy())
                .flagUpdateComment(detail.getFlagUpdateComment())
                .dateTimeCreated(detail.getDateTimeCreated())
                .dateTimeModified(detail.getDateTimeModified())
                .path(detail.getPath())
                .hearingRelevant(detail.getHearingRelevant())
                .flagCode(detail.getFlagCode())
                .status(detail.getStatus())
                .availableExternally(detail.getAvailableExternally())
                .build();
            flagDetails.add(element(flagDetail));
        }

        return flagDetails;
    }

    private Optional<String> getPartyExternalCaseFlagField(String caseType, PartyEnum partyType, Integer partyIndex) {

        Optional<String> partyExternalCaseFlagField = Optional.empty();
        boolean isC100Case = C100_CASE_TYPE.equalsIgnoreCase(caseType);

        if (PartyEnum.applicant == partyType) {
            partyExternalCaseFlagField
                = Optional.ofNullable(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
                caseType,
                isC100Case ? PartyRole.Representing.CAAPPLICANT : PartyRole.Representing.DAAPPLICANT,
                partyIndex
            ));
        } else if (PartyEnum.respondent == partyType) {
            partyExternalCaseFlagField
                = Optional.ofNullable(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
                caseType,
                isC100Case ? PartyRole.Representing.CARESPONDENT : PartyRole.Representing.DARESPONDENT,
                partyIndex
            ));
        }

        return partyExternalCaseFlagField;
    }
}
