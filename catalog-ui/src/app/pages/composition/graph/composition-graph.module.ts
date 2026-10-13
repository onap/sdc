import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { UiElementsModule } from 'app/components/ui/ui-elements.module';
import { LinksFactory } from 'app/models/graph/graph-links/links-factory';
import { NodesFactory } from 'app/models/graph/nodes/nodes-factory';
import { ConnectionWizardService } from 'app/pages/composition/graph/connection-wizard/connection-wizard.service';
import { QueueServiceUtils } from 'app/utils/queue-service-utils';
import { DndModule } from 'ngx-drag-drop';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { CanvasSearchModule } from './canvas-search/canvas-search.module';
import { ZoneModules } from './canvas-zone/zones-module';
import { CommonGraphUtils } from './common/common-graph-utils';
import { ImageCreatorService } from './common/image-creator.service';
import { CompositionGraphComponent } from './composition-graph.component';
import { ServicePathSelectorModule } from './service-path-selector/service-path-selector.module';
import { CompositionGraphLinkUtils, ServicePathGraphUtils } from './utils';
import { CompositionGraphGeneralUtils } from './utils/composition-graph-general-utils';
import { CompositionGraphNodesUtils } from './utils/composition-graph-nodes-utils';
import { CompositionGraphPaletteUtils } from './utils/composition-graph-palette-utils';
import { CompositionGraphZoneUtils } from './utils/composition-graph-zone-utils';
import { MatchCapabilitiesRequirementsUtils } from './utils/match-capability-requirement-utils';

@NgModule({
    declarations: [CompositionGraphComponent],
    imports: [CommonModule,
        ServicePathSelectorModule,
        SdcUiComponentsModule,
        UiElementsModule,
        ZoneModules,
        CanvasSearchModule,
        DndModule],
    exports: [CompositionGraphComponent],
    entryComponents: [CompositionGraphComponent],
    providers: [
        CompositionGraphZoneUtils,
        CompositionGraphGeneralUtils,
        MatchCapabilitiesRequirementsUtils,
        CompositionGraphNodesUtils,
        CompositionGraphLinkUtils,
        CommonGraphUtils,
        NodesFactory,
        LinksFactory,
        ImageCreatorService,
        ConnectionWizardService,
        CompositionGraphPaletteUtils,
        QueueServiceUtils,
        ServicePathGraphUtils
    ]
})
export class CompositionGraphModule {
}
