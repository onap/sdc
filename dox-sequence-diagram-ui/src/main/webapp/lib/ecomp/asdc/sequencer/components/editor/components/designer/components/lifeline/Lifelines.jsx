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

import Lifeline from './Lifeline';
import LifelineNew from './LifelineNew';

/**
 * Lifeline container, facilitating DND.
 * @param props lifeline element properties.
 * @returns {*}
 * @constructor
 */
export default class Lifelines extends React.Component {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct view.
   * @param props element properties.
   * @param context react context.
   */
  constructor(props, context) {
    super(props, context);
    this.setHoverIndex = this.setHoverIndex.bind(this);
    this.getHoverIndex = this.getHoverIndex.bind(this);
    this.onDrop = this.onDrop.bind(this);
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
          model.reorderLifelines(dragIndex, hoverIndex);
        }
      } else {
        model.addLifeline(hoverIndex);
      }
      this.forceUpdate();
      application.renderDiagram();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render view.
   * @returns {XML}
   */
  render() {
    const model = this.props.application.getModel();
    const metamodel = model.getMetamodel();
    const diagram = model.unwrap().diagram;

    const lifelines = [];
    for (const lifeline of diagram.lifelines) {
      lifelines.push(<Lifeline
        key={`l${lifeline.id}`}
        application={this.props.application}
        designer={this.props.designer}
        lifeline={lifeline}
        active={this.props.activeLifelineId === lifeline.id}
        id={lifeline.id}
        metamodel={metamodel}
        lifelines={this}
        index={lifelines.length}
      />);
    }

    lifelines.push(<LifelineNew
      key="_l"
      designer={this.props.designer}
      lifelines={this}
    />);

    return (
      <div className="asdcs-designer-lifelines">
        {lifelines}
      </div>
    );
  }
}

/**
 * Declare properties.
 */
Lifelines.propTypes = {
  application: React.PropTypes.object.isRequired,
  designer: React.PropTypes.object.isRequired,
  activeLifelineId: React.PropTypes.string,
};
