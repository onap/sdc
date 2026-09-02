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

import {Component as NgComponent, ChangeDetectorRef, Inject, OnInit, OnDestroy} from '@angular/core';
import * as _ from 'lodash';

import {IAppMenu} from 'app/models/app-config';
import {Component} from 'app/models/components/component';
import {Resource} from 'app/models/components/resource';
import {Service} from 'app/models/components/service';
import {Plugin, PluginsConfiguration} from 'app/models/plugins-config';
import {IUserProperties} from 'app/models/user';
// Deep-imported rather than taken from the `app/utils` barrel — see the comment on that barrel:
// re-exporting the now-@Injectable handler closed a runtime import cycle through app/models.
import {ChangeLifecycleStateHandler} from 'app/utils/change-lifecycle-state-handler';
import {ComponentFactory} from 'app/utils/component-factory';
import {CHANGE_COMPONENT_CSAR_VERSION_FLAG, ComponentState, EVENTS, ResourceType, Role, States, WorkspaceMode} from 'app/utils/constants';
import {MenuHandler, MenuItem, MenuItemGroup} from 'app/utils/menu-handler';
import {EventListenerService} from 'app/services/event-listener.service';
import {ProgressService} from 'app/services/progress.service';
import {CacheService} from 'app/services/cache.service';
import {SdcUiCommon, SdcUiComponents, SdcUiServices} from 'onap-ui-angular';
import {NotificationSettings} from 'onap-ui-angular/dist/notifications/utilities/notification.config';
import {IModalButtonComponent} from 'onap-ui-angular/dist/common';
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
import {SdcMenuToken} from '../../../config/sdc-menu.config';

/**
 * Deliberately NOT OnPush, unlike most components here. It used to be, harmlessly: as a
 * `downgradeComponent` its host view was attached to ApplicationRef, and `tick()` calls
 * `detectChanges()` on each ROOT view directly, which bypasses the OnPush `ChecksEnabled` gate
 * (core.umd.js: ViewRef_.detectChanges) — so the strategy never actually gated anything.
 *
 * Since CR 2 this shell is activated by a <router-outlet>, i.e. it is a CHILD view, reached through
 * `callViewAction`, which DOES enforce the gate — and an unchecked OnPush parent skips its entire
 * subtree. Every workspace tab was its own downgraded root before and is now a descendant of this
 * component, so keeping OnPush here silently starved all 19 of them: a tab rendered its empty
 * initial state (the click that navigated marked the parents) and then never re-rendered when its
 * own XHRs resolved, because a resolving request marks nothing. Symptom was a permanently empty
 * properties table / interface list.
 */
@NgComponent({
    selector: 'workspace-container',
    templateUrl: './workspace-container.component.html',
    styleUrls: ['./workspace-container.component.less']
})
export class WorkspaceContainerComponent implements OnInit, OnDestroy {

    component: Component;
    originComponent: Component;
    componentType: string;
    leftBarTabs: MenuItemGroup;
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

    isLoading: boolean = false;

    private role: string;
    private category: string;
    private components: Component[];
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
        private notificationsService: SdcUiServices.NotificationsService,
        private cdr: ChangeDetectorRef,
        @Inject(SdcMenuToken) public sdcMenu: IAppMenu,
        private componentFactory: ComponentFactory,
        private menuHandler: MenuHandler,
        private changeLifecycleStateHandler: ChangeLifecycleStateHandler,
        private progressService: ProgressService
    ) {
    }

    ngOnInit(): void {
        // Expose the shell's action methods so the AngularJS shim controller can delegate
        // create / save / changeLifecycleState calls that child tabs make via $scope inheritance.
        this.workspaceService.containerActions = this;
        if (this.workspaceService.component) {
            this.initWorkspace();
            this.cdr.detectChanges();
        }
    }

    private initWorkspace(): void {
        const raw = this.workspaceService.component;
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
        this.updateFullBleedFlags();
        this.initMenuItems();
        this.verifyIfDependenciesExist();
        this.updateSelectedMenuItem(this.navigationService.getCurrentStateName());
        this.eventListenerService.registerObserverCallback(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES, this.setWorkspaceButtonState);
        // Keep the highlighted sidebar item + tab title in sync as the user moves between child
        // tab states (General, TOSCA Artifacts, ...). The old WorkspaceViewModel did this via
        // $scope.$on('$stateChangeSuccess'); the shell re-mounts only on full reloads, so without
        // this the title/selection stay stuck on the initial tab (e.g. Selenium waiting for the
        // 'TOSCA Artifacts' tab-title after navigating there would time out).
        this.deregisterStateChange = this.navigationService.onNavigationSuccess(() => {
            this.updateFullBleedFlags();
            this.updateSelectedMenuItem(this.navigationService.getCurrentStateName());
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
        if (!this.navigationService.getParam('id')) {
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
        // deleteVersion is FILTERED OUT rather than hidden in the template. Both spellings look
        // equivalent, but `[hidden]` leaves a second, 0x0 element carrying
        // data-tests-id="delete_version" in the DOM, and Selenium/Playwright resolve the FIRST
        // match — so the standalone sprite span below it becomes unreachable. The AngularJS
        // original used `ng-if="key != 'deleteVersion'"`, which removed the node outright.
        this.lifecycleButtonEntries = Object.keys(this.changeLifecycleStateButtons)
            .filter((key) => key !== 'deleteVersion')
            .map((key) => ({key, value: this.changeLifecycleStateButtons[key]}));
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

    showUpgradeServicesButton(): boolean {
        return this.isDesigner() && !this.isCreateMode() &&
            this.component.lifecycleState === ComponentState.CERTIFIED &&
            (this.component.isService() || this.component.getComponentSubType() === ResourceType.VF);
    }

    showRestoreButton(): boolean {
        return !this.isCreateMode() && this.component.isArchived;
    }

    showDeleteVersionButton(): boolean {
        return this.isDesigner() && !this.isCreateMode() &&
            this.component.lifecycleState === ComponentState.NOT_CERTIFIED_CHECKOUT && !this.component.isArchived;
    }

    showDeleteArchivedButton(): boolean {
        return !this.isCreateMode() && this.component.isArchived;
    }

    showArchiveButton(): boolean {
        return this.isDesigner() && !this.isCreateMode() &&
            this.component.lifecycleState !== ComponentState.NOT_CERTIFIED_CHECKOUT && !this.component.isArchived;
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

    // configurations/menu.js supplies DOTTED states ('workspace.general') for the 7 component
    // groups and BARE states ('general') only for the DataType group, so every comparison must
    // tolerate both forms.
    private isMenuState(item: MenuItem, shortName: string): boolean {
        return !!item && this.isState(item.state, shortName);
    }

    private isState(state: string, shortName: string): boolean {
        if (!state) { return false; }
        return state === shortName || state === 'workspace.' + shortName;
    }

    private applyCreateModeDisabling(items: MenuItem[], inCreateMode: boolean): void {
        items.forEach((item: MenuItem) => {
            item.isDisabled = (inCreateMode && !this.isMenuState(item, 'general')) ||
                (this.isMenuState(item, 'deployment') && this.component.modules
                    && this.component.modules.length === 0 && this.component.isResource()) ||
                (item.disabledCategory === true);
        });
    }

    private disableMenuItems(): void {
        this.leftBarTabs.menuItems.forEach((item: MenuItem) => {
            item.isDisabled = !this.isMenuState(item, 'general');
        });
    }

    private enableMenuItems(): void {
        this.leftBarTabs.menuItems.forEach((item: MenuItem) => {
            item.isDisabled = false;
        });
    }

    private showSuccessNotificationMessage(): void {
        this.notificationsService.push(new NotificationSettings(
            'success',
            this.translateService.translate('IMPORT_VF_MESSAGE_CREATE_FINISHED_DESCRIPTION'),
            this.translateService.translate('IMPORT_VF_MESSAGE_CREATE_FINISHED_TITLE'),
            5000));
    }

    isGeneralView(): boolean {
        return this.isState(this.navigationService.getCurrentStateName(), 'general');
    }

    // --- Create (the shell's Create button in CREATE mode) ---

    create = (): void => {
        this.startProgress('Creating Asset...');
        _.first(this.leftBarTabs.menuItems).isDisabled = true; // disable General tab during create (DE246274)
        this.cdr.detectChanges(); // render the create loader before the (async) server call

        if (this.component.isResource() && (this.component as Resource).csarUUID) {
            this.notificationsService.push(new NotificationSettings(
                'info',
                this.translateService.translate('IMPORT_VF_MESSAGE_CREATE_TAKES_LONG_TIME_DESCRIPTION'),
                this.translateService.translate('IMPORT_VF_MESSAGE_CREATE_TAKES_LONG_TIME_TITLE'),
                5000));
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
            this.navigationService.navigate(States.WORKSPACE_GENERAL, {
                id: component.uniqueId,
                type: component.componentType.toLowerCase(),
                components: this.components
            }, {inherit: false} as any);
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
            this.navigationService.navigate('workspace.distribution');
            return;
        }

        let data = this.changeLifecycleStateButtons[state];
        if (!data && this.navigationService.getParam('componentCsar') && !this.isCreateMode()) {
            data = {text: 'Check Out', url: 'lifecycleState/CHECKOUT'};
        }

        const defaultActionAfterChangeLifecycleState = (): void => {
            if (this.navigationService.getUnsavedChanges()) {
                this.navigationService.setUnsavedChanges(false);
            }
            this.navigationService.navigate('dashboard');
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
                        this.notificationsService.push(new NotificationSettings(
                            'success',
                            this.translateService.translate('CHECKOUT_SUCCESS_MESSAGE_TEXT'),
                            this.translateService.translate('CHECKOUT_SUCCESS_MESSAGE_TITLE'),
                            5000));
                        this.cdr.detectChanges();
                    });
                    break;
                case 'lifecycleState/CHECKIN':
                    this.workspaceNg1BridgeService.updateIsViewOnly(true);
                    defaultActionAfterChangeLifecycleState();
                    this.notificationsService.push(new NotificationSettings(
                        'success',
                        this.translateService.translate('CHECKIN_SUCCESS_MESSAGE_TEXT'),
                        this.translateService.translate('CHECKIN_SUCCESS_MESSAGE_TITLE'),
                        5000));
                    break;
                case 'lifecycleState/UNDOCHECKOUT':
                    this.eventBusService.notify('UNDO_CHECK_OUT', eventData, false).subscribe(() => {
                        defaultActionAfterChangeLifecycleState();
                        this.notificationsService.push(new NotificationSettings(
                            'success',
                            this.translateService.translate('DELETE_SUCCESS_MESSAGE_TEXT'),
                            this.translateService.translate('DELETE_SUCCESS_MESSAGE_TITLE'),
                            5000));
                    });
                    break;
                case 'lifecycleState/certify':
                    // Refresh in place rather than a reload transition. A parent-state reload
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
                    this.notificationsService.push(new NotificationSettings(
                        'success',
                        this.translateService.translate('DISTRIBUTE_SUCCESS_MESSAGE_TEXT'),
                        this.translateService.translate('DISTRIBUTE_SUCCESS_MESSAGE_TITLE'),
                        5000));
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
        this.notificationsService.push(new NotificationSettings(
            'success',
            this.translateService.translate('SERVICE_CERTIFICATION_STATUS_TEXT'),
            this.translateService.translate('SERVICE_CERTIFICATION_STATUS_TITLE'),
            5000));
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
        const isGeneralTab = this.navigationService.getCurrentStateName() === States.WORKSPACE_GENERAL;
        if (isGeneralTab) {
            this.navigationService.reload({id: component.uniqueId, componentCsar: null});
        } else {
            this.navigationService.reload({id: component.uniqueId});
        }
    }

    // --- Archive / restore / delete-archived ---

    archiveComponent = (): void => {
        this.isLoading = true;
        this.componentServiceNg2.archiveComponent(this.component.componentType, this.component.uniqueId).subscribe(() => {
            this.isLoading = false;
            this.navigateToPreviousState();
            this.component.isArchived = true;
            this.deleteArchiveCache();
            this.notificationsService.push(new NotificationSettings(
                'success',
                this.component.name + ' ' + this.translateService.translate('ARCHIVE_SUCCESS_MESSAGE_TEXT'),
                this.translateService.translate('ARCHIVE_SUCCESS_MESSAGE_TITLE'),
                5000));
            this.cdr.detectChanges();
        }, () => {
            this.isLoading = false;
            this.cdr.detectChanges();
        });
    }

    restoreComponent = (): void => {
        this.isLoading = true;
        this.componentServiceNg2.restoreComponent(this.component.componentType, this.component.uniqueId).subscribe(() => {
            this.isLoading = false;
            this.notificationsService.push(new NotificationSettings(
                'success',
                this.component.name + ' ' + this.translateService.translate('RESTORE_SUCCESS_MESSAGE_TEXT'),
                this.translateService.translate('RESTORE_SUCCESS_MESSAGE_TITLE'),
                5000));
            this.reload(this.component);
        }, () => {
            this.isLoading = false;
            this.cdr.detectChanges();
        });
        // Deliberately OUTSIDE the subscribe, matching the AngularJS original: the flag flips and
        // the cache drops as soon as the request is fired, not when it returns.
        this.component.isArchived = false;
        this.deleteArchiveCache();
    }

    deleteArchivedComponent = (): void => {
        const modalButton = {
            testId: 'ok-button',
            text: this.sdcMenu.alertMessages.okButton,
            type: SdcUiCommon.ButtonType.warning,
            callback: this.handleDeleteArchivedComponent,
            closeModal: true
        } as IModalButtonComponent;
        this.modalServiceSdcUI.openWarningModal(
            this.translateService.translate('COMPONENT_VIEW_DELETE_MODAL_TITLE'),
            this.translateService.translate('COMPONENT_VIEW_DELETE_MODAL_TEXT'),
            'alert-modal',
            // See unsaved-changes-flag.guard.ts: openWarningModal's declared button type
            // contradicts IModalConfig.buttons upstream.
            [modalButton] as any as SdcUiComponents.ModalButtonComponent[]);
    }

    handleDeleteArchivedComponent = (): void => {
        this.isLoading = true;
        this.componentServiceNg2.deleteComponent(this.component.componentType, this.component.uniqueId).subscribe(() => {
            this.deleteArchiveCache();
            this.notificationsService.push(new NotificationSettings(
                'success',
                this.component.name + ' ' + this.translateService.translate('DELETE_SUCCESS_MESSAGE_TEXT'),
                this.translateService.translate('DELETE_SUCCESS_MESSAGE_TITLE'),
                5000));
            this.navigateToPreviousState('dashboard');
            this.isLoading = false;
            this.cdr.detectChanges();
        }, () => {
            this.notificationsService.push(new NotificationSettings(
                'error',
                this.component.name + ' ' + this.translateService.translate('DELETE_FAILURE_MESSAGE_TEXT'),
                this.translateService.translate('DELETE_FAILURE_MESSAGE_TITLE'),
                5000));
            this.isLoading = false;
            this.cdr.detectChanges();
        });
    }

    openAutomatedUpgradeModal = (): void => {
        this.isLoading = true;
        this.componentServiceNg2.getDependencies(this.component.componentType, this.component.uniqueId)
            .subscribe((response: IDependenciesServerResponse[]) => {
                this.isLoading = false;
                this.automatedUpgradeService.openAutomatedUpgradeModal(response, this.component, false);
                this.cdr.detectChanges();
            });
    }

    // The catalog's Archive left-switch view is served from this cache entry, so leaving it in place
    // makes an archived/restored/deleted component keep showing its previous archive state there.
    private deleteArchiveCache(): void {
        this.cacheService.remove('archiveComponents');
    }

    // Archive and delete-archived both leave the current component unviewable, so they return the
    // user to wherever they came from. `fallback` is where delete-archived goes when the previous
    // state is something other than catalog/dashboard; archive passes none and stays put instead.
    // Entering the workspace without a previousState at all navigates nowhere in either case.
    private navigateToPreviousState(fallback?: string): void {
        const previousState = this.navigationService.getParam('previousState');
        if (!previousState) { return; }
        if (previousState === 'catalog' || previousState === 'dashboard') {
            this.navigationService.navigate(previousState);
        } else if (fallback) {
            this.navigationService.navigate(fallback);
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
            // NOTE: deliberately navigate() and not reload() — the original transition passed no
            // {reload: true}, and adding one would tear down/re-resolve the whole workspace state.
            this.navigationService.navigate(this.navigationService.getCurrentStateName(), {
                id: versionId,
                type: this.component.componentType.toLowerCase(),
                components: this.components
            });
        });
    }

    getLatestVersion(): void {
        if (this.component.isLatestVersion()) { return; }
        const latestVersionId = _.last(_.keys(this.component.allVersions));
        // NOTE: navigate(), not reload() — the original transition passed no {reload: true}.
        this.navigationService.navigate(this.navigationService.getCurrentStateName(), {
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
        this.navigationService.navigate(state, params || {});
    }

    /**
     * The three full-bleed states: they suppress the tab-title header and deselect the left bar.
     * Kept in ONE place because the two callers (ngOnInit and the onNavigationSuccess handler) must
     * agree — `isPlugins` was previously assigned in neither, so it stayed false forever and plugin
     * tabs grew a title header the AngularJS shell never showed them
     * (workspace-view-model.ts:760 set it on every state change; the port dropped that line while
     * keeping its two neighbours). workspace.less subtracts @tab_title for the same three states, so
     * a flag that never goes true also means a 110px band of dead space.
     */
    private updateFullBleedFlags(): void {
        const stateName = this.navigationService.getCurrentStateName();
        // indexOf, not ===, for composition alone: its state name carries a panel-tab suffix
        // ('workspace.composition.details'), whereas the other two are always exact.
        this.isComposition = stateName.indexOf(States.WORKSPACE_COMPOSITION) > -1;
        this.isDeployment = stateName === States.WORKSPACE_DEPLOYMENT;
        this.isPlugins = stateName === States.WORKSPACE_PLUGINS;
    }

    // --- Menu Items ---

    private updateSelectedMenuItem(stateName: string): void {
        if (!this.leftBarTabs) { return; }
        const stateNameShort = stateName.replace('workspace.', '');
        const selectedItem: MenuItem = _.find(this.leftBarTabs.menuItems,
            (item: MenuItem) => this.isMenuState(item, stateNameShort));
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
            return menuItem;
        });
        this.applyCreateModeDisabling(this.leftBarTabs.menuItems, inCreateMode);

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
            text = 'Create new ' + this.navigationService.getParam('type');
        }
        return new MenuItem(text, null, States.WORKSPACE_GENERAL, 'goToState', [this.navigationService.getParams()]);
    }

    private initBreadcrumbsComponents(): void {
        let breadcrumbsComponentsObservable;
        const previousState = this.navigationService.getParam('previousState');

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
