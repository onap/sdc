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


import java.util.List;

import java.util.Map;
import org.onap.sdc.tosca.datatypes.model.Constraint;
import org.onap.sdc.tosca.services.DataModelNormalizeUtil;

public class SubstitutionFilter {

    private List<Map<String, List<Constraint>>> properties;

    public List<Map<String, List<Constraint>>> getProperties() {
        return properties;
    }

    public void setProperties(List<Map<String, List<Constraint>>> properties) {
        this.properties = DataModelNormalizeUtil.getNormalizePropertiesFilter(properties);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubstitutionFilter)) {
            return false;
        }

        SubstitutionFilter that = (SubstitutionFilter) o;

        return getProperties() != null ? getProperties().equals(that.getProperties()) : that.getProperties() == null;
    }

    @Override
    public int hashCode() {
        return getProperties() != null ? getProperties().hashCode() : 0;
    }
}
