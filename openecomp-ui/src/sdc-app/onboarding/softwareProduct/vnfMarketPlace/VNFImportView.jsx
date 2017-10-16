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
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import Button from 'sdc-ui/lib/react/Button.js';
import VNFImportActionHelper from '../vnfMarketPlace/VNFImportActionHelper.js';


function VNFAction({action, isHeader, downloadCSAR, id, currSoftwareProduct}) {
	if (isHeader) {
		return <span>{action}</span>;
	}
	return (
		<span>
			<SVGIcon name='download' color='positive' onClick={() => {downloadCSAR(id, currSoftwareProduct);}}/>
		</span>
	);
}

function VNFSortableCellHeader({isHeader, data, isDes, onSort, activeSortColumn}) {
	//TODO check icon sdc-ui
	if (isHeader) {
		if(activeSortColumn === data) {
			return (
				<span className='vnf-table-header' onClick={()=>{onSort(activeSortColumn);}}>
					<span>{data}</span>
					<span className={`header-sort-arrow ${isDes ? 'up' : 'down'}`}></span>
				</span>
			);
		}
		else {
			return (
				<span className='vnf-table-header' onClick={()=>{activeSortColumn = data; onSort(activeSortColumn);}}>
					<span>{data}</span>
				</span>
			);
		}
	}
	return (
		<span className='vnf-table-cell'>
			<span>{data}</span>
		</span>
	);
}

export function VNFItemList({vnf, isHeader, isDes, onSort, activeSortColumn, downloadCSAR, selectTableRow, selectedRow, currentSoftwareProduct}) {
	let {csarId, name, version, provider, shortDesc, action} = vnf;
	return (
		<li className={`vnfBrowse-list-item ${isHeader ? 'header' : ''} ${csarId === selectedRow ? 'selectedRow' : ''}`} 
			data-test-id='vnfBrowse-list-item' onClick={() => {selectTableRow(csarId);}}>
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
			<div className='table-cell vnftable-action' data-test-id='vnftable-action'>
				<VNFAction isHeader={isHeader} action={action} downloadCSAR= {downloadCSAR} id={csarId} currSoftwareProduct={currentSoftwareProduct}/>
			</div>
		</li>
	);
}

class VNFImportView extends React.Component {

	state = {
		localFilter: '',
		sortDescending: true,
		sortCrit: 'name',
		activeSortColumn : 'Name',
		selectedRow: ''
	};

	render() {
		let {onCancel, onSubmit, currentSoftwareProduct} = this.props;
		
		return (
			<div className='vnf-creation-page'>
					<GridSection className='vnf-grid-section'>
						<GridItem colSpan='4'>
							<ListEditorView
								title={i18n('VNF List Title')}
								filterValue={this.state.localFilter}
								onFilter={filter => this.setState({localFilter: filter})}>
								<VNFItemList
									isHeader={true}
									vnf={{csarId: 0, name: i18n('VNF Header Name'), version: i18n('VNF Header Version'), 
										provider: i18n('VNF Header Vendor'), shortDesc: i18n('VNF Header Desc'), action: i18n('VNF Header Action')}}
									isDes={this.state.sortDescending}
									onSort={(sortCriteria, activeSortCol) => this.setState({sortDescending: !this.state.sortDescending, sortCrit: sortCriteria, activeSortColumn: activeSortCol})} 
									activeSortColumn={this.state.activeSortColumn}/>
								{this.sortVNFItems(this.filterVNFItems(), 
									this.state.sortDescending, this.state.sortCrit).map(vnf => <VNFItemList key={vnf.id} vnf={vnf} 
										downloadCSAR={this.downloadCSAR} selectTableRow={(selID) => 
											{this.setState({selectedRow: selID});this.selectTableRow(selID);}} selectedRow={this.state.selectedRow} currentSoftwareProduct={currentSoftwareProduct}/>)}
							</ListEditorView>
						</GridItem>
						<GridItem colSpan='4'>
							<div className='vnf-modal'>
	            				<Button className='vnf-submit' type='button' btnType='default' onClick={() => 
	            					onSubmit(this.state.selectedRow, currentSoftwareProduct)}>{i18n('OK')}
	            				</Button>
	            				<Button className='Cancel' type='button' btnType='outline' onClick={onCancel}>{i18n('Cancel')}
	            				</Button>
							</div>
						</GridItem>
					</GridSection>
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
		if (sortDesc) {
			return vnfItems.sort((a, b) => 
			{
				if(a[sortCrit] < b[sortCrit]){
					return -1;
				} 
				else if(a[sortCrit] > b[sortCrit]){
					return 1;
				} 
				else {
					return 0;
				}
			});
		}
		else {
			return vnfItems.reverse();
		}
	}

	downloadCSAR(id, currSoftwareProduct) {
		VNFImportActionHelper.download(id, currSoftwareProduct);
	}
	
}

export default VNFImportView;
