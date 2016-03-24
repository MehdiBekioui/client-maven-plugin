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
package com.bekioui.maven.plugin.client.initializer;

import static com.bekioui.maven.plugin.client.util.Constants.FULL_SRC_FOLDER;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import com.bekioui.maven.plugin.client.model.Project;
import com.bekioui.maven.plugin.client.model.Properties;

public class ProjectInitializer {

	@SuppressWarnings("unchecked")
	public static Project initialize(MavenProject mavenProject, Properties properties) throws MojoExecutionException {
		String clientPackagename = mavenProject.getParent().getGroupId() + ".client";
		String contextPackageName = clientPackagename + ".context";
		String apiPackageName = clientPackagename + ".api";
		String implPackageName = clientPackagename + ".impl";
		String resourceBasedirPath = mavenProject.getBasedir().getAbsolutePath();
		String projectBasedirPath = resourceBasedirPath.substring(0, resourceBasedirPath.lastIndexOf(File.separator));

		File clientDirectory = new File(projectBasedirPath + File.separator + properties.clientArtifactId());
		if (clientDirectory.exists()) {
			try {
				FileUtils.deleteDirectory(clientDirectory);
			} catch (IOException e) {
				throw new MojoExecutionException("Failed to delete existing client directory.", e);
			}
		}
		clientDirectory.mkdir();

		File javaSourceDirectory = new File(resourceBasedirPath + FULL_SRC_FOLDER + properties.resourcePackageName().replaceAll("\\.", File.separator));
		if (!javaSourceDirectory.isDirectory()) {
			throw new MojoExecutionException("Java sources directory not found: " + javaSourceDirectory.getAbsolutePath());
		}

		List<String> classpathElements;
		try {
			classpathElements = mavenProject.getCompileClasspathElements();
		} catch (DependencyResolutionRequiredException e) {
			throw new MojoExecutionException("Failed to get compile classpath elements.", e);
		}

		List<URL> classpaths = new ArrayList<>();
		for (String element : classpathElements) {
			try {
				classpaths.add(new File(element).toURI().toURL());
			} catch (MalformedURLException e) {
				throw new MojoExecutionException(element + " is an invalid classpath element.", e);
			}
		}

		return Project.builder() //
				.mavenProject(mavenProject) //
				.properties(properties) //
				.clientPackageName(clientPackagename) //
				.contextPackageName(contextPackageName) //
				.apiPackageName(apiPackageName) //
				.implPackageName(implPackageName) //
				.clientDirectory(clientDirectory) //
				.javaSourceDirectory(javaSourceDirectory) //
				.classpaths(classpaths) //
				.build();
	}

}
