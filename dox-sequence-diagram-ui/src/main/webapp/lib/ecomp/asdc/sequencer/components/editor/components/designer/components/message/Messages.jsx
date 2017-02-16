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

import Common from '../../../../../../common/Common';

import Message from './Message';
import MessageNew from './MessageNew';

/**
 * Messages container, facilitating DND.
 * @param props lifeline element properties.
 * @returns {*}
 * @constructor
 */
export default class Messages extends React.Component {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct view.
   * @param props element properties.
   * @param context react context.
   */
  constructor(props, context) {
    super(props, context);
    this.state = {
    };
    this.setHoverIndex = this.setHoverIndex.bind(this);
    this.getHoverIndex = this.getHoverIndex.bind(this);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Record last hover index as non-state.
   * @param index index.
   */
  setHoverIndex(index) {
    this.hoverIndex = index;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get last recorded hover index.
   * @returns {*}
   */
  getHoverIndex() {
    return this.hoverIndex;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle drop.
   * @param dragIndex dragged item index; undefined if new.
   * @param hoverIndex drop index.
   */
  onDrop(dragIndex, hoverIndex) {
    if (hoverIndex >= 0) {
      const application = this.props.application;
      const model = application.getModel();
      if (Common.isNumber(dragIndex)) {
        if (dragIndex !== hoverIndex) {
          model.reorderMessages(dragIndex, hoverIndex);
        }
      } else {
        model.addMessage(hoverIndex);
      }
      this.forceUpdate();
      application.renderDiagram();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render view.
   * @returns {*}
   */
  render() {

    const model = this.props.application.getModel();
    const diagram = model.unwrap().diagram;

    // Render existing messages.

    const messages = [];
    for (const step of diagram.steps) {
      const message = step.message;
      const from = model.getLifelineById(message.from);
      const to = model.getLifelineById(message.to);
      messages.push(<Message
        key={`m${message.id}`}
        application={this.props.application}
        designer={this.props.designer}
        message={message}
        active={this.props.activeMessageId === message.id}
        from={from}
        to={to}
        model={model}
        index={messages.length}
        messages={this}
      />);
    }

    // Render add.

    messages.push(<MessageNew
      key="_m"
      designer={this.props.designer}
      messages={this}
    />);

    return (
      <div className="asdcs-designer-steps">
        {messages}
      </div>
    );
  }
}

/** Element properties. */
Messages.propTypes = {
  application: React.PropTypes.object.isRequired,
  designer: React.PropTypes.object.isRequired,
  activeMessageId: React.PropTypes.string,
};
