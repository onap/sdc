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
import {Component, ComponentRef, EventEmitter, Input, Output} from '@angular/core';
import {ButtonModel, ComponentInstance, InputBEModel, ModalModel, PropertyBEModel, PropertyModel,} from 'app/models';
import {ModalComponent} from 'app/ng2/components/ui/modal/modal.component';
import {ServiceDependenciesEditorComponent} from 'app/ng2/pages/service-dependencies-editor/service-dependencies-editor.component';
import {ModalService} from 'app/ng2/services/modal.service';
import {ComponentGenericResponse} from 'app/ng2/services/responses/component-generic-response';
import {TranslateService} from 'app/ng2/shared/translator/translate.service';
import {ComponentMetadata} from '../../../../models/component-metadata';
import {ServiceInstanceObject} from '../../../../models/service-instance-properties-and-interfaces';
import {TopologyTemplateService} from '../../../services/component-services/topology-template.service';
import {
    CapabilitiesFilterPropertiesEditorComponent
} from "../../../pages/composition/capabilities-filter-properties-editor/capabilities-filter-properties-editor.component";
import {CapabilitiesConstraintObjectUI} from "../capabilities-constraint/capabilities-constraint.component";
import {ToscaFilterConstraintType} from "../../../../models/tosca-filter-constraint-type.enum";
import {CompositionService} from "../../../pages/composition/composition.service";

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

    public isValidRule() {
        const isValidValue = this.isStatic() ? this.isValidValue : true;
        return this.servicePropertyName != null && this.servicePropertyName !== ''
            && this.value != null && this.value !== '' && isValidValue;
    }

    private isStatic() {
        return this.sourceName === 'static';
    }
}

export const OPERATOR_TYPES = {
    EQUAL: 'equal',
    GREATER_THAN: 'greater_than',
    LESS_THAN: 'less_than',
    GREATER_OR_EQUAL: 'greater_or_equal',
    LESS_OR_EQUAL: 'less_or_equal'
};

export enum SourceType {
    STATIC = 'static',
    TOSCA_FUNCTION = 'tosca_function'
}

// tslint:disable-next-line:max-classes-per-file
class I18nTexts {
    static removeDirectiveModalTitle: string;
    static removeDirectiveModalText: string;
    static updateDirectiveModalTitle: string;
    static updateDirectiveModalText: string;
    static modalApprove: string;
    static modalCancel: string;
    static modalCreate: string;
    static modalSave: string;
    static modalDelete: string;
    static addNodeFilterTxt: string;
    static updateNodeFilterTxt: string;
    static deleteNodeFilterTxt: string;
    static deleteNodeFilterMsg: string;
    static validateCapabilitiesTxt: string
    static validateCapabilitiesMsg: string
    static validateNodePropertiesTxt: string
    static validateNodePropertiesMsg: string

    public static translateTexts(translateService) {
            I18nTexts.removeDirectiveModalTitle = translateService.translate('DIRECTIVES_AND_NODE_FILTER_REMOVE_TITLE');
            I18nTexts.removeDirectiveModalText = translateService.translate('DIRECTIVES_AND_NODE_FILTER_REMOVE_TEXT');
            I18nTexts.updateDirectiveModalTitle = translateService.translate('DIRECTIVES_AND_NODE_FILTER_UPDATE_TITLE');
            I18nTexts.updateDirectiveModalText = translateService.translate('DIRECTIVES_AND_NODE_FILTER_UPDATE_TEXT');
            I18nTexts.modalApprove = translateService.translate('MODAL_APPROVE');
            I18nTexts.modalCancel = translateService.translate('MODAL_CANCEL');
            I18nTexts.modalCreate = translateService.translate('MODAL_CREATE');
            I18nTexts.modalSave = translateService.translate('MODAL_SAVE');
            I18nTexts.modalDelete = translateService.translate('MODAL_DELETE');
            I18nTexts.addNodeFilterTxt = translateService.translate('DIRECTIVES_AND_NODE_FILTER_ADD_NODE_FILTER');
            I18nTexts.updateNodeFilterTxt = translateService.translate('DIRECTIVES_AND_NODE_FILTER_UPDATE_NODE_FILTER');
            I18nTexts.deleteNodeFilterTxt = translateService.translate('DIRECTIVES_AND_NODE_FILTER_DELETE_NODE_FILTER');
            I18nTexts.deleteNodeFilterMsg = translateService.translate('DIRECTIVES_AND_NODE_FILTER_DELETE_NODE_FILTER_MSG');
            I18nTexts.validateCapabilitiesTxt = translateService.translate('VALIDATE_CAPABILITIES_TXT');
            I18nTexts.validateCapabilitiesMsg = translateService.translate('VALIDATE_CAPABILITIES_MSG');
            I18nTexts.validateNodePropertiesTxt = translateService.translate('VALIDATE_NODE_PROPERTIES_TXT');
            I18nTexts.validateNodePropertiesMsg = translateService.translate('VALIDATE_NODE_PROPERTIES_MSG');
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
    parentServiceProperties: PropertyBEModel[] = [];
    constraintProperties: ConstraintObject[] = [];
    constraintCapabilities: CapabilitiesConstraintObjectUI[] = [];
    operatorTypes: any[];
    capabilities: string = ToscaFilterConstraintType.CAPABILITIES;
    properties: string = ToscaFilterConstraintType.PROPERTIES;
    private componentInstancesConstraints: ConstraintObject[] = [];
    isEditable: boolean;

    @Input() readonly: boolean;
    @Input() compositeService: ComponentMetadata;
    @Input() currentServiceInstance: ComponentInstance;
    @Input() selectedInstanceSiblings: ServiceInstanceObject[];
    @Input() selectedInstanceConstraints: ConstraintObject[] = [];
    @Input() selectedInstanceProperties: PropertyBEModel[] = [];
    @Output() updateRulesListEvent: EventEmitter<ConstraintObject[]> = new EventEmitter<ConstraintObject[]>();
    @Output() updateNodeFilterProperties: EventEmitter<ConstraintObject[]> = new EventEmitter<ConstraintObject[]>();
    @Output() updateNodeFilterCapabilities: EventEmitter<CapabilitiesConstraintObjectUI[]> = new EventEmitter<CapabilitiesConstraintObjectUI[]>();
    @Output() loadRulesListEvent:EventEmitter<any> = new EventEmitter();
    @Output() dependencyStatus = new EventEmitter<boolean>();

    @Input() componentInstanceCapabilitiesMap: Map<string, PropertyModel[]>;

    constructor(private topologyTemplateService: TopologyTemplateService,
                private modalServiceNg2: ModalService,
                private translateService: TranslateService,
                private compositionService: CompositionService) {
    }

    ngOnInit() {
        this.isLoading = false;
        this.operatorTypes = [
            {label: '>', value: OPERATOR_TYPES.GREATER_THAN},
            {label: '<', value: OPERATOR_TYPES.LESS_THAN},
            {label: '=', value: OPERATOR_TYPES.EQUAL},
            {label: '>=', value: OPERATOR_TYPES.GREATER_OR_EQUAL},
            {label: '<=', value: OPERATOR_TYPES.LESS_OR_EQUAL}
        ];
        this.topologyTemplateService.getComponentInputsWithProperties(this.compositeService.componentType, this.compositeService.uniqueId)
        .subscribe((result: ComponentGenericResponse) => {
            this.parentServiceInputs = result.inputs;
            this.parentServiceProperties = result.properties;
        });
        this.loadNodeFilter();
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
            this.loadNodeFilter();
        }
    }

    private getActualDirectiveValue = (): string[] => {
        return this.currentServiceInstance.directives.length > 0 ? this.currentServiceInstance.directives : [];
    }

    public openRemoveDependencyModal = (): ComponentRef<ModalComponent> => {
        const actionButton: ButtonModel = new ButtonModel(I18nTexts.modalApprove, 'blue', this.onUncheckDependency);
        const cancelButton: ButtonModel = new ButtonModel(I18nTexts.modalCancel, 'grey', this.onCloseRemoveDependencyModal);
        const modalModel: ModalModel = new ModalModel('sm', I18nTexts.removeDirectiveModalTitle,
            I18nTexts.removeDirectiveModalText, [actionButton, cancelButton]);
        this.loadNodeFilter();
        return this.modalServiceNg2.createCustomModal(modalModel);
    }

    public openUpdateDependencyModal = (): ComponentRef<ModalComponent> => {
        const actionButton: ButtonModel = new ButtonModel(I18nTexts.modalApprove, 'blue', this.onUncheckDependency);
        const cancelButton: ButtonModel = new ButtonModel(I18nTexts.modalCancel, 'grey', this.onCloseRemoveDependencyModal);
        const modalModel: ModalModel = new ModalModel('sm', I18nTexts.updateDirectiveModalTitle,
            I18nTexts.updateDirectiveModalText, [actionButton, cancelButton]);
        return this.modalServiceNg2.createCustomModal(modalModel);
    }

    private loadNodeFilter = (): void => {
        this.topologyTemplateService.getServiceFilterConstraints(this.compositeService.componentType, this.compositeService.uniqueId).subscribe((response) => {
            if (response.nodeFilterforNode && response.nodeFilterforNode[this.currentServiceInstance.uniqueId]) {
                this.componentInstancesConstraints = response.nodeFilterforNode;
                this.constraintProperties = response.nodeFilterforNode[this.currentServiceInstance.uniqueId].properties;
                this.constraintCapabilities = response.nodeFilterforNode[this.currentServiceInstance.uniqueId].capabilities;
            }
        });
    }

    onUncheckDependency = () => {
        this.modalServiceNg2.closeCurrentModal();
        this.isLoading = true;
        const isDepOrig = this.isDependent;
        const rulesListOrig = this.componentInstancesConstraints;
        this.currentServiceInstance.unmarkAsDependent(this.getActualDirectiveValue());
        this.updateComponentInstance(isDepOrig, rulesListOrig);
    }

    onCloseRemoveDependencyModal = () => {
        this.isDependent = true;
        this.modalServiceNg2.closeCurrentModal();
    }

    onAddDirectives(directives: string[]) {
        this.isEditable = false;
        this.setDirectiveValue(directives);
        const rulesListOrig = this.componentInstancesConstraints;
        this.constraintProperties = [];
        this.constraintCapabilities = [];
        this.loadNodeFilter();
        this.updateComponentInstance(this.isDependent, rulesListOrig);
    }

    private onRemoveDirective() {
        this.openRemoveDependencyModal().instance.open();
        this.constraintProperties = [];
        this.constraintCapabilities = [];
    }

    private onEditDirectives() {
        this.isEditable = true;
    }

    private setDirectiveValue(newDirectiveValues: string[]) {
        this.currentServiceInstance.setDirectiveValue(newDirectiveValues);
    }

    updateComponentInstance(isDependentOrigVal: boolean, rulesListOrig: ConstraintObject[]) {
        this.isLoading = true;
        this.topologyTemplateService.updateComponentInstance(this.compositeService.uniqueId,
                                                             this.compositeService.componentType,
                                                             this.currentServiceInstance)
                                                             .subscribe((updatedServiceIns: ComponentInstance) => {
            const selectedComponentInstance = this.compositionService.getComponentInstances()
            .find(componentInstance => componentInstance.uniqueId == this.currentServiceInstance.uniqueId);
            selectedComponentInstance.directives = updatedServiceIns.directives;
            this.currentServiceInstance = new ComponentInstance(updatedServiceIns);
            this.isDependent = this.currentServiceInstance.isDependent();
            this.dependencyStatus.emit(this.isDependent);
            if (this.isDependent) {
                this.loadRulesListEvent.emit();
            }
            this.isLoading = false;
        }, (err) => {
            this.isDependent = isDependentOrigVal;
            this.componentInstancesConstraints = rulesListOrig;
            this.isLoading = false;
            console.log('An error has occurred.');
        });
    }

    onAddNodeFilter = () => {
        if (!this.selectedInstanceProperties) {
            this.modalServiceNg2.openAlertModal(I18nTexts.validateNodePropertiesTxt, I18nTexts.validateNodePropertiesMsg);
        } else {
            const cancelButton: ButtonModel = new ButtonModel(I18nTexts.modalCancel, 'outline white', this.modalServiceNg2.closeCurrentModal);
            const saveButton: ButtonModel = new ButtonModel(I18nTexts.modalCreate, 'blue', () => this.createNodeFilter(this.properties), this.getDisabled);
            const modalModel: ModalModel = new ModalModel('l', I18nTexts.addNodeFilterTxt, '', [saveButton, cancelButton], 'standard');
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
                    selectedInstanceProperties: this.selectedInstanceProperties,
                    selectedInstanceSiblings: this.selectedInstanceSiblings
                }
            );
            this.modalInstance.instance.open();
        }
    }

    onAddNodeFilterCapabilities = () => {
        if (this.componentInstanceCapabilitiesMap.size == 0) {
            this.modalServiceNg2.openAlertModal(I18nTexts.validateCapabilitiesTxt, I18nTexts.validateCapabilitiesMsg);
        } else {
            const cancelButton: ButtonModel = new ButtonModel(I18nTexts.modalCancel, 'outline white', this.modalServiceNg2.closeCurrentModal);
            const saveButton: ButtonModel = new ButtonModel(I18nTexts.modalCreate, 'blue', () => this.createNodeFilterCapabilities(this.capabilities), this.getDisabled);
            const modalModel: ModalModel = new ModalModel('l', I18nTexts.addNodeFilterTxt, '', [saveButton, cancelButton], 'standard');
            this.modalInstance = this.modalServiceNg2.createCustomModal(modalModel);
            this.modalServiceNg2.addDynamicContentToModal(
                this.modalInstance,
                CapabilitiesFilterPropertiesEditorComponent,
                {
                    currentServiceName: this.currentServiceInstance.name,
                    operatorTypes: this.operatorTypes,
                    compositeServiceName: this.compositeService.name,
                    parentServiceInputs: this.parentServiceInputs,
                    selectedInstanceProperties: this.selectedInstanceProperties,
                    selectedInstanceSiblings: this.selectedInstanceSiblings,
                    componentInstanceCapabilitiesMap: this.componentInstanceCapabilitiesMap
                }
            );
            this.modalInstance.instance.open();
        }
    }

    createNodeFilter = (constraintType: string) => {
        this.isLoading = true;
        this.topologyTemplateService.createServiceFilterConstraints(
            this.compositeService.uniqueId,
            this.currentServiceInstance.uniqueId,
            new ConstraintObject(this.modalInstance.instance.dynamicContent.instance.currentRule),
            this.compositeService.componentType,
            constraintType
        ).subscribe( (response) => {
            this.emitEventOnChanges(constraintType, response);
            this.isLoading = false;
        }, (err) => {
            this.isLoading = false;
        });
        this.modalServiceNg2.closeCurrentModal();
    }

    createNodeFilterCapabilities = (constraintType: string) => {
        this.isLoading = true;
        this.topologyTemplateService.createServiceFilterCapabilitiesConstraints(
            this.compositeService.uniqueId,
            this.currentServiceInstance.uniqueId,
            new CapabilitiesConstraintObjectUI(this.modalInstance.instance.dynamicContent.instance.currentRule),
            this.compositeService.componentType,
            constraintType
        ).subscribe( (response) => {
            this.emitEventOnChanges(constraintType, response);
            this.isLoading = false;
        }, (err) => {
            this.isLoading = false;
        });
        this.modalServiceNg2.closeCurrentModal();
    }

    onSelectNodeFilterCapability(constraintType: string, index: number) {
        const cancelButton: ButtonModel = new ButtonModel(I18nTexts.modalCancel, 'outline white', this.modalServiceNg2.closeCurrentModal);
        const saveButton: ButtonModel = new ButtonModel(I18nTexts.modalSave, 'blue', () => this.updateNodeFilterCapability(constraintType, index), this.getDisabled);
        const modalModel: ModalModel = new ModalModel('l', I18nTexts.updateNodeFilterTxt, '', [saveButton, cancelButton], 'standard');
        this.modalInstance = this.modalServiceNg2.createCustomModal(modalModel);

        this.modalServiceNg2.addDynamicContentToModal(
            this.modalInstance,
            CapabilitiesFilterPropertiesEditorComponent,
            {
                serviceRuleIndex: index,
                serviceRules: _.map(this.constraintCapabilities, (rule) => new CapabilitiesConstraintObjectUI(rule)),
                currentServiceName: this.currentServiceInstance.name,
                operatorTypes: this.operatorTypes,
                compositeServiceName: this.compositeService.name,
                parentServiceInputs: this.parentServiceInputs,
                selectedInstanceProperties: this.selectedInstanceProperties,
                selectedInstanceSiblings: this.selectedInstanceSiblings,
                componentInstanceCapabilitiesMap: this.componentInstanceCapabilitiesMap
            }
        );
        this.modalInstance.instance.open();
    }

    onSelectNodeFilter(constraintType: string, index: number) {
        const cancelButton: ButtonModel = new ButtonModel(I18nTexts.modalCancel, 'outline white', this.modalServiceNg2.closeCurrentModal);
        const saveButton: ButtonModel = new ButtonModel(I18nTexts.modalSave, 'blue', () => this.updateNodeFilter(constraintType, index), this.getDisabled);
        const modalModel: ModalModel = new ModalModel('l', I18nTexts.updateNodeFilterTxt, '', [saveButton, cancelButton], 'standard');
        this.modalInstance = this.modalServiceNg2.createCustomModal(modalModel);
        this.modalServiceNg2.addDynamicContentToModal(
            this.modalInstance,
            ServiceDependenciesEditorComponent,
            {
                serviceRuleIndex: index,
                serviceRules: this.constraintProperties.map(rule => new ConstraintObjectUI(rule)),
                currentServiceName: this.currentServiceInstance.name,
                operatorTypes: this.operatorTypes,
                compositeServiceName: this.compositeService.name,
                parentServiceInputs: this.parentServiceInputs,
                parentServiceProperties: this.parentServiceProperties,
                selectedInstanceProperties: this.selectedInstanceProperties,
                selectedInstanceSiblings: this.selectedInstanceSiblings
            }
        );
        this.modalInstance.instance.open();
    }

    getDisabled = (): boolean =>  {
        return !this.modalInstance.instance.dynamicContent.instance.checkFormValidForSubmit();
    }

    updateNodeFilter = (constraintType: string, index: number) => {
        this.isLoading = true;
        this.topologyTemplateService.updateServiceFilterConstraints(
            this.compositeService.uniqueId,
            this.currentServiceInstance.uniqueId,
            new ConstraintObject(this.modalInstance.instance.dynamicContent.instance.currentRule),
            this.compositeService.componentType,
            constraintType,
            index
        ).subscribe((response) => {
            this.emitEventOnChanges(constraintType, response);
            this.isLoading = false;
        }, (err) => {
            this.isLoading = false;
        });
        this.modalServiceNg2.closeCurrentModal();
    }

    updateNodeFilterCapability= (constraintType: string, index: number) => {
        this.isLoading = true;
        this.topologyTemplateService.updateServiceFilterCapabilitiesConstraint(
            this.compositeService.uniqueId,
            this.currentServiceInstance.uniqueId,
            new CapabilitiesConstraintObjectUI(this.modalInstance.instance.dynamicContent.instance.currentRule),
            this.compositeService.componentType,
            constraintType,
            index
        ).subscribe((response) => {
            this.emitEventOnChanges(constraintType, response);
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
            case OPERATOR_TYPES.GREATER_OR_EQUAL: return '>=';
            case OPERATOR_TYPES.LESS_OR_EQUAL: return '<=';
        }
    }

    onDeleteNodeFilter = (constraintType: string, index: number) => {
        this.isLoading = true;
        this.topologyTemplateService.deleteServiceFilterConstraints(
            this.compositeService.uniqueId,
            this.currentServiceInstance.uniqueId,
            index,
            this.compositeService.componentType,
            constraintType
        ).subscribe( (response) => {
            this.emitEventOnChanges(constraintType, response);
            this.isLoading = false;
        }, (err) => {
            this.isLoading = false;
        });
        this.modalServiceNg2.closeCurrentModal();
    }

    private emitEventOnChanges(constraintType: string, response) {
        if (this.properties === constraintType) {
            this.updateNodeFilterProperties.emit(response.properties);
            this.constraintProperties = response.properties;
        } else {
            this.updateNodeFilterCapabilities.emit(response.capabilities);
            this.constraintCapabilities = response.capabilities;
        }
    }

    openDeleteModal = (constraintType: string, index: number) => {
        this.modalServiceNg2.createActionModal(I18nTexts.deleteNodeFilterTxt, I18nTexts.deleteNodeFilterMsg,
            I18nTexts.modalDelete, () => this.onDeleteNodeFilter(constraintType, index), I18nTexts.modalCancel).instance.open();
    }

}
