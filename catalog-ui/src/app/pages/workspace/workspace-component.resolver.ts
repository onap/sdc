/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {Component} from 'app/models/components/component';
import {Resource} from 'app/models/components/resource';
import {ComponentFactory} from 'app/utils/component-factory';
import {PREVIOUS_CSAR_COMPONENT} from 'app/utils/constants';
import {CacheService} from '../../services/cache.service';
import {NavigationService} from '../../services/navigation.service';
import {WorkspaceService} from './workspace.service';
import * as _ from 'lodash';

/**
 * Port of the `injectComponent` ui-router resolve (app.ts). Every workspace tab depended on it
 * having run and having called workspaceService.setComponent() before the tab's own code executed.
 *
 * The four object-valued params (componentCsar, importedFile, resourceType, disableButtons) were
 * ui-router `params: {x: null}` entries — carried in memory, never in the URL. The Angular Router
 * has no equivalent before 7.2 (NavigationExtras.state), so NavigationService keeps them in a
 * transient store; getParam() reads from there, which is why this resolver goes through
 * NavigationService rather than the route snapshot for them.
 */
@Injectable()
export class WorkspaceComponentResolver implements Resolve<Component> {

    constructor(private componentFactory: ComponentFactory,
                private workspaceService: WorkspaceService,
                private navigationService: NavigationService,
                private cacheService: CacheService) {
    }

    public resolve(route: ActivatedRouteSnapshot): Promise<Component> | Component {
        const id: string = route.params['id'];
        const type: string = route.params['type'];
        const componentCsar: any = this.navigationService.getParam('componentCsar');

        if (id && id.length) { // need to check length in case ID is an empty string
            return Promise.resolve(
                this.componentFactory.getComponentWithMetadataFromServer(type.toUpperCase(), id) as any
            ).then((component: Component) => {
                if (componentCsar && component.isResource()) {
                    if ((component as Resource).csarVersion !== componentCsar.csarVersion) {
                        this.cacheService.set(PREVIOUS_CSAR_COMPONENT, _.cloneDeep(component));
                    }
                    component = this.componentFactory.updateComponentFromCsar(componentCsar, component as Resource);
                }
                this.workspaceService.setComponent(component);
                return component;
            });
        }

        if (componentCsar && componentCsar.csarUUID) {
            this.workspaceService.setComponent(componentCsar);
            return componentCsar;
        }

        const emptyComponent = this.componentFactory.createEmptyComponent(type.toUpperCase());
        const resourceType: string = this.navigationService.getParam('resourceType');
        const importedFile: any = this.navigationService.getParam('importedFile');
        if (emptyComponent.isResource() && resourceType) {
            (emptyComponent as Resource).resourceType = resourceType;
        }
        if (importedFile) {
            (emptyComponent as Resource).importedFile = importedFile;
        }
        this.workspaceService.setComponent(emptyComponent);
        return emptyComponent;
    }
}
