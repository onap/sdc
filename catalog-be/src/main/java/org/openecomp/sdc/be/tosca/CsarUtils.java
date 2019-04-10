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


import fj.data.Either;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.config.Configuration.ArtifactTypeConfig;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.SdcSchemaFilesCassandraDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsontitan.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.resources.data.SdcSchemaFilesData;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.openecomp.sdc.be.tosca.utils.OperationArtifactUtil;
import org.openecomp.sdc.be.utils.CommonBeUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
/**
 * @author tg851x
 *
 */
@org.springframework.stereotype.Component("csar-utils")
public class CsarUtils {
    private static final Logger log = Logger.getLogger(CsarUtils.class);

	@Autowired
	private SdcSchemaFilesCassandraDao sdcSchemaFilesCassandraDao;
	@Autowired
	private ArtifactCassandraDao artifactCassandraDao;
	@Autowired
	private ComponentsUtils componentsUtils;
	@Autowired
	private ToscaExportHandler toscaExportUtils;
	@Autowired
	protected ToscaOperationFacade toscaOperationFacade;

    private String CONFORMANCE_LEVEL;
    private String SDC_VERSION;
	public static final Pattern UUID_NORMATIVE_NEW_VERSION = Pattern.compile("^\\d{1,}.0");
	public static final String ARTIFACTS_PATH = "Artifacts/";
	public static final String RESOURCES_PATH = "Resources/";
	public static final String INFORMATIONAL_ARTIFACTS = "Informational/";
	public static final String DEPLOYMENT_ARTIFACTS = "Deployment/";

	public static final String DEFINITIONS_PATH = "Definitions/";
	private static final String CSAR_META_VERSION = "1.0";
	private static final String CSAR_META_PATH_FILE_NAME = "csar.meta";
	private static final String TOSCA_META_PATH_FILE_NAME = "TOSCA-Metadata/TOSCA.meta";
	private static final String TOSCA_META_VERSION = "1.0";
	private static final String CSAR_VERSION = "1.1";
	public static final String ARTIFACTS = "Artifacts";
    private static final String DEFINITION = "Definitions";
    private static final String DEL_PATTERN = "([/\\\\]+)";
	private static String versionFirstThreeOctates;

	public static final String VFC_NODE_TYPE_ARTIFACTS_PATH_PATTERN = ARTIFACTS + DEL_PATTERN
			+ ImportUtils.Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + "([\\d\\w\\_\\-\\.\\s]+)" + DEL_PATTERN
			+ "([\\d\\w\\_\\-\\.\\s]+)" + DEL_PATTERN + "([\\d\\w\\_\\-\\.\\s]+)" + DEL_PATTERN
			+ "([\\d\\w\\_\\-\\.\\s]+)";

	public static final String VF_NODE_TYPE_ARTIFACTS_PATH_PATTERN = ARTIFACTS + DEL_PATTERN +
	// Artifact Group (i.e Deployment/Informational)
			"([\\w\\_\\-\\.\\s]+)" + DEL_PATTERN +
			// Artifact Type
			"([\\w\\_\\-\\.\\s]+)" + DEL_PATTERN +
			// Artifact Any File Name
			".+";
	public static final String VALID_ENGLISH_ARTIFACT_NAME = "([\\w\\_\\-\\.\\s]+)";
	public static final String SERVICE_TEMPLATE_PATH_PATTERN = DEFINITION + DEL_PATTERN +
	// Service Template File Name
			"([\\w\\_\\-\\.\\s]+)";

	public static final String ARTIFACT_CREATED_FROM_CSAR = "Artifact created from csar";
    private static final String BLOCK_0_TEMPLATE = "SDC-TOSCA-Meta-File-Version: %s\nSDC-TOSCA-Definitions-Version: %s\n";

	public CsarUtils() {
		this.CONFORMANCE_LEVEL = ConfigurationManager.getConfigurationManager().getConfiguration()
				.getToscaConformanceLevel();
		this.SDC_VERSION = ExternalConfiguration.getAppVersion();
		if (SDC_VERSION != null && !SDC_VERSION.isEmpty()) {
			Matcher matcher = Pattern.compile("(?!\\.)(\\d+(\\.\\d+)+)(?![\\d\\.])").matcher(SDC_VERSION);
			matcher.find();
			versionFirstThreeOctates = matcher.group(0);
		} else {
			versionFirstThreeOctates = "";
		}
	}

	/**
	 *
	 * @param component
	 * @param getFromCS
	 * @param isInCertificationRequest
	 * @return
	 */
	public Either<byte[], ResponseFormat> createCsar(Component component, boolean getFromCS,
			boolean isInCertificationRequest) {
		return createCsar(component, getFromCS, isInCertificationRequest, false);
	}

	private Either<byte[], ResponseFormat> createCsar(Component component, boolean getFromCS,
			boolean isInCertificationRequest, boolean mockGenerator) {
		final String createdBy = component.getCreatorFullName();

		String fileName;
		Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
		ArtifactDefinition artifactDefinition = toscaArtifacts.get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE);
		fileName = artifactDefinition.getArtifactName();

		String toscaConformanceLevel = ConfigurationManager.getConfigurationManager().getConfiguration()
				.getToscaConformanceLevel();
		String csarBlock0 = createCsarBlock0(CSAR_META_VERSION, toscaConformanceLevel);
		byte[] csarBlock0Byte = csarBlock0.getBytes();

		final String toscaBlock0 = createToscaBlock0(TOSCA_META_VERSION, CSAR_VERSION, createdBy, fileName);
		byte[] toscaBlock0Byte = toscaBlock0.getBytes();

        Either<byte[], ResponseFormat> generateCsarZipResponse = generateCsarZip(csarBlock0Byte, toscaBlock0Byte, component, getFromCS, isInCertificationRequest);

		if (generateCsarZipResponse.isRight()) {
			return Either.right(generateCsarZipResponse.right().value());
		}

		return Either.left(generateCsarZipResponse.left().value());
	}

    private Either<byte[], ResponseFormat> generateCsarZip(byte[] csarBlock0Byte, byte[] toscaBlock0Byte, Component component, boolean getFromCS, boolean isInCertificationRequest) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(out)) {
			zip.putNextEntry(new ZipEntry(CSAR_META_PATH_FILE_NAME));
			zip.write(csarBlock0Byte);
			zip.putNextEntry(new ZipEntry(TOSCA_META_PATH_FILE_NAME));
			zip.write(toscaBlock0Byte);
            Either<ZipOutputStream, ResponseFormat> populateZip = populateZip(component, getFromCS, zip, isInCertificationRequest);
            if (populateZip.isRight()) {
                log.debug("Failed to populate CSAR zip file {}", populateZip.right().value());
                return Either.right(populateZip.right().value());
            }

			zip.finish();
			byte[] byteArray = out.toByteArray();

			return Either.left(byteArray);
		} catch (IOException | NullPointerException e) {
			log.debug("Failed with IOexception to create CSAR zip for component {}", component.getUniqueId(), e);

			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
			return Either.right(responseFormat);
		}
	}

    private Either<ZipOutputStream, ResponseFormat> populateZip(Component component, boolean getFromCS, ZipOutputStream zip, boolean isInCertificationRequest) throws IOException {

		LifecycleStateEnum lifecycleState = component.getLifecycleState();
		String componentYaml;
		Either<ToscaRepresentation, ToscaError> exportComponent;
		byte[] mainYaml;
		// <file name, cassandraId, component>
		List<Triple<String, String, Component>> dependencies = null;

		Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
		ArtifactDefinition artifactDefinition = toscaArtifacts.get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE);
		String fileName = artifactDefinition.getArtifactName();

		if (getFromCS || !(lifecycleState == LifecycleStateEnum.NOT_CERTIFIED_CHECKIN
				|| lifecycleState == LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT)) {
			String cassandraId = artifactDefinition.getEsId();
			Either<byte[], ActionStatus> fromCassandra = getFromCassandra(cassandraId);
			if (fromCassandra.isRight()) {
				log.debug("ArtifactName {}, unique ID {}", artifactDefinition.getArtifactName(),
						artifactDefinition.getUniqueId());
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(fromCassandra.right().value());
				return Either.right(responseFormat);
			}
			mainYaml = fromCassandra.left().value();

		} else {
			exportComponent = toscaExportUtils.exportComponent(component);
			if (exportComponent.isRight()) {
				log.debug("exportComponent failed", exportComponent.right().value());
				ActionStatus convertedFromToscaError = componentsUtils
						.convertFromToscaError(exportComponent.right().value());
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(convertedFromToscaError);
				return Either.right(responseFormat);
			}
			ToscaRepresentation exportResult = exportComponent.left().value();
			componentYaml = exportResult.getMainYaml();
			mainYaml = componentYaml.getBytes();
			dependencies = exportResult.getDependencies();
		}

		zip.putNextEntry(new ZipEntry(DEFINITIONS_PATH + fileName));
		zip.write(mainYaml);
		// US798487 - Abstraction of complex types
		if (!ModelConverter.isAtomicComponent(component)) {
			log.debug("Component {} is complex - generating abstract type for it..", component.getName());
			writeComponentInterface(component, zip, fileName, false);
		}

		if (dependencies == null) {
			Either<ToscaTemplate, ToscaError> dependenciesRes = toscaExportUtils.getDependencies(component);
			if (dependenciesRes.isRight()) {
				log.debug("Failed to retrieve dependencies for component {}, error {}", component.getUniqueId(),
						dependenciesRes.right().value());
				ActionStatus convertFromToscaError = componentsUtils
						.convertFromToscaError(dependenciesRes.right().value());
				ResponseFormat responseFormat = componentsUtils.getResponseFormat(convertFromToscaError);
				return Either.right(responseFormat);
			}
			dependencies = dependenciesRes.left().value().getDependencies();
		}

		// UID <cassandraId,filename,component>
		Map<String, ImmutableTriple<String, String, Component>> innerComponentsCache = new HashMap<>();

		if (dependencies != null && !dependencies.isEmpty()) {
			for (Triple<String, String, Component> d : dependencies) {
				String cassandraId = d.getMiddle();
				Component childComponent = d.getRight();
				Either<byte[], ActionStatus> entryData = getEntryData(cassandraId, childComponent);

				if (entryData.isRight()) {
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(entryData.right().value());
					return Either.right(responseFormat);
				}

				// fill innerComponentsCache
				fileName = d.getLeft();
				addComponentToCache(innerComponentsCache, cassandraId, fileName, childComponent);
				addInnerComponentsToCache(innerComponentsCache, childComponent);
			}

			// add inner components to CSAR
			for (Entry<String, ImmutableTriple<String, String, Component>> innerComponentTripleEntry : innerComponentsCache
					.entrySet()) {

				ImmutableTriple<String, String, Component> innerComponentTriple = innerComponentTripleEntry.getValue();

				Component innerComponent = innerComponentTriple.getRight();
				String icFileName = innerComponentTriple.getMiddle();

				// add component to zip
				Either<byte[], ActionStatus> entryData = getEntryData(innerComponentTriple.getLeft(), innerComponent);
				if (entryData.isRight()) {
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(entryData.right().value());
					log.debug("Failed adding to zip component {}, error {}", innerComponentTriple.getLeft(),
							entryData.right().value());
					return Either.right(responseFormat);
				}
				byte[] content = entryData.left().value();
				zip.putNextEntry(new ZipEntry(DEFINITIONS_PATH + icFileName));
				zip.write(content);

				// add component interface to zip
				if (!ModelConverter.isAtomicComponent(innerComponent)) {
					writeComponentInterface(innerComponent, zip, icFileName, true);
				}
			}
		}

		// retrieve SDC.zip from Cassandra
		Either<byte[], ResponseFormat> latestSchemaFilesFromCassandra = getLatestSchemaFilesFromCassandra();

		if (latestSchemaFilesFromCassandra.isRight()) {
			log.error("Error retrieving SDC Schema files from cassandra");
			return Either.right(latestSchemaFilesFromCassandra.right().value());
		}

		// add files from retireved SDC.zip to Definitions folder in CSAR
		Either<ZipOutputStream, ResponseFormat> addSchemaFilesFromCassandra = addSchemaFilesFromCassandra(zip,
				latestSchemaFilesFromCassandra.left().value());

		if (addSchemaFilesFromCassandra.isRight()) {
			return addSchemaFilesFromCassandra;
		}

		Either<CsarDefinition, ResponseFormat> collectedComponentCsarDefinition = collectComponentCsarDefinition(
				component);

		if (collectedComponentCsarDefinition.isRight()) {
			return Either.right(collectedComponentCsarDefinition.right().value());
		}

		return writeAllFilesToCsar(component, collectedComponentCsarDefinition.left().value(), zip,
				isInCertificationRequest);
	}

	private Either<ZipOutputStream, ResponseFormat> addSchemaFilesFromCassandra(ZipOutputStream zip,
			byte[] schemaFileZip) {

		final int initSize = 2048;

		log.debug("Starting copy from Schema file zip to CSAR zip");

		try (ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(schemaFileZip));
				ByteArrayOutputStream out = new ByteArrayOutputStream();
             BufferedOutputStream bos = new BufferedOutputStream(out, initSize)) {

            ZipEntry entry;
			while ((entry = zipStream.getNextEntry()) != null) {

				String entryName = entry.getName();
				int readSize = initSize;
				byte[] entryData = new byte[initSize];

				while ((readSize = zipStream.read(entryData, 0, readSize)) != -1) {
					bos.write(entryData, 0, readSize);
				}

				bos.flush();
				out.flush();
				zip.putNextEntry(new ZipEntry(DEFINITIONS_PATH + entryName));
				zip.write(out.toByteArray());
				zip.flush();
				out.reset();
			}
		} catch (IOException | NullPointerException e) {
			log.error("Error while writing the SDC schema file to the CSAR {}", e);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
		}

		log.debug("Finished coppy from Schema file zip to CSAR zip");

		return Either.left(zip);
	}

	private void addInnerComponentsToCache(Map<String, ImmutableTriple<String, String, Component>> componentCache,
			Component childComponent) {

		List<ComponentInstance> instances = childComponent.getComponentInstances();

		if (instances != null) {
			instances.forEach(ci -> {
				ImmutableTriple<String, String, Component> componentRecord = componentCache.get(ci.getComponentUid());
				if (componentRecord == null) {
					// all resource must be only once!
					Either<Resource, StorageOperationStatus> resource = toscaOperationFacade
							.getToscaElement(ci.getComponentUid());
					if (resource == null || resource.isRight()) {
                                             log.debug("Failed to fetch resource with id {} for instance {}", ci.getComponentUid(), ci.getName());
					} else {
						Component componentRI = resource.left().value();

						Map<String, ArtifactDefinition> childToscaArtifacts = componentRI.getToscaArtifacts();
						ArtifactDefinition childArtifactDefinition = childToscaArtifacts
								.get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE);
						if (childArtifactDefinition != null) {
							// add to cache
							addComponentToCache(componentCache, childArtifactDefinition.getEsId(),
									childArtifactDefinition.getArtifactName(), componentRI);
						}

						// if not atomic - insert inner components as well
						if (!ModelConverter.isAtomicComponent(componentRI)) {
							addInnerComponentsToCache(componentCache, componentRI);
						}
					}
				}
			});
		}
	}

	private void addComponentToCache(Map<String, ImmutableTriple<String, String, Component>> componentCache, String id,
			String fileName, Component component) {

		ImmutableTriple<String, String, Component> cachedComponent = componentCache.get(component.getInvariantUUID());
		if (cachedComponent == null || CommonBeUtils.compareAsdcComponentVersions(component.getVersion(),
				cachedComponent.getRight().getVersion())) {
			componentCache.put(component.getInvariantUUID(),
                    new ImmutableTriple<>(id, fileName, component));

			if (cachedComponent != null) {
				// overwriting component with newer version
				log.warn("Overwriting component invariantID {} of version {} with a newer version {}", id,
						cachedComponent.getRight().getVersion(), component.getVersion());
			}
		}
	}

	private Either<ZipOutputStream, ResponseFormat> writeComponentInterface(Component component, ZipOutputStream zip,
			String fileName, boolean isAssociatedComponent) {
		try {
			Either<ToscaRepresentation, ToscaError> componentInterface = toscaExportUtils
					.exportComponentInterface(component, isAssociatedComponent);
			ToscaRepresentation componentInterfaceYaml = componentInterface.left().value();
			String mainYaml = componentInterfaceYaml.getMainYaml();
			String interfaceFileName = DEFINITIONS_PATH + ToscaExportHandler.getInterfaceFilename(fileName);

			zip.putNextEntry(new ZipEntry(interfaceFileName));
			zip.write(mainYaml.getBytes());

		} catch (Exception e) {
			log.error("#writeComponentInterface - zip writing failed with error: ", e);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
		}

		return Either.left(zip);
	}

	private Either<byte[], ActionStatus> getEntryData(String cassandraId, Component childComponent) {
		byte[] content;
		if (cassandraId == null || cassandraId.isEmpty()) {
			Either<ToscaRepresentation, ToscaError> exportRes = toscaExportUtils.exportComponent(childComponent);
			if (exportRes.isRight()) {
				log.debug("Failed to export tosca template for child component {} error {}",
						childComponent.getUniqueId(), exportRes.right().value());
				return Either.right(componentsUtils.convertFromToscaError(exportRes.right().value()));
			}
			content = exportRes.left().value().getMainYaml().getBytes();
		} else {
			Either<byte[], ActionStatus> fromCassandra = getFromCassandra(cassandraId);
			if (fromCassandra.isRight()) {
				return Either.right(fromCassandra.right().value());
			} else {
				content = fromCassandra.left().value();
			}
		}
		return Either.left(content);
	}

	private Either<byte[], ResponseFormat> getLatestSchemaFilesFromCassandra() {
		Either<List<SdcSchemaFilesData>, CassandraOperationStatus> specificSchemaFiles = sdcSchemaFilesCassandraDao
				.getSpecificSchemaFiles(versionFirstThreeOctates, CONFORMANCE_LEVEL);

		if (specificSchemaFiles.isRight()) {
			log.debug("Failed to get the schema files SDC-Version: {} Conformance-Level {}", versionFirstThreeOctates,
					CONFORMANCE_LEVEL);
			StorageOperationStatus storageStatus = DaoStatusConverter
					.convertCassandraStatusToStorageStatus(specificSchemaFiles.right().value());
			ActionStatus convertedFromStorageResponse = componentsUtils.convertFromStorageResponse(storageStatus);
			return Either.right(componentsUtils.getResponseFormat(convertedFromStorageResponse));
		}

		List<SdcSchemaFilesData> listOfSchemas = specificSchemaFiles.left().value();

		if (listOfSchemas.isEmpty()) {
			log.debug("Failed to get the schema files SDC-Version: {} Conformance-Level {}", versionFirstThreeOctates,
					CONFORMANCE_LEVEL);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.TOSCA_SCHEMA_FILES_NOT_FOUND,
					versionFirstThreeOctates, CONFORMANCE_LEVEL));
		}

		SdcSchemaFilesData schemaFile = listOfSchemas.iterator().next();

		return Either.left(schemaFile.getPayloadAsArray());
	}

	private Either<byte[], ActionStatus> getFromCassandra(String cassandraId) {
		Either<ESArtifactData, CassandraOperationStatus> artifactResponse = artifactCassandraDao
				.getArtifact(cassandraId);

		if (artifactResponse.isRight()) {
			log.debug("Failed to fetch artifact from Cassandra by id {} error {} ", cassandraId,
					artifactResponse.right().value());

			StorageOperationStatus storageStatus = DaoStatusConverter
					.convertCassandraStatusToStorageStatus(artifactResponse.right().value());
			ActionStatus convertedFromStorageResponse = componentsUtils.convertFromStorageResponse(storageStatus);
			return Either.right(convertedFromStorageResponse);
		}
		ESArtifactData artifactData = artifactResponse.left().value();
		return Either.left(artifactData.getDataAsArray());
	}

	private String createCsarBlock0(String metaFileVersion, String toscaConformanceLevel) {
        return String.format(BLOCK_0_TEMPLATE, metaFileVersion, toscaConformanceLevel);
	}

	private String createToscaBlock0(String metaFileVersion, String csarVersion, String createdBy, String entryDef) {
		final String block0template = "TOSCA-Meta-File-Version: %s\nCSAR-Version: %s\nCreated-By: %s\nEntry-Definitions: Definitions/%s\n\nName: csar.meta\nContent-Type: text/plain\n";
		return String.format(block0template, metaFileVersion, csarVersion, createdBy, entryDef);
	}

	/**
	 * Extracts artifacts of VFCs from CSAR
	 *
	 * @param csar
	 * @return Map of <String, List<ArtifactDefinition>> the contains Lists of
	 *         artifacts according vfcToscaNamespace
	 */
	public static Map<String, List<ArtifactDefinition>> extractVfcsArtifactsFromCsar(Map<String, byte[]> csar) {

		Map<String, List<ArtifactDefinition>> artifacts = new HashMap<>();
		if (csar != null) {
			log.debug("************* Going to extract VFCs artifacts from Csar. ");
			Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();
			csar.entrySet().stream()
					// filter CSAR entry by node type artifact path
					.filter(e -> Pattern.compile(VFC_NODE_TYPE_ARTIFACTS_PATH_PATTERN).matcher(e.getKey()).matches())
					// extract ArtifactDefinition from CSAR entry for each entry with matching
					// artifact path
					.forEach(e -> addExtractedVfcArtifact(extractVfcArtifact(e, collectedWarningMessages), artifacts));
			// add counter suffix to artifact labels
			handleWarningMessages(collectedWarningMessages);

		}
		return artifacts;
	}

	/**
	 * Print warnings to log
	 *
	 * @param collectedWarningMessages
	 */
	public static void handleWarningMessages(Map<String, Set<List<String>>> collectedWarningMessages) {
		collectedWarningMessages.entrySet().stream()
				// for each vfc
				.forEach(e -> e.getValue().stream()
						// add each warning message to log
						.forEach(args -> log.warn(e.getKey(), args.toArray())));

	}

	private static void addExtractedVfcArtifact(ImmutablePair<String, ArtifactDefinition> extractedVfcArtifact,
			Map<String, List<ArtifactDefinition>> artifacts) {
		if (extractedVfcArtifact != null) {
			List<ArtifactDefinition> currArtifactsList;
			String vfcToscaNamespace = extractedVfcArtifact.getKey();
			if (artifacts.containsKey(vfcToscaNamespace)) {
				currArtifactsList = artifacts.get(vfcToscaNamespace);
			} else {
				currArtifactsList = new ArrayList<>();
				artifacts.put(vfcToscaNamespace, currArtifactsList);
			}
			currArtifactsList.add(extractedVfcArtifact.getValue());
		}
	}

	private static ImmutablePair<String, ArtifactDefinition> extractVfcArtifact(Entry<String, byte[]> entry,
			Map<String, Set<List<String>>> collectedWarningMessages) {
		ArtifactDefinition artifact;
		String[] parsedCsarArtifactPath = entry.getKey().split("/");
		Either<ArtifactGroupTypeEnum, Boolean> eitherArtifactGroupType = detectArtifactGroupType(
				parsedCsarArtifactPath[2].toUpperCase(), collectedWarningMessages);
		if (eitherArtifactGroupType.isLeft()) {
			artifact = buildArtifactDefinitionFromCsarArtifactPath(entry, collectedWarningMessages,
					parsedCsarArtifactPath, eitherArtifactGroupType.left().value());
		} else {
			return null;
		}
		return new ImmutablePair<>(parsedCsarArtifactPath[1], artifact);
	}

	private static Either<ArtifactGroupTypeEnum, Boolean> detectArtifactGroupType(String groupType,
			Map<String, Set<List<String>>> collectedWarningMessages) {
		Either<ArtifactGroupTypeEnum, Boolean> result;
		try {
			ArtifactGroupTypeEnum artifactGroupType = ArtifactGroupTypeEnum.findType(groupType.toUpperCase());
			if (artifactGroupType == null || (artifactGroupType != ArtifactGroupTypeEnum.INFORMATIONAL
					&& artifactGroupType != ArtifactGroupTypeEnum.DEPLOYMENT)) {
				String warningMessage = "Warning - unrecognized artifact group type {} was received.";
				List<String> messageArguments = new ArrayList<>();
				messageArguments.add(groupType);
				if (!collectedWarningMessages.containsKey(warningMessage)) {
					Set<List<String>> messageArgumentLists = new HashSet<>();
					messageArgumentLists.add(messageArguments);
					collectedWarningMessages.put(warningMessage, messageArgumentLists);
				} else {
					collectedWarningMessages.get(warningMessage).add(messageArguments);
				}

				result = Either.right(false);
			} else {

				result = Either.left(artifactGroupType);
			}
		} catch (Exception e) {
			log.debug("detectArtifactGroupType failed with exception", e);
			result = Either.right(false);
		}
		return result;
	}

	private static ArtifactDefinition buildArtifactDefinitionFromCsarArtifactPath(Entry<String, byte[]> entry,
			Map<String, Set<List<String>>> collectedWarningMessages, String[] parsedCsarArtifactPath,
			ArtifactGroupTypeEnum artifactGroupType) {
		ArtifactDefinition artifact;
		artifact = new ArtifactDefinition();
		artifact.setArtifactGroupType(artifactGroupType);
		artifact.setArtifactType(detectArtifactTypeVFC(artifactGroupType, parsedCsarArtifactPath[3],
				parsedCsarArtifactPath[1], collectedWarningMessages));
		artifact.setArtifactName(
				ValidationUtils.normalizeFileName(parsedCsarArtifactPath[parsedCsarArtifactPath.length - 1]));
		artifact.setPayloadData(Base64.encodeBase64String(entry.getValue()));
		artifact.setArtifactDisplayName(artifact.getArtifactName().lastIndexOf('.') > 0
				? artifact.getArtifactName().substring(0, artifact.getArtifactName().lastIndexOf('.'))
				: artifact.getArtifactName());
		artifact.setArtifactLabel(ValidationUtils.normalizeArtifactLabel(artifact.getArtifactName()));
		artifact.setDescription(ARTIFACT_CREATED_FROM_CSAR);
		artifact.setIsFromCsar(true);
		artifact.setArtifactChecksum(GeneralUtility.calculateMD5Base64EncodedByByteArray(entry.getValue()));
		return artifact;
	}

	public static final class NonMetaArtifactInfo {
		private final String path;
		private final String artifactName;
		private final String displayName;
		private final String artifactLabel;
		private final ArtifactTypeEnum artifactType;
		private final ArtifactGroupTypeEnum artifactGroupType;
		private String payloadData;
		private String artifactChecksum;
		private String artifactUniqueId;
		private final boolean isFromCsar;

		public NonMetaArtifactInfo(String artifactName, String path, ArtifactTypeEnum artifactType,
				ArtifactGroupTypeEnum artifactGroupType, byte[] payloadData, String artifactUniqueId,
				boolean isFromCsar) {
			super();
			this.path = path;
			this.isFromCsar = isFromCsar;
			this.artifactName = ValidationUtils.normalizeFileName(artifactName);
			this.artifactType = artifactType;
			this.artifactGroupType = artifactGroupType;
			final int pointIndex = artifactName.lastIndexOf('.');
			if (pointIndex > 0) {
				displayName = artifactName.substring(0, pointIndex);
			} else {
				displayName = artifactName;
			}
			this.artifactLabel = ValidationUtils.normalizeArtifactLabel(artifactName);
			if (payloadData != null) {
				this.payloadData = Base64.encodeBase64String(payloadData);
				this.artifactChecksum = GeneralUtility.calculateMD5Base64EncodedByByteArray(payloadData);
			}
			this.artifactUniqueId = artifactUniqueId;
		}

		public String getPath() {
			return path;
		}

		public String getArtifactName() {
			return artifactName;
		}

		public ArtifactTypeEnum getArtifactType() {
			return artifactType;
		}

		public String getDisplayName() {
			return displayName;
		}

		public ArtifactGroupTypeEnum getArtifactGroupType() {
			return artifactGroupType;
		}

		public String getArtifactLabel() {
			return artifactLabel;
		}

		public boolean isFromCsar() {
			return isFromCsar;
		}

		public String getPayloadData() {
			return payloadData;
		}

		public String getArtifactChecksum() {
			return artifactChecksum;
		}

		public String getArtifactUniqueId() {
			return artifactUniqueId;
		}

		public void setArtifactUniqueId(String artifactUniqueId) {
			this.artifactUniqueId = artifactUniqueId;
		}

	}

	/**
	 * This method checks the artifact GroupType & Artifact Type. <br>
	 * if there is any problem warning messages are added to
	 * collectedWarningMessages
	 *
	 * @param artifactPath
	 * @param collectedWarningMessages
	 * @return
	 */
	public static Either<NonMetaArtifactInfo, Boolean> validateNonMetaArtifact(String artifactPath, byte[] payloadData,
			Map<String, Set<List<String>>> collectedWarningMessages) {
		Either<NonMetaArtifactInfo, Boolean> ret;
		try {
			String[] parsedArtifactPath = artifactPath.split("/");
			// Validate Artifact Group Type
			Either<ArtifactGroupTypeEnum, Boolean> eitherGroupType = detectArtifactGroupType(parsedArtifactPath[1],
					collectedWarningMessages);
			if (eitherGroupType.isLeft()) {
				final ArtifactGroupTypeEnum groupTypeEnum = eitherGroupType.left().value();

				// Validate Artifact Type
				String artifactType = parsedArtifactPath[2];
				artifactType = detectArtifactTypeVF(groupTypeEnum, artifactType, collectedWarningMessages);

				String artifactFileNameType = parsedArtifactPath[3];
				ret = Either.left(new NonMetaArtifactInfo(artifactFileNameType, artifactPath,
						ArtifactTypeEnum.findType(artifactType), groupTypeEnum, payloadData, null, true));

			} else {
				ret = Either.right(eitherGroupType.right().value());
			}
		} catch (Exception e) {
			log.debug("detectArtifactGroupType failed with exception", e);
			ret = Either.right(false);
		}
		return ret;

	}

	private static String detectArtifactTypeVFC(ArtifactGroupTypeEnum artifactGroupType, String receivedTypeName,
			String parentVfName, Map<String, Set<List<String>>> collectedWarningMessages) {
		String warningMessage = "Warning - artifact type {} that was provided for VFC {} is not recognized.";
		return detectArtifactType(artifactGroupType, receivedTypeName, warningMessage, collectedWarningMessages,
				parentVfName);
	}

	private static String detectArtifactTypeVF(ArtifactGroupTypeEnum artifactGroupType, String receivedTypeName,
			Map<String, Set<List<String>>> collectedWarningMessages) {
		String warningMessage = "Warning - artifact type {} that was provided for VF is not recognized.";
		return detectArtifactType(artifactGroupType, receivedTypeName, warningMessage, collectedWarningMessages);
	}

	private static String detectArtifactType(ArtifactGroupTypeEnum artifactGroupType, String receivedTypeName,
			String warningMessage, Map<String, Set<List<String>>> collectedWarningMessages, String... arguments) {

		ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(receivedTypeName);
		Map<String, ArtifactTypeConfig> resourceValidTypeArtifacts = null;

		if (artifactGroupType != null) {
			switch (artifactGroupType) {
			case INFORMATIONAL:
				resourceValidTypeArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration()
						.getResourceInformationalArtifacts();
				break;
			case DEPLOYMENT:
				resourceValidTypeArtifacts = ConfigurationManager.getConfigurationManager().getConfiguration()
						.getResourceDeploymentArtifacts();
				break;
			default:
				break;
			}
		}

		Set<String> validArtifactTypes = null;
		if (resourceValidTypeArtifacts != null) {
			validArtifactTypes = resourceValidTypeArtifacts.keySet();
		}

		if (validArtifactTypes == null || artifactType == null
				|| !validArtifactTypes.contains(artifactType.getType())) {
			List<String> messageArguments = new ArrayList<>();
			messageArguments.add(receivedTypeName);
			messageArguments.addAll(Arrays.asList(arguments));
			if (!collectedWarningMessages.containsKey(warningMessage)) {
				Set<List<String>> messageArgumentLists = new HashSet<>();
				messageArgumentLists.add(messageArguments);
				collectedWarningMessages.put(warningMessage, messageArgumentLists);
			} else {
				collectedWarningMessages.get(warningMessage).add(messageArguments);
			}
		}

		return artifactType == null ? ArtifactTypeEnum.OTHER.getType() : artifactType.getType();
	}

	private Either<ZipOutputStream, ResponseFormat> writeAllFilesToCsar(Component mainComponent,
			CsarDefinition csarDefinition, ZipOutputStream zipstream, boolean isInCertificationRequest)
			throws IOException {
		ComponentArtifacts componentArtifacts = csarDefinition.getComponentArtifacts();

		Either<ZipOutputStream, ResponseFormat> writeComponentArtifactsToSpecifiedPath = writeComponentArtifactsToSpecifiedPath(
				mainComponent, componentArtifacts, zipstream, ARTIFACTS_PATH, isInCertificationRequest);

		if (writeComponentArtifactsToSpecifiedPath.isRight()) {
			return Either.right(writeComponentArtifactsToSpecifiedPath.right().value());
		}

		ComponentTypeArtifacts mainTypeAndCIArtifacts = componentArtifacts.getMainTypeAndCIArtifacts();
		writeComponentArtifactsToSpecifiedPath = writeArtifactsInfoToSpecifiedPath(mainComponent,
				mainTypeAndCIArtifacts.getComponentArtifacts(), zipstream, ARTIFACTS_PATH, isInCertificationRequest);

		if (writeComponentArtifactsToSpecifiedPath.isRight()) {
			return Either.right(writeComponentArtifactsToSpecifiedPath.right().value());
		}

		Map<String, ArtifactsInfo> componentInstancesArtifacts = mainTypeAndCIArtifacts
				.getComponentInstancesArtifacts();
		Set<String> keySet = componentInstancesArtifacts.keySet();

		String currentPath = ARTIFACTS_PATH + RESOURCES_PATH;
		for (String keyAssetName : keySet) {
			ArtifactsInfo artifactsInfo = componentInstancesArtifacts.get(keyAssetName);
			String pathWithAssetName = currentPath + keyAssetName + "/";
			writeComponentArtifactsToSpecifiedPath = writeArtifactsInfoToSpecifiedPath(mainComponent, artifactsInfo,
					zipstream, pathWithAssetName, isInCertificationRequest);

			if (writeComponentArtifactsToSpecifiedPath.isRight()) {
				return Either.right(writeComponentArtifactsToSpecifiedPath.right().value());
			}
		}
		return Either.left(zipstream);
	}

	private Either<ZipOutputStream, ResponseFormat> writeComponentArtifactsToSpecifiedPath(Component mainComponent,
			ComponentArtifacts componentArtifacts, ZipOutputStream zipstream, String currentPath,
			boolean isInCertificationRequest) throws IOException {
		Map<String, ComponentTypeArtifacts> componentTypeArtifacts = componentArtifacts.getComponentTypeArtifacts();
		// Keys are defined:
		// <Inner Asset TOSCA name (e.g. VFC name)> folder name: <Inner Asset TOSCA name
		// (e.g. VFC name)>_v<version>.
		// E.g. "org.openecomp.resource.vf.vipr_atm_v1.0"
		Set<String> componentTypeArtifactsKeys = componentTypeArtifacts.keySet();
		for (String keyAssetName : componentTypeArtifactsKeys) {
			ComponentTypeArtifacts componentInstanceArtifacts = componentTypeArtifacts.get(keyAssetName);
			ArtifactsInfo componentArtifacts2 = componentInstanceArtifacts.getComponentArtifacts();
			String pathWithAssetName = currentPath + keyAssetName + "/";
			Either<ZipOutputStream, ResponseFormat> writeArtifactsInfoToSpecifiedPath = writeArtifactsInfoToSpecifiedPath(
					mainComponent, componentArtifacts2, zipstream, pathWithAssetName, isInCertificationRequest);

			if (writeArtifactsInfoToSpecifiedPath.isRight()) {
				return writeArtifactsInfoToSpecifiedPath;
			}
		}

		return Either.left(zipstream);
	}

	private Either<ZipOutputStream, ResponseFormat> writeArtifactsInfoToSpecifiedPath(Component mainComponent,
			ArtifactsInfo currArtifactsInfo, ZipOutputStream zip, String path, boolean isInCertificationRequest)
			throws IOException {
		Map<ArtifactGroupTypeEnum, Map<ArtifactTypeEnum, List<ArtifactDefinition>>> artifactsInfo = currArtifactsInfo
				.getArtifactsInfo();
		Set<ArtifactGroupTypeEnum> groupTypeEnumKeySet = artifactsInfo.keySet();

		for (ArtifactGroupTypeEnum artifactGroupTypeEnum : groupTypeEnumKeySet) {
			String groupTypeFolder = path + WordUtils.capitalizeFully(artifactGroupTypeEnum.getType()) + File.separator;

			Map<ArtifactTypeEnum, List<ArtifactDefinition>> artifactTypesMap = artifactsInfo.get(artifactGroupTypeEnum);
			Set<ArtifactTypeEnum> artifactTypeEnumKeySet = artifactTypesMap.keySet();

			for (ArtifactTypeEnum artifactTypeEnum : artifactTypeEnumKeySet) {
				List<ArtifactDefinition> artifactDefinitionList = artifactTypesMap.get(artifactTypeEnum);
				String artifactTypeFolder = groupTypeFolder + artifactTypeEnum.toString() + File.separator;
				if (artifactTypeEnum == ArtifactTypeEnum.WORKFLOW) {
					artifactTypeFolder += OperationArtifactUtil.BPMN_ARTIFACT_PATH + File.separator;
				}

				Either<ZipOutputStream, ResponseFormat> writeArtifactDefinition = writeArtifactDefinition(mainComponent,
						zip, artifactDefinitionList, artifactTypeFolder, isInCertificationRequest);

				if (writeArtifactDefinition.isRight()) {
					return writeArtifactDefinition;
				}
			}
		}

		return Either.left(zip);
	}

	private Either<ZipOutputStream, ResponseFormat> writeArtifactDefinition(Component mainComponent,
			ZipOutputStream zip, List<ArtifactDefinition> artifactDefinitionList, String artifactPathAndFolder,
			boolean isInCertificationRequest) throws IOException {

		ComponentTypeEnum componentType = mainComponent.getComponentType();
		String heatEnvType = ArtifactTypeEnum.HEAT_ENV.getType();

		for (ArtifactDefinition artifactDefinition : artifactDefinitionList) {
			if (!isInCertificationRequest && componentType == ComponentTypeEnum.SERVICE
					&& artifactDefinition.getArtifactType().equals(heatEnvType) ||
			// this is placeholder
					(artifactDefinition.getEsId() == null && artifactDefinition.getMandatory())) {
				continue;
			}

			byte[] payloadData = artifactDefinition.getPayloadData();
			String artifactFileName = artifactDefinition.getArtifactName();

			if (payloadData == null) {
				Either<byte[], ActionStatus> fromCassandra = getFromCassandra(artifactDefinition.getEsId());

				if (fromCassandra.isRight()) {
					log.debug("ArtifactName {}, unique ID {}", artifactDefinition.getArtifactName(),
							artifactDefinition.getUniqueId());
					log.debug("Failed to get {} payload from DB reason: {}", artifactFileName,
							fromCassandra.right().value());
					continue;
				}
				payloadData = fromCassandra.left().value();
			}
			zip.putNextEntry(new ZipEntry(artifactPathAndFolder + artifactFileName));
			zip.write(payloadData);
		}

		return Either.left(zip);
	}

	/************************************
	 * Artifacts Structure
	 ******************************************************************/
	/**
	 * The artifacts Definition saved by their structure
	 */
	private class ArtifactsInfo {
		// Key is the type of artifacts(Informational/Deployment)
		// Value is a map between an artifact type and a list of all artifacts of this
		// type
		private Map<ArtifactGroupTypeEnum, Map<ArtifactTypeEnum, List<ArtifactDefinition>>> artifactsInfoField;

		public ArtifactsInfo() {
			this.artifactsInfoField = new EnumMap<>(ArtifactGroupTypeEnum.class);
		}

		public Map<ArtifactGroupTypeEnum, Map<ArtifactTypeEnum, List<ArtifactDefinition>>> getArtifactsInfo() {
			return artifactsInfoField;
		}

		public List<ArtifactDefinition> getFlatArtifactsListByType(ArtifactTypeEnum artifactType) {
			List<ArtifactDefinition> artifacts = new ArrayList<>();
			for (List<ArtifactDefinition> artifactsByType : artifactsInfoField.get(artifactType).values()) {
				artifacts.addAll(artifactsByType);
			}
			return artifacts;
		}

		public void addArtifactsToGroup(ArtifactGroupTypeEnum artifactGroup,
				Map<ArtifactTypeEnum, List<ArtifactDefinition>> artifactsDefinition) {
			if (artifactsInfoField.get(artifactGroup) == null) {
				artifactsInfoField.put(artifactGroup, artifactsDefinition);
			} else {
				Map<ArtifactTypeEnum, List<ArtifactDefinition>> artifactTypeEnumListMap =
						artifactsInfoField.get(artifactGroup);
				artifactTypeEnumListMap.putAll(artifactsDefinition);
				artifactsInfoField.put(artifactGroup, artifactTypeEnumListMap);
			}

		}

		public boolean isEmpty() {
			return artifactsInfoField.isEmpty();
		}

	}

	/**
	 * The artifacts of the component and of all its composed instances
	 *
	 */
	private class ComponentTypeArtifacts {
		private ArtifactsInfo componentArtifacts; // component artifacts (describes the Informational Deployment
													// folders)
		private Map<String, ArtifactsInfo> componentInstancesArtifacts; // artifacts of the composed instances mapped by
																		// the resourceInstance normalized name
																		// (describes the Resources folder)

		public ComponentTypeArtifacts() {
			componentArtifacts = new ArtifactsInfo();
			componentInstancesArtifacts = new HashMap<>();
		}

		public ArtifactsInfo getComponentArtifacts() {
			return componentArtifacts;
		}

		public void setComponentArtifacts(ArtifactsInfo artifactsInfo) {
			this.componentArtifacts = artifactsInfo;
		}

		public Map<String, ArtifactsInfo> getComponentInstancesArtifacts() {
			return componentInstancesArtifacts;
		}

		public void setComponentInstancesArtifacts(Map<String, ArtifactsInfo> componentInstancesArtifacts) {
			this.componentInstancesArtifacts = componentInstancesArtifacts;
		}

		public void addComponentInstancesArtifacts(String normalizedName, ArtifactsInfo artifactsInfo) {
			componentInstancesArtifacts.put(normalizedName, artifactsInfo);
		}

	}

	private class ComponentArtifacts {
		// artifacts of the component and CI's artifacts contained in it's composition
		// (represents Informational, Deployment & Resource folders of main component)
		private ComponentTypeArtifacts mainTypeAndCIArtifacts;
		// artifacts of all component types mapped by their tosca name
		private Map<String, ComponentTypeArtifacts> componentTypeArtifacts;

		public ComponentArtifacts() {
			mainTypeAndCIArtifacts = new ComponentTypeArtifacts();
			componentTypeArtifacts = new HashMap<>();
		}

		public ComponentTypeArtifacts getMainTypeAndCIArtifacts() {
			return mainTypeAndCIArtifacts;
		}

		public void setMainTypeAndCIArtifacts(ComponentTypeArtifacts componentInstanceArtifacts) {
			this.mainTypeAndCIArtifacts = componentInstanceArtifacts;
		}

		public Map<String, ComponentTypeArtifacts> getComponentTypeArtifacts() {
			return componentTypeArtifacts;
		}

		public void setComponentTypeArtifacts(Map<String, ComponentTypeArtifacts> componentTypeArtifacts) {
			this.componentTypeArtifacts = componentTypeArtifacts;
		}
	}

	private class CsarDefinition {
		private ComponentArtifacts componentArtifacts;

		// add list of tosca artifacts and meta describes CSAR zip root

		public CsarDefinition(ComponentArtifacts componentArtifacts) {
			this.componentArtifacts = componentArtifacts;
		}

		public ComponentArtifacts getComponentArtifacts() {
			return componentArtifacts;
		}
	}

	/************************************
	 * Artifacts Structure END
	 ******************************************************************/

	private Either<CsarDefinition, ResponseFormat> collectComponentCsarDefinition(Component component) {
		ComponentArtifacts componentArtifacts = new ComponentArtifacts();
		Component updatedComponent = component;

		// get service to receive the AII artifacts uploaded to the service
		if (updatedComponent.getComponentType() == ComponentTypeEnum.SERVICE) {
			Either<Service, StorageOperationStatus> getServiceResponse = toscaOperationFacade
					.getToscaElement(updatedComponent.getUniqueId());

			if (getServiceResponse.isRight()) {
				ActionStatus actionStatus = componentsUtils
						.convertFromStorageResponse(getServiceResponse.right().value());
				return Either.right(componentsUtils.getResponseFormat(actionStatus));
			}

			updatedComponent = getServiceResponse.left().value();
		}

		// find the artifacts of the main component, it would have its composed
		// instances artifacts in a separate folder
		ComponentTypeArtifacts componentInstanceArtifacts = new ComponentTypeArtifacts();
		ArtifactsInfo artifactsInfo = collectComponentArtifacts(updatedComponent);
		componentInstanceArtifacts.setComponentArtifacts(artifactsInfo);
		componentArtifacts.setMainTypeAndCIArtifacts(componentInstanceArtifacts);

		Map<String, ComponentTypeArtifacts> resourceTypeArtifacts = componentArtifacts.getComponentTypeArtifacts(); // artifacts
																													// mapped
																													// by
																													// the
																													// component
																													// type(tosca
																													// name+version)
		// get the component instances
		List<ComponentInstance> componentInstances = updatedComponent.getComponentInstances();
		if (componentInstances != null) {
			for (ComponentInstance componentInstance : componentInstances) {
				// call recursive to find artifacts for all the path
				Either<Boolean, ResponseFormat> collectComponentInstanceArtifacts = collectComponentInstanceArtifacts(
						updatedComponent, componentInstance, resourceTypeArtifacts, componentInstanceArtifacts);
				if (collectComponentInstanceArtifacts.isRight()) {
					return Either.right(collectComponentInstanceArtifacts.right().value());
				}
			}
		}

		if (log.isDebugEnabled()) {
			printResult(componentArtifacts, updatedComponent.getName());
		}

		return Either.left(new CsarDefinition(componentArtifacts));
	}

	private void printResult(ComponentArtifacts componentArtifacts, String name) {
		StringBuilder result = new StringBuilder();
		result.append("Artifacts of main component " + name + "\n");
		ComponentTypeArtifacts componentInstanceArtifacts = componentArtifacts.getMainTypeAndCIArtifacts();
		printArtifacts(componentInstanceArtifacts);
		result.append("Type Artifacts\n");
		for (Map.Entry<String, ComponentTypeArtifacts> typeArtifacts : componentArtifacts.getComponentTypeArtifacts()
				.entrySet()) {
			result.append("Folder " + typeArtifacts.getKey() + "\n");
			result.append(printArtifacts(typeArtifacts.getValue()));
		}

		if (log.isDebugEnabled()) {
			log.debug(result.toString());
		}
	}

	private String printArtifacts(ComponentTypeArtifacts componentInstanceArtifacts) {
		StringBuilder result = new StringBuilder();
		ArtifactsInfo artifactsInfo = componentInstanceArtifacts.getComponentArtifacts();
		Map<ArtifactGroupTypeEnum, Map<ArtifactTypeEnum, List<ArtifactDefinition>>> componetArtifacts = artifactsInfo
				.getArtifactsInfo();
		printArtifacts(componetArtifacts);
		result = result.append("Resources\n");
		for (Map.Entry<String, ArtifactsInfo> resourceInstance : componentInstanceArtifacts
				.getComponentInstancesArtifacts().entrySet()) {
			result.append("Folder" + resourceInstance.getKey() + "\n");
			result.append(printArtifacts(resourceInstance.getValue().getArtifactsInfo()));
		}

		return result.toString();
	}

	private String printArtifacts(
			Map<ArtifactGroupTypeEnum, Map<ArtifactTypeEnum, List<ArtifactDefinition>>> componetArtifacts) {
		StringBuilder result = new StringBuilder();
		for (Map.Entry<ArtifactGroupTypeEnum, Map<ArtifactTypeEnum, List<ArtifactDefinition>>> artifactGroup : componetArtifacts
				.entrySet()) {
			result.append("    " + artifactGroup.getKey().getType());
			for (Map.Entry<ArtifactTypeEnum, List<ArtifactDefinition>> groupArtifacts : artifactGroup.getValue()
					.entrySet()) {
				result.append("        " + groupArtifacts.getKey().getType());
				for (ArtifactDefinition artifact : groupArtifacts.getValue()) {
					result.append("            " + artifact.getArtifactDisplayName());
				}
			}
		}

		return result.toString();
	}

	private ComponentTypeArtifacts collectComponentTypeArtifacts(Map<String, ComponentTypeArtifacts> resourcesArtifacts,
			ComponentInstance componentInstance, Component fetchedComponent) {
		String toscaComponentName = componentInstance.getToscaComponentName() + "_v"
				+ componentInstance.getComponentVersion();

		ComponentTypeArtifacts componentArtifactsInfo = resourcesArtifacts.get(toscaComponentName);
		// if there are no artifacts for this component type we need to fetch and build
		// them
		if (componentArtifactsInfo == null) {
			ArtifactsInfo componentArtifacts = collectComponentArtifacts(fetchedComponent);
			componentArtifactsInfo = new ComponentTypeArtifacts();
			if (!componentArtifacts.isEmpty()) {
				componentArtifactsInfo.setComponentArtifacts(componentArtifacts);
				resourcesArtifacts.put(toscaComponentName, componentArtifactsInfo);
			}
		}
		return componentArtifactsInfo;
	}

	private Either<Boolean, ResponseFormat> collectComponentInstanceArtifacts(Component parentComponent,
			ComponentInstance componentInstance, Map<String, ComponentTypeArtifacts> resourcesTypeArtifacts,
			ComponentTypeArtifacts instanceArtifactsLocation) {
		// 1. get the component instance component
        String componentUid;
        if (componentInstance.getOriginType() == OriginTypeEnum.ServiceProxy) {
			componentUid = componentInstance.getSourceModelUid();
		}
		else {
			componentUid = componentInstance.getComponentUid();
		}
        Either<Component, StorageOperationStatus> component = toscaOperationFacade.getToscaElement(componentUid);
		if (component.isRight()) {
			log.error("Failed to fetch resource with id {} for instance {}", componentUid, parentComponent.getUUID());
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ASSET_NOT_FOUND_DURING_CSAR_CREATION,
					parentComponent.getComponentType().getValue(), parentComponent.getUUID(),
					componentInstance.getOriginType().getComponentType().getValue(), componentUid));
		}
		Component fetchedComponent = component.left().value();

		// 2. fill the artifacts for the current component parent type
		ComponentTypeArtifacts componentParentArtifacts = collectComponentTypeArtifacts(resourcesTypeArtifacts,
				componentInstance, fetchedComponent);

		// 3. find the artifacts specific to the instance
		Map<ArtifactTypeEnum, List<ArtifactDefinition>> componentInstanceSpecificInformationalArtifacts = getComponentInstanceSpecificArtifacts(
				componentInstance.getArtifacts(), componentParentArtifacts.getComponentArtifacts().getArtifactsInfo(),
				ArtifactGroupTypeEnum.INFORMATIONAL);
		Map<ArtifactTypeEnum, List<ArtifactDefinition>> componentInstanceSpecificDeploymentArtifacts = getComponentInstanceSpecificArtifacts(
				componentInstance.getDeploymentArtifacts(),
				componentParentArtifacts.getComponentArtifacts().getArtifactsInfo(), ArtifactGroupTypeEnum.DEPLOYMENT);

		// 4. add the instances artifacts to the component type
		ArtifactsInfo artifactsInfo = new ArtifactsInfo();
		if (!componentInstanceSpecificInformationalArtifacts.isEmpty()) {
			artifactsInfo.addArtifactsToGroup(ArtifactGroupTypeEnum.INFORMATIONAL,
					componentInstanceSpecificInformationalArtifacts);
		}
		if (!componentInstanceSpecificDeploymentArtifacts.isEmpty()) {
			artifactsInfo.addArtifactsToGroup(ArtifactGroupTypeEnum.DEPLOYMENT,
					componentInstanceSpecificDeploymentArtifacts);
		}
		if (!artifactsInfo.isEmpty()) {
			instanceArtifactsLocation.addComponentInstancesArtifacts(componentInstance.getNormalizedName(),
					artifactsInfo);
		}

		// 5. do the same for all the component instances
		List<ComponentInstance> componentInstances = fetchedComponent.getComponentInstances();
		if (componentInstances != null) {
			for (ComponentInstance childComponentInstance : componentInstances) {
				Either<Boolean, ResponseFormat> collectComponentInstanceArtifacts = collectComponentInstanceArtifacts(
						fetchedComponent, childComponentInstance, resourcesTypeArtifacts, componentParentArtifacts);
				if (collectComponentInstanceArtifacts.isRight()) {
					return collectComponentInstanceArtifacts;
				}
			}
		}

		return Either.left(true);
	}

	private Map<ArtifactTypeEnum, List<ArtifactDefinition>> getComponentInstanceSpecificArtifacts(
			Map<String, ArtifactDefinition> componentArtifacts,
			Map<ArtifactGroupTypeEnum, Map<ArtifactTypeEnum, List<ArtifactDefinition>>> componentTypeArtifacts,
			ArtifactGroupTypeEnum artifactGroupTypeEnum) {
		Map<ArtifactTypeEnum, List<ArtifactDefinition>> parentArtifacts = componentTypeArtifacts
				.get(artifactGroupTypeEnum); // the artfiacts of the component itself and not the instance

		Map<ArtifactTypeEnum, List<ArtifactDefinition>> artifactsByTypeOfComponentInstance = new EnumMap<>(
				ArtifactTypeEnum.class);
		if (componentArtifacts != null) {
			for (ArtifactDefinition artifact : componentArtifacts.values()) {
				ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifact.getArtifactType());
				List<ArtifactDefinition> parentArtifactsByType = null;
				if (parentArtifacts != null) {
					parentArtifactsByType = parentArtifacts.get(artifactType);
				}
				// the artifact is of instance
				if (parentArtifactsByType == null || !parentArtifactsByType.contains(artifact)) {
					List<ArtifactDefinition> typeArtifacts = artifactsByTypeOfComponentInstance.get(artifactType);
					if (typeArtifacts == null) {
						typeArtifacts = new ArrayList<>();
						artifactsByTypeOfComponentInstance.put(artifactType, typeArtifacts);
					}
					typeArtifacts.add(artifact);
				}
			}
		}

		return artifactsByTypeOfComponentInstance;
	}

	private ArtifactsInfo collectComponentArtifacts(Component component) {
		Map<String, ArtifactDefinition> informationalArtifacts = component.getArtifacts();
		Map<ArtifactTypeEnum, List<ArtifactDefinition>> informationalArtifactsByType = collectGroupArtifacts(
				informationalArtifacts);
		Map<String, ArtifactDefinition> deploymentArtifacts = component.getDeploymentArtifacts();
		Map<ArtifactTypeEnum, List<ArtifactDefinition>> deploymentArtifactsByType = collectGroupArtifacts(
				deploymentArtifacts);
		Map<String, ArtifactDefinition> interfaceOperationArtifacts =
				OperationArtifactUtil.getDistinctInterfaceOperationArtifactsByName(component);
		Map<ArtifactTypeEnum, List<ArtifactDefinition>> interfaceOperationArtifactsByType = collectGroupArtifacts(
				interfaceOperationArtifacts);
		ArtifactsInfo artifactsInfo = new ArtifactsInfo();
		if (!informationalArtifactsByType.isEmpty()) {
			artifactsInfo.addArtifactsToGroup(ArtifactGroupTypeEnum.INFORMATIONAL, informationalArtifactsByType);
		}
		if (!deploymentArtifactsByType.isEmpty()) {
			artifactsInfo.addArtifactsToGroup(ArtifactGroupTypeEnum.DEPLOYMENT, deploymentArtifactsByType);
		}
		//Add component interface operation artifacts
		if(MapUtils.isNotEmpty(interfaceOperationArtifacts)) {
			artifactsInfo.addArtifactsToGroup(ArtifactGroupTypeEnum.DEPLOYMENT, interfaceOperationArtifactsByType);
		}

		return artifactsInfo;
	}

	private Map<ArtifactTypeEnum, List<ArtifactDefinition>> collectGroupArtifacts(
			Map<String, ArtifactDefinition> componentArtifacts) {
		Map<ArtifactTypeEnum, List<ArtifactDefinition>> artifactsByType = new EnumMap<>(ArtifactTypeEnum.class);
		for (ArtifactDefinition artifact : componentArtifacts.values()) {
			if (artifact.getArtifactUUID() != null) {
				ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifact.getArtifactType());
				List<ArtifactDefinition> typeArtifacts = artifactsByType.get(artifactType);
				if (typeArtifacts == null) {
					typeArtifacts = new ArrayList<>();
					artifactsByType.put(artifactType, typeArtifacts);
				}
				typeArtifacts.add(artifact);
			}
		}
		return artifactsByType;
	}
}
