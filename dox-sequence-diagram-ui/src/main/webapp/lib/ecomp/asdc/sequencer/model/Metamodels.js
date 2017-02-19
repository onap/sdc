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

import Common from '../common/Common';
import Metamodel from './Metamodel';

/**
 * A simple lookup for schemas by ID.
 */
export default class Metamodels {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct metamodels from provided JSON definitions.
   * @param metamodels JSON metamodel definitions.
   */
  constructor(metamodels) {

    Common.assertType(metamodels, 'Array');

    this.lookup = {};

    // Save each metamodel. It's up to the Metamodel class to make sense of
    // potentially nonsense metamodel definitions.

    for (const json of metamodels) {
      const metamodel = new Metamodel(json);
      this.lookup[metamodel.getId()] = metamodel;
    }

    // Set (or override) the default metamodel with the inlined one.

    this.lookup.$ = Metamodel.getDefault();
    Common.assertInstanceOf(this.lookup.$, Metamodel);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get Metamodel by its @id.
   * @param id identifier.
   * @returns Metamodel, or undefined if no matching metamodel found.
   */
  getMetamodel(id) {
    return this.lookup[id];
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get the default (permissive) metamodel.
   * @returns default Metamodel.
   */
  getDefault() {
    return this.lookup.$;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get metamodel by its @id, falling back to the default.
   * @param id identifier.
   * @returns matching metamodel, or default.
   */
  getMetamodelOrDefault(id) {
    const metamodel = this.getMetamodel(id);
    if (metamodel) {
      return metamodel;
    }
    return this.getDefault();
  }

}
