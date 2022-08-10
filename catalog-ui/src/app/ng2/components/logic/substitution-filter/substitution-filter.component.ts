/*
* ============LICENSE_START=======================================================
*  Copyright (C) 2020 Nordix Foundation. All rights reserved.
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

import {Component, ComponentRef, EventEmitter, Input, Output} from '@angular/core';
import {
  ButtonModel,
  ComponentInstance,
  InputBEModel,
  ModalModel,
  PropertyBEModel,
} from 'app/models';
import {ModalComponent} from 'app/ng2/components/ui/modal/modal.component';
import {ServiceDependenciesEditorComponent} from 'app/ng2/pages/service-dependencies-editor/service-dependencies-editor.component';
import {ModalService} from 'app/ng2/services/modal.service';
import {TranslateService} from 'app/ng2/shared/translator/translate.service';
import {ComponentMetadata} from '../../../../models/component-metadata';
import {TopologyTemplateService} from '../../../services/component-services/topology-template.service';
import {ToscaFilterConstraintType} from "../../../../models/tosca-filter-constraint-type.enum";

export class ConstraintObject {
  servicePropertyName: string;
  constraintOperator: string;
  sourceType: string;
  sourceName: string;
  value: string;

  constructor(input?: any) {
    if (input) {
      this.servicePropertyName = input.servicePropertyName;
      this.constraintOperator = input.constraintOperator;
      this.sourceType = input.sourceType;
      this.sourceName = input.sourceName;
      this.value = input.value;
    }
  }
}

export class ConstraintObjectUI extends ConstraintObject {
  isValidValue: boolean;

  constructor(input?: any) {
    super(input);
    if (input) {
      this.isValidValue = input.isValidValue ? input.isValidValue : input.value !== '';
    }
  }

  public updateValidity(isValidValue: boolean) {
    this.isValidValue = isValidValue;
  }

  public isValidRule(isStatic) {
    const isValidValue = isStatic ? this.isValidValue : true;
    return this.servicePropertyName != null && this.servicePropertyName !== ''
        && this.value != null && this.value !== '' && isValidValue;
  }
}

export const OPERATOR_TYPES = {
  EQUAL: 'equal',
  GREATER_THAN: 'greater_than',
  LESS_THAN: 'less_than'
};

class I18nTexts {
  static addSubstitutionFilterTxt: string;
  static updateSubstitutionFilterTxt: string;
  static deleteSubstitutionFilterTxt: string;
  static deleteSubstitutionFilterMsg: string;
  static modalCancel: string;
  static modalCreate: string;
  static modalSave: string;
  static modalDelete: string;

  public static translateTexts(translateService) {
    I18nTexts.modalCancel = translateService.translate('MODAL_CANCEL');
    I18nTexts.modalCreate = translateService.translate('MODAL_CREATE');
    I18nTexts.modalSave = translateService.translate('MODAL_SAVE');
    I18nTexts.modalDelete = translateService.translate('MODAL_DELETE');

    I18nTexts.addSubstitutionFilterTxt = translateService.translate('ADD_SUBSTITUTION_FILTER');
    I18nTexts.updateSubstitutionFilterTxt = translateService.translate('UPDATE_SUBSTITUTION_FILTER');
    I18nTexts.deleteSubstitutionFilterTxt = translateService.translate('DELETE_SUBSTITUTION_FILTER');
    I18nTexts.deleteSubstitutionFilterMsg = translateService.translate('DELETE_SUBSTITUTION_FILTER_MSG');
  }
}

@Component({
  selector: 'substitution-filter',
  templateUrl: './substitution-filter.component.html',
  styleUrls: ['substitution-filter.component.less'],
  providers: [ModalService, TranslateService]
})

export class SubstitutionFilterComponent {
  modalInstance: ComponentRef<ModalComponent>;
  isLoading: boolean;
  operatorTypes: any[];
  constraintProperties: ConstraintObject[] = [];
  PROPERTIES: string = ToscaFilterConstraintType.PROPERTIES;

  @Input() readonly: boolean;
  @Input() compositeService: ComponentMetadata;
  @Input() currentServiceInstance: ComponentInstance;
  @Input() selectedInstanceConstraints: ConstraintObject[] = [];
  @Input() selectedInstanceProperties: PropertyBEModel[] = [];
  @Input() parentServiceProperties: PropertyBEModel[] = [];
  @Input() parentServiceInputs: InputBEModel[] = [];
  @Output() updateSubstitutionFilterProperties: EventEmitter<ConstraintObject[]> = new EventEmitter<ConstraintObject[]>();
  @Output() updateConstraintListEvent: EventEmitter<ConstraintObject[]> = new EventEmitter<ConstraintObject[]>();
  @Output() loadConstraintListEvent: EventEmitter<any> = new EventEmitter();
  @Output() hasSubstitutionFilter = new EventEmitter<boolean>();

  constructor(private topologyTemplateService: TopologyTemplateService, private modalServiceNg2: ModalService, private translateService: TranslateService) {
  }

  ngOnInit() {
    this.isLoading = false;
    this.operatorTypes = [
      {label: '>', value: OPERATOR_TYPES.GREATER_THAN},
      {label: '<', value: OPERATOR_TYPES.LESS_THAN},
      {label: '=', value: OPERATOR_TYPES.EQUAL}
    ];
    this.loadSubstitutionFilter();
    this.translateService.languageChangedObservable.subscribe((lang) => {
      I18nTexts.translateTexts(this.translateService);
    });
  }

  ngOnChanges(changes) {
    if (changes.compositeService) {
      this.compositeService = changes.compositeService.currentValue;
    }
    if (changes.selectedInstanceConstraints && changes.selectedInstanceConstraints.currentValue !== changes.selectedInstanceConstraints.previousValue) {
      this.selectedInstanceConstraints = changes.selectedInstanceConstraints.currentValue;
      this.loadSubstitutionFilter();
    }
  }

  private loadSubstitutionFilter = (): void => {
    this.topologyTemplateService.getSubstitutionFilterConstraints(this.compositeService.componentType, this.compositeService.uniqueId)
    .subscribe((response) => {
      if(response.substitutionFilters) {
        this.constraintProperties = response.substitutionFilters.properties;
      }
    });
  }

  onAddSubstitutionFilter = (constraintType: string) => {
    const cancelButton: ButtonModel = new ButtonModel(I18nTexts.modalCancel, 'outline white', this.modalServiceNg2.closeCurrentModal);
    const saveButton: ButtonModel = new ButtonModel(I18nTexts.modalCreate, 'blue', () => this.createSubstitutionFilter(constraintType), this.getDisabled);
    const modalModel: ModalModel = new ModalModel('l', I18nTexts.addSubstitutionFilterTxt, '', [saveButton, cancelButton], 'standard');
    this.modalInstance = this.modalServiceNg2.createCustomModal(modalModel);
    this.modalServiceNg2.addDynamicContentToModal(
        this.modalInstance,
        ServiceDependenciesEditorComponent,
        {
          currentServiceName: this.currentServiceInstance.name,
          operatorTypes: this.operatorTypes,
          compositeServiceName: this.compositeService.name,
          parentServiceInputs: this.parentServiceInputs,
          parentServiceProperties: this.parentServiceProperties,
          selectedInstanceProperties: this.parentServiceProperties,
        }
    );
    this.modalInstance.instance.open();
  }

  createSubstitutionFilter = (constraintType: string) => {
    const newSubstitutionFilter: ConstraintObject = new ConstraintObject(this.modalInstance.instance.dynamicContent.instance.currentRule);
    this.isLoading = true;
    this.topologyTemplateService.createSubstitutionFilterConstraints(
        this.compositeService.uniqueId,
        newSubstitutionFilter,
        this.compositeService.componentType,
        constraintType
    ).subscribe((response) => {
      this.emitEventOnChanges(constraintType, response);
      this.isLoading = false;
    }, (err) => {
      console.error(`Failed to Create Substitution Filter on the component with id: ${this.compositeService.uniqueId}`, err);
      this.isLoading = false;
    });
    this.modalServiceNg2.closeCurrentModal();
  }

  onSelectSubstitutionFilter(constraintType: string, index: number) {
    const cancelButton: ButtonModel = new ButtonModel(I18nTexts.modalCancel, 'outline white', this.modalServiceNg2.closeCurrentModal);
    const updateButton: ButtonModel = new ButtonModel(I18nTexts.modalSave, 'blue', () => this.updateSubstitutionFilter(constraintType), this.getDisabled);
    const modalModel: ModalModel = new ModalModel('l', I18nTexts.updateSubstitutionFilterTxt, '', [updateButton, cancelButton], 'standard');
    this.modalInstance = this.modalServiceNg2.createCustomModal(modalModel);
    this.modalServiceNg2.addDynamicContentToModal(
        this.modalInstance,
        ServiceDependenciesEditorComponent,
        {
          serviceRuleIndex: index,
          serviceRules: _.map(this.constraintProperties, (constraint) => new ConstraintObjectUI(constraint)),
          currentServiceName: this.currentServiceInstance.name,
          operatorTypes: this.operatorTypes,
          compositeServiceName: this.compositeService.name,
          parentServiceInputs: this.parentServiceInputs,
          parentServiceProperties: this.parentServiceProperties,
          selectedInstanceProperties: this.parentServiceProperties,
        }
    );
    this.modalInstance.instance.open();
  }

  updateSubstitutionFilter = (constraintType: string): void => {
    const constraintToUpdate: ConstraintObject[] = this.modalInstance.instance.dynamicContent.instance.serviceRulesList.map((rule) => new ConstraintObject(rule));
    this.isLoading = true;
    this.topologyTemplateService.updateSubstitutionFilterConstraints(
        this.compositeService.uniqueId,
        constraintToUpdate,
        this.compositeService.componentType,
        constraintType
    ).subscribe((response) => {
      this.emitEventOnChanges(constraintType, response);
      this.isLoading = false;
    }, () => {
      console.error("Failed to Update Substitution Filter on the component with id: ", this.compositeService.uniqueId);
      this.isLoading = false;
    });
    this.modalServiceNg2.closeCurrentModal();
  }

  onDeleteSubstitutionFilter = (constraintType: string, index: number) => {
    this.isLoading = true;
    this.topologyTemplateService.deleteSubstitutionFilterConstraints(
        this.compositeService.uniqueId,
        index,
        this.compositeService.componentType,
        constraintType
    ).subscribe((response) => {
      this.emitEventOnChanges(constraintType, response);
      this.isLoading = false;
    }, (error) => {
      console.error("Failed to Delete Substitution Filter on the component with id: ",
          this.compositeService.uniqueId, error);
      this.isLoading = false;
    });
    this.modalServiceNg2.closeCurrentModal();
  }

  getDisabled = (): boolean => {
    return !this.modalInstance.instance.dynamicContent.instance.checkFormValidForSubmit();
  }

  getSymbol(constraintOperator) {
    switch (constraintOperator) {
      case OPERATOR_TYPES.LESS_THAN:
        return '<';
      case OPERATOR_TYPES.EQUAL:
        return '=';
      case OPERATOR_TYPES.GREATER_THAN:
        return '>';
    }
  }

  openDeleteModal = (constraintType: string, index: number) => {
    this.modalServiceNg2.createActionModal(I18nTexts.deleteSubstitutionFilterTxt, I18nTexts.deleteSubstitutionFilterMsg,
        I18nTexts.modalDelete, () => this.onDeleteSubstitutionFilter(constraintType, index), I18nTexts.modalCancel).instance.open();
  }

  private emitEventOnChanges(constraintType: string, response) {
      if (ToscaFilterConstraintType.PROPERTIES === constraintType) {
              this.updateSubstitutionFilterProperties.emit(response.properties);
              this.constraintProperties = response.properties;
      }
  }
}
