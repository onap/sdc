import {ComponentFactory} from "../utils/component-factory";
import {ComponentInstanceFactory} from "../utils/component-instance-factory";
import {ChangeLifecycleStateHandler} from "../utils/change-lifecycle-state-handler";
import {ModalsHandler} from "../utils/modals-handler";
import {MenuHandler} from "../utils/menu-handler";

let moduleName:string = 'Sdc.Utils';
let serviceModule:ng.IModule = angular.module(moduleName, []);

//Utils
serviceModule.service('ComponentFactory', ComponentFactory);
serviceModule.service('ComponentInstanceFactory', ComponentInstanceFactory);
serviceModule.service('ChangeLifecycleStateHandler', ChangeLifecycleStateHandler);
serviceModule.service('ModalsHandler', ModalsHandler);
serviceModule.service('MenuHandler', MenuHandler);

