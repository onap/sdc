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

package org.openecomp.sdc.be.ui.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UIConstraint implements Serializable {

    private String servicePropertyName;
    private String constraintOperator;
    private String sourceType;
    private String sourceName;
    private Object value;

    public UIConstraint() {
    }

    public UIConstraint(String servicePropertyName, String constraintOperator, String sourceType, Object value) {
        this.servicePropertyName = servicePropertyName;
        this.constraintOperator = constraintOperator;
        this.sourceType = sourceType;
        this.value = value;
    }

    public UIConstraint(String servicePropertyName, String constraintOperator, String sourceType, String sourceName,
                        Object value) {
        this.servicePropertyName = servicePropertyName;
        this.constraintOperator = constraintOperator;
        this.sourceType = sourceType;
        this.sourceName = sourceName;
        this.value = value;
    }

}
