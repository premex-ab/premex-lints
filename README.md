# Premex lints

A set of lint checks by Premex

## How to include in your project

The library is available via MavenCentral:

```groovy
allprojects {
    repositories {
        // ...
        mavenCentral()
    }
}
```

<details>
<summary>Snapshots of the development version are available in Sonatype's snapshots repository.</summary>
<p>

```groovy
repositories {
    // ...
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}
```
```groovy
dependencies {
    lintChecks("se.premex:premex-lint-checks:1.0.0-SNAPSHOT")
}
```

</p>
</details>

Add it to your module dependencies:

```
dependencies {
    lintChecks("se.premex:premex-lint-checks:<latest_version>")
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