/**
 * Created by rc2122 on 4/20/2017.
 */
import {Component, Input, Output, EventEmitter} from "@angular/core";
import {PropertyFEModel} from "app/models";
import {PropertiesService} from "../../../services/properties.service";

@Component({
    selector: 'properties-value-inner-table',
    templateUrl: './properties-value-inner-table.component.html',
    styleUrls: ['./properties-value-inner-table.component.less']
})
export class PropertiesValueInnerTableComponent {

    @Input() property: PropertyFEModel;
    @Input() selectedPropertyId: string;
    @Input() propertyNameSearchText:string;
    
    @Output() selectChildProperty: EventEmitter<any> = new EventEmitter<PropertyFEModel>();
    @Output() valueChanged: EventEmitter<any> = new EventEmitter<any>();

    constructor ( private propertiesService:PropertiesService){
    }


    onChildPropertySelected = (property) => {
        this.selectChildProperty.emit(property);
    };

    propValueChanged = () => {
        this.valueChanged.emit(this.property);
    };

    putDefaultValueInEmptyChildProperty = (childProp:PropertyFEModel):void => {
        this.property.valueObjectRef[childProp.name] = this.property.valueObjectRef[childProp.name] || childProp.defaultValue;
    }
}
