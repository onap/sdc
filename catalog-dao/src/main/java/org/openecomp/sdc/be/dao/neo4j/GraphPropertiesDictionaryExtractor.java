/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.dao.neo4j;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;

public class GraphPropertiesDictionaryExtractor {

    private Map<String, Object> properties;
    private Gson gson = new Gson();

    public GraphPropertiesDictionaryExtractor(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getUniqueId() {
        return (String) properties.get(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
    }

    public String getName() {
        return (String) properties.get(GraphPropertiesDictionary.NAME.getProperty());
    }

    public String getVersion() {
        return (String) properties.get(GraphPropertiesDictionary.VERSION.getProperty());
    }

    public Boolean isHighestVersion() {
        return (Boolean) properties.get(GraphPropertiesDictionary.IS_HIGHEST_VERSION.getProperty());
    }

    public Long getCreationDate() {
        return (Long) properties.get(GraphPropertiesDictionary.CREATION_DATE.getProperty());
    }

    public Long getLastUpdateDate() {
        return (Long) properties.get(GraphPropertiesDictionary.LAST_UPDATE_DATE.getProperty());
    }

    public String getDescription() {
        return (String) properties.get(GraphPropertiesDictionary.DESCRIPTION.getProperty());
    }

    public String getConformanceLevel() {
        return (String) properties.get(GraphPropertiesDictionary.CONFORMANCE_LEVEL.getProperty());
    }

    @SuppressWarnings("unchecked")
    public List<String> getTags() {
        List<String> tagsFromJson;
        if (properties.get(GraphPropertiesDictionary.TAGS.getProperty()) instanceof List<?>) {
            tagsFromJson = (List<String>) properties.get(GraphPropertiesDictionary.TAGS.getProperty());
        } else {
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            tagsFromJson = gson.fromJson((String) properties.get(GraphPropertiesDictionary.TAGS.getProperty()), listType);
        }
        return tagsFromJson;
    }

    public String getIcon() {
        return (String) properties.get(GraphPropertiesDictionary.ICON.getProperty());
    }

    public String getState() {
        return (String) properties.get(GraphPropertiesDictionary.STATE.getProperty());
    }

    public String getContactId() {
        return (String) properties.get(GraphPropertiesDictionary.CONTACT_ID.getProperty());
    }

    public String getUUID() {
        return (String) properties.get(GraphPropertiesDictionary.UUID.getProperty());
    }

    public String getNormalizedName() {
        return (String) properties.get(GraphPropertiesDictionary.NORMALIZED_NAME.getProperty());
    }

    public String getSystemName() {
        return (String) properties.get(GraphPropertiesDictionary.SYSTEM_NAME.getProperty());
    }

    public Boolean isDeleted() {
        return (Boolean) properties.get(GraphPropertiesDictionary.IS_DELETED.getProperty());
    }

    public String getProjectCode() {
        return (String) properties.get(GraphPropertiesDictionary.PROJECT_CODE.getProperty());
    }

    public String getCsarUuid() {
        return (String) properties.get(GraphPropertiesDictionary.CSAR_UUID.getProperty());
    }

    public String getCsarVersion() {
        return (String) properties.get(GraphPropertiesDictionary.CSAR_VERSION.getProperty());
    }

    public String getCsarVersionId() {
        return (String) properties.get(GraphPropertiesDictionary.CSAR_VERSION_ID.getProperty());
    }

    public String getImportedToscaChecksum() {
        return (String) properties.get(GraphPropertiesDictionary.IMPORTED_TOSCA_CHECKSUM.getProperty());
    }

    public String getInvariantUuid() {
        return (String) properties.get(GraphPropertiesDictionary.INVARIANT_UUID.getProperty());
    }

    public String getVendorName() {
        return (String) properties.get(GraphPropertiesDictionary.VENDOR_NAME.getProperty());
    }



    public String getTenant() {
        return (String) properties.get(GraphPropertiesDictionary.TENANT.getProperty());
    }



    public String getVendorRelease() {
        return (String) properties.get(GraphPropertiesDictionary.VENDOR_RELEASE.getProperty());
    }

    public String getModel() {
        return (String) properties.get(GraphPropertiesDictionary.MODEL.getProperty());
    }

    public Boolean isAbstract() {
        return (Boolean) properties.get(GraphPropertiesDictionary.IS_ABSTRACT.getProperty());
    }

    public ResourceTypeEnum getResourceType() {
        if (properties.get(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty()) != null) {
            return ResourceTypeEnum.valueOf((String) properties.get(GraphPropertiesDictionary.RESOURCE_TYPE.getProperty()));
        } else {
            return null;
        }
    }

    public String getToscaResourceName() {
        return (String) properties.get(GraphPropertiesDictionary.TOSCA_RESOURCE_NAME.getProperty());
    }

    public Integer getInstanceCounter() {
        return (Integer) properties.get(GraphPropertiesDictionary.INSTANCE_COUNTER.getProperty());
    }

    public String getCost() {
        return (String) properties.get(GraphPropertiesDictionary.COST.getProperty());
    }

    public String getLicenseType() {
        return (String) properties.get(GraphPropertiesDictionary.LICENSE_TYPE.getProperty());
    }

    public String getDistributionStatus() {
        return (String) properties.get(GraphPropertiesDictionary.DISTRIBUTION_STATUS.getProperty());
    }

    public String getFullName() {
        return (String) properties.get(GraphPropertiesDictionary.FULL_NAME.getProperty());
    }

    public List<String> getContacts() {
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        return gson.fromJson((String) properties.get(GraphPropertiesDictionary.CONTACTS.getProperty()), listType);
    }

    public Boolean isActive() {
        return (Boolean) properties.get(GraphPropertiesDictionary.IS_ACTIVE.getProperty());
    }
}
