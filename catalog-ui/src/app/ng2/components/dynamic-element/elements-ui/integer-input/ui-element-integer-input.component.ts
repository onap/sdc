import {Component, ViewChild, ElementRef, ContentChildren, Input} from '@angular/core';
import { BrowserModule } from '@angular/platform-browser'
import { UiElementBase, UiElementBaseInterface } from './../ui-element-base.component';

@Component({
    selector: 'ui-element-integer-input',
    templateUrl: './ui-element-integer-input.component.html',
    styleUrls: ['./ui-element-integer-input.component.less'],
})
export class UiElementIntegerInputComponent extends UiElementBase implements UiElementBaseInterface {
    constructor() {
        super();
        this.pattern = this.validation.validationPatterns.comment;
    }

    onSave() {
        if (!this.control.invalid){
            this.baseEmitter.emit(JSON.parse(this.value));
        }
    }
}
