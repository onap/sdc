/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import {mapStateToProps} from 'sdc-app/onboarding/licenseModel/overview/LicenseModelOverview.js';
import {overviewEditorHeaders, selectedButton} from 'sdc-app/onboarding/licenseModel/overview/LicenseModelOverviewConstants.js';

import {LicenseModelOverviewFactory} from 'test-utils/factories/licenseModel/LicenseModelFactories.js';
import { EntitlementPoolStoreFactory as EntitlementPool, EntitlementPoolDataListFactory } from 'test-utils/factories/licenseModel/EntitlementPoolFactories.js';
import { FeatureGroupStoreFactory as FeatureGroup, FeatureGroupDataListFactory} from 'test-utils/factories/licenseModel/FeatureGroupFactories.js';
import {LicenseAgreementStoreFactory as LicenseAgreement, LicenseAgreementDataListFactory} from 'test-utils/factories/licenseModel/LicenseAgreementFactories.js';
import { LicenseKeyGroupStoreFactory as LicenseKeyGroup, LicenseKeyGroupDataListFactory} from 'test-utils/factories/licenseModel/LicenseKeyGroupFactories.js';

describe('License Model Overview: ', function () {

	it('should mapper exist', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	const VLM1 = LicenseModelOverviewFactory.build({
		featureGroup: {
			featureGroupsList: [],
		},
		entitlementPool: {
			entitlementPoolsList: []
		},
		licenseKeyGroup: {
			licenseKeyGroupsList: []
		}
	});

	it('should mapper return vlm overview basic data', () => {
		const state = {
			licenseModel: VLM1
		};

		var props = mapStateToProps(state);
		expect(props.isDisplayModal).toEqual(false);
		expect(props.modalHeader).toEqual(undefined);
		expect(props.licenseModelId).toEqual(VLM1.licenseModelEditor.data.id);
		expect(props.licensingDataList).toEqual([]);
		expect(props.orphanDataList).toEqual([]);
		expect(props.selectedTab).toEqual(selectedButton.VLM_LIST_VIEW);
	});

	it('should mapper return overview data for show LA modal', () => {
		const VLM1 = LicenseModelOverviewFactory.build({
			licenseAgreement: {
				licenseAgreementEditor: {
					data: LicenseAgreement.build()
				}
			},
			featureGroup: {
				featureGroupsList: [],
			},
			entitlementPool: {
				entitlementPoolsList: []
			},
			licenseKeyGroup: {
				licenseKeyGroupsList: []
			}
		});

		var state = {
			licenseModel: VLM1
		};

		var props = mapStateToProps(state);
		expect(props.isDisplayModal).toEqual(true);
		expect(props.modalHeader).toEqual(overviewEditorHeaders.LICENSE_AGREEMENT);
		expect(props.licenseModelId).toEqual(VLM1.licenseModelEditor.data.id);
		expect(props.licensingDataList).toEqual([]);
		expect(props.selectedTab).toEqual(selectedButton.VLM_LIST_VIEW);
	});

	it('should mapper return overview data for show FG modal', () => {

		const VLM1 = LicenseModelOverviewFactory.build({
			featureGroup: {
				featureGroupsList: [],
				featureGroupEditor: {
					data: FeatureGroup.build()
				}
			},
			entitlementPool: {
				entitlementPoolsList: []
			},
			licenseKeyGroup: {
				licenseKeyGroupsList: []
			},
			licenseModelOverview: {
				selectedTab: selectedButton.NOT_IN_USE
			}
		});

		var state = {
			licenseModel: VLM1
		};

		var props = mapStateToProps(state);
		expect(props.isDisplayModal).toEqual(true);
		expect(props.modalHeader).toEqual(overviewEditorHeaders.FEATURE_GROUP);
		expect(props.licenseModelId).toEqual(VLM1.licenseModelEditor.data.id);
		expect(props.licensingDataList).toEqual([]);
		expect(props.selectedTab).toEqual(selectedButton.NOT_IN_USE);
	});

	it('should mapper return overview data for show EP modal', () => {
		const VLM1 = LicenseModelOverviewFactory.build({
			featureGroup: {
				featureGroupsList: [],
			},
			entitlementPool: {
				entitlementPoolsList: [],
				entitlementPoolEditor: {
					data: EntitlementPool.build()
				}
			},
			licenseKeyGroup: {
				licenseKeyGroupsList: []
			}
		});

		var state = {
			licenseModel: VLM1
		};

		var props = mapStateToProps(state);
		expect(props.isDisplayModal).toEqual(true);
		expect(props.modalHeader).toEqual(overviewEditorHeaders.ENTITLEMENT_POOL);
		expect(props.licenseModelId).toEqual(VLM1.licenseModelEditor.data.id);
		expect(props.licensingDataList).toEqual([]);
		expect(props.selectedTab).toEqual(selectedButton.VLM_LIST_VIEW);
	});

	it('should mapper return overview data for show LKG modal', () => {
		const VLM1 = LicenseModelOverviewFactory.build({
			licenseKeyGroup: {
				licenseKeyGroupsList: [],
				licenseKeyGroupsEditor: {
					data: LicenseKeyGroup.build()
				}
			},
			entitlementPool: {
				entitlementPoolsList: []
			},
			featureGroup: {
				featureGroupsList: []
			},
			licenseModelOverview: {
				selectedTab: selectedButton.NOT_IN_USE
			}
		});

		var state = {
			licenseModel: VLM1
		};

		var props = mapStateToProps(state);
		expect(props.isDisplayModal).toEqual(true);
		expect(props.modalHeader).toEqual(overviewEditorHeaders.LICENSE_KEY_GROUP);
		expect(props.licenseModelId).toEqual(VLM1.licenseModelEditor.data.id);
		expect(props.licensingDataList).toEqual([]);
		expect(props.selectedTab).toEqual(selectedButton.NOT_IN_USE);
	});

	it('should mapper return overview data for Full-hierarchy list view', () => {
		let EP1 = EntitlementPool.build();
		let LKG1 = LicenseKeyGroup.build();
		let FG1 = FeatureGroup.build({
			entitlementPoolsIds: [EP1.id],
			licenseKeyGroupsIds: [LKG1.id]
		});
		EP1.referencingFeatureGroups = [FG1.id];
		LKG1.referencingFeatureGroups = [FG1.id];
		let LA1 = LicenseAgreement.build({
			featureGroupsIds: [FG1.id]
		});
		FG1.referencingLicenseAgreements = LA1.id;
		let LA2 = LicenseAgreement.build();

		const VLM1 = LicenseModelOverviewFactory.build({
			licenseAgreement: {
				licenseAgreementList: [LA1, LA2]
			},
			featureGroup: {
				featureGroupsList: [FG1]
			},
			entitlementPool: {
				entitlementPoolsList: [EP1]
			},
			licenseKeyGroup: {
				licenseKeyGroupsList: [LKG1]
			},
		});

		const state = {
			licenseModel: VLM1
		};

		const expectedLicensingDataList = [
			LicenseAgreementDataListFactory.build({
				...LA1,
				children: [
					FeatureGroupDataListFactory.build({
						...FG1,
						children: [
							EntitlementPoolDataListFactory.build(EP1),
							LicenseKeyGroupDataListFactory.build(LKG1)
						]
					})
				]
			}),
			LicenseAgreementDataListFactory.build(LA2)
		];

		var props = mapStateToProps(state);

		expect(props.isDisplayModal).toEqual(false);
		expect(props.modalHeader).toEqual(undefined);
		expect(props.licenseModelId).toEqual(VLM1.licenseModelEditor.data.id);
		expect(props.licensingDataList).toEqual(expectedLicensingDataList);
		expect(props.selectedTab).toEqual(selectedButton.VLM_LIST_VIEW);
	});

	it('should mapper return overview data for list view with 2 levels', () => {
		let EP1 = EntitlementPool.build();
		let LKG1 = LicenseKeyGroup.build();
		let FG1 = FeatureGroup.build();
		let LA1 = LicenseAgreement.build({
			featureGroupsIds: [FG1.id]
		});
		let LA2 = LicenseAgreement.build();
		FG1.referencingLicenseAgreements = [LA1.id];

		const VLM1 = LicenseModelOverviewFactory.build({
			licenseAgreement: {
				licenseAgreementList: [LA1, LA2]
			},
			featureGroup: {
				featureGroupsList: [FG1]
			},
			entitlementPool: {
				entitlementPoolsList: [EP1]
			},
			licenseKeyGroup: {
				licenseKeyGroupsList: [LKG1]
			},
		});

		const state = {
			licenseModel: VLM1
		};

		const expectedLicensingDataList = [
			LicenseAgreementDataListFactory.build({
				...LA1,
				children: [
					FeatureGroupDataListFactory.build(FG1)
				]
			}),
			LicenseAgreementDataListFactory.build(LA2)
		];

		var props = mapStateToProps(state);

		expect(props.isDisplayModal).toEqual(false);
		expect(props.modalHeader).toEqual(undefined);
		expect(props.licenseModelId).toEqual(VLM1.licenseModelEditor.data.id);
		expect(props.licensingDataList).toEqual(expectedLicensingDataList);
		expect(props.selectedTab).toEqual(selectedButton.VLM_LIST_VIEW);
	});

	it('should mapper return overview data for Full NOT-IN-USE list view', () => {
		let EP1 = EntitlementPool.build();
		let LKG1 = LicenseKeyGroup.build();
		let FG1 = FeatureGroup.build();

		const VLM1 = LicenseModelOverviewFactory.build({
			licenseAgreement: { licenseAgreementList: [] },
			featureGroup: {
				featureGroupsList: [FG1]
			},
			entitlementPool: {
				entitlementPoolsList: [EP1]
			},
			licenseKeyGroup: {
				licenseKeyGroupsList: [LKG1]
			},
			licenseModelOverview: {
				selectedTab: selectedButton.NOT_IN_USE
			}
		});

		const state = {
			licenseModel: VLM1
		};

		const expectedLicensingDataList = [
			FeatureGroupDataListFactory.build(FG1),
			EntitlementPoolDataListFactory.build(EP1),
			LicenseKeyGroupDataListFactory.build(LKG1)
		];

		var props = mapStateToProps(state);

		expect(props.isDisplayModal).toEqual(false);
		expect(props.modalHeader).toEqual(undefined);
		expect(props.licenseModelId).toEqual(VLM1.licenseModelEditor.data.id);
		expect(props.licensingDataList).toEqual([]);
		expect(props.orphanDataList).toEqual(expectedLicensingDataList);
		expect(props.selectedTab).toEqual(selectedButton.NOT_IN_USE);
	});

	it('should mapper return overview data for NOT-IN-USE list view (FG with children)', () => {
		let EP1 = EntitlementPool.build();
		let LKG1 = LicenseKeyGroup.build();
		let FG1 = FeatureGroup.build({
			entitlementPoolsIds: [EP1.id],
			licenseKeyGroupsIds: [LKG1.id]
		});
		EP1.referencingFeatureGroups = [FG1.id];
		LKG1.referencingFeatureGroups = [FG1.id];

		const VLM1 = LicenseModelOverviewFactory.build({
			licenseAgreement: { licenseAgreementList: [] },
			featureGroup: {
				featureGroupsList: [FG1]
			},
			entitlementPool: {
				entitlementPoolsList: [EP1]
			},
			licenseKeyGroup: {
				licenseKeyGroupsList: [LKG1]
			},
			licenseModelOverview: {
				selectedTab: selectedButton.NOT_IN_USE
			}
		});

		const state = {
			licenseModel: VLM1
		};

		const expectedLicensingDataList = [
			FeatureGroupDataListFactory.build({
				...FG1,
				children: [
					EntitlementPoolDataListFactory.build(EP1),
					LicenseKeyGroupDataListFactory.build(LKG1)]
			})
		];

		var props = mapStateToProps(state);

		expect(props.isDisplayModal).toEqual(false);
		expect(props.modalHeader).toEqual(undefined);
		expect(props.licenseModelId).toEqual(VLM1.licenseModelEditor.data.id);
		expect(props.licensingDataList).toEqual([]);
		expect(props.orphanDataList).toEqual(expectedLicensingDataList);
		expect(props.selectedTab).toEqual(selectedButton.NOT_IN_USE);
	});
});
