import {SchemaPropertyGroupModel, SchemaProperty} from '../aschema-property';
import { PROPERTY_DATA } from 'app/utils';
import { PropertyBEModel, DerivedFEPropertyMap, DerivedFEProperty } from '../../models';


export class PropertyFEModel extends PropertyBEModel {

    //START - TO REMOVE:
    treeNodeId: string;
    parent: PropertyFEModel;

    childrenProperties: Array<PropertyFEModel>;
    isAllChildrenLevelsCalculated: boolean;
    uniqueId: string;
    valueObjectRef: any;
    //END - TO REMOVE:

    expandedChildPropertyId: string;
    flattenedChildren:  Array<DerivedFEProperty>; //[parentPath] : Array<DerivedFEProp>
    isDataType: boolean; //aka- isComplexType. (Type is NOT: simple, list, or map)
    isDeclared: boolean;
    isDisabled: boolean;
    isSelected: boolean;
    isSimpleType: boolean;

    private _derivedFromSimpleTypeName:string;
    get derivedFromSimpleTypeName():string {
        return this._derivedFromSimpleTypeName;
    }
    set derivedFromSimpleTypeName(derivedFromSimpleTypeName:string) {
        this._derivedFromSimpleTypeName = derivedFromSimpleTypeName;
    }

    constructor(property?: PropertyBEModel);
    constructor(name: string, type: string, treeNodeId: string, parent: PropertyFEModel, valueObjectRef: any, schema?: SchemaPropertyGroupModel);
    constructor(nameOrPropertyObj?: string | PropertyBEModel, type?: string, treeNodeId?: string, parent?: PropertyFEModel, valueObjectRef?: any, schema?: SchemaPropertyGroupModel) {

        super(typeof nameOrPropertyObj === 'string' ? null : nameOrPropertyObj);

        if (typeof nameOrPropertyObj === 'string') {
            this.name = nameOrPropertyObj;
            this.type = type;
            this.treeNodeId = treeNodeId;
            this.parent = parent;
            this.valueObjectRef = valueObjectRef;
            this.value = this.value || this.defaultValue;
            if(schema){
                this.schema = new SchemaPropertyGroupModel(new SchemaProperty(schema.property));
            }
        }

        this.isSimpleType = PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) > -1;
        this.isDataType = PROPERTY_DATA.TYPES.indexOf(this.type) == -1;
        this.setNonDeclared();
    }

    public convertChildToInput = (childName: string): void => {
        //childName: "mac_count_required"
        let childJson = this.flattenedChildren[childName].map((child) => {

        });
    };

    public getChildJsonRecursive = (child: string, value?: string): void => {
        //TODO: use array.map for the below
       /* value += "{" + this.flattenedChildren[child].name + ":";
        if (this.flattenedChildren[child].valueType == 'simple') {
            value += this.flattenedChildren[child].value + '}';
            return value;
        } else {
            this.flattenedChildren[child].forEach(grandChild => {
                if (this.flattenedChildren[grandChild].valueType == 'simple') {
                    return "{" + this.flattenedChildren[grandChild].name + ':' + this.flattenedChildren[child].value.toString() + "}";
                } else {
                    return  this.getChildJsonRecursive(grandChild + '#' + this.flattenedChildren[child].name);
                }
            });
        }

        return "{" + this.flattenedChildren[child].name + this.flattenedChildren[child].value.toString() + "}";
*/

    };

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



    //For expand-collapse functionality
    public updateExpandedChildPropertyId = (childPropertyId: string): void => {
        if (childPropertyId.lastIndexOf('#') > -1) {
            this.expandedChildPropertyId = (this.expandedChildPropertyId == childPropertyId) ? (childPropertyId.substring(0, childPropertyId.lastIndexOf('#'))) : childPropertyId;
        } else {
            this.expandedChildPropertyId = this.name;
        }
        //console.log("expandedChild is now " + this.expandedChildPropertyId);
    }

    public convertToServerObject: Function = (): any => { //TODO: Idan, Rachel, Nechama: Decide what we need to do here
        // let serverObject = {};
        // let mapData = {
        //     'type': this.type,
        //     'required': this.required || false,
        //     'defaultValue': this.defaultValue != '' && this.defaultValue != '[]' && this.defaultValue != '{}' ? this.defaultValue : null,
        //     'description': this.description,
        //     'isPassword': this.password || false,
        //     'schema': this.schema,
        //     'name': this.name
        // };
        // serverObject[this.name] = mapData;

        //return JSON.stringify(serverObject);
    };
}
