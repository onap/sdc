import {SchemaPropertyGroupModel, SchemaProperty} from '../aschema-property';
import { PROPERTY_DATA, PROPERTY_TYPES } from 'app/utils';
import { FilterPropertiesAssignmentData, PropertyBEModel, DerivedPropertyType, DerivedFEPropertyMap, DerivedFEProperty } from 'app/models';


export class PropertyFEModel extends PropertyBEModel {

    expandedChildPropertyId: string;
    flattenedChildren:  Array<DerivedFEProperty>; //[parentPath] : Array<DerivedFEProp>
    isDeclared: boolean;
    isDisabled: boolean;
    isSelected: boolean;
    isSimpleType: boolean; //for convenience only - we can really just check if derivedDataType == derivedPropertyTypes.SIMPLE to know if the prop is simple
    uniqueId: string;
    valueObj: any; //this is the only value we relate to in the html templates
    derivedDataType: DerivedPropertyType;

    constructor(property: PropertyBEModel){
        super(property);
        this.isSimpleType = PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1;
        this.setNonDeclared();
        this.derivedDataType = this.getDerivedPropertyType();
        this.flattenedChildren = [];
    }


    public getJSONValue = (): string => {
        //If type is JSON, need to try parsing it before we stringify it so that it appears property in TOSCA - change per Bracha due to AMDOCS
        //TODO: handle this.derivedDataType == DerivedPropertyType.MAP
        if (this.derivedDataType == DerivedPropertyType.LIST && this.schema.property.type == PROPERTY_TYPES.JSON) {
            try {
                return JSON.stringify(this.valueObj.map(item => JSON.parse(item)));
            } catch (e){}
        }

        return (this.derivedDataType == DerivedPropertyType.SIMPLE) ? this.valueObj : JSON.stringify(this.valueObj);     
    }

    public setNonDeclared = (childPath?: string): void => {
        if (!childPath) { //declaring a child prop
            this.isDeclared = false;
        } else {
            let childProp: DerivedFEProperty = this.flattenedChildren.find(child => child.propertiesName == childPath);
            childProp.isDeclared = false;
        }
    }

    public setAsDeclared = (childNameToDeclare?:string): void => {
        if (!childNameToDeclare) { //declaring a child prop
            this.isSelected = false;
            this.isDeclared = true;
        } else {
            let childProp: DerivedFEProperty = this.flattenedChildren.find(child => child.propertiesName == childNameToDeclare);
            childProp.isSelected = false;
            childProp.isDeclared = true;
        }
    }

    //For expand-collapse functionality - used within HTML template
    public updateExpandedChildPropertyId = (childPropertyId: string): void => {
        if (childPropertyId.lastIndexOf('#') > -1) {
            this.expandedChildPropertyId = (this.expandedChildPropertyId == childPropertyId) ? (childPropertyId.substring(0, childPropertyId.lastIndexOf('#'))) : childPropertyId;
        } else {
            this.expandedChildPropertyId = this.name;
        }
    }

    public getIndexOfChild = (childPropName: string): number => {
        return this.flattenedChildren.findIndex(prop => prop.propertiesName.indexOf(childPropName) === 0);
    }

    public getCountOfChildren = (childPropName: string):number => {
        let matchingChildren:Array<DerivedFEProperty> = this.flattenedChildren.filter(prop => prop.propertiesName.indexOf(childPropName) === 0) || [];
        return matchingChildren.length;
    }

    // public getListIndexOfChild = (childPropName: string): number => { //gets list of siblings and then the index within that list
    //     this.flattenedChildren.filter(prop => prop.parentName == item.parentName).map(prop => prop.propertiesName).indexOf(item.propertiesName)
    // }

}
