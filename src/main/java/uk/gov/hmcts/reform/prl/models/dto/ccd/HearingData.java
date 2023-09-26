package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.reform.prl.enums.HearingChannelsEnum;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.HearingPriorityTypeEnum;
import uk.gov.hmcts.reform.prl.enums.HearingSpecificDatesOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioBeforeAEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.HearingDateTimeOption;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingDataFromTabToDocmosis;

import java.time.LocalDate;
import java.util.List;

@Data
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
public class HearingData {

    private DynamicList hearingTypes;

    private DynamicList confirmedHearingDates;

    private DynamicList hearingChannels;

    private DynamicList hearingVideoChannels;

    private DynamicList hearingTelephoneChannels;

    private DynamicList courtList;

    private DynamicList localAuthorityHearingChannel;

    private DynamicList hearingListedLinkedCases;

    private DynamicList applicantSolicitorHearingChannel;

    private DynamicList respondentHearingChannel;

    private DynamicList respondentSolicitorHearingChannel;

    private DynamicList cafcassHearingChannel;

    private DynamicList cafcassCymruHearingChannel;

    private DynamicList applicantHearingChannel;

    @JsonSerialize(using = CustomEnumSerializer.class)
    @JsonProperty("hearingDateConfirmOptionEnum")
    private HearingDateConfirmOptionEnum hearingDateConfirmOptionEnum;

    @JsonProperty("additionalHearingDetails")
    private String additionalHearingDetails;

    @JsonProperty("instructionsForRemoteHearing")
    private String instructionsForRemoteHearing;

    @JsonProperty("hearingDateTimes")
    private List<Element<HearingDateTimeOption>> hearingDateTimes;

    @JsonProperty("hearingEstimatedHours")
    private final int hearingEstimatedHours;

    @JsonProperty("hearingEstimatedMinutes")
    private final int hearingEstimatedMinutes;

    @JsonProperty("hearingEstimatedDays")
    private final int  hearingEstimatedDays;

    @JsonProperty("allPartiesAttendHearingSameWayYesOrNo")
    private final YesOrNo allPartiesAttendHearingSameWayYesOrNo;

    @JsonSerialize(using = CustomEnumSerializer.class)
    @JsonProperty("hearingAuthority")
    private DioBeforeAEnum hearingAuthority;

    @JsonSerialize(using = CustomEnumSerializer.class)
    @JsonProperty("hearingChannelsEnum")
    private HearingChannelsEnum hearingChannelsEnum;

    @JsonProperty("hearingJudgeNameAndEmail")
    private final JudicialUser hearingJudgeNameAndEmail;

    @JsonProperty("hearingJudgePersonalCode")
    private String hearingJudgePersonalCode;

    @JsonProperty("hearingJudgeLastName")
    private String hearingJudgeLastName;

    @JsonProperty("hearingJudgeEmailAddress")
    private String hearingJudgeEmailAddress;

    private String applicantName;
    private String applicantSolicitor;
    private String respondentName;
    private String respondentSolicitor;

    @JsonProperty("hearingSpecificDatesOptionsEnum")
    private HearingSpecificDatesOptionsEnum hearingSpecificDatesOptionsEnum;


    @JsonProperty("firstDateOfTheHearing")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate firstDateOfTheHearing;

    @JsonProperty("hearingMustTakePlaceAtHour")
    private int hearingMustTakePlaceAtHour;

    @JsonProperty("hearingMustTakePlaceAtMinute")
    private int hearingMustTakePlaceAtMinute;

    @JsonProperty("earliestHearingDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate earliestHearingDate;

    @JsonProperty("latestHearingDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate latestHearingDate;

    @JsonProperty("hearingPriorityTypeEnum")
    private HearingPriorityTypeEnum hearingPriorityTypeEnum;

    @JsonProperty("customDetails")
    private  String customDetails;

    @JsonProperty("isRenderingRequiredFlag")
    private YesOrNo isRenderingRequiredFlag;

    @JsonProperty("fillingFormRenderingInfo")
    private String fillingFormRenderingInfo;

    private DynamicList applicantHearingChannel1;
    private DynamicList applicantHearingChannel2;
    private DynamicList applicantHearingChannel3;
    private DynamicList applicantHearingChannel4;
    private DynamicList applicantHearingChannel5;

    private DynamicList applicantSolicitorHearingChannel1;
    private DynamicList applicantSolicitorHearingChannel2;
    private DynamicList applicantSolicitorHearingChannel3;
    private DynamicList applicantSolicitorHearingChannel4;
    private DynamicList applicantSolicitorHearingChannel5;

    private DynamicList respondentHearingChannel1;
    private DynamicList respondentHearingChannel2;
    private DynamicList respondentHearingChannel3;
    private DynamicList respondentHearingChannel4;
    private DynamicList respondentHearingChannel5;

    private DynamicList respondentSolicitorHearingChannel1;
    private DynamicList respondentSolicitorHearingChannel2;
    private DynamicList respondentSolicitorHearingChannel3;
    private DynamicList respondentSolicitorHearingChannel4;
    private DynamicList respondentSolicitorHearingChannel5;

    private final String applicantName1;
    private final String applicantName2;
    private final String applicantName3;
    private final String applicantName4;
    private final String applicantName5;

    private final String applicantSolicitor1;
    private final String applicantSolicitor2;
    private final String applicantSolicitor3;
    private final String applicantSolicitor4;
    private final String applicantSolicitor5;

    private final String respondentName1;
    private final String respondentName2;
    private final String respondentName3;
    private final String respondentName4;
    private final String respondentName5;

    private final String respondentSolicitor1;
    private final String respondentSolicitor2;
    private final String respondentSolicitor3;
    private final String respondentSolicitor4;
    private final String respondentSolicitor5;
    private List<Element<HearingDataFromTabToDocmosis>> hearingdataFromHearingTab;

    private final YesOrNo isCafcassCymru;

    private final String hearingEstimatedHoursText;
    private final String hearingEstimatedMinutesText;
    private final String  hearingEstimatedDaysText;
}
