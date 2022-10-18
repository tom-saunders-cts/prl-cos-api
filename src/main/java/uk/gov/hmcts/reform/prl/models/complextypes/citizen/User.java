package uk.gov.hmcts.reform.prl.models.complextypes.citizen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class User {
    private String idamId;
    private String email;
}
