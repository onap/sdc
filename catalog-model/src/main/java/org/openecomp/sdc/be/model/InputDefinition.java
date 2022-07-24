/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

@Getter
@Setter
@EqualsAndHashCode
public class InputDefinition extends PropertyDefinition {

    private List<ComponentInstanceInput> inputs;
    private List<ComponentInstanceProperty> properties;

    public InputDefinition(PropertyDataDefinition p) {
        super(p);
        setValue(null);
        setDefaultValue(null);
    }

    public InputDefinition() {
        super();
        setValue(null);
        setDefaultValue(null);
    }

    public InputDefinition(PropertyDefinition pd) {
        super(pd);
        setValue(null);
        setDefaultValue(null);
    }

    public InputDefinition(InputDefinition other) {
        super(other);
        setValue(null);
        setDefaultValue(null);
    }

    public void setAnnotationsToInput(Collection<Annotation> annotations) {
        this.setAnnotations(new ArrayList<>(annotations));
    }
}
