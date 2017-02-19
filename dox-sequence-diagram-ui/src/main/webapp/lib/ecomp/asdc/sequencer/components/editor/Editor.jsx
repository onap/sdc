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

import Logger from '../../common/Logger';
import Common from '../../common/Common';
import Designer from './components/designer/Designer';
import Toolbar from './components/toolbar/Toolbar';
import Source from './components/source/Source';

/**
 * Editor view, aggregating the designer, the code editor, the toolbar.
 */
export default class Editor extends React.Component {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct React view.
   * @param props properties.
   * @param context context.
   */
  constructor(props, context) {
    super(props, context);

    this.application = Common.assertNotNull(props.application);
    this.demo = this.application.getOptions().demo;

    // Bindings.

    this.selectMessage = this.selectMessage.bind(this);
    this.selectLifeline = this.selectLifeline.bind(this);

    this.onMouseDown = this.onMouseDown.bind(this);
    this.onMouseUp = this.onMouseUp.bind(this);
    this.onMouseMove = this.onMouseMove.bind(this);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Select message by ID.
   * @param id message ID.
   */
  selectMessage(id) {
    if (this.designer) {
      this.designer.selectMessage(id);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Select lifeline by ID.
   * @param id lifeline ID.
   */
  selectLifeline(id) {
    if (this.designer) {
      this.designer.selectLifeline(id);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Record that we're dragging.
   */
  onMouseDown() {
    if (this.editor) {
      this.resize = {
        initialWidth: this.editor.offsetWidth,
        initialPageX: undefined,
      };
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Record that we're not dragging.
   */
  onMouseUp() {
    this.resize = undefined;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Record mouse movement.
   */
  onMouseMove(event) {
    if (this.resize) {
      if (this.editor) {
        if (this.resize.initialPageX) {
          const deltaX = event.pageX - this.resize.initialPageX;
          const newWidth = this.resize.initialWidth + deltaX;
          const newWidthBounded = Math.min(800, Math.max(400, newWidth));
          this.editor.style.width = `${newWidthBounded}px`;
        } else {
          this.resize.initialPageX = event.pageX;
        }
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render editor.
   */
  render() {

    Logger.info('Editor.jsx - render()');

    return (

      <div
        className="asdcs-editor"
        ref={(r) => { this.editor = r; }}
      >

        <Toolbar application={this.props.application} editor={this} />

        <div className="asdcs-editor-content">
          <Source application={this.props.application} />
          <Designer
            application={this.props.application}
            ref={(r) => {
              if (r) {
                this.designer = r.getDecoratedComponentInstance();
              } else {
                this.designer = null;
              }
            }}
          />
        </div>

        <div className="asdcs-editor-statusbar">
          <div className="asdcs-editor-status"></div>
          <div className="asdcs-editor-validation"></div>
        </div>

        <div
          className="asdcs-editor-resize-handle"
          onMouseDown={this.onMouseDown}
          onMouseUp={this.onMouseUp}
        >
        </div>
      </div>
    );
  }
}

/** Element properties. */
Editor.propTypes = {
  application: React.PropTypes.object.isRequired,
};
