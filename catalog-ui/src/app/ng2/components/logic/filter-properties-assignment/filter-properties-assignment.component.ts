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

/**
 * Created by rc2122 on 5/16/2017.
 */
import {Component, Input, Output, EventEmitter, ViewChild} from '@angular/core';
import {ButtonModel, ButtonsModelMap, FilterPropertiesAssignmentData} from "app/models";
import * as sdcConfig from "../../../../../../configurations/dev"
import {PopoverComponent} from "../../ui/popover/popover.component";

@Component({
    selector: 'filter-properties-assignment',
    templateUrl: './filter-properties-assignment.component.html',
    styleUrls: ['./filter-properties-assignment.component.less']
})

export class FilterPropertiesAssignmentComponent {
    @Input() componentType: string;
    @Output() searchProperties: EventEmitter<FilterPropertiesAssignmentData> = new EventEmitter<FilterPropertiesAssignmentData>();
    footerButtons:ButtonsModelMap = {};
    typesOptions:Array<string>;//All optional types
    selectedTypes:Object = {};
    allSelected:boolean = false;//if all option selected
    filterData:FilterPropertiesAssignmentData = new FilterPropertiesAssignmentData();
    @ViewChild('filterPopover') filterPopover: PopoverComponent;

    ngOnInit() {
        this.footerButtons['Apply'] = new ButtonModel('Apply', 'blue', this.search, this.someTypesSelectedAndThereIsPropertyName);
        this.footerButtons['Close'] = new ButtonModel('Close', 'grey', this.close);
        this.componentType = this.componentType.toLocaleLowerCase();
        this.typesOptions = sdcConfig.resourceTypesFilter[this.componentType];
    }

    selectAll = () => {
        _.forEach(this.typesOptions, (type) => {
            this.selectedTypes[type] = this.allSelected;
        });
    }

    onTypeSelected = (type:string) => {
        if(!this.selectedTypes[type]){
            this.allSelected = false;//unselected 'All'
        }
    };

    search = () => {
        console.log('search props');
        this.filterData.selectedTypes = [];
        _.forEach(sdcConfig.resourceTypesFilter[this.componentType], (type) => {
            if(this.selectedTypes[type]){
                this.filterData.selectedTypes.push(type);
            }
        });
        this.searchProperties.emit(this.filterData);
        this.filterPopover.hide();
    }

    close = () => {
        this.filterPopover.hide();
    }

    someTypesSelectedAndThereIsPropertyName = ():boolean => {
        if( _.find(Object.keys(this.selectedTypes),(key) => {
            return this.selectedTypes[key];
        }) && this.filterData.propertyName ){
            return null
        }
        return true;
    }

    clearAll = ():void => {
        this.filterData.propertyName = "";
        _.forEach(this.selectedTypes,(value, key) => {
            this.selectedTypes[key] = false;
        });
        this.allSelected = false;
    }

}
