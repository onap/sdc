import { Injectable } from '@angular/core';
import { DataTypeModel, PropertyFEModel, PropertyBEModel, SchemaProperty, DerivedFEProperty, DerivedFEPropertyMap, DerivedPropertyType, InputFEModel} from "app/models";
import { DataTypeService } from "./data-type.service";
import { PROPERTY_TYPES } from "app/utils";
import { ContentAfterLastDotPipe } from "../pipes/contentAfterLastDot.pipe";
import { UUID } from "angular2-uuid";

@Injectable()
export class PropertiesService {

    constructor(private dataTypeService:DataTypeService, private contentAfterLastDotPipe:ContentAfterLastDotPipe) {
    }

    public getParentPropertyFEModelFromPath = (properties:Array<PropertyFEModel>, path:string) => {
        let parent:PropertyFEModel = _.find(properties, (property:PropertyFEModel):boolean=>{
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
    public disableRelatedProperties = (property:PropertyFEModel, childPath?: string): void => {
        if (!childPath) { //selecting the parent property
            property.isSelected = true;
            property.flattenedChildren && property.flattenedChildren.map(child => { child.isSelected = false; child.isDisabled = true; });
        } else {
            property.isSelected = false;
            property.isDisabled = true;
            property.flattenedChildren.filter((childProp: DerivedFEProperty) => {
                return (childProp.propertiesName.indexOf(childPath + "#") > -1 //is child of prop to disable
                    || childPath.indexOf(childProp.propertiesName + "#") > -1); //is parent of prop to disable
            }).map((child: DerivedFEProperty) => { child.isSelected = false; child.isDisabled = true; });
        }
    }

    public getCheckedProperties = (properties:Array<PropertyFEModel>): Array<PropertyBEModel> => {
        let selectedProps: Array<PropertyBEModel> = [];
        properties.forEach(prop => {
            if (prop.isSelected && !prop.isDeclared && !prop.isDisabled) {
                selectedProps.push(new PropertyBEModel(prop));
            } else if(prop.flattenedChildren) {
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

    /**
     * Build hirarchy structure for the tree when user selects on table row.
     * First create Array<SimpleFlatProperty> and insert also the parent (PropertyFEModel) to this array.
     * The Array is flat and contains SimpleFlatProperty that has parentName and uniqueId.
     * Now we build hirarchy from this Array (that includes childrens) and return it for the tree
     *
     * @argument property: PropertyFEModel - property contains flattenedChildren array of DerivedFEProperty
     * @returns  Array<SimpleFlatProperty> - containing childrens Array<SimpleFlatProperty>, augmantin childrens to SimpleFlatProperty.
     */
    public getSimplePropertiesTree(property: PropertyFEModel, instanceName:string):Array<SimpleFlatProperty> {
        // Build Array of SimpleFlatProperty before unflatten function
        let flattenProperties:Array<SimpleFlatProperty> = [];
        flattenProperties.push(new SimpleFlatProperty(property.uniqueId, property.name, property.name, '', instanceName)); // Push the root property
        _.each(property.flattenedChildren, (child:DerivedFEProperty):void => {
            flattenProperties.push(new SimpleFlatProperty(child.uniqueId, child.propertiesName, child.name, child.parentName, instanceName));
        });

        let tree = this.unflatten(flattenProperties, '', []);
        return tree[0].childrens; // Return the childrens without the root.
    }

    /**
     * Unflatten Array<SimpleFlatProperty> and build hirarchy.
     * The result will be Array<SimpleFlatProperty> that augmantin with childrens for each SimpleFlatProperty.
     */
    private unflatten( array:Array<SimpleFlatProperty>, parent:any, tree?:any ):any {
        tree = typeof tree!=='undefined' ? tree : [];
        parent = typeof parent!=='undefined' && parent!=='' ? parent : { path: '' };

        var childrens = _.filter( array, (child:SimpleFlatProperty):boolean => {
            return child.parentName == parent.path;
        });

        if( !_.isEmpty( childrens )  ){
            if( parent.path == '' ){
                tree = childrens;
            } else {
                parent['childrens'] = childrens;
            }
            _.each( childrens, ( child ):void => {
                this.unflatten( array, child );
            });
        }
        return tree;
    }

    // TODO: To remove
    // public convertPropertiesToFEAndInitialize(properties: Array<PropertyBEModel>): Array<PropertyFEModel> {
    //     let props:Array<PropertyFEModel> = [];
    //     _.forEach(properties, (property: PropertyFEModel, index: number) => {
    //         props[index] = new PropertyFEModel(property);
    //         this.initValueObjectRef(props[index]);
    //         if (props[index].isDataType ||
    //             ((props[index].type === PROPERTY_TYPES.MAP || props[index].type === PROPERTY_TYPES.LIST) && props[index].schema.property.isDataType)) {
    //             props[index].valueObjectRef = props[index].valueObjectRef || {};
    //             let defaultValueObject:any = props[index].defaultValue ? JSON.parse(props[index].defaultValue): null;
    //             this.createPropertiesTreeForProp(props[index], true, defaultValueObject);
    //         }
    //     });
    //     return props;
    // }


    /**
     * Converts a property's map values to properties and adds them to property.childrenProperties
     * @param property - property to add children to
     * @param onlyFirstLevel - recursively retrieve properties for each dataType?
     */
    //TODO: To remove
    // public createPropertyNodesForMapOfDataTypes(property: PropertyFEModel, onlyFirstLevel:boolean):void {
    //     property.childrenProperties = [];
    //     angular.forEach(property.valueObjectRef,function(itemInMap:any, keyInMap:string){
    //         let newProperty: PropertyFEModel = new PropertyFEModel(keyInMap,
    //             property.schema.property.type,
    //             UUID.UUID(),
    //             property,
    //             property.valueObjectRef[keyInMap]);
    //         !onlyFirstLevel && this.createPropertiesTreeForProp(newProperty);
    //         property.childrenProperties.push(newProperty);
    //     },this);
    // }


    /**
     * Converts a property's list values to properties and adds them to property.childrenProperties
     * @param property - property to add children to
     * @param onlyFirstLevel - recursively retrieve properties for each dataType?
     */
    //TODO: To remove
    // public createPropertyNodesForListOfDataTypes(property: PropertyFEModel, onlyFirstLevel:boolean):void {
    //     property.childrenProperties = [];
    //     property.valueObjectRef.forEach((itemInList:any, index:number):void =>{
    //         let newProperty: PropertyFEModel = new PropertyFEModel(this.contentAfterLastDotPipe.transform(property.schema.property.type),
    //             property.schema.property.type,
    //             UUID.UUID(),
    //             property,
    //             property.valueObjectRef[index]);
    //         !onlyFirstLevel && this.createPropertiesTreeForProp(newProperty);
    //         property.childrenProperties.push(newProperty);
    //     });
    // }

    // private checkIfPropertyDerivedFromSimpleAndUpdateProp(property:PropertyFEModel | SchemaProperty): boolean{
    //     property.derivedFromSimpleTypeName = this.dataTypeService.getTypeForDataTypeDerivedFromSimple(property.type);
    //     if(property.derivedFromSimpleTypeName){
    //         property.isSimpleType = true;
    //         property.isDataType = false;
    //     }
    //     return property.isSimpleType;
    // }

    // TODO: Remove
    public createPropertiesTreeForProp(property: PropertyFEModel, onlyFirstLevel?: boolean, defaultValueObj?: any): void {
    }

    public getPropertyFEModelFromDerivedPropertyUniqueId(properties: Array<PropertyFEModel>, uniqueId: string): PropertyFEModel {
        // _.each(properties, (property):void => {
        //     property.flattenedChildren

        // });

        // let rootProperty = _.find(properties, (property):boolean => { return property.uniqueId === uniqueId; });
        // if
        return null;
    }

    /**
     * Utilizes the dataTypeService to retrieve children properties from dataTypes recursively.
     * @param property
     * @param onlyFirstLevel
     */
    // TODO: To remove
    // public createPropertiesTreeForProp(property: PropertyFEModel, onlyFirstLevel?:boolean, defaultValueObj?:any ):void{
    //     if (property.isDataType && !this.checkIfPropertyDerivedFromSimpleAndUpdateProp(property)){
    //         let dataType: DataTypeModel = this.dataTypeService.getDataTypeByTypeName(property.type);
    //         property.childrenProperties = [];
    //         let childrenProperties = this.dataTypeService.getDataTypePropertiesRecursively(dataType);
    //         childrenProperties.forEach((childProperty: PropertyBEModel):void => {
    //             let childDataTypePropertyObj: PropertyFEModel = new PropertyFEModel(childProperty.name, childProperty.type,UUID.UUID(),property, {}, childProperty.schema);
    //             //init empty object in valueObjectRef[childProperty.name] because this property's children should has ref to this obj.
    //             if(!property.valueObjectRef[childDataTypePropertyObj.name]){
    //                 if ((childDataTypePropertyObj.isDataType && !this.checkIfPropertyDerivedFromSimpleAndUpdateProp(childDataTypePropertyObj))
    //                     || childDataTypePropertyObj.type === PROPERTY_TYPES.MAP){
    //                     property.valueObjectRef[childDataTypePropertyObj.name] = {};
    //                 }else if(childDataTypePropertyObj.type === PROPERTY_TYPES.LIST){
    //                     property.valueObjectRef[childDataTypePropertyObj.name] = [];
    //                 }
    //             }
    //             childDataTypePropertyObj.valueObjectRef = property.valueObjectRef[childDataTypePropertyObj.name];
    //             property.valueObjectRef[childDataTypePropertyObj.name] =  property.valueObjectRef[childProperty.name] || childDataTypePropertyObj.defaultValue;
    //             if( !childDataTypePropertyObj.isDataType && defaultValueObj ){//init property default value
    //                 childDataTypePropertyObj.defaultValue = JSON.stringify(defaultValueObj[childDataTypePropertyObj.name]);
    //             }
    //             property.childrenProperties.push(childDataTypePropertyObj);
    //             !onlyFirstLevel && this.createPropertiesTreeForProp(childDataTypePropertyObj, false, (defaultValueObj ? defaultValueObj[childDataTypePropertyObj.name] : null));
    //         });
    //     } else if (property.type == PROPERTY_TYPES.MAP && property.schema.property.isDataType && !this.checkIfPropertyDerivedFromSimpleAndUpdateProp(property.schema.property)){
    //         if( property.valueObjectRef && !_.isEmpty(property.valueObjectRef)){
    //             this.createPropertyNodesForMapOfDataTypes(property, onlyFirstLevel);
    //         }
    //     } else if (property.type == PROPERTY_TYPES.LIST && property.schema.property.isDataType && !this.checkIfPropertyDerivedFromSimpleAndUpdateProp(property.schema.property)){
    //         if( property.valueObjectRef && property.valueObjectRef.length){
    //             this.createPropertyNodesForListOfDataTypes(property, onlyFirstLevel);
    //         }
    //     }
    // }
}

export class SimpleFlatProperty {
    uniqueId:string;
    path:string;
    name:string;
    parentName:string;
    instanceName:string;

    constructor(uniqueId?:string, path?:string, name?: string, parentName?:string, instanceName?:string) {
        this.uniqueId = uniqueId;
        this.path = path;
        this.name = name;
        this.parentName = parentName;
        this.instanceName = instanceName;
    }
}
