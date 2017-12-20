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

import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_VOLUME_NEGATIVE;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_VOLUME_POSITIVE;


public class ResourceTranslationCinderVolumeAttachmentImplTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateAllResourcesInOneFile() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/vol_attach/volume_and_attach_one_file/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/vol_attach/volume_and_attach_one_file/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData(ConsolidationDataValidationType.VALIDATE_VOLUME,
        TEST_VOLUME_POSITIVE);
  }

  @Test
  public void testVolFileIsNestedInMainHeatFile() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/vol_attach/volume_file_nested_in_main_file_in_manifest/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/vol_attach/volume_file_nested_in_main_file_in_manifest/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData(ConsolidationDataValidationType.VALIDATE_VOLUME,
        TEST_VOLUME_POSITIVE);
  }

  @Test
  public void testVolFileAsDataOfNested() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/vol_attach/nested_with_inner_vol/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/vol_attach/nested_with_inner_vol/out";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData(ConsolidationDataValidationType.VALIDATE_VOLUME,
        TEST_VOLUME_POSITIVE);
  }

  @Test
  public void testVolFileIsParallelToMainHeatFile() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/vol_attach/volume_file_parallel_to_main_file/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/vol_attach/volume_file_parallel_to_main_file/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData(ConsolidationDataValidationType.VALIDATE_VOLUME,
        TEST_VOLUME_NEGATIVE);
  }
}
