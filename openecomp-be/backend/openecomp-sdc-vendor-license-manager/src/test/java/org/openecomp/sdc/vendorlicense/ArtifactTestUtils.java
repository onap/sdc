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

package org.openecomp.sdc.vendorlicense;

/**
 * Created by  Katyr on 29-May-16
 */
public class ArtifactTestUtils {
/*

  protected static final Version VERSION01 = new Version(0, 1);
  protected static final String USER1 = "baseTest_TestUser1";
  private static final VersioningManager versioningManager =
      VersioningManagerFactory.getInstance().createInterface();
  protected static VendorLicenseManager vendorLicenseManager = new VendorLicenseManagerImpl();
  protected static VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  protected static VendorLicenseFacade vendorLicenseFacade =
      VendorLicenseFacadeFactory.getInstance().createInterface();
  protected static VendorLicenseArtifactsService vendorLicenseArtifactsService =
      VendorLicenseArtifactServiceFactory.getInstance().createInterface();
  protected static Version currVersion;

  protected String vlm1Id;
  protected String vlm2Id;

  protected String vlm3Id;
  protected EntitlementPoolEntity ep3_1;
  protected String ep3_1Id;
  protected LicenseKeyGroupEntity lkg3_1;
  protected String lkg3_1Id;


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

  protected FeatureGroupEntity fg11;
  protected FeatureGroupEntity fg12;
  protected EntitlementPoolEntity ep11;
  protected EntitlementPoolEntity ep12;
  protected LicenseKeyGroupEntity lkg11;
  protected LicenseKeyGroupEntity lkg12;
  protected LicenseKeyGroupEntity lkg13;

  protected FeatureGroupEntity fg21;
  protected FeatureGroupEntity fg22;
  protected EntitlementPoolEntity ep21;
  protected EntitlementPoolEntity ep22;
  protected LicenseKeyGroupEntity lkg21;
  protected LicenseKeyGroupEntity lkg22;

  protected VspDetails vspDetails;
  protected VspDetails vsp2;
  protected VspDetails vspDetailsVsp3;
  private Set opScopeChoices;
  private Set opScopeChoicesLKG;
  private String la3_1Id;
  protected List featureGroupsforVlm3;
  protected LicenseAgreementEntity licenseAgreementVlm3;

  public enum OnboardingMethod {
    HEAT("HEAT"),
    Manual("Manual");


    OnboardingMethod(String method) {
      this.method = method;
    }

    private  String method;
  }


  protected static VspDetails createVspDetails(String id, Version version, String name, String desc,
                                               String vendorName, String vlm, String icon,
                                               String category, String subCategory,
                                               String licenseAgreement,
                                               List<String> featureGroups,
                                               String onboardingMethod) {
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
    vspDetails.setOnboardingMethod(onboardingMethod);
    return vspDetails;
  }

  @BeforeMethod
  public void setUp() {

    opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Other);
    opScopeChoices.add(OperationalScope.Data_Center);
    opScopeChoices.add(OperationalScope.Network_Wide);

    opScopeChoicesLKG = new HashSet<>();
    opScopeChoicesLKG.add(OperationalScope.CPU);
    opScopeChoicesLKG.add(OperationalScope.VM);
    opScopeChoicesLKG.add(OperationalScope.Availability_Zone);
    opScopeChoicesLKG.add(OperationalScope.Data_Center);

    vlm1Id = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest
            .createVendorLicenseModel("vlm1 name_" + CommonMethods.nextUuId(), "vlm1Id desc",
                "icon1"),
        USER1).getId();
    vlm2Id = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest
            .createVendorLicenseModel("vlm2 name_" + CommonMethods.nextUuId(), "vlm2Id desc",
                "icon2"),
        USER1).getId();
//    vlm3Id = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest
//            .createVendorLicenseModel("vlm3 name" + CommonMethods.nextUuId(), "vlm3Id desc",
//                "icon2"),
//        USER1).getId();


    Set<OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Other);
    opScopeChoices.add(OperationalScope.Data_Center);
    opScopeChoices.add(OperationalScope.Network_Wide);

    ep11 = EntitlementPoolTest
        .createEntitlementPool(vlm1Id, VERSION01, "EP1_" + CommonMethods.nextUuId(), "EP1 dec", 80,
            ThresholdUnit.Absolute, EntitlementMetric.Core, null, "inc1", AggregationFunction.Other,
            "agg func1", opScopeChoices, null, EntitlementTime.Hour, null, "sku1");
    ep11Id = vendorLicenseManager.createEntitlementPool(ep11, USER1).getId();
    ep12 = EntitlementPoolTest
        .createEntitlementPool(vlm1Id, VERSION01, "EP2_" + CommonMethods.nextUuId(), "EP2 dec", 70,
            ThresholdUnit.Absolute, EntitlementMetric.Other, "exception metric2", "inc2",
            AggregationFunction.Average, null, opScopeChoices, "op scope2", EntitlementTime.Other,
            "time2", "sku2");
    ep12Id = vendorLicenseManager.createEntitlementPool(ep12, USER1).getId();

    Set<OperationalScope> opScopeChoicesLKG = new HashSet<>();
    opScopeChoicesLKG.add(OperationalScope.CPU);
    opScopeChoicesLKG.add(OperationalScope.VM);
    opScopeChoicesLKG.add(OperationalScope.Availability_Zone);
    opScopeChoicesLKG.add(OperationalScope.Data_Center);

    lkg11 = LicenseKeyGroupTest
        .createLicenseKeyGroup(vlm1Id, VERSION01, "LKG1", "LKG1 dec", LicenseKeyType.One_Time,
            new MultiChoiceOrOther<>(opScopeChoicesLKG, null));
    lkg11Id = vendorLicenseManager.createLicenseKeyGroup(lkg11, USER1).getId();
    lkg11.setId(lkg11Id);

    lkg12 = LicenseKeyGroupTest
        .createLicenseKeyGroup(vlm1Id, VERSION01, "LKG2", "LKG2 dec", LicenseKeyType.Unique,
            new MultiChoiceOrOther<>(opScopeChoicesLKG, null));
    lkg12Id = vendorLicenseManager.createLicenseKeyGroup(lkg12, USER1).getId();
    lkg12.setId(lkg11Id);

    lkg13 = LicenseKeyGroupTest
        .createLicenseKeyGroup(vlm1Id, VERSION01, "LKG3", "LKG3 dec", LicenseKeyType.Universal,
            new MultiChoiceOrOther<>(opScopeChoicesLKG, null));
    lkg13Id = vendorLicenseManager.createLicenseKeyGroup(lkg13, USER1).getId();
    lkg13.setId(lkg13Id);

    fg11 = LicenseAgreementTest.createFeatureGroup(vlm1Id, VERSION01, "fg11", "FG1", "FG1 desc",
        CommonMethods.toSingleElementSet(ep11Id), CommonMethods.toSingleElementSet(lkg11Id));
    fg11Id = vendorLicenseManager.createFeatureGroup(fg11, USER1).getId();

    fg12 = LicenseAgreementTest.createFeatureGroup(vlm1Id, VERSION01, "fg2", "FG2", "FG2 desc",
        CommonMethods.toSingleElementSet(ep12Id), CommonMethods.toSingleElementSet(lkg12Id));
    fg12Id = vendorLicenseManager.createFeatureGroup(fg12, USER1).getId();


    String requirementsAndConstrains1 = "Requirements And Constraints1";
    LicenseAgreementEntity
        la1 = LicenseAgreementTest
        .createLicenseAgreement(vlm1Id, VERSION01, null, "LA1", "LA1 desc",
            requirementsAndConstrains1, new ChoiceOrOther<>(
                LicenseTerm.Unlimited, null), fg11Id);
    la11Id = vendorLicenseManager.createLicenseAgreement(la1, USER1).getId();

    List<String> fgs = new ArrayList<>();
    fgs.add(fg11Id);
    createTwoFinalVersionsForVLM(vlm1Id);
    VersionInfo versionInfo =
        vendorLicenseFacade.getVersionInfo(vlm1Id, VersionableEntityAction.Read, "");
    vspDetails =
        createVspDetails(null, null, "VSP1_" + CommonMethods.nextUuId(), "Test-vsp", "vendorName",
            vlm1Id, "icon", "category", "subCategory", la11Id, fgs, ArtifactTestUtils
                .OnboardingMethod.HEAT.name());

    List<Version> finalVersions = versionInfo.getFinalVersions();
    Version finalVersion = finalVersions.get(1);

    vspDetails.setVlmVersion(finalVersion);

    vspDetails = vendorSoftwareProductManager.createVsp(vspDetails, USER1);

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

  protected VspDetails createVspWithSpecifiedVlmVersion(String vlmToUse, Version vlmVersionToUse,
                                                        List<String> fgs, String
                                                            licenceAgreementId) {
    vspDetailsVsp3 =
        createVspDetails(null, null, "VSP3_" + CommonMethods.nextUuId(), "VSP3",
            "vendorName",
            vlm3Id, "icon", "category", "subCategory", licenceAgreementId, fgs, OnboardingMethod
                .HEAT.name());
    VersionInfo versionInfo =
        vendorLicenseFacade.getVersionInfo(vlmToUse, VersionableEntityAction.Read, "");


    vspDetailsVsp3.setVlmVersion(vlmVersionToUse);

    return vendorSoftwareProductManager.createVsp(vspDetailsVsp3, USER1);
  }

  protected void createAndSetupVlm3() {
    vlm3Id = vendorLicenseFacade.createVendorLicenseModel(VendorLicenseModelTest
            .createVendorLicenseModel("vlm3 name" + CommonMethods.nextUuId(), "vlm3Id desc",
                "icon2"),
        USER1).getId();
    ep3_1 = EntitlementPoolTest
        .createEntitlementPool(vlm3Id, VERSION01, "EP3_" + CommonMethods.nextUuId(), "EP3 dec",
            80,
            ThresholdUnit.Absolute, EntitlementMetric.Core, null, "inc1", AggregationFunction.Other,
            "agg func1", opScopeChoices, null, EntitlementTime.Quarter, null, "sku1");
    ep3_1Id = vendorLicenseManager.createEntitlementPool(ep3_1, USER1).getId();
    lkg3_1 = LicenseKeyGroupTest
        .createLicenseKeyGroup(vlm3Id, VERSION01, "LKG3_" + CommonMethods.nextUuId(), "LKG3 dec",
            LicenseKeyType.Unique,
            new MultiChoiceOrOther<>(opScopeChoicesLKG, null));
    lkg3_1Id = vendorLicenseManager.createLicenseKeyGroup(lkg3_1, USER1).getId();
    lkg3_1.setId(lkg3_1Id);

    FeatureGroupEntity fg3 = LicenseAgreementTest.createFeatureGroup(vlm3Id, VERSION01,
        "fg3" + CommonMethods.nextUuId(),
        "FG3", "FG3 " +
            "desc",
        CommonMethods.toSingleElementSet(ep3_1Id), CommonMethods.toSingleElementSet(lkg3_1Id));
    String fg3Id = vendorLicenseManager.createFeatureGroup(fg3, USER1).getId();
    String requirementsAndConstrains1 = "Requirements And Constraints1";
    licenseAgreementVlm3 = LicenseAgreementTest
        .createLicenseAgreement(vlm3Id, VERSION01, null, "LA3", "LA1 desc",
            requirementsAndConstrains1, new ChoiceOrOther<>(
                LicenseTerm.Unlimited, null), fg3Id);
    String la3_1Id =
        vendorLicenseManager.createLicenseAgreement(licenseAgreementVlm3, USER1).getId();

    featureGroupsforVlm3 = new ArrayList<>();
    featureGroupsforVlm3.add(fg3Id);

  }

  protected void addEpToVLM(String vlmToAddEP) {
    versioningManager.checkin(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlmToAddEP, USER1, "desc1");
    versioningManager.checkout(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlmToAddEP, USER1);
    versioningManager.checkin(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlmToAddEP, USER1, "desc1");
    vendorLicenseFacade.submit(vlmToAddEP, USER1);


    versioningManager.checkout(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlmToAddEP, USER1);

    EntitlementPoolEntity addedEp = EntitlementPoolTest
        .createEntitlementPool(vlmToAddEP, VERSION01, "EP_ADDED" + CommonMethods.nextUuId(),
            "EP_ADDED" +
                " desc" + CommonMethods.nextUuId(), 99,
            ThresholdUnit
                .Absolute,
            EntitlementMetric.Core, null, "inc21", AggregationFunction.Other, "agg func21",
            opScopeChoices, null, EntitlementTime.Hour, null, "sku21");
    String addedEpId = vendorLicenseManager.createEntitlementPool(addedEp, USER1).getId();
    versioningManager.checkin(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlmToAddEP, USER1, "desc1");
    vendorLicenseFacade.submit(vlmToAddEP, USER1);
    VersionInfo versionInfo =
        vendorLicenseFacade.getVersionInfo(vlm3Id, VersionableEntityAction.Read, "");
  }

  protected void createThirdFinalVersionForVLMChangeEpLKGInSome(String vlm1Id,
                                                                EntitlementPoolEntity ep,
                                                                LicenseKeyGroupEntity lkg) {
    versioningManager.checkout(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm1Id, USER1);
    vendorLicenseManager.updateEntitlementPool(ep, USER1);
    vendorLicenseManager.updateLicenseKeyGroup(lkg, USER1);
    versioningManager.checkin(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm1Id, USER1, "desc1");
    vendorLicenseFacade.submit(vlm1Id, USER1);

  }

  protected void setVlm2FirstVersion() {


    ep21 = EntitlementPoolTest
        .createEntitlementPool(vlm2Id, VERSION01, "EP21", "EP21 dec", 80, ThresholdUnit.Absolute,
            EntitlementMetric.Core, null, "inc21", AggregationFunction.Other, "agg func21",
            opScopeChoices, null, EntitlementTime.Hour, null, "sku21");
    ep21Id = vendorLicenseManager.createEntitlementPool(ep21, USER1).getId();

    lkg21 = LicenseKeyGroupTest
        .createLicenseKeyGroup(vlm2Id, VERSION01, "LKG21", "LKG21 dec", LicenseKeyType.One_Time,
            new MultiChoiceOrOther<>(opScopeChoicesLKG, null));
    lkg21Id = vendorLicenseManager.createLicenseKeyGroup(lkg21, USER1).getId();
    lkg21.setId(lkg21Id);

    fg21 = LicenseAgreementTest.createFeatureGroup(vlm2Id, VERSION01, "fg21", "FG21", "FG21 desc",
        CommonMethods.toSingleElementSet(ep21Id), CommonMethods.toSingleElementSet(lkg21Id));
    fg21Id = vendorLicenseManager.createFeatureGroup(fg21, USER1).getId();

    String requirementsAndConstrains1 = "Requirements And Constraints21";
    LicenseAgreementEntity la2 = LicenseAgreementTest
        .createLicenseAgreement(vlm2Id, VERSION01, null, "LA21", "LA21 desc",
            requirementsAndConstrains1, new ChoiceOrOther<>(LicenseTerm.Unlimited, null), fg21Id);
    la21Id = vendorLicenseManager.createLicenseAgreement(la2, USER1).getId();

//        setValuesForVlm(VERSION01, ep21, ep21Id, lkg21, lkg21Id, fg21, fg21Id, la21Id, 1);

    vendorLicenseManager.checkin(vlm2Id, USER1);
    currVersion =
        versioningManager.submit(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm2Id, USER1, null);

    List<String> fgs = new ArrayList<>();
    fgs.add(fg21Id);
    vsp2 =
        createVspDetails(null, null, "VSP2_" + CommonMethods.nextUuId(), "Test-vsp", "vendorName",
            vlm2Id, "icon", "category", "subCategory", la21Id, fgs, OnboardingMethod.HEAT.name());
    vsp2 = vendorSoftwareProductManager.createVsp(vsp2, USER1);
  }

  protected void setVlm2SecondVersion() {
    vendorLicenseManager.checkout(vlm2Id, USER1);

    Set<OperationalScope> opScopeChoices = new HashSet<>();
    opScopeChoices.add(OperationalScope.Other);
    opScopeChoices.add(OperationalScope.Data_Center);
    opScopeChoices.add(OperationalScope.Network_Wide);

    Set<OperationalScope> opScopeChoicesLKG = new HashSet<>();
    opScopeChoicesLKG.add(OperationalScope.CPU);
    opScopeChoicesLKG.add(OperationalScope.VM);
    opScopeChoicesLKG.add(OperationalScope.Availability_Zone);
    opScopeChoicesLKG.add(OperationalScope.Data_Center);

    ep22 = EntitlementPoolTest
        .createEntitlementPool(vlm2Id, currVersion, "EP22", "EP22 dec", 80, ThresholdUnit.Absolute,
            EntitlementMetric.Core, null, "inc22", AggregationFunction.Other, "agg func22",
            opScopeChoices, null, EntitlementTime.Hour, null, "sku22");
    ep22Id = vendorLicenseManager.createEntitlementPool(ep22, USER1).getId();

    lkg22 = LicenseKeyGroupTest
        .createLicenseKeyGroup(vlm2Id, currVersion, "LKG22", "LKG22 dec", LicenseKeyType.One_Time,
            new MultiChoiceOrOther<>(opScopeChoicesLKG, null));
    lkg22Id = vendorLicenseManager.createLicenseKeyGroup(lkg22, USER1).getId();
    lkg22.setId(lkg22Id);

    fg22 = LicenseAgreementTest.createFeatureGroup(vlm2Id, currVersion, "fg22", "FG22", "FG22 desc",
        CommonMethods.toSingleElementSet(ep22Id), CommonMethods.toSingleElementSet(lkg22Id));
    fg22Id = vendorLicenseManager.createFeatureGroup(fg22, USER1).getId();

    String requirementsAndConstrains1 = "Requirements And Constraints22";
    LicenseAgreementEntity la2 = LicenseAgreementTest
        .createLicenseAgreement(vlm2Id, currVersion, null, "LA22", "LA22 desc",
            requirementsAndConstrains1, new ChoiceOrOther<>(LicenseTerm.Unlimited, null), fg22Id);
    la22Id = vendorLicenseManager.createLicenseAgreement(la2, USER1).getId();

//        setValuesForVlm(currVersion, ep22, ep22Id, lkg22, lkg22Id, fg22, fg22Id, la22Id, 2);

    vendorLicenseManager.checkin(vlm2Id, USER1);
    currVersion =
        versioningManager.submit(VENDOR_LICENSE_MODEL_VERSIONABLE_TYPE, vlm2Id, USER1, null);
  }


*/
}
