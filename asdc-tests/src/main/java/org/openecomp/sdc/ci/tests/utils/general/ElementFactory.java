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

package org.openecomp.sdc.ci.tests.utils.general;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.RelationshipImpl;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ImportReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.PropertyReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceExternalReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.PropertyTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedExternalAudit;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.util.ValidationUtils;

public class ElementFactory {

	private static final String JH0003 = "jh0003";
	private static final String CI_RES = "ciRes";
	private static String DEFAULT_ARTIFACT_LABEL = "artifact1";
	private static final String RESOURCE_INSTANCE_POS_X = "20";
	private static final String RESOURCE_INSTANCE_POS_Y = "20";
	private static final String RESOURCE_INSTANCE_DESCRIPTION = "description";

	// *** RESOURCE ***

	public static ResourceReqDetails getDefaultResource() {
		return getDefaultResource(CI_RES, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, JH0003);
	}

	public static ResourceReqDetails getDefaultResource(ResourceCategoryEnum category) {
		return getDefaultResource(CI_RES, NormativeTypesEnum.ROOT, category, JH0003);
	}

	public static ResourceReqDetails getDefaultResource(String contactId) {
		return getDefaultResource(CI_RES, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, contactId);
	}

	public static ResourceReqDetails getDefaultResource(User modifier) {
		return getDefaultResource(CI_RES, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, modifier.getUserId());
	}

	public static ResourceReqDetails getDefaultResource(NormativeTypesEnum derivedFrom, ResourceCategoryEnum category) {
		return getDefaultResource(CI_RES, derivedFrom, category, JH0003);
	}

	public static ResourceReqDetails getDefaultResource(NormativeTypesEnum derivedFrom) {
		return getDefaultResource(CI_RES, derivedFrom, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, JH0003);
	}

	public static ResourceReqDetails getDefaultResource(String resourceName, NormativeTypesEnum derivedFrom) {
		return getDefaultResource(resourceName, derivedFrom, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, JH0003);
	}

	public static ResourceReqDetails getDefaultResource(NormativeTypesEnum derivedFrom, String contactId) {
		return getDefaultResource(CI_RES, derivedFrom, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, contactId);
	}

	// New
	public static ResourceReqDetails getDefaultResourceByType(ResourceTypeEnum ResourceType, String resourceName) {
		return getDefaultResourceByType(resourceName, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, JH0003, ResourceType.toString());
	}

	public static ResourceReqDetails getDefaultResourceByType(ResourceTypeEnum ResourceType, User user) {
		return getDefaultResourceByType(CI_RES, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, user.getUserId(), ResourceType.toString());
	}

	public static ResourceReqDetails getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum resourceType, NormativeTypesEnum normativeTypes, ResourceCategoryEnum resourceCategory, User user) {
		return getDefaultResourceByType(CI_RES, normativeTypes, resourceCategory, user.getUserId(), resourceType.toString());
	}

	public static PropertyReqDetails getDefaultMapProperty(PropertyTypeEnum innerType) {
		return getPropertyDetails(innerType);
	}

	public static PropertyReqDetails getDefaultMapProperty() {
		return getPropertyDetails(PropertyTypeEnum.STRING_MAP);
	}

	public static ResourceReqDetails getDefaultResource(String resourceName, NormativeTypesEnum derived, ResourceCategoryEnum category, String contactId) {
		resourceName = (resourceName + generateUUIDforSufix());
		String description = "Represents a generic software component that can be managed and run by a Compute Node Type.";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);

		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(derived.normativeName);
		String vendorName = "ATT Tosca";
		String vendorRelease = "1.0.0.wd03";
		String icon = "defaulticon";
		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, null, derivedFrom, vendorName, vendorRelease, contactId, icon);
		resourceDetails.addCategoryChain(category.getCategory(), category.getSubCategory());

		return resourceDetails;

	}

	public static ResourceReqDetails getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum resourceType, Resource normativeTypes, ResourceCategoryEnum resourceCategory, User user) {
		return getDefaultResource(CI_RES + resourceType, normativeTypes, resourceCategory, user.getUserId());
	}

	public static ResourceReqDetails getDefaultResource(String resourceName, Resource derived, ResourceCategoryEnum category, String contactId) {
		resourceName = (resourceName + generateUUIDforSufix());
		String description = "Represents a generic software component that can be managed and run by a Compute Node Type.";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);

		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(derived.getToscaResourceName());
		String vendorName = "ATT Tosca";
		String vendorRelease = "1.0.0.wd03";
		String icon = "defaulticon";
		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, null, derivedFrom, vendorName, vendorRelease, contactId, icon);
		resourceDetails.addCategoryChain(category.getCategory(), category.getSubCategory());

		return resourceDetails;

	}

	public static ResourceReqDetails getDefaultResourceByType(String resourceName, NormativeTypesEnum derived, ResourceCategoryEnum category, String contactId, ResourceTypeEnum resourceType) {
		return getDefaultResourceByType(resourceName, derived, category, contactId, resourceType.toString());
	}

	// New
	public static ResourceReqDetails getDefaultResourceByType(String resourceName, NormativeTypesEnum derived, ResourceCategoryEnum category, String contactId, String resourceType) {
		resourceName = (resourceName + resourceType + generateUUIDforSufix());
		String description = "Represents a generic software component that can be managed and run by a Compute Node Type.";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		ArrayList<String> derivedFrom = null;
		if (derived != null) {
			derivedFrom = new ArrayList<String>();
			derivedFrom.add(derived.normativeName);
		}
		String vendorName = "ATT Tosca";
		String vendorRelease = "1.0.0.wd03";
		String icon = "defaulticon";
		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, null, derivedFrom, vendorName, vendorRelease, contactId, icon, resourceType.toString());
		resourceDetails.addCategoryChain(category.getCategory(), category.getSubCategory());
		return resourceDetails;
	}
	
	public static ResourceExternalReqDetails getDefaultResourceByType(String resourceName, ResourceCategoryEnum category, String contactId, String resourceType) {
		resourceName = (resourceName + resourceType + generateUUIDforSufix());
		String description = "Represents a generic software component that can be managed and run by a Compute Node Type.";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String vendorName = "ATT Tosca";
		String vendorRelease = "1.0.0.wd03";
		String icon = "defaulticon";
		ResourceExternalReqDetails resourceDetails = new ResourceExternalReqDetails(resourceName, description, resourceTags, 
				vendorName, vendorRelease, contactId, icon,
				resourceType.toString(), category.getCategory(), category.getSubCategory());
		return resourceDetails;
	}

	// New
	public static ImportReqDetails getDefaultImportResourceByType(String resourceName, NormativeTypesEnum derived, ResourceCategoryEnum category, String contactId, String resourceType) {
		resourceName = (resourceName + resourceType + generateUUIDforSufix());
		String description = "Represents a generic software component that can be managed and run by a Compute Node Type.";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		ArrayList<String> derivedFrom = null;
		if (derived != null) {
			derivedFrom = new ArrayList<String>();
			derivedFrom.add(derived.normativeName);
		}
		String vendorName = "SDC Tosca";
		String vendorRelease = "1.0.0.wd03";
		String icon = "defaulticon";
		ImportReqDetails resourceDetails = new ImportReqDetails(resourceName, description, resourceTags, derivedFrom, vendorName, vendorRelease, contactId, icon);
		resourceDetails.addCategoryChain(category.getCategory(), category.getSubCategory());
		return resourceDetails;
	}
	////

	public static ImportReqDetails getDefaultImportResource(ResourceReqDetails resourceReqDetails) {
		ImportReqDetails importReqDetails = new ImportReqDetails(resourceReqDetails.getName(), resourceReqDetails.getDescription(), resourceReqDetails.getTags(), resourceReqDetails.getDerivedFrom(), resourceReqDetails.getVendorName(),
				resourceReqDetails.getVendorRelease(), resourceReqDetails.getContactId(), resourceReqDetails.getIcon());
		importReqDetails.setPayloadName("ciMyCompute.yaml");
		importReqDetails.setCategories(resourceReqDetails.getCategories());
		importReqDetails.setPayloadData(
				"dG9zY2FfZGVmaW5pdGlvbnNfdmVyc2lvbjogdG9zY2Ffc2ltcGxlX3lhbWxfMV8wXzANCm5vZGVfdHlwZXM6IA0KICBvcmcub3BlbmVjb21wLnJlc291cmNlLk15Q29tcHV0ZToNCiAgICBkZXJpdmVkX2Zyb206IHRvc2NhLm5vZGVzLlJvb3QNCiAgICBhdHRyaWJ1dGVzOg0KICAgICAgcHJpdmF0ZV9hZGRyZXNzOg0KICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgIHB1YmxpY19hZGRyZXNzOg0KICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgIG5ldHdvcmtzOg0KICAgICAgICB0eXBlOiBtYXANCiAgICAgICAgZW50cnlfc2NoZW1hOg0KICAgICAgICAgIHR5cGU6IHRvc2NhLmRhdGF0eXBlcy5uZXR3b3JrLk5ldHdvcmtJbmZvDQogICAgICBwb3J0czoNCiAgICAgICAgdHlwZTogbWFwDQogICAgICAgIGVudHJ5X3NjaGVtYToNCiAgICAgICAgICB0eXBlOiB0b3NjYS5kYXRhdHlwZXMubmV0d29yay5Qb3J0SW5mbw0KICAgIHJlcXVpcmVtZW50czoNCiAgICAgIC0gbG9jYWxfc3RvcmFnZTogDQogICAgICAgICAgY2FwYWJpbGl0eTogdG9zY2EuY2FwYWJpbGl0aWVzLkF0dGFjaG1lbnQNCiAgICAgICAgICBub2RlOiB0b3NjYS5ub2Rlcy5CbG9ja1N0b3JhZ2UNCiAgICAgICAgICByZWxhdGlvbnNoaXA6IHRvc2NhLnJlbGF0aW9uc2hpcHMuQXR0YWNoZXNUbw0KICAgICAgICAgIG9jY3VycmVuY2VzOiBbMCwgVU5CT1VOREVEXSAgDQogICAgY2FwYWJpbGl0aWVzOg0KICAgICAgaG9zdDogDQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5Db250YWluZXINCiAgICAgICAgdmFsaWRfc291cmNlX3R5cGVzOiBbdG9zY2Eubm9kZXMuU29mdHdhcmVDb21wb25lbnRdIA0KICAgICAgZW5kcG9pbnQgOg0KICAgICAgICB0eXBlOiB0b3NjYS5jYXBhYmlsaXRpZXMuRW5kcG9pbnQuQWRtaW4gDQogICAgICBvczogDQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5PcGVyYXRpbmdTeXN0ZW0NCiAgICAgIHNjYWxhYmxlOg0KICAgICAgICB0eXBlOiB0b3NjYS5jYXBhYmlsaXRpZXMuU2NhbGFibGUNCiAgICAgIGJpbmRpbmc6DQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5uZXR3b3JrLkJpbmRhYmxlDQo=");
				return importReqDetails;
	}

	public static ImportReqDetails getDefaultImportResource() {
		ResourceReqDetails resourceReqDetails = getDefaultResource();
		ImportReqDetails importReqDetails = new ImportReqDetails(resourceReqDetails.getName(), resourceReqDetails.getDescription(), resourceReqDetails.getTags(), resourceReqDetails.getDerivedFrom(), resourceReqDetails.getVendorName(),
				resourceReqDetails.getVendorRelease(), resourceReqDetails.getContactId(), resourceReqDetails.getIcon());
		importReqDetails.setPayloadName("ciMyCompute.yaml");
		importReqDetails.setCategories(resourceReqDetails.getCategories());
		importReqDetails.setPayloadData(
				"dG9zY2FfZGVmaW5pdGlvbnNfdmVyc2lvbjogdG9zY2Ffc2ltcGxlX3lhbWxfMV8wXzANCm5vZGVfdHlwZXM6IA0KICBvcmcub3BlbmVjb21wLnJlc291cmNlLk15Q29tcHV0ZToNCiAgICBkZXJpdmVkX2Zyb206IHRvc2NhLm5vZGVzLlJvb3QNCiAgICBhdHRyaWJ1dGVzOg0KICAgICAgcHJpdmF0ZV9hZGRyZXNzOg0KICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgIHB1YmxpY19hZGRyZXNzOg0KICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgIG5ldHdvcmtzOg0KICAgICAgICB0eXBlOiBtYXANCiAgICAgICAgZW50cnlfc2NoZW1hOg0KICAgICAgICAgIHR5cGU6IHRvc2NhLmRhdGF0eXBlcy5uZXR3b3JrLk5ldHdvcmtJbmZvDQogICAgICBwb3J0czoNCiAgICAgICAgdHlwZTogbWFwDQogICAgICAgIGVudHJ5X3NjaGVtYToNCiAgICAgICAgICB0eXBlOiB0b3NjYS5kYXRhdHlwZXMubmV0d29yay5Qb3J0SW5mbw0KICAgIHJlcXVpcmVtZW50czoNCiAgICAgIC0gbG9jYWxfc3RvcmFnZTogDQogICAgICAgICAgY2FwYWJpbGl0eTogdG9zY2EuY2FwYWJpbGl0aWVzLkF0dGFjaG1lbnQNCiAgICAgICAgICBub2RlOiB0b3NjYS5ub2Rlcy5CbG9ja1N0b3JhZ2UNCiAgICAgICAgICByZWxhdGlvbnNoaXA6IHRvc2NhLnJlbGF0aW9uc2hpcHMuQXR0YWNoZXNUbw0KICAgICAgICAgIG9jY3VycmVuY2VzOiBbMCwgVU5CT1VOREVEXSAgDQogICAgY2FwYWJpbGl0aWVzOg0KICAgICAgaG9zdDogDQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5Db250YWluZXINCiAgICAgICAgdmFsaWRfc291cmNlX3R5cGVzOiBbdG9zY2Eubm9kZXMuU29mdHdhcmVDb21wb25lbnRdIA0KICAgICAgZW5kcG9pbnQgOg0KICAgICAgICB0eXBlOiB0b3NjYS5jYXBhYmlsaXRpZXMuRW5kcG9pbnQuQWRtaW4gDQogICAgICBvczogDQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5PcGVyYXRpbmdTeXN0ZW0NCiAgICAgIHNjYWxhYmxlOg0KICAgICAgICB0eXBlOiB0b3NjYS5jYXBhYmlsaXRpZXMuU2NhbGFibGUNCiAgICAgIGJpbmRpbmc6DQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5uZXR3b3JrLkJpbmRhYmxlDQo=");
		return importReqDetails;
	}

	public static ImportReqDetails getDefaultImportResource(String name) {
		ResourceReqDetails resourceReqDetails = getDefaultResourceByType(ResourceTypeEnum.VFC, name);
		ImportReqDetails importReqDetails = new ImportReqDetails(resourceReqDetails.getName(), resourceReqDetails.getDescription(), resourceReqDetails.getTags(), resourceReqDetails.getDerivedFrom(), resourceReqDetails.getVendorName(),
				resourceReqDetails.getVendorRelease(), resourceReqDetails.getContactId(), resourceReqDetails.getIcon());
		importReqDetails.setPayloadName("ciMyCompute.yaml");
		importReqDetails.setCategories(resourceReqDetails.getCategories());
		importReqDetails.setPayloadData(
				"dG9zY2FfZGVmaW5pdGlvbnNfdmVyc2lvbjogdG9zY2Ffc2ltcGxlX3lhbWxfMV8wXzANCm5vZGVfdHlwZXM6IA0KICBvcmcub3BlbmVjb21wLnJlc291cmNlLk15Q29tcHV0ZToNCiAgICBkZXJpdmVkX2Zyb206IHRvc2NhLm5vZGVzLlJvb3QNCiAgICBhdHRyaWJ1dGVzOg0KICAgICAgcHJpdmF0ZV9hZGRyZXNzOg0KICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgIHB1YmxpY19hZGRyZXNzOg0KICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgIG5ldHdvcmtzOg0KICAgICAgICB0eXBlOiBtYXANCiAgICAgICAgZW50cnlfc2NoZW1hOg0KICAgICAgICAgIHR5cGU6IHRvc2NhLmRhdGF0eXBlcy5uZXR3b3JrLk5ldHdvcmtJbmZvDQogICAgICBwb3J0czoNCiAgICAgICAgdHlwZTogbWFwDQogICAgICAgIGVudHJ5X3NjaGVtYToNCiAgICAgICAgICB0eXBlOiB0b3NjYS5kYXRhdHlwZXMubmV0d29yay5Qb3J0SW5mbw0KICAgIHJlcXVpcmVtZW50czoNCiAgICAgIC0gbG9jYWxfc3RvcmFnZTogDQogICAgICAgICAgY2FwYWJpbGl0eTogdG9zY2EuY2FwYWJpbGl0aWVzLkF0dGFjaG1lbnQNCiAgICAgICAgICBub2RlOiB0b3NjYS5ub2Rlcy5CbG9ja1N0b3JhZ2UNCiAgICAgICAgICByZWxhdGlvbnNoaXA6IHRvc2NhLnJlbGF0aW9uc2hpcHMuQXR0YWNoZXNUbw0KICAgICAgICAgIG9jY3VycmVuY2VzOiBbMCwgVU5CT1VOREVEXSAgDQogICAgY2FwYWJpbGl0aWVzOg0KICAgICAgaG9zdDogDQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5Db250YWluZXINCiAgICAgICAgdmFsaWRfc291cmNlX3R5cGVzOiBbdG9zY2Eubm9kZXMuU29mdHdhcmVDb21wb25lbnRdIA0KICAgICAgZW5kcG9pbnQgOg0KICAgICAgICB0eXBlOiB0b3NjYS5jYXBhYmlsaXRpZXMuRW5kcG9pbnQuQWRtaW4gDQogICAgICBvczogDQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5PcGVyYXRpbmdTeXN0ZW0NCiAgICAgIHNjYWxhYmxlOg0KICAgICAgICB0eXBlOiB0b3NjYS5jYXBhYmlsaXRpZXMuU2NhbGFibGUNCiAgICAgIGJpbmRpbmc6DQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5uZXR3b3JrLkJpbmRhYmxlDQo=");
		return importReqDetails;
	}

	// *** SERVICE ***
	public static ServiceReqDetails getDefaultService() {
		return getDefaultService("ciService", ServiceCategoriesEnum.MOBILITY, "al1976");
	}

	public static ServiceReqDetails getDefaultService(String contactId) {
		return getDefaultService("ciService", ServiceCategoriesEnum.MOBILITY, contactId);
	}

	public static ServiceReqDetails getDefaultService(User user) {
		return getDefaultService("ciService", ServiceCategoriesEnum.MOBILITY, user.getUserId());
	}

	public static ServiceReqDetails getService(ServiceCategoriesEnum category) {
		return getDefaultService("ciService", category, "al1976");
	}

	public static ServiceReqDetails getDefaultService(ServiceCategoriesEnum category, User user) {
		return getDefaultService("ciService", category, user.getUserId());
	}

	public static ServiceReqDetails getDefaultService(String serviceName, ServiceCategoriesEnum category, String contactId) {
		serviceName = (serviceName + generateUUIDforSufix());
		ArrayList<String> tags = new ArrayList<String>();
		tags.add("serviceTag");
		tags.add("serviceTag1");
		tags.add(serviceName);
		String description = "service Description";
		String icon = "myIcon";

		ServiceReqDetails serviceDetails = new ServiceReqDetails(serviceName, category.getValue(), tags, description, contactId, icon);

		return serviceDetails;
	}

	// ***** PROPERTY ***

	public static PropertyReqDetails getDefaultProperty() {
		return getDefaultProperty("disk_size");
	}

	public static PropertyReqDetails getDefaultProperty(String propertyName) {
		PropertyReqDetails property = new PropertyReqDetails();
		property.setName(propertyName);
		property.setPropertyType("integer");
		property.setPropertyRequired(false);
		property.setPropertyDefaultValue("12345");
		property.setPropertyDescription("test property");
		property.setPropertyRangeMax("500");
		property.setPropertyRangeMin("100");
		property.setPropertyPassword(false);
		return property;
	}

	public static PropertyReqDetails getDefaultIntegerProperty() {
		return getPropertyDetails(PropertyTypeEnum.INTEGER);
	}

	public static PropertyReqDetails getDefaultStringProperty() {
		return getPropertyDetails(PropertyTypeEnum.STRING);
	}

	public static PropertyReqDetails getDefaultBooleanProperty() {
		return getPropertyDetails(PropertyTypeEnum.BOOLEAN);
	}

	public static PropertyReqDetails getDefaultListProperty() {
		return getPropertyDetails(PropertyTypeEnum.STRING_LIST);
	}

	public static PropertyReqDetails getDefaultListProperty(PropertyTypeEnum innerType) {
		return getPropertyDetails(innerType);
	}

	public static PropertyReqDetails getPropertyDetails(PropertyTypeEnum propType) {
		return new PropertyReqDetails(propType.getName(), propType.getType(), propType.getValue(), propType.getDescription(), propType.getSchemaDefinition());
	}

	// ***** RESOURCE INSTANCE ***
	public static ComponentInstanceReqDetails getDefaultComponentInstance() {
		return getDefaultComponentInstance("resourceInstanceName");
	}

	public static ComponentInstanceReqDetails getDefaultComponentInstance(String name) {
		String resourceUid = "resourceId";
		ComponentInstanceReqDetails resourceInstanceDetails = new ComponentInstanceReqDetails(resourceUid, RESOURCE_INSTANCE_DESCRIPTION, RESOURCE_INSTANCE_POS_X, RESOURCE_INSTANCE_POS_Y, name);

		return resourceInstanceDetails;

	}

	public static ComponentInstanceReqDetails getDefaultComponentInstance(String name, ComponentReqDetails componentReqDetails) {
		String resourceUid = componentReqDetails.getUniqueId();
		ComponentInstanceReqDetails resourceInstanceDetails = new ComponentInstanceReqDetails(resourceUid, RESOURCE_INSTANCE_DESCRIPTION, RESOURCE_INSTANCE_POS_X, RESOURCE_INSTANCE_POS_Y, name);

		return resourceInstanceDetails;

	}

	public static ComponentInstanceReqDetails getComponentResourceInstance(ComponentReqDetails compInstOriginDetails) {
		String compInstName = (compInstOriginDetails.getName() != null ? compInstOriginDetails.getName() : "resourceInstanceName");
		String resourceUid = compInstOriginDetails.getUniqueId();
		ComponentInstanceReqDetails resourceInstanceDetails = new ComponentInstanceReqDetails(resourceUid, RESOURCE_INSTANCE_DESCRIPTION, RESOURCE_INSTANCE_POS_X, RESOURCE_INSTANCE_POS_Y, compInstName);
		return resourceInstanceDetails;

	}

	public static ComponentInstanceReqDetails getComponentInstance(Component compInstOriginDetails) {
		String compInstName = (compInstOriginDetails.getName() != null ? compInstOriginDetails.getName() : "componentInstanceName");
		String compInsUid = compInstOriginDetails.getUniqueId();
		ComponentInstanceReqDetails componentInstanceDetails = new ComponentInstanceReqDetails(compInsUid, RESOURCE_INSTANCE_DESCRIPTION, RESOURCE_INSTANCE_POS_X, RESOURCE_INSTANCE_POS_Y, compInstName);
		return componentInstanceDetails;

	}

	// ******* USER **********************
	public static User getDefaultUser(UserRoleEnum userRole) {
		User sdncModifierDetails = new User();
		sdncModifierDetails.setUserId(userRole.getUserId());
		sdncModifierDetails.setFirstName(userRole.getFirstName());
		sdncModifierDetails.setLastName(userRole.getLastName());
		return sdncModifierDetails;
	}

	public static User getDefaultMechUser() {
		User sdncMechUserDetails = new User();
		sdncMechUserDetails.setUserId("m12345");
		sdncMechUserDetails.setFirstName("Shay");
		sdncMechUserDetails.setLastName("Sponder");
		sdncMechUserDetails.setEmail("mechId@intl.sdc.com");
		sdncMechUserDetails.setRole("DESIGNER");
		return sdncMechUserDetails;
	}

	// ******* CONSUMER **********************

	public static ConsumerDataDefinition getDefaultConsumerDetails() {
		ConsumerDataDefinition consumer = new ConsumerDataDefinition();
		consumer.setConsumerName("ci");
		consumer.setConsumerSalt("2a1f887d607d4515d4066fe0f5452a50");
		consumer.setConsumerPassword("0a0dc557c3bf594b1a48030e3e99227580168b21f44e285c69740b8d5b13e33b");
		return consumer;
	}

	// *** ARTIFACT ***
	public static ArtifactReqDetails getDefaultArtifact() throws IOException, Exception {
		return getDefaultArtifact(DEFAULT_ARTIFACT_LABEL);
	}

	public static ArtifactReqDetails getDefaultArtifact(String artifactLabel) throws IOException, Exception {
		List<String> artifactTypes = ResponseParser.getValuesFromJsonArray(ArtifactRestUtils.getArtifactTypesList());
		String artifactType = artifactTypes.get(0);

		return getDefaultArtifact(artifactLabel, artifactType);
	}

	public static ArtifactReqDetails getDefaultArtifact(String artifactLabel, String artifactType) throws IOException, Exception {

		String artifactName = "testArtifact.sh";
		String artifactDescription = "descriptionTest";
		String payloadData = "dGVzdA=="; // content of file

		ArtifactReqDetails artifactDetails = new ArtifactReqDetails(artifactName, artifactType, artifactDescription, payloadData, artifactLabel);
		artifactDetails.setUrl("");
		artifactDetails.setArtifactDisplayName(artifactLabel);
		return artifactDetails;
	}

	public static ArtifactReqDetails getServiceApiArtifactDetails(String artifactLabel) throws IOException, Exception {
		ArtifactReqDetails defaultArtifact = getDefaultArtifact(artifactLabel, "OTHER");
		defaultArtifact.setUrl("http://www.apple.com");
		defaultArtifact.setServiceApi(true);
		defaultArtifact.setArtifactDisplayName(StringUtils.capitalize(defaultArtifact.getArtifactLabel()));
		return defaultArtifact;
	}

	public static ArtifactReqDetails getDefaultDeploymentArtifactForType(String artifactType) throws IOException, Exception {
		return getArtifactByType(DEFAULT_ARTIFACT_LABEL, artifactType, true, false);
	}

	public static ArtifactReqDetails getArtifactByType(ArtifactTypeEnum artifactLabel, ArtifactTypeEnum artifactType, Boolean deploymentTrue) throws IOException, Exception {
		return getArtifactByType(DEFAULT_ARTIFACT_LABEL, artifactType.toString(), deploymentTrue, false);

	}

	public static ArtifactReqDetails getArtifactByType(String artifactLabel, String artifactType, Boolean deploymentTrue, Boolean updatedPayload) throws IOException, Exception {
		String artifactName;
		String updatedPayloadData =null;
		String payloadData = null;
		Integer timeout = null;
		String url = "";
		String artifactDescription = "descriptionTest";

		// PLEASE NOTE!!!
		// The non-default payloads here are real ones according to various
		// types validations,
		// Please don't change them unless you know what you are doing!

		ArtifactTypeEnum artifactTypeEnum = ArtifactTypeEnum.findType(artifactType);

		/*
		 * Missing file type: DCAE_JSON
		 */
		switch (artifactTypeEnum) {
		case DCAE_INVENTORY_TOSCA:
		case DCAE_EVENT:
		case APPC_CONFIG:
		case DCAE_DOC:
		case DCAE_TOSCA:
		case HEAT:
		case HEAT_NET:
		case HEAT_VOL: {
			artifactName = generateUUIDforSufix() + artifactType + "_install_apache2.yaml";
			payloadData = "aGVhdF90ZW1wbGF0ZV92ZXJzaW9uOiAyMDEzLTA1LTIzDQoNCmRlc2NyaXB0aW9uOiBTaW1wbGUgdGVtcGxhdGUgdG8gZGVwbG95IGEgc3RhY2sgd2l0aCB0d28gdmlydHVhbCBtYWNoaW5lIGluc3RhbmNlcw0KDQpwYXJhbWV0ZXJzOg0KICBpbWFnZV9uYW1lXzE6DQogICAgdHlwZTogc3RyaW5nDQogICAgbGFiZWw6IEltYWdlIE5hbWUNCiAgICBkZXNjcmlwdGlvbjogU0NPSU1BR0UgU3BlY2lmeSBhbiBpbWFnZSBuYW1lIGZvciBpbnN0YW5jZTENCiAgICBkZWZhdWx0OiBjaXJyb3MtMC4zLjEteDg2XzY0DQogIGltYWdlX25hbWVfMjoNCiAgICB0eXBlOiBzdHJpbmcNCiAgICBsYWJlbDogSW1hZ2UgTmFtZQ0KICAgIGRlc2NyaXB0aW9uOiBTQ09JTUFHRSBTcGVjaWZ5IGFuIGltYWdlIG5hbWUgZm9yIGluc3RhbmNlMg0KICAgIGRlZmF1bHQ6IGNpcnJvcy0wLjMuMS14ODZfNjQNCiAgbmV0d29ya19pZDoNCiAgICB0eXBlOiBzdHJpbmcNCiAgICBsYWJlbDogTmV0d29yayBJRA0KICAgIGRlc2NyaXB0aW9uOiBTQ09ORVRXT1JLIE5ldHdvcmsgdG8gYmUgdXNlZCBmb3IgdGhlIGNvbXB1dGUgaW5zdGFuY2UNCiAgICBoaWRkZW46IHRydWUNCiAgICBjb25zdHJhaW50czoNCiAgICAgIC0gbGVuZ3RoOiB7IG1pbjogNiwgbWF4OiA4IH0NCiAgICAgICAgZGVzY3JpcHRpb246IFBhc3N3b3JkIGxlbmd0aCBtdXN0IGJlIGJldHdlZW4gNiBhbmQgOCBjaGFyYWN0ZXJzLg0KICAgICAgLSByYW5nZTogeyBtaW46IDYsIG1heDogOCB9DQogICAgICAgIGRlc2NyaXB0aW9uOiBSYW5nZSBkZXNjcmlwdGlvbg0KICAgICAgLSBhbGxvd2VkX3ZhbHVlczoNCiAgICAgICAgLSBtMS5zbWFsbA0KICAgICAgICAtIG0xLm1lZGl1bQ0KICAgICAgICAtIG0xLmxhcmdlDQogICAgICAgIGRlc2NyaXB0aW9uOiBBbGxvd2VkIHZhbHVlcyBkZXNjcmlwdGlvbg0KICAgICAgLSBhbGxvd2VkX3BhdHRlcm46ICJbYS16QS1aMC05XSsiDQogICAgICAgIGRlc2NyaXB0aW9uOiBQYXNzd29yZCBtdXN0IGNvbnNpc3Qgb2YgY2hhcmFjdGVycyBhbmQgbnVtYmVycyBvbmx5Lg0KICAgICAgLSBhbGxvd2VkX3BhdHRlcm46ICJbQS1aXStbYS16QS1aMC05XSoiDQogICAgICAgIGRlc2NyaXB0aW9uOiBQYXNzd29yZCBtdXN0IHN0YXJ0IHdpdGggYW4gdXBwZXJjYXNlIGNoYXJhY3Rlci4NCiAgICAgIC0gY3VzdG9tX2NvbnN0cmFpbnQ6IG5vdmEua2V5cGFpcg0KICAgICAgICBkZXNjcmlwdGlvbjogQ3VzdG9tIGRlc2NyaXB0aW9uDQoNCnJlc291cmNlczoNCiAgbXlfaW5zdGFuY2UxOg0KICAgIHR5cGU6IE9TOjpOb3ZhOjpTZXJ2ZXINCiAgICBwcm9wZXJ0aWVzOg0KICAgICAgaW1hZ2U6IHsgZ2V0X3BhcmFtOiBpbWFnZV9uYW1lXzEgfQ0KICAgICAgZmxhdm9yOiBtMS5zbWFsbA0KICAgICAgbmV0d29ya3M6DQogICAgICAgIC0gbmV0d29yayA6IHsgZ2V0X3BhcmFtIDogbmV0d29ya19pZCB9DQogIG15X2luc3RhbmNlMjoNCiAgICB0eXBlOiBPUzo6Tm92YTo6U2VydmVyDQogICAgcHJvcGVydGllczoNCiAgICAgIGltYWdlOiB7IGdldF9wYXJhbTogaW1hZ2VfbmFtZV8yIH0NCiAgICAgIGZsYXZvcjogbTEudGlueQ0KICAgICAgbmV0d29ya3M6DQogICAgICAgIC0gbmV0d29yayA6IHsgZ2V0X3BhcmFtIDogbmV0d29ya19pZCB9";
			updatedPayloadData = "dG9zY2FfZGVmaW5pdGlvbnNfdmVyc2lvbjogdG9zY2Ffc2ltcGxlX3lhbWxfMV8wXzANCg0Kbm9kZV90eXBlczoNCiAgY29tLmF0dC5kMi5yZXNvdXJjZS5jcC5DUDoNCiAgICBkZXJpdmVkX2Zyb206IHRvc2NhLm5vZGVzLm5ldHdvcmsuUG9ydA0KICAgIHByb3BlcnRpZXM6DQogICAgICBpc190YWdnZWQ6DQogICAgICAgIHR5cGU6IGJvb2xlYW4NCiAgICAgICAgcmVxdWlyZWQ6IGZhbHNlDQogICAgICAgIGRlZmF1bHQ6IGZhbHNlDQogICAgICAgIGRlc2NyaXB0aW9uOiANCg0KICAgIHJlcXVpcmVtZW50czoNCiAgICAgIC0gdmlydHVhbExpbms6DQogICAgICAgICAgY2FwYWJpbGl0eTogdG9zY2EuY2FwYWJpbGl0aWVzLm5ldHdvcmsuTGlua2FibGUNCiAgICAgICAgICByZWxhdGlvbnNoaXA6IHRvc2NhLnJlbGF0aW9uc2hpcHMubmV0d29yay5MaW5rc1RvDQogICAgICAtIHZpcnR1YWxCaW5kaW5nOg0KICAgICAgICAgIGNhcGFiaWxpdHk6IHRvc2NhLmNhcGFiaWxpdGllcy5uZXR3b3JrLkJpbmRhYmxlDQogICAgICAgICAgcmVsYXRpb25zaGlwOiB0b3NjYS5yZWxhdGlvbnNoaXBzLm5ldHdvcmsuQmluZHNUbw0KICAgIGNhcGFiaWxpdGllczoNCiAgICAgIGF0dGFjaG1lbnQ6DQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5BdHRhY2htZW50DQogICAgICAgIG9jY3VycmVuY2VzOg0KICAgICAgICAtIDENCiAgICAgICAgLSBVTkJPVU5ERUQNCiAgICAgICAgdHlwZTogdG9zY2EuY2FwYWJpbGl0aWVzLm5ldHdvcmsuQmluZGFibGUNCiAgICAgICAgb2NjdXJyZW5jZXM6DQogICAgICAgIC0gMQ0KICAgICAgICAtIFVOQk9VTkRFRA0KICAgICAgdmlydHVhbF9saW5rYWJsZToNCiAgICAgICAgdHlwZTogY29tLmF0dC5kMi5jYXBhYmlsaXRpZXMuTWV0cmljDQogICAgICBlbmRfcG9pbnQ6DQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5FbmRwb2ludCAgICAgICA=";
			timeout = 60;
			artifactLabel = normalizeArtifactLabel(artifactName);
			break;
		}
		case DCAE_INVENTORY_POLICY:
		case DCAE_INVENTORY_BLUEPRINT:
		case DCAE_INVENTORY_EVENT: {
			artifactName = getDcaeArtifactName(artifactTypeEnum, artifactType);
			payloadData = "will be override later";
			updatedPayloadData = "override";
			timeout = 60;
			artifactLabel = normalizeArtifactLabel(artifactName);
			break;
		}
		case MURANO_PKG: {
			artifactName = artifactType + "org.openstack.Rally.zip";
			payloadData = "ODM4MTRjNzkxZjcwYTlkMjk4ZGQ2ODE4MThmNjg0N2Y=";
			updatedPayloadData = "ODM4MTRjNzkxZjcwYTlkMjk4ZGQ2ODE4MThmMTAwN2Y=";
			break;
		}
		case DCAE_POLICY: {
			artifactName = artifactType + "dcae_policy.emf";
			payloadData = "will be override later";
			updatedPayloadData = "override";
			break;
		}
		case DCAE_INVENTORY_JSON:
		case DCAE_JSON: {
			artifactName = artifactType + "dcae_policy.json";
			payloadData = "ew0KICAiYXJ0aWZhY3RzIjogImRmc2FmIiwNCiAgIm5vcm1hbGl6ZWROYW1lIjogImNpc2VydmljZTBiYzY5ODk2OTQ4ZiIsDQogICJzeXN0ZW1OYW1lIjogIkNpc2VydmljZTBiYzY5ODk2OTQ4ZiIsDQogICJpbnZhcmlhbnRVVUlEIjogIjEzZmJkNzI3LWRjNzUtNDU1OS1iNzEyLWUwMjc5YmY4YTg2MSIsDQogICJhdHRDb250YWN0IjogImNzMDAwOCIsDQogICJuYW1lIjogImNpU2VydmljZTBiYzY5ODk2OTQ4ZiINCn0=";
			updatedPayloadData = "ew0KICAiYXJ0aWZhY3RzIjogIjEyMzQzIiwNCiAgIm5vcm1hbGl6ZWROYW1lIjogIjU0MzUzNCIsDQogICJzeXN0ZW1OYW1lIjogIkNpc2VydmljZTBiYzY5ODk2OTQ4ZiIsDQogICJpbnZhcmlhbnRVVUlEIjogIjEzZmJkNzI3LWRjNzUtNDU1OS1iNzEyLWUwMjc5YmY4YTg2MSIsDQogICJhdHRDb250YWN0IjogImNzMDAwOCIsDQogICJuYW1lIjogImNpU2VydmljZTBiYzY5ODk2OTQ4ZiINCn0=";
			break;
		}
		case PUPPET:
		case CHEF:
		case DG_XML:
		case YANG: {
			artifactName = generateUUIDforSufix() + artifactType + "yangXml.xml";
			payloadData = "PD94bWwgdmVyc2lvbj0iMS4wIj8+DQo8ZGF0YT4NCiAgPHNwb3J0cz4NCiAgICA8cGVyc29uPg0KICAgICAgPG5hbWU+TGlvbmVsIEFuZHJlcyBNZXNzaTwvbmFtZT4NCiAgICAgIDxiaXJ0aGRheT4xOTg3LTA2LTI0VDAwOjAwOjAwLTAwOjAwPC9iaXJ0aGRheT4NCiAgICA8L3BlcnNvbj4NCiAgICA8cGVyc29uPg0KICAgICAgPG5hbWU+Q3Jpc3RpYW5vIFJvbmFsZG88L25hbWU+DQogICAgICA8YmlydGhkYXk+MTk4NS0wMi0wNVQwMDowMDowMC0wMDowMDwvYmlydGhkYXk+DQogICAgPC9wZXJzb24+DQogICAgPHRlYW0+DQogICAgICA8bmFtZT5GQyBCYXJjZWxvbmE8L25hbWU+DQogICAgICA8cGxheWVyPg0KICAgICAgICA8bmFtZT5MaW9uZWwgQW5kcmVzIE1lc3NpPC9uYW1lPg0KICAgICAgICA8c2Vhc29uPkNoYW1waW9ucyBMZWFndWUgMjAxNC0yMDE1PC9zZWFzb24+DQogICAgICAgIDxudW1iZXI+MTA8L251bWJlcj4NCiAgICAgICAgPHNjb3Jlcz40Mzwvc2NvcmVzPg0KICAgICAgPC9wbGF5ZXI+DQogICAgPC90ZWFtPg0KICAgIDx0ZWFtPg0KICAgICAgPG5hbWU+UmVhbCBNYWRyaWQ8L25hbWU+DQogICAgICA8cGxheWVyPg0KICAgICAgICA8bmFtZT5DcmlzdGlhbm8gUm9uYWxkbzwvbmFtZT4NCiAgICAgICAgPHNlYXNvbj5DaGFtcGlvbnMgTGVhZ3VlIDIwMTQtMjAxNTwvc2Vhc29uPg0KICAgICAgICA8bnVtYmVyPjc8L251bWJlcj4NCiAgICAgICAgPHNjb3Jlcz40ODwvc2NvcmVzPg0KICAgICAgPC9wbGF5ZXI+DQogICAgPC90ZWFtPg0KICA8L3Nwb3J0cz4NCg0KPC9kYXRhPg==";
			updatedPayloadData = "PD94bWwgdmVyc2lvbj0iMS4wIj8+DQo8ZGF0YT4NCiAgPHNwb3J0cz4NCiAgICA8cGVyc29uPg0KICAgICAgPG5hbWU+TGlvbmVsIEFuZHJlcyBNZXNzaTwvbmFtZT4NCiAgICAgIDxiaXJ0aGRheT4xOTkwLTA2LTI0VDAwOjAwOjAwLTAwOjExPC9iaXJ0aGRheT4NCiAgICA8L3BlcnNvbj4NCiAgICA8cGVyc29uPg0KICAgICAgPG5hbWU+Q3Jpc3RpYW5vIFJvbmFsZG88L25hbWU+DQogICAgICA8YmlydGhkYXk+MTk4NS0wMi0wNVQwMDowMDowMC0wMDowMDwvYmlydGhkYXk+DQogICAgPC9wZXJzb24+DQogICAgPHRlYW0+DQogICAgICA8bmFtZT5GQyBCYXJjZWxvbmE8L25hbWU+DQogICAgICA8cGxheWVyPg0KICAgICAgICA8bmFtZT5MaW9uZWwgQW5kcmVzIE1lc3NpPC9uYW1lPg0KICAgICAgICA8c2Vhc29uPkNoYW1waW9ucyBMZWFndWUgMjAxNC0yMDE1PC9zZWFzb24+DQogICAgICAgIDxudW1iZXI+MTA8L251bWJlcj4NCiAgICAgICAgPHNjb3Jlcz40Mzwvc2NvcmVzPg0KICAgICAgPC9wbGF5ZXI+DQogICAgPC90ZWFtPg0KICAgIDx0ZWFtPg0KICAgICAgPG5hbWU+UmVhbCBNYWRyaWQ8L25hbWU+DQogICAgICA8cGxheWVyPg0KICAgICAgICA8bmFtZT5DcmlzdGlhbm8gUm9uYWxkbzwvbmFtZT4NCiAgICAgICAgPHNlYXNvbj5DaGFtcGlvbnMgTGVhZ3VlIDIwMTQtMjAxNTwvc2Vhc29uPg0KICAgICAgICA8bnVtYmVyPjc8L251bWJlcj4NCiAgICAgICAgPHNjb3Jlcz40ODwvc2NvcmVzPg0KICAgICAgPC9wbGF5ZXI+DQogICAgPC90ZWFtPg0KICA8L3Nwb3J0cz4NCg0KPC9kYXRhPg==";
			timeout = 15;
			artifactLabel = normalizeArtifactLabel(artifactName);
			break;
		}
		case VF_LICENSE:
		case VENDOR_LICENSE:
		case MODEL_INVENTORY_PROFILE:
		case MODEL_QUERY_SPEC:
		case VNF_CATALOG:
		case YANG_XML: {
			artifactName = generateUUIDforSufix() + artifactType + "yangXml.xml";
			payloadData = "PD94bWwgdmVyc2lvbj0iMS4wIj8+DQo8ZGF0YT4NCiAgPHNwb3J0cz4NCiAgICA8cGVyc29uPg0KICAgICAgPG5hbWU+TGlvbmVsIEFuZHJlcyBNZXNzaTwvbmFtZT4NCiAgICAgIDxiaXJ0aGRheT4xOTg3LTA2LTI0VDAwOjAwOjAwLTAwOjAwPC9iaXJ0aGRheT4NCiAgICA8L3BlcnNvbj4NCiAgICA8cGVyc29uPg0KICAgICAgPG5hbWU+Q3Jpc3RpYW5vIFJvbmFsZG88L25hbWU+DQogICAgICA8YmlydGhkYXk+MTk4NS0wMi0wNVQwMDowMDowMC0wMDowMDwvYmlydGhkYXk+DQogICAgPC9wZXJzb24+DQogICAgPHRlYW0+DQogICAgICA8bmFtZT5GQyBCYXJjZWxvbmE8L25hbWU+DQogICAgICA8cGxheWVyPg0KICAgICAgICA8bmFtZT5MaW9uZWwgQW5kcmVzIE1lc3NpPC9uYW1lPg0KICAgICAgICA8c2Vhc29uPkNoYW1waW9ucyBMZWFndWUgMjAxNC0yMDE1PC9zZWFzb24+DQogICAgICAgIDxudW1iZXI+MTA8L251bWJlcj4NCiAgICAgICAgPHNjb3Jlcz40Mzwvc2NvcmVzPg0KICAgICAgPC9wbGF5ZXI+DQogICAgPC90ZWFtPg0KICAgIDx0ZWFtPg0KICAgICAgPG5hbWU+UmVhbCBNYWRyaWQ8L25hbWU+DQogICAgICA8cGxheWVyPg0KICAgICAgICA8bmFtZT5DcmlzdGlhbm8gUm9uYWxkbzwvbmFtZT4NCiAgICAgICAgPHNlYXNvbj5DaGFtcGlvbnMgTGVhZ3VlIDIwMTQtMjAxNTwvc2Vhc29uPg0KICAgICAgICA8bnVtYmVyPjc8L251bWJlcj4NCiAgICAgICAgPHNjb3Jlcz40ODwvc2NvcmVzPg0KICAgICAgPC9wbGF5ZXI+DQogICAgPC90ZWFtPg0KICA8L3Nwb3J0cz4NCg0KPC9kYXRhPg==";
			updatedPayloadData = "PD94bWwgdmVyc2lvbj0iMS4wIj8+DQo8ZGF0YT4NCiAgPHNwb3J0cz4NCiAgICA8cGVyc29uPg0KICAgICAgPG5hbWU+TGlvbmVsIEFuZHJlcyBNZXNzaTwvbmFtZT4NCiAgICAgIDxiaXJ0aGRheT4xOTkwLTA2LTI0VDAwOjAwOjAwLTAwOjExPC9iaXJ0aGRheT4NCiAgICA8L3BlcnNvbj4NCiAgICA8cGVyc29uPg0KICAgICAgPG5hbWU+Q3Jpc3RpYW5vIFJvbmFsZG88L25hbWU+DQogICAgICA8YmlydGhkYXk+MTk4NS0wMi0wNVQwMDowMDowMC0wMDowMDwvYmlydGhkYXk+DQogICAgPC9wZXJzb24+DQogICAgPHRlYW0+DQogICAgICA8bmFtZT5GQyBCYXJjZWxvbmE8L25hbWU+DQogICAgICA8cGxheWVyPg0KICAgICAgICA8bmFtZT5MaW9uZWwgQW5kcmVzIE1lc3NpPC9uYW1lPg0KICAgICAgICA8c2Vhc29uPkNoYW1waW9ucyBMZWFndWUgMjAxNC0yMDE1PC9zZWFzb24+DQogICAgICAgIDxudW1iZXI+MTA8L251bWJlcj4NCiAgICAgICAgPHNjb3Jlcz40Mzwvc2NvcmVzPg0KICAgICAgPC9wbGF5ZXI+DQogICAgPC90ZWFtPg0KICAgIDx0ZWFtPg0KICAgICAgPG5hbWU+UmVhbCBNYWRyaWQ8L25hbWU+DQogICAgICA8cGxheWVyPg0KICAgICAgICA8bmFtZT5DcmlzdGlhbm8gUm9uYWxkbzwvbmFtZT4NCiAgICAgICAgPHNlYXNvbj5DaGFtcGlvbnMgTGVhZ3VlIDIwMTQtMjAxNTwvc2Vhc29uPg0KICAgICAgICA8bnVtYmVyPjc8L251bWJlcj4NCiAgICAgICAgPHNjb3Jlcz40ODwvc2NvcmVzPg0KICAgICAgPC9wbGF5ZXI+DQogICAgPC90ZWFtPg0KICA8L3Nwb3J0cz4NCg0KPC9kYXRhPg==";
			timeout = 0;
			artifactLabel = normalizeArtifactLabel(artifactName);
			break;
		}
		case SNMP_POLL:
		case SNMP_TRAP:
		case DCAE_INVENTORY_DOC:
		case GUIDE:
		case OTHER: {
			artifactName = generateUUIDforSufix() + artifactType + "other.pdf";
			payloadData = "aGVhdF90ZW1wbGF0ZV92ZXJzaW9uOiAyMDEzLTA1LTIzDQoNCmRlc2NyaXB0aW9uOiBTaW1wbGUgdGVtcGxhdGUgdG8gZGVwbG95IGEgc3RhY2sgd2l0aCB0d28gdmlydHVhbCBtYWNoaW5lIGluc3RhbmNlcw0KDQpwYXJhbWV0ZXJzOg0KICBpbWFnZV9uYW1lXzE6DQogICAgdHlwZTogc3RyaW5nDQogICAgbGFiZWw6IEltYWdlIE5hbWUNCiAgICBkZXNjcmlwdGlvbjogU0NPSU1BR0UgU3BlY2lmeSBhbiBpbWFnZSBuYW1lIGZvciBpbnN0YW5jZTENCiAgICBkZWZhdWx0OiBjaXJyb3MtMC4zLjEteDg2XzY0DQogIGltYWdlX25hbWVfMjoNCiAgICB0eXBlOiBzdHJpbmcNCiAgICBsYWJlbDogSW1hZ2UgTmFtZQ0KICAgIGRlc2NyaXB0aW9uOiBTQ09JTUFHRSBTcGVjaWZ5IGFuIGltYWdlIG5hbWUgZm9yIGluc3RhbmNlMg0KICAgIGRlZmF1bHQ6IGNpcnJvcy0wLjMuMS14ODZfNjQNCiAgbmV0d29ya19pZDoNCiAgICB0eXBlOiBzdHJpbmcNCiAgICBsYWJlbDogTmV0d29yayBJRA0KICAgIGRlc2NyaXB0aW9uOiBTQ09ORVRXT1JLIE5ldHdvcmsgdG8gYmUgdXNlZCBmb3IgdGhlIGNvbXB1dGUgaW5zdGFuY2UNCiAgICBoaWRkZW46IHRydWUNCiAgICBjb25zdHJhaW50czoNCiAgICAgIC0gbGVuZ3RoOiB7IG1pbjogNiwgbWF4OiA4IH0NCiAgICAgICAgZGVzY3JpcHRpb246IFBhc3N3b3JkIGxlbmd0aCBtdXN0IGJlIGJldHdlZW4gNiBhbmQgOCBjaGFyYWN0ZXJzLg0KICAgICAgLSByYW5nZTogeyBtaW46IDYsIG1heDogOCB9DQogICAgICAgIGRlc2NyaXB0aW9uOiBSYW5nZSBkZXNjcmlwdGlvbg0KICAgICAgLSBhbGxvd2VkX3ZhbHVlczoNCiAgICAgICAgLSBtMS5zbWFsbA0KICAgICAgICAtIG0xLm1lZGl1bQ0KICAgICAgICAtIG0xLmxhcmdlDQogICAgICAgIGRlc2NyaXB0aW9uOiBBbGxvd2VkIHZhbHVlcyBkZXNjcmlwdGlvbg0KICAgICAgLSBhbGxvd2VkX3BhdHRlcm46ICJbYS16QS1aMC05XSsiDQogICAgICAgIGRlc2NyaXB0aW9uOiBQYXNzd29yZCBtdXN0IGNvbnNpc3Qgb2YgY2hhcmFjdGVycyBhbmQgbnVtYmVycyBvbmx5Lg0KICAgICAgLSBhbGxvd2VkX3BhdHRlcm46ICJbQS1aXStbYS16QS1aMC05XSoiDQogICAgICAgIGRlc2NyaXB0aW9uOiBQYXNzd29yZCBtdXN0IHN0YXJ0IHdpdGggYW4gdXBwZXJjYXNlIGNoYXJhY3Rlci4NCiAgICAgIC0gY3VzdG9tX2NvbnN0cmFpbnQ6IG5vdmEua2V5cGFpcg0KICAgICAgICBkZXNjcmlwdGlvbjogQ3VzdG9tIGRlc2NyaXB0aW9uDQoNCnJlc291cmNlczoNCiAgbXlfaW5zdGFuY2UxOg0KICAgIHR5cGU6IE9TOjpOb3ZhOjpTZXJ2ZXINCiAgICBwcm9wZXJ0aWVzOg0KICAgICAgaW1hZ2U6IHsgZ2V0X3BhcmFtOiBpbWFnZV9uYW1lXzEgfQ0KICAgICAgZmxhdm9yOiBtMS5zbWFsbA0KICAgICAgbmV0d29ya3M6DQogICAgICAgIC0gbmV0d29yayA6IHsgZ2V0X3BhcmFtIDogbmV0d29ya19pZCB9DQogIG15X2luc3RhbmNlMjoNCiAgICB0eXBlOiBPUzo6Tm92YTo6U2VydmVyDQogICAgcHJvcGVydGllczoNCiAgICAgIGltYWdlOiB7IGdldF9wYXJhbTogaW1hZ2VfbmFtZV8yIH0NCiAgICAgIGZsYXZvcjogbTEudGlueQ0KICAgICAgbmV0d29ya3M6DQogICAgICAgIC0gbmV0d29yayA6IHsgZ2V0X3BhcmFtIDogbmV0d29ya19pZCB9";
			updatedPayloadData = "aGVhdF90ZW1wbGF0ZV92ZXJzaW9uOiAyMDE2LTA1LTIzDQoNCmRlc2NyaXB0aW9uOiBTaW1wbGUgdGVtcGxhdGRzYWRzYWRzYWUgdG8gZGVwbG95IGEgc3RhY2sgd2l0aCB0d28gdmlydHVhbCBtYWNoaW5lIGluc3RhbmNlcw0KDQpwYXJhbWV0ZXJzOg0KICBpbWFnZV9uYW1lXzE6DQogICAgdHlwZTogc3RyaW5nDQogICAgbGFiZWw6IEltYWdlIE5hbWUNCiAgICBkZXNjcmlwdGlvbjogU0NPSU1BR0UgU3BlY2lmeSBhbiBpbWFkc2FkYXN3Z2UgbmFtZSBmb3IgaW5zdGFuY2UxDQogICAgZGVmYXVsdDogY2lycm9zLTAuMy4xLXg4Nl82NA0KICBpbWFnZV9uYW1lXzI6DQogICAgdHlwZTogc3RyaW5nDQogICAgbGFiZWw6IEltYWdlIE5hbWUNCiAgICBkZXNjcmlwdGlvbjogU0NPSU1BR0UgU3BlY2lmeSBhbiBpbWFnZSBuYW1lIGZvciBpbnN0YW5jZTINCiAgICBkZWZhdWx0OiBjaXJyb3MtMC4zLjEteDg2XzY0DQogIG5ldHdvcmtfaWQ6DQogICAgdHlwZTogc3RyaW5nDQogICAgbGFiZWw6IE5ldHdvcmsgSUQNCiAgICBkZXNjcmlwdGlvbjogU0NPTkVUV09SSyBOZXR3b3JrIHRvIGJlIHVzZWQgZm9yIHRoZSBjb21wdXRlIGluc3RhbmNlDQogICAgaGlkZGVuOiB0cnVlDQogICAgY29uc3RyYWludHM6DQogICAgICAtIGxlbmd0aDogeyBtaW46IDYsIG1heDogOCB9DQogICAgICAgIGRlc2NyaXB0aW9uOiBQYXNzd29yZCBsZW5ndGggbXVzdCBiZSBiZXR3ZWVuIDYgYW5kIDggY2hhcmFjdGVycy4NCiAgICAgIC0gcmFuZ2U6IHsgbWluOiA2LCBtYXg6IDggfQ0KICAgICAgICBkZXNjcmlwdGlvbjogUmFuZ2UgZGVzY3JpcHRpb24NCiAgICAgIC0gYWxsb3dlZF92YWx1ZXM6DQogICAgICAgIC0gbTEuc21hbGwNCiAgICAgICAgLSBtMS5tZWRpdW0NCiAgICAgICAgLSBtMS5sYXJnZQ0KICAgICAgICBkZXNjcmlwdGlvbjogQWxsb3dlZCB2YWx1ZXMgZGVzY3JpcHRpb24NCiAgICAgIC0gYWxsb3dlZF9wYXR0ZXJuOiAiW2EtekEtWjAtOV0rIg0KICAgICAgICBkZXNjcmlwdGlvbjogUGFzc3dvcmQgbXVzdCBjb25zaXN0IG9mIGNoYXJhY3RlcnMgYW5kIG51bWJlcnMgb25seS4NCiAgICAgIC0gYWxsb3dlZF9wYXR0ZXJuOiAiW0EtWl0rW2EtekEtWjAtOV0qIg0KICAgICAgICBkZXNjcmlwdGlvbjogUGFzc3dvcmQgbXVzdCBzdGFydCB3aXRoIGFuIHVwcGVyY2FzZSBjaGFyYWN0ZXIuDQogICAgICAtIGN1c3RvbV9jb25zdHJhaW50OiBub3ZhLmtleXBhaXINCiAgICAgICAgZGVzY3JpcHRpb246IEN1c3RvbSBkZXNjcmlwdGlvbg0KDQpyZXNvdXJjZXM6DQogIG15X2luc3RhbmNlMToNCiAgICB0eXBlOiBPUzo6Tm92YTo6U2VydmVyDQogICAgcHJvcGVydGllczoNCiAgICAgIGltYWdlOiB7IGdldF9wYXJhbTogaW1hZ2VfbmFtZV8xIH0NCiAgICAgIGZsYXZvcjogbTEuc21hbGwNCiAgICAgIG5ldHdvcmtzOg0KICAgICAgICAtIG5ldHdvcmsgOiB7IGdldF9wYXJhbSA6IG5ldHdvcmtfaWQgfQ0KICBteV9pbnN0YW5jZTI6DQogICAgdHlwZTogT1M6Ok5vdmE6OlNlcnZlcg0KICAgIHByb3BlcnRpZXM6DQogICAgICBpbWFnZTogeyBnZXRfcGFyYW06IGltYWdlX25hbWVfMiB9DQogICAgICBmbGF2b3I6IG0xLnRpbnkNCiAgICAgIG5ldHdvcmtzOg0KICAgICAgICAtIG5ldHdvcmsgOiB7IGdldF9wYXJhbSA6IG5ldHdvcmtfaWQgfQ";
			timeout = 0;
			artifactLabel = normalizeArtifactLabel(artifactName);
			break;
		}
		case SHELL_SCRIPT:
		default: {// dummy
			artifactName = generateUUIDforSufix() + "testArtifact.sh";
			payloadData = "dGVzdA==";
			updatedPayloadData = "YmVzYg==";
			artifactLabel = normalizeArtifactLabel(artifactName);
			break;
		}
		}
		artifactLabel = normalizeArtifactLabel("ci" + artifactName);
		
		ArtifactReqDetails artifactDetails = null;
		
		if (!updatedPayload){
		artifactDetails = new ArtifactReqDetails(artifactName, artifactType, artifactDescription, payloadData, artifactLabel);
		}
		else artifactDetails = new ArtifactReqDetails(artifactName, artifactType, artifactDescription,
				updatedPayloadData, artifactLabel);
		
		artifactDetails.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT.getType());
		artifactDetails.setUrl(url);
		artifactDetails.setTimeout(timeout);
		artifactDetails.setArtifactDisplayName(artifactLabel);
		return artifactDetails;
	}

	private static String getDcaeArtifactName(ArtifactTypeEnum artifactTypeEnum, String artifactType) {
		String artifactName = null;
		switch (artifactTypeEnum) {
		case DCAE_INVENTORY_TOSCA: {
			artifactName = generateUUIDforSufix() + artifactType + "_toscaSampleArtifact.yml";
			break;
		}
		case DCAE_INVENTORY_JSON: {
			artifactName = generateUUIDforSufix() + artifactType + "_jsonSampleArtifact.json";
			break;
		}
		case DCAE_INVENTORY_POLICY: {
			artifactName = generateUUIDforSufix() + artifactType + "_emfSampleArtifact.emf";
			break;
		}
		case DCAE_INVENTORY_DOC: {
			artifactName = generateUUIDforSufix() + artifactType + "_docSampleArtifact.doc";
			break;
		}
		case DCAE_INVENTORY_BLUEPRINT: {
			artifactName = generateUUIDforSufix() + artifactType + "_bluePrintSampleArtifact.xml";
			break;
		}
		case DCAE_INVENTORY_EVENT: {
			artifactName = generateUUIDforSufix() + artifactType + "_eventSampleArtifact.xml";
			break;
		}
		}
		return artifactName;
	}

	// ---------------------Audit message------------------
	public static ExpectedResourceAuditJavaObject getDefaultImportResourceAuditMsgSuccess() {

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
		expectedResourceAuditJavaObject.setAction(AuditingActionEnum.IMPORT_RESOURCE.getName());
		expectedResourceAuditJavaObject.setResourceName("defaultImportResourceName.yaml");
		expectedResourceAuditJavaObject.setResourceType("Resource");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrVersion("0.1");
		expectedResourceAuditJavaObject.setModifierName(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getFullName());
		expectedResourceAuditJavaObject.setModifierUid(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getUserId());
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject.setStatus("201");
		expectedResourceAuditJavaObject.setDesc("OK");
		expectedResourceAuditJavaObject.setToscaNodeType("");
		return expectedResourceAuditJavaObject;

	}

	public static ExpectedResourceAuditJavaObject getDefaultImportResourceAuditMsgFailure(ErrorInfo errorInfo, List<String> variables) {

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
		expectedResourceAuditJavaObject.setAction(AuditingActionEnum.IMPORT_RESOURCE.getName());
		expectedResourceAuditJavaObject.setResourceName("");
		expectedResourceAuditJavaObject.setResourceType("Resource");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setModifierName(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getFullName());
		expectedResourceAuditJavaObject.setModifierUid(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getUserId());
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		expectedResourceAuditJavaObject.setToscaNodeType("");
		return expectedResourceAuditJavaObject;

	}

	public static ExpectedResourceAuditJavaObject getDefaultCertificationRequestAuditMsgSuccess() {

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
		expectedResourceAuditJavaObject.setAction(AuditingActionEnum.CERTIFICATION_REQUEST_RESOURCE.getName());
		expectedResourceAuditJavaObject.setResourceName("defaultResourceName");
		expectedResourceAuditJavaObject.setResourceType("Resource");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrVersion("0.1");
		expectedResourceAuditJavaObject.setModifierName(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getFullName());
		expectedResourceAuditJavaObject.setModifierUid(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getUserId());
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject.setStatus("200");
		expectedResourceAuditJavaObject.setDesc("OK");
		expectedResourceAuditJavaObject.setComment("");
		return expectedResourceAuditJavaObject;

	}

	public static ExpectedResourceAuditJavaObject getDefaultCertificationRequestAuditMsgFailure(ErrorInfo errorInfo, List<String> variables) {

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
		expectedResourceAuditJavaObject.setAction(AuditingActionEnum.CERTIFICATION_REQUEST_RESOURCE.getName());
		expectedResourceAuditJavaObject.setResourceName("");
		expectedResourceAuditJavaObject.setResourceType("Resource");
		expectedResourceAuditJavaObject.setPrevVersion("0.1");
		expectedResourceAuditJavaObject.setCurrVersion("0.1");
		expectedResourceAuditJavaObject.setModifierName(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getFullName());
		expectedResourceAuditJavaObject.setModifierUid(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getUserId());
		expectedResourceAuditJavaObject.setPrevState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedResourceAuditJavaObject.setDesc(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		expectedResourceAuditJavaObject.setComment("");
		return expectedResourceAuditJavaObject;

	}

	public static ExpectedExternalAudit getDefaultExternalAuditObject(AssetTypeEnum assetType, AuditingActionEnum action, String query) {

		ExpectedExternalAudit expectedExternalAudit = new ExpectedExternalAudit();
		expectedExternalAudit.setACTION(action.getName());
		expectedExternalAudit.setCONSUMER_ID("ci");
		expectedExternalAudit.setRESOURCE_URL("/sdc/v1/catalog/" + assetType.getValue() + (query == null ? "" : query));
		expectedExternalAudit.setSTATUS("200");
		expectedExternalAudit.setDESC("OK");
		return expectedExternalAudit;

	}

	public static ExpectedExternalAudit getDefaultAssetListAudit(AssetTypeEnum assetType, AuditingActionEnum auditAction) {

		// ExpectedExternalAudit expectedAssetListAuditJavaObject = new
		// ExpectedExternalAudit();
		ExpectedExternalAudit expectedAssetListAuditJavaObject = getDefaultExternalAuditObject(assetType, auditAction, null);
		return expectedAssetListAuditJavaObject;

	}

	public static ExpectedExternalAudit getDefaultFilteredAssetListAudit(AssetTypeEnum assetType, String query) {

		// ExpectedExternalAudit expectedAssetListAuditJavaObject = new
		// ExpectedExternalAudit();
		ExpectedExternalAudit expectedAssetListAuditJavaObject = getDefaultExternalAuditObject(assetType, AuditingActionEnum.GET_FILTERED_ASSET_LIST, query);
		return expectedAssetListAuditJavaObject;

	}

	public static ExpectedExternalAudit getDefaultExternalArtifactAuditSuccess(AssetTypeEnum assetType, AuditingActionEnum action, ArtifactDefinition artifactDefinition, String componentUUID) {

		// ExpectedExternalAudit expectedExternalArtifactAudit = new
		// ExpectedExternalAudit();

		ExpectedExternalAudit expectedExternalArtifactAudit = getDefaultExternalAuditObject(assetType, action, null);
		expectedExternalArtifactAudit.setMODIFIER(AuditValidationUtils.getModifierString(artifactDefinition.getUpdaterFullName(), artifactDefinition.getUserIdLastUpdater()));
		expectedExternalArtifactAudit.setPREV_ARTIFACT_UUID("");
		expectedExternalArtifactAudit.setCURR_ARTIFACT_UUID(artifactDefinition.getArtifactUUID());
		expectedExternalArtifactAudit.setARTIFACT_DATA(AuditValidationUtils.buildArtifactDataAudit(artifactDefinition));
		expectedExternalArtifactAudit.setRESOURCE_URL(expectedExternalArtifactAudit.getRESOURCE_URL() + "/" + componentUUID + "/artifacts");
		return expectedExternalArtifactAudit;

	}
	
	public static ExpectedExternalAudit getDefaultExternalArtifactAuditSuccess(AssetTypeEnum assetType, AuditingActionEnum action, ArtifactDefinition artifactDefinition, Component component) {

		 //ExpectedExternalAudit expectedExternalArtifactAudit = new ExpectedExternalAudit();
		
		ExpectedExternalAudit expectedExternalArtifactAudit = getDefaultExternalAuditObject(assetType, action, null);
		expectedExternalArtifactAudit.setMODIFIER(AuditValidationUtils.getModifierString(artifactDefinition.getUpdaterFullName(), artifactDefinition.getUserIdLastUpdater()));
		expectedExternalArtifactAudit.setPREV_ARTIFACT_UUID("");
		expectedExternalArtifactAudit.setCURR_ARTIFACT_UUID(artifactDefinition.getArtifactUUID());
		expectedExternalArtifactAudit.setARTIFACT_DATA(AuditValidationUtils.buildArtifactDataAudit(artifactDefinition));
		expectedExternalArtifactAudit.setRESOURCE_URL(expectedExternalArtifactAudit.getRESOURCE_URL() + "/" + component.getUUID() + "/artifacts");
		expectedExternalArtifactAudit.setRESOURCE_NAME(component.getName());
		expectedExternalArtifactAudit.setRESOURCE_TYPE(component.getComponentType().getValue());
		return expectedExternalArtifactAudit;
		
	}
	
	public static ExpectedResourceAuditJavaObject getDefaultCreateResourceExternalAPI(String resourceName) {

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
		expectedResourceAuditJavaObject.setAction(AuditingActionEnum.CREATE_RESOURCE_BY_API.getName());
		expectedResourceAuditJavaObject.setResourceName(resourceName);
		expectedResourceAuditJavaObject.setResourceType("Resource");
		expectedResourceAuditJavaObject.setCONSUMER_ID("ci");
		expectedResourceAuditJavaObject.setRESOURCE_URL("/sdc/v1/catalog/resources");
		expectedResourceAuditJavaObject.setMODIFIER("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrVersion("0.1");
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
		expectedResourceAuditJavaObject.setStatus("201");
		expectedResourceAuditJavaObject.setDesc("OK");
		
		return expectedResourceAuditJavaObject;
		
		
	}

	public static ExpectedExternalAudit getDefaultExternalArtifactAuditSuccess(AssetTypeEnum assetType, AuditingActionEnum action, ArtifactDefinition artifactDefinition, String componentUUID, String resourceInstanceName) {

		ExpectedExternalAudit expectedExternalArtifactAudit = getDefaultExternalArtifactAuditSuccess(assetType, action, artifactDefinition, componentUUID);
		expectedExternalArtifactAudit.setRESOURCE_URL("/sdc/v1/catalog/" + assetType.getValue() + "/" + componentUUID + "/resourceInstances/" + resourceInstanceName + "/artifacts");
		return expectedExternalArtifactAudit;
	}

	public static ExpectedExternalAudit getDefaultExternalArtifactAuditFailure(AssetTypeEnum assetType, AuditingActionEnum action, ArtifactDefinition artifactDefinition, String componentUUID, ErrorInfo errorInfo, List<String> variables) {

		// ExpectedExternalAudit expectedExternalArtifactAudit = new
		// ExpectedExternalAudit();

		ExpectedExternalAudit expectedExternalArtifactAudit = getDefaultExternalAuditObject(assetType, action, null);
		expectedExternalArtifactAudit.setMODIFIER(AuditValidationUtils.getModifierString(artifactDefinition.getUpdaterFullName(), artifactDefinition.getUserIdLastUpdater()));
		expectedExternalArtifactAudit.setPREV_ARTIFACT_UUID("");
		expectedExternalArtifactAudit.setCURR_ARTIFACT_UUID(artifactDefinition.getArtifactUUID());
		expectedExternalArtifactAudit.setARTIFACT_DATA(AuditValidationUtils.buildArtifactDataAudit(artifactDefinition));
		expectedExternalArtifactAudit.setRESOURCE_URL(expectedExternalArtifactAudit.getRESOURCE_URL() + "/" + componentUUID + "/artifacts");
		expectedExternalArtifactAudit.setSTATUS(errorInfo.getCode().toString());
		expectedExternalArtifactAudit.setDESC(AuditValidationUtils.buildAuditDescription(errorInfo, variables));
		return expectedExternalArtifactAudit;

	}

	public static ExpectedExternalAudit getDefaultExternalArtifactAuditFailure(AssetTypeEnum assetType, AuditingActionEnum action, ArtifactDefinition artifactDefinition, String componentUUID, ErrorInfo errorInfo, List<String> variables,
			String resourceInstanceName) {

		ExpectedExternalAudit expectedExternalArtifactAudit = getDefaultExternalArtifactAuditFailure(assetType, action, artifactDefinition, componentUUID, errorInfo, variables);
		expectedExternalArtifactAudit.setRESOURCE_URL("/sdc/v1/catalog/" + assetType.getValue() + "/" + componentUUID + "/resourceInstances/" + resourceInstanceName + "/artifacts");
		return expectedExternalArtifactAudit;
	}

	public static ExpectedExternalAudit getFilteredAssetListAuditCategoryNotFound(AssetTypeEnum assetType, String query, String category) {

		// ExpectedExternalAudit expectedAssetListAuditJavaObject = new
		// ExpectedExternalAudit();
		ExpectedExternalAudit expectedAssetListAuditJavaObject = getDefaultExternalAuditObject(assetType, AuditingActionEnum.GET_FILTERED_ASSET_LIST, query);
		expectedAssetListAuditJavaObject.setSTATUS("404");
		ErrorInfo errorInfo = null;
		try {
			errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_CATEGORY_NOT_FOUND.name());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String desc = (errorInfo.getMessageId() + ": " + errorInfo.getMessage()).replace("%2", "category").replace("%3", category).replace("%1", "resource");
		expectedAssetListAuditJavaObject.setDESC(desc);

		return expectedAssetListAuditJavaObject;

	}

	public static ExpectedExternalAudit getDefaultAssetMetadataAudit(AssetTypeEnum assetType, Component component) {

		ExpectedExternalAudit expectedAssetListAuditJavaObject = new ExpectedExternalAudit();
		expectedAssetListAuditJavaObject = getDefaultExternalAuditObject(assetType, AuditingActionEnum.GET_ASSET_METADATA, null);
		expectedAssetListAuditJavaObject.setRESOURCE_URL(expectedAssetListAuditJavaObject.getRESOURCE_URL() + "/" + component.getUUID() + "/metadata");
		expectedAssetListAuditJavaObject.setRESOURCE_NAME(component.getName());
		expectedAssetListAuditJavaObject.setRESOURCE_TYPE(component.getComponentType().getValue());
		expectedAssetListAuditJavaObject.setSERVICE_INSTANCE_ID(component.getUUID());
		return expectedAssetListAuditJavaObject;

	}

	public static ExpectedExternalAudit getDefaultAssetMetadataAuditFailure(AssetTypeEnum assetType, String serviceUuid, String resourceType) {

		ExpectedExternalAudit expectedAssetListAuditJavaObject = new ExpectedExternalAudit();
		expectedAssetListAuditJavaObject = getDefaultExternalAuditObject(assetType, AuditingActionEnum.GET_ASSET_METADATA, null);
		expectedAssetListAuditJavaObject.setSTATUS("404");
		expectedAssetListAuditJavaObject.setDESC("OK");
		expectedAssetListAuditJavaObject.setRESOURCE_URL(expectedAssetListAuditJavaObject.getRESOURCE_URL() + "/" + serviceUuid + "/metadata");
		expectedAssetListAuditJavaObject.setRESOURCE_TYPE(resourceType);
		expectedAssetListAuditJavaObject.setSERVICE_INSTANCE_ID(serviceUuid);
		return expectedAssetListAuditJavaObject;

	}

	// Category/Subcategory/Group
	public static CategoryDefinition getDefaultCategory() {
		CategoryDefinition productCategoryDefinition = new CategoryDefinition();
		productCategoryDefinition.setName("CiCateg" + generateUUIDforSufix());
		return productCategoryDefinition;
	}

	public static SubCategoryDefinition getDefaultSubCategory() {
		SubCategoryDefinition productSubCategoryDefinition = new SubCategoryDefinition();
		productSubCategoryDefinition.setName("CiSubCateg" + generateUUIDforSufix());
		return productSubCategoryDefinition;
	}

	public static GroupingDefinition getDefaultGroup() {
		GroupingDefinition productGroupDefinition = new GroupingDefinition();
		productGroupDefinition.setName("CiGrouping1" + generateUUIDforSufix());
		return productGroupDefinition;
	}

	// Product

	public static ProductReqDetails getDefaultProduct() {
		return createDefaultProductReqDetails("CiProduct1", null);
	}

	public static ProductReqDetails getDefaultProduct(String name) {
		return createDefaultProductReqDetails(name, null);
	}

	public static ProductReqDetails getDefaultProduct(CategoryDefinition category) {
		List<CategoryDefinition> categories = new ArrayList<>();
		categories.add(category);
		return createDefaultProductReqDetails("CiProduct1", categories);
	}

	public static ProductReqDetails getDefaultProduct(String name, CategoryDefinition category) {
		List<CategoryDefinition> categories = new ArrayList<>();
		categories.add(category);
		return createDefaultProductReqDetails(name, categories);
	}

	public static ProductReqDetails getDefaultProduct(List<CategoryDefinition> categories) {
		return createDefaultProductReqDetails("CiProduct1", categories);
	}

	public static ProductReqDetails getDefaultProduct(String name, List<CategoryDefinition> categories) {
		return createDefaultProductReqDetails(name, categories);
	}

	private static ProductReqDetails createDefaultProductReqDetails(String name, List<CategoryDefinition> categories) {
		ProductReqDetails product = new ProductReqDetails(name);
		ArrayList<String> tags = new ArrayList<String>();
		tags.add(name);
		product.setTags(tags);
		product.setProjectCode("12345");
		product.setIcon("myIcon");
		ArrayList<String> contacts = new ArrayList<String>();
		contacts.add(ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1).getUserId());
		product.setContacts(contacts);
		product.setCategories(categories);
		String fullName = "This is my full name: " + name;
		product.setFullName(fullName);
		String description = "This is product description";
		product.setDescription(description);
		return product;
	}

	public static RequirementCapabilityRelDef getReqCapRelation(String fromCompInstId, String toCompInstId, String reqOwnerId, String capOwnerId, String capType, String reqCapName, List<CapabilityDefinition> capList,
			List<RequirementDefinition> reqList) {
		RequirementCapabilityRelDef requirementDef = new RequirementCapabilityRelDef();
		requirementDef.setFromNode(fromCompInstId);
		requirementDef.setToNode(toCompInstId);
		RelationshipInfo pair = new RelationshipInfo();
		pair.setRequirementOwnerId(reqOwnerId);
		pair.setCapabilityOwnerId(capOwnerId);
		pair.setRequirement(reqCapName);
		RelationshipImpl relationship = new RelationshipImpl();
		relationship.setType(capType);
		pair.setRelationships(relationship);
		pair.setCapabilityUid(capList.get(0).getUniqueId());
		pair.setRequirementUid(reqList.get(0).getUniqueId());
		List<CapabilityRequirementRelationship> relationships = new ArrayList<>();
		CapabilityRequirementRelationship capReqRel = new CapabilityRequirementRelationship();
		relationships.add(capReqRel);
		capReqRel.setRelation(pair);
		requirementDef.setRelationships(relationships);
		return requirementDef;
	}

	private static String generateUUIDforSufix() {

		String uniqueSufix = UUID.randomUUID().toString();
		String[] split = uniqueSufix.split("-");
		return uniqueSufix = split[4];
	}

	private static String normalizeArtifactLabel(String label) {

		label = label.substring(0, label.indexOf("."));
		String normalizedLabel = ValidationUtils.normalizeArtifactLabel(label);
		return normalizedLabel.substring(0, Math.min(25, normalizedLabel.length()));

	}

}
