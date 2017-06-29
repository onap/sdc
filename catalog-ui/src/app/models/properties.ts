'use strict';
import {SchemaPropertyGroupModel, SchemaProperty} from "./aschema-property";
import {InputPropertyBase} from "./input-property-base";

export class PropertiesGroup {
    constructor(propertiesObj?:PropertiesGroup) {
        _.forEach(propertiesObj, (properties:Array<PropertyModel>, instance) => {
            this[instance] = [];
            _.forEach(properties, (property:PropertyModel):void => {
                property.resourceInstanceUniqueId = instance;
                property.readonly = true;
                this[instance].push(new PropertyModel(property));
            });
        });
    }
}

export interface IPropertyModel extends InputPropertyBase {

    //server data
    constraints:Array<Object>;
    source:string;

    //instance properties
    valueUniqueUid:string;
    path:Array<string>;
    rules:Array<Object>;
    propertiesName:string;
    input:any;

    //custom properties
    resourceInstanceUniqueId:string;
    readonly:boolean;
    simpleType:string;
}

export class PropertyModel implements IPropertyModel {

    //server data
    uniqueId:string;
    name:string;
    constraints:Array<Object>;
    defaultValue:string;
    description:string;
    password:boolean;
    required:boolean;
    type:string;
    source:string;
    parentUniqueId:string;
    schema:SchemaPropertyGroupModel;
    componentInstanceId:string;
    parentValue:string;
    ownerId:string;

    //instance properties
    value:string;
    valueUniqueUid:string;
    path:Array<string>;
    rules:Array<Object>;
    propertiesName:string;
    input:any;

    //custom properties
    resourceInstanceUniqueId:string;
    readonly:boolean;
    simpleType:string;
    filterTerm:string;
    isAlreadySelected:boolean;
    addOn:string;


    constructor(property?:PropertyModel) {
        if (property) {
            this.uniqueId = property.uniqueId;
            this.name = property.name;
            this.constraints = property.constraints;
            this.defaultValue = property.defaultValue;
            this.description = property.description;
            this.password = property.password;
            this.required = property.required;
            this.type = property.type;
            this.source = property.source;
            this.parentUniqueId = property.parentUniqueId;
            this.schema = property.schema;
            this.value = property.value ? property.value : property.defaultValue;
            this.valueUniqueUid = property.valueUniqueUid;
            this.path = property.path;
            this.rules = property.rules;
            this.resourceInstanceUniqueId = property.resourceInstanceUniqueId;
            this.readonly = property.readonly;
            this.simpleType = property.simpleType;
            this.componentInstanceId = property.componentInstanceId;
            this.parentValue = property.parentValue;
            this.ownerId = property.ownerId;
        }

        if (!this.schema || !this.schema.property) {
            this.schema = new SchemaPropertyGroupModel(new SchemaProperty());
        } else {
            //forcing creating new object, so editing different one than the object in the table
            this.schema = new SchemaPropertyGroupModel(new SchemaProperty(this.schema.property));
        }
        if (property && property.uniqueId) {
            this.filterTerm = this.name + " " + (this.description || "") + " " + this.type.replace("org.openecomp.datatypes.heat.", "");
            if (this.schema.property && this.schema.property.type) {
                this.filterTerm += " " + this.schema.property.type.replace("org.openecomp.datatypes.heat.", "");
            }
        }
    }

    public convertToServerObject:Function = ():string => {
        let serverObject = {};
        let mapData = {
            "type": this.type,
            "required": this.required || false,
            "defaultValue": this.defaultValue != "" && this.defaultValue != "[]" && this.defaultValue != "{}" ? this.defaultValue : null,
            "description": this.description,
            "constraints": this.constraints,
            "isPassword": this.password || false,
            "schema": this.schema,
            "name": this.name
        };
        serverObject[this.name] = mapData;

        return JSON.stringify(serverObject);
    };

    public toJSON = ():any => {
        // if(!this.resourceInstanceUniqueId){
        //     this.value = undefined;
        // }
        let temp = angular.copy(this);
        temp.readonly = undefined;
        temp.resourceInstanceUniqueId = undefined;
        temp.simpleType = undefined;
        temp.value = temp.value === "{}" || temp.value === "[]" ? undefined : temp.value;
        temp.defaultValue = temp.defaultValue === "{}" || temp.defaultValue === "[]" ? undefined : temp.defaultValue;
        temp.rules = null; //don't send rules to server until feature is fully supported
        temp.isAlreadySelected = undefined;
        temp.addOn = undefined;
        return temp;
    };
}
