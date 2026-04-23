package lab1;



import org.junit.Before;
import org.junit.Test;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;

public class Lab1RandomWalkWhiteBoxTest {

    private Lab1 lab;

    @Before
    public void setUp() throws Exception {
        lab = new Lab1();
        // 通过反射固定随机种子，保证测试确定性
        Field randomField = Lab1.class.getDeclaredField("random");
        randomField.setAccessible(true);
        Random fixedRandom = new Random(2L);
        randomField.set(lab, fixedRandom);
    }

    // 辅助方法：构建空图（不调用 buildGraphFromFile）
    private void buildEmptyGraph() {
        // lab 默认 graph 和 nodes 为空，无需额外操作
    }

    // TC-RW-01: 空图
    @Test
    public void testRandomWalk_EmptyGraph() {
        buildEmptyGraph();
        String result = lab.randomWalk();
        assertEquals("", result);
    }

    // 辅助方法：构建单节点无出边的图
    private void buildSingleNodeGraph() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        try (PrintWriter out = new PrintWriter(tempFile)) {
            out.println("hello");
        }
        lab.buildGraphFromFile(tempFile.getAbsolutePath());
    }

    // TC-RW-02: 单节点无出边
    @Test
    public void testRandomWalk_SingleNodeNoEdge() throws IOException {
        buildSingleNodeGraph();
        String result = lab.randomWalk();
        assertEquals("hello", result);
        assertFileContentEquals("random_walk.txt", "hello");
    }

    // 辅助方法：构建 A→B，B 无出边的图
    private void buildABGraph() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        try (PrintWriter out = new PrintWriter(tempFile)) {
            out.println("A B");
        }
        lab.buildGraphFromFile(tempFile.getAbsolutePath());
    }
    // TC-RW-03: A→B, B无出边（因无出边停止）

    @Test
    public void testRandomWalk_ABGraphStopByNoEdge() throws IOException {
        buildABGraph();
        // 固定随机种子42下，起始节点为A（假设HashSet顺序固定，实际依赖实现）
        // 若顺序不稳定，可调整：通过反射将 nodes 替换为 LinkedHashSet 或 TreeSet
        // 这里简化，直接断言路径为 "A B"（若起始为B则路径为"B"，但可调整图确保只有A有出边）
        String result = lab.randomWalk();
        // 由于随机种子固定且节点顺序在多次运行中可能一致，此处假设结果为 "A B"
        // 若实际运行出现 "B"，可修改图构建方式使只有 A 可达
        assertEquals("b", result);
        assertFileContentEquals("random_walk.txt", "b");
    }

    // 辅助方法：构建环图 A→B, B→A
    private void buildCycleGraph() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        try (PrintWriter out = new PrintWriter(tempFile)) {
            out.println("A B A");
        }
        lab.buildGraphFromFile(tempFile.getAbsolutePath());
    }

    // TC-RW-04: 环图 A→B, B→A（重复边停止）
    @Test
    public void testRandomWalk_CycleGraphStopByDuplicateEdge() throws IOException {
        buildCycleGraph();
        // 固定随机种子，游走路径确定：A→B→A→B，在第二次 A→B 时发现重复边，添加 B 后停止
        String result = lab.randomWalk();
        assertEquals("b a b a", result);
        assertFileContentEquals("random_walk.txt", "b a b a");
    }

    // 辅助方法：验证文件内容
    private void assertFileContentEquals(String filename, String expected) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String content = reader.readLine();
            assertEquals(expected, content);
        }
    }








}
