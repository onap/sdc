/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import { Component, Input, Output, EventEmitter} from "@angular/core";
import {PropertyFEModel, DerivedFEProperty, InstanceFePropertiesMap} from "app/models";
import {PropertiesService} from "../../../services/properties.service";

@Component({
    selector: 'properties-table',
    templateUrl: './properties-table.component.html',
    styleUrls: ['./properties-table.component.less']
})
export class PropertiesTableComponent {

    @Input() fePropertiesMap: InstanceFePropertiesMap;
    @Input() feInstanceNamesMap: Map<string, string>;
    @Input() selectedPropertyId: string;
    @Input() propertyNameSearchText:string;
    @Input() searchTerm:string;
    @Input() readonly:boolean;
    @Input() isLoading:boolean;
    @Input() hasDeclareOption:boolean;
    @Input() hidePropertyType:boolean;
    
    @Output() valueChanged: EventEmitter<any> = new EventEmitter<any>();
    @Output() selectPropertyRow: EventEmitter<PropertyRowSelectedEvent> = new EventEmitter<PropertyRowSelectedEvent>();
    @Output() updateCheckedPropertyCount: EventEmitter<boolean> = new EventEmitter<boolean>();//only for hasDeclareOption

    constructor (private propertiesService:PropertiesService ){

    }
    
    ngOnInit() {
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

