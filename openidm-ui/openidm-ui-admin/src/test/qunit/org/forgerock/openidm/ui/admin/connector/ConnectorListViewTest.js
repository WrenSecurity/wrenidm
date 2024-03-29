
define([
    "org/forgerock/openidm/ui/admin/connector/ConnectorListView"
], function (ConnectorListView) {
    QUnit.module('Connector List View Tests');

    QUnit.test("prune connector collections", function (assert) {

        var testConnector = {
            "objectTypes": [
                "__GROUP__",
                "groupOfNames",
                "person",
                "organizationalPerson",
                "organization",
                "__ACCOUNT__",
                "account",
                "__SERVER_INFO__",
                "organizationalUnit"
            ],
            "ok": true
        };

        assert.equal(ConnectorListView.pruneObjectTypes(testConnector).objectTypes.length, 9, "connector collection created with good connection");

        testConnector.ok = false;
        assert.equal(ConnectorListView.pruneObjectTypes(testConnector).objectTypes.length, 0, "connector collection created with bad connection");
    });
});
