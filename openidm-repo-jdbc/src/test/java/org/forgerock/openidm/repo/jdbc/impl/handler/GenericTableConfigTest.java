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
 * Copyright 2014 ForgeRock AS.
 * Portions Copyright 2024 Wren Security
 */
package org.forgerock.openidm.repo.jdbc.impl.handler;

import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;
import static org.testng.Assert.assertFalse;

import org.forgerock.json.JsonPointer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * {@link GenericTableConfig} test case.
 */
public class GenericTableConfigTest {

    @Test
    public void testSearchable() throws Exception {
        var jsonConfig = json(object(
            field("mainTable", "managedobjects"),
            field("propertiesTable", "managedobjectproperties"),
            field("searchableDefault", false),
            field("properties", object(
                field("/userName", object(field("searchable", true))),
                field("/roles", object(field("searchable", true))),
                field("/addresses", object(field("searchable", true)))
            ))
        ));

        var tableConfig = GenericTableConfig.parse(jsonConfig);

        // simple property
        assertFalse(tableConfig.isSearchable(new JsonPointer("/arbitrary")));
        // map/object property
        Assert.assertFalse(tableConfig.isSearchable(new JsonPointer("/arbitrary2/map/x")));
        // list/array property
        Assert.assertFalse(tableConfig.isSearchable(new JsonPointer("/arbitrary3/list/0")));

        // simple property
        Assert.assertTrue(tableConfig.isSearchable(new JsonPointer("/userName")));
        // map/object property
        Assert.assertTrue(tableConfig.isSearchable(new JsonPointer("/addresses/home/street")));
        // list/array property
        Assert.assertTrue(tableConfig.isSearchable(new JsonPointer("/roles/3")));
    }

}
