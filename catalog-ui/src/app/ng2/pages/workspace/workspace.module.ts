import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {CompositionPageModule} from "../composition/composition-page.module";
import {SdcUiComponentsModule} from "onap-ui-angular";
import {LayoutModule} from "../../components/layout/layout.module";

import {NgxsModule} from "@ngxs/store";
import {TopologyTemplateService} from "../../services/component-services/topology-template.service";
import {WorkspaceState} from "../../store/states/workspace.state";
import {WorkspaceService} from "./workspace.service";
import {DeploymentPageModule} from "./deployment/deployment-page.module";
import {ToscaArtifactPageModule} from "./tosca-artifacts/tosca-artifact-page.module";
import {InformationArtifactPageModule} from "./information-artifact/information-artifact-page.module";
import {reqAndCapabilitiesModule} from "./req-and-capabilities/req-and-capabilities.module";
import {AttributesModule} from "./attributes/attributes.module";
import {ArtifactsState} from "../../store/states/artifacts.state";
import {InstanceArtifactsState} from "../../store/states/instance-artifacts.state";
import {DeploymentArtifactsPageModule} from "./deployment-artifacts/deployment-artifacts-page.module";
import {DistributionModule} from './disribution/distribution.module';
import {ActivityLogModule} from './activity-log/activity-log.module';
import {WorkspaceContainerComponent} from './workspace-container/workspace-container.component';
import {WorkspaceTopProgressComponent} from './workspace-container/top-progress.component';

@NgModule({
    declarations: [WorkspaceContainerComponent, WorkspaceTopProgressComponent],
    imports: [
        CommonModule,
        FormsModule,
        SdcUiComponentsModule,
        LayoutModule,
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

    exports: [WorkspaceContainerComponent],
    entryComponents: [WorkspaceContainerComponent],
    providers: [TopologyTemplateService, WorkspaceService],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})

export class WorkspaceModule {
}
