/**
 * Created by ob0695 on 6/4/2018.
 */
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { UiElementsModule } from '../../../../../../components/ui/ui-elements.module';
import { GlobalPipesModule } from '../../../../../../pipes/global-pipes.module';
import { TranslateModule } from '../../../../../../shared/translator/translate.module';
import { EditModuleName } from '../edit-module-name/edit-module-name.component';
import { HierarchyTabComponent } from './hierarchy-tab.component';

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
