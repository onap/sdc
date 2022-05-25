import { Component, OnInit, Input, Inject, OnDestroy } from '@angular/core';
import {
    PolicyInstance,
    GroupInstance,
    Component as TopologyTemplate,
    ComponentInstance,
    LeftPaletteComponent,
    FullComponentInstance
} from "app/models";
import {Store} from "@ngxs/store";
import { EVENTS, GRAPH_EVENTS } from 'app/utils';
import {IDropDownOption} from "onap-ui-angular/dist/form-elements/dropdown/dropdown-models";
import { CompositionPaletteService } from "app/ng2/pages/composition/palette/services/palette.service";
import { SdcUiCommon, SdcUiComponents, SdcUiServices } from "onap-ui-angular";
import { SdcMenuToken, IAppMenu } from "app/ng2/config/sdc-menu.config";
import { CompositionService } from "app/ng2/pages/composition/composition.service";
import { ServiceServiceNg2 } from "app/services-ng2";
import { WorkspaceService } from "app/ng2/pages/workspace/workspace.service";
import { ComponentInstanceServiceNg2 } from "app/ng2/services/component-instance-services/component-instance.service";
import { EventListenerService } from "app/services";
import * as _ from 'lodash';
import {SelectedComponentType, TogglePanelLoadingAction} from "../../../common/store/graph.actions";
import Dictionary = _.Dictionary;
import {TopologyTemplateService} from "../../../../../services/component-services/topology-template.service";


@Component({
    selector: 'panel-info-tab',
    templateUrl: './info-tab.component.html',
    styleUrls: ['./info-tab.component.less'],
    // providers: [SdcUiServices.ModalService]
})
export class InfoTabComponent implements OnInit, OnDestroy {

    @Input() isViewOnly: boolean;
    @Input() componentType: SelectedComponentType;
    @Input() component: TopologyTemplate | PolicyInstance | GroupInstance | ComponentInstance;
    public versions: IDropDownOption[];
    private leftPalletElements: LeftPaletteComponent[];
    private isDisabledFlag: boolean;
    private isComponentSelectedFlag: boolean;

    constructor(private store: Store,
                private compositionPaletteService: CompositionPaletteService,
                private compositionService: CompositionService,
                private workspaceService: WorkspaceService,
                private modalService: SdcUiServices.ModalService,
                private componentInstanceService: ComponentInstanceServiceNg2,
                private serviceService: ServiceServiceNg2,
                private eventListenerService: EventListenerService,
                private topologyTemplateService: TopologyTemplateService,
                @Inject(SdcMenuToken) public sdcMenu:IAppMenu) {
    }

    ngOnInit() {
        this.leftPalletElements = this.flatLeftPaletteElementsFromService(this.compositionPaletteService.getLeftPaletteElements());
        this.initEditResourceVersion(this.component, this.leftPalletElements);
        this.eventListenerService.registerObserverCallback(EVENTS.ON_CHECKOUT, (comp) => {
            this.component = comp;
        });
        this.isComponentSelectedFlag = this.isComponentInstanceSelected();
        this.isDisabledFlag = this.isDisabled();

    }

    ngOnDestroy() {
        this.eventListenerService.unRegisterObserver(EVENTS.ON_CHECKOUT);
    }

    flatLeftPaletteElementsFromService = (leftPalleteElementsFromService: Dictionary<Dictionary<LeftPaletteComponent[]>>): LeftPaletteComponent[] => {
        let retValArr = [];
        for (const category in leftPalleteElementsFromService) {
            for (const subCategory in leftPalleteElementsFromService[category]) {
                retValArr = retValArr.concat(leftPalleteElementsFromService[category][subCategory].slice(0));
            }
        }
        return retValArr;
    }

    private isComponentInstanceSelected () {
        return this.componentType === SelectedComponentType.COMPONENT_INSTANCE;
    }

    private versioning: Function = (versionNumber: string): string => {
        let version: Array<string> = versionNumber && versionNumber.split('.');
        return '00000000'.slice(version[0].length) + version[0] + '.' + '00000000'.slice(version[1].length) + version[1];
    };


    private onChangeVersion = (versionDropdown) => {
        let newVersionValue = versionDropdown.value;
        versionDropdown.value = (<FullComponentInstance>this.component).getComponentUid();

        this.store.dispatch(new TogglePanelLoadingAction({isLoading: true}));

        // let service = <Service>this.$scope.currentComponent;
        if(this.component instanceof FullComponentInstance) {

            let onCancel = (error:any) => {
                this.store.dispatch(new TogglePanelLoadingAction({isLoading: false}));
                if (error) {
                    console.log(error);
                }
            };

            let onUpdate = () => {
                //this function will update the instance version than the function call getComponent to update the current component and return the new instance version
                this.componentInstanceService.changeResourceInstanceVersion(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, this.component.uniqueId, newVersionValue)
                    .subscribe((component) => {
                        this.store.dispatch(new TogglePanelLoadingAction({isLoading: false}));
                        this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_VERSION_CHANGED, component);
                    }, onCancel);
            };

            if (this.component.isService() || this.component.isServiceProxy() || this.component.isServiceSubstitution()) {
                this.serviceService.checkComponentInstanceVersionChange(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId,
                    this.component.uniqueId, newVersionValue).subscribe((pathsToDelete:string[]) => {
                    if (pathsToDelete && pathsToDelete.length) {
                        this.store.dispatch(new TogglePanelLoadingAction({isLoading: false}));


                        const {title, message} = this.sdcMenu.alertMessages['upgradeInstance'];
                        let pathNames:string = this.getPathNamesVersionChangeModal(pathsToDelete);
                        let onOk: Function = () => {
                            this.store.dispatch(new TogglePanelLoadingAction({isLoading: true}));

                            onUpdate();
                        };
                        const okButton = {testId: "OK", text: "OK", type: SdcUiCommon.ButtonType.info, callback: onOk, closeModal: true} as SdcUiComponents.ModalButtonComponent;
                        const cancelButton = {testId: "Cancel", text: "Cancel", type: SdcUiCommon.ButtonType.secondary, callback: <Function>onCancel, closeModal: true} as SdcUiComponents.ModalButtonComponent;
                        const modal = this.modalService.openInfoModal(title, message.format([pathNames]), 'confirm-modal', [okButton, cancelButton]);
                        modal.getCloseButton().onClick(onCancel);
                    } else {
                        onUpdate();
                    }
                }, onCancel);
            } else {
                onUpdate();
            }
        }
    };


    private getPathNamesVersionChangeModal = (pathsToDelete:string[]):string => {
        const relatedPaths = _.filter(this.compositionService.forwardingPaths, path =>
            _.find(pathsToDelete, id =>
                path.uniqueId === id
            )
        ).map(path => path.name);
        const pathNames = _.join(relatedPaths, ', ') || 'none';
        return pathNames;
    };


    private initEditResourceVersion = (component, leftPaletteComponents): void => {
        if (this.component instanceof ComponentInstance) {
            let allVersions: any;
            this.topologyTemplateService.getComponentMetadata(component.getComponentUid(), component.originType)
            .subscribe((response) => {
                allVersions = response.metadata.allVersions;
                this.versions = [];
                let sorted: any = _.sortBy(_.toPairs(allVersions), (item) => {
                    return item[0] !== "undefined" && this.versioning(item[0]);
                });
                _.forEach(sorted, (item) => {
                    this.versions.push({label: item[0], value: item[1]});
                });

                let highestVersion = _.last(sorted)[0];

                if (parseFloat(highestVersion) % 1) { //if highest is minor, make sure it is the latest checked in -
                    let latestVersionComponent: LeftPaletteComponent = _.maxBy(
                        _.filter(leftPaletteComponents, (leftPaletteComponent: LeftPaletteComponent) => { //latest checked in
                            return (leftPaletteComponent.systemName === component.systemName || leftPaletteComponent.uuid === component.uuid);
                        })
                        , (component) => {
                            return component.version
                        });

                    let latestVersion: string = latestVersionComponent ? latestVersionComponent.version : highestVersion;

                    if (latestVersion && highestVersion != latestVersion) { //highest is checked out - remove from options
                        this.versions = this.versions.filter(version => version.label != highestVersion);
                    }
                }
            });
        }
    }

    private isDisabled() {
        return this.isViewOnly || this.component['archived'] || this.component['resourceType'] === 'CVFC'
    }

};

