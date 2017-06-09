'use strict';
import {SchemaProperty} from "./aschema-property";

export class SchemaAttributeGroupModel {
    property:SchemaAttribute;

    constructor(schemaAttribute?:SchemaAttribute) {
        this.property = schemaAttribute;
    }
}

export class SchemaAttribute extends SchemaProperty {

}


