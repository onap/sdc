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

package org.openecomp.sdc.be.dao.model;

import java.io.Serializable;

/**
 * Result for a multiple data query.
 * 
 * @author luc boutier
 */
@SuppressWarnings("PMD.UnusedPrivateField")
public class GetMultipleDataResult<T> implements Serializable {
	public String[] getTypes() {
		return types;
	}

	public T[] getData() {
		return data;
	}

	public void setTypes(String[] types) {
		this.types = types.clone();
	}

	public void setData(T[] data) {
		this.data = data.clone();
	}

	public void setQueryDuration(long queryDuration) {
		this.queryDuration = queryDuration;
	}

	public void setTotalResults(long totalResults) {
		this.totalResults = totalResults;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public void setTo(int to) {
		this.to = to;
	}

	public long getQueryDuration() {
		return queryDuration;
	}

	public long getTotalResults() {
		return totalResults;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	private static final long serialVersionUID = 1L;

	private String[] types;
	private T[] data;
	private long queryDuration;
	private long totalResults;
	private int from;
	private int to;

	/**
	 * Construct an object only with data and types
	 * 
	 * @param types
	 * @param data
	 */
	public GetMultipleDataResult(String[] types, T[] data) {
		this.types = types.clone();
		this.data = data.clone();
	}

	public GetMultipleDataResult(String[] types, Object[] data, long queryDuration, long totalResults, int from,
			int to) {

		this.types = types.clone();
		this.data = (T[]) data.clone();
		this.queryDuration = queryDuration;
		this.totalResults = totalResults;
		this.from = from;
		this.to = to;
	}

	public GetMultipleDataResult() {
	}
}
