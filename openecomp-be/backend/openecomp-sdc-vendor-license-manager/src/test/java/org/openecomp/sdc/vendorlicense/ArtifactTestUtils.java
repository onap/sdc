package org.openecomp.sdc.vendorlicense;

import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;
import org.openecomp.sdc.vendorlicense.impl.VendorLicenseManagerImpl;
import org.openecomp.sdc.vendorlicense.licenseartifacts.VendorLicenseArtifactsService;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.impl.VendorSoftwareProductManagerImpl;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.openecomp.core.utilities.CommonMethods;
import org.testng.annotations.BeforeMethod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.openecomp.sdc.vendorlicense.VendorLicenseConstants.VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE;

public class ArtifactTestUtils {

    protected static final Version VERSION01 = new Version(0, 1);
    protected static final String USER1 = "baseTest_TestUser1";
    protected static VendorLicenseManager vendorLicenseManager = new VendorLicenseManagerImpl();
    protected static VendorSoftwareProductManager vendorSoftwareProductManager = new VendorSoftwareProductManagerImpl();
    protected static VendorLicenseFacade vendorLicenseFacade = VendorLicenseFacadeFactory.getInstance().createInterface();
    private static final VersioningManager versioningManager = org.openecomp.sdc.versioning.VersioningManagerFactory
        .getInstance().createInterface();
    protected static VendorLicenseArtifactsService vendorLicenseArtifactsService = VendorLicenseArtifactServiceFactory
        .getInstance().createInterface();

    protected static Version currVersion;

    protected String vlm1Id;
    protected String vlm2Id;

    protected String ep11Id;
    protected String ep12Id;
    protected String lkg11Id;
    protected String lkg12Id;
    protected String lkg13Id;
    protected String fg11Id;
    protected String fg12Id;
    protected String la11Id;

    protected String ep21Id;
    protected String ep22Id;
    protected String lkg21Id;
    protected String lkg22Id;
    protected String fg21Id;
    protected String fg22Id;
    protected String la21Id;
    protected String la22Id;

    protected org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity fg11;
    protected org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity fg12;
    protected org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity ep11;
    protected org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity ep12;
    protected org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity lkg11;
    protected org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity lkg12;
    protected org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity lkg13;

    protected org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity fg21;
    protected org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity fg22;
    protected org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity ep21;
    protected org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity ep22;
    protected org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity lkg21;
    protected org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity lkg22;

    protected VspDetails vspDetails;
    protected VspDetails vsp2;


    @BeforeMethod
    public void setUp() {
        vlm1Id = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest.createVendorLicenseModel("vlm1 name" + CommonMethods.nextUuId(), "vlm1Id desc", "icon1"), USER1).getId();
        vlm2Id = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest.createVendorLicenseModel("vlm2 name" + CommonMethods.nextUuId(), "vlm2Id desc", "icon2"), USER1).getId();


        Set<org.openecomp.sdc.vendorlicense.dao.types.OperationalScope> opScopeChoices = new HashSet<>();
        opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Other);
        opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Data_Center);
        opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Network_Wide);

        ep11 = EntitlementPoolTest.createEntitlementPool(vlm1Id, VERSION01, "EP1_" + CommonMethods.nextUuId(), "EP1 dec", 80, org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit.Absolute, org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric.Core, null, "inc1", org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction.Other, "agg func1", opScopeChoices, null, org.openecomp.sdc.vendorlicense.dao.types.EntitlementTime.Hour, null, "sku1");
        ep11Id = vendorLicenseManager.createEntitlementPool(ep11, USER1).getId();
        ep12 = EntitlementPoolTest.createEntitlementPool(vlm1Id, VERSION01, "EP2_" + CommonMethods.nextUuId(), "EP2 dec", 70, org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit.Absolute, org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric.Other, "e metric2", "inc2", org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction.Average, null, opScopeChoices, "op scope2", org.openecomp.sdc.vendorlicense.dao.types.EntitlementTime.Other, "time2", "sku2");
        ep12Id = vendorLicenseManager.createEntitlementPool(ep12, USER1).getId();

        Set<org.openecomp.sdc.vendorlicense.dao.types.OperationalScope> opScopeChoicesLKG = new HashSet<>();
        opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.CPU);
        opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.VM);
        opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Availability_Zone);
        opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Data_Center);

        lkg11 = LicenseKeyGroupTest.createLicenseKeyGroup(vlm1Id, VERSION01, "LKG1", "LKG1 dec", org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyType.One_Time, new org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther<>(opScopeChoicesLKG, null));
        lkg11Id = vendorLicenseManager.createLicenseKeyGroup(lkg11, USER1).getId();
        lkg11.setId(lkg11Id);

        lkg12 = LicenseKeyGroupTest.createLicenseKeyGroup(vlm1Id, VERSION01, "LKG2", "LKG2 dec", org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyType.Unique, new org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther<>(opScopeChoicesLKG, null));
        lkg12Id = vendorLicenseManager.createLicenseKeyGroup(lkg12, USER1).getId();
        lkg12.setId(lkg11Id);

        lkg13 = LicenseKeyGroupTest.createLicenseKeyGroup(vlm1Id, VERSION01, "LKG3", "LKG3 dec", org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyType.Universal, new org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther<>(opScopeChoicesLKG, null));
        lkg13Id = vendorLicenseManager.createLicenseKeyGroup(lkg13, USER1).getId();
        lkg13.setId(lkg13Id);

        fg11 = LicenseAgreementTest.createFeatureGroup(vlm1Id, VERSION01, "fg11", "FG1", "FG1 desc", CommonMethods.toSingleElementSet(ep11Id), CommonMethods.toSingleElementSet(lkg11Id));
        fg11Id = vendorLicenseManager.createFeatureGroup(fg11, USER1).getId();

        fg12 = LicenseAgreementTest.createFeatureGroup(vlm1Id, VERSION01, "fg2", "FG2", "FG2 desc", CommonMethods.toSingleElementSet(ep12Id), CommonMethods.toSingleElementSet(lkg12Id));
        fg12Id = vendorLicenseManager.createFeatureGroup(fg12, USER1).getId();


        String requirementsAndConstrains1 = "Requirements And Constraints1";
        org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity
            la1 = LicenseAgreementTest.createLicenseAgreement(vlm1Id, VERSION01, null, "LA1", "LA1 desc", requirementsAndConstrains1, new org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther<>(
            org.openecomp.sdc.vendorlicense.dao.types.LicenseTerm.Unlimited, null), fg11Id);
        la11Id = vendorLicenseManager.createLicenseAgreement(la1, USER1).getId();

        List<String> fgs = new ArrayList<>();
        fgs.add(fg11Id);
        createTwoFinalVersionsForVLM(vlm1Id);
        VersionInfo versionInfo = vendorLicenseFacade.getVersionInfo(vlm1Id, VersionableEntityAction.Read, "");
        vspDetails = createVspDetails(null, null, "VSP1_" + CommonMethods.nextUuId(), "Test-vsp", "vendorName", vlm1Id, "icon", "category", "subCategory", la11Id, fgs);

        List<Version> finalVersions = versionInfo.getFinalVersions();
        Version finalVersion = finalVersions.get(1);

        vspDetails.setVlmVersion(finalVersion);

        vspDetails = vendorSoftwareProductManager.createNewVsp(vspDetails, USER1);

    }

    private void createTwoFinalVersionsForVLM(String vlm1Id) {
        versioningManager.checkin(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm1Id, USER1, "desc1");
        versioningManager.checkout(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm1Id, USER1);
        versioningManager.checkin(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm1Id, USER1, "desc1");
        vendorLicenseFacade.submit(vlm1Id, USER1);
        versioningManager.checkout(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm1Id, USER1);
        versioningManager.checkin(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm1Id, USER1, "desc2");
        vendorLicenseFacade.submit(vlm1Id, USER1);

    }

    protected void createThirdFinalVersionForVLMChangeEpLKGInSome(String vlm1Id, org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity ep, org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity lkg) {
        versioningManager.checkout(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm1Id, USER1);
        vendorLicenseManager.updateEntitlementPool(ep, USER1);
        vendorLicenseManager.updateLicenseKeyGroup(lkg, USER1);
        versioningManager.checkin(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm1Id, USER1, "desc1");
        vendorLicenseFacade.submit(vlm1Id, USER1);

    }


    protected void setVlm2FirstVersion() {
        Set<org.openecomp.sdc.vendorlicense.dao.types.OperationalScope> opScopeChoices = new HashSet<>();
        opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Other);
        opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Data_Center);
        opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Network_Wide);

        Set<org.openecomp.sdc.vendorlicense.dao.types.OperationalScope> opScopeChoicesLKG = new HashSet<>();
        opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.CPU);
        opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.VM);
        opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Availability_Zone);
        opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Data_Center);

        ep21 = EntitlementPoolTest.createEntitlementPool(vlm2Id, VERSION01, "EP21", "EP21 dec", 80, org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit.Absolute, org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric.Core, null, "inc21", org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction.Other, "agg func21", opScopeChoices, null, org.openecomp.sdc.vendorlicense.dao.types.EntitlementTime.Hour, null, "sku21");
        ep21Id = vendorLicenseManager.createEntitlementPool(ep21, USER1).getId();

        lkg21 = LicenseKeyGroupTest.createLicenseKeyGroup(vlm2Id, VERSION01, "LKG21", "LKG21 dec", org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyType.One_Time, new org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther<>(opScopeChoicesLKG, null));
        lkg21Id = vendorLicenseManager.createLicenseKeyGroup(lkg21, USER1).getId();
        lkg21.setId(lkg21Id);

        fg21 = LicenseAgreementTest.createFeatureGroup(vlm2Id, VERSION01, "fg21", "FG21", "FG21 desc", CommonMethods.toSingleElementSet(ep21Id), CommonMethods.toSingleElementSet(lkg21Id));
        fg21Id = vendorLicenseManager.createFeatureGroup(fg21, USER1).getId();

        String requirementsAndConstrains1 = "Requirements And Constraints21";
        org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity
            la2 = LicenseAgreementTest.createLicenseAgreement(vlm2Id, VERSION01, null, "LA21", "LA21 desc", requirementsAndConstrains1, new org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther<>(
            org.openecomp.sdc.vendorlicense.dao.types.LicenseTerm.Unlimited, null), fg21Id);
        la21Id = vendorLicenseManager.createLicenseAgreement(la2, USER1).getId();

//        setValuesForVlm(VERSION01, ep21, ep21Id, lkg21, lkg21Id, fg21, fg21Id, la21Id, 1);

        vendorLicenseManager.checkin(vlm2Id, USER1);
        currVersion = versioningManager.submit(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm2Id, USER1, null);

        List<String> fgs = new ArrayList<>();
        fgs.add(fg21Id);
        vsp2 = createVspDetails(null, null, "VSP2_" + CommonMethods.nextUuId(), "Test-vsp", "vendorName", vlm2Id, "icon", "category", "subCategory", la21Id, fgs);
        vsp2 = vendorSoftwareProductManager.createNewVsp(vsp2, USER1);
    }

    protected void setVlm2SecondVersion() {
        vendorLicenseManager.checkout(vlm2Id, USER1);

        Set<org.openecomp.sdc.vendorlicense.dao.types.OperationalScope> opScopeChoices = new HashSet<>();
        opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Other);
        opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Data_Center);
        opScopeChoices.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Network_Wide);

        Set<org.openecomp.sdc.vendorlicense.dao.types.OperationalScope> opScopeChoicesLKG = new HashSet<>();
        opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.CPU);
        opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.VM);
        opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Availability_Zone);
        opScopeChoicesLKG.add(org.openecomp.sdc.vendorlicense.dao.types.OperationalScope.Data_Center);

        ep22 = EntitlementPoolTest.createEntitlementPool(vlm2Id, currVersion, "EP22", "EP22 dec", 80, org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit.Absolute, org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric.Core, null, "inc22", org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction.Other, "agg func22", opScopeChoices, null, org.openecomp.sdc.vendorlicense.dao.types.EntitlementTime.Hour, null, "sku22");
        ep22Id = vendorLicenseManager.createEntitlementPool(ep22, USER1).getId();

        lkg22 = LicenseKeyGroupTest.createLicenseKeyGroup(vlm2Id, currVersion, "LKG22", "LKG22 dec", org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyType.One_Time, new org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther<>(opScopeChoicesLKG, null));
        lkg22Id = vendorLicenseManager.createLicenseKeyGroup(lkg22, USER1).getId();
        lkg22.setId(lkg22Id);

        fg22 = LicenseAgreementTest.createFeatureGroup(vlm2Id, currVersion, "fg22", "FG22", "FG22 desc", CommonMethods.toSingleElementSet(ep22Id), CommonMethods.toSingleElementSet(lkg22Id));
        fg22Id = vendorLicenseManager.createFeatureGroup(fg22, USER1).getId();

        String requirementsAndConstrains1 = "Requirements And Constraints22";
        org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity
            la2 = LicenseAgreementTest.createLicenseAgreement(vlm2Id, currVersion, null, "LA22", "LA22 desc", requirementsAndConstrains1, new org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther<>(
            org.openecomp.sdc.vendorlicense.dao.types.LicenseTerm.Unlimited, null), fg22Id);
        la22Id = vendorLicenseManager.createLicenseAgreement(la2, USER1).getId();

//        setValuesForVlm(currVersion, ep22, ep22Id, lkg22, lkg22Id, fg22, fg22Id, la22Id, 2);

        vendorLicenseManager.checkin(vlm2Id, USER1);
        currVersion = versioningManager.submit(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm2Id, USER1, null);
    }

    protected static VspDetails createVspDetails(String id, Version version, String name, String desc, String vendorName, String vlm, String icon, String category, String subCategory, String licenseAgreement, List<String> featureGroups) {
        VspDetails vspDetails = new VspDetails(id, version);
        vspDetails.setName(name);
        vspDetails.setDescription(desc);
        vspDetails.setIcon(icon);
        vspDetails.setCategory(category);
        vspDetails.setSubCategory(subCategory);
        vspDetails.setVendorName(vendorName);
        vspDetails.setVendorId(vlm);
        vspDetails.setLicenseAgreement(licenseAgreement);
        vspDetails.setFeatureGroups(featureGroups);
        return vspDetails;
    }


}

