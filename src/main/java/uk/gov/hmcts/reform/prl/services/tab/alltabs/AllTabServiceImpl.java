package uk.gov.hmcts.reform.prl.services.tab.alltabs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Qualifier("allTabsService")
public class AllTabServiceImpl implements AllTabsService {

    @Autowired
    ApplicationsTabService applicationsTabService;

    @Autowired
    CoreCaseDataService coreCaseDataService;

    @Autowired
    @Qualifier("caseSummaryTab")
    CaseSummaryTabService caseSummaryTabService;

    @Autowired
    ConfidentialityTabService confidentialityTabService;

    @Override
    public void updateAllTabs(CaseData caseData) {
        Map<String, Object> combinedFieldsMap = getCombinedMap(caseData);
        if (caseData.getDateSubmitted() != null) {
            combinedFieldsMap.put("dateSubmitted", caseData.getDateSubmitted());
        }
        if (caseData.getCourtName() != null) {
            combinedFieldsMap.put("courtName", caseData.getCourtName());
        }
        if (caseData.getCourtId() != null) {
            combinedFieldsMap.put("courtId", caseData.getCourtId());
        }
        // Calling event to refresh the page.
        refreshCcdUsingEvent(caseData, combinedFieldsMap);
    }

    private void refreshCcdUsingEvent(CaseData caseData, Map<String, Object> combinedFieldsMap) {
        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-all-tabs",
            combinedFieldsMap
        );
    }

    public void updateAllTabsIncludingConfTab(CaseData caseData) {
        Map<String, Object> confidentialDetails = confidentialityTabService.updateConfidentialityDetails(caseData);
        Map<String, Object> combinedFieldsMap = getCombinedMap(caseData);
        combinedFieldsMap.putAll(confidentialDetails);

        if (caseData.getDateSubmitted() != null) {
            combinedFieldsMap.put("dateSubmitted", caseData.getDateSubmitted());
        }
        if (caseData.getCourtName() != null) {
            combinedFieldsMap.put("courtName", caseData.getCourtName());
        }
        if (caseData.getCourtId() != null) {
            combinedFieldsMap.put("courtId", caseData.getCourtId());
        }
        if (caseData.getC8Document() != null) {
            combinedFieldsMap.put("c8Document", caseData.getC8Document());
        }
        if (caseData.getC8WelshDocument() != null) {
            combinedFieldsMap.put("c8WelshDocument", caseData.getC8WelshDocument());
        }
        getDocumentsMap(caseData,combinedFieldsMap);
        log.info("*******allegations doc field Tabs********* : {}",combinedFieldsMap.get("c1ADocument"));
        log.info("*******Confidental doc field Tabs********* : {}",combinedFieldsMap.get("c8Document"));
        log.info("*******Final Document doc field Tabs********* : {}",combinedFieldsMap.get("finalDocument"));
        log.info("*******welsh final document doc field Tabs********* : {}",combinedFieldsMap.get("finalWelshDocument"));

        // Calling event to refresh the page.
        refreshCcdUsingEvent(caseData, combinedFieldsMap);
    }

    private Map<String, Object> getDocumentsMap(CaseData caseData, Map<String, Object> documentMap) {
        if (caseData.getC1ADocument() != null) {
            documentMap.put("c1ADocument", caseData.getC1ADocument());
        }
        if (caseData.getC1AWelshDocument() != null) {
            documentMap.put("c1AWelshDocument", caseData.getC1AWelshDocument());
        }
        if (caseData.getFinalDocument() != null) {
            documentMap.put("finalDocument", caseData.getFinalDocument());
        }
        if (caseData.getFinalWelshDocument() != null) {
            documentMap.put("finalWelshDocument", caseData.getFinalWelshDocument());
        }
        return documentMap;
    }

    private Map<String, Object> getCombinedMap(CaseData caseData) {
        Map<String, Object> applicationTabFields = applicationsTabService.updateTab(
            caseData);

        Map<String, Object> summaryTabFields = caseSummaryTabService.updateTab(caseData);

        return Stream.concat(
                applicationTabFields.entrySet().stream(),
                summaryTabFields.entrySet().stream()
            ).collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);

    }

    @Override
    public Map<String, Object> getAllTabsFields(CaseData caseData) {
        return getCombinedMap(caseData);
    }

}
