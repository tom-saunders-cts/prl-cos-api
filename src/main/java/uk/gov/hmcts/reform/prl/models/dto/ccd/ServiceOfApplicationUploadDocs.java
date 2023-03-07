package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceOfApplicationUploadDocs {

    @JsonProperty("pd36qLetter")
    private final Document pd36qLetter;
    @JsonProperty("specialArrangementsLetter")
    private final Document specialArrangementsLetter;
    @JsonProperty("additionalDocuments")
    private final Document additionalDocuments;
    @JsonProperty("sentDocumentPlaceHolder")
    private final String sentDocumentPlaceHolder;
}