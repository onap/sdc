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

package org.openecomp.sdc.asdctool.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.ComponentCassandraDao;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.cache.ComponentCache;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.ComponentCacheData;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.common.util.SerializationUtils;
import org.openecomp.sdc.common.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

/**
 * Created by esofer on 9/1/2016.
 */
public class PopulateComponentCache {

	private static Logger log = LoggerFactory.getLogger(PopulateComponentCache.class.getName());

	@Autowired
	protected ComponentCassandraDao componentCassandraDao;
	
	@Autowired
	ToscaOperationFacade toscaOperationFacade;

	@Autowired
	protected ComponentCache componentCache;

	private void exit(String stage, int i) {
		log.error("Failed on {}", stage);
		System.exit(i);

	}

	public void populateCache() {
		populateCache(ComponentTypeEnum.RESOURCE);
		populateCache(ComponentTypeEnum.SERVICE);
		populateCache(ComponentTypeEnum.PRODUCT);
	}

	@SuppressWarnings("unchecked")
	private void populateCache(ComponentTypeEnum componentTypeEnum) {

		List<String> list = new ArrayList<>();
		Either<TitanGraph, TitanOperationStatus> graph = toscaOperationFacade.getTitanDao().getGraph();
		TitanGraph titanGraph = graph.left().value();
		Iterable<TitanVertex> vertices = titanGraph.query()
				.has(GraphPropertiesDictionary.LABEL.getProperty(), componentTypeEnum.name().toLowerCase()).vertices();

		Iterator<TitanVertex> iterator = vertices.iterator();
		while (iterator.hasNext()) {
			TitanVertex vertex = (TitanVertex) iterator.next();

			// VertexProperty<Object> state =
			// vertex.property(GraphPropertiesDictionary.STATE.getProperty());
			// String stateValue = (String)state.value();

			// if (false ==
			// stateValue.equalsIgnoreCase(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name())
			// ) {
			VertexProperty<Object> uid = vertex.property(GraphPropertiesDictionary.UNIQUE_ID.getProperty());
			String uidValue = (String) uid.value();

			list.add(uidValue);
			// }
		}

		int counter = 0;
		for (String componentUid : list) {

			long time1 = System.currentTimeMillis();

			/////////////////////////////////////////////////////////////////////////////////////
			// Pay attention. The component is fetched from the cache in case it
			///////////////////////////////////////////////////////////////////////////////////// is
			///////////////////////////////////////////////////////////////////////////////////// already
			///////////////////////////////////////////////////////////////////////////////////// there.
			/////////////////////////////////////////////////////////////////////////////////////
			Component component = null;
			Either<Resource, StorageOperationStatus> getComponentRes = toscaOperationFacade.getToscaElement(componentUid);
			if (getComponentRes.isRight()) {
				exit("get component", 1);
			}
			component = getComponentRes.left().value();

			long time2 = System.currentTimeMillis();
			// System.out.println("fetch resource " + resource.getName());
			// System.out.println("fetch resource time is " + (time2 - time1) +
			// " ms");

			boolean setComponent = componentCache.setComponent(component, componentTypeEnum.getNodeType());
			if (setComponent) {
				counter++;
			}

			/*
			 * Either<byte[], Boolean> valueRes =
			 * SerializationUtils.serializeExt(component);
			 * 
			 * if (valueRes.isRight()) { exit("serialize component " +
			 * component.getName(), 2); } byte[] value =
			 * valueRes.left().value(); log.info("byte[] size is " +
			 * value.length); //System.out.println("byte[] size is " +
			 * value.length);
			 * 
			 * byte[] zipped = null; try { zipped = ZipUtil.zipBytes(value);
			 * //System.out.println("byte[] size after zip is " +
			 * zipped.length);
			 * 
			 * ComponentCacheData componentCacheData = new ComponentCacheData();
			 * componentCacheData.setDataAsArray(zipped);
			 * componentCacheData.setIsZipped(true);
			 * componentCacheData.setId(componentUid);
			 * componentCacheData.setModificationTime(new
			 * Date(component.getLastUpdateDate()));
			 * componentCacheData.setType(component.getComponentType().name().
			 * toLowerCase());
			 * 
			 * long averageInsertTimeInMilli =
			 * writeResourceToCassandraComponent(componentCacheData); log.
			 * info("After adding component {} to cassandra. Insert time is {} ms."
			 * , componentUid, averageInsertTimeInMilli);
			 * 
			 * } catch (IOException e) {
			 * e.printStackTrace(); }
			 */

		}

		log.debug("The number of saved components of type {} is {}. Total size is {}", componentTypeEnum, counter,
				list.size());

	}

	private long writeResourceToCassandraComponent(ComponentCacheData componentCacheData) {

		long startTime = System.currentTimeMillis();

		// call to cassandra read
		CassandraOperationStatus saveArtifact = componentCassandraDao.saveComponent(componentCacheData);
		if (saveArtifact != CassandraOperationStatus.OK) {
			exit("writeResourceToCassandra", 3);
		}

		long endTime = System.currentTimeMillis();

		return (endTime - startTime);
	}

	private void deserializeByThreads(List<ESArtifactData> list, ExecutorService executor, int threadNumber) {

		long fullSearchStart = System.currentTimeMillis();
		// for (int k =0; k < parts; k++) {

		List<List<ESArtifactData>> lists = new ArrayList<>();
		for (int i = 0; i < threadNumber; i++) {
			lists.add(new ArrayList<>());
		}

		List<Future<List<Resource>>> results = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			lists.get(i % threadNumber).add(list.get(i));
		}

		for (int i = 0; i < threadNumber; i++) {

			// Callable<List<Resource>> worker = new
			// MyDesrializabletCallable(lists.get(i), i);
			Callable<List<Resource>> worker = new My3rdPartyDesrializabletCallable(lists.get(i), i);
			Future<List<Resource>> submit = executor.submit(worker);
			results.add(submit);
		}

		long fullSearchStart2 = System.currentTimeMillis();
		for (Future<List<Resource>> future : results) {
			try {
				while (false == future.isDone()) {
					Thread.sleep(1);
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		long fullSearchEnd2 = System.currentTimeMillis();
		log.info("esofer time wait to threads finish {} ms",((fullSearchEnd2 - fullSearchStart2)));
		// }
		long fullSearchEnd = System.currentTimeMillis();

		log.info("esofer full desrialize time {} ms",((fullSearchEnd - fullSearchStart)));
		System.out.println("esofer full desrialize time " + ((fullSearchEnd - fullSearchStart)) + " ms");
	}

	public class MyDesrializabletCallable implements Callable<List<Resource>> {

		List<ESArtifactData> list;
		int i;

		public MyDesrializabletCallable(List<ESArtifactData> list, int i) {
			super();
			this.list = list;
			this.i = i;
		}

		@Override
		public List<Resource> call() throws Exception {
			List<Resource> resources = new ArrayList<>();
			long startSer = System.currentTimeMillis();
			long endSer = System.currentTimeMillis();
			long startUnzip = System.currentTimeMillis();
			long endUnzip = System.currentTimeMillis();

			long avgUnzip = 0;
			long avgSer = 0;
			for (ESArtifactData esArtifactData : list) {

				byte[] dataAsArray = esArtifactData.getDataAsArray();
				startUnzip = System.nanoTime();
				dataAsArray = ZipUtil.unzip(dataAsArray);
				endUnzip = System.nanoTime();
				avgUnzip += (endUnzip - startUnzip);

				startSer = System.nanoTime();
				Either<Object, Boolean> deserialize = SerializationUtils.deserialize(dataAsArray);
				endSer = System.nanoTime();
				avgSer += (endSer - startSer);
				// Either<Object, Boolean> deserialize =
				// SerializationUtils.deserialize(esArtifactData.getDataAsArray());
				if (deserialize.isRight()) {
					exit("convertByteArrayToResource " + deserialize.right().value(), 5);
				}

				Resource resource = (Resource) deserialize.left().value();
				resources.add(resource);
				// System.out.println("After desrialize T[" + i + "]resource " +
				// resource.getUniqueId());
			}

			System.out.println("After desrialize average desrialize " + list.size() + " T[" + i + "] "
					+ (avgSer / 1000 / list.size()) + " micro");
			System.out.println(
					"After desrialize average unzip T[" + i + "] " + (avgUnzip / 1000 / list.size()) + " micro");

			////////////////////////
			// maybe register most frequently used classes on conf
			// write
			// byte barray[] = conf.asByteArray(mySerializableObject);
			// read
			// MyObject object = (MyObject)conf.asObject(barray);

			return resources;
		}
	}

	public class My3rdPartyDesrializabletCallable implements Callable<List<Resource>> {

		List<ESArtifactData> list;
		int i;

		public My3rdPartyDesrializabletCallable(List<ESArtifactData> list, int i) {
			super();
			this.list = list;
			this.i = i;
		}

		@Override
		public List<Resource> call() throws Exception {
			List<Resource> resources = new ArrayList<>();
			long startSer = System.currentTimeMillis();
			long endSer = System.currentTimeMillis();
			long startUnzip = System.currentTimeMillis();
			long endUnzip = System.currentTimeMillis();

			long avgUnzip = 0;
			long avgSer = 0;
			for (ESArtifactData esArtifactData : list) {

				byte[] dataAsArray = esArtifactData.getDataAsArray();
				startUnzip = System.nanoTime();
				dataAsArray = ZipUtil.unzip(dataAsArray);
				endUnzip = System.nanoTime();
				avgUnzip += (endUnzip - startUnzip);

				startSer = System.nanoTime();

				Either<Resource, Boolean> deserializeExt = SerializationUtils.deserializeExt(dataAsArray,
						Resource.class, "");

				if (deserializeExt.isLeft()) {
					Resource resource = deserializeExt.left().value();
					// System.out.println("=============================================");
					// System.out.println(resource.getCapabilities().size());
					// System.out.println(resource.getRequirements().size());
					endSer = System.nanoTime();
					avgSer += (endSer - startSer);
					resources.add(resource);
					// System.out.println("After desrialize T[" + i + "]resource
					// " + resource.getUniqueId());
				}
			}

			System.out.println("After desrialize average desrialize " + list.size() + " T[" + i + "] "
					+ (avgSer / 1000 / list.size()) + " micro");
			System.out.println(
					"After desrialize average unzip T[" + i + "] " + (avgUnzip / 1000 / list.size()) + " micro");

			return resources;
		}
	}

}
