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

import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.factory.QuestionnnaireDataServiceFactory;
import org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.InformationArtifactData;
import org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.InformationArtifactGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.questionnaire.QuestionnaireDataService;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.ComponentQuestionnaire;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.compute.Compute;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.compute.GuestOS;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.compute.NumOfVMs;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.general.Hypervisor;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.general.Recovery;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.nic.IpConfiguration;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.nic.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.nic.NicQuestionnaire;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.vsp.VspQuestionnaire;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.vsp.general.Availability;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.vsp.general.General;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.vsp.general.StorageDataReplication;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.List;
import java.util.Optional;

import static org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.impl.TxtInformationArtifactConstants.*;

/**
 * @author katyr
 * @since November 23, 2016
 */

public class TxtInformationArtifactGeneratorImpl implements InformationArtifactGenerator {
  private QuestionnaireDataService questionnaireDataService = QuestionnnaireDataServiceFactory
      .getInstance().createInterface();
  private StringBuilder textArtifact;
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  @Override
  public String generate(String vspId, Version version) {
    InformationArtifactData informationArtifactData =
        questionnaireDataService.generateQuestionnaireDataForInformationArtifact(vspId, version);
    return createTxtArtifact(informationArtifactData);
  }

  private String createTxtArtifact(InformationArtifactData informationArtifactData) {


    mdcDataDebugMessage.debugEntryMessage(null);

    textArtifact = new StringBuilder(TxtInformationArtifactConstants.HEADER);
    addVspVlmEntries(informationArtifactData);
    addAvailabilityEntries();
    addDataEntries(informationArtifactData);
    addEntryWithIndent(LIST_OF_VFCS, "", TAB);
    addEntryWithIndent(FOR_EACH_VFC, "", TAB + TAB);
    List<ComponentQuestionnaire> componentQuestionnaires =
        informationArtifactData.getComponentQuestionnaires();

    for (ComponentQuestionnaire componentQuestionnaire : componentQuestionnaires) {
      addEntriesPerComponent(componentQuestionnaire);

    }

    List<NicQuestionnaire> nicQuestionnaires = informationArtifactData.getNicQuestionnaires();
    for (NicQuestionnaire nicQuestionnaire : nicQuestionnaires) {
      addEntriesPerNic(nicQuestionnaire);

    }
    for (ComponentQuestionnaire componentQuestionnaire : componentQuestionnaires) {
      addRecoveryEntriesPerComponent(componentQuestionnaire);
    }
    textArtifact.append(TxtInformationArtifactConstants.FOOTER);

    mdcDataDebugMessage.debugExitMessage(null);
    return textArtifact.toString();

  }

  private void addDataEntries(InformationArtifactData informationArtifactData) {


    mdcDataDebugMessage.debugEntryMessage(null);

    addEntryWithIndent(STORAGE_BACKUP_DETAILS, "", TAB);

    Optional<StorageDataReplication> storageDataReplication =
        Optional.of(informationArtifactData).map(InformationArtifactData::getVspQuestionnaire)
            .map(VspQuestionnaire::getGeneral).map(General::getStorageDataReplication);
    storageDataReplication
        .ifPresent(replication -> addEntryWithIndent(IS_DATA_REPLICATION,
            String.valueOf(replication.isStorageReplicationAcrossRegion()), TAB + TAB));


    storageDataReplication.ifPresent(rep -> addEntryWithIndent(DATA_SIZE_TO_REP,
        String.valueOf(rep.getStorageReplicationSize()), TAB + TAB));

    storageDataReplication.ifPresent(rep -> addEntryWithIndent(DATA_REP_FREQUENCY,
        String.valueOf(rep.getStorageReplicationFrequency()), TAB + TAB));

    storageDataReplication.ifPresent(rep -> addEntryWithIndent(DATA_REP_SOURCE,
        String.valueOf(rep.getStorageReplicationSource()), TAB + TAB));

    storageDataReplication.ifPresent(rep -> addEntryWithIndent(DATA_REP_DEST,
        String.valueOf(rep.getStorageReplicationDestination()), TAB + TAB));

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private void addAvailabilityEntries() {
    mdcDataDebugMessage.debugEntryMessage(null);

    addEntryWithIndent(HIGH_AVAILABILITY, "", TAB);

    Optional<Availability> availability =
        Optional.of(new InformationArtifactData()).map(InformationArtifactData::getVspQuestionnaire)
            .map(VspQuestionnaire::getGeneral).map(General::getAvailability);
    availability
        .ifPresent(availabilityVal -> addEntryWithIndent(USING_AVAILABILITY_ZONES, String.valueOf(
            availabilityVal.isUseAvailabilityZonesForHighAvailability()), TAB + TAB));

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private void addVspVlmEntries(InformationArtifactData informationArtifactData) {
    mdcDataDebugMessage.debugEntryMessage(null);

    addEntryWithIndent(TITLE, "", "");
    Optional<VspDetails> vspDetails = Optional.of(informationArtifactData).map
        (InformationArtifactData::getVspDetails);
    addEntryWithIndent(VSP_NAME, informationArtifactData.getVspDetails().getName(), TAB);
    addEntryWithIndent(VSP_DESC, informationArtifactData.getVspDetails().getDescription(), TAB);
    addEntryWithIndent(VSP_VERSION,
        roundVersionAsNeeded(informationArtifactData.getVspDetails().getVersion()), TAB);
    addEntryWithIndent(VSP_VENDOR, informationArtifactData.getVspDetails().getVendorName(), TAB);
    addEntryWithIndent(VSP_CATEGORY, informationArtifactData.getVspDetails().getCategory(), TAB);
    addEntryWithIndent(LICENSE_DETAILS, "", TAB);
    addEntryWithIndent(LICENSE_MODEL_VERSION,
        informationArtifactData.getVspDetails().getVlmVersion() == null
            ? ""
            : informationArtifactData.getVspDetails().getVlmVersion().toString(),
        TAB + TAB);
    addEntryWithIndent(LICENSE_AGREEMENT_NAME, informationArtifactData.getVspDetails()
        .getLicenseAgreement(), TAB + TAB);
    addEntryWithIndent(LIST_OF_FEATURE_GROUPS, "", TAB + TAB);
    vspDetails.ifPresent(vspDets -> addListEntriesWithIndent(vspDets
        .getFeatureGroups(), TAB + TAB + TAB));

    mdcDataDebugMessage.debugExitMessage(null);
  }

  static String roundVersionAsNeeded(Version version) {
    if (version.isFinal()) {
      return version.toString();
    } else {
      return String.valueOf(Math.ceil(Double.valueOf(version.toString())));
    }
  }

  private void addRecoveryEntriesPerComponent(ComponentQuestionnaire componentQuestionnaire) {

    mdcDataDebugMessage.debugEntryMessage(null);

    addEntryWithIndent(RECOVERY_DETAILS, "", TAB + TAB + TAB);
    Optional<Recovery> recovery = Optional.of(componentQuestionnaire).map(
        ComponentQuestionnaire::getGeneral).map(
        org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.general
            .General::getRecovery);
    recovery.ifPresent(recoveryVal -> addEntryWithIndent(RECOVERY_DETAILS_POINT, String.valueOf(
        recoveryVal.getPointObjective()), TAB + TAB + TAB + TAB));

    recovery.ifPresent(recoveryVal -> addEntryWithIndent(RECOVERY_DETAILS_TIME, String.valueOf(
        recoveryVal.getTimeObjective()), TAB + TAB + TAB + TAB));

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private void addEntriesPerNic(NicQuestionnaire nicQuestionnaire) {


    mdcDataDebugMessage.debugEntryMessage(null);

    addEntryWithIndent(VNICS, "", TAB + TAB + TAB);
    Optional<Network> networkOpt = Optional.of(nicQuestionnaire).map(
        NicQuestionnaire::getNetwork);

    networkOpt.ifPresent(network -> addEntryWithIndent(VNICS_NAME, network
            .getNetworkDescription(),
        TAB + TAB + TAB + TAB));
    networkOpt.ifPresent(network -> addEntryWithIndent(VNICS_PURPOSE, network
        .getNetworkDescription(), TAB + TAB + TAB + TAB));
    networkOpt.ifPresent(network -> addEntryWithIndent(VNICS_INT_EXT, network
        .getNetworkDescription(), TAB + TAB + TAB + TAB));
    networkOpt.ifPresent(network -> addEntryWithIndent(VNICS_NETWORK, network.toString(),
        TAB + TAB + TAB + TAB));
    addEntryWithIndent(VNICS_PROTOCOLS, nicQuestionnaire.getProtocols().toString(),
        TAB + TAB + TAB + TAB);

    Optional<IpConfiguration> ipconfigOpt = Optional.of(nicQuestionnaire).map
        (NicQuestionnaire::getIpConfiguration);
    ipconfigOpt.ifPresent(ipconfig -> addEntryWithIndent(VNICS_IPV4, String.valueOf(ipconfig
        .isIpv4Required()), TAB + TAB + TAB + TAB));
    ipconfigOpt.ifPresent(ipconfig -> addEntryWithIndent(VNICS_IPV6, String.valueOf(ipconfig
        .isIpv6Required()), TAB + TAB + TAB + TAB));

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private void addEntriesPerComponent(ComponentQuestionnaire componentQuestionnaire) {


    mdcDataDebugMessage.debugEntryMessage(null);

    addEntryWithIndent(VFC_NAME, "", TAB + TAB + TAB);
    addEntryWithIndent(VFC_DESC, "", TAB + TAB + TAB);
    addEntryWithIndent(VFC_IMAGES, "", TAB + TAB + TAB);
    //todo component name +desc +img+vcpu
    addEntryWithIndent(VFC_COMPUTE, "", TAB + TAB + TAB);
    addEntryWithIndent(VFC_COMPUTE_VCPU, "", TAB + TAB + TAB + TAB);
    addEntryWithIndent(VFC_COMPUTE_CPU_OVER_SUBSCRIPTION, "",
        TAB + TAB + TAB + TAB);
    addEntryWithIndent(VFC_COMPUTE_MEMORY, "", TAB + TAB + TAB + TAB);
    addEntryWithIndent(VFC_COMPUTE_DISK, "", TAB + TAB + TAB + TAB);

    addEntryWithIndent(HYPERVISOR_DETAILS, "", TAB + TAB + TAB);

    Optional<Hypervisor> hypervisorOpt = Optional.of(componentQuestionnaire).map(
        ComponentQuestionnaire::getGeneral).map(
        org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.general
            .General::getHypervisor);
    hypervisorOpt.ifPresent(hypervisor -> addEntryWithIndent(HYPERVISOR_DETAILS_NAME, hypervisor
        .getHypervisor(), TAB
        + TAB + TAB + TAB));

    hypervisorOpt.ifPresent(hypervisor -> addEntryWithIndent(HYPERVISOR_DETAILS_DRIVERS, hypervisor
        .getDrivers(), TAB + TAB + TAB + TAB));

    addEntryWithIndent(GUEST_OS_DETAILS, "", TAB + TAB + TAB);
    Optional<GuestOS> guestOSOptional = Optional.of(componentQuestionnaire).map
        (ComponentQuestionnaire::getCompute).map(Compute::getGuestOS);
    guestOSOptional.ifPresent(guestOs -> addEntryWithIndent(GUEST_OS_NAME, guestOs.getName(),
        TAB + TAB + TAB + TAB));


    guestOSOptional
        .ifPresent(guestOs -> addEntryWithIndent(GUEST_OS_BIT_SIZE, String.valueOf(guestOs
            .getBitSize()), TAB + TAB + TAB + TAB));
    guestOSOptional.ifPresent(guestOs -> addEntryWithIndent(GUEST_OS_TOOLS, guestOs.getTools
        (), TAB + TAB + TAB + TAB));

    addEntryWithIndent(VFC_INSTANCE_NUMBER, "",
        TAB + TAB + TAB);

    Optional<NumOfVMs> numVmsOpt = Optional.of(componentQuestionnaire).map
        (ComponentQuestionnaire::getCompute).map(Compute::getNumOfVMs);
    numVmsOpt.ifPresent(numVms -> addEntryWithIndent(VFC_INSTANCE_NUMBER_MIN, String.valueOf
        (numVms.getMinimum()), TAB + TAB + TAB + TAB));

    numVmsOpt.ifPresent(numVms -> addEntryWithIndent(VFC_INSTANCE_NUMBER_MAX, String.valueOf
        (numVms.getMaximum()), TAB + TAB + TAB + TAB));


    mdcDataDebugMessage.debugExitMessage(null);
  }

  private void addListEntriesWithIndent(List<String> fieldValues, String indent) {
    int counter = 1;
    if (fieldValues == null) {
      return;
    }
    for (String fieldValue : fieldValues) {
      textArtifact.append(indent).append(counter++).append(".").append(TAB).append(fieldValue)
          .append
              (NL);
    }

  }


  private void addEntryWithIndent(String fieldName, String fieldValue, String indent) {
    textArtifact.append(indent).append(fieldName).append(SPACE).append(fieldValue).append(NL);
  }
}
