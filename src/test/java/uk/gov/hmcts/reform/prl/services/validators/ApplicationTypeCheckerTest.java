package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;

import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum.noNotRequired;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.yes;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationTypeCheckerTest {

    @Mock
    private TaskErrorService taskErrorService;

    @InjectMocks
    private ApplicationTypeChecker applicationTypeChecker;

    @Test
    public void whenFieldsPartiallyCompleteIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder()
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .applicationDetails("Test details")
            .build();

        assert !applicationTypeChecker.isFinished(caseData);

    }

    @Test
    public void whenAllRequiredFieldsCompletedThenIsFinishedReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .natureOfOrder("Test")
            .consentOrder(yes)
            .applicationPermissionRequired(noNotRequired)
            .applicationDetails("Test details")
            .build();

        assert applicationTypeChecker.isFinished(caseData);

    }

    @Test
    public void whenAnyFieldCompletedThenIsStartedReturnsTrue() {

        CaseData caseData = CaseData.builder()
            .natureOfOrder("Test")
            .build();

        assert applicationTypeChecker.isStarted(caseData);

    }

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assert !applicationTypeChecker.isStarted(caseData);

    }

    @Test
    public void whenNoCaseDataThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assert !applicationTypeChecker.hasMandatoryCompleted(caseData);

    }

    @Test
    public void whenCaseDataPresentThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder()
            .ordersApplyingFor(Collections.singletonList(childArrangementsOrder))
            .natureOfOrder("Test")
            .consentOrder(yes)
            .applicationPermissionRequired(noNotRequired)
            .applicationDetails("Test details")
            .build();

        assert !applicationTypeChecker.hasMandatoryCompleted(caseData);

    }


}
