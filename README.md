# yop-java-sdk
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fyop-platform%2Fyop-java-sdk.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fyop-platform%2Fyop-java-sdk?ref=badge_shield)


## Usage

本项目用于对接 YOP 上面的 API。

### 单商编推荐配置

SDK 默认从路径(相对 classPath) /config/yop_sdk_config_default.json 读取配置文件。
只需要修改该配置文件中的app_key、isv_private_key等参数后保存到对应目录即可。

如需修改读取路径可以在 VM arguments 中指定配置文件路径，示例如下：

````
mac/linux:-Dyop.sdk.config.file=file:///home/app/yop_sdk_config_default.json
windows:-Dyop.sdk.config.file=file:///D:\workspace..\config\yop_sdk_config_default.json
````

常见容器修改 JVM 参数的方式如下，仅供参考：

````
tomcat: 两种方式
在 $CATALINA_HOME/bin/目录下添加 setenv.sh，在 set env.sh 中添加 JAVA_OPTS="$JAVA_OPTS -Dyop.sdk.config.file=file:///home/app/yop_sdk_config_default.json"
在 $CATALINA_HOME/bin/catalina.sh 中直接添加 JAVA_OPTS="$JAVA_OPTS -Dyop.sdk.config.file=file:///home/app/yop_sdk_config_default.json"

jetty: 在 $jetty_home/bin/jetty.sh 中添加 JAVA_OPTIONS="-Dyop.sdk.config.file=file:///home/app/yop_sdk_config_default.json"

weblogic: 修改 user_projects\domains\base_domain\bin 下的 startWebLogic.cmd 文件，添加 set JAVA_OPTIONS=% JAVA_OPTIONS % -Dyop.sdk.config.file=file:///home/app/yop_sdk_config_default.json
````

### 多商编（平台商、系统商等）配置方式

可以自行实现凭证提供方（例如继承：YopCachedCredentialsProvider、YopFixedCredentialsProvider）来加载 appkey、私钥的对应关系。

## Requirements

Building the API client library requires:
1. Java 1.8+
2. Maven/Gradle

## Installation

To install the API client library to your local Maven repository, simply execute:

```shell
mvn clean install
```

To deploy it to a remote Maven repository instead, configure the settings of the repository and execute:

```shell
mvn clean deploy
```

### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
  <groupId>com.yeepay.yop.sdk</groupId>
  <artifactId>yop-java-sdk</artifactId>
  <version>4.1.5</version>
</dependency>
```

### Gradle users

Add this dependency to your project's build file:

```groovy
compile group: 'com.yeepay.yop.sdk', name: 'yop-java-sdk', version: '4.1.4'
```

## 发版

```
mvn clean -DskipTests release:prepare -Prelease
mvn release:perform -Prelease
```

## License
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fyop-platform%2Fyop-java-sdk.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Fyop-platform%2Fyop-java-sdk?ref=badge_large)