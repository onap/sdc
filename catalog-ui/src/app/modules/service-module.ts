import {ConfigurationUiService} from "../services/configuration-ui-service";
import {CookieService} from "../services/cookie-service";
import {EntityService} from "../services/entity-service";
import {AvailableIconsService} from "../services/available-icons-service";
import {UrlToBase64Service} from "../services/url-tobase64-service";
import {CacheService} from "../services/cache-service";
import {HeaderInterceptor} from "../services/header-interceptor";
import {HttpErrorInterceptor} from "../services/http-error-interceptor";
import {SharingService} from "../services/sharing-service";
import {SdcVersionService} from "../services/sdc-version-service";
import {ActivityLogService} from "../services/activity-log-service";
import {OnboardingService} from "../services/onboarding-service";
import {EcompHeaderService} from "../services/ecomp-service";
import {DataTypesService} from "../services/data-types-service";
import {ComponentService} from "../services/components/component-service";
import {ServiceService} from "../services/components/service-service";
import {ResourceService} from "../services/components/resource-service";
import {ProductService} from "../services/components/product-service";
import {LeftPaletteLoaderService} from "../services/components/utils/composition-left-palette-service";
import {EventListenerService} from "../services/event-listener-service";
import {ProgressService} from "../services/progress-service";
import {ArtifactsUtils} from "../utils/artifacts-utils";
import {FileUtils} from "../utils/file-utils";
import {ValidationUtils} from "../utils/validation-utils";
import {AngularJSBridge} from "../services/angular-js-bridge-service";
import {LoaderService} from "../services/loader-service";
import {UserResourceService} from "../services/user-resource-service";
import {CategoryResourceService} from "../services/category-resource-service";

let moduleName:string = 'Sdc.Services';
let serviceModule:ng.IModule = angular.module(moduleName, []);

serviceModule.service('Sdc.Services.ConfigurationUiService', ConfigurationUiService);
serviceModule.service('Sdc.Services.CookieService', CookieService);
serviceModule.service('Sdc.Services.EntityService', EntityService);
serviceModule.service('Sdc.Services.AvailableIconsService', AvailableIconsService);
serviceModule.service('Sdc.Services.UrlToBase64Service', UrlToBase64Service);
serviceModule.service('Sdc.Services.CacheService', CacheService);
serviceModule.service('Sdc.Services.HeaderInterceptor', HeaderInterceptor);
serviceModule.service('Sdc.Services.HttpErrorInterceptor', HttpErrorInterceptor);
serviceModule.service('Sdc.Services.SharingService', SharingService);
serviceModule.service('Sdc.Services.SdcVersionService', SdcVersionService);
serviceModule.service('Sdc.Services.ActivityLogService', ActivityLogService);
serviceModule.service('Sdc.Services.OnboardingService', OnboardingService);
serviceModule.service('Sdc.Services.EcompHeaderService', EcompHeaderService);
serviceModule.service('Sdc.Services.DataTypesService', DataTypesService);

//Components Services
serviceModule.service('Sdc.Services.Components.ComponentService', ComponentService);
serviceModule.service('Sdc.Services.Components.ServiceService',ServiceService);
serviceModule.service('Sdc.Services.Components.ResourceService', ResourceService);
serviceModule.service('Sdc.Services.Components.ProductService', ProductService);
serviceModule.service('LeftPaletteLoaderService', LeftPaletteLoaderService);
serviceModule.service('EventListenerService', EventListenerService);
serviceModule.service('Sdc.Services.ProgressService', ProgressService);

//Utils
serviceModule.service('ArtifactsUtils', ArtifactsUtils);
serviceModule.service('FileUtils', FileUtils);
serviceModule.service('ValidationUtils', ValidationUtils);

serviceModule.service('AngularJSBridge',AngularJSBridge);
serviceModule.service('LoaderService', LoaderService);

serviceModule.factory('Sdc.Services.UserResourceService', UserResourceService.getResource);
serviceModule.factory('Sdc.Services.CategoryResourceService', CategoryResourceService.getResource);
