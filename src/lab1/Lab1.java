package lab1;

import java.io.*;
import java.util.*;
import java.awt.Desktop;

/**
 * Lab1: 基于大模型的编程与Git实战
 * 功能：读入文本文件生成有向图，支持多种图操作
 */
public class Lab1 {
    // 图结构：邻接表，Key为单词（小写），Value为邻接单词及其权重
    private Map<String, Map<String, Integer>> graph;
    // 所有节点集合
    private Set<String> nodes;
    private Random random = new Random();

    public Lab1() {
        graph = new HashMap<>();
        nodes = new HashSet<>();
    }

    /**
     * 从文本文件构建有向图
     * @param filename 文件路径
     * @throws IOException 文件读取异常
     */
    public void buildGraphFromFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append(" ");
            }
        }
        String text = content.toString();
        // 将非字母字符替换为空格
        String processed = text.replaceAll("[^a-zA-Z]", " ");
        String[] words = processed.split("\\s+");
        List<String> wordList = new ArrayList<>();
        for (String w : words) {
            if (!w.isEmpty()) {
                wordList.add(w.toLowerCase());
            }
        }
        // 构建边
        for (int i = 0; i < wordList.size() - 1; i++) {
            String from = wordList.get(i);
            String to = wordList.get(i + 1);
            nodes.add(from);
            nodes.add(to);
            graph.putIfAbsent(from, new HashMap<>());
            graph.get(from).put(to, graph.get(from).getOrDefault(to, 0) + 1);
        }
        // 确保每个节点都有记录（即使无出边）
        for (String node : nodes) {
            graph.putIfAbsent(node, new HashMap<>());
        }
    }

    /**
     * 功能需求2：展示有向图（优先使用Graphviz生成图片，失败则回退到文本）
     */
    public void showDirectedGraph() {
        String dotExe = "D:\\useful_software\\Graphviz\\bin\\dot.exe";
        File dotFile = new File("graph.dot");
        File pngFile = new File("graph.png");

        // 生成DOT文件
        try (PrintWriter out = new PrintWriter(dotFile)) {
            out.println("digraph G {");
            for (String from : graph.keySet()) {
                Map<String, Integer> edges = graph.get(from);
                for (Map.Entry<String, Integer> entry : edges.entrySet()) {
                    String to = entry.getKey();
                    int weight = entry.getValue();
                    out.println("  \"" + from + "\" -> \"" + to + "\" [label=\"" + weight + "\"];");
                }
            }
            out.println("}");
        } catch (IOException e) {
            System.err.println("无法写入dot文件: " + e.getMessage());
            fallbackTextDisplay();
            return;
        }

        // 检查dot.exe是否存在
        if (!new File(dotExe).exists()) {
            System.err.println("未找到Graphviz的dot.exe，使用文本方式展示。");
            fallbackTextDisplay();
            dotFile.delete();
            return;
        }

        try {
            // 执行命令 dot -Tpng graph.dot -o graph.png
            ProcessBuilder pb = new ProcessBuilder(dotExe, "-Tpng", dotFile.getAbsolutePath(), "-o", pngFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("dot.exe执行失败，错误码: " + exitCode);
                fallbackTextDisplay();
                return;
            }
            // 打开图片
            if (pngFile.exists()) {
                Desktop.getDesktop().open(pngFile);
                System.out.println("已生成有向图并打开: " + pngFile.getAbsolutePath());
            } else {
                System.err.println("生成图片失败，未找到输出文件。");
                fallbackTextDisplay();
            }
        } catch (Exception e) {
            System.err.println("调用dot.exe时出错: " + e.getMessage());
            fallbackTextDisplay();
        } finally {
            // 删除临时dot文件
            dotFile.delete();
        }
    }

    /**
     * 文本形式的图展示（后备方案）
     */
    private void fallbackTextDisplay() {
        System.out.println("有向图结构（文本形式）：");
        for (String from : graph.keySet()) {
            Map<String, Integer> edges = graph.get(from);
            if (edges.isEmpty()) {
                System.out.println(from + " -> (无出边)");
            } else {
                for (Map.Entry<String, Integer> entry : edges.entrySet()) {
                    System.out.println(from + " -> " + entry.getKey() + " 权重: " + entry.getValue());
                }
            }
        }
    }

    /**
     * 功能需求3：查询桥接词
     * @param word1 第一个单词
     * @param word2 第二个单词
     * @return 描述字符串
     */
    public String queryBridgeWords(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        if (!nodes.contains(word1) || !nodes.contains(word2)) {
            if (!nodes.contains(word1) && !nodes.contains(word2))
                return "No \"" + word1 + "\" and \"" + word2 + "\" in the graph!";
            else if (!nodes.contains(word1))
                return "No \"" + word1 + "\" in the graph!";
            else
                return "No \"" + word2 + "\" in the graph!";
        }
        List<String> bridges = new ArrayList<>();
        Map<String, Integer> fromEdges = graph.get(word1);
        if (fromEdges != null) {
            for (String candidate : fromEdges.keySet()) {
                if (graph.containsKey(candidate) && graph.get(candidate).containsKey(word2)) {
                    bridges.add(candidate);
                }
            }
        }
        if (bridges.isEmpty()) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("The bridge words from \"").append(word1).append("\" to \"").append(word2).append("\" ");
            if (bridges.size() == 1) {
                sb.append("is: \"").append(bridges.get(0)).append("\"");
            } else {
                sb.append("are: ");
                for (int i = 0; i < bridges.size(); i++) {
                    if (i > 0) {
                        if (i == bridges.size() - 1) {
                            sb.append(" and ");
                        } else {
                            sb.append(", ");
                        }
                    }
                    sb.append("\"").append(bridges.get(i)).append("\"");
                }
            }
            return sb.toString();
        }
    }

    /**
     * 辅助方法：从输入文本中提取原始单词（保留大小写）
     * @param text 输入文本
     * @return 原始单词列表
     */
    private List<String> parseOriginalWords(String text) {
        List<String> original = new ArrayList<>();
        StringBuilder word = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                word.append(c);
            } else {
                if (word.length() > 0) {
                    original.add(word.toString());
                    word.setLength(0);
                }
            }
        }
        if (word.length() > 0) {
            original.add(word.toString());
        }
        return original;
    }

    /**
     * 功能需求4：根据桥接词生成新文本
     * @param inputText 用户输入的一行文本
     * @return 插入桥接词后的新文本
     */
    public String generateNewText(String inputText) {
        List<String> originalWords = parseOriginalWords(inputText);
        if (originalWords.size() < 2) {
            return inputText;
        }
        List<String> result = new ArrayList<>();
        result.add(originalWords.get(0));
        for (int i = 0; i < originalWords.size() - 1; i++) {
            String word1 = originalWords.get(i).toLowerCase();
            String word2 = originalWords.get(i + 1).toLowerCase();
            // 查找桥接词
            List<String> bridges = new ArrayList<>();
            if (nodes.contains(word1) && nodes.contains(word2)) {
                Map<String, Integer> fromEdges = graph.get(word1);
                if (fromEdges != null) {
                    for (String candidate : fromEdges.keySet()) {
                        if (graph.containsKey(candidate) && graph.get(candidate).containsKey(word2)) {
                            bridges.add(candidate);
                        }
                    }
                }
            }
            if (!bridges.isEmpty()) {
                String bridge = bridges.get(random.nextInt(bridges.size()));
                result.add(bridge); // 桥接词用小写
            }
            result.add(originalWords.get(i + 1)); // 原单词保留原始大小写
        }
        return String.join(" ", result);
    }

    /**
     * 功能需求5：计算两个单词之间的最短路径（边权和最小）
     * @param word1 起始单词
     * @param word2 目标单词
     * @return 描述路径和长度的字符串
     */
    public String calcShortestPath(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        if (!nodes.contains(word1) || !nodes.contains(word2)) {
            return "No such words in graph!";
        }
        // Dijkstra算法
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<Map.Entry<String, Double>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());
        for (String node : nodes) {
            dist.put(node, Double.MAX_VALUE);
        }
        dist.put(word1, 0.0);
        pq.add(new AbstractMap.SimpleEntry<>(word1, 0.0));
        while (!pq.isEmpty()) {
            Map.Entry<String, Double> curr = pq.poll();
            String u = curr.getKey();
            double d = curr.getValue();
            if (d > dist.get(u)) continue;
            if (u.equals(word2)) break;
            Map<String, Integer> edges = graph.get(u);
            if (edges != null) {
                for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                    String v = edge.getKey();
                    double w = edge.getValue();
                    double newDist = d + w;
                    if (newDist < dist.get(v)) {
                        dist.put(v, newDist);
                        prev.put(v, u);
                        pq.add(new AbstractMap.SimpleEntry<>(v, newDist));
                    }
                }
            }
        }
        if (!prev.containsKey(word2) && !word1.equals(word2)) {
            return "No path from \"" + word1 + "\" to \"" + word2 + "\"!";
        }
        // 重建路径
        LinkedList<String> path = new LinkedList<>();
        String step = word2;
        while (step != null) {
            path.addFirst(step);
            step = prev.get(step);
        }
        if (path.getFirst().equals(word1)) {
            return "Shortest path from \"" + word1 + "\" to \"" + word2 + "\": " +
                    String.join(" -> ", path) + ", length: " + dist.get(word2);
        } else {
            return "No path from \"" + word1 + "\" to \"" + word2 + "\"!";
        }
    }

    /**
     * 新增辅助方法：计算单个单词到图中所有其他节点的最短路径
     * @param word 源单词
     * @return 多行描述字符串
     */
    public String calcShortestPathsFrom(String word) {
        word = word.toLowerCase();
        if (!nodes.contains(word)) {
            return "单词 \"" + word + "\" 不在图中！";
        }
        // Dijkstra算法计算到所有节点的最短距离
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<Map.Entry<String, Double>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());
        for (String node : nodes) {
            dist.put(node, Double.MAX_VALUE);
        }
        dist.put(word, 0.0);
        pq.add(new AbstractMap.SimpleEntry<>(word, 0.0));
        while (!pq.isEmpty()) {
            Map.Entry<String, Double> curr = pq.poll();
            String u = curr.getKey();
            double d = curr.getValue();
            if (d > dist.get(u)) continue;
            Map<String, Integer> edges = graph.get(u);
            if (edges != null) {
                for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                    String v = edge.getKey();
                    double w = edge.getValue();
                    double newDist = d + w;
                    if (newDist < dist.get(v)) {
                        dist.put(v, newDist);
                        prev.put(v, u);
                        pq.add(new AbstractMap.SimpleEntry<>(v, newDist));
                    }
                }
            }
        }
        // 收集所有可达节点
        StringBuilder result = new StringBuilder();
        result.append("从 \"").append(word).append("\" 到其他节点的最短路径：\n");
        boolean hasAny = false;
        for (String target : nodes) {
            if (target.equals(word)) continue;
            if (dist.get(target) < Double.MAX_VALUE) {
                hasAny = true;
                // 重建路径
                LinkedList<String> path = new LinkedList<>();
                String step = target;
                while (step != null) {
                    path.addFirst(step);
                    step = prev.get(step);
                }
                result.append(String.join(" -> ", path))
                        .append(" : 长度 ").append(dist.get(target))
                        .append("\n");
            }
        }
        if (!hasAny) {
            result.append("没有从 \"").append(word).append("\" 出发可达的其他节点。");
        }
        return result.toString();
    }


    /**
     * 功能需求6：计算单词的PageRank值（d=0.85）
     * @param word 目标单词
     * @return PageRank值，若单词不存在则返回null
     */
    public Double calPageRank(String word) {
        word = word.toLowerCase();
        if (!nodes.contains(word)) {
            return null;
        }
        int N = nodes.size();
        double d = 0.85;
        Map<String, Double> pr = new HashMap<>();
        // 初始化
        for (String node : nodes) {
            pr.put(node, 1.0 / N);
        }
        double epsilon = 1e-8;
        double maxDiff;
        int iter = 0;
        do {
            Map<String, Double> newPR = new HashMap<>();
            double base = (1 - d) / N;
            for (String node : nodes) {
                newPR.put(node, base);
            }
            // 每个节点贡献PR值
            for (String u : nodes) {
                double pr_u = pr.get(u);
                Map<String, Integer> edges = graph.get(u);
                int outDegree = edges.size();
                if (outDegree > 0) {
                    double contribution = pr_u / outDegree;
                    for (String v : edges.keySet()) {
                        newPR.put(v, newPR.get(v) + d * contribution);
                    }
                } else {
                    // 出度为0，均匀分配给所有节点
                    double contribution = pr_u / N;
                    for (String v : nodes) {
                        newPR.put(v, newPR.get(v) + d * contribution);
                    }
                }
            }
            // 计算变化量
            maxDiff = 0;
            for (String node : nodes) {
                double diff = Math.abs(newPR.get(node) - pr.get(node));
                if (diff > maxDiff) maxDiff = diff;
            }
            pr = newPR;
            iter++;
        } while (maxDiff > epsilon && iter < 1000);
        return pr.get(word);
    }

    /**
     * 功能需求7：随机游走
     * 从随机节点开始，沿出边随机移动，直到遇到重复边或无出边，将路径保存到文件并返回
     * @return 路径字符串
     */
    public String randomWalk() {
        if (nodes.isEmpty()) return "";
        List<String> nodeList = new ArrayList<>(nodes);
        String current = nodeList.get(random.nextInt(nodeList.size()));
        List<String> pathNodes = new ArrayList<>();
        pathNodes.add(current);
        Set<String> visitedEdges = new HashSet<>(); // 记录走过的边 "from->to"
        while (true) {
            Map<String, Integer> edges = graph.get(current);
            if (edges == null || edges.isEmpty()) {
                break;
            }
            List<String> neighbors = new ArrayList<>(edges.keySet());
            String next = neighbors.get(random.nextInt(neighbors.size()));
            String edge = current + "->" + next;
            if (visitedEdges.contains(edge)) {
                pathNodes.add(next);
                break; // 重复边，停止
            }
            visitedEdges.add(edge);
            pathNodes.add(next);
            current = next;
        }
        String result = String.join(" ", pathNodes);
        // 写入文件
        try (PrintWriter writer = new PrintWriter("random_walk.txt")) {
            writer.println(result);
        } catch (FileNotFoundException e) {
            System.err.println("无法写入文件 random_walk.txt");
        }
        return result;
    }

    /**
     * 主程序入口
     * @param args 可指定文件路径，若不指定则提示用户输入
     */
    public static void main(String[] args) {
        Lab1 lab = new Lab1();
        Scanner scanner = new Scanner(System.in);
        String filename;
        if (args.length > 0) {
            filename = args[0];
        } else {
            System.out.print("请输入文本文件路径: ");
            filename = scanner.nextLine().trim();
        }
        try {
            lab.buildGraphFromFile(filename);
            System.out.println("图构建成功！");
        } catch (IOException e) {
            System.err.println("读取文件失败: " + e.getMessage());
            return;
        }
        while (true) {
            System.out.println("\n请选择功能：");
            System.out.println("1. 展示有向图");
            System.out.println("2. 查询桥接词");
            System.out.println("3. 根据桥接词生成新文本");
            System.out.println("4. 计算最短路径");
            System.out.println("5. 计算PageRank");
            System.out.println("6. 随机游走");
            System.out.println("7. 退出");
            System.out.print("输入选项: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    lab.showDirectedGraph();
                    break;
                case "2":
                    System.out.print("请输入第一个单词: ");
                    String w1 = scanner.nextLine().trim();
                    System.out.print("请输入第二个单词: ");
                    String w2 = scanner.nextLine().trim();
                    System.out.println(lab.queryBridgeWords(w1, w2));
                    break;
                case "3":
                    System.out.print("请输入一行新文本: ");
                    String inputText = scanner.nextLine();
                    System.out.println("生成的新文本: " + lab.generateNewText(inputText));
                    break;
                case "4":
                    System.out.print("请输入第一个单词: ");
                    String sw1 = scanner.nextLine().trim();
                    System.out.print("请输入第二个单词（直接回车表示查询从第一个单词到所有节点的路径）: ");
                    String sw2 = scanner.nextLine().trim();
                    if (sw2.isEmpty()) {
                        // 查询单个单词到所有节点的最短路径
                        System.out.println(lab.calcShortestPathsFrom(sw1));
                    } else {
                        System.out.println(lab.calcShortestPath(sw1, sw2));
                    }
                    break;
                case "5":
                    System.out.print("请输入单词: ");
                    String pword = scanner.nextLine().trim();
                    Double pr = lab.calPageRank(pword);
                    if (pr == null) {
                        System.out.println("单词 \"" + pword + "\" 不在图中！");
                    } else {
                        System.out.printf("单词 \"%s\" 的PageRank值为: %.6f\n", pword, pr);
                    }
                    break;
                case "6":
                    String walk = lab.randomWalk();
                    System.out.println("随机游走路径: " + walk);
                    System.out.println("路径已保存到 random_walk.txt");
                    break;
                case "7":
                    System.out.println("再见！");
                    return;
                default:
                    System.out.println("无效选项，请重新输入。");
            }
        }
    }
}