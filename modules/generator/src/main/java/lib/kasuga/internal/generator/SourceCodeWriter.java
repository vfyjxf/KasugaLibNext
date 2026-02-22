package lib.kasuga.internal.generator;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.resolution.UnsolvedSymbolException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SourceCodeWriter {

    public static DataKey<Boolean> TO_BE_REMOVE = new DataKey<Boolean>() {};
    Path path;
    public SourceCodeWriter(Path path) {
        this.path = path;
    }

    public void write(Path name, CompilationUnit unit) {
        postProcess(unit);
        Path outFileName = path.resolve(name);
        outFileName.getParent().toFile().mkdirs();
        System.out.println(outFileName);
        try {
            Files.writeString(outFileName, unit.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void postProcess(CompilationUnit source) {
        HashSet<AnnotationExpr> shouldRemove = new HashSet<>();

        source.walk(AnnotationExpr.class, a->{
            if(a.containsData(TO_BE_REMOVE)){
                shouldRemove.add(a);
            }
            try{
                if(a.resolve().getQualifiedName().startsWith("lib.kasuga.internal.generator")) {
                    shouldRemove.add(a);
                }
            }catch (UnsolvedSymbolException ignored){
                ignored.printStackTrace();
            }
        });

        for (AnnotationExpr annotationExpr : shouldRemove) {
            annotationExpr.remove();
        }

        source.getPackageDeclaration().ifPresent(p->{
            if(p.getNameAsString().startsWith("template.")){
                p.setName(p.getNameAsString().substring("template.".length()));
            }
        });

        for (ImportDeclaration anImport : Set.copyOf(source.getImports())) {
            if (anImport.getNameAsString().startsWith("template.")) {
                anImport.setName(anImport.getNameAsString().substring("template.".length()));
            }
        }

        HashSet<String> usedNames = new HashSet<>();
        usedNames.add("*");


        source.walk(Name.class, n->{
            usedNames.add(n.getIdentifier());
        });
        source.walk(SimpleName.class, n->{
            usedNames.add(n.getIdentifier());
        });
        source.walk(ClassOrInterfaceType.class, p->{
            usedNames.add(p.getName().getIdentifier());
        });
        source.walk(TypeParameter.class, p->{
            usedNames.add(p.getName().getIdentifier());
        });
        source.walk(ReferenceType.class, p->{
            Type inner = p.getElementType();
            if(inner instanceof ClassOrInterfaceType cit){
                usedNames.add(cit.getName().getIdentifier());
            }
        });


        for (ImportDeclaration anImport : Set.copyOf(source.getImports())) {
            if(anImport.getNameAsString().startsWith("lib.kasuga.internal.generator")){
                source.remove(anImport);
                continue;
            }
//            if(!anImport.isAsterisk() && !usedNames.contains(anImport.getName().getIdentifier())){
//                source.remove(anImport);
//                continue;
//            }
//            if(Objects.equals(anImport.getName().getQualifier().map(Name::asString).orElse(""), source.getPackageDeclaration().map(PackageDeclaration::getName).map(Name::asString).orElse(""))) {
//                source.remove(anImport);
//            }
        }
    }
}
