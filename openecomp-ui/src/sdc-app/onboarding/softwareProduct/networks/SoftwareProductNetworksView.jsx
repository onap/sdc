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
import React from 'react';
import PropTypes from 'prop-types';
import i18n from 'nfvo-utils/i18n/i18n.js';

import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';
import ListEditorItemViewField from 'nfvo-components/listEditor/ListEditorItemViewField.jsx';

class SoftwareProductNetworksView extends React.Component {

	static propTypes = {
		networksList: PropTypes.arrayOf(PropTypes.shape({
			id: PropTypes.string.isRequired,
			name: PropTypes.string.isRequired,
			dhcp: PropTypes.bool.isRequired
		})).isRequired,
		isReadOnlyMode: PropTypes.bool.isRequired
	};

	state = {
		localFilter: ''
	};

	render() {
		const {localFilter} = this.state;
		const {isReadOnlyMode} = this.props;

		return (
			<div className='vsp-networks-page'>
				<ListEditorView
					title={i18n('Networks')}
					filterValue={localFilter}
					placeholder={i18n('Filter Networks')}
					onFilter={value => this.setState({localFilter: value})}
					twoColumns>
					{this.filterList().map(network => this.renderNetworksListItem({network, isReadOnlyMode}))}
				</ListEditorView>
			</div>
		);
	}

	renderNetworksListItem({network, isReadOnlyMode}) {
		let {id, name, dhcp} = network;
		return (
			<ListEditorItemView
				key={id}
				className='list-editor-item-view'
				isReadOnlyMode={isReadOnlyMode}>

				<ListEditorItemViewField>
					<div className='name'>{name}</div>
				</ListEditorItemViewField>
				<ListEditorItemViewField>
					<div className='details'>
						<div className='title'>{i18n('DHCP')}</div>
						<div className='artifact-name'>{dhcp ? i18n('YES') : i18n('NO')}</div>
					</div>
				</ListEditorItemViewField>
			</ListEditorItemView>
		);
	}

	filterList() {
		let {networksList} = this.props;

		let {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return networksList.filter(({name = ''}) => {
				return escape(name).match(filter);
			});
		}
		else {
			return networksList;
		}
	}
}

export default SoftwareProductNetworksView;
