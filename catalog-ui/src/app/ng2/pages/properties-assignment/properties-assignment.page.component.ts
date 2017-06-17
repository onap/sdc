import {Component, ViewChild, ElementRef, Renderer, Inject} from "@angular/core";
import {PostsService} from "../../services/posts.service";
import { PropertiesService } from "../../services/properties.service";
import { HierarchyNavService } from "../../services/hierarchy-nav.service";
import { PropertiesUtils } from './properties.utils';
import { PropertyFEModel, InstanceFePropertiesMap, InstanceBePropertiesMap, InstancePropertiesAPIMap, Component as ComponentData, FilterPropertiesAssignmentData } from "app/models";
import { PROPERTY_TYPES, ResourceType } from "app/utils";
import property = require("lodash/property");
import {ComponentServiceNg2} from "../../services/component-services/component.service";
import {ComponentInstanceServiceNg2} from "../../services/component-instance-services/component-instance.service"
import { InputFEModel, ComponentInstance, PropertyBEModel, DerivedPropertyType, DerivedFEProperty, ResourceInstance, SimpleFlatProperty } from "app/models";
import {HierarchyDisplayOptions} from "../../components/hierarchy-navigtion/hierarchy-display-options"
import {PropertyRowSelectedEvent} from "./../../components/properties-table/properties-table.component";
import { KeysPipe } from 'app/ng2/pipes/keys.pipe';
import {FilterPropertiesAssignmentComponent} from "../../components/filter-properties-assignment/filter-properties-assignment.component";
import { ComponentModeService } from "app/ng2/services/component-mode.service"
import {WorkspaceMode, EVENTS} from "../../../utils/constants";
import {ComponentInstanceProperty, InputBEModel} from "app/models"
import {ComponentInstanceInput} from "../../../models/properties-inputs/input-be-model";
import {EventListenerService} from "app/services/event-listener-service"
@Component({
    templateUrl: './properties-assignment.page.component.html',
    styleUrls: ['./properties-assignment.page.component.less']
})
export class PropertiesAssignmentComponent {
    title = "Properties & Inputs";

    component:ComponentData;

    propertiesNavigationData = [];
    instancesNavigationData = [];

    instanceFePropertiesMap:InstanceFePropertiesMap;
    inputs: Array<InputFEModel> = [];
    instances: Array<ComponentInstance> = [];
    searchQuery: string;
    propertyStructureHeader: string;

    selectedFlatProperty: SimpleFlatProperty = new SimpleFlatProperty();
    selectedInstanceType: string;
    selectedInstanceData: ComponentInstance = new ComponentInstance();
    checkedPropertiesCount: number = 0;

    hierarchyPropertiesDisplayOptions:HierarchyDisplayOptions = new HierarchyDisplayOptions('path', 'name', 'childrens');
    hierarchyInstancesDisplayOptions:HierarchyDisplayOptions = new HierarchyDisplayOptions('uniqueId', 'name');
    displayClearSearch = false;
    searchPropertyName:string;
    isInpusTabSelected:boolean;
    isReadonly:boolean;
    loadingInstances:boolean = false;
    loadingInputs:boolean = false;
    loadingProperties:boolean = false;

    @ViewChild('hierarchyNavTabs') hierarchyNavTabs: ElementRef;
    @ViewChild('propertyInputTabs') propertyInputTabs: ElementRef;
    @ViewChild('advanceSearch') advanceSearch: FilterPropertiesAssignmentComponent;

    constructor(private propertiesService: PropertiesService,
                private hierarchyNavService: HierarchyNavService,
                private propertiesUtils:PropertiesUtils,
                private componentServiceNg2:ComponentServiceNg2,
                private componentInstanceServiceNg2:ComponentInstanceServiceNg2,
                @Inject("$stateParams") _stateParams,
                private renderer: Renderer,
                private componentModeService:ComponentModeService,
                private EventListenerService:EventListenerService) {

        this.instanceFePropertiesMap = new InstanceFePropertiesMap();

        /* This is the way you can access the component data, please do not use any data except metadata, all other data should be received from the new api calls on the first time
        than if the data is already exist, no need to call the api again - Ask orit if you have any questions*/
        this.component = _stateParams.component;
        this.EventListenerService.registerObserverCallback(EVENTS.ON_CHECKOUT, this.onCheckout);
        this.updateViewMode();
    }

    ngOnInit() {
        console.log("==>" + this.constructor.name + ": ngOnInit");
        this.loadingInputs = true;
        this.loadingInstances = true;
        this.loadingProperties = true;
        this.componentServiceNg2
            .getComponentInputs(this.component)
            .subscribe(response => {
                _.forEach(response.inputs, (input: InputBEModel) => {
                    this.inputs.push(new InputFEModel(input));
                });
                this.loadingInputs = false;

            });
        this.componentServiceNg2
            .getComponentResourceInstances(this.component)
            .subscribe(response => {
                this.instances = response.componentInstances;

                _.forEach(this.instances, (instance) => {
                    this.instancesNavigationData.push(instance);
                });
                this.loadingInstances = false;
                if (this.instancesNavigationData[0] == undefined) {
                    this.loadingProperties = false;
                }
                this.selectFirstInstanceByDefault();
            });
    };

    ngOnDestroy() {
        this.EventListenerService.unRegisterObserver(EVENTS.ON_CHECKOUT);
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


    onInstanceSelectedUpdate = (resourceInstance: ResourceInstance) => {
        console.log("==>" + this.constructor.name + ": onInstanceSelectedUpdate");
        let instanceBePropertiesMap: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        this.selectedInstanceData = resourceInstance;
        this.selectedInstanceType = resourceInstance.originType;

        this.loadingProperties = true;
        if(resourceInstance.originType === ResourceType.VF) {
            this.componentInstanceServiceNg2
                .getComponentInstanceInputs(this.component, resourceInstance)
                .subscribe(response => {
                    instanceBePropertiesMap[resourceInstance.uniqueId] = response;
                    this.processInstancePropertiesResponse(instanceBePropertiesMap);
                    this.loadingProperties = false;

                });
        } else {
            this.componentInstanceServiceNg2
                .getComponentInstanceProperties(this.component, resourceInstance.uniqueId)
                .subscribe(response => {
                    instanceBePropertiesMap[resourceInstance.uniqueId] = response;
                    this.processInstancePropertiesResponse(instanceBePropertiesMap);
                    this.loadingProperties = false;
                });
        }

        if( this.searchPropertyName ){
            this.clearSearch();
        }
        //clear selected property from the navigation
        this.selectedFlatProperty = new SimpleFlatProperty();
        this.propertiesNavigationData = [];
    };

    /**
     * Entry point handling response from server
     */
    processInstancePropertiesResponse = (instanceBePropertiesMap:InstanceBePropertiesMap) => {
        this.instanceFePropertiesMap = this.propertiesUtils.convertPropertiesMapToFEAndCreateChildren(instanceBePropertiesMap, this.inputs); //create flattened children, disable declared props, and init values
        this.checkedPropertiesCount = 0;
    };


    /*** VALUE CHANGE EVENTS ***/
    propertyValueChanged = (event: PropertyFEModel) => {
        console.log("==>" + this.constructor.name + ": propertyValueChanged " + event);
        // Copying the actual value from the object ref into the value if it's from a complex type
        event.value = event.getJSONValue();

        if (this.selectedInstanceData.originType === ResourceType.VF) {
            console.log("I want to update input value on the resource instance");
            let inputToUpdate = new PropertyBEModel(event);
            this.componentInstanceServiceNg2
                .updateInstanceInput(this.component, this.selectedInstanceData.uniqueId, inputToUpdate)
                .subscribe(response => {
                    console.log("update resource instance input and got this response: ", response);
                })
        }
        else {
            let propertyBe = new PropertyBEModel(event);
            this.componentInstanceServiceNg2
                .updateInstanceProperty(this.component, this.selectedInstanceData.uniqueId, propertyBe)
                .subscribe(response => {
                    console.log("updated resource instance property and got this response: ", response);
                });
            console.log(event);
        }

    };

    inputValueChanged = (event) => {
        console.log("==>" + this.constructor.name + ": inputValueChanged");
        let inputToUpdate = new PropertyBEModel(event);

        this.componentServiceNg2
            .updateComponentInput(this.component, inputToUpdate)
            .subscribe(response => {
                console.log("updated the component input and got this response: ", response);
            })
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
        if(this.selectedInstanceData.originType !== ResourceType.VF) {
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
        this.renderer.invokeElementMethod(this.hierarchyNavTabs, 'triggerTabChange', ['Property Structure']);
    };


    selectInstanceRow = ($event) => {//get instance name
        this.selectedInstanceData =  _.find(this.instancesNavigationData, (instance:ComponentInstance) => {
            return instance.name == $event;
        });
        this.renderer.invokeElementMethod(
            this.hierarchyNavTabs, 'triggerTabChange', ['Composition']);
    };

    tabChanged = (event) => {
        console.log("==>" + this.constructor.name + ": tabChanged " + event);
        this.isInpusTabSelected = event.title === "Inputs";
        this.propertyStructureHeader = null;
        this.searchQuery = '';
    };



    /*** DECLARE PROPERTIES/INPUTS ***/
    declareProperties = (): void => {
        console.log("==>" + this.constructor.name + ": declareProperties");

        let selectedProperties: InstanceBePropertiesMap = new InstanceBePropertiesMap();

        let instancesNames = new KeysPipe().transform(this.instanceFePropertiesMap, []);
        angular.forEach(instancesNames, (instanceName: string): void => {
            selectedProperties[instanceName] = this.propertiesService.getCheckedProperties(this.instanceFePropertiesMap[instanceName]);
            //selectedProperties[this.selectedInstanceData.uniqueId] = this.propertiesService.getCheckedProperties(this.properties);
        });

        let inputsToCreate: InstancePropertiesAPIMap;
        if (this.selectedInstanceType !== ResourceType.VF) {
            inputsToCreate = new InstancePropertiesAPIMap(null, selectedProperties);
        } else {
            inputsToCreate = new InstancePropertiesAPIMap(selectedProperties, null);
        }
        this.componentServiceNg2
            .createInput(this.component, inputsToCreate)
            .subscribe(response => {
                this.setInputTabIndication(response.length);
                this.checkedPropertiesCount = 0;
                _.forEach(response, (input: InputBEModel) => {
                    this.inputs.push(new InputFEModel(input));
                    this.updatePropertyValueAfterDeclare(input);
                });
            });
    };

    updatePropertyValueAfterDeclare = (input: InputBEModel) => {
        _.forEach(input.properties, (property: ComponentInstanceProperty) => {
            this.updatePropertyOrInputValueAfterDeclare(property, input);
        });

        _.forEach(input.inputs, (inputInstance: ComponentInstanceInput) => {
            this.updatePropertyOrInputValueAfterDeclare(inputInstance, input);
        });
    }

    updatePropertyOrInputValueAfterDeclare = (inputSource: ComponentInstanceProperty | ComponentInstanceInput, input: InputBEModel) => {
        if (this.instanceFePropertiesMap[inputSource.componentInstanceId]) {
            let propertyForUpdatindVal = _.find(this.instanceFePropertiesMap[inputSource.componentInstanceId], (feProperty: PropertyFEModel) => {
                return feProperty.name == inputSource.name;
            });

            if (input.inputPath == propertyForUpdatindVal.name) input.inputPath = null; //Fix - if inputPath is sent for parent props, remove it

            propertyForUpdatindVal.setAsDeclared(input.inputPath); //set prop as declared before assigning value
            this.propertiesService.disableRelatedProperties(propertyForUpdatindVal, input.inputPath);
            this.propertiesUtils.resetPropertyValue(propertyForUpdatindVal, inputSource.value, input.inputPath);
            // if (input.inputPath) {
            //     let childProp = _.find(propertyForUpdatindVal.flattenedChildren, (child: DerivedFEProperty) => {
            //         return child.propertiesName == input.inputPath;
            //     });
            //     this.propertiesUtils.assignFlattenedChildrenValues(JSON.parse(inputSource.value), [childProp], inputSource.name);
            // } else {
            //     propertyForUpdatindVal.valueObj = inputSource.value;
            // }
        }
    }

    //used for declare button, to keep count of newly checked properties (and ignore declared properties)
    updateCheckedPropertyCount = (increment: boolean): void => {
        this.checkedPropertiesCount += (increment) ? 1 : -1;
        console.log("CheckedProperties count is now.... " + this.checkedPropertiesCount);
    };

    setInputTabIndication = (numInputs: number): void => {
        this.renderer.invokeElementMethod(this.propertyInputTabs, 'setTabIndication', ['Inputs', numInputs]);
    };

    deleteInput = (input: InputFEModel) => {
        console.log("==>" + this.constructor.name + ": deleteInput");
        let inputToDelete = new PropertyBEModel(input);

        this.componentServiceNg2
            .deleteInput(this.component, inputToDelete)
            .subscribe(response => {
                this.inputs = this.inputs.filter(input => input.uniqueId !== response.uniqueId);

                //Reload the whole instance for now - TODO: CHANGE THIS after the BE starts returning properties within the response, use commented code below instead!
                this.onInstanceSelectedUpdate(this.selectedInstanceData);
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
            });
    };

    getInstanceUniqueId = (instanceName: string): string => {
        let wantedInstance: ComponentInstance = this.instances.find((instance) => {
            return instance.normalizedName === instanceName;
        });

        return wantedInstance.uniqueId;
    };



    /*** SEARCH RELATED FUNCTIONS ***/
    searchPropertiesInstances = (filterData:FilterPropertiesAssignmentData) => {
        let instanceBePropertiesMap:InstanceBePropertiesMap;
        this.componentServiceNg2
            .filterComponentInstanceProperties(this.component, filterData)
            .subscribe(response => {

                this.processInstancePropertiesResponse(response);
                this.hierarchyPropertiesDisplayOptions.searchText = filterData.propertyName;//mark results in tree
                this.searchPropertyName = filterData.propertyName;//mark in table
                this.renderer.invokeElementMethod(this.hierarchyNavTabs, 'triggerTabChange', ['Composition']);
                this.propertiesNavigationData = [];
                this.displayClearSearch = true;
            });

    };

    clearSearch = () => {
        this.instancesNavigationData = this.instances;
        this.searchPropertyName = "";
        this.hierarchyPropertiesDisplayOptions.searchText = "";
        this.displayClearSearch = false;
        this.advanceSearch.clearAll();
    };

    clickOnClearSearch = () => {
        this.clearSearch();
        this.selectFirstInstanceByDefault();
        this.renderer.invokeElementMethod(
            this.hierarchyNavTabs, 'triggerTabChange', ['Composition']);
    };

}
