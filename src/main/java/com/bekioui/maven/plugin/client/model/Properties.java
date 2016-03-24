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
public abstract class Properties {

	public static Builder builder() {
		return new AutoValue_Properties.Builder();
	}

	public abstract String apiUrl();

	public abstract String apiArtifactId();

	public abstract String clientArtifactId();

	public abstract String clientPrefix();

	public abstract String resourcePackageName();

	@AutoValue.Builder
	public static abstract class Builder {

		public abstract Builder apiUrl(String apiUrl);

		public abstract Builder apiArtifactId(String apiArtifactId);

		public abstract Builder clientArtifactId(String clientArtifactId);

		public abstract Builder clientPrefix(String clientPrefix);

		public abstract Builder resourcePackageName(String resourcePackageName);

		public abstract Properties build();

	}

}
