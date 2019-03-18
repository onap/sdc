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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.externaltesting.api.*;
import org.openecomp.core.externaltesting.errors.ExternalTestingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class ExternalTestingManagerImpl implements ExternalTestingManager {

  private Logger logger = LoggerFactory.getLogger(ExternalTestingManagerImpl.class);

  private static final String HTTP_STATUS = "httpStatus";
  private static final String CODE = "code";
  private static final String ERROR = "error";
  private static final String MESSAGE = "message";
  private static final String DETAIL = "detail";
  private static final String PATH = "path";

  private static final String CONFIG_FILE_PROPERTY = "configuration.yaml";
  private static final String CONFIG_SECTION = "externalTestingConfig";

  private static final String VTP_SCENARIOS_URI = "%s/v1/vtp/scenarios";
  private static final String VTP_TESTSUITE_URI = "%s/v1/vtp/scenarios/%s/testsuites";
  private static final String VTP_TESTCASES_URI = "%s/v1/vtp/scenarios/%s/testcases";
  private static final String VTP_TESTCASE_URI = "%s/v1/vtp/scenarios/%s/testsuites/%s/testcases/%s";
  private static final String VTP_EXECUTIONS_URI = "%s/v1/vtp/executions";
  private static final String VTP_EXECUTION_URI = "%s/v1/vtp/executions/%s";

  private static final String INVALIDATE_STATE_ERROR = "Invalid State";
  private static final String NO_ACCESS_CONFIGURATION_DEFINED = "No access configuration defined";

  private TestingAccessConfig accessConfig;
  private Map<String, RemoteTestingEndpointDefinition> endpoints = new HashMap<>();

  private RestTemplate restTemplate;

  private List<VariableResolver> variableResolvers;

  public ExternalTestingManagerImpl(@Autowired(required=false) List<VariableResolver> variableResolvers) {
    this.variableResolvers = variableResolvers;
    // nothing to do at the moment.
    restTemplate = new RestTemplate();
  }

  /**
   * Read the configuration from the yaml file for this bean.  If we get an exception during load,
   * don't force an error starting SDC but log a warning.  Do no warm...
   */
  @PostConstruct
  public void loadConfig() {

    String file = Objects.requireNonNull(System.getProperty(CONFIG_FILE_PROPERTY),
        "Config file location must be specified via system property " + CONFIG_FILE_PROPERTY);
    try {
      Object rawConfig = getExternalTestingAccessConfiguration(file);
      if (rawConfig != null) {
        accessConfig = new ObjectMapper().convertValue(rawConfig, TestingAccessConfig.class);
        accessConfig.getEndpoints()
            .stream()
            .filter(RemoteTestingEndpointDefinition::isEnabled)
            .forEach(e -> endpoints.put(e.getId(), e));
      }
    }
    catch (IOException ex) {
      logger.warn("Unable to initialize external testing configuration.  Add '" + CONFIG_SECTION + "' to configuration.yaml with url value.  Feature will be hobbled with results hardcoded to empty values.", ex);
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
      return "{\"enabled\":false}";
    }
  }

  @Override
  public TestTreeNode getTestCasesAsTree() {
    TestTreeNode root = new TestTreeNode("root", "root");

    // quick out in case of non-configured SDC
    if (accessConfig == null) {
      return root;
    }

    for (RemoteTestingEndpointDefinition ep : accessConfig.getEndpoints()) {
      if (ep.isEnabled()) {
        buildTreeFromEndpoint(ep, root);
      }
    }
    return root;
  }

  private void buildTreeFromEndpoint(RemoteTestingEndpointDefinition ep, TestTreeNode root) {
    try {
      logger.debug("process endpoint {}", ep.getId());
      getScenarios(ep.getId()).stream().filter(s ->
          ((ep.getScenarioFilter() == null) || ep.getScenarioFilterPattern().matcher(s.getName()).matches()))
          .forEach(s -> {
            addScenarioToTree(root, s);
            getTestSuites(ep.getId(), s.getName()).forEach(suite -> addSuiteToTree(root, s, suite));
            getTestCases(ep.getId(), s.getName()).forEach(tc -> {
              try {
                VtpTestCase details = getTestCase(ep.getId(), s.getName(), tc.getTestSuiteName(), tc.getTestCaseName());
                addTestCaseToTree(root, ep.getId(), s.getName(), tc.getTestSuiteName(), details);
              }
              catch (@SuppressWarnings("squid:S1166") ExternalTestingException ex) {
                // Not logging stack trace on purpose.  VTP was throwing exceptions for certain test cases.
                logger.warn("failed to load test case {}", tc.getTestCaseName());
              }
            });
          });
    }
    catch (ExternalTestingException ex) {
      logger.error("unable to contact testing endpoint {}", ep.getId(), ex);
    }
  }

  private Optional<TestTreeNode> findNamedChild(TestTreeNode root, String name) {
    if (root.getChildren() == null) {
      return Optional.empty();
    }
    return root.getChildren().stream().filter(n->n.getName().equals(name)).findFirst();
  }

  /**
   * Find the place in the tree to add the test case.
   * @param root root of the tree.
   * @param endpointName name of the endpoint to assign to the test case.
   * @param scenarioName scenario to add this case to
   * @param testSuiteName suite in the scenario to add this case to
   * @param tc test case to add.
   */
  private void addTestCaseToTree(TestTreeNode root, String endpointName, String scenarioName, String testSuiteName, VtpTestCase tc) {
    // return quickly.
    if (tc == null) {
      return;
    }
    findNamedChild(root, scenarioName)
        .ifPresent(scenarioNode -> findNamedChild(scenarioNode, testSuiteName)
            .ifPresent(suiteNode -> {
              massageTestCaseForUI(tc, endpointName, scenarioName);
              if (suiteNode.getTests() == null) {
                suiteNode.setTests(new ArrayList<>());
              }
              suiteNode.getTests().add(tc);
            }));
  }

  private void massageTestCaseForUI(VtpTestCase testcase, String endpoint, String scenario) {
    testcase.setEndpoint(endpoint);
    // VTP workaround.
    if (testcase.getScenario() == null) {
      testcase.setScenario(scenario);
    }

    // if no inputs, return.
    if (testcase.getInputs() == null) {
      return;
    }

    // to work around a VTP limitation,
    // any inputs that are marked as internal should not be sent to the client.
    testcase.setInputs(testcase.getInputs()
        .stream()
        .filter(input -> (input.getMetadata() == null) ||
            (!input.getMetadata().containsKey("internal")) ||
            !"true".equals(input.getMetadata().get("internal").toString())).collect(Collectors.toList()));
  }

  /**
   * Add the test suite to the tree at the appropriate place if it does not already exist in the tree.
   * @param root root of the tree.
   * @param scenario scenario under which this suite should be placed
   * @param suite test suite to add.
   */
  private void addSuiteToTree(final TestTreeNode root, final VtpNameDescriptionPair scenario, final VtpNameDescriptionPair suite) {
    findNamedChild(root, scenario.getName()).ifPresent(parent -> {
      if (parent.getChildren() == null) {
        parent.setChildren(new ArrayList<>());
      }
      if (parent.getChildren().stream().noneMatch(n -> StringUtils.equals(n.getName(), suite.getName()))) {
        parent.getChildren().add(new TestTreeNode(suite.getName(), suite.getDescription()));
      }
    });
  }

  /**
   * Add the scenario to the tree if it does not already exist.
   * @param root root of the tree.
   * @param s scenario to add.
   */
  private void addScenarioToTree(TestTreeNode root, VtpNameDescriptionPair s) {
    logger.debug("addScenario {} to {} with {}", s.getName(), root.getName(), root.getChildren());
    if (root.getChildren() == null) {
      root.setChildren(new ArrayList<>());
    }
    if (root.getChildren().stream().noneMatch(n->StringUtils.equals(n.getName(),s.getName()))) {
      logger.debug("createScenario {} in {}", s.getName(), root.getName());
      root.getChildren().add(new TestTreeNode(s.getName(), s.getDescription()));
    }
  }

  /**
   * Get the list of endpoints defined to the testing manager.
   * @return list of endpoints or empty list if the manager is not configured.
   */
  public List<VtpNameDescriptionPair> getEndpoints() {
    if (accessConfig != null) {
      return accessConfig.getEndpoints().stream()
          .filter(RemoteTestingEndpointDefinition::isEnabled)
          .map(e -> new VtpNameDescriptionPair(e.getId(), e.getTitle()))
          .collect(Collectors.toList());
    }
    else {
      return new ArrayList<>();
    }
  }

  /**
   * Get the list of scenarios at a given endpoint.
   */
  public List<VtpNameDescriptionPair> getScenarios(final String endpoint) {
    String url = buildEndpointUrl(VTP_SCENARIOS_URI, endpoint, ArrayUtils.EMPTY_STRING_ARRAY);
    ParameterizedTypeReference<List<VtpNameDescriptionPair>> t = new ParameterizedTypeReference<List<VtpNameDescriptionPair>>() {};
    List<VtpNameDescriptionPair> rv = proxyGetRequestToExternalTestingSite(url, t);
    if (rv == null) {
      rv = new ArrayList<>();
    }
    return rv;
  }

  /**
   * Get the list of test suites for an endpoint for the given scenario.
   */
  public List<VtpNameDescriptionPair> getTestSuites(final String endpoint, final String scenario) {
    String url = buildEndpointUrl(VTP_TESTSUITE_URI, endpoint, new String[] {scenario});
    ParameterizedTypeReference<List<VtpNameDescriptionPair>> t = new ParameterizedTypeReference<List<VtpNameDescriptionPair>>() {};
    List<VtpNameDescriptionPair> rv = proxyGetRequestToExternalTestingSite(url, t);
    if (rv == null) {
      rv = new ArrayList<>();
    }
    return rv;
  }

  /**
   * Get the list of test cases under a scenario.  This is the VTP API.  It would
   * seem better to get the list of cases under a test suite but that is not supported.
   */
  @Override
  public List<VtpTestCase> getTestCases(String endpoint, String scenario) {
    String url = buildEndpointUrl(VTP_TESTCASES_URI, endpoint, new String[] {scenario});
    ParameterizedTypeReference<List<VtpTestCase>> t = new ParameterizedTypeReference<List<VtpTestCase>>() {};
    List<VtpTestCase> rv = proxyGetRequestToExternalTestingSite(url, t);
    if (rv == null) {
      rv = new ArrayList<>();
    }
    return rv;
  }

  /**
   * Get a test case definition.
   */
  @Override
  public VtpTestCase getTestCase(String endpoint, String scenario, String testSuite, String testCaseName) {
    String url = buildEndpointUrl(VTP_TESTCASE_URI, endpoint, new String[] {scenario, testSuite, testCaseName});
    ParameterizedTypeReference<VtpTestCase> t = new ParameterizedTypeReference<VtpTestCase>() {};
    return proxyGetRequestToExternalTestingSite(url, t);
  }

  /**
   * Return the results of a previous test execution.
   * @param endpoint endpoint to query
   * @param executionId execution to query.
   * @return execution response from testing endpoint.
   */
  @Override
  public VtpTestExecutionResponse getExecution(String endpoint,String executionId) {
    String url = buildEndpointUrl(VTP_EXECUTION_URI, endpoint, new String[] {executionId});
    ParameterizedTypeReference<VtpTestExecutionResponse> t = new ParameterizedTypeReference<VtpTestExecutionResponse>() {};
    return proxyGetRequestToExternalTestingSite(url, t);
  }

  /**
   * Execute a set of tests at a given endpoint.
   * @param endpointName name of the endpoint
   * @param testsToRun set of tests to run
   * @return list of execution responses.
   */
  private List<VtpTestExecutionResponse> execute(final String endpointName, final List<VtpTestExecutionRequest> testsToRun, String requestId) {
    if (accessConfig == null) {
      throw new ExternalTestingException(INVALIDATE_STATE_ERROR, 500, NO_ACCESS_CONFIGURATION_DEFINED);
    }

    RemoteTestingEndpointDefinition endpoint = accessConfig.getEndpoints().stream()
        .filter(e -> StringUtils.equals(endpointName, e.getId()))
        .findFirst()
        .orElseThrow(() -> new ExternalTestingException("No such endpoint", 500, "No endpoint named " + endpointName + " is defined"));

    // if the endpoint requires an API key, specify it in the headers.
    HttpHeaders headers = new HttpHeaders();
    if (endpoint.getApiKey() != null) {
      headers.add("X-API-Key", endpoint.getApiKey());
    }
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    // build the body.
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    try {
      // remove the endpoint from the test request since that is a FE/BE attribute
      // add the execution profile configured for the endpoint.
      testsToRun.forEach(t -> {
        t.setEndpoint(null);
        t.setProfile(t.getScenario()); // VTP wants a profile.  Use the scenario name.
      });

      body.add("executions", new ObjectMapper().writeValueAsString(testsToRun));
    }
    catch (Exception ex) {
      logger.error("exception converting tests to string", ex);
      VtpTestExecutionResponse err = new VtpTestExecutionResponse();
      err.setHttpStatus(500);
      err.setCode("500");
      err.setMessage("Execution failed due to " + ex.getMessage());
      return Collections.singletonList(err);
    }

    for(VtpTestExecutionRequest test: testsToRun) {
      runVariableResolvers(test, body);
    }


    // form and send request.
    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
    String url = buildEndpointUrl(VTP_EXECUTIONS_URI, endpointName, ArrayUtils.EMPTY_STRING_ARRAY);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
    if (requestId != null) {
      builder = builder.queryParam("requestId", requestId);
    }
    ParameterizedTypeReference<List<VtpTestExecutionResponse>> t = new ParameterizedTypeReference<List<VtpTestExecutionResponse>>() {};
    try {
      return proxyRequestToExternalTestingSite(builder.toUriString(), requestEntity, t);
    }
    catch (ExternalTestingException ex) {
      logger.error("exception caught invoking endpoint {}", endpointName, ex);
      VtpTestExecutionResponse err = new VtpTestExecutionResponse();
      err.setHttpStatus(ex.getCode());
      err.setCode(""+ex.getCode());
      err.setMessage(ex.getTitle() + ": " + ex.getDetail());
      return Collections.singletonList(err);
    }
  }

  /**
   * Execute tests splitting them across endpoints and collecting the results.
   * @param testsToRun list of tests to be executed.
   * @return collection of result objects.
   */
  @Override
  public List<VtpTestExecutionResponse> execute(final List<VtpTestExecutionRequest> testsToRun, String requestId) {
    if (accessConfig == null) {
      throw new ExternalTestingException(INVALIDATE_STATE_ERROR, 500, NO_ACCESS_CONFIGURATION_DEFINED);
    }

    // partition the requests by endpoint.
    Map<String, List<VtpTestExecutionRequest>> partitions =
        testsToRun.stream().collect(Collectors.groupingBy(VtpTestExecutionRequest::getEndpoint));

    // process each group and collect the results.
    return partitions.entrySet().stream()
        .flatMap(e -> execute(e.getKey(), e.getValue(), requestId).stream())
        .collect(Collectors.toList());
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
   * Load the onboarding yaml config file.
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
   * Return URL with endpoint url as prefix.
   * @param format format string.
   * @param endpointName endpoint to address
   * @param args args for format.
   * @return qualified url.
   */
  private String buildEndpointUrl(String format, String endpointName, String[] args) {
    if (accessConfig != null) {
      RemoteTestingEndpointDefinition ep = endpoints.values().stream()
          .filter(e -> e.getId().equals(endpointName))
          .findFirst()
          .orElseThrow(() -> new ExternalTestingException("No such endpoint", 500, "No endpoint named " + endpointName + " is defined")
          );

      Object[] newArgs = ArrayUtils.add(args, 0, ep.getUrl());
      return String.format(format, newArgs);
    }
    throw new ExternalTestingException(INVALIDATE_STATE_ERROR, 500, NO_ACCESS_CONFIGURATION_DEFINED);
  }

  /**
   * Proxy a get request to a testing endpoint.
   * @param url URL to invoke.
   * @param responseType type of response expected.
   * @param <T> type of response expected
   * @return instance of <T> parsed from the JSON response from endpoint.
   */
  private <T> T proxyGetRequestToExternalTestingSite(String url, ParameterizedTypeReference<T> responseType) {
    return proxyRequestToExternalTestingSite(url, null, responseType);
  }

  /**
   * Make the actual HTTP post (using Spring RestTemplate) to an endpoint.
   * @param url URL to the endpoint
   * @param request optional request body to send
   * @param responseType expected type
   * @param <T> extended type
   * @return instance of expected type
   */
  private <R,T> T proxyRequestToExternalTestingSite(String url, HttpEntity<R> request, ParameterizedTypeReference<T> responseType) {
    if (request != null) {
      logger.debug("POST request to {} with {} for {}", url, request, responseType.getType().getTypeName());
    }
    else {
      logger.debug("GET request to {} for {}", url, responseType.getType().getTypeName());
    }
    SimpleClientHttpRequestFactory rf =
        (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
    if (rf != null) {
      rf.setReadTimeout(10000);
      rf.setConnectTimeout(10000);
    }
    ResponseEntity<T> re;
    try {
      if (request != null) {
        re = restTemplate.exchange(url, HttpMethod.POST, request, responseType);
      } else {
        re = restTemplate.exchange(url, HttpMethod.GET, null, responseType);
      }
    }
    catch (HttpStatusCodeException ex) {
      // make my own exception out of this.
      logger.warn("Unexpected HTTP Status from endpoint {}", ex.getRawStatusCode());
      if ((ex.getResponseHeaders().getContentType() != null) &&
          ((ex.getResponseHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)) ||
              (ex.getResponseHeaders().getContentType().isCompatibleWith(MediaType.parseMediaType("application/problem+json"))))) {
        String s = ex.getResponseBodyAsString();
        logger.warn("endpoint body content is {}", s);
        try {
          JsonObject o = new GsonBuilder().create().fromJson(s, JsonObject.class);
          throw buildTestingException(ex.getRawStatusCode(), o);
        }
        catch (JsonParseException e) {
          logger.warn("unexpected JSON response", e);
          throw new ExternalTestingException(ex.getStatusText(), ex.getStatusCode().value(), ex.getResponseBodyAsString(), ex);
        }
      }
      else {
        throw new ExternalTestingException(ex.getStatusText(), ex.getStatusCode().value(), ex.getResponseBodyAsString(), ex);
      }
    }
    catch (ResourceAccessException ex) {
      throw new ExternalTestingException("IO Error at Endpoint", 500, ex.getMessage(), ex);
    }
    catch (Exception ex) {
      throw new ExternalTestingException(ex.getMessage(), 500, "Generic Exception", ex);
    }
    if (re != null) {
      logger.debug("http status of {} from external testing entity {}", re.getStatusCodeValue(), url);
      return re.getBody();
    }
    else {
      logger.error("null response from endpoint");
      return null;
    }
  }

  /**
   * Errors from the endpoint could conform to the expected ETSI body or not.
   * Here we try to handle various response body elements.
   * @param statusCode http status code in response.
   * @param o JSON object parsed from the http response body
   * @return Testing error body that should be returned to the caller
   */
  private ExternalTestingException buildTestingException(int statusCode, JsonObject o) {
    String code = null;
    String message = null;

    if (o.has(CODE)) {
      code = o.get(CODE).getAsString();
    }
    else if (o.has(ERROR)) {
      code = o.get(ERROR).getAsString();
    }
    else {
      if (o.has(HTTP_STATUS)) {
        code = o.get(HTTP_STATUS).getAsJsonPrimitive().getAsString();
      }
    }
    if (o.has(MESSAGE)) {
      if (!o.get(MESSAGE).isJsonNull()) {
        message = o.get(MESSAGE).getAsString();
      }
    }
    else if (o.has(DETAIL)) {
      message = o.get(DETAIL).getAsString();
    }
    if (o.has(PATH)) {
      if (message == null) {
        message = o.get(PATH).getAsString();
      }
      else {
        message = message + " " + o.get(PATH).getAsString();
      }
    }
    return new ExternalTestingException(code, statusCode, message);
  }

  /**
   * Resolve variables in the request calling the built-in variable resolvers.
   * @param item test execution request item to be resolved.
   */
  private void runVariableResolvers(final VtpTestExecutionRequest item, final MultiValueMap<String, Object> body) {
    variableResolvers.forEach(vr -> {
      if (vr.resolvesVariablesForRequest(item)) {
        vr.resolve(item, body);
      }
    });
  }
}