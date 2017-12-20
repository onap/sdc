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

import SoftwareProductCreation from 'sdc-app/onboarding/softwareProduct/creation/SoftwareProductCreation.js';
import LicenseModelCreation from 'sdc-app/onboarding/licenseModel/creation/LicenseModelCreation.js';
import SoftwareProductComponentImageEditor from 'sdc-app/onboarding/softwareProduct/components/images/SoftwareProductComponentsImageEditor.js';
import VersionPageCreation from 'sdc-app/onboarding/versionsPage/creation/VersionsPageCreation.js';
import SubmitErrorResponse from 'nfvo-components/SubmitErrorResponse.jsx';
import ComputeFlavorEditor from 'sdc-app/onboarding/softwareProduct/components/compute/computeComponents/computeFlavor/ComputeFlavorEditor.js';
import NICCreation from 'sdc-app/onboarding/softwareProduct/components/network/NICCreation/NICCreation.js';
import SoftwareProductComponentsNICEditor from 'sdc-app/onboarding/softwareProduct/components/network/SoftwareProductComponentsNICEditor.js';
import ComponentCreation from 'sdc-app/onboarding/softwareProduct/components/creation/SoftwareProductComponentCreation.js';
import SoftwareProductDeploymentEditor from 'sdc-app/onboarding/softwareProduct/deployment/editor/SoftwareProductDeploymentEditor.js';
import PermissionsManager from 'sdc-app/onboarding/permissions/PermissionsManager.js';
import CommitCommentModal from 'nfvo-components/panel/versionController/components/CommitCommentModal.jsx';
import Tree from 'nfvo-components/tree/Tree.jsx';
import MergeEditor from 'sdc-app/common/merge/MergeEditor.js';
import Revisions from 'sdc-app/onboarding/revisions/Revisions.js';

export const modalContentMapper = {
	SOFTWARE_PRODUCT_CREATION: 'SOFTWARE_PRODUCT_CREATION',
	LICENSE_MODEL_CREATION: 'LICENSE_MODEL_CREATION',
	SUMBIT_ERROR_RESPONSE: 'SUMBIT_ERROR_RESPONSE',
	COMPONENT_COMPUTE_FLAVOR_EDITOR: 'COMPONENT_COMPUTE_FLAVOR_EDITOR',
	NIC_EDITOR: 'NIC_EDITOR',
	NIC_CREATION: 'NIC_CREATION',
	COMPONENT_CREATION: 'COMPONENT_CREATION',
	SOFTWARE_PRODUCT_COMPONENT_IMAGE_EDITOR : 'SOFTWARE_PRODUCT_COMPONENT_IMAGE_EDITOR',
	DEPLOYMENT_FLAVOR_EDITOR: 'DEPLOYMENT_FLAVOR_EDITOR',
	MANAGE_PERMISSIONS: 'MANAGE_PERMISSIONS',
	VERSION_CREATION: 'VERSION_CREATION',
	COMMIT_COMMENT: 'COMMIT_COMMENT',
	VERSION_TREE: 'VERSION_TREE',
	MERGE_EDITOR: 'MERGE_EDITOR',
	REVISIONS_LIST: 'REVISIONS_LIST'
};

export const modalContentComponents = {
	SUMBIT_ERROR_RESPONSE: SubmitErrorResponse,
	SOFTWARE_PRODUCT_CREATION: SoftwareProductCreation,
	VERSION_CREATION: VersionPageCreation,
	LICENSE_MODEL_CREATION: LicenseModelCreation,
	COMPONENT_COMPUTE_FLAVOR_EDITOR: ComputeFlavorEditor,
	NIC_EDITOR: SoftwareProductComponentsNICEditor,
	NIC_CREATION: NICCreation,
	COMPONENT_CREATION: ComponentCreation,
	SOFTWARE_PRODUCT_COMPONENT_IMAGE_EDITOR : SoftwareProductComponentImageEditor,
	DEPLOYMENT_FLAVOR_EDITOR: SoftwareProductDeploymentEditor,
	MANAGE_PERMISSIONS: PermissionsManager,
	COMMIT_COMMENT: CommitCommentModal,
	VERSION_TREE: Tree,
	MERGE_EDITOR: MergeEditor,
	REVISIONS_LIST: Revisions
};
