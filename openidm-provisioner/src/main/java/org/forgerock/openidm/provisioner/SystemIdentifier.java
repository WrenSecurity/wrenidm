/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011-15 ForgeRock AS. All rights reserved.
 * Portions Copyright 2018 Wren Security.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.forgerock.openidm.provisioner;

/**
 * SystemIdentifier is a composite key to identify the {@link ProvisionerService} instance.
 *
 * @version $Revision$ $Date$
 */
public interface SystemIdentifier {
    /**
     * Compare this and the {@code other} instance and returns true if both identifies the same
     * {@link ProvisionerService} instance.
     *
     * @param   other
     *          The instance against which this instance is compared.
     *
     * @return  {@code true} if this object identifies the same instance as {@code other}; or,
     *          {@code false} if it does not.
     */
    boolean is(SystemIdentifier other);

    /**
     * Checks the {@code uri} and return true if the {@link ProvisionerService} instance is
     * responsible for handling the request.
     *
     * @param   uri
     *          The URI to check.
     *
     * @return  If this instance handles the given type of request.
     */
    boolean is(Id uri);
}
