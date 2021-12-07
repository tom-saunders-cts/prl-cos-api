package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum YesNoDontKnow {

    @JsonProperty("yes")
    YES("Yes"),
    @JsonProperty("no")
    NO("No"),
    @JsonProperty("dontKnow")
    DONT_KNOW("Don't know");

    private final String displayedValue;


}
