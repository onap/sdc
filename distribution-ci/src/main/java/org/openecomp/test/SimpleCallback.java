package org.openecomp.test;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;

import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.consumer.IDistributionStatusMessage;
import org.openecomp.sdc.api.consumer.INotificationCallback;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.openecomp.sdc.api.notification.IVfModuleMetadata;
import org.openecomp.sdc.api.results.IDistributionClientDownloadResult;
import org.openecomp.sdc.api.results.IDistributionClientResult;
import org.openecomp.sdc.utils.ArtifactTypeEnum;
import org.openecomp.sdc.utils.DistributionActionResultEnum;
import org.openecomp.sdc.utils.DistributionStatusEnum;
import org.openecomp.sdc.ci.tests.utils.Decoder;
import org.openecomp.sdc.ci.tests.utils.general.FileUtils;
import com.google.common.io.BaseEncoding;

public class SimpleCallback implements INotificationCallback {
	private IDistributionClient client;
	public List<IArtifactInfo> iArtifactInfo;

	public final Map<String, IDistributionClientResult> simpleCallbackResults = new HashMap<String, IDistributionClientResult>();

	public Map<String, IDistributionClientResult> getSimpleCallbackResults() {
		return simpleCallbackResults;
	}

	public List<IArtifactInfo> getIArtifactInfo() {
		return iArtifactInfo;
	}

	public SimpleCallback(IDistributionClient client) {
		this.client = client;
	}

	public void activateCallback(INotificationData data) {

		List<IArtifactInfo> artifacts = getArtifacts(data);

		for (IArtifactInfo iArtifactInfo : artifacts) {

			IArtifactInfo artifactMetadataByUUID = data.getArtifactMetadataByUUID(iArtifactInfo.getArtifactUUID());
			assertEquals("check artifact checksum", iArtifactInfo.getArtifactChecksum(), artifactMetadataByUUID.getArtifactChecksum());
			System.out.println(artifactMetadataByUUID.getArtifactURL());
			if (artifactMetadataByUUID.getArtifactType().equals(ArtifactTypeEnum.VF_MODULES_METADATA)) {
				IDistributionClientDownloadResult download = client.download(iArtifactInfo);
				if (download.getDistributionActionResult() == DistributionActionResultEnum.SUCCESS) {
					List<IVfModuleMetadata> decodeVfModuleArtifact = client.decodeVfModuleArtifact(download.getArtifactPayload());
					// assertEquals("decoded not equal to actual group amount ", decodeVfModuleArtifact.size(), 2);
					if (!decodeVfModuleArtifact.isEmpty()) {
						for (IVfModuleMetadata moduleMetadata : decodeVfModuleArtifact) {
							List<String> moduleArtifacts = moduleMetadata.getArtifacts();
							if (moduleArtifacts != null) {

								for (String artifactId : moduleArtifacts) {

									IArtifactInfo artifactInfo = data.getArtifactMetadataByUUID(artifactId);
									IDistributionClientDownloadResult downloadArt = client.download(artifactInfo);
									assertEquals(downloadArt.getDistributionActionResult(), DistributionActionResultEnum.SUCCESS);

								}

							}
						}
					}
				}
			}
		}

		for (IArtifactInfo relevantArtifact : artifacts) {
			// Download Artifact
			IDistributionClientDownloadResult downloadResult = client.download(relevantArtifact);

			postDownloadLogic(downloadResult);

			simpleCallbackResults.put("downloadResult", downloadResult);
			System.out.println("downloadResult: " + downloadResult.toString());
			System.out.println("<<<<<<<<<<< Artifact content >>>>>>>>>>");
			System.out.println(Decoder.encode(downloadResult.getArtifactPayload()));

			///// Print artifact content to console///////

			// byte[] contentInBytes = BaseEncoding.base64().decode(Decoder.encode(downloadResult.getArtifactPayload()));
			// try {
			// System.out.println("Source content: " + new String(contentInBytes, "UTF-8"));
			// } catch (UnsupportedEncodingException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
			System.out.println("ArtInfo_timeout: " + relevantArtifact.getArtifactTimeout());
			System.out.println("ArtInfo_Art_description: " + relevantArtifact.getArtifactDescription());
			System.out.println("ArtInfo_Art_CheckSum: " + relevantArtifact.getArtifactChecksum());
			System.out.println("ArtInfo_Art_Url: " + relevantArtifact.getArtifactURL());
			System.out.println("ArtInfo_Art_Type: " + relevantArtifact.getArtifactType());
			System.out.println("ArtInfo_Art_Name: " + relevantArtifact.getArtifactName());
			System.out.println("ArtInfo_UUID: " + relevantArtifact.getArtifactUUID());
			System.out.println("ArtInfo_Version: " + relevantArtifact.getArtifactVersion());
			System.out.println("ArtInfo_RelatedArtifacts: " + relevantArtifact.getRelatedArtifacts());

			System.out.println("ArtInfo_Serv_description: " + data.getServiceDescription());
			System.out.println("ArtInfo_Serv_Name: " + data.getServiceName());
			System.out.println("Get_serviceVersion: " + data.getServiceVersion());
			System.out.println("Get_Service_UUID: " + data.getServiceUUID());
			System.out.println("ArtInfo_DistributionId: " + data.getDistributionID());
			System.out.println("ArtInfo_ServiceInvariantUUID: " + data.getServiceInvariantUUID());

			// assertTrue("response code is not 200, returned :" + downloadResult.getDistributionActionResult(), downloadResult.getDistributionActionResult() == DistributionActionResultEnum.SUCCESS );

			try {
				String payload = new String(downloadResult.getArtifactPayload());
				// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
				// System.out.println(payload);
				// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");

			} catch (Exception e) {
				System.out.println("catch");
				// break;
				// TODO: handle exception
			}

			if (downloadResult.getDistributionActionResult() == DistributionActionResultEnum.SUCCESS) {
				handleSuccessfullDownload(data, relevantArtifact);
			} else {
				handleFailedDownload(data, relevantArtifact);
			}
		}
		// if (data != null){
		// iArtifactInfo.addAll(artifacts);
		// }

	}

	private List<IArtifactInfo> getArtifacts(INotificationData data) {
		List<IArtifactInfo> ret = new ArrayList<IArtifactInfo>();
		List<IResourceInstance> resources = data.getResources();
		// data.getArtifactMetadataByUUID(arg0)
		List<String> relevantArtifactTypes = client.getConfiguration().getRelevantArtifactTypes();

		List<IArtifactInfo> collect = resources.stream().flatMap(e -> e.getArtifacts().stream()).filter(p -> relevantArtifactTypes.contains(p.getArtifactType())).collect(Collectors.toList());
		// if( resources != null ){
		// for( IResourceInstance resourceInstance : resources){
		// if( resourceInstance.getArtifacts() != null ){
		//
		//
		//
		// ret.addAll(resourceInstance.getArtifacts());
		//
		//
		// }
		// }
		// }
		ret.addAll(collect);

		List<IArtifactInfo> servicesArt = data.getServiceArtifacts();
		if (servicesArt != null) {
			ret.addAll(servicesArt);
		}

		System.out.println("I am here: " + ret.toString());
		return ret;
	}

	private void handleFailedDownload(INotificationData data, IArtifactInfo relevantArtifact) {
		// Send Download Status
		IDistributionClientResult sendDownloadStatus = client.sendDownloadStatus(buildStatusMessage(client, data, relevantArtifact, DistributionStatusEnum.DOWNLOAD_ERROR));
		postDownloadStatusSendLogic(sendDownloadStatus);
	}

	private void handleSuccessfullDownload(INotificationData data, IArtifactInfo relevantArtifact) {
		// Send Download Status
		IDistributionClientResult sendDownloadStatus = client.sendDownloadStatus(buildStatusMessage(client, data, relevantArtifact, DistributionStatusEnum.DOWNLOAD_OK));

		simpleCallbackResults.put("sendDownloadStatus", sendDownloadStatus);
		// assertTrue("response code is not 200, returned :" + sendDownloadStatus.getDistributionActionResult(), sendDownloadStatus.getDistributionActionResult() == DistributionActionResultEnum.SUCCESS );

		// Doing deployment ...
		postDownloadStatusSendLogic(sendDownloadStatus);
		boolean isDeployedSuccessfully = handleDeployment();
		IDistributionClientResult deploymentStatus;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (isDeployedSuccessfully) {
			deploymentStatus = client.sendDeploymentStatus(buildStatusMessage(client, data, relevantArtifact, DistributionStatusEnum.DEPLOY_OK));

			simpleCallbackResults.put("sendDeploymentStatus", deploymentStatus);
			// assertTrue("response code is not 200, returned :" + deploymentStatus.getDistributionActionResult(), deploymentStatus.getDistributionActionResult() == DistributionActionResultEnum.SUCCESS );

		} else {
			deploymentStatus = handleFailedDeployment(data, relevantArtifact);
		}

		postDeploymentStatusSendLogic(deploymentStatus);
	}

	private IDistributionClientResult handleFailedDeployment(INotificationData data, IArtifactInfo relevantArtifact) {
		IDistributionClientResult deploymentStatus;
		boolean isAlreadyDeployed = checkIsDeployed();
		if (isAlreadyDeployed) {
			deploymentStatus = client.sendDeploymentStatus(buildStatusMessage(client, data, relevantArtifact, DistributionStatusEnum.ALREADY_DEPLOYED));
		} else {
			deploymentStatus = client.sendDeploymentStatus(buildStatusMessage(client, data, relevantArtifact, DistributionStatusEnum.DEPLOY_ERROR));
		}
		return deploymentStatus;
	}

	private void postDownloadLogic(IDistributionClientDownloadResult downloadResult) {
		// TODO Auto-generated method stub

	}

	private void postDownloadStatusSendLogic(IDistributionClientResult sendDownloadStatus) {
		// TODO Auto-generated method stub

	}

	private void postDeploymentStatusSendLogic(IDistributionClientResult deploymentStatus) {
		// TODO Auto-generated method stub

	}

	private boolean checkIsDeployed() {
		return false;
	}

	private boolean handleDeployment() {
		return true;
		// to return deploy_error use return false
		// return false;
	}

	public static IDistributionStatusMessage buildStatusMessage(final IDistributionClient client, final INotificationData data, final IArtifactInfo relevantArtifact, final DistributionStatusEnum status) {
		IDistributionStatusMessage statusMessage = new IDistributionStatusMessage() {

			public long getTimestamp() {
				long currentTimeMillis = System.currentTimeMillis();
				return currentTimeMillis;
			}

			public DistributionStatusEnum getStatus() {
				return status;
			}

			public String getDistributionID() {
				return data.getDistributionID();
			}

			public String getConsumerID() {
				return client.getConfiguration().getConsumerID();
			}

			public String getArtifactURL() {
				return relevantArtifact.getArtifactURL();
			}
		};
		return statusMessage;
	}

}
