package uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.IncrementalInteger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor
public class DynamicMultiSelectListService {

    private final UserService userService;

    public DynamicMultiSelectList getOrdersAsDynamicMultiSelectList(CaseData caseData, String key) {
        List<Element<OrderDetails>> orders = caseData.getOrderCollection();
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        if (null != orders) {
            orders.forEach(order -> {
                OrderDetails orderDetails = order.getValue();
                if (ManageOrdersOptionsEnum.servedSavedOrders.getDisplayedValue().equals(key)
                    && orderDetails.getOtherDetails() != null
                    && orderDetails.getOtherDetails().getOrderServedDate() != null) {
                    return;
                }
                listItems.add(DynamicMultiselectListElement.builder().code(orderDetails.getOrderTypeId() + "-"
                                                                               + orderDetails.getDateCreated())
                                  .label(orderDetails.getLabelForDynamicList()).build());
            });
        }
        return DynamicMultiSelectList.builder().listItems(listItems).build();
    }

    public List<DynamicMultiselectListElement> getChildrenMultiSelectList(CaseData caseData) {
        List<Element<Child>> children = caseData.getChildren();
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        IncrementalInteger i = new IncrementalInteger(1);
        if (children != null) {
            children.forEach(child -> {
                if (!YesOrNo.Yes.equals(child.getValue().getIsFinalOrderIssued())) {
                    listItems.add(DynamicMultiselectListElement.builder().code(child.getId().toString())
                                      .label(child.getValue().getFirstName() + " "
                                                 + child.getValue().getLastName()
                                                 + " (Child " + i.getAndIncrement() + ")").build());
                }
            });
        } else if (caseData.getApplicantChildDetails() != null) {
            caseData.getApplicantChildDetails().forEach(child -> listItems.add(DynamicMultiselectListElement.builder()
                                                                                   .code(child.getId().toString())
                                                                                   .label(child.getValue().getFullName()).build()));
        }
        return listItems;
    }

    public Map<String, List<DynamicMultiselectListElement>> getRespondentsMultiSelectList(CaseData caseData) {
        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        List<DynamicMultiselectListElement> respondentSolicitorList = new ArrayList<>();
        IncrementalInteger i = new IncrementalInteger(1);
        IncrementalInteger j = new IncrementalInteger(1);
        if (respondents != null) {
            respondents.forEach(respondent -> {
                listItems.add(DynamicMultiselectListElement.builder().code(respondent.getId().toString())
                                  .label(respondent.getValue().getFirstName() + " "
                                             + respondent.getValue().getLastName()
                                             + " (Respondent " + i.getAndIncrement() + ")").build());
                if (YesNoDontKnow.yes.equals(respondent.getValue().getDoTheyHaveLegalRepresentation())) {
                    respondentSolicitorList.add(DynamicMultiselectListElement.builder()
                                                    .code(respondent.getId().toString())
                                                    .label(respondent.getValue().getRepresentativeFirstName() + " "
                                                               + respondent.getValue().getRepresentativeLastName()
                                                               + " (Respondent solicitor " + j.getAndIncrement() + ")")
                                                    .build());
                }
            });
        } else if (caseData.getRespondentsFL401() != null) {
            String name = caseData.getRespondentsFL401().getFirstName() + " "
                + caseData.getRespondentsFL401().getLastName()
                + " (Respondent)";
            respondentSolicitorList.add(DynamicMultiselectListElement.builder()
                                            .code(name)
                                            .label(caseData.getRespondentsFL401().getRepresentativeFirstName() + " "
                                                       + caseData.getRespondentsFL401().getRepresentativeLastName()
                                                       + " (Respondent solicitor)")
                                            .build());
            listItems.add(DynamicMultiselectListElement.builder().code(name).label(name).build());
        }
        Map<String, List<DynamicMultiselectListElement>> respondentdetails = new HashMap<>();
        respondentdetails.put("respondents", listItems);
        respondentdetails.put("respondentSolicitors", respondentSolicitorList);
        return respondentdetails;
    }

    public Map<String, List<DynamicMultiselectListElement>> getApplicantsMultiSelectList(CaseData caseData) {
        List<Element<PartyDetails>> applicants = caseData.getApplicants();
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        List<DynamicMultiselectListElement> applicantSolicitorList = new ArrayList<>();
        IncrementalInteger i = new IncrementalInteger(1);
        IncrementalInteger j = new IncrementalInteger(1);
        if (applicants != null) {
            applicants.forEach(applicant -> {
                listItems.add(DynamicMultiselectListElement.builder().code(applicant.getId().toString())
                                  .label(applicant.getValue().getFirstName() + " "
                                             + applicant.getValue().getLastName()
                                             + " (Applicant " + i.getAndIncrement() + ")").build());
                applicantSolicitorList.add(DynamicMultiselectListElement.builder()
                                               .code(applicant.getId().toString())
                                               .label(applicant.getValue().getRepresentativeFirstName() + " "
                                                          + applicant.getValue().getRepresentativeLastName()
                                                          + " (Applicant Solicitor " + j.getAndIncrement() + ")")
                                               .build());
            });
        } else if (caseData.getApplicantsFL401() != null) {
            String name = caseData.getApplicantsFL401().getFirstName() + " "
                + caseData.getApplicantsFL401().getLastName()
                + "(Applicant)";
            applicantSolicitorList.add(DynamicMultiselectListElement.builder().code(name)
                                           .label(caseData.getApplicantsFL401().getFirstName() + " "
                                                      + caseData.getApplicantsFL401().getRepresentativeLastName()
                                                      + "(Applicant solicitor)").build());
            listItems.add(DynamicMultiselectListElement.builder().code(name).label(name).build());
        }
        Map<String, List<DynamicMultiselectListElement>> applicantdetails = new HashMap<>();
        applicantdetails.put("applicants", listItems);
        applicantdetails.put("applicantSolicitors", applicantSolicitorList);
        return applicantdetails;
    }

    public List<DynamicMultiselectListElement> getOtherPeopleMultiSelectList(CaseData caseData) {
        List<DynamicMultiselectListElement> otherPeopleList = new ArrayList<>();
        if (caseData.getOthersToNotify() != null) {
            caseData.getOthersToNotify().forEach(others ->
                                                     otherPeopleList.add(DynamicMultiselectListElement.builder()
                                                                             .code(others.getId().toString())
                                                                             .label(others.getValue().getFirstName()
                                                                                        + " "
                                                                                        + others.getValue().getLastName())
                                                                             .build())
            );
        }
        return otherPeopleList;
    }

    public String getStringFromDynamicMultiSelectList(DynamicMultiSelectList dynamicMultiSelectList) {
        List<String> strList = new ArrayList<>();
        if (null != dynamicMultiSelectList && null != dynamicMultiSelectList.getValue()) {
            dynamicMultiSelectList.getValue().forEach(value ->
                                                          strList.add(value.getLabel().split("\\(")[0])
            );
        }
        if (!strList.isEmpty()) {
            return String.join(", ", strList);
        }
        return "";
    }

    public String getStringFromDynamicMultiSelectListFromListItems(DynamicMultiSelectList dynamicMultiSelectList) {
        List<String> strList = new ArrayList<>();
        if (null != dynamicMultiSelectList && null != dynamicMultiSelectList.getListItems()) {
            dynamicMultiSelectList.getListItems().forEach(value -> {
                if (null != value.getLabel()) {
                    strList.add(value.getLabel().split("\\(")[0]);
                }
            });
        }
        if (!strList.isEmpty()) {
            return String.join(", ", strList);
        }
        return "";
    }

    public List<Element<Child>> getChildrenForDocmosis(CaseData caseData) {
        List<Element<Child>> childList = new ArrayList<>();
        if (null != caseData.getManageOrders()
            && YesOrNo.No.equals(caseData.getManageOrders().getIsTheOrderAboutAllChildren())
            && null != caseData.getManageOrders().getChildOption()
            && null != caseData.getManageOrders().getChildOption().getValue()) {
            caseData.getManageOrders().getChildOption().getValue().forEach(value -> {
                Child child = getChildDetails(caseData, value.getCode());
                if (null != child) {
                    childList.add(element(child));

                }
            });
        }
        return childList;
    }

    public List<Element<ApplicantChild>> getApplicantChildDetailsForDocmosis(CaseData caseData) {
        List<Element<ApplicantChild>> applicantChildList = new ArrayList<>();
        if (null != caseData.getManageOrders()
            && YesOrNo.Yes.equals(caseData.getManageOrders().getIsTheOrderAboutChildren())
            && null != caseData.getManageOrders().getChildOption()
            && null != caseData.getManageOrders().getChildOption().getValue()) {
            caseData.getManageOrders().getChildOption().getValue().forEach(value -> {
                ApplicantChild applicantChild = getApplicantChildDetails(caseData, value.getCode());
                if (null != applicantChild) {
                    applicantChildList.add(element(applicantChild));

                }
            });
        }
        return applicantChildList;
    }

    private Child getChildDetails(CaseData caseData, String id) {
        Optional<Child> child = Optional.empty();
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            child = caseData.getChildren().stream().filter(element -> element.getId().toString().equalsIgnoreCase(id))
                .map(Element::getValue)
                .findFirst();
        }
        return child.orElseGet(() -> null);
    }

    private ApplicantChild getApplicantChildDetails(CaseData caseData, String id) {
        Optional<ApplicantChild> applicantChild = Optional.empty();
        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            && null != caseData.getApplicantChildDetails()) {
            applicantChild = caseData.getApplicantChildDetails().stream().filter(element -> element.getId().toString().equalsIgnoreCase(
                id))
                .map(Element::getValue)
                .findFirst();
        }
        return applicantChild.orElseGet(() -> null);
    }

    public DynamicMultiSelectList getSolicitorRepresentedParties(String authorisation, CaseData caseData) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        if (userDetails != null && userDetails.getEmail() != null) {
            log.info("listItems size is:: " + listItems.size());
            if (null != caseData.getRespondents()) {
                caseData.getRespondents().forEach(respondent -> {
                    listItems.addAll(getPartiesAsDynamicMultiSelectList(
                        userDetails,
                        respondent.getValue(),
                        respondent.getId()
                    ));
                });
                log.info("listItems size is:: " + listItems.size());
                log.info("listItems are:: " + listItems);
            }
            if (null != caseData.getApplicants()) {
                caseData.getApplicants().forEach(applicant -> {
                    listItems.addAll(getPartiesAsDynamicMultiSelectList(
                        userDetails,
                        applicant.getValue(),
                        applicant.getId()
                    ));
                });
                log.info("listItems size is:: " + listItems.size());
                log.info("listItems are:: " + listItems);
            }
            if (null != caseData.getRespondentsFL401()) {
                listItems.addAll(getPartiesAsDynamicMultiSelectList(userDetails, caseData.getRespondentsFL401(), null));
                log.info("listItems size is:: " + listItems.size());
                log.info("listItems are:: " + listItems);
            }
            if (null != caseData.getApplicantsFL401()) {
                listItems.addAll(getPartiesAsDynamicMultiSelectList(userDetails, caseData.getApplicantsFL401(), null));
                log.info("listItems size is:: " + listItems.size());
                log.info("listItems are:: " + listItems);
            }
        }
        log.info("listItems size is:: " + listItems.size());
        log.info("listItems are:: " + listItems);
        return DynamicMultiSelectList.builder().listItems(listItems).build();
    }

    private List<DynamicMultiselectListElement> getPartiesAsDynamicMultiSelectList(UserDetails userDetails,
                                                                                   PartyDetails partyDetails,
                                                                                   UUID partyId) {
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        log.info("verifying party:: " + partyDetails.getLabelForDynamicList());
        log.info("verifying solicitor:: " + partyDetails.getSolicitorEmail());
        log.info("party solicitor represented:: " + partyDetails.getUser().getSolicitorRepresented());
        log.info("logged in user email:: " + userDetails.getEmail());
        log.info("partyId is:: " + partyId);

        if (partyDetails.getUser() != null
            && YesOrNo.Yes.equals(partyDetails.getUser().getSolicitorRepresented())
            && userDetails.getEmail().equals(partyDetails.getSolicitorEmail())) {
            if (partyId != null) {
                listItems.add(DynamicMultiselectListElement
                                  .builder()
                                  .code(String.valueOf(partyDetails.getPartyId()))
                                  .label(partyDetails.getLabelForDynamicList())
                                  .build());
            } else {
                listItems.add(DynamicMultiselectListElement
                                  .builder()
                                  .code(String.valueOf(partyId))
                                  .label(partyDetails.getLabelForDynamicList())
                                  .build());
            }
        }
        return listItems;
    }
}
