package uk.gov.hmcts.reform.prl.mapper.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.bundle.BundlingDocGroupEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleHearingInfo;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingCaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANTS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_STATMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRUG_AND_ALCOHOL_TESTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRUG_AND_ALCOHOL_TESTS_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LETTERS_FROM_SCHOOL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MAIL_SCREENSHOTS_MEDIA_FILES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_RECORDS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_RECORDS_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MEDICAL_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_WITNESS_STATEMENTS_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PATERNITY_TEST_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POLICE_REPORTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POLICE_REPORT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENTS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts.restrictToGroup;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BundleCreateRequestMapper {
    public BundleCreateRequest mapCaseDataToBundleCreateRequest(CaseData caseData, String eventId, Hearings hearingDetails,
                                                                String bundleConfigFileName) {
        BundleCreateRequest bundleCreateRequest = BundleCreateRequest.builder()
            .caseDetails(BundlingCaseDetails.builder()
                .id(caseData.getApplicantName())
                .caseData(mapCaseData(caseData,hearingDetails,
                    bundleConfigFileName))
                .build())
            .caseTypeId(CASE_TYPE).jurisdictionId(JURISDICTION).eventId(eventId).build();
        log.info("*** create bundle request mapped for the case id  : {}", caseData.getId());
        return bundleCreateRequest;
    }

    private BundlingCaseData mapCaseData(CaseData caseData, Hearings hearingDetails, String bundleConfigFileName) {
        return BundlingCaseData.builder().id(String.valueOf(caseData.getId())).bundleConfiguration(
                bundleConfigFileName)
            .data(BundlingData.builder().caseNumber(String.valueOf(caseData.getId())).applicantCaseName(caseData.getApplicantCaseName())
                .hearingDetails(mapHearingDetails(hearingDetails))
                .applications(mapApplicationsFromCaseData(caseData))
                .orders(mapOrdersFromCaseData(caseData.getOrderCollection()))
                .allOtherDocuments(mapAllOtherDocuments(caseData)).build()).build();
    }

    private BundleHearingInfo mapHearingDetails(Hearings hearingDetails) {
        if (null != hearingDetails && null != hearingDetails.getCaseHearings()) {
            List<CaseHearing> listedCaseHearings = hearingDetails.getCaseHearings().stream()
                .filter(caseHearing -> LISTED.equalsIgnoreCase(caseHearing.getHmcStatus())).collect(Collectors.toList());
            if (null != listedCaseHearings && !listedCaseHearings.isEmpty()) {
                List<HearingDaySchedule> hearingDaySchedules = listedCaseHearings.get(0).getHearingDaySchedule();
                if (null != hearingDaySchedules && !hearingDaySchedules.isEmpty()) {
                    return BundleHearingInfo.builder().hearingVenueAddress(getHearingVenueAddress(hearingDaySchedules.get(0)))
                        .hearingDateAndTime(null != hearingDaySchedules.get(0).getHearingStartDateTime()
                            ? hearingDaySchedules.get(0).getHearingStartDateTime().toString() : BLANK_STRING)
                        .hearingJudgeName(hearingDaySchedules.get(0).getHearingJudgeName()).build();
                }
            }
        }
        return BundleHearingInfo.builder().build();
    }

    private String getHearingVenueAddress(HearingDaySchedule hearingDaySchedule) {
        return null != hearingDaySchedule.getHearingVenueName()
            ? hearingDaySchedule.getHearingVenueName() + "\n" +  hearingDaySchedule.getHearingVenueAddress()
            : hearingDaySchedule.getHearingVenueAddress();
    }

    private List<Element<BundlingRequestDocument>> mapAllOtherDocuments(CaseData caseData) {

        List<Element<BundlingRequestDocument>> allOtherDocuments = new ArrayList<>();

        List<Element<BundlingRequestDocument>> fl401SupportingDocs = mapFl401SupportingDocs(caseData.getFl401UploadSupportDocuments());
        if (!fl401SupportingDocs.isEmpty()) {
            allOtherDocuments.addAll(fl401SupportingDocs);
        }
        List<Element<BundlingRequestDocument>> fl401WitnessDocs = mapFl401WitnessDocs(caseData.getFl401UploadWitnessDocuments());
        if (!fl401WitnessDocs.isEmpty()) {
            allOtherDocuments.addAll(fl401WitnessDocs);
        }
        List<Element<BundlingRequestDocument>> citizenUploadedDocuments =
            mapBundlingDocsFromCitizenUploadedDocs(caseData.getCitizenUploadedDocumentList());
        if (null != citizenUploadedDocuments && !citizenUploadedDocuments.isEmpty()) {
            allOtherDocuments.addAll(citizenUploadedDocuments);
        }

        //SNI-4260 fix
        //Updated to retrieve otherDocuments according to the new manageDocuments event
        List<Element<BundlingRequestDocument>> otherDocuments = mapOtherDocumentsFromCaseData(caseData);
        if (null != otherDocuments && !otherDocuments.isEmpty()) {
            allOtherDocuments.addAll(otherDocuments);
            log.info("****** otherDocuments added" + otherDocuments);
        }
        log.info("****** allOtherDocuments" + allOtherDocuments);
        return allOtherDocuments;
    }


    private List<Element<BundlingRequestDocument>> mapFl401WitnessDocs(List<Element<Document>> fl401UploadWitnessDocuments) {
        List<Element<BundlingRequestDocument>> fl401WitnessDocs = new ArrayList<>();
        Optional<List<Element<Document>>> existingfl401WitnessDocs = ofNullable(fl401UploadWitnessDocuments);
        if (existingfl401WitnessDocs.isEmpty()) {
            return fl401WitnessDocs;
        }
        ElementUtils.unwrapElements(fl401UploadWitnessDocuments).forEach(witnessDocs ->
            fl401WitnessDocs.add(ElementUtils.element(mapBundlingRequestDocument(witnessDocs,
                BundlingDocGroupEnum.applicantWitnessStatements))));
        return fl401WitnessDocs;
    }

    private List<Element<BundlingRequestDocument>> mapFl401SupportingDocs(List<Element<Document>> fl401UploadSupportDocuments) {
        List<Element<BundlingRequestDocument>> fl401SupportingDocs = new ArrayList<>();
        Optional<List<Element<Document>>> existingfl401SupportingDocs = ofNullable(fl401UploadSupportDocuments);
        if (existingfl401SupportingDocs.isEmpty()) {
            return fl401SupportingDocs;
        }
        ElementUtils.unwrapElements(fl401UploadSupportDocuments).forEach(supportDocs ->
            fl401SupportingDocs.add(ElementUtils.element(mapBundlingRequestDocument(supportDocs,
                BundlingDocGroupEnum.applicantStatementSupportingEvidence))));
        return fl401SupportingDocs;
    }

    private List<Element<BundlingRequestDocument>> mapApplicationsFromCaseData(CaseData caseData) {
        List<BundlingRequestDocument> applications = new ArrayList<>();

        if (YesOrNo.Yes.equals(caseData.getLanguagePreferenceWelsh())) {
            if (null != caseData.getFinalWelshDocument()) {
                applications.add(mapBundlingRequestDocument(caseData.getFinalWelshDocument(), BundlingDocGroupEnum.applicantApplication));
            }
            if (null != caseData.getC1AWelshDocument()) {
                applications.add(mapBundlingRequestDocument(caseData.getC1AWelshDocument(), BundlingDocGroupEnum.applicantC1AApplication));
            }
        } else {
            if (null != caseData.getFinalDocument()) {
                applications.add(mapBundlingRequestDocument(caseData.getFinalDocument(), BundlingDocGroupEnum.applicantApplication));
            }
            if (null != caseData.getC1ADocument()) {
                applications.add(mapBundlingRequestDocument(caseData.getC1ADocument(), BundlingDocGroupEnum.applicantC1AApplication));
            }
        }
        List<BundlingRequestDocument> miamCertAndPreviousOrdersUploadedByCourtAdmin =
            mapApplicationsFromFurtherEvidences(caseData.getFurtherEvidences());
        if (!miamCertAndPreviousOrdersUploadedByCourtAdmin.isEmpty()) {
            applications.addAll(miamCertAndPreviousOrdersUploadedByCourtAdmin);
        }
        mapMiamDetails(caseData.getMiamDetails(),applications);
        List<BundlingRequestDocument> citizenUploadedC7Documents = mapC7DocumentsFromCaseData(caseData.getCitizenResponseC7DocumentList());
        if (!citizenUploadedC7Documents.isEmpty()) {
            applications.addAll(citizenUploadedC7Documents);
        }
        return ElementUtils.wrapElements(applications);
    }

    private void mapMiamDetails(MiamDetails miamDetails, List<BundlingRequestDocument> applications) {
        if (null != miamDetails) {
            Document miamCertificateUpload = miamDetails.getMiamCertificationDocumentUpload();
            if (null != miamCertificateUpload) {
                applications.add(mapBundlingRequestDocument(miamCertificateUpload, BundlingDocGroupEnum.applicantMiamCertificate));
            }
            Document miamCertificateUpload1 = miamDetails.getMiamCertificationDocumentUpload1();
            if (null != miamCertificateUpload1) {
                applications.add(mapBundlingRequestDocument(miamCertificateUpload1, BundlingDocGroupEnum.applicantMiamCertificate));
            }
        }
    }

    private List<BundlingRequestDocument> mapC7DocumentsFromCaseData(List<Element<ResponseDocuments>> citizenResponseC7DocumentList) {
        List<BundlingRequestDocument> applications = new ArrayList<>();
        Optional<List<Element<ResponseDocuments>>> uploadedC7CitizenDocs = ofNullable(citizenResponseC7DocumentList);
        if (uploadedC7CitizenDocs.isEmpty()) {
            return applications;
        }
        ElementUtils.unwrapElements(citizenResponseC7DocumentList).forEach(c7CitizenResponseDocument ->
            applications.add(mapBundlingRequestDocument(c7CitizenResponseDocument.getCitizenDocument(),
                BundlingDocGroupEnum.c7Documents)));
        return applications;
    }

    private BundlingRequestDocument mapBundlingRequestDocument(Document document, BundlingDocGroupEnum applicationsDocGroup) {
        return (null != document) ? BundlingRequestDocument.builder().documentLink(document).documentFileName(document.getDocumentFileName())
            .documentGroup(applicationsDocGroup).build() : BundlingRequestDocument.builder().build();
    }

    private BundlingRequestDocument mapBundlingRequestDocumentForOtherDocs(Document document, BundlingDocGroupEnum applicationsDocGroup) {
        return (null != document && ("notRequiredGroup").equalsIgnoreCase(applicationsDocGroup.getDisplayedValue()))
            ? BundlingRequestDocument.builder().documentLink(document).documentFileName(document.getDocumentFileName())
            .documentGroup(applicationsDocGroup).build() : BundlingRequestDocument.builder().build();
    }


    private List<BundlingRequestDocument> mapApplicationsFromFurtherEvidences(List<Element<FurtherEvidence>> furtherEvidencesFromCaseData) {
        List<BundlingRequestDocument> applications = new ArrayList<>();
        Optional<List<Element<FurtherEvidence>>> existingFurtherEvidences = ofNullable(furtherEvidencesFromCaseData);
        if (existingFurtherEvidences.isEmpty()) {
            return applications;
        }
        ElementUtils.unwrapElements(furtherEvidencesFromCaseData).forEach(furtherEvidence -> {
            if (!furtherEvidence.getRestrictCheckboxFurtherEvidence().contains(restrictToGroup)) {
                if (FurtherEvidenceDocumentType.miamCertificate.equals(furtherEvidence.getTypeOfDocumentFurtherEvidence())) {
                    applications.add(mapBundlingRequestDocument(furtherEvidence.getDocumentFurtherEvidence(),
                        BundlingDocGroupEnum.applicantMiamCertificate));
                } else if (FurtherEvidenceDocumentType.previousOrders.equals(furtherEvidence.getTypeOfDocumentFurtherEvidence())) {
                    applications.add(mapBundlingRequestDocument(furtherEvidence.getDocumentFurtherEvidence(),
                        BundlingDocGroupEnum.applicantPreviousOrdersSubmittedWithApplication));
                }
            }
        });
        return applications;
    }

    private List<Element<BundlingRequestDocument>> mapOrdersFromCaseData(List<Element<OrderDetails>> ordersFromCaseData) {
        List<BundlingRequestDocument> orders = new ArrayList<>();
        Optional<List<Element<OrderDetails>>> existingOrders = ofNullable(ordersFromCaseData);
        if (existingOrders.isEmpty()) {
            return new ArrayList<>();
        }
        ordersFromCaseData.forEach(orderDetailsElement -> {
            OrderDetails orderDetails = orderDetailsElement.getValue();
            Document document = orderDetails.getOrderDocument();
            orders.add(BundlingRequestDocument.builder().documentGroup(BundlingDocGroupEnum.ordersSubmittedWithApplication)
                .documentFileName(document.getDocumentFileName()).documentLink(document).build());
        });
        return ElementUtils.wrapElements(orders);
    }

    //SNI-4260 fix
    //Updated to retrieve otherDocuments according to the new manageDocuments event
    private List<Element<BundlingRequestDocument>> mapOtherDocumentsFromCaseData(
        CaseData caseData) {
        List<Element<QuarantineLegalDoc>>  allDocuments = new ArrayList<>();
        if (null != caseData.getCourtStaffQuarantineDocsList()) {
            List<Element<QuarantineLegalDoc>> courtStaffUploadDocList = caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab();
            allDocuments.addAll(courtStaffUploadDocList);
        }
        if (null != caseData.getCafcassQuarantineDocsList()) {
            List<Element<QuarantineLegalDoc>> cafcassUploadDocList = caseData.getReviewDocuments().getCafcassUploadDocListDocTab();
            allDocuments.addAll(cafcassUploadDocList);
        }
        if (null != caseData.getLegalProfQuarantineDocsList()) {
            List<Element<QuarantineLegalDoc>> legalProfUploadDocList = caseData.getReviewDocuments().getLegalProfUploadDocListDocTab();
            allDocuments.addAll(legalProfUploadDocList);
        }
        log.info("****** allDocuments" + allDocuments);
        List<BundlingRequestDocument> otherBundlingDocuments = new ArrayList<>();
        ElementUtils.unwrapElements(allDocuments)
            .forEach(docs ->
                         otherBundlingDocuments.add(
                             mapBundlingRequestDocumentForOtherDocs(docs.getDocument(),
                                                        getDocumentGroup(docs.getDocumentParty()
                                                                             .equalsIgnoreCase("Applicant") ? "Yes" : "No",
                                                                         docs.getCategoryName()))));
        log.info("****** otherBundlingDocuments" + otherBundlingDocuments);
        return ElementUtils.wrapElements(otherBundlingDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapBundlingDocsFromCitizenUploadedDocs(List<Element<UploadedDocuments>>
                                                                                              citizenUploadedDocumentList) {
        List<BundlingRequestDocument> bundlingCitizenDocuments = new ArrayList<>();
        Optional<List<Element<UploadedDocuments>>> citizenUploadedDocuments = ofNullable(citizenUploadedDocumentList);
        if (citizenUploadedDocuments.isEmpty()) {
            return new ArrayList<>();
        }
        citizenUploadedDocumentList.forEach(citizenUploadedDocumentElement -> {
            UploadedDocuments uploadedDocuments = citizenUploadedDocumentElement.getValue();
            Document uploadedDocument = uploadedDocuments.getCitizenDocument();
            bundlingCitizenDocuments.add(BundlingRequestDocument.builder()
                .documentGroup(getDocumentGroupForCitizen(uploadedDocuments.getIsApplicant(), uploadedDocuments.getDocumentType()))
                .documentFileName(uploadedDocument.getDocumentFileName())
                .documentLink(uploadedDocument).build());

        });
        return ElementUtils.wrapElements(bundlingCitizenDocuments);
    }

    private BundlingDocGroupEnum getDocumentGroupForCitizen(String isApplicant, String docType) {
        BundlingDocGroupEnum bundlingDocGroupEnum = BundlingDocGroupEnum.notRequiredGroup;
        switch (docType) {
            case YOUR_POSITION_STATEMENTS:
                bundlingDocGroupEnum = PrlAppsConstants.NO.equals(isApplicant) ? BundlingDocGroupEnum.respondentPositionStatements :
                    BundlingDocGroupEnum.applicantPositionStatements;
                break;
            case YOUR_WITNESS_STATEMENTS:
                bundlingDocGroupEnum = PrlAppsConstants.NO.equals(isApplicant) ? BundlingDocGroupEnum.respondentWitnessStatements :
                    BundlingDocGroupEnum.applicantWitnessStatements;
                break;
            case LETTERS_FROM_SCHOOL:
                bundlingDocGroupEnum = PrlAppsConstants.NO.equals(isApplicant) ? BundlingDocGroupEnum.respondentLettersFromSchool :
                    BundlingDocGroupEnum.applicantLettersFromSchool;
                break;
            case OTHER_WITNESS_STATEMENTS:
                bundlingDocGroupEnum =  BundlingDocGroupEnum.otherWitnessStatements;
                break;
            case MAIL_SCREENSHOTS_MEDIA_FILES:
                bundlingDocGroupEnum =
                    PrlAppsConstants.NO.equals(isApplicant) ? BundlingDocGroupEnum.respondentEmailsOrScreenshotsOrImagesOrOtherMediaFiles :
                        BundlingDocGroupEnum.applicantEmailsOrScreenshotsOrImagesOrOtherMediaFiles;
                break;
            case MEDICAL_REPORTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.expertMedicalReports;
                break;
            case MEDICAL_RECORDS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.expertMedicalRecords;
                break;
            case PATERNITY_TEST_REPORTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.expertDNAReports;
                break;
            case DRUG_AND_ALCOHOL_TESTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.expertReportsForDrugAndAlcholTest;
                break;
            case POLICE_REPORTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.policeReports;
                break;
            case CAFCASS_REPORTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.cafcassReportsUploadedByCourtAdmin;
                break;
            case EXPERT_REPORTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.expertReportsUploadedByCourtAdmin;
                break;
            case APPLICANT_STATMENT:
                bundlingDocGroupEnum = BundlingDocGroupEnum.applicantStatementDocsUploadedByCourtAdmin;
                break;
            default:
                break;
        }
        return bundlingDocGroupEnum;
    }

    private BundlingDocGroupEnum getDocumentGroup(String isApplicant, String docType) {
        BundlingDocGroupEnum bundlingDocGroupEnum = BundlingDocGroupEnum.notRequiredGroup;
        log.info("****** In BundleCreateRequestMapper method getDocumentGroup");
        log.info("******" + isApplicant);
        log.info("******" + docType);
        switch (docType) {
            case POSITION_STATEMENTS:
                bundlingDocGroupEnum = PrlAppsConstants.NO.equals(isApplicant) ? BundlingDocGroupEnum.respondentPositionStatements :
                    BundlingDocGroupEnum.applicantPositionStatements;
                break;
            case YOUR_WITNESS_STATEMENTS:
                bundlingDocGroupEnum = PrlAppsConstants.NO.equals(isApplicant) ? BundlingDocGroupEnum.respondentWitnessStatements :
                    BundlingDocGroupEnum.applicantWitnessStatements;
                break;
            case LETTERS_FROM_SCHOOL:
                bundlingDocGroupEnum = PrlAppsConstants.NO.equals(isApplicant) ? BundlingDocGroupEnum.respondentLettersFromSchool :
                    BundlingDocGroupEnum.applicantLettersFromSchool;
                break;
            case OTHER_WITNESS_STATEMENTS_DOCUMENT:
                bundlingDocGroupEnum =  BundlingDocGroupEnum.otherWitnessStatements;
                break;
            case MAIL_SCREENSHOTS_MEDIA_FILES:
                bundlingDocGroupEnum =
                    PrlAppsConstants.NO.equals(isApplicant) ? BundlingDocGroupEnum.respondentEmailsOrScreenshotsOrImagesOrOtherMediaFiles :
                        BundlingDocGroupEnum.applicantEmailsOrScreenshotsOrImagesOrOtherMediaFiles;
                break;
            case MEDICAL_REPORTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.expertMedicalReports;
                break;
            case MEDICAL_RECORDS_DOCUMENT:
                bundlingDocGroupEnum = BundlingDocGroupEnum.expertMedicalRecords;
                break;
            case PATERNITY_TEST_REPORTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.expertDNAReports;
                break;
            case DRUG_AND_ALCOHOL_TESTS_DOCUMENT:
                bundlingDocGroupEnum = BundlingDocGroupEnum.expertReportsForDrugAndAlcholTest;
                break;
            case POLICE_REPORT_DOCUMENT:
                bundlingDocGroupEnum = BundlingDocGroupEnum.policeReports;
                break;
            case CAFCASS_REPORTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.cafcassReportsUploadedByCourtAdmin;
                break;
            case EXPERT_REPORTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.expertReportsUploadedByCourtAdmin;
                break;
            case APPLICANTS_STATEMENTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.applicantStatementDocsUploadedByCourtAdmin;
                break;
            case RESPONDENTS_STATEMENTS:
                bundlingDocGroupEnum = BundlingDocGroupEnum.respondentPositionStatements;
                break;
            default:
                break;
        }
        return bundlingDocGroupEnum;
    }
}
