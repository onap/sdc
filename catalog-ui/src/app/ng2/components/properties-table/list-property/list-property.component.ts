/**
 * Created by rc2122 on 4/23/2017.
 */
import {Component, Input, Output, EventEmitter} from "@angular/core";
import { PropertyFEModel} from "app/models";
import {PropertiesService} from "app/ng2/services/properties.service";
import { ContentAfterLastDotPipe } from "app/ng2/pipes/contentAfterLastDot.pipe";
import {UUID} from "angular2-uuid";
import {ComponentType} from "app/utils";

@Component({
    selector: 'list-property',
    templateUrl: './list-property.component.html',
    styleUrls: ['../properties-value-inner-table/properties-value-inner-table.component.less', './list-property.component.less']
})
export class ListPropertyComponent {

    @Input() property: PropertyFEModel;
    @Input() selectedPropertyId: string;
    @Input() propertyNameSearchText:string;
    
    @Output() valueChanged: EventEmitter<any> = new EventEmitter<any>();
    @Output() selectChildProperty: EventEmitter<any> = new EventEmitter<PropertyFEModel>();

    constructor ( private propertiesService:PropertiesService, private contentAfterLastDotPipe:ContentAfterLastDotPipe ){
    }

    propValueChanged = () => {
        this.valueChanged.emit(this.property);
    };

    onChildPropertySelected = (property) => {
        this.selectChildProperty.emit(property);
    };

    getNumber = (valueObjectRef: any): Array<any> => {
        let num: number = (valueObjectRef) ? valueObjectRef.length : 0;
        return new Array(num);
    }

    createNewChildProperty = ():void => {
        let newProperty: PropertyFEModel = new PropertyFEModel(this.contentAfterLastDotPipe.transform(this.property.schema.property.type),
            this.property.schema.property.type,
            UUID.UUID(),
            this.property,
            this.property.valueObjectRef[this.property.childrenProperties.length]
        );
        this.propertiesService.createPropertiesTreeForProp(newProperty);
        this.property.childrenProperties.push(newProperty);
    }

    addListItem = ():void => {
        this.property.valueObjectRef = this.property.valueObjectRef || [];
        this.property.childrenProperties = this.property.childrenProperties || [];
        if (this.property.schema.property.isSimpleType){
            if( this.property.valueObjectRef.indexOf("") == -1 ) {//prevent insert multiple empty simple type items to list
                this.property.valueObjectRef.push("");
            }
        }else{
            this.property.valueObjectRef[this.property.childrenProperties.length] = {};
            this.property.childrenProperties = this.property.childrenProperties || [];
            this.createNewChildProperty();
            this.valueChanged.emit(this.property);
        }
    }

    deleteListItem = (indexInList:number):void => {
        this.property.valueObjectRef.splice(indexInList, 1);
        if(this.property.childrenProperties){
            this.property.childrenProperties.splice(indexInList, 1);
        }
        if (!this.property.valueObjectRef.length) {//only when user removes all items from list - put the default
            if ( this.property.defaultValue ) {
                angular.copy(JSON.parse(this.property.defaultValue), this.property.valueObjectRef);
                if (this.property.schema.property.isDataType){
                    _.forEach(this.property.valueObjectRef, () => {
                        this.createNewChildProperty();
                    });
                }
            }
        }
        this.valueChanged.emit(this.property);
    }

}
