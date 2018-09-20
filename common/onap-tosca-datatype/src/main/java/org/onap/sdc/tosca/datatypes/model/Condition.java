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

package org.onap.sdc.tosca.datatypes.model;


import java.util.Map;
import java.util.Objects;

public class Condition {

    private Map<String,Constraint> constraint;
    private String period;
    private Integer evaluations;
    private String method;


    public Map<String, Constraint> getConstraint() {
        return constraint;
    }

    public void setConstraint(Map<String, Constraint> constraint) {
        this.constraint = constraint;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Integer getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(Integer evaluations) {
        this.evaluations = evaluations;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Condition)) {
            return false;
        }
        Condition condition = (Condition) o;
        return Objects.equals(getConstraint(), condition.getConstraint()) && Objects.equals(getPeriod(),
                condition.getPeriod()) && Objects.equals(getEvaluations(), condition.getEvaluations())
                       && Objects.equals(getMethod(), condition.getMethod());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConstraint(), getPeriod(), getEvaluations(), getMethod());
    }
}
