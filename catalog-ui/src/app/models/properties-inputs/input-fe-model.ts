import { SchemaPropertyGroupModel, SchemaProperty } from "../aschema-property";
import { PropertyBEModel } from "../../models";
import {PROPERTY_DATA} from "../../utils/constants";
import {InputBEModel} from "./input-be-model";

export class InputFEModel extends InputBEModel {
    isSimpleType: boolean;
    isDataType: boolean;
    instanceName: string;
    instanceId: string;
    propertyName: string;


    constructor(input?: InputBEModel) {
        super(input);
        if (input) {
            this.isSimpleType = PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1;
            this.isDataType = PROPERTY_DATA.TYPES.indexOf(this.type) == -1;

            let propNameIndex:number = this.name.indexOf('_');
            this.instanceName = this.name.substring(0, propNameIndex);

            if(input.properties && input.properties.length){
                this.instanceId = input.properties[0].componentInstanceId;
                this.propertyName = input.properties[0].name;
            }else if(input.inputs && input.inputs.length){
                this.instanceId = input.inputs[0].componentInstanceId;
                this.propertyName = input.inputs[0].name;
            }else{
                if (input.inputPath) {
                    this.propertyName = input.inputPath.substring(0, input.inputPath.indexOf('#'))
                } else {
                    this.propertyName = this.name.substring(propNameIndex + 1);
                }
            }
        }
    }



    public toJSON = (): any => {
    };

}
