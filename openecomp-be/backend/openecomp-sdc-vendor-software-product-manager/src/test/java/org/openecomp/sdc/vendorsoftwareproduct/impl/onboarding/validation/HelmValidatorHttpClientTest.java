package org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.http.client.api.HttpExecuteException;
import org.openecomp.sdc.common.http.client.api.HttpRequestHandler;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.helmvalidator.HelmValidatorConfig;

@RunWith(MockitoJUnitRunner.class)
public class HelmValidatorHttpClientTest {

    private static final String EXAMPLE_RESPONSE = "{\"renderErrors\":[],\"lintWarning\":[\"[WARNING] warning description\"],\"lintError\":[],\"versionUsed\":\"3.5.2\",\"valid\":false,\"deployable\":true}";
    private static final String FILE = "file";
    private static final String IS_LINTED = "isLinted";
    private static final String IS_STRICT_LINTED = "isStrictLinted";
    private static final String VERSION_DESIRED = "versionDesired";
    private static final String VERSION_DESIRED_VALUE = "2.17";
    @Mock
    HttpRequestHandler httpRequestHandler;
    @Mock
    HelmValidatorConfig helmValidatorConfig;
    public static final String TEST_CHART_FILE_NAME = "";

    @Before
    public void init() {
        when(helmValidatorConfig.getValidationEndpoint()).thenReturn("http://localhost:8080/validate");
        when(helmValidatorConfig.getVersion()).thenReturn("2.17");
    }

    @Test
    public void shouldProcessHelmValidation() throws HttpExecuteException, IOException {
//        File file = new File(TEST_CHART_FILE_NAME);
//        InputStream entity = MultipartEntityBuilder.create()
//            .addBinaryBody(FILE, file, ContentType.DEFAULT_BINARY, file.getName())
//            .addTextBody(IS_LINTED, "false")
//            .addTextBody(IS_STRICT_LINTED, "false")
//            .addTextBody(VERSION_DESIRED, VERSION_DESIRED_VALUE)
//            .build().getContent();

        //given
        when(httpRequestHandler.post(eq(helmValidatorConfig.getValidationEndpoint()), any(), any(), any()))
            .thenReturn(new HttpResponse<>(EXAMPLE_RESPONSE, 200));
        HelmValidatorHttpClient client = new HelmValidatorHttpClient(httpRequestHandler);
        //when
        var response = client.execute(TEST_CHART_FILE_NAME, helmValidatorConfig);
        //then
        assertEquals(200, response.getStatusCode());

//        var expected = new Gson().fromJson(EXAMPLE_RESPONSE, HelmValidatorResult.class);
        // assertEquals(expected, helmValidatorResult.get());
    }

    @Test
    public void shouldReturnEmpty() {
        //given
        HelmValidatorHttpClient client = new HelmValidatorHttpClient(httpRequestHandler);
        //when
        var helmValidatorResult = client.execute("", helmValidatorConfig);
        //then
        //System.out.println(helmValidatorResult);
    }


    @Test
    public void testCorrect() {
        HelmValidatorHttpClient client = new HelmValidatorHttpClient(HttpRequestHandler.get());

//        var helmValidatorConfig = new HelmValidatorConfig.HelmValidationConfigBuilder()
//            .setDeployable(true)
//            .setEnabled(true)
//            .setLintable(true)
//            .setStrictLintable(true)
//
//            .build();

        String packageFileName = "/home/piotr/ONAP/sdc-helm-validator/dev-resources/sample-charts/correct-apiVersion-v1.tgz";
        var helmValidatorResult = client.execute(packageFileName, helmValidatorConfig);

        System.out.println(helmValidatorResult);
    }


}
