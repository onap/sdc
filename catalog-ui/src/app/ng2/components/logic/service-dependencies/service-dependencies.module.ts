
import { NgModule } from "@angular/core";
import {CommonModule} from "@angular/common";
import {ServiceDependenciesComponent} from "./service-dependencies.component";
import {UiElementsModule} from "app/ng2/components/ui/ui-elements.module";
import {TranslateModule} from 'app/ng2/shared/translator/translate.module';

@NgModule({
    declarations: [
        ServiceDependenciesComponent
    ],
    imports: [
        CommonModule,
        UiElementsModule,
        TranslateModule
    ],
    exports: [],
    entryComponents: [
        ServiceDependenciesComponent
    ],
    providers: []
})
export class ServiceDependenciesModule {
}