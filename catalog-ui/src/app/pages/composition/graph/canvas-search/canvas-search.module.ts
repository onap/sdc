import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { AutocompletePipe } from 'onap-ui-angular/dist/autocomplete/autocomplete.pipe';
import { CanvasSearchComponent } from './canvas-search.component';

@NgModule({
    declarations: [
        CanvasSearchComponent
    ],
    imports: [
        CommonModule,
        BrowserModule,
        HttpClientModule,
        BrowserAnimationsModule,
        SdcUiComponentsModule,
    ],
    exports: [
        CanvasSearchComponent
    ],
    entryComponents: [
        CanvasSearchComponent
    ],
    providers: [AutocompletePipe]
})
export class CanvasSearchModule {
}
