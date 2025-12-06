package builder.kasuga

import org.gradle.api.Project
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.tasks.JavaExec

import org.gradle.api.provider.Provider

public class KasugaGenerator {
    public static void useGenerator(Project project) {
        project.configurations {
            codeGenDependency
        }
        project.sourceSets {
            codeTemplate {
                java {
                    srcDir 'src/codeTemplate/java'
                }
                resources {
                    srcDir 'src/codeTemplate/resources'
                }

                compileClasspath += project.sourceSets.main.output
                runtimeClasspath += project.sourceSets.main.output

                compileClasspath += project.sourceSets.main.compileClasspath
                runtimeClasspath += project.sourceSets.main.runtimeClasspath
            }
        }
        var runGenerator = project.tasks.register('runGenerator', JavaExec) {
//
//            inputs.files(project.sourceSets.codeTemplate.allSource)
//                    .withPropertyName("sources");
//
//            outputs.dir("$project.buildDir/generated/sources/codegen/java/main")

            classpath = project.configurations.codeGenDependency
            mainClass = 'lib.kasuga.internal.generator.KasugaCodeGen'
            Provider<Set<FileSystemLocation>> provider = project.sourceSets.main.runtimeClasspath.getElements()
            Provider<Set<FileSystemLocation>> mainSource = project.sourceSets.main.output.getElements()
            args(project.sourceSets.codeTemplate.java.srcDirs.first().absolutePath)
            args("$project.buildDir/generated/sources/codegen/java/main")
            doFirst{
                args(provider.get().asFile.join(File.pathSeparator))
            }

            javaLauncher.set(project.javaToolchains.launcherFor {
                languageVersion = project.java.toolchain.languageVersion
            })
        }
        project.compileJava.dependsOn(runGenerator)

        project.dependencies {
            if (project.findProject(":modules:generator")) {
                codeGenDependency project.findProject(':modules:generator');
            } else {
                throw new IllegalStateException();
            }
        }
    }
}