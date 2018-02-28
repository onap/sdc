package org.openecomp.sdc.be.dto;

import org.openecomp.sdc.be.model.PolicyTypeDefinition;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PolicyTypesDTO {

    private List<PolicyTypeDefinition> policyTypes;
    private Map<String, Set<String>> excludeMapping;

    public PolicyTypesDTO() {
        /*
        empty constructor for serialization
         */
    }

    public PolicyTypesDTO(List<PolicyTypeDefinition> policyTypes, Map<String, Set<String>> excludeMapping) {
        this.policyTypes = policyTypes;
        this.excludeMapping = excludeMapping;
    }

    public List<PolicyTypeDefinition> getPolicyTypes() {
        return policyTypes;
    }

    public Map<String, Set<String>> getExcludeMapping() {
        return excludeMapping;
    }
}
