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
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openecomp.sdc.ci.tests.pages.TopSearchComponent;

public final class DataTestIdEnum {

    private DataTestIdEnum() {
    }

    @AllArgsConstructor
    @Getter
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
        BUTTON_ADD_PNF("createPNFButton"),
        BUTTON_ADD_CR("createCRButton");

        private final String value;
    }

    @AllArgsConstructor
    @Getter
    public enum LifeCyleChangeButtons {
        CREATE("create/save"),
        CHECK_IN("check_in"),
        SUBMIT_FOR_TESTING("submit_for_testing"),
        START_TESTING("start_testing"),
        ACCEPT("accept"),
        CERTIFY("certify"),
        CHECKOUT("check_out");

        private final String value;

    }

    @AllArgsConstructor
    @Getter
    public enum DistributionChangeButtons {
        APPROVE("approve"),
        REJECT("reject"),
        DISTRIBUTE("distribute"),
        MONITOR("monitor"),
        APPROVE_MESSAGE("checkindialog"),
        RE_DISTRIBUTE("redistribute");

        private final String value;

    }

    @AllArgsConstructor
    @Getter
    public enum ModalItems {
        BROWSE_BUTTON("browseButton"),
        ADD("Add"),
        DESCRIPTION("description"),
        SUMBIT_FOR_TESTING_MESSAGE("changeLifeCycleMessage"),
        OK("OK"),
        CANCEL("Cancel"),
        DELETE_INSTANCE_OK("deleteInstanceModal-button-ok"),
        DELETE_INSTANCE_CANCEL("deleteInstanceModal-button-cancel"),
        RENAME_INSTANCE_OK("renameInstanceModal-button-ok"),
        RENAME_INSTANCE_CANCEL("renameInstanceModal-button-cancel"),
        UPGRADE_SERVICES_CANCEL("upgradeVspModal-close"),
        UPGRADE_SERVICES_OK("upgradeVspModal-button-upgrade"),
        UPDATE_SERVICES_OK("upgradeVspModal-button-update"),
        UPGRADE_SERVICES_CLOSE("upgradeVspModal-button-close"),
        ACCEPT_TESTING_MESSAGE("checkindialog");

        private final String value;

    }

    @AllArgsConstructor
    @Getter
    public enum LeftPanelCanvasItems {
        BLOCK_STORAGE("BlockStorage"),
        CINDER_VOLUME("CinderVolume"),
        COMPUTE("Compute"),
        LOAD_BALANCER("LoadBalancer"),
        NOVA_SERVER("NovaServer"),
        OBJECT_STORAGE("ObjectStorage"),
        NEUTRON_PORT("NeutronPort"),
        PORT("Port"), DATABASE("Database"),
        NETWORK("Network"),
        CONTRAIL_PORT("ContrailPort"),
        CONTRAIL_VIRTUAL_NETWORK("ContrailVirtualNetwork");

        private final String value;

    }

    @AllArgsConstructor
    @Getter
    public enum GeneralCanvasItems {
        CANVAS("canvas"),
        CANVAS_RIGHT_PANEL("w-sdc-designer-sidebar-head"),
        DELETE_INSTANCE_BUTTON("deleteInstance"),
        UPDATE_INSTANCE_NAME("e-sdc-small-icon-update"),
        INSTANCE_NAME_FIELD("instanceName");

        private final String value;

    }

    @AllArgsConstructor
    @Getter
    public enum ResourceMetadataEnum {
        RESOURCE_NAME("name"),
        DESCRIPTION("description"),
        CATEGORY("selectGeneralCategory"),
        VENDOR_NAME("vendorName"),
        VENDOR_RELEASE("vendorRelease"),
        TAGS("i-sdc-tag-input"),
        CONTACT_ID("contactId"),
        ICON(" iconBox"),
        TAGS_TABLE("i-sdc-tag-text"),
        SELECT_VSP("filename");

        private final String value;

    }

    @AllArgsConstructor
    @Getter
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
        UPDATE_SERVICES_BUTTON("open-upgrade-vsp-popup"),
        UPLOAD_FILE_INPUT("browseButton"),
        RESTORE_BUTTON("restore-component-button"),
        ARCHIVE_BUTTON("archive-component-button");

        private final String value;

    }

    @AllArgsConstructor
    @Getter
    public enum ArtifactPageEnum {
        ADD_DEPLOYMENT_ARTIFACT("add-deployment-artifact-button"),
        ADD_INFORMATIONAL_ARTIFACT("add-information-artifact-button"),
        DOWNLOAD_ARTIFACT_ENV("download_env_"),
        ADD_ANOTHER_ARTIFACT("add-another-artifact-button"),
        EDIT_ARTIFACT("edit_"), //upload env file by its label(via deployment artifact view)/displayName(via Canvas)
        DELETE_ARTIFACT("delete_"),
        DOWNLOAD_ARTIFACT("download_"),
        BROWSE_ARTIFACT("gab-"),
        GET_DEPLOYMENT_ARTIFACT_DESCRIPTION("description"),
        GET_INFORMATIONAL_ARTIFACT_DESCRIPTION("Description"),
        OK("OK"),
        TYPE("artifactType_"),
        DEPLOYMENT_TIMEOUT("timeout_"),
        VERSION("artifactVersion_"),
        UUID("artifactUUID_"),
        EDIT_PARAMETERS_OF_ARTIFACT("edit-parameters-of-"),
        DELETE_PARAMETER_OF_ARTIFACT("delete-"),
        ARTIFACT_NAME("artifactDisplayName_"),
        UPLOAD_HEAT_ENV_PARAMETERS("uplaodEnv_"),
        VERSION_ENV("artifactEnvVersion_"),
        ADD_OTHER_ARTIFACT_BUTTON("//button[@class='add-button ng-scope']");

        private final String value;

    }

    @AllArgsConstructor
    @Getter
    public enum MainMenuButtons {
        HOME_BUTTON("main-menu-button-home"),
        CATALOG_BUTTON("main-menu-button-catalog"),
        ONBOARD_BUTTON("main-menu-button-onboard"),
        SEARCH_BOX(TopSearchComponent.SEARCH_INPUT_TEST_ID),
        REPOSITORY_ICON("repository-icon");

        private final String value;

    }

    @AllArgsConstructor
    @Getter
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
        MONITOR("DistributionLeftSideMenu"),
        MANAGEMENT_WORKFLOW("Management WorkflowLeftSideMenu"),
        INPUTS("Inputs"),
        HIERARCHY("Hierarchy"),
        PROPERTIES_ASSIGNMENT("Properties AssignmentLeftSideMenu");

        private final String value;

    }

    @AllArgsConstructor
    @Getter
    public enum ServiceMetadataEnum {
        SERVICE_NAME("name"),
        DESCRIPTION("description"),
        CATEGORY("selectGeneralCategory"),
        PROJECT_CODE("projectCode"),
        TAGS("i-sdc-tag-input"),
        CONTACT_ID("contactId"),
        ICON(" iconBox"),
        INSTANTIATION_TYPE("selectInstantiationType");

        private final String value;

    }

    @AllArgsConstructor
    @Getter
    public enum CompositionScreenEnum {
        CHANGE_VERSION("changeVersion", Collections.emptyList()),
        DEPLOYMENT_ARTIFACT_TAB("deployment-artifact-tab", Collections.singletonList("Deployment Artifacts")),
        ADD_ARTIFACT("add_Artifact_Button", Collections.emptyList()),
        SEARCH_ASSET("searchAsset", Collections.emptyList()),
        PROPERTIES_AND_ATTRIBUTES_TAB("properties-and-attributes-tab", Collections.emptyList()),
        MENU_INPUTS("sub-menu-button-inputs", Collections.emptyList()),
        MENU_ONBOARD("sub-menu-button-onboard", Collections.emptyList()),
        MENU_HOME("sub-menu-button-home", Collections.emptyList()),
        MENU_PROPERTIES_ASSIGNMENT("sub-menu-button-properties assignment", Collections.emptyList()),
        MENU_TRIANGLE_DROPDOWN("triangle-dropdown", Collections.emptyList()),
        ARTIFACTS_LIST("artifactName", Collections.emptyList()),
        INFORMATION_ARTIFACTS("button[tooltip-content='Information Artifacts']",
            Collections.singletonList("Informational Artifacts")),
        API("button[tooltip-content='API']", Collections.singletonList("API Artifacts")),
        INFORMATION("button[tooltip-content='Information']", Arrays.asList("General Info", "Additional Information", "Tags")),
        COMPOSITION("button[tooltip-content='Composition']", Collections.singletonList("Composition")),
        INPUTS("button[tooltip-content='Inputs']", Collections.singletonList("")),
        REQUIREMENTS_AND_CAPABILITIES("button[tooltip-content='Requirements and Capabilities']",
            Collections.emptyList()),
        INFORMATION_TAB("information-tab", Collections.emptyList()),
        CUSTOMIZATION_UUID("rightTab_customizationModuleUUID", Collections.emptyList());

        private final String value;
        private final List<String> title;

    }

    @AllArgsConstructor
    @Getter
    public enum ImportVfRepository {
        SEARCH("onboarding-search"),
        IMPORT_VSP("import-csar"),
        DOWNLOAD_CSAR("download-csar"),
        UPDATE_VSP("update-csar");

        private final String value;

    }

    @AllArgsConstructor
    @Getter
    public enum CompositionRightPanel {
        COMPONENT_TITLE("selectedCompTitle"),
        REQS_AND_CAPS_TAB("requirements-and-capabilities"),
        EDIT_PENCIL("editPencil"),
        INSTANCE_NAME_TEXTBOX("instanceName"),
        DELETE_ITEM("deleteInstance"),
        REQS_AND_CAPS_TAB_XPATH("//button[@tooltip-content='Requirements and Capabilities']");

        private final String value;

    }

    @AllArgsConstructor
    @Getter
    public enum VspValidationPage {
        VSP_VALIDATION_PAGE_NAVBAR("navbar-group-item-SOFTWARE_PRODUCT_VALIDATION"),
        VSP_VALIDATION_PAGE_BREADCRUMBS("sub-menu-button-validation"),
        VSP_VALIDATION_PAGE_PROCEED_TO_INPUTS_BUTTON("go-to-vsp-validation-inputs"),
        VSP_VALIDATION_PAGE_PROCEED_TO_SETUP_BUTTON("go-to-vsp-validation-setup"),
        VSP_VALIDATION_PAGE_INPUT("%s_%s_input"),
        VSP_VALIDATION_PAGE_PROCEED_TO_RESULTS_BUTTON("proceed-to-validation-results-btn"),
        VSP_VALIDATION_PAGE_COMPLIANCE_CHECKBOX_TREE("vsp-validation-compliance-checks-checkbox-tree"),
        VSP_VALIDATION_PAGE_CERTIFICATION_CHECKBOX_TREE("vsp-validation-certifications-query-checkbox-tree");

        private final String value;

    }

    @AllArgsConstructor
    @Getter
    public enum VspValidationResultsPage {
        VSP_VALIDATION_RESULTS_PAGE_NAVBAR("navbar-group-item-SOFTWARE_PRODUCT_VALIDATION_RESULTS"),
        VSP_VALIDATION_RESULTS_PAGE_BREADCRUMBS("sub-menu-button-validation results");

        private final String value;

    }

}
