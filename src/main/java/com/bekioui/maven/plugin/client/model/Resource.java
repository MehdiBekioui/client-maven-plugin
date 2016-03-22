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

import java.util.List;

import com.google.auto.value.AutoValue;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

@AutoValue
public abstract class Resource {

    public static Builder builder() {
        return new AutoValue_Resource.Builder();
    }

    public abstract TypeName typeName();

    public abstract String className();

    public abstract String fieldName();

    public abstract List<MethodSpec> methods();

    @AutoValue.Builder
    public static abstract class Builder {

        public abstract Builder typeName(TypeName typeName);

        public abstract Builder className(String className);

        public abstract Builder fieldName(String fieldName);

        public abstract Builder methods(List<MethodSpec> methods);

        public abstract Resource build();

    }

}
