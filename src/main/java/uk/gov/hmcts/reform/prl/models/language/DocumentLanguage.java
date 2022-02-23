package uk.gov.hmcts.reform.prl.models.language;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class DocumentLanguage {

    @Builder.Default
    private boolean isGenEng = true;
    @Builder.Default
    private boolean isGenWelsh = false;


}
