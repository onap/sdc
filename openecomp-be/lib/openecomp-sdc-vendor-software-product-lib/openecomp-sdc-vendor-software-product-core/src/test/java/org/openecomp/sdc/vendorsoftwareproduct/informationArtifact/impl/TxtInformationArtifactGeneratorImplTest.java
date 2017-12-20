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

package org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.impl;


import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openecomp.sdc.vendorsoftwareproduct.questionnaire.QuestionnaireDataService;

public class TxtInformationArtifactGeneratorImplTest {

  private static final String NETWORK_DESC = "\"network desc\"";
  private static final String HYPERVIZOR_NAME = "\"hyper hyper hypervizor\"";
  private static final String TOOLS ="all tools possible" ;
  @Mock
  QuestionnaireDataService questionnaireDataServiceMock;
  @InjectMocks
  TxtInformationArtifactGeneratorImpl informationArtifactGenerator;

  /*

  @BeforeMethod(alwaysRun = true)
  public void injectDoubles() {
    MockitoAnnotations.initMocks(this);

//    InformationArtifactData returnedQuestionnaire = new InformationArtifactData();
//    Mockito.when(questionnaireDataServiceMock.generateQuestionnaireDataForInformationArtifact
//        (anyString(), anyObject()))
//        .thenReturn(returnedQuestionnaire);
//

  }

  @Test
  public void testRoundVersion(){
    Version version = new Version(2,1);
    String rounded = TxtInformationArtifactGeneratorImpl.roundVersionAsNeeded(version);
    Assert.assertEquals("3.0",rounded);

    version = Version.valueOf("2.0");
     rounded = TxtInformationArtifactGeneratorImpl.roundVersionAsNeeded(version);
    Assert.assertEquals("2.0",rounded);

  }

  @Test
  public void testArtifactCreation() throws IOException {
    InformationArtifactData informationArtifactData = initArtifactData();
    Mockito.when(questionnaireDataServiceMock.generateQuestionnaireDataForInformationArtifact
        (anyString(), anyObject()))
        .thenReturn(informationArtifactData);


    String result = informationArtifactGenerator.generate("vsp", new Version(0, 1));
    System.out.println("result = \n" + result);

    Assert.assertTrue(result.contains(HYPERVIZOR_NAME));
    Assert.assertTrue(result.contains(HEADER));
    Assert.assertTrue(result.contains(VFC_COMPUTE_CPU_OVER_SUBSCRIPTION));
    Assert.assertTrue(result.contains(TOOLS));
    Assert.assertTrue(result.contains(TxtInformationArtifactConstants.LICENSE_AGREEMENT_NAME));
    Assert.assertTrue(result.contains(TxtInformationArtifactConstants.LIST_OF_FEATURE_GROUPS));

  }

  private InformationArtifactData initArtifactData() {
    InformationArtifactData informationArtifactData = new InformationArtifactData();

    informationArtifactData.setVspDetails(initVspDetails());

    informationArtifactData.setVspQuestionnaire(initVspQuestionnaire());
    informationArtifactData.setComponentQuestionnaires(initComponentQuestionnaires());
    informationArtifactData.setNicQuestionnaires(initNicQuestionnaires());
    return informationArtifactData;
  }

  private List<NicQuestionnaire> initNicQuestionnaires() {
    List<NicQuestionnaire> nicQuestionnaires = new ArrayList<>();
    NicQuestionnaire nic1 = new NicQuestionnaire();
    NicQuestionnaire nic2 = new NicQuestionnaire();
    nic1 = initNicQuestionnaire();
    nic2 = initNicQuestionnaire();
    nicQuestionnaires.add(nic1);
    nicQuestionnaires.add(nic2);
    return nicQuestionnaires;

  }

  private NicQuestionnaire initNicQuestionnaire() {
    NicQuestionnaire nic = new NicQuestionnaire();
    Network network = new Network();
    network.setNetworkDescription(NETWORK_DESC);
    nic.setNetwork(network);

    IpConfiguration ipconfig = new IpConfiguration();
    ipconfig.setIpv4Required(true);
    ipconfig.setIpv6Required(false);

    nic.setIpConfiguration(ipconfig);

    Protocols protocols = new Protocols();
    List<String> protocolsList = new ArrayList<>();
    protocolsList.add("45");
    protocolsList.add("55");
    protocolsList.add("HTTP");
    protocols.setProtocols(protocolsList);

    nic.setProtocols(protocols);

    return nic;
  }

  private List<ComponentQuestionnaire> initComponentQuestionnaires() {
    List<ComponentQuestionnaire> componentQuestionnaires = new ArrayList<>();
    ComponentQuestionnaire componentQuestionnaire1 = new ComponentQuestionnaire();
    ComponentQuestionnaire componentQuestionnaire2 = new ComponentQuestionnaire();

    componentQuestionnaire1 = initComponent();
    componentQuestionnaire2 = initComponent();

    componentQuestionnaires.add(componentQuestionnaire1);
    componentQuestionnaires.add(componentQuestionnaire2);

    return componentQuestionnaires;
  }

  private ComponentQuestionnaire initComponent() {
    ComponentQuestionnaire componentQuestionnaire = new ComponentQuestionnaire();
    org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.general.General general =
        new org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.general.General();
    Hypervisor hypervisor = new Hypervisor();
    hypervisor.setDrivers("driving drivers");
    hypervisor.setHypervisor(HYPERVIZOR_NAME);
    general.setHypervisor(hypervisor);
    Recovery recovery = new Recovery();
    recovery.setPointObjective(22);
    recovery.setTimeObjective(33);
    general.setRecovery(recovery);
    componentQuestionnaire.setGeneral(
        general);
    org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.network.Network net =
        new org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.network.Network();
    net.setNetworkCapacity(new NetworkCapacity());
    componentQuestionnaire.setNetwork(net);
    Compute compute = new Compute();
    GuestOS guestOS = new GuestOS();
    guestOS.setBitSize(32);
    guestOS.setTools(TOOLS);
    guestOS.setName("Ubuntu");
    compute.setGuestOS(guestOS);
    NumOfVMs numOfVMs = new NumOfVMs();
    numOfVMs.setMaximum(256);
    numOfVMs.setMinimum(2);
    compute.setNumOfVMs(numOfVMs);
    componentQuestionnaire.setCompute(compute);

    return componentQuestionnaire;
  }

  private VspQuestionnaire initVspQuestionnaire() {
    VspQuestionnaire vspQuestionnaire = new VspQuestionnaire();
    Availability availability = new Availability();
    availability.setUseAvailabilityZonesForHighAvailability(true);
    General general = new General();
    general.setAvailability(availability);

    StorageDataReplication storageDataReplication = new StorageDataReplication();
    storageDataReplication.setStorageReplicationAcrossRegion(true);
    storageDataReplication.setStorageReplicationDestination("in a galaxy far, far away");
    storageDataReplication.setStorageReplicationFrequency(6);
    storageDataReplication.setStorageReplicationSize(128);
    storageDataReplication.setStorageReplicationSource("here below");
    general.setStorageDataReplication(storageDataReplication);
    vspQuestionnaire.setGeneral(general);
    return vspQuestionnaire;
  }

  private VspDetails initVspDetails() {
    VspDetails vspDetails = new VspDetails();
    vspDetails.setCategory("vspCategory");
    vspDetails.setDescription("described");
    vspDetails.setName("vsp named Alice");
    vspDetails.setVendorName("Fortigate");
    vspDetails.setVersion(new Version(0, 79));
    vspDetails.setVlmVersion(new Version(0, 1));
    vspDetails.setLicenseAgreement("the usual license agreement");
    List<String> featureGroups = new ArrayList<>();
    featureGroups.add("first feature group");
    featureGroups.add("one too many feature group");
    featureGroups.add("the very last feature group");
    vspDetails.setFeatureGroups(featureGroups);
    vspDetails.setOnboardingMethod("HEAT");
    return vspDetails;
  }

  */

}
