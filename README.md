## Java版SDK使用指引

[易宝开放平台](https://open.yeepay.com/) 的SDK是由程序自动化生成的代码包，其中包含了构建请求、加密、返回解析等一些必要的功能。

* 支持java sdk支持1.5及以上（目前仅支持j2se标准java平台使用，不适合android平台）

## 1.准备工作

注册成为开放平台开发者并创建应用。

* 如果未注册，请访问[易宝开放平台](https://open.yeepay.com/)注册并创建应用。
* 如果是易宝商户，请从 [易宝商户后台](https://mp.yeepay.com/auth/signin) 登录。

## 2.下载SDK包

将下载好的压缩包解压，内容如下：

````
+- docs                            // 其他文档或工具
|  +- tz.jsp                       // 环境探针
+- LICENSE                         // 授权协议，请勿删除
+- pom.xml                         // maven 依赖管理文件
+- README.md                       // README 文档
+- yop-java-sdk-${version}-jdk18.jar    // All in One for JDK 1.8，已将lib目录shade打包
+- yop-java-sdk-${version}-jdk17.jar    // All in One for JDK 1.7，已将lib目录shade打包
+- yop-java-sdk-${version}-jdk16.jar    // All in One for JDK 1.6，已将lib目录shade打包
+- yop-java-sdk-${version}.jar          // 未将lib目录shade打包的原始jar包
+- yop-java-sdk-${version}-sources.jar  // 源码包
+- yop-java-sdk-${version}-tests.jar    // 单元测试
+- yop_sdk_config_default.json     // 默认用配置文件
+- yop_sdk_config_template.json    // 配置文件模版
+- lib                             // 依赖包，采用All in One时请忽略本目录
````

### 2.1 发布SDK包到私有仓库

参考 [发布第三方jar包到远程仓库](https://maven.apache.org/guides/mini/guide-3rd-party-jars-remote.html)，示例如下：

````xml
mvn deploy:deploy-file -Dversion=3.3.15 -DgroupId=com.yeepay.g3.yop -DartifactId=yop-java-sdk -Dpackaging=jar -Dfile=target/yop-java-sdk-3.3.7.jar -DpomFile=pom.xml -Durl=http://xxxx:8000/artifactory/xxx-release/ -DrepositoryId=xxx
````

pom引用方法如下：

````xml
<dependency>
	<groupId>com.yeepay.g3.yop</groupId>
	<artifactId>yop-java-sdk</artifactId>
	<version>${具体版本见SDK包}</version>
</dependency>
````

### 2.2. 配置文件说明

````yaml
- app_key：应用标识
- aes_secret_key：应用密钥。当认证鉴权机制为RSA时请忽略该项；当认证鉴权机制为对称、Basic时需要提供
- server_root: 同步API请求地址。默认值：https://openapi.yeepay.com/yop-center
- yos_server_root: YOS API 请求地址（选填，当有调用）。默认值：https://yos.yeepay.com/yop-center
- sandbox_server_root: 沙箱环境请求地址(选填)。默认值：https://sandbox.yeepay.com/yop-center
- yop_public_key（list）：YOP平台公钥，当认证鉴权机制为非对称时需要提供，不需要修改
	- store_type
    - string：密钥文本
    - file_p12：p12格式的密钥文件
    - cert_type: RSA2048/RSA4096
    - value: 如果store_type为string，则该值为密钥文本；如果store_type为file_*，则该值为密钥文件路径

- isv_private_key（list）：ISV 私钥，当认证鉴权机制为非对称时需要提供
    - store_type
    - string：密钥文本
    - file_p12：p12格式的密钥文件
    - cert_type: RSA2048/RSA4096
    - value: 如果store_type为string，则该值为密钥文本；如果store_type为file_*，则该值为密钥文件路径
    - password：如果p12需要密码
- encryptKey:加密密钥(目前仅支持aes256)，sdk在https协议之上支持额外的加密需求，这里配置的必须是用户在yop平台报备的加密密钥
- httpclient:
    - connect_timeout：全局连接超时，默认值：30*1000
    - read_timeout：全局读取超时时间，默认值：60*1000
    - max_conn_total: 最大连接数，默认值：200
    - max_conn_per_route: 最大连接数，默认值：100
- proxy:
    - host: 代理服务器IP
    - port:  代理服务器端口，默认值：-1
    - username: 代理账号
    - password: 代理密码
    - domain: 代理域
    - workstation: 代理工作站
- protocol_version：协议版本，默认：yop-auth-v2
- config_version: 配置文件版本
````

## 3. 对接第一个接口

### 3.1. 了解业务逻辑

请在文档中心了解业务模型，各接口交互规范，明确需要对接的具体接口。

### 3.2. 查看API文档

了解API的请求地址、签名算法、请求参数、响应参数、[平台错误码](https://open.yeepay.com/docs/platform_profile/error_code)、业务错误码、异步通知接口。

### 3.3. 对接非对称签名接口

代码示例：

````java
// 发起调用
YopRequest request = new YopRequest();
// YopRequest request = new YopRequest("<Your appKey>", "<Your appSecret>");//针对代理商模式的一种解决方案，其他方案请参考后续章节

// 演示普通参数传递
request.addParam("address", "13812345678");

// 演示本地文件参数传递
request.addFile(new File("src/test/resources/log4j.xml"));

// 演示本地文件流参数传递
FileInputStream stream = new FileInputStream(new File("/Users/dreambt/SiteMesh Flow Diagram.png"));
request.addFile(stream);

// 演示远程文件参数传递
request.addFile(new URL("https://www.yeepay.com/logo.png").openStream());

YopResponse response = YopRsaClient.post("/rest/v1.0/notifier/send", request);
````

###3.4. 请求加密
部分api的安全需求需要加密，用户在请求这些api的时候必须开启加密功能：
* 按照SDK配置文件的说明配置好加密密钥
* 按照以下示例代码设置加密选项
	```
	YopRequest request = new YopRequest();
	request.setNeedEncrypt(true);
	```
* sdk会自动读取加密密钥并对请求报文进行加密，同时网关在处理完加密请求以后会加密返回结果，sdk端解析返回结果时自动解密
注意：如果用户没有使用配置文件来初始化sdk配置的方式，采用下述方法配置密钥和请求加密
```
YopRequest request = new YopRequest();
request.setEncryptKey("xxxxxx")
request.setNeedEncrypt(true);
```

###3.5. 响应结果处理

响应结果`YopResponse`中包括处理状态、业务结果、错误码、子错误码。

其中，业务结果被解析为Map，可直接取值；也可以通过`getResult(Class<T> objectType)`或者`response.unmarshal(xxx.class)`自动完成反序列化为指定的类实例；还可以通过`response.getStringResult()`获取原始的字符串形式的业务结果。

###3.6. 响应结果验签

某些API可能使用了非SHA25的签名算法，需要指定签名算法：

```java
request.setSignAlg("SHA1");
```

###3.7 沙箱
沙箱网关是为了提高接入效率而提供的专门部署环境，开启沙箱模式以后，所有的请求都将发送到沙箱网关（不会发生实际业务调用），方便联调。
因此sdk引入了mode参数，mode的可选值为prod（生产），sandbox（沙箱），不配置的情况下默认为prod。
开启沙箱环境有两种方法：
* 配置文件中配置mode=sandbox,示例如下：

    ```
      {
        "app_key": "test",
        "aes_secret_key": "xxx",
        "server_root": "http://openapi.yeepay.com/yop-center",
        "yos_server_root": "http://yos.yeepay.com/yop-center",
        "sandbox_server_root": "http://sandbox.yeepay.com/yop-center",
        "yop_public_key": [
          {
            "store_type": "string",
            "cert_type": "RSA2048",
            "value": "xxx"
          }
        ],
        "isv_private_key": [
          {
            "store_type": "string",
            "cert_type": "RSA2048",
            "value": "xxx"
          }
        ],
        "http_client": {
          "connect_timeout": 10000,
          "read_timeout": 30000,
          "max_conn_total": 2000,
          "max_conn_per_route": 1000
        },
        "mode": "sandbox"
      }  
    ```
* 配置jvm启动参数-Dyop.sdk.mode=sandbox,jvm参数的优先级高于配置文件，如果在jvm中指定了mode，实际使用的mode即jvm中的mode，
配置文件中的mode不生效。

注意：
* 多配置文件的情况下，各个配置文件均可以配置自己的mode，各个配置文件中的appKey发起的请求，按照各自文件中的mode分发（前提是jvm中没有配置mode）
* 若请求了沙箱网关，在成功接收到沙箱网关的返回结果后会打印一条日志来说明正常请求到了沙箱环境，日志内容为：response from sandbox-gateway

## 4.配置文件详细说明

### 4.1. 默认配置文件读取路径(classPath)
```
/config
```

### 4.2. 配置文件命名规范

默认配置文件名 

```
yop_sdk_config_default.json
```

多配置文件命名方式
```
yop_sdk_config_{appKey}.json
```

只有一个配置文件时，该配置文件即默认配置文件（无论文件名是不是yop_sdk_config_default.json）；
当有多个配置文件时，则必须指定默认配置文件（通过default配置项）,不指定的情况下第一个配置文件作为默认配置；

### 4.3. 如何覆盖默认配置文件

在 VM arguments 中指定配置文件路径 
-Dyop.sdk.config.file 配置文件路径，多个配置文件用逗号分隔，文件路径必须为全路径，示例如下： 
* mac/linux:-Dyop.sdk.config.file=file://home/app/yop_sdk_config_default.json,file://home/app/yop_sdk_config_yop-boss.json
* windows:-Dyop.sdk.config.file=file:///D:\workspace\..\config\yop_sdk_config_default.json

常见容器修改 JVM 参数的方式如下，仅供参考：
* tomcat: 两种方式
	*  在$CATALINA_HOME/bin/目录下添加setenv.sh，在set env.sh中添加JAVA_OPTS="$JAVA_OPTS -Dyop.sdk.config.file=file://home/app/yop_sdk_config_default.json"
	* 在$CATALINA_HOME/bin/catalina.sh中直接添加JAVA_OPTS="$JAVA_OPTS -Dyop.sdk.config.file=file://home/app/yop_sdk_config_default.json"
* jetty: 在$jetty_home/bin/jetty.sh中添加JAVA_OPTIONS="-Dyop.sdk.config.file=file://home/app/yop_sdk_config_default.json"
* weblogic: 修改user_projects\domains\base_domain\bin下的startWebLogic.cmd文件，添加set JAVA_OPTIONS=%JAVA_OPTIONS% -Dyop.sdk.config.file=file://home/app/yop_sdk_config_default.json

### 4.4. 如何自定义配置

如果用户不能使用文件配置方式或者需要更自由的配置方式（例如从数据库加载sdk配置等），只需实现自定义配置提供方接口并注册，示例如下：

``` java
//全局设置
AppSdkConfigProvider provider = new MockCacheAppSdkConfigProvider("test", 30L, TimeUnit.SECONDS)
AppSdkConfigProviderRegistry.registerCustomProvider(provider);

//发起请求
```

#### 4.4.1. 实现提供方接口

* 可以直接实现接口AppSdkConfigProvider
    * AppSdkConfig是最小配置单元，包含指定appKey的所有配置信息
    * 如果用户配置有多个appKey，需要指定默认appKey，默认appKey的相关资源配置（请求地址，httpClient配置，代理配置等）是sdk实际采用的来源

```java
package com.yeepay.g3.sdk.yop.config;

public interface AppSdkConfigProvider {

    /**
     * 获取指定appKey的sdk配置
     *
     * @param appKey appKey
     * @return app sdk配置
     */
    AppSdkConfig getConfig(String appKey);

    /**
     * 获取默认的应用sdk配置
     *
     * @return app sdk配置
     */
    AppSdkConfig getDefaultConfig();

    /**
     * 获取指定appKey的sdk配置，如果不存在则返回默认配置
     *
     * @param appKey appKey
     * @return app sdk配置
     */
    AppSdkConfig getConfigWithDefault(String appKey);
}
```

* sdk提供了一些工具类便于用户实现

    * BaseFixedAppSdkConfigProvider用于sdk配置无需变化只需初始化一次的场景，需实现如下方法：

     ``` java
        /**
        * 加载用户自定义sdk配置
        *
        * @return 用户自定义sdk配置列表
        */
        protected abstract List<SDKConfig> loadCustomSdkConfig();
     ```

    provider在初始化的时候会调用该方法进行初始化工作，默认AppSdkConfig需要在相关SDKConfig中将default项配置为true，如果没有则会将读取到的第一个配置项作为默认配置，示例如下

      ```java
    public class MockFixedSDKConfigProvider extends BaseFixedAppSdkConfigProvider{

        /**
         * sdk配置加载repository，用于从数据库加载sdk配置
         */
        private SDKConfigRepository repository;

        @Override
        protected List<SDKConfig> loadCustomSdkConfig() {
            return repository.queryAllConfig();
        }
    }
      ```

    * BaseCachedAppSdkConfigProvider 用于sdk配置动态变化的场景，该类中会缓存相关配置并定时刷新以获取最新的配置，，需实现如下方法：

      ````java
/**
     * 加载sdk配置
     * 缓存会（首次加载、缓存过期、异步刷新）触发该方法去获取最新的配置
     *
     * @param appKey appKey
     * @return sdk配置
     */
    protected abstract SDKConfig loadSDKConfig(String appKey);
      ````

该类在初始化的时候必须指定默认appKey和缓存过期时间，示例如下：

```` java
        public class MockCachedSDKConfigProvider extends BaseCachedAppSdkConfigProvider{

        /**
         * sdk配置加载repository，用于从数据库加载sdk配置
         */
        private SDKConfigRepository repository;

        public MockCachedSDKConfigProvider(String defaultAppKey, Long expire, TimeUnit timeUnit) {
            super(defaultAppKey, expire, timeUnit);
        }

        /**
         * 缓存会（首次加载、缓存过期、异步刷新）触发改方法去获取最新的配置
         *
         * @param appKey appKey
         * @return
         */
        @Override
        protected SDKConfig loadSDKConfig(String appKey) {
            return repository.queryConfig(appKey);
        }
````

#### 4.4.2. 注册提供方

要想让自定义的提供方生效，需要向AppSdkConfigProviderRegistry（默认情况下采取文件提供方DefaultFileAppSdkConfigProvider）注册，示例如下：

```` java
AppSdkConfigProviderRegistry.registerCustomProvider(myProvider);
````

## 5. 系统开发指导

### 5.1. DNS 缓存配置

JVM 解析成功的域名会记录在 JVM 中，缓存有效时间默认是永远有效。

为提高系统稳定性，易宝实现了双活机房。为减小机房切换时对业务的影响，请务必设置合理的 DNS 缓存时间。当易宝切换机房时，域名可以尽早地自动解析到新的IP地址上。

以解析成功的域名缓存180秒，解析失败重新解析间隔10秒，给出修改方式如下：

方式1. 在 JAVA_OPTS 里设置

````java
-Dsun.net.inetaddr.ttl=180 -Dsun.net.inetaddr.negative.ttl=10
````

方式2. JAVA代码里修改property

````java
System.setProperty("sun.net.inetaddr.ttl", "180");
System.setProperty("sun.net.inetaddr.negative.ttl", "10");
````

### 5.2. 记录唯一请求标识以便快速获得技术支持

在每笔请求的请求头或者响应头中保存了一个名为 x-yop-request-id 的唯一请求标识，简称 requestId。请在向技术支持反馈问题时提供该信息，可以更快的获得帮助和支持。

### 5.3. 更改JCE无限制权限策略文件支持 AES 256

报错：Java Security: Illegal key size or default parameters

 说明：异常java.security.InvalidKeyException:illegal Key Size的解决方案
 <ol>
 	<li>在官方网站下载JCE无限制权限策略文件（JDK7的下载地址：
      http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html</li>
 	<li>下载后解压，可以看到local_policy.jar和US_export_policy.jar以及readme.txt</li>
 	<li>如果安装了JRE，将两个jar文件放到%JRE_HOME%\lib\security目录下覆盖原来的文件</li>
 	<li>如果安装了JDK，将两个jar文件放到%JDK_HOME%\jre\lib\security目录下覆盖原来文件</li>
 </ol>

##6. 附录

以下为本文中使用到的密钥：
#### 易宝生产公钥RSA2048
````
-----BEGIN RSA2048 PUBLIC KEY BLOCK-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6p0XWjscY+gsyqKRhw9MeLsEmhFdBRhT
2emOck/F1Omw38ZWhJxh9kDfs5HzFJMrVozgU+SJFDONxs8UB0wMILKRmqfLcfClG9MyCNuJkkfm
0HFQv1hRGdOvZPXj3Bckuwa7FrEXBRYUhK7vJ40afumspthmse6bs6mZxNn/mALZ2X07uznOrrc2
rk41Y2HftduxZw6T4EmtWuN2x4CZ8gwSyPAW5ZzZJLQ6tZDojBK4GZTAGhnn3bg5bBsBlw2+FLkC
QBuDsJVsFPiGh/b6K/+zGTvWyUcu+LUj2MejYQELDO3i2vQXVDk7lVi2/TcUYefvIcssnzsfCfja
orxsuwIDAQAB
-----END RSA2048 PUBLIC KEY BLOCK-----
````