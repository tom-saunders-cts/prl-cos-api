package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum C2AdditionalOrdersRequested {

    @JsonProperty("CHANGE_SURNAME_OR_REMOVE_JURISDICTION")
    CHANGE_SURNAME_OR_REMOVE_JURISDICTION(
        "CHANGE_SURNAME_OR_REMOVE_JURISDICTION",
        "Change surname or remove from jurisdiction."
    ),
    @JsonProperty("APPOINTMENT_OF_GUARDIAN")
    APPOINTMENT_OF_GUARDIAN(
        "APPOINTMENT_OF_GUARDIAN",
        "Appointment of a guardian"
    ),
    @JsonProperty("TERMINATION_OF_APPOINTMENT_OF_GUARDIAN")
    TERMINATION_OF_APPOINTMENT_OF_GUARDIAN(
        "TERMINATION_OF_APPOINTMENT_OF_GUARDIAN",
            "Termination of appointment of a guardian"
    ),
    @JsonProperty("applicationWithoutNotice")
    applicationWithoutNotice(
        "applicationWithoutNotice",
            "Parental responsibility"
    ),
    @JsonProperty("REQUESTING_ADJOURNMENT")
    REQUESTING_ADJOURNMENT(
        "REQUESTING_ADJOURNMENT",
            "Requesting an adjournment for a scheduled hearing"
    );


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static C2AdditionalOrdersRequested getValue(String key) {
        return C2AdditionalOrdersRequested.valueOf(key);
    }
}
