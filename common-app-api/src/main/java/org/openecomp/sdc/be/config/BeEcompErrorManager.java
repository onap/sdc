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

package org.openecomp.sdc.be.config;

import org.openecomp.sdc.common.config.AbsEcompErrorManager;
import org.openecomp.sdc.common.config.EcompErrorEnum;
import org.openecomp.sdc.common.config.IEcompConfigurationManager;

public class BeEcompErrorManager extends AbsEcompErrorManager {

	public enum ComponentName {
		SERVICE, PRODUCT, VF
	}

	public enum ErrorSeverity {
		INFO, WARNING, ERROR, FATAL
	}

	private static volatile BeEcompErrorManager instance;
	private static ConfigurationManager configurationManager;

	private BeEcompErrorManager() {
	};

	public static BeEcompErrorManager getInstance() {
		if (instance == null) {
			synchronized (BeEcompErrorManager.class){
				if (instance == null)
					instance = init();
			}
		}
		return instance;
	}

	private static synchronized BeEcompErrorManager init() {
		if (instance == null) {
			instance = new BeEcompErrorManager();
			configurationManager = ConfigurationManager.getConfigurationManager();
		}
		return instance;
	}

	@Override
	public IEcompConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public void logBeUebAuthenticationError(String context, String reason) {
		processEcompError(context, EcompErrorEnum.BeUebAuthenticationError, reason);
	}

	public void logBeHealthCheckRecovery(String context) {
		processEcompError(context, EcompErrorEnum.BeHealthCheckRecovery);
	}

	public void logBeHealthCheckJanusGraphRecovery(String context) {
		processEcompError(context, EcompErrorEnum.BeHealthCheckJanusGraphRecovery);
	}

	public void logBeHealthCheckElasticSearchRecovery(String context) {
		processEcompError(context, EcompErrorEnum.BeHealthCheckElasticSearchRecovery);
	}

	public void logBeHealthCheckUebClusterRecovery(String context) {
		processEcompError(context, EcompErrorEnum.BeHealthCheckUebClusterRecovery);
	}

	public void logDmaapHealthCheckError(String context) {
		processEcompError(context, EcompErrorEnum.DmaapHealthCheckError);
	}

	public void logDmaapHealthCheckRecovery(String context) {
		processEcompError(context, EcompErrorEnum.DmaapHealthCheckRecovery);
	}

	public void logFeHealthCheckRecovery(String context) {
		processEcompError(context, EcompErrorEnum.FeHealthCheckRecovery);
	}

	public void logBeHealthCheckError(String context) {
		processEcompError(context, EcompErrorEnum.BeHealthCheckError);
	}

	public void logBeHealthCheckJanusGraphError(String context) {
		processEcompError(context, EcompErrorEnum.BeHealthCheckJanusGraphError);
	}

	public void logBeHealthCheckElasticSearchError(String context) {
		processEcompError(context, EcompErrorEnum.BeHealthCheckElasticSearchError);
	}

	public void logBeHealthCheckUebClusterError(String context) {
		processEcompError(context, EcompErrorEnum.BeHealthCheckUebClusterError);
	}

	public void logFeHealthCheckError(String context) {
		processEcompError(context, EcompErrorEnum.FeHealthCheckError);
	}

	/**
	 * @param context
	 * @param reason
	 */
	public void logBeUebConnectionError(String context, String reason) {
		processEcompError(context, EcompErrorEnum.BeUebConnectionError, reason);
	}

	public void logBeUebUnkownHostError(String context, String host) {
		processEcompError(context, EcompErrorEnum.BeUebUnkownHostError, host);
	}

	public void logBeComponentMissingError(String context, String componentType, String name) {
		processEcompError(context, EcompErrorEnum.BeComponentMissingError, componentType, name);
	}

	public void logBeIncorrectComponentError(String context, String componentType, String name) {
		processEcompError(context, EcompErrorEnum.BeIncorrectComponentError, componentType, name);
	}

	public void logBeInvalidConfigurationError(String context, String parameterName, String parameterValue) {
		processEcompError(context, EcompErrorEnum.BeInvalidConfigurationError, parameterName, parameterValue);
	}

	public void logBeUebObjectNotFoundError(String context, String notFoundObjectName) {
		processEcompError(context, EcompErrorEnum.BeUebObjectNotFoundError, notFoundObjectName);
	}

	public void logBeDistributionEngineInvalidArtifactType(String context, String artifactType,
			String validArtifactTypes) {
		processEcompError(context, EcompErrorEnum.BeDistributionEngineInvalidArtifactType, artifactType,
				validArtifactTypes);
	}

	public void logBeMissingConfigurationError(String context, String parameterName) {
		processEcompError(context, EcompErrorEnum.BeMissingConfigurationError, parameterName);
	}

	public void logBeConfigurationInvalidListSizeError(String context, String parameterName, int listMinimumSize) {
		processEcompError(context, EcompErrorEnum.BeConfigurationInvalidListSizeError, parameterName,
				String.valueOf(listMinimumSize));
	}

	public void logErrorConfigFileFormat(String context, String description) {
		processEcompError(context, EcompErrorEnum.ErrorConfigFileFormat, description);
	}

	public void logBeMissingArtifactInformationError(String context, String missingInfo) {
		processEcompError(context, EcompErrorEnum.BeMissingArtifactInformationError, missingInfo);
	}

	public void logBeArtifactMissingError(String context, String artifactName) {
		processEcompError(context, EcompErrorEnum.BeArtifactMissingError, artifactName);
	}

	public void logBeUserMissingError(String context, String userId) {
		processEcompError(context, EcompErrorEnum.BeUserMissingError, userId);
	}

	public void logBeInvalidTypeError(String context, String type, String name) {
		processEcompError(context, EcompErrorEnum.BeInvalidTypeError, type, name);
	}

	public void logBeInvalidValueError(String context, String value, String name, String type) {
		processEcompError(context, EcompErrorEnum.BeInvalidValueError, value, name, type);
	}

	public void logBeArtifactPayloadInvalid(String context) {
		processEcompError(context, EcompErrorEnum.BeArtifactPayloadInvalid);
	}

	public void logBeArtifactInformationInvalidError(String context) {
		processEcompError(context, EcompErrorEnum.BeArtifactInformationInvalidError);
	}

	public void logBeDistributionMissingError(String context, String distributionName) {
		processEcompError(context, EcompErrorEnum.BeDistributionMissingError, "Distribution", distributionName);
	}

	public void logBeGraphObjectMissingError(String context, String objectType, String objectName) {
		processEcompError(context, EcompErrorEnum.BeGraphObjectMissingError, objectType, objectName);
	}

	public void logBeInvalidJsonInput(String context) {
		processEcompError(context, EcompErrorEnum.BeInvalidJsonInput);
	}

	public void logBeInitializationError(String context) {
		processEcompError(context, EcompErrorEnum.BeInitializationError);
	}

	public void logBeFailedAddingResourceInstanceError(String context, String resourceName, String serviceId) {
		processEcompError(context, EcompErrorEnum.BeFailedAddingResourceInstanceError, resourceName, serviceId);
	}

	public void logBeUebSystemError(String context, String operation) {
		processEcompError(context, EcompErrorEnum.BeUebSystemError, operation);
	}

	public void logBeDistributionEngineSystemError(String context, String operation) {
		processEcompError(context, EcompErrorEnum.BeDistributionEngineSystemError, operation);
	}

	public void logBeFailedAddingNodeTypeError(String context, String nodeType) {
		processEcompError(context, EcompErrorEnum.BeFailedAddingNodeTypeError, nodeType);
	}

	public void logBeDaoSystemError(String context) {
		processEcompError(context, EcompErrorEnum.BeDaoSystemError);
	}

	public void logBeSystemError(String context) {
		processEcompError(context, EcompErrorEnum.BeSystemError);
	}

	public void logBeExecuteRollbackError(String context) {
		processEcompError(context, EcompErrorEnum.BeExecuteRollbackError);
	}

	public void logBeFailedLockObjectError(String context, String type, String id) {
		processEcompError(context, EcompErrorEnum.BeFailedLockObjectError, type, id);
	}

	public void logBeFailedCreateNodeError(String context, String nodeName, String status) {
		processEcompError(context, EcompErrorEnum.BeFailedCreateNodeError, nodeName, status);
	}

	public void logBeFailedUpdateNodeError(String context, String nodeName, String status) {
		processEcompError(context, EcompErrorEnum.BeFailedUpdateNodeError, nodeName, status);
	}

	public void logBeFailedDeleteNodeError(String context, String nodeName, String status) {
		processEcompError(context, EcompErrorEnum.BeFailedDeleteNodeError, nodeName, status);
	}

	public void logBeFailedRetrieveNodeError(String context, String nodeName, String status) {
		processEcompError(context, EcompErrorEnum.BeFailedRetrieveNodeError, nodeName, status);
	}

	public void logBeFailedFindParentError(String context, String node, String status) {
		processEcompError(context, EcompErrorEnum.BeFailedFindParentError, node, status);
	}

	public void logBeFailedFindAllNodesError(String context, String nodeType, String parentNode, String status) {
		processEcompError(context, EcompErrorEnum.BeFailedFindAllNodesError, nodeType, parentNode, status);
	}

	public void logBeFailedFindAssociationError(String context, String nodeType, String fromNode, String status) {
		processEcompError(context, EcompErrorEnum.BeFailedFindAssociationError, nodeType, fromNode, status);
	}

	public void logBeComponentCleanerSystemError(String context, String operation) {
		processEcompError(context, EcompErrorEnum.BeComponentCleanerSystemError, operation);
	}

	public void logBeRestApiGeneralError(String context) {
		processEcompError(context, EcompErrorEnum.BeRestApiGeneralError);
	}

	public void logFqdnResolveError(String context, String description) {
		processEcompError(context, EcompErrorEnum.FqdnResolveError, description);
	}

	public void logSiteSwitchoverInfo(String context, String description) {
		processEcompError(context, EcompErrorEnum.SiteSwitchoverInfo, description);
	}

	public void logInternalAuthenticationError(String context, String description, ErrorSeverity severity) {

		if (severity == null) {
			processEcompError(context, EcompErrorEnum.InternalAuthenticationError, description);
		} else {
			switch (severity) {
			case INFO:
				processEcompError(context, EcompErrorEnum.InternalAuthenticationInfo, description);
				break;
			case WARNING:
				processEcompError(context, EcompErrorEnum.InternalAuthenticationWarning, description);
				break;
			case ERROR:
				processEcompError(context, EcompErrorEnum.InternalAuthenticationError, description);
				break;
			case FATAL:
				processEcompError(context, EcompErrorEnum.InternalAuthenticationFatal, description);
				break;

			default:
				break;
			}
		}

	}

	public void logInternalConnectionError(String context, String description, ErrorSeverity severity) {

		if (severity == null) {
			processEcompError(context, EcompErrorEnum.InternalConnectionError, description);
		} else {
			switch (severity) {
			case INFO:
				processEcompError(context, EcompErrorEnum.InternalConnectionInfo, description);
				break;
			case WARNING:
				processEcompError(context, EcompErrorEnum.InternalConnectionWarning, description);
				break;
			case ERROR:
				processEcompError(context, EcompErrorEnum.InternalConnectionError, description);
				break;
			case FATAL:
				processEcompError(context, EcompErrorEnum.InternalConnectionFatal, description);
				break;

			default:
				break;
			}
		}

	}

	public void logInternalDataError(String context, String description, ErrorSeverity severity) {

		if (severity == null) {
			processEcompError(context, EcompErrorEnum.InternalDataError, description);
		} else {
			switch (severity) {
			case INFO:
				processEcompError(context, EcompErrorEnum.InternalDataInfo, description);
				break;
			case WARNING:
				processEcompError(context, EcompErrorEnum.InternalDataWarning, description);
				break;
			case ERROR:
				processEcompError(context, EcompErrorEnum.InternalDataError, description);
				break;
			case FATAL:
				processEcompError(context, EcompErrorEnum.InternalDataFatal, description);
				break;

			default:
				break;
			}
		}

	}

	public void logInvalidInputError(String context, String description, ErrorSeverity severity) {

		if (severity == null) {
			processEcompError(context, EcompErrorEnum.InvalidInputError, description);
		} else {
			switch (severity) {
			case INFO:
				processEcompError(context, EcompErrorEnum.InvalidInputWarning, description);
				break;
			case WARNING:
				processEcompError(context, EcompErrorEnum.InvalidInputInfo, description);
				break;
			case ERROR:
				processEcompError(context, EcompErrorEnum.InvalidInputError, description);
				break;
			case FATAL:
				processEcompError(context, EcompErrorEnum.InvalidInputFatal, description);
				break;

			default:
				break;
			}
		}

	}

	public void logInternalFlowError(String context, String description, ErrorSeverity severity) {

		if (severity == null) {
			processEcompError(context, EcompErrorEnum.InternalFlowError, description);
		} else {
			switch (severity) {
			case INFO:
				processEcompError(context, EcompErrorEnum.InternalFlowInfo, description);
				break;
			case WARNING:
				processEcompError(context, EcompErrorEnum.InternalFlowWarning, description);
				break;
			case ERROR:
				processEcompError(context, EcompErrorEnum.InternalFlowError, description);
				break;
			case FATAL:
				processEcompError(context, EcompErrorEnum.InternalFlowFatal, description);
				break;

			default:
				break;
			}
		}

	}

	public void logInternalUnexpectedError(String context, String description, ErrorSeverity severity) {

		if (severity == null) {
			processEcompError(context, EcompErrorEnum.InternalUnexpectedError, description);
		} else {
			switch (severity) {
			case INFO:
				processEcompError(context, EcompErrorEnum.InternalUnexpectedInfo, description);
				break;
			case WARNING:
				processEcompError(context, EcompErrorEnum.InternalUnexpectedWarning, description);
				break;
			case ERROR:
				processEcompError(context, EcompErrorEnum.InternalUnexpectedError, description);
				break;
			case FATAL:
				processEcompError(context, EcompErrorEnum.InternalUnexpectedFatal, description);
				break;

			default:
				break;
			}
		}

	}

}
