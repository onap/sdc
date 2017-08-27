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
import _template from 'lodash/template';
import _merge from 'lodash/merge';
import * as d3 from 'd3';

import Common from '../../common/Common';
import Logger from '../../common/Logger';
import Popup from './components/popup/Popup';

/**
 * SVG diagram view.
 */
export default class Diagram extends React.Component {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct React view.
   * @param props properties.
   * @param context context.
   */
  constructor(props, context) {
    super(props, context);

    this.application = Common.assertNotNull(props.application);
    this.options = this.application.getOptions().diagram;

    this.events = {};
    this.state = {
      height: 0,
      width: 0,
    };

    this.templates = {
      diagram: _template(require('./templates/diagram.html')),
      lifeline: _template(require('./templates/lifeline.html')),
      message: _template(require('./templates/message.html')),
      occurrence: _template(require('./templates/occurrence.html')),
      fragment: _template(require('./templates/fragment.html')),
      title: _template(require('./templates/title.html')),
    };

    this.handleResize = this.handleResize.bind(this);
    this.initialTransformX = 0;
    this.initialTransformY = 0;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Set diagram name.
   * @param n name.
   */
  setName(n) {
    this.svg.select('').text(n);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get SVG from diagram.
   * @returns {*|string}
   */
  getSVG() {
    const svg = this.svg.node().outerHTML;
    return svg.replace('<svg ', '<svg xmlns="http://www.w3.org/2000/svg" ');
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Select message by ID.
   * @param id message ID.
   */
  selectMessage(id) {
    const sel = this.svg.selectAll('g.asdcs-diagram-message-container');
    sel.classed('asdcs-active', false);
    sel.selectAll('rect.asdcs-diagram-message-bg').attr('filter', null);
    if (id) {
      const parent = this.svg.select(`g.asdcs-diagram-message-container[data-id="${id}"]`);
      parent.classed('asdcs-active', true);
      parent.selectAll('rect.asdcs-diagram-message-bg').attr('filter', 'url(#asdcsSvgHighlight)');
    }
    this._showNotesPopup(id);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Select lifeline by ID.
   * @param id lifeline ID.
   */
  selectLifeline(id) {
    const sel = this.svg.selectAll('g.asdcs-diagram-lifeline-container');
    sel.classed('asdcs-active', false);
    sel.selectAll('rect').attr('filter', null);
    if (id) {
      const parent = this.svg.select(`g.asdcs-diagram-lifeline-container[data-id="${id}"]`);
      parent.selectAll('rect').attr('filter', 'url(#asdcsSvgHighlight)');
      parent.classed('asdcs-active', true);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle resize, including initial sizing.
   */
  handleResize() {
    if (this.wrapper) {
      const height = this.wrapper.offsetHeight;
      const width = this.wrapper.offsetWidth;
      if (this.state.height !== height || this.state.width !== width) {
        this.setState({ height, width });
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * (Re)render diagram.
   */
  render() {

    const model = this.application.getModel();
    const modelJSON = model.unwrap();
    const name = modelJSON.diagram.metadata.name;
    const options = this.application.getOptions();
    const titleHeight = options.diagram.title.height;
    const titleClass = (titleHeight && titleHeight > 0) ? `height:${titleHeight}` : 'asdcs-hidden';

    return (
      <div className="asdcs-diagram">
        <div className={`asdcs-diagram-name ${titleClass}`}>{name}</div>
        <div className="asdcs-diagram-svg" ref={(r) => { this.wrapper = r; }}></div>
        <Popup visible={false} ref={(r) => { this.popup = r; }} />
      </div>
    );
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  redraw() {
    this.updateSVG();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Initial render.
   */
  componentDidMount() {
    window.addEventListener('resize', this.handleResize);
    this.updateSVG();

    // Insurance:

    setTimeout(() => {
      this.handleResize();
    }, 500);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  componentWillUnmount() {
    window.removeEventListener('resize', this.handleResize);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render on update.
   */
  componentDidUpdate() {
    this.updateSVG();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Redraw SVG diagram. So far it's fast enough that it doesn't seem to matter whether
   * it's completely redrawn.
   */
  updateSVG() {

    if (!this.svg) {
      const svgparams = _merge({}, this.options.svg);
      this.wrapper.innerHTML = this.templates.diagram(svgparams);
      this.svg = d3.select(this.wrapper).select('svg');
    }

    if (this.state.height === 0) {

      // We'll get a resize event, and the height will be non-zero when it's actually time.

      return;
    }

    if (this.state.height && this.state.width) {
      const margin = this.options.svg.margin;
      const x = -margin;
      const y = -margin;
      const height = this.state.height + (margin * 2);
      const width = this.state.width + (margin * 2);
      const viewBox = `${x} ${y} ${width} ${height}`;
      this.svg.attr('viewBox', viewBox);
    }


    // If we've already rendered, then save the current scale/translate so that we
    // can reapply it after rendering.

    const gContentSelection = this.svg.selectAll('g.asdcs-diagram-content');
    if (gContentSelection.size() === 1) {
      const transform = gContentSelection.attr('transform');
      if (transform) {
        this.savedTransform = transform;
      }
    }

    // Empty the document. We're starting again.

    this.svg.selectAll('.asdcs-diagram-content').remove();

    // Extract the model.

    const model = this.application.getModel();
    if (!model) {
      return;
    }
    const modelJSON = model.unwrap();

    // Extract dimension options.

    const header = this.options.lifelines.header;
    const spacing = this.options.lifelines.spacing;

    // Make separate container elements so that we can control Z order.

    const gContent = this.svg.append('g').attr('class', 'asdcs-diagram-content');
    const gLifelines = gContent.append('g').attr('class', 'asdcs-diagram-lifelines');
    const gCanvas = gContent.append('g').attr('class', 'asdcs-diagram-canvas');
    gCanvas.append('g').attr('class', 'asdcs-diagram-occurrences');
    gCanvas.append('g').attr('class', 'asdcs-diagram-fragments');
    gCanvas.append('g').attr('class', 'asdcs-diagram-messages');

    // Lifelines -----------------------------------------------------------------------------------

    const actorsById = {};
    const positionsByMessageId = {};
    const lifelines = [];
    for (const actor of modelJSON.diagram.lifelines) {
      const x = (header.width / 2) + (lifelines.length * spacing.horizontal);
      Diagram._processLifeline(actor, x);
      lifelines.push({ x, actor });
      actorsById[actor.id] = actor;
    }

    // Messages ------------------------------------------------------------------------------------

    // Analyze occurrence information.

    const occurrences = model.analyzeOccurrences();
    const fragments = model.analyzeFragments();
    let y = this.options.lifelines.header.height + spacing.vertical;
    let messageIndex = 0;
    for (const step of modelJSON.diagram.steps) {
      if (step.message) {
        positionsByMessageId[step.message.id] = positionsByMessageId[step.message.id] || {};
        positionsByMessageId[step.message.id].y = y;
        this._drawMessage(gCanvas, step.message, y, actorsById,
          positionsByMessageId, ++messageIndex, occurrences, fragments);
      }
      y += spacing.vertical;
    }

    // ---------------------------------------------------------------------------------------------

    // Draw the actual (dashed) lifelines in a background <g>.

    this._drawLifelines(gLifelines, lifelines, y);

    // Initialize mouse event handlers.

    this._initMouseEvents(gLifelines, gCanvas);

    // Scale to fit.

    const bb = gContent.node().getBBox();
    this._initZoom(gContent, bb.width, bb.height);
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
   * Draw message into SVG canvas.
   * @param gCanvas container.
   * @param message message to be rendered.
   * @param y current y position.
   * @param actorsById actor lookup.
   * @param positionsByMessageId x- and y-position of each message.
   * @param messageIndex where we are in the set of messages to be rendered.
   * @param oData occurrences info.
   * @param fData fragments info.
   * @private
   */
  _drawMessage(gCanvas, message, y, actorsById, positionsByMessageId,
               messageIndex, oData, fData) {

    Common.assertNotNull(oData);

    const request = message.type === 'request';
    const fromActor = request ? actorsById[message.from] : actorsById[message.to];
    const toActor = request ? actorsById[message.to] : actorsById[message.from];

    if (!fromActor) {
      Logger.warn(`Cannot draw message ${JSON.stringify(message)}: 'from' not found.`);
      return;
    }

    if (!toActor) {
      Logger.warn(`Cannot draw message ${JSON.stringify(message)}: 'to' not found.`);
      return;
    }

    // Occurrences. --------------------------------------------------------------------------------

    if (message.occurrence) {
      Logger.debug(`Found occurrence for ${message.name}: ${JSON.stringify(message.occurrence)}`);
    }
    const activeTo = Diagram._calcActive(oData, toActor.id);
    this._drawOccurrence(gCanvas, oData, positionsByMessageId, fromActor, message.id);
    this._drawOccurrence(gCanvas, oData, positionsByMessageId, toActor, message.id);
    const activeFrom = Diagram._calcActive(oData, fromActor.id);

    // Messages. -----------------------------------------------------------------------------------

    const gMessages = gCanvas.select('g.asdcs-diagram-messages');

    // Save positions for later.

    const positions = positionsByMessageId[message.id];
    positions.x0 = fromActor.x;
    positions.x1 = toActor.x;

    // Calculate.

    const leftToRight = fromActor.x < toActor.x;
    const loopback = (message.to === message.from);
    const x1 = this._calcMessageX(activeTo, toActor.x, true, leftToRight);
    const x0 = loopback ? x1 : this._calcMessageX(activeFrom, fromActor.x, false, leftToRight);

    let messagePath;
    if (loopback) {

      // To self.

      messagePath = `M${x1},${y}`;
      messagePath = `${messagePath} L${x1 + 200},${y}`;
      messagePath = `${messagePath} L${x1 + 200},${y + 50}`;
      messagePath = `${messagePath} L${x1},${y + 50}`;
    } else {

      // Between lifelines.

      messagePath = `M${x0},${y}`;
      messagePath = `${messagePath} L${x1},${y}`;
    }

    const styles = Diagram._getMessageStyles(message);

    // Split message over lines.

    const messageWithPrefix = `${messageIndex}. ${message.name}`;
    const maxLines = this.options.messages.label.maxLines;
    const wrapWords = this.options.messages.label.wrapWords;
    const wrapLines = this.options.messages.label.wrapLines;
    const messageLines = Common.tokenize(messageWithPrefix, wrapWords, wrapLines, maxLines);

    const messageTxt = this.templates.message({
      id: message.id,
      classes: styles.css,
      marker: styles.marker,
      dasharray: styles.dasharray,
      labels: messageLines,
      lines: maxLines,
      path: messagePath,
      index: messageIndex,
      x0, x1, y,
    });

    const messageEl = Common.txt2dom(messageTxt);
    const gMessage = gMessages.append('g');
    Common.dom2svg(messageEl, gMessage);

    // Set the background's bounding box to that of the text,
    // so that they fit snugly.

    const labelBB = gMessage.select('.asdcs-diagram-message-label').node().getBBox();
    gMessage.select('.asdcs-diagram-message-label-bg')
      .attr('x', labelBB.x)
      .attr('y', labelBB.y)
      .attr('height', labelBB.height)
      .attr('width', labelBB.width);

    // Fragments. ----------------------------------------------------------------------------------

    const fragment = fData[message.id];
    if (fragment) {

      // It ends on this message.

      this._drawFragment(gCanvas, fragment, positionsByMessageId);

    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Draw a single occurrence.
   * @param gCanvas container.
   * @param oData occurrence data.
   * @param positionsByMessageId map of y positions by message ID.
   * @param actor wrapper containing lifeline ID (.id), position (.x) and name (.name).
   * @param messageId message identifier.
   * @private
   */
  _drawOccurrence(gCanvas, oData, positionsByMessageId, actor, messageId) {

    Common.assertType(oData, 'Object');
    Common.assertType(positionsByMessageId, 'Object');
    Common.assertType(actor, 'Object');
    Common.assertType(messageId, 'String');

    const gOccurrences = gCanvas.select('g.asdcs-diagram-occurrences');

    const oOptions = this.options.lifelines.occurrences;
    const oWidth = oOptions.width;
    const oHalfWidth = oWidth / 2;
    const oForeshortening = oOptions.foreshortening;
    const oMarginTop = oOptions.marginTop;
    const oMarginBottom = oOptions.marginBottom;
    const o = oData[actor.id];

    const active = Diagram._calcActive(oData, actor.id);

    const x = (actor.x - oHalfWidth) + (active * oWidth);
    const positions = positionsByMessageId[messageId];
    const y = positions.y;

    let draw = true;
    if (o) {

      if (o.start[messageId]) {

        // Starting, but drawing nothing until we find the end.

        o.active.push(messageId);
        draw = false;

      } else if (active > 0) {

        const startMessageId = o.stop[messageId];
        if (startMessageId) {

          // OK, it ends here. Draw the occurrence box.

          o.active.pop();
          const foreshorteningY = active * oForeshortening;
          const startY = positionsByMessageId[startMessageId].y;
          const height = ((oMarginTop + oMarginBottom) + (y - startY)) - (foreshorteningY * 2);
          const oProps = {
            x: (actor.x - oHalfWidth) + ((active - 1) * oWidth),
            y: ((startY - oMarginTop) + foreshorteningY),
            height,
            width: oWidth,
          };

          const occurrenceTxt = this.templates.occurrence(oProps);
          const occurrenceEl = Common.txt2dom(occurrenceTxt);
          Common.dom2svg(occurrenceEl, gOccurrences.append('g'));

        }
        draw = false;
      }
    }

    if (draw) {

      // Seems this is a singleton occurrence. We just draw a wee box around it.

      const height = (oMarginTop + oMarginBottom);
      const occurrenceProperties = { x, y: y - oMarginTop, height, width: oWidth };
      const defaultTxt = this.templates.occurrence(occurrenceProperties);
      const defaultEl = Common.txt2dom(defaultTxt);
      Common.dom2svg(defaultEl, gOccurrences.append('g'));
    }

  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Draw box(es) around fragment(s).
   * @param gCanvas container.
   * @param fragment fragment definition, corresponding to its final (stop) message.
   * @param positionsByMessageId message dimensions.
   * @private
   */
  _drawFragment(gCanvas, fragment, positionsByMessageId) {

    const optFragments = this.options.fragments;
    const gFragments = gCanvas.select('g.asdcs-diagram-fragments');
    const p1 = positionsByMessageId[fragment.stop];
    if (p1 && fragment.start && fragment.start.length > 0) {

      for (const start of fragment.start) {

        const message = this.application.getModel().getMessageById(start);
        const bounds = this._calcFragmentBounds(message, fragment, positionsByMessageId);
        if (bounds) {

          const maxLines = this.options.fragments.label.maxLines;
          const wrapWords = this.options.fragments.label.wrapWords;
          const wrapLines = this.options.fragments.label.wrapLines;
          const lines = Common.tokenize(message.fragment.guard, wrapWords, wrapLines, maxLines);

          const params = {
            id: start,
            x: bounds.x0 - optFragments.leftMargin,
            y: bounds.y0 - optFragments.topMargin,
            height: (bounds.y1 - bounds.y0) + optFragments.heightMargin,
            width: (bounds.x1 - bounds.x0) + optFragments.widthMargin,
            operator: (message.fragment.operator || 'alt'),
            lines,
          };

          const fragmentTxt = this.templates.fragment(params);
          const fragmentEl = Common.txt2dom(fragmentTxt);
          const gFragment = gFragments.append('g');
          Common.dom2svg(fragmentEl, gFragment);

          const labelBB = gFragment.select('.asdcs-diagram-fragment-guard').node().getBBox();
          gFragment.select('.asdcs-diagram-fragment-guard-bg')
            .attr('x', labelBB.x)
            .attr('y', labelBB.y)
            .attr('height', labelBB.height)
            .attr('width', labelBB.width);

        } else {
          Logger.warn(`Bad fragment: ${JSON.stringify(fragment)}`);
        }
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  _calcFragmentBounds(startMessage, fragment, positionsByMessageId) {
    if (startMessage) {
      const steps = this.application.getModel().unwrap().diagram.steps;
      const bounds = { x0: 99999, x1: 0, y0: 99999, y1: 0 };
      let foundStart = false;
      let foundStop = false;
      for (const step of steps) {
        const message = step.message;
        if (message) {
          if (message.id === startMessage.id) {
            foundStart = true;
          }
          if (foundStart && !foundStop) {
            const positions = positionsByMessageId[message.id];
            if (positions) {
              bounds.x0 = Math.min(bounds.x0, Math.min(positions.x0, positions.x1));
              bounds.y0 = Math.min(bounds.y0, positions.y);
              bounds.x1 = Math.max(bounds.x1, Math.max(positions.x0, positions.x1));
              bounds.y1 = Math.max(bounds.y1, positions.y);
            } else {
              // This probably means it hasn't been recorded yet, which is fine, because
              // we draw fragments from where they END.
              foundStop = true;
            }
          }

          if (message.id === fragment.stop) {
            foundStop = true;
          }
        }
      }
      return bounds;
    }
    return undefined;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Draw all lifelines.
   * @param gLifelines lifelines container.
   * @param lifelines lifelines definitions.
   * @param y height.
   * @private
   */
  _drawLifelines(gLifelines, lifelines, y) {

    const maxLines = this.options.lifelines.header.maxLines;
    const wrapWords = this.options.lifelines.header.wrapWords;
    const wrapLines = this.options.lifelines.header.wrapLines;

    for (const lifeline of lifelines) {
      const lines = Common.tokenize(lifeline.actor.name, wrapWords, wrapLines, maxLines);
      const lifelineTxt = this.templates.lifeline({
        x: lifeline.x,
        y0: 0,
        y1: y,
        lines,
        rows: maxLines,
        headerHeight: this.options.lifelines.header.height,
        headerWidth: this.options.lifelines.header.width,
        id: lifeline.actor.id,
      });

      const lifelineEl = Common.txt2dom(lifelineTxt);
      Common.dom2svg(lifelineEl, gLifelines.append('g'));
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Initialize all mouse events.
   * @param gLifelines lifelines container.
   * @param gCanvas top-level canvas container.
   * @private
   */
  _initMouseEvents(gLifelines, gCanvas) {

    const self = this;
    const source = 'asdcs';
    const origin = `${window.location.protocol}//${window.location.host}`;

    let timer;
    gLifelines.selectAll('.asdcs-diagram-lifeline-selectable')
      .on('mouseenter', function f() {
        timer = setTimeout(() => {
          self.application.selectLifeline(d3.select(this.parentNode).attr('data-id'));
        }, 150);
      })
      .on('mouseleave', () => {
        clearTimeout(timer);
        self.application.selectLifeline();
      })
      .on('click', function f() {
        const id = d3.select(this.parentNode).attr('data-id');
        window.postMessage({ source, id, type: 'lifeline' }, origin);
      });

    gLifelines.selectAll('.asdcs-diagram-lifeline-heading-box')
      .on('mouseenter', function f() {
        timer = setTimeout(() => {
          self.application.selectLifeline(d3.select(this.parentNode).attr('data-id'));
        }, 150);
      })
      .on('mouseleave', () => {
        clearTimeout(timer);
        self.application.selectLifeline();
      })
      .on('click', function f() {
        const id = d3.select(this.parentNode).attr('data-id');
        window.postMessage({ source, id, type: 'lifelineHeader' }, origin);
      });

    gCanvas.selectAll('.asdcs-diagram-message-selectable')
      .on('mouseenter', function f() {
        self.events.message = { x: d3.event.pageX, y: d3.event.pageY };
        timer = setTimeout(() => {
          self.application.selectMessage(d3.select(this.parentNode).attr('data-id'));
        }, 200);
      })
      .on('mouseleave', () => {
        delete self.events.message;
        clearTimeout(timer);
        self.application.selectMessage();
      })
      .on('click', function f() {
        const id = d3.select(this.parentNode).attr('data-id');
        window.postMessage({ source, id, type: 'message' }, origin);
      });

  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get CSS classes to be applied to a message, according to whether request/response
   * or synchronous/asynchronous.
   * @param message message being rendered.
   * @returns CSS class name(s).
   * @private
   */
  static _getMessageStyles(message) {

    let marker = 'asdcsDiagramArrowSolid';
    let dasharray = '';
    let css = 'asdcs-diagram-message';
    if (message.type === 'request') {
      css = `${css} asdcs-diagram-message-request`;
    } else {
      css = `${css} asdcs-diagram-message-response`;
      marker = 'asdcsDiagramArrowOpen';
      dasharray = '30, 10';
    }

    if (message.asynchronous) {
      css = `${css} asdcs-diagram-message-asynchronous`;
      marker = 'asdcsDiagramArrowOpen';
    } else {
      css = `${css} asdcs-diagram-message-synchronous`;
    }

    return { css, marker, dasharray };
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Initialize or reinitialize zoom. This sets the initial zoom in the case of
   * a re-rendering, and initializes the eventhandling in all cases.
   *
   * It does some fairly risky parsing of the 'transform' attribute, assuming that it
   * can contain scale() and translate(). But only the zoom handler and us are writing
   * the transform values, so that's probably OK.
   *
   * @param gContent container.
   * @param width diagram width.
   * @param height diagram height.
   * @private
   */
  _initZoom(gContent, width, height) {

    const zoomed = function zoomed() {
      if (!this.initialTransformX && !this.initialTransformY) {
        this.initialTransformX = d3.event.transform.x;
        this.initialTransformY = d3.event.transform.y;
      }

      gContent.attr('transform',
				`translate(${d3.event.transform.x - this.initialTransformX}, ${d3.event.transform.y
				- this.initialTransformY})scale(${d3.event.transform.k}, ${d3.event.transform.k})`);
    };

    const viewWidth = this.state.width || this.options.svg.width;
    const viewHeight = this.state.height || this.options.svg.height;
    const scaleMinimum = this.options.svg.scale.minimum;
    const scaleWidth = viewWidth / width;
    const scaleHeight = viewHeight / height;

    let scale = scaleMinimum;
    if (this.options.svg.scale.width) {
      scale = Math.max(scale, scaleWidth);
    }
    if (this.options.svg.scale.height) {
      scale = Math.min(scale, scaleHeight);
    }

    scale = Math.max(scale, scaleMinimum);

    let translate = [0, 0];
    if (this.savedTransform) {
      const s = this.savedTransform;
      const scaleStart = s.indexOf('scale(');
      if (scaleStart !== -1) {
        scale = parseFloat(s.substring(scaleStart + 6, s.length - 1));
      }
      const translateStart = s.indexOf('translate(');
      if (translateStart !== -1) {
        const spec = s.substring(translateStart + 10, s.indexOf(')', translateStart));
        const tokens = spec.split(',');
        translate = [parseFloat(tokens[0]), parseFloat(tokens[1])];
      }

      gContent.attr('transform', this.savedTransform);
    } else {
      gContent.attr('transform', `scale(${scale})`);
    }

    const zoom = d3.zoom()
      .on('zoom', zoomed);

    this.svg.call(zoom);
    this.svg.call(zoom.scaleBy, scale);

    gContent.attr('transform', `translate(${translate[0]}, ${translate[1]})`);
    gContent.attr('transform', `scale(${scale})`);

  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Hide from the linter the fact that we're modifying the lifeline.
   * @param lifeline to be updated with X position.
   * @param x X position.
   * @private
   */
  static _processLifeline(lifeline, x) {
    const actor = lifeline;
    actor.id = actor.id || actor.name;
    actor.x = x;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Derive active occurrences for lifeline.
   * @param oData occurrences data.
   * @param lifelineId lifeline to be analyzed.
   * @returns {number}
   * @private
   */
  static _calcActive(oData, lifelineId) {
    const o = oData[lifelineId];
    let active = 0;
    if (o && o.active) {
      active = o.active.length;
    }
    return active;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Derive the X position of an occurrence on a lifeline, taking into account how
   * many occurrences are active.
   * @param active active count.
   * @param x lifeline X position; basis for offset.
   * @param arrow whether this is the arrow (to) end.
   * @param leftToRight whether this message goes left-to-right.
   * @returns {*} calculated X position for occurrence left-hand side.
   * @private
   */
  _calcMessageX(active, x, arrow, leftToRight) {
    const width = this.options.lifelines.occurrences.width;
    const halfWidth = width / 2;
    const active0 = Math.max(0, active - 1);
    let calculated = x + (active0 * width);
    if (arrow) {
      // End (ARROW).
      if (leftToRight) {
        calculated -= halfWidth;
      } else {
        calculated += halfWidth;
      }
    } else {
      // Start (NOT ARROW).
      if (leftToRight) {
        calculated += halfWidth;
      } else {
        calculated -= halfWidth;
      }
    }

    return calculated;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Show popup upon hovering over a messages that has associated notes.
   * @param id
   * @private
   */
  _showNotesPopup(id) {
    if (this.popup) {
      if (id) {
        const message = this.application.getModel().getMessageById(id);
        if (message && message.notes && message.notes.length > 0 && this.events.message) {
          this.popup.setState({
            visible: true,
            left: this.events.message.x - 50,
            top: this.events.message.y + 20,
            notes: message.notes[0],
          });
        }
      } else {
        this.popup.setState({ visible: false, notes: '' });
      }
    }
  }
}


Diagram.propTypes = {
  application: React.PropTypes.object.isRequired,
};
