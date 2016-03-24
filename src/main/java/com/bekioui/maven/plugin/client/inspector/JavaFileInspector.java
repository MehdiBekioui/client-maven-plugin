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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bekioui.maven.plugin.client.model.JavaSourceFile;
import com.bekioui.maven.plugin.client.model.Project;

public class JavaFileInspector {

	private static final String JAVA_EXENSION = ".java";

	public static List<JavaSourceFile> inspect(Project project, String packageName) {
		return inspect(project.javaSourceDirectory(), packageName);
	}

	private static List<JavaSourceFile> inspect(File folder, String packageName) {
		List<JavaSourceFile> javaSourceFiles = new ArrayList<>();

		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				javaSourceFiles.addAll(inspect(file, packageName + "." + file.getName()));
			} else {
				boolean isJavaFile = false;
				String path = file.getAbsolutePath();
				isJavaFile = JAVA_EXENSION.equals(path.substring(path.lastIndexOf("."), path.length()));
				if (isJavaFile) {
					String name = path.substring(path.lastIndexOf(File.separator) + 1, path.length() - JAVA_EXENSION.length());
					javaSourceFiles.add(JavaSourceFile.builder() //
							.packageName(packageName) //
							.name(name) //
							.path(packageName + "." + name) //
							.build());
				}
			}
		}

		return javaSourceFiles;
	}

}
