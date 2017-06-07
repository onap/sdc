/**
 * Created by rc2122 on 5/4/2017.
 */
import {Component, Input, Output, EventEmitter} from "@angular/core";
import {InputFEModel} from "app/models";

@Component({
    selector: 'inputs-table',
    templateUrl: './inputs-table.component.html',
    styleUrls: ['../properties-table/properties-table.component.less']
})
export class InputsTableComponent {

    @Input() inputs: Array<InputFEModel>;

    @Output() inputValueChanged: EventEmitter<any> = new EventEmitter<any>();
    @Output() deleteInput: EventEmitter<any> = new EventEmitter<any>();

    constructor (){
    }

    onInputValueChanged = (input) => {
        this.inputValueChanged.emit(input);
    };

    onDeleteInput = (input) => {
        this.deleteInput.emit(input);
    }


}


