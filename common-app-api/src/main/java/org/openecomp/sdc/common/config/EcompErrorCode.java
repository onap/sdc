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

package org.openecomp.sdc.common.config;

public enum EcompErrorCode {

	E_100("Authentication problem towards U-EB server. Reason: %s",
			"An Authentication failure occured during access to UEB server. Please check that UEB keys are configured correctly in ASDC BE distribution configuration."), E_199(
					"Internal authentication problem. Description: %s"),

	E_200("ASDC Backend probably lost connectivity to either one of the following components: JanusGraph DB, Cassandra, Onboarding, UEB Cluster. Please check the logs for more information."), E_201(
			"ASDC Backend probably lost connectivity to JanusGraph DB. Please check the logs for more information."), E_202(
					"ASDC Backend probably lost connectivity to ElasticSearch. Please check the logs for more information."), E_203(
							"ASDC Backend probably lost connectivity to UEB Cluster. Please check the logs for more information.",
							"Check connectivity to UEB cluster which is configured under parameter uebServers in distribution-configuration.yaml."), E_204(
									"Unable to connect to a valid ASDC Backend Server",
									"Please check connectivity from this FE instance towards BE or BE Load Balancer. Please check that parameters in FE configuration.yaml: beHost,     beHttpPort and beSslPort point to a valid host and port values."),

	E_205("ASDC Backend Recovery to either one of the following components: JanusGraph DB, Cassandra, Onboarding, UEB Cluster."), E_206(
			"ASDC Backend connection recovery to JanusGraph DB."), E_207(
					"ASDC Backend connection recovery to ElasticSearch."), E_208(
							"ASDC Backend connection recovery to UEB Cluster."), E_209(
									"Connectivity to ASDC BE Server is recovered."), E_210(
											"Connection problem towards U-EB server. Reason: %s",
											"Please check that that parameter uebServers in distribution-configuration.yaml points to a valid UEB Cluster."), E_211(
													"Connection problem towards U-EB server. Cannot reach host %s",
													"Please check that that parameter uebServers in distribution-configuration.yaml points to a valid UEB Cluster."), E_212(
															"Couldn't resolve hostIP. Desciption: %s"), E_213(
																	"Site switch over was done. Site is now in %s mode"), E_214(
																				"Dmaap Connection problem."), E_299(
																								"Internal Connection problem. Description: %s"),

	// [resource/service/product]
	E_300("Mandatory %s Component %s cannot be found in repository"),
	// [SERVICE/RESOURCE/PRODUCT] [id] is not valid. Cannot be found in graph.
	E_301("%s Component %s is not valid. Cannot be found in graph."), E_302(
			"Configuration parameter %s is invalid. Value configured is %s."), E_303(
					"Error occured during access to U-EB Server. Data not found: %s",
					"An error occured during access to UEB Server, ASDC failed to either register or unregister to/from UEB topic."), E_304(
							"The artifact type %s does not appear in the list of valid artifacts %s"), E_305(
									"Configuration parameter %s is missing"), E_306(
											"Configuration parameter %s is invalid. At least %s values shall be configured"), E_307(
													"Invalid configuration in YAML file. %s"), E_308(
															"Artifact uploaded has missing information. Missing %s"), E_309(
																	"Artifact %s requested is not found"), E_310(
																			"User %s requested is not found"), E_311(
																					"Ecomp error description params mismatch between code and YAML or wrong format, name: %s"), E_312(
																							"Ecomp error element  not found in YAML, name: %s"),

	E_399("Internal Invalid Object. Description: %s"),

	E_400("The type %s of %s is invalid"), E_401("The value %s of %s from type %s is invalid"), E_402(
			"Payload of artifact uploaded is invalid (invalid MD5 or encryption)"), E_403(
					"Input for artifact metadata is invalid"), E_404("%s %s required is missing"), E_405(
							"Failed to convert json input to object"), E_406("Distribution %s required is missing"),

	E_499("Invalid input. Description: %s"),

	E_500("Catalog-BE was not initialized properly"), E_501(
			"Failed to add resource instance of resource %s to service %s"), E_502(
					"Error occured during access to U-EB Server. Operation: %s",
					"An error occured in ASDC distribution mechanism. Please check the logs for more information."), E_503(
							"Error occured in Distribution Engine. Failed operation: %s",
							"System Error occured in ASDC Distribution Engine. Please check ASDC logs for more details."), E_504(
									"Failed adding node of type %s to graph."), E_505(
											"Operation towards database failed.",
											"Please check JanusGraph DB health or look at the logs for more details."), E_506(
													"Unexpected error during operation"), E_507(
															"Going to execute rollback on graph."), E_508(
																	"Failed to lock object for update. Type = %s, Id = %s"), E_509(
																			"Failed to create node %s on graph. status is %s"), E_510(
																					"Failed to update node %s on graph. Status is %s"), E_511(
																							"Failed to delete node %s on graph. Status is %s"), E_512(
																									"Failed to retrieve node %s from graph. Status is %s"), E_513(
																											"Failed to find parent node of %s on graph. Status is %s"), E_514(
																													"Failed to fetch all nodes with type %s of parent node %s . Status is %s"), E_515(
																															"Cannot find node with type %s associated with node %s . Status is %s"), E_516(
																																	"Error occured in Component Cleaner Task. Failed operation: %s"), E_517(
																																			"Error when logging FE HTTP request/response"), E_518(
																																					"Error when trying to access FE Portal page"),

	E_599("Internal flow error. Operation: %s"),

	E_900("Unexpected error during BE REST API execution"), E_901("General error during FE Health Check"), E_999(
			"Unexpected error. Description: %s");

	/*
	 * 100-199 Security/Permission Related - Authentication problems (from
	 * external client, to external server) - Certification errors -
	 * 
	 * 200-299 Availability/Timeout Related - connectivity error - connection
	 * timeout
	 * 
	 * 300-399 Data Access/Integrity Related - Data in graph in invalid(E.g. no
	 * creator is found for service) - Artifact is missing in ES, but exists in
	 * graph.
	 * 
	 * 400-499 Schema Interface Type/Validation - received payload checksum is
	 * inavlid - received json is not valid
	 * 
	 * 500-599 Business/Flow Processing Related - check out to service is not
	 * allowed - rollback is done - failed to generate heat file
	 * 
	 * 
	 * 600-899 Reserved – do not use
	 * 
	 * 900-999 Unknown Errors - unexpected exception
	 */

	String description;
	String resolution;

	EcompErrorCode(String description, String resolution) {
		this.description = description;
		this.resolution = resolution;
	}

	EcompErrorCode(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

}
