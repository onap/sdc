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

import { downgradeInjectable } from '@angular/upgrade/static';
import { CompositionService } from 'app/ng2/pages/composition/composition.service';
import { CatalogService } from 'app/ng2/services/catalog.service';
import { TopologyTemplateService } from 'app/ng2/services/component-services/topology-template.service';
import { DynamicComponentService } from 'app/ng2/services/dynamic-component.service';
import {GabService as GabServiceNg2} from "app/ng2/services/gab.service";
import { ComponentFactory } from 'app/utils/component-factory';
import { SdcUiServices } from 'onap-ui-angular';
import { NodesFactory } from '../models/graph/nodes/nodes-factory';
import { ImportVSPService } from '../ng2/components/modals/onboarding-modal/import-vsp.service';
import { AutomatedUpgradeService } from '../ng2/pages/automated-upgrade/automated-upgrade.service';
import { ConnectionWizardService } from '../ng2/pages/composition/graph/connection-wizard/connection-wizard.service';
import { WorkspaceNg1BridgeService } from '../ng2/pages/workspace/workspace-ng1-bridge-service';
import { WorkspaceService } from '../ng2/pages/workspace/workspace.service';
import { AuthenticationService as AuthenticationServiceNg2 } from '../ng2/services/authentication.service';
import { CacheService } from '../ng2/services/cache.service';
import { ComponentInstanceServiceNg2 } from '../ng2/services/component-instance-services/component-instance.service';
import { ComponentServiceNg2 } from '../ng2/services/component-services/component.service';
import { ComponentServiceFactoryNg2 } from '../ng2/services/component-services/component.service.factory';
import { ServiceServiceNg2 } from '../ng2/services/component-services/service.service';
import { EventBusService } from '../ng2/services/event-bus.service';
import { GroupsService as GroupsServiceNg2 } from '../ng2/services/groups.service';
import { HomeService } from '../ng2/services/home.service';
import { ModalService } from '../ng2/services/modal.service';
import { OnboardingService } from '../ng2/services/onboarding.service';
import { PluginsService } from '../ng2/services/plugins.service';
import { PoliciesService as PoliciesServiceNg2 } from '../ng2/services/policies.service';
import { SharingService } from '../ng2/services/sharing.service';
import { ToscaTypesServiceNg2 } from '../ng2/services/tosca-types.service';
import { UserService as UserServiceNg2 } from '../ng2/services/user.service';
import { AngularJSBridge } from '../services/angular-js-bridge-service';
import { AvailableIconsService } from '../services/available-icons-service';
import { CategoryResourceService } from '../services/category-resource-service';
import { ComponentService } from '../services/components/component-service';
import { ResourceService } from '../services/components/resource-service';
import { ServiceService } from '../services/components/service-service';
import { LeftPaletteLoaderService } from '../services/components/utils/composition-left-palette-service';
import { ConfigurationUiService } from '../services/configuration-ui-service';
import { CookieService } from '../services/cookie-service';
import { DataTypesService } from '../services/data-types-service';
import { EcompHeaderService } from '../services/ecomp-service';
import { EventListenerService } from '../services/event-listener-service';
import { HeaderInterceptor } from '../services/header-interceptor';
import { LoaderService } from '../services/loader-service';
import { ProgressService } from '../services/progress-service';
import { SdcVersionService } from '../services/sdc-version-service';
import { UrlToBase64Service } from '../services/url-tobase64-service';
import { FileUtils } from '../utils/file-utils';
import { ValidationUtils } from '../utils/validation-utils';
import {ReqAndCapabilitiesService} from "../ng2/pages/workspace/req-and-capabilities/req-and-capabilities.service";

const moduleName: string = 'Sdc.Services';
const serviceModule: ng.IModule = angular.module(moduleName, []);

serviceModule.service('Sdc.Services.ConfigurationUiService', ConfigurationUiService);
serviceModule.service('Sdc.Services.CookieService', CookieService);
serviceModule.service('Sdc.Services.ComponentFactory', ComponentFactory); // Why you need to declare it again, already done in utils.ts
serviceModule.service('Sdc.Services.AvailableIconsService', AvailableIconsService);
serviceModule.service('Sdc.Services.UrlToBase64Service', UrlToBase64Service);
serviceModule.service('Sdc.Services.HeaderInterceptor', HeaderInterceptor);
serviceModule.service('Sdc.Services.SdcVersionService', SdcVersionService);
serviceModule.service('Sdc.Services.EcompHeaderService', EcompHeaderService);
serviceModule.service('Sdc.Services.DataTypesService', DataTypesService);

// Components Services
serviceModule.service('Sdc.Services.Components.ComponentService', ComponentService);
serviceModule.service('Sdc.Services.Components.ServiceService', ServiceService);
serviceModule.service('Sdc.Services.Components.ResourceService', ResourceService);
serviceModule.service('LeftPaletteLoaderService', LeftPaletteLoaderService);
serviceModule.service('Sdc.Services.ProgressService', ProgressService);

// Utils
serviceModule.service('FileUtils', FileUtils);
serviceModule.service('ValidationUtils', ValidationUtils);
serviceModule.service('AngularJSBridge', AngularJSBridge);
serviceModule.service('LoaderService', LoaderService);
serviceModule.factory('Sdc.Services.CategoryResourceService', CategoryResourceService.getResource);

// Angular2 upgraded services - This is in order to use the service in angular1 till we finish remove all angular1 code
serviceModule.service('Sdc.Services.SharingService', downgradeInjectable(SharingService));
serviceModule.service('Sdc.Services.CacheService', downgradeInjectable(CacheService));
serviceModule.factory('ComponentServiceNg2', downgradeInjectable(ComponentServiceNg2));
serviceModule.factory('TopologyTemplateService', downgradeInjectable(TopologyTemplateService));
serviceModule.factory('WorkspaceNg1BridgeService', downgradeInjectable(WorkspaceNg1BridgeService));
serviceModule.factory('workspaceService', downgradeInjectable(WorkspaceService));
serviceModule.factory('ComponentServiceFactoryNg2', downgradeInjectable(ComponentServiceFactoryNg2));
serviceModule.factory('ServiceServiceNg2', downgradeInjectable(ServiceServiceNg2));
serviceModule.factory('ModalServiceNg2', downgradeInjectable(ModalService));
serviceModule.factory('ModalServiceSdcUI', downgradeInjectable(SdcUiServices.ModalService));
serviceModule.factory('ConnectionWizardServiceNg2', downgradeInjectable(ConnectionWizardService));
serviceModule.factory('ComponentInstanceServiceNg2', downgradeInjectable(ComponentInstanceServiceNg2));
serviceModule.factory('UserServiceNg2', downgradeInjectable(UserServiceNg2));
serviceModule.factory('AuthenticationServiceNg2', downgradeInjectable(AuthenticationServiceNg2));
serviceModule.factory('PoliciesServiceNg2', downgradeInjectable(PoliciesServiceNg2));
serviceModule.factory('GroupsServiceNg2', downgradeInjectable(GroupsServiceNg2));
serviceModule.factory('PluginsService', downgradeInjectable(PluginsService));
serviceModule.factory('EventBusService', downgradeInjectable(EventBusService));
serviceModule.factory('DynamicComponentService', downgradeInjectable(DynamicComponentService));
serviceModule.factory('HomeService', downgradeInjectable(HomeService));
serviceModule.factory('CatalogService', downgradeInjectable(CatalogService));
serviceModule.factory('GabServiceNg2', downgradeInjectable(GabServiceNg2));
serviceModule.factory('AutomatedUpgradeService', downgradeInjectable(AutomatedUpgradeService));
serviceModule.factory('ToscaTypesServiceNg2', downgradeInjectable(ToscaTypesServiceNg2));
serviceModule.factory('EventListenerService', downgradeInjectable(EventListenerService));
serviceModule.factory('CompositionService', downgradeInjectable(CompositionService));
serviceModule.factory('ReqAndCapabilitiesService', downgradeInjectable(ReqAndCapabilitiesService));
serviceModule.factory('NodesFactory', downgradeInjectable(NodesFactory));
serviceModule.service('OnboardingService', downgradeInjectable(OnboardingService));
serviceModule.service('ImportVSPService', downgradeInjectable(ImportVSPService));
