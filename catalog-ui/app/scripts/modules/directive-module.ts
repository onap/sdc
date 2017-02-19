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
/// <reference path="../references"/>
module Sdc {
    let moduleName:string = 'Sdc.Directives';
    let directiveModule:ng.IModule = angular.module(moduleName, []);

    directiveModule.directive('clickedOutside', Directives.ClickedOutsideDirective.factory);
    directiveModule.directive('loader', Directives.LoaderDirective.factory);
    directiveModule.directive('userHeaderDetails', Directives.UserHeaderDetailsDirective.factory);
    directiveModule.directive('ellipsis', Directives.EllipsisDirective.factory);
    directiveModule.directive('downloadArtifact', Directives.DownloadArtifactDirective.factory);
    directiveModule.directive('fileType', Directives.FileTypeDirective.factory);
    directiveModule.directive('invalidCharacters', Directives.InvalidCharactersDirective.factory);
    directiveModule.directive('tutorial', Directives.TutorialDirective.factory);
    directiveModule.directive('perfectScrollbar', Directives.PerfectScrollerDirective.factory);
    directiveModule.directive('expandCollapse', Directives.ExpandCollapseDirective.factory);
    directiveModule.directive('sdcModal', Directives.SdcModalDirective.factory);
    directiveModule.directive('sdcMessages', Directives.SdcMessagesDirective.factory);
    directiveModule.directive('sdcMessage', Directives.SdcMessageDirective.factory);
    directiveModule.directive('sdcErrorTooltip', Directives.SdcErrorTooltipDirective.factory);
    directiveModule.directive('fileOpener', Directives.FileOpenerDirective.factory);
    directiveModule.directive('fileUpload', Directives.FileUploadDirective.factory);
    directiveModule.directive('structureTree', Directives.StructureTreeDirective.factory);
    directiveModule.directive('sdcWizardStep', Directives.SdcWizardStepDirective.factory);
    directiveModule.directive('sdcPageSelector', Directives.PageSelectorDirective.factory);
    directiveModule.directive('sdcSmartTooltip', Directives.SmartTooltipDirective.factory);
    directiveModule.directive('printGraphScreen', Directives.PrintGraphScreenDirective.factory);
    directiveModule.directive('sdcTag', Directives.TagDirective.factory);
    directiveModule.directive('sdcTags', Directives.SdcTagsDirective.factory);
    directiveModule.directive('sdcKeyboardEvents', Directives.SdcKeyboardEventsDirective.factory);
    directiveModule.directive('expandCollapseMenuBox', Directives.ExpandCollapseMenuBoxDirective.factory);
    directiveModule.directive('sdcPageScroll', Directives.SdcPageScrollDirective.factory);
    directiveModule.directive('punchOut', Directives.PunchOutDirective.factory);
    directiveModule.directive('relationMenu', Directives.RelationMenuDirective.factory);
    directiveModule.directive('customValidation', Directives.CustomValidationDirective.factory);
    directiveModule.directive('ecompHeader', Directives.EcompHeaderDirective.factory);
    directiveModule.directive('editNamePopover', Directives.EditNamePopoverDirective.factory);
    directiveModule.directive('fieldsStructure', Directives.DataTypeFieldsStructureDirective.factory);
    directiveModule.directive('typeMap', Directives.TypeMapDirective.factory);
    directiveModule.directive('typeList', Directives.TypeListDirective.factory);
    directiveModule.directive('infoTooltip', Directives.InfoTooltipDirective.factory);

    directiveModule.directive('sdcTabs', Directives.SdcTabsDirective.factory);
    directiveModule.directive('sdcSingleTab', Directives.SdcSingleTabDirective.factory);
    directiveModule.directive('innerSdcSingleTab', Directives.InnerSdcSingleTabDirective.factory);

    //composition
    directiveModule.directive('palette', Directives.Palette.factory);
    directiveModule.directive('compositionGraph', Directives.CompositionGraph.factory);

    //deployment
    directiveModule.directive('deploymentGraph', Directives.DeploymentGraph.factory);

    // Layouts
    directiveModule.directive('topNav', Directives.TopNavDirective.factory);
    directiveModule.directive('topProgress', Directives.TopProgressDirective.factory);

    // Elements
    directiveModule.directive('sdcCheckbox', Directives.CheckboxElementDirective.factory);
    directiveModule.directive('sdcRadioButton', Directives.RadiobuttonElementDirective.factory);

    //Graph Utils - Common 
    directiveModule.service('CommonGraphUtils', Sdc.Graph.Utils.CommonGraphUtils);

    //Composition Graph Utils 
    directiveModule.service('CompositionGraphNodesUtils', Sdc.Graph.Utils.CompositionGraphNodesUtils);
    directiveModule.service('CompositionGraphGeneralUtils', Sdc.Graph.Utils.CompositionGraphGeneralUtils);
    directiveModule.service('CompositionGraphLinkUtils', Sdc.Graph.Utils.CompositionGraphLinkUtils);
    directiveModule.service('MatchCapabilitiesRequirementsUtils', Sdc.Graph.Utils.MatchCapabilitiesRequirementsUtils);

    //Composition Graph Utils
    directiveModule.service('DeploymentGraphGeneralUtils', Sdc.Graph.Utils.DeploymentGraphGeneralUtils);
    
    //Util service for graph
    directiveModule.service('NodesFactory', Sdc.Utils.NodesFactory);
    directiveModule.service('LinksFactory', Sdc.Utils.LinksFactory);
    directiveModule.service('ImageCreatorService', Sdc.Utils.ImageCreatorService);

    //directiveModule.service('GraphUtilsServerUpdateQueue', Sdc.Directives.GraphUtilsServerUpdateQueue);

    //controller for go.js
    directiveModule.controller('SdcWizardStepDirectiveController', Directives.SdcWizardStepDirectiveController);

    // Events
    directiveModule.directive('onLastRepeat', Directives.OnLastRepeatDirective.factory);
}


