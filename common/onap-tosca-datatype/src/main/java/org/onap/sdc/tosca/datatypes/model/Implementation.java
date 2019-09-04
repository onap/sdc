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
import java.util.Objects;

import org.onap.sdc.tosca.services.DataModelCloneUtil;


public class Implementation implements Cloneable {

    private String primary;
    private List<String> dependencies;

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Implementation)) {
            return false;
        }
        Implementation that = (Implementation) o;
        return Objects.equals(primary, that.primary)
                && Objects.equals(dependencies, that.dependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primary, dependencies);
    }

    @Override
    public Implementation clone() {
        Implementation implementation = new Implementation();
        implementation.setPrimary(this.getPrimary());
        implementation.setDependencies(DataModelCloneUtil.cloneListString(this.getDependencies()));
        return implementation;
    }
}
