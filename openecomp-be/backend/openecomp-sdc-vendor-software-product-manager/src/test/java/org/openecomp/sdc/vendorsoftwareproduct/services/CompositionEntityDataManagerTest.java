package org.openecomp.sdc.vendorsoftwareproduct.services;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.NetworkCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.apache.commons.collections.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Map;

public class CompositionEntityDataManagerTest {

  public static final String VSP1 = "vsp1";
  public static final String COMPONENT1 = "component1";
  public static final String NIC1 = "nic1";
  private static CompositionEntityDataManager compositionEntityDataManager =
      new CompositionEntityDataManager();
  private static Map<CompositionEntityId, Collection<String>> errorsById;

  private static void assertValidationData(CompositionEntityValidationData validationData,
                                           String id, CompositionEntityType type,
                                           boolean hasErrors) {
    Assert.assertNotNull(validationData);
    Assert.assertEquals(validationData.getEntityId(), id);
    Assert.assertEquals(validationData.getEntityType(), type);
    Assert.assertTrue(CollectionUtils.isNotEmpty(validationData.getErrors()) == hasErrors);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testAddNullEntity_negative() {
    compositionEntityDataManager.addEntity(null, null);
  }

  @Test
  public void testAddEntity() {
    compositionEntityDataManager
        .addEntity(new VspQuestionnaireEntity(VSP1, new Version(0, 1)), null);

    String invalidQuestionnaireData = "{\"a\": \"b\"}";

    ComponentEntity component = new ComponentEntity(VSP1, new Version(0, 1), COMPONENT1);
    component.setQuestionnaireData(invalidQuestionnaireData);
    compositionEntityDataManager.addEntity(component, null);

    org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity
        nic = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity(VSP1, new Version(0, 1), COMPONENT1, NIC1);
    nic.setQuestionnaireData(invalidQuestionnaireData);
    compositionEntityDataManager.addEntity(nic, null);
  }

  @Test(dependsOnMethods = "testAddEntity")
  public void testValidateEntitiesQuestionnaire() {
    errorsById = compositionEntityDataManager.validateEntitiesQuestionnaire();
    Assert.assertNotNull(errorsById);
    Assert.assertEquals(errorsById.size(), 2); // both component and nic data don't mach schemas
    CompositionEntityId nicId =
        new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity(VSP1, new Version(0, 1), COMPONENT1, NIC1).getCompositionEntityId();
    Assert.assertTrue(errorsById.containsKey(nicId));
    Assert.assertTrue(errorsById.containsKey(nicId.getParentId()));
  }

  @Test(dependsOnMethods = "testAddEntity")
  public void testBuildTrees() {
    compositionEntityDataManager.buildTrees();
  }

  @Test(dependsOnMethods = "testBuildTrees")
  public void testAddErrorsToTrees() {
    compositionEntityDataManager.addErrorsToTrees(errorsById);
  }

  @Test(dependsOnMethods = "testAddErrorsToTrees")
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
    NetworkEntity networkEntity = new NetworkEntity(VSP1, new Version(0, 1), "network1");
    Network network = new Network();
    network.setName("network1 name");
    network.setDhcp(true);
    networkEntity.setNetworkCompositionData(network);

    NetworkCompositionSchemaInput schemaTemplateInput = new NetworkCompositionSchemaInput();
    schemaTemplateInput.setManual(false);
    schemaTemplateInput.setNetwork(network);

    CompositionEntityValidationData validationData = CompositionEntityDataManager
        .validateEntity(networkEntity, SchemaTemplateContext.composition, schemaTemplateInput);
    assertValidationData(validationData, "network1", CompositionEntityType.network, false);
  }

  @Test
  public void testValidateInvalidEntity() {
    NetworkEntity networkEntity = new NetworkEntity(VSP1, new Version(0, 1), "network1");
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

    CompositionEntityValidationData validationData = CompositionEntityDataManager
        .validateEntity(networkEntity, SchemaTemplateContext.composition, schemaTemplateInput);
    assertValidationData(validationData, "network1", CompositionEntityType.network, true);
    Assert.assertEquals(validationData.getErrors().size(), 2);
  }
}