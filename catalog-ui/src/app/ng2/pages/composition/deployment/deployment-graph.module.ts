import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {DeploymentGraphComponent} from "./deployment-graph.component";

@NgModule({
    declarations: [DeploymentGraphComponent],
    imports: [CommonModule],
    exports: [DeploymentGraphComponent],
    entryComponents: [DeploymentGraphComponent],
    providers: [

    ]
})
export class DeploymentGraphModule {
}