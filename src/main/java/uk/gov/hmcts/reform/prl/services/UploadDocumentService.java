package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.citizen.UploadedDocumentRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.google.common.collect.Lists.newArrayList;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadDocumentService {

    private final AuthTokenGenerator authTokenGenerator;
    private final CaseDocumentClient caseDocumentClient;
    private final CoreCaseDataApi coreCaseDataApi;
    private final ObjectMapper objectMapper;

    public Document uploadDocument(byte[] pdf, String fileName, String contentType, String authorisation) {
        MultipartFile file = new InMemoryMultipartFile("files", fileName, contentType, pdf);


        UploadResponse response = caseDocumentClient.uploadDocuments(authorisation, authTokenGenerator.generate(),
                                                                     CASE_TYPE, JURISDICTION, newArrayList(file)
        );

        Document document = response.getDocuments().stream()
            .findFirst()
            .orElseThrow(() ->
                             new RuntimeException("Document upload failed due to empty result"));

        log.debug("Document upload resulted with links: {}, {}", document.links.self.href, document.links.binary.href);

        return document;
    }

    public Document uploadDocument(MultipartFile file, String fileName, String contentType, String authorisation) {

        UploadResponse response = caseDocumentClient.uploadDocuments(
            authorisation,
            authTokenGenerator.generate(),
            CASE_TYPE,
            JURISDICTION,
            newArrayList(file)
        );

        Document document = response.getDocuments().stream()
            .findFirst()
            .orElseThrow(() ->
                             new RuntimeException("Document upload failed due to empty result"));

        log.debug("Document upload resulted with links: {}, {}", document.links.self.href, document.links.binary.href);

        return document;
    }

    public UploadedDocuments uploadCitizenDocument(String authorisation, UploadedDocumentRequest uploadedDocumentRequest, String caseId) {

        String parentDocumentType = "";
        String documentType = "";
        String partyName = "";
        String documentName = "";
        String partyId = "";
        LocalDate today = LocalDate.now();
        String formattedDateCreated = today.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
        String isApplicant = "";
        YesOrNo documentRequest = null;

        log.info("=========Trying to retrieve the upload doc request data=======");
        if (uploadedDocumentRequest != null) {
            log.info("=====trying to retrive doc data from request=====");

            if (null != uploadedDocumentRequest.getParentDocumentType()) {
                log.info("Parent doc type from upload doc req: {}", uploadedDocumentRequest.getParentDocumentType());
                parentDocumentType = uploadedDocumentRequest.getParentDocumentType();
            }
            if (null != uploadedDocumentRequest.getPartyId()) {
                log.info("Party Id from upload doc req: {}", uploadedDocumentRequest.getPartyId());
                partyId = uploadedDocumentRequest.getPartyId();
            }
            if (null != uploadedDocumentRequest.getDocumentType()) {
                log.info("document type from upload doc req: {}", uploadedDocumentRequest.getDocumentType());

                documentType = uploadedDocumentRequest.getDocumentType();
                if (null != uploadedDocumentRequest.getPartyName()) {
                    log.info("Party name from upload doc req: {}", uploadedDocumentRequest.getPartyName());

                    partyName = uploadedDocumentRequest.getPartyName();
                    documentName = documentType.replace("Your", partyName + "'s");
                }
            }
            if (null != uploadedDocumentRequest.getIsApplicant()) {
                isApplicant = uploadedDocumentRequest.getIsApplicant();
            }
            if (null != uploadedDocumentRequest.getDocumentRequestedByCourt()) {
                documentRequest = uploadedDocumentRequest.getDocumentRequestedByCourt();
            }

        }

        if (!uploadedDocumentRequest.getFiles().isEmpty()) {

            UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
                authorisation,
                authTokenGenerator.generate(),
                CASE_TYPE,
                JURISDICTION,
                uploadedDocumentRequest.getFiles()
            );
            log.info("Document Response: {} ", uploadResponse.getDocuments().get(0));
            log.info("Document uploaded successfully through caseDocumentClient");

            UploadedDocuments uploadedDocuments = null;

            for (MultipartFile file: uploadedDocumentRequest.getFiles()) {

                uploadedDocuments = UploadedDocuments.builder().dateCreated(LocalDate.now())
                    .uploadedBy(partyId)
                    .documentDetails(DocumentDetails.builder().documentName(file.getOriginalFilename())
                                         .documentUploadedDate(formattedDateCreated).build())
                    .partyName(partyName)
                    .isApplicant(isApplicant)
                    .parentDocumentType(parentDocumentType)
                    .documentType(documentType)
                    .dateCreated(LocalDate.now())
                    .documentRequestedByCourt(documentRequest)
                    .citizenDocument(uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                                         .documentUrl(uploadResponse.getDocuments().get(0).links.self.href)
                                         .documentBinaryUrl(uploadResponse.getDocuments().get(0).links.binary.href)
                                         .documentHash(uploadResponse.getDocuments().get(0).hashToken)
                                         .documentFileName(file.getOriginalFilename())
                                         .build()).build();

                log.info("Document has been saved in caseData {}", file.getOriginalFilename());
            }

            return uploadedDocuments;

        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

}
