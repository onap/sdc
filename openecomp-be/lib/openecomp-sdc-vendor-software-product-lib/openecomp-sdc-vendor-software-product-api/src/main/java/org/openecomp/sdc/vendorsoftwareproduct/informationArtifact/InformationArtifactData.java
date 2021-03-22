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
package org.openecomp.sdc.vendorsoftwareproduct.informationArtifact;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.ComponentQuestionnaire;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.nic.NicQuestionnaire;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.vsp.VspQuestionnaire;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InformationArtifactData {

    private VspDetails vspDetails;
    private VspQuestionnaire vspQuestionnaire;
    private List<ComponentQuestionnaire> componentQuestionnaires;
    private List<NicQuestionnaire> nicQuestionnaires;
}
