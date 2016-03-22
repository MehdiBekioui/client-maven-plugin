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

import static com.bekioui.maven.plugin.client.generator.ResteasyClientFactoryGenerator.CLASS_NAME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.lang.model.element.Modifier;

import org.apache.maven.plugin.MojoExecutionException;

import com.bekioui.maven.plugin.client.model.Project;
import com.bekioui.maven.plugin.client.model.Resource;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class ClientFactoryGenerator {

    private static final String INTERFACE_BASE_NAME = "ClientFactory";

    private static final String FIELD_PREFIX = "this.";

    private static final String METHOD_PREFIX = "get";

    private static final String RESTEASY_CLIENT_FACTORY_FIELD = "resteasyClientFactory";

    public static TypeName generate(Project project, List<Resource> resources, TypeName resteasyClientFactoryType) throws MojoExecutionException {
        try {
            String interfaceName = project.properties().clientPrefix() + INTERFACE_BASE_NAME;
            TypeName interfaceType = ClassName.get(project.apiPackageName(), interfaceName).box();
            Map<Resource, String> resourceToMethodName = createInterface(project, resources, interfaceName);
            createImplementation(project, resources, resteasyClientFactoryType, interfaceType, resourceToMethodName);
            return interfaceType;
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate client factory.", e);
        }
    }

    public static String getResourceMethodName(Resource resource) {
        return METHOD_PREFIX + resource.className();
    }

    private static Map<Resource, String> createInterface(Project project, List<Resource> resources, String interfaceName) throws MojoExecutionException {
        Map<Resource, String> resourceToMethodName = new HashMap<>();

        List<MethodSpec> methods = new ArrayList<>();
        for (Resource resource : resources) {
            String methodName = getResourceMethodName(resource);
            resourceToMethodName.put(resource, methodName);
            methods.add(MethodSpec.methodBuilder(methodName) //
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT) //
                    .returns(resource.typeName()) //
                    .build());
        }

        TypeSpec clazz = TypeSpec.interfaceBuilder(interfaceName) //
                .addModifiers(Modifier.PUBLIC) //
                .addMethods(methods) //
                .build();

        FileGenerator.generate(project, project.apiPackageName(), clazz);

        return resourceToMethodName;
    }

    private static void createImplementation(Project project, List<Resource> resources, TypeName resteasyClientFactoryType, TypeName interfaceType, //
            Map<Resource, String> resourceToMethodName) throws MojoExecutionException {
        List<FieldSpec> fields = new ArrayList<>();
        List<MethodSpec> methods = new ArrayList<>();
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

        fields.add(FieldSpec.builder(resteasyClientFactoryType, RESTEASY_CLIENT_FACTORY_FIELD, Modifier.PRIVATE).build());
        codeBlockBuilder.addStatement(FIELD_PREFIX + RESTEASY_CLIENT_FACTORY_FIELD + " = new " + CLASS_NAME + "(\"" + project.properties().apiUrl() + "\")");

        for (Resource resource : resources) {
            fields.add(FieldSpec.builder(resource.typeName(), resource.fieldName(), Modifier.PRIVATE).build());
            codeBlockBuilder.addStatement(FIELD_PREFIX + resource.fieldName() + " = " + RESTEASY_CLIENT_FACTORY_FIELD + ".create(" + resource.className() + ".class)");
            methods.add(MethodSpec.methodBuilder(resourceToMethodName.get(resource)) //
                    .addModifiers(Modifier.PUBLIC) //
                    .addAnnotation(Override.class) //
                    .returns(resource.typeName()) //
                    .addStatement("return " + resource.fieldName()) //
                    .build());
        }

        MethodSpec postConstruct = MethodSpec.methodBuilder("postConstruct") //
                .addModifiers(Modifier.PRIVATE) //
                .addAnnotation(PostConstruct.class) //
                .addCode(codeBlockBuilder.build()) //
                .build();

        TypeSpec clazz = TypeSpec.classBuilder(project.properties().clientPrefix() + "ClientFactoryImpl") //
                .addSuperinterface(interfaceType) //
                .addModifiers(Modifier.PUBLIC) //
                .addAnnotation(ClassName.get("org.springframework.stereotype", "Component")) //
                .addFields(fields) //
                .addMethod(postConstruct) //
                .addMethods(methods) //
                .build();

        FileGenerator.generate(project, project.implPackageName(), clazz);
    }

}
