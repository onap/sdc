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
import org.junit.Test;

import java.io.IOException;


public class ResourceTranslationContrailServiceInstanceImplTest
    extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateOneServiceInstance() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/oneServiceInstance/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/oneServiceInstance/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateDiffServiceTemplate() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/diffServiceTemplate/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/diffServiceTemplate/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateSharedNetworkMulti() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/sharedNetworkMulti/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/sharedNetworkMulti/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateSameServiceTemplate() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/sameServiceTemplate/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/sameServiceTemplate/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }


  @Test
  public void testTranslateConnectToNetworkMultiNested() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/connectToNetworkMultiNested/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/connectToNetworkMultiNested/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateConnectToSharedNetworkMultiNested() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/connectToNetworkSharedMultiNested/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrail2serviceinstance/connectToNetworkSharedMultiNested/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

}
