import {NgModule} from "@angular/core";
import { CommonModule } from '@angular/common';
import {DesignerFrameComponent} from "./designer-frame.component";
import {LayoutModule} from "../../layout/layout.module";
import {GlobalPipesModule} from "../../../pipes/global-pipes.module";


@NgModule({
    declarations: [
        DesignerFrameComponent
    ],
    imports: [
        CommonModule,
        LayoutModule,
        GlobalPipesModule
    ],
    entryComponents: [DesignerFrameComponent],
    exports: [
        DesignerFrameComponent
    ],
    providers: []
})
export class DesignerFrameModule {

}
