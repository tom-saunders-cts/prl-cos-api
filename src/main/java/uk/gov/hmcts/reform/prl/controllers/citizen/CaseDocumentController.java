package uk.gov.hmcts.reform.prl.controllers.citizen;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DeleteDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.UploadedDocumentRequest;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.UploadDocumentService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARTY_NAME;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class CaseDocumentController {

    private static final String SERVICE_AUTH = "ServiceAuthorization";

    public static final String CITIZEN_DOCUMENT_UPLOAD_EVENT_ID = "citizenUploadedDocument";

    @Autowired
    private AuthorisationService authorisationService;

    @Autowired
    private DocumentGenService documentGenService;

    @Autowired
    private UploadDocumentService uploadService;

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamClient idamClient;

    @Autowired
    CaseService caseService;

    Integer fileIndex;

    @PostMapping(path = "/generate-citizen-statement-document", consumes = APPLICATION_JSON, produces =
        APPLICATION_JSON)
    @Operation(description = "Generate a PDF for citizen as part of upload documents")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document generated"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public String generateCitizenStatementDocument(@RequestHeader("Authorization")
                                                       @Parameter(hidden = true)   String authorisation,
                                                   @RequestBody GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest) throws Exception {
        return documentGenService.generateCitizenStatementDocument(authorisation, generateAndUploadDocumentRequest);
    }


    @PostMapping(path = "/upload-citizen-statement-document", produces = APPLICATION_JSON_VALUE, consumes =
        MULTIPART_FORM_DATA_VALUE)
    @Operation(description = "Call CDAM to upload document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Uploaded Successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request while uploading the document"),
        @ApiResponse(responseCode = "401", description = "Provided Authroization token is missing or invalid"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity uploadCitizenStatementDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                         @RequestHeader("serviceAuthorization") String s2sToken,
                                                         @RequestBody UploadedDocumentRequest uploadedDocumentRequest) {

        log.info("Uploaded doc request: {}", uploadedDocumentRequest);
        String caseId = uploadedDocumentRequest.getValues().get("caseId").toString();
        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);
        log.info("Case Data retrieved for id : " + caseDetails.getId().toString());
        CaseData tempCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation)) && Boolean.TRUE.equals(
            authorisationService.authoriseService(s2sToken))) {
            log.info("=====trying to upload document=====");

            UploadedDocuments uploadedDocuments = uploadService.uploadCitizenDocument(
                authorisation,
                uploadedDocumentRequest,
                caseId
            );
            List<Element<UploadedDocuments>> uploadedDocumentsList;
            Element<UploadedDocuments> uploadedDocsElement = element(uploadedDocuments);
            if (tempCaseData.getCitizenUploadedDocumentList() != null
                && !tempCaseData.getCitizenUploadedDocumentList().isEmpty()) {
                uploadedDocumentsList = tempCaseData.getCitizenUploadedDocumentList();
                uploadedDocumentsList.add(uploadedDocsElement);
            } else {
                uploadedDocumentsList = new ArrayList<>();
                uploadedDocumentsList.add(uploadedDocsElement);
            }
            CaseData caseData = CaseData.builder()
                .id(Long.parseLong(caseId))
                .citizenUploadedDocumentList(uploadedDocumentsList)
                .build();

            StartEventResponse startEventResponse =
                coreCaseDataApi.startEventForCaseWorker(
                    authorisation,
                    authTokenGenerator.generate(),
                    idamClient.getUserInfo(authorisation).getUid(),
                    JURISDICTION,
                    CASE_TYPE,
                    caseId,
                    CITIZEN_DOCUMENT_UPLOAD_EVENT_ID
                );

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                           .id(startEventResponse.getEventId())
                           .build())
                .data(caseData).build();

            CaseDetails caseDetails1 = coreCaseDataApi.submitEventForCaseWorker(
                authorisation,
                authTokenGenerator.generate(),
                idamClient.getUserInfo(authorisation).getUid(),
                JURISDICTION,
                CASE_TYPE,
                caseId,
                true,
                caseDataContent
            );
            return ResponseEntity.ok().body(
                DocumentDetails.builder().documentId(uploadedDocsElement.getId().toString())
                    .documentName(uploadedDocuments.getCitizenDocument().getDocumentFileName()).build());
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping(path = "/delete-citizen-statement-document", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Delete a PDF for citizen as part of upload documents")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document generated"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public String deleteCitizenStatementDocument(@RequestBody DeleteDocumentRequest deleteDocumentRequest,
                                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                 @RequestHeader("serviceAuthorization") String s2sToken) throws Exception {
        List<Element<UploadedDocuments>> tempUploadedDocumentsList;
        List<Element<UploadedDocuments>> uploadedDocumentsList = new ArrayList<>();
        String caseId = deleteDocumentRequest.getValues().get(CASE_ID);
        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, s2sToken, caseId);
        log.info("Case Data retrieved for id : " + caseDetails.getId().toString());
        CaseData tempCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (deleteDocumentRequest.getValues() != null
            && deleteDocumentRequest.getValues().containsKey(DOCUMENT_ID)) {
            final String documenIdToBeDeleted = deleteDocumentRequest.getValues().get(DOCUMENT_ID);
            log.info("Document to be deleted with id : " + documenIdToBeDeleted);
            tempUploadedDocumentsList = tempCaseData.getCitizenUploadedDocumentList();
            /*for (Element<UploadedDocuments> element : tempUploadedDocumentsList) {
                if (!documenIdToBeDeleted.equalsIgnoreCase(
                    element.getId().toString())) {
                    uploadedDocumentsList.add(element);
                }
            }*/
            uploadedDocumentsList =
                tempUploadedDocumentsList.stream().filter(element -> !documenIdToBeDeleted.equalsIgnoreCase(
                    element.getId().toString()))
                    .collect(Collectors.toList());
        }
        log.info("uploadedDocumentsList::" + uploadedDocumentsList.size());
        CaseData caseData = CaseData.builder().id(Long.valueOf(caseId))
            .citizenUploadedDocumentList(uploadedDocumentsList).build();
        caseService.updateCase(
            caseData,
            authorisation,
            s2sToken,
            caseId,
            CITIZEN_UPLOADED_DOCUMENT
        );
        return "SUCCESS";
    }

    @PostMapping(path = "/upload-citizen-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces =
        APPLICATION_JSON)
    @Operation(description = "Call CDAM to upload document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Uploaded Successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request while uploading the document"),
        @ApiResponse(responseCode = "401", description = "Provided Authroization token is missing or invalid"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<?> uploadCitizenDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                                   @RequestHeader(SERVICE_AUTH) String serviceAuthorization,
                                                   @RequestParam("file") MultipartFile file) {

        if (!isAuthorized(authorisation, serviceAuthorization)) {
            throw (new RuntimeException("Invalid Client"));
        }
        return ResponseEntity.ok(documentGenService.uploadDocument(authorisation, file));
    }

    @DeleteMapping("/{documentId}/delete")
    @Operation(description = "Delete a document from client document api")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deleted document successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request while deleting the document"),
        @ApiResponse(responseCode = "401", description = "Provided Authorization token is missing or invalid"),
        @ApiResponse(responseCode = "404", description = "Document not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    public ResponseEntity<?> deleteDocument(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                            @RequestHeader(SERVICE_AUTH) String serviceAuthorization,
                                            @PathVariable("documentId") String documentId) {
        if (!isAuthorized(authorisation, serviceAuthorization)) {
            throw (new RuntimeException("Invalid Client"));
        }
        return ResponseEntity.ok(documentGenService.deleteDocument(authorisation, documentId));
    }

    private boolean isAuthorized(String authorisation, String serviceAuthorization) {
        return Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation)) && Boolean.TRUE.equals(
            authorisationService.authoriseService(serviceAuthorization));

}

