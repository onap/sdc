/*
 * Copyright © 2019 iconectiv
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

package org.openecomp.core.externaltesting.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.regex.Pattern;
import lombok.Data;

@Data
public class RemoteTestingEndpointDefinition {

    private boolean enabled;
    private String title;
    private String url;
    private String id;
    private String apiKey;
    private String scenarioFilter;

    // a compact way to specify and endpoint to ease docker configuration.
    @JsonIgnore
    private String config;

    private Pattern scenarioFilterPattern;

    @JsonIgnore
    public Pattern getScenarioFilterPattern() {
        if ((scenarioFilterPattern == null) && (scenarioFilter != null)) {
            scenarioFilterPattern = Pattern.compile(scenarioFilter);
        }
        return scenarioFilterPattern;
    }
}
