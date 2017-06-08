/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.common.errors;

public enum ErrorCategory {
  /**
   * System-level problems caused by external factors, resources shortage and similar. For instance:
   * <ul>
   * <li>I/O problems (network connectivity, filesystem access etc)</li>
   * <li>Java issues (missing or incompatible class definitions etc)</li>
   * <li>Environment problems</li>
   * </ul>
   */
  SYSTEM,

  /**
   * Application-level issues related to implementation of certain functionality (such as detected
   * illegal states of a program or data inconsistency).
   */
  APPLICATION,

  /**
   * Problems related to violations of different rules set either by product metadata (catalog
   * definitions or similar) or other types of constraints.
   */
  VALIDATION,

  /**
   * Problems caused by attempt of a user to perform certain operations which contradict the system
   * rules. Mostly applicable to UI-driven flows in presentation tier.
   */
  USER,

  /**
   * Improper values set in the system configuration (negative numbers, missing or inconsistent
   * definitions, mismatch with valid values list etc).
   */
  CONFIGURATION,

  /**
   * Security constraint violations (failed login attempts, lack of permissions to perform operation
   * and so on). Any problems related to authentication/authorization should use the special
   * category for system auditing purposes.
   */
  SECURITY
}
