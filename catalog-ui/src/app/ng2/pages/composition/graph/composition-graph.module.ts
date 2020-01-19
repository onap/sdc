import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {CompositionGraphComponent} from "./composition-graph.component";
import {ZoneModules} from "./canvas-zone/zones-module";
import {CompositionGraphZoneUtils} from "./utils/composition-graph-zone-utils";
import {CompositionGraphGeneralUtils} from "./utils/composition-graph-general-utils";
import {CommonGraphUtils} from "./common/common-graph-utils";
import {LinksFactory} from "app/models/graph/graph-links/links-factory";
import {NodesFactory} from "app/models/graph/nodes/nodes-factory";
import {ImageCreatorService} from "./common/image-creator.service";
import {MatchCapabilitiesRequirementsUtils} from "./utils/match-capability-requierment-utils";
import {CompositionGraphNodesUtils} from "./utils/composition-graph-nodes-utils";
import {ConnectionWizardService} from "app/ng2/pages/composition/graph/connection-wizard/connection-wizard.service";
import {CompositionGraphPaletteUtils} from "./utils/composition-graph-palette-utils";
import {QueueServiceUtils} from "app/ng2/utils/queue-service-utils";
import {DndModule} from "ngx-drag-drop";
import { MenuListNg2Module } from "app/ng2/components/downgrade-wrappers/menu-list-ng2/menu-list-ng2.module";
import { UiElementsModule } from "app/ng2/components/ui/ui-elements.module";
import {ServicePathSelectorModule} from "./service-path-selector/service-path-selector.module";
import {SdcUiComponentsModule, SdcUiServices} from "onap-ui-angular";
import {CanvasSearchModule} from "./canvas-search/canvas-search.module";
import {CompositionGraphLinkUtils, ServicePathGraphUtils} from "./utils";


@NgModule({
    declarations: [CompositionGraphComponent],
    imports: [CommonModule,
        ServicePathSelectorModule,
        SdcUiComponentsModule,
        MenuListNg2Module,
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
        SdcUiServices.simplePopupMenuService,
        ServicePathGraphUtils
    ]
})
export class CompositionGraphModule {
}