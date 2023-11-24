/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.1.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.1.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2024 Wren Security
 */
package org.forgerock.openidm.repo.jdbc.impl.refactor.handler;

import java.util.HashMap;
import java.util.Map;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;

/**
 * Parsed generic table configuration.
 *
 * <p>
 * This class corresponds to <code>$.resourceMapping.genericMapping[*]</code> properties of {@code JDBCRepoService}'s
 * service configuration.
 */
class GenericTableConfig {

    /**
     * Property value type.
     */
    public enum ValueType {

        STRING, NUMBER, BOOLEAN;

    }

    /**
     * Main table storing the generic object state.
     */
    public final String mainTableName;

    /**
     * Helper properties table used for property querying when the DB does not support JSON values.
     */
    public final String propTableName;

    /**
     * Whether properties should be stored in the helper table by default.
     */
    public final boolean searchableDefault;

    /**
     * Explicit search configuration (overrides {@link #searchableDefault}).
     */
    public final Map<JsonPointer, Boolean> explicitlySearchable;

    /**
     * Expected property value types (used for type casting).
     */
    // TODO this is a new undocumented feature (this should allow correct numeric ordering)
    public final Map<JsonPointer, ValueType> propertyTypes;

    /**
     * Flag indicating that the configuration defines at least one searchable property.
     */
    public final boolean containsSearchable;

    private GenericTableConfig(JsonValue tableConfig) {
        tableConfig.required();

        mainTableName = tableConfig.get("mainTable").required().asString();
        propTableName = tableConfig.get("propertiesTable").required().asString();
        searchableDefault = tableConfig.get("searchableDefault").defaultTo(Boolean.TRUE).asBoolean();

        var propsConfig = tableConfig.get("properties");
        Map<JsonPointer, Boolean> explicitlySearchable = new HashMap<>();
        Map<JsonPointer, ValueType> propertyTypes = new HashMap<>();
        for (var propName : propsConfig.keys()) {
            var propConfig = propsConfig.get(propName);
            var jsonPointer = new JsonPointer(propName);
            if (propConfig.isDefined("searchable")) {
                explicitlySearchable.put(jsonPointer, propConfig.get("searchable").asBoolean());
            }
            if (propConfig.isDefined("type")) {
                propertyTypes.put(jsonPointer, ValueType.valueOf(propConfig.get("type").asString()));
            }
        }
        this.explicitlySearchable = Map.copyOf(explicitlySearchable);
        this.propertyTypes = Map.copyOf(propertyTypes);

        containsSearchable = searchableDefault || explicitlySearchable.containsValue(Boolean.TRUE);
    }

    /**
     * Determine if the property defined by the given pointer can be used in query filters.
     *
     * @param pointer property pointer
     * @return {@code true} if the property can be used in query filters, {@code false} if not
     */
    public boolean isSearchable(JsonPointer pointer) {
        Boolean explicit = null;
        while (!pointer.isEmpty() && explicit == null) {
            explicit = explicitlySearchable.get(pointer);
            pointer = pointer.parent();
        }
        return explicit != null ? explicit : searchableDefault;
    }

    /**
     * Parse table configuration.
     *
     * @param tableConfig JSON object with table configuration
     * @return parsed table configuration
     */
    public static GenericTableConfig parse(JsonValue tableConfig) {
        return new GenericTableConfig(tableConfig);
    }

}
