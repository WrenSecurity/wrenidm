package com.forgerock.openidm.web.component.formgenerator;

import java.util.HashMap;
/**
 *
 * @author Vilo Repan
 */
public class ValueHashMap<K, V> extends HashMap<K, V> {

    @Override
    public V put(K key, V value) {
        if (value == null || value.toString().isEmpty()) {
            remove(key);

            return value;
        }

        return super.put(key, value);
    }
}
