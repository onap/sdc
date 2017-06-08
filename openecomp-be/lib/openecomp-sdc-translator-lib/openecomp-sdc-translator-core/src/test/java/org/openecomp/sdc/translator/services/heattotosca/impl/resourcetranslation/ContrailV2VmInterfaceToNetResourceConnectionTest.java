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


public class ContrailV2VmInterfaceToNetResourceConnectionTest extends BaseResourceTranslationTest {
  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateVMIToNetNestedConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/VMInterfaceToNettworkConnection/nested/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/VMInterfaceToNettworkConnection/nested/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  //todo - remove the ignore once we will support VMI as sub port
  @Ignore
  @Test
  public void testTranslateVlanToNetNestedConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/VlanToNetConnection/nested/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/VlanToNetConnection/nested/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateVMIToNetMultiNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/VMInterfaceToNettworkConnection/nestedMultiLevels/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/VMInterfaceToNettworkConnection/nestedMultiLevels/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  //todo - remove the ignore once we will support VMI as sub port
  @Ignore
  @Test
  public void testTranslateVlanToNetMultiNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/VlanToNetConnection/nestedMultiLevels/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/VlanToNetConnection/nestedMultiLevels/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateVMIToSharedNetNestedConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/VMInterfaceToNettworkConnection/shared/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/VMInterfaceToNettworkConnection/shared/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  //todo - remove the ignore once we will support VMI as sub port
  @Ignore
  @Test
  public void testTranslateVlanToSharedNetNestedConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/VlanToNetConnection/shared/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/VlanToNetConnection/shared/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateVMIToNetSharedMultiNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/VMInterfaceToNettworkConnection/sharedNestedMultiLevels/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/VMInterfaceToNettworkConnection/sharedNestedMultiLevels/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateVMIToNetSharedAddOnConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/VMInterfaceToNettworkConnection/sharedAddOn/inputfiles";
    outputFilesPath =
            "/mock/services/heattotosca/VMInterfaceToNettworkConnection/sharedAddOn/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  //todo - remove the ignore once we will support VMI as sub port
  @Ignore
  @Test
  public void testTranslateVlanToNetSharedAddOnConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/VlanToNetConnection/sharedAddOn/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/VlanToNetConnection/sharedAddOn/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateVMIToNetArrayParameterConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/VMInterfaceToNettworkConnection/nestedArrayParam/inputfiles";
    outputFilesPath =
            "/mock/services/heattotosca/VMInterfaceToNettworkConnection/nestedArrayParam/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

}
