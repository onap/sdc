/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.datatypes.model;

public enum ElementType {
  itemVersion,

  VendorLicenseModel,
  LicenseAgreements, LicenseAgreement,
  FeatureGroups, FeatureGroup,
  LicenseKeyGroups, LicenseKeyGroup,
  EntitlementPools, EntitlementPool,
  Limits, Limit,

  VendorSoftwareProduct,
  VSPQuestionnaire,

  VspModel, NetworkPackage,
  OrchestrationTemplateCandidate, OrchestrationTemplateCandidateContent,
  OrchestrationTemplateCandidateValidationData,
  OrchestrationTemplateStructure, OrchestrationTemplate,
  OrchestrationTemplateValidationData,
  // todo - remove OrchestrationTemplateContent
  OrchestrationTemplateContent,
  Networks, Network,
  Components, Component, ComponentQuestionnaire, ComponentDependencies, ComponentDependency,
  Nics, Nic, NicQuestionnaire,
  Mibs, SNMP_POLL, SNMP_TRAP, VES_EVENTS,
  Processes, Process,
  DeploymentFlavors, DeploymentFlavor,
  Computes, Compute, ComputeQuestionnaire,
  Images, Image, ImageQuestionnaire,
  ServiceModel, EnrichedServiceModel, ServiceTemplate, Templates, Artifact, Artifacts,

  test, ORIGINAL_ONBOARDED_PACKAGE
}
