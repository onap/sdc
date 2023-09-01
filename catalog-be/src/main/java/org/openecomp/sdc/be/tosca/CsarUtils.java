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

import fj.F;
import fj.data.Either;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.config.ArtifactConfigManager;
import org.openecomp.sdc.be.config.ArtifactConfiguration;
import org.openecomp.sdc.be.config.ComponentType;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("csar-utils")
public class CsarUtils {

    public static final String ARTIFACTS_PATH = "Artifacts/";
    public static final String ARTIFACTS = "Artifacts";
    public static final String ARTIFACT_CREATED_FROM_CSAR = "Artifact created from csar";
    private static final Logger log = Logger.getLogger(CsarUtils.class);
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(CsarUtils.class.getName());
    private static final String PATH_DELIMITER = "/";
    private static final String CSAR_META_VERSION = "1.0";
    private static final String CSAR_META_PATH_FILE_NAME = "csar.meta";
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
    private static final String VFC_NODE_TYPE_ARTIFACTS_PATH_PATTERN =
        ARTIFACTS + DEL_PATTERN + ImportUtils.Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + VALID_ENGLISH_ARTIFACT_NAME_WITH_DIGITS + DEL_PATTERN
            + VALID_ENGLISH_ARTIFACT_NAME_WITH_DIGITS + DEL_PATTERN + VALID_ENGLISH_ARTIFACT_NAME_WITH_DIGITS + DEL_PATTERN
            + VALID_ENGLISH_ARTIFACT_NAME_WITH_DIGITS;
    private static final String BLOCK_0_TEMPLATE = "SDC-TOSCA-Meta-File-Version: %s\nSDC-TOSCA-Definitions-Version: %s\n";

    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentsUtils;
    private final MapFromModelCsarGeneratorService mapFromModelCsarGeneratorService;

    @Autowired
    public CsarUtils(final ToscaOperationFacade toscaOperationFacade,
                     final ComponentsUtils componentsUtils,
                     final MapFromModelCsarGeneratorService mapFromModelCsarGeneratorService) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
        this.mapFromModelCsarGeneratorService = mapFromModelCsarGeneratorService;
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
        final String toscaConformanceLevel = ConfigurationManager.getConfigurationManager().getConfiguration().getToscaConformanceLevel();
        final byte[] csarBlock0Byte = createCsarBlock0(CSAR_META_VERSION, toscaConformanceLevel).getBytes();

        return generateCsarZip(csarBlock0Byte,
            isAsdPackage(component), component, getFromCS, isInCertificationRequest).left().map(responseFormat -> {
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

    private Either<byte[], ResponseFormat> generateCsarZip(byte[] csarBlock0Byte,
                                                           boolean isAsdPackage,
                                                           Component component,
                                                           boolean getFromCS,
                                                           boolean isInCertificationRequest) {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry(CSAR_META_PATH_FILE_NAME));
            zip.write(csarBlock0Byte);
            Either<ZipOutputStream, ResponseFormat> populateZip = mapFromModelCsarGeneratorService.generateCsarZip(
                component, getFromCS, zip, isInCertificationRequest, isAsdPackage);
            if (populateZip.isRight()) {
                log.debug("Failed to populate CSAR zip file {}. Please fix DB table accordingly ", populateZip.right().value());
                return Either.right(populateZip.right().value());
            }
            zip.finish();
            return Either.left(out.toByteArray());
        } catch (IOException e) {
            log.debug("Failed with IOexception to create CSAR zip for component {}. Please fix DB table accordingly ", component.getUniqueId(), e);
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            return Either.right(responseFormat);
        }
    }

    private String createCsarBlock0(String metaFileVersion, String toscaConformanceLevel) {
        return String.format(BLOCK_0_TEMPLATE, metaFileVersion, toscaConformanceLevel);
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
        for (Entry<String, ComponentTypeArtifacts> typeArtifacts : componentArtifacts.getComponentTypeArtifacts().entrySet()) {
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
        for (Entry<String, ArtifactsInfo> resourceInstance : componentInstanceArtifacts.getComponentInstancesArtifacts().entrySet()) {
            result.append("Folder" + resourceInstance.getKey() + "\n");
            result.append(printArtifacts(resourceInstance.getValue().getArtifactsInfo()));
        }

        return result.toString();
    }

    private String printArtifacts(Map<ArtifactGroupTypeEnum, Map<String, List<ArtifactDefinition>>> componetArtifacts) {
        StringBuilder result = new StringBuilder();
        for (Entry<ArtifactGroupTypeEnum, Map<String, List<ArtifactDefinition>>> artifactGroup : componetArtifacts.entrySet()) {
            result.append("    " + artifactGroup.getKey().getType());
            for (Entry<String, List<ArtifactDefinition>> groupArtifacts : artifactGroup.getValue().entrySet()) {
                result.append("        " + groupArtifacts.getKey());
                for (ArtifactDefinition artifact : groupArtifacts.getValue()) {
                    result.append("            " + artifact.getArtifactDisplayName());
                }
            }
        }

        return result.toString();
    }

    private ComponentTypeArtifacts collectComponentTypeArtifacts(Component fetchedComponent) {
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

