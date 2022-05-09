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

import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {BrowserModule} from '@angular/platform-browser';
import { NgModule, APP_INITIALIZER } from '@angular/core';
import {FormsModule} from '@angular/forms';
import { forwardRef } from '@angular/core';
import {AppComponent} from './app.component';
import {UpgradeAdapter} from '@angular/upgrade';
import {UpgradeModule} from '@angular/upgrade/static';
import { SdcUiComponentsModule, SdcUiComponents } from 'onap-ui-angular';
import {PropertiesAssignmentModule} from './pages/properties-assignment/properties-assignment.module';
import {
    DataTypesServiceProvider, CookieServiceProvider, StateServiceFactory,
    StateParamsServiceFactory, ScopeServiceFactory,
    NotificationServiceProvider, ComponentFactoryProvider
} from './utils/ng1-upgraded-provider';
import {ConfigService} from './services/config.service';
import {AuthenticationService} from './services/authentication.service';
import {Cookie2Service} from './services/cookie.service';
import {ComponentServiceNg2} from './services/component-services/component.service';
import {ComponentServiceFactoryNg2} from './services/component-services/component.service.factory';
import {ServiceServiceNg2} from './services/component-services/service.service';
import {ComponentInstanceServiceNg2} from './services/component-instance-services/component-instance.service';
import {ModalService} from './services/modal.service';
import {UiElementsModule} from './components/ui/ui-elements.module';
import {ConnectionWizardModule} from './pages/composition/graph/connection-wizard/connection-wizard.module';
import {InterfaceOperationModule} from './pages/interface-operation/interface-operation.module';
import {OperationCreatorModule} from './pages/interface-operation/operation-creator/operation-creator.module';
import {OperationCreatorInterfaceDefinitionModule} from './pages/interface-definition/operation-creator/operation-creator-interface-definition.module';
import {LayoutModule} from './components/layout/layout.module';
import {UserService} from './services/user.service';
import {DynamicComponentService} from './services/dynamic-component.service';
import {SdcConfig} from './config/sdc-config.config';
import {SdcMenu} from './config/sdc-menu.config';
import {TranslateModule} from './shared/translator/translate.module';
import {TranslationServiceConfig} from './config/translation.service.config';
import {MultilineEllipsisModule} from './shared/multiline-ellipsis/multiline-ellipsis.module';
import {ServicePathCreatorModule} from './pages/composition/graph/service-path-creator/service-path-creator.module';
import {ServicePathsListModule} from './pages/composition/graph/service-paths-list/service-paths-list.module';
import { ServicePathSelectorModule } from 'app/ng2/pages/composition/graph/service-path-selector/service-path-selector.module';
import { CompositionPanelModule } from 'app/ng2/pages/composition/panel/composition-panel.module';
import {CatalogModule} from './pages/catalog/catalog.module';
import {HomeModule} from './pages/home/home.module';
import {WindowRef} from './services/window.service';
import {CatalogService} from './services/catalog.service';
import { ModalsHandlerProvider } from './utils/ng1-upgraded-provider';
import {PluginFrameModule} from './components/ui/plugin/plugin-frame.module';
import {PluginsService} from './services/plugins.service';
import {EventBusService} from './services/event-bus.service';
import {GroupsService} from './services/groups.service';
import {PoliciesService} from './services/policies.service';
import {AutomatedUpgradeService} from './pages/automated-upgrade/automated-upgrade.service';
import {AutomatedUpgradeModule} from './pages/automated-upgrade/automated-upgrade.module';
import {WorkspaceModule} from './pages/workspace/workspace.module';
import {ModalsModule} from './components/modals/modals.module';
import { SharingService, CacheService, HomeService } from 'app/services-ng2';
import {ArtifactConfigService} from "./services/artifact-config.service";
import {IUserProperties} from 'app/models';
import {PluginsModule} from './pages/plugins/plugins-module';
import {WorkspaceNg1BridgeService} from './pages/workspace/workspace-ng1-bridge-service';
import {NgxsModule} from '@ngxs/store';
import {NgxsLoggerPluginModule} from '@ngxs/logger-plugin';
import {NgxsReduxDevtoolsPluginModule} from '@ngxs/devtools-plugin';
import {EventListenerService} from '../services/event-listener-service';
import {HttpClientModule} from '@angular/common/http';
import {httpInterceptorProviders} from './http-interceptor';
import {HttpHelperService} from './services/http-hepler.service';
import {ModulesService} from "./services/modules.service";
import {TranslateService} from 'app/ng2/shared/translator/translate.service';
import {FileUtilsService} from './services/file-utils.service';
import {ImportVSPService} from './components/modals/onboarding-modal/import-vsp.service';
import {OnboardingService} from './services/onboarding.service';
import {ServiceConsumptionCreatorModule} from './pages/service-consumption-editor/service-consumption-editor.module';
import {ServiceDependenciesModule} from './components/logic/service-dependencies/service-dependencies.module';
import {ServiceDependenciesEditorModule} from './pages/service-dependencies-editor/service-dependencies-editor.module';
import {PropertyCreatorModule} from './pages/properties-assignment/property-creator/property-creator.module';
import {DeclareListModule} from './pages/properties-assignment/declare-list/declare-list.module';
import { ToscaFunctionModule } from "./pages/properties-assignment/tosca-function/tosca-function.module";
import {WorkflowServiceNg2} from './services/workflow.service';
import {ToscaTypesServiceNg2} from "./services/tosca-types.service";
import {CapabilitiesFilterPropertiesEditorComponentModule} from "./pages/composition/capabilities-filter-properties-editor/capabilities-filter-properties-editor.module";
import {InterfaceOperationHandlerModule} from "./pages/composition/interface-operatons/operation-creator/interface-operation-handler.module";
import {AttributesOutputsModule} from "./pages/attributes-outputs/attributes-outputs.module";
import { ElementService } from "./services/element.service";
import { ModelService } from "./services/model.service";
import {ToscaArtifactService} from "./services/tosca-artifact.service";
import {InterfaceDefinitionModule} from "./pages/interface-definition/interface-definition.module";

declare const __ENV__: string;

export const upgradeAdapter = new UpgradeAdapter(forwardRef(() => AppModule));

export function configServiceFactory(config: ConfigService, authService: AuthenticationService, eventListener: EventListenerService) {

  return () => {
    return authService.authenticate().toPromise()
    .then((userInfo: IUserProperties) => {
      authService.setLoggedinUser(userInfo);
      return Promise.all([
        config.loadSdcSetupData(),
        config.loadValidationConfiguration(),
        config.loadPluginsConfiguration(),
      ])
    }).then(() => {
      eventListener.notifyObservers('ON_FINISH_LOADING');
    })
    .catch(() => {
      console.log('AUTH FAILED! from app module');
    });
  };
}

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserAnimationsModule,
    BrowserModule,
    UpgradeModule,
    FormsModule,
    HttpClientModule,
    LayoutModule,
    TranslateModule,
    MultilineEllipsisModule,
    UiElementsModule,
    CompositionPanelModule,
    SdcUiComponentsModule,
    AutomatedUpgradeModule,

    // We need to import them here since we use them in angular1
    ConnectionWizardModule,
    PropertiesAssignmentModule,
    AttributesOutputsModule,
    PropertyCreatorModule,
    DeclareListModule,
    ToscaFunctionModule,
    PluginFrameModule,
    PluginsModule,
    InterfaceOperationModule,
    InterfaceDefinitionModule,
    OperationCreatorModule,
    OperationCreatorInterfaceDefinitionModule,
    InterfaceOperationHandlerModule,
    ServicePathCreatorModule,
    ServicePathsListModule,
    ServicePathSelectorModule,
    ServiceConsumptionCreatorModule,
    ServiceDependenciesModule,
    ServiceDependenciesEditorModule,
    CapabilitiesFilterPropertiesEditorComponentModule,
    WorkspaceModule,
    ModalsModule,
    CatalogModule,
    HomeModule,
    NgxsModule.forRoot([]),
    NgxsLoggerPluginModule.forRoot({logger: console, collapsed: false}),
    NgxsReduxDevtoolsPluginModule.forRoot({
      disabled: __ENV__ === 'prod'
    })
  ],
  exports: [],
  entryComponents: [],
  providers: [
    WindowRef,
    httpInterceptorProviders,
    DataTypesServiceProvider,
    SharingService,
    CacheService,
    HomeService,
    ArtifactConfigService,
    ComponentFactoryProvider,
    CookieServiceProvider,
    StateServiceFactory,
    StateParamsServiceFactory,
    ScopeServiceFactory,
    NotificationServiceProvider,
    ModalsHandlerProvider,
    UserService,
    Cookie2Service,
    ConfigService,
    ComponentServiceNg2,
    ComponentServiceFactoryNg2,
    ModalService,
    ImportVSPService,
    OnboardingService,
    ElementService,
    ModelService,
    ToscaArtifactService,
    ServiceServiceNg2,
    AutomatedUpgradeService,
    WorkflowServiceNg2,
    ToscaTypesServiceNg2,
    WorkspaceNg1BridgeService,
    HttpHelperService,
    AuthenticationService,
    PoliciesService,
    GroupsService,
    ModulesService,
    DynamicComponentService,
    SdcConfig,
    SdcMenu,
    ComponentInstanceServiceNg2,
    EventListenerService,
    TranslationServiceConfig,
    TranslateService,
    PluginsService,
    CatalogService,
    EventBusService,
    FileUtilsService,
    {
      provide: APP_INITIALIZER,
      useFactory: configServiceFactory,
      deps: [ConfigService, AuthenticationService, EventListenerService],
      multi: true
    },
  ],
  bootstrap: [AppComponent]
})

export class AppModule {
  constructor(public upgrade: UpgradeModule) {

  }
}
