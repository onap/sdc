/**
 * Created by obarda on 8/24/2016.
 */
'use strict';
import {PropertyModel} from "./properties";
import {InputPropertyBase} from "./input-property-base";
import {SchemaPropertyGroupModel} from "./aschema-property";

export interface IInputModel extends InputPropertyBase {
    //server data
    definition:boolean;
    value:string;
    componentInstanceName:string;
    //costom properties
    isNew:boolean;
    properties:Array<PropertyModel>;
    inputs:Array<InputModel>;
    filterTerm:string;

}
export class InputModel implements IInputModel {

    //server data
    uniqueId:string;
    name:string;
    type:string;
    password:boolean;
    required:boolean;
    definition:boolean;
    parentUniqueId:string;
    description:string;
    componentInstanceName:string;
    componentInstanceId:string;
    schema:SchemaPropertyGroupModel;
    defaultValue:string;
    value:string;

    //costom properties
    isNew:boolean;
    isDeleteDisabled:boolean;
    properties:Array<PropertyModel>;
    inputs:Array<InputModel>;
    isAlreadySelected:boolean;
    filterTerm:string;

    constructor(input:InputModel) {

        if (input) {
            this.uniqueId = input.uniqueId;
            this.name = input.name;
            this.type = input.type;
            this.description = input.description;
            this.password = input.password;
            this.required = input.required;
            this.definition = input.definition;
            this.parentUniqueId = input.parentUniqueId;
            this.description = input.description;
            this.componentInstanceName = input.componentInstanceName;
            this.componentInstanceId = input.componentInstanceId;
            this.schema = input.schema;
            this.defaultValue = input.defaultValue;
            this.value = input.value;
            this.filterTerm = this.name + ' ' + this.description + ' ' + this.type + ' ' + this.componentInstanceName;
            this.inputs = input.inputs;
            this.properties = input.properties;
        }
    }

    public toJSON = ():any => {
        let input = angular.copy(this);
        input.isNew = undefined;
        input.isDeleteDisabled = undefined;
        input.properties = undefined;
        input.inputs = undefined;
        input.isAlreadySelected = undefined;
        input.filterTerm = undefined;
        return input;
    };
}

