import { SchemaPropertyGroupModel, SchemaProperty } from "../aschema-property";
import { PropertyBEModel } from "../../models";
import {PROPERTY_DATA} from "../../utils/constants";

export class InputFEModel extends PropertyBEModel {
    isSimpleType: boolean;
    isDataType: boolean;
    instanceName: string;
    propertyName: string;


    constructor(input?: PropertyBEModel) {
        super(input);
        if (input) {
            this.isSimpleType = PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1;
            this.isDataType = PROPERTY_DATA.TYPES.indexOf(this.type) == -1;
            let propNameIndex:number = this.name.indexOf('_');
            this.instanceName = this.name.substring(0, propNameIndex);
            if (input.inputPath) {
                this.propertyName = input.inputPath.substring(0, input.inputPath.indexOf('#'))
            } else {
                this.propertyName = this.name.substring(propNameIndex + 1);
            }
        }
    }



    public toJSON = (): any => {
    };

}
