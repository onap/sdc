package org.openecomp.sdc.vendorlicense;

import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.vendorlicense.dao.*;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.facade.impl.VendorLicenseFacadeImpl;
import org.openecomp.sdc.vendorlicense.impl.VendorLicenseManagerImpl;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * This test just verifies Feature Group Get and List APIs.
 */
public class VendorLicenseFacadeImplTest {
    //JUnit Test Cases using Mockito
    private static final Version VERSION01 = new Version(0, 1);
    public static final String EP1 = "ep1";
    public static final String MRN = "mrn";
    public static final String VLM_ID = "VLM_ID";
    public static final String USER = "USER1";


    @Mock
    private VendorLicenseModelDao vendorLicenseModelDao;

    @Mock
    private LicenseAgreementDao licenseAgreementDao;

    @Mock
    private FeatureGroupDao featureGroupDao;

    @Mock
    private EntitlementPoolDao entitlementPoolDao;

    @Mock
    private LicenseKeyGroupDao licenseKeyGroupDao;

    @Mock
    private VersioningManager versioningManager;

    @InjectMocks
    @Spy
    private VendorLicenseFacadeImpl vendorLicenseFacadeImpl;

    @BeforeMethod
    public void setUp() throws Exception{
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetFeatureGroupWhenMRNNull () {
        resetFieldModifiers();

        FeatureGroupEntity featureGroup = createFeatureGroup();

        VersionInfo info = new VersionInfo();
        info.getViewableVersions().add(VERSION01);
        info.setActiveVersion(VERSION01);

        Set<String> entitlementPoolIds;
        entitlementPoolIds = new HashSet<>();
        entitlementPoolIds.add(EP1);

        EntitlementPoolEntity ep = createEP();

        featureGroup.setEntitlementPoolIds(entitlementPoolIds);

        doReturn(info).when(vendorLicenseFacadeImpl).getVersionInfo(anyObject(),anyObject(),anyObject());
        doReturn(featureGroup).when(featureGroupDao).get(featureGroup);
        doReturn(ep).when(entitlementPoolDao).get(anyObject());
        doReturn(MRN).when(entitlementPoolDao).getManufacturerReferenceNumber(anyObject());
        FeatureGroupEntity retrieved = vendorLicenseFacadeImpl.getFeatureGroup(featureGroup, USER);
        Assert.assertEquals(MRN, retrieved.getManufacturerReferenceNumber());
    }

    @Test
    public void testListFeatureGroups () {
        resetFieldModifiers();

        FeatureGroupEntity featureGroup = createFeatureGroup();

        Collection<FeatureGroupEntity> featureGroups = new ArrayList<FeatureGroupEntity>();
        featureGroups.add(featureGroup);

        VersionInfo info = new VersionInfo();
        info.getViewableVersions().add(VERSION01);
        info.setActiveVersion(VERSION01);

        EntitlementPoolEntity ep = createEP();

        doReturn(info).when(vendorLicenseFacadeImpl).getVersionInfo(anyObject(),anyObject(),anyObject());
        doReturn(featureGroup).when(featureGroupDao).get(featureGroup);
        doReturn(ep).when(entitlementPoolDao).get(anyObject());
        doReturn(MRN).when(entitlementPoolDao).getManufacturerReferenceNumber(anyObject());
        Collection<FeatureGroupEntity> retrieved = vendorLicenseFacadeImpl.listFeatureGroups(VLM_ID,
            VERSION01, USER);
        retrieved.stream().forEach(fg -> Assert.assertEquals(MRN,fg.getManufacturerReferenceNumber()));
    }

    private void resetFieldModifiers() {
        try {
            Field fgField = VendorLicenseFacadeImpl.class.getDeclaredField("featureGroupDao");
            fgField.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(fgField, fgField.getModifiers() & ~Modifier.FINAL);
            fgField.set(null, featureGroupDao);

            Field epField = VendorLicenseFacadeImpl.class.getDeclaredField("entitlementPoolDao");
            epField.setAccessible(true);
            modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(epField, epField.getModifiers() & ~Modifier.FINAL);
            epField.set(null, entitlementPoolDao);
        } catch(NoSuchFieldException | IllegalAccessException e)
        {
            org.testng.Assert.fail();
        }
    }

    private FeatureGroupEntity createFeatureGroup() {
        FeatureGroupEntity featureGroup = new FeatureGroupEntity(VLM_ID, VERSION01, USER);
        featureGroup.setManufacturerReferenceNumber(null);
        VersionInfo info = new VersionInfo();
        info.getViewableVersions().add(VERSION01);
        info.setActiveVersion(VERSION01);

        Set<String> entitlementPoolIds;
        entitlementPoolIds = new HashSet<>();
        entitlementPoolIds.add(EP1);

        featureGroup.setEntitlementPoolIds(entitlementPoolIds);
        return featureGroup;
    }

    private EntitlementPoolEntity createEP() {
        EntitlementPoolEntity ep = new EntitlementPoolEntity(VLM_ID,VERSION01, EP1);
        ep.setManufacturerReferenceNumber(MRN);
        return ep;
    }

}
