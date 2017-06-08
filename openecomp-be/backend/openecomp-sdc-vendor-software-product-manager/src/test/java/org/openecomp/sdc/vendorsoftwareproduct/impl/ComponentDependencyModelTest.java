package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentDependencyModelManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentDependencyModelManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ComponentDependencyModelErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.doReturn;

public class ComponentDependencyModelTest {

  @Spy
  @InjectMocks
  private ComponentDependencyModelManagerImpl componentDependencyModelManager;
  @Mock
  private VendorSoftwareProductManager vendorSoftwareProductManager;
  @Mock
  private VendorSoftwareProductDao vendorSoftwareProductDao;
  @Mock
  private ComponentDao componentDao;
  @Mock
  private ComponentManager componentManager;

  private static String vsp1Id;
  private static String sourceComp1Id;
  private static String sourceComp2Id;
  private static String sourceComp3Id;
  private static String sourceComp4Id;
  private static final String USER1 = "TestUser1";
  private static final String USER2 = "TestUser2";
  private static final Version VERSION01 = new Version(0, 1);
  private static String modelId = "model1";


  @BeforeClass
  private void init() {
    MockitoAnnotations.initMocks(this);
  }

  public static VspDetails createVspDetails(String id, Version version, String name, String desc,
                                            String vendorName, String vlm, String icon,
                                            String category, String subCategory,
                                            String licenseAgreement, List<String> featureGroups
  ) {
    VspDetails vspDetails = new VspDetails(id, version);
    vspDetails.setName(name);
    vspDetails.setDescription(desc);
    vspDetails.setIcon(icon);
    vspDetails.setCategory(category);
    vspDetails.setSubCategory(subCategory);
    vspDetails.setVendorName(vendorName);
    vspDetails.setVendorId(vlm);
    vspDetails.setVlmVersion(new Version(1, 0));
    vspDetails.setLicenseAgreement(licenseAgreement);
    vspDetails.setFeatureGroups(featureGroups);
    return vspDetails;
  }

  @Test
  public void testCreateNegative_NoSourceId() {
    List<ComponentDependencyModelEntity> entities = new ArrayList<ComponentDependencyModelEntity>();
    entities.add(createModelEntity(null, sourceComp2Id));
    testCreate_negative(entities, vsp1Id, VERSION01, USER1,
        ComponentDependencyModelErrorBuilder.getNoSourceComponentErrorBuilder().id(),
        ComponentDependencyModelErrorBuilder.getNoSourceComponentErrorBuilder().message());

    entities.removeAll(entities);
    entities.add(createModelEntity("", sourceComp2Id));
    testCreate_negative(entities, vsp1Id, VERSION01, USER1,
        ComponentDependencyModelErrorBuilder.getNoSourceComponentErrorBuilder().id(),
        ComponentDependencyModelErrorBuilder.getNoSourceComponentErrorBuilder().message());
  }

  @Test
  public void testCreateNegative_SameSourceTarget() {
    List<ComponentDependencyModelEntity> entities = new ArrayList<ComponentDependencyModelEntity>();
    entities.add(createModelEntity("sourceComp1Id", "sourceComp1Id"));
    testCreate_negative(entities, vsp1Id, VERSION01, USER1,
        ComponentDependencyModelErrorBuilder.getSourceTargetComponentEqualErrorBuilder().id(),
        ComponentDependencyModelErrorBuilder.getSourceTargetComponentEqualErrorBuilder().message());
  }

  @Test
  public void testCreate() {
    List<ComponentDependencyModelEntity> entities = new ArrayList<ComponentDependencyModelEntity>();
    entities.add(createModelEntity("sourceComp1Id", "sourceComp2Id"));
    entities.add(createModelEntity("sourceComp3Id", "sourceComp4Id"));

    componentDependencyModelManager.createComponentDependencyModel(entities, vsp1Id, VERSION01,
        USER1);
    Mockito.verify(vendorSoftwareProductDao, Mockito.times(1)).createComponentDependencyModel
        (entities, vsp1Id,
        VERSION01);
  }

  private ComponentDependencyModelEntity createModelEntity(String sourceId, String targetId) {
    ComponentDependencyModelEntity entity =
        new ComponentDependencyModelEntity(vsp1Id, VERSION01, modelId);
    entity.setSourceComponentId(sourceId);
    entity.setTargetComponentId(targetId);
    entity.setRelation("dependsOn");
    return entity;
  }

  private Collection<ComponentDependencyModelEntity> getDependencyModel(String vspId,
                                                                        Version version,
                                                                        String user) {
    return componentDependencyModelManager.list(vspId, version, user);
  }

  private void testCreate_negative(List<ComponentDependencyModelEntity> entities, String vspId,
                                   Version version, String user,
                                   String expectedErrorCode, String expectedErrorMsg) {
    try {
      componentDependencyModelManager.createComponentDependencyModel(entities, vspId, version,
        user);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
      Assert.assertEquals(exception.getMessage(), expectedErrorMsg);
    }
  }
}
