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

package org.openecomp.sdc.be.components.impl;

//Pavel
//currently NOT IN USE - there are no informational deployed artifacts after US601880 

@org.springframework.stereotype.Component("informationDeployedArtifactsBusinessLogic")
public class InformationDeployedArtifactsBusinessLogic {

	/*
	 * private static Logger log = LoggerFactory.getLogger(InformationDeployedArtifactsBusinessLogic.class. getName());
	 * 
	 * @javax.annotation.Resource private ArtifactsBusinessLogic artifactBusinessLogic;
	 * 
	 * public boolean isInformationDeployedArtifact(ArtifactDefinition artifactInfo) { log.debug("checking if artifact {} is informationalDeployable", artifactInfo.getArtifactName()); boolean informationDeployedArtifact = false; Map<String,
	 * DeploymentArtifactTypeConfig> resourceInformationalDeployedArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration() .getResourceInformationalDeployedArtifacts(); if( resourceInformationalDeployedArtifacts != null &&
	 * resourceInformationalDeployedArtifacts.containsKey(artifactInfo. getArtifactType())){ if( artifactInfo.getArtifactGroupType() == ArtifactGroupTypeEnum.INFORMATIONAL && artifactInfo.checkEsIdExist()){ informationDeployedArtifact = true;
	 * 
	 * } } String message = (informationDeployedArtifact) ? "artifact {} is informationalDeployable" : "artifact {} is not informationalDeployable"; log.debug(message, artifactInfo.getArtifactName()); return informationDeployedArtifact; }
	 * 
	 * public Either<Boolean, ResponseFormat> validateArtifact(boolean isCreate, ArtifactDefinition artifactInfo, Component parent, NodeTypeEnum parentType) {
	 * 
	 * log.debug("checking if informationalDeployable artifact {} is valid ", artifactInfo.getArtifactName()); Wrapper<ResponseFormat> responseFormatWrapper = new Wrapper<ResponseFormat>(); Map<String, DeploymentArtifactTypeConfig>
	 * resourceInformationalDeployedArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration(). getResourceInformationalDeployedArtifacts();
	 * 
	 * 
	 * artifactBusinessLogic.validateArtifactTypeExists(responseFormatWrapper, artifactInfo); if( responseFormatWrapper.isEmpty() && isCreate ){ artifactBusinessLogic.validateSingleArtifactType(responseFormatWrapper,
	 * ArtifactTypeEnum.findType(artifactInfo.getArtifactType()), parent, parentType); } if( responseFormatWrapper.isEmpty() ){ ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactInfo.getArtifactType());
	 * artifactBusinessLogic.validateFileExtension(responseFormatWrapper, () -> resourceInformationalDeployedArtifacts.get(artifactInfo.getArtifactType() ), artifactInfo, parentType, artifactType); }
	 * 
	 * if( responseFormatWrapper.isEmpty() ){ validatePayloadContent(responseFormatWrapper, artifactInfo); }
	 * 
	 * Either<Boolean, ResponseFormat> response; if( responseFormatWrapper.isEmpty() ){ response = Either.left(true); } else{ response = Either.right(responseFormatWrapper.getInnerElement()); }
	 * 
	 * String message = (response.isLeft()) ? "informationalDeployable artifact {} is valid" : "informationalDeployable artifact {} is not valid"; log.debug(message, artifactInfo.getArtifactName()); return response; }
	 * 
	 * private void validatePayloadContent(Wrapper<ResponseFormat> responseFormatWrapper, ArtifactDefinition artifactToVerify) { ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifactToVerify.getArtifactType()); if( artifactType ==
	 * ArtifactTypeEnum.YANG_XML ){ String rawPayloadData = artifactToVerify.getPayloadData(); String xmlToParse = new String(org.apache.commons.codec.binary.Base64.decodeBase64(rawPayloadData ));
	 * 
	 * boolean isXmlValid = artifactBusinessLogic.isValidXml(xmlToParse.getBytes()); if( !isXmlValid ){ ResponseFormat responseFormat = ResponseFormatManager.getInstance().getResponseFormat(ActionStatus. INVALID_XML,
	 * artifactToVerify.getArtifactType()); responseFormatWrapper.setInnerElement(responseFormat); log.debug("Xml is not valid for artifact : {}", artifactToVerify.getArtifactName()); } } }
	 * 
	 * 
	 * 
	 * 
	 * public List<ArtifactDefinition> getInformationalDeployedArtifactsForResourceInstance( ComponentInstance resourceInstance ) { String resourceUid = resourceInstance.getComponentUid(); String resourceName = resourceInstance.getComponentName();
	 * return getInformationalDeployedArtifactsForResource(resourceUid, resourceName); }
	 * 
	 * private List<ArtifactDefinition> getInformationalDeployedArtifactsForResource( String resourceUid, String resourceName) { List<ArtifactDefinition> informationalDeployedArtifacts = new ArrayList<ArtifactDefinition>(); log.
	 * debug("checking if informationalDeployable artifacts exist for resource {} " , resourceName); Map<String, DeploymentArtifactTypeConfig> resourceInformationalDeployedArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration()
	 * .getResourceInformationalDeployedArtifacts();
	 * 
	 * 
	 * Either<Map<String, ArtifactDefinition>, StorageOperationStatus> eitherArtifacts = artifactBusinessLogic.getArtifacts(resourceUid, NodeTypeEnum.Resource, false, ArtifactGroupTypeEnum.INFORMATIONAL);
	 * 
	 * if( eitherArtifacts.isLeft() && resourceInformationalDeployedArtifacts != null ){ Predicate<ArtifactDefinition> predicate = p -> resourceInformationalDeployedArtifacts.containsKey(p.getArtifactType()) && p.checkEsIdExist() &&
	 * p.getArtifactGroupType() == ArtifactGroupTypeEnum.INFORMATIONAL; informationalDeployedArtifacts = eitherArtifacts.left().value().values().stream().filter( predicate ).collect(Collectors.toList()); }
	 * 
	 * if( informationalDeployedArtifacts.isEmpty() ){ log.debug("no informationalDeployable artifacts exist for resource {} ", resourceName); } else{ log. debug("informationalDeployable artifacts found for resource {} are :{}", resourceName,
	 * informationalDeployedArtifacts.toString()); }
	 * 
	 * 
	 * return informationalDeployedArtifacts; }
	 * 
	 * public List<ArtifactDefinition> getAllDeployableArtifacts( Resource resource ){ List<ArtifactDefinition> merged = new ArrayList<ArtifactDefinition>();
	 * 
	 * List<ArtifactDefinition> deploymentArtifacts = artifactBusinessLogic.getDeploymentArtifacts(resource, NodeTypeEnum.Resource); merged.addAll(deploymentArtifacts);
	 * 
	 * List<ArtifactDefinition> informationalDeployedArtifactsForResource = getInformationalDeployedArtifactsForResource( resource.getUniqueId(), resource.getName()); merged.addAll(informationalDeployedArtifactsForResource);
	 * 
	 * return merged; }
	 */
}
