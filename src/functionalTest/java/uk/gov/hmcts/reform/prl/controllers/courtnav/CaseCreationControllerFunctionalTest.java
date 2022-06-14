package uk.gov.hmcts.reform.prl.controllers.courtnav;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseCreationControllerFunctionalTest {
    private final String userToken = "Bearer testToken";

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String VALID_REQUEST_BODY = "requests/courtnav-request.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenCourtNavCaseCreationData_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header(
                "Authorization",
                idamTokenGenerator.generateIdamTokenForSystem(),
                "ServiceAuthorization",
                serviceAuthenticationGenerator.generate()
            )
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/case-creation")
            .then().assertThat().statusCode(200);
    }


    @Test
    public void givenNoCaseDataInRequestBody_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", "Bearer xyz", "ServiceAuthorization", "abc")
            .when()
            .contentType("application/json")
            .post("/case-creation")
            .then().assertThat().statusCode(500);
    }
}
