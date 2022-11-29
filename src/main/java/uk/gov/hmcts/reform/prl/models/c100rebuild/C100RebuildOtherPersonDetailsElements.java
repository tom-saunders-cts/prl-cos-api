package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class C100RebuildOtherPersonDetailsElements {

    @JsonProperty("oprs_otherPersonCheck")
    private String otherPersonsCheck;
    @JsonProperty("oprs_otherPersons")
    private List<OtherPersonDetail> otherPersonDetails;
}
