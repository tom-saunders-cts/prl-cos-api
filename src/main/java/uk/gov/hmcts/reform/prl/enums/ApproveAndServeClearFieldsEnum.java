package uk.gov.hmcts.reform.prl.enums;

public enum ApproveAndServeClearFieldsEnum {
    whatToDoWithOrderSolicitor("whatToDoWithOrderSolicitor"),
    draftOrdersDynamicList("draftOrdersDynamicList"),
    whatToDoWithOrderCourtAdmin("whatToDoWithOrderCourtAdmin");

    private final String value;

    ApproveAndServeClearFieldsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
