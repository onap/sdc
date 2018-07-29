package org.openecomp.sdc.be.components.distribution.engine.rest;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.AnythingPattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import fj.data.Either;
import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.openecomp.sdc.be.components.distribution.engine.DistributionStatusNotificationEnum;
import org.openecomp.sdc.be.components.distribution.engine.DummyDistributionConfigurationManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.common.http.config.*;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.security.SecurityUtil;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MsoRestClientTest {

    private static final String MSO_HOST = "127.0.0.1";
    private static final String MSO_API_URL = "onap/mso/infra/modelDistributions/v1";
    private static final String DISTRIBUTION_ID = "1000";

    private MSORestClient msoRestClient;
    static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);


    @ClassRule
    public static WireMockRule msoRestServer = new WireMockRule(options()
            .dynamicPort()
            .bindAddress(MSO_HOST));

    @Before
    public void setupMsoServer() throws Exception {
        String encodedPw = "";
        Either<String, String> passkey = SecurityUtil.INSTANCE.decrypt(getPwd());
        if(passkey.isLeft()) {
            encodedPw = passkey.left().value();
        }
        else {
            throw new IllegalArgumentException(passkey.right().value());
        }
        
        msoRestServer.resetToDefaultMappings();
        msoRestServer.stubFor(patch(urlMatching(String.format("/%s%s/(.*)", MSO_API_URL, getDistributionsUrl())))
                .withBasicAuth(getUserName(), encodedPw)
                .withHeader(HttpHeaders.CONTENT_TYPE, containing("application/json"))
                .withRequestBody(matchingJsonPath("$.status"))
                .withRequestBody(matchingJsonPath("$.errorReason", new AnythingPattern()))//error reason is not mandatory
                .willReturn(aResponse().withStatus(200)));
    }

    @Before
    public void setUp() throws Exception {
        DummyDistributionConfigurationManager distributionEngineConfigurationMock = new DummyDistributionConfigurationManager();
        when(distributionEngineConfigurationMock.getConfigurationMock().getMsoConfig()).thenReturn(new MsoDummyConfig(msoRestServer.port()));
        msoRestClient = new MSORestClient();
    }

    @Test
    public void notifyDistributionComplete_emptyErrReason() throws Exception {
        HttpResponse<String> response = msoRestClient.notifyDistributionComplete(DISTRIBUTION_ID, DistributionStatusNotificationEnum.DISTRIBUTION_COMPLETE_OK, "");
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    private int getNumOfRetries() {
        return ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().getMsoConfig().getHttpClientConfig().getNumOfRetries();
    }

    @Test
    public void notifyDistributionComplete_noErrReason() throws Exception {
        HttpResponse<String> response = msoRestClient.notifyDistributionComplete(DISTRIBUTION_ID, DistributionStatusNotificationEnum.DISTRIBUTION_COMPLETE_OK, null);
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    public void notifyDistributionComplete_completeWithError() throws Exception {
        HttpResponse<String> response = msoRestClient.notifyDistributionComplete(DISTRIBUTION_ID, DistributionStatusNotificationEnum.DISTRIBUTION_COMPLETE_ERROR, "my reason");
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    public void testRetries() throws Exception {
        int readTimeout = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().getMsoConfig().getHttpClientConfig().getTimeouts().getReadTimeoutMs();
        int expectedNumOfRetries = getNumOfRetries();

        UrlPattern msoReqUrlPattern = urlMatching(String.format("/%s%s/(.*)", MSO_API_URL, getDistributionsUrl()));
        msoRestServer.stubFor(patch(msoReqUrlPattern)
                .willReturn(new ResponseDefinitionBuilder().withFixedDelay(readTimeout + 1)));
        HttpResponse<String> response = msoRestClient.notifyDistributionComplete(DISTRIBUTION_ID, DistributionStatusNotificationEnum.DISTRIBUTION_COMPLETE_ERROR, "my reason");
        verify(expectedNumOfRetries + 1, patchRequestedFor(msoReqUrlPattern));
        assertThat(response.getStatusCode()).isEqualTo(500);
    }

    private static String getDistributionsUrl() {
        return "/distributions";
    }

    private static String getUserName() {
        return "asdc";
    }

    private static String getPwd() {
        return "OTLEp5lfVhYdyw5EAtTUBQ==";
    }

    private static class MsoDummyConfig extends ExternalServiceConfig {
        int port;

        MsoDummyConfig(int port) {
            this.port = port;

            BasicAuthorization basicAuthorization = new BasicAuthorization();
            basicAuthorization.setUserName(MsoRestClientTest.getUserName());
            basicAuthorization.setPassword(getPwd());
            HttpClientConfig httpClientConfig = new HttpClientConfig(new Timeouts(500, 2000), basicAuthorization);
            httpClientConfig.setNumOfRetries(getNumOfRetries());
            super.setHttpClientConfig(httpClientConfig);

            HttpRequestConfig httpRequestConfig = new HttpRequestConfig();
            httpRequestConfig.setServerRootUrl(String.format("http://%s:%s/%s", MSO_HOST, this.port, MSO_API_URL));
            httpRequestConfig.getResourceNamespaces().put(MSORestClient.DISTRIBUTIONS_RESOURCE_CONFIG_PARAM, getDistributionsUrl());
            super.setHttpRequestConfig(httpRequestConfig);
        }

        int getNumOfRetries() {
            return 1;
        }
    }

}
