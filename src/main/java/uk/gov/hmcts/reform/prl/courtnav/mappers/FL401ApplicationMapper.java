package uk.gov.hmcts.reform.prl.courtnav.mappers;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum;
import uk.gov.hmcts.reform.prl.enums.FL401Consent;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.FamilyHomeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LivingSituationEnum;
import uk.gov.hmcts.reform.prl.enums.MortgageNamedAfterEnum;
import uk.gov.hmcts.reform.prl.enums.PeopleLivingAtThisAddressEnum;
import uk.gov.hmcts.reform.prl.enums.ReasonForOrderWithoutGivingNoticeEnum;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoBothEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.InterpreterNeed;
import uk.gov.hmcts.reform.prl.models.complextypes.Landlord;
import uk.gov.hmcts.reform.prl.models.complextypes.Mortgage;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDetailsOfWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ReasonForWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.RelationshipDateComplex;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBailConditionDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationDateInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationObjectType;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationOptionsInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.StatementOfTruth;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ApplicantsDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ChildAtAddress;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ProtectedChild;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.RespondentDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicationCoverEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsApplicantEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ConsentEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ContractEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.CurrentResidentAtAddressEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.FamilyHomeOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.LivingSituationOutcomeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SpecialMeasuresEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FL401ApplicationMapper {

    private final CourtFinderService courtFinderService;
    private Court court = null;

    public CaseData mapCourtNavData(CourtNavFl401 courtNavCaseData) throws NotFoundException {

        CaseData caseData = null;
        caseData = CaseData.builder()
            .isCourtNavCase(YesOrNo.Yes)
            .state(State.SUBMITTED_PAID)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .caseOrigin(courtNavCaseData.getMetaData().getCaseOrigin())
            .courtNavApproved(courtNavCaseData.getMetaData().isCourtNavApproved() ? YesOrNo.Yes : YesOrNo.No)
            .numberOfAttachments(String.valueOf(courtNavCaseData.getMetaData().getNumberOfAttachments()))
            .applicantAge(ApplicantAge.getValue(String.valueOf(courtNavCaseData.getFl401().getBeforeStart().getApplicantHowOld())))
            .applicantCaseName(getCaseName(courtNavCaseData))
            .typeOfApplicationOrders(TypeOfApplicationOrders.builder()
                                         .orderType(courtNavCaseData.getFl401().getSituation().getOrdersAppliedFor())
                                         .build())
            .orderWithoutGivingNoticeToRespondent(WithoutNoticeOrderDetails.builder()
                                                      .orderWithoutGivingNotice(courtNavCaseData
                                                                                    .getFl401()
                                                                                    .getSituation()
                                                                                    .isOrdersAppliedWithoutNotice()
                                                                                    ? YesOrNo.Yes : YesOrNo.No)
                                                      .build())
            .reasonForOrderWithoutGivingNotice(!courtNavCaseData.getFl401().getSituation().isOrdersAppliedWithoutNotice() ? null
                                                   : (ReasonForWithoutNoticeOrder.builder()
                .reasonForOrderWithoutGivingNotice(getReasonForWithOutOrderNotice(courtNavCaseData))
                .futherDetails(courtNavCaseData.getFl401().getSituation().getOrdersAppliedWithoutNoticeReasonDetails())
                .build()))
            .bailDetails(RespondentBailConditionDetails.builder()
                             .isRespondentAlreadyInBailCondition(courtNavCaseData
                                                                     .getFl401()
                                                                     .getSituation().isBailConditionsOnRespondent()
                                                                     ? YesNoDontKnow.yes : YesNoDontKnow.no)
                             .bailConditionEndDate(courtNavCaseData.getFl401().getSituation().isBailConditionsOnRespondent()
                                                       ? LocalDate.parse(courtNavCaseData
                                                                             .getFl401()
                                                                             .getSituation()
                                                                             .getBailConditionsEndDate()
                                                                             .mergeDate()) : null)
                             .build())
            .anyOtherDtailsForWithoutNoticeOrder(OtherDetailsOfWithoutNoticeOrder.builder()
                                                     .otherDetails(courtNavCaseData.getFl401().getSituation().getAdditionalDetailsForCourt())
                                                     .build())
            .applicantsFL401(mapApplicant(courtNavCaseData.getFl401().getApplicantDetails()))
            .respondentsFL401(mapRespondent(courtNavCaseData.getFl401().getRespondentDetails()))
            .applicantFamilyDetails(ApplicantFamilyDetails.builder()
                                        .doesApplicantHaveChildren(courtNavCaseData.getFl401().getFamily()
                                                                       .getWhoApplicationIsFor()
                                                                       .equals(ApplicationCoverEnum.applicantOnly)
                                        ? YesOrNo.No : YesOrNo.Yes)
                                        .build())
            .applicantChildDetails(!courtNavCaseData.getFl401().getFamily()
                                       .getWhoApplicationIsFor()
                                       .equals(ApplicationCoverEnum.applicantOnly)
                                       ? mapProtectedChild(courtNavCaseData.getFl401()
                                                               .getFamily().getProtectedChildren()) : null)
            .respondentBehaviourData(courtNavCaseData.getFl401().getSituation()
                                         .getOrdersAppliedFor().contains(FL401OrderTypeEnum.nonMolestationOrder)
                                         ? (RespondentBehaviour.builder()
                .applicantWantToStopFromRespondentDoing(getBehaviourTowardsApplicant(courtNavCaseData))
                .applicantWantToStopFromRespondentDoingToChild(getBehaviourTowardsChildren(courtNavCaseData))
                .otherReasonApplicantWantToStopFromRespondentDoing(courtNavCaseData.getFl401()
                                                                       .getRespondentBehaviour().getStopBehaviourAnythingElse())
                .build()) : null)
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                          .applicantRelationship(ApplicantRelationshipEnum
                                                                     .getDisplayedValueFromEnumString(
                                                                         courtNavCaseData.getFl401()
                                                                             .getRelationshipWithRespondent()
                                                                             .getRelationshipDescription()
                                                                             .getId()))
                                          .build())
            .respondentRelationDateInfoObject((!courtNavCaseData.getFl401().getRelationshipWithRespondent()
                .getRelationshipDescription().getId()
                .equalsIgnoreCase("noneOfAbove"))
                                                  ? (RespondentRelationDateInfo.builder()
                .relationStartAndEndComplexType(RelationshipDateComplex.builder()
                                                    .relationshipDateComplexStartDate(LocalDate.parse(courtNavCaseData
                                                                                                          .getFl401()
                                                                                          .getRelationshipWithRespondent()
                                                                                          .getRelationshipStartDate()
                                                                                          .mergeDate()))
                                                    .relationshipDateComplexEndDate(LocalDate.parse(courtNavCaseData
                                                                                                        .getFl401()
                                                                                        .getRelationshipWithRespondent()
                                                                                        .getRelationshipEndDate()
                                                                                        .mergeDate()))
                                                    .build())
                .applicantRelationshipDate(LocalDate.parse(courtNavCaseData
                                                               .getFl401()
                                                               .getRelationshipWithRespondent()
                                                               .getCeremonyDate().mergeDate()))
                .build()) : null)
            .respondentRelationOptions((courtNavCaseData
                .getFl401()
                .getRelationshipWithRespondent()
                .getRelationshipDescription().getId().equalsIgnoreCase(
                "noneOfAbove"))
                                           ? (RespondentRelationOptionsInfo.builder()
                .applicantRelationshipOptions(courtNavCaseData.getFl401()
                                                  .getRelationshipWithRespondent().getRespondentsRelationshipToApplicant())
                .relationOptionsOther(courtNavCaseData.getFl401()
                                          .getRelationshipWithRespondent().getRelationshipToApplicantOther())
                .build()) : null)
            .home(courtNavCaseData.getFl401().getSituation()
                      .getOrdersAppliedFor().contains(FL401OrderTypeEnum.occupationOrder)
                      ? mapHomeDetails(courtNavCaseData) : null)
            .fl401StmtOfTruth(StatementOfTruth.builder()
                                  .applicantConsent(courtNavCaseData.getFl401()
                                                        .getStatementOfTruth()
                                                        .getDeclaration().stream()
                                                        .map(ConsentEnum::getId)
                                                        .map(FL401Consent::getDisplayedValueFromEnumString)
                                                        .collect(Collectors.toList()))
                                  .signature(courtNavCaseData.getFl401().getStatementOfTruth().getSignature())
                                  .fullname(courtNavCaseData.getFl401().getStatementOfTruth().getSignatureFullName())
                                  .date(LocalDate.parse(courtNavCaseData.getFl401().getStatementOfTruth().getSignatureDate().mergeDate()))
                                  .nameOfFirm(courtNavCaseData.getFl401().getStatementOfTruth().getRepresentativeFirmName())
                                  .signOnBehalf(courtNavCaseData.getFl401().getStatementOfTruth().getRepresentativePositionHeld())
                                  .build())
            .isInterpreterNeeded(courtNavCaseData.getFl401().getGoingToCourt().isInterpreterRequired() ? YesOrNo.Yes : YesOrNo.No)
            .interpreterNeeds(interpreterLanguageDetails(courtNavCaseData))
            .isDisabilityPresent(courtNavCaseData.getFl401().getGoingToCourt().isAnyDisabilityNeeds() ? YesOrNo.Yes : YesOrNo.No)
            .adjustmentsRequired(courtNavCaseData.getFl401().getGoingToCourt().isAnyDisabilityNeeds()
                                     ? courtNavCaseData.getFl401().getGoingToCourt().getDisabilityNeedsDetails() : null)
            .isSpecialArrangementsRequired(!courtNavCaseData.getFl401().getGoingToCourt().getAnySpecialMeasures().isEmpty()
                                               ? YesOrNo.Yes : YesOrNo.No)
            .specialArrangementsRequired(!courtNavCaseData.getFl401().getGoingToCourt().getAnySpecialMeasures().isEmpty()
                                             ? (courtNavCaseData.getFl401().getGoingToCourt().getAnySpecialMeasures()
                .stream()
                .map(SpecialMeasuresEnum::getDisplayedValue)
                .collect(Collectors.joining(","))) : null)
            .specialCourtName(courtNavCaseData.getFl401().getGoingToCourt().getCourtSpecialRequirements())
            .fl401OtherProceedingDetails(FL401OtherProceedingDetails.builder()
                                             .hasPrevOrOngoingOtherProceeding(courtNavCaseData.getFl401().getFamily().isAnyOngoingCourtProceedings()
                                                                                  ? YesNoDontKnow.yes : YesNoDontKnow.no)
                                             .fl401OtherProceedings(courtNavCaseData.getFl401().getFamily().isAnyOngoingCourtProceedings()
                                                                        ? getOngoingProceedings(courtNavCaseData.getFl401()
                                                                                                    .getFamily().getOngoingCourtProceedings()) : null)
                                             .build())
            .build();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
            .dateSubmittedAndTime(DateTimeFormatter.ofPattern("d MMM yyyy, hh:mm:ssa").format(zonedDateTime).toUpperCase())
            .build();

        caseData = caseData.toBuilder()
            .courtName(getCourtName(caseData))
            .courtEmailAddress(getCourtEmailAddress(court))
            .build();

        caseData = caseData.setDateSubmittedDate();

        return caseData;

    }

    private String getCaseName(CourtNavFl401 courtNavCaseData) {

        String applicantName = courtNavCaseData.getFl401().getApplicantDetails().getApplicantFirstName() + " "
            + courtNavCaseData.getFl401().getApplicantDetails().getApplicantLastName();

        String respondentName = courtNavCaseData.getFl401().getRespondentDetails().getRespondentFirstName() + " "
            + courtNavCaseData.getFl401().getRespondentDetails().getRespondentLastName();

        return applicantName + " & " + respondentName;
    }

    private List<ApplicantStopFromRespondentDoingToChildEnum> getBehaviourTowardsChildren(CourtNavFl401 courtNavCaseData) {

        List<BehaviourTowardsChildrenEnum> behaviourTowardsChildrenList = courtNavCaseData
            .getFl401()
            .getRespondentBehaviour()
            .getStopBehaviourTowardsChildren();
        List<ApplicantStopFromRespondentDoingToChildEnum> applicantStopFromRespondentDoingToChildList = new ArrayList<>();
        for (BehaviourTowardsChildrenEnum behaviourTowardsChildren : behaviourTowardsChildrenList) {

            applicantStopFromRespondentDoingToChildList.add(ApplicantStopFromRespondentDoingToChildEnum
                                                                .getDisplayedValueFromEnumString(String.valueOf(
                                                                    behaviourTowardsChildren)));

        }
        return applicantStopFromRespondentDoingToChildList;

    }

    private List<ApplicantStopFromRespondentDoingEnum> getBehaviourTowardsApplicant(CourtNavFl401 courtNavCaseData) {

        List<BehaviourTowardsApplicantEnum> behaviourTowardsApplicantList = courtNavCaseData.getFl401()
            .getRespondentBehaviour().getStopBehaviourTowardsApplicant();
        List<ApplicantStopFromRespondentDoingEnum> applicantStopFromRespondentDoingList = new ArrayList<>();
        for (BehaviourTowardsApplicantEnum behaviourTowardsApplicant : behaviourTowardsApplicantList) {

            applicantStopFromRespondentDoingList.add(ApplicantStopFromRespondentDoingEnum
                                                         .getDisplayedValueFromEnumString(String.valueOf(
                                                             behaviourTowardsApplicant)));

        }
        return applicantStopFromRespondentDoingList;
    }

    private List<ReasonForOrderWithoutGivingNoticeEnum> getReasonForWithOutOrderNotice(CourtNavFl401 courtNavCaseData) {

        List<WithoutNoticeReasonEnum> withoutOrderReasonList = courtNavCaseData.getFl401()
            .getSituation().getOrdersAppliedWithoutNoticeReason();
        List<ReasonForOrderWithoutGivingNoticeEnum> reasonForOrderWithoutGivingNoticeList = new ArrayList<>();
        for (WithoutNoticeReasonEnum withoutOrderReason : withoutOrderReasonList) {
            reasonForOrderWithoutGivingNoticeList.add(ReasonForOrderWithoutGivingNoticeEnum
                                                          .getDisplayedValueFromEnumString(String.valueOf(
                                                              withoutOrderReason)));
        }
        return reasonForOrderWithoutGivingNoticeList;
    }

    private String getCourtName(CaseData caseData) throws NotFoundException {
        court = courtFinderService.getNearestFamilyCourt(caseData);
        return court.getCourtName();
    }

    private String getCourtEmailAddress(Court court1) {

        Optional<CourtEmailAddress> courtEmailAddress = courtFinderService.getEmailAddress(court1);
        return String.valueOf(courtEmailAddress);
    }

    private List<Element<FL401Proceedings>> getOngoingProceedings(List<Element<CourtProceedings>> ongoingCourtProceedings) {

        List<FL401Proceedings> proceedingsList = new ArrayList<>();
        for (Element<CourtProceedings> courtProceedingsElement : ongoingCourtProceedings) {

            CourtProceedings proceedings = courtProceedingsElement.getValue();
            proceedingsList.add(FL401Proceedings.builder()
                .nameOfCourt(proceedings.getNameOfCourt())
                .caseNumber(proceedings.getCaseNumber())
                .typeOfCase(proceedings.getCaseType())
                .anyOtherDetails(proceedings.getCaseDetails())
                .build());
        }
        return ElementUtils.wrapElements(proceedingsList);

    }

    private List<Element<InterpreterNeed>> interpreterLanguageDetails(CourtNavFl401 courtNavCaseData) {

        InterpreterNeed interpreterNeed = InterpreterNeed.builder()
            .language(courtNavCaseData.getFl401().getGoingToCourt().getInterpreterLanguage())
            .otherAssistance(courtNavCaseData.getFl401().getGoingToCourt().getInterpreterDialect())
            .build();

        return List.of(
            ElementUtils.element(interpreterNeed));
    }

    private Home mapHomeDetails(CourtNavFl401 courtNavCaseData) {

        return Home.builder()
            .address(courtNavCaseData.getFl401().getTheHome().getOccupationOrderAddress())
            .peopleLivingAtThisAddress(getPeopleLivingAtThisAddress(courtNavCaseData))
            .textAreaSomethingElse(courtNavCaseData.getFl401().getTheHome().getCurrentlyLivesAtAddressOther())
            .everLivedAtTheAddress(YesNoBothEnum.valueOf(courtNavCaseData.getFl401()
                                                             .getTheHome()
                                                             .getPreviouslyLivedAtAddress().getDisplayedValue()))
            .intendToLiveAtTheAddress(YesNoBothEnum.valueOf(courtNavCaseData.getFl401()
                                                                .getTheHome()
                                                                .getIntendedToLiveAtAddress().getDisplayedValue()))
            .doAnyChildrenLiveAtAddress(YesOrNo.valueOf(null != courtNavCaseData.getFl401()
                .getTheHome().getChildrenApplicantResponsibility() ? "Yes" : "No"))
            .children(mapHomeChildren(courtNavCaseData.getFl401()
                                          .getTheHome().getChildrenApplicantResponsibility()))
            .isPropertyAdapted(courtNavCaseData.getFl401()
                                   .getTheHome().isPropertySpeciallyAdapted() ? YesOrNo.Yes : YesOrNo.No)
            .howIsThePropertyAdapted(courtNavCaseData.getFl401()
                                         .getTheHome().getPropertySpeciallyAdaptedDetails())
            .isThereMortgageOnProperty(courtNavCaseData.getFl401()
                                           .getTheHome().isPropertyHasMortgage() ? YesOrNo.Yes : YesOrNo.No)
            .mortgages(courtNavCaseData.getFl401().getTheHome().isPropertyHasMortgage() ? (Mortgage.builder()
                .mortgageNamedAfter(getMortageDetails(courtNavCaseData))
                .textAreaSomethingElse(courtNavCaseData.getFl401().getTheHome().getNamedOnMortgageOther())
                .mortgageLenderName(courtNavCaseData.getFl401().getTheHome().getMortgageLenderName())
                .mortgageNumber(courtNavCaseData.getFl401().getTheHome().getMortgageNumber())
                .address(courtNavCaseData.getFl401().getTheHome().getLandlordAddress())
                .build()) : null)
            .isPropertyRented(courtNavCaseData.getFl401().getTheHome().isPropertyIsRented() ? YesOrNo.Yes : YesOrNo.No)
            .landlords(courtNavCaseData.getFl401().getTheHome().isPropertyIsRented() ? (Landlord.builder()
                .mortgageNamedAfterList(getLandlordDetails(courtNavCaseData))
                .textAreaSomethingElse(courtNavCaseData.getFl401().getTheHome().getNamedOnRentalAgreementOther())
                .landlordName(courtNavCaseData.getFl401().getTheHome().getLandlordName())
                .address(courtNavCaseData.getFl401().getTheHome().getLandlordAddress())
                .build()) : null)
            .doesApplicantHaveHomeRights(courtNavCaseData.getFl401().getTheHome().isHaveHomeRights() ? YesOrNo.Yes : YesOrNo.No)
            .livingSituation(getLivingSituationDetails(courtNavCaseData))
            .familyHome(getFamilyHomeDetails(courtNavCaseData))
            .build();

    }

    private List<FamilyHomeEnum> getFamilyHomeDetails(CourtNavFl401 courtNavCaseData) {

        List<FamilyHomeOutcomeEnum> familyHomeList = courtNavCaseData.getFl401().getTheHome().getWantToHappenWithFamilyHome();
        List<FamilyHomeEnum> familyHomeEnumList = new ArrayList<>();
        for (FamilyHomeOutcomeEnum familyHome : familyHomeList) {
            familyHomeEnumList.add(FamilyHomeEnum
                                       .getDisplayedValueFromEnumString(String.valueOf(familyHome)));
        }
        return familyHomeEnumList;
    }


    private List<LivingSituationEnum> getLivingSituationDetails(CourtNavFl401 courtNavCaseData) {

        List<LivingSituationOutcomeEnum> livingSituationOutcomeList = courtNavCaseData.getFl401().getTheHome().getWantToHappenWithLivingSituation();
        List<LivingSituationEnum> livingSituationList = new ArrayList<>();
        for (LivingSituationOutcomeEnum livingSituation : livingSituationOutcomeList) {
            livingSituationList.add(LivingSituationEnum
                                        .getDisplayedValueFromEnumString(String.valueOf(livingSituation)));
        }
        return livingSituationList;

    }

    private List<MortgageNamedAfterEnum> getLandlordDetails(CourtNavFl401 courtNavCaseData) {

        List<ContractEnum> contractList = courtNavCaseData.getFl401().getTheHome().getNamedOnRentalAgreement();
        List<MortgageNamedAfterEnum> mortagageNameList = new ArrayList<>();
        for (ContractEnum contract : contractList) {
            mortagageNameList.add(MortgageNamedAfterEnum
                                      .getDisplayedValueFromEnumString(String.valueOf(contract)));
        }
        return mortagageNameList;
    }

    private List<MortgageNamedAfterEnum> getMortageDetails(CourtNavFl401 courtNavCaseData) {

        List<ContractEnum> contractList = courtNavCaseData.getFl401().getTheHome().getNamedOnMortgage();
        List<MortgageNamedAfterEnum> mortagageNameList = new ArrayList<>();
        for (ContractEnum contract : contractList) {
            mortagageNameList.add(MortgageNamedAfterEnum
                                      .getDisplayedValueFromEnumString(String.valueOf(contract)));
        }
        return mortagageNameList;
    }

    private List<PeopleLivingAtThisAddressEnum> getPeopleLivingAtThisAddress(CourtNavFl401 courtNavCaseData) {

        List<CurrentResidentAtAddressEnum> currentlyLivesAtAddressList = courtNavCaseData.getFl401()
            .getTheHome().getCurrentlyLivesAtAddress();
        List<PeopleLivingAtThisAddressEnum> peopleLivingAtThisAddressList = new ArrayList<>();
        for (CurrentResidentAtAddressEnum currentlyLivesAtAddress : currentlyLivesAtAddressList) {

            peopleLivingAtThisAddressList.add(PeopleLivingAtThisAddressEnum
                                                  .getDisplayedValueFromEnumString(String.valueOf(
                                                      currentlyLivesAtAddress)));
        }
        return peopleLivingAtThisAddressList;
    }

    private List<Element<ChildrenLiveAtAddress>> mapHomeChildren(List<Element<ChildAtAddress>> childrenApplicantResponsibility) {

        List<ChildrenLiveAtAddress> childList = new ArrayList<>();
        for (Element<ChildAtAddress> child : childrenApplicantResponsibility) {

            ChildAtAddress value = child.getValue();
            childList.add(ChildrenLiveAtAddress.builder()
                              .keepChildrenInfoConfidential(YesOrNo.No)
                              .childFullName(value.getFullName())
                              .childsAge(String.valueOf(value.getAge()))
                              .isRespondentResponsibleForChild(YesOrNo.No)
                              .build());
        }
        return ElementUtils.wrapElements(childList);
    }

    private List<Element<ApplicantChild>> mapProtectedChild(List<Element<ProtectedChild>> protectedChildren) {

        List<ApplicantChild> childList = new ArrayList<>();
        for (Element<ProtectedChild> protectedChild : protectedChildren) {

            ProtectedChild value = protectedChild.getValue();
            childList.add(ApplicantChild.builder()
                .fullName(value.getFullName())
                .dateOfBirth(LocalDate.parse(value.getDateOfBirth().mergeDate()))
                .applicantChildRelationship(value.getRelationship())
                .applicantRespondentShareParental(value.isParentalResponsibility() ? YesOrNo.Yes : YesOrNo.No)
                .respondentChildRelationship(value.getRespondentRelationship())
                .build());
        }

        return ElementUtils.wrapElements(childList);
    }

    private PartyDetails mapRespondent(RespondentDetails respondent) {
        return PartyDetails.builder()
            .firstName(respondent.getRespondentFirstName())
            .lastName(respondent.getRespondentLastName())
            .previousName(respondent.getRespondentOtherNames())
            .dateOfBirth(LocalDate.parse(respondent.getRespondentDateOfBirth().mergeDate()))
            .isDateOfBirthKnown(YesOrNo.valueOf(null != respondent.getRespondentDateOfBirth() ? "Yes" : "No"))
            .email(respondent.getRespondentEmailAddress())
            .canYouProvideEmailAddress(YesOrNo.valueOf(null != respondent.getRespondentEmailAddress() ? "Yes" : "No"))
            .phoneNumber(respondent.getRespondentPhoneNumber())
            .canYouProvidePhoneNumber(YesOrNo.valueOf(null != respondent.getRespondentPhoneNumber() ? "Yes" : "No"))
            .address(respondent.getRespondentAddress())
            .isCurrentAddressKnown(YesOrNo.valueOf(null != respondent.getRespondentAddress() ? "Yes" : "No"))
            .respondentLivedWithApplicant(respondent.isRespondentLivesWithApplicant() ? YesOrNo.Yes : YesOrNo.No)
            .applicantContactInstructions(null)
            .applicantPreferredContact(null)
            .build();
    }

    private PartyDetails mapApplicant(ApplicantsDetails applicant) {

        return PartyDetails.builder()
            .firstName(applicant.getApplicantFirstName())
            .lastName(applicant.getApplicantLastName())
            .previousName(applicant.getApplicantOtherNames())
            .dateOfBirth(LocalDate.parse(applicant.getApplicantDateOfBirth().mergeDate()))
            .gender(Gender.getDisplayedValueFromEnumString(applicant.getApplicantGender().getValue().getId()))
            .otherGender((!applicant.getApplicantGender().getValue().getId().equals("Male")
                || !applicant.getApplicantGender().getValue().getId().equals("Female"))
                             ? applicant.getApplicantGender().getOther() : null)
            .address(applicant.getApplicantAddress())
            .isAddressConfidential(!applicant.isShareContactDetailsWithRespondent() ? YesOrNo.Yes : YesOrNo.No)
            .canYouProvideEmailAddress(YesOrNo.valueOf(null != applicant.getApplicantEmailAddress() ? "Yes" : "No"))
            .email(applicant.getApplicantEmailAddress())
            .isEmailAddressConfidential(!applicant.isShareContactDetailsWithRespondent() ? YesOrNo.Yes : YesOrNo.No)
            .phoneNumber(applicant.getApplicantPhoneNumber())
            .isPhoneNumberConfidential(!applicant.isShareContactDetailsWithRespondent() ? YesOrNo.Yes : YesOrNo.No)
            .applicantPreferredContact(applicant.getApplicantPreferredContact())
            .applicantContactInstructions(applicant.getApplicantContactInstructions())
            .representativeFirstName(applicant.getLegalRepresentativeFirstName())
            .representativeLastName(applicant.getLegalRepresentativeLastName())
            .solicitorTelephone(applicant.getLegalRepresentativePhone())
            .solicitorReference(applicant.getLegalRepresentativeReference())
            .solicitorEmail(applicant.getLegalRepresentativeEmail())
            .solicitorAddress(applicant.getLegalRepresentativeAddress())
            .dxNumber(applicant.getLegalRepresentativeDx())
            .build();
    }
}
