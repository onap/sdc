import { Component, EventEmitter, Input, Output } from '@angular/core'
import { ValidationConfiguration } from "app/models";
import { FormControl, Validators } from '@angular/forms';

export interface UiElementBaseInterface {
    onSave();
}

@Component({
    template: ``,
    styles: []
})
export class UiElementBase {

    protected validation = ValidationConfiguration.validation;
    protected control: FormControl;

    // Two way binding for value (need to write the "Change" word like this)
    @Output('valueChange') baseEmitter: EventEmitter<string> = new EventEmitter<any>();
    @Input('value') set setValueValue(value) {
        this.value = value;
    }

    protected name: string;
    protected type: string;
    protected value: any;
    protected pattern: any;
    protected readonly:boolean;

    constructor() {
        //this.control = new FormControl('', [Validators.required]);
        this.control = new FormControl('', []);
    }

}
