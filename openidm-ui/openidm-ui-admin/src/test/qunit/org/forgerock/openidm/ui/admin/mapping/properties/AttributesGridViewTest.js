define([
    "jquery",
    "lodash",
    "sinon",
    "org/forgerock/openidm/ui/admin/mapping/properties/AttributesGridView",
    "org/forgerock/commons/ui/common/main/Configuration"
], function ($, _, sinon, AttributesGridView, Configuration) {
    QUnit.module('AttributesGridView Tests');
    QUnit.test("gridFromMapProps", function (assert) {
        var evalResult;
        // a simple sample source user to use in the evaluation
        Configuration.globalData = _.extend({
            "sampleSource": {
                "IDMSampleMappingName": "testMapping",
                "userName": "bjensen",
                "givenName": "Barbara",
                "phone": null
            }
        }, Configuration.globalData);
        // just return back the
        sinon.stub(AttributesGridView, "sampleEvalCheck").callsFake(function (sampleDetails, globals) {
            return $.Deferred().resolve({sampleDetails, globals});
        });
        // don't do anything with this one; just stub it out.
        sinon.stub(AttributesGridView, "loadGrid");

        AttributesGridView.model.mapping = {name : "testMapping"};

        // try loading the grid with a few different transform rules
        evalResult = AttributesGridView.gridFromMapProps([
            {
                "source": "",
                "target": "uid",
                "transform": {
                    "type": "text/javascript",
                    "source": "source.userName.toUpperCase() + source.givenName"
                }
            },
            {
                "source": "givenName",
                "target": "firstName"
            },
            {
                "source": "sn",
                "target": "lastName",
                "condition": "/sn pr"
            },
            {
                "source": "phone",
                "target": "phone",
                "transform": {
                    "type": "text/javascript",
                    "source": "source ? source.replace('-','') : null"
                }
            }
        ]);

        assert.equal(evalResult.length, 4, "A promise produced for each mapping rule provided");

        $.when.apply($, evalResult).then(function () { // happens synchronously since they are all resolved at this point
            var responses = arguments;
            assert.equal(responses[0].sampleDetails.hasTransform, true, "correctly detected presence of transform");
            assert.deepEqual(responses[0].globals.source, Configuration.globalData.sampleSource,
                "correctly used full source object when 'source' entry is empty string");
            assert.equal(responses[1].globals.source, Configuration.globalData.sampleSource.givenName,
                "correctly used specific source property when 'source' entry is a parameter name");
            assert.equal(responses[2].sampleDetails.hasCondition, true,
                "correctly detected presence of condition");
            assert.equal(responses[3].globals.source, null,
                "populated 'source' binding properly, regardless of null value (OPENIDM-6051)");

            delete Configuration.globalData.sampleSource;
        });

        AttributesGridView.sampleEvalCheck.restore();
        AttributesGridView.loadGrid.restore();
    });
});
