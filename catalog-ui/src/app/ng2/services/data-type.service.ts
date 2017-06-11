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
/*
    //if the dt derived from simple- return the first parent type, else- return null
    public getTypeForDataTypeDerivedFromSimple = (dataTypeName:string):string => {
        /////////temporary hack for tosca primitives///////////////////////
        if (!this.dataTypes[dataTypeName]) {
            return PROPERTY_TYPES.STRING;
        }
        ///////////////////////////////////////////////////////////////////
        if (this.dataTypes[dataTypeName].derivedFromName == PROPERTY_DATA.ROOT_DATA_TYPE || this.dataTypes[dataTypeName].properties) {
            return null;
        }
        if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.dataTypes[dataTypeName].derivedFromName) > -1) {
            return this.dataTypes[dataTypeName].derivedFromName
        }
        return this.getTypeForDataTypeDerivedFromSimple(this.dataTypes[dataTypeName].derivedFromName);
    };

    /**
     * The function returns all properties for the DataType passed in, and recurses through parent dataTypes (derivedFrom) to retrieve their properties as well
     * @param dataTypeObj
     *
    public getDataTypePropertiesRecursively(dataTypeObj: DataTypeModel): Array<PropertyBEModel> {
        let propertiesArray: Array<PropertyBEModel> = dataTypeObj.properties || [];
        if (PROPERTY_DATA.ROOT_DATA_TYPE !== dataTypeObj.derivedFromName) {
            propertiesArray = propertiesArray.concat(this.getDataTypePropertiesRecursively(dataTypeObj.derivedFrom));
        }
        return propertiesArray;
    }
*/

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

}

