package uk.gov.hmcts.reform.prl.services.noticeofchange;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.NoticeOfChangeEmail;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NoticeOfChangeContentProvider {

    @Value("${xui.url}")
    private String manageCaseUrl;

    public EmailTemplateVars buildNoticeOfChangeEmail(CaseData caseData, String solicitorName) {
        return NoticeOfChangeEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .solicitorName(solicitorName)
            .caseLink(manageCaseUrl + URL_STRING + caseData.getId())
            .issueDate(CommonUtils.formatDate(D_MMMM_YYYY, caseData.getIssueDate()))
            .build();
    }
}
