import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MultilineEllipsisComponent} from './multiline-ellipsis.component';

@NgModule({
	declarations: [MultilineEllipsisComponent],
    imports: [CommonModule],
	exports: [MultilineEllipsisComponent],
	entryComponents: [MultilineEllipsisComponent]
})
export class MultilineEllipsisModule {}
