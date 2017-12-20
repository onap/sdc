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

package org.openecomp.sdc.vendorsoftwareproduct.services.impl.composition;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.json.JsonSchemaDataGenerator;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.CompositionEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Component;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComputeData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.NetworkType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateInput;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CompositionEntityDataManagerImpl implements CompositionEntityDataManager {

  private static final String COMPOSITION_ENTITY_DATA_MANAGER_ERR =
      "COMPOSITION_ENTITY_DATA_MANAGER_ERR";
  private static final String COMPOSITION_ENTITY_DATA_MANAGER_ERR_MSG =
      "Invalid input: %s may not be null";
  private static final String MISSING_OR_INVALID_QUESTIONNAIRE_MSG =
      "Data is missing/invalid for this %s. Please refill and resubmit.";

  private static final Logger logger =
      LoggerFactory.getLogger(CompositionEntityDataManagerImpl.class);
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private Map<CompositionEntityId, CompositionEntityData> entities = new HashMap<>();
  private Map<CompositionEntityType, String> nonDynamicSchemas = new HashMap<>();
  private List<CompositionEntityValidationData> roots = new ArrayList<>();

  private VendorSoftwareProductInfoDao vspInfoDao;
  private ComponentDao componentDao;
  private NicDao nicDao;
  private NetworkDao networkDao;
  private ImageDao imageDao;
  private ComputeDao computeDao;
  private DeploymentFlavorDao deploymentFlavorDao;

  public CompositionEntityDataManagerImpl(VendorSoftwareProductInfoDao vspInfoDao,
                                          ComponentDao componentDao,
                                          NicDao nicDao, NetworkDao networkDao,
                                          ImageDao imageDao, ComputeDao computeDao,
                                          DeploymentFlavorDao deploymentFlavorDao) {
    this.vspInfoDao = vspInfoDao;
    this.componentDao = componentDao;
    this.nicDao = nicDao;
    this.networkDao = networkDao;
    this.imageDao = imageDao;
    this.computeDao = computeDao;
    this.deploymentFlavorDao = deploymentFlavorDao;
  }

  /**
   * Validate entity composition entity validation data.
   *
   * @param entity                the entity
   * @param schemaTemplateContext the schema template context
   * @param schemaTemplateInput   the schema template input
   * @return the composition entity validation data
   */
  @Override
  public CompositionEntityValidationData validateEntity(CompositionEntity entity,
                                                        SchemaTemplateContext schemaTemplateContext,
                                                        SchemaTemplateInput schemaTemplateInput) {
    mdcDataDebugMessage.debugEntryMessage(null);

    if (entity == null) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
              .withId(COMPOSITION_ENTITY_DATA_MANAGER_ERR).withMessage(
              String.format(COMPOSITION_ENTITY_DATA_MANAGER_ERR_MSG, "composition entity"))
              .build());
    }
    if (schemaTemplateContext == null) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
              .withId(COMPOSITION_ENTITY_DATA_MANAGER_ERR).withMessage(
              String.format(COMPOSITION_ENTITY_DATA_MANAGER_ERR_MSG, "schema template context"))
              .build());
    }

    CompositionEntityValidationData validationData =
        new CompositionEntityValidationData(entity.getType(), entity.getId());
    String json =
        schemaTemplateContext == SchemaTemplateContext.composition ? entity.getCompositionData()
            : entity.getQuestionnaireData();
    validationData.setErrors(JsonUtil.validate(
        json == null ? JsonUtil.object2Json(new Object()) : json,
        generateSchema(schemaTemplateContext, entity.getType(), schemaTemplateInput)));

    mdcDataDebugMessage.debugExitMessage(null);
    return validationData;
  }

  /**
   * Add entity.
   *
   * @param entity              the entity
   * @param schemaTemplateInput the schema template input
   */
  @Override
  public void addEntity(CompositionEntity entity, SchemaTemplateInput schemaTemplateInput) {
    if (entity == null) {
      throw new CoreException(
          new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
              .withId(COMPOSITION_ENTITY_DATA_MANAGER_ERR).withMessage(
              String.format(COMPOSITION_ENTITY_DATA_MANAGER_ERR_MSG, "composition entity"))
              .build());
    }
    entities.put(entity.getCompositionEntityId(),
        new CompositionEntityData(entity, schemaTemplateInput));
  }

  /**
   * Validate entities questionnaire map.
   *
   * @return the map
   */
  @Override
  public Map<CompositionEntityId, Collection<String>> validateEntitiesQuestionnaire() {
    mdcDataDebugMessage.debugEntryMessage(null);

    Map<CompositionEntityId, Collection<String>> errorsByEntityId = new HashMap<>();
    entities.entrySet().forEach(entry -> {
      Collection<String> errors = validateQuestionnaire(entry.getValue());
      if (errors != null) {
        errorsByEntityId.put(entry.getKey(), errors);
      }
    });

    mdcDataDebugMessage.debugExitMessage(null);
    return errorsByEntityId;
  }

  /**
   * Build trees.
   */
  @Override
  public void buildTrees() {
    Map<CompositionEntityId, CompositionEntityValidationData> entitiesValidationData =
        new HashMap<>();
    entities.entrySet().forEach(
        entry -> addValidationDataEntity(entitiesValidationData, entry.getKey(),
            entry.getValue().entity));
  }

  public Collection<CompositionEntityValidationData> getTrees() {
    return roots;
  }

  @Override
  public void saveCompositionData(String vspId, Version version, CompositionData compositionData) {
    mdcDataDebugMessage.debugEntryMessage(null);

    if (Objects.isNull(compositionData)) {
      return;
    }

    Map<String, String> networkIdByName = saveNetworks(vspId, version, compositionData);
    saveComponents(vspId, version, compositionData, networkIdByName);

    mdcDataDebugMessage.debugExitMessage(null);
  }

  @Override
  public Set<CompositionEntityValidationData> getAllErrorsByVsp(String vspId) {
    CompositionEntityValidationData matchVsp = null;
    Set<CompositionEntityValidationData> entitiesWithErrors = new HashSet<>();
    for (CompositionEntityValidationData root : roots) {
      if (root.getEntityId().equals(vspId)) {
        matchVsp = root;
        break;
      }
    }

    getEntityListWithErrors(matchVsp, entitiesWithErrors);
    if (CollectionUtils.isNotEmpty(entitiesWithErrors)) {
      updateValidationCompositionEntityName(entitiesWithErrors);
      return entitiesWithErrors;
    }

    return null;
  }

  private boolean isThereErrorsInSubTree(CompositionEntityValidationData entity) {
    if (Objects.isNull(entity)) {
      return false;
    }

    if (CollectionUtils.isNotEmpty(entity.getErrors())) {
      return true;
    }

    Collection<CompositionEntityValidationData> subEntitiesValidationData =
        entity.getSubEntitiesValidationData();
    return !CollectionUtils.isEmpty(subEntitiesValidationData) &&
        checkForErrorsInChildren(subEntitiesValidationData);

  }

  private boolean checkForErrorsInChildren(
      Collection<CompositionEntityValidationData> subEntitiesValidationData) {
    boolean result = false;
    for (CompositionEntityValidationData subEntity : subEntitiesValidationData) {
      if (CollectionUtils.isNotEmpty(subEntity.getErrors())) {
        return true;
      }

      result = isThereErrorsInSubTree(subEntity) || result;
      if (result) {
        return true;
      }
    }
    return false;
  }

  public void saveComponents(String vspId, Version version, CompositionData compositionData,
                             Map<String, String> networkIdByName) {


    mdcDataDebugMessage.debugEntryMessage(null);

    if (CollectionUtils.isNotEmpty(compositionData.getComponents())) {
      for (Component component : compositionData.getComponents()) {
        ComponentEntity componentEntity = new ComponentEntity(vspId, version, null);
        componentEntity.setComponentCompositionData(component.getData());

        String componentId = createComponent(componentEntity).getId();

        saveImagesByComponent(vspId, version, component, componentId);
        saveComputesFlavorByComponent(vspId, version, component, componentId);

        saveNicsByComponent(vspId, version, networkIdByName, component, componentId);
      }
    }

    mdcDataDebugMessage.debugExitMessage(null);
  }

  public void saveNicsByComponent(String vspId, Version version,
                                  Map<String, String> networkIdByName, Component component,
                                  String componentId) {
    if (CollectionUtils.isNotEmpty(component.getNics())) {
      for (Nic nic : component.getNics()) {
        if (nic.getNetworkName() != null && MapUtils.isNotEmpty(networkIdByName)) {
          nic.setNetworkId(networkIdByName.get(nic.getNetworkName()));
        }
        nic.setNetworkName(null);
        //For heat flow set network type to be internal by default for NIC
        nic.setNetworkType(NetworkType.Internal);

        NicEntity nicEntity = new NicEntity(vspId, version, componentId, null);
        nicEntity.setNicCompositionData(nic);
        createNic(nicEntity);
      }
    }
  }

  public Map<String, String> saveNetworks(String vspId, Version version,
                                          CompositionData compositionData) {
    mdcDataDebugMessage.debugEntryMessage(null);

    Map<String, String> networkIdByName = new HashMap<>();
    if (CollectionUtils.isNotEmpty(compositionData.getNetworks())) {
      for (Network network : compositionData.getNetworks()) {

        NetworkEntity networkEntity = new NetworkEntity(vspId, version, null);
        networkEntity.setNetworkCompositionData(network);

        if (network.getName() != null) {
          networkIdByName.put(network.getName(), createNetwork(networkEntity).getId());
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage(null);
    return networkIdByName;
  }

  @Override
  public NetworkEntity createNetwork(NetworkEntity network) {
    mdcDataDebugMessage.debugEntryMessage(null);

    //network.setId(CommonMethods.nextUuId()); will be set by the dao
    networkDao.create(network);
    mdcDataDebugMessage.debugExitMessage(null);
    return network;
  }

  @Override
  public ComponentEntity createComponent(ComponentEntity component) {
    mdcDataDebugMessage.debugEntryMessage(null);

    //component.setId(CommonMethods.nextUuId()); will be set by the dao
    component.setQuestionnaireData(
        new JsonSchemaDataGenerator(
            generateSchema(SchemaTemplateContext.questionnaire, CompositionEntityType.component,
                null))
            .generateData());

    componentDao.create(component);

    mdcDataDebugMessage.debugExitMessage(null);
    return component;
  }

  @Override
  public NicEntity createNic(NicEntity nic) {
    mdcDataDebugMessage.debugEntryMessage(null);

    //nic.setId(CommonMethods.nextUuId()); will be set by the dao
    nic.setQuestionnaireData(
        new JsonSchemaDataGenerator(
            generateSchema(SchemaTemplateContext.questionnaire, CompositionEntityType.nic, null))
            .generateData());

    nicDao.create(nic);

    mdcDataDebugMessage.debugExitMessage(null);
    return nic;
  }


  public void addErrorsToTrees(Map<CompositionEntityId, Collection<String>> errors) {
    roots.forEach(root -> addErrorsToTree(root, null, errors));
  }

  /* *
  * get a flat list of all questionnaire entities that have validation errors
  * */
  public Set<CompositionEntityValidationData> getEntityListWithErrors() {
    mdcDataDebugMessage.debugEntryMessage(null);
    Set<CompositionEntityValidationData> treeAsList = new HashSet<>();

    for (CompositionEntityValidationData entity : roots) {
      if (CollectionUtils.isNotEmpty(entity.getErrors())) {
        addNodeWithErrors(entity, treeAsList);
      }
      getEntityListWithErrors(entity, treeAsList);
    }

    updateValidationCompositionEntityName(treeAsList);

    mdcDataDebugMessage.debugExitMessage(null);
    return treeAsList;
  }

  private void getEntityListWithErrors(CompositionEntityValidationData entity,
                                      Set<CompositionEntityValidationData> compositionSet) {
    if(CollectionUtils.isNotEmpty(entity.getErrors())){
      addNodeWithErrors(entity, compositionSet);
    }

    if (CollectionUtils.isEmpty(entity.getSubEntitiesValidationData())) {
      return;
    }

    for (CompositionEntityValidationData child : entity.getSubEntitiesValidationData()) {
      getEntityListWithErrors(child, compositionSet);
    }
  }


  private void addNodeWithErrors(CompositionEntityValidationData node,
                                Set<CompositionEntityValidationData> entitiesWithErrors) {
    CompositionEntityValidationData compositionNodeToAdd = new CompositionEntityValidationData(node
        .getEntityType(), node.getEntityId());
    compositionNodeToAdd.setErrors(node.getErrors());
    compositionNodeToAdd.setSubEntitiesValidationData(null);

    entitiesWithErrors.add(compositionNodeToAdd);
  }

  public void removeNodesWithoutErrors() {
    roots.forEach(root -> removeNodesWithoutErrors(root, null));
  }


  private CompositionEntityData getCompositionEntityDataById(CompositionEntityValidationData
                                                                 entity) {
    for (Map.Entry<CompositionEntityId, CompositionEntityData> entityEntry : entities
        .entrySet()) {
      if (entityEntry.getKey().getId().equals(entity.getEntityId())) {
        return entityEntry.getValue();
      }
    }
    return null;
  }


  private void updateValidationCompositionEntityName(Set<CompositionEntityValidationData>
                                                         compositionSet) {
    for (CompositionEntityValidationData entity : compositionSet) {
      String compositionData = getCompositionDataAsString(entity);
      if (entity.getEntityType().equals(CompositionEntityType.vsp) ||
          Objects.nonNull(compositionData)) {
        entity.setEntityName(getEntityNameByEntityType(compositionData, entity));
      }
    }
  }

  private String getCompositionDataAsString(CompositionEntityValidationData entity) {
    CompositionEntityData compositionEntityData = getCompositionEntityDataById(entity);
    return compositionEntityData == null ? null : compositionEntityData.entity.getCompositionData();
  }


  private String getEntityNameByEntityType(String compositionData,
                                           CompositionEntityValidationData entity) {
    switch (entity.getEntityType()) {
      case component:
        ComponentData component = JsonUtil.json2Object(compositionData, ComponentData.class);
        return component.getDisplayName();

      case nic:
        Nic nic = JsonUtil.json2Object(compositionData, Nic.class);
        return nic.getName();

      case network:
        Network network = JsonUtil.json2Object(compositionData, Network.class);
        return network.getName();

      case image:
        Image image = JsonUtil.json2Object(compositionData, Image.class);
        return image.getFileName();

      case vsp:
        CompositionEntityData vspEntity = getCompositionEntityDataById(entity);
        VspQuestionnaireEntity vspQuestionnaireEntity = (VspQuestionnaireEntity) vspEntity.entity;
        VspDetails vspDetails =
            vspInfoDao.get(new VspDetails(vspQuestionnaireEntity.getId(),
                vspQuestionnaireEntity.getVersion()));
        return vspDetails.getName();
    }

    return null;
  }

  private void removeNodesWithoutErrors(CompositionEntityValidationData node,
                                        CompositionEntityValidationData parent) {

    if (Objects.isNull(node)) {
      return;
    }

    if (hasChildren(node)) {
      Collection<CompositionEntityValidationData> subNodes =
          new ArrayList<>(node.getSubEntitiesValidationData());
      subNodes.forEach(subNode -> removeNodesWithoutErrors(subNode, node));
      node.setSubEntitiesValidationData(subNodes);

      if (canNodeGetRemovedFromValidationDataTree(node)) {
        removeNodeFromChildren(parent, node);
      }
    } else if (canNodeGetRemovedFromValidationDataTree(node)) {
      removeNodeFromChildren(parent, node);
    }
  }

  private void removeNodeFromChildren(CompositionEntityValidationData parent,
                                      CompositionEntityValidationData childToRemove) {
    if (!Objects.isNull(parent)) {
      parent.getSubEntitiesValidationData().remove(childToRemove);
    }
  }

  private boolean hasChildren(CompositionEntityValidationData node) {
    return !CollectionUtils.isEmpty(node.getSubEntitiesValidationData());
  }

  private boolean canNodeGetRemovedFromValidationDataTree(CompositionEntityValidationData node) {
    return !hasChildren(node) && CollectionUtils.isEmpty(node.getErrors());
  }


  private void addValidationDataEntity(
      Map<CompositionEntityId, CompositionEntityValidationData> entitiesValidationData,
      CompositionEntityId entityId, CompositionEntity entity) {
    if (entitiesValidationData.containsKey(entityId)) {
      return;
    }

    CompositionEntityValidationData validationData =
        new CompositionEntityValidationData(entity.getType(), entity.getId());
    entitiesValidationData.put(entityId, validationData);

    CompositionEntityId parentEntityId = entityId.getParentId();
    if (parentEntityId == null) {
      roots.add(validationData);
    } else {
      CompositionEntityData parentEntity = entities.get(parentEntityId);
      if (parentEntity == null) {
        roots.add(validationData);
      } else {
        addValidationDataEntity(entitiesValidationData, parentEntityId, parentEntity.entity);
        entitiesValidationData.get(parentEntityId).addSubEntityValidationData(validationData);
      }
    }
  }

  private void addErrorsToTree(CompositionEntityValidationData node,
                               CompositionEntityId parentNodeId,
                               Map<CompositionEntityId, Collection<String>> errors) {
    if (node == null) {
      return;
    }
    CompositionEntityId nodeId = new CompositionEntityId(node.getEntityId(), parentNodeId);
    node.setErrors(errors.get(nodeId));

    if (node.getSubEntitiesValidationData() != null) {
      node.getSubEntitiesValidationData()
          .forEach(subNode -> addErrorsToTree(subNode, nodeId, errors));
    }
  }

  private Collection<String> validateQuestionnaire(CompositionEntityData compositionEntityData) {
    logger.debug(String.format("validateQuestionnaire start:  " +
            "[entity.type]=%s, [entity.id]=%s, [entity.questionnaireString]=%s",
        compositionEntityData.entity.getType().name(),
        compositionEntityData.entity.getCompositionEntityId().toString(),
        compositionEntityData.entity.getQuestionnaireData()));

    if (Objects.isNull(compositionEntityData.entity.getQuestionnaireData()) ||
        !JsonUtil.isValidJson(compositionEntityData.entity.getQuestionnaireData())) {
      return Collections.singletonList(String
          .format(MISSING_OR_INVALID_QUESTIONNAIRE_MSG, compositionEntityData.entity.getType()));
    }

    return JsonUtil.validate(
        compositionEntityData.entity.getQuestionnaireData() == null
            ? JsonUtil.object2Json(new Object())
            : compositionEntityData.entity.getQuestionnaireData(),
        getSchema(compositionEntityData.entity.getType(), SchemaTemplateContext.questionnaire,
            compositionEntityData.schemaTemplateInput));
  }

  private String getSchema(CompositionEntityType compositionEntityType,
                           SchemaTemplateContext schemaTemplateContext,
                           SchemaTemplateInput schemaTemplateInput) {
    return schemaTemplateInput == null
        ? nonDynamicSchemas.computeIfAbsent(compositionEntityType,
        k -> generateSchema(schemaTemplateContext, compositionEntityType, null))
        : generateSchema(schemaTemplateContext, compositionEntityType, schemaTemplateInput);
  }

  private static class CompositionEntityData {
    private CompositionEntity entity;
    private SchemaTemplateInput schemaTemplateInput;

    CompositionEntityData(CompositionEntity entity, SchemaTemplateInput schemaTemplateInput) {
      this.entity = entity;
      this.schemaTemplateInput = schemaTemplateInput;
    }

  }

  // todo - make SchemaGenerator non static and mock it in UT instead of mocking this method (and
  // make the method private

  protected String generateSchema(SchemaTemplateContext schemaTemplateContext,
                                  CompositionEntityType compositionEntityType,
                                  SchemaTemplateInput schemaTemplateInput) {
    return SchemaGenerator
        .generate(schemaTemplateContext, compositionEntityType, schemaTemplateInput);
  }

  @Override
  public DeploymentFlavorEntity createDeploymentFlavor(DeploymentFlavorEntity deploymentFlavor) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    deploymentFlavor.setId(CommonMethods.nextUuId());
    deploymentFlavorDao.create(deploymentFlavor);
    return deploymentFlavor;
  }

  @Override
  public ImageEntity createImage(ImageEntity image) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    image.setId(CommonMethods.nextUuId());

    image.setQuestionnaireData(
        new JsonSchemaDataGenerator(SchemaGenerator
            .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.image, null))
            .generateData());

    imageDao.create(image);
    mdcDataDebugMessage.debugExitMessage(null, null);
    return image;
  }

  public void saveComputesFlavorByComponent(String vspId, Version version, Component component,
                                            String componentId) {
    if (CollectionUtils.isNotEmpty(component.getCompute())) {
      for (ComputeData flavor : component.getCompute()) {
        ComputeEntity computeEntity = new ComputeEntity(vspId, version, componentId, null);
        computeEntity.setComputeCompositionData(flavor);
        computeEntity.setQuestionnaireData(
            new JsonSchemaDataGenerator(SchemaGenerator
                .generate(SchemaTemplateContext.questionnaire, CompositionEntityType.compute,
                    null)).generateData());

        computeDao.create(computeEntity);
      }
    }
  }

  public void saveImagesByComponent(String vspId, Version version, Component component, String
      componentId) {
    if (CollectionUtils.isNotEmpty(component.getImages())) {
      for (Image img : component.getImages()) {
        ImageEntity imageEntity = new ImageEntity(vspId, version, componentId, null);
        imageEntity.setImageCompositionData(img);
        createImage(imageEntity);
      }
    }
  }

}
