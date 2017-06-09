import { Injectable } from '@angular/core';
import { DataTypeModel, PropertyFEModel, PropertyBEModel, InstanceBePropertiesMap, InstanceFePropertiesMap, SchemaProperty, DerivedFEProperty, DerivedFEPropertyMap, DerivedPropertyType, InputFEModel} from "app/models";
import { DataTypeService } from "app/ng2/services/data-type.service";
import { PROPERTY_TYPES } from "app/utils";
import { UUID } from "angular2-uuid";

@Injectable()
export class PropertiesUtils {

    constructor(private dataTypeService:DataTypeService) {}

    /**
     * Entry point when getting properties from server
     * Returning InstanceFePropertiesMap
     */
    public convertPropertiesMapToFEAndCreateChildren = (instancePropertiesMap:InstanceBePropertiesMap): InstanceFePropertiesMap => {
        let instanceFePropertiesMap:InstanceFePropertiesMap = new InstanceFePropertiesMap();
        angular.forEach(instancePropertiesMap, (properties:Array<PropertyBEModel>, instanceName:string) => {
            instanceFePropertiesMap[instanceName] = this.convertPropertiesToFEAndCreateChildren(properties);
        });
        return instanceFePropertiesMap;
    }

    /**
     * Convert the properties Array<PropertyBEModel> to Array<PropertyFEModel>
     */
    private convertPropertiesToFEAndCreateChildren = (properties: Array<PropertyBEModel>): Array<PropertyFEModel> => {
        let propertyFeArray: Array<PropertyFEModel> = [];
        _.forEach(properties, (property: PropertyBEModel, index: number) => {
            //console.log("=======" + property.name + "========");
                if(!this.dataTypeService.getDataTypeByTypeName(property.type)){ // if type not exist in data types remove property from list
                    console.log("ERROR: missing type " + property.type + " in dataTypes , of property ",property);
                    return;
                }
                let propertyFe:PropertyFEModel = new PropertyFEModel(property);
                if (propertyFe.isDataType) { //prop is not simple, list, or map. Need to create children.
                    let tempProps: Array<DerivedFEProperty> = [];
                    let dataTypeObj: DataTypeModel = this.dataTypeService.getDataTypeByTypeName(propertyFe.type);
                    this.dataTypeService.getDerivedDataTypeProperties(dataTypeObj, tempProps, propertyFe.name);
                    propertyFe.flattenedChildren = tempProps;
                    propertyFe.expandedChildPropertyId = propertyFe.name;
                    this.initValueObjectRef(propertyFe);
                }
                propertyFeArray.push(propertyFe);


        });
        return propertyFeArray;

        //TODO: need to look at schema to create the nested properties for the following cases:
        // 1 - when value is populated for a complex type (list or map)
        // 2 - when adding new entries to a complex type (eg. adding a new entry to a list of AddressRequirements)
    }

    public initValueObjectRef = (property: PropertyFEModel): void => {
        //console.log("Property " + property.name + " has value: " + property.value);
        if (!property.isDataType || property.isDeclared) { //if property is declared, it gets a simple input instead. List and map values and pseudo-children will be handled in property component
            property.value = property.value || property.defaultValue;
        } else if (property.value){ //we have a complex property with a value. Lets parse property.value and populate our flattened children with those values
            this.assignValuesRecursively(JSON.parse(property.value), property.flattenedChildren, property.name);
        }
    }

    public assignValuesRecursively = (valueJSON: any, derivedPropArray: Array<DerivedFEProperty>, propName: string) => {
        if (valueJSON && Object.keys(valueJSON)) {
            Object.keys(valueJSON).forEach(valueKey => {
                let childProp: DerivedFEProperty = derivedPropArray.find(prop => prop.propertiesName == propName + "#" + valueKey);
                if (!childProp) return;
                if (childProp.isDeclared || (childProp.derivedDataType != DerivedPropertyType.COMPLEX && !_.isEmpty(valueJSON[valueKey]))) {
                    childProp.value = (typeof valueJSON[valueKey] === 'object')? JSON.stringify(valueJSON[valueKey]) : valueJSON[valueKey];
                } else {
                    this.assignValuesRecursively(valueJSON[valueKey], derivedPropArray, childProp.propertiesName)
                }
            });
        }
    }

}
