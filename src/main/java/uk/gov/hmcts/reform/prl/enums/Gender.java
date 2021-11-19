package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {

    FEMALE("female", "Female"),
    MALE("male", "Male"),
    OTHER("other", "They identify in another way");

    private final String id;
    private final String displayedValue;

}
