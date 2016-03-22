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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import javax.lang.model.element.Modifier;

import org.apache.maven.plugin.MojoExecutionException;

import com.bekioui.maven.plugin.client.model.Project;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import edu.emory.mathcs.backport.java.util.Arrays;

public class ResteasyClientFactoryGenerator {

    public static final String CLASS_NAME = "ResteasyClientFactory";

    private static final String URI_FIELD = "uri";

    private static final String CLIENT_HTTP_ENGINE_FIELD = "clientHttpEngine";

    private static final String INVOCATION_HANDLER_CLASS = "SimpleInvocationHandler";

    private static final String DELEGATE_FIELD = "delegate";

    private static final String RESTEASY_CLIENT_PACKAGE_NAME = "org.jboss.resteasy.client.jaxrs";

    private static final String APACHE_HTTP_PACKAGE_NAME = "org.apache.http.impl";

    private static final String JAVA_LANG_REFLECT_PACKAGE_NAME = "java.lang.reflect";

    private static final ClassName CLIENT_HTTP_ENGINE_CLASS_NAME = ClassName.get(RESTEASY_CLIENT_PACKAGE_NAME, "ClientHttpEngine");

    private static final ClassName RESTEASY_CLIENT_CLASS_NAME = ClassName.get(RESTEASY_CLIENT_PACKAGE_NAME, "ResteasyClient");

    private static final ClassName RESTEASY_CLIENT_BUILDER_CLASS_NAME = ClassName.get(RESTEASY_CLIENT_PACKAGE_NAME, "ResteasyClientBuilder");

    private static final ClassName RESTEASY_WEB_TARGET_CLASS_NAME = ClassName.get(RESTEASY_CLIENT_PACKAGE_NAME, "ResteasyWebTarget");

    private static final ClassName APACHE_HTTP_CLIENT_CLASS_NAME = ClassName.get(RESTEASY_CLIENT_PACKAGE_NAME + ".engines", "ApacheHttpClient4Engine");

    private static final ClassName HTTP_CLIENT_BUILDER_CLASS_NAME = ClassName.get(APACHE_HTTP_PACKAGE_NAME + ".client", "HttpClientBuilder");

    private static final ClassName POOLING_HTTP_CLIENT_CLASS_NAME = ClassName.get(APACHE_HTTP_PACKAGE_NAME + ".conn", "PoolingHttpClientConnectionManager");

    private static final ClassName PROXY_CLASS_NAME = ClassName.get(JAVA_LANG_REFLECT_PACKAGE_NAME, "Proxy");

    private static final ClassName INVOCATION_TARGET_EXCEPTION_CLASS_NAME = ClassName.get(JAVA_LANG_REFLECT_PACKAGE_NAME, "InvocationTargetException");

    public static TypeName generate(Project project) throws MojoExecutionException {
        try {
            TypeSpec clazz = TypeSpec.classBuilder(CLASS_NAME) //
                    .addModifiers(Modifier.PUBLIC) //
                    .addFields(fields()) //
                    .addMethod(constructor()) //
                    .addMethod(create()) //
                    .addType(invocationHandler()) //
                    .build();

            String packageName = project.corePackageName();
            FileGenerator.generate(project, packageName, clazz);

            return ClassName.get(packageName, CLASS_NAME).box();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate Resteasy client factory.", e);
        }
    }

    private static List<FieldSpec> fields() {
        FieldSpec uriField = FieldSpec.builder(String.class, URI_FIELD, Modifier.PRIVATE, Modifier.FINAL).build();
        FieldSpec clientHttpEngineField = FieldSpec.builder(CLIENT_HTTP_ENGINE_CLASS_NAME, CLIENT_HTTP_ENGINE_FIELD, Modifier.PRIVATE, Modifier.FINAL).build();
        return Arrays.asList(new FieldSpec[] { uriField, clientHttpEngineField });
    }

    private static MethodSpec constructor() {
        String statement1 = "this." + URI_FIELD + " = " + URI_FIELD;
        String statement2 = "this." + CLIENT_HTTP_ENGINE_FIELD + " = new $T($T.create() //" //
                + "\n.setConnectionManager(new $T()) //" //
                + "\n.build())";

        return MethodSpec.constructorBuilder() //
                .addModifiers(Modifier.PUBLIC) //
                .addParameter(ParameterSpec.builder(String.class, URI_FIELD).build()) //
                .addStatement(statement1) //
                .addStatement(statement2, APACHE_HTTP_CLIENT_CLASS_NAME, HTTP_CLIENT_BUILDER_CLASS_NAME, POOLING_HTTP_CLIENT_CLASS_NAME) //
                .build();
    }

    private static <T> MethodSpec create() {
        TypeVariableName generic = TypeVariableName.get("T");
        TypeName classOfGeneric = ParameterizedTypeName.get(ClassName.get("java.lang", "Class"), generic);
        String statement1 = "$T resteasyClient = new $T().httpEngine(" + CLIENT_HTTP_ENGINE_FIELD + ").build()";
        String statement2 = "$T resteasyWebTarget = resteasyClient.target(" + URI_FIELD + ")";
        String returnStatement = "return (T) $T.newProxyInstance(resteasyWebTarget.getClass().getClassLoader(), new Class[] { clazz }, //" //
                + "\nnew " + INVOCATION_HANDLER_CLASS + "(resteasyWebTarget.proxy(clazz)))";

        return MethodSpec.methodBuilder("create") //
                .addModifiers(Modifier.PUBLIC) //
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "\"unchecked\"").build()) //
                .addTypeVariable(generic) //
                .returns(generic) //
                .addParameter(ParameterSpec.builder(classOfGeneric, "clazz").build()) //
                .addStatement(statement1, RESTEASY_CLIENT_CLASS_NAME, RESTEASY_CLIENT_BUILDER_CLASS_NAME) //
                .addStatement(statement2, RESTEASY_WEB_TARGET_CLASS_NAME) //
                .addStatement(returnStatement, PROXY_CLASS_NAME) //
                .build();
    }

    private static TypeSpec invocationHandler() {
        FieldSpec field = FieldSpec.builder(Object.class, "delegate", Modifier.PRIVATE, Modifier.FINAL).build();

        MethodSpec constructor = MethodSpec.constructorBuilder() //
                .addModifiers(Modifier.PUBLIC) //
                .addParameter(ParameterSpec.builder(Object.class, DELEGATE_FIELD).build()) //
                .addStatement("this." + DELEGATE_FIELD + " = " + DELEGATE_FIELD) //
                .build();

        List<ParameterSpec> parameters = Arrays.asList(new ParameterSpec[] { //
                ParameterSpec.builder(Object.class, "proxy").build(), //
                        ParameterSpec.builder(Method.class, "method").build(), //
                        ParameterSpec.builder(Object[].class, "args").build() //
                });

        CodeBlock codeBlock = CodeBlock.builder() //
                .add("try {") //
                .addStatement("\nreturn method.invoke(" + DELEGATE_FIELD + ", args)") //
                .add("} catch ($T e) {", INVOCATION_TARGET_EXCEPTION_CLASS_NAME) //
                .addStatement("\nthrow e.getCause()") //
                .add("}\n") //
                .build();

        MethodSpec invoke = MethodSpec.methodBuilder("invoke") //
                .addModifiers(Modifier.PUBLIC) //
                .addAnnotation(Override.class) //
                .returns(Object.class) //
                .addParameters(parameters) //
                .addException(Throwable.class) //
                .addCode(codeBlock) //
                .build();

        return TypeSpec.classBuilder(INVOCATION_HANDLER_CLASS) //
                .addModifiers(Modifier.PRIVATE) //
                .addSuperinterface(InvocationHandler.class) //
                .addField(field) //
                .addMethod(constructor) //
                .addMethod(invoke) //
                .build();
    }

}
