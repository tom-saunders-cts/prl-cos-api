package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.UnderTakingEnum;

import java.time.LocalDate;
import java.util.List;

@Data
@Jacksonized
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
public class ManageOrders {
    @JsonProperty("cafcassEmailAddress")
    private final List<String> cafcassEmailAddress;
    @JsonProperty("otherEmailAddress")
    private final List<String> otherEmailAddress;

    private final String manageOrdersCourtName;
    @JsonProperty("manageOrdersCourtAddress")
    private final Address manageOrdersCourtAddress;
    private final String manageOrdersCaseNo;
    private final String manageOrdersApplicant;
    private final String manageOrdersApplicantReference;
    private final String manageOrdersRespondent;
    private final String manageOrdersRespondentReference;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersRespondentDob;
    @JsonProperty("manageOrdersRespondentAddress")
    private final Address manageOrdersRespondentAddress;
    private final YesOrNo manageOrdersUnderTakingRepr;
    private final UnderTakingEnum underTakingSolicitorCounsel;
    private final String manageOrdersUnderTakingPerson;
    @JsonProperty("manageOrdersUnderTakingAddress")
    private final Address manageOrdersUnderTakingAddress;
    private final String manageOrdersUnderTakingTerms;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersDateOfUnderTaking;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate underTakingDateExpiry;
    private final String underTakingExpiryTime;
    private final YesOrNo underTakingFormSign;
    private final String recitalsOrPreamble;
    private final String orderDirections;
    private final String furtherDirectionsIfRequired;

    private final String recitalsOrPreamble;
    private final String orderDirections;
    private final String furtherDirectionsIfRequired;
}
