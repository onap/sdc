import React from 'react';
import {storiesOf, action} from '@kadira/storybook';
import {select, withKnobs} from '@kadira/storybook-addon-knobs';
import CatalogItemDetails from './CatalogItemDetails.jsx';
import {FinalizedLicenseModelFactory} from 'test-utils/factories/licenseModel/LicenseModelFactories.js';
import {statusEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';



const stories = storiesOf('CatalogTiles', module);
stories.addDecorator(withKnobs);

const types = [
    'license-model-type',
    'software-product-type'
];

function selectType() {
    return select('Item type' , types, types[0]);
}

let vlm = FinalizedLicenseModelFactory.build({name: 'Test-VLM'});
let unclockedVlm = {...vlm,  status: statusEnum.CHECK_OUT_STATUS};


stories
    .add('preview', () => (
        <div className='catalog-view'>
            <div className='catalog-list'>
                <div className='catalog-items'>
                    <CatalogItemDetails catalogItemData={vlm} catalogItemTypeClass={selectType()} onSelect={action('onSelect')} onMigrate={action('onMigrate')}/>                   
                    <CatalogItemDetails catalogItemData={unclockedVlm} catalogItemTypeClass={selectType()} onSelect={action('onSelect')} onMigrate={action('onMigrate')}/>                   
                </div>    
            </div>   
        </div>                           
    ));     
