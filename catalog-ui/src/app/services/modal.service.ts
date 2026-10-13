import {
    ApplicationRef, ComponentFactory, ComponentFactoryResolver, ComponentRef, Injectable, TemplateRef, Type,
    ViewContainerRef
} from '@angular/core';
import { WizardHeaderBaseComponent } from 'app/components/ui/multi-steps-wizard/multi-steps-wizard-header-base.component';
import { ButtonModel } from 'app/models/button';
import { ModalModel } from 'app/models/modal';
import { StepModel } from 'app/models/wizard-step';
import { DynamicComponentService } from 'app/services/dynamic-component.service';
import { ModalComponent } from '../components/ui/modal/modal.component';
import { MultiStepsWizardComponent } from '../components/ui/multi-steps-wizard/multi-steps-wizard.component';
import { getSdcConfig } from '../config/sdc-config.config.factory';

@Injectable()
export class ModalService {
    currentModal: ComponentRef<any>;

    public _shouldDisplayDelayedAlertModal: boolean = true;

    constructor(private dynamicComponentService: DynamicComponentService) { }

    /* Shortcut method to open an alert modal with title, message, and close button that simply closes the modal. */
    public openAlertModal(title: string, message: string, closeButtonText?: string) {
        const closeButton: ButtonModel = new ButtonModel(closeButtonText || 'Close', 'grey', this.closeCurrentModal);
        const modalModel: ModalModel = new ModalModel('sm', title, message, [closeButton], 'alert');
        this.createCustomModal(modalModel).instance.open();
    }

    public openDelayedAlertModal(title: string, message: string,
                                 closeButtonText?: string) {
        const timeDelay: number = getSdcConfig().displayAlertValidationAfterMilisec;
        setTimeout(() => {
            if (this._shouldDisplayDelayedAlertModal) {
                this.openAlertModal(title, message, closeButtonText);
            }
        }, timeDelay);
        this._shouldDisplayDelayedAlertModal = true;
    }

    public openErrorModal = (closeButtonText?: string, errorMessage?: string): void => {
        const errorModal = this.createErrorModal(closeButtonText, errorMessage);
        errorModal.instance.open();
    }

    /**
     * Shortcut method to open a basic modal with title, message, and an action button with callback, as well as close button.
     * NOTE: To close the modal from within the callback, use modalService.closeCurrentModal() //if you run into zone issues with callbacks see:https://stackoverflow.com/questions/36566698/how-to-dynamically-create-bootstrap-modals-as-angular2-components
     * NOTE: To add dynamic content to the modal, use modalService.addDynamicContentToModal(). First param is the return value of this function -- componentRef<ModalComponent>.
     * @param title Heading for modal
     * @param message Message for modal
     * @param actionButtonText Blue call to action button
     * @param actionButtonCallback function to invoke when button is clicked
     * @param cancelButtonText text for close/cancel button
     */
    public createActionModal = (title: string, message: string, actionButtonText: string, actionButtonCallback: Function, cancelButtonText: string): ComponentRef<ModalComponent> => {
        const actionButton: ButtonModel = new ButtonModel(actionButtonText, 'blue', actionButtonCallback);
        const cancelButton: ButtonModel = new ButtonModel(cancelButtonText, 'grey', this.closeCurrentModal);
        const modalModel: ModalModel = new ModalModel('sm', title, message, [actionButton, cancelButton]);
        const modalInstance: ComponentRef<ModalComponent> = this.createCustomModal(modalModel);
        return modalInstance;
    }

    public createErrorModal = (closeButtonText?: string, errorMessage?: string): ComponentRef<ModalComponent> => {
        const closeButton: ButtonModel = new ButtonModel(closeButtonText || 'Close', 'grey', this.closeCurrentModal);
        const modalModel: ModalModel = new ModalModel('sm', 'Error', errorMessage, [closeButton], 'error');
        const modalInstance: ComponentRef<ModalComponent> = this.createCustomModal(modalModel);
        return modalInstance;
    }

    /* Use this method to create a modal with title, message, and completely custom buttons. Use response.instance.open() to open */
    public createCustomModal = (customModalData: ModalModel): ComponentRef<ModalComponent> => {
        const customModal: ComponentRef<ModalComponent> = this.dynamicComponentService.createDynamicComponent(ModalComponent);
        customModal.instance.input = customModalData;
        this.currentModal = customModal;

        return customModal;
    }

    public createMultiStepsWizard = (title: string, steps: StepModel[], callback: Function, dynamicHeaderType?: Type<WizardHeaderBaseComponent>): ComponentRef<MultiStepsWizardComponent> => {
        const cancelButton: ButtonModel = new ButtonModel('Cancel', 'outline blue', this.closeCurrentModal);
        const modalModel: ModalModel = new ModalModel('xl', title, '', [cancelButton]);
        const wizardInstance: ComponentRef<MultiStepsWizardComponent> = this.dynamicComponentService.createDynamicComponent(MultiStepsWizardComponent);
        wizardInstance.instance.input = modalModel;
        wizardInstance.instance.steps = steps;
        wizardInstance.instance.callback = callback;
        if (dynamicHeaderType) {
            const dynamicHeader = this.dynamicComponentService.createDynamicComponent(dynamicHeaderType, wizardInstance.instance.dynamicHeaderContainer);
            wizardInstance.instance.dynamicHeader = dynamicHeader;
            wizardInstance.instance.dynamicHeader.instance.currentStepIndex = 0;
        }
        this.addDynamicContentToModal(wizardInstance, steps[0].component);
        this.currentModal = wizardInstance;
        return wizardInstance;
    }

    public closeCurrentModal = () => {
        if (!this.currentModal) { return; }
        this.currentModal.instance.close();
        this.currentModal.destroy();
        delete this.currentModal;
    }

    public addDynamicContentToModal = (modalInstance: ComponentRef<ModalComponent>, dynamicComponentType: Type<any>, dynamicComponentInput?: any) => {

        const dynamicContent = this.dynamicComponentService.createDynamicComponent(dynamicComponentType, modalInstance.instance.dynamicContentContainer);
        dynamicContent.instance.input = dynamicComponentInput;
        modalInstance.instance.dynamicContent = dynamicContent;
        return modalInstance;
    }

    public addDynamicContentToModalAndBindInputs = (modalInstance: ComponentRef<ModalComponent>, dynamicComponentType: Type<any>,
                                                    dynamicComponentInput?: Object) => {

        const dynamicContent = this.dynamicComponentService
            .createDynamicComponent(dynamicComponentType, modalInstance.instance.dynamicContentContainer);
        for (const key of Object.keys(dynamicComponentInput)) {
            dynamicContent.instance[key] = dynamicComponentInput[key];
        }
        modalInstance.instance.dynamicContent = dynamicContent;
        return modalInstance;
    }

    public addDynamicTemplateToModal = (modalInstance: ComponentRef<ModalComponent>, templateRef: TemplateRef<void>) => {
        modalInstance.instance.dynamicContentContainer.clear();
        modalInstance.instance.dynamicContentContainer.createEmbeddedView(templateRef);
        return modalInstance;
    }

}
