/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ComponentDependency {
    private String name;
    private String version;
    private String uniqueId;
    private String type;
    private String icon;
    private String state;
    private List<String> instanceNames;
    
    private List<ComponentDependency> dependencies;

    public void addDependencies(List<ComponentDependency> dependencies) {
        if ( this.dependencies == null ){
            this.dependencies = new ArrayList<>();
        }
        this.dependencies.addAll(dependencies);
   }

    public void addDependency(ComponentDependency dependency){
        if ( dependencies == null ){
            dependencies = new ArrayList<>();
        }
        dependencies.add(dependency);
    }
}
