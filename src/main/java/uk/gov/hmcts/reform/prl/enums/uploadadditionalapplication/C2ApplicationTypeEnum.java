package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum C2ApplicationTypeEnum {
    @JsonProperty("applicationWithNotice")
    applicationWithNotice(
        "applicationWithNotice",
        "Application with notice. The other party will be notified about this application, even if there is no hearing"
    ),
    @JsonProperty("applicationWithoutNotice")
    applicationWithoutNotice(
        "applicationWithoutNotice",
        "Application by consent or without notice. No notice will be sent to the other party, even if there is a hearing"
    );


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static C2ApplicationTypeEnum getValue(String key) {
        return C2ApplicationTypeEnum.valueOf(key);
    }
}

