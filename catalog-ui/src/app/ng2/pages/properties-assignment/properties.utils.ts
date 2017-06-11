import { Injectable } from '@angular/core';
import { DataTypeModel, PropertyFEModel, PropertyBEModel, InstanceBePropertiesMap, InstanceFePropertiesMap, SchemaProperty, DerivedFEProperty, DerivedFEPropertyMap, DerivedPropertyType, InputFEModel} from "app/models";
import { DataTypeService } from "app/ng2/services/data-type.service";
import { PropertiesService } from "app/ng2/services/properties.service";
import { PROPERTY_TYPES } from "app/utils";
import { UUID } from "angular2-uuid";

@Injectable()
export class PropertiesUtils {

    constructor(private dataTypeService:DataTypeService, private propertiesService: PropertiesService) {}

    /**
     * Entry point when getting properties from server
     * For each instance, loop through each property, and:
     * 1. Create flattened children
     * 2. Check against inputs to see if any props are declared and disable them
     * 3. Initialize valueObj (which also creates any new list/map flattened children as needed)
     * Returns InstanceFePropertiesMap
     */
    public convertPropertiesMapToFEAndCreateChildren = (instancePropertiesMap:InstanceBePropertiesMap, inputs:Array<InputFEModel>): InstanceFePropertiesMap => {
        let instanceFePropertiesMap:InstanceFePropertiesMap = new InstanceFePropertiesMap();
        angular.forEach(instancePropertiesMap, (properties:Array<PropertyBEModel>, instanceName:string) => {
            let instanceInputs: Array<InputFEModel> = inputs.filter(input => input.instanceName == instanceName.split('.').pop());
            let propertyFeArray: Array<PropertyFEModel> = [];
            _.forEach(properties, (property: PropertyBEModel) => {

                if (!this.dataTypeService.getDataTypeByTypeName(property.type)) { // if type not exist in data types remove property from list
                    console.log("ERROR: missing type " + property.type + " in dataTypes , of property ", property);
                } else {

                    let newFEProp: PropertyFEModel = new PropertyFEModel(property); //Convert property to FE

                    if (newFEProp.derivedDataType == DerivedPropertyType.COMPLEX) { //Create children if prop is not simple, list, or map.
                        newFEProp.flattenedChildren = this.createFlattenedChildren(newFEProp.type, newFEProp.name);
                    }
                    if (instanceInputs.length) { //if this prop (or any children) are declared, set isDeclared and disable checkbox on parents/children
                        instanceInputs.filter(input => input.propertyName == newFEProp.name).forEach((input) => {
                            newFEProp.setAsDeclared(input.inputPath); //if a path was sent, its a child prop. this param is optional
                            this.propertiesService.disableRelatedProperties(newFEProp, input.inputPath);
                        });
                    }
                    this.initValueObjectRef(newFEProp); //initialize valueObj.
                    propertyFeArray.push(newFEProp);
                    newFEProp.updateExpandedChildPropertyId(newFEProp.name); //display only the first level of children
                }    
            });
            instanceFePropertiesMap[instanceName] = propertyFeArray;

        });
        return instanceFePropertiesMap;
    }
    private createListOrMapChildrenFromValueObj = (property: PropertyFEModel) => {
        if ((property.derivedDataType == DerivedPropertyType.LIST || property.derivedDataType == DerivedPropertyType.MAP)
            && Object.keys(property.valueObj).length) {

            Object.keys(property.valueObj).forEach((key) => {
                let newProps: Array<DerivedFEProperty> = this.createListOrMapChildren(property, key, property.valueObj[key]);
                property.flattenedChildren.push(...newProps);
            });
            
        }
    }

    public createListOrMapChildren = (property:PropertyBEModel, key: string, valueObj: any): Array<DerivedFEProperty> => {
        let newProps: Array<DerivedFEProperty> = [];
        let parentProp = new DerivedFEProperty(property, property.propertiesName, true, key, valueObj);
        newProps.push(parentProp);

        if (!property.schema.property.isSimpleType) {
            let additionalChildren:Array<DerivedFEProperty> = this.createFlattenedChildren(property.schema.property.type, parentProp.propertiesName);
            this.assignFlattenedChildrenValues(parentProp.valueObj, additionalChildren, parentProp.propertiesName);
            additionalChildren.forEach(prop => prop.canBeDeclared = false);
            newProps.push(...additionalChildren);
        }
        return newProps;
    }

    /**
     * Creates derivedFEProperties of a specified type and returns them.
     */
    private createFlattenedChildren = (type: string, parentName: string):Array<DerivedFEProperty> => {
        let tempProps: Array<DerivedFEProperty> = [];
        let dataTypeObj: DataTypeModel = this.dataTypeService.getDataTypeByTypeName(type);
        this.dataTypeService.getDerivedDataTypeProperties(dataTypeObj, tempProps, parentName);
        return tempProps;
    }
    
    /* Sets the valueObj of parent property and its children. 
    * Note: This logic is different than assignflattenedchildrenvalues - here we merge values, there we pick either the parents value, props value, or default value - without merging.
    */
    public initValueObjectRef = (property: PropertyFEModel): void => {
        if (property.derivedDataType == DerivedPropertyType.SIMPLE || property.isDeclared) { //if property is declared, it gets a simple input instead. List and map values and pseudo-children will be handled in property component
            property.valueObj = property.value || property.defaultValue;

            if (property.isDeclared && typeof property.valueObj == 'object')  property.valueObj = JSON.stringify(property.valueObj);
        } else {
            if (property.derivedDataType == DerivedPropertyType.LIST) {
                property.valueObj = _.merge([], JSON.parse(property.defaultValue || '[]'), JSON.parse(property.value || '[]')); //value object should be merged value and default value. Value takes higher precendence. Set valueObj to empty obj if undefined.
            } else {
                property.valueObj = _.merge({}, JSON.parse(property.defaultValue || '{}'), JSON.parse(property.value || '{}')); //value object should be merged value and default value. Value takes higher precendence. Set valueObj to empty obj if undefined.
            }
            if (property.derivedDataType == DerivedPropertyType.COMPLEX) {
                this.assignFlattenedChildrenValues(property.valueObj, property.flattenedChildren, property.name);
            } else {
                this.createListOrMapChildrenFromValueObj(property);
            }
        }
    }

    /*
    * Loops through flattened properties array and to assign values
    * Then, convert any neccessary strings to objects, and vis-versa
    * For list or map property, creates new children props if valueObj has values
    */
    public assignFlattenedChildrenValues = (parentValueJSON: any, derivedPropArray: Array<DerivedFEProperty>, parentName: string) => {
        if (!derivedPropArray || !parentName) return;
        derivedPropArray.forEach((prop, index) => {

            let propNameInObj = prop.propertiesName.substring(prop.propertiesName.indexOf(parentName) + parentName.length + 1).split('#').join('.'); //extract everything after parent name
            prop.valueObj = _.get(parentValueJSON, propNameInObj, prop.value || prop.defaultValue); //assign value -first value of parent if exists. If not, prop.value if not, prop.defaultvalue
            
            if ((prop.derivedDataType == DerivedPropertyType.SIMPLE || prop.isDeclared) && typeof prop.valueObj == 'object') { //Stringify objects that should be strings
                prop.valueObj = JSON.stringify(prop.valueObj);
            } else { //parse strings that should be objects
                if ((prop.derivedDataType == DerivedPropertyType.COMPLEX || prop.derivedDataType == DerivedPropertyType.MAP) && typeof prop.valueObj != 'object') {
                    prop.valueObj = JSON.parse(prop.valueObj || '{}');
                } else if (prop.derivedDataType == DerivedPropertyType.LIST && typeof prop.valueObj != 'object') {
                    prop.valueObj = JSON.parse(prop.valueObj || '[]');
                }
                if ((prop.derivedDataType == DerivedPropertyType.LIST || prop.derivedDataType == DerivedPropertyType.MAP) && Object.keys(prop.valueObj).length) {
                    let newProps: Array<DerivedFEProperty> = [];
                    Object.keys(prop.valueObj).forEach((key) => {
                        newProps.push(...this.createListOrMapChildren(prop, key, prop.valueObj[key]));//create new children, assign their values, and then add to array
                    });
                    derivedPropArray.splice(index + 1, 0, ...newProps);
                }
            }
        });
    }

    public resetPropertyValue = (property: PropertyFEModel, newValue: string, inputPath?: string): void => {
        property.value = newValue;
        if (inputPath) {
            let newProp = property.flattenedChildren.find(prop => prop.propertiesName == inputPath);
            newProp && this.assignFlattenedChildrenValues(JSON.parse(newValue), [newProp], property.name);
        } else {
            this.initValueObjectRef(property);
        }
    }



}
