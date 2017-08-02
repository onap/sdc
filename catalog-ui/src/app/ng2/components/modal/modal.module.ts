import { NgModule } from "@angular/core";
import { CommonModule } from '@angular/common';
import { ModalService } from 'app/ng2/services/modal.service';
import { ModalComponent } from "app/ng2/components/modal/modal.component";
import { ErrorMessageComponent } from "./error-message/error-message.component";

@NgModule({
    declarations: [
        ModalComponent,
        ErrorMessageComponent
    ],
    imports: [CommonModule],
    exports: [],
    entryComponents: [ //need to add anything that will be dynamically created
        ModalComponent,
        ErrorMessageComponent
    ],
    providers: [ModalService]
})
export class ModalModule {

}