/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 */

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author SHIRIA
 * @since December 06, 2016.
 */
public class ContrailV2VlanToInterfaceResourceConnectionTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  //todo - remove the ignore once we will support VMI as sub port
  @Ignore
  @Test
  public void testTranslateVlanToInterfaceNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/subInterfaceToInterfaceConnection/nested/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/subInterfaceToInterfaceConnection/nested/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  //todo - remove the ignore once we will support VMI as sub port
  @Ignore
  @Test
  public void testTranslateVlanToNetMultiNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/subInterfaceToInterfaceConnection/nestedMultiLevels/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/subInterfaceToInterfaceConnection/nestedMultiLevels/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  //todo - remove the ignore once we will support VMI as sub port
  @Ignore
  @Test
  public void testTranslateVTSBCVendorHeat() throws Exception {
    inputFilesPath =
            "/mock/services/heattotosca/subInterfaceToInterfaceConnection/vTSBC/inputfiles";
    outputFilesPath =
            "/mock/services/heattotosca/subInterfaceToInterfaceConnection/vTSBC/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

}
