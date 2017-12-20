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

package org.openecomp.sdc.vendorsoftwareproduct.services.impl.composition;

import org.apache.commons.collections.CollectionUtils;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.NetworkCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;

public class CompositionEntityDataManagerImplTest {
  private static final String VSP1 = "vsp1";
  private static final Version VERSION = new Version(0, 1);
  private static final String COMPONENT1 = "component1";
  private static final String NIC1 = "nic1";
  private static final String SIMPLE_SCHEMA = "{\n" +
      "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
      "  \"type\": \"object\",\n" +
      "  \"properties\": {\n" +
      "    \"a\": {\n" +
      "      \"type\": \"number\"\n" +
      "    }\n" +
      "  }\n" +
      "}";
  private static final String NETWORK_COMPOSITION_SCHEMA = "{\n" +
      "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
      "  \"type\": \"object\",\n" +
      "  \"properties\": {\n" +
      "    \"name\": {\n" +
      "      \"type\": \"string\",\n" +
      "      \"enum\": [\n" +
      "        \"network1 name\"\n" +
      "      ],\n" +
      "      \"default\": \"network1 name\"\n" +
      "    },\n" +
      "    \"dhcp\": {\n" +
      "      \"type\": \"boolean\",\n" +
      "      \"enum\": [\n" +
      "        true\n" +
      "      ],\n" +
      "      \"default\": true\n" +
      "    }\n" +
      "  },\n" +
      "  \"additionalProperties\": false,\n" +
      "  \"required\": [\n" +
      "    \"name\",\n" +
      "    \"dhcp\"\n" +
      "  ]\n" +
      "}";

  private Map<CompositionEntityId, Collection<String>> errorsById;
  @InjectMocks
  @Spy
  private CompositionEntityDataManagerImpl compositionEntityDataManager;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testAddNullEntity_negative() {
    compositionEntityDataManager.addEntity(null, null);
  }

  @Test
  public void testAddEntity() {
    compositionEntityDataManager
        .addEntity(new VspQuestionnaireEntity(VSP1, VERSION), null);

    String invalidQuestionnaireData = "{\"a\": \"b\"}";

    ComponentEntity component = new ComponentEntity(VSP1, VERSION, COMPONENT1);
    component.setQuestionnaireData(invalidQuestionnaireData);
    compositionEntityDataManager.addEntity(component, null);

    NicEntity nic = new NicEntity(VSP1, VERSION, COMPONENT1, NIC1);
    nic.setQuestionnaireData(invalidQuestionnaireData);
    compositionEntityDataManager.addEntity(nic, null);
  }

  // TODO: 3/15/2017 fix and enable
  //@Test(dependsOnMethods = "testAddEntity")
  public void testValidateEntitiesQuestionnaire() {
    doReturn(SIMPLE_SCHEMA).when(compositionEntityDataManager)
        .generateSchema(SchemaTemplateContext.questionnaire, CompositionEntityType.vsp, null);
    doReturn(SIMPLE_SCHEMA).when(compositionEntityDataManager)
        .generateSchema(SchemaTemplateContext.questionnaire, CompositionEntityType.component, null);
    doReturn(SIMPLE_SCHEMA).when(compositionEntityDataManager)
        .generateSchema(SchemaTemplateContext.questionnaire, CompositionEntityType.nic, null);

    errorsById = compositionEntityDataManager.validateEntitiesQuestionnaire();
    Assert.assertNotNull(errorsById);
    Assert.assertEquals(errorsById.size(), 2); // both component and nic data don't mach schemas
    CompositionEntityId nicId =
        new NicEntity(VSP1, VERSION, COMPONENT1, NIC1).getCompositionEntityId();
    Assert.assertTrue(errorsById.containsKey(nicId));
    Assert.assertTrue(errorsById.containsKey(nicId.getParentId()));
  }

  @Test(dependsOnMethods = "testAddEntity")
  public void testBuildTrees() {
    compositionEntityDataManager.buildTrees();
  }

  // TODO: 3/15/2017 fix and enable
  //@Test(dependsOnMethods = "testBuildTrees")
  public void testAddErrorsToTrees() {
    compositionEntityDataManager.addErrorsToTrees(errorsById);
  }

  // TODO: 3/15/2017 fix and enable
  //@Test(dependsOnMethods = "testAddErrorsToTrees")
  public void testGetTrees() {
    Collection<CompositionEntityValidationData> trees = compositionEntityDataManager.getTrees();
    Assert.assertNotNull(trees);
    Assert.assertEquals(trees.size(), 1);

    CompositionEntityValidationData vspValidationData = trees.iterator().next();
    assertValidationData(vspValidationData, VSP1, CompositionEntityType.vsp, false);
    Assert.assertEquals(vspValidationData.getSubEntitiesValidationData().size(), 1);

    CompositionEntityValidationData componentValidationData =
        vspValidationData.getSubEntitiesValidationData().iterator().next();
    assertValidationData(componentValidationData, COMPONENT1, CompositionEntityType.component,
        true);
    Assert.assertEquals(componentValidationData.getSubEntitiesValidationData().size(), 1);

    CompositionEntityValidationData nicValidationData =
        componentValidationData.getSubEntitiesValidationData().iterator().next();
    assertValidationData(nicValidationData, NIC1, CompositionEntityType.nic, true);
    Assert.assertNull(nicValidationData.getSubEntitiesValidationData());
  }

  @Test
  public void testValidateValidEntity() {
    NetworkEntity networkEntity = new NetworkEntity(VSP1, VERSION, "network1");
    Network network = new Network();
    network.setName("network1 name");
    network.setDhcp(true);
    networkEntity.setNetworkCompositionData(network);

    NetworkCompositionSchemaInput schemaTemplateInput = new NetworkCompositionSchemaInput();
    schemaTemplateInput.setManual(false);
    schemaTemplateInput.setNetwork(network);

    doReturn(NETWORK_COMPOSITION_SCHEMA).when(compositionEntityDataManager)
        .generateSchema(SchemaTemplateContext.composition, CompositionEntityType.network,
            schemaTemplateInput);

    CompositionEntityValidationData validationData = compositionEntityDataManager
        .validateEntity(networkEntity, SchemaTemplateContext.composition, schemaTemplateInput);
    assertValidationData(validationData, "network1", CompositionEntityType.network, false);
  }

  @Test
  public void testValidateInvalidEntity() {
    NetworkEntity networkEntity = new NetworkEntity(VSP1, VERSION, "network1");
    Network network = new Network();
    network.setName("network1 name changed");
    network.setDhcp(false);
    networkEntity.setNetworkCompositionData(network);

    NetworkCompositionSchemaInput schemaTemplateInput = new NetworkCompositionSchemaInput();
    schemaTemplateInput.setManual(false);
    Network origNetwork = new Network();
    origNetwork.setName("network1 name");
    origNetwork.setDhcp(true);
    schemaTemplateInput.setNetwork(origNetwork);

    doReturn(NETWORK_COMPOSITION_SCHEMA).when(compositionEntityDataManager)
        .generateSchema(SchemaTemplateContext.composition, CompositionEntityType.network,
            schemaTemplateInput);

    CompositionEntityValidationData validationData = compositionEntityDataManager
        .validateEntity(networkEntity, SchemaTemplateContext.composition, schemaTemplateInput);
    assertValidationData(validationData, "network1", CompositionEntityType.network, true);
    Assert.assertEquals(validationData.getErrors().size(), 2);
  }

  @Test
  public void testNicAndComponentValidQuestionnaire() {
    compositionEntityDataManager
        .addEntity(new VspQuestionnaireEntity(VSP1, VERSION), null);

    ComponentEntity componentEntity = new ComponentEntity(VSP1, VERSION, COMPONENT1);
    componentEntity.setQuestionnaireData(loadFileToString("quesionnaire/validComponent.json"));
    compositionEntityDataManager.addEntity(componentEntity, null);

    NicEntity nicEntity = new NicEntity(VSP1, VERSION, COMPONENT1, NIC1);
    nicEntity.setQuestionnaireData(loadFileToString("quesionnaire/validNic.json"));
    compositionEntityDataManager.addEntity(nicEntity, null);

    doReturn(SIMPLE_SCHEMA)
        .when(compositionEntityDataManager)
        .generateSchema(SchemaTemplateContext.questionnaire, CompositionEntityType.vsp, null);

    doReturn(loadFileToString("quesionnaire/schema/componentQuestionnaire.json"))
        .when(compositionEntityDataManager)
        .generateSchema(SchemaTemplateContext.questionnaire, CompositionEntityType.component, null);

    doReturn(loadFileToString("quesionnaire/schema/nicQuestionnaire.json"))
        .when(compositionEntityDataManager)
        .generateSchema(SchemaTemplateContext.questionnaire, CompositionEntityType.nic, null);

    Map<CompositionEntityId, Collection<String>> errorsById =
        compositionEntityDataManager.validateEntitiesQuestionnaire();
    Assert.assertEquals(errorsById.size(), 1);
  }

  @Test(dependsOnMethods = "testNicAndComponentValidQuestionnaire")
  public void testComponentInvalidQuestionnaire() {
    ComponentEntity componentEntity = new ComponentEntity(VSP1, VERSION, COMPONENT1);
    componentEntity.setQuestionnaireData(loadFileToString("quesionnaire/invalidComponent.json"));
    compositionEntityDataManager.addEntity(componentEntity, null);

    Map<CompositionEntityId, Collection<String>> errorsById =
        compositionEntityDataManager.validateEntitiesQuestionnaire();
    Assert.assertEquals(errorsById.size(), 2);

    CompositionEntityId component = new ArrayList<>(errorsById.keySet()).get(1);
    List<String> errors = (List<String>) errorsById.get(component);
    Assert.assertEquals(errors.size(), 1);
    Assert.assertEquals(errors.get(0),
        "#/general/recovery/pointObjective: 20.0 is not lower or equal to 15");
  }

  private static void assertValidationData(CompositionEntityValidationData validationData,
                                           String id, CompositionEntityType type,
                                           boolean hasErrors) {
    Assert.assertNotNull(validationData);
    Assert.assertEquals(validationData.getEntityId(), id);
    Assert.assertEquals(validationData.getEntityType(), type);
    Assert.assertTrue(CollectionUtils.isNotEmpty(validationData.getErrors()) == hasErrors);
  }

  private static String loadFileToString(String path) {
    return new String(FileUtils.toByteArray(FileUtils.loadFileToInputStream(path)));
  }
}