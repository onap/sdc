/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
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

import {Component, OnInit} from "@angular/core";
import {Component as ComponentData, IUserProperties, Plugin} from "app/models";
import {CacheService, PluginsService} from "app/services-ng2";
import {NavigationService} from "../../../services/navigation.service";
import {WorkspaceService} from "../../workspace/workspace.service";


@Component({
    selector: 'plugin-context-view',
    templateUrl: './plugin-context-view.page.component.html',
    styleUrls: ['./plugin-context-view.page.component.less']
})

export class PluginContextViewPageComponent implements OnInit {
    plugin: Plugin;
    user: IUserProperties;
    queryParams: Object;
    isLoading: boolean;
    show: boolean;
    component: ComponentData;

    constructor(private navigationService: NavigationService,
                private cacheService: CacheService,
                private pluginsService: PluginsService,
                private workspaceService: WorkspaceService) {

        this.show = false;
    }

    // Everything route-derived is read in ngOnInit, not the constructor: the route resolver that
    // writes WorkspaceService.component, and the router's own param snapshot, are only guaranteed
    // to be in place by ngOnInit.
    ngOnInit() {
        this.isLoading = true;
        this.component = this.workspaceService.component;
        this.plugin = this.pluginsService.getPluginByStateUrl(this.navigationService.getParam('path'));
        this.user = this.cacheService.get('user');

        this.queryParams = {
            userId: this.user.userId,
            userRole: this.user.role,
            displayType: "context",
            contextType: this.component.getComponentSubType(),
            uuid: this.component.uuid,
            componentId: this.component.uniqueId,
            lifecycleState: this.component.lifecycleState,
            isOwner: this.component.lastUpdaterUserId === this.user.userId,
            version: this.component.version,
            parentUrl: window.location.origin,
            eventsClientId: this.plugin.pluginId
        };

        const stateQueryParams = this.navigationService.getParam('queryParams');
        if (stateQueryParams) {
            _.assign(this.queryParams, stateQueryParams);
        }
    }

    onLoadingDone(plugin: Plugin) {
        if (plugin.pluginId == this.plugin.pluginId) {
            this.isLoading = false;
        }
    }


}
