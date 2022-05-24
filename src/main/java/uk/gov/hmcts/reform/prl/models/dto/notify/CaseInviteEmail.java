package uk.gov.hmcts.reform.prl.models.dto.notify;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

@Getter
public class CaseInviteEmail extends EmailTemplateVars {

    private final String caseName;
    private final String accessCode;
    private final String respondentFullName;
    private final String caseLink;
    private final String citizenSignUpLink;
    private final String applicantName;

    @Builder
    public CaseInviteEmail(CaseInvite caseInvite, String caseReference, PartyDetails party,
                           String caseLink, String citizenSignUpLink, CaseData caseData) {
        super(caseReference);
        this.accessCode = caseInvite.getAccessCode();
        this.respondentFullName = String.format("%s %s", party.getFirstName(), party.getLastName());
        this.caseLink = caseLink;
        this.citizenSignUpLink = citizenSignUpLink;
        this.caseName = caseData.getApplicantCaseName();
        this.applicantName = getFirstApplicantFullName(caseData);
    }

    private static String getFirstApplicantFullName(CaseData caseData) {
        PartyDetails applicant = ElementUtils.unwrapElements(caseData.getApplicants()).get(0);
        return String.format("%s %s", applicant.getFirstName(), applicant.getLastName());
    }
}
