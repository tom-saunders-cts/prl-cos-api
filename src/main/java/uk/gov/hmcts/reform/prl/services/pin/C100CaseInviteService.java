package uk.gov.hmcts.reform.prl.services.pin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
public class C100CaseInviteService implements CaseInviteService {

    @Autowired
    CaseInviteEmailService caseInviteEmailService;

    private CaseInvite generateRespondentCaseInvite(Element<PartyDetails> partyDetails) {
        return new CaseInvite().generateAccessCode(partyDetails.getValue().getEmail(), partyDetails.getId());
    }

    private void sendCaseInvite(CaseInvite caseInvite, PartyDetails partyDetails, CaseData caseData) {
        caseInviteEmailService.sendCaseInviteEmail(caseInvite, partyDetails, caseData);
    }

    public CaseData generateAndSendRespondentCaseInvite(CaseData caseData) {
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvite() != null ? caseData.getCaseInvite() : new ArrayList<>();

        log.info("Generating case invites and sending notification to respondents with email address present");

        for (Element<PartyDetails> respondent : caseData.getRespondents()) {
            if (YesOrNo.Yes.equals(respondent.getValue().getCanYouProvideEmailAddress())) {
                CaseInvite caseInvite = generateRespondentCaseInvite(respondent);
                caseInvites.add(element(caseInvite));
                sendCaseInvite(caseInvite, respondent.getValue(), caseData);
            }
        }
        return caseData.toBuilder().caseInvite(caseInvites).build();
    }

}
