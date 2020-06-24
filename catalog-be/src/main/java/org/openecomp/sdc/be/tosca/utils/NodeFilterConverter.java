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

package org.openecomp.sdc.be.tosca.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.be.ui.model.UINodeFilter;

public class NodeFilterConverter {


    public Map<String, UINodeFilter> convertDataMapToUI(Map<String, CINodeFilterDataDefinition> inMap) {
        return inMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, o -> convertToUi(o.getValue())));
    }

    public UINodeFilter convertToUi(CINodeFilterDataDefinition inNodeFilter) {
        UINodeFilter retVal = new UINodeFilter();
        final ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        if (inNodeFilter.getProperties() == null || inNodeFilter.getProperties().isEmpty()) {
            return retVal;
        }
        List<UIConstraint> constraints = inNodeFilter.getProperties().getListToscaDataDefinition().stream()
            .map(property -> property.getConstraints().iterator().next())
            .map(constraintConvertor::convert)
            .collect(Collectors.toList());
        retVal.setProperties(constraints);
        return retVal;
    }
}
