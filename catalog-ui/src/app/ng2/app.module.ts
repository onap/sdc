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

import { BrowserModule } from '@angular/platform-browser';
import { NgModule, APP_INITIALIZER } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forwardRef } from '@angular/core';
import { AppComponent } from './app.component';
import { UpgradeAdapter } from '@angular/upgrade';
import { UpgradeModule } from '@angular/upgrade/static';
import { SdcUiComponentsModule, SdcUiComponents } from "sdc-ui/lib/angular";
import { PropertiesAssignmentModule } from './pages/properties-assignment/properties-assignment.module';
import { PropertyCreatorModule } from './pages/properties-assignment/property-creator/property-creator.module';
import {
    DataTypesServiceProvider, SharingServiceProvider, CookieServiceProvider, StateServiceFactory,
    StateParamsServiceFactory, CacheServiceProvider, EventListenerServiceProvider, ScopeServiceFactory,
    NotificationServiceProvider, ComponentFactoryProvider
} from "./utils/ng1-upgraded-provider";
import { ConfigService } from "./services/config.service";
import { HttpModule } from '@angular/http';
import { HttpService } from './services/http.service';
import { AuthenticationService } from './services/authentication.service';
import { Cookie2Service } from "./services/cookie.service";
import { ComponentServiceNg2 } from "./services/component-services/component.service";
import { ComponentServiceFactoryNg2 } from "./services/component-services/component.service.factory";
import { ServiceServiceNg2 } from "./services/component-services/service.service";
import { ComponentInstanceServiceNg2 } from "./services/component-instance-services/component-instance.service";
import { WorkflowServiceNg2 } from './services/workflow.service';
import {ToscaTypesServiceNg2} from "./services/tosca-types.service";
import { ModalService } from "./services/modal.service";
import { UiElementsModule } from "./components/ui/ui-elements.module";
import { ConnectionWizardModule } from "./pages/connection-wizard/connection-wizard.module";
import { InterfaceOperationModule } from "./pages/interface-operation/interface-operation.module";
import { OperationCreatorModule } from "./pages/interface-operation/operation-creator/operation-creator.module";
import { LayoutModule } from "./components/layout/layout.module";
import { UserService } from "./services/user.service";
import { DynamicComponentService } from "./services/dynamic-component.service";
import { SdcConfig } from "./config/sdc-config.config";
import { SdcMenu } from "./config/sdc-menu.config";
import { TranslateModule } from "./shared/translator/translate.module";
import { TranslationServiceConfig } from "./config/translation.service.config";
import { MultilineEllipsisModule } from "./shared/multiline-ellipsis/multiline-ellipsis.module";
import { ServicePathCreatorModule } from './pages/service-path-creator/service-path-creator.module';
import { ServicePathsListModule } from './pages/service-paths-list/service-paths-list.module';
import { ServicePathModule } from 'app/ng2/components/logic/service-path/service-path.module';
import { ServicePathSelectorModule } from 'app/ng2/components/logic/service-path-selector/service-path-selector.module';
import { ServiceConsumptionModule } from 'app/ng2/components/logic/service-consumption/service-consumption.module';
import { ServiceConsumptionCreatorModule } from './pages/service-consumption-editor/service-consumption-editor.module';
import {ServiceDependenciesModule} from 'app/ng2/components/logic/service-dependencies/service-dependencies.module';
import {ServiceDependenciesEditorModule} from './pages/service-dependencies-editor/service-dependencies-editor.module';
import { CompositionPanelModule } from 'app/ng2/pages/composition/panel/panel.module';
import { WindowRef } from "./services/window.service";
import {ArchiveService} from "./services/archive.service";
import { ModalsHandlerProvider } from './utils/ng1-upgraded-provider';
import {PluginFrameModule} from "./components/ui/plugin/plugin-frame.module";
import {PluginsService} from "./services/plugins.service";
import {EventBusService} from "./services/event-bus.service";
import {GroupsService} from "./services/groups.service";
import {PoliciesService} from "./services/policies.service";
import {AutomatedUpgradeService} from "./pages/automated-upgrade/automated-upgrade.service";
import {AutomatedUpgradeModule} from "./pages/automated-upgrade/automated-upgrade.module";
import {RequirementsEditorModule} from "./pages/req-and-capabilities-editor/requirements-editor/requirements-editor.module"
import {CapabilitiesEditorModule} from "./pages/req-and-capabilities-editor/capabilities-editor/capabilities-editor.module"

export const upgradeAdapter = new UpgradeAdapter(forwardRef(() => AppModule));

export function configServiceFactory(config: ConfigService) {
    return () => {
        return Promise.all([
            config.loadValidationConfiguration(),
            config.loadPluginsConfiguration()
        ]);
    }
}


@NgModule({
    declarations: [
        AppComponent
    ],
    imports: [
        BrowserModule,
        UpgradeModule,
        FormsModule,
        HttpModule,
        LayoutModule,
        TranslateModule,
        MultilineEllipsisModule,
        UiElementsModule,
        CompositionPanelModule,
        SdcUiComponentsModule,
        AutomatedUpgradeModule,
        //We need to import them here since we use them in angular1
        ConnectionWizardModule,
        PropertiesAssignmentModule,
        PropertyCreatorModule,
        PluginFrameModule,
        InterfaceOperationModule,
        OperationCreatorModule,
        ServicePathCreatorModule,
        ServicePathsListModule,
        ServicePathModule,
        ServicePathSelectorModule,
        ServiceConsumptionModule,
        ServiceConsumptionCreatorModule,
        ServiceDependenciesModule,
        ServiceDependenciesEditorModule,
        RequirementsEditorModule,
        CapabilitiesEditorModule
    ],
    exports: [],
    entryComponents: [
        // *** sdc-ui components to be used as downgraded:
        SdcUiComponents.SvgIconComponent
    ],
    providers: [
        WindowRef,
        DataTypesServiceProvider,
        SharingServiceProvider,
        ComponentFactoryProvider,
        CookieServiceProvider,
        StateServiceFactory,
        StateParamsServiceFactory,
        ScopeServiceFactory,
        CacheServiceProvider,
        EventListenerServiceProvider,
        NotificationServiceProvider,
        ModalsHandlerProvider,
        AuthenticationService,
        Cookie2Service,
        ConfigService,
        ComponentServiceNg2,
        ComponentServiceFactoryNg2,
        ModalService,
        ServiceServiceNg2,
        AutomatedUpgradeService,
        WorkflowServiceNg2,
        ToscaTypesServiceNg2,
        HttpService,
        UserService,
        PoliciesService,
        GroupsService,
        DynamicComponentService,
        SdcConfig,
        SdcMenu,
        ComponentInstanceServiceNg2,
        TranslationServiceConfig,
        PluginsService,
        ArchiveService,
        EventBusService,
        {
            provide: APP_INITIALIZER,
            useFactory: configServiceFactory,
            deps: [ConfigService],
            multi: true
        },
    ],
    bootstrap: [AppComponent]
})


export class AppModule {
    constructor(public upgrade: UpgradeModule, public eventBusService:EventBusService) {

    }
}
