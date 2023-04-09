package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class CurrentOrPastProceedingsChecker implements RespondentEventChecker {
    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        return response
            .filter(res -> anyNonEmpty(res.getCurrentOrPastProceedingsForChildren()))
            .isPresent();
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);
        List<Optional<?>> fields = new ArrayList<>();

        if (response.isPresent()) {
            fields.add(ofNullable(response.get().getCurrentOrPastProceedingsForChildren()));

            YesNoDontKnow currentOrPastProceedingsForChildren = response.get().getCurrentOrPastProceedingsForChildren();

            if (YesNoDontKnow.yes.equals(currentOrPastProceedingsForChildren)) {
                fields.add(ofNullable(response.get().getRespondentExistingProceedings()));
            }
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }

}
