import { NgModule } from "@angular/core";
import { CommonModule } from '@angular/common';
import { ModalService } from 'app/ng2/services/modal.service';
import { ModalComponent } from "app/ng2/components/modal/modal.component"

@NgModule({
    declarations: [
        ModalComponent,
    ],
    imports: [CommonModule],
    exports: [],
    entryComponents: [
        ModalComponent
    ],
    providers: [ModalService]
})
export class ModalModule {

}