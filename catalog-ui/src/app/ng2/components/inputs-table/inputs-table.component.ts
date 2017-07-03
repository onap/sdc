/**
 * Created by rc2122 on 5/4/2017.
 */
import {Component, Input, Output, EventEmitter, ViewChild} from "@angular/core";
import {InputFEModel} from "app/models";
import {ConfirmationDeleteInputComponent} from "./confirmation-delete-input/confirmation-delete-input.component";

@Component({
    selector: 'inputs-table',
    templateUrl: './inputs-table.component.html',
    styleUrls: ['../inputs-table/inputs-table.component.less']
})
export class InputsTableComponent {

    @Input() inputs: Array<InputFEModel>;
    @Input() instanceNamesMap: Map<string, string>;
    @Input() readonly:boolean;
    @Input() isLoading:boolean;
    @Output() inputValueChanged: EventEmitter<any> = new EventEmitter<any>();
    @Output() deleteInput: EventEmitter<any> = new EventEmitter<any>();
    @ViewChild ('deleteInputConfirmation') deleteInputConfirmation:ConfirmationDeleteInputComponent;

    selectedInputToDelete:InputFEModel;

    constructor (){
    }

    onInputValueChanged = (input) => {
        this.inputValueChanged.emit(input);
    };

    onDeleteInput = () => {
        this.deleteInput.emit(this.selectedInputToDelete);
    };

    openDeleteModal = (input:InputFEModel) => {
        this.selectedInputToDelete = input;
        this.deleteInputConfirmation.openModal();
    }
}


