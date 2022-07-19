# yop-java-sdk
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fyop-platform%2Fyop-java-sdk.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fyop-platform%2Fyop-java-sdk?ref=badge_shield)

## 项目结构

本项目使用maven构建，各模块划分如下

### 1. yop-java-sdk:jar

商户可直接引入该包，改包等同于yop-java-sdk-apache，默认采用apache实现与易宝服务的http通信，内置加解密、签名验签等接口，方便商户对接加密机实现，具体请参考在线文档[基于SDK对接加密机](https://open.yeepay.com/docs/open/platform-doc/sdk_guide-sm/encryptor-support)

### 2. yop-java-sdk-apache:jar

同上。

### 3. yop-java-sdk-okhttp:jar

该模块提供okhttp实现与易宝服务的http通信，商户可以根据需要进行选择

### 4. yop-java-sdk-base:jar

该模块提供YopRequest等参数封装、YopRequestConfig请求配置等非加解密相关类，将组装报文，拆解报文等逻辑进行抽象，并提供一些基础工具类，方便商户扩展凭证存储加载器、加密器、签名器等基础接口

### 5. yop-java-sdk-crypto-api:jar

该模块定义了加密器、签名器等基础接口

### 6. yop-java-sdk-crypto-gm-base:jar

该模块提供了国密相关的基础工具类，商户在使用加密机时，可以减少一部分开发量

### 7. yop-java-sdk-crypto-gm:jar

该模块提供了国密加解密与签名的软实现，商户在对接加密机时，可以作为参考

### 8. yop-java-sdk-crypto-inter:jar:国际加解密签名软实现

该模块提供了国际(RSA/AES等)加解密与签名的软实现，目前为国外商户使用

### 9. yop-java-sdk-test:jar

该模块提供了为sdk自身功能、性能测试代码，以及商户可参考的调用示例

## 使用说明

本项目用于对接 YOP 上面的 API。详细说明请参考[**_使用JavaSDK_**](https://open.yeepay.com/docs/platform/sdk_guide/java-sdk-guide)

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
  <version>4.3.1</version>
</dependency>
```

### Gradle users

Add this dependency to your project's build file:

```groovy
compile group: 'com.yeepay.yop.sdk', name: 'yop-java-sdk', version: '4.3.1'
```

## 发版

```
mvn clean -DskipTests release:prepare -Prelease
mvn release:perform -Prelease
```

## License
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fyop-platform%2Fyop-java-sdk.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Fyop-platform%2Fyop-java-sdk?ref=badge_large)