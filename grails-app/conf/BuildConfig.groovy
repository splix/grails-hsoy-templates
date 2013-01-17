grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
        mavenRepo "http://maven.the6hours.com/release"
    }

    dependencies {
        compile 'com.the6hours:hsoy-templates:0.3'
    }

    plugins {
        build(":release:2.2.0", ":rest-client-builder:1.0.3") {
            export = false
        }
        compile(":resources:1.2.RC2")
    }
}
