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
 * ============LICENSE_END=========================================================
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdcrests.externaltesting.rest.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.openecomp.core.externaltesting.api.ClientConfiguration;
import org.openecomp.core.externaltesting.api.ExternalTestingManager;
import org.openecomp.core.externaltesting.api.RemoteTestingEndpointDefinition;
import org.openecomp.core.externaltesting.api.TestErrorBody;
import org.openecomp.core.externaltesting.api.VtpTestExecutionOutput;
import org.openecomp.core.externaltesting.api.VtpTestExecutionRequest;
import org.openecomp.core.externaltesting.api.VtpTestExecutionResponse;
import org.openecomp.core.externaltesting.errors.ExternalTestingException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdcrests.externaltesting.rest.ExternalTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@SuppressWarnings("unused")
@Named
@Service("externaltesting")
@Scope(value = "prototype")
public class ExternalTestingImpl implements ExternalTesting {

    private final ExternalTestingManager testingManager;
    private static final int REQUEST_ID_LENGTH = 8;
    private static final String TESTING_INTERNAL_ERROR = "SDC-TEST-005";
    private final VendorSoftwareProductManager vendorSoftwareProductManager;

    private static final Logger logger = LoggerFactory.getLogger(ExternalTestingImpl.class);

    @Autowired
    public ExternalTestingImpl(@Qualifier("testingManager") ExternalTestingManager testingManager) {
        this.testingManager = testingManager;
        this.vendorSoftwareProductManager = VspManagerFactory.getInstance().createInterface();
    }

    public ExternalTestingImpl(ExternalTestingManager testingManager,
                               VendorSoftwareProductManager vendorSoftwareProductManager) {
        this.testingManager = testingManager;
        this.vendorSoftwareProductManager = vendorSoftwareProductManager;
    }

    /**
     * Return the configuration of the feature to the client.
     *
     * @return JSON response content.
     */
    @Override
    public ResponseEntity getConfig() {
        try {
            return ResponseEntity.ok(testingManager.getConfig());
        } catch (ExternalTestingException e) {
            return convertTestingException(e);
        }
    }

    /**
     * To enable automated functional testing, allow
     * a put for the client configuration.
     *
     * @return JSON response content.
     */
    @Override
    public ResponseEntity setConfig(ClientConfiguration config) {
        try {
            return ResponseEntity.ok(testingManager.setConfig(config));
        } catch (ExternalTestingException e) {
            return convertTestingException(e);
        }
    }


    /**
     * Return the test tree structure created by the testing manager.
     *
     * @return JSON response content.
     */
    @Override
    public ResponseEntity getTestCasesAsTree() {
        try {
            return ResponseEntity.ok(testingManager.getTestCasesAsTree());
        } catch (ExternalTestingException e) {
            return convertTestingException(e);
        }
    }

    @Override
    public ResponseEntity getEndpoints() {
        try {
            return ResponseEntity.ok(testingManager.getEndpoints());
        } catch (ExternalTestingException e) {
            return convertTestingException(e);
        }
    }

    /**
     * To enable automated functional testing, allow a put of the endpoints.
     *
     * @return JSON response content.
     */
    @Override
    public ResponseEntity setEndpoints(List<RemoteTestingEndpointDefinition> endpoints) {
        try {
            return ResponseEntity.ok(testingManager.setEndpoints(endpoints));
        } catch (ExternalTestingException e) {
            return convertTestingException(e);
        }
    }

    @Override
    public ResponseEntity getScenarios(String endpoint) {
        try {
            return ResponseEntity.ok(testingManager.getScenarios(endpoint));
        } catch (ExternalTestingException e) {
            return convertTestingException(e);
        }

    }

    @Override
    public ResponseEntity getTestsuites(String endpoint, String scenario) {
        try {
            return ResponseEntity.ok(testingManager.getTestSuites(endpoint, scenario));
        } catch (ExternalTestingException e) {
            return convertTestingException(e);
        }
    }

    @Override
    public ResponseEntity getTestcases(String endpoint, String scenario) {
        try {
            return ResponseEntity.ok(testingManager.getTestCases(endpoint, scenario));
        } catch (ExternalTestingException e) {
            return convertTestingException(e);
        }
    }

    @Override
    public ResponseEntity getTestcase(String endpoint, String scenario, String testsuite, String testcase) {
        try {
            return ResponseEntity.ok(testingManager.getTestCase(endpoint, scenario, testsuite, testcase));
        } catch (ExternalTestingException e) {
            return convertTestingException(e);
        }
    }

    @Override
    public ResponseEntity execute(String vspId, String vspVersionId, String requestId, List<MultipartFile> files,
                                  String testDataString) {
        try {
            List<VtpTestExecutionRequest> req = getVtpTestExecutionRequestObj(testDataString);
            Map<String, byte[]> fileMap = getFileMap(files);
            List<VtpTestExecutionResponse> vtpTestExecutionResponses =
                    testingManager.execute(req, vspId, vspVersionId, requestId, fileMap);
            return ResponseEntity.status(HttpStatus.OK.value()).body(vtpTestExecutionResponses);
        } catch (ExternalTestingException e) {
            return convertTestingException(e);
        }

    }

    @Override
    public ResponseEntity getValidationResult(String requestId, List<String> endPoints) {
        try {
            List<VtpTestExecutionResponse> resultsFromVtp = new ArrayList<>();
            for (String endPoint : endPoints) {
                List<VtpTestExecutionOutput> vtpTestExecutionOutput =
                        testingManager.getExecutionIds(endPoint, requestId);
                List<String> execIds = vtpTestExecutionOutput.stream().map(VtpTestExecutionOutput::getExecutionId)
                        .collect(Collectors.toList());
                List<VtpTestExecutionResponse> resultFromVtp = getVtpResultbyExecutionId(execIds, endPoint);
                resultsFromVtp.addAll(resultFromVtp);
            }
            return ResponseEntity.status(HttpStatus.OK.value()).body(resultsFromVtp);
        } catch (ExternalTestingException e) {
            return convertTestingException(e);
        }
    }

    private List<VtpTestExecutionRequest> getVtpTestExecutionRequestObj(String testDataString) {
        try {
            return new ObjectMapper().configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true).reader()
                    .forType(new TypeReference<List<VtpTestExecutionRequest>>() { }).readValue(testDataString);
        } catch (IOException e) {
            throw new ExternalTestingException(TESTING_INTERNAL_ERROR, 500, e.getMessage(), e);

        }
    }

    private List<VtpTestExecutionResponse> getVtpResultbyExecutionId(List<String> executionIds, String endPoint) {
        List<VtpTestExecutionResponse> vtpTestExecutionResponses = new ArrayList<>();
        executionIds.stream().forEach(executionId -> {
            VtpTestExecutionResponse executionResult = testingManager.getExecution(endPoint, executionId);
            vtpTestExecutionResponses.add(executionResult);
        });
        return vtpTestExecutionResponses;
    }


    @Override
    public ResponseEntity getExecution(String endpoint, String executionId) {
        try {
            return ResponseEntity.ok(testingManager.getExecution(endpoint, executionId));
        } catch (ExternalTestingException e) {
            return convertTestingException(e);
        }
    }

    private Map<String, byte[]> getFileMap(List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            return files.stream().collect(
                Collectors.toMap(
                    MultipartFile::getOriginalFilename,   // Use original filename as key
                    file -> {
                        try {
                            return file.getBytes();  // Get file content as byte[]
                        } catch (IOException e) {
                            throw new ExternalTestingException(TESTING_INTERNAL_ERROR, 500, e.getMessage(), e);
                        }
                    }
                )
            );
        }
        return null;
    }


    private ResponseEntity convertTestingException(ExternalTestingException e) {
        if (logger.isErrorEnabled()) {
            logger.error("testing exception {} {} {}", e.getMessageCode(), e.getHttpStatus(), e.getDetail(), e);
        }
        TestErrorBody body = new TestErrorBody(e.getMessageCode(), e.getHttpStatus(), e.getDetail());
        return ResponseEntity.status(e.getHttpStatus()).body(body);
    }
}
