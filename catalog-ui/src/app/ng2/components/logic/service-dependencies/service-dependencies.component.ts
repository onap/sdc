/*!
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import { Component, ComponentRef, EventEmitter, Input, Output } from '@angular/core';
import {
    ButtonModel,
    ComponentInstance,
    InputBEModel,
    ModalModel,
    PropertyBEModel,
} from 'app/models';
import { ModalComponent } from 'app/ng2/components/ui/modal/modal.component';
import { ServiceDependenciesEditorComponent } from 'app/ng2/pages/service-dependencies-editor/service-dependencies-editor.component';
import { ModalService } from 'app/ng2/services/modal.service';
import { ComponentGenericResponse } from 'app/ng2/services/responses/component-generic-response';
import { TranslateService } from 'app/ng2/shared/translator/translate.service';
import { ComponentMetadata } from '../../../../models/component-metadata';
import { ServiceInstanceObject } from '../../../../models/service-instance-properties-and-interfaces';
import { TopologyTemplateService } from '../../../services/component-services/topology-template.service';

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

// tslint:disable-next-line:max-classes-per-file
export class ConstraintObjectUI extends ConstraintObject{
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

// tslint:disable-next-line:max-classes-per-file
class I18nTexts {
    static uncheckModalTitle: string;
    static uncheckModalText: string;
    static modalApprove: string;
    static modalCancel: string;
    static modalCreate: string;
    static modalSave: string;
    static modalDelete: string;
    static addRuleTxt: string;
    static updateRuleTxt: string;
    static deleteRuleTxt: string;
    static deleteRuleMsg: string;

    public static translateTexts(translateService) {
            I18nTexts.uncheckModalTitle = translateService.translate('SERVICE_DEPENDENCY_UNCHECK_TITLE');
            I18nTexts.uncheckModalText = translateService.translate('SERVICE_DEPENDENCY_UNCHECK_TEXT');
            I18nTexts.modalApprove = translateService.translate('MODAL_APPROVE');
            I18nTexts.modalCancel = translateService.translate('MODAL_CANCEL');
            I18nTexts.modalCreate = translateService.translate('MODAL_CREATE');
            I18nTexts.modalSave = translateService.translate('MODAL_SAVE');
            I18nTexts.modalDelete = translateService.translate('MODAL_DELETE');
            I18nTexts.addRuleTxt = translateService.translate('SERVICE_DEPENDENCY_ADD_RULE');
            I18nTexts.updateRuleTxt = translateService.translate('SERVICE_DEPENDENCY_UPDATE_RULE');
            I18nTexts.deleteRuleTxt = translateService.translate('SERVICE_DEPENDENCY_DELETE_RULE');
            I18nTexts.deleteRuleMsg = translateService.translate('SERVICE_DEPENDENCY_DELETE_RULE_MSG');
    }
}

// tslint:disable-next-line:max-classes-per-file
@Component({
    selector: 'service-dependencies',
    templateUrl: './service-dependencies.component.html',
    styleUrls: ['service-dependencies.component.less'],
    providers: [ModalService, TranslateService]
})

export class ServiceDependenciesComponent {
    modalInstance: ComponentRef<ModalComponent>;
    isDependent: boolean;
    isLoading: boolean;
    parentServiceInputs: InputBEModel[] = [];
    rulesList: ConstraintObject[] = [];
    operatorTypes: any[];

    @Input() readonly: boolean;
    @Input() compositeService: ComponentMetadata;
    @Input() currentServiceInstance: ComponentInstance;
    @Input() selectedInstanceSiblings: ServiceInstanceObject[];
    @Input() selectedInstanceConstraints: ConstraintObject[] = [];
    @Input() selectedInstanceProperties: PropertyBEModel[] = [];
    @Output() updateRulesListEvent: EventEmitter<ConstraintObject[]> = new EventEmitter<ConstraintObject[]>();
    @Output() loadRulesListEvent:EventEmitter<any> = new EventEmitter();
    @Output() dependencyStatus = new EventEmitter<boolean>();

    constructor(private topologyTemplateService: TopologyTemplateService, private modalServiceNg2: ModalService, private translateService: TranslateService) {
    }

    ngOnInit() {
        this.isLoading = false;
        this.operatorTypes = [
            {label: '>', value: OPERATOR_TYPES.GREATER_THAN},
            {label: '<', value: OPERATOR_TYPES.LESS_THAN},
            {label: '=', value: OPERATOR_TYPES.EQUAL}
        ];
        this.topologyTemplateService.getComponentInputsWithProperties(this.compositeService.componentType, this.compositeService.uniqueId).subscribe((result: ComponentGenericResponse) => {
            this.parentServiceInputs = result.inputs;
        });
        this.loadRules();
        this.translateService.languageChangedObservable.subscribe((lang) => {
            I18nTexts.translateTexts(this.translateService);
        });
    }

    ngOnChanges(changes) {
        if (changes.currentServiceInstance) {
            this.currentServiceInstance = changes.currentServiceInstance.currentValue;
            this.isDependent = this.currentServiceInstance.isDependent();
        }
        if (changes.selectedInstanceConstraints && changes.selectedInstanceConstraints.currentValue !== changes.selectedInstanceConstraints.previousValue) {
            this.selectedInstanceConstraints = changes.selectedInstanceConstraints.currentValue;
            this.loadRules();
        }
    }

    public openRemoveDependencyModal = (): ComponentRef<ModalComponent> => {
        const actionButton: ButtonModel = new ButtonModel(I18nTexts.modalApprove, 'blue', this.onUncheckDependency);
        const cancelButton: ButtonModel = new ButtonModel(I18nTexts.modalCancel, 'grey', this.onCloseRemoveDependencyModal);
        const modalModel: ModalModel = new ModalModel('sm', I18nTexts.uncheckModalTitle, I18nTexts.uncheckModalText, [actionButton, cancelButton]);
        return this.modalServiceNg2.createCustomModal(modalModel);
    }

    loadRules() {
        this.rulesList = this.selectedInstanceConstraints && this.selectedInstanceConstraints.map((co: ConstraintObject) => ({
                servicePropertyName: co.servicePropertyName,
                constraintOperator: co.constraintOperator,
                sourceType: co.sourceType,
                sourceName: co.sourceName !== 'SELF' ? co.sourceName : this.compositeService.name,
                value: co.value,
            }));
    }

    onUncheckDependency = () => {
        this.modalServiceNg2.closeCurrentModal();
        this.isLoading = true;
        const isDepOrig = this.isDependent;
        const rulesListOrig = this.rulesList;
        this.currentServiceInstance.unmarkAsDependent();
        this.updateComponentInstance(isDepOrig, rulesListOrig);
    }

    onCloseRemoveDependencyModal = () => {
        this.isDependent = true;
        this.modalServiceNg2.closeCurrentModal();
    }

    onCheckDependency = () => {
        const isDepOrig = this.isDependent;
        const rulesListOrig = this.rulesList;
        this.currentServiceInstance.markAsDependent();
        this.rulesList = [];
        this.updateComponentInstance(isDepOrig, rulesListOrig);
    }

    onMarkAsDependent() {
        if (!this.currentServiceInstance.isDependent()) {
            this.onCheckDependency();
        } else {
            this.openRemoveDependencyModal().instance.open();
        }
    }

    updateComponentInstance(isDependentOrigVal: boolean, rulesListOrig: ConstraintObject[]) {
        this.isLoading = true;
        this.topologyTemplateService.updateComponentInstance(this.compositeService.uniqueId, this.currentServiceInstance).subscribe((updatedServiceIns: ComponentInstance) => {
            this.currentServiceInstance = new ComponentInstance(updatedServiceIns);
            this.isDependent = this.currentServiceInstance.isDependent();
            this.dependencyStatus.emit(this.isDependent);
            if (this.isDependent) {
                this.loadRulesListEvent.emit();
            }
            this.isLoading = false;
        }, (err) => {
            this.isDependent = isDependentOrigVal;
            this.rulesList = rulesListOrig;
            this.isLoading = false;
            console.log('An error has occurred.');
        });
    }

    onAddRule() {
        const cancelButton: ButtonModel = new ButtonModel(I18nTexts.modalCancel, 'outline white', this.modalServiceNg2.closeCurrentModal);
        const saveButton: ButtonModel = new ButtonModel(I18nTexts.modalCreate, 'blue', this.createRule, this.getDisabled);
        const modalModel: ModalModel = new ModalModel('l', I18nTexts.addRuleTxt, '', [saveButton, cancelButton], 'standard');
        this.modalInstance = this.modalServiceNg2.createCustomModal(modalModel);
        this.modalServiceNg2.addDynamicContentToModal(
            this.modalInstance,
            ServiceDependenciesEditorComponent,
            {
                currentServiceName: this.currentServiceInstance.name,
                operatorTypes: this.operatorTypes,
                compositeServiceName: this.compositeService.name,
                parentServiceInputs: this.parentServiceInputs,
                selectedInstanceProperties: this.selectedInstanceProperties,
                selectedInstanceSiblings: this.selectedInstanceSiblings
            }
        );
        this.modalInstance.instance.open();
    }

    onSelectRule(index: number) {
        const cancelButton: ButtonModel = new ButtonModel(I18nTexts.modalCancel, 'outline white', this.modalServiceNg2.closeCurrentModal);
        const saveButton: ButtonModel = new ButtonModel(I18nTexts.modalSave, 'blue', () => this.updateRules(), this.getDisabled);
        const modalModel: ModalModel = new ModalModel('l', I18nTexts.updateRuleTxt, '', [saveButton, cancelButton], 'standard');
        this.modalInstance = this.modalServiceNg2.createCustomModal(modalModel);
        this.modalServiceNg2.addDynamicContentToModal(
            this.modalInstance,
            ServiceDependenciesEditorComponent,
            {
                serviceRuleIndex: index,
                serviceRules: _.map(this.rulesList, (rule) => new ConstraintObjectUI(rule)),
                currentServiceName: this.currentServiceInstance.name,
                operatorTypes: this.operatorTypes,
                compositeServiceName: this.compositeService.name,
                parentServiceInputs: this.parentServiceInputs,
                selectedInstanceProperties: this.selectedInstanceProperties,
                selectedInstanceSiblings: this.selectedInstanceSiblings
            }
        );
        this.modalInstance.instance.open();
    }

    getDisabled = (): boolean =>  {
        return !this.modalInstance.instance.dynamicContent.instance.checkFormValidForSubmit();
    }

    createRule  = (): void => {
        const newRuleToCreate: ConstraintObject = new ConstraintObject(this.modalInstance.instance.dynamicContent.instance.currentRule);
        this.isLoading = true;
        this.topologyTemplateService.createServiceFilterConstraints(
            this.compositeService.uniqueId,
            this.currentServiceInstance.uniqueId,
            newRuleToCreate
        ).subscribe( (response) => {
            this.updateRulesListEvent.emit(response.properties);
            this.isLoading = false;
        }, (err) => {
            this.isLoading = false;
        });
        this.modalServiceNg2.closeCurrentModal();
    }

    updateRules = (): void => {
        const allRulesToUpdate: ConstraintObject[] = this.modalInstance.instance.dynamicContent.instance.serviceRulesList.map((rule) => new ConstraintObject(rule));
        this.isLoading = true;
        this.topologyTemplateService.updateServiceFilterConstraints(
            this.compositeService.uniqueId,
            this.currentServiceInstance.uniqueId,
            allRulesToUpdate
        ).subscribe((response) => {
            this.updateRulesListEvent.emit(response.properties);
            this.isLoading = false;
        }, (err) => {
            this.isLoading = false;
        });
        this.modalServiceNg2.closeCurrentModal();
    }

    getSymbol(constraintOperator) {
        switch (constraintOperator) {
            case OPERATOR_TYPES.LESS_THAN: return '<';
            case OPERATOR_TYPES.EQUAL: return '=';
            case OPERATOR_TYPES.GREATER_THAN: return '>';
        }
    }

    onDeleteRule = (index: number) => {
        this.isLoading = true;
        this.topologyTemplateService.deleteServiceFilterConstraints(
            this.compositeService.uniqueId,
            this.currentServiceInstance.uniqueId,
            index
        ).subscribe( (response) => {
            this.updateRulesListEvent.emit(response.properties);
            this.isLoading = false;
        }, (err) => {
            this.isLoading = false;
        });
        this.modalServiceNg2.closeCurrentModal();
    }

    openDeleteModal = (index: number) => {
        this.modalServiceNg2.createActionModal(I18nTexts.deleteRuleTxt, I18nTexts.deleteRuleMsg,
            I18nTexts.modalDelete, () => this.onDeleteRule(index), I18nTexts.modalCancel).instance.open();
    }
}
