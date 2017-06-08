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

package org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.highAvailability;

/**
 * Created by TALIO on 11/22/2016.
 */
public class HighAvailabilityAndLoadBalancing {
    private String failureLoadDistribution;
    private String nkModelImplementation;
    private String architectureChoice;
    private String slaRequirements;
    private String horizontalScaling;
    private String loadDistributionMechanism;
    private String isComponentMandatory;
    private String highAvailabilityMode;

    public String getFailureLoadDistribution() {
        return failureLoadDistribution;
    }

    public void setFailureLoadDistribution(String failureLoadDistribution) {
        this.failureLoadDistribution = failureLoadDistribution;
    }

    public String getNkModelImplementation() {
        return nkModelImplementation;
    }

    public void setNkModelImplementation(String nkModelImplementation) {
        this.nkModelImplementation = nkModelImplementation;
    }

    public String getArchitectureChoice() {
        return architectureChoice;
    }

    public void setArchitectureChoice(String architectureChoice) {
        this.architectureChoice = architectureChoice;
    }

    public String getSlaRequirements() {
        return slaRequirements;
    }

    public void setSlaRequirements(String slaRequirements) {
        this.slaRequirements = slaRequirements;
    }

    public String getHorizontalScaling() {
        return horizontalScaling;
    }

    public void setHorizontalScaling(String horizontalScaling) {
        this.horizontalScaling = horizontalScaling;
    }

    public String getLoadDistributionMechanism() {
        return loadDistributionMechanism;
    }

    public void setLoadDistributionMechanism(String loadDistributionMechanism) {
        this.loadDistributionMechanism = loadDistributionMechanism;
    }

    public String getIsComponentMandatory() {
        return isComponentMandatory;
    }

    public void setIsComponentMandatory(String isComponentMandatory) {
        this.isComponentMandatory = isComponentMandatory;
    }

    public String getHighAvailabilityMode() {
        return highAvailabilityMode;
    }

    public void setHighAvailabilityMode(String highAvailabilityMode) {
        this.highAvailabilityMode = highAvailabilityMode;
    }

}
