package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class NetworkDaoZusammenImpl implements NetworkDao {

  private ZusammenAdaptor zusammenAdaptor;

  public NetworkDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {

  }

  @Override
  public Collection<NetworkEntity> list(NetworkEntity network) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(network.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(network.getVersion()));

    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, StructureElement.Networks.name())
        .stream().map(elementInfo ->
            mapElementInfoToNetwork(network.getVspId(), network.getVersion(), elementInfo))
        .collect(Collectors.toList());
  }

  private NetworkEntity mapElementInfoToNetwork(String vspId, Version version,
                                                ElementInfo elementInfo) {
    NetworkEntity networkEntity =
        new NetworkEntity(vspId, version, elementInfo.getId().getValue());
    networkEntity.setCompositionData(
        elementInfo.getInfo().getProperty(ElementPropertyName.compositionData.name()));
    return networkEntity;
  }

  @Override
  public void create(NetworkEntity network) {
    ZusammenElement networkElement = buildNetworkElement(network, Action.CREATE);
    ZusammenElement networksElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Networks, null);
    networksElement.setSubElements(Collections.singletonList(networkElement));

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(network.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    Optional<Element> savedElement =
        zusammenAdaptor.saveElement(context, elementContext, networksElement, "Create network");
    savedElement.ifPresent(element ->
        network.setId(element.getSubElements().iterator().next().getElementId().getValue()));
  }

  @Override
  public void update(NetworkEntity network) {
    ZusammenElement networkElement = buildNetworkElement(network, Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(network.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, networkElement,
        String.format("Update network with id %s", network.getId()));
  }

  @Override
  public NetworkEntity get(NetworkEntity network) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(network.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(network.getVersion()));

    Optional<Element> element =
        zusammenAdaptor.getElement(context, elementContext, network.getId());

    if (element.isPresent()) {
      network.setCompositionData(new String(FileUtils.toByteArray(element.get().getData())));
      return network;
    } else {
      return null;
    }
  }

  @Override
  public void delete(NetworkEntity network) {
    ZusammenElement networkElement = new ZusammenElement();
    networkElement.setElementId(new Id(network.getId()));
    networkElement.setAction(Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(network.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext,
        networkElement, String.format("Delete network with id %s", network.getId()));
  }


  @Override
  public void deleteAll(String vspId, Version version) {
    ZusammenElement networksElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Networks, Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, networksElement, "Delete all networks");
  }

  private ZusammenElement buildNetworkElement(NetworkEntity network, Action action) {
    ZusammenElement networkElement = new ZusammenElement();
    networkElement.setAction(action);
    if (network.getId() != null) {
      networkElement.setElementId(new Id(network.getId()));
    }
    Info info = new Info();
    info.addProperty(ElementPropertyName.type.name(), ElementType.Network);
    info.addProperty(ElementPropertyName.compositionData.name(), network.getCompositionData());
    networkElement.setInfo(info);
    networkElement.setData(new ByteArrayInputStream(network.getCompositionData().getBytes()));
    return networkElement;
  }
}
