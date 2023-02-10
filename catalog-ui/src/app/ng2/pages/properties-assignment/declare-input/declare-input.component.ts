import {Component, Input, OnInit} from '@angular/core';
import {FormControl, FormGroup} from "@angular/forms";

@Component({
  selector: 'declare-input',
  templateUrl: './declare-input.component.html'
})
export class DeclareInputComponent implements OnInit {
  inputNameForm: FormControl = new FormControl(undefined);
  formGroup: FormGroup = new FormGroup({
    'inputName': this.inputNameForm,
  });
  inputName: string;
  constructor() { }
  ngOnInit() {
  }
  setInputName(event) {
    this.inputName = event.target.value;
    this.inputNameForm.setValue(this.inputName);
  }

}
