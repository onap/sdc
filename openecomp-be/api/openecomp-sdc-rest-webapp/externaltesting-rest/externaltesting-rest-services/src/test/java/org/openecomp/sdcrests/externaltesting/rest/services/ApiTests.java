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

package org.openecomp.sdcrests.externaltesting.rest.services;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.externaltesting.api.ExternalTestingManager;
import org.openecomp.core.externaltesting.api.TestExecutionRequest;
import org.openecomp.core.externaltesting.api.TestSet;
import org.openecomp.core.externaltesting.errors.ExternalTestingException;

import javax.ws.rs.core.Response;

public class ApiTests {

  @Mock
  private ExternalTestingManager testingManager;

  @Test
  public void testApi() {
    MockitoAnnotations.initMocks(this);

    ExternalTestingImpl testing = new ExternalTestingImpl(testingManager);
    Response r1 = testing.getConfig();
    testing.getTestDefinition("bogus");
    testing.getTestDefinition("ep", "bogus");

    TestExecutionRequest req = new TestExecutionRequest();
    testing.run(req);

    testing.listTests();

    ExternalTestingManager m = new ExternalTestingManager() {
      @Override
      public TestSet listTests() {
        throw new ExternalTestingException("failed", 500, "details");
      }

      @Override
      public String run(TestExecutionRequest req) {
        throw new ExternalTestingException("failed", 500, "details");
      }

      @Override
      public org.openecomp.core.externaltesting.api.Test getTestDefinition(String testId) {
        throw new ExternalTestingException("SSsfailed", 500, "details");
      }

      @Override
      public String getConfig() {
        throw new ExternalTestingException("failed", 500, "details");
      }

      @Override
      public org.openecomp.core.externaltesting.api.Test getTestDefinition(String endpointId, String testId) {
        throw new ExternalTestingException("failed", 500, "details");
      }
    };

    ExternalTestingImpl testingF = new ExternalTestingImpl(m);
    testingF.listTests();
    testingF.getConfig();
    testingF.getTestDefinition("bogus");
    testingF.getTestDefinition("ep", "bogus");

    TestExecutionRequest reqF = new TestExecutionRequest();
    testingF.run(reqF);

  }
}
