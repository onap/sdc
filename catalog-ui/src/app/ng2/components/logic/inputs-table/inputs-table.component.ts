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
 * Created by rc2122 on 5/4/2017.
 */
import {Component, Input, Output, EventEmitter} from "@angular/core";
import {InputFEModel} from "app/models";
import {ModalService} from "../../../services/modal.service";

@Component({
    selector: 'inputs-table',
    templateUrl: './inputs-table.component.html',
    styleUrls: ['../inputs-table/inputs-table.component.less'],
})
export class InputsTableComponent {

    @Input() inputs: Array<InputFEModel>;
    @Input() instanceNamesMap: Map<string, string>;
    @Input() readonly:boolean;
    @Input() isLoading:boolean;
    @Output() inputValueChanged: EventEmitter<any> = new EventEmitter<any>();
    @Output() deleteInput: EventEmitter<any> = new EventEmitter<any>();

    selectedInputToDelete:InputFEModel;

    constructor(private modalService: ModalService){
    }

    onInputValueChanged = (input) => {
        this.inputValueChanged.emit(input);
    };

    onDeleteInput = () => {
        this.deleteInput.emit(this.selectedInputToDelete);
        this.modalService.closeCurrentModal();
    };

    openDeleteModal = (input:InputFEModel) => {
        this.selectedInputToDelete = input;
        this.modalService.createActionModal("Delete Input", "Are you sure you want to delete this input?", "Delete", this.onDeleteInput, "Close").instance.open();
    }
}


