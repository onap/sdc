import {Component, ViewChild, ElementRef, Renderer, Inject} from "@angular/core";
import {PostsService} from "../../services/posts.service";
import {PropertiesService, SimpleFlatProperty} from "../../services/properties.service";
import { PropertiesUtils } from './properties.utils';
import { PropertyFEModel, InstanceFePropertiesMap, InstanceBePropertiesMap, InstancePropertiesAPIMap, Component as ComponentData, FilterPropertiesAssignmentData } from "app/models";
import { PROPERTY_TYPES, ResourceType } from "app/utils";
import property = require("lodash/property");
import {ComponentServiceNg2} from "../../services/component-services/component.service";
import {ComponentInstanceServiceNg2} from "../../services/component-instance-services/component-instance.service"
import {InputFEModel, ComponentInstance, PropertyBEModel, DerivedFEProperty, ResourceInstance} from "app/models";
import {HierarchyDisplayOptions} from "../../components/hierarchy-navigtion/hierarchy-display-options"
import {PropertyRowSelectedEvent} from "./../../components/properties-table/properties-table.component";
import { KeysPipe } from 'app/ng2/pipes/keys.pipe';
import {FilterPropertiesAssignmentComponent} from "../../components/filter-properties-assignment/filter-properties-assignment.component";

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
    propertyStructureHeader: string

    selectedFlatProperty: SimpleFlatProperty = new SimpleFlatProperty();
    selectedInstanceType: string;
    selectedInstanceData: ComponentInstance = new ComponentInstance();
    checkedPropertiesCount: number = 0;

    hierarchyPropertiesDisplayOptions:HierarchyDisplayOptions = new HierarchyDisplayOptions('uniqueId', 'name', 'childrens');
    hierarchyInstancesDisplayOptions:HierarchyDisplayOptions = new HierarchyDisplayOptions('uniqueId', 'name');
    displayClearSearch = false;
    searchPropertyName:string;
    hideAdvanceSearch:boolean;

    @ViewChild('hierarchyNavTabs') hierarchyNavTabs: ElementRef;
    @ViewChild('propertyInputTabs') propertyInputTabs: ElementRef;
    @ViewChild('advanceSearch') advanceSearch: FilterPropertiesAssignmentComponent;

    constructor(private propertiesService:PropertiesService,
                private propertiesUtils:PropertiesUtils,
                private componentServiceNg2:ComponentServiceNg2,
                private componentInstanceServiceNg2:ComponentInstanceServiceNg2,
                @Inject("$stateParams") _stateParams,
                private renderer: Renderer) {

        this.instanceFePropertiesMap = new InstanceFePropertiesMap();

        /* This is the way you can access the component data, please do not use any data except metadata, all other data should be received from the new api calls on the first time
        than if the data is already exist, no need to call the api again - Ask orit if you have any questions*/
        this.component = _stateParams.component;
    }

    ngOnInit() {
        console.log("==>" + this.constructor.name + ": ngOnInit");
        this.componentServiceNg2
            .getComponentResourceInstances(this.component)
            .subscribe(response => {
                this.instances = response.componentInstances;

                _.forEach(this.instances, (instance) => {
                    this.instancesNavigationData.push(instance);
                });

                this.selectFirstInstanceByDefault();
            });

        this.componentServiceNg2
            .getComponentInputs(this.component)
            .subscribe(response => {
                _.forEach(response.inputs, (input: PropertyBEModel) => {
                    this.inputs.push(new InputFEModel(input));
                });
            })
    }

    selectFirstInstanceByDefault = () => {
        if (this.instancesNavigationData[0] !== undefined) {
            this.onInstanceSelectedUpdate(this.instancesNavigationData[0]);
        }
    }

    propertyValueChanged = (event) => {
        console.log("==>" + this.constructor.name + ": propertyValueChanged " + event);

        if(this.selectedInstanceData.originType === ResourceType.VF) {
            console.log("I want to update input value on the resource instance");
            let inputToUpdate = new PropertyBEModel(event);
            this.componentInstanceServiceNg2
                .updateInstanceInput(this.component, this.selectedInstanceData.uniqueId, inputToUpdate)
                .subscribe(response => {
                    console.log("update resource instance input and got this response: ",response);
                })
        }
        else {
            // Copying the actual value from the object ref into the value if it's from a complex type
            if(event.isDataType) {
                event.value = JSON.stringify(event.valueObjectRef);
            }
            let propertyBe = new PropertyBEModel(event);
            this.componentInstanceServiceNg2
                .updateInstanceProperty(this.component, this.selectedInstanceData.uniqueId, propertyBe)
                .subscribe(response => {
                    console.log("updated resource instance property and got this response: ",response);
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

    declareProperties = ():void => {
        console.log("==>" + this.constructor.name + ": declareProperties");

        let selectedProperties: InstanceBePropertiesMap = new InstanceBePropertiesMap();

        let instancesNames = new KeysPipe().transform(this.instanceFePropertiesMap,[]);
        angular.forEach(instancesNames, (instanceName:string):void=>{
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
                _.forEach(response, (input: PropertyBEModel) => { this.inputs.push(new InputFEModel(input)); });
                this.findAndDisableDeclaredProperties();
            });
    }

    //TODO: Can remove? no one use it
    // getSelectedFEProps = (): Array<PropertyFEModel> => {
    //     return this.properties.filter(prop => prop.isSelected && !prop.isDeclared);
    // }

    onInstanceSelectedUpdate = (resourceInstance: ResourceInstance) => {
        console.log("==>" + this.constructor.name + ": onInstanceSelectedUpdate");
        let instanceBePropertiesMap: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        this.selectedInstanceData = resourceInstance;
        this.selectedInstanceType = resourceInstance.originType;

        if(resourceInstance.originType === ResourceType.VF) {
            this.componentInstanceServiceNg2
                .getComponentInstanceInputs(this.component, resourceInstance)
                .subscribe(response => {
                    instanceBePropertiesMap[resourceInstance.uniqueId] = response;
                    this.processInstancePropertiesResponse(instanceBePropertiesMap);
                });
        } else {
            this.componentInstanceServiceNg2
                .getComponentInstanceProperties(this.component, resourceInstance.uniqueId)
                .subscribe(response => {
                    instanceBePropertiesMap[resourceInstance.uniqueId] = response;
                    this.processInstancePropertiesResponse(instanceBePropertiesMap);
                });
        }

        if( this.searchPropertyName ){
            this.clearSearch();
        }
    };

    /**
     * Entry point handling response from server
     */
    processInstancePropertiesResponse = (instanceBePropertiesMap:InstanceBePropertiesMap) => {
        this.instanceFePropertiesMap = this.propertiesUtils.convertPropertiesMapToFEAndCreateChildren(instanceBePropertiesMap); //create flattened children
        this.findAndDisableDeclaredProperties(); //disable properties or flattened children that are declared
        this.checkedPropertiesCount = 0;
    };

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
                simpleFlatProperty = this.propertiesService.getSimplePropertiesTree(property, instanceName);
            } else if (property instanceof DerivedFEProperty) {
                // Need to find parent PropertyFEModel
                let parentPropertyFEModel:PropertyFEModel = _.find(this.instanceFePropertiesMap[instanceName], (tmpFeProperty):boolean => {
                    return property.propertiesName.indexOf(tmpFeProperty.name)===0;
                });
                simpleFlatProperty = this.propertiesService.getSimplePropertiesTree(parentPropertyFEModel, instanceName);
            }
            this.propertiesNavigationData = simpleFlatProperty;
        }

        // Updatet the header in the navigation tree with property name.
        if(property instanceof DerivedFEProperty) {
            this.propertyStructureHeader = (property.propertiesName.split('#'))[0];
        }

        // Set selected property in table
        this.selectedFlatProperty = new SimpleFlatProperty(property.uniqueId, null, property.name, null);
        this.renderer.invokeElementMethod(this.hierarchyNavTabs, 'triggerTabChange', ['Property Structure']);
    };

    //TODO: Can remove? no one use it
    // findParentProperty = (childProp: DerivedFEProperty): PropertyFEModel => {
    //     return this.properties.find(prop => prop.name == childProp.propertiesName.substring(0, childProp.propertiesName.indexOf("#")));
    // }

    //used for declare button, to keep count of newly checked properties (and ignore declared properties)
    updateCheckedPropertyCount = (increment: boolean):void => {
        this.checkedPropertiesCount += (increment) ? 1 : -1;
        console.log("CheckedProperties count is now.... " + this.checkedPropertiesCount);
    }

    selectInstanceRow = ($event) => {//get instance name
        this.selectedInstanceData =  _.find(this.instancesNavigationData, (instance:ComponentInstance) => {
            return instance.name == $event;
        });
        this.renderer.invokeElementMethod(
            this.hierarchyNavTabs, 'triggerTabChange', ['Composition']);
    }

    tabChanged = (event) => {
        console.log("==>" + this.constructor.name + ": tabChanged " + event);
        this.hideAdvanceSearch = event.title !== "Properties";
        this.searchQuery = '';
    };

    deleteInput = (input:InputFEModel) => {
        console.log("==>" + this.constructor.name + ": deleteInput");
        let inputToDelete = new PropertyBEModel(input);

        this.componentServiceNg2
            .deleteInput(this.component, inputToDelete)
            .subscribe(response => {
                this.inputs = this.inputs.filter(input => input.uniqueId !== response.uniqueId);
                let propToEnable: PropertyFEModel = this.instanceFePropertiesMap[input.instanceName].find(prop => prop.name == input.propertyName);
                propToEnable.setNonDeclared(response.inputPath);
                this.propertiesService.undoDisableRelatedProperties(propToEnable, response.inputPath);
                //this.propertiesService.initValueObjectRef(propToEnable); //TODO:speak to BE about value returned by server
            });
    }

    setInputTabIndication = (numInputs: number): void => {
        this.renderer.invokeElementMethod( this.propertyInputTabs, 'setTabIndication', ['Inputs', numInputs]);
    }

    findAndDisableDeclaredProperties = () => {
        this.inputs.filter(input => input.instanceName === this.selectedInstanceData.normalizedName).forEach(input => {
            let prop: PropertyFEModel = this.instanceFePropertiesMap[this.selectedInstanceData.uniqueId].find(prop => prop.name === input.propertyName);
            if (prop) {
                prop.setAsDeclared(input.inputPath); //if a path was sent, its a child prop. this param is optional
                this.propertiesService.disableRelatedProperties(prop, input.inputPath)
                //this.propertiesService.initValueObjectRef(prop);
            }
        });
    };

    searchPropertiesInstances = (filterData:FilterPropertiesAssignmentData) => {
        //let filteredProperties = this.componentServiceNg2.filterComponentInstanceProperties(this.component, filterData);
        let instanceBePropertiesMap:InstanceBePropertiesMap;
        this.componentServiceNg2
            .filterComponentInstanceProperties(this.component, filterData)
            .subscribe(response => {
                //instanceBePropertiesMap=response;
                //console.log("================filter results=============");
                //console.table(instanceBePropertiesMap);
                this.processInstancePropertiesResponse(response);


                //this.properties = [];
                // _.forEach(instanceBePropertiesMap, (InstanceProperties:Array<PropertyBEModel>, instanceName:string) => {
                //     this.properties = this.properties.concat(this.propertiesService.convertPropertiesToFEAndCreateChildren(InstanceProperties, instanceName));
                // });


                // this.instancesNavigationData = _.filter(this.instancesNavigationData, (instance:ComponentInstance) => {
                //     return instanceBePropertiesMap[instance.name];
                // });

                // this.hierarchyPropertiesDisplayOptions.searchText = filterData.propertyName;//mark results in tree
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
    }

    clickOnClearSearch = () => {
        this.clearSearch();
        this.selectFirstInstanceByDefault();
        this.renderer.invokeElementMethod(
            this.hierarchyNavTabs, 'triggerTabChange', ['Composition']);
    }

}
