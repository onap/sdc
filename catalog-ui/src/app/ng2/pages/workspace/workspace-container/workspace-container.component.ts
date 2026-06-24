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

import {Component as NgComponent, ChangeDetectionStrategy, ChangeDetectorRef, Inject, OnInit, OnDestroy} from '@angular/core';
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
    styleUrls: ['./workspace-container.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkspaceContainerComponent implements OnInit, OnDestroy {

    injectComponent: Component;

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

    sdcMenu: IAppMenu;
    private role: string;
    private category: string;
    private components: Component[];
    private componentFactory: any;
    private menuHandler: any;
    private changeLifecycleStateHandler: any;
    private progressService: any;
    private $state: any;
    private $stateParams: any;
    private notification: any;
    private $filter: any;
    private $rootScope: any;
    private deregisterStateChange: Function;

    constructor(
        private cacheService: CacheService,
        private eventListenerService: EventListenerService,
        private homeService: HomeService,
        private catalogService: CatalogService,
        private componentServiceNg2: ComponentServiceNg2,
        private automatedUpgradeService: AutomatedUpgradeService,
        private eventBusService: EventBusService,
        private modalServiceSdcUI: SdcUiServices.ModalService,
        private pluginsService: PluginsService,
        private workspaceNg1BridgeService: WorkspaceNg1BridgeService,
        private workspaceService: WorkspaceService,
        private translateService: TranslateService,
        private navigationService: NavigationService,
        private cdr: ChangeDetectorRef,
        @Inject('$injector') private $injector: any
    ) {
    }

    ngOnInit(): void {
        this.resolveNg1Services();
        // Expose the shell's action methods so the AngularJS shim controller can delegate
        // create / save / changeLifecycleState calls that child tabs make via $scope inheritance.
        this.workspaceService.containerActions = this;
        const comp = this.injectComponent || this.workspaceService.component;
        if (comp) {
            this.injectComponent = comp;
            this.initWorkspace();
            this.cdr.detectChanges();
        }
    }

    private resolveNg1Services(): void {
        this.$state = this.$injector.get('$state');
        this.$stateParams = this.$injector.get('$stateParams');
        this.sdcMenu = this.$injector.get('sdcMenu');
        this.componentFactory = this.$injector.get('ComponentFactory');
        this.menuHandler = this.$injector.get('MenuHandler');
        this.changeLifecycleStateHandler = this.$injector.get('ChangeLifecycleStateHandler');
        this.progressService = this.$injector.get('Sdc.Services.ProgressService');
        this.notification = this.$injector.get('Notification');
        this.$filter = this.$injector.get('$filter');
        this.$rootScope = this.$injector.get('$rootScope');
    }

    // The General-tab form fields use AngularJS ng-model with `data-ng-model-options="{debounce:500}"`
    // (name, description, vendor*, etc.). Our Angular Create handler runs OUTSIDE the AngularJS
    // digest, so a Selenium-speed "fill fields then click Create" leaves the last-entered fields'
    // debounced writes uncommitted: the component is POSTed without e.g. description -> backend 400
    // (description required) -> create fails fast -> the loader only flashes for tens of ms and
    // LoaderHelper.waitForLoader misses it (surfacing as a misleading "loader not visible" timeout).
    // A plain $digest does NOT flush a pending debounce ($timeout-based); calling $commitViewValue()
    // on each ng-model control DOES. Find the General form (editForm) via the rendered DOM scope and
    // commit all controls so this.component is current before create() reads/submits it.
    private commitPendingFormValues(): void {
        try {
            const ng = (window as any).angular;
            if (!ng) { return; }
            const formEl = document.querySelector('form[name="editForm"]');
            const scope = formEl ? ng.element(formEl).scope() : null;
            const form = scope ? scope['editForm'] : null;
            if (form) {
                Object.keys(form).forEach((k) => {
                    const ctrl = form[k];
                    if (ctrl && typeof ctrl.$commitViewValue === 'function') {
                        ctrl.$commitViewValue();
                    }
                });
            }
            if (this.$rootScope && !this.$rootScope.$$phase) {
                this.$rootScope.$digest();
            }
        } catch (e) { /* best-effort: if no form/digest in flight, the model is already current */ }
    }

    private initWorkspace(): void {
        const raw = this.injectComponent;
        if (!raw) { return; }
        this.component = (typeof raw.isService === 'function') ? raw : this.componentFactory.createComponent(raw);
        if (!this.component) { return; }
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
        // Keep the highlighted sidebar item + tab title in sync as the user moves between child
        // tab states (General, TOSCA Artifacts, ...). The old WorkspaceViewModel did this via
        // $scope.$on('$stateChangeSuccess'); the shell re-mounts only on full reloads, so without
        // this the title/selection stay stuck on the initial tab (e.g. Selenium waiting for the
        // 'TOSCA Artifacts' tab-title after navigating there would time out).
        this.deregisterStateChange = this.$rootScope.$on('$stateChangeSuccess', () => {
            this.isComposition = (this.$state.current.name.indexOf(States.WORKSPACE_COMPOSITION) > -1);
            this.isDeployment = this.$state.current.name === States.WORKSPACE_DEPLOYMENT;
            this.updateSelectedMenuItem(this.$state.current.name);
            this.cdr.detectChanges();
        });
    }

    ngOnDestroy(): void {
        this.eventListenerService.unRegisterObserver(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES);
        if (this.deregisterStateChange) { this.deregisterStateChange(); }
        if (this.workspaceService.containerActions === this) {
            this.workspaceService.containerActions = undefined;
        }
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

    // Mirrors the AngularJS `testsId` filter (tests-id-filter.ts) the old workspace template used
    // for lifecycle-button data-tests-id values, so Selenium locators like //button[@data-tests-id='certify']
    // still match (e.g. 'Certify' -> 'certify', 'Check in' -> 'check_in').
    testsId(text: string): string {
        return text ? text.replace(/\s/g, '_').toLowerCase() : text;
    }

    // --- Mode query methods ---

    isViewMode(): boolean {
        return this.mode === WorkspaceMode.VIEW;
    }

    isDesigner(): boolean {
        return this.role === Role.DESIGNER;
    }

    isCreateMode(): boolean {
        return this.mode === WorkspaceMode.CREATE;
    }

    isDisableMode(): boolean {
        return this.mode === WorkspaceMode.VIEW;
    }

    showLifecycleIcon(): boolean {
        return this.isDesigner();
    }

    showLatestVersion(): boolean {
        return this.component && this.component.isLatestVersion();
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

    // --- Progress / loader helpers (drive the shell <sdc-loader> spinners) ---
    // ChangeLifecycleStateHandler.changeLifecycleState(component, data, scope, ...) sets
    // scope.isLoading directly; we pass `this` as that scope so the shell loader reacts.

    startProgress = (message: string): void => {
        this.progressService.initCreateComponentProgress(this.component.uniqueId);
        this.isCreateProgress = true;
        this.progressMessage = message;
    }

    stopProgress = (): void => {
        this.isCreateProgress = false;
        this.progressService.deleteProgressValue(this.component.uniqueId);
    }

    private disableMenuItems(): void {
        this.leftBarTabs.menuItems.forEach((item: MenuItem) => {
            item.isDisabled = (States.WORKSPACE_GENERAL !== item.state);
        });
    }

    private enableMenuItems(): void {
        this.leftBarTabs.menuItems.forEach((item: MenuItem) => {
            item.isDisabled = false;
        });
    }

    private showSuccessNotificationMessage(): void {
        this.notification.success({
            message: this.$filter('translate')('IMPORT_VF_MESSAGE_CREATE_FINISHED_DESCRIPTION'),
            title: this.$filter('translate')('IMPORT_VF_MESSAGE_CREATE_FINISHED_TITLE')
        });
    }

    isGeneralView(): boolean {
        return this.$state.current.name === States.WORKSPACE_GENERAL;
    }

    // --- Create (the shell's Create button in CREATE mode) ---

    create = (): void => {
        this.commitPendingFormValues(); // flush debounced ng-model writes before reading this.component
        this.startProgress('Creating Asset...');
        _.first(this.leftBarTabs.menuItems).isDisabled = true; // disable General tab during create (DE246274)
        this.cdr.detectChanges(); // render the create loader before the (async) server call

        if (this.component.isResource() && (this.component as Resource).csarUUID) {
            this.notification.info({
                message: this.$filter('translate')('IMPORT_VF_MESSAGE_CREATE_TAKES_LONG_TIME_DESCRIPTION'),
                title: this.$filter('translate')('IMPORT_VF_MESSAGE_CREATE_TAKES_LONG_TIME_TITLE')
            });
        }

        const onFailed = () => {
            this.stopProgress();
            this.isLoading = false;
            _.first(this.leftBarTabs.menuItems).isDisabled = false;
            this.eventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_SAVE_BUTTON_ERROR);
            this.component.tags = _.without(this.component.tags, this.component.name); // DE246217
            this.cdr.detectChanges();
        };

        const onSuccessCreate = (component: Component) => {
            // Keep the create loader visible through the navigation to the created asset — the
            // destination state re-resolves and re-mounts the shell with isCreateProgress=false,
            // which clears it. Calling stopProgress() here would clear the loader synchronously,
            // and on a fast backend it can vanish within a single Selenium poll interval
            // (LoaderHelper.waitForLoader would then never catch it visible). We still delete the
            // progress-service entry to avoid a leak, but leave isCreateProgress=true.
            this.progressService.deleteProgressValue(this.component.uniqueId);
            this.showSuccessNotificationMessage();
            // this.components is only populated when arriving from dashboard/catalog (initBreadcrumbs);
            // for the import path (no previousState) it is undefined — guard to avoid an NPE that
            // would abort navigation and leave the loader stuck.
            this.components = this.components || [];
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

    // --- Lifecycle state change (Certify / Check in / Check out / etc.) ---

    changeLifecycleState = (state: string): void => {
        if (this.isGeneralView() && state !== 'deleteVersion') {
            // Let the General tab save first, then it calls back handleChangeLifecycleState via $scope.
            this.eventListenerService.notifyObservers(EVENTS.ON_LIFECYCLE_CHANGE_WITH_SAVE, state);
        } else {
            this.handleChangeLifecycleState(state);
        }
    }

    handleChangeLifecycleState = (state: string, newCsarVersion?: string, onError?: Function): void => {
        if ('monitor' === state) {
            this.$state.go('workspace.distribution');
            return;
        }

        let data = this.changeLifecycleStateButtons[state];
        if (!data && this.$stateParams.componentCsar && !this.isCreateMode()) {
            data = {text: 'Check Out', url: 'lifecycleState/CHECKOUT'};
        }

        const defaultActionAfterChangeLifecycleState = (): void => {
            if (this.$state.current.data && this.$state.current.data.unsavedChanges) {
                this.$state.current.data.unsavedChanges = false;
            }
            this.$state.go('dashboard');
        };

        const onSuccess = (component: Component, url: string): void => {
            const eventData: any = {uuid: this.component.uuid, version: this.component.version};
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
                            message: this.$filter('translate')('CHECKOUT_SUCCESS_MESSAGE_TEXT'),
                            title: this.$filter('translate')('CHECKOUT_SUCCESS_MESSAGE_TITLE')
                        });
                        this.cdr.detectChanges();
                    });
                    break;
                case 'lifecycleState/CHECKIN':
                    this.workspaceNg1BridgeService.updateIsViewOnly(true);
                    defaultActionAfterChangeLifecycleState();
                    this.notification.success({
                        message: this.$filter('translate')('CHECKIN_SUCCESS_MESSAGE_TEXT'),
                        title: this.$filter('translate')('CHECKIN_SUCCESS_MESSAGE_TITLE')
                    });
                    break;
                case 'lifecycleState/UNDOCHECKOUT':
                    this.eventBusService.notify('UNDO_CHECK_OUT', eventData, false).subscribe(() => {
                        defaultActionAfterChangeLifecycleState();
                        this.notification.success({
                            message: this.$filter('translate')('DELETE_SUCCESS_MESSAGE_TEXT'),
                            title: this.$filter('translate')('DELETE_SUCCESS_MESSAGE_TITLE')
                        });
                    });
                    break;
                case 'lifecycleState/certify':
                    // Refresh in place rather than $state.go(reload:true). A parent-state reload
                    // tears down and asynchronously re-mounts this downgraded OnPush shell, leaving
                    // a window where .w-sdc-main-right-container is absent from the DOM; Selenium's
                    // post-certify ComponentPage.isLoaded() (5s) hits that gap and fails. The certify
                    // response + handleCertification already update this.component/version/buttons in
                    // place, and the next tab navigation (e.g. goToToscaArtifacts) loads fresh data,
                    // so the parent reload is unnecessary here and only introduced the race.
                    this.component.lifecycleState = component.lifecycleState;
                    this.handleCertification(component);
                    this.verifyIfDependenciesExist();
                    this.cdr.detectChanges();
                    break;
                case 'distribution/PROD/activate':
                    this.notification.success({
                        message: this.$filter('translate')('DISTRIBUTE_SUCCESS_MESSAGE_TEXT'),
                        title: this.$filter('translate')('DISTRIBUTE_SUCCESS_MESSAGE_TITLE')
                    });
                    this.initChangeLifecycleStateButtons();
                    break;
                default:
                    defaultActionAfterChangeLifecycleState();
            }
            if (url !== 'lifecycleState/CHECKOUT') {
                this.isLoading = false;
            }
            this.cdr.detectChanges();
        };

        // The handler sets `scope.isLoading` (true before the call, false on completion); pass `this`.
        this.changeLifecycleStateHandler.changeLifecycleState(this.component, data, this, onSuccess, onError);
        this.cdr.detectChanges();
    }

    private handleCertification(certifyComponent: Component): void {
        if (this.component.getComponentSubType() === ResourceType.VF || this.component.isService()) {
            this.componentServiceNg2.getDependencies(this.component.componentType, this.component.uniqueId).subscribe((response: IDependenciesServerResponse[]) => {
                this.isLoading = false;
                const isUpgradeNeeded = response.filter((c) => c.dependencies && c.dependencies.length > 0);
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

    private onSuccessWithoutUpgradeNeeded(): void {
        this.isLoading = false;
        this.notification.success({
            message: this.$filter('translate')('SERVICE_CERTIFICATION_STATUS_TEXT'),
            title: this.$filter('translate')('SERVICE_CERTIFICATION_STATUS_TITLE')
        });
        this.initVersionObject();
        this.initChangeLifecycleStateButtons();
        this.cdr.detectChanges();
    }

    private refreshDataAfterChangeLifecycleState(component: Component): void {
        this.isLoading = false;
        this.mode = this.initViewMode();
        this.initChangeLifecycleStateButtons();
        this.initVersionObject();
        this.cdr.detectChanges();
    }

    private reload(component: Component): void {
        const isGeneralTab = this.$state.current.name === States.WORKSPACE_GENERAL;
        if (isGeneralTab) {
            this.$state.go(this.$state.current.name, {id: component.uniqueId, componentCsar: null}, {reload: true});
        } else {
            this.$state.go(this.$state.current.name, {id: component.uniqueId}, {reload: true});
        }
    }

    // --- Version handling ---

    private initVersionObject(): void {
        this.versionsList = (this.component.getAllVersionsAsSortedArray()).reverse();
        this.changeVersion = {
            selectedVersion: _.find(this.versionsList, (versionObj) => {
                return versionObj.versionNumber === this.component.version;
            })
        };
    }

    onVersionChanged(versionId: string): void {
        const eventData = {
            uuid: this.component.uuid,
            version: versionId
        };
        this.eventBusService.notify('VERSION_CHANGED', eventData).subscribe(() => {
            this.$state.go(this.$state.current.name, {
                id: versionId,
                type: this.component.componentType.toLowerCase(),
                components: this.components
            });
        });
    }

    getLatestVersion(): void {
        if (this.component.isLatestVersion()) { return; }
        const latestVersionId = _.last(_.keys(this.component.allVersions));
        this.$state.go(this.$state.current.name, {
            id: latestVersionId,
            type: this.component.componentType.toLowerCase()
        });
    }

    // --- Status display ---

    getStatus(): string {
        if (this.isCreateMode()) { return 'IN DESIGN'; }
        if (this.component.isService() && this.component.lifecycleState === 'CERTIFIED') {
            return this.sdcMenu.DistributionStatuses[this.component.distributionStatus] ?
                this.sdcMenu.DistributionStatuses[this.component.distributionStatus].name : this.component.distributionStatus;
        }
        return this.sdcMenu.LifeCycleStatuses[this.component.lifecycleState] ?
            this.sdcMenu.LifeCycleStatuses[this.component.lifecycleState].text : this.component.lifecycleState;
    }

    getTabTitle(): string {
        return this.leftBarTabs && this.leftBarTabs.menuItems && this.leftBarTabs.selectedIndex >= 0 ?
            this.leftBarTabs.menuItems[this.leftBarTabs.selectedIndex].text : '';
    }

    // --- Navigation ---

    goToBreadcrumbHome(): void {
        const bcState = this.cacheService.get('breadcrumbsComponentsState');
        if (bcState === 'weightedCatalog' || bcState === 'catalog') {
            this.navigationService.navigate('catalog');
        } else {
            this.navigationService.navigate('dashboard');
        }
    }

    onMenuItemPressed(state: string, params?: any): void {
        this.$state.go(state, params || {});
    }

    // --- Menu Items ---

    private updateSelectedMenuItem(stateName: string): void {
        if (!this.leftBarTabs) { return; }
        const stateNameShort = stateName.replace('workspace.', '');
        const selectedItem: MenuItem = _.find(this.leftBarTabs.menuItems, (item: MenuItem) => {
            return item.state === stateName || item.state === stateNameShort;
        });
        let selectedIndex = selectedItem ? this.leftBarTabs.menuItems.indexOf(selectedItem) : 0;
        if (this.isComposition || this.isDeployment || this.isPlugins) {
            selectedIndex = -1;
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

    // --- Misc helpers ---

    private setWorkspaceButtonState = (newState: boolean, callback?: Function) => {
        this.unsavedChanges = newState;
        this.unsavedChangesCallback = callback;
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
}
