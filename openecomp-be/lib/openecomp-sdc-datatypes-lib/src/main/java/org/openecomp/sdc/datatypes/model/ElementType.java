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
  // todo - remove OrchestrationTemplateContent
  OrchestrationTemplate, OrchestrationTemplateValidationData, OrchestrationTemplateContent,
  Networks, Network,
  Components, Component, ComponentQuestionnaire, ComponentDependencies, ComponentDependency,
  Nics, Nic, NicQuestionnaire,
  Mibs, SNMP_POLL, SNMP_TRAP, VES_EVENTS,
  Processes, Process,
  DeploymentFlavors, DeploymentFlavor,
  Computes, Compute, ComputeQuestionnaire,
  Images, Image, ImageQuestionnaire,
  ServiceModel, EnrichedServiceModel, ServiceTemplate, Templates, Artifact, Artifacts,

  test
}
