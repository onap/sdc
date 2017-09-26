/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
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
 
import React from 'react';
import Form from 'nfvo-components/input/validation/Form.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
/*import { openVnf } from './VNFCreationActions';*/
import i18n from 'nfvo-utils/i18n/i18n.js';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';

/*const VNFCreationPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	category: React.PropTypes.string,
	subCategory: React.PropTypes.string,
	vendorId: React.PropTypes.string
});*/

function VNFAction({action, isHeader}) {
	if (isHeader) {
		return <span>{action}</span>;
	}
	return (
		<span>
			<SVGIcon name='download' color='positive'/>
		</span>
	);
}

function VNFSortableCellHeader({isHeader, data, isDes, onSort, activeSortColumn}) {
	//TODO check icon sdc-ui
	if (isHeader) {
		if(activeSortColumn === data) {
			return (
				<span className='date-header' onClick={()=>{onSort(activeSortColumn);}}>
					<span>{data}</span>
					<span className={`header-sort-arrow ${isDes ? 'up' : 'down'}`}></span>
				</span>
			);
		}
		else {
			return (
				<span className='date-header' onClick={()=>{activeSortColumn = data; onSort(activeSortColumn);}}>
					<span>{data}</span>
				</span>
			);
		}
	}
	return (
		<span className='date-cell'>
			<span>{data}</span>
		</span>
	);
}

export function VNFItemList({vnf, isHeader, isDes, onSort, activeSortColumn}) {
	let {name, version, provider, shortDesc, action} = vnf;
	return (
		<li className={`vnfBrowse-list-item ${isHeader ? 'header' : ''}`} data-test-id='vnfBrowse-list-item'>
			<div className='table-cell vnftable-name' data-test-id='vnftable-name'>
				<VNFSortableCellHeader isHeader={isHeader} data={name} isDes={isDes} onSort={(activeSort)=> {onSort('name', activeSort);}} activeSortColumn={activeSortColumn}/>
			</div>
			<div className='table-cell vnftable-version' data-test-id='vnftable-version'>
				<VNFSortableCellHeader isHeader={isHeader} data={version} isDes={isDes} onSort={(activeSort)=> {onSort('version', activeSort);}} activeSortColumn={activeSortColumn}/>
			</div>
			<div className='table-cell vnftable-provider' data-test-id='vnftable-provider'>
				<VNFSortableCellHeader isHeader={isHeader} data={provider} isDes={isDes} onSort={(activeSort)=> {onSort('provider', activeSort);}} activeSortColumn={activeSortColumn}/>
			</div>
			<div className='table-cell vnftable-shortDesc' data-test-id='vnftable-shortDesc'>
				<VNFSortableCellHeader isHeader={isHeader} data={shortDesc} isDes={isDes} onSort={(activeSort)=> {onSort('shortDesc', activeSort);}} activeSortColumn={activeSortColumn}/>
			</div>
			<div className='table-cell vnftable-action' data-test-id='vnftable-action'><VNFAction isHeader={isHeader} action={action}/></div>
		</li>
	);
}

class VNFCreationView extends React.Component {

	state = {
		localFilter: '',
		sortDescending: true,
		sortCrit: 'name',
		activeSortColumn : 'Name'
	};

	static propTypes = {
		/*data: VNFCreationPropType,
		finalizedLicenseModelList: React.PropTypes.array,
		softwareProductCategories: React.PropTypes.array,
		VSPNames: React.PropTypes.object,
		onDataChanged: React.PropTypes.func.isRequired,
		onSubmit: React.PropTypes.func.isRequired,
		onCancel: React.PropTypes.func.isRequired*/
	};

	render() {
		let {onCancel, onSubmit} = this.props;
		/*let {name, description, vendorId, subCategory, onboardingMethod} = data;

		const vendorList = this.getVendorList();*/
		return (
			<div className='vnf-creation-page'>
				<Form
					onReset={() => onCancel() }
					onSubmit={() => onSubmit() }
					labledButtons={true}
					submitButtonText={i18n('Ok')}>
					<GridSection>
						<GridItem colSpan='4'>
							<ListEditorView
								title={i18n('VNF List')}
								filterValue={this.state.localFilter}
								onFilter={filter => this.setState({localFilter: filter})}>
								<VNFItemList
									isHeader={true}
									vnf={{name: 'Name', version: 'Verison', provider: 'Vendor', shortDesc: 'Description', action: 'Action'}}
									isDes={this.state.sortDescending}
									onSort={(sortCriteria, activeSortCol) => this.setState({sortDescending: !this.state.sortDescending, sortCrit: sortCriteria, activeSortColumn: activeSortCol})} 
									activeSortColumn={this.state.activeSortColumn}/>
								{this.sortVNFItems(this.filterVNFItems(), 
									this.state.sortDescending, this.state.sortCrit).map(vnf => <VNFItemList key={vnf.id} vnf={vnf}/>)}
							</ListEditorView>
						</GridItem>
					</GridSection>
				</Form>
			</div>
		);
	}

	filterVNFItems() {
		let {vnfItems} = this.props;
		let {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return vnfItems.filter(
				({name = '', provider = '', version = '', shortDesc = ''}) => 
					escape(name).match(filter) || escape(provider).match(filter) || escape(version).match(filter) || escape(shortDesc).match(filter));
		}
		else {
			return vnfItems;
		}
	}

	sortVNFItems(vnfItems, sortDesc, sortCrit) {
		if (this.state.sortDescending) {return vnfItems.sort((a, b) => {if(a[sortCrit] < b[sortCrit]){return -1;} else if(a[sortCrit] > b[sortCrit]){return 1;} else {return 0;}});
		}
		else {
			return vnfItems.reverse();
		}
	}
}

export default VNFCreationView;
