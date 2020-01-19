import { NgModule } from "@angular/core";
import {CommonModule} from "@angular/common";
import { ServicePathsListComponent } from "./service-paths-list.component";

@NgModule({
    declarations: [
        ServicePathsListComponent
    ],
    imports: [CommonModule],
    exports: [],
    entryComponents: [
        ServicePathsListComponent
    ],
    providers: []
})
export class ServicePathsListModule {
}