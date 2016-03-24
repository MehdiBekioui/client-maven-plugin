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
package com.bekioui.maven.plugin.client.inspector;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import javax.lang.model.element.Modifier;

import org.apache.maven.plugin.MojoExecutionException;

import com.bekioui.maven.plugin.client.model.JavaSourceFile;
import com.bekioui.maven.plugin.client.model.Project;
import com.bekioui.maven.plugin.client.model.Resource;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

public class ResourceInspector {

	private static final String ARGUMENT_NAME = "arg";

	public static List<Resource> inspect(Project project, List<JavaSourceFile> javaSourceFiles) throws MojoExecutionException {
		try (URLClassLoader classLoader = new URLClassLoader(project.classPathsArray())) {
			List<Resource> resources = new ArrayList<>();

			for (JavaSourceFile javaSourceFile : javaSourceFiles) {
				Class<?> resourceClass = classLoader.loadClass(javaSourceFile.path());

				if (!resourceClass.isInterface()) {
					continue;
				}

				String resourceFieldName = resourceClass.getSimpleName().substring(0, 1).toLowerCase().concat(resourceClass.getSimpleName().substring(1));

				List<MethodSpec> methods = new ArrayList<>();
				for (Method method : resourceClass.getMethods()) {
					StringJoiner arguments = new StringJoiner(", ", "(", ")");
					List<ParameterSpec> parameters = new ArrayList<>();
					int count = 0;

					for (Type type : method.getGenericParameterTypes()) {
						String argument = ARGUMENT_NAME + count++;
						arguments.add(argument);
						parameters.add(ParameterSpec.builder(type, argument).build());
					}

					String statement = (method.getReturnType().equals(void.class) ? "" : "return ") + resourceFieldName + "." + method.getName() + arguments.toString();

					methods.add(MethodSpec.methodBuilder(method.getName()) //
							.addModifiers(Modifier.PUBLIC) //
							.returns(method.getGenericReturnType()) //
							.addParameters(parameters) //
							.addStatement(statement) //
							.build());
				}

				resources.add(Resource.builder() //
						.className(resourceClass.getSimpleName())//
						.fieldName(resourceFieldName) //
						.typeName(ClassName.get(resourceClass).box()) //
						.methods(methods) //
						.build());
			}

			return resources;
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to initialize class loader.", e);
		} catch (ClassNotFoundException e) {
			throw new MojoExecutionException("Failed to load class.", e);
		}
	}

}
