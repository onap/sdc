import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MultilineEllipsisComponent } from './multiline-ellipsis.component';

@NgModule({
	declarations: [MultilineEllipsisComponent],
    imports: [CommonModule],
	exports: [MultilineEllipsisComponent],
	entryComponents: [MultilineEllipsisComponent]
})
export class MultilineEllipsisModule {}
