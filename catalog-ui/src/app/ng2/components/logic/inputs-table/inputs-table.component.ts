/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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
 * Created by rc2122 on 5/4/2017.
 */
import { Component, Input, Output, EventEmitter } from "@angular/core";
import { InputFEModel } from "app/models";
import { ModalService } from "../../../services/modal.service";
import { InstanceFeDetails } from "app/models/instance-fe-details";
import { InstanceFePropertiesMap } from "../../../../models/properties-inputs/property-fe-map";
import { DataTypeService } from "../../../services/data-type.service";
import { DropdownValue } from "../../ui/form-components/dropdown/ui-element-dropdown.component";

@Component({
    selector: 'inputs-table',
    templateUrl: './inputs-table.component.html',
    styleUrls: ['../inputs-table/inputs-table.component.less'],
})
export class InputsTableComponent {

    @Input() inputs: Array<InputFEModel>;
    @Input() instanceNamesMap: Map<string, InstanceFeDetails>;
    @Input() readonly: boolean;
    @Input() isLoading: boolean;
    @Input() componentType: string;
    @Output() inputChanged: EventEmitter<any> = new EventEmitter<any>();
    @Output() deleteInput: EventEmitter<any> = new EventEmitter<any>();

    @Input() fePropertiesMap: InstanceFePropertiesMap;
    
    sortBy: String;
    reverse: boolean;
    selectedInputToDelete: InputFEModel;
    valuesForRequired = [
        new DropdownValue(false,'FALSE'),
        new DropdownValue(true,'TRUE')
    ];

    sort = (sortBy) => {
        this.reverse = (this.sortBy === sortBy) ? !this.reverse : true;
        let reverse = this.reverse ? 1 : -1;
        this.sortBy = sortBy;
        let instanceNameMapTemp = this.instanceNamesMap;
        let itemIdx1Val = "";
        let itemIdx2Val = "";
        this.inputs.sort(function (itemIdx1, itemIdx2) {
            if (sortBy == 'instanceUniqueId') {
                itemIdx1Val = (itemIdx1[sortBy] && instanceNameMapTemp[itemIdx1[sortBy]] !== undefined) ? instanceNameMapTemp[itemIdx1[sortBy]].name : "";
                itemIdx2Val = (itemIdx2[sortBy] && instanceNameMapTemp[itemIdx2[sortBy]] !== undefined) ? instanceNameMapTemp[itemIdx2[sortBy]].name : "";
            }
            else {
                itemIdx1Val = itemIdx1[sortBy];
                itemIdx2Val = itemIdx2[sortBy];
            }            
            if (itemIdx1Val < itemIdx2Val) {
                return -1 * reverse;
            }
            else if (itemIdx1Val > itemIdx2Val) {
                return 1 * reverse;
            }
            else {
                return 0;
            }
        });
    };


    constructor(private modalService: ModalService, private dataTypeService: DataTypeService){
        var x = 5
    }


    onInputChanged = (input, event) => {
        input.updateDefaultValueObj(event.value, event.isValid);
        this.inputChanged.emit(input);
    };

    onRequiredChanged = (input: InputFEModel, event) => {
        this.inputChanged.emit(input);
    }

    onDeleteInput = () => {
        this.deleteInput.emit(this.selectedInputToDelete);
        this.modalService.closeCurrentModal();
    };

    openDeleteModal = (input: InputFEModel) => {
        console.log('exist inputs: ' + this.inputs)
        
        
        this.selectedInputToDelete = input;
        this.modalService.createActionModal("Delete Input", "Are you sure you want to delete this input?", "Delete", this.onDeleteInput, "Close").instance.open();
    }

    getConstraints(input:InputFEModel): string[]{
        
        if (input.inputPath){
            const pathValuesName = input.inputPath.split('#');
            const rootPropertyName = pathValuesName[0];
            const propertyName = pathValuesName[1];
            let filterredRootPropertyType = _.values(this.fePropertiesMap)[0].filter(property => 
                property.name == rootPropertyName);
            if (filterredRootPropertyType.length > 0){
                let rootPropertyType = filterredRootPropertyType[0].type;
                return this.dataTypeService.getConstraintsByParentTypeAndUniqueID(rootPropertyType, propertyName);
            }else{
                return null;
            }
                
        }
        // else if(input.constraints.length > 0){
        //     return input.constraints[0].validValues
        // }
        else{
            return null;
        }
    }

    checkInstanceFePropertiesMapIsFilled(){
        return _.keys(this.fePropertiesMap).length > 0
    }
}


