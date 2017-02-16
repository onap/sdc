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

import _merge from 'lodash/merge';
// import jsonschema from 'jsonschema';

import Common from '../common/Common';
import Metamodel from './Metamodel';

/**
 * A wrapper for a model instance.
 */
export default class Model {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct model from model JSON. JSON is assumed to be in more or less
   * the correct structure, but it's OK if it's missing IDs.
   *
   * @param json initial JSON; will be updated in situ.
   * @param metamodel Metaobject definition.
   */
  constructor(json, metamodel) {

    if (metamodel) {
      Common.assertInstanceOf(metamodel, Metamodel);
    }

    this.metamodel = metamodel || Metamodel.getDefault();
    Common.assertInstanceOf(this.metamodel, Metamodel);

    this.jsonschema = require('./schema/asdc_sequencer_schema.json');
    this.templates = {
      defaultModel: require('./templates/default.model.json'),
      defaultMetamodel: require('./templates/default.metamodel.json'),
    };

    this.model = this._preprocess(Common.assertType(json, 'Object'));
    Common.assertPlainObject(this.model);

    this.renumber();

    this.addLifeline = this.addLifeline.bind(this);
    this.addMessage = this.addMessage.bind(this);
    this.renumber = this.renumber.bind(this);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Unwrap to get model object.
   * @returns {*}
   */
  unwrap() {
    return Common.assertPlainObject(this.model);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get the metamodel which defines valid states for this model.
   * @returns Metamodel definition.
   */
  getMetamodel() {
    return Common.assertInstanceOf(this.metamodel, Metamodel);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Find lifeline by its ID.
   * @param id lifeline ID.
   * @returns lifeline object, if found.
   */
  getLifelineById(id) {
    for (const lifeline of this.model.diagram.lifelines) {
      if (lifeline.id === id) {
        return lifeline;
      }
    }
    return undefined;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get message by ID.
   * @param id message ID.
   * @returns message if matched.
   */
  getMessageById(id) {
    Common.assertNotNull(id);
    const step = this.getStepByMessageId(id);
    if (step) {
      return step.message;
    }
    return undefined;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get step by message ID.
   * @param id step ID.
   * @returns step if matched.
   */
  getStepByMessageId(id) {
    Common.assertNotNull(id);
    for (const step of this.model.diagram.steps) {
      if (step.message && step.message.id === id) {
        return step;
      }
    }
    return undefined;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Add message to steps.
   * @returns {{}}
   */
  addMessage(index) {
    const d = this.model.diagram;
    const step = {};
    step.message = {};
    step.message.id = Model._guid();
    step.message.name = '[Unnamed Message]';
    step.message.type = 'request';
    step.message.from = d.lifelines.length > 0 ? d.lifelines[0].id : -1;
    step.message.to = d.lifelines.length > 1 ? d.lifelines[1].id : -1;
    if (index >= 0) {
      d.steps.splice(index, 0, step);
    } else {
      d.steps.push(step);
    }
    this.renumber();
    return step;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Delete message with ID.
   * @param id to be deleted.
   */
  deleteMessageById(id) {
    Common.assertNotNull(id);
    const step = this.getStepByMessageId(id);
    if (step) {
      const index = this.model.diagram.steps.indexOf(step);
      if (index !== -1) {
        this.model.diagram.steps.splice(index, 1);
      }
    }
    this.renumber();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Add lifeline to lifelines.
   * @param index optional index.
   * @returns {{}}
   */
  addLifeline(index) {
    const lifeline = {};
    lifeline.id = Model._guid();
    lifeline.name = '[Unnamed Lifeline]';
    if (index >= 0) {
      this.model.diagram.lifelines.splice(index, 0, lifeline);
    } else {
      this.model.diagram.lifelines.push(lifeline);
    }
    this.renumber();
    return lifeline;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Delete lifeline with ID.
   * @param id to be deleted.
   */
  deleteLifelineById(id) {
    Common.assertNotNull(id);
    this.deleteStepsByLifelineId(id);
    const lifeline = this.getLifelineById(id);
    if (lifeline) {
      const index = this.model.diagram.lifelines.indexOf(lifeline);
      if (index !== -1) {
        this.model.diagram.lifelines.splice(index, 1);
      }
    }
    this.renumber();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Delete all steps corresponding to lifeline.
   * @param id lifeline ID.
   */
  deleteStepsByLifelineId(id) {
    Common.assertNotNull(id);
    const steps = this.getStepsByLifelineId(id);
    for (const step of steps) {
      this.deleteMessageById(step.message.id);
    }
    this.renumber();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get all steps corresponding to lifeline.
   * @param id lifeline ID.
   * @return steps from/to lifeline.
   */
  getStepsByLifelineId(id) {
    Common.assertNotNull(id);
    const steps = [];
    for (const step of this.model.diagram.steps) {
      if (step.message) {
        if (step.message.from === id || step.message.to === id) {
          steps.push(step);
        }
      }
    }
    return steps;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Validate model. Disabled, because we removed the jsonschema dependency.
   * @returns {Array} of validation errors, if any.
   */
  validate() {
    const errors = [];
    return errors;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Reorder messages.
   * @param index message index.
   * @param afterIndex new (after) index.
   */
  reorderMessages(index, afterIndex) {
    Common.assertType(index, 'Number');
    Common.assertType(afterIndex, 'Number');
    const steps = this.model.diagram.steps;
    const element = steps[index];
    steps.splice(index, 1);
    steps.splice(afterIndex, 0, element);
    this.renumber();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Reorder lifelines.
   * @param index lifeline index.
   * @param afterIndex new (after) index.
   */
  reorderLifelines(index, afterIndex) {
    Common.assertType(index, 'Number');
    Common.assertType(afterIndex, 'Number');
    const lifelines = this.model.diagram.lifelines;
    const element = lifelines[index];
    lifelines.splice(index, 1);
    lifelines.splice(afterIndex, 0, element);
    this.renumber();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Renumber lifelines and messages.
   */
  renumber() {
    const modelJSON = this.unwrap();
    let stepIndex = 1;
    let lifelineIndex = 1;
    for (const step of modelJSON.diagram.steps) {
      if (step.message) {
        step.message.index = stepIndex++;
      }
    }
    for (const lifeline of modelJSON.diagram.lifelines) {
      lifeline.index = lifelineIndex++;
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Build a simple, navigable dataset describing fragments.
   * @returns {{}}, indexed by (stop) message ID, describing fragments.
   */
  analyzeFragments() {

    const fData = {};

    let depth = 0;
    const modelJSON = this.unwrap();
    const open = [];

    const getData = function g(stop, fragment) {
      let data = fData[stop];
      if (!data) {
        data = { stop, start: [], fragment };
        fData[stop] = data;
      }
      return data;
    };

    const fragmentsByStart = {};
    for (const step of modelJSON.diagram.steps) {
      if (step.message && step.message.fragment) {
        const message = step.message;
        const fragment = message.fragment;
        if (fragment.start) {
          fragmentsByStart[fragment.start] = fragment;
          open.push(message.id);
          depth++;
        }
        if (fragment.stop) {
          if (open.length > 0) {
            getData(message.id).start.push(open.pop());
          }
          depth = Math.max(depth - 1, 0);
        }
      }
    }

    if (open.length > 0) {
      for (const o of open) {
        getData(o, fragmentsByStart[o]).start.push(o);
      }
    }

    return fData;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Build a simple, navigable dataset describing occurrences.
   * @returns a map, indexed by lifeline ID, of objects containing {start:[],stop:[],active[]}.
   * @private
   */
  analyzeOccurrences() {

    const oData = {};

    // A few inline functions. They make this method kinda lengthy, but they
    // reduce clutter in the class and keep it coherent, so it's OK.

    const getDataByLifelineId = function get(lifelineId) {
      if (!oData[lifelineId]) {
        oData[lifelineId] = { active: [], start: {}, stop: {} };
      }
      return oData[lifelineId];
    };

    const contains = function contains(array, value) {
      return (array && (array.indexOf(value) !== -1));
    };

    const process = function process(message, lifelineId) {
      const oRule = message.occurrences;
      if (oRule) {

        const oDataLifeline = getDataByLifelineId(lifelineId);
        if (oDataLifeline) {

          // Record all starts.

          if (contains(oRule.start, lifelineId)) {
            oDataLifeline.active.push(message.id);
            oDataLifeline.start[message.id] = undefined;
          }

          // Reconcile with stops.

          if (contains(oRule.stop, lifelineId)) {
            const startMessageId = oDataLifeline.active.pop();
            oDataLifeline.stop[message.id] = startMessageId;
            if (startMessageId) {
              oDataLifeline.start[startMessageId] = message.id;
            }
          }
        }
      }
    };

    // Analyze start and end.

    const modelJSON = this.unwrap();
    for (const step of modelJSON.diagram.steps) {
      if (step.message) {
        const message = step.message;
        if (message.occurrences) {
          process(message, message.from);
          process(message, message.to);
        }
      }
    }

    // Reset active. (We used it, but it's not actually for us; it's for keeping
    // track of active occurrences when rendering the diagram.)

    for (const lifelineId of Object.keys(oData)) {
      oData[lifelineId].active = [];
    }

    // Reconcile the start and end (message ID) maps for each lifeline,
    // finding a "stop" for every start. Default to starting and stopping
    // on the same message, which is the same as no occurrence.

    for (const lifelineId of Object.keys(oData)) {
      const lifelineData = oData[lifelineId];
      for (const startId of Object.keys(lifelineData.start)) {
        const stopId = lifelineData.start[startId];
        if (!stopId) {
          lifelineData.start[startId] = startId;
          lifelineData.stop[startId] = startId;
        }
      }
    }

    return oData;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Preprocess model, adding IDs and whatnot.
   * @param original to be preprocessed.
   * @returns preprocessed JSON.
   * @private
   */
  _preprocess(original) {

    const json = _merge({}, this.templates.defaultModel, original);
    const metamodel = this.metamodel.unwrap();
    if (!json.diagram.metadata.ref) {
      if (metamodel.diagram.metadata.id) {
        json.diagram.metadata.ref = metamodel.diagram.metadata.id;
      } else {
        json.diagram.metadata.ref = '$';
      }
    }

    for (const lifeline of json.diagram.lifelines) {
      lifeline.id = lifeline.id || lifeline.name;
    }

    for (const step of json.diagram.steps) {
      if (step.message) {
        step.message.id = step.message.id || Model._guid();
        const occurrences = step.message.occurrences;
        if (occurrences) {
          occurrences.start = occurrences.start || [];
          occurrences.stop = occurrences.stop || [];
        }
      }
    }

    if (!json.diagram.metadata.id || json.diagram.metadata.id === '$') {
      json.diagram.metadata.id = Model._guid();
    }

    return json;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Create pseudo-UUID.
   * @returns {string}
   * @private
   */
  static _guid() {
    function s4() {
      return Math.floor((1 + Math.random()) * 0x10000)
        .toString(16)
        .substring(1);
    }
    return `${s4()}-${s4()}-${s4()}-${s4()}-${s4()}-${s4()}-${s4()}-${s4()}`;
  }

}
