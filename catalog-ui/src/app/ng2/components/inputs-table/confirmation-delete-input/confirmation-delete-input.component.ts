/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
