/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2023 Nordix Foundation. All rights reserved.
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

import static org.openecomp.sdc.be.dao.api.ActionStatus.ARTIFACT_PAYLOAD_NOT_FOUND_DURING_CSAR_CREATION;
import static org.openecomp.sdc.be.dao.api.ActionStatus.ERROR_DURING_CSAR_CREATION;
import static org.openecomp.sdc.be.tosca.ComponentCache.MergeStrategy.overwriteIfSameVersions;
import static org.openecomp.sdc.be.tosca.FJToVavrHelper.Try0.fromEither;
import static org.openecomp.sdc.be.tosca.FJToVavrHelper.Try0.javaListToVavrList;
import static org.openecomp.sdc.common.api.Constants.ADDITIONAL_TYPE_DEFINITIONS;

import com.google.common.primitives.Bytes;
import fj.F;
import fj.data.Either;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.text.WordUtils;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.config.CategoryBaseTypeConfig;
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
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.plugins.CsarEntryGenerator;
import org.openecomp.sdc.be.resources.data.DAOArtifactData;
import org.openecomp.sdc.be.tosca.utils.OperationArtifactUtil;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;

/**
 * Generates a Network Service CSAR based on a SERVICE component and wraps it in a SDC CSAR entry.
 */
@org.springframework.stereotype.Component("commonCsarGenerator")
public class CommonCsarGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonCsarGenerator.class);
    public static final String ARTIFACTS_PATH = "Artifacts/";
    private static final String RESOURCES_PATH = "Resources/";
    private static final String PATH_DELIMITER = "/";
    private static final String SERVICE_MANIFEST = "NS.mf";
    private static final String ARTIFACT_NAME_UNIQUE_ID = "ArtifactName {}, unique ID {}";
    private static final String TOSCA_META_PATH_FILE_NAME = "TOSCA-Metadata/TOSCA.meta";
    private static final String TOSCA_META_VERSION = "1.0";
    private static final String CSAR_VERSION = "1.1";
    private static final String SDC_VERSION = ExternalConfiguration.getAppVersion();
    public static final String NODES_YML = "nodes.yml";
    private static final String CONFORMANCE_LEVEL = ConfigurationManager.getConfigurationManager().getConfiguration().getToscaConformanceLevel();
    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentsUtils;
    private final ToscaExportHandler toscaExportUtils;
    private final List<CsarEntryGenerator> generators;
    private final ArtifactCassandraDao artifactCassandraDao;
    private final String versionFirstThreeOctets;
    private final SdcSchemaFilesCassandraDao sdcSchemaFilesCassandraDao;
    private final ModelOperation modelOperation;

    @Autowired
    public CommonCsarGenerator(
        final ToscaOperationFacade toscaOperationFacade,
        final ComponentsUtils componentsUtils,
        final ToscaExportHandler toscaExportUtils,
        final List<CsarEntryGenerator> generators,
        final ArtifactCassandraDao artifactCassandraDao,
        final SdcSchemaFilesCassandraDao sdcSchemaFilesCassandraDao,
        final ModelOperation modelOperation) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
        this.toscaExportUtils = toscaExportUtils;
        this.generators = generators;
        this.artifactCassandraDao = artifactCassandraDao;
        this.versionFirstThreeOctets = readVersionFirstThreeOctets();
        this.sdcSchemaFilesCassandraDao = sdcSchemaFilesCassandraDao;
        this.modelOperation = modelOperation;
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

    /**
     * Generates a Network Service CSAR based on a SERVICE component that has category configured in
     * CategoriesToGenerateNsd enum and wraps it in a SDC CSAR entry.
     *
     * @param component the component to create the NS CSAR from
     * @return an entry to be added in the Component CSAR by SDC
     */

    public Either<ZipOutputStream, ResponseFormat> generateCsarZip(
        Component component,
        boolean getFromCS,
        ZipOutputStream zip,
        boolean isInCertificationRequest,
        String createdBy,
        String toscaMetaFileName,
        boolean isAsdPackage,
        String definitionsPath,
        boolean addDependencies
    ) throws IOException {
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
        final byte[] toscaBlock0Byte = createToscaBlock0(TOSCA_META_VERSION, CSAR_VERSION, createdBy, toscaMetaFileName, isAsdPackage, definitionsPath).getBytes();
        zip.putNextEntry(new ZipEntry(TOSCA_META_PATH_FILE_NAME));
        zip.write(toscaBlock0Byte);
        String fileName = artifactDef.getArtifactName();
        zip.putNextEntry(new ZipEntry(definitionsPath + fileName));
        zip.write(mainYaml);
        LifecycleStateEnum lifecycleState = component.getLifecycleState();
        addServiceMf(component, zip, lifecycleState, isInCertificationRequest, fileName, mainYaml, definitionsPath);
        if (addDependencies) {
            //US798487 - Abstraction of complex types
            if (hasToWriteComponentSubstitutionType(component)) {
                LOGGER.debug("Component {} is complex - generating abstract type for it..", component.getName());
                dependencies.addAll(writeComponentInterface(component, zip, fileName, definitionsPath));
            }
            //UID <cassandraId,filename,component>
            Either<ZipOutputStream, ResponseFormat> zipOutputStreamOrResponseFormat =
                getZipOutputStreamResponseFormatEither(zip, dependencies, definitionsPath);
            if (zipOutputStreamOrResponseFormat != null && zipOutputStreamOrResponseFormat.isRight()) {
                return zipOutputStreamOrResponseFormat;
            }
        }
        if (component.getModel() == null) {
            //retrieve SDC.zip from Cassandra
            Either<byte[], ResponseFormat> latestSchemaFiles = getLatestSchemaFilesFromCassandra();
            if (latestSchemaFiles.isRight()) {
                LOGGER.error("Error retrieving SDC Schema files from cassandra");
                return Either.right(latestSchemaFiles.right().value());
            }
            final byte[] schemaFileZip = latestSchemaFiles.left().value();
            final List<String> nodesFromPackage = findNonRootNodesFromPackage(dependencies);
            //add files from retrieved SDC.zip to Definitions folder in CSAR
            addSchemaFilesFromCassandra(zip, schemaFileZip, nodesFromPackage, definitionsPath);
        } else {
            //retrieve schema files by model from Cassandra
            addSchemaFilesByModel(zip, component.getModel(), definitionsPath, addDependencies);
        }
        Either<CsarDefinition, ResponseFormat> collectedComponentCsarDefinition = collectComponentCsarDefinition(component);
        if (collectedComponentCsarDefinition.isRight()) {
            return Either.right(collectedComponentCsarDefinition.right().value());
        }
        if (generators != null) {
            for (CsarEntryGenerator generator : generators) {
                LOGGER.debug("Invoking CsarEntryGenerator: {}", generator.getClass().getName());
                for (Map.Entry<String, byte[]> pluginGeneratedFile : generator.generateCsarEntries(component).entrySet()) {
                    zip.putNextEntry(new ZipEntry(pluginGeneratedFile.getKey()));
                    zip.write(pluginGeneratedFile.getValue());
                }
            }
        }
        return writeAllFilesToCsar(component, collectedComponentCsarDefinition.left().value(), zip, isInCertificationRequest);
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
            LOGGER.debug("Failed to retrieve dependencies for component {}, error {}", component.getUniqueId(), toscaError);
            return componentsUtils.getResponseFormat(componentsUtils.convertFromToscaError(toscaError));
        }).left().map(tt -> ToscaRepresentation.make(mainYml, tt));
    }

    private Either<ToscaRepresentation, ResponseFormat> fetchToscaRepresentation(ArtifactDefinition artifactDef) {
        return getFromCassandra(artifactDef.getEsId()).right().map(as -> {
            LOGGER.debug(ARTIFACT_NAME_UNIQUE_ID, artifactDef.getArtifactName(), artifactDef.getUniqueId());
            return componentsUtils.getResponseFormat(as);
        }).left().map(ToscaRepresentation::make);
    }

    private Either<byte[], ActionStatus> getFromCassandra(String cassandraId) {
        return artifactCassandraDao.getArtifact(cassandraId).right().map(operationstatus -> {
            LOGGER.info("Failed to fetch artifact from Cassandra by id {} error {}.", cassandraId, operationstatus);
            StorageOperationStatus storageStatus = DaoStatusConverter.convertCassandraStatusToStorageStatus(operationstatus);
            return componentsUtils.convertFromStorageResponse(storageStatus);
        }).left().map(DAOArtifactData::getDataAsArray);
    }

    private static <L, R> F<L, Either<L, R>> iff(Predicate<L> p, Function<L, Either<L, R>> ifTrue) {
        return l -> p.test(l) ? ifTrue.apply(l) : Either.left(l);
    }

    private static <A, B> F<A, B> iff(Predicate<A> p, Supplier<B> s, Function<A, B> orElse) {
        return a -> p.test(a) ? s.get() : orElse.apply(a);
    }

    private void addServiceMf(Component component, ZipOutputStream zip, LifecycleStateEnum lifecycleState, boolean isInCertificationRequest,
                              String fileName, byte[] mainYaml, String definitionsPath) throws IOException {
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
            String serviceTemplate = definitionsPath + fileName;
            String hash = GeneralUtility.calculateMD5Base64EncodedByByteArray(mainYaml);
            String nsMfBlock0 = createNsMfBlock0(serviceName, createdBy, serviceVersion, releaseTime, serviceType, description, serviceTemplate,
                hash);
            byte[] nsMfBlock0Byte = nsMfBlock0.getBytes();
            zip.putNextEntry(new ZipEntry(SERVICE_MANIFEST));
            zip.write(nsMfBlock0Byte);
        }
    }

    private String createNsMfBlock0(String serviceName, String createdBy, String serviceVersion, String releaseTime, String serviceType,
                                    String description, String serviceTemplate, String hash) {
        final String block0template = "metadata??\n" + "ns_product_name: %s\n" + "ns_provider_id: %s\n" + "ns_package_version: %s\n" +
            "ns_release_data_time: %s\n" + "ns_type: %s\n" + "ns_package_description: %s\n\n" + "Source: %s\n" + "Algorithm: MD5\n" + "Hash: %s\n\n";
        return String.format(block0template, serviceName, createdBy, serviceVersion, releaseTime, serviceType, description, serviceTemplate, hash);
    }

    private boolean hasToWriteComponentSubstitutionType(final Component component) {
        final Map<String, CategoryBaseTypeConfig> serviceNodeTypesConfig =
            ConfigurationManager.getConfigurationManager().getConfiguration().getServiceBaseNodeTypes();
        List<CategoryDefinition> categories = component.getCategories();
        if (CollectionUtils.isNotEmpty(categories) && MapUtils.isNotEmpty(serviceNodeTypesConfig) && serviceNodeTypesConfig.get(categories.get(0).getName()) != null) {
            boolean doNotExtendBaseType = serviceNodeTypesConfig.get(categories.get(0).getName()).isDoNotExtendBaseType();
            if (doNotExtendBaseType) {
                return false;
            }
        }
        if (component instanceof Service) {
            return !ModelConverter.isAtomicComponent(component) && ((Service) component).isSubstituteCandidate();
        }
        return !ModelConverter.isAtomicComponent(component);
    }

    private Either<ZipOutputStream, ResponseFormat> writeComponentInterface(Either<ToscaRepresentation, ToscaError> interfaceRepresentation,
                                                                            ZipOutputStream zip, String fileName, String definitionsPath) {
        // TODO: This should not be done but we need this to keep the refactoring small enough to be easily reviewable
        return writeComponentInterface(interfaceRepresentation, fileName, ZipWriter.live(zip), definitionsPath)
            .map(void0 -> Either.<ZipOutputStream, ResponseFormat>left(zip)).recover(th -> {
                LOGGER.error("#writeComponentInterface - zip writing failed with error: ", th);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            }).get();
    }

    private Try<Void> writeComponentInterface(
        Either<ToscaRepresentation, ToscaError> interfaceRepresentation, String fileName, ZipWriter zw, String definitionsPath) {
        Either<byte[], ToscaError> yml = interfaceRepresentation.left()
            .map(ToscaRepresentation::getMainYaml);
        return fromEither(yml, ToscaErrorException::new).flatMap(zw.write(definitionsPath + ToscaExportHandler.getInterfaceFilename(fileName)));
    }

    private List<Triple<String, String, Component>> writeComponentInterface(final Component component, final ZipOutputStream zip,
                                                                            final String fileName, final String definitionsPath) {
        final Either<ToscaRepresentation, ToscaError> interfaceRepresentation = toscaExportUtils.exportComponentInterface(component, false);
        writeComponentInterface(interfaceRepresentation, zip, fileName, definitionsPath);
        return interfaceRepresentation.left().value().getDependencies().getOrElse(new ArrayList<>());
    }

    private Either<ZipOutputStream, ResponseFormat> getZipOutputStreamResponseFormatEither(ZipOutputStream zip,
                                                                                           List<Triple<String, String, Component>> dependencies,
                                                                                           String definitionsPath)
        throws IOException {
        ComponentCache
            innerComponentsCache = ComponentCache.overwritable(overwriteIfSameVersions()).onMerge((oldValue, newValue) -> {
            LOGGER.warn("Overwriting component invariantID {} of version {} with a newer version {}", oldValue.getId(), oldValue.getComponentVersion(),
                newValue.getComponentVersion());
        });
        if (dependencies != null && !dependencies.isEmpty()) {
            for (Triple<String, String, Component> d : dependencies) {
                String cassandraId = d.getMiddle();
                Component childComponent = d.getRight();
                Either<byte[], ResponseFormat> entryData = getEntryData(cassandraId, childComponent).right()
                    .map(componentsUtils::getResponseFormat);
                if (entryData.isRight()) {
                    return Either.right(entryData.right().value());
                }
                //fill innerComponentsCache
                String fileName = d.getLeft();
                innerComponentsCache.put(cassandraId, fileName, childComponent);
                addInnerComponentsToCache(innerComponentsCache, childComponent);
            }
            //add inner components to CSAR
            return addInnerComponentsToCSAR(zip, innerComponentsCache, definitionsPath);
        }
        return null;
    }

    private Either<ZipOutputStream, ResponseFormat> addInnerComponentsToCSAR(ZipOutputStream zip, ComponentCache innerComponentsCache,
                                                                             String definitionsPath)
        throws IOException {
        for (ImmutableTriple<String, String, Component> ict : innerComponentsCache.iterable()) {
            Component innerComponent = ict.getRight();
            String icFileName = ict.getMiddle();
            // add component to zip
            Either<Tuple2<byte[], ZipEntry>, ResponseFormat> zipEntry = toZipEntry(ict, definitionsPath);
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
                writeComponentInterface(innerComponent, zip, icFileName, definitionsPath);
            }
        }
        return null;
    }

    private Either<Tuple2<byte[], ZipEntry>, ResponseFormat> toZipEntry(ImmutableTriple<String, String, Component> cachedEntry,
                                                                        String definitionsPath) {
        String cassandraId = cachedEntry.getLeft();
        String fileName = cachedEntry.getMiddle();
        Component innerComponent = cachedEntry.getRight();
        return getEntryData(cassandraId, innerComponent).right().map(status -> {
            LOGGER.debug("Failed adding to zip component {}, error {}", cassandraId, status);
            return componentsUtils.getResponseFormat(status);
        }).left().map(content -> new Tuple2<>(content, new ZipEntry(definitionsPath + fileName)));
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

    private Component checkAndAddComponent(ComponentCache componentCache, ComponentInstance ci, Either<Resource, StorageOperationStatus> resource) {
        if (resource.isRight()) {
            LOGGER.debug("Failed to fetch resource with id {} for instance {}", ci.getComponentUid(), ci.getName());
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
            LOGGER.debug("Failed to export tosca template for child component {} error {}", childComponent.getUniqueId(), toscaError);
            return componentsUtils.convertFromToscaError(toscaError);
        };
    }

    private Either<byte[], ResponseFormat> getLatestSchemaFilesFromCassandra() {
        String fto = versionFirstThreeOctets;
        return sdcSchemaFilesCassandraDao.getSpecificSchemaFiles(fto, CONFORMANCE_LEVEL).right().map(schemaFilesFetchDBError(fto)).left()
            .bind(iff(List::isEmpty, () -> schemaFileFetchError(fto), s -> Either.left(s.iterator().next().getPayloadAsArray())));
    }

    private F<CassandraOperationStatus, ResponseFormat> schemaFilesFetchDBError(String firstThreeOctets) {
        return cos -> {
            LOGGER.debug("Failed to get the schema files SDC-Version: {} Conformance-Level {}. Please fix DB table accordingly.", firstThreeOctets,
                CONFORMANCE_LEVEL);
            StorageOperationStatus sos = DaoStatusConverter.convertCassandraStatusToStorageStatus(cos);
            return componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(sos));
        };
    }

    private Either<byte[], ResponseFormat> schemaFileFetchError(String firstThreeOctets) {
        LOGGER.debug("Failed to get the schema files SDC-Version: {} Conformance-Level {}", firstThreeOctets, CONFORMANCE_LEVEL);
        return Either.right(componentsUtils.getResponseFormat(ActionStatus.TOSCA_SCHEMA_FILES_NOT_FOUND, firstThreeOctets, CONFORMANCE_LEVEL));
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
     * Writes to a CSAR zip from casandra schema data
     *
     * @param zipOutputStream  stores the input stream content
     * @param schemaFileZip    zip data from Cassandra
     * @param nodesFromPackage list of all nodes found on the onboarded package
     */
    private void addSchemaFilesFromCassandra(final ZipOutputStream zipOutputStream, final byte[] schemaFileZip, final List<String> nodesFromPackage,
                                             final String definitionsPath) {
        final int initSize = 2048;
        LOGGER.debug("Starting copy from Schema file zip to CSAR zip");
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
                    zipOutputStream.putNextEntry(new ZipEntry(definitionsPath + entryName));
                    zipOutputStream.write(byteArrayOutputStream.toByteArray());
                    zipOutputStream.flush();
                    byteArrayOutputStream.reset();
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Error while writing the SDC schema file to the CSAR", e);
            throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        LOGGER.debug("Finished copy from Schema file zip to CSAR zip");
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

    /**
     * Filters and removes all duplicated nodes found
     *
     * @param nodesFromPackage      a List of all derived nodes found on the given package
     * @param nodesFromArtifactFile represents the nodes.yml file stored in Cassandra
     * @return a nodes Map updated
     */
    private Map<String, Object> updateNodeYml(final List<String> nodesFromPackage, final Map<String, Object> nodesFromArtifactFile) {
        if (MapUtils.isNotEmpty(nodesFromArtifactFile)) {
            final String nodeTypeBlock = TypeUtils.ToscaTagNamesEnum.NODE_TYPES.getElementName();
            final Map<String, Object> nodeTypes = (Map<String, Object>) nodesFromArtifactFile.get(nodeTypeBlock);
            nodesFromPackage.stream().filter(nodeTypes::containsKey).forEach(nodeTypes::remove);
            nodesFromArtifactFile.replace(nodeTypeBlock, nodeTypes);
        }
        return nodesFromArtifactFile;
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
     * Checks if the zip entry should or should not be added to the CSAR based on the given global type list
     *
     * @param entryName the zip entry name
     * @return true if the zip entry should be handled
     */
    private boolean shouldZipEntryBeHandled(final String entryName) {
        return ConfigurationManager.getConfigurationManager().getConfiguration().getGlobalCsarImports().stream()
            .anyMatch(entry -> entry.contains(entryName));
    }

    private void addSchemaFilesByModel(final ZipOutputStream zipOutputStream, final String modelName,
                                       final String definitionsPath, final Boolean isSingleImportsFile) {
        try {
            final List<ToscaImportByModel> modelDefaultImportList = modelOperation.findAllModelImports(modelName, true);
            final Set<Path> writtenEntryPathList = new HashSet<>();
            final var defsPath = Path.of(definitionsPath);
            Map<Path, byte[]> contentToMerge = new HashMap();
            for (final ToscaImportByModel toscaImportByModel : modelDefaultImportList) {
                var importPath = Path.of(toscaImportByModel.getFullPath());
                if (!isSingleImportsFile) {
                    if (ADDITIONAL_TYPE_DEFINITIONS.equals(Paths.get(String.valueOf(importPath)).normalize().toString())) {
                        final Path entryPath = defsPath.resolve(importPath);
                        contentToMerge.put(entryPath, toscaImportByModel.getContent().getBytes(StandardCharsets.UTF_8));
                    }
                    else {
                        if (writtenEntryPathList.contains(defsPath.resolve(importPath))) {
                            importPath =
                                ToscaDefaultImportHelper.addModelAsFilePrefix(importPath, toscaImportByModel.getModelId());
                        }
                        final Path entryPath = defsPath.resolve(importPath);
                        writtenEntryPathList.add(entryPath);
                        contentToMerge.put(entryPath, toscaImportByModel.getContent().getBytes(StandardCharsets.UTF_8));
                    }
                }
                else {
                    if (writtenEntryPathList.contains(defsPath.resolve(importPath))) {
                        importPath =
                            ToscaDefaultImportHelper.addModelAsFilePrefix(importPath, toscaImportByModel.getModelId());
                    }
                    final Path entryPath = defsPath.resolve(importPath);
                    final var zipEntry = new ZipEntry(entryPath.toString());
                    zipOutputStream.putNextEntry(zipEntry);
                    writtenEntryPathList.add(entryPath);
                    final byte[] content = toscaImportByModel.getContent().getBytes(StandardCharsets.UTF_8);
                    zipOutputStream.write(content, 0, content.length);
                    zipOutputStream.closeEntry();
                }
            }
            if (!isSingleImportsFile) {
                byte[] mergingContent = new byte[0];
                for (Map.Entry entry : contentToMerge.entrySet()) {
                    if (ADDITIONAL_TYPE_DEFINITIONS.equals(Paths.get(String.valueOf(entry.getKey())).normalize().toString())) {
                        mergingContent = (byte[])entry.getValue();
                    }
                    else {
                        final var zipEntry = new ZipEntry(entry.getKey().toString());
                        zipOutputStream.putNextEntry(zipEntry);
                        writtenEntryPathList.add((Path) entry.getKey());
                        zipOutputStream.write(Bytes.concat(mergingContent, (byte[]) entry.getValue()), 0,
                            ((byte[]) entry.getValue()).length);
                        zipOutputStream.closeEntry();
                    }
                }
            }
        } catch (final IOException e) {
            LOGGER.error(String.valueOf(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR), CsarUtils.class.getName(),
                "Error while writing the schema files by model to the CSAR", e);
            throw new ByResponseFormatComponentException(componentsUtils.getResponseFormat(ActionStatus.CSAR_TOSCA_IMPORTS_ERROR));
        }
    }

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

        if (LOGGER.isDebugEnabled()) {
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

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(result.toString());
        }
    }

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
            LOGGER.error("Failed to fetch resource with id {} for instance {}", componentUid, parentComponent.getUUID());
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

    private ComponentTypeArtifacts collectComponentTypeArtifacts(Component fetchedComponent) {
        ArtifactsInfo componentArtifacts = collectComponentArtifacts(fetchedComponent);
        ComponentTypeArtifacts componentArtifactsInfo = new ComponentTypeArtifacts();
        if (componentArtifacts.isNotEmpty()) {
            componentArtifactsInfo.setComponentArtifacts(componentArtifacts);
        }
        return componentArtifactsInfo;
    }

    private ArtifactsInfo collectComponentArtifacts(Component component) {
        Map<String, ArtifactDefinition> informationalArtifacts = component.getArtifacts();
        Map<String, List<ArtifactDefinition>> informationalArtifactsByType = collectGroupArtifacts(informationalArtifacts);
        Map<String, ArtifactDefinition> deploymentArtifacts = component.getDeploymentArtifacts();
        Map<String, List<ArtifactDefinition>> deploymentArtifactsByType = collectGroupArtifacts(deploymentArtifacts);
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
        String currentPath = ARTIFACTS_PATH + RESOURCES_PATH;
        for (String keyAssetName : componentInstancesArtifacts.keySet()) {
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
        for (Map.Entry<String, InterfaceDefinition> interfaceEntry : ((Resource) component).getInterfaces().entrySet()) {
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
                        LOGGER.error(ARTIFACT_NAME_UNIQUE_ID, artifactName, artifactUUID);
                        LOGGER.error("Failed to get {} payload from DB reason: {}", artifactName, artifactFromCassandra.right().value());
                        return Either.right(componentsUtils.getResponseFormat(
                            ARTIFACT_PAYLOAD_NOT_FOUND_DURING_CSAR_CREATION, "Resource", component.getUniqueId(), artifactName, artifactUUID));
                    }
                    zipstream.putNextEntry(new ZipEntry(OperationArtifactUtil.createOperationArtifactPath(component, null, operation, true)));
                    zipstream.write(artifactFromCassandra.left().value());
                } catch (IOException e) {
                    LOGGER.error("Component Name {},  Interface Name {}, Operation Name {}", component.getNormalizedName(), interfaceEntry.getKey(),
                        operation.getName());
                    LOGGER.error("Error while writing the operation's artifacts to the CSAR", e);
                    return Either.right(componentsUtils.getResponseFormat(ERROR_DURING_CSAR_CREATION, "Resource", component.getUniqueId()));
                }
            }
        }
        return Either.left(zipstream);
    }

    private boolean checkComponentBeforeWrite(Component component, Map.Entry<String, InterfaceDefinition> interfaceEntry,
                                              OperationDataDefinition operation) {
        final ArtifactDataDefinition implementation = operation.getImplementation();
        if (Objects.isNull(implementation)) {
            LOGGER.debug("Component Name {}, Interface Id {}, Operation Name {} - no Operation Implementation found", component.getNormalizedName(),
                interfaceEntry.getValue().getUniqueId(), operation.getName());
            return true;
        }
        final String artifactName = implementation.getArtifactName();
        if (Objects.isNull(artifactName)) {
            LOGGER.debug("Component Name {}, Interface Id {}, Operation Name {} - no artifact found", component.getNormalizedName(),
                interfaceEntry.getValue().getUniqueId(), operation.getName());
            return true;
        }
        if (OperationArtifactUtil.artifactNameIsALiteralValue(artifactName)) {
            LOGGER.debug("Component Name {}, Interface Id {}, Operation Name {} - artifact name is a literal value rather than an SDC artifact",
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
            LOGGER.debug("Component Name {}- no interfaces found", component.getNormalizedName());
            return true;
        }
        return false;
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
                LOGGER.debug(ARTIFACT_NAME_UNIQUE_ID, ad.getArtifactName(), ad.getUniqueId());
                LOGGER.debug("Failed to get {} payload from DB reason: {}", ad.getArtifactName(), as);
                return as;
            });
        } else {
            return Either.left(ad);
        }
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

    private Either<ToscaRepresentation, ResponseFormat> generateToscaRepresentation(Component component) {
        return toscaExportUtils.exportComponent(component).right().map(toscaError -> {
            LOGGER.debug("exportComponent failed {}", toscaError);
            return componentsUtils.getResponseFormat(componentsUtils.convertFromToscaError(toscaError));
        });
    }

    private String createToscaBlock0(String metaFileVersion, String csarVersion, String createdBy, String entryDef, boolean isAsdPackage,
                                     String definitionsPath) {
        final String block0template = "TOSCA-Meta-File-Version: %s\nCSAR-Version: %s\nCreated-By: %s\nEntry-Definitions: "
            + definitionsPath + "%s\n%s\nName: csar.meta\nContent-Type: text/plain\n";
        return String.format(block0template, metaFileVersion, csarVersion, createdBy, entryDef, isAsdPackage ? "entry_definition_type: asd" : "");
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

    public static class ToscaErrorException extends Exception {

        ToscaErrorException(ToscaError error) {
            super("Error while exporting component's interface (toscaError:" + error + ")");
        }
    }

}
