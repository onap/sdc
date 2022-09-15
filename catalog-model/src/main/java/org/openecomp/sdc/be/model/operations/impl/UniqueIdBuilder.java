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
package org.openecomp.sdc.be.model.operations.impl;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.resources.data.TagData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.ValidationUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UniqueIdBuilder {

    static final String HEAT_PARAM_PREFIX = "heat_";
    static final String DOT = ".";
    private static final UserData userData = new UserData();
    private static final TagData tagData = new TagData();
    private static final Map<NodeTypeEnum, String> nodeTypeToUniqueKeyMapper = new EnumMap<>(NodeTypeEnum.class);

    static {
        nodeTypeToUniqueKeyMapper.put(NodeTypeEnum.User, userData.getUniqueIdKey());
        nodeTypeToUniqueKeyMapper.put(NodeTypeEnum.Tag, tagData.getUniqueIdKey());
    }

    public static String buildPropertyUniqueId(String resourceId, String propertyName) {
        return buildUid(resourceId, propertyName);
    }

    static String buildHeatParameterUniqueId(String resourceId, String propertyName) {
        return resourceId + DOT + HEAT_PARAM_PREFIX + propertyName;
    }

    static String buildHeatParameterValueUniqueId(String resourceId, String artifactLabel, String propertyName) {
        return buildTypeUid(resourceId, artifactLabel, propertyName);
    }

    /**
     * find the unique id key of a node on the graph
     *
     * @param nodeTypeEnum
     * @return
     */
    public static String getKeyByNodeType(NodeTypeEnum nodeTypeEnum) {
        String uniqueID = nodeTypeToUniqueKeyMapper.get(nodeTypeEnum);
        if (uniqueID == null) {
            uniqueID = GraphPropertiesDictionary.UNIQUE_ID.getProperty();
        }
        return uniqueID;
    }

    public static String buildResourceUniqueId() {
        return generateUUID();
    }

    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static String buildComponentUniqueId() {
        return generateUUID();
    }

    public static String buildCapabilityTypeUid(final String modelName, String type) {
        return StringUtils.isEmpty(modelName) ? type : buildUid(modelName, type);
    }

    public static String buildRelationshipTypeUid(final String modelName, final String type) {
        return StringUtils.isEmpty(modelName) ? type : buildUid(modelName, type);
    }

    public static String buildInterfaceTypeUid(final String modelName, String type) {
        return StringUtils.isEmpty(modelName) ? type : buildUid(modelName, type);
    }

    public static String buildAttributeUid(String resourceId, String attName) {
        return buildTypeUid(NodeTypeEnum.Attribute.getName(), resourceId, attName);
    }

    public static String buildRequirementUid(String resourceId, String reqName) {
        return buildUid(resourceId, reqName);
    }

    public static String buildCapabilityUid(String resourceId, String capabilityName) {
        return buildTypeUid(NodeTypeEnum.Capability.getName(), resourceId, capabilityName);
    }

    public static String buildArtifactByInterfaceUniqueId(String resourceId, String interfaceName, String operation, String artifactLabel) {
        return resourceId + DOT + interfaceName + DOT + operation + DOT + artifactLabel;
    }

    public static String buildInstanceArtifactUniqueId(String parentId, String instanceId, String artifactLabel) {
        return buildTypeUid(parentId, instanceId, artifactLabel);
    }

    public static String buildResourceInstanceUniqueId(String serviceId, String resourceId, String logicalName) {
        return buildTypeUid(serviceId, resourceId, logicalName);
    }

    public static String buildRelationshipInstInstanceUid() {
        return generateUUID();
    }

    /*
     * TODO Pavel To be removed when new category logic comes in
     */
    static String buildResourceCategoryUid(String categoryName, String subcategoryName, NodeTypeEnum type) {
        return buildTypeUid(type.getName(), categoryName, subcategoryName);
    }

    /*
     * TODO Pavel To be removed when new category logic comes in
     */
    static String buildServiceCategoryUid(String categoryName, NodeTypeEnum type) {
        return buildUid(type.getName(), categoryName);
    }

    // New logic
    public static String buildCategoryUid(String categoryName, NodeTypeEnum type) {
        return buildUid(type.getName(), categoryName);
    }

    public static String buildComponentCategoryUid(String categoryName, VertexTypeEnum type) {
        return buildUid(type.getName(), ValidationUtils.normalizeCategoryName4Uniqueness(categoryName));
    }

    public static String buildSubCategoryUid(String categoryUid, String subCategoryName) {
        return buildUid(categoryUid, subCategoryName);
    }

    public static String buildGroupingUid(String subCategoryUid, String groupingName) {
        return buildUid(subCategoryUid, groupingName);
    }

    static String buildResourceInstancePropertyValueUid(String resourceInstanceUniqueId, Integer index) {
        return buildUid(resourceInstanceUniqueId, NodeTypeEnum.Property, index);
    }

    public static String buildComponentPropertyUniqueId(String resourceId, String propertyName) {
        return buildTypeUid(NodeTypeEnum.Property.getName(), resourceId, propertyName);
    }

    static String buildResourceInstanceAttributeValueUid(String resourceInstanceUniqueId, Integer index) {
        return buildUid(resourceInstanceUniqueId, NodeTypeEnum.Attribute, index);
    }

    static String buildResourceInstanceInputValueUid(String resourceInstanceUniqueId, Integer index) {
        return buildUid(resourceInstanceUniqueId, NodeTypeEnum.Input, index);
    }

    static String buildAdditionalInformationUniqueId(String resourceUniqueId) {
        return buildUid(resourceUniqueId, "additionalinformation");
    }

    public static String buildDataTypeUid(final String modelName, final String name) {
        return buildTypeUidWithModel(modelName, name, "datatype");
    }

    public static String buildInvariantUUID() {
        return generateUUID();
    }

    public static String buildGroupTypeUid(final String modelName, final String type, final String version) {
        return buildTypeUidWithModel(modelName, type, version, "grouptype");
    }

    public static String buildPolicyTypeUid(String modelName, String type, String version, String resourceName) {
        return buildTypeUidWithModel(modelName, type, version, resourceName);
    }

    private static String buildTypeUidWithModel(String modelName, String type, String version, String resourceName) {
        return StringUtils.isEmpty(modelName) ?
            buildTypeUid(type, version, resourceName) : modelName + DOT + buildTypeUid(type, version, resourceName);
    }

    static String buildTypeUid(String type, String version, String resourceName) {
        return type + DOT + buildUid(version, resourceName);
    }

    public static String buildPolicyUniqueId(String componentId, String name) {
        return buildUid(componentId, name) + Constants.POLICY_UID_POSTFIX;
    }

    public static String buildGroupPropertyValueUid(String groupUniqueId, Integer index) {
        return buildUid(groupUniqueId, NodeTypeEnum.Property, index);
    }

    static String buildModelUid(final String modelName) {
        return buildUid(NodeTypeEnum.Model.getName(), modelName);
    }

    public static String buildArtifactTypeUid(final String modelName, final String name) {
        return buildTypeUidWithModel(modelName, name, "artifactype");
    }

    private static String buildTypeUidWithModel(final String modelName, final String name, final String nodeType) {
        return StringUtils.isEmpty(modelName) ? buildUid(name, nodeType) : modelName + DOT + buildUid(name, nodeType);
    }

    private static String buildUid(final String first, final String second) {
        return first + DOT + second;
    }

    private static String buildUid(final String first, final NodeTypeEnum nodeType, final Integer index) {
        return first + DOT + nodeType.getName() + DOT + index;
    }
}
