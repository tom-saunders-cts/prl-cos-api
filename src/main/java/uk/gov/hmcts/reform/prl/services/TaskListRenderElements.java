

package uk.gov.hmcts.reform.prl.services;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.JURISDICTION;

@Component
public class TaskListRenderElements {


    public String renderLink(Task task) {
        return renderLink(task.getEvent());
    }

    public String renderLink(Event event) {
        return format("<a href='/cases/case-details/${[CASE_REFERENCE]}/trigger/%s/%s1'>%s</a>",
                      event.getId(), event.getId(), event.getName());
    }

    public String renderDisabledLink(Task event) {
        return format("<a>%s</a>", event.getEvent().getName());
    }

    public String renderHint(String text) {
        return format("<span class='govuk-hint govuk-!-font-size-14'>%s</span>", text);
    }

    public String renderInfo(String text) {
        return format("<div class='panel panel-border-wide govuk-!-font-size-16'>%s</div>", text);
    }

    public String renderHeader(String text) {
        return format("## %s", text);
    }

    public List<String> renderCollapsible(String header, List<String> lines) {
        final List<String> collapsible = new ArrayList<>();

        collapsible.add("<details class='govuk-details'>");
        collapsible.add("<summary class='govuk-details__summary'>");
        collapsible.add("<span class='govuk-details__summary-text'>");
        collapsible.add(header);
        collapsible.add("</span>");
        collapsible.add("</summary>");
        collapsible.add("<div class='govuk-details__text'>");
        collapsible.addAll(lines);
        collapsible.add("</div>");
        collapsible.add("</details>");

        return collapsible;
    }

}
