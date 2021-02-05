/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class GetOutputValueDataDefinition extends ToscaDataDefinition {

    private String attribName;
    private String outputName;
    private String outputId;
    private Integer indexValue;
    private GetOutputValueDataDefinition getOutputIndex;
    private boolean isList;

    public GetOutputValueDataDefinition() {
        super();
    }

    public GetOutputValueDataDefinition(Map<String, Object> pr) {
        super(pr);
    }

    public GetOutputValueDataDefinition(GetOutputValueDataDefinition p) {
        this.setAttribName(p.getAttribName());
        this.setOutputName(p.getOutputName());
        this.setOutputId(p.getOutputId());
        this.setIndexValue(p.getIndexValue());
        this.setGetOutputIndex(p.getGetOutputIndex());
        this.setList(p.isList());
    }

}
