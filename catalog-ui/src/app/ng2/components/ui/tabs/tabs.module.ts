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

import { NgModule } from '@angular/core'
import { BrowserModule } from '@angular/platform-browser'

import { Tabs } from './tabs.component';
import { Tab } from './tab/tab.component';


@NgModule({
    imports: [BrowserModule],
    declarations: [Tabs, Tab],
    bootstrap: [],
    exports: [Tabs, Tab]
})
export class TabModule { }

/** README: **/

/** USAGE Example:
 *In page.module.ts: import TabModule
 *In HTML:
 *<tabs tabStyle="class-goes-here" (tabChanged)="tabChangedEvent($event) [hideIndicationOnTabChange]="optional-boolean">
 *   <tab [tabTitle]="'Tab 1'">Content of tab 1</tab>
 *   <tab tabTitle="Tab 2" >Content of tab 2</tab>
 *   ...
 *</tabs>
 */

/**STYLING: (ViewEncapsulation is set to None to allow styles to be overridden or customized)
 * Existing options:
 * tabStyle="round-tabs" will provide generic rounded tab look
 *
 * To create or override styles:
 * Parent div has class ".tabs". Each tab has class ".tab". Active tab has class ".active".
 * Use /deep/ or >>> prefix to override styles via other components stylesheets
 */
