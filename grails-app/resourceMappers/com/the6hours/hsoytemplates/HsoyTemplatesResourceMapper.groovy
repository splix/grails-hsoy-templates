package com.the6hours.hsoytemplates

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.grails.plugin.resource.mapper.MapperPhase

/**
 * @author Igor Artamonov (http://igorartamonov.com)
 */
class HsoyTemplatesResourceMapper implements GrailsApplicationAware {

    GrailsApplication grailsApplication

    def phase = MapperPhase.GENERATION

    static defaultIncludes = ['**/*.hsoy']

    def map(resource, config) {
        File originalFile = resource.processedFile
        File target

        if (resource.sourceUrl) {
            File input = getOriginalFileSystemFile(resource.sourceUrl)
            target = new File(generateCompiledFileFromOriginal(originalFile.absolutePath))

            if (log.debugEnabled) {
                log.debug "Compiling hsoy file [${originalFile}] into [${target}]"
            }

            try {
                HsoyJsCompiler compiler = new HsoyJsCompiler()

                def w = target.newWriter()
                w << compiler.compileToString(input.text, originalFile.name)
                w.close()

                resource.processedFile = target
                resource.sourceUrlExtension = 'js'
                resource.actualUrl = generateCompiledFileFromOriginal(resource.originalUrl)
                resource.contentType = 'text/javascript'
            } catch (e) {
                log.error("Error compiling hsoy file: ${originalFile}", e)
            }
        }
    }

    private String generateCompiledFileFromOriginal(String original) {
        original.replaceAll(/(?i)\.hsoy/, '.js')
    }

    private File getOriginalFileSystemFile(String sourcePath) {
        grailsApplication.parentContext.getResource(sourcePath).file
    }
}
