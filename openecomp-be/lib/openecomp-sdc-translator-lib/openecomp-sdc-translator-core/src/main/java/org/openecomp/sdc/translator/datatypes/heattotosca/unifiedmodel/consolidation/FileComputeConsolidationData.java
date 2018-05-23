package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The type File compute consolidation data.
 */
public class FileComputeConsolidationData {

    //key - compute node type name (vm_type)
    private Map<String, TypeComputeConsolidationData> typeComputeConsolidationData;

    /**
     * Instantiates a new File compute consolidation data.
     */
    public FileComputeConsolidationData() {
        typeComputeConsolidationData = new HashMap<>();
    }

    /**
     * Gets all compute types.
     *
     * @return the all compute types
     */
    public Set<String> getAllComputeTypes() {
        return typeComputeConsolidationData.keySet();
    }

    /**
     * Gets all type compute consolidation data.
     *
     * @return the all type compute consolidation data
     */
    public Collection<TypeComputeConsolidationData> getAllTypeComputeConsolidationData() {
        return typeComputeConsolidationData.values();
    }

    /**
     * Gets type compute consolidation data.
     *
     * @param computeType the compute type
     * @return the type compute consolidation data
     */
    public TypeComputeConsolidationData getTypeComputeConsolidationData(String computeType) {
        return typeComputeConsolidationData.get(computeType);
    }

    /**
     * Sets type compute consolidation data.
     *
     * @param computeType                  the compute type
     * @param typeComputeConsolidationData the type compute consolidation data
     */
    public void setTypeComputeConsolidationData(String computeType,
                                                       TypeComputeConsolidationData typeComputeConsolidationData) {
        this.typeComputeConsolidationData.put(computeType, typeComputeConsolidationData);
    }

    /**
     * Is number of compute types legal boolean.
     *
     * @return the boolean
     */
    public boolean isNumberOfComputeTypesLegal() {
        return getAllTypeComputeConsolidationData().size() == 1;
    }

    /**
     * Is number of compute consolidation data per type legal boolean.
     *
     * @return the boolean
     */
    public boolean isNumberOfComputeConsolidationDataPerTypeLegal() {
        return getAllTypeComputeConsolidationData().iterator().next().isNumberOfComputeConsolidationDataPerTypeLegal();
    }
}
