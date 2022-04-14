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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.datatypes.enums;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JsonPresentationFieldsExtractor {

    private final Map<String, Object> properties;

    public String getUniqueId() {
        return (String) properties.get(JsonPresentationFields.UNIQUE_ID.getPresentation());
    }

    public String getName() {
        return (String) properties.get(JsonPresentationFields.NAME.getPresentation());
    }

    public String getVersion() {
        return (String) properties.get(JsonPresentationFields.VERSION.getPresentation());
    }

    public Boolean isHighestVersion() {
        return (Boolean) properties.get(JsonPresentationFields.HIGHEST_VERSION.getPresentation());
    }

    public Long getCreationDate() {
        return (Long) properties.get(JsonPresentationFields.CREATION_DATE.getPresentation());
    }

    public Long getLastUpdateDate() {
        return (Long) properties.get(JsonPresentationFields.LAST_UPDATE_DATE.getPresentation());
    }

    public String getDescription() {
        return (String) properties.get(JsonPresentationFields.DESCRIPTION.getPresentation());
    }

    public String getState() {
        return (String) properties.get(JsonPresentationFields.LIFECYCLE_STATE.getPresentation());
    }

    @SuppressWarnings("unchecked")
    public List<String> getTags() {
        return (List<String>) properties.get(JsonPresentationFields.TAGS.getPresentation());
    }

    public String getIcon() {
        return (String) properties.get(JsonPresentationFields.ICON.getPresentation());
    }

    public String getContactId() {
        return (String) properties.get(JsonPresentationFields.CONTACT_ID.getPresentation());
    }

    public String getUUID() {
        return (String) properties.get(JsonPresentationFields.UUID.getPresentation());
    }

    public String getNormalizedName() {
        return (String) properties.get(JsonPresentationFields.NORMALIZED_NAME.getPresentation());
    }

    public String getSystemName() {
        return (String) properties.get(JsonPresentationFields.SYSTEM_NAME.getPresentation());
    }

    public Boolean isDeleted() {
        return (Boolean) properties.get(JsonPresentationFields.IS_DELETED.getPresentation());
    }

    public String getProjectCode() {
        return (String) properties.get(JsonPresentationFields.PROJECT_CODE.getPresentation());
    }

    public String getCsarUuid() {
        return (String) properties.get(JsonPresentationFields.CSAR_UUID.getPresentation());
    }

    public String getCsarVersion() {
        return (String) properties.get(JsonPresentationFields.CSAR_VERSION.getPresentation());
    }

    public String getCsarVersionId() {
        return (String) properties.get(JsonPresentationFields.CSAR_VERSION_ID.getPresentation());
    }

    public String getImportedToscaChecksum() {
        return (String) properties.get(JsonPresentationFields.IMPORTED_TOSCA_CHECKSUM.getPresentation());
    }

    public String getInvariantUuid() {
        return (String) properties.get(JsonPresentationFields.INVARIANT_UUID.getPresentation());
    }

    public Boolean isArchived() {
        return (Boolean) properties.get(JsonPresentationFields.IS_ARCHIVED.getPresentation());
    }

    public Boolean isVspArchived() {
        return (Boolean) properties.get(JsonPresentationFields.IS_VSP_ARCHIVED.getPresentation());
    }

    public Long getArchiveTime() {
        Object archiveTimeObject = properties.get(JsonPresentationFields.ARCHIVE_TIME.getPresentation());
        if (archiveTimeObject instanceof Integer) {
            return Long.valueOf((Integer) archiveTimeObject);
        } else {
            return (Long) archiveTimeObject;
        }
    }

    public String getVendorName() {
        return (String) properties.get(JsonPresentationFields.VENDOR_NAME.getPresentation());
    }

    public String getVendorRelease() {
        return (String) properties.get(JsonPresentationFields.VENDOR_RELEASE.getPresentation());
    }

    public String getResourceVendorModelNumber() {
        return (String) properties.get(JsonPresentationFields.RESOURCE_VENDOR_MODEL_NUMBER.getPresentation());
    }

    public Boolean isAbstract() {
        return (Boolean) properties.get(JsonPresentationFields.IS_ABSTRACT.getPresentation());
    }

    public ResourceTypeEnum getResourceType() {
        return ResourceTypeEnum.valueOf((String) properties.get(JsonPresentationFields.RESOURCE_TYPE.getPresentation()));
    }

    public String getToscaResourceName() {
        return (String) properties.get(JsonPresentationFields.TOSCA_RESOURCE_NAME.getPresentation());
    }

    public String getServiceType() {
        return (String) properties.get(JsonPresentationFields.SERVICE_TYPE.getPresentation());
    }

    public String getServiceRole() {
        return (String) properties.get(JsonPresentationFields.SERVICE_ROLE.getPresentation());
    }

    public String getServiceFunction() {
        return (String) properties.get(JsonPresentationFields.SERVICE_FUNCTION.getPresentation());
    }

    public String getModel() {
        return (String) properties.get(JsonPresentationFields.MODEL.getPresentation());
    }
    
    public Boolean isDeleteRestricted() {
        return (Boolean) properties.get(JsonPresentationFields.DELETE_RESTRICTED.getPresentation());
    }
}
