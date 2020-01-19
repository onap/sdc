/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import {Component, EventEmitter, Output, Input} from '@angular/core'
import { UiElementBase, UiElementBaseInterface } from './../ui-element-base.component';

export class DropdownValue {
  value:any;
  label:string;
  hidden?: boolean;
  selected?: boolean;


  constructor(value:any,label:string, hidden: boolean = false, selected: boolean = false) {
    this.value = value;
    this.label = label;
    this.hidden = hidden;
    this.selected = selected;
    
    // this.hidden = hidden ? true : "hidden": "";
    // this.selected = selected ? true : "selected": "";
  }


}

@Component({
  selector: 'ui-element-dropdown',
  templateUrl: './ui-element-dropdown.component.html',
  styleUrls: ['./ui-element-dropdown.component.less'],
})
export class UiElementDropDownComponent extends UiElementBase implements UiElementBaseInterface  {
  
  @Input() values: DropdownValue[];

  constructor() {
    super();
  }
}
