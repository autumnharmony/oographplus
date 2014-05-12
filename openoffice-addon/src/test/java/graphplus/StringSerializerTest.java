package java.graphplus;

import com.sun.star.lang.XComponent;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.ssau.graphplus.DiagramModel;
import ru.ssau.graphplus.StringSerializer;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.node.ClientNode;
import ru.ssau.graphplus.api.Node;

import java.util.Collection;

import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 2/17/14
 * Time: 12:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class StringSerializerTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testFromString() throws Exception {

    }

    @Test
    public void testToString() throws Exception {

        DiagramModel diagramModel = new DiagramModel(mock(XComponent.class, withSettings().serializable()));

        ClientNode clientNode = new ClientNode();
        clientNode.setId("node0");
        clientNode.setName("NODE NAME");
        clientNode.setPosition(10,10);

        Node node = clientNode;


        diagramModel.addDiagramElement(node);
        diagramModel.setDiagramType(DiagramType.Channel);


        String s = StringSerializer.toString(diagramModel);

        DiagramModel deserializedModel = (DiagramModel) StringSerializer.fromString(s);

        Assert.assertTrue(!deserializedModel.getDiagramElements().isEmpty());
        Assert.assertTrue(deserializedModel.getDiagramType().equals(DiagramType.Channel));
        Collection<DiagramElement> diagramElements = deserializedModel.getDiagramElements();

        Node nodeDeserizalized = (Node) diagramElements.iterator().next();
        Assert.assertEquals(nodeDeserizalized.getId(), "node0");
        Assert.assertEquals(nodeDeserizalized.getName(), "NODE NAME");
        Assert.assertEquals(nodeDeserizalized.getType(), Node.NodeType.ClientPort);
    }
}
