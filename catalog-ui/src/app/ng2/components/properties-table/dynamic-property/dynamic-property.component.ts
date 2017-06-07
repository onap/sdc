import {Component, Input, Output, EventEmitter} from "@angular/core";
import { PropertyBEModel, PropertyFEModel, DerivedFEProperty, DerivedPropertyType, SchemaPropertyGroupModel, DataTypeModel } from "app/models";
import { PROPERTY_DATA, PROPERTY_TYPES } from 'app/utils';
import { PropertiesUtils } from "app/ng2/pages/properties-assignment/properties.utils";
import { DataTypeService } from "../../../services/data-type.service";
import { UUID } from "angular2-uuid";


@Component({
    selector: 'dynamic-property',
    templateUrl: './dynamic-property.component.html',
    styleUrls: ['./dynamic-property.component.less']
})
export class DynamicPropertyComponent {

    derivedPropertyTypes = DerivedPropertyType; //http://stackoverflow.com/questions/35835984/how-to-use-a-typescript-enum-value-in-an-angular2-ngswitch-statement
    propType: DerivedPropertyType;
    propPath: string;
    isPropertyFEModel: boolean;
    mapOfIDsAndKeys: Map<string, string> = new Map(); //used for map and list

    childrenCanBeDeclared: boolean;
    @Input() canBeDeclared: boolean;
    @Input() property: PropertyFEModel | DerivedFEProperty;
    @Input() propChildren: Array<DerivedFEProperty>;
    @Input() expandedChildId: string;
    @Input() selectedPropertyId: string;

    @Output() valueChanged: EventEmitter<any> = new EventEmitter<any>();
    @Output() expandChild: EventEmitter<string> = new EventEmitter<string>();
    @Output() checkProperty: EventEmitter<string> = new EventEmitter<string>();
    @Output() deleteItem: EventEmitter<string> = new EventEmitter<string>();
    @Output() clickOnPropertyRow: EventEmitter<PropertyFEModel | DerivedFEProperty> = new EventEmitter<PropertyFEModel | DerivedFEProperty>();

    constructor(private propertiesUtils: PropertiesUtils, private dataTypeService: DataTypeService) {
    }

    ngOnInit() {
        this.isPropertyFEModel = this.property instanceof PropertyFEModel;
        if (this.property instanceof PropertyFEModel) {
            this.propType = this.getDerivedPropertyType(this.property.type);
            this.propPath = this.property.name;
        } else {
            this.propType = this.property.derivedDataType;
            this.propPath = this.property.propertiesName;
        }

        this.childrenCanBeDeclared = this.canBeDeclared && this.propType != this.derivedPropertyTypes.MAP && this.propType != this.derivedPropertyTypes.LIST;

        if (this.propType == this.derivedPropertyTypes.LIST || this.propType == this.derivedPropertyTypes.MAP) {
            this.initializeValues();
        }

    }

    initializeValues = () => {
        let tempValue: any;
        if (this.property.value) {
            tempValue = JSON.parse(this.property.value);
            if (!_.isEmpty(tempValue)) {
                tempValue.forEach((element, key) => {
                    let newChildID: string = this.createNewChildProperty(JSON.stringify(element));
                    this.mapOfIDsAndKeys[newChildID] = key;
                    console.log(this.mapOfIDsAndKeys);
                });
            }
        }
        //this.pseudoChildren = [];
        //this.valueObjRef = [];
        //TODO: generate necessary elements for existing values here
        // if (this.propType == this.derivedPropertyTypes.LIST) {
        //     this.valueObjRef = (this.property.value) ? JSON.parse(this.property.value) : [];
        // } else if (this.propType == this.derivedPropertyTypes.MAP) {
        //     this.valueObjRef = (this.property.value)? JSON.parse(this.property.value) : {};
        // }
        console.log(this.property.value);
    }

    onClickPropertyRow = (property, event) => {
        // Because DynamicPropertyComponent is recrusive second time the event is fire event.stopPropagation = undefined
        event && event.stopPropagation && event.stopPropagation();
        this.clickOnPropertyRow.emit(property);
    }

    deleteListOrMapItem  = (itemName: string) => {
        this.propChildren = this.propChildren.filter(prop => prop.propertiesName.indexOf(itemName) != 0); //remove item and children;
    }

    propValueChanged = (property) => {
        console.log("property value change!! Prop type: " + property.type + " New value: " + property.value);
        this.valueChanged.emit(property);
    };

    expandChildById = (id: string) => {
        this.expandedChildId = id;
         this.expandChild.emit(id);
    }

    checkedChange = (propName: string) => {
        this.checkProperty.emit(propName);
    }



    addRows = (): void => { //from within the template, when creating empty item
        let childPropId = this.createNewChildProperty();
        this.expandChildById(this.propPath + "#" + childPropId);
    }

    createNewChildProperty = (value?:string):string => {
        let propUUID:string = UUID.UUID();
        let newProp: DerivedFEProperty;
        if (this.propType == this.derivedPropertyTypes.LIST) { //for list - create new prop of schema type
            newProp = new DerivedFEProperty(propUUID, this.propPath, this.property.schema.property.type, value, true);
        } else { //for map - create new prop of type map, with schema, but with flag that its a child
            newProp = new DerivedFEProperty(propUUID, this.propPath, this.property.type, value, true, this.property.schema);
        }


        this.propChildren = this.propChildren || [];
        this.propChildren.push(newProp);

        //if it's a complex type, add children properties
        if (!this.property.schema.property.isSimpleType) {
            let schemaDataType: DataTypeModel = this.dataTypeService.getDataTypeByTypeName(this.property.schema.property.type);
            this.dataTypeService.getDerivedDataTypeProperties(schemaDataType, this.propChildren, newProp.propertiesName);
            this.propertiesUtils.assignValuesRecursively(JSON.parse(value), this.propChildren, newProp.propertiesName);
            console.log(JSON.stringify(this.propChildren));
        }

        return propUUID;
    }    



    //TODO: remove this and move to somewhere central!! (or make all properties be the same type...)
    getDerivedPropertyType = (type) => {
        if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(type) > -1) {
            return DerivedPropertyType.SIMPLE;
        } else if (type == PROPERTY_TYPES.LIST) {
            return DerivedPropertyType.LIST;
        } else if (type == PROPERTY_TYPES.MAP) {
            return DerivedPropertyType.MAP;
        } else {
            return DerivedPropertyType.COMPLEX;
        }
    }

}
