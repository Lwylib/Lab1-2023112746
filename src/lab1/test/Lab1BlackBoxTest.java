package lab1;



import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Lab1BlackBoxTest {

    private Lab1 lab;

    @Before
    public void setUp() throws Exception {
        lab = new Lab1();
        lab.buildGraphFromFile("Easy Test.txt");  // 确保文件在 classpath 或使用绝对路径
    }

    @Test
    public void testBridge_ScientistToAnalyzed() {
        String result = lab.queryBridgeWords("scientist", "analyzed");
        assertEquals("The bridge words from \"scientist\" to \"analyzed\" is: \"carefully\"", result);
    }

    @Test
    public void testBridge_AnalyzedToData() {
        String result = lab.queryBridgeWords("analyzed", "data");
        assertEquals("The bridge words from \"analyzed\" to \"data\" is: \"the\"", result);
    }

    @Test
    public void testBridge_RequestedToData() {
        String result = lab.queryBridgeWords("requested", "data");
        assertEquals("The bridge words from \"requested\" to \"data\" is: \"more\"", result);
    }

    @Test
    public void testBridge_SharedToReport() {
        String result = lab.queryBridgeWords("shared", "report");
        assertEquals("The bridge words from \"shared\" to \"report\" is: \"the\"", result);
    }

    @Test
    public void testBridge_NoBridge_TheToTeam() {
        String result = lab.queryBridgeWords("the", "team");
        assertEquals("No bridge words from \"the\" to \"team\"!", result);
    }

    @Test
    public void testBridge_NoBridge_ScientistToIt() {
        String result = lab.queryBridgeWords("scientist", "it");
        assertEquals("No bridge words from \"scientist\" to \"it\"!", result);
    }

    @Test
    public void testBridge_Word1NotExist() {
        String result = lab.queryBridgeWords("hello", "scientist");
        assertEquals("No \"hello\" in the graph!", result);
    }

    @Test
    public void testBridge_Word2NotExist() {
        String result = lab.queryBridgeWords("scientist", "world");
        assertEquals("No \"world\" in the graph!", result);
    }

    @Test
    public void testBridge_BothNotExist() {
        String result = lab.queryBridgeWords("hello", "world");
        assertEquals("No \"hello\" and \"world\" in the graph!", result);
    }
}
