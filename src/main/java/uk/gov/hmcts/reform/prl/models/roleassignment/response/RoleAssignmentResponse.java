package uk.gov.hmcts.reform.prl.models.roleassignment.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.roleassignment.RoleRequest;

@Data
@Schema(description = "The response object for RoleAssignment")
public class RoleAssignmentResponse {

    private RoleRequest roleRequest;
    private RequestedRoles requestedRoles;
}
