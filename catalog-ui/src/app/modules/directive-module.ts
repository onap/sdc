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

import {ClickedOutsideDirective} from "../directives/clicked-outside/clicked-outside-directive";
import {LoaderDirective} from "../directives/loader/loader-directive";
import {UserHeaderDetailsDirective} from "../directives/user-header-details/user-header-details-directive";
import {FileTypeDirective} from "../directives/file-type/file-type";
import {DownloadArtifactDirective} from "../directives/download-artifact/download-artifact";
import {EllipsisDirective} from "../directives/ellipsis/ellipsis-directive";
import {InvalidCharactersDirective} from "../directives/invalid-characters/invalid-characters";
import {ExpandCollapseDirective} from "../directives/utils/expand-collapse/expand-collapse";
import {PerfectScrollerDirective} from "../directives/perfect-scrollbar/angular-perfect-scrollbar";
import {SdcModalDirective} from "../directives/modal/sdc-modal";
import {FileOpenerDirective} from "../directives/file-opener/file-opener";
import {FileUploadDirective} from "../directives/file-upload/file-upload";
import {StructureTreeDirective} from "../directives/structure-tree/structure-tree-directive";
import {SmartTooltipDirective} from "../directives/utils/smart-tooltip/smart-tooltip";
import {TagDirective} from "../directives/tag/tag-directive";
import {SdcTagsDirective} from "../directives/utils/sdc-tags/sdc-tags";
import {SdcKeyboardEventsDirective} from "../directives/utils/sdc-keyboard-events/sdc-keyboard-events";
import {ExpandCollapseMenuBoxDirective} from "../directives/utils/expand-collapse-menu-box/expand-collaps-menu-box";
import {PunchOutDirective} from "../directives/punch-out/punch-out";
import {CustomValidationDirective} from "../directives/custom-validation/custom-validation";
import {EcompHeaderDirective} from "../directives/ecomp-header/ecomp-header";
import {EditNamePopoverDirective} from "../directives/edit-name-popover/edit-name-popover-directive";
import {DataTypeFieldsStructureDirective} from "../directives/property-types/data-type-fields-structure/data-type-fields-structure";
import {TypeMapDirective} from "../directives/property-types/type-map/type-map-directive";
import {TypeListDirective} from "../directives/property-types/type-list/type-list-directive";
import {SelectDataTypeFieldsStructureDirective} from "../directives/select-property-types/select-data-type-fields-structure/select-data-type-fields-structure";
import {SelectTypeMapDirective} from "../directives/select-property-types/select-type-map/select-type-map-directive";
import {SelectTypeListDirective} from "../directives/select-property-types/select-type-list/select-type-list-directive";
import {ValidationOnLoadDirective} from "../directives/utils/validation-on-load/validation-on-load";
import {InfoTooltipDirective} from "../directives/info-tooltip/info-tooltip";
import {SdcTabsDirective} from "../directives/sdc-tabs/sdc-tabs-directive";
import {SdcSingleTabDirective, InnerSdcSingleTabDirective} from "../directives/sdc-tabs/sdc-single-tab/sdc-single-tab-directive";
import {ExpandCollapseListHeaderDirective} from "../directives/utils/expand-collapse-list-header/expand-collapse-list-header";
import {JsonExportExcelDirective} from "../directives/export-json-to-excel/export-json-to-excel";
import {TopProgressDirective} from "../directives/layout/top-progress/top-progress";
import {CheckboxElementDirective} from "../directives/elements/checkbox/checkbox";
import {RadiobuttonElementDirective} from "../directives/elements/radiobutton/radiobutton";
import {OnLastRepeatDirective} from "../directives/events/on-last-repeat/on-last-repeat";
import {InputRowDirective} from "../directives/inputs-and-properties/inputs/input-row-directive";
import {PropertyRowDirective} from "../directives/inputs-and-properties/properties/property-row-directive";
import {NodesFactory} from "../models/graph/nodes/nodes-factory";
import {LinksFactory} from "../models/graph/graph-links/links-factory";
import {ImageCreatorService} from "../directives/graphs-v2/image-creator/image-creator.service";
import {Palette} from "../directives/graphs-v2/palette/palette.directive";
import {CompositionGraph} from "../directives/graphs-v2/composition-graph/composition-graph.directive";
import {DeploymentGraph} from "../directives/graphs-v2/deployment-graph/deployment-graph.directive";
import {CommonGraphUtils} from "../directives/graphs-v2/common/common-graph-utils";
import {CompositionGraphNodesUtils} from "../directives/graphs-v2/composition-graph/utils/composition-graph-nodes-utils";
import {CompositionGraphGeneralUtils} from "../directives/graphs-v2/composition-graph/utils/composition-graph-general-utils";
import {CompositionGraphLinkUtils} from "../directives/graphs-v2/composition-graph/utils/composition-graph-links-utils";
import {DeploymentGraphGeneralUtils} from "../directives/graphs-v2/deployment-graph/deployment-utils/deployment-graph-general-utils";
import {CompositionGraphPaletteUtils} from "../directives/graphs-v2/composition-graph/utils/composition-graph-palette-utils";
import {CompositionGraphZoneUtils} from "../directives/graphs-v2/composition-graph/utils/composition-graph-zone-utils";
import {MatchCapabilitiesRequirementsUtils} from "../directives/graphs-v2/composition-graph/utils/match-capability-requierment-utils";
import {CapabilitiesListDirective} from "../directives/capabilities-and-requirements/capability/capabilities-list-directive";
import {RequirementsListDirective} from "../directives/capabilities-and-requirements/requirement/requirements-list-directive";
import {ServicePathGraphUtils} from "../directives/graphs-v2/composition-graph/utils/composition-graph-service-path-utils";
import {PreventDoubleClickDirective} from "../directives/prevent-double-click/prevent-double-click";

let moduleName:string = 'Sdc.Directives';
let directiveModule:ng.IModule = angular.module(moduleName, []);

directiveModule.directive('clickedOutside', ClickedOutsideDirective.factory);
directiveModule.directive('loader', LoaderDirective.factory);
directiveModule.directive('userHeaderDetails', UserHeaderDetailsDirective.factory);
directiveModule.directive('ellipsis', EllipsisDirective.factory);
directiveModule.directive('downloadArtifact', DownloadArtifactDirective.factory);
directiveModule.directive('fileType', FileTypeDirective.factory);
directiveModule.directive('invalidCharacters', InvalidCharactersDirective.factory);
directiveModule.directive('perfectScrollbar', PerfectScrollerDirective.factory);
directiveModule.directive('expandCollapse', ExpandCollapseDirective.factory);
directiveModule.directive('ng1Modal', SdcModalDirective.factory);
directiveModule.directive('fileOpener', FileOpenerDirective.factory);
directiveModule.directive('fileUpload', FileUploadDirective.factory);
directiveModule.directive('structureTree', StructureTreeDirective.factory);
directiveModule.directive('sdcSmartTooltip', SmartTooltipDirective.factory);
directiveModule.directive('sdcTag', TagDirective.factory);
directiveModule.directive('sdcTags', SdcTagsDirective.factory);
directiveModule.directive('sdcKeyboardEvents', SdcKeyboardEventsDirective.factory);
directiveModule.directive('expandCollapseMenuBox', ExpandCollapseMenuBoxDirective.factory);
directiveModule.directive('punchOut', PunchOutDirective.factory);
directiveModule.directive('customValidation', CustomValidationDirective.factory);
directiveModule.directive('ecompHeader', EcompHeaderDirective.factory);
directiveModule.directive('editNamePopover', EditNamePopoverDirective.factory);
directiveModule.directive('fieldsStructure', DataTypeFieldsStructureDirective.factory);
directiveModule.directive('typeMap', TypeMapDirective.factory);
directiveModule.directive('typeList', TypeListDirective.factory);
directiveModule.directive('selectFieldsStructure', SelectDataTypeFieldsStructureDirective.factory);
directiveModule.directive('selectTypeMap', SelectTypeMapDirective.factory);
directiveModule.directive('selectTypeList', SelectTypeListDirective.factory);
directiveModule.directive('infoTooltip', InfoTooltipDirective.factory);
directiveModule.directive('validationOnLoad', ValidationOnLoadDirective.factory);
directiveModule.directive('ng1Tabs', SdcTabsDirective.factory);
directiveModule.directive('sdcSingleTab', SdcSingleTabDirective.factory);
directiveModule.directive('innerSdcSingleTab', InnerSdcSingleTabDirective.factory);
directiveModule.directive('jsonExportExcel', JsonExportExcelDirective.factory);
directiveModule.directive('expandCollapseListHeader', ExpandCollapseListHeaderDirective.factory);
directiveModule.directive('preventDoubleClick', PreventDoubleClickDirective.factory);
//
//
// // Layouts
directiveModule.directive('topProgress', TopProgressDirective.factory);
//
// // Elements
directiveModule.directive('ng1Checkbox', CheckboxElementDirective.factory);
directiveModule.directive('sdcRadioButton', RadiobuttonElementDirective.factory);
//
// // Events
directiveModule.directive('onLastRepeat', OnLastRepeatDirective.factory);
//
// //Inputs & Properties
directiveModule.directive('inputRow', InputRowDirective.factory);
directiveModule.directive('propertyRow', PropertyRowDirective.factory);
//
//
// // ------------------------------------------- Composition & Deployment Graphs------------------------------------------//
//
// //Util service for Graph
directiveModule.service('NodesFactory', NodesFactory);
directiveModule.service('LinksFactory', LinksFactory);
directiveModule.service('ImageCreatorService', ImageCreatorService);
//
// //composition
directiveModule.directive('palette', Palette.factory);
directiveModule.directive('compositionGraph', CompositionGraph.factory);

//
// //deployment
directiveModule.directive('deploymentGraph', DeploymentGraph.factory);
//
// //Graph Utils - Common
directiveModule.service('CommonGraphUtils', CommonGraphUtils);
//
// //Composition Graph Utils
directiveModule.service('CompositionGraphNodesUtils', CompositionGraphNodesUtils);
directiveModule.service('CompositionGraphGeneralUtils', CompositionGraphGeneralUtils);
directiveModule.service('CompositionGraphLinkUtils', CompositionGraphLinkUtils);
directiveModule.service('CompositionGraphPaletteUtils', CompositionGraphPaletteUtils);
directiveModule.service('CompositionGraphZoneUtils', CompositionGraphZoneUtils);
directiveModule.service('MatchCapabilitiesRequirementsUtils', MatchCapabilitiesRequirementsUtils);
directiveModule.service('ServicePathGraphUtils', ServicePathGraphUtils);

//
// //Deployment Graph Utils
directiveModule.service('DeploymentGraphGeneralUtils', DeploymentGraphGeneralUtils);


//Compoisiton right tab directives
directiveModule.directive('capabilitiesList', CapabilitiesListDirective.factory);
directiveModule.directive('requirementsList', RequirementsListDirective.factory);


// *** NG2 Components (downgraded) *** //
import { downgradeComponent } from "@angular/upgrade/static";
import { MenuListNg2Component } from "../ng2/components/downgrade-wrappers/menu-list-ng2/menu-list-ng2.component";
import { TopNavComponent } from "../ng2/components/layout/top-nav/top-nav.component";
import { ZoneContainerComponent } from "../ng2/components/ui/canvas-zone/zone-container.component";
import { ZoneInstanceComponent } from "../ng2/components/ui/canvas-zone/zone-instance/zone-instance.component";
import { CompositionPanelComponent } from 'app/ng2/pages/composition/panel/panel.component';
import { CompositionPanelHeaderComponent } from 'app/ng2/pages/composition/panel/panel-header/panel-header.component';
import { PropertiesAssignmentComponent } from "../ng2/pages/properties-assignment/properties-assignment.page.component";
import { SearchWithAutoCompleteComponent } from "../ng2/components/ui/search-with-autocomplete/search-with-autocomplete.component";
import { PalettePopupPanelComponent } from "../ng2/components/ui/palette-popup-panel/palette-popup-panel.component";
import { ServicePathComponent } from '../ng2/components/logic/service-path/service-path.component';
import { ServicePathSelectorComponent } from '../ng2/components/logic/service-path-selector/service-path-selector.component';
import { ServiceDependenciesComponent } from '../ng2/components/logic/service-dependencies/service-dependencies.component';
import { MultilineEllipsisComponent } from "../ng2/shared/multiline-ellipsis/multiline-ellipsis.component";
import { InterfaceOperationComponent } from '../ng2/pages/interface-operation/interface-operation.page.component';
import { PluginFrameComponent } from "../ng2/components/ui/plugin/plugin-frame.component";
import { TileComponent } from "../ng2/components/ui/tile/tile.component";


directiveModule.directive('menuListNg2', downgradeComponent({
    component: MenuListNg2Component,
    inputs: ['props']
}) as angular.IDirectiveFactory);

directiveModule.directive('topNav', downgradeComponent({
    component: TopNavComponent,
    inputs: ['version', 'menuModel', 'topLvlSelectedIndex', 'hideSearch', 'searchTerm', 'notificationIconCallback', 'unsavedChanges', 'unsavedChangesCallback'],
    outputs: ['searchTermChange']
}) as ng.IDirectiveFactory);

directiveModule.directive('ng2ZoneContainer', downgradeComponent({
    component: ZoneContainerComponent,
        inputs: ['title', 'count', 'type', 'visible', 'minimized'],
    outputs: ['minimize', 'backgroundClick']
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2ZoneInstance', downgradeComponent({
        component: ZoneInstanceComponent,
        inputs: ['zoneInstance', 'isActive', 'activeInstanceMode', 'defaultIconText', 'isViewOnly', 'hidden', 'forceSave'],
        outputs: ['modeChange', 'tagHandleClick', 'assignmentSaveStart', 'assignmentSaveComplete']
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2CompositionPanel', downgradeComponent({
    component: CompositionPanelComponent,
    inputs: ['isViewOnly', 'isLoading', 'isCertified', 'selectedZoneInstanceId', 'selectedZoneInstanceType', 'selectedZoneInstanceName', 'topologyTemplate'],
}) as angular.IDirectiveFactory);

directiveModule.directive('propertiesAssignment', downgradeComponent({
    component: PropertiesAssignmentComponent
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2SearchWithAutocomplete', downgradeComponent({
    component: SearchWithAutoCompleteComponent,
    inputs: ['searchPlaceholder', 'searchBarClass', 'autoCompleteValues'],
    outputs: ['searchChanged', 'searchButtonClicked']
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2PalettePopupPanel', downgradeComponent({
    component: PalettePopupPanelComponent,
    inputs: [],
    outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2ServicePath', downgradeComponent({
    component: ServicePathComponent,
    inputs: ['onCreate', 'service', 'isViewOnly'],
    outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2ServicePathSelector', downgradeComponent({
    component: ServicePathSelectorComponent,
    inputs: ['drawPath', 'deletePaths', 'service', 'selectedPathId'],
    outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2ServiceDependencies', downgradeComponent({
    component: ServiceDependenciesComponent,
    inputs: ['compositeService', 'currentServiceInstance', 'selectedInstanceProperties', 'selectedInstanceSiblings', 'selectedInstanceConstraints', 'readonly'],
    outputs: ['updateRulesListEvent', 'loadRulesListEvent','dependencyStatus']
}) as angular.IDirectiveFactory);

directiveModule.directive('interfaceOperation', downgradeComponent({
    component: InterfaceOperationComponent,
    inputs: ['component', 'readonly'],
    outputs: []
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2MultilineEllipsis', downgradeComponent({
    component: MultilineEllipsisComponent,
    inputs: ['lines', 'lineHeight', 'className'],
    outputs: ['hasEllipsisChanged']
}) as angular.IDirectiveFactory);

directiveModule.directive('ng2UiTile', downgradeComponent({
    component: TileComponent,
    inputs: ['component'],
    outputs: ['onTileClick']
}) as angular.IDirectiveFactory);

directiveModule.directive('pluginFrame', downgradeComponent( {
    component: PluginFrameComponent,
    inputs: ['plugin', 'queryParams'],
    outputs: ['onLoadingDone']
}) as angular.IDirectiveFactory);
