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
