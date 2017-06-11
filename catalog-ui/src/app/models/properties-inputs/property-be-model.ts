import { SchemaPropertyGroupModel, SchemaProperty } from "../aschema-property";
import { PROPERTY_DATA, PROPERTY_TYPES } from 'app/utils';
export enum DerivedPropertyType {
    SIMPLE,
    LIST,
    MAP,
    COMPLEX
}

export class PropertyBEModel {

    defaultValue: string;
    description: string;
    fromDerived: boolean;
    name: string;
    parentUniqueId: string;
    password: boolean;
    required: boolean;
    schema: SchemaPropertyGroupModel;
    type: string;
    uniqueId: string;
    value: string;
    definition: boolean;
    inputPath: string;
    propertiesName: string;
    ownerId: string;
    input: PropertyBEModel;

    constructor(property?: PropertyBEModel, childProperty?:PropertyBEModel) {
        if (property) {
            this.defaultValue = property.defaultValue;
            this.description = property.description;
            this.fromDerived = property.fromDerived;
            this.name = property.name;
            this.parentUniqueId = property.parentUniqueId;
            this.password = property.password;
            this.required = property.required;
            this.schema = property.schema;
            this.type = property.type;
            this.uniqueId = property.uniqueId;
            this.value = property.value ? property.value : property.defaultValue;
            this.definition = property.definition;
            this.ownerId = property.ownerId;
            if (property.inputPath) {
                this.inputPath = property.inputPath;
            }
        }
        if (childProperty) {
            this.input = childProperty;
            this.propertiesName = childProperty.propertiesName;
        } else {
            this.propertiesName = this.name;
        }

        if (!this.schema || !this.schema.property) {
            this.schema = new SchemaPropertyGroupModel(new SchemaProperty());
        } else { //forcing creating new object, so editing different one than the object in the table
            this.schema = new SchemaPropertyGroupModel(new SchemaProperty(this.schema.property));
        }
    }



    public toJSON = (): any => {
        let temp = angular.copy(this);
        temp.value = temp.value === "{}" || temp.value === "[]" ? undefined : temp.value;
        temp.defaultValue = temp.defaultValue === "{}" || temp.defaultValue === "[]" ? undefined : temp.defaultValue;
        return temp;
    };

    public getDerivedPropertyType = () => {
        if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1) {
            return DerivedPropertyType.SIMPLE;
        } else if (this.type == PROPERTY_TYPES.LIST) {
            return DerivedPropertyType.LIST;
        } else if (this.type == PROPERTY_TYPES.MAP) {
            return DerivedPropertyType.MAP;
        } else {
            return DerivedPropertyType.COMPLEX;
        }
    }

}


// EXTRAS FROM CONSTRUCTOR:
//         this.source = property.source;
//         this.valueUniqueUid = property.valueUniqueUid;
//         this.path = property.path;
//         this.rules = property.rules;
//         this.resourceInstanceUniqueId = property.resourceInstanceUniqueId;
//         this.readonly = property.readonly;
//         this.simpleType = property.simpleType;
//         this.componentInstanceId = property.componentInstanceId;
//         this.parentValue = property.parentValue;
//NEW PROPERTIES MAY NEED:
// export class PropertyFEModel extends PropertyBEModel {
//     componentInstanceId: string;
//     isAlreadySelected: boolean;
//     filterTerm: string;
// }
//FOR INPUTS, BE ALSO INCLUDES:
//export class InputFEModel extends PropertyBEModel {
//     hidden: boolean;
//     label: string;
//     immutable: boolean;
// }
