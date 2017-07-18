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


public class NovaToVolResourceConnectionTest extends BaseResourceTranslationTest {
  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testNovaToVolumeConnectionSharedAddOnConnection() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/novatovolumeconnection/sharedAddOn/inputfiles";
    outputFilesPath =
            "/mock/services/heattotosca/novatovolumeconnection/sharedAddOn/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testNovaToVolumeConnectionMultiNotCreatedIfVolPorpertyInVolAttacheIsNotAReferenceToVolume()
      throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/multinotconnected/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/multinotconnected/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testNovaToVolumeConnectionNestedNotCreatedIfVolPorpertyInVolAttacheIsNotAReferenceToVolume()
      throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/nestednotconnected/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/nestednotconnected/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateNodeTemplateIdInNestedConsolidationData();
  }

  @Test
  public void testTranslateNovaToVolumeNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/nestedconnection/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/nestedconnection/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateNodeTemplateIdInNestedConsolidationData();
  }

  @Test
  public void testTranslateNovaToVolumeSharedNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/sharednestedconnection/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/sharednestedconnection/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateNodeTemplateIdInNestedConsolidationData();
  }

  @Test
  public void testTranslateNovaToVolumeSharedNestedNotCreatedIfVolPorpertyInVolAttacheIsNotAReferenceToVolume()
      throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/sharednestednotconnected/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/sharednestednotconnected/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateNodeTemplateIdInNestedConsolidationData();
  }

  @Test
  public void testTranslateNovaToVolumeInnerNestedConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/innernestedconnection/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/innernestedconnection/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateNodeTemplateIdInNestedConsolidationData();
  }

  @Test
  public void testTranslateNovaToVolumeNestedMultiLevelConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/nestedMultiLevels/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/nestedMultiLevels/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateNodeTemplateIdInNestedConsolidationData();
  }

  @Test
  public void testTranslateNovaToVolumeSharedNestedMultiLevelConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/sharedNestedMultiLevels/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/sharedNestedMultiLevels/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateNodeTemplateIdInNestedConsolidationData();
  }

  @Test
  public void testTranslateNovaToVolumeInnerHeatVolumeNestedMultiLevelConnection()
      throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/innerHeatVolNestedMultiLevel/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/novatovolumeconnection/innerHeatVolNestedMultiLevel/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateNodeTemplateIdInNestedConsolidationData();
  }


}
