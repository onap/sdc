import {Component, Input, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {DataTypeModel} from "../../../../models/data-types";
import { DEFAULT_MODEL_NAME } from "app/utils/constants";

@Component({
  selector: 'app-type-workspace-general',
  templateUrl: './type-workspace-general.component.html',
  styleUrls: ['./type-workspace-general.component.less']
})
export class TypeWorkspaceGeneralComponent implements OnInit {
  @Input() isViewOnly = true;
  @Input() dataType: DataTypeModel = new DataTypeModel();

  DEFAULT_MODEL_NAME = DEFAULT_MODEL_NAME;

  type: FormControl = new FormControl(undefined, [Validators.required, Validators.minLength(1), Validators.maxLength(300)]);
  derivedFrom: FormControl = new FormControl(undefined, [Validators.required, Validators.minLength(1)]);
  description: FormControl = new FormControl(undefined, [Validators.required, Validators.minLength(1)]);
  model: FormControl = new FormControl(undefined, [Validators.required]);
  formGroup: FormGroup = new FormGroup({
    'type': this.type,
    'description': this.description,
    'model': this.model,
    'derivedFrom': this.derivedFrom
  });

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    if (!this.dataType) {
      return;
    }
    this.type.setValue(this.dataType.name);
    this.description.setValue(this.dataType.description);
    this.model.setValue(this.dataType.model);
    this.derivedFrom.setValue(this.dataType.derivedFrom);
  }
}
