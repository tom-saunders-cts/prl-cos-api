package uk.gov.hmcts.reform.prl.enums;

public enum ManageOrderFieldsEnum {

    manageOrdersOptions("manageOrdersOptions"),
    createSelectOrderOptions("createSelectOrderOptions"),
    childArrangementOrders("childArrangementOrders"),
    domesticAbuseOrders("domesticAbuseOrders"),
    fcOrders("fcOrders"),
    otherOrdersOption("otherOrdersOption"),
    amendOrderDynamicList("amendOrderDynamicList"),
    serveOrderDynamicList("serveOrderDynamicList"),
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
    //ordersHearingDetails("ordersHearingDetails"),
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
    tempOrdersHearingDetails("tempOrdersHearingDetails");

    private final String value;

    ManageOrderFieldsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
