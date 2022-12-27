[![Maven Central](https://img.shields.io/maven-central/v/se.premex.premex-lints/premex-lint-checks)](https://search.maven.org/search?q=g:se.premex.premex-lints%20AND%20a:premex-lint-checks)

# Premex lints

A set of lint checks by Premex


## Block listed APIs [BlockListedApi]

The `BlockListedApi` is a configurable list of APIs that you might want to block developers from
using that you just can't deprecate because they are 3rd party APIs. Maybe you want to stop developer
to use Context#getDrawable() and enforce Context#getDrawableCompat() instead. Or maybe you have 
created some legacy code years ago and want to create a lint check because a 
Deprecation warning is not enough.

Create a [blocklist.xml](samples/blocklist.xml) file in the root of your project. Or configure your own path with lint options if you need the same config file for a multi-module project.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <issue id="BlockListedApi" severity="error">
      <option name="file-block-list" value="blocklist.xml" />
    </issue>
</lint>
```

## How to include in your project

The library is available via MavenCentral:

Add to `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositories {
        // ...
        mavenCentral()
    }
}
```

<details>
<summary>Snapshots of the development version are available in Sonatype's snapshots repository.</summary>
<p>

Add to `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositories {
        // ...
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}
```

Add to `modules/build.gradle.kts`

```kotlin
dependencies {
    lintChecks("se.premex.premex-lints:premex-lint-checks:1.0.0-SNAPSHOT")
}
```

</p>
</details>

Add to `modules/build.gradle.kts`

```kotlin
dependencies {
    lintChecks("se.premex.premex-lints:premex-lint-checks:+")
}
```

License
--------

    Copyright 2022 Premex AB

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.