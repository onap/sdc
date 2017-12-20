/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import keyMirror from 'nfvo-utils/KeyMirror.js';

export const actionTypes = keyMirror({
	LOAD_CONFLICTS: null,
	ADD_ACTIONS: null,
	LOAD_CONFLICT: null,
	DATA_PROCESSED: null
});

export const rules = {
	SKIP: 'skip',
	PARSE: 'parse',
	FUNCTION: 'function',
	BOOLEAN: 'boolean'
};

export const SyncStates = {
	MERGE : 'Merging',
	OUT_OF_SYNC: 'OutOfSync',
	UP_TO_DATE: 'UpToDate'
};

export const ResolutionTypes = {
	YOURS: 'YOURS',
	THEIRS: 'THEIRS'
};

export const fileTypes = {
	LKG : 'LicenseKeyGroup',
	VLM : 'VendorLicenseModel',
	EP  : 'EntitlementPool',
	FG  : 'FeatureGroup',
	LA  : 'LicenseAgreement',
	VSP : 'VendorSoftwareProduct',
	LIMIT : 'Limit',
	VSP_Q : 'VSPQuestionnaire',
	COMPONENT : 'Component',
	COMPONENT_Q : 'ComponentQuestionnaire',
	COMPONENT_DEP : 'ComponentDependencies',
	COMPUTE_Q : 'ComputeQuestionnaire',
	COMPUTE : 'Compute',
	COMPUTE_FLAVOR: 'ComputeFlavor',
	NIC : 'Nic',
	NIC_Q : 'NicQuestionnaire',
	IMAGE : 'Image',
	IMAGE_Q : 'ImageQuestionnaire',
	PROCESS : 'Process',
	DEPLOYMENT_FLAVOR : 'DeploymentFlavor',
	VENDOR : 'Vendor',
	NETWORK : 'Network',
	ORCHESTRATION_TEMPLATE_CANDIDATE : 'OrchestrationTemplateCandidate'
};

export const dataRules = {
	general: {
		id: {
			rule: rules.SKIP
		},
		questionareData: {
			rule: rules.PARSE,
			moveFields: true
		},
		startDate: {
			rule: rules.FUNCTION,
			functionName: 'parseDate'
		},
		expiryDate: {
			rule: rules.FUNCTION,
			functionName: 'parseDate'
		},
		featureGroups: {
			rule: rules.FUNCTION,
			functionName: 'reduceList',
			args: {subField: 'name'}
		},
		licenseKeyGroups: {
			rule: rules.FUNCTION,
			functionName: 'reduceList',
			args: {subField: 'name'}
		},
		entitlementPools: {
			rule: rules.FUNCTION,
			functionName: 'reduceList',
			args: {subField: 'name'}
		},
	},
	[fileTypes.COMPONENT] : {
	},
	[fileTypes.COMPUTE_FLAVOR] : {
		associatedToDeploymentFlavor: {
			rule: rules.BOOLEAN,
			trueValue: 'true'
		}
	},
	[fileTypes.COMPUTE_Q] : {
	},
	[fileTypes.COMPONENT_Q] : {
		isComponentMandatory: {
			rule: rules.BOOLEAN,
			trueValue: 'YES',
			falseValue: 'NO'
		}
	},
	[fileTypes.EP] : {
		referencingFeatureGroups: {
			rule: rules.SKIP,
			functionName: 'getFeatureGroups'
		},
		operationalScope: {
			rule: rules.FUNCTION,
			functionName: 'processChoices'
		},
	},
	[fileTypes.FG] : {
		referencingLicenseAgreements: {
			rule: rules.SKIP,
			functionName: 'getLicenseAgreements'
		}
	},
	[fileTypes.LA] : {
		licenseTerm : {
			rule: rules.FUNCTION,
			functionName: 'processChoice'
		}
	},
	[fileTypes.LIMIT] : {
		type: {
			rule: rules.FUNCTION,
			functionName: 'getEnumValue',
			args: {listName: 'limitType'}
		},
		unit: {
			rule: rules.FUNCTION,
			functionName: 'getEnumValue',
			args: {listName: 'limitUnit'}
		}
	},
	[fileTypes.LKG] : {
		operationalScope: {
			rule: rules.FUNCTION,
			functionName: 'processChoices'
		},
		referencingFeatureGroups: {
			rule: rules.SKIP,
			functionName: 'getFeatureGroups'
		},
	},
	[fileTypes.NIC] : {
		networkId: {
			rule: rules.SKIP
		}
	},
	[fileTypes.NIC_Q] : {
	},
	[fileTypes.PROCESS] : {
		type: {
			rule: rules.FUNCTION,
			functionName: 'getEnumValue',
			args: {listName: 'processType'}
		}
	},
	[fileTypes.VLM] : {
		iconRef: {
			rule: rules.SKIP
		}
	},
	[fileTypes.VSP] : {
		vendorId: {
			rule: rules.SKIP
		},
		onboardingMethod: {
			rule: rules.SKIP
		},
		validationData: {
			rule: rules.SKIP
		},
		isOldVersion: {
			rule: rules.SKIP
		},
		licensingVersion: {
			rule: rules.FUNCTION,
			functionName: 'fetchLMVersion'
		},
		category: {
			rule: rules.FUNCTION,
			functionName: 'fetchCategory'
		},
		subCategory: {
			rule: rules.SKIP
		},
	},
	[fileTypes.VSP_Q] : {
		affinityData: {
			rule: rules.SKIP
		},
		storageReplicationAcrossRegion: {
			rule: rules.BOOLEAN,
			trueValue: 'true',
			falseValue: 'false'
		}
	},
	[fileTypes.ORCHESTRATION_TEMPLATE_CANDIDATE] : {
		modules: {
			rule: rules.FUNCTION,
			functionName: 'convertArrayToObject'
		},
	},
};
