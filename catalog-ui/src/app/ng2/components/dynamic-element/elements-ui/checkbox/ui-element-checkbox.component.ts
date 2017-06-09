import { Component, ViewChild, ElementRef, ContentChildren, Input } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser'
import { UiElementBase, UiElementBaseInterface } from './../ui-element-base.component';

@Component({
  selector: 'ui-element-checkbox',
  templateUrl: './ui-element-checkbox.component.html',
  styleUrls: ['./ui-element-checkbox.component.less'],
})
export class UiElementCheckBoxComponent extends UiElementBase implements UiElementBaseInterface {

  constructor() {
    super();
  }

  ngAfterContentInit() {
    // Convert the value to boolean (instanceOf does not work, the type is undefined).
    if (this.value==='true' || this.value==='false') {
      this.value = this.value==='true'?true:false;
    }
  }

    onSave() {
        this.baseEmitter.emit(this.value);
    }

}
