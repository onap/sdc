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
package org.onap.sdc.tosca.datatypes.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Constraint implements Cloneable {

    private Object equal;
    private Object greater_or_equal;
    private Object greater_than;
    private Object less_than;
    private Object less_or_equal;
    private Object[] in_range;
    private List<Object> valid_values;
    private Integer length;
    private Integer min_length;
    private Integer max_length;
    private Object pattern;

    /**
     * Sets in_range attribute.
     *
     * @param in_range.
     */
    public void setIn_range(Object[] in_range) {
        this.in_range = new Object[2];
        this.in_range[0] = in_range[0];
        this.in_range[1] = in_range[1];
    }

    @Override
    public Constraint clone() {
        Constraint constraint = new Constraint();
        constraint.setEqual(this.getEqual());
        constraint.setGreater_or_equal(this.getGreater_or_equal());
        constraint.setGreater_than(this.getGreater_than());
        cloneInRange(constraint);
        constraint.setLength(this.getLength());
        constraint.setLess_or_equal(this.getLess_or_equal());
        constraint.setLess_than(this.getLess_than());
        constraint.setMax_length(this.getMax_length());
        constraint.setMin_length(this.getMin_length());
        constraint.setPattern(this.getPattern());
        cloneValidValues(constraint);
        return constraint;
    }

    private void cloneInRange(Constraint constraint) {
        if (this.getIn_range() != null) {
            constraint.setIn_range(new Object[]{this.getIn_range()[0], this.getIn_range()[1]});
        }
    }

    private void cloneValidValues(Constraint constraint) {
        if (this.getValid_values() != null) {
            constraint.setValid_values(new ArrayList<>());
            for (Object entry : this.getValid_values()) {
                constraint.getValid_values().add(entry);
            }
        }
    }
}
