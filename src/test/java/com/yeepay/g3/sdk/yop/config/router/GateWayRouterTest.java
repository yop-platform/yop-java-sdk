package com.yeepay.g3.sdk.yop.config.router;

import com.google.common.base.Charsets;
import com.yeepay.g3.sdk.yop.client.YopConstants;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.router.GateWayRouter;
import com.yeepay.g3.sdk.yop.client.router.ServerRootSpace;
import com.yeepay.g3.sdk.yop.client.router.SimpleGateWayRouter;
import com.yeepay.g3.sdk.yop.config.AppSdkConfigProviderRegistry;
import com.yeepay.g3.sdk.yop.config.SDKConfig;
import com.yeepay.g3.sdk.yop.config.enums.ModeEnum;
import com.yeepay.g3.sdk.yop.config.provider.BaseFixedAppSdkConfigProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2019<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-03-13 14:21
 */
public class GateWayRouterTest {

    private ServerRootSpace serverRootSpace;

    private String bancEncryptionServerRoot = "https://openapi-bank-encryption.yeepay.com/yop-center";
    private String bancEncryptionYosServerRoot = "https://yos.yeepay.com/yop-center";

    @Before
    public void before() throws MalformedURLException {
        AppSdkConfigProviderRegistry.registerCustomProvider(new MockAppSdkConfigProvider());
        serverRootSpace = new ServerRootSpace(YopConstants.DEFAULT_SERVER_ROOT, YopConstants.DEFAULT_YOS_SERVER_ROOT,
                YopConstants.DEFAULT_SANDBOX_SERVER_ROOT);
    }

    @Test
    public void test() {
        GateWayRouter gateWayRouter = new SimpleGateWayRouter(serverRootSpace);

        String simpleApiUri = "/rest/v1.0/auth/test";
        String independentGroupApiUri = "/rest/v1.0/bank-encryption/test";

        String appKey = "test";
        String serverRoot = gateWayRouter.route(simpleApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(simpleApiUri, getYosRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_YOS_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(bancEncryptionServerRoot, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getYosRequest(appKey));
        Assert.assertEquals(bancEncryptionYosServerRoot, serverRoot);

        appKey = "test1";
        serverRoot = gateWayRouter.route(simpleApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(simpleApiUri, getYosRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_YOS_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(bancEncryptionServerRoot, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getYosRequest(appKey));
        Assert.assertEquals(bancEncryptionYosServerRoot, serverRoot);

        appKey = "test2";
        serverRoot = gateWayRouter.route(simpleApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(simpleApiUri, getYosRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getYosRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);
    }

    @Test
    public void test1() {
        System.setProperty("yop.sdk.mode", "prod");
        GateWayRouter gateWayRouter = new SimpleGateWayRouter(serverRootSpace);

        String simpleApiUri = "/rest/v1.0/auth/test";
        String independentGroupApiUri = "/rest/v1.0/bank-encryption/test";

        String appKey = "test";
        String serverRoot = gateWayRouter.route(simpleApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(simpleApiUri, getYosRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_YOS_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(bancEncryptionServerRoot, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getYosRequest(appKey));
        Assert.assertEquals(bancEncryptionYosServerRoot, serverRoot);

        appKey = "test1";
        serverRoot = gateWayRouter.route(simpleApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(simpleApiUri, getYosRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_YOS_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(bancEncryptionServerRoot, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getYosRequest(appKey));
        Assert.assertEquals(bancEncryptionYosServerRoot, serverRoot);

        appKey = "test2";
        serverRoot = gateWayRouter.route(simpleApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(simpleApiUri, getYosRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_YOS_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(bancEncryptionServerRoot, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getYosRequest(appKey));
        Assert.assertEquals(bancEncryptionYosServerRoot, serverRoot);
    }

    @Test
    public void test2() {
        System.setProperty("yop.sdk.mode", "sandbox");
        GateWayRouter gateWayRouter = new SimpleGateWayRouter(serverRootSpace);

        String simpleApiUri = "/rest/v1.0/auth/test";
        String independentGroupApiUri = "/rest/v1.0/bank-encryption/test";

        String appKey = "test";
        String serverRoot = gateWayRouter.route(simpleApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(simpleApiUri, getYosRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getYosRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);

        appKey = "test1";
        serverRoot = gateWayRouter.route(simpleApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(simpleApiUri, getYosRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getYosRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);

        appKey = "test2";
        serverRoot = gateWayRouter.route(simpleApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(simpleApiUri, getYosRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getSimpleRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);
        serverRoot = gateWayRouter.route(independentGroupApiUri, getYosRequest(appKey));
        Assert.assertEquals(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT, serverRoot);
    }


    private YopRequest getSimpleRequest(String appKey) {
        return new YopRequest(appKey);
    }

    private YopRequest getYosRequest(String appKey) {
        YopRequest request = new YopRequest(appKey);
        request.addFile("test", new ByteArrayInputStream("test".getBytes(Charsets.UTF_8)));
        return request;
    }


    class MockAppSdkConfigProvider extends BaseFixedAppSdkConfigProvider {

        @Override
        protected List<SDKConfig> loadCustomSdkConfig() {
            List<SDKConfig> sdkConfigs = new ArrayList<>();

            //不设置mode
            SDKConfig sdkConfig = new SDKConfig();
            sdkConfig.setServerRoot(YopConstants.DEFAULT_SERVER_ROOT);
            sdkConfig.setYosServerRoot(YopConstants.DEFAULT_YOS_SERVER_ROOT);
            sdkConfig.setSandboxServerRoot(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT);
            sdkConfig.setDefaulted(true);
            sdkConfig.setAppKey("test");
            sdkConfigs.add(sdkConfig);

            //设置mode为pro
            sdkConfig = new SDKConfig();
            sdkConfig.setServerRoot(YopConstants.DEFAULT_SERVER_ROOT);
            sdkConfig.setYosServerRoot(YopConstants.DEFAULT_YOS_SERVER_ROOT);
            sdkConfig.setSandboxServerRoot(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT);
            sdkConfig.setAppKey("test1");
            sdkConfig.setMode(ModeEnum.prod);
            sdkConfigs.add(sdkConfig);

            //设置mode为sandbox
            sdkConfig = new SDKConfig();
            sdkConfig.setServerRoot(YopConstants.DEFAULT_SERVER_ROOT);
            sdkConfig.setYosServerRoot(YopConstants.DEFAULT_YOS_SERVER_ROOT);
            sdkConfig.setSandboxServerRoot(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT);
            sdkConfig.setAppKey("test2");
            sdkConfig.setMode(ModeEnum.sandbox);
            sdkConfigs.add(sdkConfig);


            return sdkConfigs;
        }
    }
}
