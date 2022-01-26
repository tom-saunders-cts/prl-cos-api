package uk.gov.hmcts.reform.prl.handlers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;
import uk.gov.hmcts.reform.prl.services.TaskListRenderer;
import uk.gov.hmcts.reform.prl.services.TaskListService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.SUBMIT_AND_PAY;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.FINISHED;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskState.NOT_STARTED;


@RunWith(MockitoJUnitRunner.class)
public class CaseEventHandlerTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private TaskListService taskListService;

    @Mock
    private TaskListRenderer taskListRenderer;

    @Mock
    private TaskErrorService taskErrorService;

    @InjectMocks
    private CaseEventHandler caseEventHandler;

    @Test
    public void shouldUpdateTaskListForCasesInOpenState() {
        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        final CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);
        final List<Task> tasks = List.of(
            Task.builder().event(CASE_NAME).state(FINISHED).build(),
            Task.builder().event(SUBMIT_AND_PAY).state(NOT_STARTED).build());

        final String renderedTaskLists = "<h1>Task 1</h1><h2>Task 2</h2>";

        final List<EventValidationErrors> eventsErrors = Collections.emptyList();

        when(taskListService.getTasksForOpenCase(caseData)).thenReturn(tasks);
        when(taskListRenderer.render(tasks, eventsErrors)).thenReturn(renderedTaskLists);

        caseEventHandler.handleCaseDataChange(caseDataChanged);

        verify(taskListService).getTasksForOpenCase(caseData);
        verify(taskListRenderer).render(tasks, eventsErrors);

        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-task-list",
            Map.of("taskList", renderedTaskLists,"id",String.valueOf(caseData.getId()))
        );
    }

    @Test
    public void shouldNotUpdateTaskListForCasesInOpenState() {
        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();
        final CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);
        final List<Task> tasks = List.of(
            Task.builder().event(CASE_NAME).state(FINISHED).build(),
            Task.builder().event(SUBMIT_AND_PAY).state(NOT_STARTED).build());

        final String renderedTaskLists = "<h1>Task 1</h1><h2>Task 2</h2>";

        final List<EventValidationErrors> eventsErrors = Collections.emptyList();

        caseEventHandler.handleCaseDataChange(caseDataChanged);

        verifyNoInteractions(taskListService);
        verifyNoInteractions(taskListRenderer);
    }
}
