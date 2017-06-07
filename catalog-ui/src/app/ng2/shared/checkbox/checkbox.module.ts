import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { CheckboxComponent } from './checkbox.component';


@NgModule({
    imports: [CommonModule, BrowserModule, FormsModule],
    declarations: [CheckboxComponent],
    bootstrap: [],
    exports: [CheckboxComponent]
})
export class CheckboxModule { }

/** README: **/

/** USAGE Example:
 *In page.module.ts: import CheckboxModule
 *In HTML:
 *<checkbox checkboxStyle="class-goes-here" [label]="'Text goes here'" [disabled]="variable-goes-here" [(checked)]="default-or-variable-goes-here" (checkedChange)="change-event-goes-here()"></checkbox>
 */

/**STYLING: (ViewEncapsulation is set to None to allow styles to be overridden or customized)
 *
 * To create or override styles:
 * Use /deep/ or >>> prefix to override styles via other components stylesheets
 */