package org.openecomp.sdc.be.dto;

import org.openecomp.sdc.be.model.GroupTypeDefinition;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupTypesDTO {
    private List<GroupTypeDefinition> groupTypes;
    private Map<String, Set<String>> excludeMapping;

    public GroupTypesDTO() {
    /**
     * empty constructor for serialization
     */
    }

    public GroupTypesDTO(List<GroupTypeDefinition> groupTypes, Map<String, Set<String>> excludeMapping) {
        this.groupTypes = groupTypes;
        this.excludeMapping = excludeMapping;
    }

    public List<GroupTypeDefinition> getGroupTypes() {
        return groupTypes;
    }

    public Map<String, Set<String>> getExcludeMapping() {
        return excludeMapping;
    }
}
