import { SchemaPropertyGroupModel, SchemaProperty } from '../aschema-property';
import { PROPERTY_DATA, PROPERTY_TYPES} from 'app/utils';
import { PropertyBEModel } from '../../models';

export enum DerivedPropertyType {
    SIMPLE,
    LIST,
    MAP,
    COMPLEX //other datatype, list of non-simple, or map of non-simple
}

export class DerivedFEProperty extends PropertyBEModel {
    parentName: string;
    propertiesName: string; //"network_assignments#ipv4_subnet#use_ipv4 =  parentPath + name
    derivedDataType: DerivedPropertyType;
    isDeclared: boolean;
    isSelected: boolean;
    isDisabled: boolean;
    isChildOfListOrMap: boolean;

    constructor(property: PropertyBEModel, parentName?: string)
    constructor(name: string, parentName: string, type: string, value: string, isChildOfListOrMap?:boolean, schema?: SchemaPropertyGroupModel);
    constructor(nameOrPropertyObj?: string | PropertyBEModel, parentName?: string, type?: string, value?: string, isChildOfListOrMap?: boolean, schema?: SchemaPropertyGroupModel) {

        super(typeof nameOrPropertyObj === 'string' ? null : nameOrPropertyObj);

        if (typeof nameOrPropertyObj !== 'string') { //constructor #1
            this.parentName = parentName ? parentName : null;
            this.propertiesName = (parentName) ? parentName + '#' + nameOrPropertyObj.name : nameOrPropertyObj.name;
        } else { //constructor #2
            this.name = nameOrPropertyObj;
            this.type = type;
            this.parentName = parentName;
            this.propertiesName = parentName + '#' + nameOrPropertyObj;
            this.value = value;
            if (schema) {
                this.schema = new SchemaPropertyGroupModel(new SchemaProperty(schema.property));
            }
        }
        this.derivedDataType = this.getDerivedPropertyType();
        this.isChildOfListOrMap = (isChildOfListOrMap) ? isChildOfListOrMap : false;
    }
   
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
export class DerivedFEPropertyMap {
    [parentPath: string]: Array<DerivedFEProperty>;
}



// isDataType: boolean;


// canAdd: boolean;
// canCollapse: boolean;
// canBeDeclared: boolean;

// derivedValue: string;
// derivedValueType: string;
// propertiesName: string; 