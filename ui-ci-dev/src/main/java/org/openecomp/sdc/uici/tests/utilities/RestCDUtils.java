package org.openecomp.sdc.uici.tests.utilities;

import static org.openecomp.sdc.common.datastructure.FunctionalInterfaces.retryMethodOnResult;
import static org.openecomp.sdc.common.datastructure.FunctionalInterfaces.swallowException;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.http.HttpStatus;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;

public class RestCDUtils {

	private static void setResourceUniqueIdAndUUID(ComponentReqDetails element, RestResponse getResourceResponse) {
		element.setUniqueId(ResponseParser.getUniqueIdFromResponse(getResourceResponse));
		element.setUUID(ResponseParser.getUuidFromResponse(getResourceResponse));
	}

	public static RestResponse getResourceByNameAndVersionRetryOnFail(String userId, String resourceName,
			String resourceVersion) {
		Supplier<RestResponse> resourceGetter = () -> swallowException(
				() -> ResourceRestUtils.getResourceByNameAndVersion(userId, resourceName, resourceVersion));
		Function<RestResponse, Boolean> validator = restRes -> restRes.getErrorCode() == HttpStatus.SC_OK;
		return retryMethodOnResult(resourceGetter, validator);
	}

	public static RestResponse getResource(ResourceReqDetails resource) {
		try {
			System.out.println("trying to get resource");
			RestResponse getResourceResponse = null;
			String reourceUniqueId = resource.getUniqueId();

			if (reourceUniqueId != null) {
				GeneralUIUtils.sleep(1000);
				getResourceResponse = ResourceRestUtils.getResource(reourceUniqueId);
				if (getResourceResponse.getErrorCode().intValue() == HttpStatus.SC_OK) {
					System.out.println("succeeded to get resource");
				}
				return getResourceResponse;
			}
			JSONObject getResourceJSONObject = null;
			getResourceResponse = getResourceByNameAndVersionRetryOnFail(UserRoleEnum.ADMIN.getUserId(),
					resource.getName(), resource.getVersion());
			if (getResourceResponse.getErrorCode().intValue() == HttpStatus.SC_OK) {
				JSONArray jArray = new JSONArray(getResourceResponse.getResponse());
				for (int i = 0; i < jArray.length(); i++) {
					getResourceJSONObject = jArray.getJSONObject(i);
					String resourceType = ResponseParser.getValueFromJsonResponse(getResourceJSONObject.toString(),
							"resourceType");
					if (resourceType.equals(resource.getResourceType())) {
						getResourceResponse.setResponse(getResourceJSONObject.toString());
						setResourceUniqueIdAndUUID(resource, getResourceResponse);
						System.out.println("succeeded to get resource");
						return getResourceResponse;
					}
				}
			}

			return getResourceResponse;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static RestResponse getService(ServiceReqDetails service, User user) {
		Supplier<RestResponse> serviceFetcher = () -> swallowException(
				() -> ServiceRestUtils.getServiceByNameAndVersion(user, service.getName(), service.getVersion()));
		Function<RestResponse, Boolean> verifier = restResponse -> restResponse.getErrorCode()
				.intValue() == HttpStatus.SC_OK;
		RestResponse getServiceResponse = retryMethodOnResult(serviceFetcher, verifier);

		if (getServiceResponse.getErrorCode().intValue() == HttpStatus.SC_OK) {
			setResourceUniqueIdAndUUID(service, getServiceResponse);
		}
		return getServiceResponse;
	}

	public static RestResponse getProduct(ProductReqDetails product, User user) throws Exception {
		Thread.sleep(3500);
		RestResponse getProductResponse = ProductRestUtils.getProductByNameAndVersion(product.getName(),
				product.getVersion(), user.getUserId());
		if (getProductResponse.getErrorCode().intValue() == 200) {
			setResourceUniqueIdAndUUID(product, getProductResponse);
		}
		return getProductResponse;
	}

	public static Map<String, String> getAllElementVersionsFromResponse(RestResponse getResource) throws Exception {
		Map<String, String> versionsMap = new HashMap<String, String>();
		try {
			ObjectMapper mapper = new ObjectMapper();

			JSONObject object = new JSONObject(getResource.getResponse());
			versionsMap = mapper.readValue(object.get("allVersions").toString(), Map.class);

		} catch (Exception e) {
			e.printStackTrace();
			return versionsMap;

		}

		return versionsMap;
	}

	public static void deleteElementVersions(Map<String, String> elementVersions, boolean isBeforeTest, Object clazz,
			User user) throws Exception {
		Iterator<String> iterator = elementVersions.keySet().iterator();
		while (iterator.hasNext()) {
			String singleVersion = iterator.next();
			String uniqueId = elementVersions.get(singleVersion);
			RestResponse deleteResponse = null;
			if (clazz instanceof ServiceReqDetails) {
				deleteResponse = ServiceRestUtils.deleteServiceById(uniqueId, user.getUserId());
			} else if (clazz instanceof ResourceReqDetails) {
				deleteResponse = ResourceRestUtils.deleteResource(uniqueId, user.getUserId());
			} else if (clazz instanceof ProductReqDetails) {
				deleteResponse = ProductRestUtils.deleteProduct(uniqueId, user.getUserId());
			}

			if (isBeforeTest) {
				assertTrue(deleteResponse.getErrorCode().intValue() == 204
						|| deleteResponse.getErrorCode().intValue() == 404);
			} else {
				assertTrue(deleteResponse.getErrorCode().intValue() == 204);
			}
		}
	}

	public static void deleteAllResourceVersionsAfterTest(ComponentReqDetails componentDetails,
			RestResponse getObjectResponse, User user) throws Exception {
		deleteAllComponentVersion(false, componentDetails, getObjectResponse, user);
	}

	public static void deleteAllResourceVersionsBeforeTest(ComponentReqDetails componentDetails,
			RestResponse getObjectResponse, User user) throws Exception {
		deleteAllComponentVersion(true, componentDetails, getObjectResponse, user);
	}

	public static void deleteAllComponentVersion(boolean isBeforeTest, ComponentReqDetails componentDetails,
			RestResponse getObjectResponse, User user) throws Exception {
		if (getObjectResponse.getErrorCode().intValue() == 404)
			return;
		Map<String, String> componentVersionsMap = getAllElementVersionsFromResponse(getObjectResponse);
		System.out.println("deleting...");
		deleteElementVersions(componentVersionsMap, isBeforeTest, componentDetails, user);
		componentDetails.setUniqueId(null);
	}

}