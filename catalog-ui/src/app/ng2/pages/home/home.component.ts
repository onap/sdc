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
'use strict';
import {Component as NgComponent, Inject, OnInit} from '@angular/core';
import {Component, ComponentMetadata, IConfigRoles, IUserProperties, Resource, Service} from 'app/models';
import {HomeFilter} from 'app/models/home-filter';
import {AuthenticationService, CacheService, HomeService, ResourceServiceNg2} from 'app/services-ng2';
import {ComponentState, ModalsHandler} from 'app/utils';
import {SdcUiServices} from 'onap-ui-angular';
import {CHANGE_COMPONENT_CSAR_VERSION_FLAG, ComponentType, ResourceType} from '../../../utils/constants';
import {ImportVSPService} from '../../components/modals/onboarding-modal/import-vsp.service';
import {ISdcConfig, SdcConfigToken} from '../../config/sdc-config.config';
import {IAppMenu, SdcMenuToken} from '../../config/sdc-menu.config';
import {EntityFilterPipe} from '../../pipes/entity-filter.pipe';
import {TranslateService} from '../../shared/translator/translate.service';
import {FoldersItemsMenu, FoldersItemsMenuGroup, FoldersMenu} from './folders';
import {ImportVSPdata} from "../../components/modals/onboarding-modal/onboarding-modal.component";
import {DataTypeCatalogComponent} from "../../../models/data-type-catalog-component";

@NgComponent({
    selector: 'home-page',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.less']
})
export class HomeComponent implements OnInit {
    public numberOfItemToDisplay: number;
    public homeItems: Component[];
    public homeFilteredItems: Array<Component | DataTypeCatalogComponent>;
    public homeFilteredSlicedItems: Array<Component | DataTypeCatalogComponent>;
    public folders: FoldersMenu;
    public roles: IConfigRoles;
    public user: IUserProperties;
    public showTutorial: boolean;
    public isFirstTime: boolean;
    public version: string;
    public homeFilter: HomeFilter;
    public vfcmtType: string;
    public displayActions: boolean;

    constructor(
        @Inject(SdcConfigToken) private sdcConfig: ISdcConfig,
        @Inject(SdcMenuToken) public sdcMenu: IAppMenu,
        @Inject('$state') private $state: ng.ui.IStateService,
        private homeService: HomeService,
        private authService: AuthenticationService,
        private cacheService: CacheService,
        private translateService: TranslateService,
        private modalsHandler: ModalsHandler,
        private modalService: SdcUiServices.ModalService,
        private loaderService: SdcUiServices.LoaderService,
        private importVSPService: ImportVSPService,
        private resourceService: ResourceServiceNg2
    ) { }

    ngOnInit(): void {
        this.initHomeComponentVars();
        this.initFolders();
        this.initEntities();

        if (this.$state.params) {
            if (this.$state.params.folder) {
                const folderName = this.$state.params.folder.replaceAll('_', ' ');

                const selectedFolder = this.folders.getFolders().find((tmpFolder: FoldersItemsMenu) => tmpFolder.text === folderName);
                if (selectedFolder) {
                    this.setSelectedFolder(selectedFolder);
                }
                // Show the tutorial if needed when the dashboard page is opened.<script src="bower_components/angular-filter/dist/angular-filter.min.js"></script>
                // This is called from the welcome page.
            } else if (this.$state.params.show === 'tutorial') {
                this.showTutorial = true;
                this.isFirstTime = true;
            }
        }
    }

    // Open onboarding modal
    public notificationIconCallback(): void {
        this.importVSPService.openOnboardingModal().subscribe((importVSPdata: ImportVSPdata) => {
            const actualComponent = importVSPdata.previousComponent;
            if (!actualComponent || actualComponent.csarVersion !== importVSPdata.componentCsar.csarVersion) {
                this.cacheService.set(CHANGE_COMPONENT_CSAR_VERSION_FLAG, importVSPdata.componentCsar.csarVersion);
            }
            const vfExistsAndIsNotCheckedOut: boolean = actualComponent && actualComponent.lifecycleState != ComponentState.NOT_CERTIFIED_CHECKOUT;
            if (vfExistsAndIsNotCheckedOut) {
                this.checkoutAndRedirectToWorkspace(importVSPdata);
                return;
            }
            this.$state.go('workspace.general', {
                id: actualComponent && actualComponent.uniqueId,
                componentCsar: importVSPdata.componentCsar,
                type: importVSPdata.type
            });
        });
    }

    private checkoutAndRedirectToWorkspace(importVSPdata: ImportVSPdata) {
        this.loaderService.activate();
        this.resourceService.checkout(importVSPdata.previousComponent.uniqueId)
        .subscribe((componentMetadata: ComponentMetadata) => {
            this.$state.go('workspace.general', {
                id: componentMetadata.uniqueId,
                componentCsar: importVSPdata.componentCsar,
                type: importVSPdata.type
            });
            this.loaderService.deactivate();
        }, () => {
            this.loaderService.deactivate();
        });
        return;
    }

    public onImportVf(file: any): void {
        if (file && file.filename) {
            // Check that the file has valid extension.
            const fileExtension: string = file.filename.split('.').pop();
            if (this.sdcConfig.csarFileExtension.indexOf(fileExtension.toLowerCase()) !== -1) {
                this.$state.go('workspace.general', {
                    type: ComponentType.RESOURCE.toLowerCase(),
                    importedFile: file,
                    resourceType: ResourceType.VF
                });
            } else {
                const title: string = this.translateService.translate('NEW_SERVICE_RESOURCE_ERROR_VALID_CSAR_EXTENSIONS_TITLE');
                const message: string = this.translateService.translate('NEW_SERVICE_RESOURCE_ERROR_VALID_CSAR_EXTENSIONS', {extensions: this.sdcConfig.csarFileExtension});
                this.modalService.openWarningModal(title, message, 'error-invalid-csar-ext');
            }
        }
    }

    public onImportVfc(file: any): void {
        if (file && file.filename) {
            // Check that the file has valid extension.
            const fileExtension: string = file.filename.split('.').pop();
            if (this.sdcConfig.toscaFileExtension.indexOf(fileExtension.toLowerCase()) !== -1) {
                this.$state.go('workspace.general', {
                    type: ComponentType.RESOURCE.toLowerCase(),
                    importedFile: file,
                    resourceType: ResourceType.VFC
                });
            } else {
                const title: string = this.translateService.translate('NEW_SERVICE_RESOURCE_ERROR_VALID_TOSCA_EXTENSIONS_TITLE');
                const message: string = this.translateService.translate('NEW_SERVICE_RESOURCE_ERROR_VALID_TOSCA_EXTENSIONS', {extensions: this.sdcConfig.toscaFileExtension});
                this.modalService.openWarningModal(title, message, 'error-invalid-tosca-ext');
            }
        }
    }

    public onImportService(file: any): void {
        if (file && file.filename) {
            // Check that the file has valid extension.
            const fileExtension: string = file.filename.split(".").pop();
            if (this.sdcConfig.csarFileExtension.indexOf(fileExtension.toLowerCase()) !== -1) {
                this.$state.go('workspace.general', {
                    type: ComponentType.SERVICE.toLowerCase(),
                    importedFile: file,
                    serviceType: 'Service'
                });
            } else {
                const title: string = this.translateService.translate('NEW_SERVICE_RESOURCE_ERROR_VALID_CSAR_EXTENSIONS_TITLE');
                const message: string = this.translateService.translate('NEW_SERVICE_RESOURCE_ERROR_VALID_CSAR_EXTENSIONS', {extensions: this.sdcConfig.csarFileExtension});
                this.modalService.openWarningModal(title, message, 'error-invalid-csar-ext');
            }
        }
    };

    public openCreateModal(componentType: string, importedFile: any): void {
        if (importedFile) {
            this.initEntities(true); // Return from import
        } else {
            this.$state.go('workspace.general', {type: componentType.toLowerCase()});
        }
    }

    public createPNF(): void {
        this.$state.go('workspace.general', {
            type: ComponentType.RESOURCE.toLowerCase(),
            resourceType: ResourceType.PNF
        });
    }

    public createCR(): void {
        this.$state.go('workspace.general', {
            type: ComponentType.RESOURCE.toLowerCase(),
            resourceType: ResourceType.CR
        });
    }

    public entitiesCount(folderItem: FoldersItemsMenu): any {
        let total: number = 0;
        if (folderItem.isGroup()) {
            this.folders.getFolders().forEach((tmpFolder: FoldersItemsMenu) => {
                if (tmpFolder.group && tmpFolder.group === (folderItem as FoldersItemsMenuGroup).groupname) {
                    total = total + this._getTotalCounts(tmpFolder);
                }
            });
        } else {
            total = total + this._getTotalCounts(folderItem);
        }
        return total;
    }

    public updateFilter = () => {
        this.$state.go('.', this.homeFilter.toUrlParam(), {location: 'replace', notify: false});
        this.filterHomeItems();
    }

    public getCurrentFolderDistributed(): any[] {
        const states = [];
        if (this.folders) {
            const folderItem: FoldersItemsMenu = this.folders.getCurrentFolder();
            if (folderItem.isGroup()) {
                this.folders.getFolders().forEach((tmpFolder: FoldersItemsMenu) => {
                    if (tmpFolder.group && tmpFolder.group === (folderItem as FoldersItemsMenuGroup).groupname) {
                        this._setStates(tmpFolder, states);
                    }
                });
            } else {
                this._setStates(folderItem, states);
            }
        }
        return states;
    }

    public setSelectedFolder(folderItem: FoldersItemsMenu): void {
        this.folders.setSelected(folderItem);
    }

    public goToComponent(component: Component): void {
        const loaderService = this.loaderService;
        loaderService.activate();
        this.$state.go('workspace.general', {id: component.uniqueId, type: component.componentType.toLowerCase()}).then(() => {
            loaderService.deactivate();
        });
    }

    public raiseNumberOfElementToDisplay(recalculate: boolean = false) {
        const scrollPageAmount = 35;
        if (!this.homeItems) {
            this.numberOfItemToDisplay = 0;
        } else if (this.homeItems.length > this.numberOfItemToDisplay || recalculate) {
            let fullPagesAmount = Math.ceil(this.numberOfItemToDisplay / scrollPageAmount) * scrollPageAmount;
            if (!recalculate || fullPagesAmount === 0) {  // TODO trigger infiniteScroll to check bottom and fire onBottomHit by itself (sdc-ui)
                fullPagesAmount += scrollPageAmount;
            }
            this.numberOfItemToDisplay = Math.min(this.homeItems.length, fullPagesAmount);
            this.homeFilteredSlicedItems = this.homeFilteredItems.slice(0, this.numberOfItemToDisplay);
        }
    }

    public changeCheckboxesFilter(checkboxesFilterArray: string[], checkboxValue: string, checked?: boolean) {
        const checkboxIdx = checkboxesFilterArray.indexOf(checkboxValue);

        checked = (checked !== undefined) ? checked : checkboxIdx === -1;
        if (checked && checkboxIdx === -1) {
            checkboxesFilterArray.push(checkboxValue);
        } else if (!checked && checkboxIdx !== -1) {
            checkboxesFilterArray.splice(checkboxIdx, 1);
        }
        this.updateFilter();
    }

    public changeFilterTerm(filterTerm: string): void {
        this.homeFilter.search = { filterTerm };
        this.updateFilter();
    }

    public setDisplayActions(display?: boolean) {
        this.displayActions = display !== undefined ? display : !this.displayActions;
    }

    private _getTotalCounts(tmpFolder): number {
        let total: number = 0;
        if (tmpFolder.dist !== undefined) {
            const distributions = tmpFolder.dist.split(',');
            distributions.forEach((item: any) => {
                total = total + this.getEntitiesByStateDist(tmpFolder.state, item).length;
            });
        } else {
            total = total + this.getEntitiesByStateDist(tmpFolder.state, tmpFolder.dist).length;
        }
        return total;
    }

    private _setStates(tmpFolder, states) {
        if (tmpFolder.states !== undefined) {
            tmpFolder.states.forEach((item: any) => {
                states.push({state: item.state, dist: item.dist});
            });
        } else {
            states.push({state: tmpFolder.state, dist: tmpFolder.dist});
        }
    }

    private initEntities(reload?: boolean) {
        if (reload || this.componentShouldReload()) {
            this.loaderService.activate();
            this.homeService.getAllComponents(true).subscribe(
                (components: Component[]) => {
                    this.cacheService.set('breadcrumbsComponentsState', this.$state.current.name);  // dashboard
                    this.cacheService.set('breadcrumbsComponents', components);
                    this.homeItems = components;
                    this.loaderService.deactivate();
                    this.filterHomeItems();
                }, (error) => { this.loaderService.deactivate(); });
        } else {
            this.homeItems = this.cacheService.get('breadcrumbsComponents');
            this.filterHomeItems();
        }
    }

    private isDefaultFilter = (): boolean => {
        const defaultFilter = new HomeFilter();
        return angular.equals(defaultFilter, this.homeFilter);
    }

    private componentShouldReload = (): boolean => {
        const breadcrumbsValid: boolean = (this.$state.current.name === this.cacheService.get('breadcrumbsComponentsState') && this.cacheService.contains('breadcrumbsComponents'));
        return !breadcrumbsValid || this.isDefaultFilter();
    }

    private getEntitiesByStateDist(state: string, dist: string): Component[] {
        let gObj: Component[];
        if (this.homeItems && (state || dist)) {
            gObj = this.homeItems.filter((obj: Component) => {
                if (dist !== undefined && obj.distributionStatus === dist && obj.lifecycleState === state) {
                    return true;
                } else if (dist === undefined && (obj.lifecycleState === state || obj.distributionStatus === state)) {
                    return true;
                }
                return false;
            });
        } else {
            gObj = [];
        }
        return gObj;
    }

    private filterHomeItems() {
        this.homeFilteredItems = this.makeFilteredItems(this.homeItems, this.homeFilter);
        this.raiseNumberOfElementToDisplay(true);
        this.homeFilteredSlicedItems = this.homeFilteredItems.slice(0, this.numberOfItemToDisplay);
    }

    private makeFilteredItems(homeItems: Array<Component>, filter: HomeFilter) {
        let filteredComponents: Array<Component | DataTypeCatalogComponent> = homeItems;

        // filter: exclude all resources of type 'vfcmtType':
            filteredComponents = filteredComponents.filter((c) =>
                !c.isResource() || (c as Resource).resourceType.indexOf(this.vfcmtType) === -1);

        // common entity filter
        // --------------------------------------------------------------------------
        filteredComponents = EntityFilterPipe.transform(filteredComponents, filter);

        return filteredComponents;
    }

    private initFolders = (): void => {
        // Note: Do not use SdcUi.ChecklistComponent for folders checkboxes, since from the data structure
        // it is not determined that all checkboxes under the same group are managed by the same selectedValues array.
        if (this.user) {
            this.folders = new FoldersMenu(this.roles[this.user.role].folder);
        }
    }

    private initHomeComponentVars(): void {
        this.version = this.cacheService.get('version');
        this.numberOfItemToDisplay = 0;
        this.displayActions = false;
        this.user = this.authService.getLoggedinUser();
        this.roles = this.sdcMenu.roles;
        this.showTutorial = false;
        this.isFirstTime = false;
        this.vfcmtType = ResourceType.VFCMT;

        // Checkboxes filter init
        this.homeFilter = new HomeFilter(this.$state.params);

        // bind callbacks that are transferred as inputs
        this.notificationIconCallback = this.notificationIconCallback.bind(this);
    }

}
