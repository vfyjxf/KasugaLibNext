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


                compileClasspath += project.configurations.codeGenDependency
            }
        }
        var minecraftArtifacts = project.tasks.findByName("createMinecraftArtifacts");

        def utilsProject = project.findProject(':modules:utils')
        def utilsMinecraftArtifacts = utilsProject?.tasks?.findByName("createMinecraftArtifacts")

        var runGenerator = project.tasks.register('runGenerator', JavaExec) {

            inputs.files(project.sourceSets.codeTemplate.allSource)
                    .withPropertyName("sources");

            outputs.dir("$project.buildDir/generated/sources/codegen/java/main")

            classpath = project.configurations.codeGenDependency
            mainClass = 'lib.kasuga.internal.generator.KasugaCodeGen'
            Provider<Set<FileSystemLocation>> provider = project.sourceSets.main.runtimeClasspath.getElements()
            Provider<Set<FileSystemLocation>> mainSource = project.sourceSets.main.output.getElements()
            def codeTemplateSrcDir = project.sourceSets.codeTemplate.java.srcDirs.first().absolutePath
            def generatedDir = "$project.buildDir/generated/sources/codegen/java/main"
            def buildDirPath = project.buildDir.absolutePath
            def mainSrcDirs = project.sourceSets.main.java.srcDirs
                    .findAll { !it.absolutePath.startsWith(buildDirPath) }
            args(codeTemplateSrcDir)
            args(generatedDir)
            doFirst{
                def allClasspath = mainSrcDirs.collect { it.absolutePath } + provider.get().asFile*.absolutePath
                if (utilsProject != null) {
                    def artifactsDir = new File(utilsProject.buildDir, "moddev/artifacts")
                    if (artifactsDir.exists()) {
                        artifactsDir.eachFile { f ->
                            if (f.name.endsWith('.jar') && !f.name.endsWith('-sources.jar')) {
                                allClasspath.add(f.absolutePath)
                            }
                        }
                    }
                }
                args(allClasspath.join(File.pathSeparator))
            }

            javaLauncher.set(project.javaToolchains.launcherFor {
                languageVersion = project.java.toolchain.languageVersion
            })

            if(minecraftArtifacts != null)
                dependsOn(minecraftArtifacts)
            if(utilsMinecraftArtifacts != null)
                dependsOn(utilsMinecraftArtifacts)
        }
        project.compileJava.dependsOn(runGenerator)

        project.dependencies {
            if (project.findProject(":modules:generator")) {
                codeGenDependency project.findProject(':modules:generator');
            } else if(project.findProject(":KasugaLibNext:modules:generator")) {
                codeGenDependency project.findProject(':KasugaLibNext:modules:generator');
            } else {
                //@TODO:FIXME{
                codeGenDependency 'lib.kasuga:generator:1.0.0';
            }
        }
    }
}