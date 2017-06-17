import { Component, Input, Output, EventEmitter, SimpleChanges, ViewChild, ElementRef } from "@angular/core";
import {PropertyFEModel, DerivedFEProperty, DerivedPropertyType, InstanceFePropertiesMap} from "app/models";
import {PropertiesService} from "../../services/properties.service";
import { DynamicElementComponent } from 'app/ng2/components/dynamic-element/dynamic-element.component';
import { KeysPipe } from 'app/ng2/pipes/keys.pipe';

@Component({
    selector: 'properties-table',
    templateUrl: './properties-table.component.html',
    styleUrls: ['./properties-table.component.less']
})
export class PropertiesTableComponent {

    @Input() fePropertiesMap: InstanceFePropertiesMap;
    @Input() selectedPropertyId: string;
    @Input() displayDeleteButton: boolean;
    @Input() propertyNameSearchText:string;
    @Input() searchTerm:string;
    @Input() readonly:boolean;
    @Input() isLoading:boolean;
    
    @Output() valueChanged: EventEmitter<any> = new EventEmitter<any>();
    @Output() selectPropertyRow: EventEmitter<PropertyRowSelectedEvent> = new EventEmitter<PropertyRowSelectedEvent>();
    @Output() updateCheckedPropertyCount: EventEmitter<boolean> = new EventEmitter<boolean>();
    //@Output() selectInstanceRow: EventEmitter<string> = new EventEmitter<string>();

    feInstancesNames: Array<string>;

    constructor ( private propertiesService:PropertiesService ){
    }

    /**
     * Update feInstancesNames when fePropertiesMap: InstanceFePropertiesMap change (after getting response from server)
     */
    ngOnChanges(changes: SimpleChanges) {
        if (changes['fePropertiesMap']) {
            if (changes['fePropertiesMap'].currentValue) {
                let keysPipe = new KeysPipe();
                let fiteredArr = keysPipe.transform(changes['fePropertiesMap'].currentValue,[]);
                this.feInstancesNames = fiteredArr;
            }
        }
    }

    propValueChanged = (property) => {
        !property.isDeclared && this.valueChanged.emit(property);
    };

    // Click on main row (row of propertyFEModel)
    onClickPropertyRow = (property:PropertyFEModel, instanceName:string, event?) => {
        //event && event.stopPropagation();
        this.selectedPropertyId = property.name;
        let propertyRowSelectedEvent:PropertyRowSelectedEvent = new PropertyRowSelectedEvent(property, instanceName);
        this.selectPropertyRow.emit(propertyRowSelectedEvent);
    };

    // Click on inner row (row of DerivedFEProperty)
    onClickPropertyInnerRow = (property:DerivedFEProperty, instanceName:string) => {
        let propertyRowSelectedEvent:PropertyRowSelectedEvent = new PropertyRowSelectedEvent(property, instanceName);
        this.selectPropertyRow.emit(propertyRowSelectedEvent);
    }

    propertyChecked = (prop: PropertyFEModel, childPropName?: string) => {
        let isChecked: boolean = (!childPropName)? prop.isSelected : prop.flattenedChildren.find(prop => prop.propertiesName == childPropName).isSelected;

        if (!isChecked) {
            this.propertiesService.undoDisableRelatedProperties(prop, childPropName);
        } else {
            this.propertiesService.disableRelatedProperties(prop, childPropName);
        }
        this.updateCheckedPropertyCount.emit(isChecked);
    }

}

export class PropertyRowSelectedEvent {
    propertyModel:PropertyFEModel | DerivedFEProperty;
    instanceName:string;
    constructor ( propertyModel:PropertyFEModel | DerivedFEProperty, instanceName:string ){
        this.propertyModel = propertyModel;
        this.instanceName = instanceName;
    }
}

