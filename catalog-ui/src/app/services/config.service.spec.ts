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

import {HttpErrorResponse, HttpResponse} from '@angular/common/http';
import {of} from 'rxjs/observable/of';
import {_throw} from 'rxjs/observable/throw';
import {ConfigService} from './config.service';

function serviceAnswering(body: any, status: number = 200) {
    const cacheService: any = {set: jest.fn()};
    const http: any = {get: jest.fn(() => { throw new Error('the intercepted HttpClient was used'); })};
    const backend: any = {handle: jest.fn(() => status < 300
        ? of(new HttpResponse({body, status}))
        : _throw(new HttpErrorResponse({status, url: '/sdc1/rest/config/ui/workspace'})))};
    const sdcConfig: any = {api: {root: '/sdc1/feProxy/rest', component_api_root: '/v1/catalog/',
        no_proxy_root: '/sdc1/rest', GET_workspace_configuration: '/config/ui/workspace'}};
    const service = new ConfigService(sdcConfig, cacheService, null, http, backend);
    return {service, cacheService, backend};
}

describe('ConfigService.loadWorkspaceConfiguration', () => {
    it('reads the automated-upgrade resource types from the FE', async () => {
        const {service, cacheService, backend} = serviceAnswering({automatedUpgradeResourceTypes: ['VF', 'CR']});
        await service.loadWorkspaceConfiguration();
        expect(backend.handle.mock.calls[0][0].url).toBe('/sdc1/rest/config/ui/workspace');
        expect(cacheService.set).toHaveBeenCalledWith('automatedUpgradeResourceTypes', ['VF', 'CR']);
    });

    it('falls back to VF when the key is absent', async () => {
        const {service, cacheService} = serviceAnswering({workspaceMenuConfiguration: {}});
        await service.loadWorkspaceConfiguration();
        expect(cacheService.set).toHaveBeenCalledWith('automatedUpgradeResourceTypes', ['VF']);
    });

    it('falls back to VF when the key is not a list', async () => {
        const {service, cacheService} = serviceAnswering({automatedUpgradeResourceTypes: 'CR'});
        await service.loadWorkspaceConfiguration();
        expect(cacheService.set).toHaveBeenCalledWith('automatedUpgradeResourceTypes', ['VF']);
    });

    it('falls back to VF and still resolves when the FE has no workspace configuration', async () => {
        const {service, cacheService} = serviceAnswering(null, 404);
        await expect(service.loadWorkspaceConfiguration()).resolves.toBeUndefined();
        expect(cacheService.set).toHaveBeenCalledWith('automatedUpgradeResourceTypes', ['VF']);
    });
});

describe('ConfigService.loadWorkspaceConfiguration crBaseTypes', () => {
    const RAN = 'org.openecomp.resource.abstract.nodes.RANTemplateExecutionTask';

    it('caches the CR base types per category', async () => {
        const {service, cacheService} = serviceAnswering({crBaseTypes: {Task: [RAN]}});
        await service.loadWorkspaceConfiguration();
        expect(cacheService.set).toHaveBeenCalledWith('crBaseTypes', {Task: [RAN]});
    });

    it('caches none when the key is absent', async () => {
        const {service, cacheService} = serviceAnswering({workspaceMenuConfiguration: {}});
        await service.loadWorkspaceConfiguration();
        expect(cacheService.set).toHaveBeenCalledWith('crBaseTypes', {});
    });

    it('drops malformed entries', async () => {
        const {service, cacheService} = serviceAnswering({crBaseTypes: {Task: RAN, Other: [RAN, 7], Empty: [], Ok: [RAN]}});
        await service.loadWorkspaceConfiguration();
        expect(cacheService.set).toHaveBeenCalledWith('crBaseTypes', {Ok: [RAN]});
    });

    it('caches none when crBaseTypes is not a map', async () => {
        const {service, cacheService} = serviceAnswering({crBaseTypes: [RAN]});
        await service.loadWorkspaceConfiguration();
        expect(cacheService.set).toHaveBeenCalledWith('crBaseTypes', {});
    });

    it('caches none when the FE has no workspace configuration', async () => {
        const {service, cacheService} = serviceAnswering(null, 404);
        await expect(service.loadWorkspaceConfiguration()).resolves.toBeUndefined();
        expect(cacheService.set).toHaveBeenCalledWith('crBaseTypes', {});
    });
});
