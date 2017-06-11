import {PropertyBEModel} from 'app/models';
/**
 * Created by rc2122 on 6/1/2017.
 */
export class InputBEModel extends PropertyBEModel {
    properties:Array<ComponentInstanceProperty>;
    inputs:Array<ComponentInstanceInput>;

    constructor(input?: PropertyBEModel) {
        super(input);
    }



    public toJSON = (): any => {
    };

}

export class ComponentInstanceProperty extends PropertyBEModel {
    componentInstanceId:string;
    componentInstanceName:string;

    constructor(property?: PropertyBEModel) {
        super(property);
    }



    public toJSON = (): any => {
    };

}

export class ComponentInstanceInput extends InputBEModel {
    componentInstanceId:string;
    componentInstanceName:string;

    constructor(property?: PropertyBEModel) {
        super(property);
    }



    public toJSON = (): any => {
    };

}

