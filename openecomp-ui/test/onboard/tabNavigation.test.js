/*
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import ItemsHelper from 'sdc-app/common/helpers/ItemsHelper.js';
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';
import VersionsPageActionHelper from 'sdc-app/onboarding/versionsPage/VersionsPageActionHelper.js';
import { enums, screenTypes } from 'sdc-app/onboarding/OnboardingConstants.js';
import mockRest from 'test-utils/MockRest.js';

jest.mock('sdc-app/onboarding/OnboardingActionHelper.js', () => ({
    navigateToFeatureGroups: jest.fn(),
    navigateToEntitlementPools: jest.fn(),
    navigateToLicenseKeyGroups: jest.fn(),
    navigateToLicenseModelActivityLog: jest.fn(),
    navigateToSoftwareProductDetails: jest.fn(),
    navigateToVersionsPage: jest.fn()
}));
jest.mock(
    'sdc-app/onboarding/versionsPage/VersionsPageActionHelper.js',
    () => ({
        fetchVersions: jest.fn()
    })
);

const version = { id: 'version-id', status: 'Draft', name: '1.0' };
const item = { status: 'ACTIVE' };
const itemUrl = '/onboarding-api/v1.0/items/item-id';
const versionUrl = `${itemUrl}/versions/${version.id}`;

describe('Onboarding tab metadata', () => {
    let dispatch;
    let fetch;
    beforeEach(() => {
        jest.clearAllMocks();
        dispatch = jest.fn();
        fetch = jest.spyOn(mockRest, 'fetch').mockImplementation(url => {
            if (url === itemUrl) {
                return Promise.resolve(item);
            }
            if (url === versionUrl) {
                return Promise.resolve(version);
            }
            throw new Error(`Unexpected request: ${url}`);
        });
    });
    afterEach(() => jest.restoreAllMocks());

    it.each([
        [enums.SCREEN.FEATURE_GROUPS, 'navigateToFeatureGroups'],
        [enums.SCREEN.ENTITLEMENT_POOLS, 'navigateToEntitlementPools'],
        [enums.SCREEN.LICENSE_KEY_GROUPS, 'navigateToLicenseKeyGroups'],
        [enums.SCREEN.ACTIVITY_LOG, 'navigateToLicenseModelActivityLog']
    ])(
        'fetches item metadata once when opening %s',
        async (screen, navigate) => {
            await ScreensHelper.loadScreen(dispatch, {
                screen,
                screenType: screenTypes.LICENSE_MODEL,
                props: { licenseModelId: 'item-id', version }
            });
            expect(fetch.mock.calls.map(args => args[0])).toEqual([
                itemUrl,
                versionUrl
            ]);
            expect(OnboardingActionHelper[navigate]).toHaveBeenCalledWith(
                dispatch,
                {
                    licenseModelId: 'item-id',
                    version: { ...version, archivedStatus: item.status },
                    status: item.status
                }
            );
        }
    );

    it('starts item and version requests together and waits for both', async () => {
        let resolveItem;
        fetch.mockImplementation(
            url =>
                url === itemUrl
                    ? new Promise(resolve => {
                          resolveItem = resolve;
                      })
                    : Promise.resolve(version)
        );
        const navigation = ScreensHelper.loadScreen(dispatch, {
            screen: enums.SCREEN.FEATURE_GROUPS,
            props: { licenseModelId: 'item-id', version }
        });
        expect(fetch).toHaveBeenCalledTimes(2);
        expect(
            OnboardingActionHelper.navigateToFeatureGroups
        ).not.toHaveBeenCalled();
        resolveItem(item);
        await navigation;
        expect(
            OnboardingActionHelper.navigateToFeatureGroups
        ).toHaveBeenCalledTimes(1);
    });

    it('also avoids duplicate metadata in software product tabs', async () => {
        await ScreensHelper.loadScreen(dispatch, {
            screen: enums.SCREEN.SOFTWARE_PRODUCT_DETAILS,
            props: { softwareProductId: 'item-id', version }
        });
        expect(fetch.mock.calls.map(args => args[0])).toEqual([
            itemUrl,
            versionUrl
        ]);
        expect(
            OnboardingActionHelper.navigateToSoftwareProductDetails
        ).toHaveBeenCalledTimes(1);
    });

    it('retains live certification checks on subsequent tab switches', async () => {
        await ScreensHelper.loadScreen(dispatch, {
            screen: enums.SCREEN.FEATURE_GROUPS,
            props: { licenseModelId: 'item-id', version }
        });
        fetch.mockImplementation(url =>
            Promise.resolve(
                url === itemUrl ? item : { ...version, status: 'Certified' }
            )
        );
        await ScreensHelper.loadScreen(dispatch, {
            screen: enums.SCREEN.ENTITLEMENT_POOLS,
            props: { licenseModelId: 'item-id', version }
        });
        expect(VersionsPageActionHelper.fetchVersions).toHaveBeenCalledTimes(1);
        expect(dispatch).toHaveBeenCalledWith(
            expect.objectContaining({
                data: expect.objectContaining({ title: 'Commit error' })
            })
        );
    });

    it('propagates failed metadata requests without navigating', async () => {
        fetch.mockImplementation(
            url =>
                url === versionUrl
                    ? Promise.reject(new Error('version unavailable'))
                    : Promise.resolve(item)
        );
        await expect(
            ScreensHelper.loadScreen(dispatch, {
                screen: enums.SCREEN.FEATURE_GROUPS,
                props: { licenseModelId: 'item-id', version }
            })
        ).rejects.toThrow('version unavailable');
        expect(
            OnboardingActionHelper.navigateToFeatureGroups
        ).not.toHaveBeenCalled();
    });

    it('still supports callers that check status outside tab navigation', async () => {
        await expect(
            ItemsHelper.checkItemStatus(dispatch, {
                itemId: 'item-id',
                versionId: version.id
            })
        ).resolves.toEqual({ ...version, archivedStatus: item.status });
        expect(fetch).toHaveBeenCalledTimes(2);
    });
});
