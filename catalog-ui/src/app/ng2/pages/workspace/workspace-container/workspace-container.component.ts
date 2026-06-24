/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import {Component as NgComponent, Inject, Input, OnInit, OnDestroy} from '@angular/core';
import * as _ from 'lodash';

import {Component, IAppMenu, IUserProperties, Plugin, PluginsConfiguration, Resource, Service} from 'app/models';
import {
    CHANGE_COMPONENT_CSAR_VERSION_FLAG,
    ChangeLifecycleStateHandler,
    ComponentFactory,
    ComponentState,
    EVENTS,
    MenuHandler,
    MenuItem,
    MenuItemGroup,
    PREVIOUS_CSAR_COMPONENT,
    ResourceType,
    Role,
    States,
    WorkspaceMode
} from 'app/utils';
import {EventListenerService, ProgressService} from 'app/services';
import {CacheService} from 'app/services-ng2';
import {SdcUiCommon, SdcUiComponents, SdcUiServices} from 'onap-ui-angular';
import {AutomatedUpgradeService} from '../../automated-upgrade/automated-upgrade.service';
import {CatalogService} from '../../../services/catalog.service';
import {ComponentServiceNg2} from '../../../services/component-services/component.service';
import {EventBusService} from '../../../services/event-bus.service';
import {HomeService} from '../../../services/home.service';
import {PluginsService} from '../../../services/plugins.service';
import {IDependenciesServerResponse} from '../../../services/responses/dependencies-server-response';
import {WorkspaceNg1BridgeService} from '../workspace-ng1-bridge-service';
import {WorkspaceService} from '../workspace.service';
import {TranslateService} from '../../../shared/translator/translate.service';
import {NavigationService} from '../../../services/navigation.service';

@NgComponent({
    selector: 'workspace-container',
    templateUrl: './workspace-container.component.html',
    styleUrls: ['./workspace-container.component.less']
})
export class WorkspaceContainerComponent implements OnInit, OnDestroy {

    @Input() injectComponent: Component;

    component: Component;
    originComponent: Component;
    componentType: string;
    leftBarTabs: MenuItemGroup;
    isLoading: boolean = false;
    isCreateProgress: boolean = false;
    isValidForm: boolean = true;
    mode: WorkspaceMode;
    breadcrumbsModel: Array<MenuItemGroup>;
    changeLifecycleStateButtons: any;
    version: string;
    versionsList: Array<any>;
    changeVersion: any;
    isComposition: boolean = false;
    isDeployment: boolean = false;
    isPlugins: boolean = false;
    user: IUserProperties;
    disabledButtons: boolean = false;
    menuComponentTitle: string;
    progressMessage: string;
    unsavedChanges: boolean = false;
    unsavedChangesCallback: Function;
    unsavedFile: boolean = false;
    hasNoDependencies: boolean = true;
    lifecycleButtonEntries: Array<{key: string, value: any}> = [];
    progressValue: number = 0;

    private role: string;
    private category: string;
    private components: Component[];
    private sdcMenu: IAppMenu;

    constructor(
        @Inject('$state') private $state: any,
        @Inject('$stateParams') private $stateParams: any,
        @Inject('ComponentFactory') private componentFactory: ComponentFactory,
        @Inject('sdcMenu') sdcMenu: IAppMenu,
        @Inject('MenuHandler') private menuHandler: MenuHandler,
        private cacheService: CacheService,
        @Inject('ChangeLifecycleStateHandler') private changeLifecycleStateHandler: ChangeLifecycleStateHandler,
        private eventListenerService: EventListenerService,
        @Inject('Notification') private notification: any,
        private homeService: HomeService,
        private catalogService: CatalogService,
        @Inject('Sdc.Services.ProgressService') private progressService: ProgressService,
        private componentServiceNg2: ComponentServiceNg2,
        private automatedUpgradeService: AutomatedUpgradeService,
        private eventBusService: EventBusService,
        private modalServiceSdcUI: SdcUiServices.ModalService,
        private pluginsService: PluginsService,
        private workspaceNg1BridgeService: WorkspaceNg1BridgeService,
        private workspaceService: WorkspaceService,
        private translateService: TranslateService,
        private navigationService: NavigationService
    ) {
        this.sdcMenu = sdcMenu;
    }

    ngOnInit(): void {
        this.component = this.injectComponent;
        this.menuComponentTitle = this.component.name;
        this.originComponent = this.componentFactory.createComponent(this.component);
        this.componentType = this.component.componentType;
        this.version = this.cacheService.get('version');
        this.user = this.cacheService.get('user');
        this.role = this.user.role;
        this.category = this.component.selectedCategory;
        this.mode = this.initViewMode();
        this.initChangeLifecycleStateButtons();
        this.initVersionObject();
        this.isComposition = (this.$state.current.name.indexOf(States.WORKSPACE_COMPOSITION) > -1);
        this.isDeployment = this.$state.current.name === States.WORKSPACE_DEPLOYMENT;
        this.initMenuItems();
        this.verifyIfDependenciesExist();
        this.updateSelectedMenuItem(this.$state.current.name);

        this.eventListenerService.registerObserverCallback(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES, this.setWorkspaceButtonState);
    }

    ngOnDestroy(): void {
        this.eventListenerService.unRegisterObserver(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES);
    }

    // --- Mode determination ---

    private initViewMode(): WorkspaceMode {
        let mode = WorkspaceMode.VIEW;
        if (!this.$stateParams.id) {
            mode = WorkspaceMode.CREATE;
        } else {
            if (this.component.lifecycleState === ComponentState.NOT_CERTIFIED_CHECKOUT &&
                this.component.lastUpdaterUserId === this.user.userId) {
                if ((this.component.isService() || this.component.isResource()) && this.role === Role.DESIGNER) {
                    mode = WorkspaceMode.EDIT;
                }
            }
        }
        this.workspaceNg1BridgeService.updateIsViewOnly(mode === WorkspaceMode.VIEW);
        return mode;
    }

    // --- Lifecycle state buttons ---

    private initChangeLifecycleStateButtons(): void {
        let state: string;
        if (this.component.isService() && this.component.lifecycleState === 'CERTIFIED') {
            state = this.component.distributionStatus;
        } else {
            state = this.component.lifecycleState;
        }
        this.changeLifecycleStateButtons = (this.sdcMenu.roles[this.role].changeLifecycleStateButtons[state] || [])[this.component.componentType.toUpperCase()];
        this.updateLifecycleButtonEntries();
    }

    private updateLifecycleButtonEntries(): void {
        if (!this.changeLifecycleStateButtons) {
            this.lifecycleButtonEntries = [];
            return;
        }
        this.lifecycleButtonEntries = Object.keys(this.changeLifecycleStateButtons).map(key => ({
            key, value: this.changeLifecycleStateButtons[key]
        }));
    }

    trackByKey(index: number, entry: {key: string, value: any}): string {
        return entry.key;
    }

    // --- Mode query methods ---

    isViewMode(): boolean {
        return this.mode === WorkspaceMode.VIEW;
    }

    isEditMode(): boolean {
        return this.mode === WorkspaceMode.EDIT;
    }

    isCreateMode(): boolean {
        return this.mode === WorkspaceMode.CREATE;
    }

    isDesigner(): boolean {
        return this.role === Role.DESIGNER;
    }

    isDisableMode(): boolean {
        return this.mode === WorkspaceMode.VIEW && this.component.lifecycleState === ComponentState.NOT_CERTIFIED_CHECKIN;
    }

    isGeneralView(): boolean {
        return this.$state.current.name === States.WORKSPACE_GENERAL;
    }

    showLifecycleIcon(): boolean {
        return this.role === Role.DESIGNER;
    }

    showLatestVersion(): boolean {
        if (!this.component.isLatestVersion()) {
            return false;
        }
        if (ComponentState.NOT_CERTIFIED_CHECKOUT === this.component.lifecycleState && this.isViewMode()) {
            return false;
        }
        return true;
    }

    getStatus(): string {
        if (this.isCreateMode()) {
            return 'IN DESIGN';
        }
        return this.component.getStatus(this.sdcMenu);
    }

    getTabTitle(): string {
        const currentState = this.$state.current.name;
        const found = this.leftBarTabs.menuItems.find((item: MenuItem) => {
            return item.state === currentState;
        });
        return found ? found.text : '';
    }

    isSelected(menuItem: MenuItem): boolean {
        return this.leftBarTabs.selectedIndex === _.indexOf(this.leftBarTabs.menuItems, menuItem);
    }

    // --- Button disable logic ---

    checkDisableButton(button: any): boolean {
        if (this.isCreateMode() || button.disabled || this.disabledButtons || !this.isValidForm || this.unsavedChanges || this.component.isArchived) {
            return true;
        }
        if (button.url === 'lifecycleState/CHECKOUT') {
            return !this.component.isLatestVersion();
        }
        return false;
    }

    // --- Version handling ---

    private initVersionObject(): void {
        this.versionsList = (this.component.getAllVersionsAsSortedArray()).reverse();
        this.changeVersion = {
            selectedVersion: _.find(this.versionsList, (versionObj) => {
                return versionObj.versionId === this.component.uniqueId;
            })
        };
    }

    onVersionChanged(selectedId: string): void {
        if (this.isGeneralView() && this.unsavedChanges) {
            this.changeVersion.selectedVersion = _.find(this.versionsList, (versionObj) => {
                return versionObj.versionId === this.component.uniqueId;
            });
        }

        const eventData = {
            uuid: this.component.uuid,
            version: this.changeVersion.selectedVersion.versionNumber
        };

        this.eventBusService.notify('VERSION_CHANGED', eventData).subscribe(() => {
            this.isLoading = true;
            this.$state.go(this.$state.current.name, {
                id: selectedId,
                type: this.componentType.toLowerCase(),
                mode: WorkspaceMode.VIEW,
                components: this.$stateParams.components
            }, {reload: true});
        });
    }

    getLatestVersion(): void {
        this.onVersionChanged(_.first(this.versionsList).versionId);
    }

    // --- Create / Save ---

    create(): void {
        this.startProgress('Creating Asset...');
        _.first(this.leftBarTabs.menuItems).isDisabled = true;

        if (this.component.isResource() && (this.component as Resource).csarUUID) {
            this.notification.info({
                message: this.translateService.translate('IMPORT_VF_MESSAGE_CREATE_TAKES_LONG_TIME_DESCRIPTION'),
                title: this.translateService.translate('IMPORT_VF_MESSAGE_CREATE_TAKES_LONG_TIME_TITLE')
            });
        }

        const onFailed = () => {
            this.stopProgress();
            this.isLoading = false;
            _.first(this.leftBarTabs.menuItems).isDisabled = false;
            this.eventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_SAVE_BUTTON_ERROR);
            this.component.tags = _.without(this.component.tags, this.component.name);
            this.isValidForm = true;
        };

        const onSuccessCreate = (component: Component) => {
            this.stopProgress();
            this.showSuccessNotificationMessage();
            this.components.unshift(component);
            this.$state.go(States.WORKSPACE_GENERAL, {
                id: component.uniqueId,
                type: component.componentType.toLowerCase(),
                components: this.components
            }, {inherit: false});
        };

        if ((this.component as Service).serviceType === 'Service') {
            this.componentFactory.importComponentOnServer(this.component).then(onSuccessCreate, onFailed);
        } else {
            this.componentFactory.createComponentOnServer(this.component).then(onSuccessCreate, onFailed);
        }
    }

    save(): Promise<void> {
        this.eventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_SAVE_BUTTON_CLICK);
        this.startProgress('Updating Asset...');
        this.disableMenuItems();

        return new Promise<void>((resolve, reject) => {
            const stopProgressAndEnableUI = () => {
                this.disabledButtons = false;
                this.isLoading = false;
                this.enableMenuItems();
                this.stopProgress();
            };

            const onFailed = () => {
                stopProgressAndEnableUI();
                this.unsavedFile = true;
                this.eventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_SAVE_BUTTON_ERROR);
                reject();
            };

            const onSuccessUpdate = (component: Component) => {
                stopProgressAndEnableUI();
                this.showSuccessNotificationMessage();
                component.tags = _.reject(component.tags, (item) => item === component.name);
                this.updateBreadcrumbs(component);
                this.component = component;
                this.originComponent = this.componentFactory.createComponent(this.component);

                if (this.cacheService.contains(CHANGE_COMPONENT_CSAR_VERSION_FLAG)) {
                    this.cacheService.remove(CHANGE_COMPONENT_CSAR_VERSION_FLAG);
                }
                if (this.cacheService.contains(PREVIOUS_CSAR_COMPONENT)) {
                    this.cacheService.remove(PREVIOUS_CSAR_COMPONENT);
                }

                this.unsavedChanges = false;
                this.unsavedFile = false;
                this.reload(component);
                resolve();
            };

            this.component.updateComponent().then(onSuccessUpdate, onFailed);
        });
    }

    // --- Lifecycle state changes ---

    changeLifecycleState(state: string): void {
        if (this.isGeneralView() && state !== 'deleteVersion') {
            this.eventListenerService.notifyObservers(EVENTS.ON_LIFECYCLE_CHANGE_WITH_SAVE, state);
        } else {
            this.handleChangeLifecycleState(state);
        }
    }

    handleChangeLifecycleState(state: string, newCsarVersion?: string, onError?: Function): void {
        if ('monitor' === state) {
            this.$state.go('workspace.distribution');
            return;
        }

        let data = this.changeLifecycleStateButtons[state];
        if (!data && this.$stateParams.componentCsar && !this.isCreateMode()) {
            data = {text: 'Check Out', url: 'lifecycleState/CHECKOUT'};
        }

        const defaultAfter = () => {
            this.unsavedChanges = false;
            this.navigationService.navigate('dashboard');
        };

        const onSuccess = (component: Component, url: string): void => {
            const eventData: any = {
                uuid: this.component.uuid,
                version: this.component.version
            };

            this.component.lifecycleState = component.lifecycleState;
            this.component.distributionStatus = component.distributionStatus;

            switch (url) {
                case 'lifecycleState/CHECKOUT':
                    this.workspaceNg1BridgeService.updateIsViewOnly(false);
                    this.eventBusService.notify('CHECK_OUT', eventData, false).subscribe(() => {
                        if (newCsarVersion) {
                            this.cacheService.set(CHANGE_COMPONENT_CSAR_VERSION_FLAG, newCsarVersion);
                            (this.component as Resource).csarVersion = newCsarVersion;
                        }
                        const bcIdx = _.findIndex(this.components, (item) => item.uuid === component.uuid);
                        if (bcIdx !== -1) {
                            this.components[bcIdx] = component;
                        } else {
                            this.components.unshift(component);
                        }
                        this.mode = this.initViewMode();
                        this.initChangeLifecycleStateButtons();
                        this.initVersionObject();
                        this.isLoading = false;
                        this.eventListenerService.notifyObservers(EVENTS.ON_CHECKOUT, component);
                        this.workspaceService.setComponentMetadata(component.componentMetadata);

                        this.notification.success({
                            message: this.translateService.translate('CHECKOUT_SUCCESS_MESSAGE_TEXT'),
                            title: this.translateService.translate('CHECKOUT_SUCCESS_MESSAGE_TITLE')
                        });
                    });
                    break;
                case 'lifecycleState/CHECKIN':
                    this.workspaceNg1BridgeService.updateIsViewOnly(true);
                    defaultAfter();
                    this.notification.success({
                        message: this.translateService.translate('CHECKIN_SUCCESS_MESSAGE_TEXT'),
                        title: this.translateService.translate('CHECKIN_SUCCESS_MESSAGE_TITLE')
                    });
                    break;
                case 'lifecycleState/UNDOCHECKOUT':
                    this.eventBusService.notify('UNDO_CHECK_OUT', eventData, false).subscribe(() => {
                        defaultAfter();
                        this.notification.success({
                            message: this.translateService.translate('DELETE_SUCCESS_MESSAGE_TEXT'),
                            title: this.translateService.translate('DELETE_SUCCESS_MESSAGE_TITLE')
                        });
                    });
                    break;
                case 'lifecycleState/certify':
                    this.handleCertification(component);
                    this.verifyIfDependenciesExist();
                    this.reload(component);
                    break;
                case 'distribution/PROD/activate':
                    this.notification.success({
                        message: this.translateService.translate('DISTRIBUTE_SUCCESS_MESSAGE_TEXT'),
                        title: this.translateService.translate('DISTRIBUTE_SUCCESS_MESSAGE_TITLE')
                    });
                    this.initChangeLifecycleStateButtons();
                    break;
                default:
                    defaultAfter();
            }
            if (data.url !== 'lifecycleState/CHECKOUT') {
                this.isLoading = false;
            }
        };

        this.changeLifecycleStateHandler.changeLifecycleState(this.component, data, this, onSuccess);
    }

    handleCertification(certifyComponent: Component): void {
        if (this.component.getComponentSubType() === ResourceType.VF || this.component.isService()) {
            this.componentServiceNg2.getDependencies(this.component.componentType, this.component.uniqueId).subscribe((response: Array<IDependenciesServerResponse>) => {
                this.isLoading = false;
                const isUpgradeNeeded = _.filter(response, (componentToUpgrade: IDependenciesServerResponse) => {
                    return componentToUpgrade.dependencies && componentToUpgrade.dependencies.length > 0;
                });
                if (isUpgradeNeeded.length === 0) {
                    this.onSuccessWithoutUpgradeNeeded();
                    return;
                }
                this.refreshDataAfterChangeLifecycleState(certifyComponent);
                this.automatedUpgradeService.openAutomatedUpgradeModal(response, this.component, true);
            });
        } else {
            this.onSuccessWithoutUpgradeNeeded();
        }
    }

    // --- Archive / Restore / Delete ---

    archiveComponent(): void {
        this.isLoading = true;
        this.componentServiceNg2.archiveComponent(this.componentType, this.component.uniqueId).subscribe(() => {
            this.isLoading = false;
            const previousState = this.$stateParams.previousState;
            if (previousState === 'catalog' || previousState === 'dashboard') {
                this.navigationService.navigate(previousState);
            }
            this.component.isArchived = true;
            this.deleteArchiveCache();
            this.notification.success({
                message: this.component.name + ' ' + this.translateService.translate('ARCHIVE_SUCCESS_MESSAGE_TEXT'),
                title: this.translateService.translate('ARCHIVE_SUCCESS_MESSAGE_TITLE')
            });
        }, () => { this.isLoading = false; });
    }

    restoreComponent(): void {
        this.isLoading = true;
        this.componentServiceNg2.restoreComponent(this.componentType, this.component.uniqueId).subscribe(() => {
            this.isLoading = false;
            this.notification.success({
                message: this.component.name + ' ' + this.translateService.translate('RESTORE_SUCCESS_MESSAGE_TEXT'),
                title: this.translateService.translate('RESTORE_SUCCESS_MESSAGE_TITLE')
            });
            this.reload(this.component);
        });
        this.component.isArchived = false;
        this.deleteArchiveCache();
    }

    deleteArchivedComponent(): void {
        const modalTitle: string = this.translateService.translate('COMPONENT_VIEW_DELETE_MODAL_TITLE');
        const modalMessage: string = this.translateService.translate('COMPONENT_VIEW_DELETE_MODAL_TEXT');
        const modalButton = {
            testId: 'ok-button',
            text: this.sdcMenu.alertMessages.okButton,
            type: SdcUiCommon.ButtonType.warning,
            callback: () => this.handleDeleteArchivedComponent(),
            closeModal: true
        } as SdcUiComponents.ModalButtonComponent;
        this.modalServiceSdcUI.openWarningModal(modalTitle, modalMessage, 'alert-modal', [modalButton]);
    }

    handleDeleteArchivedComponent(): void {
        this.isLoading = true;
        this.componentServiceNg2.deleteComponent(this.componentType, this.component.uniqueId).subscribe(() => {
            this.deleteArchiveCache();
            this.notification.success({
                message: this.component.name + ' ' + this.translateService.translate('DELETE_SUCCESS_MESSAGE_TEXT'),
                title: this.translateService.translate('DELETE_SUCCESS_MESSAGE_TITLE')
            });
            const previousState = this.$stateParams.previousState;
            if (previousState === 'catalog' || previousState === 'dashboard') {
                this.navigationService.navigate(previousState);
            } else {
                this.navigationService.navigate('dashboard');
            }
            this.isLoading = false;
        }, () => {
            this.notification.error({
                message: this.component.name + ' ' + this.translateService.translate('DELETE_FAILURE_MESSAGE_TEXT'),
                title: this.translateService.translate('DELETE_FAILURE_MESSAGE_TITLE')
            });
            this.isLoading = false;
        });
    }

    // --- Menu / Navigation ---

    onMenuItemPressed(state: string, params?: any): void {
        this.$state.go(state, Object.assign({
            id: this.component.uniqueId,
            type: this.component.componentType.toLowerCase(),
            components: this.components
        }, params));
    }

    updateMenuComponentName(name: string): void {
        this.menuComponentTitle = name;
    }

    setValidState(isValid: boolean): void {
        this.isValidForm = isValid;
    }

    uploadFileChangedInGeneralTab(): void {
        // Placeholder for CSAR update scenarios
    }

    goToBreadcrumbHome(): void {
        if (this.breadcrumbsModel && this.breadcrumbsModel.length > 0) {
            const bcHome: MenuItemGroup = this.breadcrumbsModel[0];
            this.navigationService.navigate(bcHome.menuItems[bcHome.selectedIndex].state);
        }
    }

    openAutomatedUpgradeModal(): void {
        this.isLoading = true;
        this.componentServiceNg2.getDependencies(this.component.componentType, this.component.uniqueId).subscribe((response: Array<IDependenciesServerResponse>) => {
            this.isLoading = false;
            this.automatedUpgradeService.openAutomatedUpgradeModal(response, this.component, false);
        });
    }

    // --- Progress ---

    startProgress(message: string): void {
        this.progressService.initCreateComponentProgress(this.component.uniqueId);
        this.isCreateProgress = true;
        this.progressMessage = message;
    }

    stopProgress(): void {
        this.isCreateProgress = false;
        this.progressService.deleteProgressValue(this.component.uniqueId);
    }

    // --- Menu enable/disable ---

    disableMenuItems(): void {
        this.leftBarTabs.menuItems.forEach((item: MenuItem) => {
            item.isDisabled = (item.state !== 'general');
        });
    }

    enableMenuItems(): void {
        this.leftBarTabs.menuItems.forEach((item: MenuItem) => {
            item.isDisabled = false;
        });
    }

    // --- Private helpers ---

    updateSelectedMenuItem(state: string): void {
        if (!this.leftBarTabs) { return; }
        const stateArray: Array<string> = state.split('.', 2);
        const stateWithoutInternalNavigate: string = stateArray[0] + '.' + stateArray[1];
        const selectedItem: MenuItem = _.find(this.leftBarTabs.menuItems, (item: MenuItem) => {
            const itemStateArray: Array<string> = item.state.split('.', 2);
            const itemStateWithoutNavigation: string = itemStateArray[0] + '.' + itemStateArray[1];
            return (itemStateWithoutNavigation === stateWithoutInternalNavigate);
        });

        let selectedIndex = selectedItem ? this.leftBarTabs.menuItems.indexOf(selectedItem) : 0;

        if (stateArray[1] === 'plugins') {
            _.forEach(PluginsConfiguration.plugins, (plugin: Plugin) => {
                if (plugin.pluginStateUrl === this.$stateParams.path) {
                    return false;
                } else if (this.pluginsService.isPluginDisplayedInContext(plugin, this.role, this.component.getComponentSubType())) {
                    selectedIndex++;
                }
            });
        }

        this.leftBarTabs.selectedIndex = selectedIndex;
    }

    private initMenuItems(): void {
        const inCreateMode = this.isCreateMode();
        this.leftBarTabs = new MenuItemGroup();
        let menuItemsObjects: any[] = this.updateMenuItemByRole(
            this.sdcMenu.component_workspace_menu_option[this.component.getComponentSubType()], this.role);

        if (this.component.getComponentSubType() === 'SERVICE') {
            menuItemsObjects = this.updateMenuItemByCategory(menuItemsObjects, this.category);
        }

        _.each(PluginsConfiguration.plugins, (plugin: Plugin) => {
            if (this.pluginsService.isPluginDisplayedInContext(plugin, this.role, this.component.getComponentSubType())) {
                menuItemsObjects.push({
                    text: plugin.pluginDisplayOptions['context'].displayName,
                    action: 'onMenuItemPressed',
                    state: 'plugins',
                    params: {path: plugin.pluginStateUrl}
                });
            }
        });

        this.leftBarTabs.menuItems = menuItemsObjects.map((item: any) => {
            const menuItem = new MenuItem(item.text, null, item.state, item.action, item.params, item.blockedForTypes, item.disabledCategory);
            if (menuItem.params) {
                menuItem.params.state = menuItem.state;
            } else {
                menuItem.params = {state: menuItem.state};
            }
            menuItem.callback = (() => { this.onMenuItemPressed(menuItem.state, menuItem.params); }) as any;
            menuItem.isDisabled = (inCreateMode && menuItem.state !== 'general') ||
                (menuItem.state === 'deployment' && this.component.modules
                    && this.component.modules.length === 0 && this.component.isResource()) ||
                (menuItem.disabledCategory === true);
            return menuItem;
        });

        if (this.cacheService.get('breadcrumbsComponents')) {
            this.initBreadcrumbs();
        } else {
            this.initBreadcrumbsComponents();
        }
    }

    private updateMenuItemByRole(menuItems: any[], role: string): any[] {
        return menuItems.filter((item: any) => {
            return !(item.disabledRoles && item.disabledRoles.indexOf(role) > -1);
        });
    }

    private updateMenuItemByCategory(menuItems: any[], category: string): any[] {
        return menuItems.map((item: any) => {
            item.disabledCategory = !!(item.disabledCategories && item.disabledCategories.indexOf(category) > -1);
            return item;
        });
    }

    // --- Breadcrumbs ---

    private initBreadcrumbs(): void {
        this.components = this.cacheService.get('breadcrumbsComponents');
        const breadcrumbsComponentsLvl = this.menuHandler.generateBreadcrumbsModelFromComponents(this.components, this.component);

        if (this.isCreateMode()) {
            const createItem = this.getNewComponentBreadcrumbItem();
            if (!breadcrumbsComponentsLvl.menuItems) {
                breadcrumbsComponentsLvl.menuItems = [];
            }
            breadcrumbsComponentsLvl.menuItems.unshift(createItem);
            breadcrumbsComponentsLvl.selectedIndex = 0;
        }

        this.breadcrumbsModel = [breadcrumbsComponentsLvl, this.leftBarTabs];
    }

    private getNewComponentBreadcrumbItem(): MenuItem {
        let text = '';
        if (this.component.isResource() && (this.component as Resource).isCsarComponent()) {
            text = this.component.getComponentSubType() + ': ' + this.component.name;
        } else {
            text = 'Create new ' + this.$stateParams.type;
        }
        return new MenuItem(text, null, States.WORKSPACE_GENERAL, 'goToState', [this.$stateParams]);
    }

    private initBreadcrumbsComponents(): void {
        let breadcrumbsComponentsObservable;
        const previousState = this.$stateParams.previousState;

        if (previousState === 'dashboard') {
            breadcrumbsComponentsObservable = this.homeService.getAllComponents(true);
        } else if (previousState === 'catalog') {
            breadcrumbsComponentsObservable = this.catalogService.getCatalog();
        } else {
            this.cacheService.remove('breadcrumbsComponentsState');
            this.cacheService.remove('breadcrumbsComponents');
            return;
        }
        breadcrumbsComponentsObservable.subscribe((components) => {
            this.cacheService.set('breadcrumbsComponentsState', previousState);
            this.cacheService.set('breadcrumbsComponents', components);
            this.initBreadcrumbs();
        });
    }

    updateBreadcrumbs(component: Component): void {
        const bcIdx = this.menuHandler.findBreadcrumbComponentIndex(this.components, component);
        if (bcIdx !== -1) {
            this.components[bcIdx] = component;
            this.initBreadcrumbs();
        }
    }

    // --- Misc helpers ---

    private onSuccessWithoutUpgradeNeeded(): void {
        this.isLoading = false;
        this.notification.success({
            message: this.translateService.translate('SERVICE_CERTIFICATION_STATUS_TEXT'),
            title: this.translateService.translate('SERVICE_CERTIFICATION_STATUS_TITLE')
        });
        this.initVersionObject();
        this.initChangeLifecycleStateButtons();
    }

    private refreshDataAfterChangeLifecycleState(component: Component): void {
        this.isLoading = false;
        this.mode = this.initViewMode();
        this.initChangeLifecycleStateButtons();
        this.initVersionObject();
        this.eventListenerService.notifyObservers(EVENTS.ON_LIFECYCLE_CHANGE, component);
    }

    private showSuccessNotificationMessage(): void {
        this.notification.success({
            message: this.translateService.translate('IMPORT_VF_MESSAGE_CREATE_FINISHED_DESCRIPTION'),
            title: this.translateService.translate('IMPORT_VF_MESSAGE_CREATE_FINISHED_TITLE')
        });
    }

    private setWorkspaceButtonState = (newState: boolean, callback?: Function) => {
        this.unsavedChanges = newState;
        this.unsavedChangesCallback = callback;
    }

    private deleteArchiveCache(): void {
        this.cacheService.remove('archiveComponents');
    }

    private verifyIfDependenciesExist(): void {
        if (this.component.componentType && this.component.uniqueId &&
            this.component.lifecycleState === 'CERTIFIED' &&
            (this.component.isService() || this.component.getComponentSubType() === 'VF')) {
            this.componentServiceNg2.getDependencies(this.component.componentType, this.component.uniqueId).subscribe((response: IDependenciesServerResponse[]) => {
                const containsDependencies = response.filter((version) => version.dependencies);
                this.hasNoDependencies = containsDependencies.length === 0;
            });
        }
    }

    reload(component: Component): void {
        const isGeneralTab = this.$state.current.name === States.WORKSPACE_GENERAL;
        if (isGeneralTab) {
            this.$state.go(this.$state.current.name, {id: component.uniqueId, componentCsar: null}, {reload: true});
        } else {
            this.$state.go(this.$state.current.name, {id: component.uniqueId}, {reload: true});
        }
    }
}
