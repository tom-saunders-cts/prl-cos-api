package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings.OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings.Proceedings;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CitizenResponseDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C7_FINAL_ENGLISH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_C7_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REVIEW_AND_SUBMIT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseApplicationResponseService {

    private final DocumentGenService documentGenService;
    private final C100RespondentSolicitorService c100RespondentSolicitorService;
    private final CaseService caseService;
    private final IdamClient idamClient;

    public CaseData updateCurrentRespondent(CaseData caseData, YesOrNo currentRespondent, String partyId) {

        for (Element<PartyDetails> partyElement : caseData.getRespondents()) {
            if (partyElement.getId().toString().equalsIgnoreCase(partyId)) {
                PartyDetails respondent = partyElement.getValue();
                respondent.setCurrentRespondent(currentRespondent);
            }
        }
        return caseData;
    }

    public CaseDetails generateCitizenResponseFinalDocuments(CaseData caseData, CaseDetails caseDetails, String authorisation, String partyId,
                                                             String caseId, String s2sToken) throws Exception {

        List<Element<Document>> responseDocs = new ArrayList<>();
        UserDetails userDetails = idamClient.getUserDetails(authorisation);

        log.info(" Generating C7 Final document for respondent ");
        Document document = generateFinalC7(caseData, authorisation);
        responseDocs.add(element(document));
        log.info("C7 Final document generated successfully for respondent ");

        Optional<Element<PartyDetails>> currentRespondent
            = caseData.getRespondents()
            .stream()
            .filter(
                respondent -> YesOrNo.Yes.equals(
                    respondent.getValue().getCurrentRespondent()))
            .findFirst();
        if (currentRespondent.isPresent()) {
            CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
            Map<String, Object> dataMap = c100RespondentSolicitorService.populateDataMap(
                callbackRequest,
                currentRespondent.get()
            );

            if (isNotEmpty(currentRespondent.get().getValue().getResponse())) {

                responseDocs = checkPreviousProceedings(responseDocs, currentRespondent);

                if (isNotEmpty(currentRespondent.get().getValue().getResponse().getSafetyConcerns())
                    && Yes.equals(currentRespondent.get().getValue().getResponse().getSafetyConcerns().getHaveSafetyConcerns())) {
                    log.info(" Generating C1A Final document for respondent ");
                    Document c1aFinalDocument = generateFinalC1A(caseData, authorisation, dataMap);
                    responseDocs.add(element(c1aFinalDocument));
                    log.info("C1A Final document generated successfully for respondent ");
                }
            }

            String partyName = caseData.getRespondents()
                .stream()
                .filter(element -> element.getId()
                    .toString()
                    .equalsIgnoreCase(partyId))
                .map(Element::getValue)
                .findFirst()
                .map(PartyDetails::getLabelForDynamicList)
                .orElse("");

            caseData = generateC8Document(
                authorisation,
                caseData,
                currentRespondent,
                dataMap,
                partyName,
                userDetails
            );
        }

        log.info("responseDocs are {}", responseDocs);

        caseData = addCitizenDocumentsToTheQuarantineList(caseData, responseDocs, userDetails);

        CaseDetails caseDetailsReturn;
        caseDetailsReturn = caseService.updateCase(
            caseData,
            authorisation,
            s2sToken,
            caseId,
            REVIEW_AND_SUBMIT,
            null
        );
        return caseDetailsReturn;
    }

    private List<Element<Document>> checkPreviousProceedings(List<Element<Document>> responseDocs,
                                                             Optional<Element<PartyDetails>> currentRespondent) {
        if (currentRespondent.isPresent()
            && isNotEmpty(currentRespondent.get().getValue().getResponse().getCurrentOrPreviousProceedings())
            && isNotEmpty(currentRespondent.get().getValue().getResponse().getCurrentOrPreviousProceedings().getProceedingsList())) {

            List<Proceedings> proceedingsList = new ArrayList<>();
            proceedingsList.add(currentRespondent.get().getValue().getResponse().getCurrentOrPreviousProceedings()
                .getProceedingsList().stream().iterator().next().getValue());

            List<Element<OtherProceedingDetails>> proceedingsDetailsElementList = new ArrayList<>();
            for (Proceedings proceedings : proceedingsList) {
                if (null != proceedings.getProceedingDetails()) {
                    proceedingsDetailsElementList = proceedings.getProceedingDetails();
                }
            }

            if (isNotEmpty(proceedingsDetailsElementList)) {
                for (Element<OtherProceedingDetails> proceedingsDetailsList : proceedingsDetailsElementList) {
                    if (isNotEmpty(proceedingsDetailsList.getValue())
                        && null != proceedingsDetailsList.getValue().getOrderDocument()) {
                        responseDocs.add(element(proceedingsDetailsList.getValue().getOrderDocument()));
                    }
                }
            }
        }

        return responseDocs;
    }

    private CaseData addCitizenDocumentsToTheQuarantineList(CaseData caseData, List<Element<Document>> responseDocs,
                                                           UserDetails userDetails) {

        List<Element<QuarantineLegalDoc>> quarantineDocs = new ArrayList<>();
        if (null != caseData.getDocumentManagementDetails() && null != caseData
            .getDocumentManagementDetails().getCitizenQuarantineDocsList()) {
            quarantineDocs = caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList();
        }

        quarantineDocs.addAll(responseDocs.stream().map(element -> Element.<QuarantineLegalDoc>builder()
                .value(QuarantineLegalDoc
                    .builder()
                    .citizenQuarantineDocument(element.getValue()
                        .toBuilder()
                        .documentCreatedOn(Date.from(ZonedDateTime.now(ZoneId.of(LONDON_TIME_ZONE))
                            .toInstant()))
                        .build())
                    .categoryId("")
                    .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
                    .uploadedBy(null != userDetails ? userDetails.getFullName() : null)
                    .uploadedByIdamId(null != userDetails ? userDetails.getId() : null)
                    .uploaderRole(CITIZEN)
                    .build())
                .id(element.getId()).build())
            .toList());

        log.info("quarantineDocs is {}", quarantineDocs);
        if (null != caseData.getDocumentManagementDetails()) {
            caseData.getDocumentManagementDetails().setCitizenQuarantineDocsList(quarantineDocs);
        } else {
            caseData.setDocumentManagementDetails(DocumentManagementDetails
                .builder()
                .citizenQuarantineDocsList(quarantineDocs)
                .build());
        }
        log.info("quarantineDocs is {}", caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList());
        return caseData;
    }

    private Document generateFinalC1A(CaseData caseData, String authorisation, Map<String, Object> dataMap) throws Exception {

        return documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            SOLICITOR_C1A_FINAL_DOCUMENT,
            false,
            dataMap
        );
    }

    private Document generateFinalC7(CaseData caseData, String authorisation) throws Exception {

        return documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            C7_FINAL_ENGLISH,
            false
        );
    }

    private CaseData generateC8Document(String authorisation, CaseData caseData, Optional<Element<PartyDetails>> currentRespondent,
                                                    Map<String, Object> dataMap, String partyName,
                                                    UserDetails userDetails) throws Exception {

        Document c8FinalDocument = null;

        if (currentRespondent.isPresent()) {
            if (dataMap.containsKey("isConfidentialDataPresent")) {
                log.info(" Generating C8 Final document for respondent ");
                c8FinalDocument = documentGenService.generateSingleDocument(
                    authorisation,
                    caseData,
                    C8_RESP_FINAL_HINT,
                    false,
                    dataMap
                );
                log.info("C8 Final document generated successfully for respondent ");
            }

            populateC8Documents(caseData, currentRespondent.get(), partyName, userDetails, c8FinalDocument);
        }
        return caseData;
    }

    private static void populateC8Documents(CaseData caseData, Element<PartyDetails> currentRespondent, String partyName,
                                            UserDetails userDetails, Document c8FinalDocument) {

        int partyIndex = caseData.getRespondents().indexOf(currentRespondent);

        if (null != c8FinalDocument && partyIndex >= 0) {
            ResponseDocuments c8ResponseDocuments = ResponseDocuments.builder()
                .partyName(partyName)
                .createdBy(userDetails.getFullName())
                .dateCreated(LocalDate.now())
                .citizenDocument(c8FinalDocument)
                .build();
            if (caseData.getCitizenResponseDocuments() == null) {
                caseData.setCitizenResponseDocuments(CitizenResponseDocuments.builder().build());
            }

            switch (partyIndex) {
                case 0 -> caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                    .respondentAc8(c8ResponseDocuments).build());
                case 1 -> caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                    .respondentBc8(c8ResponseDocuments).build());
                case 2 -> caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                    .respondentCc8(c8ResponseDocuments).build());
                case 3 -> caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                    .respondentDc8(c8ResponseDocuments).build());
                case 4 -> caseData.toBuilder().citizenResponseDocuments(CitizenResponseDocuments.builder()
                    .respondentEc8(c8ResponseDocuments).build());
                default -> caseData.toBuilder().build();
            }
        }
    }

    public Document generateC7DraftDocument(String authorisation, CaseData caseData) throws Exception {

        log.info(" Generating C7 draft document for respondent ");
        Document document = documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            DOCUMENT_C7_DRAFT_HINT,
            false
        );
        log.info("C7 draft document generated successfully for respondent ");

        return document;
    }
}
