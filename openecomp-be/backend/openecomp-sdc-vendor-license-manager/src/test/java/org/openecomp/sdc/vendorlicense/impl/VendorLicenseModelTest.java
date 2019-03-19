package org.openecomp.sdc.vendorlicense.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.dao.*;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


/**
 * Created by ayalaben on 7/19/2017
 */
public class VendorLicenseModelTest {

  private static final String USER1 = "TestUser1";
  private static final String USER2 = "TestUser2";

  private static String vlm1_id = "vlm1_id";
  private static String vlm2_id = "vlm2_id";
  private static String la1_id = "la1_id";
  private static String la2_id = "la2_id";
  private static String fg1_id = "fg1_id";
  private static String fg2_id = "fg2_id";
  public static final Version VERSION01 = new Version(0, 1);
  private static final Version VERSION10 = new Version(1, 0);

  @Mock
  private VersioningManager versioningManagerMcok;
  @Mock
  private VendorLicenseFacade vendorLicenseFacadeMcok;
  @Mock
  private VendorLicenseModelDao vendorLicenseModelDaoMcok;
  @Mock
  private LicenseAgreementDao licenseAgreementDaoMcok;
  @Mock
  private FeatureGroupDao featureGroupDaoMcok;
  @Mock
  private EntitlementPoolDao entitlementPoolDaoMcok;
  @Mock
  private LicenseKeyGroupDao licenseKeyGroupDaoMcok;
  @Mock
  private LimitDao limitDaoMcok;

  @Spy
  @InjectMocks
  private VendorLicenseManagerImpl vendorLicenseManager;


  @Captor
  private ArgumentCaptor<ActivityLogEntity> activityLogEntityArg;


  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown(){
    vendorLicenseManager = null;
  }

  @Test
  public void testValidate() {
    // TODO: 8/13/2017
    vendorLicenseManager.validate(vlm1_id, null);
    verify(vendorLicenseFacadeMcok).validate(vlm1_id, null);
  }

  @Test
  public void testCreate() {
    VendorLicenseModelEntity vlmEntity = new VendorLicenseModelEntity(vlm1_id, VERSION01);

    vendorLicenseManager.createVendorLicenseModel(vlmEntity);

    verify(vendorLicenseModelDaoMcok).create(vlmEntity);
  }

  @Test
  public void testUpdate() {

    VendorLicenseModelEntity existingVlm = new VendorLicenseModelEntity();
    existingVlm.setVersion(VERSION01);
    existingVlm.setId(vlm1_id);
    existingVlm.setIconRef("icon");
    existingVlm.setVendorName("VLM1");
    existingVlm.setDescription("decription");

    doReturn("VLM1").when(vendorLicenseModelDaoMcok).get(existingVlm);

    VendorLicenseModelEntity updatedVlm = new VendorLicenseModelEntity();
    updatedVlm.setVersion(VERSION01);
    updatedVlm.setId(vlm1_id);
    updatedVlm.setIconRef("icon");
    updatedVlm.setVendorName("VLM1_updated");
    updatedVlm.setDescription("decription");

    doNothing().when(vendorLicenseManager)
        .updateUniqueName(VendorLicenseConstants.UniqueValues.VENDOR_NAME,
            existingVlm.getVendorName(), updatedVlm.getVendorName());

    doReturn(existingVlm).when(vendorLicenseModelDaoMcok).get(any(VendorLicenseModelEntity.class));

    vendorLicenseManager.updateVendorLicenseModel(updatedVlm);

    verify(vendorLicenseModelDaoMcok).update(updatedVlm);
  }

  @Test
  public void testGetVendorLicenseModel() {
    vendorLicenseManager.getVendorLicenseModel(vlm1_id, VERSION01);
    verify(vendorLicenseFacadeMcok).getVendorLicenseModel(vlm1_id, VERSION01);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testDeleteVLMUnsupportedOperation() {
    vendorLicenseManager.deleteVendorLicenseModel(vlm1_id, null); // TODO: 8/13/2017
  }


//  @Test(expectedExceptions = CoreException.class)
//  public void testGetNonExistingVersion_negative() {
//    Version notExistversion = new Version(43, 8);
//    doReturn(null).when(vspInfoDaoMock).get(any(VspDetails.class));
//    vendorSoftwareProductManager.getVsp(VSP_ID, notExistversion);
//  }

}