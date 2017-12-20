import React from 'react';
import {storiesOf, action} from '@kadira/storybook';
import {select, withKnobs} from '@kadira/storybook-addon-knobs';
import CatalogItemDetails from './CatalogItemDetails.jsx';
import {catalogItemTypes, catalogItemStatuses} from './onboardingCatalog/OnboardingCatalogConstants.js';
import {FinalizedLicenseModelFactory} from 'test-utils/factories/licenseModel/LicenseModelFactories.js';

const stories = storiesOf('CatalogTiles', module);
stories.addDecorator(withKnobs);

const types = [
	catalogItemTypes.LICENSE_MODEL,
	catalogItemTypes.SOFTWARE_PRODUCT
];

function selectType() {
	return select('Item type' , types, types[0]);
}

let vlm = {...FinalizedLicenseModelFactory.build({name: 'Test-VLM'}), itemStatus: catalogItemStatuses.DRAFT};
let certifiedVlm = {...vlm, itemStatus: catalogItemStatuses.CERTIFIED};

stories
	.add('preview', () => (
		<div className='catalog-view'>
			<div className='catalog-list'>
				<div className='catalog-items'>
					<CatalogItemDetails catalogItemData={vlm} catalogItemTypeClass={selectType()} onSelect={action('onSelect')} onMigrate={action('onMigrate')}/>
					<CatalogItemDetails catalogItemData={certifiedVlm} catalogItemTypeClass={selectType()} onSelect={action('onSelect')} onMigrate={action('onMigrate')}/>
				</div>
			</div>
		</div>
	));
