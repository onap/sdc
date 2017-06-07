'use strict';
import { PropertyBEModel, PropertyFEModel } from "../../models";

export class InstanceBePropertiesMap {
    [instanceId: string]: Array<PropertyBEModel>;
}

export class InstanceFePropertiesMap {
    [instanceId: string]: Array<PropertyFEModel>;
}

export class InstancePropertiesAPIMap {
    componentInstanceProperties: InstanceBePropertiesMap;
    componentInstanceInputsMap: InstanceBePropertiesMap;

    constructor(inputsMapData: InstanceBePropertiesMap, propertiesMapData: InstanceBePropertiesMap) {
        this.componentInstanceInputsMap = inputsMapData ? inputsMapData: new InstanceBePropertiesMap();
        this.componentInstanceProperties = propertiesMapData ? propertiesMapData: new InstanceBePropertiesMap();
    }

}
