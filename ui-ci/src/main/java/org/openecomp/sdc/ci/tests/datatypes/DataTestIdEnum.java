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

package org.openecomp.sdc.ci.tests.datatypes;

public final class DataTestIdEnum {
	private DataTestIdEnum() {
	};

	public enum Dashboard {
		IMPORT_AREA("importButtonsArea"), 
		BUTTON_ADD_VF("createResourceButton"), 
		BUTTON_ADD_SERVICE("createServiceButton"), 
		IMPORT_VFC("importVFCbutton"), 
		IMPORT_VF("importVFbutton"), 
		IMPORT_VFC_FILE("file-importVFCbutton"), 
		IMPORT_VF_FILE("file-importVFbutton");

		private String value;

		public String getValue() {
			return value;
		}

		private Dashboard(String value) {
			this.value = value;
		}
	}

	public enum LifeCyleChangeButtons {
		CREATE("create/save"), CHECK_IN("check_in"), 
		SUBMIT_FOR_TESTING("submit_for_testing"), 
		START_TESTING("start_testing"), 
		ACCEPT("accept");

		private String value;

		public String getValue() {
			return value;
		}

		private LifeCyleChangeButtons(String value) {
			this.value = value;
		}
	}

	public enum DistributionChangeButtons {
		APPROVE("approve"), REJECT("reject"), 
		DISTRIBUTE("distribute"), 
		MONITOR("monitor"), 
		APPROVE_MESSAGE("checkindialog"), 
		RE_DISTRIBUTE("redistribute");

		private String value;

		public String getValue() {
			return value;
		}

		private DistributionChangeButtons(String value) {
			this.value = value;
		}
	}

	public enum InformationalArtifacts {
		CLOUD_QUESTIONNAIRE("Cloud Questionnaire (completed)"), 
		FEATURES("Features"), 
		VENDOR_TEST_RESULT("Vendor Test Result"), 
		TEST_SCRIPTS("Test Scripts"), 
		RESOURCE_SECURITY_TEMPLATE("Resource Security Template"), 
		HEAT_TEMPLATE_FROM_VENDOR("HEAT Template from Vendor"),
		CAPACITY("Capacity");

		private String value;

		public String getValue() {
			return value;
		}

		private InformationalArtifacts(String value) {
			this.value = value;
		}

	}

	public enum ModalItems {
		BROWSE_BUTTON("browseButton"), 
		ADD("Add"), 
		DESCRIPTION("description"), 
		SUMBIT_FOR_TESTING_MESSAGE("changeLifeCycleMessage"), 
		OK("OK"), 
		CANCEL("Cancel"), 
		ACCEP_TESTING_MESSAGE("checkindialog");

		private String value;

		public String getValue() {
			return value;
		}

		private ModalItems(String value) {
			this.value = value;
		}
	}

	public enum LeftPanelCanvasItems {
		BLOCK_STORAGE("BlockStorage"), 
		CINDER_VOLUME("CinderVolume"), 
		COMPUTE("Compute"), 
		LOAD_BALANCER("LoadBalancer"), 
		NOVA_SERVER("NovaServer"), 
		OBJECT_STORAGE("ObjectStorage"), 
		NEUTRON_PORT("NeutronPort"), 
		PORT("Port"), 
		DATABASE("Database");

		private String value;

		public String getValue() {
			return value;
		}

		private LeftPanelCanvasItems(String value) {
			this.value = value;
		}
	}

	public enum LinkMenuItems {
		CANCEL_BUTTON("link-menu-button-cancel"), 
		CONNECT_BUTTON("link-menu-button-connect"), 
		LINK_ITEM_CAP("link-item-capabilities"), 
		LINK_ITEM_REQ("link-item-requirements"), 
		LINK_MENU("link-menu-open");

		private String value;

		public String getValue() {
			return value;
		}

		private LinkMenuItems(String value) {
			this.value = value;
		}
	}

	public enum GeneralCanvasItems {
		CANVAS("canvas"), 
		CANVAS_RIGHT_PANEL("w-sdc-designer-sidebar-head"), 
		DELETE_INSTANCE_BUTTON("e-sdc-small-icon-delete"), 
		UPDATE_INSTANCE_NAME("e-sdc-small-icon-update"), 
		INSTANCE_NAME_FIELD("instanceName");

		private String value;

		public String getValue() {
			return value;
		}

		private GeneralCanvasItems(String value) {
			this.value = value;
		}

	}

	public enum ResourceMetadataEnum {
		RESOURCE_NAME("name"), 
		DESCRIPTION("description"), 
		CATEGORY("selectGeneralCategory"), 
		VENDOR_NAME("vendorName"),
		VENDOR_RELEASE("vendorRelease"), 
		TAGS("i-sdc-tag-input"), 
		CONTACT_ID("contactId"), 
		ICON(" iconBox");

		private String value;

		public String getValue() {
			return value;
		}

		private ResourceMetadataEnum(String value) {
			this.value = value;
		}

	}

	public enum GeneralElementsEnum {
		CREATE_BUTTON("create/save"), 
		CHECKIN_BUTTON("check_in"), 
		SUBMIT_FOR_TESTING_BUTTON("submit_for_testing"), 
		DELETE_VERSION_BUTTON("delete_version"), 
		REVERT_BUTTON("revert"), 
		LIFECYCLE_STATE("lifecyclestate"), 
		VERSION_HEADER("versionHeader");

		private String value;

		public String getValue() {
			return value;
		}

		private GeneralElementsEnum(String value) {
			this.value = value;
		}

	}

	public enum ArtifactPageEnum {

		ADD_INFORMATIONAL_ARTIFACT("add-information-artifact-button"), 
		ADD_DEPLOYMENT_ARTIFACT("add-deployment-artifact-button"), 
		ADD_ANOTHER_ARTIFACT("add-another-artifact-button"), 
		EDIT_ARTIFACT("edit_"), 
		DELETE_ARTIFACT("delete_"), 
		DOWNLOAD_ARTIFACT("download_"), 
		GET_DEPLOYMENT_ARTIFACT_DESCRIPTION("description"), 
		GET_INFORMATIONAL_ARTIFACT_DESCRIPTION("Description")

		;
		private String value;

		public String getValue() {
			return value;
		}

		private ArtifactPageEnum(String value) {
			this.value = value;
		}
	}

	public enum PropertiesPageEnum {

		ADD_NEW_PROPERTY("addGrey"), 
		EDIT_PROPERTY("edit_"), 
		DELETE_PROPERTY("delete_"), 
		PROPERTY_NAME("propertyName"), 
		PROPERTY_VALUE("defaultvalue"), 
		PROPERTY_DESCRIPTION("description"),
		PROPERTY_TYPE("propertyType"), 
		ADD("Add"), 
		CANCEL("Cancel"), 
		DONE("Done"), 
		PROPERTY_ROW("propertyRow"), 
		SAVE("Save");
		
		private String value;

		public String getValue() {
			return value;
		}

		private PropertiesPageEnum(String value) {
			this.value = value;
		}
	}

	public enum MainMenuButtons {
		HOME_BUTTON("main-menu-button-home"), 
		CATALOG_BUTTON("main-menu-button-catalog"), 
		ONBOARD_BUTTON("main-menu-button-onboard");
		
		private String value;

		public String getValue() {
			return value;
		}

		private MainMenuButtons(String value) {
			this.value = value;
		}

	}

	public enum MenuOptionsEnum {
		EDIT("Edit"), 
		CHECK_IN("Check in"), 
		CHECK_OUT("Check out"), 
		VIEW("View"), 
		SUBMIT_FOR_TEST("Submit For Test"), 
		ACCEPT("Accept"), 
		REJECT("Reject"), 
		START_TEST("Start test"), 
		DISTREBUTE("Distribute");

		private String value;

		public String getValue() {
			return value;
		}

		private MenuOptionsEnum(String value) {
			this.value = value;
		}
	}

	public enum StepsEnum {
		GENERAL("Generalstep"), 
		ICON("Iconstep"), 
		DEPLOYMENT_ARTIFACT("Deployment Artifactstep"), 
		INFORMATION_ARTIFACT("Information Artifactstep"), 
		PROPERTIES("Propertiesstep"), 
		COMPOSITION("Compositionstep"), 
		ACTIVITY_LOG("Activity Logstep"), 
		DEPLOYMENT_VIEW("Deploymentstep"), 
		TOSCA_ARTIFACTS("TOSCA Artifactsstep"), 
		MONITOR("Monitor step");

		private String value;

		public String getValue() {
			return value;
		}

		private StepsEnum(String value) {
			this.value = value;
		}

	}

	public enum ArtifactPopup {

		BROWSE("browseButton"), 
		ARTIFACT_DESCRIPTION("description"), 
		ARTIFACT_LABEL("selectArtifact"), 
		ARTIFACT_TYPE("artifacttype"), 
		ADD_BUTTON("Add"), 
		CANCEL_BUTTON("Cancel"), 
		UPDATE_BUTTON("Update");

		private String value;

		public String getValue() {
			return value;
		}

		private ArtifactPopup(String value) {
			this.value = value;
		}

	}

	public enum ServiceMetadataEnum {
		SERVICE_NAME("name"), 
		DESCRIPTION("description"), 
		CATEGORY("selectGeneralCategory"), 
		PROJECT_CODE("projectCode"), 
		TAGS("i-sdc-tag-input"), 
		CONTACT_ID("contactId"), 
		ICON(" iconBox");

		private String value;

		public String getValue() {
			return value;
		}

		private ServiceMetadataEnum(String value) {
			this.value = value;
		}

	}

}
