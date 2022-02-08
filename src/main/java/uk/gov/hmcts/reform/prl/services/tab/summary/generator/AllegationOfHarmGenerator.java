package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

@Component
public class AllegationOfHarmGenerator implements FieldGenerator {

    @Override
    public CaseSummary generate(CaseData caseData) {
        String typeOfHarm = getTypeOfHarm(caseData);
        return CaseSummary.builder().allegationOfHarm(AllegationOfHarm.builder()
                                                          .typesOfHarm(typeOfHarm)
                                                          .status(typeOfHarm.isEmpty() ? "No Allegations of harm" : "")
                                                          .build()).build();
    }

    private String getTypeOfHarm(CaseData caseData) {
        List<String> typeOfHarm = new ArrayList<>();
        if (YesOrNo.Yes.equals(caseData.getAllegationsOfHarmDomesticAbuseYesNo())) {
            typeOfHarm.add("Domestic abuse");
        }
        if (YesOrNo.Yes.equals(caseData.getAllegationsOfHarmChildAbductionYesNo())) {
            typeOfHarm.add("Child abduction");
        }
        if (YesOrNo.Yes.equals(caseData.getAllegationsOfHarmChildAbuseYesNo())) {
            typeOfHarm.add("Child abuse");
        }
        if (YesOrNo.Yes.equals(caseData.getAllegationsOfHarmSubstanceAbuseYesNo())) {
            typeOfHarm.add("Drugs, alcohol or substance abuse");
        }
        if (YesOrNo.Yes.equals(caseData.getAllegationsOfHarmOtherConcernsYesNo())) {
            typeOfHarm.add("Safety or welfare concerns");
        }

        if (typeOfHarm.isEmpty()) {
            return "No Allegations of harm";
        }

        String join = String.join(", ", typeOfHarm);
        return join.substring(0,1).toUpperCase() + join.substring(1).toLowerCase();

    }
}
