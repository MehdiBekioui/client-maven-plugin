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
package com.bekioui.maven.plugin.client;

import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.bekioui.maven.plugin.client.generator.ClientConfigGenerator;
import com.bekioui.maven.plugin.client.generator.ClientFactoryGenerator;
import com.bekioui.maven.plugin.client.generator.ClientGenerator;
import com.bekioui.maven.plugin.client.generator.ContextGenerator;
import com.bekioui.maven.plugin.client.generator.PomGenerator;
import com.bekioui.maven.plugin.client.generator.ResteasyClientFactoryGenerator;
import com.bekioui.maven.plugin.client.initializer.ProjectInitializer;
import com.bekioui.maven.plugin.client.inspector.JavaFileInspector;
import com.bekioui.maven.plugin.client.inspector.ResourceInspector;
import com.bekioui.maven.plugin.client.model.ContextResource;
import com.bekioui.maven.plugin.client.model.JavaSourceFile;
import com.bekioui.maven.plugin.client.model.Project;
import com.bekioui.maven.plugin.client.model.Properties;
import com.bekioui.maven.plugin.client.model.Resource;
import com.squareup.javapoet.TypeName;

@Mojo(name = "generate")
public class ClientMojo extends AbstractMojo {

	@Parameter(property = "client.apiUrl", required = true)
	public String apiUrl;

	@Parameter(property = "client.apiArtifactId", required = true)
	public String apiArtifactId;

	@Parameter(property = "client.clientArtifactId", required = true)
	public String clientArtifactId;

	@Parameter(property = "client.clientPrefix")
	public String clientPrefix = new String();

	@Parameter(property = "client.resourcePackageName", required = true)
	public String resourcePackageName;

	@Component
	private MavenProject mavenProject;

	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws MojoExecutionException {
		if (!mavenProject.getArtifactId().equals(apiArtifactId)) {
			return;
		}

		if (mavenProject.getParent() == null) {
			throw new MojoExecutionException("API has to be a maven module with parent pom.");
		}

		List<Plugin> plugins = mavenProject.getParent().getBuildPlugins();
		for (Plugin plugin : plugins) {
			if (!plugin.getGroupId().equals("com.bekioui.plugin") || !plugin.getArtifactId().equals("client-maven-plugin")) {
				continue;
			}
			if (!plugin.getExecutions().isEmpty()) {
				throw new MojoExecutionException("client-maven-plugin has to not contain execution configuration.");
			}
			break;
		}

		Properties properties = Properties.builder() //
				.apiUrl(apiUrl) //
				.apiArtifactId(apiArtifactId) //
				.clientArtifactId(clientArtifactId) //
				.clientPrefix(clientPrefix) //
				.resourcePackageName(resourcePackageName) //
				.build();

		Project project = ProjectInitializer.initialize(mavenProject, properties);
		PomGenerator.generate(project);
		ClientConfigGenerator.generate(project);
		TypeName resteasyClientFactoryType = ResteasyClientFactoryGenerator.generate(project);
		List<JavaSourceFile> javaSourceFiles = JavaFileInspector.inspect(project, resourcePackageName);
		List<Resource> resources = ResourceInspector.inspect(project, javaSourceFiles);
		TypeName clientFactoryType = ClientFactoryGenerator.generate(project, resources, resteasyClientFactoryType);
		List<ContextResource> contextResources = ContextGenerator.generate(project, resources, clientFactoryType);
		ClientGenerator.generate(project, contextResources, clientFactoryType);
	}

}
