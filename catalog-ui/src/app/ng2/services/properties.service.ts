import { Injectable } from '@angular/core';
import { DataTypeModel, PropertyFEModel, PropertyBEModel, SchemaProperty, DerivedFEProperty, DerivedFEPropertyMap, DerivedPropertyType, InputFEModel} from "app/models";
import { DataTypeService } from "./data-type.service";
import { PROPERTY_TYPES } from "app/utils";
import { ContentAfterLastDotPipe } from "../pipes/contentAfterLastDot.pipe";
import { UUID } from "angular2-uuid";

@Injectable()
export class PropertiesService {

    constructor(private dataTypeService: DataTypeService, private contentAfterLastDotPipe: ContentAfterLastDotPipe) {
    }

    public getParentPropertyFEModelFromPath = (properties: Array<PropertyFEModel>, path: string) => {
        let parent: PropertyFEModel = _.find(properties, (property: PropertyFEModel): boolean => {
            return property.name === path.substring(0, path.indexOf('#'));
        });
        return parent;
    }

    //undo disabling of parent and child props=
    public undoDisableRelatedProperties = (property: PropertyFEModel, childPath?: string): void => {
        property.isDisabled = false;
        if (!childPath) {
            property.isSelected = false;
            property.flattenedChildren && property.flattenedChildren.map(child => child.isDisabled = false);
        } else { //QND - unselect everything and then re-do the disabling of declared props. TODO: put a flag on propertyFEModel instead to indicate who's causing them to be disabled instead
            property.flattenedChildren.filter(child => child.isDisabled && !child.isDeclared).map(child => child.isDisabled = false);
            property.flattenedChildren.filter(child => child.isDeclared || child.isSelected).forEach((childProp) => { //handle brothers who are selected - redo their disabled relatives as well
                this.disableRelatedProperties(property, childProp.propertiesName);
            });
        }
    }

    //disable parents and children of prop
    public disableRelatedProperties = (property: PropertyFEModel, childPath?: string): void => {
        if (!childPath) { //selecting the parent property
            property.isSelected = true;
            property.flattenedChildren && property.flattenedChildren.map(child => { child.isSelected = false; child.isDisabled = true; });
        } else {
            property.isSelected = false;
            property.isDisabled = true;
            property.flattenedChildren.filter((childProp: DerivedFEProperty) => {
                return (childProp.propertiesName.indexOf(childPath + "#") === 0 //is child of prop to disable
                    || childPath.indexOf(childProp.propertiesName + "#") === 0); //is parent of prop to disable
            }).map((child: DerivedFEProperty) => { child.isSelected = false; child.isDisabled = true; });
        }
    }

    public getCheckedProperties = (properties: Array<PropertyFEModel>): Array<PropertyBEModel> => {
        let selectedProps: Array<PropertyBEModel> = [];
        properties.forEach(prop => {
            if (prop.isSelected && !prop.isDeclared && !prop.isDisabled) {
                selectedProps.push(new PropertyBEModel(prop));
            } else if (prop.flattenedChildren) {
                prop.flattenedChildren.forEach((child) => {
                    if (child.isSelected && !child.isDeclared && !child.isDisabled) {
                        let childProp = new PropertyBEModel(prop, child); //create it from the parent
                        selectedProps.push(childProp);
                    }
                })
            }
        });
        return selectedProps;
    }


}