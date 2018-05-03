/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.sdc.activityspec.api.rest.types;

import io.swagger.annotations.ApiModel;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import java.util.List;

@ApiModel(value = "ActivitySpecRequest")
@lombok.Data
public class ActivitySpecRequestDto {

    @NotBlank(message = "Mandatory %s field is missing")
    @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "%s should match with \"^[a-zA-Z0-9-]*$\" pattern")
    private String name;
    private String description;

    private List<String> categoryList;
    private List<ActivitySpecParameterDto> inputs;
    private List<ActivitySpecParameterDto> outputs;
    private String type;
    private String content;
}
