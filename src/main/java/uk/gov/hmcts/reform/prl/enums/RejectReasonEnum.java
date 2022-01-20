package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RejectReasonEnum {

    @JsonProperty("consentOrderNotProvided")
    ConsentOrderNotProvided("consentOrderNotProvided","Consent Order not provided","Consent order not provided\n\n"
                            + "Your application is being returned because the document uploaded is not a draft consent order "
                            + "and/or is not signed by both parties."
                            + "\n\nNext steps"
                            + "\n\nPlease upload the correct version of the document and it contains all the relevant details.\n\n\n"),
    @JsonProperty("miamCertificateNotProvided")
    MiamCertificateNotProvided("miamCertificateNotProvided","MIAM certificate not provided","MIAM certificate not provided\n\n"
                               + "Your application is being returned because the MIAM certificate is not uploaded or is incomplete."
                               + "\n\nNext steps\n\n"
                               + "Please upload the correct version of the "
                               + "MIAM certificate and it contains all the relevant details.\n\n\n"),
    @JsonProperty("incompleteEvidenceOfMiamExamption")
    IncompleteEvidenceOfMiamExamption("incompleteEvidenceOfMiamExamption","Evidence of MIAM exemption incomplete","Evidence of MIAM "
                                      + "exemption incomplete\n\n"
                                      + "Your application is being returned because the document uploaded is not a draft consent order "
                                      + "and/or is not signed by both parties."
                                      + "\n\nNext steps\n\n"
                                      + "Please specify which MIAM exemptions apply.\n\n\n"),
    @JsonProperty("confidentalDetailListed")
    ConfidentalDetailListed("confidentalDetailListed","Confidential detail listed","Confidential detail listed\n\n"
                            + "Your application has been returned because you have listed "
                            + "some of your confidential details in the application forms.\n\n"
                            + "Next steps\n\nPlease remove your confidential details from the application.\n\n\n"),
    @JsonProperty("section9114OrderInForce")
    Section9114OrderInForce("section9114OrderInForce","Section 91(14) order in force","Section 91(14) order in force\n\n"
                            + "Your application has been returned because a Section 91(14) order is in force.\n"
                            + "In case number <x> your client is prevented from making a further application until <date>.\n\n"
                            + "Next steps\n\nPlease state why your client should be given permission to make this application.\n\n\n"),
    @JsonProperty("permissionIsNeeded")
    PermissionIsNeeded("permissionIsNeeded","Permission is needed to make application","Permission is needed to make application\n\n"
                       + "Your application has been returned because the reason for permission to make this application has not been provided."
                       + "\n\nNext steps\n\n"
                       + "Please complete the “Have you applied to the court for permission to make this application?” "
                       + "question in the Type of application section in the form.\n\n\n"),
    @JsonProperty("parentalResponsibility")
    ParentalResponsibility("parentalResponsibility","Parental responsibility","Parental responsibility\n\n"
                           + "Your application has been returned because you have not stated who has parental responsibility for each child."
                           + "\n\nNext steps\n\nPlease check the application and ensure all relevant sections have been completed in full.\n\n\n\n"),
    @JsonProperty("applicationIncomplete")
    ApplicationIncomplete("applicationIncomplete","Application incomplete","Application incorrect\n\n"
                          + "Your application has been returned because the application has not been filled out correctly."
                          + "\n\nNext steps\n\nPlease check the application and ensure all relevant sections have been completed correctly.\n\n\n"),
    @JsonProperty("clarificationNeeded")
    ClarificationNeeded("clarificationNeeded","Clarification needed","Clarification needed\n\n"
                        + "Your application has been returned because some details are not clear.\n\n"
                        + "Next steps\n\nPlease check the application and ensure all relevant sections are clear.\n\n\n"),
    @JsonProperty("otherReason")
    OtherReason("otherReason","Other reason","Other reason\n\n\n"),;

    private final String id;
    private final String displayedValue;
    private final String returnMsgText;
}
