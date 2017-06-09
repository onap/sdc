/**
 * Created by rc2122 on 4/24/2017.
 */
/**
 * Created by rc2122 on 4/23/2017.
 */
import {Component, Input, Output, EventEmitter} from "@angular/core";
import { PropertyFEModel} from "app/models";
import { PropertiesService } from "../../../services/properties.service";
import {ComponentType} from "app/utils";
import {UUID} from "angular2-uuid";

@Component({
    selector: 'map-property',
    templateUrl: './map-property.component.html',
    styleUrls: ['../properties-value-inner-table/properties-value-inner-table.component.less']
})
export class MapPropertyComponent {

    @Input() property: PropertyFEModel;
    @Input() selectedPropertyId: string;
    @Input() propertyNameSearchText:string;
    
    @Output() valueChanged: EventEmitter<any> = new EventEmitter<any>();
    @Output() selectChildProperty: EventEmitter<any> = new EventEmitter<PropertyFEModel>();

    constructor ( private propertiesService:PropertiesService){
    }

    mapKeys:Array<string>;

    ngOnInit() {
        this.mapKeys = Object.keys(this.property.valueObjectRef);
    }

    propValueChanged = () => {
        this.valueChanged.emit(this.property);
    };

    onChildPropertySelected = (property) => {
        this.selectChildProperty.emit(property);
    };

    getNumber = (num:number):Array<any> => {
        return new Array(num);
    }

    createNewChildProperty = (mapKey:string):void => {

        let newProperty: PropertyFEModel = new PropertyFEModel(mapKey,
            this.property.schema.property.type,
            UUID.UUID(), this.property,
            this.property.valueObjectRef[mapKey]);
        this.propertiesService.createPropertiesTreeForProp(newProperty);
        this.property.childrenProperties = this.property.childrenProperties || [];
        this.property.childrenProperties.push(newProperty);
    }

    //get: new key and the index of this item in the map
    //This method checks if the new key isn't exist already in the map and update the object and the children array with the new key
    changeKeyOfMap = (newKey:string, index:number):void => {
        //let fieldName:string = "mapKey" + this.property.treeNodeId + index;
        let oldKey:string = Object.keys(this.property.valueObjectRef)[index];
        let existsKeyIndex:number = Object.keys(this.property.valueObjectRef).indexOf(newKey);
        if (existsKeyIndex > -1 && existsKeyIndex != index) {
            //error for exists key validation
        } else {
            //remove error for exists key validation and if the form is valid - update the map object
            let newObj = {};
            angular.forEach(this.property.valueObjectRef,function(value:any,key:string){
                if(key == oldKey){
                    newObj[newKey] = value;
                }else{
                    newObj[key] = value;
                }
            });
            this.property.valueObjectRef = newObj;
            this.property.parent.valueObjectRef[this.property.name] = this.property.valueObjectRef;//in order to prevent break ref
            if(this.property.childrenProperties){
                this.property.childrenProperties[index].name = newKey;//update this property childrenProperties with the new key
            }
        }
    }

    //get: index of the item in the map
    //This method removes item from map.
    deleteMapItem = (index:number):void=> {
        delete this.property.valueObjectRef[this.mapKeys[index]];
        this.mapKeys.splice(index, 1);
        if(this.property.childrenProperties){
            this.property.childrenProperties.splice(index, 1);
        }
        if (!this.mapKeys.length) {//only when user removes all pairs of key-value fields - put the default
            if (this.property.defaultValue) {
                angular.copy(JSON.parse(this.property.defaultValue), this.property.valueObjectRef);
                this.mapKeys = Object.keys(this.property.valueObjectRef);
                if (this.property.schema.property.isDataType){
                    angular.forEach(this.property.valueObjectRef, (value, key) => {
                        this.createNewChildProperty(key);
                    }, this);
                }
            }
        }
        this.valueChanged.emit(this.property);
    }

    //This method inserts new empty item to map
    addMapItemFields = ():void => {
        this.property.valueObjectRef = this.property.valueObjectRef || {};
        if (this.property.schema.property.isSimpleType){
            this.property.valueObjectRef[''] = null;
        }else{
            if(!this.property.valueObjectRef['']){
                this.property.valueObjectRef[''] = {};
                this.createNewChildProperty('');
            }
        }
        this.mapKeys = Object.keys(this.property.valueObjectRef);
    }
}

