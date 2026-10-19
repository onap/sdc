import { NgModule } from "@angular/core";
import {CommonModule} from "@angular/common";
import { ServicePathsListComponent } from "./service-paths-list.component";
import {ButtonsModule} from "onap-ui-angular/dist/buttons/buttons.module";

@NgModule({
    declarations: [
        ServicePathsListComponent
    ],
    imports: [CommonModule, ButtonsModule],
    exports: [],
    entryComponents: [
        ServicePathsListComponent
    ],
    providers: []
})
export class ServicePathsListModule {
}