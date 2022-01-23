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
import {Component, EventEmitter, Input, Output, ViewEncapsulation} from "@angular/core";
import {ModalService} from "../../../services/modal.service";
import {TranslateService} from "../../../shared/translator/translate.service";
import {NgSelectModule} from "@ng-select/ng-select";
import {TopologyTemplateService} from "../../../services/component-services/topology-template.service";

@Component({
  selector: 'multi-select',
  templateUrl: './multi-select.component.html',
  styleUrls: ['multi-select.component.less'],
  providers: [ModalService, TranslateService, NgSelectModule],
  encapsulation: ViewEncapsulation.None
})

export class MultiSelectComponent {
  isDependent: boolean;
  isLoading: boolean;
  selectedDirectiveOptions: string[];
  directiveOptions: string[];

  @Output() onAddClick = new EventEmitter<string[]>();
  @Input() updateDirectives: string[];

  constructor(private topologyTemplateService: TopologyTemplateService) {
  }

  ngOnInit() {
    this.loadDirectives();
    if (this.updateDirectives) {
      this.selectedDirectiveOptions = this.updateDirectives;
    }
  }

  onAddDirectives() {
    this.onAddClick.emit(this.selectedDirectiveOptions);
  }

  loadDirectives() {
    this.topologyTemplateService.getDirectiveList().subscribe((data: string[]) => {
      this.directiveOptions = data;
    })
  }

  onClearDirectives() {
    this.selectedDirectiveOptions = [];
  }
}