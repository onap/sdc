/*
 * Copyright © 2018 European Support Limited
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

package org.onap.sdc.backend.ci.tests.execute.globaltypes;

import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.api.Urls;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.backend.ci.tests.datatypes.http.HttpHeaderEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.HttpRequest;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class InterfaceLifecycleTypesTest extends ComponentBaseTest {

  protected Config config = Config.instance();
  protected HttpRequest httpRequest = new HttpRequest();
  protected Map<String, String> headersMap = new HashMap<>();
  @Rule
  public static final TestName testName = new TestName();

  @BeforeMethod
  public void init() {
    headersMap.put(HttpHeaderEnum.USER_ID.getValue(), new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER).getUserId());
  }

  @Test
  public void testGetAllInterfaceLifecycleTypes() throws Exception {
    final String standardType = "tosca.interfaces.node.lifecycle.standard";
    final String nfvType = "tosca.interfaces.nfv.vnf.lifecycle.nfv";
    String url = String.format(Urls.GET_All_INTERFACE_LIFECYCLE_TYPES, config.getCatalogBeHost(), config.getCatalogBePort());
    RestResponse restResponse = httpRequest.httpSendGet(url, headersMap);
    AssertJUnit.assertTrue(restResponse.getErrorCode() == 200);
    JSONObject jsonObject = (JSONObject) JSONValue.parse(restResponse.getResponse());
    AssertJUnit.assertTrue(jsonObject.containsKey(standardType));
    AssertJUnit.assertTrue(jsonObject.containsKey(nfvType));
  }
}
