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

package com.att.sdc.translator.services.heattotosca.impl.resourcetranslation;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ResourceTranslationValetGroupAssignmentImplTest
    extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateSimpleOnlyNova() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/simpleOnlyNova/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/simpleOnlyNova/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData();
  }

  @Test
  public void testTranslateGroupNameParameter() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/groupNameParameter/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/groupNameParameter/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData();
  }

  @Test
  public void testTranslateComplexNovaAndGroup() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/complexNovaAndGroup/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/complexNovaAndGroup/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData();
  }

  @Test
  public void testTranslateComplexOnlyGroup() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/complexOnlyGroup/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/complexOnlyGroup/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData();
  }

  @Test
  public void testTranslateComplexTwoGroups() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/complexTwoGroups/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/complexTwoGroups/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData();
  }

  @Test
  public void testInvalidGroupIsNotTranslatedAndNotAddedAsMemberOfGroup() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/invalidGroupAssignment/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/invalidGroupAssignment/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData();
  }

  @Test
  public void testSimpleNovaAndNotSupportedResource() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/simpleNovaAndNotSupportedResource/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/simpleNovaAndNotSupportedResource/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData();
  }

  @Test
  public void testSimpleOnlyNotSupportedResources() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/simpleOnlyNotSupportedResources/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/simpleOnlyNotSupportedResources/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData();
  }

  @Test
  public void testSimpleOnlySingleNova() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/simpleOnlySingleNova/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/simpleOnlySingleNova/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData();
  }

  @Test
  public void testConfigNoConnection() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/notconnected/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/notconnected/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData();
  }

  @Test
  public void testDynamicGroup() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/dynamicGroup/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/attValetGroupAssignment/dynamicGroup/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData();
  }


}
