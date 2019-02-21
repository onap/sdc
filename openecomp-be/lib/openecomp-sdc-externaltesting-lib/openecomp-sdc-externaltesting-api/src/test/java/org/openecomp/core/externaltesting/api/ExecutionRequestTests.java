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

import java.util.ArrayList;

public class ExecutionRequestTests {

  private String json = "{\n" +
      "  \"endpoint\": \"vtp\",\n" +
      "  \"tests\": [\n" +
      "    {\n" +
      "      \"testId\": \"certquery\",\n" +
      "      \"parameterValues\": [\n" +
      "        {\n" +
      "          \"id\": \"vspId\",\n" +
      "          \"value\": \"ID\"\n" +
      "        },\n" +
      "        {\n" +
      "          \"id\": \"vspVersion\",\n" +
      "          \"value\": \"1.0\"\n" +
      "        }\n" +
      "      ]\n" +
      "    }\n" +
      "  ]\n" +
      "}";


  @Test
  public void testRequest() throws Exception {
    TestExecutionRequest req = new ObjectMapper().readValue(json, TestExecutionRequest.class);
    Assert.assertEquals("Endpoint must match", "vtp", req.getEndpoint());
    Assert.assertEquals("Must contain 1 test request", 1, req.getTests().size());

    if (!req.getTests().isEmpty()) {
      TestExecutionRequestItem item = req.getTests().get(0);
      Assert.assertEquals("Item test id must match", "certquery", item.getTestId());
      Assert.assertEquals("Item must contain two parameters", 2, item.getParameterValues().size());
      Assert.assertEquals("First parameter must be vspID", "vspId", item.getParameterValues().get(0).getId());
      Assert.assertEquals("First parameter must be ID", "ID", item.getParameterValues().get(0).getValue());
      Assert.assertEquals("Second parameter must be vspVersion", "vspVersion", item.getParameterValues().get(1).getId());
      Assert.assertEquals("Second parameter must be 1.0", "1.0", item.getParameterValues().get(1).getValue());
    }
  }

  @Test
  public void testRequestContentItems() throws Exception {
    TestExecutionRequest req = new ObjectMapper().readValue(json, TestExecutionRequest.class);
    req.getTests().get(0).setContentItems(new ArrayList<>());
    CsarMetadataContentItem ci = new CsarMetadataContentItem();
    ci.setFilename("hello");
    ci.setContent("world".getBytes());
    req.getTests().get(0).getContentItems().add(ci);

    Assert.assertEquals("content item name", "hello", req.getTests().get(0).getContentItems().get(0).getFilename());
    Assert.assertEquals("content item value", "world", new String(req.getTests().get(0).getContentItems().get(0).getContent()));
  }
}
