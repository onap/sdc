package org.openecomp.sdc.be.components.csar;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fj.data.Either;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.BaseBusinessLogic;
import org.openecomp.sdc.be.components.impl.CsarValidationUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.config.Configuration.VfModuleProperty;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.ArtifactUtils;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.ArtifactTemplateInfo;
import org.openecomp.sdc.be.info.MergedArtifactInfo;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.heat.HeatParameterType;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.openecomp.sdc.be.tosca.CsarUtils.ARTIFACTS_PATH;


@org.springframework.stereotype.Component("csarArtifactsAndGroupsBusinessLogic")
public class CsarArtifactsAndGroupsBusinessLogic extends BaseBusinessLogic {

    private static final Logger log = Logger.getLogger(CsarArtifactsAndGroupsBusinessLogic.class.getName());
    public static final String ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMATR_FILE_NAME = "Artifact  file is not in expected formatr, fileName  {}";
    public static final String ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME = "Artifact  file is not in expected format, fileName  {}";
    public static final String ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMATR_FILE_NAME1 = "Artifact  file is not in expected formatr, fileName ";
    public static final String ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME1 = "Artifact  file is not in expected format, fileName ";
    public static final String ARTIFACT_INTERNALS_ARE_INVALID = "Artifact internals are invalid";
    public static final String ARTIFACT_WITH_NAME_AND_TYPE_ALREADY_EXIST_WITH_TYPE = "Artifact with name {} and type {} already exist with type  {}";
    private final Gson gson = new Gson();
    private static final Pattern pattern = Pattern.compile("\\..(.*?)\\..");
    @Autowired
    protected ArtifactsBusinessLogic artifactsBusinessLogic;

    public Either<Resource, ResponseFormat> createResourceArtifactsFromCsar(CsarInfo csarInfo, Resource resource,
            String artifactsMetaFile, String artifactsMetaFileName, List<ArtifactDefinition> createdArtifacts,
            boolean shouldLock, boolean inTransaction) {

        log.debug("parseResourceArtifactsInfoFromFile start");
        return  parseResourceArtifactsInfoFromFile(resource, artifactsMetaFile, artifactsMetaFileName)
                .left()
                .bind( p-> createResourceArtifacts(csarInfo, resource, p, createdArtifacts,shouldLock, inTransaction))
                .right()
                .map(rf -> { componentsUtils.auditResource(rf, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE); return rf;})
                .left()
                .bind(this::getResourcetFromGraph);
    }


    public Either<Resource, ResponseFormat> updateResourceArtifactsFromCsar(CsarInfo csarInfo, Resource resource,
            String artifactsMetaFile, String artifactsMetaFileName, List<ArtifactDefinition> createdNewArtifacts,
            boolean shouldLock, boolean inTransaction){

        Resource updatedResource = resource;

        Either<Map<String, List<ArtifactTemplateInfo>>, ResponseFormat> parseResourceInfoFromYamlEither = parseResourceArtifactsInfoFromFile(
                updatedResource, artifactsMetaFile, artifactsMetaFileName);
        if (parseResourceInfoFromYamlEither.isRight()) {
            ResponseFormat responseFormat = parseResourceInfoFromYamlEither.right().value();
            componentsUtils.auditResource(responseFormat, csarInfo.getModifier(), resource, AuditingActionEnum.IMPORT_RESOURCE);
            return Either.right(responseFormat);
        }

        List<GroupDefinition> groups = updatedResource.getGroups();
        Map<String, ArtifactDefinition> deplymentArtifact = updatedResource.getDeploymentArtifacts();
        if (deplymentArtifact == null || deplymentArtifact.isEmpty()) {
            if(groups != null){
                List<GroupDefinition> listToDelete =  groups.stream().filter(g -> g.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)).collect(Collectors.toList());
                groupBusinessLogic.deleteGroups(updatedResource, listToDelete);
            }
            return createResourceArtifacts(csarInfo, updatedResource, parseResourceInfoFromYamlEither.left().value(),
                    createdNewArtifacts, shouldLock, inTransaction);
        }

        List<ArtifactDefinition> createdDeplymentArtifactsAfterDelete = deplymentArtifact.values().stream().collect(Collectors.toList());

        int labelCounter = createdDeplymentArtifactsAfterDelete.size();


        ////////////////////////////////////// create set parsed
        ////////////////////////////////////// artifacts///////////////////////////////////////////
        Map<String, List<ArtifactTemplateInfo>> parsedArtifactsMap = parseResourceInfoFromYamlEither.left().value();


        List<ArtifactTemplateInfo> artifactsWithoutGroups = null;
        if (parsedArtifactsMap.containsKey(ArtifactTemplateInfo.CSAR_ARTIFACT)) {
            artifactsWithoutGroups = parsedArtifactsMap.get(ArtifactTemplateInfo.CSAR_ARTIFACT);
            parsedArtifactsMap.remove(ArtifactTemplateInfo.CSAR_ARTIFACT);
        }
        Collection<List<ArtifactTemplateInfo>> parsedArifactsCollection = parsedArtifactsMap.values();

        Either<Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>>, ResponseFormat> parsedArtifactsPerGroupEither = createArtifactsTemplateCollection(csarInfo, updatedResource, createdNewArtifacts, shouldLock, inTransaction,
                createdDeplymentArtifactsAfterDelete, labelCounter, parsedArifactsCollection);
        if(parsedArtifactsPerGroupEither.isRight()){
            log.error("Failed to parse artifacts. Status is {} ", parsedArtifactsPerGroupEither.right().value());
            return Either.right(parsedArtifactsPerGroupEither.right().value());
        }

        Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroup = parsedArtifactsPerGroupEither.left().value();

        // find master in group
        Map<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupArtifact = findMasterArtifactInGroup(
                groups, deplymentArtifact);

        ///////////////////////////////// find artifacts to
        ///////////////////////////////// delete////////////////////////////////////////////////////


        Map<String, List<ArtifactDefinition>> groupToDelete = new HashMap<>();
        Set<ArtifactDefinition> artifactsToDelete = findArtifactThatNotInGroupToDelete(parsedGroup, createdDeplymentArtifactsAfterDelete);

        Set<ArtifactTemplateInfo> jsonMasterArtifacts = parsedGroup.keySet();
        Map<GroupDefinition, MergedArtifactInfo> mergedgroup = mergeGroupInUpdateFlow(groupArtifact, parsedGroup,
                artifactsToDelete, groupToDelete, jsonMasterArtifacts, createdDeplymentArtifactsAfterDelete);

        List<ArtifactDefinition> deletedArtifacts = new ArrayList<>();;
        Either<Resource, ResponseFormat> deletedArtifactsEither = deleteArtifactsInUpdateCsarFlow(
                updatedResource, csarInfo.getModifier(), shouldLock, inTransaction, artifactsToDelete, groupToDelete, deletedArtifacts);
        if (deletedArtifactsEither.isRight()) {
            log.debug("Failed to delete artifacts. Status is {} ", deletedArtifactsEither.right().value());

            return Either.right(deletedArtifactsEither.right().value());

        }
        updatedResource = deletedArtifactsEither.left().value();

        // need to update resource if we updated artifacts
        if (!deletedArtifacts.isEmpty()) {
            for (ArtifactDefinition deletedArtifact : deletedArtifacts) {
                ArtifactDefinition artToRemove = null;
                for (ArtifactDefinition artFromResource : createdDeplymentArtifactsAfterDelete) {
                    if (deletedArtifact.getUniqueId().equalsIgnoreCase(artFromResource.getUniqueId())) {
                        artToRemove = artFromResource;
                        break;
                    }
                }
                if (artToRemove != null) {
                    createdDeplymentArtifactsAfterDelete.remove(artToRemove);
                }

            }
        }

        ////////////// dissociate, associate or create
        ////////////// artifacts////////////////////////////
        Either<Resource, ResponseFormat> assDissotiateEither = associateAndDissociateArtifactsToGroup(csarInfo,
                updatedResource, createdNewArtifacts, labelCounter, inTransaction,
                createdDeplymentArtifactsAfterDelete, mergedgroup, deletedArtifacts);
        groups = updatedResource.getGroups();
        if (assDissotiateEither.isRight()) {
            log.debug("Failed to delete artifacts. Status is {} ", assDissotiateEither.right().value());

            return Either.right(assDissotiateEither.right().value());

        }
        updatedResource = assDissotiateEither.left().value();
        deplymentArtifact = updatedResource.getDeploymentArtifacts();
        createdDeplymentArtifactsAfterDelete.clear();
        if (deplymentArtifact != null && !deplymentArtifact.isEmpty()) {
            for (Entry<String, ArtifactDefinition> entry : deplymentArtifact.entrySet()) {
                createdDeplymentArtifactsAfterDelete.add(entry.getValue());
            }
        }

        // update vfModule names
        Set<GroupDefinition> groupForAssociateWithMembers = mergedgroup.keySet();
        if (groups != null && !groups.isEmpty()) {
            Either<List<GroupDefinition>, ResponseFormat> validateUpdateVfGroupNamesRes = groupBusinessLogic
                    .validateUpdateVfGroupNamesOnGraph(groups, updatedResource);
            if (validateUpdateVfGroupNamesRes.isRight()) {
                return Either.right(validateUpdateVfGroupNamesRes.right().value());
            }
            List<GroupDefinition> heatGroups = null;

            heatGroups = groups.stream().filter(e -> e.getMembers() != null).collect(Collectors.toList());

            for (GroupDefinition updatedGroupDef : groupForAssociateWithMembers) {

                if (updatedGroupDef.getMembers() != null && !updatedGroupDef.getMembers().isEmpty()) {
                    updatedGroupDef.getMembers().clear();
                }
                Map<String, String> members = new HashMap<>();
                Set<String> artifactsGroup = new HashSet<>();
                artifactsGroup.addAll(updatedGroupDef.getArtifacts());
                associateMembersToArtifacts(createdNewArtifacts, createdDeplymentArtifactsAfterDelete, heatGroups,
                        artifactsGroup, members);
                if (!members.isEmpty()) {
                    updatedGroupDef.setMembers(members);

                }

            }

        }

        //////////////// create new artifacts in update
        //////////////// flow////////////////////////////
        List<ArtifactTemplateInfo> newArtifactsGroup = new ArrayList<>();

        for (Entry<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroupSetEntry : parsedGroup.entrySet()) {
            ArtifactTemplateInfo parsedArtifactMaster = parsedGroupSetEntry.getKey();
            boolean isNewGroup = true;
            for (Entry<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupListEntry : groupArtifact
                    .entrySet()) {
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
        if (!newArtifactsGroup.isEmpty()) {
            Collections.sort(newArtifactsGroup, ArtifactTemplateInfo::compareByGroupName);
            int startGroupCounter = groupBusinessLogic.getNextVfModuleNameCounter(groups);
            Either<Boolean, ResponseFormat> validateGroupNamesRes = groupBusinessLogic
                    .validateGenerateVfModuleGroupNames(newArtifactsGroup, updatedResource.getSystemName(), startGroupCounter);
            if (validateGroupNamesRes.isRight()) {
                return Either.right(validateGroupNamesRes.right().value());
            }
            Either<Resource, ResponseFormat> resStatus = createGroupDeploymentArtifactsFromCsar(csarInfo, updatedResource,
                    newArtifactsGroup, createdNewArtifacts, createdDeplymentArtifactsAfterDelete, labelCounter,
                    shouldLock, inTransaction);
            if (resStatus.isRight()) {
                return resStatus;
            }
        }

        // updatedGroup
        if (!groupForAssociateWithMembers.isEmpty()) {

            List<GroupDefinition> groupsId = groupForAssociateWithMembers.stream().map(e -> e)
                    .collect(Collectors.toList());

            Either<List<GroupDefinition>, ResponseFormat> updateVersionEither = groupBusinessLogic
                    .updateGroups(updatedResource, groupsId, true);
            if (updateVersionEither.isRight()) {
                log.debug("Failed to update groups version. Status is {} ", updateVersionEither.right().value());

                return Either.right(updateVersionEither.right().value());

            }
        }
        if (artifactsWithoutGroups != null && !artifactsWithoutGroups.isEmpty()) {
            for (ArtifactTemplateInfo t : artifactsWithoutGroups) {
                List<ArtifactTemplateInfo> arrtifacts = new ArrayList<>();
                arrtifacts.add(t);
                Either<Resource, ResponseFormat> resStatus = createGroupDeploymentArtifactsFromCsar(csarInfo, updatedResource,
                        arrtifacts, createdNewArtifacts, createdDeplymentArtifactsAfterDelete, labelCounter, shouldLock,
                        inTransaction);
                if (resStatus.isRight()) {
                    return resStatus;
                }
            }

        }

        Either<Resource, StorageOperationStatus> eitherGerResource = toscaOperationFacade
                .getToscaElement(updatedResource.getUniqueId());
        if (eitherGerResource.isRight()) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(
                    componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), updatedResource);

            return Either.right(responseFormat);

        }
        return Either.left(eitherGerResource.left().value());
    }

    private Set<ArtifactDefinition> findArtifactThatNotInGroupToDelete(
            Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroup,
            List<ArtifactDefinition> createdDeplymentArtifactsAfterDelete) {
        Set<ArtifactDefinition> artifactsToDelete = new HashSet<>();
        for (Entry<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroupSetEntry : parsedGroup.entrySet()) {
            Set<ArtifactTemplateInfo> artifactsNames = parsedGroupSetEntry.getValue();
            for (ArtifactTemplateInfo template : artifactsNames) {
                if(template.getType().equals(ArtifactTypeEnum.HEAT_ARTIFACT.getType())){
                    Optional<ArtifactDefinition> op = createdDeplymentArtifactsAfterDelete.stream().filter(a -> a.getArtifactName().equalsIgnoreCase(template.getFileName())).findAny();
                    if(op.isPresent()){
                        if(!op.get().getArtifactType().equalsIgnoreCase(template.getType())){
                            artifactsToDelete.add(op.get());
                        }
                            
                    }
                } 
            }
        }
	        
        return artifactsToDelete;
    }


    private Either<Resource, ResponseFormat> createResourceArtifacts(CsarInfo csarInfo, Resource resource,
            Map<String, List<ArtifactTemplateInfo>> artifactsMap,
            List<ArtifactDefinition> createdArtifacts, boolean shouldLock, boolean inTransaction) {

        Either<Resource, ResponseFormat> resStatus = Either.left(resource);

        Collection<List<ArtifactTemplateInfo>> arifactsCollection = artifactsMap.values();

        for (List<ArtifactTemplateInfo> groupTemplateList : arifactsCollection) {
            if (groupTemplateList != null) {
                resStatus = createGroupDeploymentArtifactsFromCsar(csarInfo, resource, groupTemplateList,
                        createdArtifacts, 0, shouldLock, inTransaction);
                if (resStatus.isRight()) {
                    return resStatus;
                }
            }
        }

        return resStatus;

    }


    private Either<Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>>, ResponseFormat> createArtifactsTemplateCollection(CsarInfo csarInfo, Resource resource,
            List<ArtifactDefinition> createdNewArtifacts, boolean shouldLock, boolean inTransaction,
            List<ArtifactDefinition> createdDeplymentArtifactsAfterDelete, int labelCounter,
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
                    Either<Resource, ResponseFormat> resStatus = createGroupDeploymentArtifactsFromCsar(csarInfo,
                            resource, arrtifacts, createdNewArtifacts, createdDeplymentArtifactsAfterDelete,
                            labelCounter, shouldLock, inTransaction);
                    if (resStatus.isRight()) {
                        return Either.right(resStatus.right().value());
                    }

                }
            }

        }
        return Either.left(parsedGroup);
    }

    @SuppressWarnings({ "unchecked", "static-access" })
    public Either<Map<String, List<ArtifactTemplateInfo>>, ResponseFormat> parseResourceArtifactsInfoFromFile(
            Resource resource, String artifactsMetaFile, String artifactFileName) {

        try {
            JsonObject jsonElement = new JsonObject();
            jsonElement = gson.fromJson(artifactsMetaFile, jsonElement.getClass());

            JsonElement importStructureElement = jsonElement.get(Constants.IMPORT_STRUCTURE);
            if (importStructureElement == null || importStructureElement.isJsonNull()) {
                log.debug(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMATR_FILE_NAME, artifactFileName);
                BeEcompErrorManager.getInstance().logInternalDataError(
                        ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMATR_FILE_NAME1 + artifactFileName,
                        ARTIFACT_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
                return Either
                        .right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, artifactFileName));
            }

            Map<String, List<Map<String, Object>>> artifactTemplateMap = new HashMap<>();
            artifactTemplateMap = ComponentsUtils.parseJsonToObject(importStructureElement.toString(), HashMap.class);
            if (artifactTemplateMap.isEmpty()) {
                log.debug(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMATR_FILE_NAME, artifactFileName);
                BeEcompErrorManager.getInstance().logInternalDataError(
                        ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMATR_FILE_NAME1 + artifactFileName,
                        ARTIFACT_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
                return Either
                        .right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, artifactFileName));
            }

            Set<String> artifactsTypeKeys = artifactTemplateMap.keySet();
            Map<String, List<ArtifactTemplateInfo>> artifactsMap = new HashMap<>();
            List<ArtifactTemplateInfo> allGroups = new ArrayList<>();
            for (String artifactsTypeKey : artifactsTypeKeys) {

                Either <List<ArtifactTemplateInfo>, ResponseFormat> artifactTemplateInfoListEither = parseArtifactTemplateList(artifactFileName,
                        artifactTemplateMap, allGroups, artifactsTypeKey);
                if(artifactTemplateInfoListEither.isRight()){
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
            BeEcompErrorManager.getInstance().logInternalDataError(
                    ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME1 + artifactFileName,
                    ARTIFACT_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, artifactFileName));
        }

    }

    private Either< List<ArtifactTemplateInfo>, ResponseFormat> parseArtifactTemplateList(String artifactFileName,
            Map<String, List<Map<String, Object>>> artifactTemplateMap, List<ArtifactTemplateInfo> allGroups,
            String artifactsTypeKey) {
        List<Map<String, Object>> o = artifactTemplateMap.get(artifactsTypeKey);
        Either<List<ArtifactTemplateInfo>, ResponseFormat> artifactTemplateInfoListPairStatus = createArtifactTemplateInfoModule(
                artifactsTypeKey, o);
        if (artifactTemplateInfoListPairStatus.isRight()) {
            log.debug(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMATR_FILE_NAME, artifactFileName);
            BeEcompErrorManager.getInstance().logInternalDataError(
                    ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME1 + artifactFileName,
                    ARTIFACT_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
            return Either.right(artifactTemplateInfoListPairStatus.right().value());
        }
        List<ArtifactTemplateInfo> artifactTemplateInfoList = artifactTemplateInfoListPairStatus.left().value();
        if (artifactTemplateInfoList == null) {
            log.debug(ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMATR_FILE_NAME, artifactFileName);
            BeEcompErrorManager.getInstance().logInternalDataError(
                    ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMAT_FILE_NAME1 + artifactFileName,
                    ARTIFACT_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
            return Either.right(
                    componentsUtils.getResponseFormat(ActionStatus.CSAR_INVALID_FORMAT, artifactFileName));

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
                    .createArtifactTemplateInfoFromJson(componentsUtils, artifactsTypeKey, o, artifactTemplateInfoList,
                            null);
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



    private Either<Resource, ResponseFormat> createGroupDeploymentArtifactsFromCsar(CsarInfo csarInfo,
            Resource resource, List<ArtifactTemplateInfo> artifactsTemplateList,
            List<ArtifactDefinition> createdArtifacts, int labelCounter, boolean shouldLock, boolean inTransaction) {
        Either<Resource, ResponseFormat> resStatus = Either.left(resource);
        List<GroupDefinition> createdGroups = resource.getGroups();
        List<GroupDefinition> heatGroups = null;
        if (createdGroups != null && !createdGroups.isEmpty()) {

            heatGroups = createdGroups.stream().filter(e -> e.getMembers() != null).collect(Collectors.toList());

        }
        List<GroupDefinition> needToCreate = new ArrayList<>();
        for (ArtifactTemplateInfo groupTemplateInfo : artifactsTemplateList) {
            String groupName = groupTemplateInfo.getGroupName();
            Set<String> artifactsGroup = new HashSet<>();
            Set<String> artifactsUUIDGroup = new HashSet<>();

            log.debug("createDeploymentArtifactsFromCsar start");
            resStatus = createDeploymentArtifactFromCsar(csarInfo, ARTIFACTS_PATH, resource, artifactsGroup,
                    artifactsUUIDGroup, groupTemplateInfo, createdArtifacts, labelCounter, shouldLock, inTransaction);
            log.debug("createDeploymentArtifactsFromCsar end");
            if (resStatus.isRight()) {
                return resStatus;
            }
            if (groupName != null && !groupName.isEmpty()) {

                Either<GroupDefinition, ResponseFormat> groupDefinitionEither = buildGroupDefinition(createdArtifacts, heatGroups, groupTemplateInfo,
                        groupName, artifactsGroup, artifactsUUIDGroup);
                if (groupDefinitionEither.isRight()) {
                    return Either.right(groupDefinitionEither.right().value());
                }
                needToCreate.add(groupDefinitionEither.left().value());
            }
        }

        ComponentParametersView componentParametersView = new ComponentParametersView();
        componentParametersView.disableAll();
        componentParametersView.setIgnoreUsers(false);
        componentParametersView.setIgnoreArtifacts(false);
        componentParametersView.setIgnoreGroups(false);

        componentParametersView.setIgnoreComponentInstances(false);

        Either<Resource, StorageOperationStatus> component = toscaOperationFacade
                .getToscaElement(resource.getUniqueId(), componentParametersView);

        if (component.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

        Either<List<GroupDefinition>, ResponseFormat> createGroups = groupBusinessLogic
                .addGroups(component.left().value(), needToCreate, false);
        if (createGroups.isRight()) {
            return Either.right(createGroups.right().value());
        }

        return Either.left(component.left().value());
    }

    private Either<GroupDefinition, ResponseFormat>  buildGroupDefinition(List<ArtifactDefinition> createdArtifacts,
            List<GroupDefinition> heatGroups, ArtifactTemplateInfo groupTemplateInfo, String groupName,
            Set<String> artifactsGroup, Set<String> artifactsUUIDGroup) {

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
                .getLatestGroupTypeByType(Constants.DEFAULT_GROUP_VF_MODULE, true);
        if (getLatestGroupTypeRes.isRight()) {
            return Either.right(componentsUtils.getResponseFormat(
                    componentsUtils.convertFromStorageResponse(getLatestGroupTypeRes.right().value())));
        }
        properties = createVfModuleAdditionalProperties(groupTemplateInfo.isBase(), groupName, properties,
                createdArtifacts, artifactsList, getLatestGroupTypeRes.left().value());
        groupDefinition.convertFromGroupProperties(properties);
        log.debug("createGroup start");
        return Either.left(groupDefinition);
    }

    private Either<Resource, ResponseFormat> createDeploymentArtifactFromCsar(CsarInfo csarInfo, String artifactPath,
            Resource resource, Set<String> artifactsGroup, Set<String> artifactsUUIDGroup,
            ArtifactTemplateInfo artifactTemplateInfo, List<ArtifactDefinition> createdArtifacts, int labelCounter,
            boolean shoudLock, boolean inTransaction) {
        Either<Resource, ResponseFormat> resStatus = Either.left(resource);

        String artifactUid = "";
        String artifactEnvUid = "";
        String artifactUUID = "";


        // check if artifacts already exist
        Either<ArtifactDefinition, ResponseFormat> createdArtifactEther = checkIfArtifactAlreadyExist(artifactTemplateInfo, createdArtifacts);
        if(createdArtifactEther.isRight()){
            return Either.right(createdArtifactEther.right().value());
        }
        ArtifactDefinition createdArtifact = createdArtifactEther.left().value();
        if(createdArtifact == null){

            Either<ArtifactDefinition, ResponseFormat> newArtifactEither = createDeploymentArtifact(csarInfo, resource,
                    artifactPath, artifactTemplateInfo, createdArtifacts, labelCounter);
            if (newArtifactEither.isRight()) {
                resStatus = Either.right(newArtifactEither.right().value());
                return resStatus;
            }
            ArtifactDefinition newArtifact = newArtifactEither.left().value();
            artifactUid = newArtifact.getUniqueId();
            artifactUUID = newArtifact.getArtifactUUID();

            ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(newArtifact.getArtifactType());
            if (artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_NET
                    || artifactType == ArtifactTypeEnum.HEAT_VOL) {
                Either<ArtifactDefinition, ResponseFormat> createHeatEnvPlaceHolder = artifactsBusinessLogic
                        .createHeatEnvPlaceHolder(newArtifact, ArtifactsBusinessLogic.HEAT_VF_ENV_NAME,
                                resource.getUniqueId(), NodeTypeEnum.Resource, resource.getName(),
                                csarInfo.getModifier(), resource, null);
                if (createHeatEnvPlaceHolder.isRight()) {
                    return Either.right(createHeatEnvPlaceHolder.right().value());
                }
                artifactEnvUid = createHeatEnvPlaceHolder.left().value().getUniqueId();
            }
        }else{
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
                resStatus = createDeploymentArtifactFromCsar(csarInfo, artifactPath, resource, artifactsGroup,
                        artifactsUUIDGroup, relatedArtifactTemplateInfo, createdArtifacts, labelCounter, shoudLock,
                        inTransaction);
                if (resStatus.isRight()) {
                    return resStatus;
                }
            }
        }
        return resStatus;
    }

    private String checkAndGetHeatEnvId(ArtifactDefinition createdArtifact) {
        String artifactEnvUid = "";
        ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(createdArtifact.getArtifactType());
        if (artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_NET
                || artifactType == ArtifactTypeEnum.HEAT_VOL) {
            artifactEnvUid = createdArtifact.getUniqueId() + ArtifactsBusinessLogic.HEAT_ENV_SUFFIX;
        }
        return artifactEnvUid;
    }

    private Either<ArtifactDefinition, ResponseFormat> checkIfArtifactAlreadyExist(ArtifactTemplateInfo artifactTemplateInfo, List<ArtifactDefinition> createdArtifacts){

        ArtifactDefinition res = null;
        String artifactFileName = artifactTemplateInfo.getFileName();
        Optional<ArtifactDefinition> op = createdArtifacts.stream().filter(a -> a.getArtifactName().equals(artifactFileName)).findAny();
        if(op.isPresent()){
            res = op.get();
            if (!res.getArtifactType().equalsIgnoreCase(artifactTemplateInfo.getType())) {
                log.debug(ARTIFACT_WITH_NAME_AND_TYPE_ALREADY_EXIST_WITH_TYPE, artifactFileName,
                        artifactTemplateInfo.getType(), res.getArtifactType());
                BeEcompErrorManager.getInstance().logInternalDataError(
                        ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMATR_FILE_NAME1 + artifactFileName,
                        ARTIFACT_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(
                        ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, artifactFileName,
                        artifactTemplateInfo.getType(), res.getArtifactType()));
            }
        }
        return Either.left(res);


    }

    private Either<ArtifactDefinition, ResponseFormat> createDeploymentArtifact(CsarInfo csarInfo, Resource resource,
                                                                                String artifactPath, ArtifactTemplateInfo artifactTemplateInfo, List<ArtifactDefinition> createdArtifacts,
                                                                                int label) {
        int updatedlabel = label;
        final String artifactFileName = artifactTemplateInfo.getFileName();
        Either<ImmutablePair<String, byte[]>, ResponseFormat> artifactContententStatus = CsarValidationUtils
                .getArtifactsContent(csarInfo.getCsarUUID(), csarInfo.getCsar(), artifactPath + artifactFileName,
                        artifactFileName, componentsUtils);
        if (artifactContententStatus.isRight()) {
            return Either.right(artifactContententStatus.right().value());
        }
        updatedlabel += createdArtifacts.size();

        Map<String, Object> json = ArtifactUtils.buildJsonForArtifact(artifactTemplateInfo,
                artifactContententStatus.left().value().getValue(), updatedlabel, true);

        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> uploadArtifactToService = createOrUpdateCsarArtifactFromJson(
                resource, csarInfo.getModifier(), json,
                artifactsBusinessLogic.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.CREATE));

        if (uploadArtifactToService.isRight()) {
            return Either.right(uploadArtifactToService.right().value());
        }

        ArtifactDefinition currentInfo = uploadArtifactToService.left().value().left().value();
        if (currentInfo.getHeatParameters() != null) {

            Either<ArtifactDefinition, ResponseFormat> updateEnvEither = updateHeatParamsFromCsar(resource, csarInfo,
                    artifactTemplateInfo, currentInfo, false);
            if (updateEnvEither.isRight()) {
                log.debug("failed to update parameters to artifact {}", artifactFileName);
                return Either.right(updateEnvEither.right().value());

            }
            currentInfo = updateEnvEither.left().value();

        }

        createdArtifacts.add(currentInfo);

        return Either.left(currentInfo);

    }



    private Either<ArtifactDefinition, ResponseFormat> updateHeatParamsFromCsar(Resource resource, CsarInfo csarInfo,
            ArtifactTemplateInfo artifactTemplateInfo, ArtifactDefinition currentInfo, boolean isUpdateEnv) {

        Resource updatedResource = resource;
        Either<ArtifactDefinition, ResponseFormat> resStatus = Either.left(currentInfo);
        if (artifactTemplateInfo.getEnv() != null && !artifactTemplateInfo.getEnv().isEmpty()) {

            Either<ImmutablePair<String, byte[]>, ResponseFormat> artifactparamsStatus = CsarValidationUtils
                    .getArtifactsContent(csarInfo.getCsarUUID(), csarInfo.getCsar(),
                            CsarUtils.ARTIFACTS_PATH + artifactTemplateInfo.getEnv(), artifactTemplateInfo.getEnv(),
                            componentsUtils);
            if (artifactparamsStatus.isRight()) {
                resStatus = Either.right(artifactparamsStatus.right().value());
                return resStatus;
            }
            Either<List<HeatParameterDefinition>, ResponseFormat> propsStatus = extractHeatParameters(
                    ArtifactTypeEnum.HEAT_ENV.getType(), artifactTemplateInfo.getEnv(),
                    artifactparamsStatus.left().value().getValue(), false);

            if (propsStatus.isLeft()) {
                List<HeatParameterDefinition> updatedHeatEnvParams = propsStatus.left().value();
                resStatus = updateHeatParams(updatedResource, currentInfo, updatedHeatEnvParams);
                if (resStatus.isRight()) {
                    return resStatus;
                }

            }
        }
        if (isUpdateEnv) {
            ComponentParametersView parametersView = new ComponentParametersView();
            parametersView.disableAll();
            parametersView.setIgnoreComponentInstances(false);
            parametersView.setIgnoreUsers(false);
            parametersView.setIgnoreArtifacts(false);
            parametersView.setIgnoreGroups(false);

            Either<Resource, StorageOperationStatus> eitherGerResource = toscaOperationFacade
                    .getToscaElement(updatedResource.getUniqueId(), parametersView);

            if (eitherGerResource.isRight()) {
                ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(
                        componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), updatedResource);

                resStatus = Either.right(responseFormat);
                return resStatus;

            }

            updatedResource = eitherGerResource.left().value();
            Map<String, ArtifactDefinition> artifacts = updatedResource.getDeploymentArtifacts();
            Optional<ArtifactDefinition> op = artifacts.values().stream().filter(
                    p -> p.getGeneratedFromId() != null && p.getGeneratedFromId().equals(currentInfo.getUniqueId()))
                    .findAny();
            if (op.isPresent()) {
                ArtifactDefinition artifactInfoHeatEnv = op.get();
                Either<ArtifactDefinition, StorageOperationStatus> updateArifactOnResource = artifactToscaOperation
                        .updateArtifactOnResource(artifactInfoHeatEnv, updatedResource.getUniqueId(),
                                artifactInfoHeatEnv.getUniqueId(), null, null);
                if (updateArifactOnResource.isRight()) {
                    log.debug("Failed to update heat env on CSAR flow for component {} artifact {} label {}",
                            updatedResource.getUniqueId(), artifactInfoHeatEnv.getUniqueId(),
                            artifactInfoHeatEnv.getArtifactLabel());
                    return Either.right(componentsUtils.getResponseFormat(
                            componentsUtils.convertFromStorageResponse(updateArifactOnResource.right().value())));
                }
                resStatus = Either.left(updateArifactOnResource.left().value());
            }
        }
        return resStatus;
    }

    private Either<List<HeatParameterDefinition>, ResponseFormat> extractHeatParameters(String artifactType,
            String fileName, byte[] content, boolean is64Encoded) {
        // extract heat parameters
        String heatDecodedPayload = is64Encoded ? new String(Base64.decodeBase64(content)) : new String(content);
        Either<List<HeatParameterDefinition>, ResultStatusEnum> heatParameters = ImportUtils
                .getHeatParamsWithoutImplicitTypes(heatDecodedPayload, artifactType);
        if (heatParameters.isRight()) {
            log.debug("File {} is not in expected key-value form in csar ", fileName);
            BeEcompErrorManager.getInstance().logInternalDataError(
                    "File " + fileName + " is not in expected key-value form in csar ", "CSAR internals are invalid",
                    ErrorSeverity.ERROR);
            return Either
                    .right(componentsUtils.getResponseFormat(ActionStatus.INVALID_DEPLOYMENT_ARTIFACT_HEAT, fileName));

        }
        return Either.left(heatParameters.left().value());

    }

    private Either<ArtifactDefinition, ResponseFormat> updateHeatParams(Resource resource,
            ArtifactDefinition currentInfo, List<HeatParameterDefinition> updatedHeatEnvParams) {

        Either<ArtifactDefinition, ResponseFormat> resStatus = Either.left(currentInfo);
        List<HeatParameterDefinition> currentHeatEnvParams = currentInfo.getListHeatParameters();

        if (updatedHeatEnvParams != null && !updatedHeatEnvParams.isEmpty() && currentHeatEnvParams != null
                && !currentHeatEnvParams.isEmpty()) {

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
                            ResponseFormat responseFormat = componentsUtils.getResponseFormat(status,
                                    ArtifactTypeEnum.HEAT_ENV.getType(), paramType.getType(), paramName);
                            resStatus = Either.right(responseFormat);
                            return resStatus;
                        }
                        currHeatParam.setCurrentValue(
                                paramType.getConverter().convert(updatedParamValue, null, null));

                        break;
                    }
                }
            }
            currentInfo.setListHeatParameters(currentHeatEnvParams);
            Either<ArtifactDefinition, StorageOperationStatus> updateArifactOnResource = artifactToscaOperation
                    .updateArtifactOnResource(currentInfo, resource.getUniqueId(), currentInfo.getUniqueId(),
                            null, null);
            if (updateArifactOnResource.isRight()) {
                log.debug(
                        "Failed to update heat paratemers of heat on CSAR flow for component {} artifact {} label {}",
                        resource.getUniqueId(), currentInfo.getUniqueId(), currentInfo.getArtifactLabel());
                return Either.right(componentsUtils.getResponseFormat(
                        componentsUtils.convertFromStorageResponse(updateArifactOnResource.right().value())));
            }
            resStatus = Either.left(updateArifactOnResource.left().value());
        }
        return resStatus;
    }




    public Either<Either<ArtifactDefinition, Operation>, ResponseFormat> createOrUpdateCsarArtifactFromJson(
            Resource resource, User user, Map<String, Object> json, ArtifactOperationInfo operation) {

        String jsonStr = gson.toJson(json);

        String origMd5 = GeneralUtility.calculateMD5Base64EncodedByString(jsonStr);
        ArtifactDefinition artifactDefinitionFromJson = RepresentationUtils.convertJsonToArtifactDefinition(jsonStr,
                ArtifactDefinition.class);
		
        String artifactUniqueId = artifactDefinitionFromJson == null ? null : artifactDefinitionFromJson.getUniqueId();
        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> uploadArtifactToService = artifactsBusinessLogic
                .validateAndHandleArtifact(resource.getUniqueId(), ComponentTypeEnum.RESOURCE, operation,
                        artifactUniqueId, artifactDefinitionFromJson, origMd5, jsonStr, null, null, user,
                        resource, false, true, false);
        if (uploadArtifactToService.isRight()) {
            return Either.right(uploadArtifactToService.right().value());
        }

        return Either.left(uploadArtifactToService.left().value());
    }

    private void associateMembersToArtifacts(List<ArtifactDefinition> createdArtifacts,
            List<ArtifactDefinition> artifactsFromResource, List<GroupDefinition> heatGroups,
            Set<String> artifactsGroup, Map<String, String> members) {
        if (heatGroups != null && !heatGroups.isEmpty()) {
            for (GroupDefinition heatGroup : heatGroups) {
                List<GroupProperty> grpoupProps = heatGroup.convertToGroupProperties();
                if (grpoupProps != null) {
                    associatemembersToVFgroups(createdArtifacts, artifactsFromResource, grpoupProps, artifactsGroup, heatGroup, members);
                }
            }

        }
    }

    private void associatemembersToVFgroups(List<ArtifactDefinition> createdArtifacts,List<ArtifactDefinition> artifactsFromResource, List<GroupProperty> grpoupProps, Set<String> artifactsGroup, GroupDefinition heatGroup, Map<String, String> members){
        Optional<GroupProperty> op = grpoupProps.stream()
                .filter(p -> p.getName().equals(Constants.HEAT_FILE_PROPS)).findAny();
        if (op.isPresent()) {
            GroupProperty prop = op.get();
            String heatFileNAme = prop.getValue();
            if (null == heatFileNAme || heatFileNAme.isEmpty()) {
                return;
            }
            List<ArtifactDefinition> artifacts = new ArrayList<>();
            for (String artifactId : artifactsGroup) {
                Optional<ArtifactDefinition> opArt = createdArtifacts.stream()
                        .filter(p -> p.getUniqueId().equals(artifactId)).findAny();
                if (opArt.isPresent()) {
                    artifacts.add(opArt.get());
                }
                if (artifactsFromResource != null) {
                    opArt = artifactsFromResource.stream().filter(p -> p.getUniqueId().equals(artifactId))
                            .findAny();
                    if (opArt.isPresent()) {
                        artifacts.add(opArt.get());
                    }
                }
            }
            Optional<ArtifactDefinition> resOp = artifacts.stream()
                    .filter(p -> heatFileNAme.contains(p.getArtifactName())).findAny();
            if (resOp.isPresent()) {
                members.putAll(heatGroup.getMembers());
            }
        }
    }

    public List<GroupProperty> createVfModuleAdditionalProperties(boolean isBase, String moduleName,
            List<GroupProperty> properties, List<ArtifactDefinition> deploymentArtifacts, List<String> artifactsInGroup,
            GroupTypeDefinition groupType) {
        Map<String, VfModuleProperty> vfModuleProperties = ConfigurationManager.getConfigurationManager()
                .getConfiguration().getVfModuleProperties();
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
            if (artifactDef != null
                    && artifactDef.getArtifactType().equalsIgnoreCase(ArtifactTypeEnum.HEAT_VOL.getType())) {
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

    private void mergeWithGroupTypeProperties(List<GroupProperty> properties,
            List<PropertyDefinition> groupTypeProperties) {

        Map<String, GroupProperty> propertiesMap = properties.stream()
                .collect(Collectors.toMap(PropertyDataDefinition::getName, p -> p));
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
            List<ArtifactDefinition> createdDeplymentArtifacts) {
        Map<GroupDefinition, MergedArtifactInfo> mergedgroup = new HashMap<>();
        for (Entry<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupListEntry : groupArtifact
                .entrySet()) {
            Map<ArtifactDefinition, List<ArtifactDefinition>> createdArtifactMap = groupListEntry.getValue();
            boolean isNeedToDeleteGroup = true;
            List<ArtifactDefinition> listToDelete = null;
            for (ArtifactDefinition maserArtifact : createdArtifactMap.keySet()) {
                listToDelete = createdArtifactMap.get(maserArtifact);
                for (ArtifactDefinition artToDelete : listToDelete) {
                    findArtifactToDelete(parsedGroup, artifactsToDelete, artToDelete, createdDeplymentArtifacts);
                }
                if (artifactsToDelete != null && !artifactsToDelete.isEmpty()) {
                    GroupDefinition group = groupListEntry.getKey();
                    for (ArtifactDefinition artifactDefinition : artifactsToDelete) {
                        if (CollectionUtils.isNotEmpty(group.getArtifacts())
                                && group.getArtifacts().contains(artifactDefinition.getUniqueId())) {
                            group.getArtifacts().remove(artifactDefinition.getUniqueId());

                        }
                        if (CollectionUtils.isNotEmpty(group.getArtifactsUuid())
                                && group.getArtifactsUuid().contains(artifactDefinition.getArtifactUUID())) {
                            group.getArtifactsUuid().remove(artifactDefinition.getArtifactUUID());

                        }
                    }

                }

                for (ArtifactTemplateInfo jsonMasterArtifact : jsonMasterArtifacts) {
                    if (maserArtifact.getArtifactName().equalsIgnoreCase(jsonMasterArtifact.getFileName())) {
                        MergedArtifactInfo mergedGroup = new MergedArtifactInfo();
                        mergedGroup.setJsonArtifactTemplate(jsonMasterArtifact);
                        mergedGroup.setCreatedArtifact(createdArtifactMap.get(maserArtifact));
                        mergedgroup.put(groupListEntry.getKey(), mergedGroup);
                        isNeedToDeleteGroup = false;

                    }
                }

            }
            if (isNeedToDeleteGroup) {
                groupToDelete.put(groupListEntry.getKey().getUniqueId(), listToDelete);
            }

        }
        return mergedgroup;
    }

    private void findArtifactToDelete(Map<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroup,
            Set<ArtifactDefinition> artifactsToDelete, ArtifactDefinition artifact,
            List<ArtifactDefinition> createdDeplymentArtifacts) {
        boolean isNeedToDeleteArtifact = true;
        String artifactType = artifact.getArtifactType();
        ArtifactDefinition generatedFromArt = null;
        if (artifact.getGeneratedFromId() != null && !artifact.getGeneratedFromId().isEmpty()) {
            Optional<ArtifactDefinition> op = createdDeplymentArtifacts.stream()
                    .filter(p -> p.getUniqueId().equals(artifact.getGeneratedFromId())).findAny();
            if (op.isPresent()) {
                generatedFromArt = op.get();
            }

        }

        for (Entry<ArtifactTemplateInfo, Set<ArtifactTemplateInfo>> parsedGroupSetEntry : parsedGroup.entrySet()) {
            Set<ArtifactTemplateInfo> artifactsNames = parsedGroupSetEntry.getValue();
            for (ArtifactTemplateInfo template : artifactsNames) {
                if (artifact.getArtifactName().equalsIgnoreCase(template.getFileName())
                        && artifactType.equalsIgnoreCase(template.getType())) {
                    isNeedToDeleteArtifact = false;
                    break;

                } else {

                    if (generatedFromArt != null) {
                        if (generatedFromArt.getArtifactName().equalsIgnoreCase(template.getFileName())
                                && generatedFromArt.getArtifactType().equalsIgnoreCase(template.getType())) {
                            isNeedToDeleteArtifact = false;
                            break;
                        }
                    }
                }
            }

        }
        if (isNeedToDeleteArtifact) {
            artifactsToDelete.add(artifact);

        }
    }

    private Map<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> findMasterArtifactInGroup(
            List<GroupDefinition> groups, Map<String, ArtifactDefinition> deplymentArtifact) {
        Map<GroupDefinition, Map<ArtifactDefinition, List<ArtifactDefinition>>> groupArtifact = new HashMap<>();

        for (GroupDefinition group : groups) {
            Map<ArtifactDefinition, List<ArtifactDefinition>> gupsMap = new HashMap<>();
            List<ArtifactDefinition> artifacts = new ArrayList<>();
            List<String> artifactsList = group.getArtifacts();
            if (artifactsList != null && !artifactsList.isEmpty()) {

                ArtifactDefinition masterArtifact = ArtifactUtils.findMasterArtifact(deplymentArtifact, artifacts,
                        artifactsList);
                if (masterArtifact != null) {
                    gupsMap.put(masterArtifact, artifacts);
                }
                groupArtifact.put(group, gupsMap);

            }
        }
        return groupArtifact;
    }

    private Either<Resource, ResponseFormat> deleteArtifactsInUpdateCsarFlow(Resource resource,
            User user, boolean shouldLock, boolean inTransaction, Set<ArtifactDefinition> artifactsToDelete,
            Map<String, List<ArtifactDefinition>> groupToDelete, List<ArtifactDefinition> deletedArtifacts) {

        Resource updatedResource = resource;

        String resourceId = updatedResource.getUniqueId();
        if (!artifactsToDelete.isEmpty()) {
            for (ArtifactDefinition artifact : artifactsToDelete) {
                String artifactType = artifact.getArtifactType();
                ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.findType(artifactType);
                if (artifactTypeEnum != ArtifactTypeEnum.HEAT_ENV) {
                    Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleDelete = artifactsBusinessLogic
                            .handleDelete(resourceId, artifact.getUniqueId(), user, AuditingActionEnum.ARTIFACT_DELETE,
                                    ComponentTypeEnum.RESOURCE, updatedResource, shouldLock, inTransaction);
                    if (handleDelete.isRight()) {
                        return Either.right(handleDelete.right().value());
                    }

                    deletedArtifacts.add(handleDelete.left().value().left().value());
                }

            }
        }
        if (!groupToDelete.isEmpty()) {
            log.debug("try to delete group");
            List<GroupDefinition> groupDefinitionstoDelete = new ArrayList<>();
            List<GroupDefinition> groups = updatedResource.getGroups();
            for (Entry<String, List<ArtifactDefinition>> deleteGroup : groupToDelete.entrySet()) {
                Optional<GroupDefinition> op = groups.stream()
                        .filter(gr -> gr.getUniqueId().equals(deleteGroup.getKey())).findAny();
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
        Either<Resource, StorageOperationStatus> eitherGerResource = toscaOperationFacade
                .getToscaElement(updatedResource.getUniqueId());
        if (eitherGerResource.isRight()) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(
                    componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), updatedResource);

            return Either.right(responseFormat);

        }
        updatedResource = eitherGerResource.left().value();
        updatedResource.setGroups(oldGroups);
        return Either.left(updatedResource);
    }

    private void createArtifactsGroupSet(List<ArtifactTemplateInfo> parsedGroupTemplateList,
            Set<ArtifactTemplateInfo> parsedArtifactsName) {

        for (ArtifactTemplateInfo parsedGroupTemplate : parsedGroupTemplateList) {
            parsedArtifactsName.add(parsedGroupTemplate);
            List<ArtifactTemplateInfo> relatedArtifacts = parsedGroupTemplate.getRelatedArtifactsInfo();
            if (relatedArtifacts != null && !relatedArtifacts.isEmpty()) {
                createArtifactsGroupSet(relatedArtifacts, parsedArtifactsName);
            }
        }
    }

    private Either<Resource, ResponseFormat> createGroupDeploymentArtifactsFromCsar(CsarInfo csarInfo,
            Resource resource, List<ArtifactTemplateInfo> artifactsTemplateList,
            List<ArtifactDefinition> createdNewArtifacts, List<ArtifactDefinition> artifactsFromResource,
            int labelCounter, boolean shouldLock, boolean inTransaction) {

        Resource updatedResource = resource;

        Either<Resource, ResponseFormat> resStatus = Either.left(updatedResource);
        List<GroupDefinition> createdGroups = updatedResource.getGroups();
        List<GroupDefinition> heatGroups = null;
        if (createdGroups != null && !createdGroups.isEmpty()) {
            heatGroups = createdGroups.stream().filter(e -> e.getMembers() != null).collect(Collectors.toList());
        }

        List<GroupDefinition> needToAdd = new ArrayList<>();
        for (ArtifactTemplateInfo groupTemplateInfo : artifactsTemplateList) {
            String groupName = groupTemplateInfo.getGroupName();
            Set<String> artifactsGroup = new HashSet<>();
            Set<String> artifactsUUIDGroup = new HashSet<>();

            resStatus = createDeploymentArtifactsFromCsar(csarInfo, updatedResource, artifactsGroup, artifactsUUIDGroup,
                    groupTemplateInfo, createdNewArtifacts, artifactsFromResource, labelCounter, shouldLock,
                    inTransaction);
            if (resStatus.isRight()) {
                return resStatus;
            }
            if (groupName != null && !groupName.isEmpty()) {
                Map<String, String> members = new HashMap<>();
                associateMembersToArtifacts(createdNewArtifacts, artifactsFromResource, heatGroups, artifactsGroup,
                        members);

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
                        .getLatestGroupTypeByType(Constants.DEFAULT_GROUP_VF_MODULE, true);
                if (getLatestGroupTypeRes.isRight()) {
                    return Either.right(componentsUtils.getResponseFormat(
                            componentsUtils.convertFromStorageResponse(getLatestGroupTypeRes.right().value())));
                }
                properties = createVfModuleAdditionalProperties(groupTemplateInfo.isBase(), groupName, properties,
                        createdArtifacts, artifactsList, getLatestGroupTypeRes.left().value());
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

    private Either<Resource, ResponseFormat> createDeploymentArtifactsFromCsar(CsarInfo csarInfo, Resource resource,
            Set<String> artifactsGroup, Set<String> artifactsUUIDGroup, ArtifactTemplateInfo artifactTemplateInfo,
            List<ArtifactDefinition> createdArtifacts, List<ArtifactDefinition> artifactsFromResource, int labelCounter,
            boolean shoudLock, boolean inTransaction) {
        Either<Resource, ResponseFormat> resStatus = Either.left(resource);
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
                        log.debug(ARTIFACT_WITH_NAME_AND_TYPE_ALREADY_EXIST_WITH_TYPE, artifactFileName,
                                artifactTemplateInfo.getType(), artifactFromResource.getArtifactType());
                        BeEcompErrorManager.getInstance().logInternalDataError(
                                ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMATR_FILE_NAME1 + artifactFileName,
                                ARTIFACT_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
                        return Either.right(componentsUtils.getResponseFormat(
                                ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, artifactFileName,
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
                        log.debug(ARTIFACT_WITH_NAME_AND_TYPE_ALREADY_EXIST_WITH_TYPE, artifactFileName,
                                artifactTemplateInfo.getType(), createdArtifact.getArtifactType());
                        BeEcompErrorManager.getInstance().logInternalDataError(
                                ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMATR_FILE_NAME1 + artifactFileName,
                                ARTIFACT_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
                        return Either.right(componentsUtils.getResponseFormat(
                                ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, artifactFileName,
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

            Either<ArtifactDefinition, ResponseFormat> newArtifactEither = createDeploymentArtifact(csarInfo, resource,
                    ARTIFACTS_PATH, artifactTemplateInfo, createdArtifacts, labelCounter);
            if (newArtifactEither.isRight()) {
                resStatus = Either.right(newArtifactEither.right().value());
                return resStatus;
            }
            ArtifactDefinition newArtifact = newArtifactEither.left().value();
            artifactUid = newArtifact.getUniqueId();
            artifactUUID = newArtifact.getArtifactUUID();
            ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(newArtifact.getArtifactType());
            if (artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_NET
                    || artifactType == ArtifactTypeEnum.HEAT_VOL) {
                Either<ArtifactDefinition, ResponseFormat> createHeatEnvPlaceHolder = artifactsBusinessLogic
                        .createHeatEnvPlaceHolder(newArtifact, ArtifactsBusinessLogic.HEAT_VF_ENV_NAME,
                                resource.getUniqueId(), NodeTypeEnum.Resource, resource.getName(),
                                csarInfo.getModifier(), resource, null);
                if (createHeatEnvPlaceHolder.isRight()) {
                    return Either.right(createHeatEnvPlaceHolder.right().value());
                }
                artifactEnvUid = createHeatEnvPlaceHolder.left().value().getUniqueId();
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
                resStatus = createDeploymentArtifactsFromCsar(csarInfo, resource, artifactsGroup, artifactsUUIDGroup,
                        relatedArtifactTemplateInfo, createdArtifacts, artifactsFromResource, labelCounter, shoudLock,
                        inTransaction);
                if (resStatus.isRight()) {
                    return resStatus;
                }
            }
        }
        return resStatus;
    }

    private Either<Resource, ResponseFormat> associateAndDissociateArtifactsToGroup(CsarInfo csarInfo,
                                                                                    Resource resource, List<ArtifactDefinition> createdNewArtifacts, int labelCounter,
                                                                                    boolean inTransaction, List<ArtifactDefinition> createdDeplymentArtifactsAfterDelete,
                                                                                    Map<GroupDefinition, MergedArtifactInfo> mergedgroup, List<ArtifactDefinition> deletedArtifacts) {
        Map<GroupDefinition, List<ArtifactTemplateInfo>> artifactsToAssotiate = new HashMap<>();
        Map<GroupDefinition, List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>>> artifactsToUpdateMap = new HashMap<>();
        Either<Resource, ResponseFormat> resEither = Either.left(resource);
        for (Entry<GroupDefinition, MergedArtifactInfo> entry : mergedgroup.entrySet()) {
            List<ArtifactDefinition> dissArtifactsInGroup = entry.getValue()
                    .getListToDissotiateArtifactFromGroup(deletedArtifacts);
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

            List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>> artifactsToUpdate = entry.getValue()
                    .getListToUpdateArtifactInGroup();
            if (artifactsToUpdate != null && !artifactsToUpdate.isEmpty()) {
                artifactsToUpdateMap.put(entry.getKey(), artifactsToUpdate);
            }
        }

        if (!artifactsToUpdateMap.isEmpty()) {
            List<ArtifactDefinition> updatedArtifacts = new ArrayList<>();
            for (Entry<GroupDefinition, List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>>> artifactsToUpdateEntry : artifactsToUpdateMap
                    .entrySet()) {
                List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>> artifactsToUpdateList = artifactsToUpdateEntry
                        .getValue();
                GroupDefinition groupToUpdate = artifactsToUpdateEntry.getKey();

                for (ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo> artifact : artifactsToUpdateList) {
                    String prevUUID = artifact.getKey().getArtifactUUID();
                    String prevId = artifact.getKey().getUniqueId();
                    String prevHeatEnvId = checkAndGetHeatEnvId(artifact.getKey());
                    Either<ArtifactDefinition, ResponseFormat> updateArtifactEither = updateDeploymentArtifactsFromCsar(
                            csarInfo, resource, artifact.getKey(), artifact.getValue(), updatedArtifacts,
                            artifact.getRight().getRelatedArtifactsInfo());
                    if (updateArtifactEither.isRight()) {
                        log.debug("failed to update artifacts. status is {}", updateArtifactEither.right().value());
                        resEither = Either.right(updateArtifactEither.right().value());
                        return resEither;
                    }
                    ArtifactDefinition artAfterUpdate = updateArtifactEither.left().value();
                    if (!prevUUID.equals(artAfterUpdate.getArtifactUUID())
                            || !prevId.equals(artAfterUpdate.getUniqueId())) {
                        groupToUpdate.getArtifacts().remove(prevId);
                        groupToUpdate.getArtifactsUuid().remove(prevUUID);
                        groupToUpdate.getArtifacts().add(artAfterUpdate.getUniqueId());
                        groupToUpdate.getArtifactsUuid().add(artAfterUpdate.getArtifactUUID());
                    }
                    Optional<ArtifactDefinition> op = updatedArtifacts.stream()
                            .filter(p -> p.getGeneratedFromId() != null
                            && p.getGeneratedFromId().equals(artAfterUpdate.getUniqueId()))
                            .findAny();
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
                for (ArtifactDefinition createdArtifact : createdDeplymentArtifactsAfterDelete) {
                    if (artifactTemplate.getFileName().equalsIgnoreCase(createdArtifact.getArtifactName())) {
                        arifactsUids.add(createdArtifact.getUniqueId());
                        arifactsUuids.add(createdArtifact.getArtifactUUID());
                        isCreate = false;
                        String heatEnvId = checkAndGetHeatEnvId(createdArtifact);
                        if (!heatEnvId.isEmpty()) {
                            arifactsUids.add(heatEnvId);
                            Optional<ArtifactDefinition> op = createdDeplymentArtifactsAfterDelete.stream()
                                    .filter(p -> p.getUniqueId().equals(heatEnvId)).findAny();
                            if (op.isPresent()) {
                                this.artifactToscaOperation.updateHeatEnvPlaceholder(op.get(), resource.getUniqueId(),
                                        resource.getComponentType().getNodeType());

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
                    Either<ArtifactDefinition, ResponseFormat> createArtifactEither = createDeploymentArtifact(csarInfo,
                            resource, ARTIFACTS_PATH, artifactTemplate, createdNewArtifacts, labelCounter);
                    if (createArtifactEither.isRight()) {
                        resEither = Either.right(createArtifactEither.right().value());
                        return resEither;
                    }
                    ArtifactDefinition createdArtifact = createArtifactEither.left().value();
                    arifactsUids.add(createdArtifact.getUniqueId());
                    arifactsUuids.add(createdArtifact.getArtifactUUID());
                    ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(createdArtifact.getArtifactType());
                    if (artifactType == ArtifactTypeEnum.HEAT || artifactType == ArtifactTypeEnum.HEAT_NET
                            || artifactType == ArtifactTypeEnum.HEAT_VOL) {
                        Either<ArtifactDefinition, ResponseFormat> createHeatEnvPlaceHolder = artifactsBusinessLogic
                                .createHeatEnvPlaceHolder(createdArtifact, ArtifactsBusinessLogic.HEAT_VF_ENV_NAME,
                                        resource.getUniqueId(), NodeTypeEnum.Resource, resource.getName(),
                                        csarInfo.getModifier(), resource, null);
                        if (createHeatEnvPlaceHolder.isRight()) {
                            return Either.right(createHeatEnvPlaceHolder.right().value());
                        }
                        String heatEnvId = createHeatEnvPlaceHolder.left().value().getUniqueId();
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

        Either<Resource, StorageOperationStatus> eitherGerResource = toscaOperationFacade
                .getToscaElement(resource.getUniqueId(), parametersView);

        if (eitherGerResource.isRight()) {
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(
                    componentsUtils.convertFromStorageResponse(eitherGerResource.right().value()), resource);

            resEither = Either.right(responseFormat);
            return resEither;

        }
        resEither = Either.left(eitherGerResource.left().value());
        return resEither;
    }

    private Either<ArtifactDefinition, ResponseFormat> updateDeploymentArtifactsFromCsar(CsarInfo csarInfo,
            Resource resource, ArtifactDefinition oldArtifact, ArtifactTemplateInfo artifactTemplateInfo,
            List<ArtifactDefinition> updatedArtifacts, List<ArtifactTemplateInfo> updatedRequiredArtifacts) {

        Either<ArtifactDefinition, ResponseFormat> resStatus = null;
        String artifactFileName = artifactTemplateInfo.getFileName();

        // check if artifacts already exist
        for (ArtifactDefinition updatedArtifact : updatedArtifacts) {
            if (updatedArtifact.getArtifactName().equals(artifactFileName)) {
                if (!updatedArtifact.getArtifactType().equalsIgnoreCase(artifactTemplateInfo.getType())) {
                    log.debug("Artifact with name {} and type {} already updated with type  {}", artifactFileName,
                            artifactTemplateInfo.getType(), updatedArtifact.getArtifactType());
                    BeEcompErrorManager.getInstance().logInternalDataError(
                            ARTIFACT_FILE_IS_NOT_IN_EXPECTED_FORMATR_FILE_NAME1 + artifactFileName,
                            ARTIFACT_INTERNALS_ARE_INVALID, ErrorSeverity.ERROR);
                    resStatus = Either.right(componentsUtils.getResponseFormat(
                            ActionStatus.ARTIFACT_ALREADY_EXIST_IN_DIFFERENT_TYPE_IN_CSAR, artifactFileName,
                            artifactTemplateInfo.getType(), updatedArtifact.getArtifactType()));
                    return resStatus;
                }
                resStatus = Either.left(updatedArtifact);
                return resStatus;
            }

        }

        Either<ImmutablePair<String, byte[]>, ResponseFormat> artifactContententStatus = CsarValidationUtils
                .getArtifactsContent(csarInfo.getCsarUUID(), csarInfo.getCsar(),
                        CsarUtils.ARTIFACTS_PATH + artifactFileName, artifactFileName, componentsUtils);
        if (artifactContententStatus.isRight()) {
            resStatus = Either.right(artifactContententStatus.right().value());
            return resStatus;
        }

        Map<String, Object> json = ArtifactUtils.buildJsonForUpdateArtifact(oldArtifact.getUniqueId(), artifactFileName,
                oldArtifact.getArtifactType(), ArtifactGroupTypeEnum.DEPLOYMENT, oldArtifact.getArtifactLabel(),
                oldArtifact.getArtifactDisplayName(), oldArtifact.getDescription(),
                artifactContententStatus.left().value().getRight(), updatedRequiredArtifacts, oldArtifact.getIsFromCsar());

        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> uploadArtifactToService = createOrUpdateCsarArtifactFromJson(
                resource, csarInfo.getModifier(), json,
                artifactsBusinessLogic.new ArtifactOperationInfo(false, false, ArtifactOperationEnum.UPDATE));

        if (uploadArtifactToService.isRight()) {
            resStatus = Either.right(uploadArtifactToService.right().value());
            return resStatus;
        }
        ArtifactDefinition currentInfo = uploadArtifactToService.left().value().left().value();
        updatedArtifacts.add(currentInfo);

        Either<ArtifactDefinition, ResponseFormat> updateEnvEither = updateHeatParamsFromCsar(resource, csarInfo,
                artifactTemplateInfo, currentInfo, true);
        if (updateEnvEither.isRight()) {
            log.debug("failed to update parameters to artifact {}", artifactFileName);
            resStatus = Either.right(updateEnvEither.right().value());
            return resStatus;
        }

        updatedArtifacts.add(updateEnvEither.left().value());
        resStatus = Either.left(currentInfo);

        return resStatus;

    }

    public Either<Resource, ResponseFormat> deleteVFModules(Resource resource, CsarInfo csarInfo, boolean shouldLock, boolean inTransaction) {
        Resource updatedResource = resource;
        List<GroupDefinition> groupsToDelete = updatedResource.getGroups();
        if(groupsToDelete != null && !groupsToDelete.isEmpty()){
            List<GroupDefinition> vfGroupsToDelete = groupsToDelete.stream().filter(g -> g.getType().equals(Constants.DEFAULT_GROUP_VF_MODULE)).collect(Collectors.toList());
            if(vfGroupsToDelete != null && !vfGroupsToDelete.isEmpty()){
                for(GroupDefinition gr : vfGroupsToDelete){
                    List<String> artifacts = gr.getArtifacts();
                    for (String artifactId : artifacts) {
                        Either<Either<ArtifactDefinition, Operation>, ResponseFormat> handleDelete = artifactsBusinessLogic.handleDelete(updatedResource.getUniqueId(), artifactId, csarInfo.getModifier(), AuditingActionEnum.ARTIFACT_DELETE, ComponentTypeEnum.RESOURCE,
                                updatedResource, shouldLock, inTransaction);
                        if (handleDelete.isRight()) {
                            log.debug("Couldn't delete  artifact {}", artifactId);
                            return Either.right(handleDelete.right().value());
                        }
                    }

                }
                groupBusinessLogic.deleteGroups(updatedResource, vfGroupsToDelete);

                Either<Resource, StorageOperationStatus> eitherGetResource = toscaOperationFacade.getToscaElement(updatedResource.getUniqueId());
                if (eitherGetResource.isRight()) {
                    ResponseFormat responseFormat = componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(eitherGetResource.right().value()), updatedResource);

                    return Either.right(responseFormat);

                }
                updatedResource = eitherGetResource.left().value();
            }
        }
        return Either.left(updatedResource);
    }

    private Either<Resource, ResponseFormat>  getResourcetFromGraph(Resource component){
        log.debug("getResource start");
        return toscaOperationFacade.getToscaElement(component.getUniqueId())
                .right()
                .map(rf ->  componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(rf), component))
                .left()
                .map (c -> (Resource) c);
        

    }

}
