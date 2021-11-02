load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "2.8"
RULES_JVM_EXTERNAL_SHA = "79c9850690d7614ecdb72d68394f994fef7534b292c4867ce5e7dec0aa7bdfad"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        "com.google.guava:guava:30.0-jre",
        "commons-codec:commons-codec:1.10",
        "commons-io:commons-io:2.8.0",
        "com.fasterxml.jackson.datatype:jackson-datatype-joda:2.11.2",
        "com.fasterxml.jackson.core:jackson-annotations:2.11.2",
        "com.fasterxml.jackson.core:jackson-core:2.11.2",
        "com.fasterxml.jackson.core:jackson-databind:2.11.2",
        "joda-time:joda-time:2.9.4",
        "org.apache.httpcomponents:httpcore:4.4.13",
        "org.apache.httpcomponents:httpclient:4.5.13",
        "org.apache.httpcomponents:httpmime:4.5.13",
        "org.apache.tika:tika-core:1.22",
        "org.apache.commons:commons-lang3:3.6",
        "org.apache.commons:commons-collections4:4.1",
        "org.springframework:spring-core:5.3.7",
        "org.bouncycastle:bcprov-jdk15on:1.67",
        "org.bouncycastle:bcpkix-jdk15on:1.67",
        "org.slf4j:slf4j-api:1.7.21",
    ],
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
)