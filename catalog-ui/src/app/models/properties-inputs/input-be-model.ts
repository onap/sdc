import {PropertyBEModel} from 'app/models';
/**
 * Created by rc2122 on 6/1/2017.
 */
export class InputBEModel extends PropertyBEModel {

    inputPath: string;
    inputs: Array<ComponentInstanceModel>;
    instanceUniqueId: string;
    ownerId: string;
    propertyId: string;
    properties: Array<ComponentInstanceModel>;

    constructor(input?: InputBEModel) {
        super(input);
        this.instanceUniqueId = input.instanceUniqueId;
        this.propertyId = input.propertyId;
        this.properties = input.properties;
        this.inputs = input.inputs;
        this.ownerId = input.ownerId;
        this.inputPath = input.inputPath;
    }

    public toJSON = (): any => {
    };

}

export interface ComponentInstanceModel extends InputBEModel {
    componentInstanceId:string;
    componentInstanceName: string;
}
