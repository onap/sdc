import { Injectable } from '@angular/core';
import { DataTypeModel, DataTypesMap, PropertyBEModel, PropertyFEModel, DerivedFEProperty, DerivedFEPropertyMap } from "app/models";
import { DataTypesService } from "app/services/data-types-service";
import { PROPERTY_DATA, PROPERTY_TYPES } from "app/utils";

/** This is a new service for NG2, to eventually replace app/services/data-types-service.ts
 *
 *  This service is a singleton that holds a map of all DataTypes, recieved from server on load.
 *  It also contains convenience methods to check if a string is a valid dataType, and to retrieve a dataType's properties recursively
 */

@Injectable()
export class DataTypeService {
    private dataTypes: DataTypesMap;

    constructor(private dataTypeService: DataTypesService) {
        this.dataTypes = dataTypeService.getAllDataTypes(); //This should eventually be replaced by an NG2 call to the backend instead of utilizing Angular1 downgraded component.
    }

    public getDataTypeByTypeName(typeName: string): DataTypeModel {
        return this.dataTypes[typeName];
    }


    public getDerivedDataTypeProperties(dataTypeObj: DataTypeModel, propertiesArray: Array<DerivedFEProperty>, parentName: string) {
        //push all child properties to array
        if (dataTypeObj.properties) {
            dataTypeObj.properties.forEach((derivedProperty) => {
                if(dataTypeObj.name !== PROPERTY_DATA.OPENECOMP_ROOT || derivedProperty.name !== PROPERTY_DATA.SUPPLEMENTAL_DATA){//The requirement is to not display the property supplemental_data
                    propertiesArray.push(new DerivedFEProperty(derivedProperty, parentName));
                }
                let derivedDataTypeObj: DataTypeModel = this.getDataTypeByTypeName(derivedProperty.type);
                this.getDerivedDataTypeProperties(derivedDataTypeObj, propertiesArray, parentName + "#" + derivedProperty.name);
            });
        }
        //recurse parent (derivedFrom), in case one of parents contains properties
        if (PROPERTY_DATA.ROOT_DATA_TYPE !== dataTypeObj.derivedFrom.name) {
            this.getDerivedDataTypeProperties(dataTypeObj.derivedFrom, propertiesArray, parentName);
        }
    }

    /**
     * Checks for custom behavior for a given data type by checking if a function exists within data-type.service with that name
     * Additional custom behavior can be added by adding a function with the given dataType name
     */    
    public checkForCustomBehavior = (property:PropertyFEModel) => {
        let shortTypeName:string = property.type.split('.').pop();
        if (this[shortTypeName]) {
            this[shortTypeName](property); //execute function for given type, pass property as param
        }
    }

    public Naming = (property: PropertyFEModel) => {
        let generatedNamingVal: boolean = _.get(property.valueObj, 'ecomp_generated_naming', true);
        property.flattenedChildren.forEach((prop) => {
            if (prop.name == 'naming_policy') prop.hidden = !generatedNamingVal;
            if (prop.name == 'instance_name') prop.hidden = generatedNamingVal;
        });
    }

}

