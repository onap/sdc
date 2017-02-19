package org.openecomp.sdc.uici.tests.datatypes;

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
		CREATE("create/save"), 
		CHECK_IN("check_in"), 
		SUBMIT_FOR_TESTING("submit_for_testing"), 
		START_TESTING("start_testing"), 
		ACCEPT("accept"), 
		APPROVE("approve"), 
		DISTRIBUTE("distribute");

		private String value;

		public String getValue() {
			return value;
		}

		private LifeCyleChangeButtons(String value) {
			this.value = value;
		}
	}

	/**
	 * Artifacts Related Elements
	 * 
	 * @author mshitrit
	 *
	 */
	public enum Artifatcs {
		ADD_DEPLOYMENT_ARTIFACT("add-deployment-artifact-button"), 
		SELECT_ARTIFACT_DROPDOWN("selectArtifact"), 
		ARTIFACT_TYPE_DROPDOWN("artifacttype"), 
		ARTIFACT_DESCRIPTION("description"), 
		ARTIFACT_LABEL("artifactLabel"), 
		BROWSE_BUTTON("browseButton"), 
		ADD_BUTTON("Add");
		
		private String value;

		public String getValue() {
			return value;
		}

		private Artifatcs(String value) {
			this.value = value;
		}

	}

	public enum InformationalArtifatcs {
		CLOUD_QUESTIONNAIRE("Cloud Questionnaire (completed)"), 
		FEATURES("Features"), 
		VENDOR_TEST_RESULT("Vendor Test Result"), 
		TEST_SCRIPTS("Test Scripts"), 
		HEAT_TEMPLATE_FROM_VENDOR("HEAT Template from Vendor"), 
		CAPACITY("Capacity");

		private String value;

		public String getValue() {
			return value;
		}

		private InformationalArtifatcs(String value) {
			this.value = value;
		}

	}

	public enum ArtifactModal {
		LABEL("artifact-label"), TYPE("artifacttype"),;

		private String value;

		public String getValue() {
			return value;
		}

		private ArtifactModal(String value) {
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
		ACCEP_TESTING_MESSAGE("checkindialog"), 
		MESSAGE_TEXT("message"), 
		DONE("Done");

		private String value;

		public String getValue() {
			return value;
		}

		private ModalItems(String value) {
			this.value = value;
		}
	}

	public static final String LEFT_PANEL_PREFIX = "leftbar-section-content-item-";

	public enum LeftPanelCanvasItems {
		BLOCK_STORAGE(LEFT_PANEL_PREFIX + "BlockStorage"), 
		CINDER_VOLUME(LEFT_PANEL_PREFIX + "CinderVolume"), 
		COMPUTE(LEFT_PANEL_PREFIX + "Compute"), 
		LOAD_BALANCER(LEFT_PANEL_PREFIX + "LoadBalancer"), 
		NOVA_SERVER(LEFT_PANEL_PREFIX + "NovaServer"), 
		OBJECT_STORAGE(LEFT_PANEL_PREFIX + "ObjectStorage"),
		// NEUTRON_PORT(LEFT_PANEL_PREFIX + "-NeutronPort"),
		// PORT(LEFT_PANEL_PREFIX + "-Port"),
		DATABASE(LEFT_PANEL_PREFIX + "-Database"), 
		VMMC(LEFT_PANEL_PREFIX + "vmmc_work");

		private String value;

		public String getValue() {
			return value;
		}

		private LeftPanelCanvasItems(String value) {
			this.value = value;
		}
	}

	public enum RightBar {
		PROPERTIES_AND_ATTRIBUTES("properties-and-attributes-tab"), 
		DEPLOYMENT_ARTIFACTS("deployment-artifact-tab"), 
		ARTIFACT_NAME("artifactName"), 
		ADD_ARTIFACT_BUTTON("add_Artifact_Button"), 
		DELETE_ARTIFACT_BUTTON("delete"), 
		MYATTR_ATTR_FROM_LIST("my_attr-attr"), 
		MYATTR_ATTR_VALUE_FROM_LIST("value-of-my_attr"),;

		private String value;

		public String getValue() {
			return value;
		}

		private RightBar(String value) {
			this.value = value;
		}
	}
	
	// for now we use index to work with the breadcrumbs
	// any change in the breadcrumbs position will require an update here also
	public enum BreadcrumbsButtonsEnum {
		HOME("breadcrumbs-button-0"),
		COMPONENT("breadcrumbs-button-1");
		
		private String value;
		
		public String getValue() {
			return value;
		}
		
		private BreadcrumbsButtonsEnum(String value) {
			this.value = value;
		}
	}
	
	public enum InputsEnum {
		VF_INSTANCE("inputs-vf-instance-0"),
		FIRST_INPUT_CHECKBOX("inputs-checkbox-0"),
		SECOND_INPUT_CHECKBOX("inputs-checkbox-1"),
		ADD_INPUTS_BUTTON("add-inputs-to-service-button"),
		SERVICE_INPUT("service-input-0"),
		DELETE_INPUT("delete-input-0");
		
		private String value;
		
		public String getValue() {
			return value;
		}
		
		private InputsEnum(String value) {
			this.value = value;
		}
		
	}

	public enum TabsBar {
		HIERARCHY_TAB("hierarchy-tab"), 
		SELECTED_TAB("selected-tab"), 
		TAB_HEADER("tab-header"), 
		TAB_SUB_HEADER("tab-sub-header"), 
		HIERARCHY_MODULE("hierarchy-module-0"), 
		HIERARCHY_MODULE_TITLE("hierarchy-module-0-title"), 
		HIERARCHY_SELECTED_MODULE_DATA("selected-module-data"), 
		HIERARCHY_SELECTED_MODULE_NAME("selected-module-name"), 
		HIERARCHY_SELECTED_MODULE_UUID("selected-module-group-uuid"), 
		HIERARCHY_SELECTED_MODULE_VERSION("selected-module-version"), 
		HIERARCHY_SELECTED_MODULE_IS_BASE("selected-module-is-base"), 
		HIERARCHY_SELECTED_MODULE_ARTIFACT_NAME("selected-module-artifact-name"), 
		HIERARCHY_SELECTED_MODULE_ARTIFACT_UUID("selected-module-artifact-uuid"), 
		HIERARCHY_SELECTED_MODULE_ARTIFACT_VERSION("selected-module-artifact-version");

		private String value;

		public String getValue() {
			return value;
		}

		private TabsBar(String value) {
			this.value = value;
		}
	}

	public enum UpdateNamePopover {
		OPEN_POPOVER_ICON("edit-name-popover-icon"), 
		POPOVER_FORM("popover-form"), 
		POPOVER_SAVE_BUTTON("popover-save-button"), 
		POPOVER_INSTANCE_NAME("popover-vfinstance-name"), 
		POPOVER_HEAT_NAME("popover-heat-name"), 
		POPOVER_MODULE_NAME("popover-module-name"), 
		POPOVER_CLOSE_BUTTON("popover-close-button"), 
		POPOVER_X_BUTTON("popover-x-button");

		private String value;

		public String getValue() {
			return value;
		}

		private UpdateNamePopover(String value) {
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
		DELETE_INSTANCE_BUTTON("e-sdc-small-icon-delete");

		private String value;

		public String getValue() {
			return value;
		}

		private GeneralCanvasItems(String value) {
			this.value = value;
		}
	}

	public enum AttributesSection {
		TABLE_ROWS("attributes-table-row"), 
		EDIT_BUTTON_FOR_NETWORK_ATTR("edit_networks"), 
		DELETE_BUTTON_FOR_NETWORK_ATTR("delete_networks"), 
		ADD_BUTTON("add-attribute-button");

		private String value;

		public String getValue() {
			return value;
		}

		private AttributesSection(String value) {
			this.value = value;
		}
	}

	public enum AttributeForm {
		NAME_FIELD("attributeName"), 
		DESCRIPTION_FIELD("description"), 
		TYPE_FIELD("type-field"), 
		DEFAULT_VAL_FIELD("defaultvalue"),
		BOOL_DEFAULT_VAL_FIELD("booleantype"), 
		SCHEMA_FIELD("schema"), 
		BOOL_VALUE_FIELD("boolean-type-value"), 
		HIDDEN_FIELD("hidden"), 
		UPDATE_BUTTON("Update"), 
		DONE_BUTTON("Done"), 
		ADD_BUTTON("Add");

		private String value;

		public String getValue() {
			return value;
		}

		private AttributeForm(String value) {
			this.value = value;
		}
	}

	public enum PropertiesSection {
		ADD_BUTTON("addGrey"),;

		private String value;

		public String getValue() {
			return value;
		}

		private PropertiesSection(String value) {
			this.value = value;
		}
	}

	public enum PropertyForm {
		FORM_CONTAINER("sdc-edit-property-container"), 
		NAME_FIELD("propertyName"), 
		DESCRIPTION_FIELD("description"), 
		TYPE_FIELD("propertyType"), 
		SCHEMA_FIELD("schema-type"), 
		SIMPLE_TYPE_DEFAULT_VAL_FIELD("defaultvalue"), 
		SIMPLE_TYPE_BOOL_DEFAULT_VAL_FIELD("booleantype"), 
		LIST_TYPE_DEFAULT_VAL_FIELD("listNewItem-1"), 
		MAP_TYPE_DEFAULT_VAL_KEY_FIELD_FOR_FIRST_ITEM("mapKey-10"), 
		MAP_TYPE_DEFAULT_VAL_VALUE_FIELD_FOR_FIRST_ITEM("mapValue-10"), 
		MAP_TYPE_DEFAULT_VAL_KEY_FIELD_FOR_SECOND_ITEM("mapKey-11"), 
		MAP_TYPE_DEFAULT_VAL_VALUE_FIELD_FOR_SECOND_ITEM("mapValue-11"), 
		ADD_ITEM_TO_LIST_BUTTON("add-list-item-1"),
		ADD_ITEM_TO_MAP_BUTTON("add-map-item"),
		DELETE_FIRST_ITEM_FROM_MAP_BUTTON("delete-map-item-10"), 
		START_PORT_FIELD_FOR_PORT_PAIRS_DT("-1start_port"), 
		SAVE_BUTTON("Save");

		private String value;

		public String getValue() {
			return value;
		}

		private PropertyForm(String value) {
			this.value = value;
		}
	}

	public enum GeneralSection {
		BROWSE_BUTTON("browseButton"), FILE_NAME("filename"), NAME("name"), LOADER("tlv-loader");

		private String value;

		public String getValue() {
			return value;
		}

		private GeneralSection(String value) {
			this.value = value;
		}
	}

	public enum ReqAndCapabilitiesSection {
		SEARCH_BOX("search-box"), CAP_TAB("cap-tab"), REQ_TAB("req-tab");

		private String value;

		public String getValue() {
			return value;
		}

		private ReqAndCapabilitiesSection(String value) {
			this.value = value;
		}
	}

	public enum OnBoardingTable {
		OPEN_MODAL_BUTTON("repository-icon"), 
		VENDOR_HEADER_COL("Vendor"), 
		NAME_HEADER_COL("Name"), 
		CATEGORY_HEADER_COL("Category"), 
		VERSION_HEADER_COL("Version"), 
		IMPORT_ICON("import-csar"), 
		UPDATE_ICON("update-csar"), 
		CSAR_ROW("csar-row"), 
		ONBOARDING_SEARCH("onboarding-search");

		private String value;

		public String getValue() {
			return value;
		}

		private OnBoardingTable(String value) {
			this.value = value;
		}
	}
}
