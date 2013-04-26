package com.the6hours.hsoytemplates

import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.grails.plugin.resource.ResourceMeta
import org.grails.plugin.resource.mapper.MapperPhase
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

/**
 * @author Igor Artamonov (http://igorartamonov.com)
 */
class HsoyTemplatesResourceMapper implements GrailsApplicationAware, ResourceLoaderAware {

    GrailsApplication grailsApplication
    ResourceLoader resourceLoader

    def phase = MapperPhase.GENERATION

    static defaultIncludes = ['**/*.hsoy']

    long lastTemplate = 0
    String lastTemplateName = null
    String template

    def map(ResourceMeta resource, config) {
        File originalFile = resource.processedFile
        File target

        if (resource.sourceUrl) {
            File input = getOriginalFileSystemFile(resource.sourceUrl)
            target = new File(generateCompiledFileFromOriginal(originalFile.absolutePath))

            if (log.debugEnabled) {
                log.debug "Compiling hsoy file [${originalFile}] into [${target}]"
            }

            try {
                String templateFileName = grailsApplication.config.grails.plugins.hsoytemplates.javascript.template
                if (StringUtils.isNotEmpty(templateFileName) && templateFileName != '{}')  {
                    Resource templateFile = resourceLoader.getResource(templateFileName)
                    if (templateFile != null) {
                        if (!(templateFile.filename == lastTemplateName && templateFile.lastModified() < lastTemplate)) {
                            log.debug("Reload JavascriptTemplate for Hsoy")
                            template = templateFile.inputStream.text
                            lastTemplate = templateFile.lastModified()
                            lastTemplateName = templateFile.filename
                        }
                    } else {
                        log.warn("Template '${templateFileName}' is not exists ot not a file")
                    }
                }
            } catch (Exception e) {
                log.error("Can't process JS template", e)
            }

            try {
                HsoyJsCompiler compiler = new HsoyJsCompiler()

                def w = target.newWriter()
                String compiled = compiler.compileToString(input.text, originalFile.name)
                if (StringUtils.isNotEmpty(template)) {
                    String[] parts = template.split('\\$hsoy\\.body;?')
                    if (parts.size() == 2) {
                        w << parts[0]
                        w << compiled
                        w << parts[1]
                    } else {
                        log.warn("Invalid JS template. Doesn't contain \$hsoy.body")
                        w << '// Invalid JavaScript template for Hsoy Template.\n'
                        w << '// File doesn\'t contain $hsoy.body \n'
                        w << compiled
                    }
                } else {
                    w << compiled
                }
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
