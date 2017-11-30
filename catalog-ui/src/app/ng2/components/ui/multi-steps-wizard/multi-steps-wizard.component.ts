/**
 * Created by rc2122 on 8/15/2017.
 */
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
import {
    Component, ElementRef, forwardRef, Inject, Input, trigger, state, style,
    transition, animate, ViewChild, ViewContainerRef, ComponentRef
} from "@angular/core";
import {StepModel} from "app/models";
import {ModalService} from "../../../services/modal.service";
import {ModalComponent} from "../modal/modal.component";
import {WizardHeaderBaseComponent} from "./multi-steps-wizard-header-base.component";


@Component({
    selector: 'multi-steps-wizard',
    templateUrl: './multi-steps-wizard.component.html',
    styleUrls: ['./../modal/modal.component.less','./multi-steps-wizard.component.less'],
    animations: [
        trigger('displayLineAnimation', [
            state('true', style({
                width: '100%',
            })),
            state('false', style({
                width:'0px',
            })),
            transition('* => *', animate('500ms')),
        ]),
    ],
})
export class MultiStepsWizardComponent extends ModalComponent {

    @Input() steps:Array<StepModel>;
    @Input() callback: Function;
    @Input() data:any;
    @Input() dynamicHeader: ComponentRef<WizardHeaderBaseComponent>;

    @ViewChild('dynamicHeaderContainer', { read: ViewContainerRef }) dynamicHeaderContainer: ViewContainerRef;
    constructor(@Inject(forwardRef(() => ModalService)) public modalService: ModalService, el: ElementRef ) {
        super(el);
    }

    private currentStepIndex:number = 0;

    nextStep = ():void => {
        if(this.currentStepIndex + 1 < this.steps.length){
            this.currentStepIndex++;
            this.modalService.addDynamicContentToModal(this.modalService.currentModal, this.steps[this.currentStepIndex].component);
            this.dynamicHeader.instance.currentStepIndex = this.currentStepIndex;
        }
    }

    prevStep = ():void => {
        if(this.currentStepIndex > 0){
            this.currentStepIndex--;
            this.modalService.addDynamicContentToModal(this.modalService.currentModal, this.steps[this.currentStepIndex].component);
            this.dynamicHeader.instance.currentStepIndex = this.currentStepIndex;
        }
    }
}