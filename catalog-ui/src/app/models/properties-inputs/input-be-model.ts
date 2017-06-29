import {PropertyBEModel} from 'app/models';
/**
 * Created by rc2122 on 6/1/2017.
 */
export class InputBEModel extends PropertyBEModel {
    properties: Array<ComponentInstanceModel>;
    inputs: Array<ComponentInstanceModel>;
    instanceUniqueId: string;
    propertyId: string;

    constructor(input?: InputBEModel) {
        super(input);
        this.instanceUniqueId = input.instanceUniqueId;
        this.propertyId = input.propertyId;
        this.properties = input.properties;
        this.inputs = input.inputs;
    }



    public toJSON = (): any => {
    };

}

export interface ComponentInstanceModel extends InputBEModel {
    componentInstanceId:string;
    componentInstanceName: string;
}
