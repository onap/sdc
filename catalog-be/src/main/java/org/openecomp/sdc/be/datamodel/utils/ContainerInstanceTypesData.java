package org.openecomp.sdc.be.datamodel.utils;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ContainerInstanceTypesData {

    private List<ResourceTypeEnum> validInstanceTypesInServiceContainer = Arrays.asList(ResourceTypeEnum.PNF,
            ResourceTypeEnum.VF, ResourceTypeEnum.CP,
            ResourceTypeEnum.VL, ResourceTypeEnum.CR,
            ResourceTypeEnum.ServiceProxy,ResourceTypeEnum.Configuration);
    private Map<ResourceTypeEnum,List<ResourceTypeEnum>> validInstanceTypesInResourceContainer = new HashMap<>();

    private ContainerInstanceTypesData() {
        List<ResourceTypeEnum> vfContainerInstances = Arrays.asList(ResourceTypeEnum.CP,
                ResourceTypeEnum.VL, ResourceTypeEnum.Configuration,
                ResourceTypeEnum.VFC);
        List<ResourceTypeEnum> crContainerInstances = Arrays.asList(ResourceTypeEnum.CP,
                ResourceTypeEnum.VL, ResourceTypeEnum.Configuration,
                ResourceTypeEnum.VFC);
        List<ResourceTypeEnum> pnfContainerInstances = Arrays.asList(ResourceTypeEnum.CP,
                ResourceTypeEnum.VL, ResourceTypeEnum.Configuration,
                ResourceTypeEnum.VFC);
        validInstanceTypesInResourceContainer.put(ResourceTypeEnum.VF, vfContainerInstances);
        validInstanceTypesInResourceContainer.put(ResourceTypeEnum.CR, crContainerInstances);
        validInstanceTypesInResourceContainer.put(ResourceTypeEnum.PNF, pnfContainerInstances);
    }

    public List<ResourceTypeEnum> getServiceContainerList() {
        return validInstanceTypesInServiceContainer;
    }

    public Map<ResourceTypeEnum, List<ResourceTypeEnum>> getValidInstanceTypesInResourceContainer() {
        return validInstanceTypesInResourceContainer;
    }
}
