import { SchemaPropertyGroupModel, SchemaProperty } from '../aschema-property';
import { DerivedPropertyType, PropertyBEModel } from '../../models';
import { PROPERTY_TYPES } from 'app/utils';
import { UUID } from "angular2-uuid";


export class DerivedFEProperty extends PropertyBEModel {
    valueObj: any; 
    parentName: string;
    propertiesName: string; //"network_assignments#ipv4_subnet#use_ipv4 =  parentPath + name
    derivedDataType: DerivedPropertyType;
    isDeclared: boolean;
    isSelected: boolean;
    isDisabled: boolean;
    hidden: boolean;
    isChildOfListOrMap: boolean;
    canBeDeclared: boolean;
    mapKey: string;

    constructor(property: PropertyBEModel, parentName?: string, createChildOfListOrMap?: boolean, key?:string, value?:any) {
        if (!createChildOfListOrMap) { //creating a standard derived prop
            super(property);
            this.parentName = parentName ? parentName : null;
            this.propertiesName = (parentName) ? parentName + '#' + property.name : property.name;
            this.canBeDeclared = true; //defaults to true
        } else { //creating a direct child of list or map (ie. Item that can be deleted, with UUID instead of name)
            super(null);
            this.isChildOfListOrMap = true;
            this.canBeDeclared = false;
            this.name = UUID.UUID();
            this.parentName = parentName;
            this.propertiesName = parentName + '#' + this.name;

            
            if (property.type == PROPERTY_TYPES.LIST) {
                this.mapKey = property.schema.property.type.split('.').pop();
                this.type = property.schema.property.type;
            } else { //map
                this.mapKey = key || "";
                this.type = property.type;
            }
            this.valueObj = (this.type == PROPERTY_TYPES.JSON && typeof value == 'object') ? JSON.stringify(value) : value;
            this.schema = new SchemaPropertyGroupModel(new SchemaProperty(property.schema.property));
        }
        this.derivedDataType = this.getDerivedPropertyType();
    }
    
}
export class DerivedFEPropertyMap {
    [parentPath: string]: Array<DerivedFEProperty>;
}

