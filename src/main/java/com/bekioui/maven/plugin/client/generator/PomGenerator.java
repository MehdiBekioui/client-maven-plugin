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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecutionException;

import com.bekioui.maven.plugin.client.model.Project;

public class PomGenerator {

	private static final String POM_FILE_NAME = "pom.xml";

	public static void generate(Project project) throws MojoExecutionException {
		boolean overrideParent = false;
		Model parentModel = project.mavenProject().getParent().getOriginalModel();
		if (!parentModel.getModules().contains(project.properties().clientArtifactId())) {
			overrideParent = true;
			parentModel.getModules().add(project.properties().clientArtifactId());
		}

		Model model = new Model();
		model.setModelVersion("4.0.0");

		Parent parent = new Parent();
		parent.setGroupId(project.mavenProject().getParent().getGroupId());
		parent.setArtifactId(project.mavenProject().getParent().getArtifactId());
		parent.setVersion(project.mavenProject().getParent().getVersion());
		model.setParent(parent);

		model.setArtifactId(project.properties().clientArtifactId());

		List<Dependency> dependencies = new ArrayList<>();

		Dependency apiDependency = new Dependency();
		apiDependency.setGroupId(project.mavenProject().getGroupId());
		apiDependency.setArtifactId(project.mavenProject().getArtifactId());
		apiDependency.setVersion(project.mavenProject().getVersion());
		dependencies.add(apiDependency);

		Dependency springContextDependency = new Dependency();
		springContextDependency.setGroupId("org.springframework");
		springContextDependency.setArtifactId("spring-context");
		springContextDependency.setVersion("4.2.5.RELEASE");
		dependencies.add(springContextDependency);

		Dependency jaxrsClientDependency = new Dependency();
		jaxrsClientDependency.setGroupId("com.bekioui.jaxrs");
		jaxrsClientDependency.setArtifactId("jaxrs-client");
		jaxrsClientDependency.setVersion("1.0.0");
		dependencies.add(jaxrsClientDependency);

		model.setDependencies(dependencies);

		try {
			MavenXpp3Writer writer = new MavenXpp3Writer();
			if (overrideParent) {
				writer.write(new FileWriter(new File(parentModel.getProjectDirectory(), POM_FILE_NAME)), parentModel);
			}
			writer.write(new FileWriter(new File(project.clientDirectory(), POM_FILE_NAME)), model);
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to create client pom.xml file.", e);
		}
	}
}
