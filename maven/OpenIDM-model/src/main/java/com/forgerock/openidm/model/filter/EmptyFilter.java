
package com.forgerock.openidm.model.filter;

import com.forgerock.openidm.api.logging.Trace;
import com.forgerock.openidm.logging.TraceManager;
import com.forgerock.openidm.util.DebugUtil;
import org.w3c.dom.Node;

/**
 * Empty filter. It does not tranformate the value at all.
 * It only logs it. Can be used in tests and for diagnostics.
 *
 * @author Igor Farinic
 * @author Radovan Semancik
 * @version $Revision$ $Date$
 * @since 0.1
 */
public class EmptyFilter extends AbstractFilter {

    private static transient Trace logger = TraceManager.getTrace(EmptyFilter.class);

    @Override
    public Node apply(Node node) {
       logger.debug("EmptyFilter see {}",DebugUtil.prettyPrint(node));
       return node;
    }

}
