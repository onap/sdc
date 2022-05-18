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

import static org.openecomp.sdc.be.tosca.ComponentCache.MergeStrategy.overwriteIfSameVersions;
import static org.openecomp.sdc.be.tosca.FJToVavrHelper.Try0.fromEither;

import fj.F;
import fj.data.Either;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.text.WordUtils;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.config.ArtifactConfigManager;
import org.openecomp.sdc.be.config.ArtifactConfiguration;
import org.openecomp.sdc.be.config.ComponentType;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.SdcSchemaFilesCassandraDao;
import org.openecomp.sdc.be.data.model.ToscaImportByModel;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.plugins.CsarEntryGenerator;
import org.openecomp.sdc.be.resources.data.DAOArtifactData;
import org.openecomp.sdc.be.tosca.utils.OperationArtifactUtil;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;

/**
 * @author tg851x
 */
@org.springframework.stereotype.Component("csar-utils")
public class CsarUtils {

    public static final String NODES_YML = "nodes.yml";
    public static final String ARTIFACTS_PATH = "Artifacts/";
    public static final String ARTIFACTS = "Artifacts";
    public static final String ARTIFACT_CREATED_FROM_CSAR = "Artifact created from csar";
    private static final Logger log = Logger.getLogger(CsarUtils.class);
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(CsarUtils.class.getName());
    private static final String PATH_DELIMITER = "/";
    private static final String CONFORMANCE_LEVEL = ConfigurationManager.getConfigurationManager().getConfiguration().getToscaConformanceLevel();
    private static final String SDC_VERSION = ExternalConfiguration.getAppVersion();
    private static final String RESOURCES_PATH = "Resources/";
    private static final String DEFINITIONS_PATH = "Definitions/";
    private static final String CSAR_META_VERSION = "1.0";
    private static final String CSAR_META_PATH_FILE_NAME = "csar.meta";
    private static final String TOSCA_META_PATH_FILE_NAME = "TOSCA-Metadata/TOSCA.meta";
    private static final String TOSCA_META_VERSION = "1.0";
    private static final String CSAR_VERSION = "1.1";
    // add manifest
    private static final String SERVICE_MANIFEST = "NS.mf";
    private static final String DEFINITION = "Definitions";
    private static final String DEL_PATTERN = "([/\\\\]+)";
    private static final String WORD_PATTERN = "\\w\\_\\@\\-\\.\\s]+)";
    public static final String VALID_ENGLISH_ARTIFACT_NAME = "([" + WORD_PATTERN;
    public static final String VF_NODE_TYPE_ARTIFACTS_PATH_PATTERN = ARTIFACTS + DEL_PATTERN +
        // Artifact Group (i.e Deployment/Informational)
        VALID_ENGLISH_ARTIFACT_NAME + DEL_PATTERN +
        // Artifact Type
        VALID_ENGLISH_ARTIFACT_NAME + DEL_PATTERN +
        // Artifact Any File Name
        ".+";
    public static final String SERVICE_TEMPLATE_PATH_PATTERN = DEFINITION + DEL_PATTERN +
        // Service Template File Name
        VALID_ENGLISH_ARTIFACT_NAME;
    private static final String VALID_ENGLISH_ARTIFACT_NAME_WITH_DIGITS = "([\\d" + WORD_PATTERN;
    private static final String ARTIFACT_NAME_UNIQUE_ID = "ArtifactName {}, unique ID {}";
    private static final String VFC_NODE_TYPE_ARTIFACTS_PATH_PATTERN =
        ARTIFACTS + DEL_PATTERN + ImportUtils.Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + VALID_ENGLISH_ARTIFACT_NAME_WITH_DIGITS + DEL_PATTERN
            + VALID_ENGLISH_ARTIFACT_NAME_WITH_DIGITS + DEL_PATTERN + VALID_ENGLISH_ARTIFACT_NAME_WITH_DIGITS + DEL_PATTERN
            + VALID_ENGLISH_ARTIFACT_NAME_WITH_DIGITS;
    private static final String BLOCK_0_TEMPLATE = "SDC-TOSCA-Meta-File-Version: %s\nSDC-TOSCA-Definitions-Version: %s\n";

    private final ToscaOperationFacade toscaOperationFacade;
    private final SdcSchemaFilesCassandraDao sdcSchemaFilesCassandraDao;
    private final ArtifactCassandraDao artifactCassandraDao;
    private final ComponentsUtils componentsUtils;
    private final ToscaExportHandler toscaExportUtils;
    private final List<CsarEntryGenerator> generators;
    private final ModelOperation modelOperation;
    private final String versionFirstThreeOctets;

    @Autowired
    public CsarUtils(final ToscaOperationFacade toscaOperationFacade, final SdcSchemaFilesCassandraDao sdcSchemaFilesCassandraDao,
                     final ArtifactCassandraDao artifactCassandraDao, final ComponentsUtils componentsUtils,
                     final ToscaExportHandler toscaExportUtils, final List<CsarEntryGenerator> generators, final ModelOperation modelOperation) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.sdcSchemaFilesCassandraDao = sdcSchemaFilesCassandraDao;
        this.artifactCassandraDao = artifactCassandraDao;
        this.componentsUtils = componentsUtils;
        this.toscaExportUtils = toscaExportUtils;
        this.generators = generators;
        this.modelOperation = modelOperation;
        this.versionFirstThreeOctets = readVersionFirstThreeOctets();
    }

    private String readVersionFirstThreeOctets() {
        if (StringUtils.isEmpty(SDC_VERSION)) {
            return "";
        }
        // change regex to avoid DoS sonar issue
        Matcher matcher = Pattern.compile("(?!\\.)(\\d{1,9}(\\.\\d{1,9}){1,9})(?![\\d\\.])").matcher(SDC_VERSION);
        matcher.find();
        return matcher.group(0);
    }


    private static <L, R> F<L, Either<L, R>> iff(Predicate<L> p, Function<L, Either<L, R>> ifTrue) {
        return l -> p.test(l) ? ifTrue.apply(l) : Either.left(l);
    }

    private static <A, B> F<A, B> iff(Predicate<A> p, Supplier<B> s, Function<A, B> orElse) {
        return a -> p.test(a) ? s.get() : orElse.apply(a);
    }

    /**
     * Extracts artifacts of VFCs from CSAR
     *
     * @param csar
     * @return Map of <String, List<ArtifactDefinition>> the contains Lists of artifacts according vfcToscaNamespace
     */
    public static Map<String, List<ArtifactDefinition>> extractVfcsArtifactsFromCsar(Map<String, byte[]> csar) {
        Map<String, List<ArtifactDefinition>> artifacts = new HashMap<>();
        if (csar != null) {
            log.debug("************* Going to extract VFCs artifacts from Csar. ");
            Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();
            csar.entrySet().stream()
                // filter CSAR entry by node type artifact path
                .filter(e -> Pattern.compile(VFC_NODE_TYPE_ARTIFACTS_PATH_PATTERN).matcher(e.getKey()).matches())
                // extract ArtifactDefinition from CSAR entry for each entry with matching artifact path
                .forEach(e -> extractVfcArtifact(e, collectedWarningMessages).ifPresent(ip -> addExtractedVfcArtifact(ip, artifacts)));
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
        String vfcToscaNamespace = extractedVfcArtifact.getKey();
        artifacts.computeIfAbsent(vfcToscaNamespace, k -> new ArrayList<>());
        artifacts.get(vfcToscaNamespace).add(extractedVfcArtifact.getValue());
    }

    private static Optional<ImmutablePair<String, ArtifactDefinition>> extractVfcArtifact(Entry<String, byte[]> entry,
                                                                                          Map<String, Set<List<String>>> collectedWarningMessages) {
        String[] parsedCsarArtifactPath = entry.getKey().split(PATH_DELIMITER);
        String groupType = parsedCsarArtifactPath[2].toUpperCase();
        return detectArtifactGroupType(groupType, collectedWarningMessages).left()
            .map(buildArtifactDefinitionFromCsarArtifactPath(entry, collectedWarningMessages, parsedCsarArtifactPath))
            .either(ad -> Optional.of(new ImmutablePair<>(parsedCsarArtifactPath[1], ad)), b -> Optional.empty());
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

    private static F<ArtifactGroupTypeEnum, ArtifactDefinition> buildArtifactDefinitionFromCsarArtifactPath(Entry<String, byte[]> entry,
                                                                                                            Map<String, Set<List<String>>> collectedWarningMessages,
                                                                                                            String[] parsedCsarArtifactPath) {
        return artifactGroupType -> {
            ArtifactDefinition artifact;
            artifact = new ArtifactDefinition();
            artifact.setArtifactGroupType(artifactGroupType);
            artifact.setArtifactType(
                detectArtifactTypeVFC(artifactGroupType, parsedCsarArtifactPath[3], parsedCsarArtifactPath[1], collectedWarningMessages));
            artifact.setArtifactName(ValidationUtils.normalizeFileName(parsedCsarArtifactPath[parsedCsarArtifactPath.length - 1]));
            artifact.setPayloadData(Base64.encodeBase64String(entry.getValue()));
            artifact.setArtifactDisplayName(
                artifact.getArtifactName().lastIndexOf('.') > 0 ? artifact.getArtifactName().substring(0, artifact.getArtifactName().lastIndexOf('.'))
                    : artifact.getArtifactName());
            artifact.setArtifactLabel(ValidationUtils.normalizeArtifactLabel(artifact.getArtifactName()));
            artifact.setDescription(ARTIFACT_CREATED_FROM_CSAR);
            artifact.setIsFromCsar(true);
            artifact.setArtifactChecksum(GeneralUtility.calculateMD5Base64EncodedByByteArray(entry.getValue()));
            return artifact;
        };
    }

    /**
     * This method checks the artifact GroupType & Artifact Type. <br> if there is any problem warning messages are added to collectedWarningMessages
     *
     * @param artifactPath
     * @param collectedWarningMessages
     * @return
     */
    public static Either<NonMetaArtifactInfo, Boolean> validateNonMetaArtifact(String artifactPath, byte[] payloadData,
                                                                               Map<String, Set<List<String>>> collectedWarningMessages) {
        try {
            String[] parsedArtifactPath = artifactPath.split(PATH_DELIMITER);
            String groupType = parsedArtifactPath[1];
            String receivedTypeName = parsedArtifactPath[2];
            String artifactFileNameType = parsedArtifactPath[3];
            return detectArtifactGroupType(groupType, collectedWarningMessages).left().bind(artifactGroupType -> {
                String artifactType = detectArtifactTypeVF(artifactGroupType, receivedTypeName, collectedWarningMessages);
                return Either
                    .left(new NonMetaArtifactInfo(artifactFileNameType, artifactPath, artifactType, artifactGroupType, payloadData, null, true));
            });
        } catch (Exception e) {
            log.debug("detectArtifactGroupType failed with exception", e);
            return Either.right(false);
        }
    }

    private static String detectArtifactTypeVFC(ArtifactGroupTypeEnum artifactGroupType, String receivedTypeName, String parentVfName,
                                                Map<String, Set<List<String>>> collectedWarningMessages) {
        String warningMessage = "Warning - artifact type {} that was provided for VFC {} is not recognized.";
        return detectArtifactType(artifactGroupType, receivedTypeName, warningMessage, collectedWarningMessages, parentVfName);
    }

    private static String detectArtifactTypeVF(ArtifactGroupTypeEnum artifactGroupType, String receivedTypeName,
                                               Map<String, Set<List<String>>> collectedWarningMessages) {
        String warningMessage = "Warning - artifact type {} that was provided for VF is not recognized.";
        return detectArtifactType(artifactGroupType, receivedTypeName, warningMessage, collectedWarningMessages);
    }

    private static String detectArtifactType(final ArtifactGroupTypeEnum artifactGroupType, final String receivedTypeName,
                                             final String warningMessage, final Map<String, Set<List<String>>> collectedWarningMessages,
                                             final String... arguments) {
        final ArtifactConfiguration artifactConfiguration = ArtifactConfigManager.getInstance()
            .find(receivedTypeName, artifactGroupType, ComponentType.RESOURCE).orElse(null);
        if (artifactConfiguration == null) {
            final List<String> messageArguments = new ArrayList<>();
            messageArguments.add(receivedTypeName);
            messageArguments.addAll(Arrays.asList(arguments));
            if (!collectedWarningMessages.containsKey(warningMessage)) {
                final Set<List<String>> messageArgumentLists = new HashSet<>();
                messageArgumentLists.add(messageArguments);
                collectedWarningMessages.put(warningMessage, messageArgumentLists);
            } else {
                collectedWarningMessages.get(warningMessage).add(messageArguments);
            }
        }
        return artifactConfiguration == null ? ArtifactTypeEnum.OTHER.getType() : receivedTypeName;
    }

    /**
     * @param component
     * @param getFromCS
     * @param isInCertificationRequest
     * @return
     */
    public Either<byte[], ResponseFormat> createCsar(final Component component, final boolean getFromCS, final boolean isInCertificationRequest) {
        loggerSupportability
            .log(LoggerSupportabilityActions.GENERATE_CSAR, StatusCode.STARTED, "Starting to create Csar for component {} ", component.getName());
        final String createdBy = component.getCreatorFullName();
        final Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
        final ArtifactDefinition artifactDefinition = toscaArtifacts.get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE);
        final String fileName = artifactDefinition.getArtifactName();
        final String toscaConformanceLevel = ConfigurationManager.getConfigurationManager().getConfiguration().getToscaConformanceLevel();
        final byte[] csarBlock0Byte = createCsarBlock0(CSAR_META_VERSION, toscaConformanceLevel).getBytes();
        final byte[] toscaBlock0Byte = createToscaBlock0(TOSCA_META_VERSION, CSAR_VERSION, createdBy, fileName, isAsdPackage(component)).getBytes();

        return generateCsarZip(csarBlock0Byte, toscaBlock0Byte, component, getFromCS, isInCertificationRequest).left().map(responseFormat -> {
            loggerSupportability
                .log(LoggerSupportabilityActions.GENERATE_CSAR, StatusCode.COMPLETE, "Ended create Csar for component {} ", component.getName());
            return responseFormat;
        });
    }

    private boolean isAsdPackage(final Component component) {
        final Either<CsarDefinition, ResponseFormat> collectedComponentCsarDefinition = collectComponentCsarDefinition(component);
        if (collectedComponentCsarDefinition.isLeft()) {
            final ComponentArtifacts componentArtifacts = collectedComponentCsarDefinition.left().value().getComponentArtifacts();
            if (componentArtifacts != null) {
                final ComponentTypeArtifacts mainTypeAndCIArtifacts = componentArtifacts.getMainTypeAndCIArtifacts();
                if (mainTypeAndCIArtifacts != null) {
                    final ArtifactsInfo artifactsInfo = mainTypeAndCIArtifacts.getComponentArtifacts();
                    if (artifactsInfo != null) {
                        final Map<ArtifactGroupTypeEnum, Map<String, List<ArtifactDefinition>>> artifactsInfosMap = artifactsInfo.getArtifactsInfo();
                        if (MapUtils.isNotEmpty(artifactsInfosMap) && artifactsInfosMap.containsKey(ArtifactGroupTypeEnum.DEPLOYMENT)) {
                            return artifactsInfosMap.get(ArtifactGroupTypeEnum.DEPLOYMENT).containsKey(ArtifactTypeEnum.ASD_PACKAGE.getType());
                        }
                    }
                }
            }
        }
        return false;
    }

    private Either<byte[], ResponseFormat> generateCsarZip(byte[] csarBlock0Byte, byte[] toscaBlock0Byte, Component component, boolean getFromCS,
                                                           boolean isInCertificationRequest) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry(CSAR_META_PATH_FILE_NAME));
            zip.write(csarBlock0Byte);
            zip.putNextEntry(new ZipEntry(TOSCA_META_PATH_FILE_NAME));
            zip.write(toscaBlock0Byte);
            Either<ZipOutputStream, ResponseFormat> populateZip = populateZip(component, getFromCS, zip, isInCertificationRequest);
            if (populateZip.isRight()) {
                log.debug("Failed to populate CSAR zip file {}. Please fix DB table accordingly ", populateZip.right().value());
                return Either.right(populateZip.right().value());
            }
            zip.finish();
            byte[] byteArray = out.toByteArray();
            return Either.left(byteArray);
        } catch (IOException e) {
            log.debug("Failed with IOexception to create CSAR zip for component {}. Please fix DB table accordingly ", component.getUniqueId(), e);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            return Either.right(responseFormat);
        }
    }

    private Either<ZipOutputStream, ResponseFormat> populateZip(Component component, boolean getFromCS, ZipOutputStream zip,
                                                                boolean isInCertificationRequest) throws IOException {
        ArtifactDefinition artifactDef = component.getToscaArtifacts().get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE);
        Either<ToscaRepresentation, ResponseFormat> toscaRepresentation = fetchToscaRepresentation(component, getFromCS, artifactDef);
        // This should not be done but in order to keep the refactoring small enough we stop here.

        // TODO: Refactor the rest of this function
        byte[] mainYaml;
        List<Triple<String, String, Component>> dependencies;
        if (toscaRepresentation.isLeft()) {
            mainYaml = toscaRepresentation.left().value().getMainYaml();
            dependencies = toscaRepresentation.left().value().getDependencies().getOrElse(new ArrayList<>());
        } else {
            return Either.right(toscaRepresentation.right().value());
        }
        String fileName = artifactDef.getArtifactName();
        zip.putNextEntry(new ZipEntry(DEFINITIONS_PATH + fileName));
        zip.write(mainYaml);
        LifecycleStateEnum lifecycleState = component.getLifecycleState();
        addServiceMf(component, zip, lifecycleState, isInCertificationRequest, fileName, mainYaml);
        //US798487 - Abstraction of complex types
        if (hasToWriteComponentSubstitutionType(component)) {
            log.debug("Component {} is complex - generating abstract type for it..", component.getName());
            dependencies.addAll(writeComponentInterface(component, zip, fileName));
        }
        //UID <cassandraId,filename,component>
        Either<ZipOutputStream, ResponseFormat> zipOutputStreamOrResponseFormat = getZipOutputStreamResponseFormatEither(zip, dependencies);
        if (zipOutputStreamOrResponseFormat != null && zipOutputStreamOrResponseFormat.isRight()) {
            return zipOutputStreamOrResponseFormat;
        }
        if (component.getModel() == null) {
            //retrieve SDC.zip from Cassandra
            Either<byte[], ResponseFormat> latestSchemaFiles = getLatestSchemaFilesFromCassandra();
            if (latestSchemaFiles.isRight()) {
                log.error("Error retrieving SDC Schema files from cassandra");
                return Either.right(latestSchemaFiles.right().value());
            }
            final byte[] schemaFileZip = latestSchemaFiles.left().value();
            final List<String> nodesFromPackage = findNonRootNodesFromPackage(dependencies);
            //add files from retrieved SDC.zip to Definitions folder in CSAR
            addSchemaFilesFromCassandra(zip, schemaFileZip, nodesFromPackage);
        } else {
            //retrieve schema files by model from Cassandra
            addSchemaFilesByModel(zip, component.getModel());
        }
        Either<CsarDefinition, ResponseFormat> collectedComponentCsarDefinition = collectComponentCsarDefinition(component);
        if (collectedComponentCsarDefinition.isRight()) {
            return Either.right(collectedComponentCsarDefinition.right().value());
        }
        if (generators != null) {
            for (CsarEntryGenerator generator : generators) {
                log.debug("Invoking CsarEntryGenerator: {}", generator.getClass().getName());
                for (Entry<String, byte[]> pluginGeneratedFile : generator.generateCsarEntries(component).entrySet()) {
                    zip.putNextEntry(new ZipEntry(pluginGeneratedFile.getKey()));
                    zip.write(pluginGeneratedFile.getValue());
                }
            }
        }
        return writeAllFilesToCsar(component, collectedComponentCsarDefinition.left().value(), zip, isInCertificationRequest);
    }

    private void addServiceMf(Component component, ZipOutputStream zip, LifecycleStateEnum lifecycleState, boolean isInCertificationRequest,
                              String fileName, byte[] mainYaml) throws IOException {
        // add mf
        if ((component.getComponentType() == ComponentTypeEnum.SERVICE) && (lifecycleState != LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT)) {
            String serviceName = component.getName();
            String createdBy = component.getCreatorUserId();
            String serviceVersion;
            if (isInCertificationRequest) {
                int tmp = Integer.valueOf(component.getVersion().split("\\.")[0]) + 1;
                serviceVersion = String.valueOf(tmp) + ".0";
            } else {
                serviceVersion = component.getVersion();
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = new Date();
            String releaseTime = format.format(date);
            if (component.getCategories() == null || component.getCategories().get(0) == null) {
                return;
            }
            String serviceType = component.getCategories().get(0).getName();
            String description = component.getDescription();
            String serviceTemplate = DEFINITIONS_PATH + fileName;
            String hash = GeneralUtility.calculateMD5Base64EncodedByByteArray(mainYaml);
            String nsMfBlock0 = createNsMfBlock0(serviceName, createdBy, serviceVersion, releaseTime, serviceType, description, serviceTemplate,
                hash);
            byte[] nsMfBlock0Byte = nsMfBlock0.getBytes();
            zip.putNextEntry(new ZipEntry(SERVICE_MANIFEST));
            zip.write(nsMfBlock0Byte);
        }
    }

    private Either<ToscaRepresentation, ResponseFormat> fetchToscaRepresentation(Component component, boolean getFromCS,
                                                                                 ArtifactDefinition artifactDef) {
        LifecycleStateEnum lifecycleState = component.getLifecycleState();
        boolean shouldBeFetchedFromCassandra =
            getFromCS || !(lifecycleState == LifecycleStateEnum.NOT_CERTIFIED_CHECKIN || lifecycleState == LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        Either<ToscaRepresentation, ResponseFormat> toscaRepresentation =
            shouldBeFetchedFromCassandra ? fetchToscaRepresentation(artifactDef) : generateToscaRepresentation(component);
        return toscaRepresentation.left()
            .bind(iff(myd -> !myd.getDependencies().isDefined(), myd -> fetchToscaTemplateDependencies(myd.getMainYaml(), component)));
    }

    private Either<ToscaRepresentation, ResponseFormat> fetchToscaTemplateDependencies(byte[] mainYml, Component component) {
        return toscaExportUtils.getDependencies(component).right().map(toscaError -> {
            log.debug("Failed to retrieve dependencies for component {}, error {}", component.getUniqueId(), toscaError);
            return componentsUtils.getResponseFormat(componentsUtils.convertFromToscaError(toscaError));
        }).left().map(tt -> ToscaRepresentation.make(mainYml, tt));
    }

    private Either<ToscaRepresentation, ResponseFormat> generateToscaRepresentation(Component component) {
        return toscaExportUtils.exportComponent(component).right().map(toscaError -> {
            log.debug("exportComponent failed {}", toscaError);
            return componentsUtils.getResponseFormat(componentsUtils.convertFromToscaError(toscaError));
        });
    }

    private Either<ToscaRepresentation, ResponseFormat> fetchToscaRepresentation(ArtifactDefinition artifactDef) {
        return getFromCassandra(artifactDef.getEsId()).right().map(as -> {
            log.debug(ARTIFACT_NAME_UNIQUE_ID, artifactDef.getArtifactName(), artifactDef.getUniqueId());
            return componentsUtils.getResponseFormat(as);
        }).left().map(ToscaRepresentation::make);
    }

    /**
     * Create a list of all derived nodes found on the package
     *
     * @param dependencies all node dependencies
     * @return a list of nodes
     */
    private List<String> findNonRootNodesFromPackage(final List<Triple<String, String, Component>> dependencies) {
        final List<String> nodes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dependencies)) {
            final String NATIVE_ROOT = "tosca.nodes.Root";
            dependencies.forEach(dependency -> {
                if (dependency.getRight() instanceof Resource) {
                    final Resource resource = (Resource) dependency.getRight();
                    if (CollectionUtils.isNotEmpty(resource.getDerivedList())) {
                        resource.getDerivedList().stream().filter(node -> !nodes.contains(node) && !NATIVE_ROOT.equalsIgnoreCase(node))
                            .forEach(node -> nodes.add(node));
                    }
                }
            });
        }
        return nodes;
    }

    /**
     * Writes a new zip entry
     *
     * @param zipInputStream the zip entry to be read
     * @return a map of the given zip entry
     */
    private Map<String, Object> readYamlZipEntry(final ZipInputStream zipInputStream) throws IOException {
        final int initSize = 2048;
        final StringBuilder zipEntry = new StringBuilder();
        final byte[] buffer = new byte[initSize];
        int read = 0;
        while ((read = zipInputStream.read(buffer, 0, initSize)) >= 0) {
            zipEntry.append(new String(buffer, 0, read));
        }
        return (Map<String, Object>) new Yaml().load(zipEntry.toString());
    }

    /**
     * Filters and removes all duplicated nodes found
     *
     * @param nodesFromPackage      a List of all derived nodes found on the given package
     * @param nodesFromArtifactFile represents the nodes.yml file stored in Cassandra
     * @return a nodes Map updated
     */
    private Map<String, Object> updateNodeYml(final List<String> nodesFromPackage, final Map<String, Object> nodesFromArtifactFile) {
        if (MapUtils.isNotEmpty(nodesFromArtifactFile)) {
            final String nodeTypeBlock = ToscaTagNamesEnum.NODE_TYPES.getElementName();
            final Map<String, Object> nodeTypes = (Map<String, Object>) nodesFromArtifactFile.get(nodeTypeBlock);
            nodesFromPackage.stream().filter(nodeTypes::containsKey).forEach(nodeTypes::remove);
            nodesFromArtifactFile.replace(nodeTypeBlock, nodeTypes);
        }
        return nodesFromArtifactFile;
    }

    /**
     * Updates the zip entry from the given parameters
     *
     * @param byteArrayOutputStream an output stream in which the data is written into a byte array.
     * @param nodesYaml             a Map of nodes to be written
     */
    private void updateZipEntry(final ByteArrayOutputStream byteArrayOutputStream, final Map<String, Object> nodesYaml) throws IOException {
        if (MapUtils.isNotEmpty(nodesYaml)) {
            byteArrayOutputStream.write(new YamlUtil().objectToYaml(nodesYaml).getBytes());
        }
    }

    private Either<ZipOutputStream, ResponseFormat> getZipOutputStreamResponseFormatEither(ZipOutputStream zip,
                                                                                           List<Triple<String, String, Component>> dependencies)
        throws IOException {
        ComponentCache innerComponentsCache = ComponentCache.overwritable(overwriteIfSameVersions()).onMerge((oldValue, newValue) -> {
            log.warn("Overwriting component invariantID {} of version {} with a newer version {}", oldValue.id, oldValue.getComponentVersion(),
                newValue.getComponentVersion());
        });
        if (dependencies != null && !dependencies.isEmpty()) {
            for (Triple<String, String, Component> d : dependencies) {
                String cassandraId = d.getMiddle();
                Component childComponent = d.getRight();
                Either<byte[], ResponseFormat> entryData = getEntryData(cassandraId, childComponent).right()
                    .map(x -> componentsUtils.getResponseFormat(x));
                if (entryData.isRight()) {
                    return Either.right(entryData.right().value());
                }
                //fill innerComponentsCache
                String fileName = d.getLeft();
                innerComponentsCache.put(cassandraId, fileName, childComponent);
                addInnerComponentsToCache(innerComponentsCache, childComponent);
            }
            //add inner components to CSAR
            return addInnerComponentsToCSAR(zip, innerComponentsCache);
        }
        return null;
    }

    private Either<ZipOutputStream, ResponseFormat> addInnerComponentsToCSAR(ZipOutputStream zip, ComponentCache innerComponentsCache)
        throws IOException {
        for (ImmutableTriple<String, String, Component> ict : innerComponentsCache.iterable()) {
            Component innerComponent = ict.getRight();
            String icFileName = ict.getMiddle();
            // add component to zip
            Either<Tuple2<byte[], ZipEntry>, ResponseFormat> zipEntry = toZipEntry(ict);
            // TODO: this should not be done, we should instead compose this either further,

            // but in order to keep this refactoring small, we'll stop here.
            if (zipEntry.isRight()) {
                return Either.right(zipEntry.right().value());
            }
            Tuple2<byte[], ZipEntry> value = zipEntry.left().value();
            zip.putNextEntry(value._2);
            zip.write(value._1);
            // add component interface to zip
            if (hasToWriteComponentSubstitutionType(innerComponent)) {
                writeComponentInterface(innerComponent, zip, icFileName);
            }
        }
        return null;
    }

    private boolean hasToWriteComponentSubstitutionType(final Component component) {
        if (component instanceof Service) {
            return !ModelConverter.isAtomicComponent(component) && ((Service) component).isSubstituteCandidate();
        }
        return !ModelConverter.isAtomicComponent(component);
    }

    private Either<Tuple2<byte[], ZipEntry>, ResponseFormat> toZipEntry(ImmutableTriple<String, String, Component> cachedEntry) {
        String cassandraId = cachedEntry.getLeft();
        String fileName = cachedEntry.getMiddle();
        Component innerComponent = cachedEntry.getRight();
        return getEntryData(cassandraId, innerComponent).right().map(status -> {
            log.debug("Failed adding to zip component {}, error {}", cassandraId, status);
            return componentsUtils.getResponseFormat(status);
        }).left().map(content -> new Tuple2<>(content, new ZipEntry(DEFINITIONS_PATH + fileName)));
    }

    /**
     * Writes to a CSAR zip from casandra schema data
     *
     * @param zipOutputStream  stores the input stream content
     * @param schemaFileZip    zip data from Cassandra
     * @param nodesFromPackage list of all nodes found on the onboarded package
     */
    private void addSchemaFilesFromCassandra(final ZipOutputStream zipOutputStream, final byte[] schemaFileZip, final List<String> nodesFromPackage) {
        final int initSize = 2048;
        log.debug("Starting copy from Schema file zip to CSAR zip");
        try (final ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(
            schemaFileZip)); final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
            byteArrayOutputStream, initSize)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                ZipUtils.checkForZipSlipInRead(entry);
                final String entryName = entry.getName();
                int readSize = initSize;
                final byte[] entryData = new byte[initSize];
                if (shouldZipEntryBeHandled(entryName)) {
                    if (NODES_YML.equalsIgnoreCase(entryName)) {
                        handleNode(zipInputStream, byteArrayOutputStream, nodesFromPackage);
                    } else {
                        while ((readSize = zipInputStream.read(entryData, 0, readSize)) != -1) {
                            bufferedOutputStream.write(entryData, 0, readSize);
                        }
                        bufferedOutputStream.flush();
                    }
                    byteArrayOutputStream.flush();
                    zipOutputStream.putNextEntry(new ZipEntry(DEFINITIONS_PATH + entryName));
                    zipOutputStream.write(byteArrayOutputStream.toByteArray());
                    zipOutputStream.flush();
                    byteArrayOutputStream.reset();
                }
            }
        } catch (final Exception e) {
            log.error("Error while writing the SDC schema file to the CSAR", e);
            throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        log.debug("Finished copy from Schema file zip to CSAR zip");
    }

    /**
     * Checks if the zip entry should or should not be added to the CSAR based on the given global type list
     *
     * @param entryName the zip entry name
     * @return true if the zip entry should be handled
     */
    private boolean shouldZipEntryBeHandled(final String entryName) {
        return ConfigurationManager.getConfigurationManager().getConfiguration().getGlobalCsarImports().stream()
            .anyMatch(entry -> entry.contains(entryName));
    }

    /**
     * Handles the nodes.yml zip entry, updating the nodes.yml to avoid duplicated nodes on it.
     *
     * @param zipInputStream        the zip entry to be read
     * @param byteArrayOutputStream an output stream in which the data is written into a byte array.
     * @param nodesFromPackage      list of all nodes found on the onboarded package
     */
    private void handleNode(final ZipInputStream zipInputStream, final ByteArrayOutputStream byteArrayOutputStream,
                            final List<String> nodesFromPackage) throws IOException {
        final Map<String, Object> nodesFromArtifactFile = readYamlZipEntry(zipInputStream);
        final Map<String, Object> nodesYaml = updateNodeYml(nodesFromPackage, nodesFromArtifactFile);
        updateZipEntry(byteArrayOutputStream, nodesYaml);
    }

    private void addInnerComponentsToCache(ComponentCache componentCache, Component childComponent) {
        javaListToVavrList(childComponent.getComponentInstances()).filter(ci -> componentCache.notCached(ci.getComponentUid())).forEach(ci -> {
            // all resource must be only once!
            Either<Resource, StorageOperationStatus> resource = toscaOperationFacade.getToscaElement(ci.getComponentUid());
            Component componentRI = checkAndAddComponent(componentCache, ci, resource);
            //if not atomic - insert inner components as well

            // TODO: This could potentially create a StackOverflowException if the call stack

            // happens to be too large. Tail-recursive optimization should be used here.
            if (!ModelConverter.isAtomicComponent(componentRI)) {
                addInnerComponentsToCache(componentCache, componentRI);
            }
        });
    }

    // TODO: Move this function in FJToVavrHelper.java once Change 108540 is merged
    private io.vavr.collection.List<ComponentInstance> javaListToVavrList(List<ComponentInstance> componentInstances) {
        return Option.of(componentInstances).map(io.vavr.collection.List::ofAll).getOrElse(io.vavr.collection.List::empty);
    }

    private Component checkAndAddComponent(ComponentCache componentCache, ComponentInstance ci, Either<Resource, StorageOperationStatus> resource) {
        if (resource.isRight()) {
            log.debug("Failed to fetch resource with id {} for instance {}", ci.getComponentUid(), ci.getName());
        }
        Component componentRI = resource.left().value();
        Map<String, ArtifactDefinition> childToscaArtifacts = componentRI.getToscaArtifacts();
        ArtifactDefinition childArtifactDefinition = childToscaArtifacts.get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE);
        if (childArtifactDefinition != null) {
            //add to cache
            componentCache.put(childArtifactDefinition.getEsId(), childArtifactDefinition.getArtifactName(), componentRI);
        }
        return componentRI;
    }

    private List<Triple<String, String, Component>> writeComponentInterface(final Component component, final ZipOutputStream zip,
                                                                            final String fileName) {
        final Either<ToscaRepresentation, ToscaError> interfaceRepresentation = toscaExportUtils.exportComponentInterface(component, false);
        writeComponentInterface(interfaceRepresentation, zip, fileName);
        return interfaceRepresentation.left().value().getDependencies().getOrElse(new ArrayList<>());
    }


    private Either<ZipOutputStream, ResponseFormat> writeComponentInterface(Either<ToscaRepresentation, ToscaError> interfaceRepresentation,
                                                                            ZipOutputStream zip, String fileName) {
        // TODO: This should not be done but we need this to keep the refactoring small enough to be easily reviewable
        return writeComponentInterface(interfaceRepresentation, fileName, ZipWriter.live(zip))
            .map(void0 -> Either.<ZipOutputStream, ResponseFormat>left(zip)).recover(th -> {
                log.error("#writeComponentInterface - zip writing failed with error: ", th);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            }).get();
    }

    private Try<Void> writeComponentInterface(
        Either<ToscaRepresentation, ToscaError> interfaceRepresentation, String fileName, ZipWriter zw) {
        Either<byte[], ToscaError> yml = interfaceRepresentation.left()
            .map(ToscaRepresentation::getMainYaml);
        return fromEither(yml, ToscaErrorException::new).flatMap(zw.write(DEFINITIONS_PATH + ToscaExportHandler.getInterfaceFilename(fileName)));
    }

    private Either<byte[], ActionStatus> getEntryData(String cassandraId, Component childComponent) {
        if (cassandraId == null || cassandraId.isEmpty()) {
            return toscaExportUtils.exportComponent(childComponent).right().map(toscaErrorToActionStatus(childComponent)).left()
                .map(ToscaRepresentation::getMainYaml);
        } else {
            return getFromCassandra(cassandraId);
        }
    }

    private F<ToscaError, ActionStatus> toscaErrorToActionStatus(Component childComponent) {
        return toscaError -> {
            log.debug("Failed to export tosca template for child component {} error {}", childComponent.getUniqueId(), toscaError);
            return componentsUtils.convertFromToscaError(toscaError);
        };
    }

    private Either<byte[], ResponseFormat> getLatestSchemaFilesFromCassandra() {
        String fto = getVersionFirstThreeOctets();
        return sdcSchemaFilesCassandraDao.getSpecificSchemaFiles(fto, CONFORMANCE_LEVEL).right().map(schemaFilesFetchDBError(fto)).left()
            .bind(iff(List::isEmpty, () -> schemaFileFetchError(fto), s -> Either.left(s.iterator().next().getPayloadAsArray())));
    }

    private void addSchemaFilesByModel(final ZipOutputStream zipOutputStream, final String modelName) {
        try {
            final List<ToscaImportByModel> modelDefaultImportList = modelOperation.findAllModelImports(modelName, true);
            final Set<Path> writtenEntryPathList = new HashSet<>();
            final var definitionsPath = Path.of(DEFINITIONS_PATH);
            for (final ToscaImportByModel toscaImportByModel : modelDefaultImportList) {
                var importPath = Path.of(toscaImportByModel.getFullPath());
                if (writtenEntryPathList.contains(definitionsPath.resolve(importPath))) {
                    importPath =
                        ToscaDefaultImportHelper.addModelAsFilePrefix(importPath, toscaImportByModel.getModelId());
                }
                final Path entryPath = definitionsPath.resolve(importPath);
                final var zipEntry = new ZipEntry(entryPath.toString());
                zipOutputStream.putNextEntry(zipEntry);
                writtenEntryPathList.add(entryPath);
                final byte[] content = toscaImportByModel.getContent().getBytes(StandardCharsets.UTF_8);
                zipOutputStream.write(content, 0, content.length);
                zipOutputStream.closeEntry();
            }
        } catch (final IOException e) {
            log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, CsarUtils.class.getName(),
                "Error while writing the schema files by model to the CSAR", e);
            throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(ActionStatus.CSAR_TOSCA_IMPORTS_ERROR));
        }
    }

    private F<CassandraOperationStatus, ResponseFormat> schemaFilesFetchDBError(String firstThreeOctets) {
        return cos -> {
            log.debug("Failed to get the schema files SDC-Version: {} Conformance-Level {}. Please fix DB table accordingly.", firstThreeOctets,
                CONFORMANCE_LEVEL);
            StorageOperationStatus sos = DaoStatusConverter.convertCassandraStatusToStorageStatus(cos);
            return componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(sos));
        };
    }

    private Either<byte[], ResponseFormat> schemaFileFetchError(String firstThreeOctets) {
        log.debug("Failed to get the schema files SDC-Version: {} Conformance-Level {}", firstThreeOctets, CONFORMANCE_LEVEL);
        return Either.right(componentsUtils.getResponseFormat(ActionStatus.TOSCA_SCHEMA_FILES_NOT_FOUND, firstThreeOctets, CONFORMANCE_LEVEL));
    }

    private Either<byte[], ActionStatus> getFromCassandra(String cassandraId) {
        return artifactCassandraDao.getArtifact(cassandraId).right().map(cos -> {
            log.debug("Failed to fetch artifact from Cassandra by id {} error {} ", cassandraId, cos);
            StorageOperationStatus storageStatus = DaoStatusConverter.convertCassandraStatusToStorageStatus(cos);
            return componentsUtils.convertFromStorageResponse(storageStatus);
        }).left().map(DAOArtifactData::getDataAsArray);
    }

    private String createCsarBlock0(String metaFileVersion, String toscaConformanceLevel) {
        return String.format(BLOCK_0_TEMPLATE, metaFileVersion, toscaConformanceLevel);
    }

    private String createToscaBlock0(String metaFileVersion, String csarVersion, String createdBy, String entryDef, boolean isAsdPackage) {
        final String block0template = "TOSCA-Meta-File-Version: %s\nCSAR-Version: %s\nCreated-By: %s\nEntry-Definitions: Definitions/%s\n%s\nName: csar.meta\nContent-Type: text/plain\n";
        return String.format(block0template, metaFileVersion, csarVersion, createdBy, entryDef, isAsdPackage ? "entry_definition_type: asd" : "");
    }

    private String createNsMfBlock0(String serviceName, String createdBy, String serviceVersion, String releaseTime, String serviceType,
                                    String description, String serviceTemplate, String hash) {
        final String block0template = "metadata??\n" + "ns_product_name: %s\n" + "ns_provider_id: %s\n" + "ns_package_version: %s\n" +
            //"ns_create_date_time: %s\n" +
            "ns_release_data_time: %s\n" + "ns_type: %s\n" + "ns_package_description: %s\n\n" + "Source: %s\n" + "Algorithm: MD5\n" + "Hash: %s\n\n";
        return String.format(block0template, serviceName, createdBy, serviceVersion, releaseTime, serviceType, description, serviceTemplate, hash);
    }

    private Either<ZipOutputStream, ResponseFormat> writeAllFilesToCsar(Component mainComponent, CsarDefinition csarDefinition,
                                                                        ZipOutputStream zipstream, boolean isInCertificationRequest)
        throws IOException {
        ComponentArtifacts componentArtifacts = csarDefinition.getComponentArtifacts();
        Either<ZipOutputStream, ResponseFormat> writeComponentArtifactsToSpecifiedPath = writeComponentArtifactsToSpecifiedPath(mainComponent,
            componentArtifacts, zipstream, ARTIFACTS_PATH, isInCertificationRequest);
        if (writeComponentArtifactsToSpecifiedPath.isRight()) {
            return Either.right(writeComponentArtifactsToSpecifiedPath.right().value());
        }
        ComponentTypeArtifacts mainTypeAndCIArtifacts = componentArtifacts.getMainTypeAndCIArtifacts();
        writeComponentArtifactsToSpecifiedPath = writeArtifactsInfoToSpecifiedPath(mainComponent, mainTypeAndCIArtifacts.getComponentArtifacts(),
            zipstream, ARTIFACTS_PATH, isInCertificationRequest);
        if (writeComponentArtifactsToSpecifiedPath.isRight()) {
            return Either.right(writeComponentArtifactsToSpecifiedPath.right().value());
        }
        Map<String, ArtifactsInfo> componentInstancesArtifacts = mainTypeAndCIArtifacts.getComponentInstancesArtifacts();
        Set<String> keySet = componentInstancesArtifacts.keySet();
        String currentPath = ARTIFACTS_PATH + RESOURCES_PATH;
        for (String keyAssetName : keySet) {
            ArtifactsInfo artifactsInfo = componentInstancesArtifacts.get(keyAssetName);
            String pathWithAssetName = currentPath + keyAssetName + PATH_DELIMITER;
            writeComponentArtifactsToSpecifiedPath = writeArtifactsInfoToSpecifiedPath(mainComponent, artifactsInfo, zipstream, pathWithAssetName,
                isInCertificationRequest);
            if (writeComponentArtifactsToSpecifiedPath.isRight()) {
                return Either.right(writeComponentArtifactsToSpecifiedPath.right().value());
            }
        }
        writeComponentArtifactsToSpecifiedPath = writeOperationsArtifactsToCsar(mainComponent, zipstream);
        if (writeComponentArtifactsToSpecifiedPath.isRight()) {
            return Either.right(writeComponentArtifactsToSpecifiedPath.right().value());
        }
        return Either.left(zipstream);
    }

    private Either<ZipOutputStream, ResponseFormat> writeOperationsArtifactsToCsar(Component component, ZipOutputStream zipstream) {
        if (checkComponentBeforeOperation(component)) {
            return Either.left(zipstream);
        }
        final Map<String, InterfaceDefinition> interfaces = ((Resource) component).getInterfaces();
        for (Map.Entry<String, InterfaceDefinition> interfaceEntry : interfaces.entrySet()) {
            for (OperationDataDefinition operation : interfaceEntry.getValue().getOperations().values()) {
                try {
                    if (checkComponentBeforeWrite(component, interfaceEntry, operation)) {
                        continue;
                    }
                    final String artifactUUID = operation.getImplementation().getArtifactUUID();
                    if (artifactUUID == null) {
                        continue;
                    }
                    final Either<byte[], ActionStatus> artifactFromCassandra = getFromCassandra(artifactUUID);
                    final String artifactName = operation.getImplementation().getArtifactName();
                    if (artifactFromCassandra.isRight()) {
                        log.error(ARTIFACT_NAME_UNIQUE_ID, artifactName, artifactUUID);
                        log.error("Failed to get {} payload from DB reason: {}", artifactName, artifactFromCassandra.right().value());
                        return Either.right(componentsUtils
                            .getResponseFormat(ActionStatus.ARTIFACT_PAYLOAD_NOT_FOUND_DURING_CSAR_CREATION, "Resource", component.getUniqueId(),
                                artifactName, artifactUUID));
                    }
                    final byte[] payloadData = artifactFromCassandra.left().value();
                    zipstream.putNextEntry(new ZipEntry(OperationArtifactUtil.createOperationArtifactPath(component, null, operation, true)));
                    zipstream.write(payloadData);
                } catch (IOException e) {
                    log.error("Component Name {},  Interface Name {}, Operation Name {}", component.getNormalizedName(), interfaceEntry.getKey(),
                        operation.getName());
                    log.error("Error while writing the operation's artifacts to the CSAR " + "{}", e);
                    return Either
                        .right(componentsUtils.getResponseFormat(ActionStatus.ERROR_DURING_CSAR_CREATION, "Resource", component.getUniqueId()));
                }
            }
        }
        return Either.left(zipstream);
    }

    private boolean checkComponentBeforeWrite(Component component, Entry<String, InterfaceDefinition> interfaceEntry,
                                              OperationDataDefinition operation) {
        final ArtifactDataDefinition implementation = operation.getImplementation();
        if (Objects.isNull(implementation)) {
            log.debug("Component Name {}, Interface Id {}, Operation Name {} - no Operation Implementation found", component.getNormalizedName(),
                interfaceEntry.getValue().getUniqueId(), operation.getName());
            return true;
        }
        final String artifactName = implementation.getArtifactName();
        if (Objects.isNull(artifactName)) {
            log.debug("Component Name {}, Interface Id {}, Operation Name {} - no artifact found", component.getNormalizedName(),
                interfaceEntry.getValue().getUniqueId(), operation.getName());
            return true;
        }
        if (OperationArtifactUtil.artifactNameIsALiteralValue(artifactName)) {
            log.debug("Component Name {}, Interface Id {}, Operation Name {} - artifact name is a literal value rather than an SDC artifact",
                component.getNormalizedName(), interfaceEntry.getValue().getUniqueId(), operation.getName());
            return true;
        }
        return false;
    }

    private boolean checkComponentBeforeOperation(Component component) {
        if (component instanceof Service) {
            return true;
        }
        if (Objects.isNull(((Resource) component).getInterfaces())) {
            log.debug("Component Name {}- no interfaces found", component.getNormalizedName());
            return true;
        }
        return false;
    }

    private Either<ZipOutputStream, ResponseFormat> writeComponentArtifactsToSpecifiedPath(Component mainComponent,
                                                                                           ComponentArtifacts componentArtifacts,
                                                                                           ZipOutputStream zipstream, String currentPath,
                                                                                           boolean isInCertificationRequest) throws IOException {
        Map<String, ComponentTypeArtifacts> componentTypeArtifacts = componentArtifacts.getComponentTypeArtifacts();
        //Keys are defined:

        //<Inner Asset TOSCA name (e.g. VFC name)> folder name: <Inner Asset TOSCA name (e.g. VFC name)>_v<version>.

        //E.g. "org.openecomp.resource.vf.vipr_atm_v1.0"
        Set<String> componentTypeArtifactsKeys = componentTypeArtifacts.keySet();
        for (String keyAssetName : componentTypeArtifactsKeys) {
            ComponentTypeArtifacts componentInstanceArtifacts = componentTypeArtifacts.get(keyAssetName);
            ArtifactsInfo componentArtifacts2 = componentInstanceArtifacts.getComponentArtifacts();
            String pathWithAssetName = currentPath + keyAssetName + PATH_DELIMITER;
            Either<ZipOutputStream, ResponseFormat> writeArtifactsInfoToSpecifiedPath = writeArtifactsInfoToSpecifiedPath(mainComponent,
                componentArtifacts2, zipstream, pathWithAssetName, isInCertificationRequest);
            if (writeArtifactsInfoToSpecifiedPath.isRight()) {
                return writeArtifactsInfoToSpecifiedPath;
            }
        }
        return Either.left(zipstream);
    }

    private Either<ZipOutputStream, ResponseFormat> writeArtifactsInfoToSpecifiedPath(final Component mainComponent,
                                                                                      final ArtifactsInfo currArtifactsInfo,
                                                                                      final ZipOutputStream zip, final String path,
                                                                                      final boolean isInCertificationRequest) throws IOException {
        final Map<ArtifactGroupTypeEnum, Map<String, List<ArtifactDefinition>>> artifactsInfo = currArtifactsInfo.getArtifactsInfo();
        for (final ArtifactGroupTypeEnum artifactGroupTypeEnum : artifactsInfo.keySet()) {
            final String groupTypeFolder = path + WordUtils.capitalizeFully(artifactGroupTypeEnum.getType()) + PATH_DELIMITER;
            final Map<String, List<ArtifactDefinition>> artifactTypesMap = artifactsInfo.get(artifactGroupTypeEnum);
            for (final String artifactType : artifactTypesMap.keySet()) {
                final List<ArtifactDefinition> artifactDefinitionList = artifactTypesMap.get(artifactType);
                String artifactTypeFolder = groupTypeFolder + artifactType + PATH_DELIMITER;
                if (ArtifactTypeEnum.WORKFLOW.getType().equals(artifactType) && path.contains(ARTIFACTS_PATH + RESOURCES_PATH)) {
                    // Ignore this packaging as BPMN artifacts needs to be packaged in different manner
                    continue;
                }
                if (ArtifactTypeEnum.WORKFLOW.getType().equals(artifactType)) {
                    artifactTypeFolder += OperationArtifactUtil.BPMN_ARTIFACT_PATH + File.separator;
                } else if (ArtifactTypeEnum.ONBOARDED_PACKAGE.getType().equals(artifactType)) {
                    // renaming legacy folder ONBOARDED_PACKAGE to the new folder ETSI_PACKAGE
                    artifactTypeFolder = artifactTypeFolder
                        .replace(ArtifactTypeEnum.ONBOARDED_PACKAGE.getType(), ArtifactTypeEnum.ETSI_PACKAGE.getType());
                }
                // TODO: We should not do this but in order to keep this refactoring small enough,

                // we'll leave this as is for now
                List<ArtifactDefinition> collect = filterArtifactDefinitionToZip(mainComponent, artifactDefinitionList, isInCertificationRequest)
                    .collect(Collectors.toList());
                for (ArtifactDefinition ad : collect) {
                    zip.putNextEntry(new ZipEntry(artifactTypeFolder + ad.getArtifactName()));
                    zip.write(ad.getPayloadData());
                }
            }
        }
        return Either.left(zip);
    }

    private Stream<ArtifactDefinition> filterArtifactDefinitionToZip(Component mainComponent, List<ArtifactDefinition> artifactDefinitionList,
                                                                     boolean isInCertificationRequest) {
        return artifactDefinitionList.stream().filter(shouldBeInZip(isInCertificationRequest, mainComponent)).map(this::fetchPayLoadData)
            .filter(Either::isLeft).map(e -> e.left().value());
    }

    private Predicate<ArtifactDefinition> shouldBeInZip(boolean isInCertificationRequest, Component component) {
        return artifactDefinition -> !(!isInCertificationRequest && component.isService() && artifactDefinition.isHeatEnvType() || artifactDefinition
            .hasNoMandatoryEsId());
    }

    private Either<ArtifactDefinition, ActionStatus> fetchPayLoadData(ArtifactDefinition ad) {
        byte[] payloadData = ad.getPayloadData();
        if (payloadData == null) {
            return getFromCassandra(ad.getEsId()).left().map(pd -> {
                ad.setPayload(pd);
                return ad;
            }).right().map(as -> {
                log.debug(ARTIFACT_NAME_UNIQUE_ID, ad.getArtifactName(), ad.getUniqueId());
                log.debug("Failed to get {} payload from DB reason: {}", ad.getArtifactName(), as);
                return as;
            });
        } else {
            return Either.left(ad);
        }
    }

    /************************************ Artifacts Structure END******************************************************************/

    private Either<CsarDefinition, ResponseFormat> collectComponentCsarDefinition(Component component) {
        ComponentArtifacts componentArtifacts = new ComponentArtifacts();
        Component updatedComponent = component;

        //get service to receive the AII artifacts uploaded to the service
        if (updatedComponent.getComponentType() == ComponentTypeEnum.SERVICE) {
            Either<Service, StorageOperationStatus> getServiceResponse = toscaOperationFacade.getToscaElement(updatedComponent.getUniqueId());

            if (getServiceResponse.isRight()) {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getServiceResponse.right().value());
                return Either.right(componentsUtils.getResponseFormat(actionStatus));
            }

            updatedComponent = getServiceResponse.left().value();
        }

        //find the artifacts of the main component, it would have its composed instances artifacts in a separate folder
        ComponentTypeArtifacts componentInstanceArtifacts = new ComponentTypeArtifacts();
        ArtifactsInfo artifactsInfo = collectComponentArtifacts(updatedComponent);
        componentInstanceArtifacts.setComponentArtifacts(artifactsInfo);
        componentArtifacts.setMainTypeAndCIArtifacts(componentInstanceArtifacts);

        Map<String, ComponentTypeArtifacts> resourceTypeArtifacts = componentArtifacts
            .getComponentTypeArtifacts();    //artifacts mapped by the component type(tosca name+version)
        //get the component instances
        List<ComponentInstance> componentInstances = updatedComponent.getComponentInstances();
        if (componentInstances != null) {
            for (ComponentInstance componentInstance : componentInstances) {
                //call recursive to find artifacts for all the path
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
        for (Map.Entry<String, ComponentTypeArtifacts> typeArtifacts : componentArtifacts.getComponentTypeArtifacts().entrySet()) {
            result.append("Folder " + typeArtifacts.getKey() + "\n");
            result.append(printArtifacts(typeArtifacts.getValue()));
        }

        if (log.isDebugEnabled()) {
            log.debug(result.toString());
        }
    }

    /************************************ Artifacts Structure ******************************************************************/

    private String printArtifacts(ComponentTypeArtifacts componentInstanceArtifacts) {
        StringBuilder result = new StringBuilder();
        ArtifactsInfo artifactsInfo = componentInstanceArtifacts.getComponentArtifacts();
        Map<ArtifactGroupTypeEnum, Map<String, List<ArtifactDefinition>>> componentArtifacts = artifactsInfo.getArtifactsInfo();
        printArtifacts(componentArtifacts);
        result = result.append("Resources\n");
        for (Map.Entry<String, ArtifactsInfo> resourceInstance : componentInstanceArtifacts.getComponentInstancesArtifacts().entrySet()) {
            result.append("Folder" + resourceInstance.getKey() + "\n");
            result.append(printArtifacts(resourceInstance.getValue().getArtifactsInfo()));
        }

        return result.toString();
    }

    private String printArtifacts(Map<ArtifactGroupTypeEnum, Map<String, List<ArtifactDefinition>>> componetArtifacts) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<ArtifactGroupTypeEnum, Map<String, List<ArtifactDefinition>>> artifactGroup : componetArtifacts.entrySet()) {
            result.append("    " + artifactGroup.getKey().getType());
            for (Map.Entry<String, List<ArtifactDefinition>> groupArtifacts : artifactGroup.getValue().entrySet()) {
                result.append("        " + groupArtifacts.getKey());
                for (ArtifactDefinition artifact : groupArtifacts.getValue()) {
                    result.append("            " + artifact.getArtifactDisplayName());
                }
            }
        }

        return result.toString();
    }

    private ComponentTypeArtifacts collectComponentTypeArtifacts(
        Component fetchedComponent
    ) {
        ArtifactsInfo componentArtifacts = collectComponentArtifacts(fetchedComponent);
        ComponentTypeArtifacts componentArtifactsInfo = new ComponentTypeArtifacts();
        if (componentArtifacts.isNotEmpty()) {
            componentArtifactsInfo.setComponentArtifacts(componentArtifacts);
        }
        return componentArtifactsInfo;
    }

    private Either<Boolean, ResponseFormat> collectComponentInstanceArtifacts(Component parentComponent, ComponentInstance componentInstance,
                                                                              Map<String, ComponentTypeArtifacts> resourcesTypeArtifacts,
                                                                              ComponentTypeArtifacts instanceArtifactsLocation) {
        //1. get the component instance component
        String componentUid;
        if (componentInstance.getOriginType() == OriginTypeEnum.ServiceProxy) {
            componentUid = componentInstance.getSourceModelUid();
        } else {
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

        //2. fill the artifacts for the current component parent type
        String toscaComponentName =
            componentInstance.getToscaComponentName() + "_v" + componentInstance.getComponentVersion();

        // if there are no artifacts for this component type we need to fetch and build them
        ComponentTypeArtifacts componentParentArtifacts = Optional
            .ofNullable(resourcesTypeArtifacts.get(toscaComponentName))
            .orElseGet(() -> collectComponentTypeArtifacts(fetchedComponent));

        if (componentParentArtifacts.getComponentArtifacts().isNotEmpty()) {
            resourcesTypeArtifacts.put(toscaComponentName, componentParentArtifacts);
        }

        //3. find the artifacts specific to the instance
        Map<String, List<ArtifactDefinition>> componentInstanceSpecificInformationalArtifacts =
            getComponentInstanceSpecificArtifacts(componentInstance.getArtifacts(),
                componentParentArtifacts.getComponentArtifacts().getArtifactsInfo(), ArtifactGroupTypeEnum.INFORMATIONAL);
        Map<String, List<ArtifactDefinition>> componentInstanceSpecificDeploymentArtifacts =
            getComponentInstanceSpecificArtifacts(componentInstance.getDeploymentArtifacts(),
                componentParentArtifacts.getComponentArtifacts().getArtifactsInfo(), ArtifactGroupTypeEnum.DEPLOYMENT);

        //4. add the instances artifacts to the component type
        ArtifactsInfo artifactsInfo = new ArtifactsInfo();
        if (!componentInstanceSpecificInformationalArtifacts.isEmpty()) {
            artifactsInfo.addArtifactsToGroup(ArtifactGroupTypeEnum.INFORMATIONAL, componentInstanceSpecificInformationalArtifacts);
        }
        if (!componentInstanceSpecificDeploymentArtifacts.isEmpty()) {
            artifactsInfo.addArtifactsToGroup(ArtifactGroupTypeEnum.DEPLOYMENT, componentInstanceSpecificDeploymentArtifacts);
        }
        if (!artifactsInfo.isEmpty()) {
            instanceArtifactsLocation.addComponentInstancesArtifacts(componentInstance.getNormalizedName(), artifactsInfo);
        }

        //5. do the same for all the component instances
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

    public String getVersionFirstThreeOctets() {
        return versionFirstThreeOctets;
    }

    private Map<String, List<ArtifactDefinition>> getComponentInstanceSpecificArtifacts(Map<String, ArtifactDefinition> componentArtifacts,
                                                                                        Map<ArtifactGroupTypeEnum, Map<String, List<ArtifactDefinition>>> componentTypeArtifacts,
                                                                                        ArtifactGroupTypeEnum artifactGroupTypeEnum) {
        Map<String, List<ArtifactDefinition>> parentArtifacts = componentTypeArtifacts
            .get(artifactGroupTypeEnum);    //the artfiacts of the component itself and not the instance

        Map<String, List<ArtifactDefinition>> artifactsByTypeOfComponentInstance = new HashMap<>();
        if (componentArtifacts != null) {
            for (ArtifactDefinition artifact : componentArtifacts.values()) {
                List<ArtifactDefinition> parentArtifactsByType = null;
                if (parentArtifacts != null) {
                    parentArtifactsByType = parentArtifacts.get(artifact.getArtifactType());
                }
                //the artifact is of instance
                if (parentArtifactsByType == null || !parentArtifactsByType.contains(artifact)) {
                    List<ArtifactDefinition> typeArtifacts = artifactsByTypeOfComponentInstance.get(artifact.getArtifactType());
                    if (typeArtifacts == null) {
                        typeArtifacts = new ArrayList<>();
                        artifactsByTypeOfComponentInstance.put(artifact.getArtifactType(), typeArtifacts);
                    }
                    typeArtifacts.add(artifact);
                }
            }
        }

        return artifactsByTypeOfComponentInstance;
    }

    private ArtifactsInfo collectComponentArtifacts(Component component) {
        Map<String, ArtifactDefinition> informationalArtifacts = component.getArtifacts();
        Map<String, List<ArtifactDefinition>> informationalArtifactsByType = collectGroupArtifacts(informationalArtifacts);
        Map<String, ArtifactDefinition> deploymentArtifacts = component.getDeploymentArtifacts();
        Map<String, List<ArtifactDefinition>> deploymentArtifactsByType = collectGroupArtifacts(deploymentArtifacts);
        Map<String, ArtifactDefinition> interfaceOperationArtifacts =
            OperationArtifactUtil.getDistinctInterfaceOperationArtifactsByName(component);
        Map<String, List<ArtifactDefinition>> interfaceOperationArtifactsByType = collectGroupArtifacts(
            interfaceOperationArtifacts);
        ArtifactsInfo artifactsInfo = new ArtifactsInfo();
        if (!informationalArtifactsByType.isEmpty()) {
            artifactsInfo.addArtifactsToGroup(ArtifactGroupTypeEnum.INFORMATIONAL, informationalArtifactsByType);
        }
        if (!deploymentArtifactsByType.isEmpty()) {
            artifactsInfo.addArtifactsToGroup(ArtifactGroupTypeEnum.DEPLOYMENT, deploymentArtifactsByType);
        }

        return artifactsInfo;
    }

    private Map<String, List<ArtifactDefinition>> collectGroupArtifacts(
        final Map<String, ArtifactDefinition> componentArtifacts) {
        final Map<String, List<ArtifactDefinition>> artifactsByType = new HashMap<>();
        for (final ArtifactDefinition artifact : componentArtifacts.values()) {
            if (artifact.getArtifactUUID() != null) {
                artifactsByType.putIfAbsent(artifact.getArtifactType(), new ArrayList<>());
                final List<ArtifactDefinition> typeArtifacts = artifactsByType.get(artifact.getArtifactType());
                typeArtifacts.add(artifact);
            }
        }
        return artifactsByType;
    }

    public static class ToscaErrorException extends Exception {

        ToscaErrorException(ToscaError error) {
            super("Error while exporting component's interface (toscaError:" + error + ")");
        }
    }

    @Getter
    public static final class NonMetaArtifactInfo {

        private final String path;
        private final String artifactName;
        private final String displayName;
        private final String artifactLabel;
        private final String artifactType;
        private final ArtifactGroupTypeEnum artifactGroupType;
        private final String payloadData;
        private final String artifactChecksum;
        private final boolean isFromCsar;
        @Setter
        private String artifactUniqueId;

        public NonMetaArtifactInfo(final String artifactName, final String path, final String artifactType,
                                   final ArtifactGroupTypeEnum artifactGroupType, final byte[] payloadData, final String artifactUniqueId,
                                   final boolean isFromCsar) {
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
            if (payloadData == null) {
                this.payloadData = null;
                this.artifactChecksum = null;
            } else {
                this.payloadData = Base64.encodeBase64String(payloadData);
                this.artifactChecksum = GeneralUtility.calculateMD5Base64EncodedByByteArray(payloadData);
            }
            this.artifactUniqueId = artifactUniqueId;
        }
    }

    /**
     * The artifacts Definition saved by their structure
     */
    private class ArtifactsInfo {
        //Key is the type of artifacts(Informational/Deployment)

        //Value is a map between an artifact type and a list of all artifacts of this type
        private Map<ArtifactGroupTypeEnum, Map<String, List<ArtifactDefinition>>> artifactsInfoField;

        public ArtifactsInfo() {
            this.artifactsInfoField = new EnumMap<>(ArtifactGroupTypeEnum.class);
        }

        public Map<ArtifactGroupTypeEnum, Map<String, List<ArtifactDefinition>>> getArtifactsInfo() {
            return artifactsInfoField;
        }

        public void addArtifactsToGroup(ArtifactGroupTypeEnum artifactGroup, Map<String, List<ArtifactDefinition>> artifactsDefinition) {
            if (artifactsInfoField.get(artifactGroup) == null) {
                artifactsInfoField.put(artifactGroup, artifactsDefinition);
            } else {
                Map<String, List<ArtifactDefinition>> artifactTypeEnumListMap = artifactsInfoField.get(artifactGroup);
                artifactTypeEnumListMap.putAll(artifactsDefinition);
                artifactsInfoField.put(artifactGroup, artifactTypeEnumListMap);
            }
        }

        public boolean isEmpty() {
            return artifactsInfoField.isEmpty();
        }

        public boolean isNotEmpty() {
            return !isEmpty();
        }
    }

    /**
     * The artifacts of the component and of all its composed instances
     */
    private class ComponentTypeArtifacts {

        private ArtifactsInfo componentArtifacts;    //component artifacts (describes the Informational Deployment folders)

        private Map<String, ArtifactsInfo> componentInstancesArtifacts;        //artifacts of the composed instances mapped by the resourceInstance normalized name (describes the Resources folder)

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

        public void addComponentInstancesArtifacts(String normalizedName, ArtifactsInfo artifactsInfo) {
            componentInstancesArtifacts.put(normalizedName, artifactsInfo);
        }
    }

    private class ComponentArtifacts {

        //artifacts of the component and CI's artifacts contained in it's composition (represents Informational, Deployment & Resource folders of main component)
        private ComponentTypeArtifacts mainTypeAndCIArtifacts;
        //artifacts of all component types mapped by their tosca name
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
}

