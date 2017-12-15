package com.bmuschko.gradle.docker

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class DockerJavaApplicationPluginIntegrationTest extends AbstractIntegrationTest {
    Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
    }

    def "Does not create tasks out-of-the-box when application plugin is not applied"() {
        when:
        applyDockerJavaApplicationPluginWithoutApplicationPlugin(project)

        then:
        !project.tasks.findByName(DockerJavaApplicationPlugin.COPY_DIST_RESOURCES_TASK_NAME)
        !project.tasks.findByName(DockerJavaApplicationPlugin.DOCKERFILE_TASK_NAME)
        !project.tasks.findByName(DockerJavaApplicationPlugin.BUILD_IMAGE_TASK_NAME)
        !project.tasks.findByName(DockerJavaApplicationPlugin.PUSH_IMAGE_TASK_NAME)
    }

    def "Creates tasks out-of-the-box when application plugin is applied"() {
        when:
        applyDockerJavaApplicationPluginAndApplicationPlugin(project)

        then:
        project.tasks.findByName(DockerJavaApplicationPlugin.COPY_DIST_RESOURCES_TASK_NAME)
        project.tasks.findByName(DockerJavaApplicationPlugin.DOCKERFILE_TASK_NAME)
        project.tasks.findByName(DockerJavaApplicationPlugin.BUILD_IMAGE_TASK_NAME)
        project.tasks.findByName(DockerJavaApplicationPlugin.PUSH_IMAGE_TASK_NAME)
    }

    def "Configures image task without project group and version"() {
        when:
        project.apply(plugin: 'application')
        project.apply(plugin: DockerJavaApplicationPlugin)

        then:
        DockerBuildImage task = project.tasks.findByName(DockerJavaApplicationPlugin.BUILD_IMAGE_TASK_NAME)
        task.tags == ["${project.applicationName}:latest"] as Set
    }

    def "Configures image task without project group and but with version"() {
        given:
        String projectVersion = '1.0'

        when:
        project.version = projectVersion
        applyDockerJavaApplicationPluginAndApplicationPlugin(project)

        then:
        DockerBuildImage task = project.tasks.findByName(DockerJavaApplicationPlugin.BUILD_IMAGE_TASK_NAME)
        task.tags == ["${project.applicationName}:${projectVersion}"] as Set
    }

    def "Configures image task with project group and version"() {
        given:
        String projectGroup = 'com.company'
        String projectVersion = '1.0'

        when:
        project.group = projectGroup
        project.version = projectVersion
        applyDockerJavaApplicationPluginAndApplicationPlugin(project)

        then:
        DockerBuildImage task = project.tasks.findByName(DockerJavaApplicationPlugin.BUILD_IMAGE_TASK_NAME)
        task.tags == ["${projectGroup}/${project.applicationName}:${projectVersion}"] as Set
    }

    def "Can access the dockerJava.javaApplication extension dynamically"() {
        given:
        applyDockerJavaApplicationPluginAndApplicationPlugin(project)
        when:
        project.docker.javaApplication
        then:
        noExceptionThrown()
    }

    @CompileStatic
    def "Can access the dockerJava.javaApplication extension statically"() {
        given:
        applyDockerJavaApplicationPluginAndApplicationPlugin(project)
        when:
        project.extensions.getByType(DockerExtension).javaApplication
        then:
        noExceptionThrown()
    }

    @TypeChecked
    def "Can configure the dockerJava.javaApplication extension statically"() {
        given:
        String testTagName = "some-test-tag"
        applyDockerJavaApplicationPluginAndApplicationPlugin(project)
        when:
        project.extensions.getByType(DockerExtension).javaApplication {
            // In kotlin this becomes much nicer.
            it.tag = "some-test-tag"
        }
        then:
        DockerBuildImage task = project
            .tasks
            .findByName(DockerJavaApplicationPlugin.BUILD_IMAGE_TASK_NAME) as DockerBuildImage
        task.getTags() == [testTagName] as Set
    }

    def "Can configure the dockerJava.javaApplication extension dynamically"() {
        given:
        String testTagName = "some-test-tag"
        applyDockerJavaApplicationPluginAndApplicationPlugin(project)
        when:
        project.docker.javaApplication {
            tag = testTagName
        }
        then:
        DockerBuildImage task = project.tasks.findByName(DockerJavaApplicationPlugin.BUILD_IMAGE_TASK_NAME)
        task.tags == [testTagName] as Set
    }

    private void applyDockerJavaApplicationPluginWithoutApplicationPlugin(Project project) {
        project.apply(plugin: DockerJavaApplicationPlugin)
    }

    private void applyDockerJavaApplicationPluginAndApplicationPlugin(Project project) {
        project.apply(plugin: 'application')
        applyDockerJavaApplicationPluginWithoutApplicationPlugin(project)
    }
}
