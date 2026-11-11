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
 * Copyright 2014-2016 ForgeRock AS.
 * Portions Copyright 2023-2026 Wren Security.
 */

define([
    "jquery",
    "lodash",
    "org/forgerock/openidm/ui/common/util/Constants",
    "org/forgerock/commons/ui/common/components/Messages"
], ($, _, constants, messagesManager) => {
    const POLICY_FAILURE_MESSAGES = ["Failed policy validation", "Policy validation failed"];

    // Translated without interpolation escaping, the whole text is escaped once instead.
    const translateRequirement = ({ policyRequirement, params }) => _.escape(
        $.t(`common.form.validation.${policyRequirement}`, _.assign({}, params, { interpolation: { escapeValue: false } }))
    );

    const formatValidationErrors = (failedPolicyRequirements) =>
        Object.entries(_.groupBy(failedPolicyRequirements, "property"))
            .map(([property, failures]) => {
                const requirements = failures.flatMap(({ policyRequirements }) =>
                    _.map(policyRequirements, translateRequirement));
                return ` - ${_.escape(property)}: ${requirements.join(", ")}`;
            })
            .join(" <br/> ");

    return [
        {
            startEvent: constants.EVENT_POLICY_FAILURE,
            description: "Failure to save record due to policy validation",
            dependencies: [],
            processDescription(event) {
                const response = event.error.responseObj;
                const errors = _.isObject(_.get(response, "detail")) && POLICY_FAILURE_MESSAGES.includes(response.message)
                    ? formatValidationErrors(response.detail.failedPolicyRequirements)
                    : "Unknown";

                // The message contains HTML line breaks; the dynamic parts are escaped above.
                messagesManager.messages.addMessage({
                    type: "error",
                    message: $.t("config.messages.AdminMessages.resourceValidationError", {
                        validationErrors: errors,
                        interpolation: { escapeValue: false }
                    }),
                    escape: false
                });
            }
        }
    ];
});
