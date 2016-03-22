# Client Maven Plugin

Basically, when you are developing RESTful APIs, you could need to provide a java client for some java programs. You could need to call some of yours RESTful APIs too (in particular with microservices architecture). Writing a java client is redundant. This plugin generates it for you. It's based on your RESTful service resources.

## Using context

This plugin works with this specific flow:

* Create a pom maven project with maven modules (for example: one for your API and another for your implementation)
* Develop your API
* Declare the plugin in your pom parent
* Run the plugin to generate your client module
* Build your project with maven

## Plugin declaration
> Not yet available

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.bekioui.plugin</groupId>
      <artifactId>client-maven-plugin</artifactId>
	  <version>1.0.0</version>
	  <configuration>
	    <apiUrl>http://localhost:8080</apiUrl>
	    <apiArtifactId>rest-api</apiArtifactId>
	    <clientArtifactId>rest-client</clientArtifactId>
	    <clientPrefix>Rest</clientPrefix>
	    <resourcePackageName>com.rest.api.resource</resourcePackageName>
	  </configuration>
    </plugin>
  </plugins>
</build>
```

## Goal

* `client:generate`: generate the java client

## Configuration

| Property            | Required |                       |
|---------------------|----------|-----------------------|
| apiUrl     		  |	true	 |						 |
| apiArtifactId       | true     |                       |
| clientArtifactId    | true     |                       |
| clientPrefix        | false    | default: empty string |
| resourcePackageName | true	 |                       |

## License
	
	Copyright (C) 2016 Mehdi Bekioui (consulting@bekioui.com)
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
		http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.	