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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.resources.data.ResourceCategoryData;
import org.openecomp.sdc.be.resources.data.ServiceCategoryData;
import org.openecomp.sdc.be.resources.data.TagData;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.common.util.ValidationUtils;

public class UniqueIdBuilder {

	private static String DOT = ".";
	private static final String HEAT_PARAM_PREFIX = "heat_";
	
	public static String buildPropertyUniqueId(String resourceId, String propertyName) {
		return resourceId + DOT + propertyName;
	}
	
	public static String buildHeatParameterUniqueId(String resourceId, String propertyName) {
		return resourceId + DOT + HEAT_PARAM_PREFIX + propertyName;
	}

	public static String buildHeatParameterValueUniqueId(String resourceId, String artifactLabel, String propertyName) {
		return resourceId + DOT + artifactLabel + DOT + propertyName;
	}

	private static UserData userData = new UserData();
	private static TagData tagData = new TagData();
	private static ResourceCategoryData resCategoryData = new ResourceCategoryData();
	private static ServiceCategoryData serCategoryData = new ServiceCategoryData();

	private static Map<NodeTypeEnum, String> nodeTypeToUniqueKeyMapper = new HashMap<NodeTypeEnum, String>();

	static {

		nodeTypeToUniqueKeyMapper.put(NodeTypeEnum.User, userData.getUniqueIdKey());
		nodeTypeToUniqueKeyMapper.put(NodeTypeEnum.Tag, tagData.getUniqueIdKey());
		nodeTypeToUniqueKeyMapper.put(NodeTypeEnum.ResourceCategory, resCategoryData.getUniqueIdKey());
		nodeTypeToUniqueKeyMapper.put(NodeTypeEnum.ServiceCategory, serCategoryData.getUniqueIdKey());
	}

	/**
	 * find the unique id key of a node on the graph
	 * 
	 * @param nodeTypeEnum
	 * @return
	 */
	public static String getKeyByNodeType(NodeTypeEnum nodeTypeEnum) {

		String key = nodeTypeToUniqueKeyMapper.get(nodeTypeEnum);
		if (key == null) {
			key = GraphPropertiesDictionary.UNIQUE_ID.getProperty();
		}

		return key;
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

	public static String buildConstantProductId() {
		return generateUUID();
	}

	public static String buildCapabilityTypeUid(String type) {
		return type;
	}

	public static String buildAttributeUid(String resourceId, String attName) {
		return NodeTypeEnum.Attribute.getName() + DOT + resourceId + DOT + attName;
	}
	public static String buildArtifactUid(String parentId, String label) {
		return parentId + DOT + label;
	}
	public static String buildRequirementUid(String resourceId, String reqName) {
		return resourceId + DOT + reqName;
	}

	public static String buildRequirementImplUid(String resourceId, String reqName) {

		return NodeTypeEnum.RequirementImpl.getName() + DOT + resourceId + DOT + reqName;

	}

	public static String buildCapabilityUid(String resourceId, String capabilityName) {
		return NodeTypeEnum.Capability.getName() + DOT + resourceId + DOT + capabilityName;
	}

	public static String buildCapabilityInstanceUid(String parentId, String capabilityName) {
		return NodeTypeEnum.CapabilityInst.getName() + DOT + parentId + DOT + capabilityName;
	}

	public static String buildPropertyValueUniqueId(String parentId, String paramName) {
		return NodeTypeEnum.PropertyValue.getName() + DOT + parentId + DOT + paramName;
	}
	
	public static String buildArtifactByInterfaceUniqueId(String resourceId, String interfaceName, String operation, String artifactLabel) {

		return resourceId + DOT + interfaceName + DOT + operation + DOT + artifactLabel;
	}

	// public static String
	// buildArtifactByInterfaceUniqueIdAndRsrcNameVersion(String
	// resourceName,String resourceVersion,String interfaceName,String
	// operation,String artifactLabel) {
	// String resourceId = UniqueIdBuilder.buildResourceUniqueId(resourceName,
	// resourceVersion);
	// return resourceId + DOT + interfaceName + DOT +operation + DOT +
	// artifactLabel;
	// }
	public static String buildArtifactByInterfaceUniqueIdAndRsrcId(String resourceId, String interfaceName, String operation, String artifactLabel) {
		return resourceId + DOT + interfaceName + DOT + operation + DOT + artifactLabel;
	}

	public static String buildOperationByInterfaceUniqueId(String resourceId, String interfaceName, String operation) {

		return resourceId + DOT + interfaceName + DOT + operation;
	}

	public static String buildInterfaceUniqueId(String resourceId, String interfaceName) {
		return resourceId + DOT + interfaceName;
	}

	public static String buildResourceInstanceUniuqeId(String serviceId, String resourceId, String logicalName) {

		return serviceId + DOT + resourceId + DOT + logicalName;
	}

	public static String buildRelationsipInstInstanceUid(String resourceInstUid, String requirement) {

		return generateUUID();
	}

	/*
	 * TODO Pavel To be removed when new category logic comes in
	 */
	public static String buildResourceCategoryUid(String categoryName, String subcategoryName, NodeTypeEnum type) {
		return type.getName() + DOT + categoryName + DOT + subcategoryName;
	}

	/*
	 * TODO Pavel To be removed when new category logic comes in
	 */
	public static String buildServiceCategoryUid(String categoryName, NodeTypeEnum type) {
		return type.getName() + DOT + categoryName;
	}

	// New logic
	public static String buildCategoryUid(String categoryName, NodeTypeEnum type) {
		return type.getName() + DOT + categoryName;
	}
	public static String buildComponentCategoryUid(String categoryName, VertexTypeEnum type) {
		return type.getName() + DOT + ValidationUtils.normalizeCategoryName4Uniqueness(categoryName);
	}

	public static String buildSubCategoryUid(String categoryUid, String subCategoryName) {
		return categoryUid + DOT + subCategoryName;
	}

	public static String buildGroupingUid(String subCategoryUid, String groupingName) {
		return subCategoryUid + DOT + groupingName;
	}

	public static String buildResourceInstancePropertyValueUid(String resourceInstanceUniqueId, Integer index) {
		return resourceInstanceUniqueId + DOT + "property" + DOT + index;
	}
	
	public static String buildComponentPropertyUniqueId(String resourceId, String propertyName) {
		return NodeTypeEnum.Property.getName() + DOT + resourceId + DOT + propertyName;
	}

	public static String buildResourceInstanceAttributeValueUid(String resourceInstanceUniqueId, Integer index) {
		return resourceInstanceUniqueId + DOT + "attribute" + DOT + index;
	}

	public static String buildResourceInstanceInputValueUid(String resourceInstanceUniqueId, Integer index) {
		return resourceInstanceUniqueId + DOT + "input" + DOT + index;
	}

	public static String buildAdditionalInformationUniqueId(String resourceUniqueId) {
		return resourceUniqueId + DOT + "additionalinformation";
	}

	public static String buildHeatParamValueUid(String heatEnvArtifactId, String parameterName) {
		return heatEnvArtifactId + DOT + parameterName;
	}

	public static String buildDataTypeUid(String name) {
		return name + DOT + "datatype";
	}

	public static String buildInvariantUUID() {
		return generateUUID();
	}

	public static String buildGroupTypeUid(String type, String version) {
		return type + DOT + version + DOT + "grouptype";
	}

	public static String buildPolicyTypeUid(String type, String version) {
		return type + DOT + version + DOT + "policytype";
	}

	public static String buildGroupUniqueId(String componentId, String name) {
		return componentId + DOT + name + DOT + "group";
	}

	public static String buildGroupPropertyValueUid(String groupUniqueId, Integer index) {
		return groupUniqueId + DOT + "property" + DOT + index;

	}
	
	public static String buildUserFunctionalMenuUid(String userId) {
		return userId + DOT + "functionalmenu";

	}
}
