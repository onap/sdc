/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.components.csar;

import static org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.FAILED_UPLOAD_ARTIFACT_TO_COMPONENT;
import static org.openecomp.sdc.be.tosca.CsarUtils.ARTIFACTS_PATH;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fj.data.Either;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.BaseBusinessLogic;
import org.openecomp.sdc.be.components.impl.CsarValidationUtils;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.config.Configuration.VfModuleProperty;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.ArtifactUtils;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.info.MergedArtifactInfo;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.heat.HeatParameterType;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component("csarArtifactsAndGroupsBusinessLogic")
public class CsarArtifactsAndGroupsBusinessLogic extends BaseBusinessLogic {

    private static final Logger log = Logger.getLogger(CsarArtifactsAndGroupsBusinessLogic.class.getName());
    private static final String ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME = "Artifact  file is not in expected format, fileName  {}";
    private static final String ARTIFACT_INTERNALS_ARE_INVALID = "Artifact internals are invalid";
    private static final String ARTIFACT_WITH_NAME_AND_TYPE_ALREADY_EXIST_WITH_TYPE = "Artifact with name {} and type {} already exist with type  {}";
    private static final Pattern pattern = Pattern.compile("\\..(.*?)\\..");
    private static final String LABEL_COUNTER_DELIMITER = "[^0-9]+";
    protected final ArtifactsBusinessLogic artifactsBusinessLogic;
    private final Gson gson = new Gson();
    private final GroupBusinessLogic groupBusinessLogic;

    @Autowired
    public CsarArtifactsAndGroupsBusinessLogic(IElementOperation elementDao, IGroupOperation groupOperation,
                                               IGroupInstanceOperation groupInstanceOperation, IGroupTypeOperation groupTypeOperation,
                                               GroupBusinessLogic groupBusinessLogic, InterfaceOperation interfaceOperation,
                                               InterfaceLifecycleOperation interfaceLifecycleTypeOperation,
                                               ArtifactsBusinessLogic artifactsBusinessLogic, ArtifactsOperations artifactToscaOperation) {
        super(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation, interfaceOperation, interfaceLifecycleTypeOperation,
            artifactToscaOperation);
        this.artifactsBusinessLogic = artifactsBusinessLogic;
        this.groupBusinessLogic = groupBusinessLogic;
    }

    public Either<Resource, ResponseFormat> createResourceArtifactsFromCsar(CsarInfo csarInfo, Resource resource, String artifactsMetaFile,
                                                                            String artifactsMetaFileName, List<ArtifactDefinition> createdArtifacts) {
        log.debug("parseResourceArtifactsInfoFromFile start");
        return parseResourceArtifactsInfoFromFile(resource, artifactsMetaFile, artifactsMetaFileName).left()
            .bind(p -> createResourceArtifacts(csarInfo, resource, p, createdArtifacts)).right().map(rf -> {
                componentsUtils.auditResource(rf, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE);
                return rf;
            }).left().bind(c -> checkoutRes(c));
    }

    private Either<Resource, ResponseFormat> checkoutRes(Component component) {
        return Either.left((Resource) getResourcetFromGraph(component).left().value());
    }

    public Either<Service, ResponseFormat> createResourceArtifactsFromCsar(CsarInfo csarInfo, Service resource, String artifactsMetaFile,
                                                                           String artifactsMetaFileName, List<ArtifactDefinition> createdArtifacts) {
        log.debug("parseResourceArtifactsInfoFromFile start");
        return parseResourceArtifactsInfoFromFile(resource, artifactsMetaFile, artifactsMetaFileName).left()
            .bind(p -> createResourceArtifacts(csarInfo, resource, p, createdArtifacts)).right().map(rf -> rf).left().bind(c ->
                Either.left((Service) getResourcetFromGraph(c).left().value())
            );
    }

    public Either<Component, ResponseFormat> updateResourceArtifactsFromCsar(CsarInfo csarInfo, Component resource, String artifactsMetaFile,
                                                                             String artifactsMetaFileName,
                                                                             List<ArtifactDefinition> createdNewArtifacts, boolean shouldLock,
                                                                             boolean inTransaction) {
        Component updatedResource = resource;
        Either<Map<String, List<ArtifactTemplateInfo>>, ResponseFormat> parseResourceInfoFromYamlEither = parseResourceArtifactsInfoFromFile(
            updatedResource, artifactsMetaFile, artifactsMetaFileName);
        if (parseResourceInfoFromYamlEither.isRight()) {
            ResponseFormat responseFormat = parseResourceInfoFromYamlEither.right().value();
            if (resource instanceof Resource) {
                componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), (Resource) resource, AuditingActionEnum.IMPORT_RESOURCE);
            }
            return Either.right(responseFormat);
        }
        List<GroupDefinition> groups = updatedResource.getGroups();
        Map<String, ArtifactDefinition> deploymentArtifact = updatedResource.getDeploymentArtifacts();
        if (MapUtils.isEmpty(deploymentArtifact)) {
            deleteGroupsByType(groups, Constants.DEFAULT_GROUP_VF_MODULE, updatedResource);
            return createResourceArtifacts(csarInfo, updatedResource, parseResourceInfoFromYamlEither.left().value(), createdNewArtifacts);
        }
        List<ArtifactDefinition> createdDeploymentArtifactsAfterDelete = deploymentArtifact.values().stream().collect(Collectors.toList());
        int labelCounter =
            createdDeploymentArtifactsAfterDelete.stream().map(ArtifactDefinition::getArtifactLabel).filter(this::isLastCharacterInLabelADigit)
                .map(this::getNextInt).flatMapToInt(this::toStream).max().orElse(-1) + 1;
        ////////////////////////////////////// create set parsed

        ////////////////////////////////////// artifacts///////////////////////////////////////////
        Map<String, List<ArtifactTemplateInfo>> parsedArtifactsMap = parseResourceInfoFromYamlEither.left().value();
        List<ArtifactTemplateInfo> artifactsWithoutGroups = parsedArtifactsMap.remove(ArtifactTemplateInfo.CSAR_ARTIFACT);
        Collection<List<ArtifactTemplateInfo>> parsedArifactsCollection = parsedArtifactsMap.values();
        Either<Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>>, ResponseFormat> parsedArtifactsPerGroupEither = createArtifactsTemplateCollection(
            csarInfo, updatedResource, createdNewArtifacts, createdDeploymentArtifactsAfterDelete, labelCounter, parsedArifactsCollection);
        if (parsedArtifactsPerGroupEither.isRight()) {
            log.error("Failed to parse artifacts. Status is {} ", parsedArtifactsPerGroupEither.right().value());
            return Either.right(parsedArtifactsPerGroupEither.right().value());
        }
        Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroup = parsedArtifactsPerGroupEither.left().value();
        // find master in group
        Map<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupArtifact = findMasterArtifactInGroup(groups, deploymentArtifact);
        ///////////////////////////////// find artifacts to

        ///////////////////////////////// delete////////////////////////////////////////////////////
        Map<String, List<ArtifactDefinition>> groupToDelete = new HashMap<>();
        Set<ArtifactDefinition> artifactsToDelete = findArtifactThatNotInGroupToDelete(parsedGroup, createdDeploymentArtifactsAfterDelete);
        Set<ArtifactTemplateInfo> jsonMasterArtifacts = parsedGroup.keySet();
        Map<GroupDefinition, MergedArtifactInfo> mergedgroup = mergeGroupInUpdateFlow(groupArtifact, parsedGroup, artifactsToDelete, groupToDelete,
            jsonMasterArtifacts, createdDeploymentArtifactsAfterDelete);
        List<ArtifactDefinition> deletedArtifacts = new ArrayList<>();
        Either<Component, ResponseFormat> deletedArtifactsEither = deleteArtifactsInUpdateCsarFlow(updatedResource, csarInfo.getModifier(),
            shouldLock, inTransaction, artifactsToDelete, groupToDelete, deletedArtifacts);
        if (deletedArtifactsEither.isRight()) {
            log.debug("Failed to delete artifacts. Status is {} ", deletedArtifactsEither.right().value());
            return Either.right(deletedArtifactsEither.right().value());
        }
        updatedResource = deletedArtifactsEither.left().value();
        // need to update resource if we updated artifacts
        excludeDeletedArtifacts(deletedArtifacts, createdDeploymentArtifactsAfterDelete);
        ////////////// dissociate, associate or create

        ////////////// artifacts////////////////////////////
        Either<Component, ResponseFormat> assDissotiateEither = associateAndDissociateArtifactsToGroup(csarInfo, updatedResource, createdNewArtifacts,
            labelCounter, createdDeploymentArtifactsAfterDelete, mergedgroup, deletedArtifacts);
        groups = updatedResource.getGroups();
        if (assDissotiateEither.isRight()) {
            log.debug("Failed to delete artifacts. Status is {} ", assDissotiateEither.right().value());
            return Either.right(assDissotiateEither.right().value());
        }
        updatedResource = assDissotiateEither.left().value();
        deploymentArtifact = updatedResource.getDeploymentArtifacts();
        createdDeploymentArtifactsAfterDelete = deploymentArtifact.values().stream().collect(Collectors.toList());
        // update vfModule names
        Set<GroupDefinition> groupForAssociateWithMembers = mergedgroup.keySet();
        Either<Component, ResponseFormat> validateUpdateVfGroupNamesRes = updateVfModuleNames(createdNewArtifacts, updatedResource, groups,
            createdDeploymentArtifactsAfterDelete, groupForAssociateWithMembers);
        if (validateUpdateVfGroupNamesRes != null) {
            return validateUpdateVfGroupNamesRes;
        }
        //////////////// create new artifacts in update

        //////////////// flow////////////////////////////
        List<ArtifactTemplateInfo> newArtifactsGroup = createNewArtifcats(parsedGroup, groupArtifact);
        Either<Component, ResponseFormat> validateGroupNamesRes = handleArtifactsInGroup(csarInfo, createdNewArtifacts, updatedResource, groups,
            createdDeploymentArtifactsAfterDelete, labelCounter, newArtifactsGroup);
        if (validateGroupNamesRes != null) {
            return validateGroupNamesRes;
        }
        // updatedGroup
        Either<Component, ResponseFormat> updateVersionEither = updateGroupVersion(updatedResource, groupForAssociateWithMembers);
        if (updateVersionEither != null) {
            return updateVersionEither;
        }
        if (!CollectionUtils.isEmpty(artifactsWithoutGroups)) {
            for (ArtifactTemplateInfo t : artifactsWithoutGroups) {
                List<ArtifactTemplateInfo> artifacts = new ArrayList<>();
                artifacts.add(t);
                Either<Component, ResponseFormat> resStatus = createGroupDeploymentArtifactsFromCsar(csarInfo, updatedResource, artifacts,
                    createdNewArtifacts, createdDeploymentArtifactsAfterDelete, labelCounter);
                if (checkResponse(resStatus)) {
                    return resStatus;
                }
            }
        }
        Either<Component, StorageOperationStatus> eitherGetResource = toscaOperationFacade.getToscaElement(updatedResource.getUniqueId());
        return mapResult(eitherGetResource, updatedResource);
    }

    private Either<Component, ResponseFormat> handleArtifactsInGroup(CsarInfo csarInfo, List<ArtifactDefinition> createdNewArtifacts,
                                                                     Component updatedResource, List<GroupDefinition> groups,
                                                                     List<ArtifactDefinition> createdDeploymentArtifactsAfterDelete, int labelCounter,
                                                                     List<ArtifactTemplateInfo> newArtifactsGroup) {
        if (!newArtifactsGroup.isEmpty()) {
            Collections.sort(newArtifactsGroup, ArtifactTemplateInfo::compareByGroupName);
            int startGroupCounter = groupBusinessLogic.getNextVfModuleNameCounter(groups);
            Either<Boolean, ResponseFormat> validateGroupNamesRes = groupBusinessLogic
                .validateGenerateVfModuleGroupNames(newArtifactsGroup, updatedResource.getSystemName(), startGroupCounter);
            if (validateGroupNamesRes.isRight()) {
                return Either.right(validateGroupNamesRes.right().value());
            }
            Either<Component, ResponseFormat> resStatus = createGroupDeploymentArtifactsFromCsar(csarInfo, updatedResource, newArtifactsGroup,
                createdNewArtifacts, createdDeploymentArtifactsAfterDelete, labelCounter);
            checkResponse(resStatus);
        }
        return null;
    }

    private boolean checkResponse(Either<Component, ResponseFormat> resStatus) {
        return (resStatus.isRight());
    }

    private Either<Component, ResponseFormat> updateVfModuleNames(List<ArtifactDefinition> createdNewArtifacts, Component updatedResource,
                                                                  List<GroupDefinition> groups,
                                                                  List<ArtifactDefinition> createdDeploymentArtifactsAfterDelete,
                                                                  Set<GroupDefinition> groupForAssociateWithMembers) {
        if (!CollectionUtils.isEmpty(groups)) {
            Either<List<GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = groupBusinessLogic
                .validateUpdateVfGroupNamesOnGraph(groups, updatedResource);
            if (validateUpdateVfGroupNamesRes.isRight()) {
                return Either.right(validateUpdateVfGroupNamesRes.right().value());
            }
            updateGroupMemebers(groups, groupForAssociateWithMembers, createdNewArtifacts, createdDeploymentArtifactsAfterDelete);
        }
        return null;
    }

    private Either<Component, ResponseFormat> updateGroupVersion(Component updatedResource, Set<GroupDefinition> groupForAssociateWithMembers) {
        if (!groupForAssociateWithMembers.isEmpty()) {
            List<GroupDefinition> groupsId = groupForAssociateWithMembers.stream().collect(Collectors.toList());
            Either<List<GroupDefinition>, ResponseFormat> updateVersionEither = groupBusinessLogic.updateGroups(updatedResource, groupsId, true);
            if (updateVersionEither.isRight()) {
                log.debug("Failed to update groups version. Status is {} ", updateVersionEither.right().value());
                return Either.right(updateVersionEither.right().value());
            }
        }
        return null;
    }

    private IntStream toStream(OptionalInt optionalInt) {
        if (optionalInt.isPresent()) {
            return IntStream.of(optionalInt.getAsInt());
        }
        return IntStream.empty();
    }

    private OptionalInt getNextInt(String artifactLabel) {
        try (Scanner scanner = new Scanner(artifactLabel).useDelimiter(LABEL_COUNTER_DELIMITER)) {
            if (scanner.hasNextInt()) {
                return OptionalInt.of(scanner.nextInt());
            }
            return OptionalInt.empty();
        }
    }

    private boolean isLastCharacterInLabelADigit(String artifactLabel) {
        return Character.isDigit(artifactLabel.charAt(artifactLabel.length() - 1));
    }

    private Either<Component, ResponseFormat> mapResult(Either<Component, StorageOperationStatus> result, Component resource) {
        return result.right()
            .map(status -> componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(status), resource));
    }

    private void updateGroupMemebers(List<GroupDefinition> groups, Set<GroupDefinition> groupForAssociateWithMembers,
                                     List<ArtifactDefinition> createdNewArtifacts, List<ArtifactDefinition> createdDeploymentArtifactsAfterDelete) {
        List<GroupDefinition> heatGroups = collectGroupsWithMembers(groups);
        for (GroupDefinition updatedGroupDef : groupForAssociateWithMembers) {
            Map<String, String> members = new HashMap<>();
            Set<String> artifactsGroup = new HashSet<>();
            artifactsGroup.addAll(updatedGroupDef.getArtifacts());
            associateMembersToArtifacts(createdNewArtifacts, createdDeploymentArtifactsAfterDelete, heatGroups, artifactsGroup, members);
            updatedGroupDef.setMembers(members);
        }
    }

    /**
     * @param groups
     * @return
     */
    private List<GroupDefinition> collectGroupsWithMembers(List<GroupDefinition> groups) {
        return groups.stream().filter(e -> e.getMembers() != null).collect(Collectors.toList());
    }

    /**
     * Exclude deleted Artificats from Deployment Artifcats
     *
     * @param deletedArtifacts
     * @param createdDeploymentArtifactsAfterDelete
     */
    private void excludeDeletedArtifacts(List<ArtifactDefinition> deletedArtifacts, List<ArtifactDefinition> createdDeploymentArtifactsAfterDelete) {
        for (ArtifactDefinition deletedArtifact : deletedArtifacts) {
            ArtifactDefinition artToRemove = null;
            for (ArtifactDefinition artFromResource : createdDeploymentArtifactsAfterDelete) {
                if (deletedArtifact.getUniqueId().equalsIgnoreCase(artFromResource.getUniqueId())) {
                    artToRemove = artFromResource;
                    break;
                }
            }
            if (artToRemove != null) {
                createdDeploymentArtifactsAfterDelete.remove(artToRemove);
            }
        }
    }

    private void deleteGroupsByType(List<GroupDefinition> groups, String groupType, Component resource) {
        if (groups != null) {
            List<GroupDefinition> listToDelete = groups.stream().filter(g -> g.getType().equals(groupType)).collect(Collectors.toList());
            groupBusinessLogic.deleteGroups(resource, listToDelete);
        }
    }

    private List<ArtifactTemplateInfo> createNewArtifcats(Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroup,
                                                          Map<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupArtifact) {
        List<ArtifactTemplateInfo> newArtifactsGroup = new ArrayList<>();
        for (Entry<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroupSetEntry : parsedGroup.entrySet()) {
            ArtifactTemplateInfo parsedArtifactMaster = parsedGroupSetEntry.getKey();
            boolean isNewGroup = true;
            for (Entry<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupListEntry : groupArtifact.entrySet()) {
                Map<ArtifactDefinition, List<ArtifactDefinition>> groupArtifacts = groupListEntry.getValue();
                Set<ArtifactDefinition> group = groupArtifacts.keySet();
                for (ArtifactDefinition artifactInfo : group) {
                    if (parsedArtifactMaster.getFileName().equalsIgnoreCase(artifactInfo.getArtifactName())) {
                        parsedArtifactMaster.setGroupName(groupListEntry.getKey().getName());
                        isNewGroup = false;
                    }
                }
            }
            if (isNewGroup) {
                newArtifactsGroup.add(parsedArtifactMaster);
            }
        }
        return newArtifactsGroup;
    }

    private Set<ArtifactDefinition> findArtifactThatNotInGroupToDelete(Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroup,
                                                                       List<ArtifactDefinition> createdDeploymentArtifactsAfterDelete) {
        Set<ArtifactDefinition> artifactsToDelete = new HashSet<>();
        for (Entry<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroupSetEntry : parsedGroup.entrySet()) {
            Set<ArtifactTemplateInfo> artifactsNames = parsedGroupSetEntry.getValue();
            for (ArtifactTemplateInfo template : artifactsNames) {
                if (template.getType().equals(ArtifactTypeEnum.HEAT_ARTIFACT.getType())) {
                    Optional<ArtifactDefinition> op = createdDeploymentArtifactsAfterDelete.stream()
                        .filter(a -> a.getArtifactName().equalsIgnoreCase(template.getFileName())).findAny();
                    if (op.isPresent() && !op.get().getArtifactType().equalsIgnoreCase(template.getType())) {
                        artifactsToDelete.add(op.get());
                    }
                }
            }
        }
        return artifactsToDelete;
    }

    private Either<Component, ResponseFormat> createResourceArtifacts(CsarInfo csarInfo, Component resource,
                                                                      Map<String, List<ArtifactTemplateInfo>> artifactsMap,
                                                                      List<ArtifactDefinition> createdArtifacts) {
        Either<Component, ResponseFormat> resStatus = Either.left(resource);
        Collection<List<ArtifactTemplateInfo>> arifactsCollection = artifactsMap.values();
        for (List<ArtifactTemplateInfo> groupTemplateList : arifactsCollection) {
            if (groupTemplateList != null) {
                resStatus = createGroupDeploymentArtifactsFromCsar(csarInfo, resource, groupTemplateList, createdArtifacts, 0);
                if (resStatus.isRight()) {
                    return resStatus;
                }
            }
        }
        return resStatus;
    }

    private Either<Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>>, ResponseFormat> createArtifactsTemplateCollection(CsarInfo csarInfo,
                                                                                                                           Component resource,
                                                                                                                           List<ArtifactDefinition> createdNewArtifacts,
                                                                                                                           List<ArtifactDefinition> createdDeploymentArtifactsAfterDelete,
                                                                                                                           int labelCounter,
                                                                                                                           Collection<List<ArtifactTemplateInfo>> parsedArifactsCollection) {
        Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroup = new HashMap<>();
        for (List<ArtifactTemplateInfo> parsedGroupTemplateList : parsedArifactsCollection) {
            for (ArtifactTemplateInfo parsedGroupTemplate : parsedGroupTemplateList) {
                if (parsedGroupTemplate.getGroupName() != null) {
                    parsedGroupTemplate.setGroupName("");
                    Set<ArtifactTemplateInfo> parsedArtifactsNames = new HashSet<>();
                    parsedArtifactsNames.add(parsedGroupTemplate);
                    List<ArtifactTemplateInfo> relatedGroupTemplateList = parsedGroupTemplate.getRelatedArtifactsInfo();
                    if (relatedGroupTemplateList != null && !relatedGroupTemplateList.isEmpty()) {
                        createArtifactsGroupSet(parsedGroupTemplateList, parsedArtifactsNames);
                    }
                    parsedGroup.put(parsedGroupTemplate, parsedArtifactsNames);
                } else {
                    List<ArtifactTemplateInfo> arrtifacts = new ArrayList<>();
                    arrtifacts.add(parsedGroupTemplate);
                    Either<Component, ResponseFormat> resStatus = createGroupDeploymentArtifactsFromCsar(csarInfo, resource, arrtifacts,
                        createdNewArtifacts, createdDeploymentArtifactsAfterDelete, labelCounter);
                    if (resStatus.isRight()) {
                        return Either.right(resStatus.right().value());
                    }
                }
            }
        }
        return Either.left(parsedGroup);
    }

    @SuppressWarnings({"unchecked", "static-access"})
    public Either<Map<String, List<ArtifactTemplateInfo>>, ResponseFormat> parseResourceArtifactsInfoFromFile(Component resource,
                                                                                                              String artifactsMetaFile,
                                                                                                              String artifactFileName) {
        try {
            JsonObject jsonElement = new JsonObject();
            jsonElement = gson.fromJson(artifactsMetaFile, jsonElement.getClass());
            JsonElement importStructureElement = jsonElement.get(Constants.IMPORT_STRUCTURE);
            if (importStructureElement == null || importStructureElement.isJsonNull()) {
                log.debug(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME, artifactFileName);
                BeEcompErrorManager.getInstance()
                    .logInternalDataError(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME + artifactFileName, ARTIFACT_INTERNALS_ARE_INVALID,
                        ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, artifactFileName));
            }
            Map<String, List<Map<String, Object>>> artifactTemplateMap;
            artifactTemplateMap = ComponentsUtils.parseJsonToObject(importStructureElement.toString(), HashMap.class);
            if (artifactTemplateMap.isEmpty()) {
                log.debug(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME, artifactFileName);
                BeEcompErrorManager.getInstance()
                    .logInternalDataError(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME + artifactFileName, ARTIFACT_INTERNALS_ARE_INVALID,
                        ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, artifactFileName));
            }
            Set<String> artifactsTypeKeys = artifactTemplateMap.keySet();
            Map<String, List<ArtifactTemplateInfo>> artifactsMap = new HashMap<>();
            List<ArtifactTemplateInfo> allGroups = new ArrayList<>();
            for (String artifactsTypeKey : artifactsTypeKeys) {
                Either<List<ArtifactTemplateInfo>, ResponseFormat> artifactTemplateInfoListEither = parseArtifactTemplateList(artifactFileName,
                    artifactTemplateMap, allGroups, artifactsTypeKey);
                if (artifactTemplateInfoListEither.isRight()) {
                    return Either.right(artifactTemplateInfoListEither.right().value());
                }
                artifactsMap.put(artifactsTypeKey, artifactTemplateInfoListEither.left().value());
            }
            int counter = groupBusinessLogic.getNextVfModuleNameCounter(resource.getGroups());
            Either<Boolean, ResponseFormat> validateGroupNamesRes = groupBusinessLogic
                .validateGenerateVfModuleGroupNames(allGroups, resource.getSystemName(), counter);
            if (validateGroupNamesRes.isRight()) {
                return Either.right(validateGroupNamesRes.right().value());
            }
            return Either.left(artifactsMap);
        } catch (Exception e) {
            log.debug(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME, artifactFileName);
            log.debug("failed with exception.", e);
            BeEcompErrorManager.getInstance()
                .logInternalDataError(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME + artifactFileName, ARTIFACT_INTERNALS_ARE_INVALID,
                    ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, artifactFileName));
        }
    }

    private Either<List<ArtifactTemplateInfo>, ResponseFormat> parseArtifactTemplateList(String artifactFileName,
                                                                                         Map<String, List<Map<String, Object>>> artifactTemplateMap,
                                                                                         List<ArtifactTemplateInfo> allGroups,
                                                                                         String artifactsTypeKey) {
        List<Map<String, Object>> o = artifactTemplateMap.get(artifactsTypeKey);
        Either<List<ArtifactTemplateInfo>, ResponseFormat> artifactTemplateInfoListPairStatus = createArtifactTemplateInfoModule(artifactsTypeKey, o);
        if (artifactTemplateInfoListPairStatus.isRight()) {
            log.debug(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME, artifactFileName);
            BeEcompErrorManager.getInstance()
                .logInternalDataError(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME + artifactFileName, ARTIFACT_INTERNALS_ARE_INVALID,
                    ErrorSeverity.ERROR);
            return Either.right(artifactTemplateInfoListPairStatus.right().value());
        }
        List<ArtifactTemplateInfo> artifactTemplateInfoList = artifactTemplateInfoListPairStatus.left().value();
        if (artifactTemplateInfoList == null) {
            log.debug(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME, artifactFileName);
            BeEcompErrorManager.getInstance()
                .logInternalDataError(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME + artifactFileName, ARTIFACT_INTERNALS_ARE_INVALID,
                    ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, artifactFileName));
        }
        if (!artifactsTypeKey.equalsIgnoreCase(ArtifactTemplateInfo.CSAR_ARTIFACT)) {
            allGroups.addAll(artifactTemplateInfoList);
        }
        return Either.left(artifactTemplateInfoList);
    }

    private Either<List<ArtifactTemplateInfo>, ResponseFormat> createArtifactTemplateInfoModule(String artifactsTypeKey,
                                                                                                List<Map<String, Object>> jsonObject) {
        List<ArtifactTemplateInfo> artifactTemplateInfoList = new ArrayList<>();
        for (Map<String, Object> o : jsonObject) {
            Either<ArtifactTemplateInfo, ResponseFormat> artifacttemplateInfoStatus = ArtifactTemplateInfo
                .createArtifactTemplateInfoFromJson(componentsUtils, artifactsTypeKey, o, artifactTemplateInfoList, null);
            if (artifacttemplateInfoStatus.isRight()) {
                return Either.right(artifacttemplateInfoStatus.right().value());
            }
            ArtifactTemplateInfo artifacttemplateInfo = artifacttemplateInfoStatus.left().value();
            if (artifacttemplateInfo != null) {
                artifactTemplateInfoList.add(artifacttemplateInfo);
            }
        }
        return Either.left(artifactTemplateInfoList);
    }

    private Either<Component, ResponseFormat> createGroupDeploymentArtifactsFromCsar(CsarInfo csarInfo, Component resource,
                                                                                     List<ArtifactTemplateInfo> artifactsTemplateList,
                                                                                     List<ArtifactDefinition> createdArtifacts, int labelCounter) {
        List<GroupDefinition> createdGroups = resource.getGroups();
        List<GroupDefinition> heatGroups = null;
        if (!CollectionUtils.isEmpty(createdGroups)) {
            heatGroups = collectGroupsWithMembers(createdGroups);
        }
        List<GroupDefinition> needToCreate = new ArrayList<>();
        for (ArtifactTemplateInfo groupTemplateInfo : artifactsTemplateList) {
            String groupName = groupTemplateInfo.getGroupName();
            Set<String> artifactsGroup = new HashSet<>();
            Set<String> artifactsUUIDGroup = new HashSet<>();
            log.debug("createDeploymentArtifactsFromCsar start");
            Either<Component, ResponseFormat> resStatus = createDeploymentArtifactFromCsar(csarInfo, ARTIFACTS_PATH, resource, artifactsGroup,
                artifactsUUIDGroup, groupTemplateInfo, createdArtifacts, labelCounter);
            log.debug("createDeploymentArtifactsFromCsar end");
            if (resStatus.isRight()) {
                return resStatus;
            }
            Map<String, ArtifactDefinition> createdArtifactsMap = createdArtifacts.stream()
                .collect(Collectors.toMap(ArtifactDataDefinition::getArtifactLabel, artifact -> artifact));
            resource.setDeploymentArtifacts(createdArtifactsMap);
            if (groupName != null && !groupName.isEmpty()) {
                Either<GroupDefinition, ResponseFormat> groupDefinitionEither = buildGroupDefinition(createdArtifacts, heatGroups, groupTemplateInfo,
                    groupName, artifactsGroup, artifactsUUIDGroup, resource.getModel());
                if (groupDefinitionEither.isRight()) {
                    return Either.right(groupDefinitionEither.right().value());
                }
                needToCreate.add(groupDefinitionEither.left().value());
            }
        }
        Map<String, ArtifactDefinition> createdArtifactsMap = createdArtifacts.stream()
            .collect(Collectors.toMap(ArtifactDataDefinition::getArtifactLabel, artifact -> artifact));
        resource.setDeploymentArtifacts(createdArtifactsMap);
        Either<List<GroupDefinition>, ResponseFormat> createGroups = groupBusinessLogic.addGroups(resource, needToCreate, false);
        if (createGroups.isRight()) {
            return Either.right(createGroups.right().value());
        }
        return Either.left(resource);
    }

    private Either<GroupDefinition, ResponseFormat> buildGroupDefinition(List<ArtifactDefinition> createdArtifacts, List<GroupDefinition> heatGroups,
                                                                         ArtifactTemplateInfo groupTemplateInfo, String groupName,
                                                                         Set<String> artifactsGroup, Set<String> artifactsUUIDGroup,
                                                                         String model) {
        Map<String, String> members = new HashMap<>();
        associateMembersToArtifacts(createdArtifacts, null, heatGroups, artifactsGroup, members);
        List<String> artifactsList = new ArrayList<>(artifactsGroup);
        List<String> artifactsUUIDList = new ArrayList<>(artifactsUUIDGroup);
        GroupDefinition groupDefinition = new GroupDefinition();
        groupDefinition.setName(groupName);
        groupDefinition.setType(Constants.DEFAULT_GROUP_VF_MODULE);
        groupDefinition.setArtifacts(artifactsList);
        groupDefinition.setArtifactsUuid(artifactsUUIDList);
        if (!members.isEmpty()) {
            groupDefinition.setMembers(members);
        }
        List<GroupProperty> properties = new ArrayList<>();
        GroupProperty prop = new GroupProperty();
        prop.setName(Constants.IS_BASE);
        prop.setValue(Boolean.toString(groupTemplateInfo.isBase()));
        properties.add(prop);
        Either<GroupTypeDefinition, StorageOperationStatus> getLatestGroupTypeRes = groupTypeOperation
            .getLatestGroupTypeByType(Constants.DEFAULT_GROUP_VF_MODULE, model);
        if (getLatestGroupTypeRes.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(getLatestGroupTypeRes.right().value())));
        }
        properties = createVfModuleAdditionalProperties(groupTemplateInfo.isBase(), groupName, properties, createdArtifacts, artifactsList,
            getLatestGroupTypeRes.left().value());
        groupDefinition.convertFromGroupProperties(properties);
        log.debug("createGroup start");
        return Either.left(groupDefinition);
    }

    private Either<Component, ResponseFormat> createDeploymentArtifactFromCsar(CsarInfo csarInfo, String artifactPath, Component resource,
                                                                               Set<String> artifactsGroup, Set<String> artifactsUUIDGroup,
                                                                               ArtifactTemplateInfo artifactTemplateInfo,
                                                                               List<ArtifactDefinition> createdArtifacts, int labelCounter) {
        Either<Component, ResponseFormat> resStatus = Either.left(resource);
        String artifactUid = "";
        String artifactEnvUid = "";
        String artifactUUID = "";
        // check if artifacts already exist
        Either<ArtifactDefinition, ResponseFormat> createdArtifactEther = checkIfArtifactAlreadyExist(artifactTemplateInfo, createdArtifacts);
        if (createdArtifactEther.isRight()) {
            return Either.right(createdArtifactEther.right().value());
        }
        ArtifactDefinition createdArtifact = createdArtifactEther.left().value();
        if (createdArtifact == null) {
            Either<ArtifactDefinition, ResponseFormat> newArtifactEither = createDeploymentArtifact(csarInfo, resource, artifactPath,
                artifactTemplateInfo, createdArtifacts, labelCounter);
            if (newArtifactEither.isRight()) {
                resStatus = Either.right(newArtifactEither.right().value());
                return resStatus;
            }
            ArtifactDefinition newArtifact = newArtifactEither.left().value();
            artifactUid = newArtifact.getUniqueId();
            artifactUUID = newArtifact.getArtifactUUID();
            final ArtifactTypeEnum artifactType = ArtifactTypeEnum.parse(newArtifact.getArtifactType());
            if (artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_NET || artifactType == ArtifactTypeEnum.HEAT_VOL) {
                ArtifactDefinition createHeatEnvPlaceHolder = artifactsBusinessLogic
                    .createHeatEnvPlaceHolder(createdArtifacts, newArtifact, ArtifactsBusinessLogic.HEAT_VF_ENV_NAME, resource.getUniqueId(),
                        NodeTypeEnum.Resource, resource.getName(), csarInfo.getModifier(), resource, null);
                artifactEnvUid = createHeatEnvPlaceHolder.getUniqueId();
            }
        } else {
            artifactUid = createdArtifact.getUniqueId();
            artifactUUID = createdArtifact.getArtifactUUID();
            artifactEnvUid = checkAndGetHeatEnvId(createdArtifact);
        }
        artifactsGroup.add(artifactUid);
        artifactsUUIDGroup.add(artifactUUID);
        if (!artifactEnvUid.isEmpty()) {
            artifactsGroup.add(artifactEnvUid);
        }
        List<ArtifactTemplateInfo> relatedArtifacts = artifactTemplateInfo.getRelatedArtifactsInfo();
        if (relatedArtifacts != null) {
            for (ArtifactTemplateInfo relatedArtifactTemplateInfo : relatedArtifacts) {
                resStatus = createDeploymentArtifactFromCsar(csarInfo, artifactPath, resource, artifactsGroup, artifactsUUIDGroup,
                    relatedArtifactTemplateInfo, createdArtifacts, labelCounter);
                if (resStatus.isRight()) {
                    return resStatus;
                }
            }
        }
        return resStatus;
    }

    private String checkAndGetHeatEnvId(ArtifactDefinition createdArtifact) {
        String artifactEnvUid = "";
        final ArtifactTypeEnum artifactType = ArtifactTypeEnum.parse(createdArtifact.getArtifactType());
        if (artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_NET || artifactType == ArtifactTypeEnum.HEAT_VOL) {
            artifactEnvUid = createdArtifact.getUniqueId() + ArtifactsBusinessLogic.HEAT_ENV_SUFFIX;
        }
        return artifactEnvUid;
    }

    private Either<ArtifactDefinition, ResponseFormat> checkIfArtifactAlreadyExist(ArtifactTemplateInfo artifactTemplateInfo,
                                                                                   List<ArtifactDefinition> createdArtifacts) {
        ArtifactDefinition res = null;
        String artifactFileName = artifactTemplateInfo.getFileName();
        Optional<ArtifactDefinition> op = createdArtifacts.stream().filter(a -> a.getArtifactName().equals(artifactFileName)).findAny();
        if (op.isPresent()) {
            res = op.get();
            if (!res.getArtifactType().equalsIgnoreCase(artifactTemplateInfo.getType())) {
                log.debug(ARTIFACT_WITH_NAME_AND_TYPE_ALREADY_EXIST_WITH_TYPE, artifactFileName, artifactTemplateInfo.getType(),
                    res.getArtifactType());
                BeEcompErrorManager.getInstance()
                    .logInternalDataError(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME + artifactFileName, ARTIFACT_INTERNALS_ARE_INVALID,
                        ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, artifactFileName,
                    artifactTemplateInfo.getType(), res.getArtifactType()));
            }
        }
        return Either.left(res);
    }

    private Either<ArtifactDefinition, ResponseFormat> createDeploymentArtifact(CsarInfo csarInfo, Component resource, String artifactPath,
                                                                                ArtifactTemplateInfo artifactTemplateInfo,
                                                                                List<ArtifactDefinition> createdArtifacts, int label) {
        int updatedlabel = label;
        final String artifactFileName = artifactTemplateInfo.getFileName();
        Either<ImmutablePair<String, byte[]>, ResponseFormat> artifactContentStatus = CsarValidationUtils
            .getArtifactContent(csarInfo.getCsarUUID(), csarInfo.getCsar(), artifactPath + artifactFileName, artifactFileName, componentsUtils);
        if (artifactContentStatus.isRight()) {
            return Either.right(artifactContentStatus.right().value());
        }
        updatedlabel += createdArtifacts.size();
        Map<String, Object> json = ArtifactUtils
            .buildJsonForArtifact(artifactTemplateInfo, artifactContentStatus.left().value().getValue(), updatedlabel, true);
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> uploadArtifactToService = createOrUpdateCsarArtifactFromJson(resource,
            csarInfo.getModifier(), json, new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE));
        if (uploadArtifactToService.isRight()) {
            return Either.right(uploadArtifactToService.right().value());
        }
        ArtifactDefinition currentInfo = uploadArtifactToService.left().value().left().value();
        if (currentInfo.getHeatParameters() != null) {
            Either<ArtifactDefinition, ResponseFormat> updateEnvEither = updateHeatParamsFromCsar(resource, csarInfo, artifactTemplateInfo,
                currentInfo, false);
            if (updateEnvEither.isRight()) {
                log.debug("failed to update parameters to artifact {}", artifactFileName);
                return Either.right(updateEnvEither.right().value());
            }
            currentInfo = updateEnvEither.left().value();
        }
        createdArtifacts.add(currentInfo);
        return Either.left(currentInfo);
    }

    private Either<ArtifactDefinition, ResponseFormat> updateHeatParamsFromCsar(Component resource, CsarInfo csarInfo,
                                                                                ArtifactTemplateInfo artifactTemplateInfo,
                                                                                ArtifactDefinition currentInfo, boolean isUpdateEnv) {
        Component updatedResource = resource;
        Either<ArtifactDefinition, ResponseFormat> resStatus = Either.left(currentInfo);
        if (artifactTemplateInfo.getEnv() != null && !artifactTemplateInfo.getEnv().isEmpty()) {
            Either<ImmutablePair<String, byte[]>, ResponseFormat> artifactParamsStatus = CsarValidationUtils
                .getArtifactContent(csarInfo.getCsarUUID(), csarInfo.getCsar(), CsarUtils.ARTIFACTS_PATH + artifactTemplateInfo.getEnv(),
                    artifactTemplateInfo.getEnv(), componentsUtils);
            if (artifactParamsStatus.isRight()) {
                resStatus = Either.right(artifactParamsStatus.right().value());
                return resStatus;
            }
            Either<List<HeatParameterDefinition>, ResponseFormat> propsStatus = extractHeatParameters(ArtifactTypeEnum.HEAT_ENV.getType(),
                artifactTemplateInfo.getEnv(), artifactParamsStatus.left().value().getValue(), false);
            if (propsStatus.isLeft()) {
                List<HeatParameterDefinition> updatedHeatEnvParams = propsStatus.left().value();
                resStatus = updateHeatParams(updatedResource, currentInfo, updatedHeatEnvParams);
                if (resStatus.isRight()) {
                    return resStatus;
                }
            }
        }
        if (isUpdateEnv) {
            Map<String, ArtifactDefinition> artifacts = updatedResource.getDeploymentArtifacts();
            Optional<ArtifactDefinition> op = artifacts.values().stream()
                .filter(p -> p.getGeneratedFromId() != null && p.getGeneratedFromId().equals(currentInfo.getUniqueId())).findAny();
            if (op.isPresent()) {
                ArtifactDefinition artifactInfoHeatEnv = op.get();
                artifactInfoHeatEnv.setHeatParamUpdated(true);
                Either<ArtifactDefinition, StorageOperationStatus> updateArtifactOnResource = artifactToscaOperation
                    .updateArtifactOnResource(artifactInfoHeatEnv, updatedResource, artifactInfoHeatEnv.getUniqueId(), null, null, true);
                if (updateArtifactOnResource.isRight()) {
                    log.debug("Failed to update heat env on CSAR flow for component {} artifact {} label {}", updatedResource.getUniqueId(),
                        artifactInfoHeatEnv.getUniqueId(), artifactInfoHeatEnv.getArtifactLabel());
                    return Either.right(
                        componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(updateArtifactOnResource.right().value())));
                }
                resource.getDeploymentArtifacts()
                    .put(updateArtifactOnResource.left().value().getArtifactLabel(), updateArtifactOnResource.left().value());
                resStatus = Either.left(updateArtifactOnResource.left().value());
            }
        }
        return resStatus;
    }

    private Either<List<HeatParameterDefinition>, ResponseFormat> extractHeatParameters(String artifactType, String fileName, byte[] content,
                                                                                        boolean is64Encoded) {
        // extract heat parameters
        String heatDecodedPayload = is64Encoded ? new String(Base64.decodeBase64(content)) : new String(content);
        Either<List<HeatParameterDefinition>, ResultStatusEnum> heatParameters = ImportUtils
            .getHeatParamsWithoutImplicitTypes(heatDecodedPayload, artifactType);
        if (heatParameters.isRight()) {
            log.debug("File {} is not in expected key-value form in csar ", fileName);
            BeEcompErrorManager.getInstance()
                .logInternalDataError("File " + fileName + " is not in expected key-value form in csar ", "CSAR internals are invalid",
                    ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT, fileName));
        }
        return Either.left(heatParameters.left().value());
    }

    private Either<ArtifactDefinition, ResponseFormat> updateHeatParams(Component resource, ArtifactDefinition currentInfo,
                                                                        List<HeatParameterDefinition> updatedHeatEnvParams) {
        Either<ArtifactDefinition, ResponseFormat> resStatus = Either.left(currentInfo);
        List<HeatParameterDefinition> currentHeatEnvParams = currentInfo.getListHeatParameters();
        if (updatedHeatEnvParams != null && !updatedHeatEnvParams.isEmpty() && currentHeatEnvParams != null && !currentHeatEnvParams.isEmpty()) {
            String paramName;
            for (HeatParameterDefinition heatEnvParam : updatedHeatEnvParams) {
                paramName = heatEnvParam.getName();
                for (HeatParameterDefinition currHeatParam : currentHeatEnvParams) {
                    if (paramName.equalsIgnoreCase(currHeatParam.getName())) {
                        String updatedParamValue = heatEnvParam.getCurrentValue();
                        if (updatedParamValue == null) {
                            updatedParamValue = heatEnvParam.getDefaultValue();
                        }
                        HeatParameterType paramType = HeatParameterType.isValidType(currHeatParam.getType());
                        if (!paramType.getValidator().isValid(updatedParamValue, null)) {
                            ActionStatus status = ActionStatus.INVALID_HEAT_PARAMETER_VALUE;
                            ResponseFormat responseFormat = componentsUtils
                                .getResponseFormat(status, ArtifactTypeEnum.HEAT_ENV.getType(), paramType.getType(), paramName);
                            resStatus = Either.right(responseFormat);
                            return resStatus;
                        }
                        currHeatParam.setCurrentValue(paramType.getConverter().convert(updatedParamValue, null, null));
                        break;
                    }
                }
            }
            currentInfo.setListHeatParameters(currentHeatEnvParams);
            Either<ArtifactDefinition, StorageOperationStatus> updateArtifactOnResource = artifactToscaOperation
                .updateArtifactOnResource(currentInfo, resource, currentInfo.getUniqueId(), null, null, true);
            if (updateArtifactOnResource.isRight()) {
                log.debug("Failed to update heat parameters of heat on CSAR flow for component {} artifact {} label {}", resource.getUniqueId(),
                    currentInfo.getUniqueId(), currentInfo.getArtifactLabel());
                return Either
                    .right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(updateArtifactOnResource.right().value())));
            }
            resource.getDeploymentArtifacts().put(currentInfo.getArtifactLabel(), currentInfo);
            resStatus = Either.left(updateArtifactOnResource.left().value());
        }
        return resStatus;
    }

    public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> createOrUpdateCsarArtifactFromJson(Component component, User user,
                                                                                                            Map<String, Object> json,
                                                                                                            ArtifactOperationInfo operation) {
        String jsonStr = gson.toJson(json);
        ArtifactDefinition artifactDefinitionFromJson = RepresentationUtils.convertJsonToArtifactDefinition(jsonStr, ArtifactDefinition.class, false);
        Either<ArtifactDefinition, Operation> result;
        try {
            result = artifactsBusinessLogic
                .handleLoadedArtifact(component, user, operation, false, true, component.getComponentType(), artifactDefinitionFromJson);
        } catch (ComponentException e) {
            log.debug(FAILED_UPLOAD_ARTIFACT_TO_COMPONENT, component.getComponentType(), component.getName());
            return Either.right(componentsUtils.getResponseFormat(e));
        } catch (Exception e) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR);
            log.debug("Exception occurred when createOrUpdateCsarArtifactFromJson, error is:{}", e.getMessage(), e);
            return Either.right(responseFormat);
        }
        return Either.left(result);
    }

    private void associateMembersToArtifacts(List<ArtifactDefinition> createdArtifacts, List<ArtifactDefinition> artifactsFromResource,
                                             List<GroupDefinition> heatGroups, Set<String> artifactsGroup, Map<String, String> members) {
        if (heatGroups != null && !heatGroups.isEmpty()) {
            for (GroupDefinition heatGroup : heatGroups) {
                List<GroupProperty> grpoupProps = heatGroup.convertToGroupProperties();
                if (grpoupProps != null) {
                    associateMembersToVFgroups(createdArtifacts, artifactsFromResource, grpoupProps, artifactsGroup, heatGroup, members);
                }
            }
        }
    }

    private void associateMembersToVFgroups(List<ArtifactDefinition> createdArtifacts, List<ArtifactDefinition> artifactsFromResource,
                                            List<GroupProperty> grpoupProps, Set<String> artifactsGroup, GroupDefinition heatGroup,
                                            Map<String, String> members) {
        Optional<GroupProperty> op = grpoupProps.stream().filter(p -> p.getName().equals(Constants.HEAT_FILE_PROPS)).findAny();
        if (op.isPresent()) {
            GroupProperty prop = op.get();
            String heatFileNAme = prop.getValue();
            if (null == heatFileNAme || heatFileNAme.isEmpty()) {
                return;
            }
            List<ArtifactDefinition> artifacts = new ArrayList<>();
            for (String artifactId : artifactsGroup) {
                Optional<ArtifactDefinition> opArt = createdArtifacts.stream().filter(p -> p.getUniqueId().equals(artifactId)).findAny();
                opArt.ifPresent(artifacts::add);
                if (artifactsFromResource != null) {
                    opArt = artifactsFromResource.stream().filter(p -> p.getUniqueId().equals(artifactId)).findAny();
                    opArt.ifPresent(artifacts::add);
                }
            }
            Optional<ArtifactDefinition> resOp = artifacts.stream().filter(p -> heatFileNAme.contains(p.getArtifactName())).findAny();
            resOp.ifPresent(artifactDefinition -> members.putAll(heatGroup.getMembers()));
        }
    }

    public List<GroupProperty> createVfModuleAdditionalProperties(boolean isBase, String moduleName, List<GroupProperty> properties,
                                                                  List<ArtifactDefinition> deploymentArtifacts, List<String> artifactsInGroup,
                                                                  GroupTypeDefinition groupType) {
        Map<String, VfModuleProperty> vfModuleProperties = ConfigurationManager.getConfigurationManager().getConfiguration().getVfModuleProperties();
        vfModuleProperties.entrySet().forEach(p -> {
            GroupProperty prop = new GroupProperty();
            prop.setName(p.getKey());
            if (isBase) {
                prop.setValue(p.getValue().getForBaseModule());
                prop.setDefaultValue(p.getValue().getForBaseModule());
            } else {
                prop.setValue(p.getValue().getForNonBaseModule());
                prop.setDefaultValue(p.getValue().getForNonBaseModule());
            }
            properties.add(prop);
        });
        GroupProperty proplabel = new GroupProperty();
        proplabel.setName("vf_module_label");
        Matcher matcher = pattern.matcher(moduleName);
        if (matcher.find()) {
            proplabel.setValue(matcher.group(1));
            proplabel.setDefaultValue(matcher.group(1));
        } else {
            proplabel.setValue(moduleName);
            proplabel.setDefaultValue(moduleName);
        }
        properties.add(proplabel);
        GroupProperty propvolume = new GroupProperty();
        propvolume.setName("volume_group");
        boolean isVolume = false;
        for (String artifactId : artifactsInGroup) {
            ArtifactDefinition artifactDef = null;
            artifactDef = ArtifactUtils.findArtifactInList(deploymentArtifacts, artifactId);
            if (artifactDef != null && artifactDef.getArtifactType().equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType())) {
                isVolume = true;
                break;
            }
        }
        propvolume.setValue(String.valueOf(isVolume));
        propvolume.setDefaultValue(String.valueOf(isVolume));
        properties.add(propvolume);
        mergeWithGroupTypeProperties(properties, groupType.getProperties());
        return properties;
    }

    private void mergeWithGroupTypeProperties(List<GroupProperty> properties, List<PropertyDefinition> groupTypeProperties) {
        Map<String, GroupProperty> propertiesMap = properties.stream().collect(Collectors.toMap(PropertyDataDefinition::getName, p -> p));
        for (PropertyDefinition groupTypeProperty : groupTypeProperties) {
            if (!propertiesMap.containsKey(groupTypeProperty.getName())) {
                properties.add(new GroupProperty(groupTypeProperty));
            }
        }
    }

    private Map<GroupDefinition, MergedArtifactInfo> mergeGroupInUpdateFlow(
        Map<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupArtifact,
        Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroup, Set<ArtifactDefinition> artifactsToDelete,
        Map<String, List<ArtifactDefinition>> groupToDelete, Set<ArtifactTemplateInfo> jsonMasterArtifacts,
        List<ArtifactDefinition> createdDeploymentArtifacts) {
        Map<GroupDefinition, MergedArtifactInfo> mergedgroup = new HashMap<>();
        for (Entry<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupListEntry : groupArtifact.entrySet()) {
            Map<ArtifactDefinition, List<ArtifactDefinition>> createdArtifactMap = groupListEntry.getValue();
            boolean isNeedToDeleteGroup = true;
            List<ArtifactDefinition> listToDelete = null;
            for (ArtifactDefinition maserArtifact : createdArtifactMap.keySet()) {
                listToDelete = prepareArtifactsToDelete(parsedGroup, artifactsToDelete, createdDeploymentArtifacts, createdArtifactMap,
                    maserArtifact);
                if (artifactsToDelete != null && !artifactsToDelete.isEmpty()) {
                    GroupDefinition group = groupListEntry.getKey();
                    deleteArtifacts(artifactsToDelete, group);
                }
                for (ArtifactTemplateInfo jsonMasterArtifact : jsonMasterArtifacts) {
                    isNeedToDeleteGroup = isNeedToDeleteGroup(mergedgroup, groupListEntry, createdArtifactMap, isNeedToDeleteGroup, maserArtifact,
                        jsonMasterArtifact);
                }
            }
            if (isNeedToDeleteGroup) {
                groupToDelete.put(groupListEntry.getKey().getUniqueId(), listToDelete);
            }
        }
        return mergedgroup;
    }

    private boolean isNeedToDeleteGroup(Map<GroupDefinition, MergedArtifactInfo> mergedgroup,
                                        Entry<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupListEntry,
                                        Map<ArtifactDefinition, List<ArtifactDefinition>> createdArtifactMap, boolean isNeedToDeleteGroup,
                                        ArtifactDefinition maserArtifact, ArtifactTemplateInfo jsonMasterArtifact) {
        if (maserArtifact.getArtifactName().equalsIgnoreCase(jsonMasterArtifact.getFileName())) {
            MergedArtifactInfo mergedGroup = new MergedArtifactInfo();
            mergedGroup.setJsonArtifactTemplate(jsonMasterArtifact);
            mergedGroup.setCreatedArtifact(createdArtifactMap.get(maserArtifact));
            mergedgroup.put(groupListEntry.getKey(), mergedGroup);
            isNeedToDeleteGroup = false;
        }
        return isNeedToDeleteGroup;
    }

    private List<ArtifactDefinition> prepareArtifactsToDelete(Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroup,
                                                              Set<ArtifactDefinition> artifactsToDelete,
                                                              List<ArtifactDefinition> createdDeploymentArtifacts,
                                                              Map<ArtifactDefinition, List<ArtifactDefinition>> createdArtifactMap,
                                                              ArtifactDefinition maserArtifact) {
        List<ArtifactDefinition> listToDelete;
        listToDelete = createdArtifactMap.get(maserArtifact);
        for (ArtifactDefinition artToDelete : listToDelete) {
            findArtifactToDelete(parsedGroup, artifactsToDelete, artToDelete, createdDeploymentArtifacts);
        }
        return listToDelete;
    }

    private void deleteArtifacts(Set<ArtifactDefinition> artifactsToDelete, GroupDefinition group) {
        for (ArtifactDefinition artifactDefinition : artifactsToDelete) {
            if (CollectionUtils.isNotEmpty(group.getArtifacts()) && group.getArtifacts().contains(artifactDefinition.getUniqueId())) {
                group.getArtifacts().remove(artifactDefinition.getUniqueId());
            }
            if (CollectionUtils.isNotEmpty(group.getArtifactsUuid()) && group.getArtifactsUuid().contains(artifactDefinition.getArtifactUUID())) {
                group.getArtifactsUuid().remove(artifactDefinition.getArtifactUUID());
            }
        }
    }

    private void findArtifactToDelete(Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroup, Set<ArtifactDefinition> artifactsToDelete,
                                      ArtifactDefinition artifact, List<ArtifactDefinition> createdDeploymentArtifacts) {
        ArtifactDefinition generatedFromArt = null;
        if (artifact.getGeneratedFromId() != null && !artifact.getGeneratedFromId().isEmpty()) {
            Optional<ArtifactDefinition> op = createdDeploymentArtifacts.stream().filter(p -> p.getUniqueId().equals(artifact.getGeneratedFromId()))
                .findAny();
            if (op.isPresent()) {
                generatedFromArt = op.get();
            }
        }
        isNeedToDeleteArtifact(parsedGroup, artifactsToDelete, artifact, generatedFromArt);
    }

    private void isNeedToDeleteArtifact(Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroup, Set<ArtifactDefinition> artifactsToDelete,
                                        ArtifactDefinition artifact, ArtifactDefinition generatedFromArt) {
        final String artifactType = artifact.getArtifactType();
        final String artifactName = artifact.getArtifactName();
        boolean isNeedToDeleteArtifact = true;
        for (final Entry<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroupSetEntry : parsedGroup.entrySet()) {
            if (isNeedToDeleteArtifact) {
                for (final ArtifactTemplateInfo template : parsedGroupSetEntry.getValue()) {
                    if (isNeedToDeleteArtifact) {
                        final String fileName = template.getFileName();
                        final String type = template.getType();
                        if ((artifactName.equalsIgnoreCase(fileName) && artifactType.equalsIgnoreCase(type))
                            || (generatedFromArt != null && generatedFromArt.getArtifactName().equalsIgnoreCase(fileName) &&
                            generatedFromArt.getArtifactType().equalsIgnoreCase(type))) {
                            isNeedToDeleteArtifact = false;
                        }
                    }
                }
            }
        }
        if (isNeedToDeleteArtifact) {
            artifactsToDelete.add(artifact);
        }
    }

    private Map<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> findMasterArtifactInGroup(List<GroupDefinition> groups,
                                                                                                              Map<String, ArtifactDefinition> deploymentArtifact) {
        Map<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupArtifact = new HashMap<>();
        for (GroupDefinition group : groups) {
            Map<ArtifactDefinition, List<ArtifactDefinition>> gupsMap = new HashMap<>();
            List<ArtifactDefinition> artifacts = new ArrayList<>();
            List<String> artifactsList = group.getArtifacts();
            if (artifactsList != null && !artifactsList.isEmpty()) {
                ArtifactDefinition masterArtifact = ArtifactUtils.findMasterArtifact(deploymentArtifact, artifacts, artifactsList);
                if (masterArtifact != null) {
                    gupsMap.put(masterArtifact, artifacts);
                }
                groupArtifact.put(group, gupsMap);
            }
        }
        return groupArtifact;
    }

    private Either<Component, ResponseFormat> deleteArtifactsInUpdateCsarFlow(Component resource, User user, boolean shouldLock,
                                                                              boolean inTransaction, Set<ArtifactDefinition> artifactsToDelete,
                                                                              Map<String, List<ArtifactDefinition>> groupToDelete,
                                                                              List<ArtifactDefinition> deletedArtifacts) {
        Component updatedResource = resource;
        String resourceId = updatedResource.getUniqueId();
        if (!artifactsToDelete.isEmpty()) {
            for (ArtifactDefinition artifact : artifactsToDelete) {
                String artifactType = artifact.getArtifactType();
                final ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.parse(artifactType);
                if (artifactTypeEnum != ArtifactTypeEnum.HEAT_ENV) {
                    Either<ArtifactDefinition, ResponseFormat> handleDelete = artifactsBusinessLogic
                        .handleDelete(resourceId, artifact.getUniqueId(), user, updatedResource, shouldLock, inTransaction);
                    if (handleDelete.isRight()) {
                        return Either.right(handleDelete.right().value());
                    }
                    deletedArtifacts.add(handleDelete.left().value());
                }
            }
        }
        if (!groupToDelete.isEmpty()) {
            log.debug("try to delete group");
            List<GroupDefinition> groupDefinitionstoDelete = new ArrayList<>();
            List<GroupDefinition> groups = updatedResource.getGroups();
            for (Entry<String, List<ArtifactDefinition>> deleteGroup : groupToDelete.entrySet()) {
                Optional<GroupDefinition> op = groups.stream().filter(gr -> gr.getUniqueId().equals(deleteGroup.getKey())).findAny();
                if (op.isPresent()) {
                    groupDefinitionstoDelete.add(op.get());
                }
            }
            if (!groupDefinitionstoDelete.isEmpty()) {
                Either<List<GroupDefinition>, ResponseFormat> prepareGroups = groupBusinessLogic.deleteGroups(resource, groupDefinitionstoDelete);
                if (prepareGroups.isRight()) {
                    return Either.right(prepareGroups.right().value());
                }
            }
        }
        List<GroupDefinition> oldGroups = updatedResource.getGroups();
        Either<Resource, StorageOperationStatus> eitherGerResource = toscaOperationFacade.getToscaElement(updatedResource.getUniqueId());
        if (eitherGerResource.isRight()) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), updatedResource,
                    resource.getComponentType());
            return Either.right(responseFormat);
        }
        updatedResource = eitherGerResource.left().value();
        updatedResource.setGroups(oldGroups);
        return Either.left(updatedResource);
    }

    private void createArtifactsGroupSet(List<ArtifactTemplateInfo> parsedGroupTemplateList, Set<ArtifactTemplateInfo> parsedArtifactsName) {
        for (ArtifactTemplateInfo parsedGroupTemplate : parsedGroupTemplateList) {
            parsedArtifactsName.add(parsedGroupTemplate);
            List<ArtifactTemplateInfo> relatedArtifacts = parsedGroupTemplate.getRelatedArtifactsInfo();
            if (relatedArtifacts != null && !relatedArtifacts.isEmpty()) {
                createArtifactsGroupSet(relatedArtifacts, parsedArtifactsName);
            }
        }
    }

    private Either<Component, ResponseFormat> createGroupDeploymentArtifactsFromCsar(CsarInfo csarInfo, Component resource,
                                                                                     List<ArtifactTemplateInfo> artifactsTemplateList,
                                                                                     List<ArtifactDefinition> createdNewArtifacts,
                                                                                     List<ArtifactDefinition> artifactsFromResource,
                                                                                     int labelCounter) {
        Component updatedResource = resource;
        Either<Component, ResponseFormat> resStatus = Either.left(updatedResource);
        List<GroupDefinition> createdGroups = updatedResource.getGroups();
        List<GroupDefinition> heatGroups = null;
        if (createdGroups != null && !createdGroups.isEmpty()) {
            heatGroups = collectGroupsWithMembers(createdGroups);
        }
        List<GroupDefinition> needToAdd = new ArrayList<>();
        for (ArtifactTemplateInfo groupTemplateInfo : artifactsTemplateList) {
            String groupName = groupTemplateInfo.getGroupName();
            Set<String> artifactsGroup = new HashSet<>();
            Set<String> artifactsUUIDGroup = new HashSet<>();
            resStatus = createDeploymentArtifactsFromCsar(csarInfo, updatedResource, artifactsGroup, artifactsUUIDGroup, groupTemplateInfo,
                createdNewArtifacts, artifactsFromResource, labelCounter);
            if (resStatus.isRight()) {
                return resStatus;
            }
            if (!StringUtils.isEmpty(groupName)) {
                Map<String, String> members = new HashMap<>();
                associateMembersToArtifacts(createdNewArtifacts, artifactsFromResource, heatGroups, artifactsGroup, members);
                List<String> artifactsList = new ArrayList<>(artifactsGroup);
                List<String> artifactsUUIDList = new ArrayList<>(artifactsUUIDGroup);
                GroupDefinition groupDefinition = new GroupDefinition();
                groupDefinition.setName(groupName);
                groupDefinition.setType(Constants.DEFAULT_GROUP_VF_MODULE);
                groupDefinition.setArtifacts(artifactsList);
                groupDefinition.setArtifactsUuid(artifactsUUIDList);
                if (!members.isEmpty()) {
                    groupDefinition.setMembers(members);
                }
                List<GroupProperty> properties = new ArrayList<>();
                GroupProperty prop = new GroupProperty();
                prop.setName(Constants.IS_BASE);
                prop.setValue(Boolean.toString(groupTemplateInfo.isBase()));
                properties.add(prop);
                List<ArtifactDefinition> createdArtifacts = new ArrayList<>();
                createdArtifacts.addAll(createdNewArtifacts);
                createdArtifacts.addAll(artifactsFromResource);
                Either<GroupTypeDefinition, StorageOperationStatus> getLatestGroupTypeRes = groupTypeOperation
                    .getLatestGroupTypeByType(Constants.DEFAULT_GROUP_VF_MODULE, resource.getModel());
                if (getLatestGroupTypeRes.isRight()) {
                    return Either
                        .right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(getLatestGroupTypeRes.right().value())));
                }
                properties = createVfModuleAdditionalProperties(groupTemplateInfo.isBase(), groupName, properties, createdArtifacts, artifactsList,
                    getLatestGroupTypeRes.left().value());
                groupDefinition.convertFromGroupProperties(properties);
                needToAdd.add(groupDefinition);
            }
        }
        ComponentParametersView componentParametersView = new ComponentParametersView();
        componentParametersView.disableAll();
        componentParametersView.setIgnoreArtifacts(false);
        componentParametersView.setIgnoreGroups(false);
        componentParametersView.setIgnoreComponentInstances(false);
        Either<Resource, StorageOperationStatus> component = toscaOperationFacade
            .getToscaElement(updatedResource.getUniqueId(), componentParametersView);
        if (component.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        updatedResource = component.left().value();
        Either<List<GroupDefinition>, ResponseFormat> addGroups = groupBusinessLogic.addGroups(updatedResource, needToAdd, false);
        if (addGroups.isRight()) {
            return Either.right(addGroups.right().value());
        }
        return resStatus;
    }

    private Either<Component, ResponseFormat> createDeploymentArtifactsFromCsar(CsarInfo csarInfo, Component resource, Set<String> artifactsGroup,
                                                                                Set<String> artifactsUUIDGroup,
                                                                                ArtifactTemplateInfo artifactTemplateInfo,
                                                                                List<ArtifactDefinition> createdArtifacts,
                                                                                List<ArtifactDefinition> artifactsFromResource, int labelCounter) {
        Either<Component, ResponseFormat> resStatus = Either.left(resource);
        String artifactFileName = artifactTemplateInfo.getFileName();
        String artifactUid = "";
        String artifactUUID = "";
        String artifactEnvUid = "";
        boolean alreadyExist = false;
        // check if artifacts already exist
        if (artifactsFromResource != null && !artifactsFromResource.isEmpty()) {
            for (ArtifactDefinition artifactFromResource : artifactsFromResource) {
                if (artifactFromResource.getArtifactName().equals(artifactFileName)) {
                    artifactUid = artifactFromResource.getUniqueId();
                    artifactUUID = artifactFromResource.getArtifactUUID();
                    if (!artifactFromResource.getArtifactType().equalsIgnoreCase(artifactTemplateInfo.getType())) {
                        log.debug(ARTIFACT_WITH_NAME_AND_TYPE_ALREADY_EXIST_WITH_TYPE, artifactFileName, artifactTemplateInfo.getType(),
                            artifactFromResource.getArtifactType());
                        BeEcompErrorManager.getInstance().logInternalDataError(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME + artifactFileName,
                            ARTIFACT_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
                        return Either.right(componentsUtils
                            .getResponseFormat(ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, artifactFileName,
                                artifactTemplateInfo.getType(), artifactFromResource.getArtifactType()));
                    }
                    alreadyExist = true;
                    artifactEnvUid = checkAndGetHeatEnvId(artifactFromResource);
                    break;
                }
            }
        }
        if (!alreadyExist) {
            for (ArtifactDefinition createdArtifact : createdArtifacts) {
                if (createdArtifact.getArtifactName().equals(artifactFileName)) {
                    artifactUid = createdArtifact.getUniqueId();
                    artifactUUID = createdArtifact.getArtifactUUID();
                    if (!createdArtifact.getArtifactType().equalsIgnoreCase(artifactTemplateInfo.getType())) {
                        log.debug(ARTIFACT_WITH_NAME_AND_TYPE_ALREADY_EXIST_WITH_TYPE, artifactFileName, artifactTemplateInfo.getType(),
                            createdArtifact.getArtifactType());
                        BeEcompErrorManager.getInstance().logInternalDataError(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME + artifactFileName,
                            ARTIFACT_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
                        return Either.right(componentsUtils
                            .getResponseFormat(ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, artifactFileName,
                                artifactTemplateInfo.getType(), createdArtifact.getArtifactType()));
                    }
                    alreadyExist = true;
                    artifactEnvUid = checkAndGetHeatEnvId(createdArtifact);
                    break;
                }
            }
        }
        // if not exist need to create
        if (!alreadyExist) {
            Either<ArtifactDefinition, ResponseFormat> newArtifactEither = createDeploymentArtifact(csarInfo, resource, ARTIFACTS_PATH,
                artifactTemplateInfo, createdArtifacts, labelCounter);
            if (newArtifactEither.isRight()) {
                resStatus = Either.right(newArtifactEither.right().value());
                return resStatus;
            }
            ArtifactDefinition newArtifact = newArtifactEither.left().value();
            artifactUid = newArtifact.getUniqueId();
            artifactUUID = newArtifact.getArtifactUUID();
            final ArtifactTypeEnum artifactType = ArtifactTypeEnum.parse(newArtifact.getArtifactType());
            if (artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_NET || artifactType == ArtifactTypeEnum.HEAT_VOL) {
                ArtifactDefinition createHeatEnvPlaceHolder = artifactsBusinessLogic
                    .createHeatEnvPlaceHolder(createdArtifacts, newArtifact, ArtifactsBusinessLogic.HEAT_VF_ENV_NAME, resource.getUniqueId(),
                        NodeTypeEnum.Resource, resource.getName(), csarInfo.getModifier(), resource, null);
                artifactEnvUid = createHeatEnvPlaceHolder.getUniqueId();
            }
        }
        artifactsGroup.add(artifactUid);
        artifactsUUIDGroup.add(artifactUUID);
        if (!artifactEnvUid.isEmpty()) {
            artifactsGroup.add(artifactEnvUid);
        }
        List<ArtifactTemplateInfo> relatedArtifacts = artifactTemplateInfo.getRelatedArtifactsInfo();
        if (relatedArtifacts != null) {
            for (ArtifactTemplateInfo relatedArtifactTemplateInfo : relatedArtifacts) {
                resStatus = createDeploymentArtifactsFromCsar(csarInfo, resource, artifactsGroup, artifactsUUIDGroup, relatedArtifactTemplateInfo,
                    createdArtifacts, artifactsFromResource, labelCounter);
                if (resStatus.isRight()) {
                    return resStatus;
                }
            }
        }
        return resStatus;
    }

    private Either<Component, ResponseFormat> associateAndDissociateArtifactsToGroup(CsarInfo csarInfo, Component resource,
                                                                                     List<ArtifactDefinition> createdNewArtifacts, int labelCounter,
                                                                                     List<ArtifactDefinition> createdDeploymentArtifactsAfterDelete,
                                                                                     Map<GroupDefinition, MergedArtifactInfo> mergedgroup,
                                                                                     List<ArtifactDefinition> deletedArtifacts) {
        Map<GroupDefinition, List<ArtifactTemplateInfo>> artifactsToAssotiate = new HashMap<>();
        Map<GroupDefinition, List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>>> artifactsToUpdateMap = new HashMap<>();
        Either<Component, ResponseFormat> resEither;
        for (Entry<GroupDefinition, MergedArtifactInfo> entry : mergedgroup.entrySet()) {
            List<ArtifactDefinition> dissArtifactsInGroup = entry.getValue().getListToDissotiateArtifactFromGroup(deletedArtifacts);
            GroupDefinition grDef = entry.getKey();
            if (dissArtifactsInGroup != null && !dissArtifactsInGroup.isEmpty()) {
                for (ArtifactDefinition art : dissArtifactsInGroup) {
                    grDef.getArtifacts().remove(art.getUniqueId());
                    grDef.getArtifactsUuid().remove(art.getArtifactUUID());
                }
            }
            List<ArtifactTemplateInfo> newArtifactsInGroup = entry.getValue().getListToAssociateArtifactToGroup();
            if (newArtifactsInGroup != null && !newArtifactsInGroup.isEmpty()) {
                artifactsToAssotiate.put(entry.getKey(), newArtifactsInGroup);
            }
            List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>> artifactsToUpdate = entry.getValue().getListToUpdateArtifactInGroup();
            if (artifactsToUpdate != null && !artifactsToUpdate.isEmpty()) {
                artifactsToUpdateMap.put(entry.getKey(), artifactsToUpdate);
            }
        }
        if (!artifactsToUpdateMap.isEmpty()) {
            List<ArtifactDefinition> updatedArtifacts = new ArrayList<>();
            for (Entry<GroupDefinition, List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>>> artifactsToUpdateEntry : artifactsToUpdateMap
                .entrySet()) {
                List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>> artifactsToUpdateList = artifactsToUpdateEntry.getValue();
                GroupDefinition groupToUpdate = artifactsToUpdateEntry.getKey();
                for (ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo> artifact : artifactsToUpdateList) {
                    String prevUUID = artifact.getKey().getArtifactUUID();
                    String prevId = artifact.getKey().getUniqueId();
                    String prevHeatEnvId = checkAndGetHeatEnvId(artifact.getKey());
                    Either<ArtifactDefinition, ResponseFormat> updateArtifactEither = updateDeploymentArtifactsFromCsar(csarInfo, resource,
                        artifact.getKey(), artifact.getValue(), updatedArtifacts, artifact.getRight().getRelatedArtifactsInfo());
                    if (updateArtifactEither.isRight()) {
                        log.debug("failed to update artifacts. status is {}", updateArtifactEither.right().value());
                        resEither = Either.right(updateArtifactEither.right().value());
                        return resEither;
                    }
                    ArtifactDefinition artAfterUpdate = updateArtifactEither.left().value();
                    if (!prevUUID.equals(artAfterUpdate.getArtifactUUID()) || !prevId.equals(artAfterUpdate.getUniqueId())) {
                        groupToUpdate.getArtifacts().remove(prevId);
                        groupToUpdate.getArtifactsUuid().remove(prevUUID);
                        groupToUpdate.getArtifacts().add(artAfterUpdate.getUniqueId());
                        groupToUpdate.getArtifactsUuid().add(artAfterUpdate.getArtifactUUID());
                    }
                    Optional<ArtifactDefinition> op = updatedArtifacts.stream()
                        .filter(p -> p.getGeneratedFromId() != null && p.getGeneratedFromId().equals(artAfterUpdate.getUniqueId())).findAny();
                    if (op.isPresent()) {
                        ArtifactDefinition artifactInfoHeatEnv = op.get();
                        groupToUpdate.getArtifacts().remove(prevHeatEnvId);
                        groupToUpdate.getArtifacts().add(artifactInfoHeatEnv.getUniqueId());
                    }
                }
            }
        }
        for (Entry<GroupDefinition, List<ArtifactTemplateInfo>> associateEntry : artifactsToAssotiate.entrySet()) {
            List<ArtifactTemplateInfo> associatedArtifact = associateEntry.getValue();
            Set<String> arifactsUids = new HashSet<>();
            Set<String> arifactsUuids = new HashSet<>();
            for (ArtifactTemplateInfo artifactTemplate : associatedArtifact) { // try

                // to

                // find

                // artifact

                // in

                // resource
                boolean isCreate = true;
                for (ArtifactDefinition createdArtifact : createdDeploymentArtifactsAfterDelete) {
                    if (artifactTemplate.getFileName().equalsIgnoreCase(createdArtifact.getArtifactName())) {
                        arifactsUids.add(createdArtifact.getUniqueId());
                        arifactsUuids.add(createdArtifact.getArtifactUUID());
                        isCreate = false;
                        String heatEnvId = checkAndGetHeatEnvId(createdArtifact);
                        if (!heatEnvId.isEmpty()) {
                            arifactsUids.add(heatEnvId);
                            Optional<ArtifactDefinition> op = createdDeploymentArtifactsAfterDelete.stream()
                                .filter(p -> p.getUniqueId().equals(heatEnvId)).findAny();
                            if (op.isPresent()) {
                                this.artifactToscaOperation.updateHeatEnvPlaceholder(op.get(), resource, resource.getComponentType().getNodeType());
                            }
                        }
                        break;
                    }
                }
                if (isCreate) { // check if already created
                    for (ArtifactDefinition createdNewArtifact : createdNewArtifacts) {
                        if (artifactTemplate.getFileName().equalsIgnoreCase(createdNewArtifact.getArtifactName())) {
                            arifactsUids.add(createdNewArtifact.getUniqueId());
                            arifactsUuids.add(createdNewArtifact.getArtifactUUID());
                            isCreate = false;
                            String heatEnvId = checkAndGetHeatEnvId(createdNewArtifact);
                            if (!heatEnvId.isEmpty()) {
                                arifactsUids.add(heatEnvId);
                            }
                            break;
                        }
                    }
                }
                if (isCreate) {
                    Either<ArtifactDefinition, ResponseFormat> createArtifactEither = createDeploymentArtifact(csarInfo, resource, ARTIFACTS_PATH,
                        artifactTemplate, createdNewArtifacts, labelCounter);
                    if (createArtifactEither.isRight()) {
                        resEither = Either.right(createArtifactEither.right().value());
                        return resEither;
                    }
                    ArtifactDefinition createdArtifact = createArtifactEither.left().value();
                    arifactsUids.add(createdArtifact.getUniqueId());
                    arifactsUuids.add(createdArtifact.getArtifactUUID());
                    final ArtifactTypeEnum artifactType = ArtifactTypeEnum.parse(createdArtifact.getArtifactType());
                    if (artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_NET
                        || artifactType == ArtifactTypeEnum.HEAT_VOL) {
                        ArtifactDefinition createHeatEnvPlaceHolder = artifactsBusinessLogic
                            .createHeatEnvPlaceHolder(new ArrayList<>(), createdArtifact, ArtifactsBusinessLogic.HEAT_VF_ENV_NAME,
                                resource.getUniqueId(), NodeTypeEnum.Resource, resource.getName(), csarInfo.getModifier(), resource, null);
                        String heatEnvId = createHeatEnvPlaceHolder.getUniqueId();
                        arifactsUids.add(heatEnvId);
                    }
                }
            }
            if (arifactsUids != null && !arifactsUids.isEmpty()) {
                List<String> artifactsToAssociate = new ArrayList<>();
                artifactsToAssociate.addAll(arifactsUids);
                GroupDefinition assotiateGroup = associateEntry.getKey();
                assotiateGroup.getArtifacts().addAll(arifactsUids);
                assotiateGroup.getArtifactsUuid().addAll(arifactsUuids);
            }
        }
        ComponentParametersView parametersView = new ComponentParametersView();
        parametersView.disableAll();
        parametersView.setIgnoreComponentInstances(false);
        parametersView.setIgnoreUsers(false);
        parametersView.setIgnoreArtifacts(false);
        parametersView.setIgnoreGroups(false);
        Either<Resource, StorageOperationStatus> eitherGerResource = toscaOperationFacade.getToscaElement(resource.getUniqueId(), parametersView);
        if (eitherGerResource.isRight()) {
            ResponseFormat responseFormat = componentsUtils
                .getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), resource,
                    resource.getComponentType());
            resEither = Either.right(responseFormat);
            return resEither;
        }
        resEither = Either.left(eitherGerResource.left().value());
        return resEither;
    }

    private Either<ArtifactDefinition, ResponseFormat> updateDeploymentArtifactsFromCsar(CsarInfo csarInfo, Component resource,
                                                                                         ArtifactDefinition oldArtifact,
                                                                                         ArtifactTemplateInfo artifactTemplateInfo,
                                                                                         List<ArtifactDefinition> updatedArtifacts,
                                                                                         List<ArtifactTemplateInfo> updatedRequiredArtifacts) {
        String artifactFileName = artifactTemplateInfo.getFileName();
        // check if artifacts already exist
        for (ArtifactDefinition updatedArtifact : updatedArtifacts) {
            if (updatedArtifact.getArtifactName().equals(artifactFileName)) {
                if (!updatedArtifact.getArtifactType().equalsIgnoreCase(artifactTemplateInfo.getType())) {
                    log.debug("Artifact with name {} and type {} already updated with type  {}", artifactFileName, artifactTemplateInfo.getType(),
                        updatedArtifact.getArtifactType());
                    BeEcompErrorManager.getInstance()
                        .logInternalDataError(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME + artifactFileName, ARTIFACT_INTERNALS_ARE_INVALID,
                            ErrorSeverity.ERROR);
                    return Either.right(componentsUtils
                        .getResponseFormat(ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, artifactFileName,
                            artifactTemplateInfo.getType(), updatedArtifact.getArtifactType()));
                }
                return Either.left(updatedArtifact);
            }
        }
        Either<ImmutablePair<String, byte[]>, ResponseFormat> artifactContententStatus = CsarValidationUtils
            .getArtifactContent(csarInfo.getCsarUUID(), csarInfo.getCsar(), CsarUtils.ARTIFACTS_PATH + artifactFileName, artifactFileName,
                componentsUtils);
        if (artifactContententStatus.isRight()) {
            return Either.right(artifactContententStatus.right().value());
        }
        Map<String, Object> json = ArtifactUtils
            .buildJsonForUpdateArtifact(oldArtifact.getUniqueId(), artifactFileName, oldArtifact.getArtifactType(), ArtifactGroupTypeEnum.DEPLOYMENT,
                oldArtifact.getArtifactLabel(), oldArtifact.getArtifactDisplayName(), oldArtifact.getDescription(),
                artifactContententStatus.left().value().getRight(), updatedRequiredArtifacts, oldArtifact.getIsFromCsar());
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> uploadArtifactToService = createOrUpdateCsarArtifactFromJson(resource,
            csarInfo.getModifier(), json, new ArtifactOperationInfo(false, false, ArtifactOperationEnum.UPDATE));
        if (uploadArtifactToService.isRight()) {
            return Either.right(uploadArtifactToService.right().value());
        }
        ArtifactDefinition previousInfo = uploadArtifactToService.left().value().left().value();
        ArtifactDefinition currentInfo = uploadArtifactToService.left().value().left().value();
        updatedArtifacts.add(currentInfo);
        Either<ArtifactDefinition, ResponseFormat> updateEnvEither = updateHeatParamsFromCsar(resource, csarInfo, artifactTemplateInfo, currentInfo,
            true);
        if (updateEnvEither.isRight()) {
            log.debug("failed to update parameters to artifact {}", artifactFileName);
            return Either.right(updateEnvEither.right().value());
        }
        artifactsBusinessLogic.updateGroupForHeat(previousInfo, updateEnvEither.left().value(), resource);
        updatedArtifacts.add(updateEnvEither.left().value());
        return Either.left(currentInfo);
    }

    public Either<Resource, ResponseFormat> deleteVFModules(Resource resource, CsarInfo csarInfo, boolean shouldLock, boolean inTransaction) {
        Resource updatedResource = resource;
        List<GroupDefinition> groupsToDelete = updatedResource.getGroups();
        if (groupsToDelete != null && !groupsToDelete.isEmpty()) {
            List<GroupDefinition> vfGroupsToDelete = groupsToDelete.stream().filter(g -> g.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE))
                .collect(Collectors.toList());
            if (!vfGroupsToDelete.isEmpty()) {
                for (GroupDefinition gr : vfGroupsToDelete) {
                    List<String> artifacts = gr.getArtifacts();
                    for (String artifactId : artifacts) {
                        Either<ArtifactDefinition, ResponseFormat> handleDelete = artifactsBusinessLogic
                            .handleDelete(updatedResource.getUniqueId(), artifactId, csarInfo.getModifier(), updatedResource, shouldLock,
                                inTransaction);
                        if (handleDelete.isRight()) {
                            log.debug("Couldn't delete  artifact {}", artifactId);
                            return Either.right(handleDelete.right().value());
                        }
                    }
                }
                groupBusinessLogic.deleteGroups(updatedResource, vfGroupsToDelete);
                Either<Resource, StorageOperationStatus> eitherGetResource = toscaOperationFacade.getToscaElement(updatedResource.getUniqueId());
                if (eitherGetResource.isRight()) {
                    ResponseFormat responseFormat = componentsUtils
                        .getResponseFormatByResource(componentsUtils.convertFromStorageResponse(eitherGetResource.right().value()), updatedResource);
                    return Either.right(responseFormat);
                }
                updatedResource = eitherGetResource.left().value();
            }
        }
        return Either.left(updatedResource);
    }

    public Either<Service, ResponseFormat> deleteVFModules(Service resource, CsarInfo csarInfo, boolean shouldLock, boolean inTransaction) {
        Service updatedResource = resource;
        List<GroupDefinition> groupsToDelete = updatedResource.getGroups();
        if (groupsToDelete != null && !groupsToDelete.isEmpty()) {
            List<GroupDefinition> vfGroupsToDelete = groupsToDelete.stream().filter(g -> g.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE))
                .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(vfGroupsToDelete)) {
                Either<Service, ResponseFormat> eitherGetResource = deleteVfGroups(vfGroupsToDelete, updatedResource, csarInfo, shouldLock,
                    inTransaction);
                if (eitherGetResource.isRight()) {
                    return Either.right(eitherGetResource.right().value());
                }
                updatedResource = eitherGetResource.left().value();
            }
        }
        return Either.left(updatedResource);
    }

    private Either<Service, ResponseFormat> deleteVfGroups(List<GroupDefinition> vfGroupsToDelete, Service resource, CsarInfo csarInfo,
                                                           boolean shouldLock, boolean inTransaction) {
        if (vfGroupsToDelete != null && !vfGroupsToDelete.isEmpty()) {
            for (GroupDefinition gr : vfGroupsToDelete) {
                List<String> artifacts = gr.getArtifacts();
                for (String artifactId : artifacts) {
                    Either<ArtifactDefinition, ResponseFormat> handleDelete = artifactsBusinessLogic
                        .handleDelete(resource.getUniqueId(), artifactId, csarInfo.getModifier(), resource, shouldLock, inTransaction);
                    if (handleDelete.isRight()) {
                        log.debug("Couldn't delete  artifact {}", artifactId);
                        return Either.right(handleDelete.right().value());
                    }
                }
            }
            groupBusinessLogic.deleteGroups(resource, vfGroupsToDelete);
            Either<Service, StorageOperationStatus> eitherGetResource = toscaOperationFacade.getToscaElement(resource.getUniqueId());
            if (eitherGetResource.isRight()) {
                return Either.right(componentsUtils.getResponseFormatByComponent(
                    componentsUtils.convertFromStorageResponse(eitherGetResource.right().value()), resource, resource.getComponentType()));
            }
            return Either.left(eitherGetResource.left().value());
        }
        return Either.right(componentsUtils.getResponseFormatByComponent(ActionStatus.INVALID_CONTENT, resource, resource.getComponentType()));
    }

    private Either<? extends Component, ResponseFormat> getResourcetFromGraph(Component component) {
        log.debug("getResource start");
        return toscaOperationFacade.getToscaElement(component.getUniqueId()).right().map(rf -> componentsUtils
            .getResponseFormatByComponent(componentsUtils.convertFromStorageResponse(rf), component, component.getComponentType())).left()
            .map(c -> c);
    }
}
