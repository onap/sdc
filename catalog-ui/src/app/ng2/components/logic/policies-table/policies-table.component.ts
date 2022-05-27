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

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PolicyInstance } from 'app/models';
import { InstanceFeDetails } from 'app/models/instance-fe-details';
import { TranslateService } from 'app/ng2/shared/translator/translate.service';
import { ModalService } from '../../../services/modal.service';

@Component({
    selector: 'policies-table',
    templateUrl: 'policies-table.component.html',
    styleUrls: ['policies-table.component.less'],
})
export class PoliciesTableComponent {

    @Input() policies: PolicyInstance[];
    @Input() instanceNamesMap: { [key: string]: InstanceFeDetails };
    @Input() readonly: boolean;
    @Input() isLoading: boolean;
    @Output() deletePolicy: EventEmitter<any> = new EventEmitter<any>();

    sortBy: string;
    reverse: boolean;
    selectedPolicyToDelete: PolicyInstance;
    deleteMsgTitle: string;
    deleteMsgBodyTxt: string;
    modalDeleteBtn: string;
    modalCancelBtn: string;

    constructor(private modalService: ModalService, private translateService: TranslateService) {
    }

    sort = (sortBy) => {
        this.reverse = (this.sortBy === sortBy) ? !this.reverse : true;
        const reverse = this.reverse ? 1 : -1;
        this.sortBy = sortBy;
        const instanceNameMapTemp = this.instanceNamesMap;
        let itemIdx1Val = '';
        let itemIdx2Val = '';
        this.policies.sort((itemIdx1, itemIdx2) => {
            if (sortBy === 'instanceUniqueId') {
                itemIdx1Val = (itemIdx1[sortBy] && instanceNameMapTemp[itemIdx1[sortBy]] !== undefined) ? instanceNameMapTemp[itemIdx1[sortBy]].name : '';
                itemIdx2Val = (itemIdx2[sortBy] && instanceNameMapTemp[itemIdx2[sortBy]] !== undefined) ? instanceNameMapTemp[itemIdx2[sortBy]].name : '';
            } else {
                itemIdx1Val = itemIdx1[sortBy];
                itemIdx2Val = itemIdx2[sortBy];
            }
            if (itemIdx1Val < itemIdx2Val) {
                return -1 * reverse;
            } else if (itemIdx1Val > itemIdx2Val) {
                return 1 * reverse;
            } else {
                return 0;
            }
        });
    }

    ngOnInit() {
        this.translateService.languageChangedObservable.subscribe((lang) => {
            this.deleteMsgTitle = this.translateService.translate('DELETE_POLICY_TITLE');
            this.modalDeleteBtn = this.translateService.translate('MODAL_DELETE');
            this.modalCancelBtn = this.translateService.translate('MODAL_CANCEL');

        });
    }

    onDeletePolicy = () => {
        this.deletePolicy.emit(this.selectedPolicyToDelete);
        this.modalService.closeCurrentModal();
    }

    openDeleteModal = (policy: PolicyInstance) => {
        this.selectedPolicyToDelete = policy;
        this.translateService.languageChangedObservable.subscribe((lang) => {
            this.deleteMsgBodyTxt = this.translateService.translate('DELETE_POLICY_MSG', {policyName: policy.name});
            this.modalService.createActionModal(this.deleteMsgTitle, this.deleteMsgBodyTxt, this.modalDeleteBtn, this.onDeletePolicy, this.modalCancelBtn).instance.open();
        });
    }
}
