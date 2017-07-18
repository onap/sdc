package org.openecomp.sdc.vendorsoftwareproduct.types.composition;


public class ComponentComputeAssociation {
    private String componentId;
    private String computeFlavorId;

    public String getComponentId() {
        return componentId;
    }
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }
    public String getComputeFlavorId() {
        return computeFlavorId;
    }

    public void setComputeFlavorId(String computeFlavorId) {
        this.computeFlavorId = computeFlavorId;
    }

}
