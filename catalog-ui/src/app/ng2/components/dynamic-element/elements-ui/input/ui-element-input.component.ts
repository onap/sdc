import { Component, ViewChild, ElementRef, ContentChildren } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser'
import { UiElementBase, UiElementBaseInterface } from './../ui-element-base.component';

@Component({
    selector: 'ui-element-input',
    templateUrl: './ui-element-input.component.html',
    styleUrls: ['./ui-element-input.component.less'],
})
export class UiElementInputComponent extends UiElementBase implements UiElementBaseInterface {

    constructor() {
        super();
        this.pattern = this.validation.validationPatterns.comment;
    }

    onSave() {
        if (!this.control.invalid){
            this.baseEmitter.emit(this.value);
        }
    }
}
