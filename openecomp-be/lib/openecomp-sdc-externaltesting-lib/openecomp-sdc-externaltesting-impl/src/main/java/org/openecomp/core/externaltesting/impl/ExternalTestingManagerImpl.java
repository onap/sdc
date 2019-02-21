/*
 * Copyright © 2019 iconectiv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.core.externaltesting.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.externaltesting.api.*;
import org.openecomp.core.externaltesting.errors.ExternalTestingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ExternalTestingManagerImpl implements ExternalTestingManager {

  private Logger logger = LoggerFactory.getLogger(ExternalTestingManagerImpl.class);

  private static final String CONFIGURATION_ERROR = "Configuration Error";
  private static final String NO_ENDPOINTS_DEFINED_IN_BACK_END_CONFIGURATION = "No endpoints defined in back end configuration";
  private static final String BAD_REQUEST = "Bad Request";
  private static final String CONFIG_FILE_PROPERTY = "configuration.yaml";
  private static final String CONFIG_SECTION = "externalTestingConfig";
  private static final String VTP_TESTS_URI = "/v1/vtp/tests";

  private TestingAccessConfig accessConfig;
  private Map<String, RemoteTestingEndpointDefinition> endpoints;

  private RestTemplate restTemplate;

  protected List<VariableResolver> variableResolvers;

  public ExternalTestingManagerImpl() {
    // nothing to do at the moment.
    restTemplate = new RestTemplate();
  }

  /**
   * Read the configuration from the yaml file for this bean.  If we get an exception during load,
   * don't force an error starting SDC but log a warning.  Do no warm...
   */
  @PostConstruct
  public void loadConfig() {

    variableResolvers = new ArrayList<>();
    createVariableResolvers();
    variableResolvers.forEach(VariableResolver::init);

    String file = Objects.requireNonNull(System.getProperty(CONFIG_FILE_PROPERTY),
            "Config file location must be specified via system property " + CONFIG_FILE_PROPERTY);
    try {
      Object rawConfig = getExternalTestingAccessConfiguration(file);
      if (rawConfig != null) {
        accessConfig = new ObjectMapper().convertValue(rawConfig, TestingAccessConfig.class);
        endpoints = new HashMap<>();
        accessConfig.getEndpoints().stream().filter(RemoteTestingEndpointDefinition::isEnabled).forEach(e -> endpoints.put(e.getId(), e));
      }
    }
    catch (IOException ex) {
      logger.warn("Unable to initialize external testing configuration.  Add 'testingConfig' to configuration.yaml with url value.  Feature will be hobbled with results hardcoded to empty values.", ex);
    }
  }

  protected void createVariableResolvers() {
    variableResolvers.add(new CsarMetadataVariableResolver());
  }

  /**
   * Return the definition for a given test.  In post-Dublin environment,
   * we can have multiple testing endpoints we interact with so caller
   * would need to invoke the API with the endpointId provided.
   * @param testId identifier of the test to query the parameters for.
   * @return Test definition matching id provided
   */
  @Override
  public Test getTestDefinition(String testId) {

    if (endpoints == null) {
      throw new ExternalTestingException(CONFIGURATION_ERROR, 500, NO_ENDPOINTS_DEFINED_IN_BACK_END_CONFIGURATION);
    }

    if (endpoints.size() == 1) {
      return proxyGetRequestToExternalTestingSite(endpoints.values().iterator().next().getUrl() + VTP_TESTS_URI + "/" + testId, Test.class);
    } else {
      throw new ExternalTestingException(BAD_REQUEST, 400, "Multiple endpoints exist and endpoint not specified for request");
    }
  }

  /**
   * Return the definition for a given test.  In post-Dublin environment,
   * we can have multiple testing endpoints we interact with.
   * @param testId identifier of the test to query the parameters for.
   * @param endpointId endpoint to which the request should be sent.
   * @return Test definition matching id provided
   */
  @Override
  public Test getTestDefinition(String endpointId, String testId) {

    if (endpoints == null) {
      throw new ExternalTestingException(CONFIGURATION_ERROR, 500, NO_ENDPOINTS_DEFINED_IN_BACK_END_CONFIGURATION);
    }
    RemoteTestingEndpointDefinition endpoint = endpoints.get(endpointId);
    if (endpoint == null) {
      throw new ExternalTestingException(BAD_REQUEST, 400, "Unable to execute test " + testId + ".  No endpoint named " + endpointId + " found");
    }
    else {
      String url = endpoint.getUrl() + VTP_TESTS_URI + "/" + testId;
      return proxyGetRequestToExternalTestingSite(url, Test.class);
    }
  }

  /**
   * Return the configuration of this feature that we want to
   * expose to the client.  Treated as a JSON blob for flexibility.
   */
  @Override
  public String getConfig() {
    ClientConfiguration cc = null;
    if (accessConfig != null) {
      cc = accessConfig.getClient();
    }
    if (cc == null) {
      cc = new ClientConfiguration();
      cc.setEnabled(false);
    }
    try {
      return new ObjectMapper().writeValueAsString(cc);
    } catch (JsonProcessingException e) {
      logger.error("failed to write client config", e);
      return "{}";
    }
  }

  /**
   * Proxy request to VTP/Repository for list of available tests.
   * If there are multiple endpoints defined (post Dublin), send a request
   * to each endpoint and merge results into a single document for client.
   * @return set of tests at each endpoint.
   */
  public TestSet listTests() {
    if (accessConfig != null) {
      // if only 1 endpoint, fallback to direct call and do not merge.
      if (endpoints.size() == 1) {
        return proxyGetRequestToExternalTestingSite(accessConfig.getEndpoints().get(0).getUrl() + VTP_TESTS_URI, TestSet.class);
      }
      else {
        // create an artificial root to hold test from each endpoint.
        TestSet root = new TestSet();
        root.setId("root");
        root.setTitle("Root");
        root.setSets(new ArrayList<>());
        endpoints.values().forEach(e -> {
          TestSet ts = proxyGetRequestToExternalTestingSite(e.getUrl() + VTP_TESTS_URI, TestSet.class);
          ts.setEndpointId(e.getId());
          root.getSets().add(ts);
        });
        return root;
      }

    }
    else {
      // external testing URL not configured, returned empty result to not break SDC.
      return new TestSet();
    }
  }

  /**
   * Load the external testing access configuration from the SDC onboarding yaml configuration file.
   * @param file filename to retrieve data from
   * @return parsed YAML object
   * @throws IOException thrown if failure in reading YAML content.
   */
  private Object getExternalTestingAccessConfiguration(String file) throws IOException {
    Map<?, ?> configuration = Objects.requireNonNull(readConfigurationFile(file), "Configuration cannot be empty");
    Object testingConfig = configuration.get(CONFIG_SECTION);
    if (testingConfig == null) {
      logger.warn("Unable to initialize external testing access configuration.  Add 'testingConfig' to configuration.yaml with url value.  Feature will be hobbled with results hardcoded to empty values.");
    }

    return testingConfig;
  }

  /**
   *  Load the onboarding yaml config file.
   * @param file name of file to load
   * @return map containing YAML properties.
   * @throws IOException thrown in the event of YAML parse or IO failure.
   */
  private static Map<?, ?> readConfigurationFile(String file) throws IOException {
    try (InputStream fileInput = new FileInputStream(file)) {
      YamlUtil yamlUtil = new YamlUtil();
      return yamlUtil.yamlToMap(fileInput);
    }
  }


  /**
   * Proxy the request to run a test.   Certain tests require processing here in the SDC backend.
   * This includes look-up of CSAR or HEAT metadata for processing by the remote testing service.
   * To do so, we run a set of variable resolvers on the input request.
   * @param req parsed text to execute.
   * @return results from the remote testing service
   * @throws ExternalTestingException exception thrown if request to external testing site fails.
   */
  public String run(TestExecutionRequest req) {

    // short circuit fail.
    if (endpoints == null) {
      throw new ExternalTestingException(CONFIGURATION_ERROR, 500, NO_ENDPOINTS_DEFINED_IN_BACK_END_CONFIGURATION);
    }

    // Some tests require work on the back end to resolve variables into the
    // data required in the test.  In particular, we may need to load content
    // from a CSAR file for use in testing.
    if (req.getTests() != null) {
      req.getTests().forEach(this::runVariableResolvers);
    }

    if (endpoints.size() == 1) {
      // there is only one endpoint so we just send it through.
      return runAtEndpoint(endpoints.values().iterator().next(), req);
    }
    else {
      // find the endpoint definition matching the input request.
      RemoteTestingEndpointDefinition endpoint = endpoints.get(req.getEndpoint());
      if (endpoint == null) {
        throw new ExternalTestingException(BAD_REQUEST, 400, "No endpoint named " + req.getEndpoint());
      }
      return runAtEndpoint(endpoint, req);
    }
  }

  /**
   * Run the test at a particular endpoint.
   * @param endpoint definition of the endpoint
   * @param req request to be processed.
   * @return results of HTTP request to endpoint.
   */
  protected String runAtEndpoint(RemoteTestingEndpointDefinition endpoint, TestExecutionRequest req) {
    // endpoints can be configured to accept a JSON object or a multipart form post.  We must create the
    // appropriate request body (http entity).
    if (MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(endpoint.getPostStyle()))) {
      HttpHeaders headers = new HttpHeaders();
      if (endpoint.getApiKey() != null) {
        headers.add("X-API-Key", endpoint.getApiKey());
      }
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<TestExecutionRequest> entity = new HttpEntity<>(req, headers);
      return proxyRequestToExternalTestingSite(endpoint.getUrl() + VTP_TESTS_URI + "/run", entity, String.class);
    }
    else {
      // turn our content into a form post...
      HttpHeaders headers = new HttpHeaders();
      if (endpoint.getApiKey() != null) {
        headers.add("X-API-Key", endpoint.getApiKey());
      }
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      MultiValueMap<String, Object> body  = new LinkedMultiValueMap<>();
      body.add("request", req);
      if (req.getTests() != null) {
        req.getTests().stream().flatMap(item -> {
          if (item.getContentItems() != null) {
            return item.getContentItems().stream();
          }
          return Stream.empty();
        }).forEach(item -> body.add(item.getFilename(), item.getContent()));
      }

      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
      return proxyRequestToExternalTestingSite(endpoint.getUrl() + VTP_TESTS_URI + "/run", requestEntity, String.class);
    }
  }

  /**
   * Proxy a get request to a testing endpoint.
   * @param url URL to invoke.
   * @param responseType type of response expected.
   * @param <T> type of response expected
   * @return instance of <T> parsed from the JSON response from endpoint.
   */
  protected <T> T proxyGetRequestToExternalTestingSite(String url, Class responseType) {
    return proxyRequestToExternalTestingSite(url, null, responseType);
  }

  /**
   * Errors from the endpoint could conform to the expected ETSI body or not.
   * Here we try to handle various response body elements.
   * @param statusCode http status code in response.
   * @param o JSON object parsed from the http response body
   * @return Testing error body that should be returned to the caller
   */
  private ExternalTestingException buildTestingException(int statusCode, JsonObject o) {
    ExternalTestingException ex = new ExternalTestingException();
    ex.setCode(statusCode);
    if (o.has("title")) {
      ex.setTitle(o.get("title").getAsString());
    }
    else if (o.has("error")) {
      ex.setTitle(o.get("error").getAsString());
    }
    if (o.has("detail")) {
      ex.setDetail(o.get("detail").getAsString());
    }
    else if (o.has("message")) {
      ex.setDetail(o.get("message").getAsString());
    }
    if (o.has("path")) {
      if (ex.getDetail() == null) {
        ex.setDetail(o.get("path").getAsString());
      }
      else {
        ex.setDetail(ex.getDetail() + " " + o.get("path").getAsString());
      }
    }
    return ex;
  }

  /**
   * Make the actual HTTP post (using Spring RestTemplate) to an endpoint.
   * @param url URL to the endpoint
   * @param request optional request body to send
   * @param responseType expected type
   * @param <T> extended type
   * @return instance of expected type
   */
  @SuppressWarnings("unchecked")
  protected <T> T proxyRequestToExternalTestingSite(String url, Object request, Class responseType) {
    logger.debug("submit request to {} with {} for {}", url, request, responseType.getSimpleName());
    SimpleClientHttpRequestFactory rf =
            (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
    if (rf != null) {
      rf.setReadTimeout(10000);
      rf.setConnectTimeout(10000);
    }
    ResponseEntity<T> re;
    try {
      if (request != null) {
        re = restTemplate.postForEntity(url, request, responseType);
      } else {
        re = restTemplate.getForEntity(url, responseType);
      }
    }
    catch (HttpStatusCodeException ex) {
      logger.warn("Unexpected HTTP Status from endpoint", ex);
      if (ex.getResponseHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)) {
        String s = ex.getResponseBodyAsString();
        logger.warn("endpoint body content is {}", s);
        try {
          JsonObject o = new GsonBuilder().create().fromJson(s, JsonObject.class);
          throw buildTestingException(ex.getRawStatusCode(), o);
        }
        catch (JsonParseException e) {
          logger.warn("unexpected JSON response", e);
          throw new ExternalTestingException(ex.getStatusText(), ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
      }
      else {
        throw new ExternalTestingException(ex.getStatusText(), ex.getStatusCode().value(), ex.getResponseBodyAsString());
      }
    }
    catch (ResourceAccessException ex) {
      logger.error("IO Error Caught accessing endpoint {}", url, ex);
      throw new ExternalTestingException("IO Error at Endpoint", 500, ex.getMessage());
    }
    catch (Exception ex) {
      logger.error("Generic exception caught", ex);
      throw new ExternalTestingException(ex.getMessage(), 500, "Generic Exception");
    }
    logger.debug("http status of {} from external testing entity {}", re.getStatusCodeValue(), url);
    return re.getBody();
  }

  /**
   * Resolve variables in the request calling the built-in variable resolvers.
   * @param item test execution request item to be resolved.
   */
  protected void runVariableResolvers(final TestExecutionRequestItem item) {
    variableResolvers.forEach(vr -> {
      if (vr.resolvesVariablesForRequest(item)) {
        vr.resolve(item);
      }
    });
  }
}
