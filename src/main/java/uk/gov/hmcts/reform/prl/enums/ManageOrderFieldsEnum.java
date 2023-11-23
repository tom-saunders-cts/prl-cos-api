package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.prl.enums.dio.DioOtherDirectionEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioTransferCourtDirectionEnum;
import uk.gov.hmcts.reform.prl.enums.dio.MiamOtherDirectionEnum;
import uk.gov.hmcts.reform.prl.enums.dio.OtherDirectionPositionStatementEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.AllocateOrReserveJudgeEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoApplicantRespondentEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtRequestedEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoDocumentationAndEvidenceEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoFurtherInstructionsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingUrgentCheckListEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoNextStepsAllocationEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoOtherEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoReportSentByEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoReportsAlsoSentToEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoReportsSentToEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoScheduleOfAllegationsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoSection7ImpactAnalysisEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoTransferApplicationReasonEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoWitnessStatementsSentToEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoWrittenStatementEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.MiamAttendingPersonName;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio.SdoDioProvideOtherDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.AddNewPreamble;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.PartyNameDA;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.SdoDisclosureOfPapersCaseNumber;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.SdoLanguageDialect;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.SdoFurtherDirections;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.SdoNameOfApplicant;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.SdoNameOfRespondent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

import java.time.LocalDate;
import java.util.List;

public enum ManageOrderFieldsEnum {

    manageOrdersOptions("manageOrdersOptions"),
    createSelectOrderOptions("createSelectOrderOptions"),
    childArrangementOrders("childArrangementOrders"),
    domesticAbuseOrders("domesticAbuseOrders"),
    fcOrders("fcOrders"),
    otherOrdersOption("otherOrdersOption"),
    amendOrderDynamicList("amendOrderDynamicList"),
    ordersNeedToBeServed("ordersNeedToBeServed"),
    loggedInUserType("loggedInUserType"),
    doYouWantToServeOrder("doYouWantToServeOrder"),
    whatDoWithOrder("whatDoWithOrder"),
    currentOrderCreatedDateTime("currentOrderCreatedDateTime"),
    approvalDate("approvalDate"),
    previewOrderDoc("previewOrderDoc"),
    previewOrderDocWelsh("previewOrderDocWelsh"),
    wasTheOrderApprovedAtHearing("wasTheOrderApprovedAtHearing"),
    judgeOrMagistratesLastName("judgeOrMagistratesLastName"),
    magistrateLastName("magistrateLastName"),
    justiceLegalAdviserFullName("justiceLegalAdviserFullName"),
    dateOrderMade("dateOrderMade"),
    hasJudgeProvidedHearingDetails("hasJudgeProvidedHearingDetails"),
    amendOrderSelectCheckOptions("amendOrderSelectCheckOptions"),
    hearingsType("hearingsType"),
    ordersHearingDetails("ordersHearingDetails"),
    solicitorOrdersHearingDetails("solicitorOrdersHearingDetails"),
    c21OrderOptions("c21OrderOptions"),
    selectChildArrangementsOrder("selectChildArrangementsOrder"),
    childArrangementsOrdersToIssue("childArrangementsOrdersToIssue"),
    childOption("childOption"),
    makeChangesToUploadedOrder("makeChangesToUploadedOrder"),
    editedUploadOrderDoc("editedUploadOrderDoc"),
    previewUploadedOrder("previewUploadedOrder"),
    uploadOrderDoc("uploadOrderDoc"),
    orderUploadedAsDraftFlag("orderUploadedAsDraftFlag"),
    judgeDirectionsToAdmin("judgeDirectionsToAdmin"),
    instructionsFromJudge("instructionsFromJudge"),
    courtAdminNotes("courtAdminNotes"),
    nameOfOrder("nameOfOrder"),
    appointedGuardianName("appointedGuardianName"),
    orderName("orderName"),
    orderType("orderType"),
    previewDraftOrder("previewDraftOrder"),
    isHearingPageNeeded("isHearingPageNeeded"),
    postalInformationCA("postalInformationCA"),
    cafcassCymruDocuments("cafcassCymruDocuments"),
    draftOrdersDynamicList("draftOrdersDynamicList"),
    previewDraftOrderWelsh("previewDraftOrderWelsh"),
    whenReportsMustBeFiled("whenReportsMustBeFiled"),
    doYouWantToEditTheOrder("doYouWantToEditTheOrder"),
    isOrderCreatedBySolicitor("isOrderCreatedBySolicitor"),
    servingRespondentsOptionsCA("servingRespondentsOptionsCA"),
    otherPeoplePresentInCaseFlag("otherPeoplePresentInCaseFlag"),
    isOnlyC47aOrderSelectedToServe("isOnlyC47aOrderSelectedToServe"),
    cafcassOrCymruNeedToProvideReport("cafcassOrCymruNeedToProvideReport"),
    orderEndsInvolvementOfCafcassOrCymru("orderEndsInvolvementOfCafcassOrCymru"),
    selectTypeOfOrder("selectTypeOfOrder"),

    //Hearing screen field show params
    isCafcassCymru("isCafcassCymru"),
    isFL401ApplicantPresent("isFL401ApplicantPresent"),
    isFL401ApplicantSolicitorPresent("isFL401ApplicantSolicitorPresent"),
    isFL401RespondentPresent("isFL401RespondentPresent"),
    isFL401RespondentSolicitorPresent("isFL401RespondentSolicitorPresent"),
    isApplicant1Present("isApplicant1Present"),
    isApplicant2Present("isApplicant2Present"),
    isApplicant3Present("isApplicant3Present"),
    isApplicant4Present("isApplicant4Present"),
    isApplicant5Present("isApplicant5Present"),
    isApplicant1SolicitorPresent("isApplicant1SolicitorPresent"),
    isApplicant2SolicitorPresent("isApplicant2SolicitorPresent"),
    isApplicant3SolicitorPresent("isApplicant3SolicitorPresent"),
    isApplicant4SolicitorPresent("isApplicant4SolicitorPresent"),
    isApplicant5SolicitorPresent("isApplicant5SolicitorPresent"),
    isRespondent1Present("isRespondent1Present"),
    isRespondent2Present("isRespondent2Present"),
    isRespondent3Present("isRespondent3Present"),
    isRespondent4Present("isRespondent4Present"),
    isRespondent5Present("isRespondent5Present"),
    isRespondent1SolicitorPresent("isRespondent1SolicitorPresent"),
    isRespondent2SolicitorPresent("isRespondent2SolicitorPresent"),
    isRespondent3SolicitorPresent("isRespondent3SolicitorPresent"),
    isRespondent4SolicitorPresent("isRespondent4SolicitorPresent"),
    isRespondent5SolicitorPresent("isRespondent5SolicitorPresent"),

    //clear all missing fields
    selectedOrder("selectedOrder"),
    isTheOrderUploadedByConsent("isTheOrderUploadedByConsent"),
    selectedC21Order("selectedC21Order"),
    withdrawnOrRefusedOrder("withdrawnOrRefusedOrder"),
    isCaseWithdrawn("isCaseWithdrawn"),
    isTheOrderByConsent("isTheOrderByConsent"),
    hearingType("hearingType"),
    judgeOrMagistrateTitle("judgeOrMagistrateTitle"),
    isTheOrderAboutAllChildren("isTheOrderAboutAllChildren"),
    isTheOrderAboutChildren("isTheOrderAboutChildren"),
    recitalsOrPreamble("recitalsOrPreamble"),
    orderDirections("orderDirections"),
    furtherDirectionsIfRequired("furtherDirectionsIfRequired"),
    furtherInformationIfRequired("furtherInformationIfRequired"),

    manageOrdersCourtName("manageOrdersCourtName"),
    manageOrdersCourtAddress("manageOrdersCourtAddress"),
    manageOrdersCaseNo("manageOrdersCaseNo"),
    manageOrdersApplicant("manageOrdersApplicant"),
    manageOrdersApplicantReference("manageOrdersApplicantReference"),
    manageOrdersRespondent("manageOrdersRespondent"),
    manageOrdersRespondentReference("manageOrdersRespondentReference"),
    manageOrdersRespondentDob("manageOrdersRespondentDob"),
    manageOrdersRespondentAddress("manageOrdersRespondentAddress"),
    manageOrdersUnderTakingRepr("manageOrdersUnderTakingRepr"),
    underTakingSolicitorCounsel("underTakingSolicitorCounsel"),
    manageOrdersUnderTakingPerson("manageOrdersUnderTakingPerson"),
    manageOrdersUnderTakingAddress("manageOrdersUnderTakingAddress"),
    manageOrdersUnderTakingTerms("manageOrdersUnderTakingTerms"),
    manageOrdersDateOfUnderTaking("manageOrdersDateOfUnderTaking"),
    underTakingExpiryDateTime("underTakingExpiryDateTime"),
    underTakingDateExpiry("underTakingDateExpiry"),
    underTakingExpiryTime("underTakingExpiryTime"),
    underTakingFormSign("underTakingFormSign"),

    manageOrdersFl402CourtName("manageOrdersFl402CourtName"),
    manageOrdersFl402CourtAddress("manageOrdersFl402CourtAddress"),
    manageOrdersFl402CaseNo("manageOrdersFl402CaseNo"),
    manageOrdersFl402Applicant("manageOrdersFl402Applicant"),
    manageOrdersFl402ApplicantRef("manageOrdersFl402ApplicantRef"),
    manageOrdersDateOfhearing("manageOrdersDateOfhearing"),
    dateOfHearingTime("dateOfHearingTime"),
    dateOfHearingTimeEstimate("dateOfHearingTimeEstimate"),
    fl402HearingCourtname("fl402HearingCourtname"),
    fl402HearingCourtAddress("fl402HearingCourtAddress"),

    parentalResponsibility("parentalResponsibility"),
    parentName("parentName"),

    caseTransferOptions("caseTransferOptions"),
    reasonsForTransfer("reasonsForTransfer"),
    giveDetails("giveDetails"),

    guardianTextBox("guardianTextBox"),

    fl404CustomFields("fl404CustomFields"),

    fl404bCustomFields("fl404bCustomFields"),

    caffcassOfficeName("caffcassOfficeName"),
    cafcassOfficeDetails("cafcassOfficeDetails"),

    sdoPreamblesList("sdoPreamblesList"),
    sdoHearingsAndNextStepsList("sdoHearingsAndNextStepsList"),
    sdoCafcassOrCymruList("sdoCafcassOrCymruList"),
    sdoLocalAuthorityList("sdoLocalAuthorityList"),
    sdoCourtList("sdoCourtList"),
    sdoDocumentationAndEvidenceList("sdoDocumentationAndEvidenceList"),
    sdoFurtherList("sdoFurtherList"),
    sdoOtherList("sdoOtherList"),
    sdoRightToAskCourt("sdoRightToAskCourt"),
    sdoPartiesRaisedAbuseCollection("sdoPartiesRaisedAbuseCollection"),
    sdoNextStepsAfterSecondGK("sdoNextStepsAfterSecondGK"),
    sdoNextStepsAllocationTo("sdoNextStepsAllocationTo"),
    sdoHearingUrgentCheckList("sdoHearingUrgentCheckList"),
    sdoHearingUrgentAnotherReason("sdoHearingUrgentAnotherReason"),
    sdoHearingUrgentCourtConsider("sdoHearingUrgentCourtConsider"),
    sdoHearingUrgentTimeShortened("sdoHearingUrgentTimeShortened"),
    sdoHearingUrgentMustBeServedBy("sdoHearingUrgentMustBeServedBy"),
    sdoHearingNotNeeded("sdoHearingNotNeeded"),
    sdoParticipationDirections("sdoParticipationDirections"),
    sdoPositionStatementDeadlineDate("sdoPositionStatementDeadlineDate"),
    sdoPositionStatementWritten("sdoPositionStatementWritten"),
    sdoMiamAttendingPerson("sdoMiamAttendingPerson"),
    sdoJoiningInstructionsForRH("sdoJoiningInstructionsForRH"),
    sdoHearingAllegationsMadeBy("sdoHearingAllegationsMadeBy"),
    sdoHearingCourtHasRequested("sdoHearingCourtHasRequested"),
    sdoAllegationsDeadlineDate("sdoAllegationsDeadlineDate"),
    sdoWrittenResponseDeadlineDate("sdoWrittenResponseDeadlineDate"),
    sdoHearingReportsAlsoSentTo("sdoHearingReportsAlsoSentTo"),
    sdoHearingMaximumPages("sdoHearingMaximumPages"),
    sdoHearingHowManyWitnessEvidence("sdoHearingHowManyWitnessEvidence"),
    sdoDocsEvidenceWitnessEvidence("sdoDocsEvidenceWitnessEvidence"),
    sdoInterpreterDialectRequired("sdoInterpreterDialectRequired"),
    sdoUpdateContactDetails("sdoUpdateContactDetails"),
    sdoCafcassFileAndServe("sdoCafcassFileAndServe"),
    sdoCafcassNextStepEditContent("sdoCafcassNextStepEditContent"),
    sdoCafcassCymruFileAndServe("sdoCafcassCymruFileAndServe"),
    sdoCafcassCymruNextStepEditContent("sdoCafcassCymruNextStepEditContent"),
    sdoNewPartnersToCafcass("sdoNewPartnersToCafcass"),
    sdoNewPartnersToCafcassCymru("sdoNewPartnersToCafcassCymru"),
    sdoSection7EditContent("sdoSection7EditContent"),
    sdoSection7ImpactAnalysisOptions("sdoSection7ImpactAnalysisOptions"),
    sdoSection7FactsEditContent("sdoSection7FactsEditContent"),
    sdoSection7daOccuredEditContent("sdoSection7daOccuredEditContent"),
    sdoSection7ChildImpactAnalysis("sdoSection7ChildImpactAnalysis"),
    sdoNameOfCouncil("sdoNameOfCouncil"),
    sdoCafcassCymruReportSentByDate("sdoCafcassCymruReportSentByDate"),
    sdoLocalAuthorityName("sdoLocalAuthorityName"),
    sdoLocalAuthorityTextArea("sdoLocalAuthorityTextArea"),
    sdoLocalAuthorityReportSubmitByDate("sdoLocalAuthorityReportSubmitByDate"),
    sdoTransferApplicationCourtDynamicList("sdoTransferApplicationCourtDynamicList"),
    sdoTransferApplicationReason("sdoTransferApplicationReason"),
    sdoTransferApplicationSpecificReason("sdoTransferApplicationSpecificReason"),
    sdoCrossExaminationCourtHavingHeard("sdoCrossExaminationCourtHavingHeard"),
    sdoCrossExaminationEx740("sdoCrossExaminationEx740"),
    sdoCrossExaminationEx741("sdoCrossExaminationEx741"),
    sdoCrossExaminationQualifiedLegal("sdoCrossExaminationQualifiedLegal"),
    sdoWitnessStatementsDeadlineDate("sdoWitnessStatementsDeadlineDate"),
    sdoWitnessStatementsSentTo("sdoWitnessStatementsSentTo"),
    sdoWitnessStatementsCopiesSentTo("sdoWitnessStatementsCopiesSentTo"),
    sdoWitnessStatementsMaximumPages("sdoWitnessStatementsMaximumPages"),
    sdoSpecifiedDocuments("sdoSpecifiedDocuments"),
    sdoInstructionsFilingPartiesDynamicList("sdoInstructionsFilingPartiesDynamicList"),
    sdoSpipAttendance("sdoSpipAttendance"),
    sdoHospitalRecordsDeadlineDate("sdoHospitalRecordsDeadlineDate"),
    sdoMedicalDisclosureUploadedBy("sdoMedicalDisclosureUploadedBy"),
    sdoLetterFromGpDeadlineDate("sdoLetterFromGpDeadlineDate"),
    sdoLetterFromGpUploadedBy("sdoLetterFromGpUploadedBy"),
    sdoLetterFromSchoolDeadlineDate("sdoLetterFromSchoolDeadlineDate"),
    sdoLetterFromSchoolUploadedBy("sdoLetterFromSchoolUploadedBy"),
    sdoScheduleOfAllegationsOption("sdoScheduleOfAllegationsOption"),
    sdoDisclosureOfPapersCaseNumbers("sdoDisclosureOfPapersCaseNumbers"),
    sdoParentWithCare("sdoParentWithCare"),
    sdoPermissionHearingDirections("sdoPermissionHearingDirections"),
    sdoPermissionHearingDetails("sdoPermissionHearingDetails"),
    sdoSecondHearingDetails("sdoSecondHearingDetails"),
    sdoNextStepJudgeName("sdoNextStepJudgeName"),
    sdoAllocateOrReserveJudge("sdoAllocateOrReserveJudge"),
    sdoAllocateOrReserveJudgeName("sdoAllocateOrReserveJudgeName"),
    sdoUrgentHearingDetails("sdoUrgentHearingDetails"),
    sdoFhdraHearingDetails("sdoFhdraHearingDetails"),
    sdoPositionStatementOtherCheckDetails("sdoPositionStatementOtherCheckDetails"),
    sdoPositionStatementOtherDetails("sdoPositionStatementOtherDetails"),
    sdoMiamOtherCheckDetails("sdoMiamOtherCheckDetails"),
    sdoMiamOtherDetails("sdoMiamOtherDetails"),
    sdoDraHearingDetails("sdoDraHearingDetails"),
    sdoSettlementHearingDetails("sdoSettlementHearingDetails"),
    sdoFactFindingOtherCheck("sdoFactFindingOtherCheck"),
    sdoFactFindingOtherDetails("sdoFactFindingOtherDetails"),
    sdoInterpreterOtherDetailsCheck("sdoInterpreterOtherDetailsCheck"),
    sdoInterpreterOtherDetails("sdoInterpreterOtherDetails"),
    sdoCafcassFileAndServeCheck("sdoCafcassFileAndServeCheck"),
    sdoCafcassFileAndServeDetails("sdoCafcassFileAndServeDetails"),
    safeguardingCafcassCymruCheck("safeguardingCafcassCymruCheck"),
    safeguardingCafcassCymruDetails("safeguardingCafcassCymruDetails"),
    sdoPartyToProvideDetailsCheck("sdoPartyToProvideDetailsCheck"),
    sdoPartyToProvideDetails("sdoPartyToProvideDetails"),
    sdoNewPartnersToCafcassCheck("sdoNewPartnersToCafcassCheck"),
    sdoNewPartnersToCafcassDetails("sdoNewPartnersToCafcassDetails"),
    sdoSection7Check("sdoSection7Check"),
    sdoSection7CheckDetails("sdoSection7CheckDetails"),
    sdoLocalAuthorityCheck("sdoLocalAuthorityCheck"),
    sdoLocalAuthorityDetails("sdoLocalAuthorityDetails"),
    sdoTransferCourtDetailsCheck("sdoTransferCourtDetailsCheck"),
    sdoTransferCourtDetails("sdoTransferCourtDetails"),
    sdoCrossExaminationCourtCheck("sdoCrossExaminationCourtCheck"),
    sdoCrossExaminationCourtDetails("sdoCrossExaminationCourtDetails"),
    sdoWitnessStatementsCheck("sdoWitnessStatementsCheck"),
    sdoWitnessStatementsCheckDetails("sdoWitnessStatementsCheckDetails"),
    sdoInstructionsFilingCheck("sdoInstructionsFilingCheck"),
    sdoInstructionsFilingDetails("sdoInstructionsFilingDetails"),
    sdoMedicalDiscApplicantName("sdoMedicalDiscApplicantName"),
    sdoMedicalDiscRespondentName("sdoMedicalDiscRespondentName"),
    sdoMedicalDiscFilingCheck("sdoMedicalDiscFilingCheck"),
    sdoMedicalDiscFilingDetails("sdoMedicalDiscFilingDetails"),
    sdoGpApplicantName("sdoGpApplicantName"),
    sdoGpRespondentName("sdoGpRespondentName"),
    sdoLetterFromDiscGpCheck("sdoLetterFromDiscGpCheck"),
    sdoLetterFromGpDetails("sdoLetterFromGpDetails"),
    sdoLsApplicantName("sdoLsApplicantName"),
    sdoLsRespondentName("sdoLsRespondentName"),
    sdoLetterFromSchoolCheck("sdoLetterFromSchoolCheck"),
    sdoLetterFromSchoolDetails("sdoLetterFromSchoolDetails"),
    sdoScheduleOfAllegationsDetails("sdoScheduleOfAllegationsDetails"),
    sdoDisClosureProceedingCheck("sdoDisClosureProceedingCheck"),
    sdoDisClosureProceedingDetails("sdoDisClosureProceedingDetails"),
    sdoFurtherDirectionDetails("sdoFurtherDirectionDetails"),
    sdoCrossExaminationEditContent("sdoCrossExaminationEditContent"),
    sdoNamedJudgeFullName("sdoNamedJudgeFullName"),
    sdoAfterSecondGatekeeping("sdoAfterSecondGatekeeping"),
    sdoAddNewPreambleCollection("sdoAddNewPreambleCollection"),
    sdoNextStepsAfterGatekeeping("sdoNextStepsAfterGatekeeping"),
    sdoNewPartnerPartiesCafcass("sdoNewPartnerPartiesCafcass"),
    sdoNewPartnerPartiesCafcassCymru("sdoNewPartnerPartiesCafcassCymru"),
    sdoNewPartnerPartiesCafcassText("sdoNewPartnerPartiesCafcassText"),
    sdoNewPartnerPartiesCafcassCymruText("sdoNewPartnerPartiesCafcassText"),
    sdoAllocateDecisionJudgeFullName("sdoAllocateDecisionJudgeFullName"),

    dioPreamblesList("dioPreamblesList"),
    dioHearingsAndNextStepsList("dioHearingsAndNextStepsList"),
    dioCafcassOrCymruList("dioCafcassOrCymruList"),
    dioLocalAuthorityList("dioLocalAuthorityList"),
    dioCourtList("dioCourtList"),
    dioOtherList("dioOtherList"),
    dioFurtherList("dioFurtherList"),

    manageOrdersAmendedOrder("manageOrdersAmendedOrder"),
    amendOrderSelectJudgeOrLa("amendOrderSelectJudgeOrLa"),
    nameOfJudgeAmendOrder("nameOfJudgeAmendOrder"),
    nameOfLaAmendOrder("nameOfLaAmendOrder"),
    nameOfJudgeToReviewOrder("nameOfJudgeToReviewOrder"),
    nameOfLaToReviewOrder("nameOfLaToReviewOrder"),
    manageOrdersDocumentToAmend("manageOrdersDocumentToAmend");

    private final String value;

    ManageOrderFieldsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
