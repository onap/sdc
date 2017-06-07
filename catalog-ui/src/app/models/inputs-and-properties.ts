/**
 * Created by obarda on 1/11/2017.
 */
'use strict';
import {PropertyModel} from "./properties";
import {InputModel} from "./inputs";

export class InputsAndProperties {

    inputs:Array<InputModel>;
    properties:Array<PropertyModel>;


    constructor(inputs?:Array<InputModel>, properties?:Array<PropertyModel>) {
        if (inputs && inputs.length > 0) {
            this.inputs = inputs;
        }
        if (properties && properties.length > 0) {
            this.properties = properties;
        }
    }
}
