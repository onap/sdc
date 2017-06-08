package org.openecomp.sdc.migration;

import org.apache.commons.io.IOUtils;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.dao.EnrichedServiceModelDaoFactory;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.dao.ServiceModelDaoFactory;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.Import;
import org.openecomp.sdc.tosca.datatypes.model.Old1610ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.yamlutil.ToscaExtensionYamlUtil;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.core.zusammen.impl.CassandraConnectionInitializer;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


/**
 * Created by TALIO on 3/19/2017
 */
public class ToscaNamespaceMigration {

  private static VendorSoftwareProductManager vendorSoftwareProductManager =
      VspManagerFactory.getInstance().createInterface();
  private static OrchestrationTemplateCandidateManager orchestrationTemplateCandidateManager =
      OrchestrationTemplateCandidateManagerFactory.getInstance().createInterface();
  private static VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();
  private static ServiceModelDao<ToscaServiceModel, ServiceElement> serviceModelDao =
      ServiceModelDaoFactory.getInstance().createInterface();
  private static EnrichedServiceModelDao<ToscaServiceModel, ServiceElement>
      enrichedServiceModelDao =
      EnrichedServiceModelDaoFactory.getInstance().createInterface();
  private static VendorSoftwareProductInfoDao vspInfoDao =
      VendorSoftwareProductInfoDaoFactory.getInstance().createInterface();
  private static PackageInfoDao packageInfoDao = PackageInfoDaoFactory.getInstance()
      .createInterface();
  private static Logger logger = LoggerFactory.getLogger(ToscaNamespaceMigration.class);
  private static int status = 0;


  public static void main(String[] args) {
    CassandraConnectionInitializer.setCassandraConnectionPropertiesToSystem();

    Collection<VspDetails> vspList = vspInfoDao.list(new VspDetails());

    List<PackageInfo> packagesList =
        packageInfoDao.listByCategory(null, null);

    for (VspDetails vspInfo : vspList) {
      printMessage("Performing migration on vsp " + vspInfo.getName() + " and version " + vspInfo
          .getVersion().toString() + "\n");
      performMigration(vspInfo);
    }

    System.exit(status);
  }

  private static void performMigration(VspDetails vspDetails) {
    try {
      changeNamespaceInServiceTemplates(vspDetails);
    } catch (Exception e) {
      printMessage(
          "Could not perform migration for service templates on vsp " + vspDetails.getName());
      status = -1;
    }

    if (vspDetails.getVersion().isFinal()) {
      changeNamespaceInPackage(vspDetails);
    }
  }

  private static void changeNamespaceInServiceTemplates(VspDetails vspDetails) throws IOException {
    String vspId = vspDetails.getId();
    Version version = vspDetails.getVersion();
    ToscaServiceModel serviceModel;
    ToscaServiceModel enrichedServiceModel;

    serviceModel =
        serviceModelDao.getServiceModel(vspId, version);
    enrichedServiceModel =
        enrichedServiceModelDao.getServiceModel(vspId, version);

    printMessage("Working on vsp_service_template table in DB \n");
    changeNamespaceInServiceModel(serviceModel);
    printMessage("Finished Working on vsp_service_template table in DB \n");

    printMessage("Working on vsp_enriched_service_template table in DB \n");
    changeNamespaceInServiceModel(enrichedServiceModel);
    printMessage("Finished Working on vsp_enriched_service_template table in DB \n");

    serviceModelDao.storeServiceModel(vspId, version, serviceModel);
    enrichedServiceModelDao.storeServiceModel(vspId, version, enrichedServiceModel);
  }

  private static void changeNamespaceInServiceModel(ToscaServiceModel serviceModel) {
    Map<String, ServiceTemplate> changedServiceTemplates = new HashMap<>();
    Map<String, ServiceTemplate> serviceTemplates = serviceModel.getServiceTemplates();

    for (Map.Entry<String, ServiceTemplate> serviceTemplateEntry : serviceTemplates.entrySet()) {
      printMessage(
          "Changing namespace for Service Template " + serviceTemplateEntry.getKey() + "\n");

      ServiceTemplate serviceTemplate = serviceTemplateEntry.getValue();
      String fileAsJson = JsonUtil.object2Json(serviceTemplate);
      String replacedNamespace = fileAsJson.replace("org.openecomp.d2", "org.openecomp");
      ServiceTemplate newServiceTemplate;
      try {
        newServiceTemplate =
            new ToscaExtensionYamlUtil().yamlToObject(replacedNamespace, ServiceTemplate.class);
      } catch (Exception e) {
        System.out.println("Found vsp with old-versioned tosca service template");
        Old1610ServiceTemplate oldServiceTemplate =
            JsonUtil.json2Object(replacedNamespace, Old1610ServiceTemplate.class);
        newServiceTemplate = mapOldSTToCurrentST(oldServiceTemplate);

      }
      changedServiceTemplates.put(
          serviceTemplateEntry.getKey(), newServiceTemplate);
    }

    serviceModel.setServiceTemplates(changedServiceTemplates);
  }

  private static org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate mapOldSTToCurrentST(
      Old1610ServiceTemplate oldServiceTemplate) {
    org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate
        serviceTemplate = new org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate();

    serviceTemplate.setArtifact_types(oldServiceTemplate.getArtifact_types());
    serviceTemplate.setCapability_types(oldServiceTemplate.getCapability_types());
    serviceTemplate.setData_types(oldServiceTemplate.getData_types());
    serviceTemplate.setDescription(oldServiceTemplate.getDescription());
    serviceTemplate.setGroup_types(oldServiceTemplate.getGroup_types());
    serviceTemplate.setInterface_types(oldServiceTemplate.getInterface_types());
    serviceTemplate.setMetadata(oldServiceTemplate.getMetadata());
    serviceTemplate.setNode_types(oldServiceTemplate.getNode_types());
    serviceTemplate.setPolicy_types(oldServiceTemplate.getPolicy_types());
    serviceTemplate.setRelationship_types(oldServiceTemplate.getRelationship_types());
    serviceTemplate.setTopology_template(oldServiceTemplate.getTopology_template());

    List<Map<String, Import>> imports = new ArrayList<>();
    for (Map.Entry<String, Import> importEntry : oldServiceTemplate.getImports().entrySet()) {
      Map<String, Import> importMap = new HashMap<>();
      importMap.put(importEntry.getKey(), importEntry.getValue());
      imports.add(importMap);
    }
    serviceTemplate.setImports(imports);

    return serviceTemplate;

  }

  private static void changeNamespaceInPackage(VspDetails vspDetails) {
    String vspId = vspDetails.getId();
    printMessage("Start updating CSAR file with new namespace in DB \n");

    File translatedFile;
    try {
      translatedFile =
          getTranslatedFile(vspId, vspDetails.getVersion());
    } catch (Exception e) {
      printMessage("No translated file was found under vsp " + vspDetails.getName() + "\n");
      return;
    }
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ZipOutputStream zos = new ZipOutputStream(baos);
         ZipFile zipFile = new ZipFile(translatedFile)) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        ZipEntry ze = entries.nextElement();
        InputStream zipEntryIs = zipFile.getInputStream(ze);
        byte[] contentAsByte = IOUtils.toByteArray(zipEntryIs);
        String fileContent = new String(contentAsByte);

        String replacedNamespace = fileContent.replace("org.openecomp.d2", "org.openecomp");

        zos.putNextEntry(new ZipEntry(ze.getName()));
        zos.write(replacedNamespace.getBytes());
      }
      printMessage("Changed Tosca namesapce in package for vsp " + vspDetails.getName() + "\n");


      packageInfoDao.updateTranslatedContent(
          vspId, vspDetails.getVersion(), ByteBuffer.wrap(baos.toByteArray()));
      printMessage("Updated CSAR file with new namespace in DB \n");

    } catch (Exception e) {
      printMessage("Could not perform migration on csar");
    }
  }

  private static void printMessage(String message) {
    System.out.print(message);
    logger.debug(message);
  }

  private static File getTranslatedFile(String vspId, Version version)
      throws IOException {
    PackageInfo packageInfo =
        packageInfoDao.get(new PackageInfo(vspId, version));
    ByteBuffer translatedFileBuffer = packageInfo == null ? null :  packageInfo.getTranslatedFile();

    File translatedFile = new File(VendorSoftwareProductConstants.VSP_PACKAGE_ZIP);

    FileOutputStream fos = new FileOutputStream(translatedFile);
    fos.write(translatedFileBuffer.array());
    fos.close();


    return translatedFile;
  }
}
