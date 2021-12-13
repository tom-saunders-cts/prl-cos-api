package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.*;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allEmpty;

@Service
public class InternationalElementChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {


        Optional<YesOrNo> habitualResidence = ofNullable(caseData.getHabitualResidentInOtherState());
        Optional<String> habitualResidenceReason = ofNullable(caseData.getHabitualResidentInOtherStateGiveReason());

        Optional<YesOrNo> jurisdictionIssue = ofNullable(caseData.getJurisdictionIssue());
        Optional<String> jurisdictionIssueReason = ofNullable(caseData.getJurisdictionIssueGiveReason());

        Optional<YesOrNo> requestToForeignAuthority = ofNullable(caseData.getRequestToForeignAuthority());
        Optional<String> requestToForeignAuthorityReason = ofNullable(caseData.getRequestToForeignAuthorityGiveReason());

        if (habitualResidence.isEmpty() && jurisdictionIssue.isEmpty() && requestToForeignAuthority.isEmpty()) {
            return false;
        }

        boolean fieldsCompleted = true;

        if (habitualResidence.isPresent() && habitualResidence.get().equals(YES)) {
            fieldsCompleted = habitualResidenceReason.isPresent();
        }
        if (jurisdictionIssue.isPresent() && jurisdictionIssue.get().equals(YES)) {
            fieldsCompleted = jurisdictionIssueReason.isPresent();
        }
        if (requestToForeignAuthority.isPresent() && requestToForeignAuthority.get().equals(YES)) {
            fieldsCompleted = requestToForeignAuthorityReason.isPresent();
        }

        if (fieldsCompleted) {
            taskErrorService.removeError(INTERNATIONAL_ELEMENT_ERROR);
            return true;
        }
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {

        boolean isStarted = false;

        Optional<YesOrNo> habitualResidence = ofNullable(caseData.getHabitualResidentInOtherState());
        Optional<String> habitualResidenceReason = ofNullable(caseData.getHabitualResidentInOtherStateGiveReason());

        Optional<YesOrNo> jurisdictionIssue = ofNullable(caseData.getJurisdictionIssue());
        Optional<String> jurisdictionIssueReason = ofNullable(caseData.getJurisdictionIssueGiveReason());

        Optional<YesOrNo> requestToForeignAuthority = ofNullable(caseData.getRequestToForeignAuthority());
        Optional<String> requestToForeignAuthorityReason = ofNullable(caseData.getRequestToForeignAuthorityGiveReason());

        if (habitualResidence.isPresent() &&  (habitualResidenceReason.isEmpty() || habitualResidenceReason.get().isBlank())) {
            isStarted = addErrorAndReturnTrue();
        }
        if (jurisdictionIssue.isPresent() &&  (jurisdictionIssueReason.isEmpty() || jurisdictionIssueReason.get().isBlank())) {
            isStarted = addErrorAndReturnTrue();
        }
        if (requestToForeignAuthority.isPresent() &&  (requestToForeignAuthorityReason.isEmpty() || requestToForeignAuthorityReason.get().isBlank())) {
            isStarted = addErrorAndReturnTrue();
        }
        if (isStarted) {
            taskErrorService.addEventError(
                INTERNATIONAL_ELEMENT,
                INTERNATIONAL_ELEMENT_ERROR,
                INTERNATIONAL_ELEMENT_ERROR.getError()
            );
            return true;
        }
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    public boolean addErrorAndReturnTrue() {
        taskErrorService.addEventError(INTERNATIONAL_ELEMENT, INTERNATIONAL_ELEMENT_ERROR, INTERNATIONAL_ELEMENT_ERROR.getError());
        return true;
    }

//    public void removeValidationErrors(CaseData caseData) {
//        Optional<YesOrNo> habitualResidence = ofNullable(caseData.getHabitualResidentInOtherState());
//        Optional<String> habitualResidenceReason = ofNullable(caseData.getHabitualResidentInOtherStateGiveReason());
//
//        Optional<YesOrNo> jurisdictionIssue = ofNullable(caseData.getJurisdictionIssue());
//        Optional<String> jurisdictionIssueReason = ofNullable(caseData.getJurisdictionIssueGiveReason());
//
//        Optional<YesOrNo> requestToForeignAuthority = ofNullable(caseData.getRequestToForeignAuthority());
//        Optional<String> requestToForeignAuthorityReason = ofNullable(caseData.getRequestToForeignAuthorityGiveReason());
//
//        if ((habitualResidence.isPresent() && habitualResidenceReason.isPresent()) ||
//            (jurisdictionIssue.isPresent() && jurisdictionIssueReason.isPresent()) ||
//            (requestToForeignAuthority.isPresent() && requestToForeignAuthorityReason.isPresent())) {
//
//            taskErrorService.removeError(INTERNATIONAL_ELEMENT_ERROR);
//        }
    }




