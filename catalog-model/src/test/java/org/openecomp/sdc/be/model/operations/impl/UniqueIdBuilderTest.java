/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 *
 */

package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder.DOT;
import static org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder.HEAT_PARAM_PREFIX;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.common.api.Constants;

class UniqueIdBuilderTest {

    private static final String resourceId = "resourceId";
    private static final String propertyName = "propertyName";
    private static final String modelName = "modelName";
    private static final String version = "version";
    private static final String name = "name";
    private static final String componentId = "componentId";
    private static final String groupUniqueId = "groupUniqueId";
    private static final String attName = "attName";
    private static final String reqName = "reqName";
    private static final String capabilityName = "capabilityName";
    private static final String interfaceName = "interfaceName";
    private static final String operation = "operation";
    private static final String type = "type";
    private static final String artifactLabel = "artifactLabel";
    private static final String parentId = "parentId";
    private static final String instanceId = "instanceId";
    private static final String serviceId = "serviceId";
    private static final String logicalName = "logicalName";
    private static final String categoryName = "categoryName";
    private static final String subcategoryName = "subcategoryName";
    private static final String groupingName = "groupingName";
    private static final String resourceInstanceUniqueId = "resourceInstanceUniqueId";
    private static final String pattern = "\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}";

    @Test
    void test_buildPropertyUniqueId() {
        final String result = UniqueIdBuilder.buildPropertyUniqueId(resourceId, propertyName);
        assertEquals(resourceId + DOT + propertyName, result);
    }

    @Test
    void test_buildHeatParameterUniqueId() {
        final String result = UniqueIdBuilder.buildHeatParameterUniqueId(resourceId, propertyName);
        assertEquals(resourceId + DOT + HEAT_PARAM_PREFIX + propertyName, result);
    }

    @Test
    void test_buildHeatParameterValueUniqueId() {
        final String result = UniqueIdBuilder.buildHeatParameterValueUniqueId(resourceId, artifactLabel, propertyName);
        assertEquals(resourceId + DOT + artifactLabel + DOT + propertyName, result);
    }

    @Test
    void test_getKeyByNodeType() {
        for (final NodeTypeEnum value : NodeTypeEnum.values()) {
            switch (value) {
                case User:
                    assertEquals("userId", UniqueIdBuilder.getKeyByNodeType(value));
                    break;
                case Tag:
                    assertEquals("name", UniqueIdBuilder.getKeyByNodeType(value));
                    break;
                default:
                    assertEquals("uid", UniqueIdBuilder.getKeyByNodeType(value));
                    break;
            }
        }
    }

    @Test
    void test_buildResourceUniqueId() {
        final String result = UniqueIdBuilder.buildResourceUniqueId();
        assertTrue(Pattern.matches(pattern, result));
    }

    @Test
    void test_generateUUID() {
        final String result = UniqueIdBuilder.generateUUID();
        assertTrue(Pattern.matches(pattern, result));
    }

    @Test
    void test_buildComponentUniqueId() {
        final String result = UniqueIdBuilder.buildComponentUniqueId();
        assertTrue(Pattern.matches(pattern, result));
    }

    @Test
    void test_buildCapabilityTypeUid() {
        String result = UniqueIdBuilder.buildCapabilityTypeUid(modelName, type);
        assertEquals(modelName + DOT + type, result);
        result = UniqueIdBuilder.buildCapabilityTypeUid(null, type);
        assertEquals(type, result);
    }

    @Test
    void test_buildRelationshipTypeUid() {
        String result = UniqueIdBuilder.buildRelationshipTypeUid(modelName, type);
        assertEquals(modelName + DOT + type, result);
        result = UniqueIdBuilder.buildRelationshipTypeUid(null, type);
        assertEquals(type, result);
    }

    @Test
    void test_buildInterfaceTypeUid() {
        String result = UniqueIdBuilder.buildInterfaceTypeUid(modelName, type);
        assertEquals(modelName + DOT + type, result);
        result = UniqueIdBuilder.buildInterfaceTypeUid(null, type);
        assertEquals(type, result);
    }

    @Test
    void test_buildAttributeUid() {
        final String result = UniqueIdBuilder.buildAttributeUid(resourceId, attName);
        assertEquals(NodeTypeEnum.Attribute.getName() + DOT + resourceId + DOT + attName, result);
    }

    @Test
    void test_buildRequirementUid() {
        final String result = UniqueIdBuilder.buildRequirementUid(resourceId, reqName);
        assertEquals(resourceId + DOT + reqName, result);
    }

    @Test
    void test_buildCapabilityUid() {
        final String result = UniqueIdBuilder.buildCapabilityUid(resourceId, capabilityName);
        assertEquals(NodeTypeEnum.Capability.getName() + DOT + resourceId + DOT + capabilityName, result);
    }

    @Test
    void test_buildArtifactByInterfaceUniqueId() {
        final String result = UniqueIdBuilder.buildArtifactByInterfaceUniqueId(resourceId, interfaceName, operation, artifactLabel);
        assertEquals(resourceId + DOT + interfaceName + DOT + operation + DOT + artifactLabel, result);
    }

    @Test
    void test_buildInstanceArtifactUniqueId() {
        final String result = UniqueIdBuilder.buildInstanceArtifactUniqueId(parentId, instanceId, artifactLabel);
        assertEquals(parentId + DOT + instanceId + DOT + artifactLabel, result);
    }

    @Test
    void test_buildResourceInstanceUniqueId() {
        final String result = UniqueIdBuilder.buildResourceInstanceUniqueId(serviceId, resourceId, logicalName);
        assertEquals(serviceId + DOT + resourceId + DOT + logicalName, result);
    }

    @Test
    void test_buildRelationshipInstInstanceUid() {
        final String result = UniqueIdBuilder.buildRelationshipInstInstanceUid();
        assertTrue(Pattern.matches(pattern, result));
    }

    @Test
    void test_buildResourceCategoryUid() {
        final String result = UniqueIdBuilder.buildResourceCategoryUid(categoryName, subcategoryName, NodeTypeEnum.Product);
        assertEquals(NodeTypeEnum.Product.getName() + DOT + categoryName + DOT + subcategoryName, result);
    }

    @Test
    void test_buildServiceCategoryUid() {
        final String result = UniqueIdBuilder.buildServiceCategoryUid(categoryName, NodeTypeEnum.Service);
        assertEquals(NodeTypeEnum.Service.getName() + DOT + categoryName, result);
    }

    @Test
    void test_buildCategoryUid() {
        final String result = UniqueIdBuilder.buildCategoryUid(categoryName, NodeTypeEnum.Requirement);
        assertEquals(NodeTypeEnum.Requirement.getName() + DOT + categoryName, result);
    }

    @Test
    void test_buildComponentCategoryUid() {
        final String result = UniqueIdBuilder.buildComponentCategoryUid(categoryName, VertexTypeEnum.TOPOLOGY_TEMPLATE);
        assertEquals(VertexTypeEnum.TOPOLOGY_TEMPLATE.getName() + DOT + categoryName.toLowerCase(), result);
    }

    @Test
    void test_buildSubCategoryUid() {
        final String result = UniqueIdBuilder.buildSubCategoryUid(categoryName, subcategoryName);
        assertEquals(categoryName + DOT + subcategoryName, result);
    }

    @Test
    void test_buildGroupingUid() {
        final String result = UniqueIdBuilder.buildGroupingUid(subcategoryName, groupingName);
        assertEquals(subcategoryName + DOT + groupingName, result);
    }

    @Test
    void test_buildResourceInstancePropertyValueUid() {
        final String result = UniqueIdBuilder.buildResourceInstancePropertyValueUid(resourceInstanceUniqueId, 99);
        assertEquals(resourceInstanceUniqueId + DOT + NodeTypeEnum.Property.getName() + DOT + 99, result);
    }

    @Test
    void test_buildComponentPropertyUniqueId() {
        final String result = UniqueIdBuilder.buildComponentPropertyUniqueId(resourceId, propertyName);
        assertEquals(NodeTypeEnum.Property.getName() + DOT + resourceId + DOT + propertyName, result);
    }

    @Test
    void test_buildResourceInstanceAttributeValueUid() {
        final String result = UniqueIdBuilder.buildResourceInstanceAttributeValueUid(resourceInstanceUniqueId, 88);
        assertEquals(resourceInstanceUniqueId + DOT + NodeTypeEnum.Attribute.getName() + DOT + 88, result);
    }

    @Test
    void test_buildResourceInstanceInputValueUid() {
        final String result = UniqueIdBuilder.buildResourceInstanceInputValueUid(resourceInstanceUniqueId, 77);
        assertEquals(resourceInstanceUniqueId + DOT + NodeTypeEnum.Input.getName() + DOT + 77, result);
    }

    @Test
    void test_buildAdditionalInformationUniqueId() {
        final String result = UniqueIdBuilder.buildAdditionalInformationUniqueId(resourceId);
        assertEquals(resourceId + DOT + "additionalinformation", result);
    }

    @Test
    void test_buildDataTypeUid() {
        String result = UniqueIdBuilder.buildDataTypeUid(modelName, name);
        assertEquals(modelName + DOT + name + DOT + "datatype", result);
        result = UniqueIdBuilder.buildDataTypeUid(null, name);
        assertEquals(name + DOT + "datatype", result);
    }

    @Test
    void test_buildInvariantUUID() {
        final String result = UniqueIdBuilder.buildInvariantUUID();
        assertTrue(Pattern.matches(pattern, result));
    }

    @Test
    void test_buildGroupTypeUid() {
        String result = UniqueIdBuilder.buildGroupTypeUid(modelName, type, version);
        assertEquals(modelName + DOT + type + DOT + version + DOT + "grouptype", result);
        result = UniqueIdBuilder.buildGroupTypeUid(null, type, version);
        assertEquals(type + DOT + version + DOT + "grouptype", result);
    }

    @Test
    void test_buildPolicyTypeUid() {
        String result = UniqueIdBuilder.buildPolicyTypeUid(modelName, type, version, "policytype");
        assertEquals(modelName + DOT + type + DOT + version + DOT + "policytype", result);
        result = UniqueIdBuilder.buildPolicyTypeUid(null, type, version, "policytype");
        assertEquals(type + DOT + version + DOT + "policytype", result);
    }

    @Test
    void test_buildTypeUid() {
        final String result = UniqueIdBuilder.buildTypeUid(type, version, NodeTypeEnum.HeatParameterValue.getName());
        assertEquals(type + DOT + version + DOT + "heatParameterValue", result);
    }

    @Test
    void test_buildPolicyUniqueId() {
        final String result = UniqueIdBuilder.buildPolicyUniqueId(componentId, name);
        assertEquals(componentId + DOT + name + Constants.POLICY_UID_POSTFIX, result);
    }

    @Test
    void test_buildGroupPropertyValueUid() {
        final String result = UniqueIdBuilder.buildGroupPropertyValueUid(groupUniqueId, 55);
        assertEquals(groupUniqueId + DOT + NodeTypeEnum.Property.getName() + DOT + 55, result);
    }

    @Test
    void test_buildModelUid() {
        final String result = UniqueIdBuilder.buildModelUid(modelName);
        assertEquals(NodeTypeEnum.Model.getName() + DOT + modelName, result);
    }

    @Test
    void test_buildArtifactTypeUid() {
        String result = UniqueIdBuilder.buildArtifactTypeUid(modelName, name);
        assertEquals(modelName + DOT + name + DOT + "artifactype", result);
        result = UniqueIdBuilder.buildArtifactTypeUid(null, name);
        assertEquals(name + DOT + "artifactype", result);
    }
}
