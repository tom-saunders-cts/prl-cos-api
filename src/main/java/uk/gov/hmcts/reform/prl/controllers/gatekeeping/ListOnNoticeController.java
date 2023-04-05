package uk.gov.hmcts.reform.prl.controllers.gatekeeping;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.ListOnNoticeReasonsEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TIER_OF_JUDICIARY;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class ListOnNoticeController {

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(path = "/listOnNotice/reasonUpdation/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to amend order mid-event")
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse serveOrderMidEvent(
        @RequestHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        log.info("*** mid event triggered for List ON Notice : {}", caseData.getId());
        log.info("*** selectedReasonsForListOnNotice : {}", callbackRequest);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<ListOnNoticeReasonsEnum> listOnNoticeReasonsEnums = (List<ListOnNoticeReasonsEnum>)(caseDataUpdated.get("selectedReasonsForListOnNotice"));
        log.info("*** inside null check for reasons Enum : {}", getReasonsSelected(listOnNoticeReasonsEnums));

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    private String getReasonsSelected(List<ListOnNoticeReasonsEnum> listOnNoticeReasonsEnum) {
        String reasonsSelected = "";
        listOnNoticeReasonsEnum.stream().forEach(reason ->  reasonsSelected.concat(reason.getDisplayedValue() + "\n"));
        return reasonsSelected;
    }
}
