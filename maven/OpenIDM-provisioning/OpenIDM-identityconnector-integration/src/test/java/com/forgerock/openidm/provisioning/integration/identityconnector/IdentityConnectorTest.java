
import org.junit.Ignore;
import com.forgerock.openidm.provisioning.conversion.JAXBConverter;
import com.forgerock.openidm.xml.ns._public.resource.idconnector.configuration_1.ConnectorRef;
import com.forgerock.openidm.provisioning.objects.ResourceAttribute;
import com.forgerock.openidm.provisioning.objects.ResourceObject;
import com.forgerock.openidm.provisioning.schema.AttributeFlag;
import com.forgerock.openidm.provisioning.schema.ResourceAttributeDefinition;
import com.forgerock.openidm.provisioning.schema.ResourceObjectDefinition;
import com.forgerock.openidm.provisioning.schema.ResourceSchema;
import com.forgerock.openidm.provisioning.schema.util.ObjectValueWriter;
import com.forgerock.openidm.test.repository.BaseXDatabaseFactory;
import com.forgerock.openidm.test.util.SampleObjects;
import com.forgerock.openidm.xml.ns._public.common.common_1.PropertyReferenceListType;
import com.forgerock.openidm.xml.ns._public.common.common_1.ResourceType;
import com.forgerock.openidm.xml.ns._public.repository.repository_1.RepositoryPortType;
import java.util.List;
import javax.xml.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.w3c.dom.Element;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author elek
 */
public class IdentityConnectorTest {

    private RepositoryPortType repositoryPort;

    @Before
    public void initXmlDatabasep() throws Exception {
        //Create Repository mock
        repositoryPort = BaseXDatabaseFactory.getRepositoryPort();

        //Make suere the Repository is mocked
        assertNotNull(repositoryPort);
    }

    @After
    public void resetXmlDatabase() {
        BaseXDatabaseFactory.XMLServerStop();
    }

    public IdentityConnectorTest() {
    }

    @Test
    @Ignore("It required non-canonical example data. Should be refactored after the new schema born")
    public void buildConfigurationObject() throws Exception {
        //GIVEN
        ObjectValueWriter ovw = new ObjectValueWriter();


        //schema creation
        String ICCI = "http://openidm.forgerock.com/xml/ns/public/resource/idconnector/configuration-1.xsd";
                
        ResourceSchema schema = new ResourceSchema(ICCI);
        schema.addConverter(new JAXBConverter(ICCI, ConnectorRef.class));

        ResourceObjectDefinition def = new ResourceObjectDefinition(new QName(ICCI, "configuration"));

        ResourceAttributeDefinition connectorRef = new ResourceAttributeDefinition(new QName(ICCI, "ConnectorRef"));
        connectorRef.setType(new QName(ICCI, "ConnectorRef"));
        connectorRef.getAttributeFlag().add(AttributeFlag.NOT_UPDATEABLE);

        def.addAttribute(connectorRef);

        schema.addObjectClass(def);
        
        assertNotNull(schema.getConverterFactory().getConverter(new QName(ICCI, "ConnectorRef")));

        //load values
        ResourceType rt = (ResourceType) repositoryPort.getObject(SampleObjects.RESOURCETYPE_LOCALHOST_OPENDJ.getOID(), new PropertyReferenceListType()).getObject();
        List<Element> values = rt.getConfiguration().getAny();

        //when
        ResourceObject ro = ovw.readValues(def, values);

        //then
        ResourceAttribute a = ro.getValue(new QName(ICCI, "ConnectorRef"));
        assertNotNull(a);
        ConnectorRef ref = a.getSingleJavaValue(ConnectorRef.class);
        assertEquals("1.0.5531", ref.getBundleVersion());

    }
}
