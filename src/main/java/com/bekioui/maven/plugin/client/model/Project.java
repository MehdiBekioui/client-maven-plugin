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
package com.bekioui.maven.plugin.client.model;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.maven.project.MavenProject;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Project {

	public static Builder builder() {
		return new AutoValue_Project.Builder();
	}

	public abstract MavenProject mavenProject();

	public abstract Properties properties();

	public abstract String clientPackageName();

	public abstract String contextPackageName();

	public abstract String apiPackageName();

	public abstract String implPackageName();

	public abstract File clientDirectory();

	public abstract File javaSourceDirectory();

	public abstract List<URL> classpaths();

	public URL[] classPathsArray() {
		return classpaths().toArray(new URL[0]);
	}

	@AutoValue.Builder
	public static abstract class Builder {

		public abstract Builder mavenProject(MavenProject mavenProject);

		public abstract Builder properties(Properties properties);

		public abstract Builder clientPackageName(String clientPackageName);

		public abstract Builder contextPackageName(String contextPackageName);

		public abstract Builder apiPackageName(String apiPackageName);

		public abstract Builder implPackageName(String implPackageName);

		public abstract Builder clientDirectory(File clientDirectory);

		public abstract Builder javaSourceDirectory(File javaSourceDirectory);

		public abstract Builder classpaths(List<URL> classpaths);

		public abstract Project build();

	}

}
