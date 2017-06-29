import { SchemaPropertyGroupModel, SchemaProperty } from "../aschema-property";
import { PropertyBEModel } from "../../models";
import {PROPERTY_DATA} from "../../utils/constants";
import {InputBEModel} from "./input-be-model";

export class InputFEModel extends InputBEModel {
    isSimpleType: boolean;
    relatedProperty: SimpleRelatedProperty;

    constructor(input?: InputBEModel) {
        super(input);
        if (input) {
            this.isSimpleType = PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1;
            this.relatedProperty = new SimpleRelatedProperty(input);
        }
    }

}

export class SimpleRelatedProperty {
    name: string;
    value: string;
    nestedPath: string;

    constructor(input: InputBEModel) {
       if(!input.instanceUniqueId){
           return;
       }
        //Check if input is on DerivedFEProperty level, in which case we want to set a nested path
        let instanceName = input.instanceUniqueId.split('.').pop();
        if (input.inputPath && input.inputPath.indexOf('#') > -1
            && instanceName + "_" + input.inputPath.split('#').join('_') == input.name) {  //Ignore inputPath for a complex child on VL that was declared within VF, that was then dragged into service. For that case, input.name will be missing the vl name, so we'll know to ignore the path and fall into else case.
            this.nestedPath = input.inputPath;
            this.name = input.inputPath.substring(0, input.inputPath.indexOf('#'));
        } else { //PropertyFEModel level. Can parse input name to get prop name.
            let propNameLength = input.name.length - instanceName.length + 1;
            this.name = input.name.substr(instanceName.length + 1, propNameLength);
        }

        //In declare response, input contains nested property, and we need to extract value so we can update our prop.
        let nestedProperty = input.properties && input.properties[0] || input.inputs && input.inputs[0];
        if (nestedProperty) {
            this.value = nestedProperty.value;
        }
    }
};
