package org.openecomp.sdc.vendorlicense.impl;

import org.junit.rules.ExpectedException;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.activityLog.ActivityLogManager;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDao;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDao;
import org.openecomp.sdc.vendorlicense.dao.LimitDao;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDao;
import org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseTerm;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.types.VersionedVendorLicenseModel;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.VersionedVendorSoftwareProductInfo;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.testng.annotations.BeforeMethod;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE;


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
  @Mock
  private ActivityLogManager activityLogManagerMcok;


  @Spy
  @InjectMocks
  private VendorLicenseManagerImpl vendorLicenseManager;


  @Captor
  private ArgumentCaptor<ActivityLogEntity> activityLogEntityArg;


  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCheckout() {

    doReturn(VERSION01).when(versioningManagerMcok)
        .checkout(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm1_id, USER1);

    vendorLicenseManager.checkout(vlm1_id, USER1);

    Assert.assertEquals(VERSION01.getStatus(), VersionStatus.Locked);
    verify(vendorLicenseFacadeMcok).updateVlmLastModificationTime(vlm1_id, VERSION01);

    verify(versioningManagerMcok)
        .checkout(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm1_id, USER1);

    verify(activityLogManagerMcok).addActionLog(activityLogEntityArg.capture(), eq(USER1));
    ActivityLogEntity activityLogEntity = activityLogEntityArg.getValue();
    Assert.assertEquals(activityLogEntity.getVersionId(), String.valueOf(VERSION01.getMajor() + 1));
    Assert.assertTrue(activityLogEntity.isSuccess());
  }

  @Test
  public void testUndoCheckout() {
    Version existingVersion = new Version(0, 2);
    doReturn(existingVersion).when(versioningManagerMcok).undoCheckout(
        VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm1_id, USER1);

    vendorLicenseManager.undoCheckout(vlm1_id, USER1);

    verify(vendorLicenseFacadeMcok).updateVlmLastModificationTime(vlm1_id, existingVersion);
  }

  @Test
  public void testCheckin() {

    doReturn(VERSION10).when(vendorLicenseFacadeMcok).checkin(vlm1_id, USER1);

    vendorLicenseManager.checkin(vlm1_id, USER1);
    verify(activityLogManagerMcok).addActionLog(activityLogEntityArg.capture(), eq(USER1));
    ActivityLogEntity activityLogEntity = activityLogEntityArg.getValue();
    Assert.assertEquals(activityLogEntity.getVersionId(), String.valueOf(VERSION10.getMajor() + 1));
    verify(vendorLicenseFacadeMcok).checkin(vlm1_id, USER1);

  }

  @Test
  public void testSubmit() {

    doReturn(VERSION10).when(vendorLicenseFacadeMcok).submit(vlm1_id, USER1);

    vendorLicenseManager.submit(vlm1_id, USER1);
    verify(activityLogManagerMcok).addActionLog(activityLogEntityArg.capture(), eq(USER1));
    ActivityLogEntity activityLogEntity = activityLogEntityArg.getValue();
    Assert.assertEquals(activityLogEntity.getVersionId(), String.valueOf(VERSION10.getMajor()));
    verify(vendorLicenseFacadeMcok).submit(vlm1_id, USER1);

  }

  @Test
  public void testListWhenNone() {
    doReturn(new HashMap<>()).when(versioningManagerMcok).listEntitiesVersionInfo
        (VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, USER1, VersionableEntityAction.Read);
    Collection<VersionedVendorLicenseModel> vlms = vendorLicenseManager.listVendorLicenseModels
        (null, USER1);
    Assert.assertEquals(vlms.size(), 0);
  }

  @Test
  public void testList() {

    Map<String, VersionInfo> vlmsTobeReturned = new HashMap<>();

    VersionInfo versionInfo1 = new VersionInfo();
    versionInfo1.setActiveVersion(VERSION01);
    vlmsTobeReturned.put(vlm1_id, versionInfo1);

    VersionInfo versionInfo2 = new VersionInfo();
    versionInfo2.setActiveVersion(VERSION10);
    vlmsTobeReturned.put(vlm2_id, versionInfo2);

    doReturn(vlmsTobeReturned).when(versioningManagerMcok).listEntitiesVersionInfo
        (VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, USER1, VersionableEntityAction.Read);

    VendorLicenseModelEntity vlm1 = new VendorLicenseModelEntity(vlm1_id, VERSION01);
    vlm1.setWritetimeMicroSeconds(8L);
    doReturn(vlm1).when(vendorLicenseModelDaoMcok).get(any(VendorLicenseModelEntity.class));

    Collection<VersionedVendorLicenseModel> vlms = vendorLicenseManager.listVendorLicenseModels
        (null, USER1);
    Assert.assertEquals(vlms.size(), 2);
  }

  @Test
  public void testListFinalsWhenNone() {

    Map<String, VersionInfo> vspsTobeReturned = new HashMap<>();

    VersionInfo versionInfo1 = new VersionInfo();
    versionInfo1.setActiveVersion(VERSION01);
    vspsTobeReturned.put(vlm1_id, versionInfo1);

    VersionInfo versionInfo2 = new VersionInfo();
    versionInfo2.setActiveVersion(VERSION10);
    vspsTobeReturned.put(vlm2_id, versionInfo2);

    doReturn(vspsTobeReturned).when(versioningManagerMcok).listEntitiesVersionInfo
        (VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, USER1, VersionableEntityAction.Read);

    Collection<VersionedVendorLicenseModel> vlms = vendorLicenseManager.listVendorLicenseModels
        (VersionStatus.Final.name(), USER1);
    Assert.assertEquals(vlms.size(), 0);
  }

  @Test
  public void testListFinals() {
    Map<String, VersionInfo> vlmsTobeReturned = new HashMap<>();

    VersionInfo versionInfo1 = new VersionInfo();
    versionInfo1.setActiveVersion(VERSION01);
    vlmsTobeReturned.put(vlm1_id, versionInfo1);

    VersionInfo versionInfo2 = new VersionInfo();

    versionInfo2.setActiveVersion(new Version(1, 3));
    versionInfo2.setLatestFinalVersion(VERSION10);
    vlmsTobeReturned.put(vlm2_id, versionInfo2);

    doReturn(vlmsTobeReturned).when(versioningManagerMcok).listEntitiesVersionInfo
        (VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, USER1, VersionableEntityAction.Read);

    VendorLicenseModelEntity vlm1 = new VendorLicenseModelEntity(vlm1_id, VERSION01);
    vlm1.setWritetimeMicroSeconds(8L);
    doReturn(vlm1).when(vendorLicenseModelDaoMcok).get(any(VendorLicenseModelEntity.class));

    Collection<VersionedVendorLicenseModel> vlms = vendorLicenseManager.listVendorLicenseModels
        (VersionStatus.Final.name(), USER1);

    Assert.assertEquals(vlms.size(), 1);
  }

  @Test
  public void testCreate() {

    VendorLicenseModelEntity vlmEntity = new VendorLicenseModelEntity(vlm1_id, VERSION01);

    doReturn(vlmEntity).when(vendorLicenseFacadeMcok).createVendorLicenseModel(vlmEntity, USER1);

    vendorLicenseManager.createVendorLicenseModel(vlmEntity, USER1);

    verify(vendorLicenseFacadeMcok).createVendorLicenseModel(vlmEntity, USER1);
    verify(activityLogManagerMcok).addActionLog(activityLogEntityArg.capture(), eq(USER1));
    ActivityLogEntity activityLogEntity = activityLogEntityArg.getValue();
    Assert.assertEquals(activityLogEntity.getVersionId(), String.valueOf(VERSION01.getMajor() + 1));
    Assert.assertTrue(activityLogEntity.isSuccess());

  }

  @Test
  public void testUpdate() {

    VendorLicenseModelEntity existingVlm = new VendorLicenseModelEntity();
    existingVlm.setVersion(VERSION01);
    existingVlm.setId(vlm1_id);
    existingVlm.setIconRef("icon");
    existingVlm.setVendorName("VLM1");
    existingVlm.setDescription("decription");

    VersionInfo versionInfo = new VersionInfo();
    versionInfo.setActiveVersion(VERSION01);

    doReturn(versionInfo).when(vendorLicenseManager).getVersionInfo(existingVlm.getId(),
        VersionableEntityAction.Write, USER1);

    doReturn(VERSION01).when(vendorLicenseManager).resloveVersion(vlm1_id,null, versionInfo, USER1);

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

    existingVlm.setWritetimeMicroSeconds(8L);

    doReturn(existingVlm).when(vendorLicenseModelDaoMcok).get(any(VendorLicenseModelEntity.class));

    vendorLicenseManager.updateVendorLicenseModel(updatedVlm, USER1);

    verify(vendorLicenseModelDaoMcok).update(updatedVlm);
    verify(vendorLicenseFacadeMcok).updateVlmLastModificationTime(vlm1_id, VERSION01);
  }

  @Test
  public void testGetVendorLicenseModel(){
    vendorLicenseManager.getVendorLicenseModel(vlm1_id,VERSION01,USER1);
    verify(vendorLicenseFacadeMcok).getVendorLicenseModel(vlm1_id,VERSION01,USER1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testDeleteVLMUnsupportedOperation() {
    vendorLicenseManager.deleteVendorLicenseModel(vlm1_id, USER1);
  }


//  @Test(expectedExceptions = CoreException.class)
//  public void testGetNonExistingVersion_negative() {
//    Version notExistversion = new Version(43, 8);
//    doReturn(null).when(vspInfoDaoMock).get(any(VspDetails.class));
//    vendorSoftwareProductManager.getVsp(VSP_ID, notExistversion, USER1);
//  }

}