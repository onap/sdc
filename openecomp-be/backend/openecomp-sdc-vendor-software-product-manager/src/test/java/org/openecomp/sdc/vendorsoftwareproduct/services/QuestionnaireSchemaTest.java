package org.openecomp.sdc.vendorsoftwareproduct.services;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.core.utilities.file.FileUtils;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class QuestionnaireSchemaTest {

  public static final String VSP1 = "vsp1";
  public static final String COMPONENT1 = "component1";
  public static final String NIC1 = "nic1";
  private static CompositionEntityDataManager compositionEntityDataManager =
      new CompositionEntityDataManager();
  private static Map<CompositionEntityId, Collection<String>> errorsById;
  private static org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity componentEntity;
  private static org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity nicEntity;

  private static String loadFileToString(String path) {
    return new String(FileUtils.toByteArray(FileUtils.loadFileToInputStream(path)));
  }

  @Test
  public void testAddEntity() {
    compositionEntityDataManager
        .addEntity(new VspQuestionnaireEntity(VSP1, new Version(0, 1)), null);

    componentEntity = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity(VSP1, new Version(0, 1), COMPONENT1);
    nicEntity = new org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity(VSP1, new Version(0, 1), COMPONENT1, NIC1);
    compositionEntityDataManager.addEntity(componentEntity, null);

    componentEntity.setQuestionnaireData(loadFileToString("quesionnaire/validComponent.json"));
    nicEntity.setQuestionnaireData(loadFileToString("quesionnaire/validNic.json"));
    compositionEntityDataManager.addEntity(nicEntity, null);
  }

  @Test(dependsOnMethods = "testAddEntity")
  public void testNicAndComponentValidQuestionnaire() {
    errorsById = compositionEntityDataManager.validateEntitiesQuestionnaire();
    Assert.assertEquals(errorsById.size(), 0);
  }

  @Test(dependsOnMethods = "testNicAndComponentValidQuestionnaire")
  public void testComponentInvalidQuestionnaire() {
    componentEntity.setQuestionnaireData(loadFileToString("quesionnaire/invalidComponent.json"));
    compositionEntityDataManager.addEntity(componentEntity, null);

    errorsById = compositionEntityDataManager.validateEntitiesQuestionnaire();
    Assert.assertEquals(errorsById.size(), 1);

    CompositionEntityId component = errorsById.keySet().iterator().next();
    List<String> errors = (List<String>) errorsById.get(component);
    Assert.assertEquals(errors.size(), 1);
    Assert.assertEquals(errors.get(0),
        "#/general/recovery/pointObjective: 20.0 is not lower or equal to 15");
  }
}
