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

import javax.lang.model.element.Modifier;

import org.apache.maven.plugin.MojoExecutionException;

import com.bekioui.maven.plugin.client.model.Project;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

public class ClientConfigGenerator {

	private static final String SPRING_ANNOTATION_PACKAGE_NAME = "org.springframework.context.annotation";

	public static void generate(Project project) throws MojoExecutionException {
		TypeSpec clazz = TypeSpec.interfaceBuilder(project.properties().clientPrefix() + "ClientConfig") //
				.addModifiers(Modifier.PUBLIC) //
				.addAnnotation(ClassName.get(SPRING_ANNOTATION_PACKAGE_NAME, "Configuration")) //
				.addAnnotation(ClassName.get(SPRING_ANNOTATION_PACKAGE_NAME, "ComponentScan")) //
				.build();

		FileGenerator.generate(project, project.clientPackageName(), clazz);
	}

}
