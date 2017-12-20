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
import i18n from 'nfvo-utils/i18n/i18n.js';
import Form from 'nfvo-components/input/validation/Form.jsx';

import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';
import ListEditorItemViewField from 'nfvo-components/listEditor/ListEditorItemViewField.jsx';
import Input from'nfvo-components/input/validation/Input.jsx';

class SoftwareProductComponentsImageListView extends React.Component {
	state = {
		localFilter: ''
	};

	render() {
		let {dataMap, onQDataChanged, isReadOnlyMode, qgenericFieldInfo} = this.props;
		return(
			<div className='vsp-components-image'>
				<div className='image-data'>
					<div>
						{ qgenericFieldInfo && <Form
							formReady={null}
							isValid={true}
							onSubmit={() => this.save()}
							isReadOnlyMode={isReadOnlyMode}
							hasButtons={false}>

							<h3 className='section-title'>{i18n('Image')}</h3>
							<div className='rows-section'>
								<div className='row-flex-components'>
									<div className='single-col'>
										<Input
											data-test-id='providedBy'
											label={i18n('Image provided by')}
											type='select'
											isValid={qgenericFieldInfo['general/image/providedBy'].isValid}
											errorText={qgenericFieldInfo['general/image/providedBy'].errorText}
											value={dataMap['general/image/providedBy']}
											onChange={(e) => {
												const selectedIndex = e.target.selectedIndex;
												const val = e.target.options[selectedIndex].value;
												onQDataChanged({'general/image/providedBy' : val});}
											}>
											<option key='placeholder' value=''>{i18n('Select...')}</option>
											{ qgenericFieldInfo['general/image/providedBy'].enum.map(proto =>
												<option value={proto.enum} key={proto.enum}>{proto.title}</option>) }
										</Input>
									</div>
									<div className='empty-two-col' />
								</div>
							</div>

						</Form> }
					</div>
				</div>
	            {this.renderImagesList()}
            </div>
		);
	};

	renderImagesList() {
		const {localFilter} = this.state;
		let {isReadOnlyMode, onAddImage, isManual} = this.props;

		return (
			<ListEditorView
				title={i18n('Images')}
				filterValue={localFilter}
				placeholder={i18n('Filter Images by Name')}
				isReadOnlyMode={isReadOnlyMode}
				onFilter={value => this.setState({localFilter: value})}
				onAdd={isManual ? () => onAddImage(isReadOnlyMode) : null}
				plusButtonTitle={i18n('Add Image')}
				twoColumns>
				{this.filterList().map(image => this.renderImagesListItem(image, isReadOnlyMode))}
			</ListEditorView>
		);
	};


	renderImagesListItem(image, isReadOnlyMode) {
		let {id, fileName} = image;
		let {onEditImageClick, isManual, onDeleteImage} =  this.props;
		return (
			<ListEditorItemView
				key={id}
				isReadOnlyMode={isReadOnlyMode}
				onSelect={() => onEditImageClick(image, isReadOnlyMode)}
				onDelete={isManual ? () => onDeleteImage(image) : null}>

				<ListEditorItemViewField>
					<div className='image-filename-cell'><span className='image-filename'>{fileName}</span></div>
				</ListEditorItemViewField>
			</ListEditorItemView>
		);
	}

	filterList() {
		let {imagesList} = this.props;
		let {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return imagesList.filter(({fileName = ''}) => {
				return escape(fileName).match(filter);
			});
		}
		else {
			return imagesList;
		}
	}

	save() {
		let {onSubmit, qdata} = this.props;
		return onSubmit(qdata);
	}
}
export default SoftwareProductComponentsImageListView;
