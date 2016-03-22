/**
 * Copyright (C) 2016 Mehdi Bekioui (consulting@bekioui.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bekioui.maven.plugin.client.generator;

import static com.bekioui.maven.plugin.client.util.Constants.SRC_FOLDER;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;

import com.bekioui.maven.plugin.client.model.Project;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

public class FileGenerator {

    public static void generate(Project project, String packageName, TypeSpec typeSpec) throws MojoExecutionException {
        try {
            JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
            javaFile.writeTo(new File(project.clientDirectory(), SRC_FOLDER));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate file.", e);
        }
    }

}
