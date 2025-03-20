/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.config.provider.db;

import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.base.auth.credentials.provider.YopBaseCredentialsProvider;
import com.yeepay.yop.sdk.base.config.YopAppConfig;
import com.yeepay.yop.sdk.config.enums.CertStoreType;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * title: 示例：从数据库加载密钥配置 <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/12/12
 */
@Component// 该注解依赖spring包, Maven坐标: org.springframework:spring-context:xxx
public class YopDbCredentialsProvider extends YopBaseCredentialsProvider {

    // 1、表结构简单设计
    // ｜-------id--------｜appKey-------｜-----certType-----｜-----certValue-----｜
    // ｜ 1 ｜app_001｜RSA2048|xxxxxxxxxx｜
    // ｜ 2 ｜app_002｜SM2|xxxxxxxxxx｜

    // 2、此处模拟数据库存储
    Map<String, String[]> mockDbStore = new HashMap<>();
    {
        mockDbStore.put("app_001", new String[]{"1", "app_001", "RSA2048", "app_001的密钥串xxxxxx"});
        mockDbStore.put("app_002", new String[]{"2", "app_002", "SM2", "app_002的密钥串xxxxxx"});
    }

    // 3、借助spring 方式, 在provider构造好后，保证系统启动时注册该自定义provider为单例
    @PostConstruct
    public void init() {
        YopCredentialsProviderRegistry.registerProvider(this);
    }

    // 4、指定默认的appKey，当请求时没指定appKey时，会使用该appKey
    @Override
    public String getDefaultAppKey() {
        // 此处简单硬编码，可以根据自身情况指定
        return "app_001";
    }

    // 5、模拟从数据库查询
    private YopCertConfig mockFindCertConfigByAppKey(String appKey) {
        YopCertConfig yopCertConfig = new YopCertConfig();
        String[] dbRow = mockDbStore.get(appKey);
        yopCertConfig.setStoreType(CertStoreType.STRING);
        yopCertConfig.setCertType(CertTypeEnum.parse(dbRow[2]));
        yopCertConfig.setValue(dbRow[3]);
        return yopCertConfig;
    }

    // 6、实现根据appKey和certType，构造调用身份
    @Override
    public YopCredentials<?> getCredentials(String appKey, String credentialType) {
        // 兼容默认appKey的场景
        appKey = useDefaultIfBlank(appKey);

        // 构造调用身份
        YopAppConfig yopAppConfig = new YopAppConfig();
        yopAppConfig.setAppKey(appKey);

        // 从数据库加载密钥配置
        YopCertConfig yopCertConfig = mockFindCertConfigByAppKey(appKey);

        // 装载调用身份
        List<YopCertConfig> isvPrivateKeys = new LinkedList<>();
        isvPrivateKeys.add(yopCertConfig);
        yopAppConfig.setIsvPrivateKey(isvPrivateKeys);
        return buildCredentials(yopAppConfig, credentialType);
    }


    // 7、实现支持的密钥类型，此处根据实际情况返回即可
    @Override
    public List<CertTypeEnum> getSupportCertTypes(String appKey) {
        Set<CertTypeEnum> result = new HashSet<>();
        // 从数据库加载支持的密钥类型列表
        for (Map.Entry<String, String[]> appCert : mockDbStore.entrySet()) {
            result.add(CertTypeEnum.parse(appCert.getValue()[2]));
        }
        return new ArrayList<>(result);
    }
}
