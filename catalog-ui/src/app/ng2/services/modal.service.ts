import { Injectable, Type, ViewContainerRef, ApplicationRef, ComponentFactory, ComponentFactoryResolver, ComponentRef } from '@angular/core';
import { ModalModel, ButtonModel } from 'app/models';
import { ModalComponent } from 'app/ng2/components/modal/modal.component';


@Injectable()
export class ModalService {
    currentModal: ComponentRef<any>;


    constructor(private componentFactoryResolver: ComponentFactoryResolver, private applicationRef: ApplicationRef) { }

    
    /* Shortcut method to open an alert modal with title, message, and close button that simply closes the modal. */
    public openAlertModal(title: string, message: string, closeButtonText?:string) {
        let closeButton: ButtonModel = new ButtonModel(closeButtonText || 'Close', 'grey', this.closeCurrentModal);
        let modalModel: ModalModel = new ModalModel('sm', title, message, [closeButton], 'alert');
        this.createCustomModal(modalModel).instance.open();
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
        let actionButton: ButtonModel = new ButtonModel(actionButtonText, 'blue', actionButtonCallback);
        let cancelButton: ButtonModel = new ButtonModel(cancelButtonText, 'grey', this.closeCurrentModal);
        let modalModel: ModalModel = new ModalModel('sm', title, message, [actionButton, cancelButton]);
        let modalInstance: ComponentRef<ModalComponent> = this.createCustomModal(modalModel);
        return modalInstance;
    }


    public createErrorModal = (closeButtonText?: string, errorMessage?: string):ComponentRef<ModalComponent> => {
        let closeButton: ButtonModel = new ButtonModel(closeButtonText || 'Close', 'grey', this.closeCurrentModal);
        let modalModel: ModalModel = new ModalModel('sm', 'Error', errorMessage, [closeButton], 'error');
        let modalInstance: ComponentRef<ModalComponent> = this.createCustomModal(modalModel);
        return modalInstance;
    }

    /* Use this method to create a modal with title, message, and completely custom buttons. Use response.instance.open() to open */
    public createCustomModal = (customModalData: ModalModel): ComponentRef<ModalComponent> => {
        let customModal: ComponentRef<ModalComponent> = this.createDynamicComponent(ModalComponent);
        customModal.instance.input = customModalData;
        this.currentModal = customModal;

        return customModal;
    }

    
    public closeCurrentModal = () => {
        if (!this.currentModal) return;
        this.currentModal.instance.close();
        this.currentModal.destroy();
    }


    public addDynamicContentToModal = (modalInstance: ComponentRef<ModalComponent>, dynamicComponentType: Type<any>, dynamicComponentInput: any) => {

        let dynamicContent = this.createDynamicComponent(dynamicComponentType, modalInstance.instance.dynamicContentContainer);
        dynamicContent.instance.input = dynamicComponentInput;
        modalInstance.instance.dynamicContent = dynamicContent;
        return modalInstance;
    }

    //Creates a component dynamically (aka during runtime). If a view container is not specified, it will append the new component to the app root. 
    //To subscribe to an event from invoking component: componentRef.instance.clicked.subscribe((m) => console.log(m.name));
    private createDynamicComponent<T>(componentType: Type<T>, viewContainerRef?:ViewContainerRef): ComponentRef<any> {

        viewContainerRef = viewContainerRef || this.getRootViewContainerRef();
        viewContainerRef.clear();

        let factory: ComponentFactory<any> = this.componentFactoryResolver.resolveComponentFactory(componentType); //Ref: https://angular.io/guide/dynamic-component-loader
        let componentRef = viewContainerRef.createComponent(factory);
        
        return componentRef; 
    }

    
    private getRootViewContainerRef(): ViewContainerRef {
        return this.applicationRef.components[0].instance.viewContainerRef;
    }
}