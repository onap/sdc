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
 * Modifications copyright (c) 2019 Nokia
 */
package org.onap.sdc.tosca.datatypes.model;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.onap.sdc.tosca.services.DataModelCloneUtil;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Implementation implements Cloneable {

    private String primary;
    private List<String> dependencies;

    @Override
    public Implementation clone() {
        Implementation implementation = new Implementation();
        implementation.setPrimary(this.getPrimary());
        implementation.setDependencies(DataModelCloneUtil.cloneListString(this.getDependencies()));
        return implementation;
    }
}
