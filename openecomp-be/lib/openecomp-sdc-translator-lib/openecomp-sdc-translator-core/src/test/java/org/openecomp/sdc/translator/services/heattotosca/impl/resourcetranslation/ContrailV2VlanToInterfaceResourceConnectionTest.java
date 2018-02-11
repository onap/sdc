/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.sdc.common.togglz.ToggleableFeature;
import org.togglz.testing.TestFeatureManager;
import org.togglz.testing.TestFeatureManagerProvider;

import java.io.IOException;

public class ContrailV2VlanToInterfaceResourceConnectionTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateVlanToInterfaceNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/subInterfaceToInterfaceConnection/nested/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/subInterfaceToInterfaceConnection/nested/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateVlanToNetMultiNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/subInterfaceToInterfaceConnection/nestedMultiLevels/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/subInterfaceToInterfaceConnection/nestedMultiLevels" +
            "/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

}
