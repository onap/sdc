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

package org.openecomp.core.externaltesting.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class ExecutionRequestTest {

  @Test
  public void testTestCase() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    VtpTestCase req = mapper.readValue(new File("src/test/data/testcase.json"), VtpTestCase.class);

    Assert.assertEquals("Scenario must match", "compliance", req.getScenario());
    Assert.assertEquals("Suite name must match", "compliancetests", req.getTestSuiteName());
    Assert.assertEquals("Test case name must match", "sriov", req.getTestCaseName());
    Assert.assertEquals("Description must match", "SR-IOV Test", req.getDescription());
    Assert.assertEquals("Author must match", "Jim", req.getAuthor());
    Assert.assertEquals("Endpoint must match", "vtp", req.getEndpoint());
    Assert.assertEquals("Test must contain two inputs", 3, req.getInputs().size());
    Assert.assertEquals("Test must contain one outputs", 1, req.getOutputs().size());

    VtpTestCaseInput input1 = req.getInputs().get(0);
    Assert.assertEquals("Name match", "vspId", input1.getName());
    Assert.assertEquals("Description match", "VSP ID", input1.getDescription());
    Assert.assertEquals("Input type match", "text", input1.getType());
    Assert.assertEquals("Input default match", "", input1.getDefaultValue());
    Assert.assertFalse("Input optional match", input1.getIsOptional());

    VtpTestCaseOutput output1 = req.getOutputs().get(0);
    Assert.assertEquals("Name match", "something", output1.getName());
    Assert.assertEquals("Description match", "is produced", output1.getDescription());
    Assert.assertEquals("Output type match", "integer", output1.getType());


    Map<String,Object> meta = input1.getMetadata();
    Assert.assertEquals("Metadata count", 3, meta.size());

    VtpTestCase req2 = mapper.readValue(new File("src/test/data/testcase.json"), VtpTestCase.class);

    Assert.assertEquals("test equality", req, req2);

  }

  @Test
  public void testExecutionRequest() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    VtpTestExecutionRequest req = mapper.readValue(new File("src/test/data/executionrequest.json"), VtpTestExecutionRequest.class);
    Assert.assertEquals("compliance", req.getScenario());
    Assert.assertEquals("compliance", req.getProfile());
    Assert.assertEquals("sriov", req.getTestCaseName());
    Assert.assertEquals("compliancetests", req.getTestSuiteName());
    Assert.assertEquals("repository", req.getEndpoint());

    Assert.assertEquals(3, req.getParameters().size());
  }

  @Test
  public void testExecutionResponse() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    VtpTestExecutionResponse rsp = mapper.readValue(new File("src/test/data/priorexecution.json"), VtpTestExecutionResponse.class);
    Assert.assertEquals("compliance", rsp.getScenario());
    Assert.assertEquals("computeflavors", rsp.getTestCaseName());
    Assert.assertEquals("compliancetests", rsp.getTestSuiteName());
    Assert.assertTrue(UUID.fromString(rsp.getExecutionId()).getLeastSignificantBits() != 0);
    Assert.assertEquals("parameters", 6, rsp.getParameters().size());
    Assert.assertNotNull(rsp.getResults());
    Assert.assertEquals("COMPLETED", rsp.getStatus());
    Assert.assertNotNull(rsp.getStartTime());
    Assert.assertNotNull(rsp.getEndTime());

    rsp = mapper.readValue(new File("src/test/data/failedexecution.json"), VtpTestExecutionResponse.class);
    Assert.assertEquals("F-1131", rsp.getCode());
    Assert.assertEquals("Failure reason", rsp.getMessage());
    Assert.assertEquals(500, rsp.getHttpStatus().intValue());
  }

  @Test
  public void testTreeConstructor() {
    // test constructor.
    TestTreeNode tree = new TestTreeNode("root", "Root");
    Assert.assertEquals("root", tree.getName());
    Assert.assertEquals("Root", tree.getDescription());
  }

  @Test
  public void testTree() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    TestTreeNode tree = mapper.readValue(new File("src/test/data/testtree.json"), TestTreeNode.class);

    Assert.assertEquals(2, tree.getChildren().size());
    Assert.assertEquals(0, tree.getTests().size());
  }
}
