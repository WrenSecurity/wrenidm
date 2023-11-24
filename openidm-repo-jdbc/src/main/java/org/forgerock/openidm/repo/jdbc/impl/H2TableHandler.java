package org.forgerock.openidm.repo.jdbc.impl;

import org.forgerock.json.JsonValue;
import org.forgerock.openidm.repo.jdbc.SQLExceptionHandler;
import org.forgerock.openidm.repo.jdbc.impl.GenericTableHandler.QueryDefinition;

import java.util.Map;

public class H2TableHandler extends GenericTableHandler {

    public H2TableHandler(JsonValue tableConfig, String dbSchemaName, JsonValue queriesConfig, JsonValue commandsConfig, int maxBatchSize,
            SQLExceptionHandler sqlExceptionHandler) {
        super(tableConfig, dbSchemaName, queriesConfig, commandsConfig, maxBatchSize, sqlExceptionHandler);
    }

    @Override
    protected Map<QueryDefinition, String> initializeQueryMap() {
        Map<QueryDefinition, String> result = super.initializeQueryMap();

        String typeTable = dbSchemaName == null ? "objecttypes" : dbSchemaName + ".objecttypes";
        String mainTable = dbSchemaName == null ? mainTableName : dbSchemaName + "." + mainTableName;
        String propertyTable = dbSchemaName == null ? propTableName : dbSchemaName + "." + propTableName;

        result.put(QueryDefinition.UPDATEQUERYSTR, "UPDATE " + mainTable + " SET objectid = ?, rev = ?, fullobject = ? FORMAT JSON WHERE id = ?");
        result.put(QueryDefinition.CREATEQUERYSTR, "INSERT INTO " + mainTable + " (objecttypes_id, objectid, rev, fullobject) VALUES (?,?,?,? FORMAT JSON)");
        result.put(QueryDefinition.DELETEQUERYSTR, "DELETE FROM " + mainTable + " obj WHERE EXISTS (SELECT 1 FROM " + typeTable + " objtype WHERE obj.objecttypes_id = objtype.id AND objtype.objecttype = ?) AND obj.objectid = ? AND obj.rev = ?");
        result.put(QueryDefinition.PROPDELETEQUERYSTR, "DELETE FROM " + propertyTable + " WHERE " + mainTableName + "_id IN (SELECT obj.id FROM " + mainTable + " obj INNER JOIN " + typeTable + " objtype ON obj.objecttypes_id = objtype.id WHERE objtype.objecttype = ? AND obj.objectid = ?)");
        result.put(QueryDefinition.READFORUPDATEQUERYSTR, "SELECT obj.* FROM " + mainTable + " obj INNER JOIN " + typeTable + " objtype ON obj.objecttypes_id = objtype.id AND objtype.objecttype = ? WHERE obj.objectid  = ? FOR UPDATE OF obj");
        return result;
    }

}
