/*
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
*  Copyright (C) 2021 Nordix Foundation. All rights reserved.
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

import {Component, ComponentRef, Input} from '@angular/core';
import {TopologyTemplateService} from '../../../services/component-services/topology-template.service';
import {TranslateService} from "../../../shared/translator/translate.service";
import {ModalService } from 'app/ng2/services/modal.service';
import { ModalComponent } from 'app/ng2/components/ui/modal/modal.component';
import {
  Component as TopologyTemplate
} from "../../../../models/components/component";
import {PluginsService} from "app/ng2/services/plugins.service";
import {SelectedComponentType} from "../common/store/graph.actions";

import {WorkspaceService} from "../../workspace/workspace.service";
import {
  ComponentInstanceInterfaceModel,
  InterfaceOperationModel
} from "../../../../models/interfaceOperation";
import {
  InterfaceOperationHandlerComponent
} from "./operation-creator/interface-operation-handler.component";

import {
  ButtonModel,
  ComponentMetadata,
  InterfaceModel,
  InputBEModel,
  ModalModel,
  ComponentInstance
} from 'app/models';

export class UIInterfaceOperationModel extends InterfaceOperationModel {
  isCollapsed: boolean = true;
  isEllipsis: boolean;
  MAX_LENGTH = 75;
  _description: string;

  constructor(operation: InterfaceOperationModel) {
    super(operation);

    if (!operation.description) {
      this.description = '';
    }

    if (this.description.length > this.MAX_LENGTH) {
      this.isEllipsis = true;
    } else {
      this.isEllipsis = false;
    }
  }

  getDescriptionEllipsis(): string {
    if (this.isCollapsed && this.description.length > this.MAX_LENGTH) {
      return this.description.substr(0, this.MAX_LENGTH - 3) + '...';
    }
    return this.description;
  }

  toggleCollapsed(e) {
    e.stopPropagation();
    this.isCollapsed = !this.isCollapsed;
  }
}

class ModalTranslation {
  EDIT_TITLE: string;
  CANCEL_BUTTON: string;
  SAVE_BUTTON: string;

  constructor(private TranslateService: TranslateService) {
    this.TranslateService.languageChangedObservable.subscribe(lang => {
      this.EDIT_TITLE = this.TranslateService.translate('INTERFACE_EDIT_TITLE');
      this.CANCEL_BUTTON = this.TranslateService.translate("INTERFACE_CANCEL_BUTTON");
      this.SAVE_BUTTON = this.TranslateService.translate("INTERFACE_SAVE_BUTTON");
    });
  }
}

export class UIInterfaceModel extends ComponentInstanceInterfaceModel {
  isCollapsed: boolean = false;

  constructor(interf?: any) {
    super(interf);
    this.operations = _.map(
        this.operations,
        (operation) => new UIInterfaceOperationModel(operation)
    );
  }

  toggleCollapse() {
    this.isCollapsed = !this.isCollapsed;
  }
}

@Component({
  selector: 'app-interface-operations',
  templateUrl: './interface-operations.component.html',
  styleUrls: ['./interface-operations.component.less'],
  providers: [ModalService, TranslateService]
})
export class InterfaceOperationsComponent {
  interfaces: UIInterfaceModel[];
  selectedOperation: InterfaceOperationModel;
  inputs: Array<InputBEModel>;
  isLoading: boolean;
  interfaceTypes: { [interfaceType: string]: string[] };
  topologyTemplate: TopologyTemplate;
  componentMetaData: ComponentMetadata;
  componentInstanceSelected: ComponentInstance;
  modalInstance: ComponentRef<ModalComponent>;
  modalTranslation: ModalTranslation;
  componentInstancesInterfaces: Map<string, InterfaceModel[]>;

  @Input() component: ComponentInstance;
  @Input() readonly: boolean;
  @Input() enableMenuItems: Function;
  @Input() disableMenuItems: Function;
  @Input() componentType: SelectedComponentType;


  constructor(
      private TranslateService: TranslateService,
      private PluginsService: PluginsService,
      private topologyTemplateService: TopologyTemplateService,
      private modalServiceNg2: ModalService,
      private workspaceService: WorkspaceService,
  ) {
    this.modalTranslation = new ModalTranslation(TranslateService);
  }

  ngOnInit(): void {
    this.componentMetaData = this.workspaceService.metadata;
    this.loadComponentInstances();
  }

  private loadComponentInstances() {
    this.isLoading = true;
    this.topologyTemplateService.getComponentInstances(this.componentMetaData.componentType, this.componentMetaData.uniqueId)
    .subscribe((response) => {
      this.componentInstanceSelected = response.componentInstances.find(ci => ci.uniqueId === this.component.uniqueId);
      this.initComponentInstanceInterfaceOperations();
      this.isLoading = false;
    });
  }

  private initComponentInstanceInterfaceOperations() {
    this.initInterfaces(this.componentInstanceSelected.interfaces);
    this.sortInterfaces();
  }

  private initInterfaces(interfaces: InterfaceModel[]): void {
    this.interfaces = _.map(interfaces, (interfaceModel) => new UIInterfaceModel(interfaceModel));
  }

  private sortInterfaces(): void {
    this.interfaces = _.filter(this.interfaces, (interf) => interf.operations && interf.operations.length > 0); // remove empty interfaces
    this.interfaces.sort((a, b) => a.type.localeCompare(b.type)); // sort interfaces alphabetically
    _.forEach(this.interfaces, (interf) => {
      interf.operations.sort((a, b) => a.name.localeCompare(b.name)); // sort operations alphabetically
    });
  }

  collapseAll(value: boolean = true): void {
    _.forEach(this.interfaces, (interf) => {
      interf.isCollapsed = value;
    });
  }

  isAllCollapsed(): boolean {
    return _.every(this.interfaces, (interf) => interf.isCollapsed);
  }

  isAllExpanded(): boolean {
    return _.every(this.interfaces, (interf) => !interf.isCollapsed);
  }

  isListEmpty(): boolean {
    return _.filter(
        this.interfaces,
        (interf) => interf.operations && interf.operations.length > 0
    ).length === 0;
  }

  private enableOrDisableSaveButton = (): boolean => {
    return !this.modalInstance.instance.dynamicContent.instance.checkFormValidForSubmit();
  }

  onSelectInterfaceOperation(interfaceModel: UIInterfaceModel, operation: InterfaceOperationModel) {
    const cancelButton: ButtonModel = new ButtonModel(this.modalTranslation.CANCEL_BUTTON, 'outline white', this.cancelAndCloseModal);
    const saveButton: ButtonModel = new ButtonModel(this.modalTranslation.SAVE_BUTTON, 'blue', () =>
        this.updateInterfaceOperation(), this.enableOrDisableSaveButton);
    const modalModel: ModalModel = new ModalModel('l', this.modalTranslation.EDIT_TITLE, '', [saveButton, cancelButton], 'custom');
    this.modalInstance = this.modalServiceNg2.createCustomModal(modalModel);

    this.modalServiceNg2.addDynamicContentToModal(
        this.modalInstance,
        InterfaceOperationHandlerComponent,
        {
          selectedInterface: interfaceModel,
          selectedInterfaceOperation: operation,
          validityChangedCallback: this.enableOrDisableSaveButton
        }
    );
    this.modalInstance.instance.open();
  }

  private cancelAndCloseModal = () => {
    this.loadComponentInstances();
    return this.modalServiceNg2.closeCurrentModal();
  }

  private updateInterfaceOperation() {
    this.isLoading = true;
    let operationUpdated = this.modalInstance.instance.dynamicContent.instance.operationToUpdate;
    this.topologyTemplateService.updateComponentInstanceInterfaceOperation(
        this.componentMetaData.uniqueId,
        this.componentMetaData.componentType,
        this.componentInstanceSelected.uniqueId,
        operationUpdated)
    .subscribe((updatedComponentInstance: ComponentInstance) => {
      this.componentInstanceSelected = new ComponentInstance(updatedComponentInstance);
      this.initComponentInstanceInterfaceOperations();
    });
    this.modalServiceNg2.closeCurrentModal();
    this.isLoading = false;
  }

}
