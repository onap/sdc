import {NgModule} from "@angular/core";
import {TooltipContentComponent} from "./tooltip-content.component";
import {TooltipComponent} from "./tooltip.component";
import {CommonModule} from "@angular/common";

@NgModule({
    declarations: [
        TooltipComponent,
        TooltipContentComponent,
    ],
    imports: [
        CommonModule
    ],
    exports: [
        TooltipComponent,
        TooltipContentComponent,
    ],
    entryComponents: [
        TooltipContentComponent
    ],
    providers: []
})
export class TooltipModule {

}
