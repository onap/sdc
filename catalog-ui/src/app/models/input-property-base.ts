/**
 * Created by obarda on 1/22/2017.
 */
'use strict';
import {SchemaPropertyGroupModel} from "./aschema-property";

export interface InputPropertyBase {

    uniqueId:string;
    name:string;
    defaultValue:string;
    description:string;
    password:boolean;
    required:boolean;
    type:string;
    parentUniqueId:string;
    schema:SchemaPropertyGroupModel;
    componentInstanceId:string;

    //instance properties
    value:string;

    //custom properties
    isAlreadySelected:boolean;
}

