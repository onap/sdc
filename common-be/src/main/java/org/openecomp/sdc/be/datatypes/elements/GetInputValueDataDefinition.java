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

package org.openecomp.sdc.be.datatypes.elements;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Map;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class GetInputValueDataDefinition extends ToscaDataDefinition {

    private String propName;
    private String inputName;
    private String inputType;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private String inputId;
    private Integer indexValue;
    private GetInputValueDataDefinition getInputIndex;

    private boolean isList = false;

    public GetInputValueDataDefinition() {
        super();
    }

    public GetInputValueDataDefinition(Map<String, Object> pr) {
        super(pr);
    }

    public GetInputValueDataDefinition(GetInputValueDataDefinition p) {
        this.setPropName(p.getPropName());
        this.setInputName(p.getInputName());
        this.setInputId(p.getInputId());
        this.setIndexValue(p.getIndexValue());
        this.setGetInputIndex(p.getGetInputIndex());
        this.setList(p.isList());
    }
}
