package org.openecomp.test;

import org.junit.rules.TestName;

//import static org.junit.Assert.assertTrue;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.concurrent.TimeUnit;
//
//import org.junit.After;
//import org.junit.Test;
//import org.junit.rules.TestName;
//
//import org.openecomp.sdc.api.IDistributionClient;
//import org.openecomp.sdc.api.notification.IArtifactInfo;
//import org.openecomp.sdc.api.notification.INotificationData;
//import org.openecomp.sdc.api.results.IDistributionClientResult;
//import org.openecomp.sdc.impl.DistributionClientFactory;
//import org.openecomp.sdc.utils.DistributionActionResultEnum;
//import org.openecomp.sdc.be.model.DistributionStatusEnum;
//import org.openecomp.sdc.be.model.Service;
//import org.openecomp.sdc.ci.tests.execute.lifecycle.LCSbaseTest;
//import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
////import org.openecomp.sdc.ci.tests.users.UserUtils;
//import org.openecomp.sdc.ci.tests.utils.DbUtils;
//import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;

public class E2eFlows {



//	private IDistributionClient client = DistributionClientFactory.createDistributionClient();
//	private ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();
//	
//	private CallableTask callableTask;
//	private NotificationCallback notifCallback;
//	private IArtifactInfo iArtifactInfo;
//

	
//
//	@After
//	public void after() throws Exception {
//		client.stop();
//		Thread.sleep(5000);
//	}
//
//	// ---------------------------------Success
//	// scenario--------------------------------------------------------------------------------
//
//	@Test
//	public void distributeService() throws Exception{
//		createServiceReadyForDistribution();
//		//RestResponse changeStateToDISTRIBUTED = serviceUtils.changeDistributionStatus(serviceDetails, "1.0", UserUtils.getGovernorDetails1(), "change", DistributionStatusEnum.DISTRIBUTED);
//		//assertTrue("response code is not 200, returned :" + changeStateToDISTRIBUTED.getErrorCode(), changeStateToDISTRIBUTED.getErrorCode() == 200);
//
//	}
//	@Test
//	public void E2E_Success() throws Exception {
//			Service service = createServiceReadyForDistribution();
//			
////			clean audit
//			DbUtils.cleanAllAudits();
//			
//			//Create task to run in BG
//			callableTask = new CallableTask(service);
//			
//			//Run the task in BG
//			System.err.println("Executing Task...");
//			Future<Boolean> f = newSingleThreadExecutor.submit(callableTask);
//			
//			//Wait for task to complete and return with result
//			
////			while (!f.isDone()){
//			System.err.println("Waiting for result...");
//			//	Thread.sleep(30000);
////			}
//			Boolean result = f.get(15, TimeUnit.SECONDS);
//			
//			System.out.println("future:" + result);
//			
//			//Check Results
//			notifCallback = callableTask.getNotifCallback();
//			
//			INotificationData data = notifCallback.getData();
//			
//			System.out.println("result map size  = "+  notifCallback.getSimpleCallbackResults().size());
//			IDistributionClientResult downloadResult = notifCallback.getSimpleCallbackResults().get("downloadResult");
//			assertTrue("response code is not SUCCESS, returned :"+ downloadResult.getDistributionActionResult(),downloadResult.getDistributionActionResult() == DistributionActionResultEnum.SUCCESS);
//
//	}
//
//
//	private Service createServiceReadyForDistribution() throws Exception {
////		RestResponse response = LCSbaseTest.certifyService(serviceDetails);
////		assertTrue("response code is not 200, returned :" + response.getErrorCode(), response.getErrorCode() == 200);
////		Service service = ResponseParser.convertServiceResponseToJavaObject(response.getResponse());
////		
////		RestResponse changeDistributionStateToApprove = serviceUtils.changeDistributionStateToApprove(service,UserUtils.getGovernorDetails1());
////		assertTrue("response code is not 200, returned :" + changeDistributionStateToApprove.getErrorCode(), changeDistributionStateToApprove.getErrorCode() == 200);
////		service = ResponseParser.convertServiceResponseToJavaObject(changeDistributionStateToApprove.getResponse());
////		return service;
//		return null;
//	}

}
