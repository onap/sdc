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

package org.openecomp.sdc.ci.tests.datatypes.enums;

public enum MandatoryServiceArtifactTypeEnum {

	MESSAGE_FLOWS(null, "messageflows".toLowerCase(), "Message Flows"), 
	INSTANT_FLOWS(null, "instantiationflows".toLowerCase(), "Management Flows"), 
	SERVICE_ART_PLAN(null, "serviceartifactplan".toLowerCase(), "Service Artifact Plan"), 
	SUM_OF_ELEMENTS(null, "summaryofimpactstoecompelements".toLowerCase(), "Summary of impacts to ECOMP elements,OSSs, BSSs"), 
	CONTROL_LOOP_FUN(null, "controlloopfunctions".toLowerCase(), "Control Loop Functions"), 
	DIMENSIONNING_INFO(null, "dimensioninginfo".toLowerCase(), "Dimensioning Info"), 
	AFFINITY_RULES(null, "affinityrules".toLowerCase(), "Affinity Rules"), 
	OPERATIONAL_POLICIES(null, "operationalpolicies".toLowerCase(), "Operational Policies"), 
	SERVICE_SPECIFIC_POLICIES(null, "servicespecificpolicies".toLowerCase(), "Service-specific Policies"), 
	ENGINEERING_RULES(null, "engineeringrules".toLowerCase(), "Engineering Rules (ERD)"), 
	DISTRIB_INSTRUCTIONS(null, "distributioninstructions".toLowerCase(), "Distribution Instructions"), 
	DEPLOYMENT_VOTING_REC(null, "deploymentvotingrecord".toLowerCase(), "Deployment Voting Record"), 
	CERTIFICATION_TEST_RESULT(null, "certificationtestresults".toLowerCase(), "TD Certification Test Results");
	// SERVICE_QUESTIONNAIRE(null, "serviceQuestionnaire".toLowerCase());

	String artifactName;
	String logicalName;
	String artifactDisplayName;

	private MandatoryServiceArtifactTypeEnum(String artifactName, String logicalName, String artifactDisplayName) {
		this.artifactName = artifactName;
		this.logicalName = logicalName;
		this.artifactDisplayName = artifactDisplayName;
	}

	public String getArtifactName() {
		return artifactName;
	}

	public String getLogicalName() {
		return logicalName;
	}

	public String getArtifactDisplayName() {
		return artifactDisplayName;
	}

	public void setArtifactDisplayName(String artifactDisplayName) {
		this.artifactDisplayName = artifactDisplayName;
	}

}
