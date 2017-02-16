package org.openecomp.sdc.vendorsoftwareproduct.dao;

import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;

import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.core.model.dao.ServiceModelDaoFactory;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class ServiceModelDaoFactoryTest {

  private static final String vspId = CommonMethods.nextUuId();
  private static final Version version = Version.valueOf("1.0");
  private static final String baseServiceTemplateName = "baseYaml.yaml";
  private static String artifact001;


  @Test
  public void storeServiceModelTest() {


    ToscaServiceModel model = getToscaServiceModel();
    ServiceModelDaoFactory.getInstance().createInterface().storeServiceModel(vspId, version, model);
  }


  @Test(dependsOnMethods = "storeServiceModelTest")
  public void getServiceModelTest() {
    Object model =
        ServiceModelDaoFactory.getInstance().createInterface().getServiceModel(vspId, version);
    Assert.assertNotNull(model);
    Assert.assertTrue(model instanceof ToscaServiceModel);
    if (model instanceof ToscaServiceModel) {

      artifact001 =
          (String) ((ToscaServiceModel) model).getArtifactFiles().getFileList().toArray()[0];
    }
  }

  @Test(dependsOnMethods = "getServiceModelTest")
  public void getServiceModelInfoTest() {
    Object info = ServiceModelDaoFactory.getInstance().createInterface()
        .getServiceModelInfo(vspId, version, artifact001);
    Assert.assertNotNull(info);
    Assert.assertTrue(info instanceof ServiceArtifact);
    if (info instanceof ServiceArtifact) {
      Assert.assertEquals(((ServiceArtifact) info).getName(), artifact001);
    }
  }

  private ToscaServiceModel getToscaServiceModel() {

    Map<String, ServiceTemplate> serviceTemplates = getServiceTemplates(baseServiceTemplateName);
    FileContentHandler artifacts = getArtifacts();
    return new ToscaServiceModel(artifacts, serviceTemplates, baseServiceTemplateName);
  }


  private Map<String, ServiceTemplate> getServiceTemplates(String base) {

    Map<String, ServiceTemplate> serviceTemplates = new HashMap<>();

    serviceTemplates.put(base, getServiceTemplate());
    serviceTemplates.put("SERV1", getServiceTemplate());
    serviceTemplates.put("SERV2", getServiceTemplate());
    serviceTemplates.put("SERV3", getServiceTemplate());
    serviceTemplates.put("SERV4", getServiceTemplate());

    return serviceTemplates;
  }

  public FileContentHandler getArtifacts() {
    Map<String, byte[]> artifacts = new HashMap<>();
    artifacts.put("art1", "this is art1".getBytes());
    artifacts.put("art2", ("this is art2 desc:" + CommonMethods.nextUuId()).getBytes());
    artifacts.put("art2", ("this is art3 desc:" + CommonMethods.nextUuId()).getBytes());
    artifacts.put("art2", ("this is art4 desc:" + CommonMethods.nextUuId()).getBytes());

    FileContentHandler fileContentHandler = new FileContentHandler();
    fileContentHandler.putAll(artifacts);
    return fileContentHandler;
  }

  public ServiceTemplate getServiceTemplate() {
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    serviceTemplate.setTosca_definitions_version("version 1.0");
    serviceTemplate.setDescription(CommonMethods.nextUuId());
    return serviceTemplate;
  }
}
