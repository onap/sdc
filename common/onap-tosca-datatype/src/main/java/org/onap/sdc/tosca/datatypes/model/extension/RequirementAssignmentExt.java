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

import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;

public class RequirementAssignmentExt extends RequirementAssignment {

    private ServiceFilter service_filter;

    public ServiceFilter getService_filter() {
        return service_filter;
    }

    public void setService_filter(ServiceFilter serviceFilter) {
        this.service_filter = serviceFilter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RequirementAssignmentExt)) {
            return false;
        }

        RequirementAssignmentExt that = (RequirementAssignmentExt) o;

        return getService_filter() != null ? getService_filter().equals(that.getService_filter()) :
                       that.getService_filter() == null;
    }

    @Override
    public int hashCode() {
        return getService_filter() != null ? getService_filter().hashCode() : 0;
    }
}
