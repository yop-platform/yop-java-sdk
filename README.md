# YOP Open Platform Java SDK (yop-java-sdk)

[![Maven Central](https://img.shields.io/maven-central/v/com.yeepay.yop.sdk/yop-java-sdk.svg)](https://search.maven.org/artifact/com.yeepay.yop.sdk/yop-java-sdk)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

[阅读中文文档](README_zh-CN.md)

**Empowering merchants with secure and efficient access to YeePay's comprehensive services.**

This SDK aims to provide Java developers with a convenient, secure, and reliable toolkit to quickly integrate various API services of the YeePay Open Platform (YOP). Whether you are a first-time user or an experienced developer, this SDK enables you to seamlessly connect with the YeePay payment system.

---

## ✨ Core Advantages

*   **🚀 Rapid Integration**: Clean API design and clear documentation help you integrate payment, account, marketing, and other capabilities within hours.
*   **🛡️ Secure and Reliable**: Built-in comprehensive signature, verification, encryption, and decryption mechanisms, with full support for Chinese national cryptographic algorithms (SM2/SM3/SM4) and international standard encryption algorithms (RSA/AES), ensuring the confidentiality and integrity of transaction data.
*   **🧩 Flexible Extension**: Modular design, supporting multiple HTTP clients (Apache HttpClient, OkHttp), and providing rich interfaces for merchants to customize credential management, hardware security module integration, and other advanced features.
*   **🇨🇳 National Cryptographic Compliance**: Deep integration of Chinese national cryptographic algorithm support, meeting financial security compliance requirements, safeguarding your business.
*   **🌍 International Perspective**: Simultaneous support for international common encryption standards, facilitating global business expansion.
*   **📄 Comprehensive Examples**: Providing rich API call examples and test code, reducing learning costs and accelerating development.

---

## 🏁 Quick Start

### 1. Environment Requirements
*   Java Development Kit (JDK) 1.8 or higher
*   Maven 3.x or Gradle

### 2. Add Dependency

**Maven Users:**
Add the following dependency to your project's `pom.xml` file:
```xml
<dependency>
  <groupId>com.yeepay.yop.sdk</groupId>
  <artifactId>yop-java-sdk</artifactId>
  <version>4.4.15</version> <!-- Please replace with the latest stable version -->
</dependency>
```

**Gradle Users:**
Add the following dependency to your `build.gradle` file:
```groovy
implementation 'com.yeepay.yop.sdk:yop-java-sdk:4.4.15' // Please replace with the latest stable version
```

### 3. Configure Your Merchant Credentials

For **single merchant applications**, the SDK will load the configuration file from the `classpath:/config/yop_sdk_config_default.json` path by default. You only need to:
1.  Copy the following template file to your project's `src/main/resources/config/` directory.
2.  Modify key parameters such as `app_key` (your application AppKey) and `isv_private_key` (your application private key) in the file.

```json
// Example: yop_sdk_config_default.json
{
  "app_key": "YOUR_APP_KEY", // Replace with your application AppKey
  "isv_private_key": { // Application private key configuration
    "value": "YOUR_PRIVATE_KEY_STRING" // Replace with your application private key string
  },
  "yos_server_root": "https://yos.yeepay.com/yop-center", // YOP service address
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
    "connect_timeout": 10000, // Connection timeout (milliseconds)
    "connect_request_timeout": 5000,
    "read_timeout": 30000 // Read timeout (milliseconds)
  }
}
```

**Custom Configuration File Path:**
If you want to load the configuration file from another location, you can specify it using the JVM parameter `-Dyop.sdk.config.file`:
*   **Mac/Linux:** `-Dyop.sdk.config.file=file:///path/to/your/yop_sdk_config.json`
*   **Windows:** `-Dyop.sdk.config.file=file:///D:/path/to/your/yop_sdk_config.json`

For specific container (such as Tomcat, Jetty, WebLogic) JVM parameter settings, please refer to the original documentation or the official documentation of the respective container.

### 4. Make Your First API Call

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
        // Example 1: GET Standard Request
        getCommonExample();

        // Example 2: POST Form Request
        postFormExample();

        // Example 3: POST JSON Format Request
        postJsonExample();

        // Example 4: File Upload
        fileUploadExample();

        // Example 5: File Download
        fileDownloadExample();
    }

    /**
     * GET Standard Request Example
     */
    private static void getCommonExample() {
        try {
            // Create request object
            YopRequest request = new YopRequest("/rest/v1.0/test/product-query/query-for-doc", "GET");
            request.addParameter("string0", "dsbzb");

            // Set application information
            String appKey = "app_10085525305"; // Replace with your application AppKey
            request.getRequestConfig().setAppKey(appKey);
            request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");

            // Set authentication information
//            request.getRequestConfig().setCredentials(new YopPKICredentials(
//                    appKey, new PKICredentialsItem(getPrivateKey(
//                    "YOUR_PRIVATE_KEY_STRING", // Replace with your private key
//                    CertTypeEnum.RSA2048),
//                    CertTypeEnum.RSA2048)));

            // Send request
            YopClient yopClient = YopClientBuilder.builder().build();
            YopResponse response = yopClient.request(request);

            // Process response
            if (response != null && response.getResult() != null) {
                LOGGER.info("API call successful: {}", response.getResult());
                // Parse response.getResult() according to API documentation
            } else {
                LOGGER.warn("API call failed: {}", response.getResult());
            }
        } catch (Exception e) {
            LOGGER.error("API call exception", e);
        }
    }

    /**
     * POST Form Request Example
     */
    private static void postFormExample() {
        try {
            // Create request object
            YopRequest request = new YopRequest("/rest/v1.0/test-wdc/test/http-json", "POST");

            // Set application information
            String appKey = "app_10085525305"; // Replace with your application AppKey
            request.getRequestConfig().setAppKey(appKey);
            request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");

            // Set authentication information
//            request.getRequestConfig().setCredentials(new YopPKICredentials(
//                    appKey, new PKICredentialsItem(getPrivateKey(
//                    "YOUR_PRIVATE_KEY_STRING", // Replace with your private key
//                    CertTypeEnum.RSA2048),
//                    CertTypeEnum.RSA2048)));

            // Add form parameters
            request.addParameter("string", "hello");

            // Send request
            YopClient yopClient = YopClientBuilder.builder().build();
            YopResponse response = yopClient.request(request);

            // Process response
            if (response != null && response.getResult() != null) {
                LOGGER.info("API call successful: {}", response.getResult());
                // Parse response.getResult() according to API documentation
            } else {
                LOGGER.warn("API call failed: {}", response.getResult());
            }
        } catch (Exception e) {
            LOGGER.error("API call exception", e);
        }
    }

    /**
     * POST JSON Format Request Example
     */
    private static void postJsonExample() {
        try {
            // Create request object
            YopRequest request = new YopRequest("/rest/v1.0/test-wdc/test/http-json", "POST");

            // Set application information
            String appKey = "app_10085525305"; // Replace with your application AppKey
            request.getRequestConfig().setAppKey(appKey);
            request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");

            // Set authentication information
//            request.getRequestConfig().setCredentials(new YopPKICredentials(
//                    appKey, new PKICredentialsItem(getPrivateKey(
//                    "YOUR_PRIVATE_KEY_STRING", // Replace with your private key
//                    CertTypeEnum.RSA2048),
//                    CertTypeEnum.RSA2048)));

            // Build JSON request body
            Map<String, Object> obj = new HashMap<>();
            Map<String, Object> arg0 = new HashMap<>();
            arg0.put("string", "hello");
            obj.put("arg0", arg0);
            request.setContent(JsonUtils.toJsonString(obj));

            // Send request
            YopClient yopClient = YopClientBuilder.builder().build();
            YopResponse response = yopClient.request(request);

            // Process response
            if (response != null && response.getResult() != null) {
                LOGGER.info("API call successful: {}", response.getResult());
                // Parse response.getResult() according to API documentation
            } else {
                LOGGER.warn("API call failed: {}", response.getResult());
            }
        } catch (Exception e) {
            LOGGER.error("API call exception", e);
        }
    }

    /**
     * File Upload Example
     */
    private static void fileUploadExample() {
        try {
            // Create request object
            YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");

            // Set application information
            String appKey = "app_10085525305"; // Replace with your application AppKey
            request.getRequestConfig().setAppKey(appKey);
            request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");

            // Set authentication information
//            request.getRequestConfig().setCredentials(new YopPKICredentials(
//                    appKey, new PKICredentialsItem(getPrivateKey(
//                    "YOUR_PRIVATE_KEY_STRING", // Replace with your private key
//                    CertTypeEnum.RSA2048),
//                    CertTypeEnum.RSA2048)));

            // Add upload file
            // Here using ClassLoader to get resource file, in actual use it can be a local file path
            InputStream fileStream = YopApiDemo.class.getResourceAsStream("/your-file.txt");
            request.addMultiPartFile("_file", fileStream);

            // Send request
            YopClient yopClient = YopClientBuilder.builder().build();
            YosUploadResponse response = yopClient.upload(request);

            // Process response
            if (response != null && response.getResult() != null) {
                LOGGER.info("File upload successful: {}", response.getResult());
                // Parse response.getResult() according to API documentation
            } else {
                LOGGER.warn("File upload failed: {}", response.getResult());
            }
        } catch (Exception e) {
            LOGGER.error("File upload exception", e);
        }
    }

    /**
     * File Download Example
     */
    private static void fileDownloadExample() {
        YosDownloadInputStream downloadInputStream = null;
        try {
            // Create request object
            YopRequest request = new YopRequest("/yos/v1.0/test/test/ceph-download", "GET");
            request.addParameter("fileName", "example.txt");

            // Set application information
            String appKey = "app_10085525305"; // Replace with your application AppKey
            request.getRequestConfig().setAppKey(appKey);
            request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");

            // Set authentication information
//            request.getRequestConfig().setCredentials(new YopPKICredentials(
//                    appKey, new PKICredentialsItem(getPrivateKey(
//                    "YOUR_PRIVATE_KEY_STRING", // Replace with your private key
//                    CertTypeEnum.RSA2048),
//                    CertTypeEnum.RSA2048)));

            // Send request
            YopClient yopClient = YopClientBuilder.builder().build();
            YosDownloadResponse response = yopClient.download(request);

            // Process response
            if (response != null && response.getResult() != null) {
                downloadInputStream = response.getResult();
                // Read downloaded file content
                String fileContent = IOUtils.toString(downloadInputStream, "UTF-8");
                LOGGER.info("File download successful, content length: {}", fileContent.length());

                // In actual use, you can save the file content to a local file
                // Files.write(Paths.get("downloaded-file.txt"), fileContent.getBytes());
            } else {
                LOGGER.warn("File download failed: {}", response.getResult());
            }
        } catch (Exception e) {
            LOGGER.error("File download exception", e);
        } finally {
            // Close stream
            StreamUtils.closeQuietly(downloadInputStream);
        }
    }

    /**
     * Get Private Key
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
**Important Note**: The above code is for illustration purposes. Please refer to the specific examples in the `yop-java-sdk-test` module and the [YeePay Open Platform Official API Documentation](https://open.yeepay.com/docs-v2) to build and send requests.

---

## 📖 In-Depth Understanding

### SDK Modular Overview

This SDK is built with Maven and has been carefully designed with a modular structure to meet the customization needs of different merchants:

*   **`yop-java-sdk` (Core Integration Package - Recommended)**
    *   **Functionality**: Merchant's preferred dependency. Integrates Apache HttpClient as the HTTP communication component by default, with built-in encryption, decryption, signing, and verification core security features.
    *   **Applicable**: General integration needs of most merchants.
    *   **Note**: Equivalent to `yop-java-sdk-apache`.

*   **`yop-java-sdk-apache` (Apache HttpClient Communication Module)**
    *   **Functionality**: Integrates Apache HttpClient as the HTTP communication component, with built-in encryption, decryption, signing, and verification core security features.
    *   **Applicable**: General integration needs of most merchants.

*   **`yop-java-sdk-okhttp` (OkHttp Communication Module)**
    *   **Functionality**: Provides HTTP communication implementation based on OkHttp.
    *   **Applicable**: Merchants with specific HTTP client preferences or projects that already extensively use OkHttp.

*   **`yop-java-sdk-base` (Basic Functionality Module)**
    *   **Functionality**: Encapsulates request objects (`YopRequest`), configuration (`YopRequestConfig`), and other non-encryption related core classes, abstracts message assembly and parsing logic, and provides basic utility classes.
    *   **Applicable**: Merchants who need deep customization of credential storage, encryptors, signers, and other advanced features.

*   **Cryptography-related Modules**:
    *   `yop-java-sdk-crypto-api`: Defines basic interfaces for encryptors, signers, and other cryptographic operations.
    *   `yop-java-sdk-crypto-gm-base`: Provides utility classes related to Chinese national cryptographic algorithms, simplifying development work when integrating with hardware security modules.
    *   `yop-java-sdk-crypto-gm`: Provides software implementation of Chinese national cryptographic algorithms (SM2/SM3/SM4), which can serve as a reference when integrating with hardware security modules.
    *   `yop-java-sdk-crypto-inter`: Provides software implementation of international common encryption algorithms (RSA/AES, etc.), mainly for overseas businesses or merchants with specific needs.

*   **`yop-java-sdk-test` (Testing and Example Module)**
    *   **Functionality**: Contains SDK's own functionality and performance test code, and more importantly, provides a large number of API call examples that merchants can reference.
    *   **Strong Recommendation**: Carefully review the example code in this module during integration.

### Multi-Merchant (Platform Merchant, Service Provider) Configuration Solution

For platform-type merchants who need to manage credentials for multiple downstream merchants, the SDK supports dynamically loading different merchants' `appKey` and private keys through custom credential providers (`YopCredentialsProvider`). You can extend `YopCachedCredentialsProvider` or `YopFixedCredentialsProvider` to implement your logic.

---

## 🛠️ Advanced Features and Customization

*   **Hardware Security Module (HSM) Integration**: The SDK design fully considers integration with hardware security modules. You can implement the `com.yeepay.yop.sdk.security.Encrypter` and `com.yeepay.yop.sdk.security.Signer` interfaces to delegate encryption and signing operations to your HSM device. For detailed guidance, please refer to the official documentation: [SDK-based HSM Integration](https://open.yeepay.com/docs/open/platform-doc/sdk_guide-sm/encryptor-support).

*   **Custom HTTP Client**: If the default Apache HttpClient or optional OkHttp client does not meet your specific needs (e.g., requiring more detailed connection pool management, proxy configuration, etc.), you can implement the `com.yeepay.yop.sdk.http.YopHttpClient` interface and configure the SDK to use your custom implementation.

---

## 📚 Developer Resources

*   **Official Java SDK (RSA) Usage Guide**: [https://open.yeepay.com/docs/platform/sdk_guide/java-sdk-guide](https://open.yeepay.com/docs/platform/sdk_guide/java-sdk-guide)
*   **API Interface List and Documentation**: [https://open.yeepay.com/docs-v2](https://open.yeepay.com/docs/api-list)
*   **HSM Integration Guide**: [https://open.yeepay.com/docs/open/platform-doc/sdk_guide-sm/encryptor-support](https://open.yeepay.com/docs/open/platform-doc/sdk_guide-sm/encryptor-support)

---

## 📜 License

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fyop-platform%2Fyop-java-sdk.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Fyop-platform%2Fyop-java-sdk?ref=badge_large)

This SDK follows the [Apache License 2.0](LICENSE) open source license agreement.

---

We are committed to providing an excellent developer experience. If you encounter any issues or have any suggestions during use, please feel free to contact us through official channels.
