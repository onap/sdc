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

package org.openecomp.sdc.common.datastructure;

/**
 * Very Basic Wrapper class.
 * 
 * @author mshitrit
 * 
 * @param <T>
 */
public class Wrapper<T> {
	private T innerElement;

	public Wrapper(T innerElement) {
		this.innerElement = innerElement;
	}

	public Wrapper() {
		this.innerElement = null;
	}

	public T getInnerElement() {
		return innerElement;
	}

	public void setInnerElement(T innerElement) {
		this.innerElement = innerElement;
	}

	public boolean isEmpty() {
		return innerElement == null;
	}
}
