import {SdcUiComponentsModule} from "onap-ui-angular";
import { NgModule } from "@angular/core";
import {CanvasSearchComponent} from "./canvas-search.component";
import {CommonModule} from "@angular/common";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {HttpClientModule} from "@angular/common/http";
import {BrowserModule} from "@angular/platform-browser";
import {AutocompletePipe} from "onap-ui-angular/dist/autocomplete/autocomplete.pipe";

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
