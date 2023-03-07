package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;

import javax.validation.constraints.NotNull;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@Jacksonized
public class BeforeStart {

    @NotNull(message = "Applicant age should be provided")
    private final ApplicantAge applicantHowOld;

}