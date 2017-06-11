import {Component, ViewChild, ElementRef, Input} from '@angular/core';
import {UiElementBase, UiElementBaseInterface} from "../ui-element-base.component";
import {ButtonsModelMap, ButtonModel} from "app/models";
import { PopoverContentComponent } from "app/ng2/components/popover/popover-content.component"
import { PopoverComponent } from "app/ng2/components/popover/popover.component"

@Component({
    selector: 'ui-element-popover-input',
    templateUrl: './ui-element-popover-input.component.html',
    styleUrls: ['./ui-element-popover-input.component.less']
})
export class UiElementPopoverInputComponent extends UiElementBase implements UiElementBaseInterface {
    @ViewChild('textArea') textArea: ElementRef;
    @ViewChild('popoverForm') popoverContentComponent: PopoverContentComponent;

    saveButton: ButtonModel;
    buttonsArray: ButtonsModelMap;

    onSave = ():void => {
        if (!this.control.invalid){
            this.baseEmitter.emit(this.value);
            this.popoverContentComponent.hide();
        }
    }

    constructor() {
        super();
        // Create Save button and insert to buttons map
        this.saveButton = new ButtonModel('save', 'blue', this.onSave);
        this.buttonsArray = { 'test': this.saveButton };

        // Define the regex pattern for this controller
        this.pattern = this.validation.validationPatterns.comment;

        // Disable / Enable button according to validation
        //this.control.valueChanges.subscribe(data => this.saveButton.disabled = this.control.invalid);
    }
}
