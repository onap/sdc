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

package org.openecomp.sdc.vendorsoftwareproduct;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.openecomp.core.enrichment.types.ArtifactType;
import org.openecomp.core.model.dao.EnrichedServiceModelDaoFactory;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.VersionedVendorSoftwareProductInfo;
import org.openecomp.sdc.vendorsoftwareproduct.utils.VSPCommon;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class VSPFullTest {
/*


  public static final Version VERSION01 = new Version(0, 1);
  private static final VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory
          .getInstance().createInterface();
  private static final String USER1 = "vspTestUser1";
  private static VendorSoftwareProductManager vendorSoftwareProductManager = null;
  //new VendorSoftwareProductManagerImpl();
  private static VendorLicenseFacade vendorLicenseFacade =
      VendorLicenseFacadeFactory.getInstance().createInterface();
  private OrchestrationTemplateCandidateManager candidateManager;
  private MibManager mibManager;
  private NicManager nicManager;

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

    FeatureGroupEntity featureGroup = new FeatureGroupEntity(vlm1Id, null, null);
    featureGroup.getEntitlementPoolIds().add(entitlementPoolId);
    String featureGroupId = vendorLicenseFacade.createFeatureGroup(featureGroup, USER1).getId();

    LicenseAgreementEntity licenseAgreement = new LicenseAgreementEntity(vlm1Id, null, null);
    licenseAgreement.getFeatureGroupIds().add(featureGroupId);
    String licenseAgreementId =
        vendorLicenseFacade.createLicenseAgreement(licenseAgreement, USER1).getId();

    vendorLicenseFacade.checkin(vlm1Id, USER1);
    vendorLicenseFacade.submit(vlm1Id, USER1);

    String vspId = createVsp(vlm1Id, licenseAgreementId, licenseAgreement.getFeatureGroupIds());

    Collection<ComponentEntity> components = uploadFullCompositionFile(vspId);

    InputStream zis1 = getFileInputStream("/validation/zips/various/MIB.zip");
    mibManager
        .upload(zis1, "MMSC.zip", vspId, VERSION01,
            components.iterator().next().getId(),
            ArtifactType.SNMP_TRAP,
            USER1);

    //check in
    vendorSoftwareProductManager.checkin(vspId, USER1);
    //submit
    try {
      ValidationResponse result = vendorSoftwareProductManager.submit(vspId, USER1);
      //Assert.assertTrue(result.isValid());
      //PackageInfo createPackageResult = vendorSoftwareProductManager.createPackage(vspId, USER1);

    } catch (IOException exception) {
      Assert.fail();
    }
    VersionedVendorSoftwareProductInfo details =
        vendorSoftwareProductManager.getVsp(vspId, null, USER1);


    //File csar = vendorSoftwareProductManager.getTranslatedFile(vspId,details.getVersionInfo().getActiveVersion(),USER1);
    // writeFile(csar);


    ToscaServiceModel model =
        (ToscaServiceModel) EnrichedServiceModelDaoFactory.getInstance().createInterface()
            .getServiceModel(vspId, details.getVersionInfo().getActiveVersion());

    Map<String, CapabilityDefinition> capabilities = new HashMap<>();
    for (ComponentEntity component : components) {
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

  private Collection<ComponentEntity> uploadFullCompositionFile(String vspId) {
    candidateManager.upload(vspId, VERSION01,
        getFileInputStream("/vspmanager/zips/fullComposition.zip"), USER1);
    candidateManager.process(vspId, VERSION01, USER1);

    Collection<ComponentEntity> components = null;
    //vendorSoftwareProductManager.listComponents(vspId, null, USER1);
    Assert.assertFalse(components.isEmpty());

    for (ComponentEntity component : components) {
*/
/*      Assert.assertNotNull(vendorSoftwareProductManager
          .getQuestionnaire(vspId, null, component.getId(), USER1).getData());*//*


      Collection<NicEntity> nics =
          nicManager.listNics(vspId, null, component.getId(), USER1);
      Assert.assertFalse(nics.isEmpty());
      for (NicEntity nic : nics) {
        Assert.assertNotNull(nicManager
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
    String vspId = vendorSoftwareProductManager.createVsp(expectedVsp, USER1).getId();

    VspDetails actualVsp =
        vendorSoftwareProductDao.getVendorSoftwareProductInfo(new VspDetails(vspId, VERSION01));
    expectedVsp.setId(vspId);
    expectedVsp.setVersion(VERSION01);

    //VendorSoftwareProductManagerImplTest.assertVspsEquals(actualVsp, expectedVsp);
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
    } catch (IOException exception) {
      throw new RuntimeException(exception);
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
    } catch (IOException exception) {
      exception.printStackTrace();
      return null;
    }
  }


*/
}
