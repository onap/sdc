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

package org.onap.sdc.tosca.datatypes.model.extension;

import org.onap.sdc.tosca.datatypes.model.SubstitutionMapping;

public class SubstitutionMappingExt extends SubstitutionMapping {

    private SubstitutionFilter substitution_filter;

    public SubstitutionFilter getSubstitution_filter() {
        return substitution_filter;
    }

    public void setSubstitution_filter(SubstitutionFilter substitution_filter) {
        this.substitution_filter = substitution_filter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubstitutionMappingExt)) {
            return false;
        }

        SubstitutionMappingExt that = (SubstitutionMappingExt) o;

        return getSubstitution_filter() != null ? getSubstitution_filter().equals(that.getSubstitution_filter())
                : that.getSubstitution_filter() == null;
    }

    @Override
    public int hashCode() {
        return getSubstitution_filter() != null ? getSubstitution_filter().hashCode() : 0;
    }
}
