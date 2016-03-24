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

import static com.bekioui.maven.plugin.client.generator.ClientFactoryGenerator.getResourceMethodName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import org.apache.maven.plugin.MojoExecutionException;

import com.bekioui.maven.plugin.client.model.ContextResource;
import com.bekioui.maven.plugin.client.model.Project;
import com.bekioui.maven.plugin.client.model.Resource;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class ContextGenerator {

	private static final String CONTEXT_SUFFIX = "Context";

	public static List<ContextResource> generate(Project project, List<Resource> resources, TypeName clientFactoryType) throws MojoExecutionException {
		try {
			List<ContextResource> contextResources = new ArrayList<>(resources.size());

			for (Resource resource : resources) {
				String className = resource.className() + CONTEXT_SUFFIX;
				contextResources.add(ContextResource.create(ClassName.get(project.contextPackageName(), className).box(), className, resource));

				FieldSpec resourceField = FieldSpec.builder(resource.typeName(), resource.fieldName()) //
						.addModifiers(Modifier.PRIVATE, Modifier.FINAL) //
						.build();

				MethodSpec constructor = MethodSpec.constructorBuilder() //
						.addModifiers(Modifier.PUBLIC) //
						.addParameter(ParameterSpec.builder(clientFactoryType, "clientFactory").build()) //
						.addStatement("this." + resource.fieldName() + " = clientFactory." + getResourceMethodName(resource) + "()") //
						.build();

				TypeSpec clazz = TypeSpec.classBuilder(className) //
						.addModifiers(Modifier.PUBLIC) //
						.addField(resourceField) //
						.addMethod(constructor) //
						.addMethods(resource.methods()) //
						.build();

				FileGenerator.generate(project, project.contextPackageName(), clazz);
			}

			return contextResources;
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to generate context classes.", e);
		}
	}
}
