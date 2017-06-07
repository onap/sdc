/**
 * Created by rc2122 on 5/17/2017.
 */
import {NgModule} from "@angular/core";
import { CommonModule } from '@angular/common';
import {PopoverComponent} from "./popover.component";
import {PopoverContentComponent} from "./popover-content.component";

@NgModule({
    declarations: [
        PopoverComponent,
        PopoverContentComponent
    ],
    imports: [
        // PopoverComponent,
        // PopoverContentComponent
        CommonModule
    ],
    exports: [
        PopoverComponent,
        PopoverContentComponent
    ],
    providers: []
})
export class PopoverModule {

}
