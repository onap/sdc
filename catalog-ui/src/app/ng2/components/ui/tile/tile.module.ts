import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { TileComponent } from './tile.component';
import { GlobalPipesModule } from "../../../pipes/global-pipes.module";
import { TooltipModule } from "../tooltip/tooltip.module";
import {MultilineEllipsisModule} from "../../../shared/multiline-ellipsis/multiline-ellipsis.module";


@NgModule({
    imports: [BrowserModule, GlobalPipesModule, TooltipModule, MultilineEllipsisModule],
    declarations: [TileComponent],
    exports: [TileComponent],
    entryComponents: [TileComponent]
})
export class TileModule { }
