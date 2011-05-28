package com.forgerock.openidm.xpath;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;
import org.apache.commons.lang.Validate;

/**
 * 
 *
 * @author Igor Farinic
 * @version $Revision$ $Date$
 * @since 0.1
 */
public class OpenIdmXPathFunctionResolver implements XPathFunctionResolver {

    Map<QName, XPathFunction>  map = new HashMap();

    @Override
    public XPathFunction resolveFunction(QName fname, int arity) {
        Validate.notNull("The function name cannot be null");
        Validate.isTrue(arity >= 0, "Provided negative value for function arity");

        return map.get(fname);
    }

    public void registerFunction(QName fname, XPathFunction function) {
        map.put(fname, function);
    }

}
