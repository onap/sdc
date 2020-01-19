/**
 * Created by rc2122 on 5/24/2018.
 */
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { CommentModalComponent } from 'app/ng2/components/modals/comment-modal/comment-modal.component';
import { PopoverModule } from 'app/ng2/components/ui/popover/popover.module';
import { TranslateModule } from 'app/ng2/shared/translator/translate.module';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { OnboardingService } from '../../services/onboarding.service';
import { ImportVSPService } from './onboarding-modal/import-vsp.service';
import { OnboardingModalComponent } from './onboarding-modal/onboarding-modal.component';

@NgModule({
    declarations: [CommentModalComponent, OnboardingModalComponent],
    imports: [TranslateModule,
        SdcUiComponentsModule,
        CommonModule,
        PopoverModule,
        NgxDatatableModule],
    exports: [CommentModalComponent, OnboardingModalComponent],
    entryComponents: [CommentModalComponent, OnboardingModalComponent],
    providers: [OnboardingService, ImportVSPService],
    bootstrap: []
})

export class ModalsModule {
}
