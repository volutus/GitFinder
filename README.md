# GitFinder
Simple utility to find all Git repos on the local machine. 

It should be Windows and Linux friendly but the path exclusion logic may need some formalization if this usage becomes widespread.


```bash
Scanned 230865 directories in 2187ms
Repo exists at /home/volutus/IdeaProjects/GitFinder/.git
...
Number of repos found - 8
```

## Gradle

I've never used Gradle before and this project is dead simple, but it needed a build system for packaging.

To run the JAR build, just use `gradlew clean jar`

You might need to use a fat JAR plugin if the application gets more complicated.
There are several but it looks like [Shadow](https://plugins.gradle.org/plugin/com.gradleup.shadow) is the most official.


