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

package org.openecomp.sdc.common.config;

public enum EcompErrorName {
	// general
	EcompConfigFileFormat, EcompErrorNotFound, EcompErrorDescParamsMismatch,

	// FE
	FeHealthCheckConnectionError, FeHealthCheckRecovery /* Recovery */, FeHttpLoggingError, FePortalServletError, FeHealthCheckGeneralError,

	// BE section here...
	BeHealthCheckError, BeHealthCheckRecovery, /* Recovery */
	BeRestApiGeneralError, BeInitializationError, BeResourceMissingError, BeServiceMissingError, BeMissingArtifactInformationError, BeArtifactMissingError, BeArtifactPayloadInvalid, BeUserMissingError, BeArtifactInformationInvalidError, BeIncorrectServiceError, BeFailedAddingResourceInstanceError,
	// BeFailedDeletingResourceInstanceError,
	BeFailedAddingCapabilityTypeError, BeCapabilityTypeMissingError, BeInterfaceMissingError,
	// BeRepositoryObjectNotFoundError,
	// BeRepositoryDeleteError,
	// BeRepositoryQueryError,
	BeDaoSystemError, BeSystemError, BeInvalidConfigurationError, BeMissingConfigurationError, BeUebConnectionError, BeUebObjectNotFoundError, BeUebSystemError, BeDistributionEngineSystemError, BeDistributionEngineInvalidArtifactType, BeConfigurationInvalidListSizeError, BeUebAuthenticationError, BeUebUnkownHostError, BeInvalidTypeError, BeInvalidValueError, BeFailedLockObjectError, BeInvalidJsonInput, BeDistributionMissingError, ErrorConfigFileFormat,
	// model
	BeFailedCreateNodeError, BeFailedUpdateNodeError, BeFailedDeleteNodeError, BeFailedRetrieveNodeError, BeExecuteRollbackError, BeFailedFindParentError, BeFailedFindAllNodesError, BeFailedFindAssociationError, BeFailedToAssociateError, BeComponentCleanerSystemError;
}
