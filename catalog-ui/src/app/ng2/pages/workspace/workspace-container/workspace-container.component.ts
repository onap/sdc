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

import {Component as NgComponent, Inject, Input, OnInit, OnDestroy, OnChanges, SimpleChanges, ChangeDetectionStrategy, ChangeDetectorRef} from '@angular/core';
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
export class WorkspaceContainerComponent implements OnInit, OnDestroy, OnChanges {

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

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['injectComponent'] && this.injectComponent && !this.component) {
            this.resolveServicesAndInit();
        }
    }

    ngOnInit(): void {
        this.resolveNg1Services();
        const comp = this.injectComponent || this.workspaceService.component;
        if (comp) {
            this.injectComponent = comp;
            this.initWorkspace();
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
    }

    private resolveServicesAndInit(): void {
        if (!this.$state) { this.resolveNg1Services(); }
        this.initWorkspace();
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
        this.cdr.markForCheck();
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
