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

package org.openecomp.sdc.vendorsoftwareproduct.services;

import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateInput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Composition entity data manager.
 */
public class CompositionEntityDataManager {

  private static final String COMPOSITION_ENTITY_DATA_MANAGER_ERR =
      "COMPOSITION_ENTITY_DATA_MANAGER_ERR";
  private static final String COMPOSITION_ENTITY_DATA_MANAGER_ERR_MSG =
      "Invalid input: %s may not be null";

  private Map<org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId,
      CompositionEntityData> entities = new HashMap<>();
  private Map<org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType,
      String> nonDynamicSchemas = new HashMap<>();
  private List<CompositionEntityValidationData> roots = new ArrayList<>();

  /**
   * Validate entity composition entity validation data.
   *
   * @param entity                the entity
   * @param schemaTemplateContext the schema template context
   * @param schemaTemplateInput   the schema template input
   * @return the composition entity validation data
   */
  public static CompositionEntityValidationData validateEntity(
      org.openecomp.sdc.vendorsoftwareproduct.dao.type.CompositionEntity entity,
      SchemaTemplateContext schemaTemplateContext,
      SchemaTemplateInput schemaTemplateInput) {
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
        SchemaGenerator.generate(schemaTemplateContext, entity.getType(), schemaTemplateInput)));

    return validationData;
  }

  /**
   * Add entity.
   *
   * @param entity              the entity
   * @param schemaTemplateInput the schema template input
   */
  public void addEntity(org.openecomp.sdc.vendorsoftwareproduct.dao.type.CompositionEntity entity,
                        SchemaTemplateInput schemaTemplateInput) {
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
  public Map<org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId,
      Collection<String>> validateEntitiesQuestionnaire() {
    Map<org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId,
        Collection<String>>
        errorsByEntityId = new HashMap<>();

    entities.entrySet().stream().forEach(entry -> {
      Collection<String> errors = validateQuestionnaire(entry.getValue());
      if (errors != null) {
        errorsByEntityId.put(entry.getKey(), errors);
      }
    });

    return errorsByEntityId;
  }

  /**
   * Build trees.
   */
  public void buildTrees() {
    Map<org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId,
        CompositionEntityValidationData>
        entitiesValidationData =
        new HashMap<>();
    entities.entrySet().stream().forEach(
        entry -> addValidationDataEntity(entitiesValidationData, entry.getKey(),
            entry.getValue().entity));
  }

  /**
   * Gets trees.
   *
   * @return the trees
   */
  public Collection<CompositionEntityValidationData> getTrees() {
    return roots;
  }

  /**
   * Add errors to trees.
   *
   * @param errors the errors
   */
  public void addErrorsToTrees(
      Map<org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId,
          Collection<String>> errors) {
    roots.stream().forEach(root -> addErrorsToTree(root, null, errors));
  }

  private void addValidationDataEntity(
      Map<org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId,
          CompositionEntityValidationData> entitiesValidationData,
      org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId entityId,
      org.openecomp.sdc.vendorsoftwareproduct.dao.type.CompositionEntity entity) {
    if (entitiesValidationData.containsKey(entityId)) {
      return;
    }

    CompositionEntityValidationData validationData =
        new CompositionEntityValidationData(entity.getType(), entity.getId());
    entitiesValidationData.put(entityId, validationData);

    org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId parentEntityId =
        entityId.getParentId();
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
       org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId parentNodeId,
       Map<org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId,
           Collection<String>> errors) {
    if (node == null) {
      return;
    }
    org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId
        nodeId = new org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId(
        node.getEntityId(), parentNodeId);
    node.setErrors(errors.get(nodeId));

    if (node.getSubEntitiesValidationData() != null) {
      node.getSubEntitiesValidationData().stream()
          .forEach(subNode -> addErrorsToTree(subNode, nodeId, errors));
    }
  }

  private Collection<String> validateQuestionnaire(CompositionEntityData compositionEntityData) {
    return JsonUtil.validate(
        compositionEntityData.entity.getQuestionnaireData() == null ? JsonUtil
            .object2Json(new Object()) : compositionEntityData.entity.getQuestionnaireData(),
        getSchema(compositionEntityData.entity.getType(), SchemaTemplateContext.questionnaire,
            compositionEntityData.schemaTemplateInput));
  }

  private String getSchema(
      org.openecomp.sdc.vendorsoftwareproduct.types
          .composition.CompositionEntityType compositionEntityType,
      SchemaTemplateContext schemaTemplateContext,
      SchemaTemplateInput schemaTemplateInput) {
    return schemaTemplateInput == null ? getNonDynamicSchema(schemaTemplateContext,
        compositionEntityType) : SchemaGenerator
        .generate(schemaTemplateContext, compositionEntityType, schemaTemplateInput);
  }

  private String getNonDynamicSchema(SchemaTemplateContext schemaTemplateContext,
      org.openecomp.sdc.vendorsoftwareproduct.types.composition
        .CompositionEntityType compositionEntityType) {
    String schema = nonDynamicSchemas.get(compositionEntityType);
    if (schema == null) {
      schema = SchemaGenerator.generate(schemaTemplateContext, compositionEntityType, null);
      nonDynamicSchemas.put(compositionEntityType, schema);
    }
    return schema;
  }

  private static class CompositionEntityData {
    private org.openecomp.sdc.vendorsoftwareproduct.dao.type.CompositionEntity entity;
    private SchemaTemplateInput schemaTemplateInput;

    /**
     * Instantiates a new Composition entity data.
     *
     * @param entity              the entity
     * @param schemaTemplateInput the schema template input
     */
    public CompositionEntityData(
        org.openecomp.sdc.vendorsoftwareproduct.dao.type.CompositionEntity entity,
        SchemaTemplateInput schemaTemplateInput) {
      this.entity = entity;
      this.schemaTemplateInput = schemaTemplateInput;
    }
  }
}
