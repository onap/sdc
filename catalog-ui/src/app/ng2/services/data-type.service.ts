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
import {Inject, Injectable} from '@angular/core';
import {DataTypeModel, DataTypesMap, PropertyFEModel, DerivedFEProperty, PropertyBEModel} from "app/models";
import { DataTypesService } from "app/services/data-types-service";
import { PROPERTY_DATA } from "app/utils";
import {DerivedFEAttribute} from "../../models/attributes-outputs/derived-fe-attribute";
import {ISdcConfig} from "../config/sdc-config.config.factory";
import {SdcConfigToken} from "../config/sdc-config.config";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";

/** This is a new service for NG2, to eventually replace app/services/data-types-service.ts
 *
 *  This service is a singleton that holds a map of all DataTypes, recieved from server on load.
 *  It also contains convenience methods to check if a string is a valid dataType, and to retrieve a dataType's properties recursively
 */

@Injectable()
export class DataTypeService {
    public dataTypes: DataTypesMap;
    private readonly baseUrl: string;
    private readonly dataTypeUrl: string;

    constructor(private dataTypeService: DataTypesService, private httpClient: HttpClient, @Inject(SdcConfigToken) sdcConfig: ISdcConfig) {
        this.dataTypes = dataTypeService.getAllDataTypes(); //This should eventually be replaced by an NG2 call to the backend instead of utilizing Angular1 downgraded component.
        this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
        this.dataTypeUrl = `${this.baseUrl}data-types`
    }


    public getDataTypeByModelAndTypeName(modelName: string, typeName: string): DataTypeModel {
        this.dataTypes = this.dataTypeService.getAllDataTypesFromModel(modelName);
        let dataTypeFound = this.dataTypes[typeName];
        if (!dataTypeFound) {
            console.log("MISSING Datatype for model " + modelName + " and type: " + typeName);
        }
        return dataTypeFound;
    }

    public getDataTypeByTypeName(typeName: string): DataTypeModel {
        if(!this.dataTypes){
            this.dataTypes = this.dataTypeService.getAllDataTypes();
        }
        if (!this.dataTypes[typeName]) console.log("MISSING Datatype: " + typeName);
        return this.dataTypes[typeName];
    }

    public getDataTypeByModel(modelName: string): DataTypesMap {
        return this.dataTypeService.getAllDataTypesFromModel(modelName);
    }

    public findAllDataTypesByModel(modelName: string): Promise<Map<string, DataTypeModel>> {
        return this.dataTypeService.findAllDataTypesByModel(modelName);
    }

    public findById(id: string): Observable<DataTypeModel> {
        const url = `${this.dataTypeUrl}/${id}`
        return this.httpClient.get<DataTypeModel>(url);
    }

    public findAllProperties(id: string): Observable<Array<PropertyBEModel>> {
        const url = `${this.dataTypeUrl}/${id}/properties`
        return this.httpClient.get<Array<PropertyBEModel>>(url);
    }

    public getConstraintsByParentTypeAndUniqueID(rootPropertyType, propertyName){
        // const property = this.dataTypes[rootPropertyType].properties.filter(property =>
        //     property.name == propertyName);
        // return property[0] && property[0].constraints ? property[0].constraints[0].validValues : null;
        return null;
    }

    public getDerivedDataTypeProperties(dataTypeObj: DataTypeModel, propertiesArray: Array<DerivedFEProperty>, parentName: string) {
        //push all child properties to array
        if (!dataTypeObj) return;
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
        if (dataTypeObj.derivedFrom && PROPERTY_DATA.ROOT_DATA_TYPE !== dataTypeObj.derivedFrom.name) {
            this.getDerivedDataTypeProperties(dataTypeObj.derivedFrom, propertiesArray, parentName);
        }
    }

    public getDerivedDataTypeAttributes(dataTypeObj: DataTypeModel, attributesArray: Array<DerivedFEAttribute>, parentName: string) {
        //push all child properties to array
        if (!dataTypeObj) return;
        if (dataTypeObj.attributes) {
            dataTypeObj.attributes.forEach((derivedAttribute) => {
                if(dataTypeObj.name !== PROPERTY_DATA.OPENECOMP_ROOT || derivedAttribute.name !== PROPERTY_DATA.SUPPLEMENTAL_DATA){//The requirement is to not display the property supplemental_data
                    attributesArray.push(new DerivedFEAttribute(derivedAttribute, parentName));
                }
                let derivedDataTypeObj: DataTypeModel = this.getDataTypeByTypeName(derivedAttribute.type);
                this.getDerivedDataTypeAttributes(derivedDataTypeObj, attributesArray, parentName + "#" + derivedAttribute.name);
            });
        }
        //recurse parent (derivedFrom), in case one of parents contains properties
        if (dataTypeObj.derivedFrom && PROPERTY_DATA.ROOT_DATA_TYPE !== dataTypeObj.derivedFrom.name) {
            this.getDerivedDataTypeAttributes(dataTypeObj.derivedFrom, attributesArray, parentName);
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

