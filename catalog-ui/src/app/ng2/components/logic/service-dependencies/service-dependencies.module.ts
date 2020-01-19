
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { UiElementsModule } from 'app/ng2/components/ui/ui-elements.module';
import { TranslateModule } from 'app/ng2/shared/translator/translate.module';
import { ServiceDependenciesComponent } from './service-dependencies.component';

@NgModule({
    declarations: [
        ServiceDependenciesComponent
    ],
    imports: [
        CommonModule,
        UiElementsModule,
        TranslateModule
    ],
    exports: [
        ServiceDependenciesComponent
    ],
    entryComponents: [
        ServiceDependenciesComponent
    ],
    providers: []
})
export class ServiceDependenciesModule {
}
