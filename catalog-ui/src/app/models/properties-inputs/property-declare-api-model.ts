'use strict';
import { PropertyBEModel, PropertyFEModel, DerivedFEProperty } from "../../models";


export class PropertyDeclareAPIModel extends PropertyBEModel{
    input: PropertyBEModel;
    propertiesName: string;


    constructor(property: PropertyFEModel, childProperty?: DerivedFEProperty) {
        super(property);
        if (childProperty) {
            this.input = childProperty;
            this.propertiesName = childProperty.propertiesName;
        }
    }

}
