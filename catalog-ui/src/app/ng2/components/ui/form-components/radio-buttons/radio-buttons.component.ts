/**
 * Created by rc2122 on 9/4/2017.
 */
import { Component, Input, Output, EventEmitter, ViewEncapsulation } from '@angular/core';
import { RadioButtonModel } from 'app/models'
import {UiElementBaseInterface, UiElementBase} from "../ui-element-base.component";

@Component({
    selector: 'radio-buttons',
    templateUrl: './radio-buttons.component.html',
    styleUrls: ['./radio-button.component.less']
})
export class RadioButtonComponent  extends UiElementBase implements UiElementBaseInterface {
    
    onSave() {
        this.baseEmitter.emit(this.value);
    }

    @Input() options:Array<RadioButtonModel>;
    @Input() readonly:boolean;
    @Input() direction:string = 'vertical'; //get 'horizontal' | 'vertical'
    value:any;
    
    select(value:any) {
        this.value = value;
        this.baseEmitter.emit(this.value);
    }
}

