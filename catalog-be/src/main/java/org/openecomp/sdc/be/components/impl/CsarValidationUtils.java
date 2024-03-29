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

import fj.data.Either;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.exception.ResponseFormat;

public class CsarValidationUtils {

    private static final Logger log = Logger.getLogger(CsarValidationUtils.class.getName());
    private static final String TOSCA_META_FILE_VERSION = "TOSCA-Meta-File-Version";
    private static final String CSAR_VERSION = "CSAR-Version";
    private static final String CREATED_BY = "Created-By";
    private static final String NEW_LINE_DELM = "\n";
    private static final String TOSCA_METADATA = "TOSCA-Metadata";
    private static final String TOSCA_FILE = "TOSCA.meta";
    private static final String DEL_PATTERN = "([/\\\\]+)";
    public static final String TOSCA_METADATA_PATH_PATTERN = TOSCA_METADATA +
        // Artifact Group (i.e Deployment/Informational)
        DEL_PATTERN + TOSCA_FILE;
    public static final String TOSCA_META_ENTRY_DEFINITIONS = "Entry-Definitions";
    private static final String[] TOSCA_METADATA_FIELDS = {TOSCA_META_FILE_VERSION, CSAR_VERSION, CREATED_BY, TOSCA_META_ENTRY_DEFINITIONS};
    private static final String ARTIFACTS_METADATA_FILE = "HEAT.meta";
    private static final String TOSCA_CSAR_EXTENSION = ".csar";
    private static final String TOSCA_METADATA_TOSCA_META_FILE_IS_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_CSAR_ID = "TOSCA-Metadata/TOSCA.meta file is not in expected key-value form in csar, csar ID '%s'";
    private static final String TOSCA_METADATA_TOSCA_META_FILE_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_WITH_ID = "TOSCA-Metadata/TOSCA.meta file not in expected key-value form in CSAR with id ";
    private static final String CSAR_INTERNALS_ARE_INVALID = "CSAR internals are invalid";
    private static final String ENTRY_DEFINITIONS_ENTRY_NOT_FOUND_IN_TOSCA_METADATA_TOSCA_META_FILE_CSAR_ID = "Entry-Definitions entry not found in TOSCA-Metadata/TOSCA.meta file, csar ID {}";
    private static final String FILE_NOT_FOUND_IN_CSAR_WITH_ID = " file not found in CSAR with id ";
    private static final String CSAR_STRUCTURE_IS_INVALID = "CSAR structure is invalid";
    private static final String ENTRY_DEFINITIONS = "Entry-Definitions ";
    private static final Pattern floatPattern = Pattern.compile("^\\d+\\.\\d+$");

    /**
     * Utility classes, which are collections of static members, are not meant to be instantiated
     */
    private CsarValidationUtils() {
    }

    /**
     * Validates Csar
     *
     * @param csar
     * @param csarUUID
     * @param componentsUtils
     * @return
     */
    public static Either<Boolean, ResponseFormat> validateCsar(Map<String, byte[]> csar, String csarUUID, ComponentsUtils componentsUtils) {
        Either<Boolean, ResponseFormat> validateStatus = validateIsTOSCAMetadataExist(csar, csarUUID, componentsUtils);
        if (validateStatus.isRight()) {
            return validateStatus;
        }
        removeNonUniqueArtifactsFromCsar(csar);
        log.trace("TOSCA-Metadata/TOSCA.meta file found, CSAR id {}", csarUUID);
        validateStatus = validateTOSCAMetadataFile(csar, csarUUID, componentsUtils);
        if (validateStatus.isRight()) {
            return validateStatus;
        }
        return Either.left(true);
    }

    private static void removeNonUniqueArtifactsFromCsar(Map<String, byte[]> csar) {
        List<String> nonUniqueArtifactsToRemove = new ArrayList<>();
        String[] paths = csar.keySet().toArray(new String[csar.keySet().size()]);
        int numberOfArtifacts = paths.length;
        for (int i = 0; i < numberOfArtifacts; ++i) {
            collectNonUniqueArtifact(paths, i, numberOfArtifacts, nonUniqueArtifactsToRemove);
        }
        nonUniqueArtifactsToRemove.stream().forEach(csar::remove);
    }

    private static void collectNonUniqueArtifact(String[] paths, int currInd, int numberOfArtifacts, List<String> nonUniqueArtifactsToRemove) {
        String[] parsedPath = paths[currInd].split("/");
        String[] otherParsedPath;
        int artifactNameInd = parsedPath.length - 1;
        for (int j = currInd + 1; j < numberOfArtifacts; ++j) {
            otherParsedPath = paths[j].split("/");
            if (parsedPath.length == otherParsedPath.length && parsedPath.length > 3 && isEqualArtifactNames(parsedPath, otherParsedPath)) {
                log.error(
                    "Can't upload two artifact with the same name {}. The artifact with path {} will be handled, and the artifact with path {} will be ignored. ",
                    parsedPath[artifactNameInd], paths[currInd], paths[j]);
                nonUniqueArtifactsToRemove.add(paths[j]);
            }
        }
    }

    private static boolean isEqualArtifactNames(String[] parsedPath, String[] otherParsedPath) {
        boolean isEqualArtifactNames = false;
        int artifactNameInd = parsedPath.length - 1;
        int artifactGroupTypeInd = parsedPath.length - 3;
        String groupType = parsedPath[artifactGroupTypeInd];
        String artifactName = parsedPath[artifactNameInd];
        String otherGroupType = otherParsedPath[artifactGroupTypeInd];
        String otherArtifactName = otherParsedPath[artifactNameInd];
        String vfcToscaName = parsedPath.length == 5 ? parsedPath[1] : null;
        if (artifactName.equalsIgnoreCase(otherArtifactName) && groupType.equalsIgnoreCase(otherGroupType)) {
            isEqualArtifactNames = vfcToscaName == null || vfcToscaName.equalsIgnoreCase(otherParsedPath[1]);
        }
        return isEqualArtifactNames;
    }

    public static Either<ImmutablePair<String, String>, ResponseFormat> getToscaYaml(Map<String, byte[]> csar, String csarUUID,
                                                                                     ComponentsUtils componentsUtils, AuditingActionEnum auditingAction) {
        if (!AuditingActionEnum.UPDATE_SERVICE_TOSCA_TEMPLATE.equals(auditingAction)) {
            Either<Boolean, ResponseFormat> validateStatus = validateIsTOSCAMetadataExist(csar, csarUUID, componentsUtils);
            if (validateStatus.isRight()) {
                return Either.right(validateStatus.right().value());
            }
        }
        Pattern pattern = Pattern.compile(TOSCA_METADATA_PATH_PATTERN);
        Optional<String> keyOp = csar.keySet().stream().filter(k -> pattern.matcher(k).matches()).findAny();
        if (keyOp.isEmpty()) {
            log.error(String.format(TOSCA_METADATA_TOSCA_META_FILE_IS_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_CSAR_ID, csarUUID));
            BeEcompErrorManager.getInstance()
                .logInternalDataError(TOSCA_METADATA_TOSCA_META_FILE_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_WITH_ID + csarUUID,
                    CSAR_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, csarUUID,
                String.format(TOSCA_METADATA_TOSCA_META_FILE_IS_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_CSAR_ID, csarUUID)));
        }
        Properties props = new Properties();
        try {
            props.load(new StringReader(new String(csar.get(keyOp.get())).replace("\\", "\\\\")));
        } catch (IOException e) {
            log.error(String.format(TOSCA_METADATA_TOSCA_META_FILE_IS_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_CSAR_ID, csarUUID), e);
            BeEcompErrorManager.getInstance()
                .logInternalDataError(TOSCA_METADATA_TOSCA_META_FILE_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_WITH_ID + csarUUID,
                    CSAR_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, csarUUID,
                String.format(TOSCA_METADATA_TOSCA_META_FILE_IS_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_CSAR_ID, csarUUID)));
        }
        String yamlFileName = props.getProperty(TOSCA_META_ENTRY_DEFINITIONS);
        String result = Arrays.asList(yamlFileName.split(DEL_PATTERN)).stream().map(x -> x).collect(Collectors.joining(DEL_PATTERN));
        keyOp = csar.keySet().stream().filter(k -> Pattern.compile(result).matcher(k).matches()).findAny();
        if (keyOp.isEmpty()) {
            log.error(ENTRY_DEFINITIONS_ENTRY_NOT_FOUND_IN_TOSCA_METADATA_TOSCA_META_FILE_CSAR_ID, csarUUID);
            BeEcompErrorManager.getInstance()
                .logInternalDataError(ENTRY_DEFINITIONS_ENTRY_NOT_FOUND_IN_TOSCA_METADATA_TOSCA_META_FILE_CSAR_ID + csarUUID,
                    CSAR_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.YAML_NOT_FOUND_IN_CSAR, csarUUID, yamlFileName));
        }
        log.trace("Found Entry-Definitions property in TOSCA-Metadata/TOSCA.meta, Entry-Definitions: {}, CSAR id: {}", yamlFileName, csarUUID);
        byte[] yamlFileBytes = csar.get(yamlFileName);
        if (yamlFileBytes == null) {
            log.error("Entry-Definitions {} file not found in csar, csar ID {}", yamlFileName, csarUUID);
            BeEcompErrorManager.getInstance()
                .logInternalDataError(ENTRY_DEFINITIONS + yamlFileName + FILE_NOT_FOUND_IN_CSAR_WITH_ID + csarUUID, CSAR_STRUCTURE_IS_INVALID,
                    ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.YAML_NOT_FOUND_IN_CSAR, csarUUID, yamlFileName));
        }
        return Either.left(new ImmutablePair<>(yamlFileName, new String(yamlFileBytes)));
    }

    public static Either<ImmutablePair<String, String>, ResponseFormat> getArtifactsMeta(Map<String, byte[]> csar, String csarUUID,
                                                                                         ComponentsUtils componentsUtils) {
        if (!csar.containsKey(CsarUtils.ARTIFACTS_PATH + ARTIFACTS_METADATA_FILE)) {
            log.error(ENTRY_DEFINITIONS_ENTRY_NOT_FOUND_IN_TOSCA_METADATA_TOSCA_META_FILE_CSAR_ID, csarUUID);
            BeEcompErrorManager.getInstance()
                .logInternalDataError(ENTRY_DEFINITIONS_ENTRY_NOT_FOUND_IN_TOSCA_METADATA_TOSCA_META_FILE_CSAR_ID + csarUUID,
                    CSAR_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.YAML_NOT_FOUND_IN_CSAR, csarUUID, ARTIFACTS_METADATA_FILE));
        }
        log.trace("Found Entry-Definitions property in TOSCA-Metadata/TOSCA.meta, Entry-Definitions: {}, CSAR id: {}", ARTIFACTS_METADATA_FILE,
            csarUUID);
        byte[] artifactsMetaBytes = csar.get(CsarUtils.ARTIFACTS_PATH + ARTIFACTS_METADATA_FILE);
        if (artifactsMetaBytes == null) {
            log.error("Entry-Definitions {}{} file not found in csar, csar ID {}", CsarUtils.ARTIFACTS_PATH, ARTIFACTS_METADATA_FILE, csarUUID);
            BeEcompErrorManager.getInstance().logInternalDataError(
                ENTRY_DEFINITIONS + CsarUtils.ARTIFACTS_PATH + ARTIFACTS_METADATA_FILE + FILE_NOT_FOUND_IN_CSAR_WITH_ID + csarUUID,
                CSAR_STRUCTURE_IS_INVALID, ErrorSeverity.ERROR);
            return Either.right(
                componentsUtils.getResponseFormat(ActionStatus.YAML_NOT_FOUND_IN_CSAR, csarUUID, CsarUtils.ARTIFACTS_PATH + ARTIFACTS_METADATA_FILE));
        }
        return Either.left(new ImmutablePair<>(CsarUtils.ARTIFACTS_PATH + ARTIFACTS_METADATA_FILE, new String(artifactsMetaBytes)));
    }

    public static Either<ImmutablePair<String, byte[]>, ResponseFormat> getArtifactContent(String csarUUID, Map<String, byte[]> csar,
                                                                                           String artifactPath, String artifactName,
                                                                                           ComponentsUtils componentsUtils) {
        if (!csar.containsKey(artifactPath)) {
            log.error("Entry-Definitions entry not found in Artifacts/HEAT.meta file, csar ID {}", csarUUID);
            BeEcompErrorManager.getInstance()
                .logInternalDataError(ENTRY_DEFINITIONS_ENTRY_NOT_FOUND_IN_TOSCA_METADATA_TOSCA_META_FILE_CSAR_ID + csarUUID,
                    CSAR_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
            return Either
                .right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND_IN_CSAR, CsarUtils.ARTIFACTS_PATH + artifactName, csarUUID));
        }
        log.trace("Found Entry-Definitions property in Artifacts/HEAT.meta, Entry-Definitions: {}, CSAR id: {}", artifactPath, csarUUID);
        byte[] artifactFileBytes = csar.get(artifactPath);
        if (artifactFileBytes == null) {
            log.error("Entry-Definitions {}{} file not found in csar, csar ID {}", CsarUtils.ARTIFACTS_PATH, artifactName, csarUUID);
            BeEcompErrorManager.getInstance()
                .logInternalDataError(ENTRY_DEFINITIONS + artifactPath + FILE_NOT_FOUND_IN_CSAR_WITH_ID + csarUUID, CSAR_STRUCTURE_IS_INVALID,
                    ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_NOT_FOUND_IN_CSAR, artifactPath, csarUUID));
        }
        return Either.left(new ImmutablePair<>(artifactName, artifactFileBytes));
    }

    private static Either<Boolean, ResponseFormat> validateTOSCAMetadataFile(Map<String, byte[]> csar, String csarUUID,
                                                                             ComponentsUtils componentsUtils) {
        Pattern pattern = Pattern.compile(TOSCA_METADATA_PATH_PATTERN);
        Optional<String> keyOp = csar.keySet().stream().filter(k -> pattern.matcher(k).matches()).findAny();
        if (keyOp.isEmpty()) {
            log.error(String.format(TOSCA_METADATA_TOSCA_META_FILE_IS_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_CSAR_ID, csarUUID));
            BeEcompErrorManager.getInstance()
                .logInternalDataError(TOSCA_METADATA_TOSCA_META_FILE_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_WITH_ID + csarUUID,
                    CSAR_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, csarUUID,
                String.format(TOSCA_METADATA_TOSCA_META_FILE_IS_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_CSAR_ID, csarUUID)));
        }
        String toscaMetadata = new String(csar.get(keyOp.get()));
        Either<Boolean, ResponseFormat> blockStatus = validateBlock(csarUUID, componentsUtils, toscaMetadata);
        if (blockStatus.isRight()) {
            return blockStatus;
        }
        return Either.left(true);
    }

    private static Either<Boolean, ResponseFormat> validateBlock(String csarUUID, ComponentsUtils componentsUtils, String toscaMetadata) {
        final String[] splited = toscaMetadata.split(NEW_LINE_DELM);
        if (splited == null || splited.length < TOSCA_METADATA_FIELDS.length) {
            log.error(String.format(TOSCA_METADATA_TOSCA_META_FILE_IS_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_CSAR_ID, csarUUID));
            BeEcompErrorManager.getInstance()
                .logInternalDataError(TOSCA_METADATA_TOSCA_META_FILE_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_WITH_ID + csarUUID,
                    CSAR_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, csarUUID,
                String.format(TOSCA_METADATA_TOSCA_META_FILE_IS_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_CSAR_ID, csarUUID)));
        }
        final Properties props = new Properties();
        try {
            props.load(new ByteArrayInputStream(toscaMetadata.getBytes()));
        } catch (final IOException e) {
            log.error(String.format(TOSCA_METADATA_TOSCA_META_FILE_IS_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_CSAR_ID, csarUUID), e);
            BeEcompErrorManager.getInstance()
                .logInternalDataError(TOSCA_METADATA_TOSCA_META_FILE_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_WITH_ID + csarUUID,
                    CSAR_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, csarUUID,
                String.format(TOSCA_METADATA_TOSCA_META_FILE_IS_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_CSAR_ID, csarUUID)));
        }
        for (final String toscaField : TOSCA_METADATA_FIELDS) {
            if (!props.containsKey(toscaField)) {
                final String msg = "TOSCA.meta file format is invalid: No new line after block_0 as expected in csar, csar ID {}";
                log.error(msg, csarUUID);
                BeEcompErrorManager.getInstance()
                    .logInternalDataError(TOSCA_METADATA_TOSCA_META_FILE_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_WITH_ID + csarUUID,
                        CSAR_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, csarUUID, msg, csarUUID));
            }
            final String value = props.getProperty(toscaField);
            if (value == null || value.isEmpty()) {
                log.error(String.format(TOSCA_METADATA_TOSCA_META_FILE_IS_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_CSAR_ID, csarUUID));
                BeEcompErrorManager.getInstance()
                    .logInternalDataError(TOSCA_METADATA_TOSCA_META_FILE_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_WITH_ID + csarUUID,
                        CSAR_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, csarUUID,
                    String.format(TOSCA_METADATA_TOSCA_META_FILE_IS_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_CSAR_ID, csarUUID)));
            }
            // TOSCA-Meta-File-Version & CSAR-Version : digit.digit - format validation
            if ((toscaField.equals(TOSCA_META_FILE_VERSION) || toscaField.equals(CSAR_VERSION)) && !validateTOSCAMetaProperty(value)) {
                final String msg = "TOSCA-Metadata/TOSCA.meta file contains {} in wrong format (digit.digit), csar ID {}";
                log.error(msg, toscaField, csarUUID);
                BeEcompErrorManager.getInstance()
                    .logInternalDataError(TOSCA_METADATA_TOSCA_META_FILE_NOT_IN_EXPECTED_KEY_VALUE_FORM_IN_CSAR_WITH_ID + csarUUID,
                        CSAR_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, csarUUID, msg, toscaField, csarUUID));
            }
        }
        return Either.left(true);
    }

    private static boolean validateTOSCAMetaProperty(String toscaProperty) {
        return floatPattern.matcher(toscaProperty).matches();
    }

    private static Either<Boolean, ResponseFormat> validateIsTOSCAMetadataExist(Map<String, byte[]> csar, String csarUUID,
                                                                                ComponentsUtils componentsUtils) {
        if (csar == null || csar.isEmpty()) {
            log.error("Error when fetching csar with ID {}", csarUUID);
            BeEcompErrorManager.getInstance().logBeDaoSystemError("Creating resource from CSAR: fetching CSAR with id " + csarUUID + " failed");
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID, csarUUID));
        }
        Pattern pattern = Pattern.compile(TOSCA_METADATA_PATH_PATTERN);
        Optional<String> keyOp = csar.keySet().stream().filter(k -> pattern.matcher(k).matches()).findAny();
        if (keyOp.isEmpty()) {
            log.error("TOSCA-Metadata/TOSCA.meta file not found in csar, csar ID {}", csarUUID);
            BeEcompErrorManager.getInstance()
                .logInternalDataError("TOSCA-Metadata/TOSCA.meta file not found in CSAR with id " + csarUUID, CSAR_STRUCTURE_IS_INVALID,
                    ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID, csarUUID));
        }
        byte[] toscaMetaBytes = csar.get(keyOp.get());
        if (toscaMetaBytes == null || toscaMetaBytes.length == 0) {
            log.error("TOSCA-Metadata/TOSCA.meta file not found in csar, csar ID {}", csarUUID);
            BeEcompErrorManager.getInstance()
                .logInternalDataError("TOSCA-Metadata/TOSCA.meta file not found in CSAR with id " + csarUUID, CSAR_STRUCTURE_IS_INVALID,
                    ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID, csarUUID));
        }
        return Either.left(Boolean.TRUE);
    }

    public static Either<String, ResponseFormat> getToscaYamlChecksum(Map<String, byte[]> csar, String csarUUID, ComponentsUtils componentsUtils) {
        Either<ImmutablePair<String, String>, ResponseFormat> toscaYamlRes = getToscaYaml(csar, csarUUID, componentsUtils, AuditingActionEnum.GET_TOSCA_MODEL);
        if (toscaYamlRes.isRight() || toscaYamlRes.left().value() == null || toscaYamlRes.left().value().getRight() == null) {
            log.error("Faild to create toscaYamlChecksum for csar, csar ID {}", csarUUID);
            return Either.right(toscaYamlRes.right().value());
        }
        return Either.left(GeneralUtility.calculateMD5Base64EncodedByByteArray(toscaYamlRes.left().value().getRight().getBytes()));
    }

    public static boolean isCsarPayloadName(String payloadName) {
        return payloadName != null && payloadName.toLowerCase().endsWith(TOSCA_CSAR_EXTENSION);
    }
}
