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

import {Component, Inject, OnInit} from '@angular/core';
import {IStepComponent} from "../../../../../../models/wizard-step";
import {ConnectionWizardService} from "../connection-wizard.service";
import {Component as IComponent} from "../../../../../../models/components/component";
import {ComponentServiceNg2} from "../../../../../services/component-services/component.service";
import {Observable} from "rxjs";
import {Operation} from "../create-interface-operation/model/operation";

@Component({
  selector: 'app-relationship-operations-step',
  templateUrl: './relationship-operations-step.component.html',
  styleUrls: ['./relationship-operations-step.component.less']
})
export class RelationshipOperationsStepComponent implements OnInit, IStepComponent {

  private connectionWizardService: ConnectionWizardService;
  private componentService: ComponentServiceNg2;
  interfaceTypeMap: Map<string, Array<string>>;
  component: IComponent;
  operationList: Array<Operation>;
  operationList$: Observable<Array<Operation>>;
  enableAddOperation: boolean;

  constructor(@Inject('$stateParams') private stateParams,
              connectionWizardService: ConnectionWizardService,
              componentService: ComponentServiceNg2) {
    this.component = stateParams.component;
    this.componentService = componentService;
    this.connectionWizardService = connectionWizardService;
    this.interfaceTypeMap = new Map<string, Array<string>>();
  }

  ngOnInit() {
    this.loadOperationList();
    this.loadInterfaceTypeMap();
  }

  private loadOperationList(): void {
    if (this.connectionWizardService.selectedMatch.operations) {
      this.operationList = this.connectionWizardService.selectedMatch.operations.slice();
    } else {
      this.operationList = new Array<Operation>();
    }
    this.operationList$ = Observable.of(this.operationList);
  }

  private loadInterfaceTypeMap(): void {
    this.componentService.getInterfaceTypes(this.component).subscribe(response => {
      for (const interfaceType in response) {
        let operationList = response[interfaceType];
        //ignore interfaceTypes that doesn't contain operations
        if (operationList && operationList.length > 0) {
          //remove operations already on the list
          const existingOperations =
              this.operationList.filter(operation => operation.interfaceType === interfaceType);
          operationList = operationList
          .filter(operationType => !existingOperations.find(operation => operation.operationType === operationType));
          if (operationList && operationList.length > 0) {
            operationList.sort();
            this.interfaceTypeMap.set(interfaceType, operationList);
          }
        }
      }
    });
  }

  preventBack(): boolean {
    return false;
  }

  preventNext(): boolean {
    return false;
  }

  addOperation() {
    this.enableAddOperation = !this.enableAddOperation;
  }

  operationAdded(operation: Operation) {
    this.enableAddOperation = false;
    if (operation) {
      const foundOperation = this.operationList
      .find(operation1 => operation1.interfaceType === operation.interfaceType
          && operation1.operationType === operation.operationType);
      if (foundOperation) {
        return;
      }
      this.operationList.push(operation);
      this.operationList = this.operationList.slice();
      this.connectionWizardService.selectedMatch.addToOperations(operation);
      this.removeFromInterfaceMap(operation);
    }
  }

  onRemoveOperation(operation: Operation) {
    if (!this.operationList) {
      return;
    }
    const index = this.operationList.indexOf(operation);
    if (index > -1) {
      this.operationList.splice(index, 1);
      this.operationList = this.operationList.slice();
      this.connectionWizardService.selectedMatch.removeFromOperations(operation);
      this.addToInterfaceMap(operation);
    }
  }

  private removeFromInterfaceMap(operation: Operation) {
    if (!this.interfaceTypeMap.has(operation.interfaceType)) {
      return;
    }
    const operationList = this.interfaceTypeMap.get(operation.interfaceType);
    if (!operationList) {
      return;
    }

    const index = operationList.indexOf(operation.operationType);
    if (index > -1) {
      operationList.splice(index, 1);
    }
    if (operationList.length == 0) {
      this.interfaceTypeMap.delete(operation.interfaceType);
    } else {
      this.interfaceTypeMap.set(operation.interfaceType, operationList);
    }
  }

  private addToInterfaceMap(operation: Operation) {
    if (!this.interfaceTypeMap.has(operation.interfaceType)) {
      this.interfaceTypeMap.set(operation.interfaceType, new Array<string>(operation.operationType));
      return;
    }

    const operationList = this.interfaceTypeMap.get(operation.interfaceType);
    if (!operationList) {
      this.interfaceTypeMap.set(operation.interfaceType, new Array<string>(operation.operationType));
      return;
    }
    operationList.push(operation.operationType);
    operationList.sort();
    this.interfaceTypeMap.set(operation.interfaceType, operationList);
  }

}
