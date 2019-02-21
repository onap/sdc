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
import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.model.TestTimedOutException;

import java.util.UUID;

public class ExecutionResponseTests {

  @Test
  public void testResponse() throws Exception {
    String json = "{\n" +
        "  \"id\": \"12752020-325b-4fda-9c36-d378ccd8928e\",\n" +
        "  \"initiator\": \"TBD\",\n" +
        "  \"status\": \"Completed\",\n" +
        "  \"results\": [\n" +
        "    {\n" +
        "      \"test\": {\n" +
        "        \"id\": \"certquery\",\n" +
        "        \"title\": \"VSP Certifications\"\n" +
        "      },\n" +
        "      \"status\": \"Success\",\n" +
        "      \"details\": \"{\\\"k1\\\":\\\"v1\\\",\\\"k2\\\":\\\"v2\\\",\\\"a1\\\":[{\\\"a1v1\\\":10}]}\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"total\": 1,\n" +
        "  \"failures\": 0,\n" +
        "  \"successes\": 1,\n" +
        "  \"startDateTime\": \"2019-01-29T20:49:19.483+0000\",\n" +
        "  \"completionDateTime\": \"2019-01-29T20:49:19.484+0000\"\n" +
        "}";

    ObjectMapper mapper = new ObjectMapper();
    TestExecutionResponse rsp = mapper.readValue(json, TestExecutionResponse.class);
    Assert.assertTrue("ID must be a UUID", UUID.fromString(rsp.getId()).getLeastSignificantBits() != 0);
    Assert.assertEquals("Status is Completed", TestExecutionStatus.Completed.name(), rsp.getStatus().name());
    Assert.assertEquals("Total", 1, rsp.getTotal());
    Assert.assertEquals("Failures", 0, rsp.getFailures());
    Assert.assertEquals("Successes", 1, rsp.getSuccesses());
    Assert.assertTrue("start date is valid", rsp.getStartDateTime().getTime() > 0);
    Assert.assertTrue("end date is valid", rsp.getCompletionDateTime().getTime() > 0);
    Assert.assertEquals("initiator is set", "TBD", rsp.getInitiator());

    TestExecutionResponseItem firstResult = rsp.getResults().get(0);
    Assert.assertEquals("Result item id", "certquery", firstResult.getTest().getId());
    Assert.assertEquals("Result item title", "VSP Certifications", firstResult.getTest().getTitle());
    Assert.assertEquals("Result item status", TestExecutionStatus.Success, firstResult.getStatus());
    Assert.assertNotNull("Result item details", firstResult.getDetails());


  }
}
