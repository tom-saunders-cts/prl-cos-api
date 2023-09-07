package uk.gov.hmcts.reform.prl.services.hearings;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.cafcass.RefDataService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingService {

    private List<CaseLinkedData> caseLinkedData;

    private final AuthTokenGenerator authTokenGenerator;

    private final HearingApiClient hearingApiClient;

    private final RefDataService refDataService;

    private final HearingDataService hearingDataService;

    @Value("#{'${hearing_component.futureHearingStatus}'.split(',')}")
    private List<String> futureHearingStatusList;

    @Value("${refdata.category-id}")
    private String hearingTypeCategoryId;

    public Hearings getHearings(String userToken, String caseReferenceNumber) {

        Hearings hearings = null;
        try {
            hearings = hearingApiClient.getHearingDetails(userToken, authTokenGenerator.generate(), caseReferenceNumber);
            if (hearings != null) {
                Map<String, String> refDataCategoryValueMap = getRefDataMap(
                    userToken,
                    authTokenGenerator.generate(),
                    hearings.getHmctsServiceCode(),
                    hearingTypeCategoryId
                );
                for (CaseHearing eachHearing : hearings.getCaseHearings()) {
                    eachHearing.setNextHearingDate(getNextHearingDateWithInHearing(eachHearing));
                    eachHearing.setUrgentFlag(getUrgentFlagWithInHearing(eachHearing));
                    eachHearing.setHearingTypeValue(getHearingTypeValueWithInHearing(eachHearing,refDataCategoryValueMap));
                }

                List<CaseHearing> sortedByLatest = hearings.getCaseHearings().stream()
                    .sorted(Comparator.comparing(CaseHearing::getNextHearingDate, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

                hearings.setCaseHearings(sortedByLatest);
            }
            return hearings;

        } catch (Exception e) {
            log.error("Error in getting hearings ", e);
        }
        return null;
    }


    public List<CaseLinkedData> getCaseLinkedData(String userToken, CaseLinkedRequest caseLinkedRequest) {

        try {
            caseLinkedData = hearingApiClient.getCaseLinkedData(userToken, authTokenGenerator.generate(), caseLinkedRequest);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return caseLinkedData;
    }


    public NextHearingDetails getNextHearingDate(String userToken, String caseReferenceNumber) {

        try {
            return hearingApiClient.getNextHearingDate(userToken, authTokenGenerator.generate(), caseReferenceNumber);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public Hearings getFutureHearings(String userToken, String caseReferenceNumber) {

        try {
            return hearingApiClient.getFutureHearings(userToken, authTokenGenerator.generate(), caseReferenceNumber);
        } catch (Exception e) {
            log.error("Error in getFutureHearings ----> {}", e);
        }
        return null;
    }


    private LocalDateTime getNextHearingDateWithInHearing(CaseHearing hearing) {

        LocalDateTime nextHearingDate = null;
        LocalDateTime tempNextDateListed = null;
        if (hearing.getHmcStatus().equals(LISTED)) {
            Optional<LocalDateTime> minDateOfHearingDaySche = nullSafeCollection(hearing.getHearingDaySchedule()).stream()
                .map(HearingDaySchedule::getHearingStartDateTime)
                .filter(hearingStartDateTime -> hearingStartDateTime.isAfter(LocalDateTime.now()))
                .min(LocalDateTime::compareTo);
            if (minDateOfHearingDaySche.isPresent() && (tempNextDateListed == null || tempNextDateListed.isAfter(minDateOfHearingDaySche.get()))) {
                tempNextDateListed = minDateOfHearingDaySche.get();
                nextHearingDate = tempNextDateListed;
            }
        }
        return nextHearingDate;
    }

    private boolean getUrgentFlagWithInHearing(CaseHearing hearing) {

        LocalDateTime urgencyLimitDate = LocalDateTime.now().plusDays(5).plusMinutes(1).withNano(1);
        final List<String> hearingStatuses =
            futureHearingStatusList.stream().map(String::trim).toList();

        boolean isInFutureHearingStatusList = hearingStatuses.stream()
            .anyMatch(
                hearingStatus -> hearingStatus.equals(hearing.getHmcStatus())
            );

        return isInFutureHearingStatusList && hearing.getHmcStatus().equals(LISTED)
            && hearing.getHearingDaySchedule() != null
            && !hearing.getHearingDaySchedule().stream()
            .filter(
                hearDaySche ->
                    hearDaySche
                        .getHearingStartDateTime()
                        .isAfter(
                            LocalDateTime
                                .now())
                        &&
                        hearDaySche
                            .getHearingStartDateTime()
                            .isBefore(
                                urgencyLimitDate)
            )
            .toList()
            .isEmpty();

    }

    private String getHearingTypeValueWithInHearing(CaseHearing hearing, Map<String, String> refDataCategoryValueMap) {

        return !refDataCategoryValueMap.isEmpty() ? refDataCategoryValueMap.get(
            hearing.getHearingType()) : EMPTY_STRING;

    }

    private Map<String, String> getRefDataMap(String authorization, String s2sToken, String serviceCode, String hearingTypeCategoryId) {
        try {
            return refDataService.getRefDataCategoryValueMap(
                authorization,
                s2sToken,
                serviceCode,
                hearingTypeCategoryId
            );
        } catch (Exception e) {
            log.error("Error while calling Ref data api in getRefDataMap method --->  ", e);
        }
        return Collections.emptyMap();
    }

    public List<Hearings> getHearingsByListOfCaseIds(String userToken, Map<String, String> caseIds) {

        try {

            List<Hearings> hearingsList = hearingApiClient.getHearingsByListOfCaseIds(userToken, authTokenGenerator.generate(), caseIds);
            if (null != hearingsList) {
                for (Hearings hearings : hearingsList) {
                    Map<String, String> refDataCategoryValueMap = getRefDataMap(
                        userToken,
                        authTokenGenerator.generate(),
                        hearings.getHmctsServiceCode(),
                        hearingTypeCategoryId
                    );

                    for (CaseHearing eachHearing : hearings.getCaseHearings()) {
                        eachHearing.setNextHearingDate(getNextHearingDateWithInHearing(eachHearing));
                        eachHearing.setUrgentFlag(getUrgentFlagWithInHearing(eachHearing));
                        eachHearing.setHearingTypeValue(getHearingTypeValueWithInHearing(eachHearing,refDataCategoryValueMap));
                    }

                    List<CaseHearing> sortedByLatest = hearings.getCaseHearings().stream()
                        .sorted(Comparator.comparing(CaseHearing::getNextHearingDate,
                                                     Comparator.nullsLast(Comparator.naturalOrder()))
                        ).toList();

                    hearings.setCaseHearings(sortedByLatest);
                }
            }
            return hearingsList;

        } catch (Exception e) {
            log.error("Error in getting hearings ", e);
        }
        return Collections.emptyList();
    }

    public List<Element<HearingData>> getHearingDataFromExistingHearingData(String authorisation,
                                                                                   List<Element<HearingData>> existingOrderHearingDetails,
                                                                                   CaseData caseData) {
        String caseReferenceNumber = String.valueOf(caseData.getId());
        log.info("Inside common HearingService::updateExistingHearingData for {}", caseReferenceNumber);
        if (isNotEmpty(existingOrderHearingDetails)) {
            log.info("Existing hearing details are not empty");
            Hearings hearings = this.getHearings(authorisation, caseReferenceNumber);
            HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
                hearingDataService.populateHearingDynamicLists(authorisation, caseReferenceNumber, caseData, hearings);

            return hearingDataService.getHearingData(
                existingOrderHearingDetails,
                hearingDataPrePopulatedDynamicLists,
                caseData
            );
        }
        return Collections.emptyList();
    }
}
