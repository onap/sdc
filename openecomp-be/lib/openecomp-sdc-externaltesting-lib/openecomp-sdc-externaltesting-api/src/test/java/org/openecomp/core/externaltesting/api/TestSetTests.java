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

public class TestSetTests {

  @Test
  public void testResponse() throws Exception {

    TestSet testSet = new ObjectMapper().readValue(new File("src/test/data/testset.json"), TestSet.class);
    Assert.assertEquals("root id should be all", "ALL", testSet.getId());
    Assert.assertEquals("root title should be All", "ALL", testSet.getTitle());
    Assert.assertEquals("All should contain the right number of children", 2, testSet.getSets().size());
    Assert.assertNull("All should contain no tests", testSet.getTests());

    TestSet subSet = testSet.getSets().get(0);
    Assert.assertEquals("First sub set id", "certification", subSet.getId());
    Assert.assertEquals("First sub set should have 1 test", 1, subSet.getTests().size());

    org.openecomp.core.externaltesting.api.Test firstTest = subSet.getTests().get(0);
    Assert.assertEquals("First test id should match", "certquery", firstTest.getId());
    Assert.assertEquals("First test title should match", "VSP Certifications", firstTest.getTitle());
    Assert.assertNotNull("Parameters should be non null", firstTest.getParameters());

    TestParameter tp = firstTest.getParameters().get(0);
    Assert.assertEquals("first param id", "vspId", tp.getId());
    Assert.assertEquals("first param label", "VSP ID", tp.getLabel());
    Assert.assertEquals("first param type", "text", tp.getInputType());
    Assert.assertEquals("first param maxLength", new Integer(36), tp.getMaxLength());
    Assert.assertEquals("first param minLength", new Integer(1), tp.getMinLength());
    Assert.assertFalse("first param disabled", tp.isDisabled());
    Assert.assertTrue("first param required", tp.isRequired());
  }
}