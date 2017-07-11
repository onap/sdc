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
import * as $ from 'jquery';
import {ButtonsModelMap} from "app/models/button";

@Component({
    selector: 'modal',
    templateUrl: './modal.component.html',
    styleUrls:['modal.component.less']
})

export class ModalComponent implements OnInit, OnDestroy {
    @Input() size: string; 'xl|l|md|sm|xsm'
    @Input() title: string;
    @Input() public buttons:ButtonsModelMap;
    private modalElement: JQuery;
    private buttonsNames:Array<string>;

    constructor( el: ElementRef ) {
        this.modalElement = $(el.nativeElement);
    }

    ngOnInit(): void {
        let modal = this;
        this.modalElement.appendTo('body');
        if(this.buttons){
            this.buttonsNames = Object.keys(this.buttons);
        }
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
