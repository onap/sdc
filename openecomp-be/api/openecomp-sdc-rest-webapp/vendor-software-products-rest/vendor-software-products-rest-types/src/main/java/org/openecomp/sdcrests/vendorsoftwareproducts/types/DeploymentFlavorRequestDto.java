/*
* Copyright © 2016-2018 European Support Limited
*
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
*/

package org.openecomp.sdcrests.vendorsoftwareproducts.types;


import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentComputeAssociation;

import javax.validation.constraints.Size;
import java.util.List;

@Data
public class DeploymentFlavorRequestDto {
    @NotBlank(message = "is mandatory and should not be empty")
    @Size(min = 0, max = 30, message = "length should not exceed 30 characters.")
    private String model;
    @Size(min = 0, max = 300, message = "length should not exceed 300 characters.")
    private String description;
    private String featureGroupId;
    private List<ComponentComputeAssociation> componentComputeAssociations;

}
