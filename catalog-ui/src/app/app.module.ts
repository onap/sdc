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
import {APP_INITIALIZER, NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {AppComponent} from './app.component';
import {SdcUiComponentsModule} from 'onap-ui-angular';
import {PropertiesAssignmentModule} from './pages/properties-assignment/properties-assignment.module';
import {ConfigService} from './services/config.service';
import {AuthenticationService} from './services/authentication.service';
import {UiElementsModule} from './components/ui/ui-elements.module';
import {ConnectionWizardModule} from './pages/composition/graph/connection-wizard/connection-wizard.module';
import {InterfaceOperationModule} from './pages/interface-operation/interface-operation.module';
import {OperationCreatorModule} from './pages/interface-operation/operation-creator/operation-creator.module';
import {
  OperationCreatorInterfaceDefinitionModule
} from './pages/interface-definition/operation-creator/operation-creator-interface-definition.module';
import {LayoutModule} from './components/layout/layout.module';
import {SdcConfig} from './config/sdc-config.config';
import {SdcMenu} from './config/sdc-menu.config';
import {TranslateModule} from './shared/translator/translate.module';
import {GlossaryModule} from './shared/glossary/glossary.module';
import {TranslationServiceConfig} from './config/translation.service.config';
import {MultilineEllipsisModule} from './shared/multiline-ellipsis/multiline-ellipsis.module';
import {ServicePathCreatorModule} from './pages/composition/graph/service-path-creator/service-path-creator.module';
import {ServicePathsListModule} from './pages/composition/graph/service-paths-list/service-paths-list.module';
import {ServicePathSelectorModule} from 'app/pages/composition/graph/service-path-selector/service-path-selector.module';
import {CompositionPanelModule} from 'app/pages/composition/panel/composition-panel.module';
import {CatalogModule} from './pages/catalog/catalog.module';
import {HomeModule} from './pages/home/home.module';
import {PluginFrameModule} from './components/ui/plugin/plugin-frame.module';
import {AutomatedUpgradeModule} from './pages/automated-upgrade/automated-upgrade.module';
import {WorkspaceModule} from './pages/workspace/workspace.module';
import {PropertyFormModalModule} from './pages/property-form-modal/property-form-modal.module';
import {IconsModalModule} from './components/modals/icons-modal/icons-modal.module';
import {ModulePropertyModalModule} from './pages/module-property-modal/module-property-modal.module';
import {StandalonePagesModule} from './pages/standalone-pages.module';
import {ModalsModule} from './components/modals/modals.module';
import {IUserProperties} from 'app/models/user';
import {PluginsModule} from './pages/plugins/plugins-module';
import {NgxsModule} from '@ngxs/store';
import {NgxsLoggerPluginModule} from '@ngxs/logger-plugin';
import {NgxsReduxDevtoolsPluginModule} from '@ngxs/devtools-plugin';
import {EventListenerService} from './services/event-listener.service';
import {DataTypesService} from './services/data-types.service';
import {HttpClientModule} from '@angular/common/http';
import {httpInterceptorProviders} from './interceptors';
import {ServiceConsumptionCreatorModule} from './pages/service-consumption-editor/service-consumption-editor.module';
import {ServiceDependenciesModule} from './components/logic/service-dependencies/service-dependencies.module';
import {ServiceDependenciesEditorModule} from './pages/service-dependencies-editor/service-dependencies-editor.module';
import {PropertyCreatorModule} from './pages/properties-assignment/property-creator/property-creator.module';
import {DeclareListModule} from './pages/properties-assignment/declare-list/declare-list.module';
import {ToscaFunctionModule} from "./pages/properties-assignment/tosca-function/tosca-function.module";
import {ConstraintsModule} from "./pages/properties-assignment/constraints/constraints.module";
import {PropertyMetadataModule} from "./pages/properties-assignment/property-metadata/property-metadata.module";
import {InterfaceOperationHandlerModule} from "./pages/composition/interface-operations/operation-creator/interface-operation-handler.module";
import {AttributesOutputsModule} from "./pages/attributes-outputs/attributes-outputs.module";
import {InterfaceDefinitionModule} from "./pages/interface-definition/interface-definition.module";
import {TypeWorkspaceModule} from "./pages/type-workspace/type-workspace.module";
import {DeclareInputModule} from "./pages/properties-assignment/declare-input/declare-input.module";
import {AppRoutingModule} from "./app-routing.module";
import {LocationStrategy} from '@angular/common';
import {RouteReuseStrategy} from '@angular/router';
import {SdcHashLocationStrategy} from './utils/sdc-hash-location-strategy';
import {SdcRouteReuseStrategy} from './utils/sdc-route-reuse-strategy';
import {environment} from '../environments/environment';


export function configServiceFactory(config: ConfigService, authService: AuthenticationService,
                                     eventListener: EventListenerService, dataTypesService: DataTypesService) {

  return () => {
    return authService.authenticate().toPromise()
    .then((userInfo: IUserProperties) => {
      authService.setLoggedinUser(userInfo);
      return Promise.all([
        config.loadSdcSetupData(),
        config.loadValidationConfiguration(),
        config.loadPluginsConfiguration(),
        config.loadWorkspaceConfiguration(),
      ])
    }).then(() => {
      // Moved here from the deleted AngularJS run block (app.ts:593), which primed the default-model
      // data-type cache at startup. Deliberately NOT awaited, matching the run block: it was a
      // fire-and-forget call there too, and blocking bootstrap on it would delay first paint.
      dataTypesService.loadDataTypesCache(null);
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
    FormsModule,
    HttpClientModule,
    LayoutModule,
    TranslateModule,
    GlossaryModule,
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
    DeclareInputModule,
    DeclareListModule,
    ToscaFunctionModule,
    ConstraintsModule,
    PropertyMetadataModule,
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
    WorkspaceModule,
    PropertyFormModalModule,
    IconsModalModule,
    ModulePropertyModalModule,
    StandalonePagesModule,
    TypeWorkspaceModule,
    ModalsModule,
    CatalogModule,
    HomeModule,
    AppRoutingModule,
    NgxsModule.forRoot([]),
    NgxsLoggerPluginModule.forRoot({logger: console, collapsed: false}),
    NgxsReduxDevtoolsPluginModule.forRoot({
      disabled: environment.production
    })
  ],
  exports: [],
  entryComponents: [],
  providers: [
    httpInterceptorProviders,
    SdcConfig,
    SdcMenu,
    TranslationServiceConfig,
    // Must be provided HERE and not in AppRoutingModule: RouterModule.forRoot() registers its own
    // `provideLocationStrategy` and RouteReuseStrategy in the module it is imported into, so a
    // competing provider in that same module is ambiguous — the root module's wins.
    {provide: LocationStrategy, useClass: SdcHashLocationStrategy},
    {provide: RouteReuseStrategy, useClass: SdcRouteReuseStrategy},
    {
      provide: APP_INITIALIZER,
      useFactory: configServiceFactory,
      deps: [ConfigService, AuthenticationService, EventListenerService, DataTypesService],
      multi: true
    },
  ],
  bootstrap: [AppComponent]
})

export class AppModule {
}
