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
 * Copyright 2015-2016 ForgeRock AS.
 * Portions Copyright 2023 Wren Security.
 */

define([
    "jquery",
    "lodash",
    "org/forgerock/openidm/ui/admin/util/AdminAbstractView",
    "org/forgerock/openidm/ui/common/delegates/ResourceDelegate",
    "org/forgerock/commons/ui/common/util/UIUtils",
    "org/forgerock/commons/ui/common/main/AbstractModel",
    "org/forgerock/commons/ui/common/main/AbstractCollection",
    "org/forgerock/commons/ui/common/main/EventManager",
    "org/forgerock/commons/ui/common/util/Constants",
    "org/forgerock/openidm/ui/admin/util/BackgridUtils",
    "org/forgerock/commons/ui/common/main/Router",
    "backgrid",
    "backgrid-paginator"
], function($, _,
        AdminAbstractView,
        ResourceDelegate,
        uiUtils,
        AbstractModel,
        AbstractCollection,
        eventManager,
        constants,
        BackgridUtils,
        router,
        Backgrid) {
    var ProcessHistoryView = AdminAbstractView.extend({
        template: "templates/admin/workflow/ProcessHistoryViewTemplate.html",
        events: {
            "change #processHistoryFilterType" : "filterType"
        },
        model : {
            userFilter: "anyone",
            processTypeFilter: "all"
        },
        element: "#processHistory",
        render: function(args, callback) {
            this.data.processDefinitions = args[0];

            this.parentRender(_.bind(function() {
                var processGrid,
                    ProcessModel = AbstractModel.extend({ "url": "/openidm/workflow/processinstance/history" }),
                    Process = AbstractCollection.extend({ model: ProcessModel }),
                    paginator;

                this.model.processes = new Process();

                this.model.processes.on('backgrid:sort', function(model) {
                    var cid = model.cid,
                        filtered = model.collection.filter(function (model) {
                            return model.cid !== cid;
                        });

                    _.each(filtered, function (model) {
                        model.set('direction', null);
                    });
                });

                this.model.processes.url = "/openidm/workflow/processinstance/history?_queryId=filtered-query&finished=true";
                this.model.processes.state.pageSize = 50;
                this.model.processes.state.sortKey = "-startTime";
                this.model.processes.state.totalPagedResultsPolicy = "EXACT";

                processGrid = new Backgrid.Grid({
                    className: "table backgrid",
                    emptyText: $.t("templates.workflows.processes.noCompletedProcesses"),
                    columns: BackgridUtils.addSmallScreenCell([
                        {
                            name: "processDefinitionResourceName",
                            label: $.t("templates.workflows.processes.processInstance"),
                            cell: Backgrid.Cell.extend({
                                render: function () {
                                    this.$el.html('<a href="#workflow/processinstance/' +this.model.id +'">' +this.model.get("processDefinitionResourceName") +'<small class="text-muted"> (' +this.model.id +')</small></a>');

                                    this.delegateEvents();
                                    return this;
                                }
                            }),
                            sortable: false,
                            editable: false
                        },
                        {
                            name: "startUserId",
                            label: $.t("templates.workflows.processes.startedBy"),
                            cell: "string",
                            sortable: false,
                            editable: false
                        },
                        {
                            name: "startTime",
                            label: $.t("templates.workflows.processes.created"),
                            cell: BackgridUtils.DateCell("startTime"),
                            sortable: true,
                            editable: false,
                            sortType: "toggle"
                        },
                        {
                            name: "endTime",
                            label: "COMPLETED",
                            cell: BackgridUtils.DateCell("endTime"),
                            sortable: true,
                            editable: false,
                            sortType: "toggle"
                        },
                        {
                            name: "",
                            cell: BackgridUtils.ButtonCell([
                                {
                                    className: "fa fa-eye grid-icon",
                                    callback: function() {
                                        eventManager.sendEvent(constants.EVENT_CHANGE_VIEW, {route: router.configuration.routes.processInstanceView, args: [this.model.id]});
                                    }
                                }
                            ]),
                            sortable: false,
                            editable: false
                        }]),
                    collection: this.model.processes
                });

                paginator = new Backgrid.Extension.Paginator({
                    collection: this.model.processes,
                    windowSize: 0
                });

                this.$el.find("#processHistoryGridHolder").append(processGrid.render().el);
                this.$el.find('#processHistoryGridHolder-paginator').append(paginator.render().el);

                this.model.processes.getFirstPage();

                this.$el.find("#processHistoryAssignedTo").selectize({
                    valueField: 'userName',
                    labelField: 'userName',
                    searchField: ["userName","givenName", "sn"],
                    create: false,
                    preload: true,
                    onChange: _.bind(function(value) {
                        this.model.userFilter = value;

                        this.reloadGrid();
                    },this),
                    render : {
                        item: function(item, escape) {
                            var userName = item.userName.length > 0 ? ' (' + escape(item.userName) + ')': "",
                                displayName = (item.displayName) ? item.displayName : item.givenName + " " + item.sn;


                            return '<div class="item">' +
                                '<span class="user-title">' +
                                '<span class="user-fullname">' + escape(displayName) + userName + '</span>' +
                                '</span>' +
                                '</div>';
                        },
                        option: function(item, escape) {
                            var userName = item.userName.length > 0 ? ' (' + escape(item.userName) + ')': "",
                                displayName = (item.displayName) ? item.displayName : item.givenName + " " + item.sn;


                            return '<div class="option">' +
                                '<span class="user-title">' +
                                '<span class="user-fullname">' + escape(displayName) + userName + '</span>' +
                                '</span>' +
                                '</div>';
                        }
                    },
                    load: _.bind(function(query, callback) {
                        var queryFilter;

                        if (!query.length) {
                            queryFilter = "userName sw \"\" &_pageSize=10";
                        } else {
                            queryFilter = "givenName sw \"" + query +"\" or sn sw \"" + query +"\" or userName sw \"" + query +"\"";
                        }

                        ResourceDelegate.searchResource(queryFilter, "managed/user").then(function(search) {
                            callback(search.result);
                        }, function(){
                            callback();
                        });

                    }, this)
                });

                this.$el.find("#processHistoryAssignedTo")[0].selectize.addOption({
                    _id : "anyone",
                    userName: "anyone",
                    givenName : "Anyone",
                    sn : ""
                });

                this.$el.find("#processHistoryAssignedTo")[0].selectize.setValue("anyone", true);

                if (callback) {
                    callback();
                }
            }, this));
        },

        filterType : function(event) {
            this.model.processTypeFilter = $(event.target).val();

            this.reloadGrid();
        },

        reloadGrid: function() {
            var filterString = "_queryId=filtered-query&finished=true";

            if (this.model.userFilter !== "anyone") {
                filterString = filterString +"&startUserId=" + this.model.userFilter;

                if (this.model.processTypeFilter !== "all") {
                    filterString = filterString + "&processDefinitionKey=" +this.model.processTypeFilter;
                }
            } else if (this.model.processTypeFilter !== "all") {
                filterString = filterString + "&processDefinitionKey=" + this.model.processTypeFilter;
            }

            this.model.processes.url = "/openidm/workflow/processinstance/history?" + filterString;

            this.model.processes.getFirstPage();
        }
    });

    return new ProcessHistoryView();
});