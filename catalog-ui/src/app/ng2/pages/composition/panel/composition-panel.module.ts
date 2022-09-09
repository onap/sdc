/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
import { NgModule } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { BrowserModule } from "@angular/platform-browser";
import { CompositionPanelComponent } from "./composition-panel.component";
import { CompositionPanelHeaderModule } from "app/ng2/pages/composition/panel/panel-header/panel-header.module";
import { SdcUiComponentsModule, SdcUiServices } from "onap-ui-angular";
// import { SdcUiServices } from "onap-ui-angular/";
import { UiElementsModule } from 'app/ng2/components/ui/ui-elements.module';
import { AddElementsModule } from "../../../components/ui/modal/add-elements/add-elements.module";
import { TranslateModule } from "app/ng2/shared/translator/translate.module";
import { InfoTabComponent } from './panel-tabs/info-tab/info-tab.component';
import { PanelTabComponent } from "app/ng2/pages/composition/panel/panel-tabs/panel-tab.component";
import { ArtifactsTabComponent } from "app/ng2/pages/composition/panel/panel-tabs/artifacts-tab/artifacts-tab.component";
import { PropertiesTabComponent } from "app/ng2/pages/composition/panel/panel-tabs/properties-tab/properties-tab.component";
import { ReqAndCapabilitiesTabComponent } from "app/ng2/pages/composition/panel/panel-tabs/req-capabilities-tab/req-capabilities-tab.component";
import { RequirementListComponent } from "app/ng2/pages/composition/panel/panel-tabs/req-capabilities-tab/requirement-list/requirement-list.component";
import { PolicyTargetsTabComponent } from "app/ng2/pages/composition/panel/panel-tabs/policy-targets-tab/policy-targets-tab.component";
import { GroupMembersTabComponent } from "app/ng2/pages/composition/panel/panel-tabs/group-members-tab/group-members-tab.component";
import { GroupOrPolicyPropertiesTab } from "app/ng2/pages/composition/panel/panel-tabs/group-or-policy-properties-tab/group-or-policy-properties-tab.component";
import { GlobalPipesModule } from "app/ng2/pipes/global-pipes.module";
import {ModalModule} from "../../../components/ui/modal/modal.module";
import {EnvParamsComponent} from "../../../components/forms/env-params/env-params.component";
import {ModalsModule} from "../../../components/modals/modals.module";
// import {EnvParamsModule} from "../../../components/forms/env-params/env-params.module";
import { NgxDatatableModule } from "@swimlane/ngx-datatable";
import {EnvParamsModule} from "../../../components/forms/env-params/env-params.module";
import { ServiceConsumptionTabComponent } from "./panel-tabs/service-consumption-tab/service-consumption-tab.component";
import { ServiceDependenciesTabComponent } from "./panel-tabs/service-dependencies-tab/service-dependencies-tab.component";
import { ServiceDependenciesModule } from "../../../components/logic/service-dependencies/service-dependencies.module";
import { ServiceConsumptionModule } from "../../../components/logic/service-consumption/service-consumption.module";
import {SubstitutionFilterTabComponent} from "./panel-tabs/substitution-filter-tab/substitution-filter-tab.component";
import {SubstitutionFilterModule} from "../../../components/logic/substitution-filter/substitution-filter.module";
import {InterfaceOperationsComponent} from "../interface-operatons/interface-operations.component";
import {CompositionService} from "../composition.service";

@NgModule({
    declarations: [
        CompositionPanelComponent,
        PolicyTargetsTabComponent,
        GroupOrPolicyPropertiesTab,
        GroupMembersTabComponent,
        InfoTabComponent,
        PanelTabComponent,
        ArtifactsTabComponent,
        PropertiesTabComponent,
        ReqAndCapabilitiesTabComponent,
        ServiceConsumptionTabComponent,
        ServiceDependenciesTabComponent,
        SubstitutionFilterTabComponent,
        RequirementListComponent,
        EnvParamsComponent,
        InterfaceOperationsComponent,
    ],
    imports: [
        GlobalPipesModule,
        BrowserModule,
        FormsModule,
        CompositionPanelHeaderModule,
        SdcUiComponentsModule,
        UiElementsModule,
        AddElementsModule,
        TranslateModule,
        NgxDatatableModule,
        ServiceDependenciesModule,
        ServiceConsumptionModule,
        SubstitutionFilterModule,
        // EnvParamsModule
    ],
    entryComponents: [
        CompositionPanelComponent,
        PolicyTargetsTabComponent,
        GroupOrPolicyPropertiesTab,
        GroupMembersTabComponent,
        InfoTabComponent,
        ArtifactsTabComponent,
        PropertiesTabComponent,
        ReqAndCapabilitiesTabComponent,
        ServiceConsumptionTabComponent,
        ServiceDependenciesTabComponent,
        SubstitutionFilterTabComponent,
        RequirementListComponent,
        PanelTabComponent,
        EnvParamsComponent,
        InterfaceOperationsComponent
        ],
    exports: [
        CompositionPanelComponent
        // EnvParamsModule
    ],
    providers: [SdcUiServices.ModalService, CompositionService]
})
export class CompositionPanelModule {

}
