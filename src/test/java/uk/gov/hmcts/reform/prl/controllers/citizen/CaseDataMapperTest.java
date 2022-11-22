package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.TestUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.liveWithOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.prohibitedStepsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.specificIssueOrder;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataMapperTest {

    private static final String CASE_TYPE = "C100";
    private final ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private CaseDataMapper caseDataMapper;

    private CaseData caseData;

    @Before
    public void setUp() throws IOException {
        mapper.registerModule(new JSR310Module());
        caseData = CaseData.builder()
                .id(1234567891234567L)
                .caseTypeOfApplication(CASE_TYPE)
                .c100RebuildInternationalElements(TestUtil.readFileFrom("classpath:c100-rebuild/ie.json"))
                .c100RebuildHearingWithoutNotice(TestUtil.readFileFrom("classpath:c100-rebuild/hwn.json"))
                .c100RebuildTypeOfOrder(TestUtil.readFileFrom("classpath:c100-rebuild/too.json"))
                .c100RebuildOtherProceedings(TestUtil.readFileFrom("classpath:c100-rebuild/op.json"))
                .c100RebuildMaim(TestUtil.readFileFrom("classpath:c100-rebuild/miam.json"))
                .c100RebuildHearingUrgency(TestUtil.readFileFrom("classpath:c100-rebuild/hu.json"))
                .c100RebuildChildDetails(TestUtil.readFileFrom("classpath:c100-rebuild/cd.json"))
                .c100RebuildApplicantDetails(TestUtil.readFileFrom("classpath:c100-rebuild/appl.json"))
                .c100RebuildOtherChildrenDetails(TestUtil.readFileFrom("classpath:c100-rebuild/ocd.json"))
                .c100RebuildReasonableAdjustments(TestUtil.readFileFrom("classpath:c100-rebuild/ra.json"))
                .c100RebuildOtherPersonsDetails(TestUtil.readFileFrom("classpath:c100-rebuild/oprs.json"))
                .c100RebuildRespondentDetails(TestUtil.readFileFrom("classpath:c100-rebuild/resp.json"))
                .c100RebuildConsentOrderDetails(TestUtil.readFileFrom("classpath:c100-rebuild/co.json"))
                .build();
    }

    @Test
    public void testCaseDataMapper() throws IOException {

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData);

        //Then
        JSONAssert.assertEquals(TestUtil.readFileFrom("classpath:c100-rebuild/mapper-response.json"),
                mapper.writeValueAsString(updatedCaseData), false);
    }

    @Test
    public void testCaseDataMapperForOrderTypeExtraFields() throws IOException {

        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildTypeOfOrder(TestUtil.readFileFrom("classpath:c100-rebuild/too1.json"))
                .build();
        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertEquals(CASE_TYPE, updatedCaseData.getCaseTypeOfApplication());
        assertEquals(List.of(childArrangementsOrder, prohibitedStepsOrder, specificIssueOrder),
                updatedCaseData.getOrdersApplyingFor());
        assertEquals(liveWithOrder, updatedCaseData.getTypeOfChildArrangementsOrder());
    }

    @Test
    public void testCaseDataMapperWhenNoOtherProceedingOrdersExist() throws IOException {

        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildOtherProceedings(TestUtil.readFileFrom("classpath:c100-rebuild/op1.json"))
                .build();
        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNull(updatedCaseData.getExistingProceedings());
    }

    @Test
    public void testCaseDataMapperForMiamExtraFields() throws IOException {

        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildMaim(TestUtil.readFileFrom("classpath:c100-rebuild/miam1.json"))
                .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNull(updatedCaseData.getMiamExemptionsChecklist());
    }

    @Test
    public void testCaseDataMapperForChildDetail() throws IOException {
        //Given
        CaseData caseData1 = caseData.toBuilder()
                .c100RebuildChildDetails(TestUtil.readFileFrom("classpath:c100-rebuild/cd1.json")).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getChildren());
    }

    @Test
    public void testCaseDataMapperForOtherChildrenDetail() throws IOException {
        //Given
        CaseData caseData1 = caseData.toBuilder()
                .c100RebuildOtherChildrenDetails(TestUtil.readFileFrom("classpath:c100-rebuild/ocd1.json")).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getOtherChildren());
    }

    @Test
    public void testCaseDataMapperForOtherChildrenDetailNull() throws IOException {
        //Given
        CaseData caseData1 = caseData.toBuilder()
                .c100RebuildOtherChildrenDetails(TestUtil.readFileFrom("classpath:c100-rebuild/ocd2.json")).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNull(updatedCaseData.getOtherChildren());
    }

    @Test
    public void testCaseDataMapperReasonableAdjustmentsExtraFields() throws IOException {
        CaseData caseData1 = caseData.toBuilder()
                .c100RebuildReasonableAdjustments(TestUtil.readFileFrom("classpath:c100-rebuild/ra1.json"))
                .build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
    }

    @Test
    public void testCaseDataMapperForOtherPersonDetails() throws IOException {
        //Given
        CaseData caseData1 = caseData.toBuilder()
                .c100RebuildOtherPersonsDetails(TestUtil.readFileFrom("classpath:c100-rebuild/oprs1.json")).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getOthersToNotify());
    }

    @Test
    public void testCaseDataMapperForOtherPersonDetailsUnknownDoB() throws IOException {
        //Given
        CaseData caseData1 = caseData.toBuilder()
                .c100RebuildOtherPersonsDetails(TestUtil.readFileFrom("classpath:c100-rebuild/oprs2.json")).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getOthersToNotify());
    }

    @Test
    public void testCaseDataMapperForRespondentDetails() throws IOException {
        //Given
        CaseData caseData1 = caseData.toBuilder()
                .c100RebuildRespondentDetails(TestUtil.readFileFrom("classpath:c100-rebuild/resp1.json")).build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getRespondents());
    }

}
