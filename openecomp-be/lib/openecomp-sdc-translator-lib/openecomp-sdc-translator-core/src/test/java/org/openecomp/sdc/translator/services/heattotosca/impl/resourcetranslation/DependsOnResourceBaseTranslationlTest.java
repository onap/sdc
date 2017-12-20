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

import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_DEPENDS_ON_INVALID_DEPENDENCY_CANDIDATE;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_DEPENDS_ON_MULTIPLE_COMPUTE;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_DEPENDS_ON_NODES_CONNECTED_IN;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_DEPENDS_ON_NODES_CONNECTED_IN_AND_OUT;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_DEPENDS_ON_NODES_CONNECTED_OUT;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_DEPENDS_ON_NODE_TEMPLATE_TRANSLATION_ORDER_INVARIANCE;
import static org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants.TEST_DEPENDS_ON_NO_DEPENDENCY;


public class DependsOnResourceBaseTranslationlTest extends BaseResourceTranslationTest {
  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslate() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/baseResourceTranslation/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/baseResourceTranslation/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testDependsOnConsolidationNodesConnectedOut() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/nodesConnectedOut/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/nodesConnectedOut/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData(ConsolidationDataValidationType.VALIDATE_DEPENDS_ON,
        TEST_DEPENDS_ON_NODES_CONNECTED_OUT);
  }

  @Test
  public void testDependsOnConsolidationNodesConnectedIn() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/nodesConnectedIn/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/nodesConnectedIn/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData(ConsolidationDataValidationType.VALIDATE_DEPENDS_ON,
        TEST_DEPENDS_ON_NODES_CONNECTED_IN);
  }

  @Test
  public void testDependsOnConsolidationNodesConnectedInAndOut() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/nodesConnectedInOut/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/nodesConnectedInOut/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData(ConsolidationDataValidationType.VALIDATE_DEPENDS_ON,
        TEST_DEPENDS_ON_NODES_CONNECTED_IN_AND_OUT);
  }

  @Test
  public void testDependsOnConsolidationNoDependency() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/noDependency/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/noDependency/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData(ConsolidationDataValidationType.VALIDATE_DEPENDS_ON,
        TEST_DEPENDS_ON_NO_DEPENDENCY);
  }

  @Test
  public void testDependsOnConsolidationInvalidDependencyCandidate() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/noDependency/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/noDependency/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData(ConsolidationDataValidationType.VALIDATE_DEPENDS_ON,
        TEST_DEPENDS_ON_INVALID_DEPENDENCY_CANDIDATE);
  }

  @Test
  public void testDependsOnConsolidationMultipleCompute() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/multiplecompute/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource/multiplecompute/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData(ConsolidationDataValidationType.VALIDATE_DEPENDS_ON,
        TEST_DEPENDS_ON_MULTIPLE_COMPUTE);
  }

  @Test
  public void testDependsOnNodeTemplateTranslationOrderInvariance() throws Exception {
    //Tests that the resource dependency is independent of the order of resource translation
    inputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/translationorderinvariance/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/translationorderinvariance/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateComputeTemplateConsolidationData(ConsolidationDataValidationType.VALIDATE_DEPENDS_ON,
        TEST_DEPENDS_ON_NODE_TEMPLATE_TRANSLATION_ORDER_INVARIANCE);
  }

  @Test
  public void testDependsOnRemoveDependencyForInvalidCandidates() throws Exception {
    //Tests the deletion of dependencies of Compute->Compute, Compute->Port, Port->Port,
    // Port->Compute from the original tosca data model
    inputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/removeInvalidDependencyFromTosca/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/buildconsolidationdata/dependsonresource" +
        "/removeInvalidDependencyFromTosca/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }
}
