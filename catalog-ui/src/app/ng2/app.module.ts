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
import {ChangeLifecycleStateHandler} from '../utils/change-lifecycle-state-handler';
import {MenuHandler} from '../utils/menu-handler';
import {ModalsHandler} from '../utils/modals-handler';
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
import {
  OperationCreatorInterfaceDefinitionModule
} from './pages/interface-definition/operation-creator/operation-creator-interface-definition.module';
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
import {ServicePathSelectorModule} from 'app/ng2/pages/composition/graph/service-path-selector/service-path-selector.module';
import {CompositionPanelModule} from 'app/ng2/pages/composition/panel/composition-panel.module';
import {CatalogModule} from './pages/catalog/catalog.module';
import {HomeModule} from './pages/home/home.module';
import {WindowRef} from './services/window.service';
import {CatalogService} from './services/catalog.service';
import {PluginFrameModule} from './components/ui/plugin/plugin-frame.module';
import {PluginsService} from './services/plugins.service';
import {EventBusService} from './services/event-bus.service';
import {GroupsService} from './services/groups.service';
import {PoliciesService} from './services/policies.service';
import {AutomatedUpgradeService} from './pages/automated-upgrade/automated-upgrade.service';
import {AutomatedUpgradeModule} from './pages/automated-upgrade/automated-upgrade.module';
import {WorkspaceModule} from './pages/workspace/workspace.module';
import {AdminDashboardModule} from './pages/admin-dashboard/admin-dashboard.module';
import {PropertyFormModalModule} from './pages/property-form-modal/property-form-modal.module';
import {IconsModalModule} from './components/modals/icons-modal/icons-modal.module';
import {ModulePropertyModalModule} from './pages/module-property-modal/module-property-modal.module';
import {StandaloneStatesModule} from './pages/standalone-states.module';
import {ModalsModule} from './components/modals/modals.module';
import {CacheService, HomeService, SharingService} from 'app/services-ng2';
import {ArtifactConfigService} from "./services/artifact-config.service";
import {IUserProperties} from 'app/models';
import {PluginsModule} from './pages/plugins/plugins-module';
import {WorkspaceNg1BridgeService} from './pages/workspace/workspace-ng1-bridge-service';
import {NgxsModule} from '@ngxs/store';
import {NgxsLoggerPluginModule} from '@ngxs/logger-plugin';
import {NgxsReduxDevtoolsPluginModule} from '@ngxs/devtools-plugin';
import {EventListenerService} from '../services/event-listener-service';
import {CookieService} from '../services/cookie-service';
import {DataTypesService} from '../services/data-types-service';
import {ProgressService} from '../services/progress-service';
import {ValidationUtils} from '../utils/validation-utils';
import {ComponentFactory} from '../utils/component-factory';
import {ComponentService} from '../services/components/component-service';
import {ResourceService} from '../services/components/resource-service';
import {ServiceService} from '../services/components/service-service';
import {LeftPaletteLoaderService} from '../services/components/utils/composition-left-palette-service';
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
import {ToscaFunctionModule} from "./pages/properties-assignment/tosca-function/tosca-function.module";
import {ConstraintsModule} from "./pages/properties-assignment/constraints/constraints.module";
import {PropertyMetadataModule} from "./pages/properties-assignment/property-metadata/property-metadata.module";
import {WorkflowServiceNg2} from './services/workflow.service';
import {ToscaTypesServiceNg2} from "./services/tosca-types.service";
import {InterfaceOperationHandlerModule} from "./pages/composition/interface-operatons/operation-creator/interface-operation-handler.module";
import {AttributesOutputsModule} from "./pages/attributes-outputs/attributes-outputs.module";
import {ElementService} from "./services/element.service";
import {ModelService} from "./services/model.service";
import {ToscaArtifactService} from "./services/tosca-artifact.service";
import {InterfaceDefinitionModule} from "./pages/interface-definition/interface-definition.module";
import {TypeWorkspaceModule} from "./pages/type-workspace/type-workspace.module";
import {DeclareInputModule} from "./pages/properties-assignment/declare-input/declare-input.module";
import {NavigationService} from "./services/navigation.service";
import {AppRoutingModule} from "./app.routing.module";
import {AuthGuard} from "./guards/auth.guard";
import {LocationStrategy} from '@angular/common';
import {SdcHashLocationStrategy} from './utils/sdc-hash-location-strategy';
import {UnsavedChangesFlagGuard} from './guards/unsaved-changes-flag.guard';
import {UnsavedChangesGuard} from './guards/unsaved-changes.guard';
import {WorkspaceComponentResolver} from './pages/workspace/workspace-component.resolver';
import {WorkspaceService} from './pages/workspace/workspace.service';
import {RouteMetadataService} from './services/route-metadata.service';

declare const __ENV__: string;

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
    AdminDashboardModule,
    PropertyFormModalModule,
    IconsModalModule,
    ModulePropertyModalModule,
    StandaloneStatesModule,
    TypeWorkspaceModule,
    ModalsModule,
    CatalogModule,
    HomeModule,
    AppRoutingModule,
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
    DataTypesService,
    SharingService,
    CacheService,
    HomeService,
    ArtifactConfigService,
    ComponentService,
    ResourceService,
    ServiceService,
    ComponentFactory,
    LeftPaletteLoaderService,
    CookieService,
    ProgressService,
    ValidationUtils,
    ModalsHandler,
    MenuHandler,
    ChangeLifecycleStateHandler,
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
    NavigationService,
    RouteMetadataService,
    AuthGuard,
    UnsavedChangesGuard,
    UnsavedChangesFlagGuard,
    WorkspaceComponentResolver,
    // Root-provided, NOT WorkspaceModule-provided: the root NavigationService and the two
    // CanDeactivate guards read the dirty flag off it, and PropertiesAssignmentModule /
    // AttributesOutputsModule inject it without importing WorkspaceModule. One instance in the root
    // injector is also what keeps the flag shared across a tab switch.
    WorkspaceService,
    // Must be provided HERE and not in AppRoutingModule: RouterModule.forRoot() registers its own
    // `provideLocationStrategy` in the module it is imported into, so a competing provider in that
    // same module is ambiguous — the root module's wins.
    {provide: LocationStrategy, useClass: SdcHashLocationStrategy},
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
