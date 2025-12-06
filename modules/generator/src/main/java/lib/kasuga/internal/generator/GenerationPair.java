package lib.kasuga.internal.generator;

import com.github.javaparser.ast.CompilationUnit;

import java.io.File;

public record GenerationPair(
        CompilationUnit unit,
        File in
) {
}
