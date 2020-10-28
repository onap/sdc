package org.onap.sdc.tosca.services;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.parser.ParserException;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class StrictMapAppenderConstructor extends Constructor {
    /**
     * Instantiates a new Strict map appender constructor.
     *
     * @param theRoot the the root
     */
    public StrictMapAppenderConstructor(Class<?> theRoot) {
        super(theRoot);
    }

    @Override
    protected Map<Object, Object> createDefaultMap(int initSize) {
        final Map<Object, Object> delegate = super.createDefaultMap(initSize);
        return new AbstractMap<>() {
            @Override
            public Object put(Object key, Object value) {
                if (delegate.containsKey(key)) {
                    throw new IllegalStateException("duplicate key: " + key);
                }
                return delegate.put(key, value);
            }

            @Override
            public Set<Entry<Object, Object>> entrySet() {
                return delegate.entrySet();
            }
        };
    }

    @Override
    protected Map<Object, Object> constructMapping(MappingNode node) {
        try {
            return super.constructMapping(node);
        } catch (IllegalStateException exception) {
            throw new ParserException("while parsing MappingNode",
                    node.getStartMark(), exception.getMessage(),
                    node.getEndMark());
        }
    }
}
