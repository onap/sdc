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
import org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.ConsolidationDataValidationType;

import java.io.IOException;

import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_CONNECTIVITY_POSITIVE;

/**
 * @author Avrahamg
 * @since August 10, 2016
 */
public class ResourceTranslationContrailV2VmInterfaceImplTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateVMIWithGetResource() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/contrailv2VMinterface/oneNet/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/contrailv2VMinterface/oneNet/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validatePortTemplateConsolidationData(ConsolidationDataValidationType
        .VALIDATE_CONNECTIVITY, TEST_CONNECTIVITY_POSITIVE);
  }

  @Test
  public void testTranslateVMIWithListOfNetworks() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/contrailv2VMinterface/listNet/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrailv2VMinterface/listNet/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validatePortTemplateConsolidationData(ConsolidationDataValidationType
        .VALIDATE_CONNECTIVITY, TEST_CONNECTIVITY_POSITIVE);
  }

  @Test
  public void testMacAddressesValueMapOfListWithOneItem() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/contrailv2VMinterface/macAddressesValueMapOneItem/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrailv2VMinterface/macAddressesValueMapOneItem/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testMacAddressesValueMapOfListWithTwoItems() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/contrailv2VMinterface/macAddressesValueMapTwoItems/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrailv2VMinterface/macAddressesValueMapTwoItems/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testMacAddressesValueMapOfListWithStaticValue() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/contrailv2VMinterface/macAddressStaticValue/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrailv2VMinterface/macAddressStaticValue/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testMacAddressesValueNotList() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/contrailv2VMinterface/macAddressesValueNotAList/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrailv2VMinterface/macAddressesValueNotAList/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testMacAddressesValueInProperForm() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/contrailv2VMinterface/macAddressesValueInProperForm/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/contrailv2VMinterface/macAddressesValueInProperForm/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }
}
