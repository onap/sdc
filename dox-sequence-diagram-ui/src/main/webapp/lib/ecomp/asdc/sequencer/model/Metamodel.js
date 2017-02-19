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

import Common from '../common/Common';

/**
 * Rules governing what a definition can contain.
 */
export default class Metamodel {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct from JSON definition.
   * @param json schema definition.
   */
  constructor(json) {
    Common.assertType(json, 'Object');
    const dfault = require('./templates/default.metamodel.json');
    this.json = _merge({}, dfault, json);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get schema identifier.
   * @returns ID.
   */
  getId() {
    return this.json.diagram.metadata.id;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get lifeline constraints.
   * @returns {*}
   */
  getConstraints() {
    return this.json.diagram.lifelines.constraints;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get lifeline metadata by lifeline ID.
   * @param id sought lifeline.
   * @returns lifeline if found.
   */
  getLifelineById(id) {
    for (const lifeline of this.json.diagram.lifelines.lifelines) {
      if (lifeline.id === id) {
        return lifeline;
      }
    }
    return undefined;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get original JSON.
   * @returns JSON.
   */
  unwrap() {
    return this.json;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get default schema.
   * @returns Metamodel default (permissive) Metamodel.
   */
  static getDefault() {
    return new Metamodel({});
  }

}
