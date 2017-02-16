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
/// <reference path="../references"/>
module Sdc {
    let moduleName:string = 'Sdc.Services';
    let serviceModule:ng.IModule = angular.module(moduleName, []);

    serviceModule.service('Sdc.Services.ConfigurationUiService', Services.ConfigurationUiService);
    serviceModule.service('Sdc.Services.CookieService', Services.CookieService);
    serviceModule.service('Sdc.Services.EntityService', Services.EntityService);
    serviceModule.service('Sdc.Services.AvailableIconsService', Services.AvailableIconsService);
    serviceModule.service('Sdc.Services.RelationIconsService', Services.RelationIconsService);
    serviceModule.service('Sdc.Services.UrlToBase64Service', Services.UrlToBase64Service);
    serviceModule.service('Sdc.Services.CacheService', Services.CacheService);
    serviceModule.service('Sdc.Services.HeaderInterceptor', Services.HeaderInterceptor);
    serviceModule.service('Sdc.Services.HttpErrorInterceptor', Services.HttpErrorInterceptor);
    serviceModule.service('Sdc.Services.SharingService', Services.SharingService);
    serviceModule.service('Sdc.Services.SdcVersionService', Services.SdcVersionService);
    serviceModule.service('Sdc.Services.ActivityLogService', Services.ActivityLogService);
    serviceModule.service('Sdc.Services.OnboardingService', Services.OnboardingService);
    serviceModule.service('Sdc.Services.EcompHeaderService', Services.EcompHeaderService);
    serviceModule.service('Sdc.Services.DataTypesService', Services.DataTypesService);

    //Components Services
    serviceModule.service('Sdc.Services.Components.ComponentService', Services.Components.ComponentService);
    serviceModule.service('Sdc.Services.Components.ServiceService', Services.Components.ServiceService);
    serviceModule.service('Sdc.Services.Components.ResourceService', Services.Components.ResourceService);
    serviceModule.service('Sdc.Services.Components.ProductService', Services.Components.ProductService);
    serviceModule.service('LeftPaletteLoaderService', Services.Components.LeftPaletteLoaderService);
    serviceModule.service('EventListenerService', Services.EventListenerService);
    serviceModule.service('Sdc.Services.ProgressService', Services.ProgressService);

    //Utils
    serviceModule.service('ArtifactsUtils', Sdc.Utils.ArtifactsUtils);
    serviceModule.service('FileUtils', Sdc.Utils.FileUtils);
    serviceModule.service('ValidationUtils', Sdc.Utils.ValidationUtils);




    serviceModule.service('AngularJSBridge', Sdc.Services.AngularJSBridge);
    serviceModule.service('LoaderService', Sdc.Services.LoaderService);

    serviceModule.factory('Sdc.Services.UserResourceService', Services.UserResourceService.getResource);
    serviceModule.factory('Sdc.Services.CategoryResourceService', Services.CategoryResourceService.getResource);

}
