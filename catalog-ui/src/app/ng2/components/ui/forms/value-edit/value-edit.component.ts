import { Component, Input } from "@angular/core";

@Component({
    selector: 'value-edit',
    templateUrl: './value-edit.component.html',
    styleUrls: ['./value-edit.component.less']
})
export class ValueEditComponent {

    @Input() name:String;
    @Input() validityChangedCallback: Function;

    private pattern:string = "^[\\s\\w\&_.:-]{1,1024}$"
    constructor(){
    }

    private validityChanged = (value):void => {
        if(this.validityChangedCallback) {
            this.validityChangedCallback(value);
        }
    }



}