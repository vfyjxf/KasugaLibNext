package test.kasuga.utils.path;

import lib.kasuga.utils.path.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class PathMatcherTest {

    private PathMatcher matcher;
    private PathEntryFilterTreeNode root;

    // 模拟文件系统结构 (用于非并行匹配的 listFunction)
    // 结构:
    // root -> dirA
    //      -> dirB -> fileB1.txt
    //      -> file0.txt
    // dirA -> fileA1.txt
    //      -> fileA2.java

    private HashMap<PathEntry, Collection<PathEntry>> mockListCache = new HashMap<>();
    private Collection<PathEntry> generateMockList(PathEntry parent) {
        String pathStr = String.join("/", parent);
        switch (pathStr) {
            case "": // 初始路径 (Root)
                return Arrays.asList(
                        PathEntry.parse("dirA", "/"),
                        PathEntry.parse("dirB", "/"),
                        PathEntry.parse("file0.txt", "/")
                );
            case "dirA":
                return Arrays.asList(
                        PathEntry.parse("dirA/fileA1.txt", "/"),
                        PathEntry.parse("dirA/fileA2.java", "/")
                );
            case "dirB":
                return Arrays.asList(
                        PathEntry.parse("dirB/fileB1.txt", "/")
                );
            case "dirA/fileA1.txt":
            case "dirA/fileA2.java":
            case "dirB/fileB1.txt":
            case "file0.txt":
                return Arrays.asList(); // 文件没有子路径
            default:
                return Arrays.asList();
        }
    }
    private final Function<PathEntry, Collection<PathEntry>> mockListFunction = (parent) -> {
        return mockListCache.computeIfAbsent(parent, (s)->List.copyOf(generateMockList(parent)));
    };

    // 所有的 PathEntry 列表 (用于并行匹配)
    private final Collection<PathEntry> allEntries = Arrays.asList(
            PathEntry.parse("dirA", "/"),
            PathEntry.parse("dirB", "/"),
            PathEntry.parse("file0.txt", "/"),
            PathEntry.parse("dirA/fileA1.txt", "/"),
            PathEntry.parse("dirA/fileA2.java", "/"),
            PathEntry.parse("dirB/fileB1.txt", "/")
    );


    @BeforeEach
    void setUp() {
        // 创建 PathEntryFilterTreeNode 的根节点
        // 由于 PathMatcher 内部使用的是 PathEntryFilterTreeNode 的 root，
        // 且 PathEntryFilterTreeNode 的构造函数需要一个 PathFilter，
        // 我们创建一个总是 ACCEPT 的根过滤器。
        PathFilter alwaysAcceptFilter = pathPart -> PathFilter.Result.INITIAL;
        root = new PathEntryFilterTreeNode(alwaysAcceptFilter);
        matcher = new PathMatcher(root);
    }

    private void addFilter(String... pathParts) {
        PathEntryFilter filter = new PathEntryFilter();
        for (String part : pathParts) {
            filter.add(new PathFilter.GlobPathFilter(part));
        }
        root.add(filter);
    }

    // 辅助方法：将匹配结果转换为易于比较的字符串列表
    private List<String> toPathStrings(Collection<PathEntry> entries) {
        return entries.stream()
                .map(pathEntry -> String.join("/", pathEntry))
                .sorted()
                .collect(Collectors.toList());
    }

    // --- 非并行匹配测试 (match) ---

    @Test
    void testMatch_BasicFile() {
        // 过滤规则: file0.txt
        addFilter("file0.txt");

        PathEntry initialPath = PathEntry.parse("", "/"); // 模拟从根目录开始
        Collection<PathEntry> result = matcher.match(mockListFunction, initialPath);
        List<String> expected = Arrays.asList("file0.txt");

        assertEquals(expected, toPathStrings(result));
    }

    @Test
    void testMatch_SingleDirectory() {
        // 过滤规则: dirA
        addFilter("dirA");

        PathEntry initialPath = PathEntry.parse("", "/");
        Collection<PathEntry> result = matcher.match(mockListFunction, initialPath);
        List<String> expected = Arrays.asList("dirA"); // 期望只匹配到 dirA 本身

        assertEquals(expected, toPathStrings(result));
    }

    @Test
    void testMatch_WildcardSingleLevel() {
        // 过滤规则: dirA/*
        addFilter("dirA", "*");

        PathEntry initialPath = PathEntry.parse("", "/");
        Collection<PathEntry> result = matcher.match(mockListFunction, initialPath);
        List<String> expected = Arrays.asList(
                "dirA/fileA1.txt",
                "dirA/fileA2.java"
        );

        assertEquals(expected, toPathStrings(result));
    }

    @Test
    void testMatch_WildcardSpecificExtension() {
        // 过滤规则: dirA/*.java
        addFilter("dirA", "*.java");

        PathEntry initialPath = PathEntry.parse("", "/");
        Collection<PathEntry> result = matcher.match(mockListFunction, initialPath);
        List<String> expected = Arrays.asList("dirA/fileA2.java");

        assertEquals(expected, toPathStrings(result));
    }

    @Test
    void testMatch_RecursiveWildcard() {
        // 过滤规则: **/*.txt (匹配所有目录下的 .txt 文件)
        addFilter("**", "*.txt");

        PathEntry initialPath = PathEntry.parse("", "/");
        Collection<PathEntry> result = matcher.match(mockListFunction, initialPath);
        List<String> expected = Arrays.asList(
                "dirA/fileA1.txt",
                "dirB/fileB1.txt",
                "file0.txt" // file0.txt 在根目录下，且 PathMatcher 的实现是先匹配路径本身，然后递归
        );

        assertEquals(expected, toPathStrings(result));
    }

    @Test
    void testMatch_MultipleRules() {
        // 过滤规则1: file0.txt
        addFilter("file0.txt");
        // 过滤规则2: dirB/*
        addFilter("dirB", "*");

        PathEntry initialPath = PathEntry.parse("", "/");
        Collection<PathEntry> result = matcher.match(mockListFunction, initialPath);
        List<String> expected = Arrays.asList(
                "dirB/fileB1.txt",
                "file0.txt"
        );

        assertEquals(expected, toPathStrings(result));
    }

    // --- 并行匹配测试 (matchParallel) ---

    @Test
    void testMatchParallel_BasicFile() {
        // 过滤规则: file0.txt
        addFilter("file0.txt");

        // 从索引 0 (路径的第一个部分) 开始匹配
        Collection<PathEntry> result = matcher.matchParallel(allEntries, root);
        List<String> expected = Arrays.asList("file0.txt");

        assertEquals(expected, toPathStrings(result));
    }

    @Test
    void testMatchParallel_SingleDirectory() {
        // 过滤规则: dirA
        addFilter("dirA");

        // 从索引 0 开始，只匹配路径长度为 1 的 "dirA"
        Collection<PathEntry> result = matcher.matchParallel(allEntries, root);
        List<String> expected = Arrays.asList("dirA");

        assertEquals(expected, toPathStrings(result));
    }

    @Test
    void testMatchParallel_WildcardSingleLevel() {
        // 过滤规则: dirA/*
        addFilter("dirA", "*");

        // 从索引 0 开始
        Collection<PathEntry> result = matcher.matchParallel(allEntries, root);
        List<String> expected = Arrays.asList(
                "dirA/fileA1.txt",
                "dirA/fileA2.java"
        );

        assertEquals(expected, toPathStrings(result));
    }

    @Test
    void testMatchParallel_WildcardSpecificExtension() {
        // 过滤规则: dirB/*.txt
        addFilter("dirB", "*.txt");

        Collection<PathEntry> result = matcher.matchParallel(allEntries, root);
        List<String> expected = Arrays.asList("dirB/fileB1.txt");

        assertEquals(expected, toPathStrings(result));
    }

    @Test
    void testMatchParallel_RecursiveWildcard() {
        // 过滤规则: **/*.txt (匹配所有目录下的 .txt 文件)
        addFilter("**", "*.txt");

        Collection<PathEntry> result = matcher.matchParallel(allEntries, root);
        List<String> expected = Arrays.asList(
                "dirA/fileA1.txt",
                "dirB/fileB1.txt",
                "file0.txt"
        );

        assertEquals(expected, toPathStrings(result));
    }

    @Test
    void testMatchParallel_WildcardQuestionMark() {
        // 过滤规则: file?.txt
        addFilter("file?.txt");

        Collection<PathEntry> result = matcher.matchParallel(allEntries, root);
        List<String> expected = Arrays.asList("file0.txt");

        assertEquals(expected, toPathStrings(result));

        // 清空并添加新规则: dirA/fileA?.???a
        root = new PathEntryFilterTreeNode(pathPart -> PathFilter.Result.INITIAL);
        matcher.setRoot(root);
        addFilter("dirA", "fileA?.???a"); // 匹配 fileA2.java

        result = matcher.matchParallel(allEntries, root);
        expected = Arrays.asList("dirA/fileA2.java");

        assertEquals(expected, toPathStrings(result));
    }

    @Test
    void testMatchParallel_MultipleRules() {
        // 过滤规则1: dirA/*.txt
        addFilter("dirA", "*.txt");
        // 过滤规则2: dirB/*
        addFilter("dirB", "*");

        Collection<PathEntry> result = matcher.matchParallel(allEntries, root);
        List<String> expected = Arrays.asList(
                "dirA/fileA1.txt",
                "dirB/fileB1.txt"
        );

        assertEquals(expected, toPathStrings(result));
    }

    @Test
    void testMatchParallel_NoMatch() {
        // 过滤规则: nonExistentDir/*
        addFilter("nonExistentDir", "*");

        Collection<PathEntry> result = matcher.matchParallel(allEntries, root);
        assertTrue(result.isEmpty());
    }

    @Test
    void testMatchParallel_RecursiveWildcardOnly() {
        // 过滤规则: ** (匹配所有路径)
        root = new PathEntryFilterTreeNode(pathPart -> PathFilter.Result.INITIAL);
        matcher.setRoot(root);
        addFilter("**");

        Collection<PathEntry> result = matcher.matchParallel(allEntries, root);
        List<PathEntry> expectedPath = new ArrayList<>(allEntries);
        expectedPath.add(new PathEntry()); // Add root path
        List<String> expected = expectedPath.stream()
                .map(pathEntry -> String.join("/", pathEntry))
                .sorted()
                .collect(Collectors.toList());


        assertEquals(expected, toPathStrings(result));
    }

    private final Collection<PathEntry> allComplexEntries = generateComplexEntries();

    protected HashMap<PathEntry, Collection<PathEntry>> CACHE = new HashMap<>();
    // 模拟复杂文件系统结构 (用于非并行匹配的 listFunction)
    private final Function<PathEntry, Collection<PathEntry>> mockComplexListFunction = (parent) -> {
        if(CACHE.containsKey(parent)) {
            return CACHE.get(parent);
        }
        Collection<PathEntry> subPaths = generateSubPaths(parent);
        CACHE.put(parent, subPaths);
        return subPaths;
    };

    private Collection<PathEntry> generateSubPaths(PathEntry parent) {
        parent = new PathEntry(parent);
        if(Objects.equals(parent.getFirst(), "")) {
            parent.removeFirst();
        }
        String[] chars = new String[12];
        for (int i = 0; i < 12; i++) {
            chars[i] = String.valueOf((char) ('a' + i));
        }
        if(parent.size() < 4) {
            Collection<PathEntry> createdEntries = new ArrayList<>();
            for (String aChar : chars) {
                createdEntries.add(parent.extend(aChar));
            }
            return createdEntries;
        } else if(parent.size() == 4) {
            PathEntry finalParent = parent;
            return allComplexEntries.stream()
                    .filter(entry -> String.join("/", entry).startsWith(String.join("/", finalParent) + "/"))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    /**
     * 生成复杂路径列表: [a~z]/[a~z]/[a~z]/[a~z]/[a~z].txt 和一些额外的目录
     * 并添加 `c/b/c/a/e.txt` 用于测试
     */
    private static Collection<PathEntry> generateComplexEntries() {
        Set<PathEntry> entries = new HashSet<>();
        String[] chars = new String[12];
        for (int i = 0; i < 12; i++) {
            chars[i] = String.valueOf((char) ('a' + i));
        }

        // 核心结构: L1/L2/L3/L4/L5.txt
        for (String c1 : chars) {
            for (String c2 : chars) {
                for (String c3 : chars) {
                    for (String c4 : chars) {
                        entries.add(PathEntry.parse(c1 + "/" + c2 + "/" + c3 + "/" + c4 + "/" + chars[0] + ".txt", "/"));
                        // 额外添加一些目录项，确保 PathEntry 可以代表目录
                        entries.add(PathEntry.parse(c1, "/"));
                        entries.add(PathEntry.parse(c1 + "/" + c2, "/"));
                        entries.add(PathEntry.parse(c1 + "/" + c2 + "/" + c3, "/"));
                        entries.add(PathEntry.parse(c1 + "/" + c2 + "/" + c3 + "/" + c4, "/"));
                    }
                }
            }
        }

        // 添加一个明确的匹配项和不匹配项用于测试
        // 匹配项: c/b/c/a/e.txt (因为规则是 c/**/a)
        entries.add(PathEntry.parse("c/b/c/a/e.txt", "/"));
        entries.add(PathEntry.parse("c/b/c/a", "/")); // 目录
        entries.add(PathEntry.parse("c/b/c", "/")); // 目录
        entries.add(PathEntry.parse("c/b", "/")); // 目录
        entries.add(PathEntry.parse("c", "/")); // 目录

        // 不匹配项: b/c/d/e/z.txt
        entries.add(PathEntry.parse("b/c/d/e/z.txt", "/"));
        entries.add(PathEntry.parse("b/c/d/e", "/")); // 目录
        entries.add(PathEntry.parse("b/c/d", "/")); // 目录
        entries.add(PathEntry.parse("b/c", "/")); // 目录
        entries.add(PathEntry.parse("b", "/")); // 目录


        // 去重并确保目录结构完整性 (重要)
        return entries.stream().distinct().collect(Collectors.toList());
    }

    // ... (保留 beforeEach 和 addFilter, toPathStrings 方法) ...

    // --- 复杂测试用例：c/**/a/ (非并行匹配) ---

    @Test
    void testMatch_ComplexRecursiveWildcard_Manual() {
        root = new PathEntryFilterTreeNode(pathPart -> PathFilter.Result.INITIAL);
        matcher.setRoot(root);
        addFilter("c", "**", "a");
        PathEntry initialPath = PathEntry.parse("", "/");
        matcher.match(mockComplexListFunction, initialPath);
        matcher.match(mockComplexListFunction, initialPath);// For optimization
        long startTime = System.nanoTime();
        Collection<PathEntry> result = matcher.match(mockComplexListFunction, initialPath);
        long stopTime = System.nanoTime();
        List<String> expected = allComplexEntries.stream()
                .map(pathEntry -> String.join("/", pathEntry))
                .filter(path -> {
                    // 路径必须以 c/ 开头，且以 /a 结尾
                    return path.startsWith("c/") && path.endsWith("/a");
                })
                .sorted()
                .collect(Collectors.toList());
        List<String> actualResult = toPathStrings(result);
        System.out.println("Time cost of wildcard: " +  ((double) (startTime - stopTime) / 1000000) + "ms");
        assertEquals(expected, actualResult, "Complex recursive wildcard 'c/**/a' should match all paths starting with c/ and ending with /a.");
    }

    @Test
    void testBenchmarkingMatchWithRegex() {
        Pattern pattern = Pattern.compile("^c/.+/a$");
        Predicate<String> stringPredicate = pattern.asMatchPredicate();
        List<String> pathEntries = allComplexEntries.stream().map(s->String.join("/", s)).toList();
        List<String> acceptedEntries = new ArrayList<>();

        for (String str : pathEntries) {
            if(stringPredicate.test(str)) {
                acceptedEntries.add(str);
            }
        }

        for (String str : pathEntries) {
            if(stringPredicate.test(str)) {
                acceptedEntries.add(str);
            }
        }

        long startTime = System.nanoTime();
        for (String str : pathEntries) {
            if(stringPredicate.test(str)) {
                acceptedEntries.add(str);
            }
        }
        System.out.println("Size: " + acceptedEntries.size());
        long stopTime = System.nanoTime();
        System.out.println("Time cost of regex: " + ((double) (startTime - stopTime) / 1000000) + "ms");

    }

    // --- 复杂测试用例：c/**/a/ (并行匹配) ---

    @Test
    void testMatchParallel_ComplexRecursiveWildcard_Manual() {

        // 过滤规则: c/**/a

        // 每次测试前重置 root
        root = new PathEntryFilterTreeNode(pathPart -> PathFilter.Result.INITIAL);
        matcher.setRoot(root);

        addFilter("c", "**", "a");

        // 使用 allComplexEntries 进行并行匹配
        matcher.matchParallel(allComplexEntries, root);
        matcher.matchParallel(allComplexEntries, root); // Optimization

        long startTime = System.nanoTime();
        Collection<PathEntry> result = matcher.matchParallel(allComplexEntries, root);
        long stopTime = System.nanoTime();

        System.out.println("Time cost of wildcard: " +  ((double) (startTime - stopTime) / 1000000) + "ms");

        // 验证逻辑：从 allComplexEntries 中筛选出满足 "c/**/a" 的路径
        List<String> expected = allComplexEntries.stream()
                .map(pathEntry -> String.join("/", pathEntry))
                .filter(path -> {
                    // 路径必须以 c/ 开头，且以 /a 结尾
                    return path.startsWith("c/") && path.endsWith("/a");
                })
                .sorted()
                .collect(Collectors.toList());

        List<String> actualResult = toPathStrings(result);

        // 严格断言
        assertEquals(expected, actualResult, "Complex parallel recursive wildcard 'c/**/a' should match all paths starting with c/ and ending with /a.");

        // 额外的验证，确保文件 c/b/c/a/e.txt **不** 被匹配
        assertFalse(actualResult.contains("c/b/c/a/e.txt"), "The file c/b/c/a/e.txt should not be matched by 'c/**/a'");
    }

    @Test
    void testMatchWithTerminalReturn() {
        root = new PathEntryFilterTreeNode(pathPart -> PathFilter.Result.INITIAL);
        matcher.setRoot(root);

        addFilter("c", "**", "a");

        matcher.setNoExplore(true);

        PathEntry initialPath = PathEntry.parse("", "/");
        Collection<PathEntry> result = matcher.match(mockComplexListFunction, initialPath);
        List<String> expected = allComplexEntries.stream()
                .map(pathEntry -> String.join("/", pathEntry))
                .filter(path -> {
                    return path.startsWith("c/") && path.endsWith("a") && path.indexOf("a") == path.lastIndexOf("a");
                })
                .sorted()
                .collect(Collectors.toList());
        List<String> actualResult = toPathStrings(result);
        assertEquals(expected, actualResult, "Complex recursive wildcard 'c/**/a' should match all paths starting with c/ and ending with /a.");
    }

    @Test
    void testMatchWithTerminalReturnParallel() {
        root = new PathEntryFilterTreeNode(pathPart -> PathFilter.Result.INITIAL);
        matcher.setRoot(root);

        addFilter("c", "**", "a");

        matcher.setNoExplore(true);

        PathEntry initialPath = PathEntry.parse("", "/");
        Collection<PathEntry> result = matcher.matchParallel(allComplexEntries, root);
        List<String> expected = allComplexEntries.stream()
                .map(pathEntry -> String.join("/", pathEntry))
                .filter(path -> {
                    return path.startsWith("c/") && path.endsWith("a") && path.indexOf("a") == path.lastIndexOf("a");
                })
                .sorted()
                .collect(Collectors.toList());
        List<String> actualResult = toPathStrings(result);
        assertEquals(expected, actualResult, "Complex recursive wildcard 'c/**/a' should match all paths starting with c/ and ending with /a.");
    }

    // --- 多过滤器同时匹配测试 ---

    @Test
    void testMatch_MultipleFiltersAtOnce() {
        // 每次测试前重置 root
        root = new PathEntryFilterTreeNode(pathPart -> PathFilter.Result.INITIAL);
        matcher.setRoot(root);

        // 过滤规则 1: file0.txt
        addFilter("file0.txt");
        // 过滤规则 2: dirA/*.java
        addFilter("dirA", "*.java");
        // 过滤规则 3: dirB/**
        addFilter("dirB", "**");

        PathEntry initialPath = PathEntry.parse("", "/");
        Collection<PathEntry> result = matcher.match(mockListFunction, initialPath);

        // 预期匹配结果:
        // 1. file0.txt
        // 2. dirA/fileA2.java
        // 3. dirB, dirB/fileB1.txt (dirB/** 会匹配 dirB 及其所有子项)
        List<String> expected = Arrays.asList(
                "dirA/fileA2.java",
                "dirB",
                "dirB/fileB1.txt",
                "file0.txt"
        );

        assertEquals(expected, toPathStrings(result), "Should match all entries satisfying the three filters: file0.txt, dirA/*.java, and dirB/**.");
    }

    @Test
    void testMatchParallel_MultipleFiltersAtOnce() {
        // 每次测试前重置 root
        root = new PathEntryFilterTreeNode(pathPart -> PathFilter.Result.INITIAL);
        matcher.setRoot(root);

        // 过滤规则 1: file0.txt
        addFilter("file0.txt");
        // 过滤规则 2: dirA/*.java
        addFilter("dirA", "*.java");
        // 过滤规则 3: dirB/**
        addFilter("dirB", "**");

        Collection<PathEntry> result = matcher.matchParallel(allEntries, root);

        // 预期匹配结果:
        // 1. file0.txt
        // 2. dirA/fileA2.java
        // 3. dirB, dirB/fileB1.txt (dirB/** 会匹配 dirB 及其所有子项)
        List<String> expected = Arrays.asList(
                "dirA/fileA2.java",
                "dirB",
                "dirB/fileB1.txt",
                "file0.txt"
        );

        assertEquals(expected, toPathStrings(result), "Should match all entries satisfying the three filters in parallel: file0.txt, dirA/*.java, and dirB/**.");
    }
}