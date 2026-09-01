/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

'use strict';
import {Inject, Injectable} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {DataTypePropertyModel} from "../../models/data-type-properties";
import {IAppConfigurtaion} from '../../models/app-config';
import {ComponentInstance} from '../../models/componentsInstances/componentInstance';
import {DataTypeModel} from '../../models/data-types';
import {DataTypesMap} from '../../models/data-types-map';
import {IFileDownload} from '../../models/file-download';
import {InputPropertyBase} from '../../models/input-property-base';
import {InputModel} from '../../models/inputs';
import {PropertyModel} from '../../models/properties';
import {SchemaProperty} from '../../models/schema-property';
import {SdcConfigToken} from "../config/sdc-config.config";
import {PROPERTY_DATA} from "../../utils/constants";
import {List} from "lodash";
import {Observable} from "rxjs/Observable";

export interface IDataTypesService {

    dataTypes:DataTypesMap; //Data type map
    selectedPropertiesName:string;
    selectedInput:PropertyModel;
    alreadySelectedProperties:Array<InputPropertyBase>;
    selectedInstance:ComponentInstance;
    selectedComponentInputs:Array<InputModel>;
    //declare methods
    loadDataTypesCache(modelName:string):Promise<void>;
    findAllDataTypesByModel(modelName: string): void;
    getAllDataTypes():DataTypesMap;
    getFirsLevelOfDataTypeProperties(dataTypeName:string):Array<DataTypePropertyModel>;
    isDataTypeForSchemaType(property:SchemaProperty):boolean;
    isDataTypeForPropertyType(property:PropertyModel):boolean;
    isDataTypeForDataTypePropertyType(property:DataTypePropertyModel):boolean;
}

@Injectable()
export class DataTypesService implements IDataTypesService {

    private baseUrl:string;
    // Non-enumerable so `_.cloneDeep()` never traverses it. This service is held (as an enumerable
    // field) by the Resource/Service component services, which are in turn held by the
    // Component/Resource model classes whose toJSON() deep-copies the whole graph. An HttpClient's
    // reachable object graph is large and ends up in the POST body. Keeping http off the enumerable
    // surface avoids the copy entirely (failure-catalog §SS). This used to be load-bearing against a
    // hard `ng:cpws` throw from angular.copy; lodash tolerates such graphs, so the same mistake now
    // fails silently as a bloated request body instead of a visible hang — keep the guard.
    private http:HttpClient;
    // In-flight load per model, so the many synchronous getAllDataTypesFromModel() callers on one page
    // render share a single request instead of each firing their own.
    private pendingLoads = new Map<string, Promise<void>>();

    constructor(@Inject(SdcConfigToken) sdcConfig:IAppConfigurtaion, http:HttpClient) {
        this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
        Object.defineProperty(this, 'http', {value: http, enumerable: false, writable: false, configurable: true});
    }

    // Starts as an empty map, never undefined: getAllDataTypesFromModel is a SYNCHRONOUS accessor over
    // an asynchronously-filled cache, and its callers index straight into the result. Handing them
    // `undefined` before the first /dataTypes response threw inside
    // DataTypeService.getDataTypeByModelAndTypeName and aborted the whole property conversion (SDC-4855).
    dataTypes:DataTypesMap = {} as DataTypesMap; //Data type map
    selectedPropertiesName:string;
    selectedInput:PropertyModel;
    alreadySelectedProperties:Array<InputPropertyBase>;
    selectedInstance:ComponentInstance;
    selectedComponentInputs:Array<InputModel>;

    public loadDataTypesCache = (modelName: string): Promise<void> => {
        const key = modelName || '';
        const pending = this.pendingLoads.get(key);
        if (pending) {
            return pending;
        }
        const load = this.fetchDataTypesByModel(modelName)
            .then((dataTypes: DataTypesMap) => {
                delete dataTypes['tosca.datatypes.Root'];
                this.dataTypes = dataTypes;
            })
            .catch((reason) => {
                // Leave the previous cache in place; a failed refresh must not turn a populated map
                // back into an unusable one for the synchronous accessors.
                console.error('Failed to load data types for model ' + modelName, reason);
            })
            .then(() => {
                this.pendingLoads.delete(key);
            });
        this.pendingLoads.set(key, load);
        return load;
    };

    // Resolves once the cache for this model is populated. Callers that can await (and therefore
    // avoid reading a stale map) should prefer this over getAllDataTypesFromModel.
    public getAllDataTypesFromModelAsync = (modelName: string): Promise<DataTypesMap> => {
        return this.loadDataTypesCache(modelName).then(() => this.dataTypes);
    }

    public fetchDataTypesByModel = (modelName: string): Promise<DataTypesMap> => {
        let params = new HttpParams();
        if (modelName) {
            params = params.set('model', modelName);
        }
        return this.http.get<DataTypesMap>(this.baseUrl + "dataTypes", {params}).toPromise();
    };

    public getAllDataTypesFromModel = (modelName: string): DataTypesMap => {
        this.loadDataTypesCache(modelName);
        return this.dataTypes;
    }

    public getDataTypesFromAllModelExcludePrimitives = (): Observable<Array<DataTypeModel>> => {
        return this.http.get<List<DataTypesMap>>(this.baseUrl + "allDataTypes?excludePrimitives=true")
            .map((dataTypesListOfMap: List<DataTypesMap>) => this.getDataTypesItems(dataTypesListOfMap));
    }

    private getDataTypesItems(dataTypesListOfMap: List<DataTypesMap>):Array<DataTypeModel> {
        const dataTypes = new Array<DataTypeModel>();
        (dataTypesListOfMap as Array<DataTypesMap>).forEach((dataTypesMap: DataTypesMap): void => {
            for (const dataTypeKey of Object.keys(dataTypesMap)) {
                dataTypes.push(new DataTypeModel(dataTypesMap[dataTypeKey]))
            }
        });
        return dataTypes;
    }

    public findAllDataTypesByModel = (modelName: string): Promise<Map<string, DataTypeModel>> => {
        return new Promise<Map<string, DataTypeModel>>((resolve, reject) => {
            this.fetchDataTypesByModel(modelName).then(dataTypes => {
                delete dataTypes[PROPERTY_DATA.ROOT_DATA_TYPE];
                const dataTypeMap = new Map<string, DataTypeModel>();
                for(const dataTypeKey of Object.keys(dataTypes)) {
                    dataTypeMap.set(dataTypeKey, new DataTypeModel(dataTypes[dataTypeKey]))
                }
                resolve(dataTypeMap);
            }).catch(reason => {
                reject(reason);
            });
        });
    }

    public findAllDataTypesByModelIncludingRoot = (modelName: string): Promise<Map<string, DataTypeModel>> => {
        return new Promise<Map<string, DataTypeModel>>((resolve, reject) => {
            this.fetchDataTypesByModel(modelName).then(dataTypes => {
                const dataTypeMap = new Map<string, DataTypeModel>();
                for(const dataTypeKey of Object.keys(dataTypes)) {
                    dataTypeMap.set(dataTypeKey, new DataTypeModel(dataTypes[dataTypeKey]))
                }
                resolve(dataTypeMap);
            }).catch(reason => {
                reject(reason);
            });
        });
    }

    public getAllDataTypes = ():DataTypesMap => {
        return this.dataTypes;
    };

    //if the dt derived from simple- return the first parent type, else- return null
    private getTypeForDataTypeDerivedFromSimple = (dataTypeName:string):string => {
        /////////temporary hack for tosca primitives///////////////////////
        if (!this.dataTypes[dataTypeName]) {
            return 'string';
        }
        ///////////////////////////////////////////////////////////////////
        if (this.dataTypes[dataTypeName].derivedFromName == "tosca.datatypes.Root" || this.dataTypes[dataTypeName].properties) {
            return null;
        }
        if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.dataTypes[dataTypeName].derivedFromName) > -1) {
            return this.dataTypes[dataTypeName].derivedFromName
        }
        return this.getTypeForDataTypeDerivedFromSimple(this.dataTypes[dataTypeName].derivedFromName);
    };


    //return list of data type properties and all its parents properties
    //(not include the properties of its properties, in case this data type has not primitive properties)
    public getFirsLevelOfDataTypeProperties = (dataTypeName:string):Array<DataTypePropertyModel> => {
        let properties = this.dataTypes[dataTypeName].properties || [];
        if (this.dataTypes[dataTypeName].derivedFromName != "tosca.datatypes.Root") {
            properties = this.getFirsLevelOfDataTypeProperties(this.dataTypes[dataTypeName].derivedFromName).concat(properties);
        }
        return properties;
    };

    //return false when type= data type (=not simple type) that not derived from simple type
    public isDataTypeForSchemaType = (property:SchemaProperty):boolean=> {
        property.simpleType = "";
        if (property.type && PROPERTY_DATA.TYPES.indexOf(property.type) > -1) {
            return false;
        }
        let simpleType = this.getTypeForDataTypeDerivedFromSimple(property.type);
        if (simpleType) {
            property.simpleType = simpleType;
            return false;
        }
        return true;
    };

    public isDataTypeForPropertyType = (property:PropertyModel):boolean=> {
        property.simpleType = "";
        if (property.type && PROPERTY_DATA.TYPES.indexOf(property.type) > -1) {
            return false;
        }
        let simpleType = this.getTypeForDataTypeDerivedFromSimple(property.type);
        if (simpleType) {
            property.simpleType = simpleType;
            return false;
        }
        return true;
    };


    public isDataTypeForDataTypePropertyType = (property:DataTypePropertyModel):boolean=> {
        property.simpleType = "";
        let isScalarForNFoD:boolean = property.type === 'scalar-unit.size';
        if (property.type && PROPERTY_DATA.TYPES.indexOf(property.type) > -1 || isScalarForNFoD) {
            return false;
        }
        let simpleType = this.getTypeForDataTypeDerivedFromSimple(property.type);
        if (simpleType) {
            property.simpleType = simpleType;
            return false;
        }
        return true;
    };

    public downloadDataType = (dataTypeId: string): Promise<IFileDownload> => {
        return this.http.get<IFileDownload>(this.baseUrl + "downloadDataType" + ((dataTypeId) ? '?dataTypeId=' + dataTypeId : '')).toPromise();
    }
}
