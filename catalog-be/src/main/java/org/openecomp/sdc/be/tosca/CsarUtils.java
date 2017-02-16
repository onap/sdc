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

package org.openecomp.sdc.be.tosca;

import java.io.IOException;
import org.apache.commons.codec.binary.Base64;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Triple;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperation;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.ServiceOperation;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.openecomp.sdc.generator.data.Artifact;
import org.openecomp.sdc.generator.data.ArtifactType;
import org.openecomp.sdc.generator.data.GenerationData;
import org.openecomp.sdc.generator.impl.ArtifactGenerationServiceImpl;
import com.google.gson.Gson;

import fj.data.Either;

/**
 * @author tg851x
 *
 */
@org.springframework.stereotype.Component("csar-utils")
public class CsarUtils {
	private static Logger log = LoggerFactory.getLogger(ToscaExportHandler.class.getName());

	@Autowired
	private ArtifactCassandraDao artifactCassandraDao;
	@Autowired
	private ComponentsUtils componentsUtils;
	@Autowired
	private ToscaExportHandler toscaExportUtils;
	@Autowired
	private ArtifactsBusinessLogic artifactsBusinessLogic;
	@Autowired
	protected ServiceOperation serviceOperation;

	@javax.annotation.Resource
	private ServiceBusinessLogic serviceBusinessLogic;

	private Gson gson = new Gson();

	private static final String DEFINITIONS_PATH = "Definitions/";
	private static final String ARTIFACTS_PATH = "Artifacts/";
	private static final String TOSCA_META_PATH_FILE_NAME = "TOSCA-Metadata/TOSCA.meta";
	private static final String TOSCA_META_VERSION = "1.0";
	private static final String CSAR_VERSION = "1.1";

	/**
	 * 
	 * @param component
	 * @param getFromCS
	 * @param isInCertificationRequest
	 * @param shouldLock
	 * @param inTransaction
	 * @return
	 */
	public Either<byte[], ResponseFormat> createCsar(Component component, boolean getFromCS, boolean isInCertificationRequest, boolean shouldLock, boolean inTransaction) {
		return createCsar(component, getFromCS, isInCertificationRequest, false, shouldLock, inTransaction);
	}

	private Either<byte[], ResponseFormat> createCsar(Component component, boolean getFromCS, boolean isInCertificationRequest, boolean mockGenerator, boolean shouldLock, boolean inTransaction) {
		final String CREATED_BY = component.getCreatorFullName();

		String fileName;
		Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
		ArtifactDefinition artifactDefinition = toscaArtifacts.get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE);
		fileName = artifactDefinition.getArtifactName();

		String toscaBlock0 = createToscaBlock0(TOSCA_META_VERSION, CSAR_VERSION, CREATED_BY, fileName);

		byte[] toscaBlock0Byte = toscaBlock0.getBytes();

		Either<byte[], ResponseFormat> generateCsarZipResponse = generateCsarZip(toscaBlock0Byte, component, getFromCS, isInCertificationRequest, mockGenerator, shouldLock, inTransaction);

		if (generateCsarZipResponse.isRight()) {
			return Either.right(generateCsarZipResponse.right().value());
		}

		return Either.left(generateCsarZipResponse.left().value());
	}

	private Either<byte[], ResponseFormat> generateCsarZip(byte[] toscaBlock0Byte, Component component, boolean getFromCS, boolean isInCertificationRequest, boolean mockGenerator, boolean shouldLock, boolean inTransaction) {

		ZipOutputStream zip = null;
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			zip = new ZipOutputStream(out);

			zip.putNextEntry(new ZipEntry(TOSCA_META_PATH_FILE_NAME));
			zip.write(toscaBlock0Byte);
			Either<ZipOutputStream, ResponseFormat> populateZip = populateZip(component, getFromCS, zip, isInCertificationRequest, mockGenerator, shouldLock, inTransaction);
			if (populateZip.isRight()) {
				log.debug("Failed to populate CSAR zip file {}", populateZip.right().value());
				return Either.right(populateZip.right().value());
			}
			zip = populateZip.left().value();

			zip.finish();
			byte[] byteArray = out.toByteArray();

			return Either.left(byteArray);
		} catch (IOException e) {
			log.debug("createCsar failed IOexception", e);

			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			return Either.right(responseFormat);
		} finally {
			try {
				if (zip != null) {
					zip.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				log.error("Failed to close resources ", e);
			}
		}
	}

	private Either<ZipOutputStream, ResponseFormat> populateZip(Component component, boolean getFromCS, ZipOutputStream zip, boolean isInCertificationRequest, boolean mockGenerator, boolean shouldLock, boolean inTransaction) {

		LifecycleStateEnum lifecycleState = component.getLifecycleState();
		String componentYaml = null;
		Either<ToscaRepresentation, ToscaError> exportComponent = null;
		byte[] mainYaml = null;
		// <file name, esid, component>
		List<Triple<String, String, Component>> dependencies = null;
		List<ImmutablePair<Component, byte[]>> generatorInputs = new LinkedList<>();

		String fileName;
		Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
		ArtifactDefinition artifactDefinition = toscaArtifacts.get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE);
		fileName = artifactDefinition.getArtifactName();

		if (getFromCS || !(lifecycleState == LifecycleStateEnum.NOT_CERTIFIED_CHECKIN || lifecycleState == LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT)) {
			String esId = artifactDefinition.getEsId();
			Either<byte[], ActionStatus> fromCassandra = getFromCassandra(esId);
			if (fromCassandra.isRight()) {
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(fromCassandra.right().value());
				return Either.right(responseFormat);
			}
			mainYaml = fromCassandra.left().value();

		} else {
			exportComponent = toscaExportUtils.exportComponent(component);
			if (exportComponent.isRight()) {
				log.debug("exportComponent failed", exportComponent.right().value());
				ActionStatus convertedFromToscaError = componentsUtils.convertFromToscaError(exportComponent.right().value());
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(convertedFromToscaError);
				return Either.right(responseFormat);
			}
			ToscaRepresentation exportResult = exportComponent.left().value();
			componentYaml = exportResult.getMainYaml();
			mainYaml = componentYaml.getBytes();
			dependencies = exportResult.getDependencies();
		}

		try {
			zip.putNextEntry(new ZipEntry(DEFINITIONS_PATH + fileName));
			zip.write(mainYaml);

			generatorInputs.add(new ImmutablePair<Component, byte[]>(component, mainYaml));

			if (dependencies == null) {
				Either<ToscaTemplate, ToscaError> dependenciesRes = toscaExportUtils.getDependencies(component);
				if (dependenciesRes.isRight()) {
					log.debug("Failed to retrieve dependencies for component {}, error {}", component.getUniqueId(), dependenciesRes.right().value());
					ActionStatus convertFromToscaError = componentsUtils.convertFromToscaError(dependenciesRes.right().value());
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(convertFromToscaError);
					return Either.right(responseFormat);
				}
				dependencies = dependenciesRes.left().value().getDependencies();
			}
			if (dependencies != null && !dependencies.isEmpty()) {
				for (Triple<String, String, Component> d : dependencies) {
					String esId = d.getMiddle();
					Component childComponent = d.getRight();
					fileName = d.getLeft();
					Either<byte[], ActionStatus> entryData = getEntryData(esId, childComponent);

					if (entryData.isRight()) {
						ResponseFormat responseFormat = componentsUtils.getResponseFormat(entryData.right().value());
						return Either.right(responseFormat);
					}

					byte[] content = entryData.left().value();
					zip.putNextEntry(new ZipEntry(DEFINITIONS_PATH + fileName));
					zip.write(content);

					generatorInputs.add(new ImmutablePair<Component, byte[]>(childComponent, content));
				}
			}

			List<ArtifactDefinition> aiiArtifactList = new LinkedList<>();
			// Artifact Generation
			if (component.getComponentType() == ComponentTypeEnum.SERVICE && (lifecycleState == LifecycleStateEnum.NOT_CERTIFIED_CHECKIN || lifecycleState == LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT)) {
				Either<List<ArtifactDefinition>, ResponseFormat> handleAAIArtifacts = handleAAIArtifacts(component, zip, mockGenerator, shouldLock, inTransaction, generatorInputs);

				if (handleAAIArtifacts.isLeft()) {
					aiiArtifactList = handleAAIArtifacts.left().value();
				} else {
					log.debug("AAI Artifacts handling failed");
					return Either.right(handleAAIArtifacts.right().value());
				}

				if (isInCertificationRequest) {
					Either<ActionStatus, ResponseFormat> handleAllAAIArtifactsInDataModel = handleAllAAIArtifactsInDataModel(component, aiiArtifactList, shouldLock, inTransaction);

					if (handleAllAAIArtifactsInDataModel.isRight()) {
						log.debug("AAI Artifacts handling (create, update, delete) failed");
						return Either.right(handleAllAAIArtifactsInDataModel.right().value());
					}
				}

			}

			// Collecting All Deployment Artifacts
			Either<ZipOutputStream, ActionStatus> collectAndWriteToScarDeploymentArtifacts = collectAndWriteToScarDeploymentArtifacts(zip, component, aiiArtifactList);

			if (collectAndWriteToScarDeploymentArtifacts.isRight()) {
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
			}
		} catch (IOException e) {
			log.debug("Failed to create CSAR zip for component {}", component.getUniqueId(), e);
		}

		return Either.left(zip);
	}

	private Either<ZipOutputStream, ActionStatus> collectAndWriteToScarDeploymentArtifacts(ZipOutputStream zip, Component component, List<ArtifactDefinition> aiiArtifactList) throws IOException {

		Collection<ArtifactDefinition> deploymentArtifactsToAdd = null;
		Collection<ArtifactDefinition> allArtifactsToAdd = new LinkedList<>();

		if (component.getComponentType() == ComponentTypeEnum.SERVICE) {
			Either<Service, StorageOperationStatus> getServiceResponse = serviceOperation.getService(component.getUniqueId());

			if (getServiceResponse.isLeft()) {
				Service service = getServiceResponse.left().value();

				if (!aiiArtifactList.isEmpty()) {
					deploymentArtifactsToAdd = service.getDeploymentArtifacts().values().stream().filter(e -> e.getGenerated() == null || !e.getGenerated()).collect(Collectors.toList());
					allArtifactsToAdd.addAll(aiiArtifactList);
					allArtifactsToAdd.addAll(deploymentArtifactsToAdd);
				} else {
					allArtifactsToAdd.addAll(service.getDeploymentArtifacts().values());
				}
			}
		}

		if (!allArtifactsToAdd.isEmpty()) {

			for (ArtifactDefinition deploymentArtifactDefinition : allArtifactsToAdd) {
				String artifactFileName = deploymentArtifactDefinition.getArtifactName();
				byte[] payloadData = deploymentArtifactDefinition.getPayloadData();

				if (payloadData == null) {
					String esId = deploymentArtifactDefinition.getEsId();
					if (esId != null) {
						Either<byte[], ActionStatus> fromCassandra = getFromCassandra(esId);

						if (fromCassandra.isRight()) {
							return Either.right(fromCassandra.right().value());
						}
						payloadData = fromCassandra.left().value();
					} else {
						log.debug("Artifact {} payload not supplied in ArtifactDefinition and not found in DB", artifactFileName);
						continue;
					}
				}

				byte[] decodedPayload = null;

				if (Base64.isBase64(payloadData)) {
					// decodedPayload = Base64.getDecoder().decode(payloadData);
					decodedPayload = Base64.decodeBase64(payloadData);
				} else {
					decodedPayload = payloadData;
				}

				zip.putNextEntry(new ZipEntry(ARTIFACTS_PATH + artifactFileName));
				zip.write(decodedPayload);
			}
		}

		return Either.left(zip);
	}

	private Either<List<ArtifactDefinition>, ResponseFormat> handleAAIArtifacts(Component component, ZipOutputStream zip, boolean mockGenerator, boolean shouldLock, boolean inTransaction, List<ImmutablePair<Component, byte[]>> generatorInputs) {

		ComponentTypeEnum componentType = component.getComponentType();
		List<Artifact> generatedArtifacts = null;
		List<ArtifactDefinition> aaiArtifacts = null;

		if (componentType == ComponentTypeEnum.SERVICE && !generatorInputs.isEmpty()) {
			List<Artifact> convertedGeneratorInputs = convertToGeneratorArtifactsInput(generatorInputs);

			Either<List<Artifact>, ResponseFormat> generatorResponse;

			if (mockGenerator) {
				generatorResponse = artifactGenerator(convertedGeneratorInputs, ArtifactType.OTHER, component);
			} else {
				generatorResponse = artifactGenerator(convertedGeneratorInputs, ArtifactType.AAI, component);
			}

			if (generatorResponse.isRight()) {
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.AAI_ARTIFACT_GENERATION_FAILED, component.getComponentType().getValue(), component.getName(), generatorResponse.toString());
				return Either.right(responseFormat);
			}

			generatedArtifacts = generatorResponse.left().value();

			aaiArtifacts = convertToArtifactDefinitionFromArtifactGeneratedData(generatedArtifacts);

		}

		return Either.left(aaiArtifacts);
	}

	private Either<ActionStatus, ResponseFormat> handleAllAAIArtifactsInDataModel(Component component, List<ArtifactDefinition> artifactsFromAAI, boolean shouldLock, boolean inTransaction) {

		Either<ActionStatus, ResponseFormat> handleAAIArtifactsResponse = null;
		User lastComponentUpdater = null;

		List<ArtifactDefinition> aaiArtifatcsToCreate = getAAIArtifatcsForCreate(artifactsFromAAI, component);
		List<ArtifactDefinition> aaiArtifatcsToDelete = getAAIArtifatcsForDelete(artifactsFromAAI, component);
		List<ArtifactDefinition> aaiArtifatcsToUpdate = getAAIArtifatcsForUpdate(artifactsFromAAI, component);

		String lastUpdaterUserId = component.getLastUpdaterUserId();
		Either<User, ResponseFormat> validateUserExists = artifactsBusinessLogic.validateUserExists(lastUpdaterUserId, "CSAR creation util", true);

		if (validateUserExists.isRight()) {
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.AAI_ARTIFACT_GENERATION_FAILED, component.getComponentType().getValue(), component.getName(), "User not found");
			return Either.right(responseFormat);
		}

		lastComponentUpdater = validateUserExists.left().value();

		handleAAIArtifactsResponse = handleAAIArtifactsInDataModelByOperationType(component, aaiArtifatcsToDelete, ArtifactOperation.Delete, lastComponentUpdater, shouldLock, inTransaction);

		if (handleAAIArtifactsResponse.isRight()) {
			return handleAAIArtifactsResponse;
		}

		handleAAIArtifactsResponse = handleAAIArtifactsInDataModelByOperationType(component, aaiArtifatcsToCreate, ArtifactOperation.Create, lastComponentUpdater, shouldLock, inTransaction);

		if (handleAAIArtifactsResponse.isRight()) {
			return handleAAIArtifactsResponse;
		}

		return handleAAIArtifactsInDataModelByOperationType(component, aaiArtifatcsToUpdate, ArtifactOperation.Update, lastComponentUpdater, shouldLock, inTransaction);
	}

	private List<ArtifactDefinition> getAAIArtifatcsForUpdate(List<ArtifactDefinition> artifactsFromAAI, Component component) {

		Set<String> componetDeploymentArtifactLables = component.getDeploymentArtifacts().keySet();
		Set<String> componetInformationalArtifactLables = component.getArtifacts().keySet();

		List<ArtifactDefinition> artifactsAaiUpdate = artifactsFromAAI.stream().filter(e -> (componetDeploymentArtifactLables.contains(e.getArtifactLabel()) || componetInformationalArtifactLables.contains(e.getArtifactLabel())))
				.filter(e -> checkAaiForUpdate(component, e)).collect(Collectors.toList());

		return artifactsAaiUpdate;
	}

	private boolean checkAaiForUpdate(Component component, ArtifactDefinition artifactDefinition) {
		ArtifactDefinition artifactDefinitionComp = component.getDeploymentArtifacts().get(artifactDefinition.getArtifactLabel());

		if (artifactDefinitionComp == null) {
			log.warn("Failed to get {} artifact", artifactDefinition.getArtifactLabel());
			return false;
		}

		// Old Artifacts before the generated flag introduction if contains "aai" ignore case prefix updated
		if (artifactDefinitionComp.getGenerated() == null) {
			if (artifactDefinitionComp.getArtifactLabel().toLowerCase().startsWith("aai")) {
				return true;
			} else {
				log.warn("The artifact {} flag is null but AAI prefix is abssent Not updated", artifactDefinition.getArtifactLabel());
			}
		} else {
			if (artifactDefinition.getGenerated()) {
				return true;
			} else {
				log.warn("Generated artifact {} was already uploaded manually", artifactDefinition.getArtifactLabel());
			}
		}
		return false;
	}

	private List<ArtifactDefinition> getAAIArtifatcsForDelete(List<ArtifactDefinition> artifactsFromAAI, Component component) {

		Set<String> aaiLabels = artifactsFromAAI.stream().map(e -> e.getArtifactLabel()).collect(Collectors.toSet());

		List<ArtifactDefinition> artifactsForDeleteDeployment = component.getDeploymentArtifacts().values().stream().
		// Filter Out Artifacts that are not contained in artifacts returned
		// from AAI API
				filter(e -> !aaiLabels.contains(e.getArtifactLabel())).collect(Collectors.toList());

		List<ArtifactDefinition> artifactsForDeleteInformational = component.getArtifacts().values().stream().
		// Filter Out Artifacts that are not contained in artifacts returned
		// from AAI API
				filter(e -> !aaiLabels.contains(e.getArtifactLabel())).collect(Collectors.toList());

		artifactsForDeleteDeployment.addAll(artifactsForDeleteInformational);

		return artifactsForDeleteDeployment.stream().filter(e -> (e.getGenerated() != null && e.getGenerated().equals(Boolean.TRUE)) || (e.getGenerated() == null && e.getArtifactLabel().toLowerCase().startsWith("aai"))).collect(Collectors.toList());
	}

	private List<ArtifactDefinition> getAAIArtifatcsForCreate(List<ArtifactDefinition> artifactsFromAAI, Component component) {

		Set<String> componentDeploymentLabels = component.getDeploymentArtifacts().keySet();
		Set<String> componentInfoLabels = component.getArtifacts().keySet();

		// If the artifact label does not exist in the service -
		// store the artifact (generate uuid and version, "generated" flag is TRUE)
		return artifactsFromAAI.stream().filter(e -> !componentDeploymentLabels.contains(e.getArtifactLabel()) && !componentInfoLabels.contains(e.getArtifactLabel())).collect(Collectors.toList());
	}

	private Either<ActionStatus, ResponseFormat> handleAAIArtifactsInDataModelByOperationType(Component component, List<ArtifactDefinition> generatedArtifactsDefinitions, ArtifactOperation operationType, User user, boolean shouldLock,
			boolean inTransaction) {

		String componentUniqueId = component.getUniqueId();
		ComponentTypeEnum componentType = component.getComponentType();

		for (ArtifactDefinition artDef : generatedArtifactsDefinitions) {
			String data = gson.toJson(artDef);
			String dataMD5 = GeneralUtility.calculateMD5ByString(data);
			String artifactUniqueId = null;

			if (operationType.equals(ArtifactOperation.Update) || operationType.equals(ArtifactOperation.Delete)) {
				String artifactLabel = artDef.getArtifactLabel();
				ArtifactDefinition artifactDefinition = component.getDeploymentArtifacts().get(artifactLabel);
				if (artifactDefinition != null) {
					artifactUniqueId = artifactDefinition.getUniqueId();
				}
			}

			Either<Either<ArtifactDefinition, Operation>, ResponseFormat> validateAndHandleArtifact = artifactsBusinessLogic.validateAndHandleArtifact(componentUniqueId, componentType, operationType, artifactUniqueId, artDef, dataMD5, data, null,
					null, null, user, component, shouldLock, inTransaction);

			if (validateAndHandleArtifact.isRight()) {
				if (ArtifactOperation.Create == operationType || ArtifactOperation.Update == operationType) {
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.AAI_ARTIFACT_GENERATION_FAILED, componentType.getValue(), component.getName(), validateAndHandleArtifact.right().value().toString());

					Either.right(responseFormat);
				} else {
					log.warn("Generated artifact {} could not be deleted", artDef.getArtifactLabel());
				}
			}
		}

		return Either.left(ActionStatus.OK);
	}

	private List<ArtifactDefinition> convertToArtifactDefinitionFromArtifactGeneratedData(List<Artifact> generatorOutput) {
		List<ArtifactDefinition> artifactDefList = new LinkedList<>();

		for (Artifact artifact : generatorOutput) {
			ArtifactDefinition newEntry = new ArtifactDefinition();
			newEntry.setArtifactName(artifact.getName());
			newEntry.setArtifactType(artifact.getType());
			newEntry.setArtifactGroupType(ArtifactGroupTypeEnum.findType(artifact.getGroupType()));
			newEntry.setDescription(artifact.getDescription());

			// Normalizing the artifact label to match those stored in DB
			String normalizeArtifactLabel = ValidationUtils.normalizeArtifactLabel(artifact.getLabel());
			newEntry.setArtifactLabel(normalizeArtifactLabel);
			newEntry.setPayload(artifact.getPayload());
			newEntry.setArtifactChecksum(artifact.getChecksum());
			// Flag that set to true in case that the artifact is generated by AI&I generator
			newEntry.setGenerated(Boolean.TRUE);

			artifactDefList.add(newEntry);
		}

		return artifactDefList;
	}

	// List<ImmutablePair<Component, byte[] artifactBytes>>
	// artifact stored by label
	private List<Artifact> convertToGeneratorArtifactsInput(List<ImmutablePair<Component, byte[]>> inputs) {
		List<Artifact> listOfArtifactsInput = new LinkedList<>();
		for (ImmutablePair<Component, byte[]> triple : inputs) {
			Component component = triple.getLeft();

			Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
			ArtifactDefinition artifactDefinition = toscaArtifacts.get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE);

			String artifactName = artifactDefinition.getArtifactName();
			String artifactType = artifactDefinition.getArtifactType();
			String artifactGroupType = artifactDefinition.getArtifactGroupType().getType();
			String artifactDescription = artifactDefinition.getDescription();
			String artifactLabel = artifactDefinition.getArtifactLabel();
			byte[] right = triple.getRight();
			// The md5 calculated on the uncoded data
			String md5Hex = DigestUtils.md5Hex(right);
			// byte[] payload = Base64.getEncoder().encode(right);
			byte[] payload = Base64.encodeBase64(right);
			String artifactVersion = artifactDefinition.getArtifactVersion();

			Artifact convertedArtifact = new Artifact(artifactType, artifactGroupType, md5Hex, payload);
			convertedArtifact.setName(artifactName);
			convertedArtifact.setDescription(artifactDescription);
			convertedArtifact.setLabel(artifactLabel);
			convertedArtifact.setVersion(artifactVersion);

			listOfArtifactsInput.add(convertedArtifact);
		}

		return listOfArtifactsInput;
	}

	private Either<byte[], ActionStatus> getEntryData(String esId, Component childComponent) {
		byte[] content;
		if (esId == null || esId.isEmpty()) {
			Either<ToscaRepresentation, ToscaError> exportRes = toscaExportUtils.exportComponent(childComponent);
			if (exportRes.isRight()) {
				log.debug("Failed to export tosca template for child component {} error {}", childComponent.getUniqueId(), exportRes.right().value());
				return Either.right(componentsUtils.convertFromToscaError(exportRes.right().value()));
			}
			content = exportRes.left().value().getMainYaml().getBytes();
		} else {
			Either<byte[], ActionStatus> fromCassandra = getFromCassandra(esId);
			if (fromCassandra.isRight()) {
				return Either.right(fromCassandra.right().value());
			} else {
				content = fromCassandra.left().value();
			}
		}
		return Either.left(content);
	}

	private Either<byte[], ActionStatus> getFromCassandra(String esId) {
		Either<ESArtifactData, CassandraOperationStatus> artifactResponse = artifactCassandraDao.getArtifact(esId);

		if (artifactResponse.isRight()) {
			log.debug("In createCsar fetching of artifact from CS failed");
			log.debug("Failed to fetch from Cassandra by id {} error {} ", esId, artifactResponse.right().value());

			StorageOperationStatus storageStatus = DaoStatusConverter.convertCassandraStatusToStorageStatus(artifactResponse.right().value());
			ActionStatus convertedFromStorageResponse = componentsUtils.convertFromStorageResponse(storageStatus);
			return Either.right(convertedFromStorageResponse);
		} else {
			ESArtifactData artifactData = artifactResponse.left().value();
			return Either.left(artifactData.getDataAsArray());

		}
	}

	private String createToscaBlock0(String metaFileVersion, String csarVersion, String createdBy, String entryDef) {
		final String BLOCK_0_TEMPLATE = "TOSCA-Meta-File-Version: %s\nCSAR-Version: %s\nCreated-By: %s\nEntry-Definitions: Definitions/%s\n";
		String readyBlock = String.format(BLOCK_0_TEMPLATE, metaFileVersion, csarVersion, createdBy, entryDef);
		return readyBlock;
	}

	private Either<List<Artifact>, ResponseFormat> artifactGenerator(List<Artifact> artifactList, ArtifactType type, Component component) {

		ArtifactGenerationServiceImpl artifactGenerationServiceImpl = new ArtifactGenerationServiceImpl();
		ArtifactTypes artifactTypes = new ArtifactTypes();
		List<ArtifactType> artifactTypesList = new LinkedList<>();
		ArtifactType otherType;

		if (type == null) {
			otherType = ArtifactType.OTHER;
		} else {
			otherType = type;
		}

		artifactTypesList.add(otherType);
		artifactTypes.setArtifactTypes(artifactTypesList);

		String configJson = gson.toJson(artifactTypes);
		GenerationData generatedArtifacts = artifactGenerationServiceImpl.generateArtifact(artifactList, configJson);

		Map<String, List<String>> errorData = generatedArtifacts.getErrorData();

		if (!errorData.isEmpty()) {
			Set<String> keySet = errorData.keySet();

			for (String key : keySet) {
				List<String> errorList = errorData.get(key);
				log.debug("The Artifact Generator Failed - {} with following: {}", key, errorList);
			}

			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.AAI_ARTIFACT_GENERATION_FAILED, component.getComponentType().getValue(), component.getName(), errorData.toString());
			return Either.right(responseFormat);
		}

		return Either.left(generatedArtifacts.getResultData());
	}

}
