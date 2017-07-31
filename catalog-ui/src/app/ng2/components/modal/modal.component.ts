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
import { Component, ElementRef, Input, OnInit, OnDestroy } from '@angular/core';
import {ViewContainerRef, ViewChild} from '@angular/core';
import * as $ from 'jquery';
import { ButtonsModelMap, ModalModel } from 'app/models';

@Component({
    selector: 'modal',
    templateUrl: './modal.component.html',
    styleUrls:['modal.component.less']
})

export class ModalComponent implements OnInit, OnDestroy {
    @Input() input: ModalModel;
    @Input() dynamicContent: any;
    @ViewChild('dynamicContentContainer', { read: ViewContainerRef }) dynamicContentContainer: ViewContainerRef; //Allows for custom component as body instead of simple message. See ModalService.createActionModal for implementation details, and HttpService's catchError() for example.
    private modalElement: JQuery;

    constructor( el: ElementRef ) {
        this.modalElement = $(el.nativeElement);
    }

    ngOnInit(): void {
        this.modalElement.appendTo('body');
    }

    ngOnDestroy(): void {
        this.modalElement.remove();
    }

    open(): void {
        this.modalElement.show();
        $('body').addClass('modal-open');
    }

    close(): void {
        this.modalElement.hide();
        $('body').removeClass('modal-open');
    }
}
