# 易宝开放平台 Java SDK (yop-java-sdk)

[![Maven Central](https://img.shields.io/maven-central/v/com.yeepay.yop.sdk/yop-java-sdk.svg)](https://search.maven.org/artifact/com.yeepay.yop.sdk/yop-java-sdk)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

[Read this document in English](README.md)

**赋能商户，安全高效接入易宝支付全方位服务。**

本 SDK 旨在为 Java 开发者提供一套便捷、安全、可靠的工具集，以快速集成易宝开放平台（YOP）的各项 API 服务。无论您是初次接触还是资深开发者，都能通过本 SDK 轻松实现与易宝支付系统的无缝对接。

---

## ✨ 核心优势

*   **🚀 快速集成**: 简洁的 API 设计与清晰的文档指引，助您数小时内完成支付、账户、营销等多种能力的集成。
*   **🛡️ 安全可靠**: 内置完善的签名、验签、加密、解密机制，全面支持国密算法（SM2/SM3/SM4）与国际标准加密算法（RSA/AES），保障交易数据的机密性与完整性。
*   **🧩 灵活扩展**: 模块化设计，支持多种 HTTP 客户端（Apache HttpClient, OkHttp），并提供丰富的接口供商户自定义凭证管理、加密机对接等高级功能。
*   **🇨🇳 国密合规**: 深度集成国密算法支持，满足金融安全合规要求，为您的业务保驾护航。
*   **🌍 国际视野**: 同时支持国际通用的加密标准，助力业务全球化拓展。
*   **📄 详尽示例**: 提供丰富的调用示例与测试代码，降低学习成本，加速开发进程。

---

## 🏁 快速上手

### 1. 环境要求
*   Java Development Kit (JDK) 1.8 或更高版本
*   Maven 3.x 或 Gradle

### 2. 添加依赖

**Maven 用户:**
请在您的项目 `pom.xml` 文件中添加以下依赖：
```xml
<dependency>
  <groupId>com.yeepay.yop.sdk</groupId>
  <artifactId>yop-java-sdk</artifactId>
  <version>4.4.15</version> <!-- 请替换为最新的稳定版本 -->
</dependency>
```

**Gradle 用户:**
请在您的 `build.gradle` 文件中添加以下依赖：
```groovy
implementation 'com.yeepay.yop.sdk:yop-java-sdk:4.4.15' // 请替换为最新的稳定版本
```

### 3. 配置您的商户凭证

对于**单商户应用**，SDK 默认会从 `classpath:/config/yop_sdk_config_default.json` 路径加载配置文件。您只需：
1.  复制以下模板文件到您项目的 `src/main/resources/config/` 目录下。
2.  修改该文件中的 `app_key` (您的应用AppKey) 和 `isv_private_key` (您的应用私钥) 等关键参数。

```json
// 示例: yop_sdk_config_default.json
{
  "app_key": "YOUR_APP_KEY", // 替换为您的应用AppKey
  "isv_private_key": { // 应用私钥配置
    "value": "YOUR_PRIVATE_KEY_STRING" // 替换为您的应用私钥字符串
  },
  "yos_server_root": "https://yos.yeepay.com/yop-center", // YOP服务地址
  "preferred_server_roots": [
    "https://openapi.yeepay.com/yop-center"
  ],
  "yop_cert_store": {
    "enable": false,
    "valid_after_expire_period": 1674727773000
  },
  "yop_report": {
    "enable": true,
    "enable_success_report": true,
    "send_interval_ms": 3000,
    "stat_interval_ms": 5000,
    "max_queue_size": 500,
    "max_fail_count": 10,
    "max_fail_count_per_exception": 5,
    "max_elapsed_ms": 15000,
    "max_packet_size": 50
  },
  "http_client": {
    "connect_timeout": 10000, // 连接超时时间 (毫秒)
    "connect_request_timeout": 5000,
    "read_timeout": 30000 // 读取超时时间 (毫秒)
  }
}
```

**自定义配置文件路径:**
如果您希望从其他位置加载配置文件，可以通过 JVM 参数 `-Dyop.sdk.config.file` 指定：
*   **Mac/Linux:** `-Dyop.sdk.config.file=file:///path/to/your/yop_sdk_config.json`
*   **Windows:** `-Dyop.sdk.config.file=file:///D:/path/to/your/yop_sdk_config.json`

具体容器（如 Tomcat, Jetty, WebLogic）的 JVM 参数设置方式，请参考原文档或相应容器的官方文档。

### 4. 发起您的第一次 API 调用

```java
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.config.enums.CertStoreType;
import com.yeepay.yop.sdk.base.security.cert.parser.YopCertParserFactory;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.cert.YopCertCategory;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.model.yos.YosDownloadResponse;
import com.yeepay.yop.sdk.model.yos.YosDownloadInputStream;
import com.yeepay.yop.sdk.service.common.response.YosUploadResponse;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.utils.JsonUtils;
import com.yeepay.yop.sdk.utils.StreamUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.PrivateKey;
import java.util.Map;
import java.util.HashMap;

public class YopApiDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(YopApiDemo.class);

    public static void main(String[] args) {
        // 示例1: GET 普通请求
        getCommonExample();

        // 示例2: POST Form表单请求
        postFormExample();

        // 示例3: POST JSON格式请求
        postJsonExample();

        // 示例4: 文件上传
        fileUploadExample();

        // 示例5: 文件下载
        fileDownloadExample();
    }

    /**
     * GET 普通请求示例
     */
    private static void getCommonExample() {
        try {
            // 创建请求对象
            YopRequest request = new YopRequest("/rest/v1.0/test/product-query/query-for-doc", "GET");
            request.addParameter("string0", "dsbzb");

            // 设置应用信息
            String appKey = "app_10085525305"; // 替换为您的应用AppKey
            request.getRequestConfig().setAppKey(appKey);
            request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");

            // 设置认证信息
//            request.getRequestConfig().setCredentials(new YopPKICredentials(
//                    appKey, new PKICredentialsItem(getPrivateKey(
//                    "YOUR_PRIVATE_KEY_STRING", // 替换为您的私钥
//                    CertTypeEnum.RSA2048),
//                    CertTypeEnum.RSA2048)));

            // 发送请求
            YopClient yopClient = YopClientBuilder.builder().build();
            YopResponse response = yopClient.request(request);

            // 处理响应
            if (response != null && response.getResult() != null) {
                LOGGER.info("API调用成功: {}", response.getResult());
                // 根据API文档解析 response.getResult()
            } else {
                LOGGER.warn("API调用失败: {}", response.getResult());
            }
        } catch (Exception e) {
            LOGGER.error("API调用异常", e);
        }
    }

    /**
     * POST Form表单请求示例
     */
    private static void postFormExample() {
        try {
            // 创建请求对象
            YopRequest request = new YopRequest("/rest/v1.0/test-wdc/test/http-json", "POST");

            // 设置应用信息
            String appKey = "app_10085525305"; // 替换为您的应用AppKey
            request.getRequestConfig().setAppKey(appKey);
            request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");

            // 设置认证信息
//            request.getRequestConfig().setCredentials(new YopPKICredentials(
//                    appKey, new PKICredentialsItem(getPrivateKey(
//                    "YOUR_PRIVATE_KEY_STRING", // 替换为您的私钥
//                    CertTypeEnum.RSA2048),
//                    CertTypeEnum.RSA2048)));

            // 添加表单参数
            request.addParameter("string", "你好");

            // 发送请求
            YopClient yopClient = YopClientBuilder.builder().build();
            YopResponse response = yopClient.request(request);

            // 处理响应
            if (response != null && response.getResult() != null) {
                LOGGER.info("API调用成功: {}", response.getResult());
                // 根据API文档解析 response.getResult()
            } else {
                LOGGER.warn("API调用失败: {}", response.getResult());
            }
        } catch (Exception e) {
            LOGGER.error("API调用异常", e);
        }
    }

    /**
     * POST JSON格式请求示例
     */
    private static void postJsonExample() {
        try {
            // 创建请求对象
            YopRequest request = new YopRequest("/rest/v1.0/test-wdc/test/http-json", "POST");

            // 设置应用信息
            String appKey = "app_10085525305"; // 替换为您的应用AppKey
            request.getRequestConfig().setAppKey(appKey);
            request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");

            // 设置认证信息
//            request.getRequestConfig().setCredentials(new YopPKICredentials(
//                    appKey, new PKICredentialsItem(getPrivateKey(
//                    "YOUR_PRIVATE_KEY_STRING", // 替换为您的私钥
//                    CertTypeEnum.RSA2048),
//                    CertTypeEnum.RSA2048)));

            // 构建JSON请求体
            Map<String, Object> obj = new HashMap<>();
            Map<String, Object> arg0 = new HashMap<>();
            arg0.put("string", "你好");
            obj.put("arg0", arg0);
            request.setContent(JsonUtils.toJsonString(obj));

            // 发送请求
            YopClient yopClient = YopClientBuilder.builder().build();
            YopResponse response = yopClient.request(request);

            // 处理响应
            if (response != null && response.getResult() != null) {
                LOGGER.info("API调用成功: {}", response.getResult());
                // 根据API文档解析 response.getResult()
            } else {
                LOGGER.warn("API调用失败: {}", response.getResult());
            }
        } catch (Exception e) {
            LOGGER.error("API调用异常", e);
        }
    }

    /**
     * 文件上传示例
     */
    private static void fileUploadExample() {
        try {
            // 创建请求对象
            YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");

            // 设置应用信息
            String appKey = "app_10085525305"; // 替换为您的应用AppKey
            request.getRequestConfig().setAppKey(appKey);
            request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");

            // 设置认证信息
//            request.getRequestConfig().setCredentials(new YopPKICredentials(
//                    appKey, new PKICredentialsItem(getPrivateKey(
//                    "YOUR_PRIVATE_KEY_STRING", // 替换为您的私钥
//                    CertTypeEnum.RSA2048),
//                    CertTypeEnum.RSA2048)));

            // 添加上传文件
            // 这里使用ClassLoader获取资源文件，实际使用时可以是本地文件路径
            InputStream fileStream = YopApiDemo.class.getResourceAsStream("/your-file.txt");
            request.addMultiPartFile("_file", fileStream);

            // 发送请求
            YopClient yopClient = YopClientBuilder.builder().build();
            YosUploadResponse response = yopClient.upload(request);

            // 处理响应
            if (response != null && response.getResult() != null) {
                LOGGER.info("文件上传成功: {}", response.getResult());
                // 根据API文档解析 response.getResult()
            } else {
                LOGGER.warn("文件上传失败: {}", response.getResult());
            }
        } catch (Exception e) {
            LOGGER.error("文件上传异常", e);
        }
    }

    /**
     * 文件下载示例
     */
    private static void fileDownloadExample() {
        YosDownloadInputStream downloadInputStream = null;
        try {
            // 创建请求对象
            YopRequest request = new YopRequest("/yos/v1.0/test/test/ceph-download", "GET");
            request.addParameter("fileName", "example.txt");

            // 设置应用信息
            String appKey = "app_10085525305"; // 替换为您的应用AppKey
            request.getRequestConfig().setAppKey(appKey);
            request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");

            // 设置认证信息
//            request.getRequestConfig().setCredentials(new YopPKICredentials(
//                    appKey, new PKICredentialsItem(getPrivateKey(
//                    "YOUR_PRIVATE_KEY_STRING", // 替换为您的私钥
//                    CertTypeEnum.RSA2048),
//                    CertTypeEnum.RSA2048)));

            // 发送请求
            YopClient yopClient = YopClientBuilder.builder().build();
            YosDownloadResponse response = yopClient.download(request);

            // 处理响应
            if (response != null && response.getResult() != null) {
                downloadInputStream = response.getResult();
                // 读取下载的文件内容
                String fileContent = IOUtils.toString(downloadInputStream, "UTF-8");
                LOGGER.info("文件下载成功，内容长度: {}", fileContent.length());

                // 实际使用时，可以将文件内容保存到本地文件
                // Files.write(Paths.get("downloaded-file.txt"), fileContent.getBytes());
            } else {
                LOGGER.warn("文件下载失败: {}", response.getResult());
            }
        } catch (Exception e) {
            LOGGER.error("文件下载异常", e);
        } finally {
            // 关闭流
            StreamUtils.closeQuietly(downloadInputStream);
        }
    }

    /**
     * 获取私钥
     */
    private static PrivateKey getPrivateKey(String priKey, CertTypeEnum certType) {
        final YopCertConfig yopCertConfig = new YopCertConfig();
        yopCertConfig.setCertType(certType);
        yopCertConfig.setValue(priKey);
        yopCertConfig.setStoreType(CertStoreType.STRING);
        return (PrivateKey) YopCertParserFactory.getCertParser(YopCertCategory.PRIVATE, certType).parse(yopCertConfig);
    }
}
```
**重要提示**: 上述代码为示意。请务必参考 `yop-java-sdk-test` 模块中的具体示例以及[易宝开放平台官方API文档](https://open.yeepay.com/docs-v2)来构建和发起请求。

---

## 📖 深入了解

### SDK 模块化概览

本 SDK 采用 Maven 构建，并进行了精心的模块化设计，以满足不同商户的定制化需求：

*   **`yop-java-sdk` (核心集成包 - 推荐)**
    *   **功能**: 商户首选依赖。默认集成 Apache HttpClient 作为 HTTP 通信组件，内置加解密、签名验签等核心安全功能。
    *   **适用**: 大多数商户的通用集成需求。
    *   **提示**: 等同于 `yop-java-sdk-apache`。

*   **`yop-java-sdk-apache` (Apache HttpClient 通信模块)**
    *   **功能**: 集成 Apache HttpClient 作为 HTTP 通信组件，内置加解密、签名验签等核心安全功能。
    *   **适用**: 大多数商户的通用集成需求。

*   **`yop-java-sdk-okhttp` (OkHttp 通信模块)**
    *   **功能**: 提供基于 OkHttp 的 HTTP 通信实现。
    *   **适用**: 对 HTTP 客户端有特定偏好或项目中已广泛使用 OkHttp 的商户。

*   **`yop-java-sdk-base` (基础功能模块)**
    *   **功能**: 封装了请求对象（`YopRequest`）、配置（`YopRequestConfig`）等非加解密相关的核心类，抽象了报文组装与解析逻辑，并提供了基础工具。
    *   **适用**: 需要深度定制凭证存储、加密器、签名器等高级功能的商户。

*   **密码学相关模块**:
    *   `yop-java-sdk-crypto-api`: 定义了加密器、签名器等密码学操作的基础接口。
    *   `yop-java-sdk-crypto-gm-base`: 提供国密算法相关的辅助工具类，简化对接加密机时的开发工作。
    *   `yop-java-sdk-crypto-gm`: 提供国密算法（SM2/SM3/SM4）的软件实现，可作为对接加密机时的参考。
    *   `yop-java-sdk-crypto-inter`: 提供国际通用加密算法（RSA/AES等）的软件实现，主要供海外业务或有特定需求的商户使用。

*   **`yop-java-sdk-test` (测试与示例模块)**
    *   **功能**: 包含 SDK 自身的功能与性能测试代码，更重要的是，提供了大量可供商户参考的 API 调用示例。
    *   **强烈建议**: 在集成过程中，仔细查阅此模块下的示例代码。

### 多商户（平台商、服务商）配置方案

对于需要管理多个下游商户凭证的平台型商户，SDK 支持通过自定义凭证提供方（`YopCredentialsProvider`）来动态加载不同商户的 `appKey` 和私钥。您可以继承 `YopCachedCredentialsProvider` 或 `YopFixedCredentialsProvider` 来实现您的逻辑。

---

## 🛠️ 高级特性与定制

*   **对接硬件加密机 (HSM)**: SDK 设计充分考虑了与硬件加密机的集成。您可以实现 `com.yeepay.yop.sdk.security.Encrypter` 和 `com.yeepay.yop.sdk.security.Signer` 接口，将加密和签名操作代理到您的 HSM 设备。详细指引请参考官方文档：[基于SDK对接加密机](https://open.yeepay.com/docs/open/platform-doc/sdk_guide-sm/encryptor-support)。

*   **自定义 HTTP 客户端**: 如果默认的 Apache HttpClient 或可选的 OkHttp 客户端不满足您的特定需求（例如，需要更细致的连接池管理、代理配置等），您可以实现 `com.yeepay.yop.sdk.http.YopHttpClient` 接口，并配置 SDK 使用您的自定义实现。

---

## 📚 开发者资源

*   **官方Java SDK(RSA)使用指南**: [https://open.yeepay.com/docs/platform/sdk_guide/java-sdk-guide](https://open.yeepay.com/docs/platform/sdk_guide/java-sdk-guide)
*   **API接口列表与文档**: [https://open.yeepay.com/docs-v2](https://open.yeepay.com/docs/api-list)
*   **加密机对接指南**: [https://open.yeepay.com/docs/open/platform-doc/sdk_guide-sm/encryptor-support](https://open.yeepay.com/docs/open/platform-doc/sdk_guide-sm/encryptor-support)

---

## 📜 License

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fyop-platform%2Fyop-java-sdk.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Fyop-platform%2Fyop-java-sdk?ref=badge_large)

本 SDK 遵循 [Apache License 2.0](LICENSE) 开源许可协议。

---

我们致力于提供卓越的开发者体验。如果您在使用过程中遇到任何问题或有任何建议，欢迎通过官方渠道与我们联系。
