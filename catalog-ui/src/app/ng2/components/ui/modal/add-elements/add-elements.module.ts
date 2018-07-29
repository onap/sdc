/**
 * Created by ob0695 on 11.04.2018.
 */
import {NgModule} from "@angular/core";
import {SdcUiComponentsModule} from "sdc-ui/lib/angular/index";
import {AddElementsComponent} from "./add-elements.component";
import {CommonModule} from "@angular/common";

/**
 * Created by ob0695 on 9.04.2018.
 */
@NgModule({
    declarations: [
        AddElementsComponent
    ],

    imports: [
        CommonModule,
        SdcUiComponentsModule
    ],

    entryComponents: [
        AddElementsComponent
    ],
    exports: [],
    providers: []
})
export class AddElementsModule {

}
