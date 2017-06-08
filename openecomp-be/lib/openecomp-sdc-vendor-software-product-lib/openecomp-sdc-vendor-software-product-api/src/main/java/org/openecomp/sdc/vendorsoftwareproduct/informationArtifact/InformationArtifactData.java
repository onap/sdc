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


import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.ComponentQuestionnaire;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.nic.NicQuestionnaire;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.vsp.VspQuestionnaire;

import java.util.List;

/**
 * Created by TALIO on 11/22/2016
 */
public class InformationArtifactData {
    private VspDetails vspDetails;
    private VspQuestionnaire vspQuestionnaire;
    private List<ComponentQuestionnaire> componentQuestionnaires;
    private List<NicQuestionnaire> nicQuestionnaires;


    public InformationArtifactData(VspDetails vspDetails, VspQuestionnaire vspQuestionnaire, List<ComponentQuestionnaire> componentQuestionnaires, List<NicQuestionnaire> nicQuestionnaires) {
        this.vspDetails = vspDetails;
        this.vspQuestionnaire = vspQuestionnaire;
        this.componentQuestionnaires = componentQuestionnaires;
        this.nicQuestionnaires = nicQuestionnaires;
    }

  public InformationArtifactData() {

  }

  public VspQuestionnaire getVspQuestionnaire() {
        return vspQuestionnaire;
    }

    public void setVspQuestionnaire(VspQuestionnaire vspQuestionnaire) {
        this.vspQuestionnaire = vspQuestionnaire;
    }

    public List<ComponentQuestionnaire> getComponentQuestionnaires() {
        return componentQuestionnaires;
    }

    public void setComponentQuestionnaires(List<ComponentQuestionnaire> componentQuestionnaires) {
        this.componentQuestionnaires = componentQuestionnaires;
    }

    public List<NicQuestionnaire> getNicQuestionnaires() {
        return nicQuestionnaires;
    }

    public void setNicQuestionnaires(List<NicQuestionnaire> nicQuestionnaires) {
        this.nicQuestionnaires = nicQuestionnaires;
    }

    public VspDetails getVspDetails() {
        return vspDetails;
    }

    public void setVspDetails(VspDetails vspDetails) {
        this.vspDetails = vspDetails;
    }
}
