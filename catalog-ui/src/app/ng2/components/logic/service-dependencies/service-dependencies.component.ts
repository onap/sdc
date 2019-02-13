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
import {Component, Input, Output, EventEmitter, ComponentRef} from '@angular/core';
import {ModalService} from 'app/ng2/services/modal.service';
import {Service, ComponentInstance, ModalModel, ButtonModel, PropertyBEModel, ServiceInstanceObject} from 'app/models';
import {ServiceDependenciesEditorComponent} from 'app/ng2/pages/service-dependencies-editor/service-dependencies-editor.component';
import {ModalComponent} from 'app/ng2/components/ui/modal/modal.component';
import {ComponentServiceNg2} from 'app/ng2/services/component-services/component.service';
import {TranslateService} from 'app/ng2/shared/translator/translate.service';

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

export class ConstraintObjectUI extends ConstraintObject{
    isValidValue: boolean;

    constructor(input?: any) {
        super(input);
        if(input) {
            this.isValidValue = input.isValidValue ? input.isValidValue : input.value !== '';
        }
    }

    public updateValidity(isValidValue: boolean) {
        this.isValidValue = isValidValue;
    }

    public isValidRule(isStatic) {
        let isValidValue = isStatic ? this.isValidValue : true;
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
            I18nTexts.uncheckModalTitle = translateService.translate("SERVICE_DEPENDENCY_UNCHECK_TITLE");
            I18nTexts.uncheckModalText = translateService.translate("SERVICE_DEPENDENCY_UNCHECK_TEXT");
            I18nTexts.modalApprove = translateService.translate("MODAL_APPROVE");
            I18nTexts.modalCancel = translateService.translate("MODAL_CANCEL");
            I18nTexts.modalCreate = translateService.translate("MODAL_CREATE");
            I18nTexts.modalSave = translateService.translate("MODAL_SAVE");
            I18nTexts.modalDelete = translateService.translate("MODAL_DELETE");
            I18nTexts.addRuleTxt = translateService.translate("SERVICE_DEPENDENCY_ADD_RULE");
            I18nTexts.updateRuleTxt = translateService.translate("SERVICE_DEPENDENCY_UPDATE_RULE");
            I18nTexts.deleteRuleTxt = translateService.translate("SERVICE_DEPENDENCY_DELETE_RULE");
            I18nTexts.deleteRuleMsg = translateService.translate("SERVICE_DEPENDENCY_DELETE_RULE_MSG");
    }
}


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
    compositeServiceProperties: Array<PropertyBEModel> = [];
    rulesList: Array<ConstraintObject> = [];
    operatorTypes: Array<any>;

    @Input() readonly: boolean;
    @Input() compositeService: Service;
    @Input() currentServiceInstance: ComponentInstance;
    @Input() selectedInstanceSiblings: Array<ServiceInstanceObject>;
    @Input() selectedInstanceConstraints: Array<ConstraintObject> = [];
    @Input() selectedInstanceProperties: Array<PropertyBEModel> = [];
    @Output() updateRulesListEvent:EventEmitter<Array<ConstraintObject>> = new EventEmitter<Array<ConstraintObject>>();
    @Output() loadRulesListEvent:EventEmitter<any> = new EventEmitter();
    @Output() dependencyStatus = new EventEmitter<boolean>();


    constructor(private componentServiceNg2: ComponentServiceNg2, private ModalServiceNg2: ModalService, private translateService: TranslateService) {
    }

    ngOnInit() {
        this.isLoading = false;
        this.operatorTypes = [
            {label: ">", value: OPERATOR_TYPES.GREATER_THAN},
            {label: "<", value: OPERATOR_TYPES.LESS_THAN},
            {label: "=", value: OPERATOR_TYPES.EQUAL}
        ];
        this.componentServiceNg2.getServiceProperties(this.compositeService).subscribe((properties: Array<PropertyBEModel>) => {
            this.compositeServiceProperties = properties;
        });
        this.loadRules();
        this.translateService.languageChangedObservable.subscribe(lang => {
            I18nTexts.translateTexts(this.translateService);
        });
    }

    ngOnChanges(changes) {
        if(changes.currentServiceInstance) {
            this.currentServiceInstance = changes.currentServiceInstance.currentValue;
            this.isDependent = this.currentServiceInstance.isDependent();
        }
        if(changes.selectedInstanceConstraints && changes.selectedInstanceConstraints.currentValue !== changes.selectedInstanceConstraints.previousValue) {
            this.selectedInstanceConstraints = changes.selectedInstanceConstraints.currentValue;
            this.loadRules();
        }
    }

    public openRemoveDependencyModal = (): ComponentRef<ModalComponent> => {
        let actionButton: ButtonModel = new ButtonModel(I18nTexts.modalApprove, 'blue', this.onUncheckDependency);
        let cancelButton: ButtonModel = new ButtonModel(I18nTexts.modalCancel, 'grey', this.onCloseRemoveDependencyModal);
        let modalModel: ModalModel = new ModalModel('sm', I18nTexts.uncheckModalTitle, I18nTexts.uncheckModalText, [actionButton, cancelButton]);
        return this.ModalServiceNg2.createCustomModal(modalModel);
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
        this.ModalServiceNg2.closeCurrentModal();
        this.isLoading = true;
        let isDepOrig = this.isDependent;
        let rulesListOrig = this.rulesList;
        this.currentServiceInstance.unmarkAsDependent();
        this.updateComponentInstance(isDepOrig, rulesListOrig);
    }

    onCloseRemoveDependencyModal = () => {
        this.isDependent = true;
        this.ModalServiceNg2.closeCurrentModal();
    }

    onCheckDependency = () => {
        let isDepOrig = this.isDependent;
        let rulesListOrig = this.rulesList;
        this.currentServiceInstance.markAsDependent();
        this.rulesList = [];
        this.updateComponentInstance(isDepOrig, rulesListOrig);
    }

    onMarkAsDependent() {
        if(!this.currentServiceInstance.isDependent()) {
            this.onCheckDependency();
        }
        else {
            this.openRemoveDependencyModal().instance.open();
        }
    }

    updateComponentInstance(isDependent_origVal : boolean, rulesList_orig: Array<ConstraintObject>) {
        this.isLoading = true;
        this.componentServiceNg2.updateComponentInstance(this.compositeService, this.currentServiceInstance).subscribe((updatedServiceIns: ComponentInstance) => {
            this.currentServiceInstance = new ComponentInstance(updatedServiceIns);
            this.isDependent = this.currentServiceInstance.isDependent();
            this.dependencyStatus.emit(this.isDependent);
            if(this.isDependent) {
                this.loadRulesListEvent.emit();
            }
            this.isLoading = false;
        }, err=> {
            this.isDependent = isDependent_origVal;
            this.rulesList = rulesList_orig;
            this.isLoading = false;
            console.log('An error has occurred.');
        });
    }

    onAddRule () {
        let cancelButton: ButtonModel = new ButtonModel(I18nTexts.modalCancel, 'outline white', this.ModalServiceNg2.closeCurrentModal);
        let saveButton: ButtonModel = new ButtonModel(I18nTexts.modalCreate, 'blue', this.createRule, this.getDisabled);
        let modalModel: ModalModel = new ModalModel('l', I18nTexts.addRuleTxt, '', [saveButton, cancelButton], 'standard');
        this.modalInstance = this.ModalServiceNg2.createCustomModal(modalModel);
        this.ModalServiceNg2.addDynamicContentToModal(
            this.modalInstance,
            ServiceDependenciesEditorComponent,
            {
                currentServiceName: this.currentServiceInstance.name,
                operatorTypes: this.operatorTypes,
                compositeServiceName: this.compositeService.name,
                compositeServiceProperties: this.compositeServiceProperties,
                selectedInstanceProperties: this.selectedInstanceProperties,
                selectedInstanceSiblings: this.selectedInstanceSiblings
            }
        );
        this.modalInstance.instance.open();
    }

    onSelectRule(index: number) {
        let cancelButton: ButtonModel = new ButtonModel(I18nTexts.modalCancel, 'outline white', this.ModalServiceNg2.closeCurrentModal);
        let saveButton: ButtonModel = new ButtonModel(I18nTexts.modalSave, 'blue', () => this.updateRules(), this.getDisabled);
        let modalModel: ModalModel = new ModalModel('l', I18nTexts.updateRuleTxt, '', [saveButton, cancelButton], 'standard');
        this.modalInstance = this.ModalServiceNg2.createCustomModal(modalModel);
        this.ModalServiceNg2.addDynamicContentToModal(
            this.modalInstance,
            ServiceDependenciesEditorComponent,
            {
                serviceRuleIndex: index,
                serviceRules: _.map(this.rulesList, rule => new ConstraintObjectUI(rule)),
                currentServiceName: this.currentServiceInstance.name,
                operatorTypes: this.operatorTypes,
                compositeServiceName: this.compositeService.name,
                compositeServiceProperties: this.compositeServiceProperties,
                selectedInstanceProperties: this.selectedInstanceProperties,
                selectedInstanceSiblings: this.selectedInstanceSiblings
            }
        );
        this.modalInstance.instance.open();
    }

    getDisabled = ():boolean =>  {
        return !this.modalInstance.instance.dynamicContent.instance.checkFormValidForSubmit();
    };

    createRule  = ():void => {
        let newRuleToCreate: ConstraintObject = new ConstraintObject(this.modalInstance.instance.dynamicContent.instance.currentRule);
        this.isLoading = true;
        this.componentServiceNg2.createServiceFilterConstraints(
            this.compositeService,
            this.currentServiceInstance,
            newRuleToCreate
        ).subscribe( (response) => {
            this.updateRulesListEvent.emit(response.properties);
            this.isLoading = false;
        }, err=> {
            this.isLoading = false;
        });
        this.ModalServiceNg2.closeCurrentModal();
    };

    updateRules = ():void => {
        let allRulesToUpdate: Array<ConstraintObject> = this.modalInstance.instance.dynamicContent.instance.serviceRulesList.map(rule => new ConstraintObject(rule));
        this.isLoading = true;
        this.componentServiceNg2.updateServiceFilterConstraints(
            this.compositeService,
            this.currentServiceInstance,
            allRulesToUpdate
        ).subscribe((response) => {
            this.updateRulesListEvent.emit(response.properties);
            this.isLoading = false;
        }, err => {
            this.isLoading = false;
        });
        this.ModalServiceNg2.closeCurrentModal();
    }

    getSymbol(constraintOperator) {
        switch (constraintOperator) {
            case OPERATOR_TYPES.LESS_THAN: return '<';
            case OPERATOR_TYPES.EQUAL: return '=';
            case OPERATOR_TYPES.GREATER_THAN: return '>';
        }
    }

    onDeleteRule = (index:number) => {
        this.isLoading = true;
        this.componentServiceNg2.deleteServiceFilterConstraints(
            this.compositeService,
            this.currentServiceInstance,
            index
        ).subscribe( (response) => {
            this.updateRulesListEvent.emit(response.properties);
            this.isLoading = false;
        }, err=> {
            this.isLoading = false;
        });
        this.ModalServiceNg2.closeCurrentModal();
    };

    openDeleteModal = (index:number) => {
        this.ModalServiceNg2.createActionModal(I18nTexts.deleteRuleTxt, I18nTexts.deleteRuleMsg,
            I18nTexts.modalDelete, () => this.onDeleteRule(index), I18nTexts.modalCancel).instance.open();
    }
}