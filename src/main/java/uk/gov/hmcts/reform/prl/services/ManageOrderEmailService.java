package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.ManageOrderEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;




@Service
@Slf4j
@RequiredArgsConstructor
public class ManageOrderEmailService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private CourtFinderService courtLocatorService;

    @Value("${uk.gov.notify.email.application.email-id}")
    private String courtEmail;

    @Value("${xui.url}")
    private String manageCaseUrl;

    @Value("${citizen.dashboardUrl}")
    private String citizenDashboardUrl;

    private static final String URL_STRING = "/";
    private static final String URGENT_CASE = "Urgent ";
    private static final String DATE_FORMAT = "dd-MM-yyyy";


    public void sendEmail(CaseDetails caseDetails) {
        log.info("Sending the manage order emails for caseId {}", caseDetails.getId());
        List<String> emailList = new ArrayList<>();

        CaseData caseData = emailService.getCaseData(caseDetails);

        emailList.addAll(getEmailAddress(caseData.getApplicants()));
        emailList.addAll(getEmailAddress(caseData.getRespondents()));
        emailList.forEach(email -> emailService.send(
            email,
            EmailTemplateNames.SOLICITOR,
            buildEmail(caseDetails),
            LanguagePreference.english
        ));

    }

    public void sendEmailToApplicantAndRespondent(CaseDetails caseDetails) {
        log.info("Sending the new manage order emails for Applicant/Respondent {}",caseDetails.getId());
        CaseData caseData = emailService.getCaseData(caseDetails);

        if(caseData.getCaseTypeOfApplication().equalsIgnoreCase( "C100")) {
            Map<String, String> applicantsMap = getEmailPartyWithName(caseData
                                                                         .getApplicants());
            Map<String, String> respondentMap = getEmailPartyWithName(caseData
                                                                         .getRespondents());
            for (Map.Entry<String, String> appValues : applicantsMap.entrySet()) {

                emailService.send(
                    appValues.getKey(),
                    EmailTemplateNames.CA_DA_MANAGE_ORDER_EMAIL,
                    buildApplicantRespondentEmail(caseDetails, appValues.getValue()),
                    LanguagePreference.english
                );
            }

            for (Map.Entry<String, String> appValues : respondentMap.entrySet()) {

                emailService.send(
                    appValues.getKey(),
                    EmailTemplateNames.CA_DA_MANAGE_ORDER_EMAIL,
                    buildApplicantRespondentEmail(caseDetails, appValues.getValue()),
                    LanguagePreference.english
                );
            }
        } else {
            emailService.send(
                caseData.getApplicantsFL401().getEmail(),
                EmailTemplateNames.CA_DA_MANAGE_ORDER_EMAIL,
                buildApplicantRespondentEmail(caseDetails, caseData.getApplicantsFL401().getFirstName()
                    +" "+caseData.getApplicantsFL401().getFirstName()),
                LanguagePreference.english
            );
            emailService.send(
                caseData.getRespondentsFL401().getEmail(),
                EmailTemplateNames.CA_DA_MANAGE_ORDER_EMAIL,
                buildApplicantRespondentEmail(caseDetails, caseData.getRespondentsFL401().getFirstName()
                    +" "+caseData.getRespondentsFL401().getFirstName()),
                LanguagePreference.english
            );
        }


    }

    private Map<String, String> getEmailPartyWithName(List<Element<PartyDetails>> party) {
        return party
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toMap(
                PartyDetails::getEmail,
                i -> i.getFirstName() + " " + i.getLastName()
            ));
    }

    private EmailTemplateVars buildApplicantRespondentEmail(CaseDetails caseDetails,String name) {

        CaseData caseData = emailService.getCaseData(caseDetails);


        return ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(name)
            .courtName(caseData.getCourtName())
            .dashboardLink(citizenDashboardUrl)
            .build();
    }


    private EmailTemplateVars buildEmail(CaseDetails caseDetails) {

        CaseData caseData = emailService.getCaseData(caseDetails);
        String applicantNames = getApplicants(caseData).stream()
            .map(element -> element.getFirstName() + " " + element.getLastName())
            .collect(Collectors.joining(", "));

        return ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName(caseData.getCourtName())
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .courtEmail(courtEmail).build();
    }


    private List<PartyDetails> getApplicants(CaseData caseData) {
        return caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
    }

    private List<PartyDetails> getRespondents(CaseData caseData) {
        return caseData
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
    }



    private List<String> getEmailAddress(List<Element<PartyDetails>> partyDetails) {
        return partyDetails
            .stream()
            .map(Element::getValue)
                .filter(a -> a.getCanYouProvideEmailAddress().equals(YesOrNo.Yes))
            .map(PartyDetails::getEmail)
            .collect(Collectors.toList());
    }


    public void sendEmailToCafcassAndOtherParties(CaseDetails caseDetails) {

        CaseData caseData = emailService.getCaseData(caseDetails);

        ManageOrders manageOrders  = caseData.getManageOrders();

        List<String> cafcassEmails = manageOrders.getCafcassEmailAddress()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> otherEmails = manageOrders.getOtherEmailAddress()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        cafcassEmails.addAll(otherEmails);

        cafcassEmails.forEach(email ->   emailService.send(
            email,
            EmailTemplateNames.CAFCASS_OTHER,
            buildEmailToCafcassAndOtherParties(caseData),
            LanguagePreference.english
        ));

    }

    public EmailTemplateVars buildEmailToCafcassAndOtherParties(CaseData caseData) {

        String typeOfHearing = " ";

        if (YesOrNo.Yes.equals(caseData.getIsCaseUrgent())) {
            typeOfHearing = URGENT_CASE;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        return ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseUrgency(typeOfHearing)
            .issueDate(caseData.getIssueDate().format(dateTimeFormatter))
            .familyManNumber(caseData.getFamilymanCaseNumber())
            .orderLink(caseData.getPreviewOrderDoc().getDocumentFileName())
            .build();

    }

}
