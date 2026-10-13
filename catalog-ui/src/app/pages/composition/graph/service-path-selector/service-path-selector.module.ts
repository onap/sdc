import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { UiElementsModule } from 'app/components/ui/ui-elements.module';
import { CompositionService } from 'app/pages/composition/composition.service';
import { ServicePathSelectorComponent } from './service-path-selector.component';

@NgModule({
    declarations: [
        ServicePathSelectorComponent
    ],
    imports: [
    	CommonModule,
    	UiElementsModule
    ],
    exports: [ServicePathSelectorComponent],
    entryComponents: [
        ServicePathSelectorComponent
    ],
    providers: [CompositionService]
})
export class ServicePathSelectorModule {
}
