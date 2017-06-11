/**
 * Created by rc2122 on 6/1/2017.
 */
import {Component, Output, EventEmitter, ViewChild} from "@angular/core";
import {ButtonsModelMap, ButtonModel} from "app/models/button";
import {ModalComponent} from "app/ng2/components/modal/modal.component";

@Component({
    selector: 'confirm-delete-input',
    templateUrl: './confirmation-delete-input.component.html'
})
export class ConfirmationDeleteInputComponent {

    @Output() deleteInput: EventEmitter<any> = new EventEmitter<any>();
    @ViewChild ('confirmationModal') confirmationModal:ModalComponent;
    footerButtons:ButtonsModelMap = {};

    constructor (){
    }

    ngOnInit() {
        this.footerButtons['Delete'] = new ButtonModel('Delete', 'blue', this.onDeleteInput);
        this.footerButtons['Close'] = new ButtonModel('Close', 'grey', this.closeModal);
    }

    onDeleteInput = (input) => {
        this.deleteInput.emit(input);
        this.closeModal();
    };

    openModal = () => {
        this.confirmationModal.open();
    }

    closeModal = () => {
        this.confirmationModal.close();
    }
}
