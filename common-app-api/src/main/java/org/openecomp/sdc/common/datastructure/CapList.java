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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread Safe List with a cap for Max elements.<br>
 * If an element would be inserted to the list and it is full, the oldest
 * element will be taken out.
 * 
 * @author mshitrit
 *
 * @param <T>
 */
public class CapList<T> implements List<T> {
	private static final int DEFAULT_CAP = 1000;
	private int cap;
	private ReadWriteLock readWriteLock;
	private List<T> innerList;

	public CapList() {
		this(DEFAULT_CAP);
	}

	public CapList(int cap) {
		if (cap < 1) {
			throw new RuntimeException("List Cap Must Be Positive");
		}
		this.cap = cap;
		innerList = new ArrayList<>();
		readWriteLock = new ReentrantReadWriteLock();
	}

	@Override
	public boolean add(T e) {
		try {
			readWriteLock.writeLock().lock();
			boolean result = innerList.add(e);
			removeExtras();
			return result;

		} finally {
			readWriteLock.writeLock().unlock();
		}

	}

	private void removeExtras() {
		while (innerList.size() > cap) {
			innerList.remove(0);
		}
	}

	@Override
	public void add(int index, T element) {
		try {
			readWriteLock.writeLock().lock();
			innerList.add(index, element);
		} finally {
			readWriteLock.writeLock().unlock();
		}

	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		try {
			readWriteLock.writeLock().lock();
			boolean result = innerList.addAll(c);
			removeExtras();
			return result;
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		try {
			readWriteLock.writeLock().lock();
			boolean result = innerList.addAll(index, c);
			removeExtras();
			return result;
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	@Override
	public void clear() {
		try {
			readWriteLock.writeLock().lock();
			innerList.clear();
		} finally {
			readWriteLock.writeLock().unlock();
		}

	}

	@Override
	public boolean contains(Object o) {
		try {
			readWriteLock.readLock().lock();
			return innerList.contains(o);

		} finally {
			readWriteLock.readLock().unlock();
		}

	}

	@Override
	public boolean containsAll(Collection<?> c) {
		try {
			readWriteLock.readLock().lock();
			return innerList.containsAll(c);

		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public T get(int index) {
		try {
			readWriteLock.readLock().lock();
			return innerList.get(index);

		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public int indexOf(Object o) {
		try {
			readWriteLock.readLock().lock();
			return innerList.indexOf(o);

		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public boolean isEmpty() {
		try {
			readWriteLock.readLock().lock();
			return innerList.isEmpty();

		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public Iterator<T> iterator() {
		try {
			readWriteLock.readLock().lock();
			return innerList.iterator();

		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public int lastIndexOf(Object o) {
		try {
			readWriteLock.readLock().lock();
			return innerList.lastIndexOf(o);

		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public ListIterator<T> listIterator() {
		try {
			readWriteLock.readLock().lock();
			return innerList.listIterator();

		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		try {
			readWriteLock.readLock().lock();
			return innerList.listIterator(index);

		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public boolean remove(Object o) {
		try {
			readWriteLock.writeLock().lock();
			return innerList.remove(o);

		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	@Override
	public T remove(int index) {
		try {
			readWriteLock.writeLock().lock();
			return innerList.remove(index);

		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		try {
			readWriteLock.writeLock().lock();
			return innerList.removeAll(c);

		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		try {
			readWriteLock.writeLock().lock();
			return innerList.retainAll(c);

		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	@Override
	public T set(int index, T element) {
		try {
			readWriteLock.writeLock().lock();
			return innerList.set(index, element);

		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	@Override
	public int size() {
		try {
			readWriteLock.readLock().lock();
			return innerList.size();

		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		try {
			readWriteLock.readLock().lock();
			return innerList.subList(fromIndex, toIndex);

		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public Object[] toArray() {
		try {
			readWriteLock.readLock().lock();
			return innerList.toArray();

		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public <T> T[] toArray(T[] a) {
		try {
			readWriteLock.readLock().lock();
			return innerList.toArray(a);

		} finally {
			readWriteLock.readLock().unlock();
		}
	}

}
