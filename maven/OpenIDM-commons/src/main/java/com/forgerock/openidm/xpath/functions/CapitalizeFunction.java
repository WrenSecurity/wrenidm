package com.forgerock.openidm.xpath.functions;

import java.util.List;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 *
 * @author Igor Farinic
 * @version $Revision$ $Date$
 * @since 0.1
 */
public class CapitalizeFunction implements XPathFunction {

    @Override
    public Object evaluate(List args) throws XPathFunctionException {
        if (null == args || args.size() != 1) {
            throw new XPathFunctionException("Wrong number of arguments");
        }

        Object argument = args.get(0);

        if (argument instanceof String) {
            return StringUtils.capitalize((String) argument);
        } else if (argument instanceof Node) {
            //NodeList nodes = (NodeList) argument;
            Node node = (Node)argument;
            return StringUtils.capitalize(node.getTextContent());
        } else {
            throw new XPathFunctionException("Wrong argument type, was " + argument.getClass().getName());
        }


    }
}
