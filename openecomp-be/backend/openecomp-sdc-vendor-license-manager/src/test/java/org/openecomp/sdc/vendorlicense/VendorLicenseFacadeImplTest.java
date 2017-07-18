package org.openecomp.sdc.vendorlicense;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.vendorlicense.dao.*;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.facade.impl.VendorLicenseFacadeImpl;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by diveshm on 7/3/2017.
 */
public class VendorLicenseFacadeImplTest {
    //JUnit Test Cases using Mockito
    private static final Version VERSION01 = new Version(0, 1);
    private final String FG1_NAME = "FG1 name";

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

    public FeatureGroupEntity createFeatureGroup(String vlmId, Version version, String id, String name, String desc,
                                                 String partNumber, String manufacturerReferenceNumber, Set<String>
                                                         licenseKeyGroupIds, Set<String> entitlementPoolIds, Set<String>
                                                         referencingLicenseAgreements){
        FeatureGroupEntity featureGroup = new FeatureGroupEntity(vlmId, version, id);
        featureGroup.setVendorLicenseModelId(vlmId);
        featureGroup.setVersion(version);
        featureGroup.setId(id);
        featureGroup.setName(name);
        featureGroup.setDescription(desc);
        featureGroup.setPartNumber(partNumber);
        //featureGroup.setManufacturerReferenceNumber(manufacturerReferenceNumber);
        featureGroup.setLicenseKeyGroupIds(licenseKeyGroupIds);
        featureGroup.setEntitlementPoolIds(entitlementPoolIds);
        featureGroup.setReferencingLicenseAgreements(referencingLicenseAgreements);

        return featureGroup;
    }

    @BeforeMethod
    public void setUp() throws Exception{
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreate(){
        Set<String> licenseKeyGroupIds;
        licenseKeyGroupIds = new HashSet<>();
        licenseKeyGroupIds.add("lkg1");

        Set<String> entitlementPoolIds;
        entitlementPoolIds = new HashSet<>();
        entitlementPoolIds.add("ep1");

        Set<String> referencingLicenseAgreements;
        referencingLicenseAgreements = new HashSet<>();
        referencingLicenseAgreements.add("la1");

        FeatureGroupEntity featureGroupEntity = createFeatureGroup("vlmId", VERSION01, "fgId", FG1_NAME, "fg1 desc",
                "partNumber", "MRN", licenseKeyGroupIds, entitlementPoolIds,
                referencingLicenseAgreements);

        doReturn(featureGroupEntity).when(featureGroupDao).get(anyObject());

        /*if(featureGroupEntity.getManufacturerReferenceNumber() != null)
            featureGroupDao.create(featureGroupEntity);
        verify(featureGroupDao).create(anyObject());*/
    }

    @Test
    public void testCreateWithoutManufacturerReferenceNumber(){
        Set<String> licenseKeyGroupIds;
        licenseKeyGroupIds = new HashSet<>();
        licenseKeyGroupIds.add("lkg1");

        Set<String> entitlementPoolIds;
        entitlementPoolIds = new HashSet<>();
        entitlementPoolIds.add("ep1");

        Set<String> referencingLicenseAgreements;
        referencingLicenseAgreements = new HashSet<>();
        referencingLicenseAgreements.add("la1");

        FeatureGroupEntity featureGroupEntity = createFeatureGroup("vlmId", VERSION01, "fgId", FG1_NAME, "fg1 desc",
                "partNumber", null, licenseKeyGroupIds, entitlementPoolIds,
                referencingLicenseAgreements);
        doReturn(featureGroupEntity).when(featureGroupDao).get(anyObject());

        /*if(featureGroupEntity.getManufacturerReferenceNumber() != null)
            featureGroupDao.create(featureGroupEntity);

        verify(featureGroupDao, never()).create(anyObject());*/

    }
}
