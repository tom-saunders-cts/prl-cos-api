package uk.gov.hmcts.reform.prl.services.tab.summary.generators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.ConfidentialDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.ConfidentialDetailsGenerator;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ConfidentialDetailsGeneratorTest {

    private final ConfidentialDetailsGenerator generator = new ConfidentialDetailsGenerator();

    @Test
    public void testIfChildDetailsHasConfidential() {
        Child child = Child.builder().firstName("Test").lastName("Name")
            .isChildAddressConfidential(YesOrNo.Yes).build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .children(listOfChildren)
                                                             .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .confidentialDetails(ConfidentialDetails.builder()
                                                                       .isConfidentialDetailsAvailable(
                                                                           YesOrNo.Yes.getDisplayedValue()).build())
                                                               .build());

    }

    @Test
    public void testIfChildDoesNotHaveDetailsHasConfidential() {
        Child child = Child.builder().firstName("Test").lastName("Name")
            .isChildAddressConfidential(YesOrNo.No).build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .children(listOfChildren)
                                                         .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .confidentialDetails(ConfidentialDetails.builder()
                                                                       .isConfidentialDetailsAvailable(
                                                                           YesOrNo.No.getDisplayedValue()).build())
                                              .build());

    }

    @Test
    public void testIfAnotherPersonDetailsHasConfidentialInfo() {

        Address address = Address.builder()
            .addressLine1("address")
            .postTown("London")
            .build();

        OtherPersonWhoLivesWithChild personWhoLivesWithChild = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();

        Element<OtherPersonWhoLivesWithChild> wrappedList = Element.<OtherPersonWhoLivesWithChild>builder().value(
            personWhoLivesWithChild).build();
        List<Element<OtherPersonWhoLivesWithChild>> listOfOtherPersonsWhoLivedWithChild = Collections.singletonList(
            wrappedList);

        Child child = Child.builder().firstName("Test").lastName("Name")
            .isChildAddressConfidential(YesOrNo.No).personWhoLivesWithChild(listOfOtherPersonsWhoLivedWithChild)
            .build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .children(listOfChildren)
                                                         .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .confidentialDetails(ConfidentialDetails.builder()
                                                                       .isConfidentialDetailsAvailable(
                                                                           YesOrNo.Yes.getDisplayedValue()).build())
                                              .build());

    }

    @Test
    public void testIfApplicantAddressMarkedConfidentialInfo() {
        PartyDetails applicant = PartyDetails.builder().firstName("TestName")
            .isAddressConfidential(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No).build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .applicants(applicantList)
                                                         .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .confidentialDetails(ConfidentialDetails.builder()
                                                                       .isConfidentialDetailsAvailable(
                                                                           YesOrNo.Yes.getDisplayedValue()).build())
                                              .build());

    }

    @Test
    public void testIfApplicantPhonesMarkedConfidentialInfo() {
        PartyDetails applicant = PartyDetails.builder().firstName("TestName")
            .isAddressConfidential(YesOrNo.No)
            .isEmailAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.Yes).build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .applicants(applicantList)
                                                         .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder()
                                              .confidentialDetails(ConfidentialDetails.builder()
                                                                       .isConfidentialDetailsAvailable(
                                                                           YesOrNo.Yes.getDisplayedValue()).build())
                                              .build());

    }
}
