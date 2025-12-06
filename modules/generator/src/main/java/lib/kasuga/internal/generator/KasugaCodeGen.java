package lib.kasuga.internal.generator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lib.kasuga.internal.generator.annotations.CodeTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

public class KasugaCodeGen {
    protected static Logger LOGGER = Logger.getLogger(KasugaCodeGen.class.getName());
    public static void main(String[] args) throws IOException {
        String mainSrcDir = args[0];
        String generatedDir = args[1];
        String classPath = args[2];
        System.out.println(mainSrcDir);
        System.out.println(generatedDir);
        System.out.println(classPath);
        doGeneration(Path.of(mainSrcDir).toFile(), Path.of(generatedDir).toFile(), classPath);
    }

    private static void doGeneration(File src, File dst, String classPath) {
        if(!src.exists())
            return;

        dst.mkdirs();

        List<File> sources = collectSources(src);

        String[] classPathEntries = classPath.split(File.pathSeparator);

        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();

        combinedTypeSolver.add(new ReflectionTypeSolver(false));

        combinedTypeSolver.add(new JavaParserTypeSolver(src));

        for (String path : classPathEntries) {
            File file = new File(path);

            // 确保路径存在且有效
            if (!file.exists()) {
                System.out.println("Warning: Classpath entry not found: " + path);
                continue;
            }

            try {
                if (file.isDirectory()) {
                    // 对于目录，使用 JavaParserTypeSolver (它能解析编译后的 .class 文件和未编译的 .java 文件)
                    // 如果这些目录只包含编译后的 .class 文件，它也能工作。
                    combinedTypeSolver.add(new JavaParserTypeSolver(file));
                    combinedTypeSolver.add(new JarTypeSolver(file));
                } else if (file.getName().toLowerCase().endsWith(".jar")) {
                    // 对于 JAR 文件，使用 JarTypeSolver
                    combinedTypeSolver.add(new JarTypeSolver(path));
                }
            } catch (Exception e) {
                System.err.println("Error adding TypeSolver for " + path + ": " + e.getMessage());
                // 可以选择在这里跳过错误的 TypeSolver，继续处理下一个
            }
        }


        final ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        parserConfiguration.setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));

        JavaParser javaParser = new JavaParser(parserConfiguration);

        List<ParseResult<CompilationUnit>> parseResults = sources.stream().map(file -> {
            try {
                return javaParser.parse(file);
            } catch (FileNotFoundException e) {
                LOGGER.warning("Failed to read file" + file.toString());
                return null;
            }
        }).toList();

        HashMap<String, List<GenerationPair>> toGenerate = new HashMap<>();

        for (int i = 0; i < parseResults.size(); i++) {
            ParseResult<CompilationUnit> parseResult = parseResults.get(i);
            if(parseResult == null)
                continue;
            File f = sources.get(i);
            if(!parseResult.isSuccessful()) {
                LOGGER.warning("Failed to compile the file" + f.toString());
                for (Problem problem : parseResult.getProblems()) {
                    LOGGER.warning(problem.getMessage());
                }
                continue;
            }


            NodeList<AnnotationExpr> annotations = parseResult.getResult()
                    .flatMap(CompilationUnit::getPrimaryType)
                    .map(BodyDeclaration::getAnnotations).orElse(NodeList.nodeList());

            String generatorName = null;

            for (AnnotationExpr annotation : annotations) {
                try{
                    var expr = annotation.resolve();
                    if(expr.getQualifiedName().startsWith("lib.kasuga.internal.generator.annotations")) {
                        annotation.setData(SourceCodeWriter.TO_BE_REMOVE, true);
                    }
                    if(Objects.equals(expr.getQualifiedName(), CodeTemplate.class.getName())){
                        List<MemberValuePair> pair = annotation.asAnnotationExpr().getChildNodes().stream().filter(x->x instanceof MemberValuePair).map(x->(MemberValuePair)x).toList();
                        for (MemberValuePair memberValuePair : pair) {
                            if(Objects.equals(memberValuePair.getNameAsString(), "lib/kasuga/internal/generator") && memberValuePair.getValue().isStringLiteralExpr()) {
                                generatorName = memberValuePair.getValue().asStringLiteralExpr().getValue();
                                break;
                            }
                        }
                    }
                }catch (UnsolvedSymbolException e) {}
                if(generatorName != null) break;
            }
            if(generatorName == null)
                continue;

            toGenerate.computeIfAbsent(generatorName, (s)->new ArrayList<>()).add(new GenerationPair(parseResult.getResult().get(), f));


            parseResult.getResult().get().setStorage(src.toPath().relativize(f.toPath()));
        }

        for (String gen : toGenerate.keySet()) {
            for (GenerationPair generationPair : toGenerate.get(gen)) {
                CodeGenerator generator = AllKasugaCodeGen.GENERATORS.get(gen);
                Path outFileName = src.toPath().relativize(generationPair.in().toPath());
                if(outFileName.startsWith("template")){
                    outFileName = Path.of("template").relativize(outFileName);
                }
                Path outFile = dst.toPath().resolve(outFileName);
                SourceCodeWriter writer = new SourceCodeWriter(outFile.getParent());

                for (ClassOrInterfaceType extendedType : generationPair.unit().getPrimaryType().get().asClassOrInterfaceDeclaration().getImplementedTypes()) {
                    if(!extendedType.getNameAsString().equals("Inline"))
                        continue;
                    ClassOrInterfaceType original = extendedType;
                    extendedType = extendedType.getTypeArguments().get().get(0).asClassOrInterfaceType();
                    try{
                        ResolvedType extendedTypeResolved = extendedType.resolve();
                        original.remove();
                        Optional<ResolvedReferenceTypeDeclaration> resolvedTypeDeclaration = extendedTypeResolved.asReferenceType().getTypeDeclaration();
                        if(resolvedTypeDeclaration.isEmpty())
                            continue;
                        Optional<Node> node = resolvedTypeDeclaration.get().toAst();
                        if(!node.isPresent())
                            continue;
                        Node n = node.get();
                        if(n instanceof ClassOrInterfaceDeclaration cd) {
                            for (BodyDeclaration<?> member : cd.getMembers()) {
                                if (member.isConstructorDeclaration())
                                    continue;
                                generationPair.unit().getPrimaryType().get().addMember(member.clone());
                            }
                            for (AnnotationExpr annotation : cd.getAnnotations()) {
                                generationPair.unit().getPrimaryType().get().addAnnotation(annotation);
                            }
                        }
                    }catch(UnsolvedSymbolException exception) {}
                    break;
                }

                CompilationUnit generated = generator.generate(generationPair.unit(), writer);
                SourceCodeWriter.postProcess(generated);

                outFile.getParent().toFile().mkdirs();
                try {
                    Files.writeString(outFile, generated.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }



    private static List<File> collectSources(File src) {
        List<File> fileList = new ArrayList<>();
        collectSources(src, fileList);
        return fileList;
    }

    private static void collectSources(File src, List<File> fileList) {
        if(src == null)
            return;
        File[] files = src.listFiles();
        if(files == null)
            return;
        for (File file : files) {
            if(file.isDirectory()){
                collectSources(file, fileList);
                continue;
            }
            if(file.getName().endsWith(".java")) {
                fileList.add(file);
            }
        }
    }
}
