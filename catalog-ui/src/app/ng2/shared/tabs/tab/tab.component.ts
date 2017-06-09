import { Component, Input } from '@angular/core';
import { ViewEncapsulation } from '@angular/core';

@Component({
    selector: 'tab',
    template: `
    <div *ngIf="active" class="tab-content">
      <ng-content></ng-content>
    </div>
    `,
    encapsulation: ViewEncapsulation.None
})
export class Tab {
    @Input('tabTitle') title: string;
    @Input() active:boolean = false;
    @Input() indication?: number;

}