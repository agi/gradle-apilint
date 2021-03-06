/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.apilint

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.javadoc.Javadoc
import org.mozilla.apilint.Config

class ApiCompatLintTask extends Javadoc {
    @OutputFile
    File outputFile

    @Input
    String packageFilter

    @Input
    List<String> skipClassesRegex

    @Input
    String rootDir

    @InputFiles
    List<File> sourcePath

    private final static String CONFIG_NAME = 'apidoc-plugin'

    @TaskAction
    @Override
    protected void generate() {
        // javadoc 8 has a bug that requires the rt.jar file from the JRE to be
        // in the bootclasspath (https://stackoverflow.com/a/30458820).
        options.bootClasspath = [
                project.file("${System.properties['java.home']}/lib/rt.jar")] + project.android.bootClasspath

        def config = project.configurations.findByName(CONFIG_NAME)

        if (config == null) {
            config = project.configurations.create(CONFIG_NAME)
            project.dependencies.add(CONFIG_NAME,
                    "${Config.GROUP}:apidoc-plugin:${Config.API_DOC_VERSION}")
        }

        options.doclet = "org.mozilla.doclet.ApiDoclet"
        options.docletpath = config.files.asType(List)

        options.addStringOption('output', outputFile.absolutePath)
        options.addStringOption('subpackages', packageFilter)
        options.addStringOption('sourcepath', sourcePath.join(':'))
        options.addStringOption('root-dir', rootDir)

        skipClassesRegex.each{ className ->
                options.addStringOption('skip-class-regex', className)
        }

        super.generate()
    }
}
