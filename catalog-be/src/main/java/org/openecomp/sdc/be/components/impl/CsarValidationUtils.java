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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

public class CsarValidationUtils {

	private static Logger log = LoggerFactory.getLogger(CsarValidationUtils.class.getName());

	private static final String TOSCA_META_FILE_VERSION = "TOSCA-Meta-File-Version";

	private static final String CSAR_VERSION = "CSAR-Version";

	private static final String CREATED_BY = "Created-By";

	private static final String NEW_LINE_DELM = "\n";

	public final static String TOSCA_METADATA_FILE = "TOSCA-Metadata/TOSCA.meta";

	public static final String TOSCA_META_ENTRY_DEFINITIONS = "Entry-Definitions";

	public final static String[] TOSCA_METADATA_FIELDS = { TOSCA_META_FILE_VERSION, CSAR_VERSION, CREATED_BY, TOSCA_META_ENTRY_DEFINITIONS };

	public final static String ARTIFACTS_METADATA_FILE = "HEAT.meta";

	public final static String ARTIFACTS = "Artifacts/";

	public static final String TOSCA_CSAR_EXTENSION = ".csar";

	public static Either<Boolean, ResponseFormat> validateCsar(Map<String, byte[]> csar, String csarUUID, ComponentsUtils componentsUtils) {
		Either<Boolean, ResponseFormat> validateStatus = validateIsTOSCAMetadataExist(csar, csarUUID, componentsUtils);
		if (validateStatus.isRight()) {
			return Either.right(validateStatus.right().value());
		}
		log.trace("TOSCA-Metadata/TOSCA.meta file found, CSAR id {}", csarUUID);
		return validateTOSCAMetadataFile(csar, csarUUID, componentsUtils);

	}

	public static Either<ImmutablePair<String, String>, ResponseFormat> getToscaYaml(Map<String, byte[]> csar, String csarUUID, ComponentsUtils componentsUtils) {
		Either<Boolean, ResponseFormat> validateStatus = validateIsTOSCAMetadataExist(csar, csarUUID, componentsUtils);
		if (validateStatus.isRight()) {
			return Either.right(validateStatus.right().value());
		}
		byte[] toscaMetaBytes = csar.get(TOSCA_METADATA_FILE);
		Properties props = new Properties();
		try {
			props.load(new ByteArrayInputStream(toscaMetaBytes));
		} catch (IOException e) {
			log.debug("TOSCA-Metadata/TOSCA.meta file is not in expected key-value form in csar, csar ID {}", csarUUID);
			BeEcompErrorManager.getInstance().logInternalDataError("TOSCA-Metadata/TOSCA.meta file not in expected key-value form in CSAR with id " + csarUUID, "CSAR internals are invalid", ErrorSeverity.ERROR);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, csarUUID));
		}

		String yamlFileName = props.getProperty(TOSCA_META_ENTRY_DEFINITIONS);

		if (!csar.containsKey(yamlFileName)) {
			log.debug("Entry-Definitions entry not found in TOSCA-Metadata/TOSCA.meta file, csar ID {}", csarUUID);
			BeEcompErrorManager.getInstance().logInternalDataError("Entry-Definitions entry not found in TOSCA-Metadata/TOSCA.meta file in CSAR with id " + csarUUID, "CSAR internals are invalid", ErrorSeverity.ERROR);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.YAML_NOT_FOUND_IN_CSAR, csarUUID, yamlFileName));
		}

		log.trace("Found Entry-Definitions property in TOSCA-Metadata/TOSCA.meta, Entry-Definitions: {}, CSAR id: {}", yamlFileName, csarUUID);
		byte[] yamlFileBytes = csar.get(yamlFileName);
		if (yamlFileBytes == null) {
			log.debug("Entry-Definitions {} file not found in csar, csar ID {}", yamlFileName, csarUUID);
			BeEcompErrorManager.getInstance().logInternalDataError("Entry-Definitions " + yamlFileName + " file not found in CSAR with id " + csarUUID, "CSAR structure is invalid", ErrorSeverity.ERROR);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.YAML_NOT_FOUND_IN_CSAR, csarUUID, yamlFileName));
		}

		String yamlFileContents = new String(yamlFileBytes);

		return Either.left(new ImmutablePair<String, String>(yamlFileName, yamlFileContents));
	}

	public static Either<ImmutablePair<String, String>, ResponseFormat> getArtifactsMeta(Map<String, byte[]> csar, String csarUUID, ComponentsUtils componentsUtils) {

		if (!csar.containsKey(ARTIFACTS + ARTIFACTS_METADATA_FILE)) {
			log.debug("Entry-Definitions entry not found in TOSCA-Metadata/TOSCA.meta file, csar ID {}", csarUUID);
			BeEcompErrorManager.getInstance().logInternalDataError("Entry-Definitions entry not found in TOSCA-Metadata/TOSCA.meta file in CSAR with id " + csarUUID, "CSAR internals are invalid", ErrorSeverity.ERROR);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.YAML_NOT_FOUND_IN_CSAR, csarUUID, ARTIFACTS_METADATA_FILE));
		}

		log.trace("Found Entry-Definitions property in TOSCA-Metadata/TOSCA.meta, Entry-Definitions: {}, CSAR id: {}", ARTIFACTS_METADATA_FILE, csarUUID);
		byte[] artifactsMetaBytes = csar.get(ARTIFACTS + ARTIFACTS_METADATA_FILE);
		if (artifactsMetaBytes == null) {
			log.debug("Entry-Definitions {} file not found in csar, csar ID {}", ARTIFACTS + ARTIFACTS_METADATA_FILE, csarUUID);
			BeEcompErrorManager.getInstance().logInternalDataError("Entry-Definitions " + ARTIFACTS + ARTIFACTS_METADATA_FILE + " file not found in CSAR with id " + csarUUID, "CSAR structure is invalid", ErrorSeverity.ERROR);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.YAML_NOT_FOUND_IN_CSAR, csarUUID, ARTIFACTS + ARTIFACTS_METADATA_FILE));
		}

		String artifactsFileContents = new String(artifactsMetaBytes);

		return Either.left(new ImmutablePair<String, String>(ARTIFACTS + ARTIFACTS_METADATA_FILE, artifactsFileContents));
	}

	public static Either<ImmutablePair<String, byte[]>, ResponseFormat> getArtifactsContent(String csarUUID, Map<String, byte[]> csar, String artifactName, ComponentsUtils componentsUtils) {
		if (!csar.containsKey(ARTIFACTS + artifactName)) {
			log.debug("Entry-Definitions entry not found in Artifacts/HEAT.meta file, csar ID {}", csarUUID);
			BeEcompErrorManager.getInstance().logInternalDataError("Entry-Definitions entry not found in TOSCA-Metadata/TOSCA.meta file in CSAR with id " + csarUUID, "CSAR internals are invalid", ErrorSeverity.ERROR);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND_IN_CSAR, ARTIFACTS + artifactName, csarUUID));
		}

		log.trace("Found Entry-Definitions property in Artifacts/HEAT.meta, Entry-Definitions: {}, CSAR id: {}", ARTIFACTS + artifactName, csarUUID);
		byte[] artifactFileBytes = csar.get(ARTIFACTS + artifactName);
		if (artifactFileBytes == null) {
			log.debug("Entry-Definitions {} file not found in csar, csar ID {}", ARTIFACTS + artifactName, csarUUID);
			BeEcompErrorManager.getInstance().logInternalDataError("Entry-Definitions " + ARTIFACTS + artifactName + " file not found in CSAR with id " + csarUUID, "CSAR structure is invalid", ErrorSeverity.ERROR);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND_IN_CSAR, ARTIFACTS + artifactName, csarUUID));
		}

		return Either.left(new ImmutablePair<String, byte[]>(artifactName, artifactFileBytes));
	}

	private static Either<Boolean, ResponseFormat> validateTOSCAMetadataFile(Map<String, byte[]> csar, String csarUUID, ComponentsUtils componentsUtils) {

		byte[] toscaMetaBytes = csar.get(TOSCA_METADATA_FILE);
		String toscaMetadata = new String(toscaMetaBytes);
		String[] splited = toscaMetadata.split(NEW_LINE_DELM);
		if (splited == null || splited.length < TOSCA_METADATA_FIELDS.length) {
			log.debug("TOSCA-Metadata/TOSCA.meta file is not in expected key-value form in csar, csar ID {}", csarUUID);
			BeEcompErrorManager.getInstance().logInternalDataError("TOSCA-Metadata/TOSCA.meta file not in expected key-value form in CSAR with id " + csarUUID, "CSAR internals are invalid", ErrorSeverity.ERROR);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, csarUUID));
		}
		/*
		 * if(splited.length == TOSCA_METADATA_FIELDS.length){ if(!toscaMetadata.endsWith(NEW_LINE_DELM)){ log. debug("TOSCA-Metadata/TOSCA.meta file is not in expected key-value form in csar, csar ID {}" , csarUUID);
		 * BeEcompErrorManager.getInstance(). logInternalDataError("TOSCA-Metadata/TOSCA.meta file not in expected key-value form in CSAR with id " +csarUUID, "CSAR internals are invalid", ErrorSeverity.ERROR); return
		 * Either.right(componentsUtils.getResponseFormat(ActionStatus. CSAR_INVALID_FORMAT, csarUUID)); } }
		 */

		Either<Boolean, ResponseFormat> block_0Status = validateBlock_0(csarUUID, splited, componentsUtils);
		if (block_0Status.isRight()) {
			return Either.right(block_0Status.right().value());
		}

		return Either.left(true);

	}

	private static Either<Boolean, ResponseFormat> validateBlock_0(String csarUUID, String[] splited, ComponentsUtils componentsUtils) {
		int index = 0;
		for (String toscaField : TOSCA_METADATA_FIELDS) {

			Properties props = new Properties();

			try {
				props.load(new ByteArrayInputStream(splited[index].getBytes()));
			} catch (IOException e) {
				log.debug("TOSCA-Metadata/TOSCA.meta file is not in expected key-value form in csar, csar ID {}", csarUUID);
				BeEcompErrorManager.getInstance().logInternalDataError("TOSCA-Metadata/TOSCA.meta file not in expected key-value form in CSAR with id " + csarUUID, "CSAR internals are invalid", ErrorSeverity.ERROR);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, csarUUID));
			}
			if (!props.containsKey(toscaField)) {
				log.debug("TOSCA.meta file format is invalid: No new line after block_0 as expected in csar, csar ID {}", csarUUID);
				BeEcompErrorManager.getInstance().logInternalDataError("TOSCA-Metadata/TOSCA.meta file not in expected key-value form in CSAR with id " + csarUUID, "CSAR internals are invalid", ErrorSeverity.ERROR);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, csarUUID));
			}
			String value = props.getProperty(toscaField);
			if (value == null || value.isEmpty()) {
				log.debug("TOSCA-Metadata/TOSCA.meta file is not in expected key-value form in csar, csar ID {}", csarUUID);
				BeEcompErrorManager.getInstance().logInternalDataError("TOSCA-Metadata/TOSCA.meta file not in expected key-value form in CSAR with id " + csarUUID, "CSAR internals are invalid", ErrorSeverity.ERROR);
				return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, csarUUID));
			}

			// TOSCA-Meta-File-Version & CSAR-Version : digit.digit - format
			// validation
			if (toscaField.equals(TOSCA_META_FILE_VERSION) || toscaField.equals(CSAR_VERSION)) {
				if (!validateTOSCAMetaProperty(value)) {
					log.debug("TOSCA-Metadata/TOSCA.meta file contains {} in wrong format (digit.digit), csar ID {}", toscaField, csarUUID);
					BeEcompErrorManager.getInstance().logInternalDataError("TOSCA-Metadata/TOSCA.meta file not in expected key-value form in CSAR with id " + csarUUID, "CSAR internals are invalid", ErrorSeverity.ERROR);
					return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, csarUUID));
				}
			}
			index++;
		}
		return Either.left(true);
	}

	private static boolean validateTOSCAMetaProperty(String toscaProperty) {
		final String FLOAT_STRING = "^\\d{1}[.]\\d{1}$";
		final Pattern FLOAT_PATTERN = Pattern.compile(FLOAT_STRING);

		Matcher floatMatcher = FLOAT_PATTERN.matcher(toscaProperty);
		return floatMatcher.matches();
	}

	private static Either<Boolean, ResponseFormat> validateIsTOSCAMetadataExist(Map<String, byte[]> csar, String csarUUID, ComponentsUtils componentsUtils) {
		if (csar == null || csar.isEmpty()) {
			log.debug("Error when fetching csar with ID {}", csarUUID);
			BeEcompErrorManager.getInstance().logBeDaoSystemError("Creating resource from CSAR: fetching CSAR with id " + csarUUID + " failed");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID, csarUUID);
			return Either.right(responseFormat);
		}
		if (!csar.containsKey(TOSCA_METADATA_FILE)) {
			log.debug("TOSCA-Metadata/TOSCA.meta file not found in csar, csar ID {}", csarUUID);
			BeEcompErrorManager.getInstance().logInternalDataError("TOSCA-Metadata/TOSCA.meta file not found in CSAR with id " + csarUUID, "CSAR structure is invalid", ErrorSeverity.ERROR);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID, csarUUID));
		}
		byte[] toscaMetaBytes = csar.get(TOSCA_METADATA_FILE);
		// Tal && exchanged for ||
		if (toscaMetaBytes == null || toscaMetaBytes.length == 0) {
			log.debug("TOSCA-Metadata/TOSCA.meta file not found in csar, csar ID {}", csarUUID);
			BeEcompErrorManager.getInstance().logInternalDataError("TOSCA-Metadata/TOSCA.meta file not found in CSAR with id " + csarUUID, "CSAR structure is invalid", ErrorSeverity.ERROR);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID, csarUUID));
		}

		return Either.left(Boolean.TRUE);
	}

	public static Either<String, ResponseFormat> getToscaYamlChecksum(Map<String, byte[]> csar, String csarUUID, ComponentsUtils componentsUtils) {

		Either<ImmutablePair<String, String>, ResponseFormat> toscaYamlRes = getToscaYaml(csar, csarUUID, componentsUtils);
		if (toscaYamlRes.isRight() || toscaYamlRes.left().value() == null || toscaYamlRes.left().value().getRight() == null) {
			log.debug("Faild to create toscaYamlChecksum for csar, csar ID {}", csarUUID);
			return Either.right(toscaYamlRes.right().value());
		}

		String newCheckSum = GeneralUtility.calculateMD5ByByteArray(toscaYamlRes.left().value().getRight().getBytes());
		return Either.left(newCheckSum);

	}

	public static boolean isCsarPayloadName(String payloadName) {
		if (payloadName == null)
			return false;
		return payloadName.toLowerCase().endsWith(TOSCA_CSAR_EXTENSION);
	}

}
