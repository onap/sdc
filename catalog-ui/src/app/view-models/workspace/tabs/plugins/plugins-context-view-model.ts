/*

* Copyright (c) 2018 AT&T Intellectual Property.

*

* Licensed under the Apache License, Version 2.0 (the "License");

* you may not use this file except in compliance with the License.

* You may obtain a copy of the License at

*

*     http://www.apache.org/licenses/LICENSE-2.0

*

* Unless required by applicable law or agreed to in writing, software

* distributed under the License is distributed on an "AS IS" BASIS,

* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

* See the License for the specific language governing permissions and

* limitations under the License.

*/

import {Plugin, IUserProperties} from "app/models";
import {CacheService} from "app/services";
import {PluginsService} from "../../../../ng2/services/plugins.service";
import {IWorkspaceViewModelScope} from "../../workspace-view-model";


interface IPluginsContextViewModelScope extends IWorkspaceViewModelScope {
    plugin: Plugin;
    user:IUserProperties;
    queryParams: Object;
    isLoading: boolean;
    show: boolean;

    onLoadingDone(plugin: Plugin): void;
}

export class PluginsContextViewModel {
    static '$inject' = [
        '$scope',
        '$stateParams',
        'Sdc.Services.CacheService',
        'PluginsService'
    ];

    constructor(private $scope:IPluginsContextViewModelScope,
                private $stateParams:any,
                private cacheService:CacheService,
                private pluginsService:PluginsService) {

        this.initScope();
    }

    private initScope = ():void => {
        this.$scope.show = false;
        this.$scope.plugin = this.pluginsService.getPluginByStateUrl(this.$stateParams.path);
        this.$scope.user = this.cacheService.get('user');

        // Don't show loader if the plugin isn't online
        if (this.$scope.plugin.isOnline) {
            this.$scope.isLoading = true;
        }

        this.$scope.queryParams = {
            userId: this.$scope.user.userId,
            userRole: this.$scope.user.role,
            displayType: "context",
            contextType: this.$scope.component.getComponentSubType(),
            uuid: this.$scope.component.uuid,
            lifecycleState: this.$scope.component.lifecycleState,
            isOwner: this.$scope.component.lastUpdaterUserId === this.$scope.user.userId,
            version: this.$scope.component.version,
            parentUrl: window.location.origin,
            eventsClientId: this.$scope.plugin.pluginId
        };

        if (this.$stateParams.queryParams) {
            _.assign(this.$scope.queryParams, this.$stateParams.queryParams);
        }

        this.$scope.onLoadingDone = (plugin: Plugin) => {
            if (plugin.pluginId == this.$scope.plugin.pluginId) {
                this.$scope.isLoading = false;
            }
        };

    }
}
