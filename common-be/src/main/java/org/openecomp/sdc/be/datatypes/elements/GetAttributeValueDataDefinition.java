/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.datatypes.elements;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class GetAttributeValueDataDefinition extends ToscaDataDefinition {

    private String propName;
    private String outputName;
    private String outputId;
    private Integer indexValue;
    private GetAttributeValueDataDefinition getAttributeIndex;

    private boolean isList = false;

    public GetAttributeValueDataDefinition(Map<String, Object> pr) {
        super(pr);
    }

    public GetAttributeValueDataDefinition(GetAttributeValueDataDefinition p) {
        this.setPropName(p.getPropName());
        this.setOutputName(p.getOutputName());
        this.setOutputId(p.getOutputId());
        this.setIndexValue(p.getIndexValue());
        this.setGetAttributeIndex(p.getGetAttributeIndex());
        this.setList(p.isList());
    }

}
