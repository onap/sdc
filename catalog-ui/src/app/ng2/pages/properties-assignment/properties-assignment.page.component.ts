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
import {Component, Inject, ViewChild} from "@angular/core";
import {PropertiesService} from "../../services/properties.service";
import {
    ButtonModel,
    Component as ComponentData,
    ComponentInstance,
    DerivedFEProperty,
    FilterPropertiesAssignmentData,
    GroupInstance,
    InputBEModel,
    InputFEModel,
    InstanceBePropertiesMap,
    InstanceFePropertiesMap,
    InstancePropertiesAPIMap,
    ModalModel,
    PolicyInstance,
    PropertyBEModel,
    PropertyFEModel,
    Service,
    SimpleFlatProperty
} from "app/models";
import {ResourceType} from "app/utils";
import {ComponentServiceNg2} from "../../services/component-services/component.service";
import {TopologyTemplateService} from "../../services/component-services/topology-template.service";
import {ComponentInstanceServiceNg2} from "../../services/component-instance-services/component-instance.service"
import {KeysPipe} from 'app/ng2/pipes/keys.pipe';
import {EVENTS, PROPERTY_TYPES, WorkspaceMode} from "../../../utils/constants";
import {EventListenerService} from "app/services/event-listener-service"
import {HierarchyDisplayOptions} from "../../components/logic/hierarchy-navigtion/hierarchy-display-options";
import {FilterPropertiesAssignmentComponent} from "../../components/logic/filter-properties-assignment/filter-properties-assignment.component";
import {PropertyRowSelectedEvent} from "../../components/logic/properties-table/properties-table.component";
import {HierarchyNavService} from "./services/hierarchy-nav.service";
import {PropertiesUtils} from "./services/properties.utils";
import {ComponentModeService} from "../../services/component-services/component-mode.service";
import {Tab, Tabs} from "../../components/ui/tabs/tabs.component";
import {InputsUtils} from "./services/inputs.utils";
import {InstanceFeDetails} from "../../../models/instance-fe-details";
import {SdcUiCommon, SdcUiServices} from "onap-ui-angular";
import {UnsavedChangesComponent} from "app/ng2/components/ui/forms/unsaved-changes/unsaved-changes.component";
import {PropertyCreatorComponent} from "./property-creator/property-creator.component";
import {ModalService} from "../../services/modal.service";
import {DeclareListComponent} from "./declare-list/declare-list.component";
import {PropertyDropdownValue, ToscaFunctionComponent} from "./tosca-function/tosca-function.component";
import {CapabilitiesGroup, Capability} from "../../../models/capability";
import {ToscaPresentationData} from "../../../models/tosca-presentation";
import {Observable} from "rxjs";
import {ToscaGetFunctionType} from "../../../models/tosca-get-function-type";
import {TranslateService} from "../../shared/translator/translate.service";
import {ToscaGetFunctionDtoBuilder} from '../../../models/tosca-get-function-dto';
import {PropertySource} from '../../../models/property-source';
import {ToscaGetFunctionTypeConverter} from '../../../models/tosca-get-function-type-converter';

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

    instanceFePropertiesMap: InstanceFePropertiesMap;
    inputs: Array<InputFEModel> = [];
    policies: Array<PolicyInstance> = [];
    instances: Array<ComponentInstance | GroupInstance | PolicyInstance> = [];
    searchQuery: string;
    propertyStructureHeader: string;

    selectedFlatProperty: SimpleFlatProperty = new SimpleFlatProperty();
    selectedInstanceData: ComponentInstance | GroupInstance | PolicyInstance = null;
    checkedPropertiesCount: number = 0;
    checkedChildPropertiesCount: number = 0;

    hierarchyPropertiesDisplayOptions: HierarchyDisplayOptions = new HierarchyDisplayOptions('path', 'name', 'childrens');
    hierarchyInstancesDisplayOptions: HierarchyDisplayOptions = new HierarchyDisplayOptions('uniqueId', 'name', 'archived', null, 'iconClass');
    displayClearSearch = false;
    searchPropertyName: string;
    currentMainTab: Tab;
    isInputsTabSelected: boolean;
    isPropertiesTabSelected: boolean;
    isPoliciesTabSelected: boolean;
    isReadonly: boolean;
    resourceIsReadonly: boolean;
    loadingInstances: boolean = false;
    loadingInputs: boolean = false;
    loadingPolicies: boolean = false;
    loadingProperties: boolean = false;
    changedData: Array<PropertyFEModel | InputFEModel>;
    hasChangedData: boolean;
    isValidChangedData: boolean;
    savingChangedData: boolean;
    stateChangeStartUnregister: Function;
    serviceBePropertiesMap: InstanceBePropertiesMap;
    serviceBeCapabilitiesPropertiesMap: InstanceBePropertiesMap;
    selectedInstance_FlattenCapabilitiesList: Capability[];
    btnToscaFunctionText: string;

    @ViewChild('hierarchyNavTabs') hierarchyNavTabs: Tabs;
    @ViewChild('propertyInputTabs') propertyInputTabs: Tabs;
    @ViewChild('advanceSearch') advanceSearch: FilterPropertiesAssignmentComponent;

    constructor(private propertiesService: PropertiesService,
                private hierarchyNavService: HierarchyNavService,
                private propertiesUtils: PropertiesUtils,
                private inputsUtils: InputsUtils,
                private componentServiceNg2: ComponentServiceNg2,
                private componentInstanceServiceNg2: ComponentInstanceServiceNg2,
                private propertyCreatorComponent: PropertyCreatorComponent,
                @Inject("$stateParams") _stateParams,
                @Inject("$scope") private $scope: ng.IScope,
                @Inject("$state") private $state: ng.ui.IStateService,
                @Inject("Notification") private notification: any,
                private componentModeService: ComponentModeService,
                private eventListenerService: EventListenerService,
                private ModalServiceSdcUI: SdcUiServices.ModalService,
                private modalService: ModalService,
                private keysPipe: KeysPipe,
                private topologyTemplateService: TopologyTemplateService,
                private translateService: TranslateService) {

        this.instanceFePropertiesMap = new InstanceFePropertiesMap();
        /* This is the way you can access the component data, please do not use any data except metadata, all other data should be received from the new api calls on the first time
        than if the data is already exist, no need to call the api again - Ask orit if you have any questions*/
        this.component = _stateParams.component;
        this.eventListenerService.registerObserverCallback(EVENTS.ON_LIFECYCLE_CHANGE, this.onCheckout);
        this.updateViewMode();
        this.changedData = [];
        this.updateHasChangedData();
        this.isValidChangedData = true;
    }

    ngOnInit() {
        console.debug("==>" + this.constructor.name + ": ngOnInit");
        this.btnToscaFunctionText = this.translateService.translate('TOSCA_FUNCTION_LABEL');
        this.loadingInputs = true;
        this.loadingPolicies = true;
        this.loadingInstances = true;
        this.loadingProperties = true;
        this.topologyTemplateService
        .getComponentInputsWithProperties(this.component.componentType, this.component.uniqueId)
        .subscribe(response => {
            _.forEach(response.inputs, (input: InputBEModel) => {
                const newInput: InputFEModel = new InputFEModel(input);
                this.inputsUtils.resetInputDefaultValue(newInput, input.defaultValue);
                this.inputs.push(newInput); //only push items that were declared via SDC
            });
            this.loadingInputs = false;

        }, error => {
        }); //ignore error
        this.componentServiceNg2
        .getComponentResourcePropertiesData(this.component)
        .subscribe(response => {
            this.loadingPolicies = false;
            this.instances = [];
            this.instances.push(...response.componentInstances);
            this.instances.push(...response.groupInstances);
            this.instances.push(...response.policies);

            _.forEach(response.policies, (policy: any) => {
                const newPolicy: InputFEModel = new InputFEModel(policy);
                this.inputsUtils.resetInputDefaultValue(newPolicy, policy.defaultValue);
                this.policies.push(policy);
            });

            // add the service self instance to the top of the list.
            const serviceInstance = new ComponentInstance();
            serviceInstance.name = SERVICE_SELF_TITLE;
            serviceInstance.uniqueId = this.component.uniqueId;
            this.instances.unshift(serviceInstance);

            _.forEach(this.instances, (instance) => {
                this.instancesNavigationData.push(instance);
                this.componentInstanceNamesMap[instance.uniqueId] = <InstanceFeDetails>{
                    name: instance.name,
                    iconClass: instance.iconClass,
                    originArchived: instance.originArchived
                };
            });
            this.loadingInstances = false;
            if (this.instancesNavigationData[0] == undefined) {
                this.loadingProperties = false;
            }
            this.selectFirstInstanceByDefault();
        }, error => {
            this.loadingInstances = false;
        }); //ignore error

        this.stateChangeStartUnregister = this.$scope.$on('$stateChangeStart', (event, toState, toParams) => {
            // stop if has changed properties
            if (this.hasChangedData) {
                event.preventDefault();
                this.showUnsavedChangesAlert().then(() => {
                    this.$state.go(toState, toParams);
                }, () => {
                });
            }
        });

      this.loadDataTypesByComponentModel(this.component.model);
    }

    ngOnDestroy() {
        this.eventListenerService.unRegisterObserver(EVENTS.ON_LIFECYCLE_CHANGE);
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

    onCheckout = (component: ComponentData) => {
        this.component = component;
        this.updateViewMode();
    }

    isSelf = (): boolean => {
        return this.selectedInstanceData && this.selectedInstanceData.uniqueId == this.component.uniqueId;
    }

    showAddProperties = (): boolean => {
        if (this.component.isService() && !(<Service>this.component).isSubstituteCandidate()) {
            return false;
        }
        return this.isSelf();
    }

    getServiceProperties() {
        this.loadingProperties = true;
        this.topologyTemplateService
        .getServiceProperties(this.component.uniqueId)
        .subscribe((response) => {
            this.serviceBePropertiesMap = new InstanceBePropertiesMap();
            this.serviceBePropertiesMap[this.component.uniqueId] = response;
            this.processInstancePropertiesResponse(this.serviceBePropertiesMap, false);
            this.loadingProperties = false;
        }, (error) => {
            this.loadingProperties = false;
        });
    }

    onInstanceSelectedUpdate = (instance: ComponentInstance | GroupInstance | PolicyInstance) => {
        // stop if has changed properties
        if (this.hasChangedData) {
            this.showUnsavedChangesAlert().then((resolve) => {
                this.changeSelectedInstance(instance)
            }, (reject) => {
            });
            return;
        }
        this.changeSelectedInstance(instance);
    };

    changeSelectedInstance = (instance: ComponentInstance | GroupInstance | PolicyInstance) => {
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
                }, () => {
                    //ignore error
                }, () => {
                    this.loadingProperties = false;
                });
            } else if (this.isSelf()) {
                this.getServiceProperties();
            } else {
                this.componentInstanceServiceNg2
                .getComponentInstanceProperties(this.component, instance.uniqueId)
                .subscribe(response => {
                    instanceBePropertiesMap[instance.uniqueId] = response;
                    this.processInstancePropertiesResponse(instanceBePropertiesMap, false);
                }, () => {
                    //ignore error
                }, () => {
                    this.loadingProperties = false;
                });
            }
            this.loadingProperties = false;
            this.resourceIsReadonly = (instance.componentName === "vnfConfiguration");
        } else if (instance instanceof GroupInstance) {
            let instanceBePropertiesMap: InstanceBePropertiesMap = new InstanceBePropertiesMap();
            this.componentInstanceServiceNg2
            .getComponentGroupInstanceProperties(this.component, this.selectedInstanceData.uniqueId)
            .subscribe((response) => {
                instanceBePropertiesMap[instance.uniqueId] = response;
                this.processInstancePropertiesResponse(instanceBePropertiesMap, false);
            }, () => {
                //ignore error
            }, () => {
                this.loadingProperties = false;
            });
        } else if (instance instanceof PolicyInstance) {
            let instanceBePropertiesMap: InstanceBePropertiesMap = new InstanceBePropertiesMap();
            this.componentInstanceServiceNg2
            .getComponentPolicyInstanceProperties(this.component.componentType, this.component.uniqueId, this.selectedInstanceData.uniqueId)
            .subscribe((response) => {
                instanceBePropertiesMap[instance.uniqueId] = response;
                this.processInstancePropertiesResponse(instanceBePropertiesMap, false);
            }, () => {
                //ignore error
            }, () => {
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
        this.instanceFePropertiesMap = this.propertiesUtils.convertPropertiesMapToFEAndCreateChildren(instanceBePropertiesMap, originTypeIsVF, this.inputs, this.component.model); //create flattened children, disable declared props, and init values
        this.checkedPropertiesCount = 0;
        this.checkedChildPropertiesCount = 0;
    };

    processInstanceCapabilitiesPropertiesResponse = (originTypeIsVF: boolean) => {
        let selectedComponentInstanceData = <ComponentInstance>(this.selectedInstanceData);
        let currentUniqueId = this.selectedInstanceData.uniqueId;
        this.serviceBeCapabilitiesPropertiesMap = new InstanceBePropertiesMap();
        let isCapabilityOwnedByInstance: boolean;
        this.serviceBeCapabilitiesPropertiesMap[currentUniqueId] = _.reduce(
            this.selectedInstance_FlattenCapabilitiesList,
            (result, cap: Capability) => {
                isCapabilityOwnedByInstance = cap.ownerId === currentUniqueId ||
                    selectedComponentInstanceData.isServiceProxy() || selectedComponentInstanceData.isServiceSubstitution() &&
                    cap.ownerId === selectedComponentInstanceData.sourceModelUid;
                if (cap.properties && isCapabilityOwnedByInstance) {
                    _.forEach(cap.properties, prop => {
                        if (!prop.origName) {
                            prop.origName = prop.name;
                            prop.name = cap.name + '_' + prop.name;//for display. (before save - the name returns to its orig value: prop.name)
                        }
                    });
                    return result.concat(cap.properties);
                }
                return result;
            }, []);
        let instanceFECapabilitiesPropertiesMap = this.propertiesUtils.convertPropertiesMapToFEAndCreateChildren(this.serviceBeCapabilitiesPropertiesMap, originTypeIsVF, this.inputs); //create flattened children, disable declared props, and init values
        //update FECapabilitiesProperties with their origName according to BeCapabilitiesProperties
        _.forEach(instanceFECapabilitiesPropertiesMap[currentUniqueId], prop => {
            prop.origName = _.find(this.serviceBeCapabilitiesPropertiesMap[currentUniqueId], p => p.uniqueId === prop.uniqueId).origName;
        });
        //concatenate capabilitiesProps to all props list
        this.instanceFePropertiesMap[currentUniqueId] = (this.instanceFePropertiesMap[currentUniqueId] || []).concat(instanceFECapabilitiesPropertiesMap[currentUniqueId]);
        this.checkedPropertiesCount = 0;
    };

    isCapabilityProperty = (prop: PropertyBEModel) => {
        return _.find(this.selectedInstance_FlattenCapabilitiesList, cap => cap.uniqueId === prop.parentUniqueId);
    };

    /*** VALUE CHANGE EVENTS ***/
    dataChanged = (item: PropertyFEModel | InputFEModel) => {
        let itemHasChanged;
        if (this.isPropertiesTabSelected && item instanceof PropertyFEModel) {
            itemHasChanged = item.hasValueObjChanged();
        } else if (this.isInputsTabSelected && item instanceof InputFEModel) {
            itemHasChanged = item.hasChanged();
        } else if (this.isPoliciesTabSelected && item instanceof InputFEModel) {
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
            this.isValidChangedData = this.changedData.every((changedItem) => (<InputFEModel>changedItem).defaultValueObjIsValid && (<InputFEModel>changedItem).metadataIsValid);
        } else if (this.isPoliciesTabSelected) {
            this.isValidChangedData = this.changedData.every((changedItem) => (<InputFEModel>changedItem).defaultValueObjIsValid);
        }
        this.updateHasChangedData();
    };


    /*** HEIRARCHY/NAV RELATED FUNCTIONS ***/

    /**
     * Handle select node in navigation area, and select the row in table
     */
    onPropertySelectedUpdate = ($event) => {
        console.debug("==>" + this.constructor.name + ": onPropertySelectedUpdate");
        this.selectedFlatProperty = $event;
        let parentProperty: PropertyFEModel = this.propertiesService.getParentPropertyFEModelFromPath(this.instanceFePropertiesMap[this.selectedFlatProperty.instanceName], this.selectedFlatProperty.path);
        parentProperty.expandedChildPropertyId = this.selectedFlatProperty.path;
    };

    /**
     * When user select row in table, this will prepare the hirarchy object for the tree.
     */
    selectPropertyRow = (propertyRowSelectedEvent: PropertyRowSelectedEvent) => {
        console.debug("==>" + this.constructor.name + ": selectPropertyRow " + propertyRowSelectedEvent.propertyModel.name);
        let property = propertyRowSelectedEvent.propertyModel;
        let instanceName = propertyRowSelectedEvent.instanceName;
        this.propertyStructureHeader = null;

        // Build hirarchy tree for the navigation and update propertiesNavigationData with it.
        if (!(this.selectedInstanceData instanceof ComponentInstance) || this.selectedInstanceData.originType !== ResourceType.VF) {
            let simpleFlatProperty: Array<SimpleFlatProperty>;
            if (property instanceof PropertyFEModel) {
                simpleFlatProperty = this.hierarchyNavService.getSimplePropertiesTree(property, instanceName);
            } else if (property instanceof DerivedFEProperty) {
                // Need to find parent PropertyFEModel
                let parentPropertyFEModel: PropertyFEModel = _.find(this.instanceFePropertiesMap[instanceName], (tmpFeProperty): boolean => {
                    return property.propertiesName.indexOf(tmpFeProperty.name) === 0;
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
        this.selectedInstanceData = _.find(this.instancesNavigationData, (instance: ComponentInstance) => {
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
            }, () => {
            });
            return;
        }

        console.debug("==>" + this.constructor.name + ": tabChanged " + event);
        this.currentMainTab = this.propertyInputTabs.tabs.find((tab) => tab.title === event.title);
        this.isPropertiesTabSelected = this.currentMainTab.title === "Properties";
        this.isInputsTabSelected = this.currentMainTab.title === "Inputs";
        this.isPoliciesTabSelected = this.currentMainTab.title === "Policies";
        this.propertyStructureHeader = null;
        this.searchQuery = '';
    };

    /**
     * Select Tosca function value from defined values
     */
    selectToscaFunctionAndValues = (): void => {
        const selectedInstanceData: ComponentInstance = this.getSelectedComponentInstance();
        if (!selectedInstanceData) {
            return;
        }
        const property: PropertyBEModel = this.buildCheckedInstanceProperty();
        if (property.isToscaGetFunction()) {
            this.clearCheckedInstancePropertyValue();
            return;
        }
        this.openToscaGetFunctionModal();
    }

    private getSelectedComponentInstance(): ComponentInstance {
        const instancesIds = this.keysPipe.transform(this.instanceFePropertiesMap, []);
        const instanceId: string = instancesIds[0];
        return <ComponentInstance> this.instances.find(instance => instance.uniqueId == instanceId && instance instanceof ComponentInstance);
    }

    private buildCheckedInstanceProperty(): PropertyBEModel {
        return this.buildCheckedInstanceProperties()[0];
    }

    private buildCheckedInstanceProperties(): PropertyBEModel[] {
        const instancesIds = this.keysPipe.transform(this.instanceFePropertiesMap, []);
        const instanceId: string = instancesIds[0];
        return this.propertiesService.getCheckedProperties(this.instanceFePropertiesMap[instanceId]);
    }

    private openToscaGetFunctionModal() {
        const modalTitle = 'Set value using TOSCA functions';
        const modal = this.modalService.createCustomModal(new ModalModel(
            'sm',
            modalTitle,
            null,
            [
                new ButtonModel(this.translateService.translate('MODAL_SAVE'), 'blue',
                    () => {
                        const selectedToscaFunction = modal.instance.dynamicContent.instance.selectToscaFunction;
                        const selectedPropertyFromModal:PropertyDropdownValue = modal.instance.dynamicContent.instance.selectedProperty;
                        const toscaFunctionType: ToscaGetFunctionType = ToscaGetFunctionTypeConverter.convertFromString(selectedToscaFunction);
                        this.updateCheckedInstancePropertyGetFunctionValue(selectedPropertyFromModal, toscaFunctionType);
                        modal.instance.close();
                    }
                ),
                new ButtonModel(this.translateService.translate('MODAL_CANCEL'), 'outline grey', () => {
                    modal.instance.close();
                }),
            ],
            null /* type */
        ));
        const checkedInstanceProperty = this.buildCheckedInstanceProperty();
        this.modalService.addDynamicContentToModalAndBindInputs(modal, ToscaFunctionComponent, {
            'property': checkedInstanceProperty,
        });
        modal.instance.open();
    }

    private clearCheckedInstancePropertyValue() {
        const checkedInstanceProperty: PropertyBEModel = this.buildCheckedInstanceProperty();
        checkedInstanceProperty.getInputValues = null;
        checkedInstanceProperty.value = null;
        checkedInstanceProperty.toscaGetFunction = null;
        this.updateInstanceProperty(checkedInstanceProperty);
    }

    private updateCheckedInstancePropertyGetFunctionValue(propertyToGet: PropertyDropdownValue, toscaGetFunctionType: ToscaGetFunctionType) {
        const toscaGetFunctionBuilder: ToscaGetFunctionDtoBuilder =
            new ToscaGetFunctionDtoBuilder()
                .withPropertyUniqueId(propertyToGet.propertyId)
                .withFunctionType(toscaGetFunctionType)
                .withPropertySource(PropertySource.SELF)
                .withPropertyName(propertyToGet.propertyName)
                .withSourceName(this.component.name)
                .withSourceUniqueId(this.component.uniqueId);

        const checkedProperty: PropertyBEModel = this.buildCheckedInstanceProperty();
        if (propertyToGet.propertyPath && propertyToGet.propertyPath.length) {
            toscaGetFunctionBuilder.withPropertyPathFromSource(propertyToGet.propertyPath);
        }
        checkedProperty.toscaGetFunction = toscaGetFunctionBuilder.build();
        this.updateInstanceProperty(checkedProperty);
    }

    updateInstanceProperty(instanceProperty: PropertyBEModel) {
        this.loadingProperties = true;
        this.componentInstanceServiceNg2.updateInstanceProperties(this.component.componentType, this.component.uniqueId,
            this.selectedInstanceData.uniqueId, [instanceProperty])
        .subscribe(() => {
            this.changeSelectedInstance(this.getSelectedComponentInstance());
        }, (error) => {
            const errorMsg =
                this.translateService.translate('TOSCA_FUNCTION_SELECT_ERROR', {'propertyName': instanceProperty.name, 'error': error});
            this.notification.error({
                title: this.translateService.translate('FAILURE_LABEL'),
                message: errorMsg
            });
            console.error(errorMsg, error);
        }, () => {
            this.loadingProperties = false;
            this.btnToscaFunctionText = this.translateService.translate('TOSCA_FUNCTION_LABEL');
        });
    }

    selectInputBtnLabel = () => {
        let instancesIds = this.keysPipe.transform(this.instanceFePropertiesMap, []);
        angular.forEach(instancesIds, (instanceId: string): void => {
            let checkedProperties: PropertyBEModel[] = this.propertiesService.getCheckedProperties(this.instanceFePropertiesMap[instanceId]);
            angular.forEach(checkedProperties, (property: PropertyBEModel) => {
                if(this.checkedPropertiesCount == 1) {
                    if (property.isToscaGetFunction()) {
                        this.btnToscaFunctionText = this.translateService.translate('CLEAR_VALUE_LABEL');
                    } else {
                        this.btnToscaFunctionText = this.translateService.translate('TOSCA_FUNCTION_LABEL');
                    }
                } else {
                    this.btnToscaFunctionText = this.translateService.translate('TOSCA_FUNCTION_LABEL');
                }
            });
        });
    }

    /*** DECLARE PROPERTIES/INPUTS ***/
    declareProperties = (): void => {
        console.debug("==>" + this.constructor.name + ": declareProperties");

        let selectedComponentInstancesProperties: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        let selectedGroupInstancesProperties: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        let selectedPolicyInstancesProperties: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        let selectedComponentInstancesInputs: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        let instancesIds = this.keysPipe.transform(this.instanceFePropertiesMap, []);

        angular.forEach(instancesIds, (instanceId: string): void => {
            let selectedInstanceData: any = this.instances.find(instance => instance.uniqueId == instanceId);
            if (selectedInstanceData instanceof ComponentInstance) {
                if (!this.isInput(selectedInstanceData.originType)) {
                    // convert Property FE model -> Property BE model, extract only checked
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

	//move changed capabilities properties from componentInstanceInputsMap obj to componentInstanceProperties
        inputsToCreate.componentInstanceProperties[this.selectedInstanceData.uniqueId] =
            (inputsToCreate.componentInstanceProperties[this.selectedInstanceData.uniqueId] || []).concat(
                _.filter(
                    inputsToCreate.componentInstanceInputsMap[this.selectedInstanceData.uniqueId],
                    (prop: PropertyBEModel) => this.isCapabilityProperty(prop)
                )
            );
        inputsToCreate.componentInstanceInputsMap[this.selectedInstanceData.uniqueId] = _.filter(
            inputsToCreate.componentInstanceInputsMap[this.selectedInstanceData.uniqueId],
            prop => !this.isCapabilityProperty(prop)
        );
        if (inputsToCreate.componentInstanceInputsMap[this.selectedInstanceData.uniqueId].length === 0) {
            delete inputsToCreate.componentInstanceInputsMap[this.selectedInstanceData.uniqueId];
        }

        let isCapabilityPropertyChanged = false;
        _.forEach(
            inputsToCreate.componentInstanceProperties[this.selectedInstanceData.uniqueId],
            (prop: PropertyBEModel) => {
                prop.name = prop.origName || prop.name;
                if (this.isCapabilityProperty(prop)) {
                    isCapabilityPropertyChanged = true;
                }
            }
        );
        this.topologyTemplateService
            .createInput(this.component, inputsToCreate, this.isSelf())
            .subscribe((response) => {
                this.selectInstanceRow(SERVICE_SELF_TITLE);
                this.onInstanceSelectedUpdate(this.instances[0]);
                this.setInputTabIndication(response.length);
                this.checkedPropertiesCount = 0;
                this.checkedChildPropertiesCount = 0;
                _.forEach(response, (input: InputBEModel) => {
                    const newInput: InputFEModel = new InputFEModel(input);
                    this.inputsUtils.resetInputDefaultValue(newInput, input.defaultValue);
                    this.inputs.push(newInput);
                    this.updatePropertyValueAfterDeclare(newInput);
                });
                if (isCapabilityPropertyChanged) {
                    this.reloadInstanceCapabilities();
                }
            }, error => {}); //ignore error
    };

    declareListProperties = (): void => {
        // get selected properties
        let selectedComponentInstancesProperties: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        let selectedGroupInstancesProperties: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        let selectedPolicyInstancesProperties: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        let selectedComponentInstancesInputs: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        let instancesIds = new KeysPipe().transform(this.instanceFePropertiesMap, []);
        let propertyNameList: Array<string> = [];
        let insId :string;

        angular.forEach(instancesIds, (instanceId: string): void => {
            insId = instanceId;
            let selectedInstanceData: any = this.instances.find(instance => instance.uniqueId == instanceId);
            let checkedProperties: PropertyBEModel[] = this.propertiesService.getCheckedProperties(this.instanceFePropertiesMap[instanceId]);

            if (selectedInstanceData instanceof ComponentInstance) {
                if (!this.isInput(selectedInstanceData.originType)) {
                    // convert Property FE model -> Property BE model, extract only checked
                    selectedComponentInstancesProperties[instanceId] = checkedProperties;
                } else {
                    selectedComponentInstancesInputs[instanceId] = checkedProperties;
                }
            } else if (selectedInstanceData instanceof GroupInstance) {
                selectedGroupInstancesProperties[instanceId] = checkedProperties;
            } else if (selectedInstanceData instanceof PolicyInstance) {
                selectedPolicyInstancesProperties[instanceId] = checkedProperties;
            }

            angular.forEach(checkedProperties, (property: PropertyBEModel) => {
                propertyNameList.push(property.name);
            });
        });

        let inputsToCreate: InstancePropertiesAPIMap = new InstancePropertiesAPIMap(selectedComponentInstancesInputs, selectedComponentInstancesProperties, selectedGroupInstancesProperties, selectedPolicyInstancesProperties);

        let modalTitle = 'Declare Properties as List Input';
        const modal = this.modalService.createCustomModal(new ModalModel(
            'sm', /* size */
            modalTitle, /* title */
            null, /* content */
            [ /* buttons */
                new ButtonModel(
                    'Save', /* text */
                    'blue', /* css class */
                    () => { /* callback */
                        let content:any = modal.instance.dynamicContent.instance;

                        /* listInput */
                        let reglistInput: InstanceBePropertiesMap = new InstanceBePropertiesMap();
                        let typelist: any = PROPERTY_TYPES.LIST;
                        let uniID: any = insId;
                        let boolfalse: any = false;
                        let required: any = content.propertyModel.required;
                        let schem :any = {
                            "empty": boolfalse,
                            "property": {
                                "type": content.propertyModel.simpleType,
                                "required": required
                            }
                        }
                        let schemaProp :any = {
                            "type": content.propertyModel.simpleType,
                            "required": required
                        }

                        reglistInput.description = content.propertyModel.description;
                        reglistInput.name = content.propertyModel.name;
                        reglistInput.type = typelist;
                        reglistInput.schemaType = content.propertyModel.simpleType;
                        reglistInput.instanceUniqueId = uniID;
                        reglistInput.uniqueId = uniID;
                        reglistInput.required = required;
                        reglistInput.schema = schem;
                        reglistInput.schemaProperty = schemaProp;

                        let input = {
                            componentInstInputsMap: content.inputsToCreate,
                            listInput: reglistInput
                        };

                        this.topologyTemplateService
                        .createListInput(this.component, input, this.isSelf())
                        .subscribe(response => {
                            this.setInputTabIndication(response.length);
                            this.checkedPropertiesCount = 0;
                            this.checkedChildPropertiesCount = 0;
                            _.forEach(response, (input: InputBEModel) => {
                                let newInput: InputFEModel = new InputFEModel(input);
                                this.inputsUtils.resetInputDefaultValue(newInput, input.defaultValue);
                                this.inputs.push(newInput);
                                // create list input does not return updated properties info, so need to reload
                                //this.updatePropertyValueAfterDeclare(newInput);
                                // Reload the whole instance for now - TODO: CHANGE THIS after the BE starts returning properties within the response, use commented code below instead!
                                this.changeSelectedInstance(this.selectedInstanceData);

                                modal.instance.close();
                            });
                        }, error => {}); //ignore error
            
                    }
                    /*, getDisabled: function */
                ),
                new ButtonModel('Cancel', 'outline grey', () => {
                    modal.instance.close();
                }),
            ],
            null /* type */
        ));
        // 3rd arg is passed to DeclareListComponent instance
        this.modalService.addDynamicContentToModal(modal, DeclareListComponent, {properties: inputsToCreate, propertyNameList: propertyNameList});
        modal.instance.open();
    };

     /*** DECLARE PROPERTIES/POLICIES ***/
     declarePropertiesToPolicies = (): void => {
        let selectedComponentInstancesProperties: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        let instancesIds = new KeysPipe().transform(this.instanceFePropertiesMap, []);

        angular.forEach(instancesIds, (instanceId: string): void => {
            let selectedInstanceData: any = this.instances.find(instance => instance.uniqueId == instanceId);
            if (selectedInstanceData instanceof ComponentInstance) {
                if (!this.isInput(selectedInstanceData.originType)) {
                    selectedComponentInstancesProperties[instanceId] = this.propertiesService.getCheckedProperties(this.instanceFePropertiesMap[instanceId]);
                }
            }
        });

        let policiesToCreate: InstancePropertiesAPIMap = new InstancePropertiesAPIMap(null, selectedComponentInstancesProperties, null, null);
        this.loadingPolicies = true;

        this.topologyTemplateService
            .createPolicy(this.component, policiesToCreate, this.isSelf())
            .subscribe(response => {
                this.setPolicyTabIndication(response.length);
                this.checkedPropertiesCount = 0;
                this.displayPoliciesAsDeclared(response);
                this.loadingPolicies = false;
            }); //ignore error

    }

    displayPoliciesAsDeclared = (policies) => {
        _.forEach(policies, (policy: any) => {
            let newPolicy: InputFEModel = new InputFEModel(policy);
            this.inputsUtils.resetInputDefaultValue(newPolicy, policy.defaultValue);
            newPolicy.relatedPropertyName = policy.name;
            newPolicy.relatedPropertyValue = policy.value;
            this.updatePropertyValueAfterDeclare(newPolicy);
            this.policies.push(policy);
        });
    }

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
            let changedInputsProperties = [], changedCapabilitiesProperties = [];
            if (this.isPropertiesTabSelected) {
                const changedProperties: PropertyBEModel[] = this.changedData.map((changedProp) => {
                    changedProp = <PropertyFEModel>changedProp;
                    const propBE = new PropertyBEModel(changedProp);
                    propBE.toscaPresentation = new ToscaPresentationData();
                    propBE.toscaPresentation.ownerId = changedProp.parentUniqueId;
                    propBE.value = changedProp.getJSONValue();
                    propBE.name = changedProp.origName || changedProp.name;
                    delete propBE.origName;
                    return propBE;
                });
                changedCapabilitiesProperties = _.filter(changedProperties, prop => this.isCapabilityProperty(prop));

                if (this.selectedInstanceData instanceof ComponentInstance) {
                    if (this.isInput(this.selectedInstanceData.originType)) {
                        changedInputsProperties = _.filter(changedProperties, prop => !this.isCapabilityProperty(prop));
                        if (changedInputsProperties.length && changedCapabilitiesProperties.length) {
                            request = Observable.forkJoin(
                                this.componentInstanceServiceNg2.updateInstanceInputs(this.component, this.selectedInstanceData.uniqueId, changedInputsProperties),
                                this.componentInstanceServiceNg2.updateInstanceProperties(this.component.componentType, this.component.uniqueId,
                                    this.selectedInstanceData.uniqueId, changedCapabilitiesProperties)
                            );
                        }
                        else if (changedInputsProperties.length) {
                            request = this.componentInstanceServiceNg2
                                .updateInstanceInputs(this.component, this.selectedInstanceData.uniqueId, changedInputsProperties);
                        }
                        else if (changedCapabilitiesProperties.length) {
                            request = this.componentInstanceServiceNg2
                                .updateInstanceProperties(this.component.componentType, this.component.uniqueId, this.selectedInstanceData.uniqueId, changedCapabilitiesProperties);
                        }
                        handleSuccess = (response) => {
                            // reset each changed property with new value and remove it from changed properties list
                            response.forEach((resInput) => {
                                const changedProp = <PropertyFEModel>this.changedData.shift();
                                this.propertiesUtils.resetPropertyValue(changedProp, resInput.value);
                            });
                        };
                    } else {
                        if (this.isSelf()) {
                            request = this.topologyTemplateService.updateServiceProperties(this.component.uniqueId,  _.map(changedProperties, cp => {
                                delete cp.constraints;
                                return cp;
                            }));
                        } else {
                            request = this.componentInstanceServiceNg2
                                .updateInstanceProperties(this.component.componentType, this.component.uniqueId, this.selectedInstanceData.uniqueId, changedProperties);
                        }
                        handleSuccess = (response) => {
                            // reset each changed property with new value and remove it from changed properties list
                            response.forEach((resProp) => {
                                const changedProp = <PropertyFEModel>this.changedData.shift();
                                this.propertiesUtils.resetPropertyValue(changedProp, resProp.value);
                            });
                            resolve(response);
                        };
                    }
                } else if (this.selectedInstanceData instanceof GroupInstance) {
                    request = this.componentInstanceServiceNg2
                        .updateComponentGroupInstanceProperties(this.component.componentType, this.component.uniqueId, this.selectedInstanceData.uniqueId, changedProperties);
                    handleSuccess = (response) => {
                        // reset each changed property with new value and remove it from changed properties list
                        response.forEach((resProp) => {
                            const changedProp = <PropertyFEModel>this.changedData.shift();
                            this.propertiesUtils.resetPropertyValue(changedProp, resProp.value);
                        });
                        resolve(response);
                    };
                } else if (this.selectedInstanceData instanceof PolicyInstance) {
                    request = this.componentInstanceServiceNg2
                        .updateComponentPolicyInstanceProperties(this.component.componentType, this.component.uniqueId, this.selectedInstanceData.uniqueId, changedProperties);
                    handleSuccess = (response) => {
                        // reset each changed property with new value and remove it from changed properties list
                        response.forEach((resProp) => {
                            const changedProp = <PropertyFEModel>this.changedData.shift();
                            this.propertiesUtils.resetPropertyValue(changedProp, resProp.value);
                        });
                        resolve(response);
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
                        changedInput.required = resInput.required;
                        changedInput.requiredOrig = resInput.required;
                    });
                }
            }

            this.savingChangedData = true;
            request.subscribe(
                (response) => {
                    this.savingChangedData = false;
                    if (changedCapabilitiesProperties.length) {
                        this.reloadInstanceCapabilities();
                    }
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

    reloadInstanceCapabilities = (): void => {
        let currentInstanceIndex = _.findIndex(this.instances, instance => instance.uniqueId == this.selectedInstanceData.uniqueId);
        this.componentServiceNg2.getComponentResourceInstances(this.component).subscribe(result => {
            let instanceCapabilitiesData: CapabilitiesGroup = _.reduce(result.componentInstances, (res, instance) => {
                if (instance.uniqueId === this.selectedInstanceData.uniqueId) {
                    return instance.capabilities;
                }
                return res;
            }, new CapabilitiesGroup());
            (<ComponentInstance>this.instances[currentInstanceIndex]).capabilities = instanceCapabilitiesData;
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
                changedItem.resetMetadata();
                changedItem.required = changedItem.requiredOrig;
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
                this.eventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES, this.hasChangedData, this.showUnsavedChangesAlert);
            } else {
                this.eventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES, false);
            }
        } 
        return this.hasChangedData;
    };

    doSaveChangedData = (onSuccessFunction?:Function, onError?:Function):void => {
        this.saveChangedData().then(
            () => {
                this.notification.success({
                    message: 'Successfully saved changes',
                    title: 'Saved'
                });
                if(onSuccessFunction) onSuccessFunction();
            },
            () => {
                this.notification.error({
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
                    type: SdcUiCommon.ModalType.custom,
                    testId: "navigate-modal",

                    buttons: [
                        {id: 'cancelButton', text: 'Cancel', type: SdcUiCommon.ButtonType.secondary, size: 'xsm', closeModal: true, callback: () => reject()},
                        {id: 'discardButton', text: 'Discard', type: SdcUiCommon.ButtonType.secondary, size: 'xsm', closeModal: true, callback: () => { this.reverseChangedData(); resolve()}},
                        {id: 'saveButton', text: 'Save', type: SdcUiCommon.ButtonType.primary, size: 'xsm', closeModal: true, disabled: !this.isValidChangedData, callback: () => this.doSaveChangedData(resolve, reject)}
                    ] as SdcUiCommon.IModalButtonComponent[]
                } as SdcUiCommon.IModalConfig, UnsavedChangesComponent, {isValidChangedData: this.isValidChangedData});
        });

    }

    updatePropertyValueAfterDeclare = (input: InputFEModel) => {
        if (this.instanceFePropertiesMap[input.instanceUniqueId]) {
            const instanceName = input.instanceUniqueId.slice(input.instanceUniqueId.lastIndexOf('.') + 1);
            const propertyForUpdatindVal = _.find(this.instanceFePropertiesMap[input.instanceUniqueId], (feProperty: PropertyFEModel) => {
                return feProperty.name == input.relatedPropertyName &&
                    (feProperty.name == input.relatedPropertyName || input.name === instanceName.concat('_').concat(feProperty.name.replace(/[.]/g, '_')));
            });
            const inputPath = (input.inputPath && input.inputPath != propertyForUpdatindVal.name) ? input.inputPath : undefined;
            propertyForUpdatindVal.setAsDeclared(inputPath); //set prop as declared before assigning value
            this.propertiesService.disableRelatedProperties(propertyForUpdatindVal, inputPath);
            this.propertiesUtils.resetPropertyValue(propertyForUpdatindVal, input.relatedPropertyValue, inputPath);
        }
    }

    //used for declare button, to keep count of newly checked properties (and ignore declared properties)
    updateCheckedPropertyCount = (increment: boolean): void => {
        this.checkedPropertiesCount += (increment) ? 1 : -1;
        console.debug("CheckedProperties count is now.... " + this.checkedPropertiesCount);
        this.selectInputBtnLabel();
    };

    updateCheckedChildPropertyCount = (increment: boolean): void => {
        this.checkedChildPropertiesCount += (increment) ? 1 : -1;
    };

    setInputTabIndication = (numInputs: number): void => {
        this.propertyInputTabs.setTabIndication('Inputs', numInputs);
    };

    setPolicyTabIndication = (numPolicies: number): void => {
        this.propertyInputTabs.setTabIndication('Policies', numPolicies);
    }

    resetUnsavedChangesForInput = (input:InputFEModel) => {
        this.inputsUtils.resetInputDefaultValue(input, input.defaultValue);
        this.changedData = this.changedData.filter((changedItem) => changedItem.uniqueId !== input.uniqueId);
        this.updateHasChangedData();
    }

    deleteInput = (input: InputFEModel) => {
        //reset any unsaved changes to the input before deleting it
        this.resetUnsavedChangesForInput(input);

        console.debug("==>" + this.constructor.name + ": deleteInput");
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

    deletePolicy = (policy: PolicyInstance) => {
        this.loadingPolicies = true;
        this.topologyTemplateService
            .deletePolicy(this.component, policy)
            .subscribe((response) => {
                this.policies = this.policies.filter(policy => policy.uniqueId !== response.uniqueId);
                this.changeSelectedInstance(this.selectedInstanceData);
                this.loadingPolicies = false;
            });
    };

    deleteProperty = (property: PropertyFEModel) => {
        const propertyToDelete = new PropertyFEModel(property);
        this.loadingProperties = true;
        const feMap = this.instanceFePropertiesMap;
        this.topologyTemplateService
            .deleteServiceProperty(this.component.uniqueId, propertyToDelete)
            .subscribe((response) => {
                const props = feMap[this.component.uniqueId];
                props.splice(props.findIndex(p => p.uniqueId === response),1);
                this.loadingProperties = false;
            }, (error) => {
                this.loadingProperties = false;
                console.error(error);
            });
    }

    /*** addProperty ***/
    addProperty = (model: string) => {
        this.loadDataTypesByComponentModel(model)
        let modalTitle = 'Add Property';
        let modal = this.modalService.createCustomModal(new ModalModel(
            'sm',
            modalTitle,
            null,
            [
                new ButtonModel('Save', 'blue', () => {
                    modal.instance.dynamicContent.instance.isLoading = true;
                    const newProperty: PropertyBEModel = modal.instance.dynamicContent.instance.propertyModel;
                    this.topologyTemplateService.createServiceProperty(this.component.uniqueId, newProperty)
                        .subscribe((response) => {
                            modal.instance.dynamicContent.instance.isLoading = false;
                            const newProp: PropertyFEModel = this.propertiesUtils.convertAddPropertyBAToPropertyFE(response);
                            this.instanceFePropertiesMap[this.component.uniqueId].push(newProp);
                            modal.instance.close();
                        }, (error) => {
                            modal.instance.dynamicContent.instance.isLoading = false;
                            this.notification.error({
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
        modal.instance.open();
        this.modalService.addDynamicContentToModal(modal, PropertyCreatorComponent, {});
    }

    /*** addInput ***/
    addInput = () => {
        let modalTitle = 'Add Input';
        let modal = this.modalService.createCustomModal(new ModalModel(
            'sm',
            modalTitle,
            null,
            [
                new ButtonModel('Save', 'blue', () => {
                    modal.instance.dynamicContent.instance.isLoading = true;
                    const newInput: InputBEModel = modal.instance.dynamicContent.instance.propertyModel;
                    this.topologyTemplateService.createServiceInput(this.component.uniqueId, newInput)
                        .subscribe((response) => {
                            modal.instance.dynamicContent.instance.isLoading = false;
                            const newInputProp: InputFEModel = this.inputsUtils.convertInputBEToInputFE(response);
                            this.inputs.push(newInputProp);
                            modal.instance.close();
                        }, (error) => {
                            modal.instance.dynamicContent.instance.isLoading = false;
                            this.notification.error({
                                message: 'Failed to add input:' + error,
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
        this.modalService.addDynamicContentToModal(modal, PropertyCreatorComponent, {});
        modal.instance.open();
    }

    /*** SEARCH RELATED FUNCTIONS ***/
    searchPropertiesInstances = (filterData:FilterPropertiesAssignmentData) => {
        let instanceBePropertiesMap:InstanceBePropertiesMap;
        this.componentServiceNg2
            .filterComponentInstanceProperties(this.component, filterData)
            .subscribe((response) => {
                this.processInstancePropertiesResponse(response, false);
                this.hierarchyPropertiesDisplayOptions.searchText = filterData.propertyName;//mark results in tree
                this.searchPropertyName = filterData.propertyName;//mark in table
                this.hierarchyNavTabs.triggerTabChange('Composition');
                this.propertiesNavigationData = [];
                this.displayClearSearch = true;
            }, (error) => {}); //ignore error

    }

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

    loadDataTypesByComponentModel(model:string) {
        this.propertyCreatorComponent.filterDataTypesByModel(model);
    }

}
