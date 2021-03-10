package org.forgerock.openidm.repo.jdbc.impl;

import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.openidm.crypto.CryptoService;
import org.forgerock.openidm.repo.jdbc.SQLExceptionHandler;
import org.forgerock.openidm.util.Accessor;

import java.util.Map;

public class H2MappedTableHandler extends MappedTableHandler {

    public H2MappedTableHandler(String tableName, Map<String, Object> mapping, String dbSchemaName, JsonValue queriesConfig, JsonValue commandsConfig,
            SQLExceptionHandler sqlExceptionHandler, Accessor<CryptoService> cryptoServiceAccessor) throws InternalServerErrorException {
        super(tableName, mapping, dbSchemaName, queriesConfig, commandsConfig, sqlExceptionHandler, cryptoServiceAccessor);
    }

    @Override
    protected void initializeQueries() {
        final String mainTable = dbSchemaName == null ? tableName : dbSchemaName + "." + tableName;
        final StringBuffer colNames = new StringBuffer();
        final StringBuffer tokenNames = new StringBuffer();
        final StringBuffer prepTokens = new StringBuffer();
        final StringBuffer updateAssign = new StringBuffer();
        boolean isFirst = true;

        for (ColumnMapping colMapping : explicitMapping.getColumnMappings()) {
            if (!isFirst) {
                colNames.append(", ");
                tokenNames.append(",");
                prepTokens.append(",");
                updateAssign.append(", ");
            }
            colNames.append(colMapping.dbColName);
            tokenNames.append("${").append(colMapping.objectColName).append("}");

            if (ColumnMapping.TYPE_JSON_LIST.equals(colMapping.dbColType) ||
                    ColumnMapping.TYPE_JSON_MAP.equals(colMapping.dbColType)) {
                prepTokens.append("? FORMAT JSON");
                updateAssign.append(colMapping.dbColName).append(" = ? FORMAT JSON");
            } else {
                prepTokens.append("?");
                updateAssign.append(colMapping.dbColName).append(" = ?");
            }

            tokenReplacementPropPointers.add(colMapping.objectColPointer);
            isFirst = false;
        }

        readQueryStr = "SELECT * FROM " + mainTable + " WHERE objectid = ?";
        readForUpdateQueryStr = "SELECT * FROM " + mainTable + " WHERE objectid = ? FOR UPDATE";
        createQueryStr =
                "INSERT INTO " + mainTable + " (" + colNames + ") VALUES ( " + prepTokens + ")";
        updateQueryStr = "UPDATE " + mainTable + " SET " + updateAssign + " WHERE objectid = ?";
        deleteQueryStr = "DELETE FROM " + mainTable + " WHERE objectid = ? AND rev = ?";

        logger.debug("Unprepared query strings {} {} {} {} {}",
                readQueryStr, createQueryStr, updateQueryStr, deleteQueryStr);
    }

}
