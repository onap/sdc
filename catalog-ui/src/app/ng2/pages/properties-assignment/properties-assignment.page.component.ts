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

import * as _ from "lodash";
import {Component, ViewChild, Inject, TemplateRef} from "@angular/core";
import { PropertiesService } from "../../services/properties.service";
import { PropertyFEModel, InstanceFePropertiesMap, InstanceBePropertiesMap, InstancePropertiesAPIMap, Component as ComponentData, FilterPropertiesAssignmentData, ModalModel, ButtonModel } from "app/models";
import { ResourceType } from "app/utils";
import {ComponentServiceNg2} from "../../services/component-services/component.service";
import {ComponentInstanceServiceNg2} from "../../services/component-instance-services/component-instance.service"
import { InputBEModel, InputFEModel, ComponentInstance, GroupInstance, PolicyInstance, PropertyBEModel, DerivedFEProperty, SimpleFlatProperty } from "app/models";
import { KeysPipe } from 'app/ng2/pipes/keys.pipe';
import {WorkspaceMode, EVENTS} from "../../../utils/constants";
import {EventListenerService} from "app/services/event-listener-service"
import {HierarchyDisplayOptions} from "../../components/logic/hierarchy-navigtion/hierarchy-display-options";
import {FilterPropertiesAssignmentComponent} from "../../components/logic/filter-properties-assignment/filter-properties-assignment.component";
import {PropertyRowSelectedEvent} from "../../components/logic/properties-table/properties-table.component";
import {HierarchyNavService} from "./services/hierarchy-nav.service";
import {PropertiesUtils} from "./services/properties.utils";
import {ComponentModeService} from "../../services/component-services/component-mode.service";
import {ModalService} from "../../services/modal.service";
import {Tabs, Tab} from "../../components/ui/tabs/tabs.component";
import {InputsUtils} from "./services/inputs.utils";
import {PropertyCreatorComponent} from "./property-creator/property-creator.component";
import { InstanceFeDetails } from "../../../models/instance-fe-details";
import { SdcUiComponents } from "sdc-ui/lib/angular";
//import { ModalService as ModalServiceSdcUI} from "sdc-ui/lib/angular/modals/modal.service";
import { IModalButtonComponent } from "sdc-ui/lib/angular/modals/models/modal-config";
import { UnsavedChangesComponent } from "app/ng2/components/ui/forms/unsaved-changes/unsaved-changes.component";

const SERVICE_SELF_TITLE = "SELF";
@Component({
    templateUrl: './properties-assignment.page.component.html',
    styleUrls: ['./properties-assignment.page.component.less']
})
export class PropertiesAssignmentComponent {
    title = "Properties & Inputs";

    component: ComponentData;
    componentInstanceNamesMap: Map<string, InstanceFeDetails> = new Map<string, InstanceFeDetails>();//instanceUniqueId, {name, iconClass}

    propertiesNavigationData = [];
    instancesNavigationData = [];

    instanceFePropertiesMap:InstanceFePropertiesMap;
    inputs: Array<InputFEModel> = [];
    instances: Array<ComponentInstance|GroupInstance|PolicyInstance> = [];
    searchQuery: string;
    propertyStructureHeader: string;

    selectedFlatProperty: SimpleFlatProperty = new SimpleFlatProperty();
    selectedInstanceData: ComponentInstance|GroupInstance|PolicyInstance = null;
    checkedPropertiesCount: number = 0;

    hierarchyPropertiesDisplayOptions:HierarchyDisplayOptions = new HierarchyDisplayOptions('path', 'name', 'childrens');
    hierarchyInstancesDisplayOptions:HierarchyDisplayOptions = new HierarchyDisplayOptions('uniqueId', 'name', 'archived', null, 'iconClass');
    displayClearSearch = false;
    searchPropertyName:string;
    currentMainTab:Tab;
    isInputsTabSelected:boolean;
    isPropertiesTabSelected:boolean;
    isReadonly:boolean;
    resourceIsReadonly:boolean;
    loadingInstances:boolean = false;
    loadingInputs:boolean = false;
    loadingProperties:boolean = false;
    changedData:Array<PropertyFEModel|InputFEModel>;
    hasChangedData:boolean;
    isValidChangedData:boolean;
    savingChangedData:boolean;
    stateChangeStartUnregister:Function;
    serviceBePropertiesMap: InstanceBePropertiesMap;

    @ViewChild('hierarchyNavTabs') hierarchyNavTabs: Tabs;
    @ViewChild('propertyInputTabs') propertyInputTabs: Tabs;
    @ViewChild('advanceSearch') advanceSearch: FilterPropertiesAssignmentComponent;
   
    constructor(private propertiesService: PropertiesService,
                private hierarchyNavService: HierarchyNavService,
                private propertiesUtils:PropertiesUtils,
                private inputsUtils:InputsUtils,
                private componentServiceNg2:ComponentServiceNg2,
                private componentInstanceServiceNg2:ComponentInstanceServiceNg2,
                @Inject("$stateParams") _stateParams,
                @Inject("$scope") private $scope:ng.IScope,
                @Inject("$state") private $state:ng.ui.IStateService,
                @Inject("Notification") private Notification:any,
                private componentModeService:ComponentModeService,
                private ModalService:ModalService,
                private EventListenerService:EventListenerService,
                private ModalServiceSdcUI: SdcUiComponents.ModalService) {

        this.instanceFePropertiesMap = new InstanceFePropertiesMap();

        /* This is the way you can access the component data, please do not use any data except metadata, all other data should be received from the new api calls on the first time
        than if the data is already exist, no need to call the api again - Ask orit if you have any questions*/
        this.component = _stateParams.component;
        this.EventListenerService.registerObserverCallback(EVENTS.ON_LIFECYCLE_CHANGE, this.onCheckout);
        this.updateViewMode();

        this.changedData = [];
        this.updateHasChangedData();
        this.isValidChangedData = true;
    }

    ngOnInit() {
        console.log("==>" + this.constructor.name + ": ngOnInit");
        this.loadingInputs = true;
        this.loadingInstances = true;
        this.loadingProperties = true;
        this.componentServiceNg2
            .getComponentInputsWithProperties(this.component)
            .subscribe(response => {
                _.forEach(response.inputs, (input: InputBEModel) => {
                    const newInput: InputFEModel = new InputFEModel(input);
                    this.inputsUtils.resetInputDefaultValue(newInput, input.defaultValue);
                    this.inputs.push(newInput); //only push items that were declared via SDC
                });
                this.loadingInputs = false;

            });
        this.componentServiceNg2
            .getComponentResourcePropertiesData(this.component)
            .subscribe(response => {
                this.instances = [];
                this.instances.push(...response.componentInstances);
                this.instances.push(...response.groupInstances);
                this.instances.push(...response.policies);

                // add the service self instance to the top of the list.
                const serviceInstance = new ComponentInstance();
                serviceInstance.name = SERVICE_SELF_TITLE;
                serviceInstance.uniqueId = this.component.uniqueId;
                this.instances.unshift(serviceInstance);

                _.forEach(this.instances, (instance) => {
                    this.instancesNavigationData.push(instance);
                    this.componentInstanceNamesMap[instance.uniqueId] = <InstanceFeDetails>{name: instance.name, iconClass:instance.iconClass, originArchived:instance.originArchived};
                });
                this.loadingInstances = false;
                if (this.instancesNavigationData[0] == undefined) {
                    this.loadingProperties = false;
                }
                this.selectFirstInstanceByDefault();
            });

        this.stateChangeStartUnregister = this.$scope.$on('$stateChangeStart', (event, toState, toParams) => {
            // stop if has changed properties
            if (this.hasChangedData) {
                event.preventDefault();
                this.showUnsavedChangesAlert().then(() => {
                    this.$state.go(toState, toParams);
                }, () => {});
            }
        });
    };

    ngOnDestroy() {
        this.EventListenerService.unRegisterObserver(EVENTS.ON_LIFECYCLE_CHANGE);
        this.stateChangeStartUnregister();
    }

    selectFirstInstanceByDefault = () => {
        if (this.instancesNavigationData[0] !== undefined) {
            this.onInstanceSelectedUpdate(this.instancesNavigationData[0]);
        }
    };

    updateViewMode = () => {
        this.isReadonly = this.componentModeService.getComponentMode(this.component) === WorkspaceMode.VIEW;
    }

    onCheckout = (component:ComponentData) => {
        this.component = component;
        this.updateViewMode();
    }

    isSelf = ():boolean => {
        return this.selectedInstanceData && this.selectedInstanceData.uniqueId == this.component.uniqueId;
    }

    getServiceProperties(){
        this.loadingProperties = false;
        this.componentServiceNg2
            .getServiceProperties(this.component)
            .subscribe(response => {
                this.serviceBePropertiesMap = new InstanceBePropertiesMap();
                this.serviceBePropertiesMap[this.component.uniqueId] = response;
                this.processInstancePropertiesResponse(this.serviceBePropertiesMap, false);
                this.loadingProperties = false;
            }, error => {
                this.loadingProperties = false;
            });
    }

    onInstanceSelectedUpdate = (instance: ComponentInstance|GroupInstance|PolicyInstance) => {
        // stop if has changed properties
        if (this.hasChangedData) {
            this.showUnsavedChangesAlert().then((resolve)=> {
                this.changeSelectedInstance(instance)
            }, (reject) => {
            });
            return;
        }
        this.changeSelectedInstance(instance);
    };

    changeSelectedInstance =  (instance: ComponentInstance|GroupInstance|PolicyInstance) => {
        this.selectedInstanceData = instance;
        this.loadingProperties = true;
        if (instance instanceof ComponentInstance) {
            let instanceBePropertiesMap: InstanceBePropertiesMap = new InstanceBePropertiesMap();
            if (this.isInput(instance.originType)) {
                this.componentInstanceServiceNg2
                    .getComponentInstanceInputs(this.component, instance)
                    .subscribe(response => {
                        instanceBePropertiesMap[instance.uniqueId] = response;
                        this.processInstancePropertiesResponse(instanceBePropertiesMap, true);
                        this.loadingProperties = false;
                    }, error => {
                    }); //ignore error
            } else if (this.isSelf()) {
                this.getServiceProperties();
            } else {
                this.componentInstanceServiceNg2
                    .getComponentInstanceProperties(this.component, instance.uniqueId)
                    .subscribe(response => {
                        instanceBePropertiesMap[instance.uniqueId] = response;
                        this.processInstancePropertiesResponse(instanceBePropertiesMap, false);
                        this.loadingProperties = false;
                    }, error => {
                    }); //ignore error
            }

            this.resourceIsReadonly = (instance.componentName === "vnfConfiguration");
        } else if (instance instanceof GroupInstance) {
            let instanceBePropertiesMap: InstanceBePropertiesMap = new InstanceBePropertiesMap();
            this.componentInstanceServiceNg2
                .getComponentGroupInstanceProperties(this.component, this.selectedInstanceData.uniqueId)
                .subscribe((response) => {
                    instanceBePropertiesMap[instance.uniqueId] = response;
                    this.processInstancePropertiesResponse(instanceBePropertiesMap, false);
                    this.loadingProperties = false;
                });
        } else if (instance instanceof PolicyInstance) {
            let instanceBePropertiesMap: InstanceBePropertiesMap = new InstanceBePropertiesMap();
            this.componentInstanceServiceNg2
                .getComponentPolicyInstanceProperties(this.component, this.selectedInstanceData.uniqueId)
                .subscribe((response) => {
                    instanceBePropertiesMap[instance.uniqueId] = response;
                    this.processInstancePropertiesResponse(instanceBePropertiesMap, false);
                    this.loadingProperties = false;
                });
        } else {
            this.loadingProperties = false;
        }

        if (this.searchPropertyName) {
            this.clearSearch();
        }
        //clear selected property from the navigation
        this.selectedFlatProperty = new SimpleFlatProperty();
        this.propertiesNavigationData = [];
    };

    /**
     * Entry point handling response from server
     */
    processInstancePropertiesResponse = (instanceBePropertiesMap: InstanceBePropertiesMap, originTypeIsVF: boolean) => {
        this.instanceFePropertiesMap = this.propertiesUtils.convertPropertiesMapToFEAndCreateChildren(instanceBePropertiesMap, originTypeIsVF, this.inputs); //create flattened children, disable declared props, and init values
        this.checkedPropertiesCount = 0;
    };


    /*** VALUE CHANGE EVENTS ***/
    dataChanged = (item:PropertyFEModel|InputFEModel) => {
        let itemHasChanged;
        if (this.isPropertiesTabSelected && item instanceof PropertyFEModel) {
            itemHasChanged = item.hasValueObjChanged();
        } else if (this.isInputsTabSelected && item instanceof InputFEModel) {
            itemHasChanged = item.hasDefaultValueChanged();
        }

        const dataChangedIdx = this.changedData.findIndex((changedItem) => changedItem === item);
        if (itemHasChanged) {
            if (dataChangedIdx === -1) {
                this.changedData.push(item);
            }
        } else {
            if (dataChangedIdx !== -1) {
                this.changedData.splice(dataChangedIdx, 1);
            }
        }

        if (this.isPropertiesTabSelected) {
            this.isValidChangedData = this.changedData.every((changedItem) => (<PropertyFEModel>changedItem).valueObjIsValid);
        } else if (this.isInputsTabSelected) {
            this.isValidChangedData = this.changedData.every((changedItem) => (<InputFEModel>changedItem).defaultValueObjIsValid);
        }
        this.updateHasChangedData();
    };


    /*** HEIRARCHY/NAV RELATED FUNCTIONS ***/

    /**
     * Handle select node in navigation area, and select the row in table
     */
    onPropertySelectedUpdate = ($event) => {
        console.log("==>" + this.constructor.name + ": onPropertySelectedUpdate");
        this.selectedFlatProperty = $event;
        let parentProperty:PropertyFEModel = this.propertiesService.getParentPropertyFEModelFromPath(this.instanceFePropertiesMap[this.selectedFlatProperty.instanceName], this.selectedFlatProperty.path);
        parentProperty.expandedChildPropertyId = this.selectedFlatProperty.path;
    };

    /**
     * When user select row in table, this will prepare the hirarchy object for the tree.
     */
    selectPropertyRow = (propertyRowSelectedEvent:PropertyRowSelectedEvent) => {
        console.log("==>" + this.constructor.name + ": selectPropertyRow " + propertyRowSelectedEvent.propertyModel.name);
        let property = propertyRowSelectedEvent.propertyModel;
        let instanceName = propertyRowSelectedEvent.instanceName;
        this.propertyStructureHeader = null;

        // Build hirarchy tree for the navigation and update propertiesNavigationData with it.
        if (!(this.selectedInstanceData instanceof ComponentInstance) || this.selectedInstanceData.originType !== ResourceType.VF) {
            let simpleFlatProperty:Array<SimpleFlatProperty>;
            if (property instanceof PropertyFEModel) {
                simpleFlatProperty = this.hierarchyNavService.getSimplePropertiesTree(property, instanceName);
            } else if (property instanceof DerivedFEProperty) {
                // Need to find parent PropertyFEModel
                let parentPropertyFEModel:PropertyFEModel = _.find(this.instanceFePropertiesMap[instanceName], (tmpFeProperty):boolean => {
                    return property.propertiesName.indexOf(tmpFeProperty.name)===0;
                });
                simpleFlatProperty = this.hierarchyNavService.getSimplePropertiesTree(parentPropertyFEModel, instanceName);
            }
            this.propertiesNavigationData = simpleFlatProperty;
        }

        // Update the header in the navigation tree with property name.
        this.propertyStructureHeader = (property.propertiesName.split('#'))[0];

        // Set selected property in table
        this.selectedFlatProperty = this.hierarchyNavService.createSimpleFlatProperty(property, instanceName);
        this.hierarchyNavTabs.triggerTabChange('Property Structure');
    };


    selectInstanceRow = ($event) => {//get instance name
        this.selectedInstanceData =  _.find(this.instancesNavigationData, (instance:ComponentInstance) => {
            return instance.name == $event;
        });
        this.hierarchyNavTabs.triggerTabChange('Composition');
    };

    tabChanged = (event) => {
        // stop if has changed properties
        if (this.hasChangedData) {
            this.propertyInputTabs.triggerTabChange(this.currentMainTab.title);
            this.showUnsavedChangesAlert().then((proceed) => {
                this.propertyInputTabs.selectTab(this.propertyInputTabs.tabs.find((tab) => tab.title === event.title));
            }, ()=> {
            });
            return;
        }

        console.log("==>" + this.constructor.name + ": tabChanged " + event);
        this.currentMainTab = this.propertyInputTabs.tabs.find((tab) => tab.title === event.title);
        this.isPropertiesTabSelected = this.currentMainTab.title === "Properties";
        this.isInputsTabSelected = this.currentMainTab.title === "Inputs";
        this.propertyStructureHeader = null;
        this.searchQuery = '';
    };



    /*** DECLARE PROPERTIES/INPUTS ***/
    declareProperties = (): void => {
        console.log("==>" + this.constructor.name + ": declareProperties");

        let selectedComponentInstancesProperties: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        let selectedGroupInstancesProperties: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        let selectedPolicyInstancesProperties: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        let selectedComponentInstancesInputs: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        let instancesIds = new KeysPipe().transform(this.instanceFePropertiesMap, []);

        angular.forEach(instancesIds, (instanceId: string): void => {
            let selectedInstanceData: any = this.instances.find(instance => instance.uniqueId == instanceId);
            if (selectedInstanceData instanceof ComponentInstance) {
                if (!this.isInput(selectedInstanceData.originType)) {
                    selectedComponentInstancesProperties[instanceId] = this.propertiesService.getCheckedProperties(this.instanceFePropertiesMap[instanceId]);
                } else {
                    selectedComponentInstancesInputs[instanceId] = this.propertiesService.getCheckedProperties(this.instanceFePropertiesMap[instanceId]);
                }
            } else if (selectedInstanceData instanceof GroupInstance) {
                selectedGroupInstancesProperties[instanceId] = this.propertiesService.getCheckedProperties(this.instanceFePropertiesMap[instanceId]);
            } else if (selectedInstanceData instanceof PolicyInstance) {
                selectedPolicyInstancesProperties[instanceId] = this.propertiesService.getCheckedProperties(this.instanceFePropertiesMap[instanceId]);
            }
        });

        let inputsToCreate: InstancePropertiesAPIMap = new InstancePropertiesAPIMap(selectedComponentInstancesInputs, selectedComponentInstancesProperties, selectedGroupInstancesProperties, selectedPolicyInstancesProperties);

        this.componentServiceNg2
            .createInput(this.component, inputsToCreate, this.isSelf())
            .subscribe(response => {
                this.setInputTabIndication(response.length);
                this.checkedPropertiesCount = 0;
                _.forEach(response, (input: InputBEModel) => {
                    let newInput: InputFEModel = new InputFEModel(input);
                    this.inputsUtils.resetInputDefaultValue(newInput, input.defaultValue);
                    this.inputs.push(newInput);
                    this.updatePropertyValueAfterDeclare(newInput);
                });
            }, error => {}); //ignore error
    };

    saveChangedData = ():Promise<(PropertyBEModel|InputBEModel)[]> => {
        return new Promise((resolve, reject) => {
            if (!this.isValidChangedData) {
                reject('Changed data is invalid - cannot save!');
                return;
            }
            if (!this.changedData.length) {
                resolve([]);
                return;
            }

            // make request and its handlers
            let request;
            let handleSuccess, handleError;
            if (this.isPropertiesTabSelected) {
                const changedProperties: PropertyBEModel[] = this.changedData.map((changedProp) => {
                    changedProp = <PropertyFEModel>changedProp;
                    const propBE = new PropertyBEModel(changedProp);
                    propBE.value = changedProp.getJSONValue();
                    return propBE;
                });

                if (this.selectedInstanceData instanceof ComponentInstance) {
                    if (this.isInput(this.selectedInstanceData.originType)) {
                        request = this.componentInstanceServiceNg2
                            .updateInstanceInputs(this.component, this.selectedInstanceData.uniqueId, changedProperties);
                        handleSuccess = (response) => {
                            // reset each changed property with new value and remove it from changed properties list
                            response.forEach((resInput) => {
                                const changedProp = <PropertyFEModel>this.changedData.shift();
                                this.propertiesUtils.resetPropertyValue(changedProp, resInput.value);
                            });
                            console.log('updated instance inputs:', response);
                        };
                    } else {
                        if (this.isSelf()) {
                            request = this.componentServiceNg2.updateServiceProperties(this.component, _.map(changedProperties, cp => {
                                delete cp.constraints;
                                return cp;
                            }));
                        } else {
                            request = this.componentInstanceServiceNg2
                                .updateInstanceProperties(this.component, this.selectedInstanceData.uniqueId, changedProperties);
                        }
                        handleSuccess = (response) => {
                            // reset each changed property with new value and remove it from changed properties list
                            response.forEach((resProp) => {
                                const changedProp = <PropertyFEModel>this.changedData.shift();
                                this.propertiesUtils.resetPropertyValue(changedProp, resProp.value);
                            });
                            resolve(response);
                            console.log("updated instance properties: ", response);
                        };
                    }
                } else if (this.selectedInstanceData instanceof GroupInstance) {
                    request = this.componentInstanceServiceNg2
                        .updateComponentGroupInstanceProperties(this.component, this.selectedInstanceData.uniqueId, changedProperties);
                    handleSuccess = (response) => {
                        // reset each changed property with new value and remove it from changed properties list
                        response.forEach((resProp) => {
                            const changedProp = <PropertyFEModel>this.changedData.shift();
                            this.propertiesUtils.resetPropertyValue(changedProp, resProp.value);
                        });
                        resolve(response);
                        console.log("updated group instance properties: ", response);
                    };
                } else if (this.selectedInstanceData instanceof PolicyInstance) {
                    request = this.componentInstanceServiceNg2
                        .updateComponentPolicyInstanceProperties(this.component, this.selectedInstanceData.uniqueId, changedProperties);
                    handleSuccess = (response) => {
                        // reset each changed property with new value and remove it from changed properties list
                        response.forEach((resProp) => {
                            const changedProp = <PropertyFEModel>this.changedData.shift();
                            this.propertiesUtils.resetPropertyValue(changedProp, resProp.value);
                        });
                        resolve(response);
                        console.log("updated policy instance properties: ", response);
                    };
                }
            } else if (this.isInputsTabSelected) {
                const changedInputs: InputBEModel[] = this.changedData.map((changedInput) => {
                    changedInput = <InputFEModel>changedInput;
                    const inputBE = new InputBEModel(changedInput);
                    inputBE.defaultValue = changedInput.getJSONDefaultValue();
                    return inputBE;
                });
                request = this.componentServiceNg2
                    .updateComponentInputs(this.component, changedInputs);
                handleSuccess = (response) => {
                    // reset each changed property with new value and remove it from changed properties list
                    response.forEach((resInput) => {
                        const changedInput = <InputFEModel>this.changedData.shift();
                        this.inputsUtils.resetInputDefaultValue(changedInput, resInput.defaultValue);
                    });
                    console.log("updated the component inputs and got this response: ", response);
                }
            }

            this.savingChangedData = true;
            request.subscribe(
                (response) => {
                    this.savingChangedData = false;
                    handleSuccess && handleSuccess(response);
                    this.updateHasChangedData();
                    resolve(response);
                },
                (error) => {
                    this.savingChangedData = false;
                    handleError && handleError(error);
                    this.updateHasChangedData();
                    reject(error);
                }
            );
        });
    };

    reverseChangedData = ():void => {
        // make reverse item handler
        let handleReverseItem;
        if (this.isPropertiesTabSelected) {
            handleReverseItem = (changedItem) => {
                changedItem = <PropertyFEModel>changedItem;
                this.propertiesUtils.resetPropertyValue(changedItem, changedItem.value);
            };
        } else if (this.isInputsTabSelected) {
            handleReverseItem = (changedItem) => {
                changedItem = <InputFEModel>changedItem;
                this.inputsUtils.resetInputDefaultValue(changedItem, changedItem.defaultValue);
            };
        }

        this.changedData.forEach(handleReverseItem);
        this.changedData = [];
        this.updateHasChangedData();
    };

    updateHasChangedData = ():boolean => {
        const curHasChangedData:boolean = (this.changedData.length > 0);
        if (curHasChangedData !== this.hasChangedData) {
            this.hasChangedData = curHasChangedData;
            if(this.hasChangedData) {
                this.EventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES, this.hasChangedData, this.showUnsavedChangesAlert);
            } else {
                this.EventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES, false);
            }
        } 
        return this.hasChangedData;
    };

    doSaveChangedData = (onSuccessFunction?:Function, onError?:Function):void => {
        this.saveChangedData().then(
            () => {
                this.Notification.success({
                    message: 'Successfully saved changes',
                    title: 'Saved'
                });
                if(onSuccessFunction) onSuccessFunction();
            },
            () => {
                this.Notification.error({
                    message: 'Failed to save changes!',
                    title: 'Failure'
                });
                if(onError) onError();
            }
        );
    };

    showUnsavedChangesAlert = ():Promise<any> => {
        let modalTitle:string;
        if (this.isPropertiesTabSelected) {
            modalTitle = `Unsaved properties for ${this.selectedInstanceData.name}`;
        } else if (this.isInputsTabSelected) {
            modalTitle = `Unsaved inputs for ${this.component.name}`;
        }

        return new Promise<any>((resolve, reject) => {
            const modal = this.ModalServiceSdcUI.openCustomModal(
                {
                    title: modalTitle,
                    size: 'sm',
                    type: 'custom',
                    testId: "id",
                    
                    buttons: [
                        {id: 'cancelButton', text: 'Cancel', type: 'secondary', size: 'xsm', closeModal: true, callback: () => reject()},
                        {id: 'discardButton', text: 'Discard', type: 'secondary', size: 'xsm', closeModal: true, callback: () => { this.reverseChangedData(); resolve()}},
                        {id: 'saveButton', text: 'Save', type: 'primary', size: 'xsm', closeModal: true, disabled: !this.isValidChangedData, callback: () => this.doSaveChangedData(resolve, reject)}
                ] as IModalButtonComponent[]
            }, UnsavedChangesComponent, {isValidChangedData: this.isValidChangedData});
        });

    }

    updatePropertyValueAfterDeclare = (input: InputFEModel) => {
        if (this.instanceFePropertiesMap[input.instanceUniqueId]) {
            let propertyForUpdatindVal = _.find(this.instanceFePropertiesMap[input.instanceUniqueId], (feProperty: PropertyFEModel) => {
                return feProperty.name == input.relatedPropertyName;
            });
            let inputPath = (input.inputPath && input.inputPath != propertyForUpdatindVal.name) ? input.inputPath : undefined;
            propertyForUpdatindVal.setAsDeclared(inputPath); //set prop as declared before assigning value
            this.propertiesService.disableRelatedProperties(propertyForUpdatindVal, inputPath);
            this.propertiesUtils.resetPropertyValue(propertyForUpdatindVal, input.relatedPropertyValue, inputPath);
        }
    }

    //used for declare button, to keep count of newly checked properties (and ignore declared properties)
    updateCheckedPropertyCount = (increment: boolean): void => {
        this.checkedPropertiesCount += (increment) ? 1 : -1;
        console.log("CheckedProperties count is now.... " + this.checkedPropertiesCount);
    };

    setInputTabIndication = (numInputs: number): void => {
        this.propertyInputTabs.setTabIndication('Inputs', numInputs);
    };

    resetUnsavedChangesForInput = (input:InputFEModel) => {
        this.inputsUtils.resetInputDefaultValue(input, input.defaultValue);
        this.changedData = this.changedData.filter((changedItem) => changedItem.uniqueId !== input.uniqueId);
        this.updateHasChangedData();
    }

    deleteInput = (input: InputFEModel) => {
        //reset any unsaved changes to the input before deleting it
        this.resetUnsavedChangesForInput(input);

        console.log("==>" + this.constructor.name + ": deleteInput");
        let inputToDelete = new InputBEModel(input);

        this.componentServiceNg2
            .deleteInput(this.component, inputToDelete)
            .subscribe(response => {
                this.inputs = this.inputs.filter(input => input.uniqueId !== response.uniqueId);

                //Reload the whole instance for now - TODO: CHANGE THIS after the BE starts returning properties within the response, use commented code below instead!
                this.changeSelectedInstance(this.selectedInstanceData);
                // let instanceFeProperties = this.instanceFePropertiesMap[this.getInstanceUniqueId(input.instanceName)];

                // if (instanceFeProperties) {
                //     let propToEnable: PropertyFEModel = instanceFeProperties.find((prop) => {
                //         return prop.name == input.propertyName;
                //     });

                //     if (propToEnable) {
                //         if (propToEnable.name == response.inputPath) response.inputPath = null;
                //         propToEnable.setNonDeclared(response.inputPath);
                //         //this.propertiesUtils.resetPropertyValue(propToEnable, newValue, response.inputPath);
                //         this.propertiesService.undoDisableRelatedProperties(propToEnable, response.inputPath);
                //     }
                // }
            }, error => {}); //ignore error
    };

    deleteProperty = (property: PropertyFEModel) => {
        let propertyToDelete = new PropertyFEModel(property);
        this.loadingProperties = true;
        let feMap = this.instanceFePropertiesMap;
        this.componentServiceNg2
            .deleteServiceProperty(this.component, propertyToDelete)
            .subscribe(response => {
                const props = feMap[this.component.uniqueId];
                props.splice(props.findIndex(p => p.uniqueId === response),1);
                this.loadingProperties = false;
            }, error => {
                this.loadingProperties = false;
                console.error(error);
            });
    };

    /*** addProperty ***/
    addProperty = () => {
        let modalTitle = 'Add Property';
        const modal = this.ModalService.createCustomModal(new ModalModel(
            'sm',
            modalTitle,
            null,
            [
                new ButtonModel('Save', 'blue', () => {
                    modal.instance.dynamicContent.instance.isLoading = true;
                    const newProperty: PropertyBEModel = modal.instance.dynamicContent.instance.propertyModel;
                    this.componentServiceNg2.createServiceProperty(this.component, newProperty)
                        .subscribe(response => {
                            modal.instance.dynamicContent.instance.isLoading = false;
                            let newProp: PropertyFEModel = this.propertiesUtils.convertAddPropertyBAToPropertyFE(response);
                            this.instanceFePropertiesMap[this.component.uniqueId].push(newProp);
                            modal.instance.close();
                        }, (error) => {
                            modal.instance.dynamicContent.instance.isLoading = false;
                            this.Notification.error({
                                message: 'Failed to add property:' + error,
                                title: 'Failure'
                            });
                        });

                }, () => !modal.instance.dynamicContent.instance.checkFormValidForSubmit()),
                new ButtonModel('Cancel', 'outline grey', () => {
                    modal.instance.close();
                }),
            ],
            null
        ));
        this.ModalService.addDynamicContentToModal(modal, PropertyCreatorComponent, {});
        modal.instance.open();
    };

    /*** SEARCH RELATED FUNCTIONS ***/
    searchPropertiesInstances = (filterData:FilterPropertiesAssignmentData) => {
        let instanceBePropertiesMap:InstanceBePropertiesMap;
        this.componentServiceNg2
            .filterComponentInstanceProperties(this.component, filterData)
            .subscribe(response => {

                this.processInstancePropertiesResponse(response, false);
                this.hierarchyPropertiesDisplayOptions.searchText = filterData.propertyName;//mark results in tree
                this.searchPropertyName = filterData.propertyName;//mark in table
                this.hierarchyNavTabs.triggerTabChange('Composition');
                this.propertiesNavigationData = [];
                this.displayClearSearch = true;
            }, error => {}); //ignore error

    };

    clearSearch = () => {
        this.instancesNavigationData = this.instances;
        this.searchPropertyName = "";
        this.hierarchyPropertiesDisplayOptions.searchText = "";
        this.displayClearSearch = false;
        this.advanceSearch.clearAll();
        this.searchQuery = '';
    };

    clickOnClearSearch = () => {
        this.clearSearch();
        this.selectFirstInstanceByDefault();
        this.hierarchyNavTabs.triggerTabChange('Composition');
    };

    private isInput = (instanceType:string):boolean =>{
        return instanceType === ResourceType.VF || instanceType === ResourceType.PNF || instanceType === ResourceType.CVFC || instanceType === ResourceType.CR;
    }

}
