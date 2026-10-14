/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
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

import { Component, Input } from '@angular/core';
import { ViewEncapsulation } from '@angular/core';

@Component({
    selector: 'tab',
    template: `
    <div *ngIf="active && show" class="tab-content" role="tabpanel" [attr.id]="panelId" [attr.aria-labelledby]="tabId">
      <ng-content></ng-content>
    </div>
    `,
    encapsulation: ViewEncapsulation.None
})
export class Tab {
    // A different prefix from onap-ui-angular's sdc-tabs (sdc-tab-N): both render on the same pages.
    private static nextId: number = 0;

    @Input('tabTitle') title: string;
    @Input() active:boolean = false;
    @Input() show:boolean = true;
    @Input() highlight?: string;
    @Input() indication?: number;

    public tabId: string;
    public panelId: string;

    constructor() {
        const n = Tab.nextId++;
        this.tabId = 'ui-tab-' + n;
        this.panelId = 'ui-tabpanel-' + n;
    }
}
