import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { GlobalPipesModule } from '../../../pipes/global-pipes.module';
import { TooltipModule } from '../tooltip/tooltip.module';
import { TileComponent } from './tile.component';

@NgModule({
    imports: [BrowserModule, GlobalPipesModule, SdcUiComponentsModule, TooltipModule],
    declarations: [TileComponent],
    exports: [TileComponent],
    entryComponents: [TileComponent]
})
export class SdcTileModule { }
