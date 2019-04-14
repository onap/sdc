package org.openecomp.sdc.be.model;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertiesOwner;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
/**
 * public class representing the component policy
 */
public class PolicyDefinition extends PolicyDataDefinition implements PropertiesOwner {

    /**
     * public constructor by default
     */
    public PolicyDefinition() {
        super();
    }

    public PolicyDefinition(PropertyDataDefinition propertyDataDefinition) {
        super(propertyDataDefinition);
    }

    /**
     * public constructor from superclass
     * @param policy
     */
    public PolicyDefinition(Map<String, Object> policy) {
        super(policy);
    }

    /**
     * public copy constructor
     * @param other
     */
    public PolicyDefinition(PolicyDataDefinition other) {
        super(other);
    }

    /**
     * public converter constructor
     * builds PolicyDefinition object based on received PolicyTypeDefinition object
     * @param policyType
     */
    public PolicyDefinition(PolicyTypeDefinition policyType) {
        this.setPolicyTypeName(policyType.getType());
        this.setPolicyTypeUid(policyType.getUniqueId());
        this.setDerivedFrom(policyType.getDerivedFrom());
        this.setDescription(policyType.getDescription());
        this.setVersion(policyType.getVersion());
        if (policyType.getProperties() != null) {
            this.setProperties(policyType.getProperties().stream().map(PropertyDataDefinition::new).collect(Collectors.toList()));
        }
        this.setTargets(new HashMap<>());

    }

    @Override
    public String getNormalizedName() {
        return getName();
    }
}
