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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;

/**
 * Created by TALIO on 4/25/2016.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "VspDetails")
public class VspDetailsDto extends VspRequestDto {

    private String id;
    private String version;
    private ValidationStructureList validationData;
    private String candidateOnboardingOrigin;
    private String onboardingOrigin;
    private String networkPackageName;
    private String owner;
    private String status;
    private String tenant;
}
