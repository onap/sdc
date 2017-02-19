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
  let moduleName: string = 'Sdc.ViewModels';
  let viewModelModule: ng.IModule = angular.module(moduleName, []);

  viewModelModule
    .controller(moduleName+'.DashboardViewModel', ViewModels.DashboardViewModel)
    .controller(moduleName+'.CompositionViewModel', ViewModels.CompositionViewModel)

    .controller(moduleName+'.DetailsViewModel', ViewModels.DetailsViewModel)
    .controller(moduleName+'.ResourceArtifactsViewModel', ViewModels.ResourceArtifactsViewModel)
    .controller(moduleName+'.PropertyFormViewModel', ViewModels.PropertyFormViewModel)
    .controller(moduleName+'.ArtifactResourceFormViewModel', ViewModels.ArtifactResourceFormViewModel)
    .controller(moduleName+'.AttributeFormViewModel', ViewModels.AttributeFormViewModel)
    .controller(moduleName+'.ResourcePropertiesViewModel', ViewModels.ResourcePropertiesViewModel)
    .controller(moduleName+'.CatalogViewModel', ViewModels.CatalogViewModel)
    .controller(moduleName+'.OnboardVendorViewModel', ViewModels.OnboardVendorViewModel)
    .controller(moduleName+'.DistributionViewModel', ViewModels.DistributionViewModel)
    .controller(moduleName+'.SupportViewModel', ViewModels.SupportViewModel)
    .controller(moduleName+'.ConfirmationModalViewModel', ViewModels.ConfirmationModalViewModel)
    .controller(moduleName+'.EmailModalViewModel', ViewModels.EmailModalViewModel)
    .controller(moduleName+'.MessageModalViewModel', ViewModels.MessageModalViewModel)
    .controller(moduleName+'.ServerMessageModalViewModel', ViewModels.ServerMessageModalViewModel)
    .controller(moduleName+'.ClientMessageModalViewModel', ViewModels.ClientMessageModalViewModel)
    .controller(moduleName+'.ErrorViewModel', ViewModels.ErrorViewModel)
    .controller(moduleName+'.ComponentViewerViewModel', ViewModels.ComponentViewerViewModel)
    .controller(moduleName+'.RelationsViewModel', ViewModels.RelationsViewModel)
    .controller(moduleName+'.ResourceInstanceNameViewModel', ViewModels.ResourceInstanceNameViewModel)
    .controller(moduleName+'.WelcomeViewModel', ViewModels.WelcomeViewModel)
    .controller(moduleName+'.PreLoadingViewModel', ViewModels.PreLoadingViewModel)
    .controller(moduleName+'.TutorialEndViewModel', ViewModels.TutorialEndViewModel)
    .controller(moduleName+'.AdminDashboardViewModel', ViewModels.AdminDashboardViewModel)
    .controller(moduleName+'.EnvParametersFormViewModel', ViewModels.EnvParametersFormViewModel)
    .controller(moduleName+'.StructureViewModel', ViewModels.StructureViewModel)
    .controller(moduleName+'.AddCategoryModalViewModel', ViewModels.AddCategoryModalViewModel)
    .controller(moduleName+'.DashboardCoverViewModel', ViewModels.DashboardCoverViewModel)
    .controller(moduleName+'.UserManagementViewModel', ViewModels.UserManagementViewModel)
    .controller(moduleName+'.CategoryManagementViewModel', ViewModels.CategoryManagementViewModel)
    .controller(moduleName+'.WelcomeStepsControllerViewModel', ViewModels.WelcomeStepsControllerViewModel)
    .controller(moduleName+'.OnboardingModalViewModel', ViewModels.OnboardingModalViewModel)
    .controller(moduleName+'.DistributionStatusModalViewModel', ViewModels.DistributionStatusModalViewModel)

    .controller(moduleName+'.Wizard.EditWizardViewModel', ViewModels.Wizard.EditWizardViewModel)
    .controller(moduleName+'.Wizard.CreateWizardViewModel', ViewModels.Wizard.CreateWizardViewModel)
    .controller(moduleName+'.Wizard.ImportWizardViewModel', ViewModels.Wizard.ImportWizardViewModel)
    .controller(moduleName+'.Wizard.GeneralStepViewModel', ViewModels.Wizard.GeneralStepViewModel)
    .controller(moduleName+'.Wizard.IconsStepViewModel', ViewModels.Wizard.IconsStepViewModel)
    .controller(moduleName+'.Wizard.ArtifactInformationStepViewModel', ViewModels.Wizard.ArtifactInformationStepViewModel)
    .controller(moduleName+'.Wizard.ArtifactDeploymentStepViewModel', ViewModels.Wizard.ArtifactDeploymentStepViewModel)
    .controller(moduleName+'.Wizard.PropertiesStepViewModel', ViewModels.Wizard.PropertiesStepViewModel)
    .controller(moduleName+'.Wizard.ArtifactResourceFormStepViewModel', ViewModels.Wizard.ArtifactResourceFormStepViewModel)
    .controller(moduleName+'.Wizard.PropertyFormViewModel', ViewModels.Wizard.PropertyFormViewModel)
    .controller(moduleName+'.Wizard.HierarchyStepViewModel',ViewModels.Wizard.HierarchyStepViewModel)

     //NEW
    .controller(moduleName+'.WorkspaceViewModel', ViewModels.WorkspaceViewModel)
    .controller(moduleName+'.GeneralViewModel', ViewModels.GeneralViewModel)
    .controller(moduleName+'.IconsViewModel', ViewModels.IconsViewModel)
    .controller(moduleName+'.DeploymentArtifactsViewModel', ViewModels.DeploymentArtifactsViewModel)
    .controller(moduleName+'.InformationArtifactsViewModel', ViewModels.InformationArtifactsViewModel)
    .controller(moduleName+'.ToscaArtifactsViewModel', ViewModels.ToscaArtifactsViewModel)
    .controller(moduleName+'.PropertiesViewModel', ViewModels.PropertiesViewModel)
    .controller(moduleName+'.AttributesViewModel', ViewModels.AttributesViewModel)
    .controller(moduleName+'.ProductHierarchyViewModel',ViewModels.ProductHierarchyViewModel)
    .controller(moduleName+'.ActivityLogViewModel',ViewModels.ActivityLogViewModel)
    .controller(moduleName+'.ManagementWorkflowViewModel',ViewModels.ManagementWorkflowViewModel)
    .controller(moduleName+'.NetworkCallFlowViewModel',ViewModels.NetworkCallFlowViewModel)
    .controller(moduleName+'.DeploymentViewModel',ViewModels.DeploymentViewModel)
    .controller(moduleName+'.ResourceInputsViewModel',ViewModels.ResourceInputsViewModel)
    .controller(moduleName+'.ServiceInputsViewModel', ViewModels.ServiceInputsViewModel)
    .controller(moduleName+'.ReqAndCapabilitiesViewModel', ViewModels.ReqAndCapabilitiesViewModel)



    //TABS
    .controller(moduleName+'.HierarchyViewModel',ViewModels.HierarchyViewModel)
}
