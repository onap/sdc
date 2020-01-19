import { Component, Input } from "@angular/core";

@Component({
    selector: 'edit-name-modal',
    templateUrl: './edit-name-modal.component.html',
    styleUrls: ['./edit-name-modal.component.less']
})
export class EditNameModalComponent {

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