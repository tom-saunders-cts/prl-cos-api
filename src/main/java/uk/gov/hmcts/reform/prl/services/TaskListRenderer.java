package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskSection;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_APPLICANT_FAMILY_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_HOME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_SOT_AND_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_UPLOAD_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.prl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.LITIGATION_CAPACITY;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.RELATIONSHIP_TO_RESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_BEHAVIOUR;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.Event.SUBMIT_AND_PAY;
import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.VIEW_PDF_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.WELSH_LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.WITHOUT_NOTICE_ORDER;
import static uk.gov.hmcts.reform.prl.enums.State.AWAITING_RESUBMISSION_TO_HMCTS;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskSection.newSection;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListRenderer {

    private static final String HORIZONTAL_LINE = "<hr class='govuk-!-margin-top-3 govuk-!-margin-bottom-2'/>";
    private static final String NEW_LINE = "<br/>";
    private static final String NOT_STARTED = "not-started.png";
    private static final String CANNOT_START_YET = "cannot-start-yet.png";
    private static final String IN_PROGRESS = "in-progress.png";
    private static final String INFORMATION_ADDED = "information-added.png";
    private static final String FINISHED = "finished.png";

    private final TaskListRenderElements taskListRenderElements;


    public String render(List<Task> allTasks, List<EventValidationErrors> tasksErrors, boolean isC100CaseType, CaseData caseData) {
        final List<String> lines = new LinkedList<>();

        lines.add("<div class='width-50'>");

        (isC100CaseType ? groupInSections(allTasks, caseData) : groupInSectionsForFL401(allTasks, caseData))
            .forEach(section -> lines.addAll(renderSection(section)));

        lines.add("</div>");

        lines.addAll(renderTasksErrors(tasksErrors));

        return String.join("\n\n", lines);
    }

    private List<TaskSection> groupInSections(List<Task> allTasks, CaseData caseData) {
        final Map<Event, Task> tasks = allTasks.stream().collect(toMap(Task::getEvent, identity()));

        final TaskSection applicationDetails = newSection("Add application details")
            .withTask(tasks.get(CASE_NAME))
            .withTask(tasks.get(TYPE_OF_APPLICATION))
            .withTask(tasks.get(HEARING_URGENCY));

        final TaskSection peopleInTheCase = newSection("Add people to the case")
            .withTask(tasks.get(APPLICANT_DETAILS))
            .withTask(tasks.get(CHILD_DETAILS))
            .withTask(tasks.get(RESPONDENT_DETAILS));

        final TaskSection requiredDetails = newSection("Add required details")
            .withTask(tasks.get(MIAM))
            .withTask(tasks.get(ALLEGATIONS_OF_HARM));

        final TaskSection additionalInformation = newSection("Add additional information")
            .withInfo("Only complete if relevant")
            .withTask(tasks.get(OTHER_PEOPLE_IN_THE_CASE))
            .withTask(tasks.get(OTHER_PROCEEDINGS))
            .withTask(tasks.get(ATTENDING_THE_HEARING))
            .withTask(tasks.get(INTERNATIONAL_ELEMENT))
            .withTask(tasks.get(LITIGATION_CAPACITY))
            .withTask(tasks.get(WELSH_LANGUAGE_REQUIREMENTS));

        final TaskSection pdfApplication = newSection("View PDF application")
            .withTask(tasks.get(VIEW_PDF_DOCUMENT));

        final TaskSection submit;

        if (caseData.getState().equals(AWAITING_RESUBMISSION_TO_HMCTS)) {
            submit = newSection("Submit")
                .withTask(tasks.get(SUBMIT));
        } else {
            submit = newSection("Submit and pay")
                .withTask(tasks.get(SUBMIT_AND_PAY));
        }

        return Stream.of(applicationDetails,
                         peopleInTheCase,
                         requiredDetails,
                         additionalInformation,
                         pdfApplication,
                         submit)
            .filter(TaskSection::hasAnyTask)
            .collect(toList());
    }

    private List<String> renderSection(TaskSection sec) {
        final List<String> section = new LinkedList<>();

        section.add(NEW_LINE);
        section.add(taskListRenderElements.renderHeader(sec.getName()));

        sec.getHint().map(taskListRenderElements::renderHint).ifPresent(section::add);
        sec.getInfo().map(taskListRenderElements::renderInfo).ifPresent(section::add);

        section.add(HORIZONTAL_LINE);
        sec.getTasks().forEach(task -> {
            section.addAll(renderTask(task));
            section.add(HORIZONTAL_LINE);
        });

        return section;
    }

    private List<String> renderTask(Task task) {
        final List<String> lines = new LinkedList<>();

        switch (task.getState()) {

            case NOT_STARTED:
                if (task.getEvent().equals(VIEW_PDF_DOCUMENT) || task.getEvent().equals(FL401_UPLOAD_DOCUMENTS)) {
                    lines.add(taskListRenderElements.renderLink(task));
                } else if (task.getEvent().equals(SUBMIT_AND_PAY) || task.getEvent().equals(FL401_SOT_AND_SUBMIT)
                    || task.getEvent().equals(SUBMIT)) {
                    lines.add(taskListRenderElements.renderDisabledLink(task)
                                  + taskListRenderElements.renderImage(CANNOT_START_YET, "Cannot start yet"));
                } else {
                    lines.add(taskListRenderElements.renderLink(task)
                                  + taskListRenderElements.renderImage(NOT_STARTED, "Not started"));
                }
                break;
            case IN_PROGRESS:
                lines.add(taskListRenderElements.renderLink(task)
                              + taskListRenderElements.renderImage(IN_PROGRESS, "In progress"));
                break;
            case MANDATORY_COMPLETED:
                lines.add(taskListRenderElements.renderLink(task)
                              + taskListRenderElements.renderImage(INFORMATION_ADDED, "Information added"));
                break;
            case FINISHED:
                if (task.getEvent().equals(SUBMIT_AND_PAY) || task.getEvent().equals(FL401_SOT_AND_SUBMIT)
                    || task.getEvent().equals(SUBMIT)) {
                    lines.add(taskListRenderElements.renderLink(task)
                                  + taskListRenderElements.renderImage(NOT_STARTED, "Not started yet"));
                } else {
                    lines.add(taskListRenderElements.renderLink(task)
                                  + taskListRenderElements.renderImage(FINISHED, "Finished"));
                }
                break;
            default:
                lines.add(taskListRenderElements.renderLink(task));
        }

        task.getHint().map(taskListRenderElements::renderHint).ifPresent(lines::add);
        return lines;
    }

    private List<String> renderTasksErrors(List<EventValidationErrors> taskErrors) {
        if (isEmpty(taskErrors)) {
            return emptyList();
        }
        final List<String> errors = taskErrors.stream()
            .flatMap(task -> task.getErrors()
                .stream()
                .map(error -> format("%s in %s", error, taskListRenderElements.renderLink(task.getEvent()))))
            .collect(toList());

        return taskListRenderElements.renderCollapsible("Why can't I submit my application?", errors);
    }


    private List<TaskSection> groupInSectionsForFL401(List<Task> allTasks, CaseData caseData) {
        final Map<Event, Task> tasks = allTasks.stream().collect(toMap(Task::getEvent, identity()));
        Optional<TypeOfApplicationOrders> ordersOptional = ofNullable(caseData.getTypeOfApplicationOrders());

        final TaskSection applicationDetails = newSection("Add application details")
            .withTask(tasks.get(FL401_CASE_NAME))
            .withTask(tasks.get(FL401_TYPE_OF_APPLICATION))
            .withTask(tasks.get(WITHOUT_NOTICE_ORDER));

        final TaskSection peopleInTheCase = newSection("Add people to the case")
            .withTask(tasks.get(APPLICANT_DETAILS))
            .withTask(tasks.get(RESPONDENT_DETAILS))
            .withTask(tasks.get(FL401_APPLICANT_FAMILY_DETAILS));

        final TaskSection addCaseDetails = newSection("Add case details")
            .withTask(tasks.get(RELATIONSHIP_TO_RESPONDENT));

        if (ordersOptional.isEmpty() || (ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)
            && ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder))) {
            addCaseDetails.withTask(tasks.get(RESPONDENT_BEHAVIOUR));
            addCaseDetails.withTask(tasks.get(FL401_HOME));
        } else  if (ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)) {
            addCaseDetails.withTask(tasks.get(FL401_HOME));
        } else if (ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder)) {
            addCaseDetails.withTask(tasks.get(RESPONDENT_BEHAVIOUR));
        }

        final TaskSection additionalInformation = newSection("Add additional information")
            .withInfo("Only complete if relevant")
            .withTask(tasks.get(FL401_OTHER_PROCEEDINGS))
            .withTask(tasks.get(ATTENDING_THE_HEARING))
            .withTask(tasks.get(WELSH_LANGUAGE_REQUIREMENTS));

        final TaskSection uploadDocuments = newSection("Upload documents")
            .withTask(tasks.get(FL401_UPLOAD_DOCUMENTS));

        final TaskSection checkAndSignApplication = newSection("Check and sign application")
            .withTask(tasks.get(VIEW_PDF_DOCUMENT))
            .withTask(tasks.get(FL401_SOT_AND_SUBMIT));

        return Stream.of(applicationDetails,
                         peopleInTheCase,
                         addCaseDetails,
                         additionalInformation,
                         uploadDocuments,
                         checkAndSignApplication)
            .filter(TaskSection::hasAnyTask)
            .collect(toList());
    }

}
