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

import java.util.Arrays;
import java.util.List;

public final class DataTestIdEnum {
	private DataTestIdEnum() {
	}

	public enum Dashboard {
		IMPORT_AREA("importButtonsArea"),
		ADD_AREA("AddButtonsArea"),
		BUTTON_ADD_VF("createResourceButton"), 
		BUTTON_ADD_SERVICE("createServiceButton"), 
		IMPORT_VFC("importVFCbutton"), 
		IMPORT_VF("importVFbutton"), 
		IMPORT_VFC_FILE("file-importVFCbutton"), 
		IMPORT_VF_FILE("file-importVFbutton"),
		BUTTON_ADD_PRODUCT("createProductButton"),
		BUTTON_ADD_PNF("createPNFButton"), ;

		private String value;

		public String getValue() {
			return value;
		}

		private Dashboard(String value) {
			this.value = value;
		}
	}

	public enum LifeCyleChangeButtons {
		CREATE("create/save"), 
		CHECK_IN("check_in"), 
		SUBMIT_FOR_TESTING("submit_for_testing"), 
		START_TESTING("start_testing"), 
		ACCEPT("accept"),
		CHECKOUT("check_out");

		private String value;

		public String getValue() {
			return value;
		}

		private LifeCyleChangeButtons(String value) {
			this.value = value;
		}
	}

	public enum DistributionChangeButtons {
		APPROVE("approve"), 
		REJECT("reject"), 
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

	public enum InformationalArtifactsPlaceholders {
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

		private InformationalArtifactsPlaceholders(String value) {
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
		PORT("Port"), DATABASE("Database"), 
		NETWORK("Network");

		private String value;

		public String getValue() {
			return value;
		}

		private LeftPanelCanvasItems(String value) {
			this.value = value;
		}
	}

	public enum LinkMenuItems {
//		CANCEL_BUTTON("link-menu-button-cancel"), 
//		CONNECT_BUTTON("link-menu-button-connect"), 
//		LINK_ITEM_CAP("link-item-capabilities"), 
//		LINK_ITEM_REQ("link-item-requirements"), 
//		LINK_MENU("link-menu-open");
		LINK_ITEM_CAP_Or_REQ("req-or-cap-item");

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
		ICON(" iconBox"),
		TAGS_TABLE("i-sdc-tag-text");	
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
		CHECKOUT_BUTTON("check_out"), 
		SUBMIT_FOR_TESTING_BUTTON("submit_for_testing"), 
		DELETE_VERSION_BUTTON("delete_version"), 
		REVERT_BUTTON("revert"), 
		LIFECYCLE_STATE("formlifecyclestate"), 
		VERSION_HEADER("versionHeader"),
		OK("OK"), 
		UPLOAD_FILE_INPUT("browseButton");

		private String value;

		public String getValue() {
			return value;
		}

		private GeneralElementsEnum(String value) {
			this.value = value;
		}
	}

	public enum ArtifactPageEnum {
		ADD_DEPLOYMENT_ARTIFACT("add-deployment-artifact-button"),
		ADD_INFORMATIONAL_ARTIFACT("add-information-artifact-button"), 
		DOWNLOAD_ARTIFACT_ENV("download_env_"),
		ADD_ANOTHER_ARTIFACT("add-another-artifact-button"), 
		EDIT_ARTIFACT("edit_"), //upload env file by its label(via deployment artifact view)/displayName(via Canvas)
		DELETE_ARTIFACT("delete_"),
		DOWNLOAD_ARTIFACT("download_"), 
		GET_DEPLOYMENT_ARTIFACT_DESCRIPTION("description"), 
		GET_INFORMATIONAL_ARTIFACT_DESCRIPTION("Description"), 
		OK("OK"),
		TYPE("artifactType_"), 
		DEPLOYMENT_TIMEOUT("timeout_"), 
		VERSION("artifactVersion_"), 
		UUID("artifactUUID_"), 
		EDIT_PARAMETERS_OF_ARTIFACT("edit-parameters-of-"),
		ARTIFACT_NAME("artifactDisplayName_"),
		UPLOAD_HEAT_ENV_PARAMETERS("uplaodEnv_"),
		VERSION_ENV("artifactEnvVersion_");
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
		PROPERTY_NAME("propertyName_"), 
		PROPERTY_DESCRIPTION("propertyDescription_"), 
		PROPERTY_TYPE("propertyType_"), 
		ENTRY_SCHEMA("propertySchema_"), 
		ADD("Add"), CANCEL("Cancel"), 
		DONE("Done"), 
		PROPERTY_ROW("propertyRow"), 
		SAVE("Save"), 
		POPUP_FORM("sdc-edit-property-container");
		private String value;

		public String getValue() {
			return value;
		}

		private PropertiesPageEnum(String value) {
			this.value = value;
		}
	}
	
	public enum PropertiesPopupEnum {

		PROPERTY_NAME("propertyName"), 
		PROPERTY_VALUE("defaultvalue"), 
		PROPERTY_BOOLEAN_VALUE("booleantype"), 
		PROPERTY_DESCRIPTION("description"), 
		PROPERTY_TYPE("propertyType"), 
		ENTRY_SCHEMA("schema-type"), 
		CANCEL("Cancel"),  
		SAVE("Save"), 
		POPUP_FORM("sdc-edit-property-container"), 
		ADD("Add"), 
		DONE("Done"),
		PROPERTY_RADIO_BUTTON_CONTAINER("propertyRadioButton_"),
		RADIO_BUTTON_CLASS("tlv-radio-label");
		private String value;

		public String getValue() {
			return value;
		}

		private PropertiesPopupEnum(String value) {
			this.value = value;
		}
	}
	
	public enum AdminPageTabs {
		USER_MANAGEMENT("usermanagmenttab"), 
		CATEGORY_MANAGEMENT("categorymanagmenttab");
		
		private String value;

		public String getValue() {
			return value;
		}

		private AdminPageTabs(String value) {
			this.value = value;
		}
	}
	
	public enum UserManagementEnum {
		
		SEARCH_BOX("searchbox"),
		NEW_USER_FIELD("newuserId"),
		ROLE_SELECT("selectrole"),
		CREATE_BUTTON("creategreen"),
		CLASS__USER_MANAGEMENT_TABLE("sdc-user-management-table"),
		ROW_TABLE("row_"),
		FIRST_NAME("firstName_"),
		LAST_NAME("lastName__"),
		USER_ID("userId_"),
		EMAIL("email_"),
		ROLE("role_"),
		LAST_ACTIVE("lastActive_"),
		UPDATE_ROLE("selectRole_"),
		UPDATE_USER_BUTTON("updateUser_"),
		SAVE_USER("save_"),
		DELETE_USER("delete_"),
		;
		
		
		private String value;

		public String getValue() {
			return value;
		}

		private UserManagementEnum(String value) {
			this.value = value;
		}
	}
	
	public enum CategoryManagement {
	
		SERVICE_CATEGORY_HEADER("servicecategoryheader"), 
		RESOURCE_CATEGORY_HEADER("resourcecategoryheader"), 
		SERVICE_CATEGORY_LIST("servicecategory"), 
		RESOURCE_CATEGORY_LIST("resourcecategory"), 
		NEW_CATEGORY_BUTTON("newcategory"), 
		NEW_SUB_CATEGORY_BUTTON("newsubcategory"), 
		NEW_CATEGORY_NAME("i-sdc-form-input");
		
		private String value;

		public String getValue() {
			return value;
		}

		private CategoryManagement(String value) {
			this.value = value;
		}
	}
	
	

	public enum MainMenuButtons {
		HOME_BUTTON("main-menu-button-home"), 
		CATALOG_BUTTON("main-menu-button-catalog"), 
		ONBOARD_BUTTON("main-menu-button-onboard"), 
		SEARCH_BOX("main-menu-input-search"),
		REPOSITORY_ICON("repository-icon");
		private String value;

		public String getValue() {
			return value;
		}

		private MainMenuButtons(String value) {
			this.value = value;
		}
	}
	
	public enum MainMenuButtonsFromInsideFrame {
		HOME_BUTTON("breadcrumbs-button-0");
		private String value;

		public String getValue() {
			return value;
		}

		private MainMenuButtonsFromInsideFrame(String value) {
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
		GENERAL("GeneralLeftSideMenu"), 
		ICON("Iconstep"), 
		DEPLOYMENT_ARTIFACT("Deployment ArtifactLeftSideMenu"), 
		INFORMATION_ARTIFACT("Information ArtifactLeftSideMenu"), 
		PROPERTIES("PropertiesLeftSideMenu"), 
		COMPOSITION("CompositionLeftSideMenu"), 
		ACTIVITY_LOG("Activity LogLeftSideMenu"), 
		DEPLOYMENT_VIEW("DeploymentLeftSideMenu"), 
		TOSCA_ARTIFACTS("TOSCA ArtifactsLeftSideMenu"), 
		MONITOR("Monitor LeftSideMenu"),
		MANAGEMENT_WORKFLOW("Management WorkflowLeftSideMenu"), 
		INPUTS("Inputs"), 
		HIERARCHY("Hierarchy"),
		PROPERTIES_ASSIGNMENT("Properties AssignmentLeftSideMenu");

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
		ARTIFACT_LABEL("artifactLabel"), 
		ARTIFACT_TYPE("artifacttype"),
		OK("OK"), 
		SAVE("Save"),
		DONE_BUTTON("Done"), 
		CANCEL_BUTTON("Cancel"),
		URL("input[class^='i-sdc-form-input']"), 
		MODAL_WINDOW("sdc-add-artifact");

		private String value;

		public String getValue() {
			return value;
		}

		private ArtifactPopup(String value) {
			this.value = value;
		}
	}

	public enum ServiceMetadataEnum {
		SERVICE_NAME("name"), DESCRIPTION("description"), CATEGORY("selectGeneralCategory"), PROJECT_CODE("projectCode"), TAGS("i-sdc-tag-input"), CONTACT_ID("contactId"), ICON(" iconBox");

		private String value;

		public String getValue() {
			return value;
		}

		private ServiceMetadataEnum(String value) {
			this.value = value;
		}
	}
	
	public enum ProductMetadataEnum {
		PRODUCT_NAME("name"), 
		FULL_NAME("fullName"), 
		DESCRIPTION("description"),  
		PROJECT_CODE("projectCode"), 
		TAGS("i-sdc-tag-input"), 
		ATT_CONTACT("attContact"), 
		ICON(" iconBox");

		private String value;

		public String getValue() {
			return value;
		}

		private ProductMetadataEnum(String value) {
			this.value = value;
		}
	}
	
	public enum DashboardCardEnum {
		ASSET_TYPE("asset-type"), LIFECYCLE_STATE("span[class^='w-sdc-dashboard-card-info-lifecycleState']"),
		INFO_NAME("div.sdc-tile-content-info-item-name"), 
		VERSION("div[class^='w-sdc-dashboard-card-info-user']"),
		DASHBOARD_CARD("div[class^='w-sdc-dashboard-card ']"),
		ASSET_TYPE_CSS("span[data-tests-id='asset-type']");
		
		private String value;

		public String getValue() {
			return value;
		}

		private DashboardCardEnum(String value) {
			this.value = value;
		}
	}
	
	public enum CatalogPageLeftPanelCategoryCheckbox {
		GENERIC_CHECKBOX("span[data-tests-id='checkbox-resourcenewcategory.generic']"),
		NETWORK_L2_3("span[data-tests-id='checkbox-resourcenewcategory.networkl2-3']"),
		NETWORK_L4_PLUS("span[data-tests-id='checkbox-resourcenewcategory.networkl4+']"),
		NETWORK_CONNECTIVITY("span[data-tests-id='checkbox-resourcenewcategory.networkconnectivity']"),
		APPLICATION_L4_PLUS("span[data-tests-id='checkbox-resourcenewcategory.applicationl4+']"),
		DCAE("span[data-tests-id='checkbox-resourcenewcategory.dcaecomponent']");
		
		private String value;

		public String getValue() {
			return value;
		}

		private  CatalogPageLeftPanelCategoryCheckbox(String value) {
			this.value = value;
		}
	}
	
	public enum CatalogPageLeftPanelFilterTitle {
		TYPE("span[data-tests-id='typeFilterTitle']"), 
		CATEGORIES("span[data-tests-id='categoriesFilterTitle']"), 
		STATUS("span[data-tests-id='statusFilterTitle']");
		
		private String value;

		public String getValue() {
			return value;
		}

		private  CatalogPageLeftPanelFilterTitle(String value) {
			this.value = value;
		}
	}
	
	public enum CatalogPageLeftPanelSubCategoryCheckbox {
		COMMON_NETWORK_RESOURCES("span[data-tests-id='checkbox-resourcenewcategory.networkl4+.commonnetworkresources']"),
		ROUTER("span[data-tests-id='checkbox-resourcenewcategory.networkl2-3.router']"),				
		WAN_CONNECTORS("span[data-tests-id='checkbox-resourcenewcategory.networkl2-3.wanconnectors']"),
		LAN_CONNECTORS("span[data-tests-id='checkbox-resourcenewcategory.networkl2-3.lanconnectors']"),
		INFRASTRUCTERE_NETWORKl2_3("span[data-tests-id='checkbox-resourcenewcategory.networkl2-3.infrastructure']"),
		GATEWAY("span[data-tests-id='checkbox-resourcenewcategory.networkl2-3.gateway']"),
		NETWORK_ELEMENTS("span[data-tests-id='checkbox-resourcenewcategory.generic.networkelements']"),
		ABSTRACT("span[data-tests-id='checkbox-resourcenewcategory.generic.abstract']"),
		RULES("span[data-tests-id='checkbox-resourcenewcategory.generic.rules']"),
		DATABASE("span[data-tests-id='checkbox-resourcenewcategory.generic.database']"),
		INFRASTRUCTERE_GENERIC("span[data-tests-id='checkbox-resourcenewcategory.generic.infrastructure']"),
		VIRTUAL_LINKS("span[data-tests-id='checkbox-resourcenewcategory.networkconnectivity.virtuallinks']"),
		CONNECTION_POINTS("span[data-tests-id='checkbox-resourcenewcategory.networkconnectivity.connectionpoints']"),
		APPLICATION_SERVER("span[data-tests-id='checkbox-resourcenewcategory.applicationl4+.applicationserver']"),
		CALL_CONTROL("span[data-tests-id='checkbox-resourcenewcategory.applicationl4+.callcontrol']"),
		MEDIASERVERS("span[data-tests-id='checkbox-resourcenewcategory.applicationl4+.mediaservers']"),
		WEBSERVER("span[data-tests-id='checkbox-resourcenewcategory.applicationl4+.webserver']"),
		LOAD_BALANCER("span[data-tests-id='checkbox-resourcenewcategory.applicationl4+.loadbalancer']"),
		BORDER_ELEMENT("span[data-tests-id='checkbox-resourcenewcategory.applicationl4+.borderelement']"),
		DATABASE_APPLIVATION_L4_PLUS("span[data-tests-id='checkbox-resourcenewcategory.applicationl4+.database']"),
		FIREWALL("span[data-tests-id='checkbox-resourcenewcategory.applicationl4+.firewall']"),
		DATABASE_DCAE("span[data-tests-id='checkbox-resourcenewcategory.dcaecomponent.database']"),
		POLICY("span[data-tests-id='checkbox-resourcenewcategory.dcaecomponent.policy']"),
		MICROSERVICE("span[data-tests-id='checkbox-resourcenewcategory.dcaecomponent.microservice']"),
		SOURCE("span[data-tests-id='checkbox-resourcenewcategory.dcaecomponent.source']"),
		COLLECTOR("span[data-tests-id='checkbox-resourcenewcategory.dcaecomponent.collector']"),
		UTILITY("span[data-tests-id='checkbox-resourcenewcategory.dcaecomponent.utility']"),
		ANALYTICS("span[data-tests-id='checkbox-resourcenewcategory.dcaecomponent.analytics']");
		
		private String value;

		public String getValue() {
			return value;
		}

		private  CatalogPageLeftPanelSubCategoryCheckbox(String value) {
			this.value = value;
		}
	}
	
	public enum CompositionScreenEnum {
		
		CHANGE_VERSION("changeVersion", Arrays.asList()),
		DEPLOYMENT_ARTIFACT_TAB("deployment-artifact-tab", Arrays.asList("Deployment Artifacts")),
		ADD_ARTIFACT("add_Artifact_Button", Arrays.asList()),
		SEARCH_ASSET("searchAsset", Arrays.asList()),
		PROPERTIES_AND_ATTRIBUTES_TAB("properties-and-attributes-tab",Arrays.asList()),
		MENU_INPUTS("sub-menu-button-inputs",Arrays.asList()),
		MENU_TRIANGLE_DROPDOWN("triangle-dropdown", Arrays.asList()),
		ARTIFACTS_LIST("artifactName", Arrays.asList()),
		INFORMATION_ARTIFACTS("button[tooltip-content='Information Artifacts']", Arrays.asList("Informational Artifacts")),
		API("button[tooltip-content='API']", Arrays.asList("API Artifacts")),
		INFORMATION("button[tooltip-content='Information']", Arrays.asList("General Info", "Additional Information", "Tags")),
		COMPOSITION("button[tooltip-content='Composition']",  Arrays.asList("Composition")),
		INPUTS("button[tooltip-content='Inputs']", Arrays.asList("")),
		REQUIREMENTS_AND_CAPABILITIES("button[tooltip-content='Requirements and Capabilities']", Arrays.asList()),
		INFORMATION_TAB("information-tab", Arrays.asList()),
		CUSTOMIZATION_UUID("rightTab_customizationModuleUUID", Arrays.asList());
		
		private String value;
		private List<String> title;

		public String getValue() {
			return value;
		}
		
		public List<String> getTitle() {
			return title;
		}				

		private  CompositionScreenEnum(String value, List<String> title) {
			this.value = value;
			this.title = title;
		}		
	}
	
	public enum ToscaArtifactsScreenEnum {
		
		TOSCA_MODEL("download-Tosca Model"),
		TOSCA_TEMPLATE("download-Tosca Template"),
		ARTIFACT_VERSION("version-"),
		ARTIFACT_NAME("name-"),
		ARTIFACT_TYPE("type-"),
		ARTIFACT_DETAILS("details-"),
		DOWNLOAD_ARTIFACT("download-"),
		DOWNLOAD_CSAR("download-Tosca Model");
		
		private String value;

		public String getValue() {
			return value;
		}

		private  ToscaArtifactsScreenEnum(String value) {
			this.value = value;
		}		
	}
	
	public enum InformationalArtifactsService {
		AFFINITY_RULES("artifact_Display_Name-Affinity Rules"),
		CONTROL_LOOP_FUNCTIONS("artifact_Display_Name-Control Loop Functions"),
		DEPLOYMENT_VOTING_RECORD("artifact_Display_Name-Deployment Voting Record"),
		DIMENSIONING_INFO("artifact_Display_Name-Dimensioning Info"),
		DISTRIBUTION_INSTRUCTION("artifact_Display_Name-Distribution Instructions"),
		ENGINEERING_RULES("artifact_Display_Name-Engineering Rules (ERD)"),
		OPERATIONAL_POLICES("artifact_Display_Name-Operational Policies"),
		SERVICE_ARTIFACT_PLAN("artifact_Display_Name-Service Artifact Plan"),
		SERVICE_QUESTIONNAIRE("artifact_Display_Name-Service Questionnaire"),
		SERVICE_SECURITY_TEMPLATE("artifact_Display_Name-Service Security Template"),
		SERVICE_SPECIFIC_POLICIES("artifact_Display_Name-Service-specific Policies"),
		SUMMARY_OF_IMPACTS_TO_ECOMP("artifact_Display_Name-Summary of impacts to ECOMP elements,OSSs, BSSs"),
		TD_CERTIFICATION_TEST_RESULTS("artifact_Display_Name-TD Certification Test Results");

		private String value;

		public String getValue() {
			return value;
		}

		private InformationalArtifactsService(String value) {
			this.value = value;
		}
	}
	
	public enum APIArtifactsService {
		
		CONFIGURATION("artifact_Display_Name-Configuration"),
		INSTANTIATION("artifact_Display_Name-Instantiation"),
		LOGGING("artifact_Display_Name-Logging"),
		MONITORING("artifact_Display_Name-Monitoring"),
		REPORTING("artifact_Display_Name-Reporting"),
		TESTING("artifact_Display_Name-Testing");
		
		private String value;

		public String getValue() {
			return value;
		}

		private  APIArtifactsService(String value) {
			this.value = value;
		}
	}
	
	public enum DeploymentArtifactCompositionRightMenu {
		ARTIFACT_NAME("artifactName-"),
		ARTIFACT_DISPLAY_NAME("artifact_Display_Name-"),
		DOWNLOAD("download_"), 
		DELETE("delete_"),
		ADD_ARTIFACT_BUTTON("add_Artifact_Button"), 
		EDIT_PARAMETERS_OF_ARTIFACT("edit-parameters-of-"),
		ARTIFACT_ITEM("artifact-item-"),
		ARTIFACT_ENV("heat_env_");
		
		private String value;

		public String getValue() {
			return value;
		}

		private DeploymentArtifactCompositionRightMenu(String value) {
			this.value = value;
		}
		
	}
	
	public enum InputsScreenService {
		ADD_SELECTED_INPUTS_BTN("add-inputs-to-service-button"), 
		VF_INSTANCE_ROWS("expand-collapse[expanded-selector^='.vf-instance-list.']"),
		VF_INSTANCE_ROW_NAME("span[class^='title-text']"),
		VF_INSTANCE_INPUTS("div[class^='vf-instance-list ']"),
		VF_INSTANCE_INPUT("div[class^='input-row ng-scope']"),
		VF_INSTNCE_PROPERTY_NAME("div[class^='title-text']"),
		INPUT_CHECKBOX("span[class^='tlv-checkbox-label']"),
		SERVICE_INPUT_ROW("div[class^='service-input-row input-row']"),
		DELETE_INPUT_BTN("span[class$='remove-input-icon']"),
		RESOURCE_INSTANCE_PROPERTY_NAME("propertyName_"),
		RESOURCE_INSTANCE_PROPERTY_TYPE("propertyType_"),
		RESOURCE_INSTANCE_PROPERTY_CHECKBOX("propertyCheckbox_"),
		SERVICE_INPUTS_DELETE_BUTTON("deleteInput_")
		;
		
		private String value;

		public String getValue() {
			return value;
		}

		private InputsScreenService(String value) {
			this.value = value;
		}
		
	}
	
	public enum DeploymentScreen {
		MODULES("span[class^='expand-collapse-title-text']"),
		MEMBERS("div[class^='expand-collapse-sub-title']"),
		PROPERTIES("list-of-Properties"),
		ARTIFACTS("list-of-Artifacts"),
		BUTTON_PROPERTIES("div[data-tests-id='list-of-Properties'] span[class^='hand']"),
		BUTTON_ARTIFACTS("div[data-tests-id='list-of-Artifacts'] span[class^='hand']"),
		PROPERTY_NAMES("div[data-tests-id='selected-module-property-name'] span"),
		PROPERTY_TYPES("selected-module-property-type"),
		PROPERTY_SCHEMA_TYPE("selected-module-property-schema-type"),
		ARTIFACT_NAME("selected-module-artifact-name"),
		ARTIFACT_UUID("selected-module-artifact-uuid"),
		ARTIFACT_VERSION("selected-module-artifact-version"),
		PENCIL_ICON("edit-name-popover-icon"),
		MODULE_NAME("selected-module-name"),
		MODULE_ID("selected-module-group-uuid"),
		RESOURCE_NAME_ON_POPOVER("popover-vfinstance-name"),
		MODULE_NAME_ON_POPOVER("popover-module-name"),
		NAME_INPUT("popover-heat-name"),
		SAVE("popover-save-button"),
		CANCEL("popover-close-button"),
		X_BUTTON("popover-x-button");
				
		private String value;

		public String getValue() {
			return value;
		}

		private DeploymentScreen(String value) {
			this.value = value;
		}
	}
	
	public enum PropertiesAssignmentScreen {

		PROPERTIES_TAB("Properties"),
		INPUTS_TAB("Inputs"),
		COMPOSITION_TAB("Composition"),
		PROPERTY_STRUCTURE_TAB("Property Structure"),
		DECLARE_BUTTON("declare-button"),
		SEARCH_BOX("search-box"),
		SEARCH_BUTTON("search-button"),
		FILTER_BUTTON("filter-button"),
		FILTER_BOX("filter-box"),
		CLEAR_FILTER_BUTTON("clear-filter-button"),
		INPUT_DELETE_BUTTON("delete-input-button"),
		INPUT_DELETE_DIALOG_DELETE("Delete"),
		INPUT_DELETE_DIALOG_CLOSE("Close"),
		FILTER_CHECKBOX_ALL("filter-checkbox-all");
		
		
		private String value;

		public String getValue() {
			return value;
		}

		private PropertiesAssignmentScreen(String value) {
			this.value = value;
		}
		
	}
	
	public enum ImportVfRepository {
		SEARCH("onboarding-search"), 
		IMPORT_VSP("import-csar"), 
		DOWNLOAD_CSAR("download-csar"),
		UPDATE_VSP("update-csar");

		private String value;

		public String getValue() {
			return value;
		}

		private ImportVfRepository(String value) {
			this.value = value;
		}
	}
	
	public enum EnvParameterView {
		SEARCH_ENV_PARAM_NAME("search-env-param-name"), 
		ENV_CURRENT_VALUE("value-field-of-"),//value-field-of-oam_volume_name_0 - parameter name 
		ENV_DEFAULT_VALUE("default-value-of-");// default-value-of-vnf_name

		private String value;

		public String getValue() {
			return value;
		}

		private EnvParameterView(String value) {
			this.value = value;
		}
	}
	
	
}
