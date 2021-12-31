package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.CHILD_DETAILS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.Gender.OTHER;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.ANOTHER_PERSON;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.YES;

@Service
public class ChildChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<List<Element<Child>>> childrenWrapped = ofNullable(caseData.getChildren());

        if (childrenWrapped.isPresent() && childrenWrapped.get().size() != 0) {
            List<Child> children = childrenWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (Child c : children) {
                if (!(validateMandatoryFieldsCompleted(c))) {
                    taskErrorService.addEventError(CHILD_DETAILS, CHILD_DETAILS_ERROR, CHILD_DETAILS_ERROR.getError());
                    return false;
                }
            }
        }
        if (childrenWrapped.isEmpty()) {
            taskErrorService.addEventError(CHILD_DETAILS, CHILD_DETAILS_ERROR, CHILD_DETAILS_ERROR.getError());
            return false;
        }
        taskErrorService.removeError(CHILD_DETAILS_ERROR);
        return true;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        Optional<List<Element<Child>>> childrenWrapped = ofNullable(caseData.getChildren());

        boolean anyStarted = false;

        if (childrenWrapped.isPresent() && childrenWrapped.get().size() != 0) {
            List<Child> children = childrenWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (Child c : children) {
                if (validateAnyFieldStarted(c)) {
                    anyStarted = true;
                }
            }
        }
        return anyStarted;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    private boolean validateMandatoryFieldsCompleted(Child child) {

        List<Optional> fields = new ArrayList<>();
        fields.add(ofNullable(child.getFirstName()));
        fields.add(ofNullable(child.getLastName()));
        fields.add(ofNullable(child.getDateOfBirth()));
        Optional<Gender> gender = ofNullable(child.getGender());
        fields.add(gender);
        if (gender.isPresent() && gender.get().equals(OTHER)) {
            fields.add(ofNullable(child.getOtherGender()));
        }
        fields.add(ofNullable(child.getOrderAppliedFor()));
        fields.add(ofNullable(child.getApplicantsRelationshipToChild()));
        fields.add(ofNullable(child.getRespondentsRelationshipToChild()));
        Optional<List<LiveWithEnum>> childLivesWith = ofNullable(child.getChildLiveWith());
        if (childLivesWith.isPresent() && childLivesWith.get().equals(Collections.emptyList())) {
            return false;
        }
        if (childLivesWith.isPresent() && childLivesWith.get().contains(ANOTHER_PERSON)) {
            fields.add(ofNullable(child.getOtherPersonWhoLivesWithChild()));
        }
        Optional<YesNoDontKnow> childLocalAuth = ofNullable(child.getChildrenKnownToLocalAuthority());
        fields.add(childLocalAuth);
        if (childLocalAuth.isPresent() && childLocalAuth.get().equals(YES)) {
            fields.add(ofNullable(child.getChildrenKnownToLocalAuthorityTextArea()));
        }
        fields.add(ofNullable(child.getChildrenSubjectOfChildProtectionPlan()));

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }

    private boolean validateAnyFieldStarted(Child c) {

        List<Optional> fields = new ArrayList<>();
        fields.add(ofNullable(c.getFirstName()));
        fields.add(ofNullable(c.getLastName()));
        fields.add(ofNullable(c.getDateOfBirth()));
        fields.add(ofNullable(c.getGender()));
        fields.add(ofNullable(c.getOtherGender()));
        fields.add(ofNullable(c.getOrderAppliedFor()));
        fields.add(ofNullable(c.getApplicantsRelationshipToChild()));
        fields.add(ofNullable(c.getRespondentsRelationshipToChild()));
        fields.add(ofNullable(c.getChildLiveWith()));
        fields.add(ofNullable(c.getChildrenKnownToLocalAuthority()));
        fields.add(ofNullable(c.getChildrenSubjectOfChildProtectionPlan()));

        return  fields.stream().anyMatch(Optional::isPresent);
    }

}
