package lib.kasuga.internal.generator;

import com.github.javaparser.ast.CompilationUnit;

public interface CodeGenerator {
    public default CompilationUnit generate(CompilationUnit source, SourceCodeWriter writer) {
        return generate(source);
    }

    public default CompilationUnit generate(CompilationUnit unit) {
        return null;
    }
}
