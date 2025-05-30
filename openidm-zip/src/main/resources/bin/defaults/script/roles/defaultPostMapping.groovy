/**
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 ForgeRock AS.
 * Portions copyright 2025 Wren Security
 */
package bin.defaults.script.roles

import static org.forgerock.json.JsonValue.field
import static org.forgerock.json.JsonValue.object
import static org.forgerock.json.resource.ResourcePath.resourcePath

import org.forgerock.openidm.util.DateUtil
import org.forgerock.json.JsonValue
import org.forgerock.openidm.sync.SyncContext

def syncContext = context.containsContext(SyncContext.class) \
    ? context.asContext(SyncContext.class)
    : null;

def mappingSource = mappingConfig.source as String
def sourceObject = source as JsonValue
def oldSource = oldSource as JsonValue

def mappingName =  mappingConfig.name as String

try {
    if (!mappingSource.equals("managed/user") && syncContext == null) {
        return;
    }

    // Get source object's effective assignments
    JsonValue sourceEffectiveAssignments = sourceObject.get("effectiveAssignments");
    if (sourceEffectiveAssignments.isNull()) {
        return;
    }

    // Filter out effective assignments of the current mapping
    JsonValue appliedEffectiveAssignments = JsonValue.json(sourceEffectiveAssignments.asList()
            .findAll({ it?.mapping == mappingName }))

    // Resolve previously applied effective assignments
    JsonValue previousEffectiveAssignments = oldSource != null \
        ? oldSource.get("lastSync").get(mappingName).get("effectiveAssignments")
        : sourceObject.get("lastSync").get(mappingName).get("effectiveAssignments");

    if (cacheEffectiveAssignments(previousEffectiveAssignments, appliedEffectiveAssignments)) {
        def patch = [["operation" : "replace",
                      "field" : "/lastSync/"+ mappingName,
                      "value" : object(
                                    field("effectiveAssignments", appliedEffectiveAssignments),
                                    field("timestamp", DateUtil.getDateUtil().now()))]];

        syncContext.disableSync()
        JsonValue patched = openidm.patch(
                resourcePath(mappingSource).child(sourceObject.get("_id").asString()).toString(), null, patch);
        source.put("_rev", patched.get("_rev").asString());
    }
} finally {
    if (syncContext != null) {
        syncContext.enableSync();
    }
}

/**
 * Determine if effective assignments are used and check whether we should cache the effective assignments
 * in the lastSync attribute of managed/user if so.
 *
 * @return true to cache; false otherwise
 */
private boolean cacheEffectiveAssignments(JsonValue previousEffectiveAssignments, JsonValue appliedEffectiveAssignments) {
    return appliedEffectiveAssignments.isNotNull() \
            && !previousEffectiveAssignments.isEqualTo(appliedEffectiveAssignments)
}
