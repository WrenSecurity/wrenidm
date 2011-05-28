/*
 *
 * Copyright (c) 2010 ForgeRock Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1.php or
 * OpenIDM/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at OpenIDM/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted 2010 [name of copyright owner]"
 *
 * $Id$
 */
package com.forgerock.openidm.xml.common;

/**
 * Sample Class Doc
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class Pair<V1, V2> {

    public static final String code_id = "$Id$";

	private V1 first;
	private V2 second;
	
	public Pair() {
		
	}
	
	public Pair(V1 first, V2 second) {
		this.first = first;
		this.second = second;
	}
	
	public V1 getFirst() {
		return first;
	}
	
	protected void setFirst(V1 first) {
		this.first = first;
	}
	
	public V2 getSecond() {
		return second;
	}
	
	protected void setSecond(V2 second) {
		this.second = second;
	}

}
