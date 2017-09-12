package org.openecomp.core.converter.factory;

import org.openecomp.core.converter.ToscaConverter;
import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

public abstract class ToscaConverterFactory extends AbstractComponentFactory<ToscaConverter> {

  public static ToscaConverterFactory getInstance(){
    return AbstractFactory.getInstance(ToscaConverterFactory.class);
  }
}
