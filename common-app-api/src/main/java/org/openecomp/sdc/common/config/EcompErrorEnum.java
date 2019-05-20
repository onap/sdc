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

public enum EcompErrorEnum {


	BeUebAuthenticationError(EcompErrorCode.E_100, ErrorType.AUTHENTICATION_PROBLEM, AlarmSeverity.MAJOR,
			EcompClassification.ERROR),


	DmaapHealthCheckError(EcompErrorCode.E_214, ErrorType.CONNECTION_PROBLEM, AlarmSeverity.MAJOR,
			EcompClassification.ERROR),

	InternalAuthenticationInfo(EcompErrorCode.E_199, ErrorType.AUTHENTICATION_PROBLEM, AlarmSeverity.INFORMATIONAL,
			EcompClassification.INFORMATION), InternalAuthenticationWarning(EcompErrorCode.E_199,
					ErrorType.AUTHENTICATION_PROBLEM, AlarmSeverity.MINOR,
					EcompClassification.WARNING), InternalAuthenticationError(EcompErrorCode.E_199,
							ErrorType.AUTHENTICATION_PROBLEM, AlarmSeverity.MAJOR,
							EcompClassification.ERROR), InternalAuthenticationFatal(EcompErrorCode.E_199,
									ErrorType.AUTHENTICATION_PROBLEM, AlarmSeverity.CRITICAL,
									EcompClassification.FATAL),

	BeHealthCheckRecovery(EcompErrorCode.E_205, ErrorType.RECOVERY, AlarmSeverity.INFORMATIONAL,
			EcompClassification.INFORMATION, null), BeHealthCheckJanusGraphRecovery(EcompErrorCode.E_206,	ErrorType.RECOVERY,
					AlarmSeverity.INFORMATIONAL, EcompClassification.INFORMATION,
					null), BeHealthCheckElasticSearchRecovery(EcompErrorCode.E_207, ErrorType.RECOVERY,
							AlarmSeverity.INFORMATIONAL, EcompClassification.INFORMATION,
							null), BeHealthCheckUebClusterRecovery(EcompErrorCode.E_208, ErrorType.RECOVERY,
									AlarmSeverity.INFORMATIONAL, EcompClassification.INFORMATION,
									null), FeHealthCheckRecovery(EcompErrorCode.E_209, ErrorType.RECOVERY,
											AlarmSeverity.INFORMATIONAL, EcompClassification.INFORMATION,
											null),DmaapHealthCheckRecovery( EcompErrorCode.E_210, ErrorType.RECOVERY,
													AlarmSeverity.INFORMATIONAL, EcompClassification.INFORMATION,
													null), BeHealthCheckError(EcompErrorCode.E_200, ErrorType.SYSTEM_ERROR,
													AlarmSeverity.CRITICAL, EcompClassification.ERROR,
													BeHealthCheckRecovery),

	BeHealthCheckJanusGraphError(EcompErrorCode.E_201, ErrorType.SYSTEM_ERROR, AlarmSeverity.CRITICAL,
			EcompClassification.ERROR, BeHealthCheckJanusGraphRecovery), BeHealthCheckElasticSearchError(
					EcompErrorCode.E_202, ErrorType.SYSTEM_ERROR, AlarmSeverity.CRITICAL, EcompClassification.ERROR,
					BeHealthCheckElasticSearchRecovery), BeHealthCheckUebClusterError(EcompErrorCode.E_203,
							ErrorType.SYSTEM_ERROR, AlarmSeverity.CRITICAL, EcompClassification.ERROR,
							BeHealthCheckUebClusterRecovery), FeHealthCheckError(EcompErrorCode.E_204,
									ErrorType.SYSTEM_ERROR, AlarmSeverity.CRITICAL, EcompClassification.ERROR,
									FeHealthCheckRecovery), BeUebConnectionError(EcompErrorCode.E_210,
											ErrorType.CONNECTION_PROBLEM, AlarmSeverity.MAJOR,
											EcompClassification.ERROR), BeUebUnkownHostError(EcompErrorCode.E_211,
													ErrorType.CONNECTION_PROBLEM, AlarmSeverity.MAJOR,
													EcompClassification.ERROR),

	FqdnResolveError(EcompErrorCode.E_212, ErrorType.CONNECTION_PROBLEM, AlarmSeverity.MAJOR,
			EcompClassification.ERROR), SiteSwitchoverInfo(EcompErrorCode.E_213, ErrorType.RECOVERY,
					AlarmSeverity.INFORMATIONAL, EcompClassification.INFORMATION),

	InternalConnectionInfo(EcompErrorCode.E_299, ErrorType.CONNECTION_PROBLEM, AlarmSeverity.INFORMATIONAL,
			EcompClassification.INFORMATION), InternalConnectionWarning(EcompErrorCode.E_299,
					ErrorType.CONNECTION_PROBLEM, AlarmSeverity.MINOR,
					EcompClassification.WARNING), InternalConnectionError(EcompErrorCode.E_299,
							ErrorType.CONNECTION_PROBLEM, AlarmSeverity.MAJOR,
							EcompClassification.ERROR), InternalConnectionFatal(EcompErrorCode.E_299,
									ErrorType.CONNECTION_PROBLEM, AlarmSeverity.CRITICAL, EcompClassification.FATAL),

	BeComponentMissingError(EcompErrorCode.E_300, ErrorType.SYSTEM_ERROR, AlarmSeverity.MAJOR,
			EcompClassification.ERROR), BeIncorrectComponentError(EcompErrorCode.E_301, ErrorType.SYSTEM_ERROR,
					AlarmSeverity.MAJOR, EcompClassification.ERROR), BeInvalidConfigurationError(EcompErrorCode.E_302,
							ErrorType.CONFIG_ERROR, AlarmSeverity.MAJOR,
							EcompClassification.FATAL), BeUebObjectNotFoundError(EcompErrorCode.E_303,
									ErrorType.DATA_ERROR, AlarmSeverity.MAJOR,
									EcompClassification.ERROR), BeDistributionEngineInvalidArtifactType(
											EcompErrorCode.E_304, ErrorType.DATA_ERROR, AlarmSeverity.MAJOR,
											EcompClassification.WARNING), BeMissingConfigurationError(
													EcompErrorCode.E_305, ErrorType.CONFIG_ERROR, AlarmSeverity.MAJOR,
													EcompClassification.FATAL), BeConfigurationInvalidListSizeError(
															EcompErrorCode.E_306, ErrorType.CONFIG_ERROR,
															AlarmSeverity.MAJOR,
															EcompClassification.FATAL), ErrorConfigFileFormat(
																	EcompErrorCode.E_307, ErrorType.CONFIG_ERROR,
																	AlarmSeverity.MAJOR,
																	EcompClassification.ERROR), BeMissingArtifactInformationError(
																			EcompErrorCode.E_308, ErrorType.DATA_ERROR,
																			AlarmSeverity.MAJOR,
																			EcompClassification.ERROR), BeArtifactMissingError(
																					EcompErrorCode.E_309,
																					ErrorType.DATA_ERROR,
																					AlarmSeverity.MAJOR,
																					EcompClassification.ERROR), BeUserMissingError(
																							EcompErrorCode.E_310,
																							ErrorType.DATA_ERROR,
																							AlarmSeverity.MAJOR,
																							EcompClassification.ERROR), EcompMismatchParam(
																									EcompErrorCode.E_311,
																									ErrorType.CONFIG_ERROR,
																									AlarmSeverity.MAJOR,
																									EcompClassification.ERROR), EcompMissingError(
																											EcompErrorCode.E_312,
																											ErrorType.CONFIG_ERROR,
																											AlarmSeverity.MAJOR,
																											EcompClassification.ERROR),

	InternalDataInfo(EcompErrorCode.E_399, ErrorType.DATA_ERROR, AlarmSeverity.INFORMATIONAL,
			EcompClassification.INFORMATION), InternalDataWarning(EcompErrorCode.E_399, ErrorType.DATA_ERROR,
					AlarmSeverity.MINOR, EcompClassification.WARNING), InternalDataError(EcompErrorCode.E_399,
							ErrorType.DATA_ERROR, AlarmSeverity.MAJOR,
							EcompClassification.ERROR), InternalDataFatal(EcompErrorCode.E_399, ErrorType.DATA_ERROR,
									AlarmSeverity.CRITICAL, EcompClassification.FATAL),

	BeInvalidTypeError(EcompErrorCode.E_400, ErrorType.DATA_ERROR, AlarmSeverity.MAJOR,
			EcompClassification.WARNING), BeInvalidValueError(EcompErrorCode.E_401, ErrorType.DATA_ERROR,
					AlarmSeverity.MAJOR, EcompClassification.WARNING), BeArtifactPayloadInvalid(EcompErrorCode.E_402,
							ErrorType.DATA_ERROR, AlarmSeverity.MAJOR,
							EcompClassification.ERROR), BeArtifactInformationInvalidError(EcompErrorCode.E_403,
									ErrorType.DATA_ERROR, AlarmSeverity.MAJOR,
									EcompClassification.ERROR), BeGraphObjectMissingError(EcompErrorCode.E_404,
											ErrorType.DATA_ERROR, AlarmSeverity.CRITICAL,
											EcompClassification.ERROR), BeInvalidJsonInput(EcompErrorCode.E_405,
													ErrorType.SYSTEM_ERROR, AlarmSeverity.MAJOR,
													EcompClassification.ERROR), BeDistributionMissingError(
															EcompErrorCode.E_406, ErrorType.DATA_ERROR,
															AlarmSeverity.MAJOR, EcompClassification.ERROR),

	InvalidInputInfo(EcompErrorCode.E_499, ErrorType.SYSTEM_ERROR, AlarmSeverity.INFORMATIONAL,
			EcompClassification.INFORMATION), InvalidInputWarning(EcompErrorCode.E_499, ErrorType.SYSTEM_ERROR,
					AlarmSeverity.MINOR, EcompClassification.WARNING), InvalidInputError(EcompErrorCode.E_499,
							ErrorType.SYSTEM_ERROR, AlarmSeverity.MAJOR,
							EcompClassification.ERROR), InvalidInputFatal(EcompErrorCode.E_499, ErrorType.SYSTEM_ERROR,
									AlarmSeverity.CRITICAL, EcompClassification.FATAL),

	BeInitializationError(EcompErrorCode.E_500, ErrorType.SYSTEM_ERROR, AlarmSeverity.CRITICAL,
			EcompClassification.ERROR), BeFailedAddingResourceInstanceError(EcompErrorCode.E_501,
					ErrorType.SYSTEM_ERROR, AlarmSeverity.MAJOR, EcompClassification.ERROR), BeUebSystemError(
							EcompErrorCode.E_502, ErrorType.SYSTEM_ERROR, AlarmSeverity.MAJOR,
							EcompClassification.ERROR), BeDistributionEngineSystemError(EcompErrorCode.E_503,
									ErrorType.SYSTEM_ERROR, AlarmSeverity.MAJOR,
									EcompClassification.ERROR), BeFailedAddingNodeTypeError(EcompErrorCode.E_504,
											ErrorType.DATA_ERROR, AlarmSeverity.CRITICAL,
											EcompClassification.ERROR), BeDaoSystemError(EcompErrorCode.E_505,
													ErrorType.SYSTEM_ERROR, AlarmSeverity.CRITICAL,
													EcompClassification.ERROR), BeSystemError(EcompErrorCode.E_506,
															ErrorType.SYSTEM_ERROR, AlarmSeverity.CRITICAL,
															EcompClassification.ERROR), BeExecuteRollbackError(
																	EcompErrorCode.E_507, ErrorType.DATA_ERROR,
																	AlarmSeverity.MAJOR,
																	EcompClassification.ERROR), BeFailedLockObjectError(
																			EcompErrorCode.E_508,
																			ErrorType.SYSTEM_ERROR,
																			AlarmSeverity.CRITICAL,
																			EcompClassification.WARNING), BeFailedCreateNodeError(
																					EcompErrorCode.E_509,
																					ErrorType.DATA_ERROR,
																					AlarmSeverity.MAJOR,
																					EcompClassification.ERROR), BeFailedUpdateNodeError(
																							EcompErrorCode.E_510,
																							ErrorType.DATA_ERROR,
																							AlarmSeverity.MAJOR,
																							EcompClassification.ERROR), BeFailedDeleteNodeError(
																									EcompErrorCode.E_511,
																									ErrorType.DATA_ERROR,
																									AlarmSeverity.MAJOR,
																									EcompClassification.ERROR), BeFailedRetrieveNodeError(
																											EcompErrorCode.E_512,
																											ErrorType.DATA_ERROR,
																											AlarmSeverity.MAJOR,
																											EcompClassification.ERROR), BeFailedFindParentError(
																													EcompErrorCode.E_513,
																													ErrorType.DATA_ERROR,
																													AlarmSeverity.MAJOR,
																													EcompClassification.ERROR), BeFailedFindAllNodesError(
																															EcompErrorCode.E_514,
																															ErrorType.DATA_ERROR,
																															AlarmSeverity.MAJOR,
																															EcompClassification.ERROR), BeFailedFindAssociationError(
																																	EcompErrorCode.E_515,
																																	ErrorType.DATA_ERROR,
																																	AlarmSeverity.MAJOR,
																																	EcompClassification.ERROR), BeComponentCleanerSystemError(
																																			EcompErrorCode.E_516,
																																			ErrorType.SYSTEM_ERROR,
																																			AlarmSeverity.MAJOR,
																																			EcompClassification.ERROR), FeHttpLoggingError(
																																					EcompErrorCode.E_517,
																																					ErrorType.SYSTEM_ERROR,
																																					AlarmSeverity.MINOR,
																																					EcompClassification.ERROR), FePortalServletError(
																																							EcompErrorCode.E_518,
																																							ErrorType.SYSTEM_ERROR,
																																							AlarmSeverity.MAJOR,
																																							EcompClassification.ERROR),

	InternalFlowInfo(EcompErrorCode.E_599, ErrorType.SYSTEM_ERROR, AlarmSeverity.INFORMATIONAL,
			EcompClassification.INFORMATION), InternalFlowWarning(EcompErrorCode.E_599, ErrorType.SYSTEM_ERROR,
					AlarmSeverity.MINOR, EcompClassification.WARNING), InternalFlowError(EcompErrorCode.E_599,
							ErrorType.SYSTEM_ERROR, AlarmSeverity.MAJOR,
							EcompClassification.ERROR), InternalFlowFatal(EcompErrorCode.E_599, ErrorType.SYSTEM_ERROR,
									AlarmSeverity.CRITICAL, EcompClassification.FATAL),

	BeRestApiGeneralError(EcompErrorCode.E_900, ErrorType.SYSTEM_ERROR, AlarmSeverity.CRITICAL,
			EcompClassification.ERROR, null), FeHealthCheckGeneralError(EcompErrorCode.E_901, ErrorType.SYSTEM_ERROR,
					AlarmSeverity.CRITICAL, EcompClassification.ERROR, null),

	InternalUnexpectedInfo(EcompErrorCode.E_999, ErrorType.SYSTEM_ERROR, AlarmSeverity.INFORMATIONAL,
			EcompClassification.INFORMATION), InternalUnexpectedWarning(EcompErrorCode.E_999, ErrorType.SYSTEM_ERROR,
					AlarmSeverity.MINOR, EcompClassification.WARNING), InternalUnexpectedError(EcompErrorCode.E_999,
							ErrorType.SYSTEM_ERROR, AlarmSeverity.MAJOR,
							EcompClassification.ERROR), InternalUnexpectedFatal(EcompErrorCode.E_999,
									ErrorType.SYSTEM_ERROR, AlarmSeverity.CRITICAL, EcompClassification.FATAL),

	/*
	 * BeUebAuthenticationError(EcompErrorCode.E_100,
	 * ErrorType.AUTHENTICATION_PROBLEM, AlarmSeverity.MAJOR,
	 * EcompClassification.ERROR),
	 * 
	 * InternalAuthenticationInfo(EcompErrorCode.E_199,
	 * ErrorType.AUTHENTICATION_PROBLEM, AlarmSeverity.INFORMATIONAL,
	 * EcompClassification.INFORMATION),
	 * InternalAuthenticationWarning(EcompErrorCode.E_199,
	 * ErrorType.AUTHENTICATION_PROBLEM, AlarmSeverity.MINOR,
	 * EcompClassification.WARNING),
	 * InternalAuthenticationError(EcompErrorCode.E_199,
	 * ErrorType.AUTHENTICATION_PROBLEM, AlarmSeverity.MAJOR,
	 * EcompClassification.ERROR),
	 * InternalAuthenticationFatal(EcompErrorCode.E_199,
	 * ErrorType.AUTHENTICATION_PROBLEM, AlarmSeverity.CRITICAL,
	 * EcompClassification.FATAL),
	 * //BeFailedDeletingResourceInstanceError(EcompErrorCode.E_200,
	 * ErrorType.SYSTEM_ERROR, AlarmSeverity.MAJOR, Classification.ERROR),
	 * 
	 * BeHealthCheckRecovery(EcompErrorCode.E_205, ErrorType.RECOVERY,
	 * AlarmSeverity.INFORMATIONAL, EcompClassification.INFORMATION, null),
	 * BeHealthCheckJanusGraphRecovery(EcompErrorCode.E_206, ErrorType.RECOVERY,
	 * AlarmSeverity.INFORMATIONAL, EcompClassification.INFORMATION, null),
	 * BeHealthCheckElasticSearchRecovery(EcompErrorCode.E_207,
	 * ErrorType.RECOVERY, AlarmSeverity.INFORMATIONAL,
	 * EcompClassification.INFORMATION, null),
	 * BeHealthCheckUebClusterRecovery(EcompErrorCode.E_208, ErrorType.RECOVERY,
	 * AlarmSeverity.INFORMATIONAL, EcompClassification.INFORMATION, null),
	 * FeHealthCheckRecovery(EcompErrorCode.E_209, ErrorType.RECOVERY,
	 * AlarmSeverity.INFORMATIONAL, EcompClassification.INFORMATION, null),
	 * BeHealthCheckError(EcompErrorCode.E_200, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.ERROR,
	 * BeHealthCheckRecovery),
	 * 
	 * BeHealthCheckJanusGraphError(EcompErrorCode.E_201, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.ERROR,
	 * BeHealthCheckJanusGraphRecovery),
	 * BeHealthCheckElasticSearchError(EcompErrorCode.E_202,
	 * ErrorType.SYSTEM_ERROR, AlarmSeverity.CRITICAL,
	 * EcompClassification.ERROR, BeHealthCheckElasticSearchRecovery),
	 * BeHealthCheckUebClusterError(EcompErrorCode.E_203,
	 * ErrorType.SYSTEM_ERROR, AlarmSeverity.CRITICAL,
	 * EcompClassification.ERROR, BeHealthCheckUebClusterRecovery),
	 * FeHealthCheckError(EcompErrorCode.E_204, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.ERROR,
	 * FeHealthCheckRecovery), BeUebConnectionError(EcompErrorCode.E_210,
	 * ErrorType.CONNECTION_PROBLEM, AlarmSeverity.MAJOR,
	 * EcompClassification.ERROR), BeUebUnkownHostError(EcompErrorCode.E_211,
	 * ErrorType.CONNECTION_PROBLEM, AlarmSeverity.MAJOR,
	 * EcompClassification.ERROR),
	 * 
	 * InternalConnectionInfo(EcompErrorCode.E_299,
	 * ErrorType.CONNECTION_PROBLEM, AlarmSeverity.INFORMATIONAL,
	 * EcompClassification.INFORMATION),
	 * InternalConnectionWarning(EcompErrorCode.E_299,
	 * ErrorType.CONNECTION_PROBLEM, AlarmSeverity.MINOR,
	 * EcompClassification.WARNING),
	 * InternalConnectionError(EcompErrorCode.E_299,
	 * ErrorType.CONNECTION_PROBLEM, AlarmSeverity.MAJOR,
	 * EcompClassification.ERROR), InternalConnectionFatal(EcompErrorCode.E_299,
	 * ErrorType.CONNECTION_PROBLEM, AlarmSeverity.CRITICAL,
	 * EcompClassification.FATAL),
	 * 
	 * BeComponentMissingError(EcompErrorCode.E_300, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeIncorrectComponentError(EcompErrorCode.E_301, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeInvalidConfigurationError(EcompErrorCode.E_302, ErrorType.CONFIG_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.FATAL),
	 * BeUebObjectNotFoundError(EcompErrorCode.E_303, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeDistributionEngineInvalidArtifactType(EcompErrorCode.E_304,
	 * ErrorType.DATA_ERROR, AlarmSeverity.MAJOR, EcompClassification.WARNING),
	 * BeMissingConfigurationError(EcompErrorCode.E_305, ErrorType.CONFIG_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.FATAL),
	 * BeConfigurationInvalidListSizeError(EcompErrorCode.E_306,
	 * ErrorType.CONFIG_ERROR, AlarmSeverity.MAJOR, EcompClassification.FATAL),
	 * ErrorConfigFileFormat(EcompErrorCode.E_307, ErrorType.CONFIG_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeMissingArtifactInformationError(EcompErrorCode.E_308,
	 * ErrorType.DATA_ERROR, AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeArtifactMissingError(EcompErrorCode.E_309, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeUserMissingError(EcompErrorCode.E_310, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * EcompMismatchParam(EcompErrorCode.E_311, ErrorType.CONFIG_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * EcompMissingError(EcompErrorCode.E_312, ErrorType.CONFIG_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * 
	 * InternalDataInfo(EcompErrorCode.E_399, ErrorType.DATA_ERROR,
	 * AlarmSeverity.INFORMATIONAL, EcompClassification.INFORMATION),
	 * InternalDataWarning(EcompErrorCode.E_399, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MINOR, EcompClassification.WARNING),
	 * InternalDataError(EcompErrorCode.E_399, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * InternalDataFatal(EcompErrorCode.E_399, ErrorType.DATA_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.FATAL),
	 * 
	 * BeInvalidTypeError(EcompErrorCode.E_400, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.WARNING),
	 * BeInvalidValueError(EcompErrorCode.E_401, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.WARNING),
	 * BeArtifactPayloadInvalid(EcompErrorCode.E_402, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeArtifactInformationInvalidError(EcompErrorCode.E_403,
	 * ErrorType.DATA_ERROR, AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeDistributionMissingError(EcompErrorCode.E_404, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeGraphObjectMissingError(EcompErrorCode.E_404, ErrorType.DATA_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.ERROR),
	 * BeInvalidJsonInput(EcompErrorCode.E_405, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * 
	 * InvalidInputInfo(EcompErrorCode.E_499, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.INFORMATIONAL, EcompClassification.INFORMATION),
	 * InvalidInputWarning(EcompErrorCode.E_499, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.MINOR, EcompClassification.WARNING),
	 * InvalidInputError(EcompErrorCode.E_499, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * InvalidInputFatal(EcompErrorCode.E_499, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.FATAL),
	 * 
	 * BeInitializationError(EcompErrorCode.E_500, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.ERROR),
	 * BeFailedAddingResourceInstanceError(EcompErrorCode.E_501,
	 * ErrorType.SYSTEM_ERROR, AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeUebSystemError(EcompErrorCode.E_502, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeDistributionEngineSystemError(EcompErrorCode.E_503,
	 * ErrorType.SYSTEM_ERROR, AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeFailedAddingNodeTypeError(EcompErrorCode.E_504, ErrorType.DATA_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.ERROR),
	 * BeDaoSystemError(EcompErrorCode.E_505, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.ERROR),
	 * BeSystemError(EcompErrorCode.E_506, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.ERROR),
	 * BeExecuteRollbackError(EcompErrorCode.E_507, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeFailedLockObjectError(EcompErrorCode.E_508, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.WARNING),
	 * BeFailedCreateNodeError(EcompErrorCode.E_509, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeFailedUpdateNodeError(EcompErrorCode.E_510, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeFailedDeleteNodeError(EcompErrorCode.E_511, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeFailedRetrieveNodeError(EcompErrorCode.E_512, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeFailedFindParentError(EcompErrorCode.E_513, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeFailedFindAllNodesError(EcompErrorCode.E_514, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeFailedFindAssociationError(EcompErrorCode.E_515, ErrorType.DATA_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * BeComponentCleanerSystemError(EcompErrorCode.E_516,
	 * ErrorType.SYSTEM_ERROR, AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * FeHttpLoggingError(EcompErrorCode.E_517, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.MINOR, EcompClassification.ERROR),
	 * FePortalServletError(EcompErrorCode.E_518, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * 
	 * InternalFlowInfo(EcompErrorCode.E_599, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.INFORMATIONAL, EcompClassification.INFORMATION),
	 * InternalFlowWarning(EcompErrorCode.E_599, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.MINOR, EcompClassification.WARNING),
	 * InternalFlowError(EcompErrorCode.E_599, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * InternalFlowFatal(EcompErrorCode.E_599, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.FATAL),
	 * 
	 * BeRestApiGeneralError(EcompErrorCode.E_900, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.ERROR, null),
	 * FeHealthCheckGeneralError(EcompErrorCode.E_901, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.ERROR, null),
	 * 
	 * InternalUnexpectedInfo(EcompErrorCode.E_999, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.INFORMATIONAL, EcompClassification.INFORMATION),
	 * InternalUnexpectedWarning(EcompErrorCode.E_999, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.MINOR, EcompClassification.WARNING),
	 * InternalUnexpectedError(EcompErrorCode.E_999, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.MAJOR, EcompClassification.ERROR),
	 * InternalUnexpectedFatal(EcompErrorCode.E_999, ErrorType.SYSTEM_ERROR,
	 * AlarmSeverity.CRITICAL, EcompClassification.FATAL),
	 */

	;

	EcompErrorCode ecompErrorCode;
	ErrorType eType;
	AlarmSeverity alarmSeverity;
	EcompClassification classification;
	EcompErrorEnum clearCode;

	EcompErrorEnum(EcompErrorCode ecompErrorCode, ErrorType eType, AlarmSeverity alarmSeverity,
			EcompClassification classification, EcompErrorEnum clearCode) {

		this.ecompErrorCode = ecompErrorCode;
		this.eType = eType;
		this.alarmSeverity = alarmSeverity;
		this.classification = classification;
		this.clearCode = clearCode;
	}

	EcompErrorEnum(EcompErrorCode ecompErrorCode, ErrorType eType, AlarmSeverity alarmSeverity,
			EcompClassification classification) {

		this.ecompErrorCode = ecompErrorCode;
		this.eType = eType;
		this.alarmSeverity = alarmSeverity;
		this.classification = classification;
	}

	public ErrorType geteType() {
		return eType;
	}

	public void seteType(ErrorType eType) {
		this.eType = eType;
	}

	public AlarmSeverity getAlarmSeverity() {
		return alarmSeverity;
	}

	public void setAlarmSeverity(AlarmSeverity alarmSeverity) {
		this.alarmSeverity = alarmSeverity;
	}

	public EcompErrorCode getEcompErrorCode() {
		return ecompErrorCode;
	}

	public void setEcompErrorCode(EcompErrorCode ecompErrorCode) {
		this.ecompErrorCode = ecompErrorCode;
	}

	public EcompClassification getClassification() {
		return classification;
	}

	public void setClassification(EcompClassification classification) {
		this.classification = classification;
	}

	public EcompErrorEnum getClearCode() {
		return clearCode;
	}

	public void setClearCode(EcompErrorEnum clearCode) {
		this.clearCode = clearCode;
	}

	public static enum ErrorType {
		RECOVERY, CONFIG_ERROR, SYSTEM_ERROR, DATA_ERROR, CONNECTION_PROBLEM, AUTHENTICATION_PROBLEM
	}

	public static enum AlarmSeverity {
		CRITICAL, MAJOR, MINOR, INFORMATIONAL, NONE
	}

	// public String toString() {
	// return eType + "," + eCode + "," + desc;
	// }
}
