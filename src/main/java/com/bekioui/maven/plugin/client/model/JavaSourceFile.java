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

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class JavaSourceFile {

	public static Builder builder() {
		return new AutoValue_JavaSourceFile.Builder();
	}

	public abstract String packageName();

	public abstract String name();

	public abstract String path();

	@AutoValue.Builder
	public static abstract class Builder {

		public abstract Builder packageName(String packageName);

		public abstract Builder name(String name);

		public abstract Builder path(String path);

		public abstract JavaSourceFile build();

	}

}
