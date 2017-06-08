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

import HTML5Backend from 'react-dnd-html5-backend';
import { DragDropContext } from 'react-dnd';

import Common from '../../../../common/Common';
import Logger from '../../../../common/Logger';

import Actions from './components/actions/Actions';
import Lifelines from './components/lifeline/Lifelines';
import Messages from './components/message/Messages';
import Metadata from './components/metadata/Metadata';

import Icon from '../../../icons/Icon';
import iconExpanded from '../../../../../../../../res/ecomp/asdc/sequencer/sprites/icons/expanded.svg';
import iconCollapsed from '../../../../../../../../res/ecomp/asdc/sequencer/sprites/icons/collapsed.svg';

/**
 * LHS design wid` view.
 */
class Designer extends React.Component {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct view.
   * @param props element properties.
   * @param context react context.
   */
  constructor(props, context) {
    super(props, context);

    Logger.noop();

    this.application = Common.assertNotNull(props.application);

    this.state = {
      lifelinesExpanded: false,
      messagesExpanded: true,
      activeLifelineId: undefined,
      activeMessageId: undefined,
    };

    // Bind this.

    this.onToggle = this.onToggle.bind(this);
    this.onMouseEnterLifeline = this.onMouseEnterLifeline.bind(this);
    this.onMouseLeaveLifeline = this.onMouseLeaveLifeline.bind(this);
    this.onMouseEnterMessage = this.onMouseEnterMessage.bind(this);
    this.onMouseLeaveMessage = this.onMouseLeaveMessage.bind(this);

    this.addMessage = this.addMessage.bind(this);
    this.updateMessage = this.updateMessage.bind(this);
    this.deleteMessage = this.deleteMessage.bind(this);
    this.addLifeline = this.addLifeline.bind(this);
    this.updateLifeline = this.updateLifeline.bind(this);
    this.deleteLifeline = this.deleteLifeline.bind(this);

    this.selectMessage = this.selectMessage.bind(this);
    this.selectLifeline = this.selectLifeline.bind(this);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Select message by ID.
   * @param id message ID.
   */
  selectMessage(id) {

    // TODO: scroll into view.

    this.setState({ activeMessageId: id });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Select lifeline by ID.
   * @param id lifeline ID.
   */
  selectLifeline(id) {

    // TODO: scroll into view.

    this.setState({ activeLifelineId: id });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Show/hide lifelines section.
   */
  onToggle() {
    const lifelinesExpanded = !this.state.lifelinesExpanded;
    const messagesExpanded = !lifelinesExpanded;
    this.setState({ lifelinesExpanded, messagesExpanded });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle mouse event.
   * @param id lifeline identifier.
   */
  onMouseEnterLifeline(id) {
    this.application.selectLifeline(id);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle mouse event.
   */
  onMouseLeaveLifeline() {
    this.application.selectLifeline();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle mouse event.
   * @param id message identifier.
   */
  onMouseEnterMessage(id) {
    this.application.selectMessage(id);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle mouse event.
   */
  onMouseLeaveMessage() {
    // Only on next selection.
    // this.application.selectMessage();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Add new message.
   */
  addMessage() {

    if (this.application.getModel().unwrap().diagram.lifelines.length < 2) {
      self.application.showErrorDialog('You need at least two lifelines.');
      return;
    }

    this.application.getModel().addMessage();
    this.forceUpdate();
    this.application.renderDiagram();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Apply property changes to the message identified by props.id.
   * @param props properties to be updated (excluding 'id').
   */
  updateMessage(props) {
    Common.assertPlainObject(props);
    const model = this.application.getModel();
    const message = model.getMessageById(props.id);
    if (message) {
      for (const k of Object.keys(props)) {
        if (k !== 'id') {
          message[k] = props[k];
        }
      }
    }
    this.forceUpdate();
    this.application.renderDiagram();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Delete message after confirmation.
   * @param id ID of message to be deleted.
   */
  deleteMessage(id) {

    const self = this;
    const model = this.application.getModel();

    const confirmComplete = function f() {
      model.deleteMessageById(id);
      self.render();
      self.application.renderDiagram();
    };

    this.application.showConfirmDialog('Delete this message?',
      confirmComplete);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Add new lifeline.
   */
  addLifeline() {
    this.application.getModel().addLifeline();
    this.forceUpdate();
    this.application.renderDiagram();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Apply property changes to the lifeline identified by props.id.
   * @param props properties to be updated (excluding 'id').
   */
  updateLifeline(props) {
    Common.assertPlainObject(props);
    const model = this.application.getModel();
    const lifeline = model.getLifelineById(props.id);
    if (lifeline) {
      for (const k of Object.keys(props)) {
        if (k !== 'id') {
          lifeline[k] = props[k];
        }
      }
    }
    this.forceUpdate();
    this.application.renderDiagram();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Delete lifeline after confirmation.
   * @param id candidate for deletion.
   */
  deleteLifeline(id) {

    const self = this;
    const model = this.application.getModel();

    const confirmComplete = function f() {
      model.deleteLifelineById(id);
      self.forceUpdate();
      self.application.renderDiagram();
    };
    this.application.showConfirmDialog('Delete this lifeline and all its steps?',
      confirmComplete);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render designer.
   */
  render() {

    const application = this.props.application;
    const model = application.getModel();
    const diagram = model.unwrap().diagram;
    const metadata = diagram.metadata;

    const lifelinesIcon = this.state.lifelinesExpanded ? iconExpanded : iconCollapsed;
    const lifelinesClass = this.state.lifelinesExpanded ? '' : 'asdcs-hidden';
    const messagesIcon = this.state.messagesExpanded ? iconExpanded : iconCollapsed;
    const messagesClass = this.state.messagesExpanded ? '' : 'asdcs-hidden';

    return (

      <div className="asdcs-editor-designer">
        <div className="asdcs-designer-accordion">

          <div className="asdcs-designer-metadata-container">
            <Metadata metadata={metadata} />
          </div>

          <h3 onClick={this.onToggle}>Lifelines
            <div className="asdcs-designer-icon" onClick={this.onToggle}>
              <Icon glyph={lifelinesIcon} />
            </div>
          </h3>

          <div className={`asdcs-designer-lifelines-container ${lifelinesClass}`}>
            <Lifelines
              application={this.application}
              designer={this}
              activeLifelineId={this.state.activeLifelineId}
            />
          </div>

          <h3 onClick={this.onToggle}>Steps
            <div className="asdcs-designer-icon" onClick={this.onToggle}>
              <Icon glyph={messagesIcon} />
            </div>
          </h3>

          <div className={`asdcs-designer-steps-container ${messagesClass}`} >
            <Messages
              application={this.application}
              designer={this}
              activeMessageId={this.state.activeMessageId}
            />
          </div>

        </div>

        <Actions
          application={this.props.application}
          model={model}
          ref={(r) => { this.actions = r; }}
        />

      </div>
    );
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Scroll accordion pane to make
   * @param $element focused element.
   * @private
   */
  static _scrollIntoView($element) {
    const $pane = $element.closest('.ui-accordion-content');
    const paneScrollTop = $pane.scrollTop();
    const paneHeight = $pane.height();
    const paneBottom = paneScrollTop + paneHeight;
    const elementTop = $element[0].offsetTop - $pane[0].offsetTop;
    const elementHeight = $element.height();
    const elementBottom = elementTop + elementHeight;
    if (elementBottom > paneBottom) {
      $pane.scrollTop(elementTop);
    } else if (elementTop < paneScrollTop) {
      $pane.scrollTop(elementTop);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Show actions menu.
   * @param id selected message ID.
   * @param position page coordinates.
   */
  showActions(id, position) {
    if (this.actions) {
      this.actions.show(id, position);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Show notes popup.
   * @param id selected message identifier.
   */
  showNotes(id) {
    const model = this.application.getModel();
    const options = this.application.getOptions();
    const message = model.getMessageById(id);
    const notes = (message.notes && (message.notes.length > 0)) ? message.notes[0] : '';
    const editComplete = function f(p) {
      message.notes = [];
      if (p && p.text) {
        const sanitized = Common.sanitizeText(p.text, options, 'notes');
        message.notes.push(sanitized);
      }
    };
    this.application.showEditDialog('Notes:', notes, editComplete);
  }
}

/** Element properties. */
Designer.propTypes = {
  application: React.PropTypes.object.isRequired,
};

export default DragDropContext(HTML5Backend)(Designer);
