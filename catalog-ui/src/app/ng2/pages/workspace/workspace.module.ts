import {NgModule, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CompositionPageModule} from "../composition/composition-page.module";
import {SdcUiComponentsModule} from "onap-ui-angular";
import {LayoutModule} from "../../components/layout/layout.module";
import {GeneralTabComponent} from "./general-tab/general-tab.component";
import {GeneralFormService} from "./general-tab/general-form.service";
import {ComponentMetadataService} from "./general-tab/component-metadata.service";

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
import {ManagementWorkflowTabComponent} from './flow-editor/management-workflow-tab.component';
import {NetworkCallFlowTabComponent} from './flow-editor/network-call-flow-tab.component';
import {WorkspacePropertiesTabComponent} from './properties-tab/properties-tab.component';

@NgModule({
    declarations: [WorkspaceContainerComponent, WorkspaceTopProgressComponent, GeneralTabComponent,
        ManagementWorkflowTabComponent, NetworkCallFlowTabComponent, WorkspacePropertiesTabComponent],
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
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

    exports: [WorkspaceContainerComponent, GeneralTabComponent],
    entryComponents: [WorkspaceContainerComponent, GeneralTabComponent,
        ManagementWorkflowTabComponent, NetworkCallFlowTabComponent, WorkspacePropertiesTabComponent],
    providers: [TopologyTemplateService, WorkspaceService, GeneralFormService, ComponentMetadataService],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})

export class WorkspaceModule {
}
