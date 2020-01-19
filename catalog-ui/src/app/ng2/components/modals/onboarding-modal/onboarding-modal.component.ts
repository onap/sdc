/**
 * Created by rc2122 on 6/3/2018.
 */
import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import * as _ from 'lodash';
import { SdcUiServices } from 'onap-ui-angular';
import { ComponentMetadata, IComponentMetadata } from '../../../../models/component-metadata';
import { IUserProperties } from '../../../../models/user';

import { Resource } from '../../../../models/components/resource';
import { ComponentType } from '../../../../utils/constants';
import { CacheService } from '../../../services/cache.service';
import { FileUtilsService } from '../../../services/file-utils.service';
import { OnboardingService } from '../../../services/onboarding.service';
import { TranslateService } from '../../../shared/translator/translate.service';

export interface ImportVSPdata {
    componentCsar: Resource;
    previousComponent?: Resource;
    type: string;
}

// tslint:disable-next-line:interface-name
export interface IPoint {
    x: number;
    y: number;
}

@Component({
    selector: 'onboarding-modal',
    templateUrl: './onboarding-modal.component.html',
    styleUrls: ['onboarding-modal.component.less', '../../../../../assets/styles/table-style.less']
})
export class OnboardingModalComponent implements OnInit {
    @Input() currentCsarUUID: string;
    @Input() currentCsarVersion: string;
    @ViewChild('componentsMetadataTable') table: any;
    @Output() closeModalEvent: EventEmitter<ImportVSPdata> = new EventEmitter<ImportVSPdata>();

    private columns = [
        {name: 'Name', prop: 'name', flexGrow: 22},
        {name: 'Vendor', prop: 'vendorName', flexGrow: 26},
        {name: 'Category', prop: 'categories', flexGrow: 33},
        {name: 'Version', prop: 'csarVersion', flexGrow: 10},
        {name: 'Type', prop: 'resourceType', flexGrow: 10},
        {name: '#', prop: '', flexGrow: 20}
    ];
    private componentsMetadataList: IComponentMetadata[] = [];
    private temp: IComponentMetadata[] = [];
    private componentFromServer: ComponentMetadata;
    private isCsarComponentExists: boolean = false;
    private selectedComponent: ComponentMetadata;
    private isLoading: boolean;
    private user: IUserProperties;

    constructor(private onBoardingService: OnboardingService,
                private translateService: TranslateService,
                private cacheService: CacheService,
                private fileUtilsService: FileUtilsService,
                private popoverService: SdcUiServices.PopoverService,
                private loaderService: SdcUiServices.LoaderService) {
    }

    public ngOnInit(): void {
        this.initOnboardingComponentsList();
        this.user = this.cacheService.get('user');
    }

    initMaxVersionOfItemsInList = (onboardingResponse: IComponentMetadata[]): void => {
        // Get only the latest version of each item
        this.componentsMetadataList = [];

        // group all items according to packageId
        const groupByPackageIdItems = _.groupBy(onboardingResponse, 'packageId');
        // Loop on all the groups and push to componentsMetadataList the max version for each package
        _.each(groupByPackageIdItems, (items: any): void => {
            let maxItem: any = items[0];
            items.forEach((item) => {
                if (parseFloat(maxItem.csarVersion) < parseFloat(item.csarVersion)) {
                    maxItem = item;
                }
            });
            if (maxItem) {
                this.componentsMetadataList.push(maxItem);
            }
        });
    }

    onSelectComponent({selected}) {
        this.table.rowDetail.collapseAllRows();
        if (selected[0] === this.selectedComponent) {
            this.selectedComponent = undefined;
            this.componentFromServer = undefined;
            this.table.rowDetail.toggleExpandRow(null);
            return;
        }
        this.isLoading = true;
        this.componentFromServer = undefined;
        this.selectedComponent = selected[0];
        this.onBoardingService.getComponentFromCsarUuid(this.selectedComponent.csarUUID).subscribe(
            (componentFromServer: ComponentMetadata) => {
                this.isLoading = false;
                if (componentFromServer) {
                    this.componentFromServer = componentFromServer;
                    this.populateRowDetails(true);
                } else {
                    this.populateRowDetails(false);
                }
            }, (error) => {
                this.isLoading = false;
                this.populateRowDetails(false);
            });
    }

    populateRowDetails(isCsarComponentExists: boolean) {
        this.isCsarComponentExists = isCsarComponentExists;
        this.table.rowDetail.toggleExpandRow(this.selectedComponent);
    }

    importOrUpdateCsar = (): void => {
        const selectedComponentConverted = this.onBoardingService.convertMetaDataToComponent(this.selectedComponent);
        const componentFromServerConverted = this.componentFromServer ?
            this.onBoardingService.convertMetaDataToComponent(this.componentFromServer) : undefined;
        const importVSPdata: ImportVSPdata = {
            componentCsar: selectedComponentConverted,
            previousComponent: componentFromServerConverted,
            type: ComponentType.RESOURCE.toLowerCase()
        };
        this.closeModalEvent.emit(importVSPdata);
    }

    downloadCsar = (packageId: string): void => {
        this.isLoading = true;
        this.onBoardingService.downloadOnboardingCsar(packageId).subscribe(
            (file: any): void => {
                this.isLoading = false;
                if (file.body) {
                    this.fileUtilsService.downloadFile(file.body, packageId + '.csar');
                }
            }, (): void => {
                this.isLoading = false;
            }
        );
    }

    updateFilter(event) {
        const val = event.target.value.toLowerCase();

        // filter our data
        const temp = this.temp.filter((componentMetadata: ComponentMetadata) => {
            return !val ||
                (componentMetadata.name && componentMetadata.name.toLowerCase().indexOf(val) !== -1) ||
                (componentMetadata.vendorName && componentMetadata.vendorName.toLowerCase().indexOf(val) !== -1) ||
                (componentMetadata.categories[0] && componentMetadata.categories[0].name.toLowerCase().indexOf(val) !== -1) ||
                (componentMetadata.categories[0] && componentMetadata.categories[0].subcategories[0] && componentMetadata.categories[0].subcategories[0].name.toLowerCase().indexOf(val) !== -1) ||
                (componentMetadata.csarVersion && componentMetadata.csarVersion.toLowerCase().indexOf(val) !== -1) ||
                (componentMetadata.description && componentMetadata.description.toLowerCase().indexOf(val) !== -1);
        });

        // update the rows
        this.componentsMetadataList = temp;
    }

    checkNotCertified = (): boolean => {
        return this.componentFromServer && this.componentFromServer.lifecycleState === 'NOT_CERTIFIED_CHECKOUT' &&
            this.componentFromServer.lastUpdaterUserId !== this.user.userId;
    }

    openPopover = ($event: any, popoverContent): void => {
        this.popoverService.createPopOver('', this.translateService.translate(popoverContent), {
            x: $event.pageX,
            y: $event.pageY
        }, 'bottom');
    }

    private initOnboardingComponentsList = (): void => {
        this.loaderService.activate();
        this.onBoardingService.getOnboardingComponents().subscribe(
            (onboardingResponse: IComponentMetadata[]) => {
                this.loaderService.deactivate();
                if (this.currentCsarUUID) {
                    onboardingResponse = _.filter(onboardingResponse, (input): boolean => {
                        return (input as ComponentMetadata).csarUUID === this.currentCsarUUID;
                    });
                }
                this.initMaxVersionOfItemsInList(onboardingResponse);
                this.temp = [...this.componentsMetadataList];
            }, (error) => {
                this.loaderService.deactivate();
            }
        );
    }
}
