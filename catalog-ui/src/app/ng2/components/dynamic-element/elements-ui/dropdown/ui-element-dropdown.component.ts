import { Component, EventEmitter, Output, Input } from '@angular/core'
import { BrowserModule } from '@angular/platform-browser'
import { UiElementBase, UiElementBaseInterface } from './../ui-element-base.component';

export class DropdownValue {
  value:string;
  label:string;

  constructor(value:string,label:string) {
    this.value = value;
    this.label = label;
  }
}

@Component({
  selector: 'ui-element-dropdown',
  templateUrl: './ui-element-dropdown.component.html',
  styleUrls: ['./ui-element-dropdown.component.less'],
})
export class UiElementDropDownComponent extends UiElementBase implements UiElementBaseInterface {

  @Input()
  values: DropdownValue[];

  constructor() {
    super();
  }

    onSave() {
        this.baseEmitter.emit(this.value);
    }

}
