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
 * revisions and limitations under the License.
 */
import React from 'react';
import Form from 'nfvo-components/input/validation/Form.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';
import ShowMore from 'react-show-more';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';

import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';
import ListEditorItemViewField from 'nfvo-components/listEditor/ListEditorItemViewField.jsx';


class RevisionsView extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			revertId : null
		};
	}

	render() {
		let {onCancel, onRevert, revisions, users} = this.props;
		return (
			<div className='manage-revisions-page'>
				<Form
					hasButtons={true}
					onSubmit={() => onRevert(this.state.revertId)}
					onReset={() => onCancel() }
					submitButtonText={i18n('Revert')}
					labledButtons={true}>
					<ListEditorView
						title={i18n('Select a Commit')}
						isReadOnlyMode={false}>
						{revisions.map((revision) => {
							return (
								<div key={revision.id} data-test-id='revision-list-item' className={`revision-list-item ${this.state.revertId === revision.id ? 'selected' : ''}`}>
									<ListEditorItemView
										isReadOnlyMode={false}
										onSelect={() => this.setState({revertId : revision.id})}>
											<ListEditorItemViewField>
												<div className='revision-list-item-fields'>
												    <div data-test-id='revision-user' className='revision-user'>
													    <SVGIcon name='user' label={users.find(userObject => userObject.userId === revision.user).fullName} labelPosition='right'/>
												    </div>
													<div className='revision-date' data-test-id='revision-date'>
														<span className='revision-date'>{i18n.dateNormal(revision.time, {
															year: 'numeric', month: 'numeric', day: 'numeric'
														})}</span>
														<span className='revision-time'>{i18n.dateNormal(revision.time, {
															hour: 'numeric', minute: 'numeric',
															hour12: true
														})}</span>
													</div>
													<div className='revision-message'data-test-id='revision-message'>
														{revision.message && <ShowMore anchorClass='more-less' lines={2} more={i18n('More')} less={i18n('Less')}>
														{revision.message}
													</ShowMore>}</div>
												</div>
											</ListEditorItemViewField>
									</ListEditorItemView>
								</div>

							);
						})}
					</ListEditorView>
				</Form>
			</div>
		);
	}

}

export default RevisionsView;
