package uk.gov.hmcts.reform.prl.controllers.cafcaas;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassCdamService;

import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@Slf4j
@RestController
@RequestMapping("/cases")
public class CafcassDocumentManagementController {
    @Autowired
    CafcassCdamService cafcassCdamService;

    @Autowired
    private AuthorisationService authorisationService;

    @GetMapping(path = "/documents/{documentId}/binary")
    @Operation(description = "Call CDAM to download document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Downloaded Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request while downloading the document"),
            @ApiResponse(responseCode = "401", description = "Provided Authorization token is missing or invalid"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Resource> downloadDocument(@RequestHeader(AUTHORIZATION) String authorisation,
                                                     @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                                                     @PathVariable UUID documentId) {
        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation)) && Boolean.TRUE.equals(
            authorisationService.authoriseService(serviceAuthorisation))) {
            log.info("processing  request after authorization");
            return cafcassCdamService.getDocument(authorisation, serviceAuthorisation, documentId);

        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
