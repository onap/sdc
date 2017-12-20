package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToNetworkConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.types.ElementPropertyName;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

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
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(network.getVspId(), network.getVersion().getId());

    Optional<ElementInfo> vspModel = zusammenAdaptor
        .getElementInfoByName(context, elementContext, null, ElementType.VspModel.name());
    if (!vspModel.isPresent()) {
      return new ArrayList<>();
    }

    ElementToNetworkConvertor convertor = new ElementToNetworkConvertor();
    return zusammenAdaptor.listElementsByName(context, elementContext, vspModel.get().getId(),
        ElementType.Networks.name()).stream()
        .map(convertor::convert)
        .map(entity -> {
          entity.setVspId(network.getVspId());
          entity.setVersion(network.getVersion());
          return entity;
        })
        .collect(Collectors.toList());
  }

  @Override
  public void create(NetworkEntity network) {
    ZusammenElement networkElement = buildNetworkElement(network, Action.CREATE);

    ZusammenElement networksElement = buildStructuralElement(ElementType.Networks, Action.IGNORE);
    networksElement.setSubElements(Collections.singletonList(networkElement));

    ZusammenElement vspModel = buildStructuralElement(ElementType.VspModel, Action.IGNORE);
    vspModel.addSubElement(networksElement);

    SessionContext context = createSessionContext();
    Element savedElement = zusammenAdaptor
        .saveElement(context, new ElementContext(network.getVspId(), network.getVersion().getId()),
            vspModel, "Create network");
    network.setId(savedElement.getSubElements().iterator().next()
        .getSubElements().iterator().next().getElementId().getValue());
  }

  @Override
  public void update(NetworkEntity network) {
    ZusammenElement networkElement = buildNetworkElement(network, Action.UPDATE);

    SessionContext context = createSessionContext();
    zusammenAdaptor
        .saveElement(context, new ElementContext(network.getVspId(), network.getVersion().getId()),
            networkElement, String.format("Update network with id %s", network.getId()));
  }

  @Override
  public NetworkEntity get(NetworkEntity network) {
    SessionContext context = createSessionContext();

    Optional<Element> element =
        zusammenAdaptor.getElement(context,
            new ElementContext(network.getVspId(), network.getVersion().getId()), network.getId());

    if (element.isPresent()) {
      ElementToNetworkConvertor convertor = new ElementToNetworkConvertor();
      NetworkEntity entity = convertor.convert(element.get());
      entity.setVspId(network.getVspId());
      entity.setVersion(network.getVersion());
      return entity;
    } else {
      return null;
    }
  }

  @Override
  public void delete(NetworkEntity network) {
    ZusammenElement networkElement = buildElement(new Id(network.getId()), Action.DELETE);

    SessionContext context = createSessionContext();
    zusammenAdaptor
        .saveElement(context, new ElementContext(network.getVspId(), network.getVersion().getId()),
            networkElement, String.format("Delete network with id %s", network.getId()));
  }


  @Override
  public void deleteAll(String vspId, Version version) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Collection<ElementInfo> vspModelSubs = zusammenAdaptor
        .listElementsByName(context, elementContext, null, ElementType.VspModel.name());

    Optional<ElementInfo> networksElement = vspModelSubs.stream()
        .filter(elementInfo -> elementInfo.getInfo() != null
            && ElementType.Networks.name().equals(elementInfo.getInfo().getName()))
        .findFirst();
    if (!networksElement.isPresent()) {
      return;
    }

    ZusammenElement networks = buildElement(networksElement.get().getId(), Action.IGNORE);
    networks.setSubElements(networksElement.get().getSubElements().stream()
        .map(network -> buildElement(network.getId(), Action.DELETE))
        .collect(Collectors.toList()));

    zusammenAdaptor.saveElement(context, elementContext, networks, "Delete all networks");
  }

  private ZusammenElement buildNetworkElement(NetworkEntity network, Action action) {
    ZusammenElement networkElement =
        buildElement(network.getId() == null ? null : new Id(network.getId()), action);
    Info info = new Info();
    info.addProperty(ElementPropertyName.elementType.name(), ElementType.Network);
    info.addProperty(ElementPropertyName.compositionData.name(), network.getCompositionData());
    networkElement.setInfo(info);
    networkElement.setData(new ByteArrayInputStream(network.getCompositionData().getBytes()));
    return networkElement;
  }
}
