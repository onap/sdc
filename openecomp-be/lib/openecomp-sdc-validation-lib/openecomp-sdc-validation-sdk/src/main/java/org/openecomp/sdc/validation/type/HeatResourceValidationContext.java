/*
 * Copyright © 2018 European Support Limited
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
package org.openecomp.sdc.validation.type;

import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.validation.ValidationContext;

/**
 * Created by TALIO on 2/23/2017.
 */
@Getter
@EqualsAndHashCode
public class HeatResourceValidationContext implements ValidationContext {

    private HeatOrchestrationTemplate heatOrchestrationTemplate;
    // key - resource type, value - map with key = resource id and

    // value = map with key = pointing / pointed resource type and

    // value = pointing / pointed resource id
    private Map<String, Map<String, Map<String, List<String>>>> fileLevelResourceDependencies;
    private String envFileName;

    public HeatResourceValidationContext(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                         Map<String, Map<String, Map<String, List<String>>>> fileLevelResourceDependencies, String envFileName) {
        this.heatOrchestrationTemplate = heatOrchestrationTemplate;
        this.fileLevelResourceDependencies = fileLevelResourceDependencies;
        this.envFileName = envFileName;
    }
}
