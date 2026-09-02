import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { TileComponent } from './tile.component';
import { GlobalPipesModule } from "../../../pipes/global-pipes.module";
import { TooltipModule } from "../tooltip/tooltip.module";
import {SdcUiComponentsModule} from "onap-ui-angular";


@NgModule({
    imports: [BrowserModule, GlobalPipesModule, SdcUiComponentsModule, TooltipModule],
    declarations: [TileComponent],
    exports: [TileComponent],
    entryComponents: [TileComponent]
})
export class SdcTileModule { }
