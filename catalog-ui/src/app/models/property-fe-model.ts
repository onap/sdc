import {SchemaPropertyGroupModel, SchemaProperty} from './aschema-property';
import { PROPERTY_DATA } from 'app/utils';
import { FilterPropertiesAssignmentData, PropertyBEModel } from 'app/models';

export class PropertyFEModel extends PropertyBEModel {
    public static filterData:FilterPropertiesAssignmentData;
    childrenProperties: Array<PropertyFEModel>;
    expandedChildPropertyId: string;
    isAllChildrenLevelsCalculated: boolean;
    isDataType: boolean;
    isDisabled: boolean;
    isSelected: boolean;
    isSimpleType: boolean;
    parent: PropertyFEModel;
    treeNodeId: string;
    valueObjectRef: any;
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


    public setNonDeclared = (): void => {
        this.isSelected = false;
        this.isDisabled = false;
    }

    public setAsDeclared = (): void => {
        this.isSelected = true;
        this.isDisabled = true;
    }

    //For expand-collapse functionality
    public updateExpandedChildPropertyId = (childPropertyId: string): void => {
        this.expandedChildPropertyId = (this.expandedChildPropertyId == childPropertyId) ? '' : childPropertyId;
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
