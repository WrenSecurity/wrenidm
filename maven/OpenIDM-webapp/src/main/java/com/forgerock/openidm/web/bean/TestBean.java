package com.forgerock.openidm.web.bean;

import com.forgerock.openidm.provisioning.schema.util.SchemaParserException;
import com.forgerock.openidm.util.Utils;
import com.forgerock.openidm.web.component.formgenerator.SchemaFormBean;
import com.forgerock.openidm.web.dto.GuiAccountShadowDto;
import com.forgerock.openidm.web.model.AccountShadowDto;
import com.forgerock.openidm.web.model.AccountShadowManager;
import com.forgerock.openidm.web.model.ObjectManager;
import com.forgerock.openidm.web.model.ObjectTypeCatalog;
import com.forgerock.openidm.xml.ns._public.common.common_1.*;
import com.forgerock.openidm.xml.schema.SchemaConstants;
import com.forgerock.openidm.xml.schema.XPathType;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Element;

/**
 *
 * @author Vilo Repan
 */
@Controller
@Scope("session")
public class TestBean implements Serializable {

    private static transient Logger logger = LoggerFactory.getLogger(TestBean.class);
    @Autowired(required = true)
    private transient ObjectTypeCatalog objectTypeCatalog;
    private String accountOid = "d943f611-22a0-4d3d-8617-ee6ccc2dc041";
    private List<FormBean> beanList = new ArrayList<FormBean>();

    public ObjectTypeCatalog getObjectTypeCatalog() {
        return objectTypeCatalog;
    }

    public void setObjectTypeCatalog(ObjectTypeCatalog objectTypeCatalog) {
        this.objectTypeCatalog = objectTypeCatalog;
    }

    public String getAccountOid() {
        return accountOid;
    }

    public void setAccountOid(String accountOid) {
        this.accountOid = accountOid;
    }

    public List<FormBean> getBeanList() {
        return beanList;
    }

    public void reloadForm(ActionEvent evt) {
        listResources();
    }

    public String listResources() {
        try {
            ObjectManager<AccountShadowDto> objectManager = objectTypeCatalog.getObjectManager(AccountShadowDto.class, GuiAccountShadowDto.class);
            AccountShadowManager userManager = (AccountShadowManager) objectManager;

            ObjectFactory of = new ObjectFactory();
            PropertyReferenceListType listProperty = of.createPropertyReferenceListType();

            PropertyReferenceType property = of.createPropertyReferenceType();
            property.setProperty((new XPathType(Utils.getPropertyName("Resource")).toElement(SchemaConstants.NS_C,"property")));
            listProperty.getProperty().add(property);

            AccountShadowDto account = userManager.get(accountOid, listProperty);

            QName accountType = new QName("http://mi6.gov.uk/schema/", "Mi6PersonObjectClass", "mi6"); //only mock, it will be obtained from schema handling
            SchemaFormBean bean = new SchemaFormBean();
            bean.generateForm(account, accountType);
            beanList.add(new FormBean(bean));

            QName qname = new QName(account.getResource().getNamespace(), "AccountObjectClass");
            bean = new SchemaFormBean();
            bean.generateForm(account, qname);
            beanList.add(new FormBean(bean));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "/index3";
    }

    public String submitForm() {
        try {
            for (FormBean formBean : beanList) {
                System.out.println("Bean: ");
                printAttributes(formBean.getBean());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void printAttributes(SchemaFormBean bean) throws SchemaParserException {
        AccountShadowDto account = bean.getAccount();
        List<Element> list = account.getAttributes();
        logger.trace("Updated attributes:");
        for (Element node : list) {
            logger.trace(node.getNamespaceURI() + node.getLocalName() + "\t" + node.getFirstChild().getNodeValue());
        }
    }

    public static class FormBean {

        private SchemaFormBean bean;
        private boolean rendered = true;

        public FormBean(SchemaFormBean bean) {
            this.bean = bean;
        }

        public SchemaFormBean getBean() {
            return bean;
        }

        public void setBean(SchemaFormBean bean) {
            this.bean = bean;
        }

        public boolean isRendered() {
            return rendered;
        }

        public void setRendered(boolean rendered) {
            this.rendered = rendered;
        }
    }
}
