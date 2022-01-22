package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MiamUrgencyReasonChecklistEnum {

    @JsonProperty("miamUrgencyReasonChecklistEnum_Value_1")
    MIAMUrgencyReasonChecklistEnum_Value_1("There is risk to the life, liberty or physical safety of the prospective applicant or his"
                                               +
                                               " or her family or his or her home; or"),
    @JsonProperty("miamUrgencyReasonChecklistEnum_Value_2")
    MIAMUrgencyReasonChecklistEnum_Value_2("Any delay caused by MIAM would cause significant risk of a miscarriage of justice"),
    @JsonProperty("miamUrgencyReasonChecklistEnum_Value_3")
    MIAMUrgencyReasonChecklistEnum_Value_3("Any delay caused by MIAM would cause unreasonable hardship to the prospective applicant"),
    @JsonProperty("miamUrgencyReasonChecklistEnum_Value_4")
    MIAMUrgencyReasonChecklistEnum_Value_4("Any delay caused by MIAM would cause irretrievable problems in dealing with the dispute "
                                               +
                                               "(including the irretrievable loss of significant evidence)"),
    @JsonProperty("miamUrgencyReasonChecklistEnum_Value_5")
    MIAMUrgencyReasonChecklistEnum_Value_5("There  is a significant risk that in the period necessary to schedule and attend a MIAM, "
                                               +
                                               "proceedings relating to the dispute will be brought in another state in which a valid "
                                               +
                                               "claim to jurisdiction may exist, such that a court in that other State would be seized of the "
                                               +
                                               "dispute before a court in England and Wales."),
    @JsonProperty("miamUrgencyReasonChecklistEnum_Value_6")
    MIAMUrgencyReasonChecklistEnum_Value_6("There is a risk of unlawful removal of a child from the United Kingdom,"
                                                +
                                                "or a risk of unlawful retention of a child who is currently outside England and Wales"),
    @JsonProperty("miamUrgencyReasonChecklistEnum_Value_7")
    MIAMUrgencyReasonChecklistEnum_Value_7("There is a risk of harm to a child");

    private final String displayedValue;


}
