package org.openecomp.sdc.be.datatypes.elements;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;

/**
 * public class representing the component policy,
 * described by the next properties:
 * <p>
 * name
 * uniqueId
 * type (policy type name)
 * typeUid (policy type uniqueId)
 * version (version)
 * derivedFrom (policy type derivedFrom)
 * description
 * policyUUID
 * invariantUUID
 * members
 * metadata
 * properties
 * targets
 * isFromCsar
 */
public class PolicyDataDefinition extends PropertyDataDefinition {

    /**
     * public constructor by default
     */
    public PolicyDataDefinition() {
        super();
    }

    public PolicyDataDefinition(PropertyDataDefinition propertyDataDefinition) {
        super(propertyDataDefinition);
    }

    /**
     * public constructor from superclass
     *
     * @param policy
     */
    public PolicyDataDefinition(Map<String, Object> policy) {
        super(policy);
    }

    /**
     * public copy constructor
     *
     * @param other
     */
    public PolicyDataDefinition(PolicyDataDefinition other) {
        super(other);
        this.setName(other.getName());
        this.setUniqueId(other.getUniqueId());
        this.setPolicyTypeName(other.getPolicyTypeName());
        this.setPolicyTypeUid(other.getPolicyTypeUid());
        this.setVersion(other.getVersion());
        this.setDerivedFrom(other.getDerivedFrom());
        this.setDescription(other.getDescription());
        this.setPolicyUUID(other.getPolicyUUID());
        this.setInvariantUUID(other.getInvariantUUID());
        this.setInvariantName(other.getInvariantName());
        this.setComponentName(other.getComponentName());
        this.setIsFromCsar(other.getIsFromCsar());
        this.setValue(other.getValue());
        this.setOwnerId(other.getOwnerId());
        this.setType(other.getType());
        this.setInstanceUniqueId(other.getInstanceUniqueId());
        this.setInputPath(other.getInputPath());
        if (other.getProperties() != null) {
            this.setProperties(other.getProperties());
        }
        if (other.getTargets() != null) {
            this.setTargets(other.getTargets());
        }
    }

    private void setIsFromCsar(Boolean isFromCsar) {
        if (isFromCsar == null) {
            setToscaPresentationValue(JsonPresentationFields.IS_FROM_CSAR, false);
        } else {
            setToscaPresentationValue(JsonPresentationFields.IS_FROM_CSAR, isFromCsar);
        }
    }

    public Boolean getIsFromCsar() {
        Boolean isFromCsar = (Boolean) getToscaPresentationValue(JsonPresentationFields.IS_FROM_CSAR);
        return (isFromCsar != null) ? isFromCsar : false;
    }

    public String getComponentName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_NAME);
    }

    public void setComponentName(String componentName) {
        setToscaPresentationValue(JsonPresentationFields.CI_COMPONENT_NAME, componentName);
    }

    public String getInvariantName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.CI_INVARIANT_NAME);
    }

    public void setInvariantName(Object invariantName) {
        setToscaPresentationValue(JsonPresentationFields.CI_INVARIANT_NAME, invariantName);
    }

    public String getPolicyTypeName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.TYPE);
    }

    public void setPolicyTypeName(String policyTypeName) {
        setToscaPresentationValue(JsonPresentationFields.TYPE, policyTypeName);
    }

    public String getPolicyTypeUid() {
        return (String) getToscaPresentationValue(JsonPresentationFields.TYPE_UNIQUE_ID);
    }

    public void setPolicyTypeUid(String policyTypeUid) {
        setToscaPresentationValue(JsonPresentationFields.TYPE_UNIQUE_ID, policyTypeUid);
    }

    public String getVersion() {
        return (String) getToscaPresentationValue(JsonPresentationFields.VERSION);
    }

    public void setVersion(String version) {
        setToscaPresentationValue(JsonPresentationFields.VERSION, version);
    }

    public String getDerivedFrom() {
        return (String) getToscaPresentationValue(JsonPresentationFields.DERIVED_FROM);
    }

    public void setDerivedFrom(String derivedFrom) {
        setToscaPresentationValue(JsonPresentationFields.DERIVED_FROM, derivedFrom);
    }

    public String getDescription() {
        return (String) getToscaPresentationValue(JsonPresentationFields.DESCRIPTION);
    }

    public void setDescription(String description) {
        setToscaPresentationValue(JsonPresentationFields.DESCRIPTION, description);
    }

    public String getPolicyUUID() {
        return (String) getToscaPresentationValue(JsonPresentationFields.UUID);
    }

    public void setPolicyUUID(String policyUUID) {
        setToscaPresentationValue(JsonPresentationFields.UUID, policyUUID);
    }

    public String getInvariantUUID() {
        return (String) getToscaPresentationValue(JsonPresentationFields.INVARIANT_UUID);
    }

    public void setInvariantUUID(String invariantUUID) {
        setToscaPresentationValue(JsonPresentationFields.INVARIANT_UUID, invariantUUID);
    }

    @SuppressWarnings("unchecked")
    public List<PropertyDataDefinition> getProperties() {
        return (List<PropertyDataDefinition>) getToscaPresentationValue(JsonPresentationFields.PROPERTIES);
    }

    public void setProperties(List<PropertyDataDefinition> properties) {
        setToscaPresentationValue(JsonPresentationFields.PROPERTIES, properties);
    }

    @SuppressWarnings("unchecked")
    public Map<PolicyTargetType, List<String>> getTargets() {
        return (Map<PolicyTargetType, List<String>>) getToscaPresentationValue(JsonPresentationFields.TARGETS);
    }

    public void setTargets(Map<PolicyTargetType, List<String>> metadata) {
        setToscaPresentationValue(JsonPresentationFields.TARGETS, metadata);
    }

    public List<String> resolveComponentInstanceTargets() {
        return resolveTargetsByType(PolicyTargetType.COMPONENT_INSTANCES);
    }

    public List<String> resolveGroupTargets() {
        return resolveTargetsByType(PolicyTargetType.GROUPS);
    }

    public boolean containsTarget(String targetId, PolicyTargetType policyTargetType) {
        return resolveTargetsByType(policyTargetType).contains(targetId);
    }

    private List<String> resolveTargetsByType(PolicyTargetType targetType) {
        Map<PolicyTargetType, List<String>> targets = getTargets();
        return targets == null || !targets.containsKey(targetType) ? emptyList() : targets.get(targetType);
    }

}
