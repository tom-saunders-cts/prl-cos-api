package uk.gov.hmcts.reform.prl.controllers;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = TaskListControllerIntegrationTest.class)
public class CaseInitiationControllerIntegrationTest extends IntegrationTest {

    private final String caseInitiationControllerEndpoint = "/case-initiation/submitted";

    private final String validBody = "controller/valid-request-body.json";

    @Test
    public void whenInvalidRequestFormat_Return400() throws IOException {

        HttpPost httpPost = new HttpPost("http://localhost:4044" + caseInitiationControllerEndpoint);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void whenValidRequestFormat_Return200() throws Exception {

        HttpPost httpPost = new HttpPost("http://localhost:4044" + caseInitiationControllerEndpoint);
        String requestBody = ResourceLoader.loadJson(validBody);
        httpPost.addHeader("Authorization", "TestAuth");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

}
