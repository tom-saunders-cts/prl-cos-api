package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.time.Time;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendOrderService {
    private static final String MEDIA_TYPE = "application/pdf";
    private static final String FILE_NAME_PREFIX = "amended_";

    private final AmendedOrderStamper stamper;
    private final  UploadDocumentService uploadService;
    private final Time time;

    public Map<String, Object> updateOrder(CaseData caseData, String authorisation) throws IOException {
        ManageOrders eventData = caseData.getManageOrders();

        byte[] stampedBinaries = stamper.amendDocument(eventData.getManageOrdersDocumentToAmend(), authorisation);
        String amendedFileName = updateFileName(eventData.getManageOrdersDocumentToAmend());
        Document stampedDocument = uploadService.uploadDocument(stampedBinaries, amendedFileName, MEDIA_TYPE, authorisation);


        uk.gov.hmcts.reform.prl.models.documents.Document updatedDocument = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName(stampedDocument.originalDocumentName)
            .documentUrl(stampedDocument.links.self.href)
            .documentBinaryUrl(stampedDocument.links.binary.href)
            .build();


        return updateAmendedOrderDetails(caseData, updatedDocument);

    }

    private String updateFileName(uk.gov.hmcts.reform.prl.models.documents.Document original) {
        String filename = original.getDocumentFileName();
        return filename.startsWith(FILE_NAME_PREFIX) ? filename : FILE_NAME_PREFIX + filename;
    }

    private Map<String, Object> updateAmendedOrderDetails(CaseData caseData,
                                                          uk.gov.hmcts.reform.prl.models.documents.Document amendedDocument) {

        UUID selectedOrderId = caseData.getManageOrders().getAmendOrderDynamicList().getValueCodeAsUuid();
        List<Element<OrderDetails>> orders = caseData.getOrderCollection();

        log.info("***** ROLE *** {} ", caseData.getManageOrders().getIsJudgeOrLa());
        log.info("Result =====" + (!"Judge".equalsIgnoreCase(caseData.getManageOrders().getIsJudgeOrLa())
            && (YesOrNo.Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())
            || WhatToDoWithOrderEnum.finalizeSaveToServeLater
            .equals(caseData.getServeOrderData().getWhatDoWithOrder()))));
        if (!"Judge".equalsIgnoreCase(caseData.getManageOrders().getIsJudgeOrLa())
            && (YesOrNo.Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())
            || WhatToDoWithOrderEnum.finalizeSaveToServeLater
                .equals(caseData.getServeOrderData().getWhatDoWithOrder()))) {
            orders.stream()
                .filter(order -> Objects.equals(order.getId(), selectedOrderId))
                .findFirst()
                .ifPresent(order -> {
                    OrderDetails amended = order.getValue().toBuilder()
                        .orderDocument(amendedDocument)
                        .orderType(order.getValue().getOrderType())
                        .typeOfOrder(order.getValue().getTypeOfOrder())
                        .otherDetails(order.getValue().getOtherDetails().toBuilder()
                                          .orderAmendedDate(time.now().format(DateTimeFormatter.ofPattern(
                                              PrlAppsConstants.D_MMMM_YYYY,
                                              Locale.UK
                                          )))
                                          .build())
                        .build();

                    orders.addAll(List.of(element(amended)));
                });
            orders.sort(Comparator.comparing(
                m -> m.getValue().getOtherDetails().getOrderCreatedDate(),
                Comparator.reverseOrder()
            ));
            return Map.of("orderCollection", orders);
        } else {
            return  setDraftOrderCollection(caseData, amendedDocument);
        }

    }

    public Map<String, Object> setDraftOrderCollection(CaseData caseData, uk.gov.hmcts.reform.prl.models.documents.Document amendedDocument) {
        List<Element<DraftOrder>> draftOrderList = new ArrayList<>();
        Element<DraftOrder> draftOrderElement = element(getCurrentDraftOrderDetails(caseData,amendedDocument));
        if (caseData.getDraftOrderCollection() != null) {
            draftOrderList.addAll(caseData.getDraftOrderCollection());
            draftOrderList.add(draftOrderElement);
        } else {
            draftOrderList.add(draftOrderElement);
        }
        draftOrderList.sort(Comparator.comparing(
            m -> m.getValue().getOtherDetails().getDateCreated(),
            Comparator.reverseOrder()
        ));
        return Map.of("draftOrderCollection", draftOrderList
        );
    }

    private DraftOrder getCurrentDraftOrderDetails(CaseData caseData,
                                                   uk.gov.hmcts.reform.prl.models.documents.Document amendedDocument) {
        UUID selectedOrderId = caseData.getManageOrders().getAmendOrderDynamicList().getValueCodeAsUuid();
        List<Element<OrderDetails>> orders = caseData.getOrderCollection();
        Optional<Element<OrderDetails>> orderDetails  = orders.stream()
            .filter(order -> Objects.equals(order.getId(), selectedOrderId))
            .findFirst();
        String orderType = orderDetails.get().getValue().getOrderType();

        return DraftOrder.builder()
            .typeOfOrder(orderType)
            .orderTypeId(orderType)
            .orderDocument(amendedDocument)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy(caseData.getJudgeOrMagistratesLastName())
                              .dateCreated(time.now())
                              .status("Draft").build())
            .dateOrderMade(caseData.getDateOrderMade())
            .orderSelectionType(caseData.getManageOrdersOptions())
            .build();
    }
}
