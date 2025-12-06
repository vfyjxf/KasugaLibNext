package lib.kasuga.test.graph;

import lib.kasuga.content.graph.GraphManager;
import lib.kasuga.structure.Pair;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {
    public static class SimpleGraphManager extends GraphManager<Integer, String, String> {

        private AtomicInteger counter = new AtomicInteger(0);

        @Override
        protected Graph<String, String> createGraph() {
            return new DefaultUndirectedGraph<>(String.class);
        }

        @Override
        protected Integer createKey() {
            return counter.getAndIncrement();
        }
    }


    private SimpleGraphManager manager;

    @BeforeEach
    void setup() {
        manager = new SimpleGraphManager();
    }

    @Test
    void testComplexGraphMergeAndSplit() {
        // 创建多个节点
        for (char c = 'A'; c <= 'H'; c++) {
            manager.createNode(String.valueOf(c));
        }


        // 创建多个连接，形成两部分连通图
        manager.addConnection("A", "B", "edge1");
        manager.addConnection("B", "C", "edge2");
        manager.addConnection("D", "E", "edge3");
        manager.addConnection("E", "F", "edge4");
        manager.addConnection("F", "E", "edge5");

        // 两个独立图
        Graph<String, String> graph1 = manager.getGraphByNode("A");
        Graph<String, String> graph2 = manager.getGraphByNode("D");
        assertNotEquals(graph1, graph2);

        // 合并两个图
        manager.addConnection("C", "D", "edge5");
        Graph<String, String> mergedGraph = manager.getGraphByNode("A");
        assertEquals(mergedGraph, manager.getGraphByNode("F"));
        assertTrue(mergedGraph.containsEdge("C", "D"));

        // 再增加一些节点和边
        manager.addConnection("G", "H", "edge6");
        manager.addConnection("F", "G", "edge7");

        // 删除边触发拆分
        manager.removeConnection("C", "D");
        Graph<String, String> graphPart1 = manager.getGraphByNode("A");
        Graph<String, String> graphPart2 = manager.getGraphByNode("D");

        assertNotEquals(graphPart1, graphPart2);
        assertTrue(graphPart1.containsVertex("A"));
        assertTrue(graphPart1.containsVertex("C"));
        assertFalse(graphPart1.containsVertex("D"));
        assertFalse(graphPart1.containsVertex("F"));
        assertFalse(graphPart1.containsVertex("G"));
        assertFalse(graphPart1.containsVertex("H"));


        assertFalse(graphPart2.containsVertex("A"));
        assertFalse(graphPart2.containsVertex("C"));
        assertTrue(graphPart2.containsVertex("D"));
        assertTrue(graphPart2.containsVertex("F"));
        assertTrue(graphPart2.containsVertex("G"));
        assertTrue(graphPart2.containsVertex("H"));

        // 删除节点触发再次拆分
        manager.removeNode("F");
        Graph<String, String> graphFRemoved1 = manager.getGraphByNode("D");
        Graph<String, String> graphFRemoved2 = manager.getGraphByNode("G");
        Graph<String, String> graphFRemoved3 = manager.getGraphByNode("H");

        assertNotEquals(graphFRemoved1, graphFRemoved2);
        assertEquals(graphFRemoved2, graphFRemoved3);
        assertTrue(graphFRemoved1.containsVertex("D"));
        assertTrue(graphFRemoved2.containsVertex("G"));
        assertTrue(graphFRemoved3.containsVertex("H"));


        // 删除边触发再次拆分
        manager.removeConnection("G", "H");
        Graph<String, String> graphFRemoved4 = manager.getGraphByNode("G");
        Graph<String, String> graphFRemoved5 = manager.getGraphByNode("H");

        assertNotEquals(graphFRemoved4, graphFRemoved5);
        assertTrue(graphFRemoved4.containsVertex("G"));
        assertTrue(graphFRemoved5.containsVertex("H"));
    }

    @Test
    void testSynchronizeNodeComplex() {
        // 创建节点和部分连接
        manager.createNode("X");
        manager.createNode("Y");
        manager.addConnection("X", "Y", "edgeXY");

        // 新节点 Z 与 X, Y, 新节点 A 的连接
        Supplier<List<Pair<String, String>>> supplier = () -> List.of(
                Pair.of("X", "edgeZX"),
                Pair.of("Y", "edgeZY"),
                Pair.of("A", "edgeZA")
        );

        manager.synchronizeNode("Z", supplier);

        Graph<String, String> graphZ = manager.getGraphByNode("Z");

        assertTrue(graphZ.containsVertex("Z"));
        assertTrue(graphZ.containsVertex("X"));
        assertTrue(graphZ.containsVertex("Y"));
        assertTrue(graphZ.containsVertex("A"));
        assertTrue(graphZ.containsEdge("Z", "X"));
        assertTrue(graphZ.containsEdge("Z", "Y"));
        assertTrue(graphZ.containsEdge("Z", "A"));
    }
}
