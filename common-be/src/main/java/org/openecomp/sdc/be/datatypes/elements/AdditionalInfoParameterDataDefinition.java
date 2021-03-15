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

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AdditionalInfoParameterDataDefinition extends ToscaDataDefinition {

    private String uniqueId;
    private Long creationTime;
    private Long modificationTime;
    private Integer lastCreatedCounter = 0;

    @ToString.Exclude
    private List<AdditionalInfoParameterInfo> parameters;

    public AdditionalInfoParameterDataDefinition(AdditionalInfoParameterDataDefinition p) {
        this.uniqueId = p.uniqueId;
        this.creationTime = p.creationTime;
        this.modificationTime = p.modificationTime;
        this.lastCreatedCounter = p.lastCreatedCounter;
        this.parameters = p.parameters;
    }

}
