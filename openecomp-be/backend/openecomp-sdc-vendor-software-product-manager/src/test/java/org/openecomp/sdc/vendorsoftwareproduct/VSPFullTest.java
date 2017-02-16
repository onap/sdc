package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.VendorSoftwareProductManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.VersionedVendorSoftwareProductInfo;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.core.model.dao.EnrichedServiceModelDaoFactory;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VSPFullTest {


  public static final Version VERSION01 = new Version(0, 1);
  private static final org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao
      vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();
  private static final String USER1 = "vspTestUser1";
  private static VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  private static VendorLicenseFacade vendorLicenseFacade =
      org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory.getInstance().createInterface();

  @Test
  public void testEnrichModelInSubmit() {
    UniqueValueUtil
        .deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME,
            "VSP_FullTest");

    String vlm1Id = vendorLicenseFacade.createVendorLicenseModel(VSPCommon
            .createVendorLicenseModel("vlmName " + CommonMethods.nextUuId(), "vlm1Id desc", "icon1"),
        USER1).getId();
    String entitlementPoolId = vendorLicenseFacade
        .createEntitlementPool(new EntitlementPoolEntity(vlm1Id, null, null), USER1).getId();

    org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity
        featureGroup = new org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity(vlm1Id, null, null);
    featureGroup.getEntitlementPoolIds().add(entitlementPoolId);
    String featureGroupId = vendorLicenseFacade.createFeatureGroup(featureGroup, USER1).getId();

    org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity
        licenseAgreement = new org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity(vlm1Id, null, null);
    licenseAgreement.getFeatureGroupIds().add(featureGroupId);
    String licenseAgreementId =
        vendorLicenseFacade.createLicenseAgreement(licenseAgreement, USER1).getId();

    vendorLicenseFacade.checkin(vlm1Id, USER1);
    vendorLicenseFacade.submit(vlm1Id, USER1);

    String vspId = createVsp(vlm1Id, licenseAgreementId, licenseAgreement.getFeatureGroupIds());

    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity> components = uploadFullCompositionFile(vspId);


    //check in
    vendorSoftwareProductManager.checkin(vspId, USER1);
    //submit
    try {
      ValidationResponse result = vendorSoftwareProductManager.submit(vspId, USER1);
      //Assert.assertTrue(result.isValid());
      //PackageInfo createPackageResult = vendorSoftwareProductManager.createPackage(vspId, USER1);

    } catch (IOException e) {
      Assert.fail();
    }
    VersionedVendorSoftwareProductInfo details =
        vendorSoftwareProductManager.getVspDetails(vspId, null, USER1);


    //File csar = vendorSoftwareProductManager.getTranslatedFile(vspId,details.getVersionInfo().getActiveVersion(),USER1);
    // writeFile(csar);


    ToscaServiceModel model =
        (ToscaServiceModel) EnrichedServiceModelDaoFactory.getInstance().createInterface()
            .getServiceModel(vspId, details.getVersionInfo().getActiveVersion());

    Map<String, CapabilityDefinition> capabilities = new HashMap<>();
    for (org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity component : components) {
      model.getServiceTemplates().
          entrySet().
          stream().
          filter(entryValue -> entryValue.getValue() != null &&
              entryValue.getValue().getNode_types() != null &&
              entryValue.getValue().
                  getNode_types().
                  containsKey(component.getComponentCompositionData().getName())).
          forEach(entryValue -> entryValue.getValue().getNode_types().
              values().
              stream().
              filter(type -> MapUtils.isNotEmpty(type.getCapabilities())).
              forEach(type -> type.getCapabilities().
                  entrySet().
                  forEach(entry -> addCapability(entryValue.getKey(), capabilities, entry.getKey(),
                      entry.getValue()))));

    }

    Assert.assertNotNull(capabilities);
  }

  private Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity> uploadFullCompositionFile(String vspId) {
    vendorSoftwareProductManager
        .uploadFile(vspId, getFileInputStream("/vspmanager/zips/fullComposition.zip"), USER1);

    Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity> components =
        vendorSoftwareProductManager.listComponents(vspId, null, USER1);
    Assert.assertFalse(components.isEmpty());

    for (org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity component : components) {
      Assert.assertNotNull(vendorSoftwareProductManager
          .getComponentQuestionnaire(vspId, null, component.getId(), USER1).getData());

      Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity> nics =
          vendorSoftwareProductManager.listNics(vspId, null, component.getId(), USER1);
      Assert.assertFalse(nics.isEmpty());
      for (org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity nic : nics) {
        Assert.assertNotNull(vendorSoftwareProductManager
            .getNicQuestionnaire(vspId, null, component.getId(), nic.getId(), USER1).getData());
      }
    }

    return components;
  }

  private String createVsp(String vlm1Id, String licenseAgreementId, Set<String> featureGroupIds) {
    VspDetails expectedVsp = VSPCommon
        .createVspDetails(null, null, "VSP_FullTest", "Test-vsp_fullTest", "vendorName", vlm1Id,
            "icon", "category", "subCategory", licenseAgreementId,
            featureGroupIds.stream().collect(Collectors.toList()));
    String vspId = vendorSoftwareProductManager.createNewVsp(expectedVsp, USER1).getId();

    VspDetails actualVsp =
        vendorSoftwareProductDao.getVendorSoftwareProductInfo(new VspDetails(vspId, VERSION01));
    expectedVsp.setId(vspId);
    expectedVsp.setVersion(VERSION01);

    VendorSoftwareProductManagerTest.assertVspsEquals(actualVsp, expectedVsp);
    Assert.assertNotNull(
        vendorSoftwareProductManager.getVspQuestionnaire(vspId, null, USER1).getData());
    return vspId;
  }

  private void writeFile(File csar) {
    try {
      FileInputStream in = new FileInputStream(csar);
      File output = new File("CSAR_vDNS.zip");

      FileOutputStream out = new FileOutputStream(output);

      IOUtils.copy(in, out);
      in.close();
      out.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void addCapability(String entryValueKey, Map<String, CapabilityDefinition> capabilities,
                             String key, CapabilityDefinition value) {

    capabilities.put(entryValueKey + "_" + key, value);
  }

  private InputStream getFileInputStream(String fileName) {
    URL url = this.getClass().getResource(fileName);
    try {
      return url.openStream();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }


}
