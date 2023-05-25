package uk.gov.hmcts.reform.prl.services.managedocuments;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarentineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts.restrictToGroup;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsService {

    @Autowired
    private final CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private final AuthTokenGenerator authTokenGenerator;

    private final ObjectMapper objectMapper;

    public CaseData populateDocumentCategories(String authorization, CaseData caseData) {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentCategories(getCategoriesSubcategories(authorization, String.valueOf(caseData.getId())))
            .build();

        return caseData.toBuilder()
            .manageDocuments(Arrays.asList(element(manageDocuments)))
            .build();
    }

    private DynamicList getCategoriesSubcategories(String authorisation, String caseReference) {
        try {
            CategoriesAndDocuments categoriesAndDocuments = coreCaseDataApi.getCategoriesAndDocuments(
                authorisation,
                authTokenGenerator.generate(),
                caseReference
            );
            if (null != categoriesAndDocuments) {
                List<Category> parentCategories = nullSafeCollection(categoriesAndDocuments.getCategories())
                    .stream()
                    .sorted(Comparator.comparing(Category::getCategoryName))
                    .collect(Collectors.toList());

                List<DynamicListElement> dynamicListElementList = new ArrayList<>();
                CaseUtils.createCategorySubCategoryDynamicList(parentCategories, dynamicListElementList);

                return DynamicList.builder().value(DynamicListElement.EMPTY)
                    .listItems(dynamicListElementList).build();
            }
        } catch (Exception e) {
            log.error("Error in getCategoriesAndDocuments method", e);
        }
        return DynamicList.builder()
            .value(DynamicListElement.EMPTY).build();
    }

    public Map<String, Object> copyDocument(CallbackRequest callbackRequest) {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        List<Element<ManageDocuments>> manageDocuments = caseData.getManageDocuments();

        if (manageDocuments != null && !manageDocuments.isEmpty()) {
            List<Element<QuarentineLegalDoc>> quarantineDocs = !isEmpty(caseData.getLegalProfQuarentineDocsList())
                ? caseData.getLegalProfQuarentineDocsList() : new ArrayList<>();
            List<Element<QuarentineLegalDoc>> legalProfUploadDocListDocTab = !isEmpty(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab())
                ? caseData.getReviewDocuments().getLegalProfUploadDocListDocTab() : new ArrayList<>();

            log.info("*** manageDocuments List *** {}", manageDocuments);
            log.info("*** quarantineDocs -> before *** {}", quarantineDocs);
            log.info("*** legalProfUploadDocListDocTab -> before *** {}", legalProfUploadDocListDocTab);

            Predicate<Element<ManageDocuments>> restricted = manageDocumentsElement -> manageDocumentsElement.getValue()
                .getDocumentRestrictCheckbox().contains(restrictToGroup);

            for (Element<ManageDocuments> element : manageDocuments) {
                // if restricted then add to quarantine docs list
                if (restricted.test(element)) {
                    QuarentineLegalDoc quarantineLegalDoc = QuarentineLegalDoc.builder()
                        .document(element.getValue().getDocument())
                        .documentParty(element.getValue().getDocumentParty().getDisplayedValue())
                        .restrictCheckboxCorrespondence(element.getValue().getDocumentRestrictCheckbox())
                        .notes(element.getValue().getDocumentDetails())
                        .category(element.getValue().getDocumentCategories().getValueCode())
                        .build();
                    log.info("*** quarantineLegalDoc element *** {}", quarantineLegalDoc);
                    quarantineDocs.add(element(quarantineLegalDoc));
                } else {

                    // If not restricted access then add to legalProfUploadDocListDocTab list
                    final String categoryId = element.getValue().getDocumentCategories().getValueCode();
                    final Document document = element.getValue().getDocument();

                    QuarentineLegalDoc legalProfUploadDoc = QuarentineLegalDoc.builder()
                        //.document(element.getValue().getDocument())
                        .documentParty(element.getValue().getDocumentParty().getDisplayedValue())
                        .restrictCheckboxCorrespondence(element.getValue().getDocumentRestrictCheckbox())
                        .notes(element.getValue().getDocumentDetails())
                        //.category(categoryId)
                        .build();
                    if ("applicantC1AApplication".equalsIgnoreCase(categoryId)) {
                        legalProfUploadDoc = legalProfUploadDoc.toBuilder()
                            .applicantC1AApplicationDocument(element.getValue().getDocument())
                            .build();
                    } else if ("applicantC1AResponse".equalsIgnoreCase(categoryId)) {
                        legalProfUploadDoc = legalProfUploadDoc.toBuilder()
                            .applicantC1AResponseDocument(element.getValue().getDocument())
                            .build();
                    }
                    log.info("*** legalProfUploadDoc element *** {}", legalProfUploadDoc);
                    legalProfUploadDocListDocTab.add(element(legalProfUploadDoc));

                    log.info("Category Id in ManageDocument Service ---> {}", categoryId);
                    log.info("document in ManageDocument Service ---> {}", document);

                    //for case file view
                    List<Element<Document>> documents = (List<Element<Document>>) caseDataUpdated.get(categoryId);

                    if (!isEmpty(documents)) {
                        documents.add(element(document));
                    } else {
                        documents = new ArrayList<>();
                        documents.add(element(document));
                    }
                    caseDataUpdated.put(categoryId, documents);
                }
            }

            log.info("quarantineDocs List ---> after {}", quarantineDocs);
            log.info("legalProfUploadDocListDocTab List ---> after {}", legalProfUploadDocListDocTab);

            if (!quarantineDocs.isEmpty()) {
                caseDataUpdated.put("legalProfQuarentineDocsList", quarantineDocs);
            }
            if (!legalProfUploadDocListDocTab.isEmpty()) {
                caseDataUpdated.put("legalProfUploadDocListDocTab", legalProfUploadDocListDocTab);
            }
        }
        return caseDataUpdated;
    }
}
