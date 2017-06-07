/**
 * Created by osonsino on 16/05/2016.
 */
'use strict';
import { PROPERTY_DATA } from "app/utils";

export class SchemaPropertyGroupModel {
    property:SchemaProperty;

    constructor(schemaProperty?:SchemaProperty) {
        this.property = schemaProperty;
    }
}

export class SchemaProperty {

    type:string;
    required:boolean;
    definition:boolean;
    description:string;
    password:boolean;
    //custom properties
    simpleType:string;
    isSimpleType: boolean;
    isDataType: boolean;
    private _derivedFromSimpleTypeName:string;
    get derivedFromSimpleTypeName():string {
        return this._derivedFromSimpleTypeName;
    }
    set derivedFromSimpleTypeName(derivedFromSimpleTypeName:string) {
        this._derivedFromSimpleTypeName = derivedFromSimpleTypeName;
    }

    constructor(schemaProperty?:SchemaProperty) {
        if (schemaProperty) {
            this.type = schemaProperty.type;
            this.required = schemaProperty.required;
            this.definition = schemaProperty.definition;
            this.description = schemaProperty.description;
            this.password = schemaProperty.password;
            this.simpleType = schemaProperty.simpleType;
            this.isSimpleType = (-1 < PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type));
            this.isDataType = PROPERTY_DATA.TYPES.indexOf(this.type) == -1;
        }
    }

    public toJSON = ():any => {
        this.simpleType = undefined;
        this.isSimpleType = undefined;
        this.isDataType = undefined;
        this._derivedFromSimpleTypeName = undefined;
        return this;
    };
}


