import { SchemaPropertyGroupModel, SchemaProperty } from "../aschema-property";
import { PropertyBEModel } from "../../models";
import {PROPERTY_DATA} from "../../utils/constants";
import {InputBEModel} from "./input-be-model";

export class InputFEModel extends InputBEModel {
    isSimpleType: boolean;
    relatedPropertyValue: any;
    relatedPropertyName: string;

    constructor(input?: InputBEModel) {
        super(input);
        if (input) {
            this.isSimpleType = PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1;
            let relatedProperty = input.properties && input.properties[0] || input.inputs && input.inputs[0];
            if (relatedProperty) {
                this.relatedPropertyValue = relatedProperty.value;
                this.relatedPropertyName = relatedProperty.name;
            }
        }
    }

}