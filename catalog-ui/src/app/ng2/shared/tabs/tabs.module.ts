import { Component, NgModule } from '@angular/core'
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