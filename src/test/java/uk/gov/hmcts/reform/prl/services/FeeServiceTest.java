package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class FeeServiceTest {

    @Mock
    private FeeService feeService;

    @Mock
    private FeeResponse feeResponse;

    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setUp() {

        feeResponse = FeeResponse.builder()
            .code("FEE0325")
            .amount(BigDecimal.valueOf(232.00))
            .build();

    }

    @Test
    public void testToCheckFeeAmount() throws Exception {

        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        BigDecimal actualResult = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE).getAmount();

        assertEquals(BigDecimal.valueOf(232.00), actualResult);

    }

    @Test
    public void testToCheckFeeAmountWithWrongCode() throws Exception {

        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        BigDecimal actualResult = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE).getAmount();

        assertNotEquals(BigDecimal.valueOf(65.00), actualResult);
    }
}
