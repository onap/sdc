/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

'use strict';
import {Component, DataTypeModel, IAppMenu, IUserProperties} from 'app/models';
import {MenuItem, MenuItemGroup, WorkspaceMode} from 'app/utils';
import {ProgressService} from 'app/services';
import {ComponentServiceNg2} from '../../ng2/services/component-services/component.service';

/**
 * The AngularJS workspace $scope contract that the workspace state's shim controller (app.ts)
 * exposes and that the remaining AngularJS workspace child tabs (properties, management-workflow,
 * network-call-flow) and the type-workspace Angular components consume via $scope inheritance.
 *
 * Extracted from the (now removed) WorkspaceViewModel controller, which was dead code after the
 * Phase 2 migration to WorkspaceContainerComponent. The interface survives because it is still the
 * shared scope contract; the controller class did not.
 */
export interface IWorkspaceViewModelScope extends ng.IScope {

    isLoading: boolean;
    isCreateProgress: boolean;
    component: Component;
    dataType: DataTypeModel;
    originComponent: Component;
    componentType: string;
    importFile: any;
    leftBarTabs: MenuItemGroup;
    isNew: boolean;
    isFromImport: boolean;
    isValidForm: boolean;
    isActiveTopBar: boolean;
    mode: WorkspaceMode;
    breadcrumbsModel: Array<MenuItemGroup>;
    sdcMenu: IAppMenu;
    changeLifecycleStateButtons: any;
    version: string;
    versionsList: Array<any>;
    changeVersion: any;
    isComposition: boolean;
    isDeployment: boolean;
    isPlugins: boolean;
    $state: ng.ui.IStateService;
    user: IUserProperties;
    thirdParty: boolean;
    disabledButtons: boolean;
    menuComponentTitle: string;
    progressService: ProgressService;
    progressMessage: string;
    ComponentServiceNg2: ComponentServiceNg2;
    unsavedChanges: boolean;
    unsavedChangesCallback: Function;
    unsavedFile: boolean;
    hasNoDependencies: boolean;
    models: Array<string>;

    startProgress(message: string): void;
    stopProgress(): void;
    updateBreadcrumbs(component: Component): void;
    updateUnsavedFileFlag(isUnsaved: boolean): void;
    showChangeStateButton(): boolean;
    getComponent(): Component;
    setComponent(component: Component): void;
    setOriginComponent(component: Component): void;
    onMenuItemPressed(state: string, params: any): ng.IPromise<boolean>;
    create(): void;
    save(): Promise<void>;
    setValidState(isValid: boolean): void;
    changeLifecycleState(state: string): void;
    handleChangeLifecycleState(state: string, newCsarVersion?: string, errorFunction?: Function): void;
    disableMenuItems(): void;
    enableMenuItems(): void;
    isDesigner(): boolean;
    isViewMode(): boolean;
    isEditMode(): boolean;
    isCreateMode(): boolean;
    isDisableMode(): boolean;
    isGeneralView(): boolean;
    goToBreadcrumbHome(): void;
    onVersionChanged(selectedId: string): void;
    getLatestVersion(): void;
    getStatus(): string;
    showLifecycleIcon(): boolean;
    updateSelectedMenuItem(state: string): void;
    isSelected(menuItem: MenuItem): boolean;
    uploadFileChangedInGeneralTab(): void;
    updateMenuComponentName(ComponentName: string): void;
    getTabTitle(): string;
    reload(component: Component): void;
}
