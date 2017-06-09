'use strict';
import {SchemaAttributeGroupModel, SchemaAttribute} from "./schema-attribute";
import {SchemaPropertyGroupModel, SchemaProperty} from "./aschema-property";

export class AttributesGroup {
    constructor(attributesObj?:AttributesGroup) {
        _.forEach(attributesObj, (attributes:Array<AttributeModel>, instance) => {
            this[instance] = [];
            _.forEach(attributes, (attribute:AttributeModel):void => {
                attribute.resourceInstanceUniqueId = instance;
                attribute.readonly = true;
                this[instance].push(new AttributeModel(attribute));
            });
        });
    }
}

export interface IAttributeModel {

    //server data
    uniqueId:string;
    name:string;
    defaultValue:string;
    description:string;
    type:string;
    schema:SchemaAttributeGroupModel;
    status:string;
    value:string;
    hidden:boolean;
    parentUniqueId:string;
    //custom data
    resourceInstanceUniqueId:string;
    readonly:boolean;
    valueUniqueUid:string;
}

export class AttributeModel implements IAttributeModel {

    //server data
    uniqueId:string;
    name:string;
    defaultValue:string;
    description:string;
    type:string;
    schema:SchemaAttributeGroupModel;
    status:string;
    value:string;
    hidden:boolean;
    parentUniqueId:string;
    //custom data
    resourceInstanceUniqueId:string;
    readonly:boolean;
    valueUniqueUid:string;

    constructor(attribute?:AttributeModel) {
        if (attribute) {
            this.uniqueId = attribute.uniqueId;
            this.name = attribute.name;
            this.defaultValue = attribute.defaultValue;
            this.description = attribute.description;
            this.type = attribute.type;
            this.status = attribute.status;
            this.schema = attribute.schema;
            this.value = attribute.value;
            this.hidden = attribute.hidden;
            this.parentUniqueId = attribute.parentUniqueId;
            this.resourceInstanceUniqueId = attribute.resourceInstanceUniqueId;
            this.readonly = attribute.readonly;
            this.valueUniqueUid = attribute.valueUniqueUid;
        }

        if (!this.schema || !this.schema.property) {
            this.schema = new SchemaPropertyGroupModel(new SchemaProperty());
        } else {
            //forcing creating new object, so editing different one than the object in the table
            this.schema = new SchemaAttributeGroupModel(new SchemaAttribute(this.schema.property));
        }

        this.convertValueToView();
    }

    public convertToServerObject:Function = ():string => {
        if (this.defaultValue && this.type === 'map') {
            this.defaultValue = '{' + this.defaultValue + '}';
        }
        if (this.defaultValue && this.type === 'list') {
            this.defaultValue = '[' + this.defaultValue + ']';
        }
        this.defaultValue = this.defaultValue != "" && this.defaultValue != "[]" && this.defaultValue != "{}" ? this.defaultValue : null;

        return JSON.stringify(this);
    };


    public convertValueToView() {
        //unwrapping value {} or [] if type is complex
        if (this.defaultValue && (this.type === 'map' || this.type === 'list') &&
            ['[', '{'].indexOf(this.defaultValue.charAt(0)) > -1 &&
            [']', '}'].indexOf(this.defaultValue.slice(-1)) > -1) {
            this.defaultValue = this.defaultValue.slice(1, -1);
        }

        //also for value - for the modal in canvas
        if (this.value && (this.type === 'map' || this.type === 'list') &&
            ['[', '{'].indexOf(this.value.charAt(0)) > -1 &&
            [']', '}'].indexOf(this.value.slice(-1)) > -1) {
            this.value = this.value.slice(1, -1);
        }
    }

    public toJSON = ():any => {
        if (!this.resourceInstanceUniqueId) {
            this.value = undefined;
        }
        this.readonly = undefined;
        this.resourceInstanceUniqueId = undefined;
        return this;
    };
}
