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

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import org.apache.maven.plugin.MojoExecutionException;

import com.bekioui.maven.plugin.client.model.ContextResource;
import com.bekioui.maven.plugin.client.model.Project;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class ClientGenerator {

    private static final String INTERFACE_BASE_NAME = "Client";

    private static final String CLIENT_FACTORY_FIELD = "clientFactory";

    public static void generate(Project project, List<ContextResource> contextResources, TypeName clientFactoryType) throws MojoExecutionException {
        try {
            String interfaceName = project.properties().clientPrefix() + INTERFACE_BASE_NAME;
            TypeName interfaceType = ClassName.get(project.apiPackageName(), interfaceName).box();
            createInterface(project, contextResources, interfaceName);
            createImplementation(project, contextResources, clientFactoryType, interfaceType);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate client.", e);
        }
    }

    private static void createInterface(Project project, List<ContextResource> contextResources, String interfaceName) throws MojoExecutionException {
        List<MethodSpec> methods = new ArrayList<>();
        for (ContextResource contextResource : contextResources) {
            methods.add(MethodSpec.methodBuilder(contextResource.resource().fieldName()) //
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT) //
                    .returns(contextResource.typeName()) //
                    .build());
        }

        TypeSpec clazz = TypeSpec.interfaceBuilder(interfaceName) //
                .addModifiers(Modifier.PUBLIC) //
                .addMethods(methods) //
                .build();

        FileGenerator.generate(project, project.apiPackageName(), clazz);
    }

    private static void createImplementation(Project project, List<ContextResource> contextResources, TypeName clientFactoryType, TypeName interfaceType) //
            throws MojoExecutionException {
        FieldSpec field = FieldSpec.builder(clientFactoryType, CLIENT_FACTORY_FIELD) //
                .addModifiers(Modifier.PRIVATE) //
                .addAnnotation(ClassName.get("org.springframework.beans.factory.annotation", "Autowired")) //
                .build();

        List<MethodSpec> methods = new ArrayList<>();
        for (ContextResource contextResource : contextResources) {
            methods.add(MethodSpec.methodBuilder(contextResource.resource().fieldName()) //
                    .addModifiers(Modifier.PUBLIC) //
                    .addAnnotation(Override.class) //
                    .returns(contextResource.typeName()) //
                    .addStatement("return new " + contextResource.className() + "(" + CLIENT_FACTORY_FIELD + ")") //
                    .build());
        }

        TypeSpec clazz = TypeSpec.classBuilder(project.properties().clientPrefix() + "ClientImpl") //
                .addSuperinterface(interfaceType) //
                .addModifiers(Modifier.PUBLIC) //
                .addAnnotation(ClassName.get("org.springframework.stereotype", "Component")) //
                .addField(field) //
                .addMethods(methods) //
                .build();

        FileGenerator.generate(project, project.implPackageName(), clazz);
    }

}
