package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
public class AllegationsOfHarmOrders {

    private final YesOrNo ordersNonMolestation;
    Order nonMolestationOrder;
    private final YesOrNo ordersOccupation;
    Order occupationOrder;
    private final YesOrNo ordersForcedMarriageProtection;
    Order forcedMarriageOrder;
    private final YesOrNo ordersRestraining;
    Order restrainingOrder;
    private final YesOrNo ordersOtherInjunctive;
    Order otherInjunctiveOrder;
    private final YesOrNo ordersUndertakingInPlace;
    Order undertakingInPlaceOrder;

}
