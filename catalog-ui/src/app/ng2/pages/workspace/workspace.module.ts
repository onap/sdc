/**
 * Created by ob0695 on 6/4/2018.
 */
/**
 * Created by ob0695 on 6/4/2018.
 */
import {NgModule} from "@angular/core";
import {CompositionPageModule} from "../composition/composition-page.module";

import {NgxsModule} from "@ngxs/store";
import {TopologyTemplateService} from "../../services/component-services/topology-template.service";
import {WorkspaceState} from "../../store/states/workspace.state";
import {WorkspaceService} from "./workspace.service";
import {DeploymentPageModule} from "./deployment/deployment-page.module";
import {ToscaArtifactPageModule} from "./tosca-artifacts/tosca-artifact-page.module";
import {InformationArtifactPageModule} from "./information-artifact/information-artifact-page.module";
import { reqAndCapabilitiesModule } from "./req-and-capabilities/req-and-capabilities.module";
import {AttributesModule} from "./attributes/attributes.module";
import {ArtifactsState} from "../../store/states/artifacts.state";
import {InstanceArtifactsState} from "../../store/states/instance-artifacts.state";
import {DeploymentArtifactsPageModule} from "./deployment-artifacts/deployment-artifacts-page.module";
import { DistributionModule } from './disribution/distribution.module';
import { ActivityLogModule } from './activity-log/activity-log.module';

@NgModule({
    declarations: [],
    imports: [
        DeploymentPageModule,
        CompositionPageModule,
        AttributesModule,
        reqAndCapabilitiesModule,
        ToscaArtifactPageModule,
        DeploymentArtifactsPageModule,
        InformationArtifactPageModule,
        DistributionModule,
        ActivityLogModule,
        NgxsModule.forFeature([WorkspaceState, ArtifactsState, InstanceArtifactsState])
    ],

    exports: [],
    entryComponents: [],
    providers: [TopologyTemplateService, WorkspaceService]
})

export class WorkspaceModule {

    constructor() {

    }
}
