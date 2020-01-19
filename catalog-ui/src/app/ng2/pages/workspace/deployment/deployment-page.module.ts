/**
 * Created by ob0695 on 6/4/2018.
 */
import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {DeploymentPageComponent} from "./deployment-page.component";
import {SdcUiComponentsModule} from "onap-ui-angular";
import {UiElementsModule} from "../../../components/ui/ui-elements.module";
import {TranslateModule} from "../../../shared/translator/translate.module";
import {GlobalPipesModule} from "../../../pipes/global-pipes.module";
import {HierarchyTabModule} from "./panel/panel-tabs/hierarchy-tab/hierarchy-tab.module";
import {DeploymentGraphService} from "../../composition/deployment/deployment-graph.service";
import {DeploymentGraphModule} from "../../composition/deployment/deployment-graph.module";

@NgModule({
    declarations: [DeploymentPageComponent],
    imports: [CommonModule,
        DeploymentGraphModule,
        SdcUiComponentsModule,
        UiElementsModule,
        TranslateModule,
        GlobalPipesModule,
        HierarchyTabModule
    ],
    exports: [DeploymentPageComponent],
    entryComponents: [DeploymentPageComponent],
    providers: [DeploymentGraphService]
})
export class DeploymentPageModule {
}