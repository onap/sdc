/*!
 * Copyright © 2016-2018 European Support Limited
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
import {VSPEditorFactoryWithLicensingData} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import {actionTypes} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';

describe('SoftwareProduct Attachments - validation flow test: ', function () {

	it('candidate in process flag test', () => {
		const softwareProduct = VSPEditorFactoryWithLicensingData.build();
		const store = storeCreator({			
			softwareProduct: {
				softwareProductEditor: {data: softwareProduct},
				softwareProductQuestionnaire: {qdata: 'test', qschema: {type: 'string'}}
			}
		});		
		store.dispatch({
			type: actionTypes.CANDIDATE_IN_PROCESS,
			inProcess: true
		}); 
		let unsubscribe = store.subscribe(() => {
			expect(store.getState().softwareProduct.softwareProductEditor.data.onboardingOrigin).toEqual(true);			
		});							
		unsubscribe();
	});

});