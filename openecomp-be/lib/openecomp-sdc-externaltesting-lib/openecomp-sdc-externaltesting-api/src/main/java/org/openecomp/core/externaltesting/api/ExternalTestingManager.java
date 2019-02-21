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


import org.openecomp.core.externaltesting.errors.ExternalTestingException;

public interface ExternalTestingManager {

  /**
   * Return the list of tests.
   * @return test definitions.
   * @throws ExternalTestingException when the external testing resource reports an error or cannot be reached.
   */
  TestSet listTests();


  /**
   * Proxy the request to run a test.   Certain tests require processing here in the SDC backend.
   * Certain tests require look-up of CSAR or HEAT metadata for processing by the remote testing service.
   * @param req body of JSON from the client parsed as a test execution request
   * @return results from the remote testing service
   * @throws ExternalTestingException if something goes wrong invoking the external testing resource
   */
  String run(TestExecutionRequest req);


  /**
   * Return the full test definition including parameters available to the caller for a given test.
   * @param testId identifier of the test to query the parameters for.
   * @return returns whatever testing endpoint provides.
   * @throws ExternalTestingException if something goes wrong invoking the external testing resource
   */
  Test getTestDefinition(String testId);


  /**
   * Return the configuration of this feature that we want to
   * expose to the client.  Treated as a JSON blob for flexibility.
   */
  String getConfig();

  /**
   * Return the full test definition including parameters available to the caller for a given test.
   * @param endpointId what endpoint to get the data from.
   * @param testId identifier of the test to query the parameters for.
   * @return returns whatever testing endpoint provides.
   * @throws ExternalTestingException if something goes wrong invoking the external testing resource
   */
  Test getTestDefinition(String endpointId, String testId);
}
