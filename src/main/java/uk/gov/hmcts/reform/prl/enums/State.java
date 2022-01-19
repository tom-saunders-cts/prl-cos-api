package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum State {

    @JsonProperty("AWAITING_C100_SUBMISSION_TO_HMCTS")
    AWAITING_C100_SUBMISSION_TO_HMCTS("AWAITING_C100_SUBMISSION_TO_HMCTS"),
    @JsonProperty("AWAITING_FL401_SUBMISSION_TO_HMCTS")
    AWAITING_FL401_SUBMISSION_TO_HMCTS("AWAITING_FL401_SUBMISSION_TO_HMCTS");

    private final String value;

}
