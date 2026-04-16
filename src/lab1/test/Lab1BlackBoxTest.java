package lab1;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Lab1BlackBoxTest {

    private Lab1 lab;

    @Before
    public void setUp() throws Exception {
        lab = new Lab1();
        // 确保 Easy Test.txt 文件在项目根目录或提供正确路径
        lab.buildGraphFromFile("src/lab1/Easy Test.txt");
    }

    // 测试用例 1: 存在单个桥接词
    @Test
    public void testBridgeWords_ExistSingle() {
        String result = lab.queryBridgeWords("scientist", "analyzed");
        assertEquals("The bridge words from \"scientist\" to \"analyzed\" is: \"carefully\"", result);
    }

    // 测试用例 2: 两个单词都在图中，但无桥接词
    @Test
    public void testBridgeWords_NoBridge() {
        String result = lab.queryBridgeWords("the", "team");
        assertEquals("No bridge words from \"the\" to \"team\"!", result);
    }

    // 测试用例 3: word1 不在图中，word2 在图中
    @Test
    public void testBridgeWords_Word1NotExist() {
        String result = lab.queryBridgeWords("hello", "the");
        assertEquals("No \"hello\" in the graph!", result);
    }

    // 测试用例 4: word1 在图中，word2 不在图中
    @Test
    public void testBridgeWords_Word2NotExist() {
        String result = lab.queryBridgeWords("but", "city");
        assertEquals("No \"city\" in the graph!", result);
    }

    // 测试用例 5: word1 和 word2 都不在图中
    @Test
    public void testBridgeWords_BothNotExist() {
        String result = lab.queryBridgeWords("hello", "city");
        assertEquals("No \"hello\" and \"city\" in the graph!", result);
    }
}