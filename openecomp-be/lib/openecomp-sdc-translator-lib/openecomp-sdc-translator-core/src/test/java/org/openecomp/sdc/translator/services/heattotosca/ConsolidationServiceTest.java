/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.translator.services.heattotosca;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.SubInterfaceConsolidationDataTestInfo;
import org.openecomp.sdc.translator.TestUtils;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionMode;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.GetAttrFuncData;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ConsolidationServiceTest {


  private static final String CM_01_PORT_TYPE = "cm01_port";
  private static final String SM_01_PORT_TYPE = "sm01_port";
  @Spy
  private TranslationContext translationContext = new TranslationContext();
  private static String mainST = "MainServiceTemplate.yaml";

  @Mock
  private UnifiedCompositionService unifiedCompositionServiceMock;

  @Captor
  private ArgumentCaptor<List<UnifiedCompositionData>> unifiedModelListArg;
  @Captor
  private ArgumentCaptor<UnifiedCompositionMode> unifiedCompositionModeArg;

  @Spy
  @InjectMocks
  private ConsolidationService consolidationService;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testConsolidationValidPreCondition() throws IOException, URISyntaxException {

    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel(
        "/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/valid_pre_condition/",
        null,
        null);

    TestUtils.initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName,
        consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
            null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1", CM_01_PORT_TYPE,
            null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.ScalingInstances);

    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testConsolidationFalsePreConditionOneComputeNode() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/one_compute_node/",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
            null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1", SM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testConsolidationFalsePreConditionMoreThanOnePortPerType() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/more_than_one_port/",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_2"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);


    TestUtils.initPortConsolidationData(mainST, consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testConsolidationFalsePreConditionDifferentPortTypesBetweenComputes()
      throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/different_port_types",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1", SM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);

  }

  @Test
  public void testConsolidationResultTrueWithMoreThanTwoCompute() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/three_compute_valid",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1", SM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);

    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testConsolidationResultFalseWithMoreThanTwoComputeOneIsDiff() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/three_compute_valid",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1", SM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testConsolidationResultFalseForTwoTypesOfComputeWithOneInstanceEach()
      throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName1 = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    String computeNodeTypeName2 = "org.openecomp.resource.vfc.nodes.heat.cmaui";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/three_compute_valid",
            null, null);

    TestUtils.initComputeNodeTypeInConsolidationData
        (mainST, computeNodeTypeName1, consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData
        (mainST, computeNodeTypeName2, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName1, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName2, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1", SM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);

    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.SingleSubstitution,
            UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(2, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testConsolidationValidForTwoSimilarComputeAndFalseForSingleCompute()
      throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName1 = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    String computeNodeTypeName2 = "org.openecomp.resource.vfc.nodes.heat.cmaui";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/three_compute_two_similar_one_diff",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName1, consolidationData);
    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName2, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName1, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName1, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName2, "server_ps03",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1", SM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays.asList(UnifiedCompositionMode.ScalingInstances,
            UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(2, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testValidComputeAndPortConsolidation() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_compute_valid",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1", SM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testFalseComputeConsolidationForTwoSimilarImageNamesAndOneDiff() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName1 = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    String computeNodeTypeName2 = "org.openecomp.resource.vfc.nodes.heat.pd_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/one_image_name_diff",
            null, null);

    TestUtils.initComputeNodeTypeInConsolidationData(
        mainST, computeNodeTypeName1, consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(
        mainST, computeNodeTypeName2, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName1, "server_ps01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName1, "server_ps02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName1, "server_ps03",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName2, "server_pd01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName2, "server_pd02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName2, "server_pd03",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1", SM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Arrays
            .asList(UnifiedCompositionMode.CatalogInstance, UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(2, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testFalseComputeConsolidationOneImageNameMissing() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.pd_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/one_image_missing",
            null, null);

    TestUtils.initComputeNodeTypeInConsolidationData(
        mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_pd01",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_pd02",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_pd03",
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1", SM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testFalseComputeConsolidationForTwoSimilarFlavorNamesAndOneDiff() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/one_flavor_name_diff",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "sm01_port_1", SM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testFalsePortConsolidationForOneDiffFixedIpsValue() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/one_exCP_naming_diff",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testFalsePortConsolidationForOneDiffExpcNaming() {

  }

  @Test
  public void testFalsePortConsolidationForTwoPortsWithFixedIpsAndOneWithout() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/one_port_without_fixed_ips",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testFalsePortConsolidationForTwoPortsWithAddressPairsAndOneWithout()
      throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/one_port_without_allowed_address_pairs",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);
    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testFalsePortConsolidationForTwoPortsWithMacAddressAndOneWithout()
      throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/one_port_without_mac_address",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);
    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        null, null, null, null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testValidPortConsolidationForPortsWithNoneOfTheCheckedProperties()
      throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/ports_with_none_of_the_properties",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", null, null, null, null, null, null,
        portTypeToIdList, consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2", CM_01_PORT_TYPE,
        Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3", CM_01_PORT_TYPE,
        Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testComputeRelationsSimilarBetweenComputeNodes() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_compute_with_same_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("group_id1"), null,
        null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
        Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2", CM_01_PORT_TYPE,
        Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3", CM_01_PORT_TYPE,
        Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);


    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testNodesInRelationsDiffBetweenThreeComputes() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id1"), null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_2"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id1"), null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE, null, null, null, null, null,
        consolidationData);


    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testNodesOutRelationsDiffBetweenThreeComputes() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id1"), null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume_1"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE, null, null, null, null, null,
        consolidationData);


    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testVolumeRelationsDiffBetweenThreeComputes() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_compute_valid",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id1"), null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume_1"),
        Collections.singletonList("group_id1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE, null, null, null, null, null,
        consolidationData);


    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testGroupRelationsDiffBetweenThreeComputes() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_compute_valid",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"), null,
        null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_2"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE, null, null, null, null, null,
        consolidationData);


    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testPortConsolidationDataRelationsSimilar() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"), null,
        null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testNodesInRelationsDiffBetweenThreePortConsolidationDatas() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"), null,
        null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_2"), Collections.singletonList("net_id_1"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testNodesOutRelationsDiffBetweenThreePortConsolidationDatas() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"), null,
        null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_2"),
        Collections.singletonList("group_id_1"), null,
        null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testGroupIdsRelationsDiffBetweenThreePortConsolidationDatas() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"), null,
        null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_3"), null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testValidConsolidationForComputesWithValidGetAttr() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("node_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("node_2"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("node_3"), null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testInvalidConsolidationForComputesWithGetAttrForEachOther() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("server_ps02"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("node_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("node_2"), null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"), null, null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testValidConsolidationForPortsWithValidGetAttr() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"), null,
        null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        null, null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    List<String> portNodeTemplateIds =
        Arrays.asList("cm01_port_1", "cm01_port_2", "cm01_port_3");
    List<String> toUpdatePortTypeIds =
            Arrays.asList("cm01_port", "cm01_port", "cm01_port");
    List<List<String>> nodesConnectedInIds =
        Arrays.asList(Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"));
    List<List<String>> nodesConnectedOutIds =
        Arrays.asList(Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"));
    List<List<String>> getAttrInIds =
        Arrays.asList(Collections.singletonList("get_attr_1"),
            Collections.singletonList("get_attr_2"),
            Collections.singletonList("get_attr_3"));
    List<List<String>> groupIds =
        Arrays.asList(Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("group_id_1", "group_id_2"));
    List<List<Pair<String, GetAttrFuncData>>> getAttrOut =
        Arrays.asList(null, null, null);

    TestUtils.updateMultiplePortConsolidationDatas(
        mainST, portNodeTemplateIds, toUpdatePortTypeIds, nodesConnectedInIds, nodesConnectedOutIds, groupIds,
        getAttrInIds, getAttrOut, consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testInvalidConsolidationForPortsWithGetAttrInForEachOther() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation" +
                "/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("node_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("node_2"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("node_3"), null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    List<String> portNodeTemplateIds =
        Arrays.asList("cm01_port_1", "cm01_port_2", "cm01_port_3");
    List<String> toUpdatePortTypeIds =
            Arrays.asList("cm01_port", "cm01_port", "cm01_port");
    List<List<String>> nodesConnectedInIds =
        Arrays.asList(Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"));
    List<List<String>> nodesConnectedOutIds =
        Arrays.asList(Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"));
    List<List<String>> getAttrInIds =
        Arrays.asList(Collections.singletonList("get_attr_1"),
            Collections.singletonList("get_attr_2"),
            Collections.singletonList("cm01_port_1"));
    List<List<String>> groupIds =
        Arrays.asList(Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("group_id_1", "group_id_2"));
    List<List<Pair<String, GetAttrFuncData>>> getAttrOut =
        Arrays.asList(null, null, null);

    TestUtils.updateMultiplePortConsolidationDatas(
        mainST, portNodeTemplateIds, toUpdatePortTypeIds, nodesConnectedInIds, nodesConnectedOutIds, groupIds,
        getAttrInIds, getAttrOut, consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.CatalogInstance);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testValidConsolidationForComputesWithSamePortTypesPointingByGetAttrIn() throws
      IOException, URISyntaxException {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_2"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_3"), null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    List<String> portNodeTemplateIds =
        Arrays.asList("cm01_port_1", "cm01_port_2", "cm01_port_3");
    List<String> toUpdatePortTypeIds =
            Arrays.asList("cm01_port", "cm01_port", "cm01_port");
    List<List<String>> nodesConnectedInIds =
        Arrays.asList(Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"));
    List<List<String>> nodesConnectedOutIds =
        Arrays.asList(Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"));
    List<List<String>> getAttrInIds =
        Arrays.asList(Collections.singletonList("get_attr_1"),
            Collections.singletonList("get_attr_2"),
            Collections.singletonList("get_attr_3"));
    List<List<String>> groupIds =
        Arrays.asList(Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("group_id_1", "group_id_2"));
    List<List<Pair<String, GetAttrFuncData>>> getAttrOut =
        Arrays.asList(null, null, null);

    TestUtils.updateMultiplePortConsolidationDatas(
        mainST, portNodeTemplateIds, toUpdatePortTypeIds, nodesConnectedInIds, nodesConnectedOutIds, groupIds,
        getAttrInIds, getAttrOut, consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testValidGetAttrOutFromComputes() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    GetAttrFuncData getAttrFuncData = new GetAttrFuncData("name", "c1_name");
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_1"),
        Collections.singletonList(
            new ImmutablePair<>("cm01_port_1", getAttrFuncData)),
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_1"),
        Collections.singletonList(
            new ImmutablePair<>("cm01_port_1", getAttrFuncData)),
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_1"),
        Collections.singletonList(
            new ImmutablePair<>("cm01_port_1", getAttrFuncData)),
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"),
        Collections.singletonList("sm01_port_2"), null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"),
        Collections.singletonList("sm01_port_1"), null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_3", CM_01_PORT_TYPE,
            Collections.singletonList("node_in_1"), Collections.singletonList("net_id_1"),
        Arrays.asList("group_id_1", "group_id_2"),
        Collections.singletonList("rm01_port_1"), null,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testInValidGetAttrOutFromComputesPortTypeDifferent() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    GetAttrFuncData getAttrFuncData = new GetAttrFuncData("name", "c1_name");
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_1"),
        Collections.singletonList(
            new ImmutablePair<>("cm01_port_1", getAttrFuncData)),
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_1"),
        Collections.singletonList(
            new ImmutablePair<>("cm01_port_1", getAttrFuncData)),
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_1"),
        Collections.singletonList(
            new ImmutablePair<>("sm01_port_1", getAttrFuncData)),
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);

    List<String> portNodeTemplateIds =
        Arrays.asList("cm01_port_1", "cm01_port_2", "cm01_port_3", "sm01_port_1",
            "sm01_port_2",
            "sm01_port_3");
    List<String> toUpdatePortTypeIds =
            Arrays.asList("cm01_port", "cm01_port", "cm01_port", "sm01_port", "sm01_port", "sm01_port");
    List<List<String>> nodesConnectedInIds =
        Arrays.asList(Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"), Collections.singletonList
                ("node_in_1"), Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"));
    List<List<String>> nodesConnectedOutIds =
        Arrays.asList(Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"), Collections.singletonList
                ("node_out_1"), Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"));
    List<List<String>> getAttrInIds =
        Arrays.asList(Collections.singletonList("get_attr_1"),
            Collections.singletonList("get_attr_2"), Collections.singletonList
                ("get_attr_3"), Collections.singletonList("get_attr_1"),
            Collections.singletonList("get_attr_2"),
            Collections.singletonList("get_attr_3"));
    List<List<String>> groupIds =
        Arrays.asList(Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1",
                "group_id_2"), Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1",
                "group_id_2"), Arrays.asList("group_id_1", "group_id_2"));
    List<List<Pair<String, GetAttrFuncData>>> getAttrOut =
        Arrays.asList(null, null, null, null,
            null, null);

    TestUtils.updateMultiplePortConsolidationDatas(mainST, portNodeTemplateIds, toUpdatePortTypeIds,
        nodesConnectedInIds, nodesConnectedOutIds, groupIds, getAttrInIds, getAttrOut,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testValidGetAttrOutFromPorts() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_1"), null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);

    GetAttrFuncData getAttrFuncData = new GetAttrFuncData("name", "s1_name");
    List<String> portNodeTemplateIds =
        Arrays.asList("cm01_port_1", "cm01_port_2", "cm01_port_3", "sm01_port_1",
            "sm01_port_2",
            "sm01_port_3");
    List<String> toUpdatePortTypeIds =
            Arrays.asList("cm01_port", "cm01_port", "cm01_port", "sm01_port", "sm01_port", "sm01_port");
    List<List<String>> nodesConnectedInIds =
        Arrays.asList(Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"), Collections.singletonList
                ("node_in_1"), Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"));
    List<List<String>> nodesConnectedOutIds =
        Arrays.asList(Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"), Collections.singletonList
                ("node_out_1"), Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"));
    List<List<String>> getAttrInIds =
        Arrays.asList(null, null, null, null, null, null);
    List<List<String>> groupIds =
        Arrays.asList(Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1",
                "group_id_2"), Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1",
                "group_id_2"), Arrays.asList("group_id_1", "group_id_2"));
    List<List<Pair<String, GetAttrFuncData>>> getAttrOut = Arrays.asList(
        Collections.singletonList(
            new ImmutablePair<>("sm01_port_1", getAttrFuncData)),
        Collections.singletonList(
            new ImmutablePair<>("sm01_port_2", getAttrFuncData)),
        Collections.singletonList(
            new ImmutablePair<>("sm01_port_3", getAttrFuncData)),
        Collections.singletonList(
            new ImmutablePair<>("cm01_port_1", getAttrFuncData)),
        Collections.singletonList(
            new ImmutablePair<>("cm01_port_2", getAttrFuncData)),
        Collections.singletonList(
            new ImmutablePair<>("cm01_port_3", getAttrFuncData)));

    TestUtils.updateMultiplePortConsolidationDatas(mainST, portNodeTemplateIds, toUpdatePortTypeIds,
        nodesConnectedInIds, nodesConnectedOutIds, groupIds, getAttrInIds, getAttrOut,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.ScalingInstances);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testInvalidGetAttrOutFromPorts() throws IOException, URISyntaxException  {
    ConsolidationData consolidationData = new ConsolidationData();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/computeportconsolidation/three_ports_similar_relations",
            null, null);

    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_1"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_1"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps01", Collections.singletonList("cm01_port_1"),
        Collections.singletonList
            ("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_2"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_2"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps02", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_1"), null,
        portTypeToIdList,
        consolidationData);

    portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(CM_01_PORT_TYPE, "cm01_port_3"));
    portTypeToIdList.add(new ImmutablePair<>(SM_01_PORT_TYPE, "sm01_port_3"));
    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, "server_ps03", Collections.singletonList("cm01_port_1"),
        Collections.singletonList("cmaui_volume"), Collections.singletonList("cmaui_volume"),
        Collections.singletonList("group_id_1"),
        Collections.singletonList("cm01_port_1"), null,
        portTypeToIdList,
        consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);

    GetAttrFuncData getAttrFuncData = new GetAttrFuncData("name", "s1_name");
    List<String> portNodeTemplateIds =
        Arrays.asList("cm01_port_1", "cm01_port_2", "cm01_port_3", "sm01_port_1",
            "sm01_port_2",
            "sm01_port_3");
    List<String> toUpdatePortTypeIds =
            Arrays.asList("cm01_port", "cm01_port", "cm01_port", "sm01_port", "sm01_port", "sm01_port");
    List<List<String>> nodesConnectedInIds =
        Arrays.asList(Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"), Collections.singletonList
                ("node_in_1"), Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"),
            Collections.singletonList("node_in_1"));
    List<List<String>> nodesConnectedOutIds =
        Arrays.asList(Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"), Collections.singletonList
                ("node_out_1"), Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"),
            Collections.singletonList("node_out_1"));
    List<List<String>> getAttrInIds =
        Arrays.asList(null, null, null, null, null, null);
    List<List<String>> groupIds =
        Arrays.asList(Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1", "group_id_2"), Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1", "group_id_2"),
            Arrays.asList("group_id_1",
                "group_id_2"), Arrays.asList("group_id_1", "group_id_2"));
    List<List<Pair<String, GetAttrFuncData>>> getAttrOut = Arrays.asList(
        Collections.singletonList(
            new ImmutablePair<>("server_ps01", getAttrFuncData)),
        Collections.singletonList(
            new ImmutablePair<>("server_ps01", getAttrFuncData)),
        Collections.singletonList(
            new ImmutablePair<>("sm01_port_3", getAttrFuncData)),
        Collections.singletonList(
            new ImmutablePair<>("cm01_port_1", getAttrFuncData)),
        Collections.singletonList(
            new ImmutablePair<>("cm01_port_2", getAttrFuncData)),
        Collections.singletonList(
            new ImmutablePair<>("cm01_port_3", getAttrFuncData)));

    TestUtils.updateMultiplePortConsolidationDatas(mainST, portNodeTemplateIds, toUpdatePortTypeIds,
        nodesConnectedInIds, nodesConnectedOutIds, groupIds, getAttrInIds, getAttrOut,
        consolidationData);

    translationContext.setConsolidationData(consolidationData);
    List<UnifiedCompositionMode> expectedUnifiedModes =
        Collections.singletonList(UnifiedCompositionMode.SingleSubstitution);
    verifyMainServiceTemplateConsolidation(1, expectedUnifiedModes, toscaServiceModel);
  }

  @Test
  public void testSubstitutionConsolidationPreConditionTrue() throws IOException, URISyntaxException  {
    translationContext = new TranslationContext();
    String mainSTName = "MainServiceTemplate.yaml";
    String nestedServiceTemplateName = "nested-pcm_v0.1ServiceTemplate.yaml";

    ConsolidationData consolidationData = new ConsolidationData();
    TestUtils.updateNestedConsolidationData(mainSTName, Collections.singletonList("server_pcm_001"),
        consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat.pcm_server", "server_pcm_001", consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes" +
            ".heat.pcm_server", "pcm_server", consolidationData);

    translationContext.setConsolidationData(consolidationData);

    addMockServiceTemplateToContext(mainSTName, "Main");
    addMockServiceTemplateToContext(nestedServiceTemplateName, "nested-pcm_v0.1");

    verifySubstitutionServiceTemplateConsolidation
        (1, translationContext.getTranslatedServiceTemplates().get(mainSTName),
            translationContext.getTranslatedServiceTemplates().get(nestedServiceTemplateName),
            UnifiedCompositionMode.NestedSingleCompute);
  }

  @Test
  public void testSubstitutionConsolidationPreConditionFalseMoreThanOneComputeType()
      throws IOException, URISyntaxException  {
    translationContext = new TranslationContext();
    String mainSTName = "MainServiceTemplate.yaml";
    String nestedServiceTemplateName = "nested-pcm_v0.1ServiceTemplate.yaml";

    ConsolidationData consolidationData = new ConsolidationData();
    TestUtils.updateNestedConsolidationData(mainSTName, Collections.singletonList("server_pcm_001"),
        consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes" +
            ".heat.pcm_server", "server_pcm_001", consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);

    TestUtils.updateNestedConsolidationData(mainSTName, Collections.singletonList("server_oam_001"),
        consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".oam_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat.oam_server", "server_oam_001", consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".oam_server", consolidationData);

    translationContext.setConsolidationData(consolidationData);

    addMockServiceTemplateToContext(mainSTName, "Main");
    addMockServiceTemplateToContext(nestedServiceTemplateName, "nested-pcm_v0.1");

    verifySubstitutionServiceTemplateConsolidation
        (1, translationContext.getTranslatedServiceTemplates().get(mainSTName),
            translationContext.getTranslatedServiceTemplates().get
                (nestedServiceTemplateName), UnifiedCompositionMode.SingleSubstitution);
  }

  @Test
  public void testSubstitutionConsolidationPreConditionFalseMoreThanOneComputeOfSameType()
      throws IOException, URISyntaxException  {
    translationContext = new TranslationContext();
    String mainSTName = "MainServiceTemplate.yaml";
    String nestedServiceTemplateName = "nested-pcm_v0.1ServiceTemplate.yaml";

    ConsolidationData consolidationData = new ConsolidationData();
    TestUtils.updateNestedConsolidationData(mainSTName, Collections.singletonList("server_pcm_001"),
        consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes" +
            ".heat.pcm_server", "pcm_server_1", consolidationData);

    TestUtils
        .updateNestedConsolidationData(nestedServiceTemplateName,
            Collections.singletonList("server_pcm_002"),
            consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes" +
            ".heat.pcm_server", "pcm_server_2", consolidationData);

    translationContext.setConsolidationData(consolidationData);

    addMockServiceTemplateToContext(mainSTName, "Main");
    addMockServiceTemplateToContext(nestedServiceTemplateName, "nested-pcm_v0.1");

    Mockito.doNothing().when(consolidationService).serviceTemplateConsolidation
        (translationContext.getTranslatedServiceTemplates().get(nestedServiceTemplateName),
            translationContext);
    verifySubstitutionServiceTemplateConsolidation
        (1, translationContext.getTranslatedServiceTemplates().get(mainSTName),
            translationContext.getTranslatedServiceTemplates().get(nestedServiceTemplateName),
            UnifiedCompositionMode.SingleSubstitution);
  }

  @Test
  public void testSubstitutionConsolidationPreConditionFalseNestedInsideNested() {
    translationContext = new TranslationContext();
    String mainSTName = "MainServiceTemplate.yaml";
    String nestedServiceTemplateName = "nested-pcm_v0.1ServiceTemplate.yaml";

    ConsolidationData consolidationData = new ConsolidationData();
    TestUtils.updateNestedConsolidationData(mainSTName, Collections.singletonList("server_pcm_001"),
        consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(mainSTName,
        "org.openecomp.resource.vfc.nodes.heat.pcm_server", "server_pcm_001", consolidationData);
    TestUtils.initComputeNodeTypeInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes.heat" +
            ".pcm_server", consolidationData);
    TestUtils.initComputeNodeTemplateIdInConsolidationData(nestedServiceTemplateName,
        "org.openecomp.resource.vfc.nodes" +
            ".heat.pcm_server", "pcm_server", consolidationData);

    TestUtils
        .updateNestedConsolidationData(nestedServiceTemplateName,
            Collections.singletonList("nested_resource"),
            consolidationData);

    translationContext.setConsolidationData(consolidationData);

    addMockServiceTemplateToContext(mainSTName, "Main");
    addMockServiceTemplateToContext(nestedServiceTemplateName, "nested-pcm_v0.1");

    verifySubstitutionServiceTemplateConsolidation
        (0, translationContext.getTranslatedServiceTemplates().get(mainSTName),
            translationContext.getTranslatedServiceTemplates().get(nestedServiceTemplateName),
            UnifiedCompositionMode.SingleSubstitution);
  }

  @Test
  public void testConsolidationPreConditionFalseDiffSubportTypes() throws IOException, URISyntaxException  {
    translationContext = new TranslationContext();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/different_subinterface_types",
            null, null);

    ConsolidationData consolidationData = new ConsolidationData();
    generateComputeAndPortConsolidationData(computeNodeTypeName, consolidationData);

    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_1", "nested1.yaml", 2,
                "role_1", "interface_1", null, null, null, null, consolidationData));
    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_2", "nested2.yaml", 2,
                "role_2", "interface_2", null, null, null, null, consolidationData));
    testConsolidation(1, toscaServiceModel, consolidationData, Collections.singletonList(UnifiedCompositionMode.SingleSubstitution));
  }

  private void generateComputeAndPortConsolidationData(String computeNodeTypeName,
                                                       ConsolidationData consolidationData) {
    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    updateComputeConsolidationData("server_ps01", computeNodeTypeName, CM_01_PORT_TYPE,
        "cm01_port_1", consolidationData);

    updateComputeConsolidationData("server_ps02", computeNodeTypeName, CM_01_PORT_TYPE,
        "cm01_port_2", consolidationData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
            null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2", CM_01_PORT_TYPE,
            null, null, null, null, null,
        consolidationData);
  }

  @Test
  public void testConsolidationPreConditionFalseDiffSubportNumber() throws IOException, URISyntaxException  {
    translationContext = new TranslationContext();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/different_subinterface_types",
            null, null);

    ConsolidationData consolidationData = new ConsolidationData();
    generateComputeAndPortConsolidationData(computeNodeTypeName, consolidationData);

    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_1", "nested1.yaml", 2,
                "role_1", "interface_1", null, null, null, null, consolidationData));
    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_2", "nested1.yaml", 2,
                "role_2", "interface_2", null, null, null, null, consolidationData));
    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_2", "nested1.yaml", 2,
                "role_3", "interface_3", null, null, null, null, consolidationData));

    testConsolidation(1, toscaServiceModel,
        consolidationData, Collections.singletonList(UnifiedCompositionMode.SingleSubstitution));
  }

  @Test
  public void testConsolidationRuleFalseDifferentCountInResourceGroup() throws IOException, URISyntaxException  {
    translationContext = new TranslationContext();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/different_subinterface_types",
            null, null);

    ConsolidationData consolidationData = new ConsolidationData();
    generateComputeAndPortConsolidationData(computeNodeTypeName, consolidationData);

    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_1", "nested1.yaml", 2,
                "role_1", "interface_1", null, null, null, null, consolidationData));
    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_2", "nested1.yaml", 3,
                "role_1", "interface_2", null, null, null, null, consolidationData));

    testConsolidation(1, toscaServiceModel,
        consolidationData, Collections.singletonList(UnifiedCompositionMode.CatalogInstance));
  }

  @Test
  public void testConsolidationRuleFalseDifferentNodesConnectedInToResourceGroup()
      throws IOException, URISyntaxException  {
    translationContext = new TranslationContext();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/different_subinterface_types",
            null, null);

    ConsolidationData consolidationData = new ConsolidationData();
    generateComputeAndPortConsolidationData(computeNodeTypeName, consolidationData);

    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_1", "nested1.yaml", 2,
                "role_1", "interface_1", Collections.singletonList("node_1"), null, null, null,
                consolidationData));
    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_2", "nested1.yaml", 2,
                "role_1", "interface_2", Collections.singletonList("node_2"), null, null, null,
                consolidationData));

    testConsolidation(1, toscaServiceModel, consolidationData, Collections.singletonList(UnifiedCompositionMode.ScalingInstances));
  }

  @Test
  public void testConsolidationRuleFalseDifferentNodesConnectedOutFromResourceGroup()
      throws IOException, URISyntaxException  {
    translationContext = new TranslationContext();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/different_subinterface_types",
            null, null);

    ConsolidationData consolidationData = new ConsolidationData();
    generateComputeAndPortConsolidationData(computeNodeTypeName, consolidationData);

    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_1", "nested1.yaml", 2,
                "role_1", "interface_1", null, Collections.singletonList("node_1"), null, null,
                consolidationData));
    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_2", "nested1.yaml", 2,
                "role_1", "interface_2", null, Collections.singletonList("node_2"), null, null,
                consolidationData));

    testConsolidation(1, toscaServiceModel, consolidationData, Collections.singletonList(UnifiedCompositionMode.ScalingInstances));
  }

  @Test
  public void testConsolidationRuleFalseGetAttrInBetweenSubInterfacesOfSameType()
      throws IOException, URISyntaxException  {
    translationContext = new TranslationContext();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/different_subinterface_types",
            null, null);

    ConsolidationData consolidationData = new ConsolidationData();
    generateComputeAndPortConsolidationData(computeNodeTypeName, consolidationData);

    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_1", "nested1.yaml", 2,
                "role_1", "interface_1", null, null, Collections.singletonList("in_node_1"), null,
                consolidationData));
    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_2", "nested1.yaml", 2,
                "role_1", "interface_2", null, null, Collections.singletonList("interface_1"), null,
                consolidationData));

    testConsolidation(1, toscaServiceModel, consolidationData, Collections.singletonList(UnifiedCompositionMode.CatalogInstance));
  }

  @Test
  public void testConsolidationRuleFalseGetAttrOutBetweenSubInterfacesOfSameType()
      throws IOException, URISyntaxException  {
    translationContext = new TranslationContext();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/different_subinterface_types",
            null, null);

    ConsolidationData consolidationData = new ConsolidationData();
    TestUtils
        .initComputeNodeTypeInConsolidationData(mainST, computeNodeTypeName, consolidationData);

    updateComputeConsolidationData("server_ps01", computeNodeTypeName, CM_01_PORT_TYPE,
        "cm01_port_1", consolidationData);
    updateComputeConsolidationData("server_ps02", computeNodeTypeName, CM_01_PORT_TYPE,
        "cm01_port_2", consolidationData);

    GetAttrFuncData getAttrFuncData = new GetAttrFuncData("name", "c1_name");
    Pair<String, GetAttrFuncData> getAttrOutInvalid1 =
        new ImmutablePair<>("interface_1", getAttrFuncData);
    Pair<String, GetAttrFuncData> getAttrOutInvalid2 =
        new ImmutablePair<>("interface_2", getAttrFuncData);

    TestUtils.initPortConsolidationData(mainST, consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_1", CM_01_PORT_TYPE,
            null, null, null, null, null,
        consolidationData);
    TestUtils.updatePortConsolidationData(mainST, "cm01_port_2", CM_01_PORT_TYPE,
            null, null, null, null, null,
        consolidationData);

    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_1", "nested1.yaml", 2,
                "role_1", "interface_1", null, null, null,
                Collections.singletonList(getAttrOutInvalid1), consolidationData));
    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_2", "nested1.yaml", 2,
                "role_1", "interface_2", null, null, null,
                Collections.singletonList(getAttrOutInvalid2), consolidationData));

    testConsolidation(1, toscaServiceModel, consolidationData, Collections.singletonList(UnifiedCompositionMode.CatalogInstance));
  }

  @Test
  public void testConsolidationRuleFalseDifferentRoleInResourceGroup() throws IOException, URISyntaxException  {
    translationContext = new TranslationContext();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/different_subinterface_types",
            null, null);

    ConsolidationData consolidationData = new ConsolidationData();
    generateComputeAndPortConsolidationData(computeNodeTypeName, consolidationData);

    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_1", "nested1.yaml", 2,
                "role_1", "interface_1", null, null, null, null, consolidationData));
    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_2", "nested1.yaml", 2,
                "role_2", "interface_2", null, null, null, null, consolidationData));

    testConsolidation(1, toscaServiceModel, consolidationData, Collections.singletonList(UnifiedCompositionMode.CatalogInstance));
  }

  @Test
  public void testConsolidationRuleTrueForSubInterfaces() throws IOException, URISyntaxException  {
    translationContext = new TranslationContext();
    String computeNodeTypeName = "org.openecomp.resource.vfc.nodes.heat.ps_server";
    ToscaServiceModel toscaServiceModel = TestUtils.loadToscaServiceModel
        ("/mock/services/heattotosca/consolidation/translatedfiles/pre_condition/different_subinterface_types",
            null, null);

    ConsolidationData consolidationData = new ConsolidationData();
    generateComputeAndPortConsolidationData(computeNodeTypeName, consolidationData);

    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_1", "nested1.yaml", 2,
                "role_1", "interface_1", Collections.singletonList("node_1"),
                Collections.singletonList("node_1"), null, null, consolidationData));
    TestUtils
        .addPortSubInterface(
            new SubInterfaceConsolidationDataTestInfo(mainST, "cm01_port_2", "nested1.yaml", 2,
                "role_1", "interface_2", Collections.singletonList("node_1"),
                Collections.singletonList("node_1"), null, null, consolidationData));

    testConsolidation(1, toscaServiceModel, consolidationData, Collections.singletonList(UnifiedCompositionMode.ScalingInstances));
  }

  private void testConsolidation(int times, ToscaServiceModel toscaServiceModel,
                                 ConsolidationData consolidationData,
                                 List<UnifiedCompositionMode> expectedUnifiedModes) {
    Mockito.doNothing().when(unifiedCompositionServiceMock).createUnifiedComposition(
        any(),
        any(),
        any(),
        any(),
        any());
    translationContext.setConsolidationData(consolidationData);
    verifyMainServiceTemplateConsolidation(times, expectedUnifiedModes, toscaServiceModel);
  }

  private void updateComputeConsolidationData(String computeNodeTemplateId,
                                              String computeNodeTypeName,
                                              String portType,
                                              String portNodeTemplateId,
                                              ConsolidationData consolidationData) {
    List<Pair<String, String>> portTypeToIdList = new ArrayList<>();
    portTypeToIdList.add(new ImmutablePair<>(portType, portNodeTemplateId));

    TestUtils.updateComputeTemplateConsolidationData(
        mainST,
        computeNodeTypeName, computeNodeTemplateId,
        null, null, null, null, null, null,
        portTypeToIdList, consolidationData);
  }


  private void verifyMainServiceTemplateConsolidation(int times,
                                                      List<UnifiedCompositionMode> expectedUnifiedCompositionModes,
                                                      ToscaServiceModel toscaServiceModel) {
    consolidationService
        .serviceTemplateConsolidation(toscaServiceModel.getServiceTemplates().get(mainST),
            translationContext);

    ServiceTemplate mainServiceTemplate = toscaServiceModel.getServiceTemplates().get(mainST);
    verify(unifiedCompositionServiceMock, times(times))
        .createUnifiedComposition(
            eq(mainServiceTemplate),
            any(),
            unifiedModelListArg.capture(),
            unifiedCompositionModeArg.capture(),
            eq(translationContext));

    List<UnifiedCompositionMode> actualUnifiedCompositionModes =
        unifiedCompositionModeArg.getAllValues();
    Assert.assertEquals(expectedUnifiedCompositionModes, actualUnifiedCompositionModes);
  }

  private void verifySubstitutionServiceTemplateConsolidation(int times,
                                                              ServiceTemplate mainST,
                                                              ServiceTemplate nestedST,
                                                              UnifiedCompositionMode expectedMode) {
    consolidationService.substitutionServiceTemplateConsolidation(null, mainST,
        nestedST, translationContext);

    if (expectedMode.equals(UnifiedCompositionMode.NestedSingleCompute)) {
      verify(unifiedCompositionServiceMock, times(times)).
          createUnifiedComposition(
              eq(mainST),
              eq(nestedST),
              unifiedModelListArg.capture(),
              unifiedCompositionModeArg.capture(),
              eq(translationContext));
      Assert.assertEquals(expectedMode, unifiedCompositionModeArg.getValue());

    }
  }

  private void addMockServiceTemplateToContext(String serviceTemplateFileName,
                                               String templateName) {
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    Map<String, String> metadata = new HashMap<>();
    metadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, templateName);
    serviceTemplate.setMetadata(metadata);
    translationContext.getTranslatedServiceTemplates()
        .put(serviceTemplateFileName, serviceTemplate);
  }

}
