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

package org.openecomp.sdc.ci.tests.tosca.model;

public class ToscaMetadataFieldsPresentationEnum {

	public enum ToscaMetadataFieldsEnum {
//		general
		INVARIANT_UUID                         ("invariantUUID",                    ComponentTypeEnum.RESOURCE_SERVICE_NODE_TEMPLATE.value), 
		UUID                                   ("UUID",                             ComponentTypeEnum.RESOURCE_SERVICE_NODE_TEMPLATE.value), 
		NAME                                   ("name",                             ComponentTypeEnum.RESOURCE_SERVICE_NODE_TEMPLATE.value),
		DESCRIPTION                            ("description",                      ComponentTypeEnum.RESOURCE_SERVICE_NODE_TEMPLATE.value), 
		CATEGORY                               ("category",                         ComponentTypeEnum.RESOURCE_SERVICE_NODE_TEMPLATE.value),
		TYPE                                   ("type",                             ComponentTypeEnum.RESOURCE_SERVICE_NODE_TEMPLATE.value),
	
//		resource
		SUBCATEGORY                            ("subcategory",                      ComponentTypeEnum.RESOURCE_NODE_TEMPLATE.value), 
		RESOURCE_VENDOR_NAME                   ("resourceVendor",                   ComponentTypeEnum.RESOURCE_NODE_TEMPLATE.value), 
		RESOURCE_VENDOR_RELEASE                ("resourceVendorRelease",            ComponentTypeEnum.RESOURCE_NODE_TEMPLATE.value),
		RESOURCE_VENDOR_MODEL_NUMBER           ("resourceVendorModelNumber",        ComponentTypeEnum.RESOURCE_NODE_TEMPLATE.value),
		
//		service
		SERVICE_TYPE                           ("serviceType",                      ComponentTypeEnum.SERVICE.value), 
		SERVICE_ROLE                           ("serviceRole",                      ComponentTypeEnum.SERVICE.value), 
		SERVICE_ECOMP_NAMING                   ("serviceEcompNaming",               ComponentTypeEnum.SERVICE.value),
		ECOMP_GENERATED_NAMING                 ("ecompGeneratedNaming",             ComponentTypeEnum.SERVICE.value),
		NAMING_POLICY                          ("namingPolicy",                     ComponentTypeEnum.SERVICE.value),
		INSTANTIATION_TYPE                     ("instantiationType",                ComponentTypeEnum.SERVICE.value),
		
//		node_template
		CUSTOMIZATION_UUID                     ("customizationUUID",                ComponentTypeEnum.NODE_TEMPLATE.value), 
		VERSION                                ("version",                          ComponentTypeEnum.RESOURCE_GROUP_NODE_TEMPLATE.value), 
		
//		service group:
		VF_MODULE_MODEL_NAME                   ("vfModuleModelName",                ComponentTypeEnum.RESOURCE_GROUP_SERVICE_GROUP.value), 
		VF_MODULE_MODEL_INVARIANT_UUID         ("vfModuleModelInvariantUUID",       ComponentTypeEnum.RESOURCE_GROUP_SERVICE_GROUP.value), 
		VF_MODULE_MODEL_UUID                   ("vfModuleModelUUID",                ComponentTypeEnum.RESOURCE_GROUP_SERVICE_GROUP.value),
		VF_MODULE_MODEL_VERSION                ("vfModuleModelVersion",             ComponentTypeEnum.RESOURCE_GROUP_SERVICE_GROUP.value),
		
		VF_MODULE_MODEL_CUSTOMIZATION_UUID     ("vfModuleModelCustomizationUUID",   ComponentTypeEnum.SERVICE_GROUP.value)
		;
		
		
		
		public String value;
		public String componentTypes;
	
		private ToscaMetadataFieldsEnum(String value, String componentTypes) {
			this.value = value;
			this.componentTypes = componentTypes;
		}
		
	}
	

	public enum ComponentTypeEnum {
//		RESOURCE_SERVICE_NODE_TEMPLATE_RESOURCE_GROUP("resource, service, nodeTemplate, resourceGroup"), 
		RESOURCE_SERVICE_NODE_TEMPLATE("resource, service, nodeTemplate"),
		RESOURCE_NODE_TEMPLATE("resource, nodeTemplate"),
		SERVICE("service"),
		NODE_TEMPLATE("nodeTemplate"),
		RESOURCE_GROUP_NODE_TEMPLATE("resourceGroup, nodeTemplate"),
		SERVICE_GROUP("serviceGroup"),
		RESOURCE_GROUP_SERVICE_GROUP("resourceGroup, serviceGroup"),
		
	;
	
		private String value;

		private ComponentTypeEnum(String value) {
			this.value = value;
		}
		
	}
	
	
}
