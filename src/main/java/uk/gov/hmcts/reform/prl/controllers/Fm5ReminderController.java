package uk.gov.hmcts.reform.prl.controllers;


import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.Fm5ReminderService;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/fm5")
public class Fm5ReminderController {

    private final Fm5ReminderService fm5ReminderService;
    private final AuthorisationService authorisationService;

    /**
     * TEST API TO RUN FM5 SYSTEM RULES & TRIGGER REMINDER NOTIFICATIONS.
     */
    @PostMapping("/reminder-notifications/{hearingAwayDays}")
    public void sendFm5ReminderNotifications(
        @PathVariable("hearingAwayDays") Long hearingAwayDays,
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            log.info("*** Trigger FM5 reminder notifications via API ***");
            fm5ReminderService.sendFm5ReminderNotifications(hearingAwayDays);
        } else {
            log.error("Authorization failed, throwing invalid client error");
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
