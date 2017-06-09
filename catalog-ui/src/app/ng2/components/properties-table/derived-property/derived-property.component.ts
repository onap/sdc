/**
 * Created by rc2122 on 4/20/2017.
 */
import {Component, Input, Output, EventEmitter} from "@angular/core";
import { DerivedFEProperty, DerivedPropertyType} from "app/models";
import {PropertiesService} from "../../../services/properties.service";

@Component({
    selector: 'derived-property',
    templateUrl: './derived-property.component.html',
    styleUrls: ['./derived-property.component.less']
})
export class DerivedPropertyComponent {

    derivedPropertyTypes = DerivedPropertyType; //http://stackoverflow.com/questions/35835984/how-to-use-a-typescript-enum-value-in-an-angular2-ngswitch-statement
    
    @Input() propertyObj: DerivedFEProperty;
    @Input() propertyNameSearchText: string;
    @Input() expanded: boolean;
    @Output() valueChanged: EventEmitter<any> = new EventEmitter<any>();
    @Output() expandChild: EventEmitter<string> = new EventEmitter<string>();
    @Output() selectProperty: EventEmitter<boolean> = new EventEmitter<boolean>();


    constructor ( private propertiesService:PropertiesService){
    }


    propValueChanged = () => {
        this.valueChanged.emit(this.propertyObj);
    };

    expandChildById = (id: string) => {
        this.expandChild.emit(id);
    }

    checkedChange = (isChecked:boolean) => {
        this.selectProperty.emit(isChecked);
    }
    
    addRows = (flatProperty: DerivedFEProperty): void => {
        console.log("ADDING A ROW OF TYPE " + flatProperty.type);
        console.log(flatProperty);
    }

}
