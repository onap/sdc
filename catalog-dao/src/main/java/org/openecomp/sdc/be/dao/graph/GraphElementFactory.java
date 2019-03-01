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

package org.openecomp.sdc.be.dao.graph;

import org.openecomp.sdc.be.dao.graph.datatype.GraphElementTypeEnum;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.graph.datatype.GraphRelation;
import org.openecomp.sdc.be.dao.graph.datatype.RelationEndPoint;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.resources.data.*;
import org.openecomp.sdc.be.resources.data.category.CategoryData;
import org.openecomp.sdc.be.resources.data.category.GroupingData;
import org.openecomp.sdc.be.resources.data.category.SubCategoryData;

import java.util.Map;

public class GraphElementFactory {

	public static <T extends GraphNode> T createElement(String label, GraphElementTypeEnum type,
			Map<String, Object> properties, Class<T> clazz) {
		T element = null;

		if (type.equals(GraphElementTypeEnum.Node)) {
			element = createNode(label, properties, clazz);
		}
		return element;
	}

	public static GraphNode createElement(String label, GraphElementTypeEnum type, Map<String, Object> properties) {
		GraphNode element = null;

		if (type.equals(GraphElementTypeEnum.Node)) {
			element = createNode(label, properties);
		}
		return element;
	}

	public static GraphRelation createRelation(String type, Map<String, Object> properties, GraphNode from,
			GraphNode to) {
		GraphRelation element = new GraphRelation(type);
		RelationEndPoint endPOintFrom = new RelationEndPoint(NodeTypeEnum.getByName(from.getLabel()),
				from.getUniqueIdKey(), from.getUniqueId());
		RelationEndPoint endPOintTo = new RelationEndPoint(NodeTypeEnum.getByName(to.getLabel()), to.getUniqueIdKey(),
				to.getUniqueId());
		element.setFrom(endPOintFrom);
		element.setTo(endPOintTo);
		element.addPropertis(properties);
		return element;
	}

	private static GraphNode createNode(String label, Map<String, Object> properties) {
		GraphNode element = null;
		NodeTypeEnum type = NodeTypeEnum.getByName(label);
		if (type != null) {
			switch (type) {
			case User:
				element = new UserData(properties);
				break;
			case ResourceCategory:
				element = new ResourceCategoryData(properties);
				break;
			case ServiceCategory:
				element = new ServiceCategoryData(properties);
				break;
			case Tag:
				element = new TagData(properties);
				break;
			case Service:
				element = new ServiceMetadataData(properties);
				break;
			case Resource:
				element = new ResourceMetadataData(properties);
				break;
			case Property:
				element = new PropertyData(properties);
				break;
			case HeatParameter:
				element = new HeatParameterData(properties);
				break;
			case HeatParameterValue:
				element = new HeatParameterValueData(properties);
				break;
			}
		}
		return element;
	}

	private static <T extends GraphNode> T createNode(String label, Map<String, Object> properties, Class<T> clazz) {
		T element = null;
		NodeTypeEnum type = NodeTypeEnum.getByName(label);
		if (type != null) {
			switch (type) {
			case User:
				element = clazz.cast(new UserData(properties));
				break;
			case ResourceCategory:
				element = clazz.cast(new ResourceCategoryData(properties));
				break;
			case ServiceCategory:
				element = clazz.cast(new ServiceCategoryData(properties));
				break;
			case ResourceNewCategory:
			case ServiceNewCategory:
			case ProductCategory:
				element = clazz.cast(new CategoryData(properties));
				break;
			case ResourceSubcategory:
			case ProductSubcategory:
				element = clazz.cast(new SubCategoryData(properties));
				break;
			case ProductGrouping:
				element = clazz.cast(new GroupingData(properties));
				break;
			case Tag:
				element = clazz.cast(new TagData(properties));
				break;
			case Service:
				element = clazz.cast(new ServiceMetadataData(properties));
				break;
			case Product:
				element = clazz.cast(new ProductMetadataData(properties));
				break;
			case Resource:
				element = clazz.cast(new ResourceMetadataData(properties));
				break;
			case Attribute:
				element = clazz.cast(new AttributeData(properties));
				break;
			case Property:
				element = clazz.cast(new PropertyData(properties));
				break;
			case CapabilityType:
				element = clazz.cast(new CapabilityTypeData(properties));
				break;
			case Requirement:
				element = clazz.cast(new RequirementData(properties));
				break;
			case RequirementImpl:
				element = clazz.cast(new RequirementImplData(properties));
				break;
			case Capability:
				element = clazz.cast(new CapabilityData(properties));
				break;
			case CapabilityInst:
				element = clazz.cast(new CapabilityInstData(properties));
				break;
			case PropertyValue:
				element = clazz.cast(new PropertyValueData(properties));
				break;
			case AttributeValue:
				element = clazz.cast(new AttributeValueData(properties));
				break;
			case InputValue:
				element = clazz.cast(new InputValueData(properties));
				break;
			case RelationshipType:
				element = clazz.cast(new RelationshipTypeData(properties));
				break;
			case LockNode:
				element = clazz.cast(new GraphNodeLock(properties));
				break;
			case ArtifactRef:
				element = clazz.cast(new ArtifactData(properties));
				break;
			case Interface:
				element = clazz.cast(new InterfaceData(properties));
				break;
			case InterfaceOperation:
				element = clazz.cast(new OperationData(properties));
				break;
			case Input:
				element = clazz.cast(new InputsData(properties));
				break;
			case ResourceInstance:
				element = clazz.cast(new ComponentInstanceData(properties));
				break;
			case RelationshipInst:
				element = clazz.cast(new RelationshipInstData(properties));
				break;
			case AdditionalInfoParameters:
				element = clazz.cast(new AdditionalInfoParameterData(properties));
				break;
			case ConsumerCredentials:
				element = clazz.cast(new ConsumerData(properties));
				break;
			case HeatParameter:
				element = clazz.cast(new HeatParameterData(properties));
				break;
			case HeatParameterValue:
				element = clazz.cast(new HeatParameterValueData(properties));
				break;
			case DataType:
				element = clazz.cast(new DataTypeData(properties));
				break;
			case Group:
				element = clazz.cast(new GroupData(properties));
				break;
			case GroupType:
				element = clazz.cast(new GroupTypeData(properties));
				break;
			case UserFunctionalMenu:
				element = clazz.cast(new UserFunctionalMenuData(properties));
				break;
			case PolicyType:
				element = clazz.cast(new PolicyTypeData(properties));
				break;
			case GroupInstance:
				element = clazz.cast(new GroupInstanceData(properties));
				break;
			case AnnotationType:
				element = clazz.cast(new AnnotationTypeData(properties));
			default:
				break;
			}

		}
		return element;
	}

}
