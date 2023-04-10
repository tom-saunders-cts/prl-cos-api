package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class C100RespondentSolicitorControllerFunctionalTest {

    private final String userToken = "Bearer testToken";
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private C100RespondentSolicitorService respondentSolicitorService;
    @MockBean
    private DocumentGenService documentGenService;
    @Autowired
    IdamTokenGenerator idamTokenGenerator;

    private static final String VALID_REQUEST_BODY = "requests/call-back-controller.json";


    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenRequestBody_whenRespondent_solicitor_about_to_start_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/respondent-solicitor/about-to-start")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenRespondent_solicitor_about_to_submit_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/respondent-solicitor/about-to-submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenPopulate_solicitor_respondent_list_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/respondent-solicitor/populate-solicitor-respondent-list")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenRespondent_selection_about_to_submit_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/respondent-solicitor/respondent-selection-about-to-submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenKeep_details_private_list_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/respondent-solicitor/keep-details-private-list")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenGenerate_c7response_draft_document_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/respondent-solicitor/generate-c7response-draft-document")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

}
