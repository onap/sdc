package org.openecomp.sdc.healing.healers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.Map;


public class ComponentQuestionnaireHealer implements Healer {
  /*private static final VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();*/

  private static final ComponentDao componentDao =
      ComponentDaoFactory.getInstance().createInterface();
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  public ComponentQuestionnaireHealer(){

  }
  @Override
  public Object heal(Map<String, Object> healingParams) throws Exception {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    String vspId = (String) healingParams.get(SdcCommon.VSP_ID);
    Version version = (Version) healingParams.get(SdcCommon.VERSION);
    String user = (String) healingParams.get(SdcCommon.USER);
    Collection<ComponentEntity> componentEntities =
        componentDao.list(new ComponentEntity(vspId, version, null));
    componentEntities.forEach(componentEntity -> {
      /*String questionnaire=vendorSoftwareProductDao.getComponent(vspId, version, componentEntity
          .getId()).getQuestionnaireData();*/
      String questionnaire = componentDao.getQuestionnaireData(vspId, version, componentEntity
          .getId()).getQuestionnaireData();
      if (questionnaire != null) {
      JsonParser jsonParser = new JsonParser();
      JsonObject  json = (JsonObject) jsonParser.parse(questionnaire);
        if (json.getAsJsonObject("compute") != null && json.getAsJsonObject("compute")
            .getAsJsonObject("vmSizing") != null) {
          json.getAsJsonObject("compute").remove("vmSizing");
        }

        if (json.getAsJsonObject("compute") != null && json.getAsJsonObject("compute")
            .getAsJsonObject("numOfVMs") != null ) {
          if (json.getAsJsonObject("compute").getAsJsonObject("numOfVMs").
              get("CpuOverSubscriptionRatio") != null ) {
            json.getAsJsonObject("compute").getAsJsonObject("numOfVMs").remove
                ("CpuOverSubscriptionRatio");
          }
          if (json.getAsJsonObject("compute").getAsJsonObject("numOfVMs").
              get("MemoryRAM") != null ) {
            json.getAsJsonObject("compute").getAsJsonObject("numOfVMs").remove("MemoryRAM");
          }
        }

        if (json.getAsJsonObject("general") != null && json.getAsJsonObject("general")
            .getAsJsonObject("image") != null && json.getAsJsonObject("general").getAsJsonObject
            ("image").get("format") != null) {
          json.getAsJsonObject("general").getAsJsonObject("image").remove("format");
        }
      String questionnaireData = json.toString();
        /*vendorSoftwareProductDao.updateComponentQuestionnaire(vspId, version, componentEntity
            .getId(),questionnaireData);*/
        componentDao.updateQuestionnaireData(vspId, version, componentEntity.getId(),
            questionnaireData);
      }
    });
    return componentEntities;
  }
}
