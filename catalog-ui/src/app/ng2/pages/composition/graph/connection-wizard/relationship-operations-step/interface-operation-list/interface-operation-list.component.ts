/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {Operation} from "../../create-interface-operation/model/operation";

@Component({
  selector: 'app-interface-operation-list',
  templateUrl: './interface-operation-list.component.html',
  styleUrls: ['./interface-operation-list.component.css']
})
export class InterfaceOperationListComponent implements OnInit {

  @Input('readonly') isReadOnly: boolean;
  @Input() set interfaceOperationList(value: Array<Operation>) {
    this.interfaceOperationList$.next(value);
  }
  @Output('onRemoveOperation') onRemoveOperationEmitter: EventEmitter<Operation> = new EventEmitter<Operation>();

  interfaceOperationList$: BehaviorSubject<Array<Operation>>;
  interfaceTypeMap: Map<string, Array<Operation>>;
  expandCollapseControlMap: Map<string, boolean>;

  constructor() {
    this.interfaceOperationList$ = new BehaviorSubject<Array<Operation>>(new Array<Operation>());
    this.expandCollapseControlMap = new Map<string, boolean>();
  }

  ngOnInit() {
    this.loadInterfaces();
  }

  private loadInterfaces() {
    this.interfaceOperationList$.subscribe(operationArray => {
      this.interfaceTypeMap = new Map<string, Array<Operation>>();
      operationArray.forEach(operation => {
        if (this.interfaceTypeMap.has(operation.interfaceType)) {
          let operations = this.interfaceTypeMap.get(operation.interfaceType);
          operations.push(operation);
          operations.sort((a, b) => a.operationType.localeCompare(b.operationType));
          this.interfaceTypeMap.set(operation.interfaceType, operations);
        } else {
          this.interfaceTypeMap.set(operation.interfaceType, new Array(operation))
        }
        if (!this.expandCollapseControlMap.has(operation.interfaceType)) {
          this.expandCollapseControlMap.set(operation.interfaceType, true);
        }
      });
    });
  }

  toggleAllExpand() {
    this.toggleAll(true);
  }

  toggleAllCollapse() {
    this.toggleAll(false);
  }

  private toggleAll(toggle: boolean) {
    for (const key of Array.from(this.expandCollapseControlMap.keys())) {
      this.expandCollapseControlMap.set(key, toggle);
    }
  }

  isAllExpanded(): boolean {
    return Array.from(this.expandCollapseControlMap.values()).every(value => value);
  }

  isAllCollapsed(): boolean {
    return Array.from(this.expandCollapseControlMap.values()).every(value => !value);
  }


  onRemoveOperation($event: MouseEvent, operation: any) {
    this.onRemoveOperationEmitter.emit(operation);
  }

  onEditOperation(operation?: any) {

  }

  getKeys(interfaceTypeMap: Map<string, Array<Operation>>) {
    return Array.from(interfaceTypeMap.keys());
  }

  toggleCollapse(interfaceType: string) {
    this.expandCollapseControlMap.set(interfaceType, !this.expandCollapseControlMap.get(interfaceType));
  }

  isInterfaceCollapsed(interfaceType: string): boolean {
    return this.expandCollapseControlMap.get(interfaceType);
  }
}
