/**
 * Created by ob0695 on 6/4/2018.
 */
import {NgModule} from "@angular/core";
import {SdcUiComponentsModule} from "onap-ui-angular";
import {HierarchyTabComponent} from "./hierarchy-tab.component";
import {UiElementsModule} from "../../../../../../components/ui/ui-elements.module";
import {TranslateModule} from "../../../../../../shared/translator/translate.module";
import {CommonModule} from "@angular/common";
import {GlobalPipesModule} from "../../../../../../pipes/global-pipes.module";
import { EditModuleName } from "../edit-module-name/edit-module-name.component";

@NgModule({
    declarations: [HierarchyTabComponent, EditModuleName],
    imports: [CommonModule,
        UiElementsModule,
        SdcUiComponentsModule,
        TranslateModule,
        GlobalPipesModule],
    entryComponents: [HierarchyTabComponent, EditModuleName],
    exports: [HierarchyTabComponent, EditModuleName],
})
export class HierarchyTabModule {
}