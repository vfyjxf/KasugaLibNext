package lib.kasuga.annotation;

import lib.kasuga.annotations.ConditionalMixin;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.*;

@SupportedAnnotationTypes("")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class AnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(roundEnv.processingOver())
            return false;

        for (Element rootElement : roundEnv.getRootElements()) {
            if(rootElement.getKind() != ElementKind.CLASS && rootElement.getKind() != ElementKind.INTERFACE)
                continue;
            TypeElement typeElement = (TypeElement) rootElement;
            for (AnnotationMirror annotationMirror : rootElement.getAnnotationMirrors()) {
                // TypeElement annotationElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
                List<AnnotationMirror> annotationCondition = new ArrayList<>();
                collectCondition(annotationCondition, annotationMirror);
            }
        }

        return false;
    }

    private void collectCondition(List<AnnotationMirror> collected, AnnotationMirror annotationMirror) {
        TypeElement annotationElement = (TypeElement) annotationMirror.getAnnotationType().asElement();

        for (AnnotationMirror mirror : annotationElement.getAnnotationMirrors()) {
            collectCondition(collected, mirror);
        }


        if(annotationElement.getQualifiedName().toString().equals(ConditionalMixin.class.getName()))
            return;

        collected.add(annotationMirror);
    }
}
