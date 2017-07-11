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

/**
 * Created by rcohen on 9/25/2016.
 */
'use strict';
import {SchemaPropertyGroupModel} from "./aschema-property";
import {PropertyModel} from "./properties";

export class DataTypePropertyModel extends PropertyModel{
    //custom
    simpleType:string;
    valueObjectRef:any;
    childrenProperties:Array<DataTypePropertyModel>;
    isAllChildrenLevelsCalculated:boolean;
    treeNodeId:string;
    parent:DataTypePropertyModel;
    expandedChildPropertyId:string;

    constructor(property?:PropertyModel);
    constructor(name:string, type:string, treeNodeId:string, parent:DataTypePropertyModel, valueObjectRef:any, schema?:SchemaPropertyGroupModel);
    constructor(nameOrPropertyObj?:string | PropertyModel, type?:string, treeNodeId?:string, parent?:DataTypePropertyModel, valueObjectRef?:any, schema?:SchemaPropertyGroupModel){
        super(typeof nameOrPropertyObj === "string" ? null : nameOrPropertyObj);
        if ( typeof nameOrPropertyObj === "string" ) {
            this.name = nameOrPropertyObj;
            this.type = type;
            this.treeNodeId = treeNodeId;
            this.parent = parent;
            this.valueObjectRef = valueObjectRef;
            this.schema = schema;
        }
    }

    public updateExpandedChildPropertyId = (childPropertyId:string):void =>{
        if(this.expandedChildPropertyId == childPropertyId){
            this.expandedChildPropertyId = "";
        }else{
            this.expandedChildPropertyId = childPropertyId;
        }
    }
}
