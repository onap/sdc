package org.openecomp.sdc.be.datatypes.enums;

import java.util.List;
import java.util.Map;

public class JsonPresentationFieldsExtractor {

    private Map<String, Object> properties;

    public JsonPresentationFieldsExtractor(Map<String, Object> properties) {
        this.properties = properties;
    }

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
        return ResourceTypeEnum.valueOf((String)properties.get(JsonPresentationFields.RESOURCE_TYPE.getPresentation()));
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

}
