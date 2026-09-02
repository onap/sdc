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

import { Component, Input, Output, ViewEncapsulation, AfterViewInit } from '@angular/core';

export enum ExpandState {
    EXPANDED,
    COLLAPSED
}

@Component({
    selector: 'ng2-expand-collapse',
    templateUrl: './expand-collapse.component.html',
    styleUrls: ['./expand-collapse.component.less'],
    encapsulation: ViewEncapsulation.None
})

export class ExpandCollapseComponent implements AfterViewInit {
    @Input() caption: String;
    @Input() state: ExpandState;
    @Input() titleTooltip: String;

    constructor() {

    }

    toggleState():void {
        if (this.state == ExpandState.EXPANDED) {
            this.state = ExpandState.COLLAPSED;
        } else {
            this.state = ExpandState.EXPANDED;
        }
    }

    ngAfterViewInit(): void {

    }

}
