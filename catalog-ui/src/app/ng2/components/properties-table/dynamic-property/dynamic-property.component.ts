import {Component, Input, Output, EventEmitter} from "@angular/core";
import { PropertyBEModel, PropertyFEModel, DerivedFEProperty, DerivedPropertyType, SchemaPropertyGroupModel, DataTypeModel } from "app/models";
import { PROPERTY_DATA, PROPERTY_TYPES } from 'app/utils';
import { PropertiesUtils } from "app/ng2/pages/properties-assignment/properties.utils";
import { DataTypeService } from "../../../services/data-type.service";


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
    childrenCount: number;

    @Input() canBeDeclared: boolean;
    @Input() property: PropertyFEModel | DerivedFEProperty;
    @Input() expandedChildId: string;
    @Input() selectedPropertyId: string;
    @Input() propertyNameSearchText: string;
    @Input() readonly: boolean;

    @Output() valueChanged: EventEmitter<any> = new EventEmitter<any>();
    @Output() expandChild: EventEmitter<string> = new EventEmitter<string>();
    @Output() checkProperty: EventEmitter<string> = new EventEmitter<string>();
    @Output() deleteItem: EventEmitter<string> = new EventEmitter<string>();
    @Output() clickOnPropertyRow: EventEmitter<PropertyFEModel | DerivedFEProperty> = new EventEmitter<PropertyFEModel | DerivedFEProperty>();
    @Output() mapKeyChanged: EventEmitter<string> = new EventEmitter<string>();
    @Output() addChildPropsToParent: EventEmitter<Array<DerivedFEProperty>> = new EventEmitter<Array<DerivedFEProperty>>();


    constructor(private propertiesUtils: PropertiesUtils, private dataTypeService: DataTypeService) {
    }

    ngOnInit() {
        this.isPropertyFEModel = this.property instanceof PropertyFEModel;
        this.propType = this.property.derivedDataType;
        this.propPath = (this.property instanceof PropertyFEModel) ? this.property.name : this.property.propertiesName;
    }


    onClickPropertyRow = (property, event) => {
        // Because DynamicPropertyComponent is recrusive second time the event is fire event.stopPropagation = undefined
        event && event.stopPropagation && event.stopPropagation();
        this.clickOnPropertyRow.emit(property);
    }


    expandChildById = (id: string) => {
        this.expandedChildId = id;
        this.expandChild.emit(id);
    }

    checkedChange = (propName: string) => {
        this.checkProperty.emit(propName);
    }

    hasChildren = (): number => {
        return (this.property.valueObj && typeof this.property.valueObj == 'object') ? Object.keys(this.property.valueObj).length : 0;
    }

    createNewChildProperty = (): void => {
        
        let newProps: Array<DerivedFEProperty> = this.propertiesUtils.createListOrMapChildren(this.property, "", null);
        if (this.property instanceof PropertyFEModel) {
            this.addChildProps(newProps, this.property.name);
        } else {
            this.addChildPropsToParent.emit(newProps);
        }
    }

    addChildProps = (newProps: Array<DerivedFEProperty>, childPropName: string) => {
        
        if (this.property instanceof PropertyFEModel) {
            let insertIndex: number = this.property.getIndexOfChild(childPropName) + this.property.getCountOfChildren(childPropName); //insert after parent prop and existing children 
            this.property.flattenedChildren.splice(insertIndex, 0, ...newProps); //using ES6 spread operator 
            this.expandChildById(newProps[0].propertiesName);
        }
    }

    childValueChanged = (property: DerivedFEProperty) => { //value of child property changed

        if (this.property instanceof PropertyFEModel) { // will always be the case
            let parentNames = this.getParentNamesArray(property.propertiesName, []);
            if (parentNames.length) {
                _.set(this.property.valueObj, parentNames.join('.'), property.valueObj);
            }
            console.log(parentNames);
            this.valueChanged.emit(this.property.name);
        }
    }    

    deleteListOrMapItem = (item: DerivedFEProperty) => {
        if (this.property instanceof PropertyFEModel) {
            this.removeValueFromParent(item);
            this.property.flattenedChildren.splice(this.property.getIndexOfChild(item.propertiesName), this.property.getCountOfChildren(item.propertiesName));
            this.expandChildById(item.propertiesName);
        }
    }

    removeValueFromParent = (item: DerivedFEProperty, replaceKey?: string) => {
        if (this.property instanceof PropertyFEModel) {
            let itemParent = (item.parentName == this.property.name) ? this.property : this.property.flattenedChildren.find(prop => prop.propertiesName == item.parentName);

            if (item.derivedDataType == DerivedPropertyType.MAP) {
                let oldKey = item.mapKey;
                if (typeof replaceKey == 'string') { //allow saving empty string
                    _.set(itemParent.valueObj, replaceKey, itemParent.valueObj[oldKey]);
                    item.mapKey = replaceKey;
                }
                delete itemParent.valueObj[oldKey];
            } else {
                let itemIndex: number = this.property.flattenedChildren.filter(prop => prop.parentName == item.parentName).map(prop => prop.propertiesName).indexOf(item.propertiesName);
                itemParent.valueObj.splice(itemIndex, 1);
            }

            if (itemParent instanceof PropertyFEModel) { //direct child
                this.valueChanged.emit(this.property.name);
            } else { //nested child - need to update parent prop by getting flattened name (recurse through parents and replace map/list keys, etc)
                this.childValueChanged(itemParent);
            }
        }
    }


    getParentNamesArray = (parentPropName: string, parentNames?: Array<string>): Array<string> => {
        if (this.property instanceof PropertyFEModel) {

            if (parentPropName.indexOf("#") == -1) { return parentNames; } //finished recursing parents. return

            let parentProp: DerivedFEProperty = this.property.flattenedChildren.find(prop => prop.propertiesName === parentPropName);
            let nameToInsert: string = parentProp.name;

            if (parentProp.isChildOfListOrMap) {
                if (parentProp.derivedDataType == DerivedPropertyType.MAP) {
                    nameToInsert = parentProp.mapKey;
                } else { //LIST
                    let siblingProps = this.property.flattenedChildren.filter(prop => prop.parentName == parentProp.parentName).map(prop => prop.propertiesName);
                    nameToInsert = siblingProps.indexOf(parentProp.propertiesName).toString();
                }
            }

            parentNames.splice(0, 0, nameToInsert); //add prop name to array
            return this.getParentNamesArray(parentProp.parentName, parentNames); //continue recursing
            
        }
    }


}
