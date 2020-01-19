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
