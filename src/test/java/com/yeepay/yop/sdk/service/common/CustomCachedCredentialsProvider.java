/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.service.common;

import com.yeepay.yop.sdk.auth.credentials.provider.YopCachedCredentialsProvider;
import com.yeepay.yop.sdk.config.YopAppConfig;
import com.yeepay.yop.sdk.config.enums.CertStoreType;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.security.CertTypeEnum;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2020/11/25 上午10:39
 */
public class CustomCachedCredentialsProvider extends YopCachedCredentialsProvider {

    @Override
    protected YopAppConfig loadAppConfig(String appKey) {
        YopAppConfig yopAppConfig = new YopAppConfig();
        yopAppConfig.setAppKey(appKey);

        YopCertConfig yopCertConfig = new YopCertConfig();
        yopCertConfig.setCertType(CertTypeEnum.RSA2048);
        yopCertConfig.setStoreType(CertStoreType.STRING);
        yopCertConfig.setValue("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDFtjrUYU2t+WWOOm6CK3L6nikNKY1gty2x2cmIaAF0IWlBQ33ljJubzNODWGs4kL2nqfQnmMqetLatlMdgvquuftNL3kpZvOX8E746REHPH+MP+dyalvbitwP6vUbFbiKiO555hM1AhtdY17TVZthNtklqDmkXZ7U9Lb5+OIQrxK8uvy+0hupZehpHs0lXBx8Hh2kXFO6BomUWAuc/GVWSBhL1ziq9zx+M37LP4yYEogsqZNq70vi77IaD06r0MgYJ1foyyHz1b5J5joYrkPb9+w8hoU5iK62aitX8bsfRbGh93BbrYBGVwpZGmo8xmxmnEqDyU0HCj50P0GbJ77EtAgMBAAECggEADR318TkzaiNttW3y/vfa/P2ZQ6JKGuyMP4xvbnlX/1hoH0hXBe+6My/3qHMpSetwabtA04+zgawDoqiIQcbkpQMNCa+Jx0JdD8hPipYUt6Up71loZWk2n/v8a5o7I8YWziSecvl4lJtdlitZd/8GwsEhRcQG/OKIh7KNPNqLCkw4TzJkYsL02QogF7fL/NVCw/AakIX1MjDFXCm+e7ifylztRNdm2oUuyw66C+nkORVL3GcabHOixsxUsqhMGBcpIJ4Y+Naypb79nRH255LsexE2N2y39pi9niZJNmfprUJMGhvA1NFcaN363IADfkcoYF0InQA7mMtzszPSWbTTgQKBgQD0PEmzxJxUhpXOKJVcZJhn+1wSyPqclHDRC+uYhyJhut/lSp7gIhoI2+wvIok7bYH3zrbCBregY4hlLiQjTH5zRoF9KvWf6O6OhtZT9RTVFQ+i9yjn/+7t4alWkYaA+oaNG/YXxKLXHOG8KLH7OdBjQpqVYmPyCoqJYNrjQS9axQKBgQDPPEAMPLtFR4esztB1vdcR4lCizyHBUHab/eWxyER22AyHh5VJFP5BTfKB34TFcKXKP/Nn3X5OEzQdoEivbvy+0QkIC2AzODZlcifUY5hj+JljpmldAO4TZM8qW4hrRkvAvu8CLekyVWk6h7gQLr7H2TL7qcqJW8F0ywNRR8yDSQKBgAs3oaLyCsQPEl5PmtyNejp8XvQ16ty6LJxNUdrFihy2+oWLcdSVfGCfyS85BNiH5Qo+okIzEMf7Ck6rWdmNL9mXiWb4TCO2DQ7avolInlZTC07Oz0Aojw656I8jS+wslXVxrVHWJCyBFRURQWtqclm8u2DVDgYV2dsJacQ6QDSJAoGAIKE8+HBLkFH98+CwhAl7Jq66wZfZmcWgl1k35HFDDm9gMarQf6xViFTMnVRjZG8jO6AsJCuE6qgtaYjGSRExrJ3fTSv1Xrs4HWsHCHMSGJOZG06lgmZWFimmUOYOqc1suhGWMoKmGC3IntWlzq59jZwOYf/PCyeuY0Rf5llmwzkCgYEA7NViHZgcukk1iwy4QAu4q4IyzckKIT2pTiKFlVhb4cO48w99HSvfmqJSPUZVN3I15UwVs0lqnzLanU/+aSTYJDArpgEhEwloDAkhfy8NQzM9Ji+XXA3itDdR7A7+Wy5XULqK0FVjXMXNK3sV/O0qGWey7aOiH1QUwMyhT4qlenc=");

        YopCertConfig[] isvPrivateKeys = new YopCertConfig[1];
        isvPrivateKeys[0] = yopCertConfig;
        yopAppConfig.storeIsvPrivateKey(isvPrivateKeys);

        YopCertConfig isvEncryptKey = new YopCertConfig();
        isvEncryptKey.setCertType(CertTypeEnum.SM4);
        isvEncryptKey.setStoreType(CertStoreType.STRING);
        isvEncryptKey.setValue("c2c7000fe2f5649c0b0b3bf79c08f711");
        yopAppConfig.setIsvEncryptKey(new YopCertConfig[]{isvEncryptKey});

        return yopAppConfig;
    }

    @Override
    public void removeConfig(String key) {
    }
}
