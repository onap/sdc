import { ComponentRef, Injectable } from '@angular/core';
import { SdcUiCommon, SdcUiComponents, SdcUiServices } from 'onap-ui-angular';
import { Component } from '../../models/components/component';
import { ComponentService } from '../../services/component-services/component.service';
import { AutomatedUpgradeStatusResponse } from '../../services/responses/automated-upgrade-response';
import { IDependenciesServerResponse } from '../../services/responses/dependencies-server-response';
import { ITranslateArgs, TranslateService } from '../../shared/translator/translate.service';
import { ComponentType, GeneralStatus } from '../../utils/constants';
import { AllottedResourceInstanceUiObject, ServiceContainerToUpgradeUiObject, VspInstanceUiObject } from './automated-upgrade-models/ui-component-to-upgrade';
import { AutomatedUpgradeStatusComponent } from './automated-upgrade-status/automated-upgrade-status.component';
import { AutomatedUpgradeComponent } from './automated-upgrade.component';
import Dictionary = _.Dictionary;

export interface IAutomatedUpgradeRequestObj {
    serviceId: string;
    resourceId?: string;
}

export enum Placement {
    left = 'left'
}

@Injectable()
export class AutomatedUpgradeService {

    private vspComponent: Component;
    private uiComponentsToUpgrade: ServiceContainerToUpgradeUiObject[];
    private componentType: string;
    private modalInstance: ComponentRef<SdcUiComponents.ModalComponent>;

    constructor(private modalService: SdcUiServices.ModalService,
                private componentService: ComponentService,
                private translateService: TranslateService) {
    }

    public convertToServerRequest = (selectedServices: string[]): IAutomatedUpgradeRequestObj[] => {

        const automatedRequest: IAutomatedUpgradeRequestObj[] = [];
        _.forEach(selectedServices, (serviceId: string) => {
            const serviceToUpgrade: ServiceContainerToUpgradeUiObject = _.find(this.uiComponentsToUpgrade, (service: ServiceContainerToUpgradeUiObject) => {
                return serviceId === service.uniqueId;
            });

            if (serviceToUpgrade.vspInstances[0] instanceof AllottedResourceInstanceUiObject) { // If this is allotted resource instances, we need to take the origin vf id (all the instances have the save origin vspId
                automatedRequest.push({
                    serviceId,
                    resourceId: (serviceToUpgrade.vspInstances[0] as AllottedResourceInstanceUiObject).originVfId
                });
            } else {
                automatedRequest.push({serviceId});
            }
        });
        return automatedRequest;
    }

    public changeUpgradeButtonState = (isDisabled: boolean): void => {
        if (this.modalInstance.instance.buttons[0].disabled !== isDisabled) {
            this.modalInstance.instance.buttons[0].disabled = isDisabled;
        }
    }

    // TODO We will need to replace this function after sdc-ui modal new design, this is just a workaround
    public automatedUpgrade = (): void => {

        const selectedServices = this.modalInstance.instance.innerModalContent.instance.selectedComponentsToUpgrade;
        this.disabledAllModalButtons();
        this.componentService.automatedUpgrade(this.vspComponent.componentType, this.vspComponent.uniqueId, this.convertToServerRequest(selectedServices)).subscribe((automatedUpgradeStatus: any) => {

            if (automatedUpgradeStatus.status === GeneralStatus.OK) {

                const statusMap: Dictionary<AutomatedUpgradeStatusResponse> = _.keyBy(automatedUpgradeStatus.componentToUpgradeStatus, 'name');
                // In the status modal we only showing the upgraded component that the user selected, not the entire list
                const upgradedComponent: ServiceContainerToUpgradeUiObject[] = _.filter(this.uiComponentsToUpgrade, (component: ServiceContainerToUpgradeUiObject) => {
                    return selectedServices.indexOf(component.uniqueId) > -1;
                });

                _.forEach(upgradedComponent, (upgradedComponent: ServiceContainerToUpgradeUiObject) => { // If upgrade success we need to upgrade the version  all success
                    if (statusMap[upgradedComponent.name].status === GeneralStatus.OK) {
                        upgradedComponent.version = statusMap[upgradedComponent.name].version;
                        _.forEach(upgradedComponent.vspInstances, (instance: VspInstanceUiObject) => {
                            instance.vspVersion = this.vspComponent.version;
                        });
                    }
                });

                const statusModalTitle = this.getTextByComponentType('_UPGRADE_STATUS_TITLE');
                this.modalInstance.instance.setTitle(statusModalTitle);
                this.modalInstance.instance.getButtons().splice(0, 1); // Remove the upgrade button
                this.modalInstance.instance.buttons[0].disabled = false; // enable close again
                this.modalInstance.instance.innerModalContent.destroy();
                this.modalService.createInnnerComponent(this.modalInstance, AutomatedUpgradeStatusComponent, {
                    upgradedComponentsList: upgradedComponent,
                    upgradeStatusMap: statusMap,
                    statusText: this.getStatusText(statusMap)
                });
            }
        });
    }

    public isAlreadyAdded = (uniqueId: string): ServiceContainerToUpgradeUiObject => {
        const componentToUpgrade = _.find(this.uiComponentsToUpgrade, (componentToUpgrade: ServiceContainerToUpgradeUiObject) => {
            return componentToUpgrade.uniqueId === uniqueId;
        });
        return componentToUpgrade;
    }

    public initVfUpgradeData = (serviceToUpgrade: IDependenciesServerResponse, vsp: IDependenciesServerResponse) => {

        const existed = this.isAlreadyAdded(serviceToUpgrade.uniqueId);
        if (existed) { // We will take the VF with the lower version existed - only one exist all the time in vf upgrade
            if (vsp.version < existed.vspInstances[0].vspVersion) {
                existed.vspInstances = [];
                existed.addVfInstance(vsp, this.vspComponent.version);
            }
        } else {
            const dependencyUiObj: ServiceContainerToUpgradeUiObject = new ServiceContainerToUpgradeUiObject(serviceToUpgrade);
            dependencyUiObj.addVfInstance(vsp, this.vspComponent.version);
            this.uiComponentsToUpgrade.push(dependencyUiObj);
        }
    }

    // Service data will create instances of proxy or allotted resources
    public initServiceUpgradeData = (serviceToUpgrade: IDependenciesServerResponse, vsp: IDependenciesServerResponse, instanceNames: string[], allottedOriginVf?: IDependenciesServerResponse) => {

        const existedService = this.isAlreadyAdded(serviceToUpgrade.uniqueId);
        if (existedService) {
            existedService.addMultipleInstances(vsp, this.vspComponent.version, instanceNames, allottedOriginVf);
        } else {
            const dependencyUiObj: ServiceContainerToUpgradeUiObject = new ServiceContainerToUpgradeUiObject(serviceToUpgrade);
            dependencyUiObj.addMultipleInstances(vsp, this.vspComponent.version, instanceNames, allottedOriginVf);
            this.uiComponentsToUpgrade.push(dependencyUiObj);
        }
    }

    /*
     The server return response of 3 level nested object
     First level - Vsp data by version
     Each vsp have a decencies (the services contains the vsp - By default this is vf upgrade
     If instancesNames exist - this can be proxy or allotted
     If we have second layer of dependencies than this is allotted
     Since we display the data the opposite way the BE return, this function will order the data in order to display it
     */
    public convertToComponentsToUpgradeUiObjArray = (dependenciesServerResponse: IDependenciesServerResponse[]): void => {

        this.uiComponentsToUpgrade = [];

        _.forEach(dependenciesServerResponse, (vsp: IDependenciesServerResponse) => { // 3 nested levels - 1 level for vf, 2 level proxy, 3 levels allotted
            if (vsp.dependencies) {
                _.forEach(vsp.dependencies, (dependency: IDependenciesServerResponse) => {
                    if (dependency.instanceNames) { // Init service upgrade data
                        if (dependency.dependencies) {
                            _.forEach(dependency.dependencies, (serviceContainer: IDependenciesServerResponse) => { // Initiate allotted_resource instances
                                this.initServiceUpgradeData(serviceContainer, vsp, dependency.instanceNames, dependency);
                            });
                        } else { // Init service_proxy instances
                            this.initServiceUpgradeData(dependency, vsp, dependency.instanceNames);
                        }
                    } else { // Init vf upgrade data
                        this.initVfUpgradeData(dependency, vsp);
                    }
                });
            }
        });
    }

    public isAllComponentsUpgraded = (): boolean => {
        const isAllComponentUpgrade = _.filter(this.uiComponentsToUpgrade, (component: ServiceContainerToUpgradeUiObject) => {
            return !component.isAlreadyUpgrade;
        });
        return isAllComponentUpgrade.length === 0;
    }

    public isAllComponentsLocked = (): boolean => {
        const unLockedComponents = _.filter(this.uiComponentsToUpgrade, (component: ServiceContainerToUpgradeUiObject) => {
            return !component.isLock;
        });
        return unLockedComponents.length === 0;
    }

    public isUpgradeNeeded = (): boolean => {
        const neededUpgradeList = _.filter(this.uiComponentsToUpgrade, (component: ServiceContainerToUpgradeUiObject) => {
            return !component.isLock && !component.isAlreadyUpgrade;
        });
        return neededUpgradeList.length > 0;
    }
    public getInformationTextToDisplay = (): string => {

        const isAllComponentsUpgraded = this.isAllComponentsUpgraded();
        const isAllComponentsLocked = this.isAllComponentsLocked();
        const params = {vspName: this.vspComponent.name, vspVersion: this.vspComponent.version};

        if (this.uiComponentsToUpgrade.length === 0) {
            return this.getTextByComponentType('_NOTHING_TO_UPGRADE', params);
        }

        switch (true) {

            case this.isUpgradeNeeded(): {
                return this.getTextByComponentType('_AUTOMATED_UPGRADE_WITH_COMPONENTS_TO_UPGRADE', params);
            }
            case  !this.isUpgradeNeeded() && isAllComponentsLocked: {
                return this.getTextByComponentType('_AUTOMATED_UPGRADE_ALL_COMPONENTS_LOCKED', params);
            }
            case !this.isUpgradeNeeded() && !isAllComponentsLocked && isAllComponentsUpgraded: {
                return this.getTextByComponentType('_AUTOMATED_UPGRADE_ALL_COMPONENTS_UPGRADED', params);
            }
            case !this.isUpgradeNeeded() && !isAllComponentsLocked && !isAllComponentsUpgraded: {
                return this.getTextByComponentType('_AUTOMATED_UPGRADE_ALL_COMPONENTS_LOCKED', params);
            }
        }
    }

    public openAutomatedUpgradeModal = (componentsToUpgrade: IDependenciesServerResponse[], component: Component, isAfterCertification?: boolean): void => {

        this.vspComponent = component;
        this.componentType = this.vspComponent.isResource() ? ComponentType.RESOURCE : ComponentType.SERVICE;

        this.convertToComponentsToUpgradeUiObjArray(componentsToUpgrade);
        const informationalText = this.getInformationTextToDisplay();
        const modalTitle = this.getTextByComponentType('_UPGRADE_TITLE');
        const certificationText = isAfterCertification ? this.getTextByComponentType('_CERTIFICATION_STATUS_TEXT', {resourceName: this.vspComponent.name}) : undefined;

        const upgradeVspModalConfig = {
            title: modalTitle,
            size: 'md',
            type: SdcUiCommon.ModalType.custom,
            testId: 'upgradeVspModal',
            buttons: [
                {
                    text: this.vspComponent.isResource() ? 'UPGRADE' : 'UPDATE',
                    spinner_position: Placement.left,
                    size: 'sm',
                    callback: this.automatedUpgrade,
                    closeModal: false,
                    disabled: !this.isUpgradeNeeded(),

                },
                {text: 'CLOSE', size: 'sm', closeModal: true, type: 'secondary'}
            ] as SdcUiCommon.IModalButtonComponent[]
        } as SdcUiCommon.IModalConfig;

        this.modalInstance = this.modalService.openModal(upgradeVspModalConfig);
        this.modalService.createInnnerComponent(this.modalInstance, AutomatedUpgradeComponent, {
            componentsToUpgrade: this.uiComponentsToUpgrade,
            informationText: informationalText,
            certificationStatusText: certificationText
        });
    }

    private getStatusText = (statusMap: Dictionary<AutomatedUpgradeStatusResponse>): string => {
        const failedUpgraded = _.filter(_.flatMap(statusMap), (upgradeStatus: AutomatedUpgradeStatusResponse) => {
            return upgradeStatus.status !== GeneralStatus.OK;
        });

        if (failedUpgraded.length > 0) {
            return this.getTextByComponentType('_UPGRADE_STATUS_FAIL');
        }
        return this.getTextByComponentType('_UPGRADE_STATUS_SUCCESS');
    }

    private disabledAllModalButtons = (): void => {
        this.modalInstance.instance.innerModalContent.instance.disabled = true;
        this.modalInstance.instance.buttons[0].show_spinner = true;
        this.modalInstance.instance.buttons[1].disabled = true;
    }

    private getTextByComponentType(textLabel: string, params?: ITranslateArgs) {
        return this.translateService.translate(this.componentType + textLabel, params);
    }
}
