package com.yeepay.yop.sdk.http;


import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.base.auth.signer.YopSigner;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptor;

import java.util.concurrent.Future;

public class ExecutionContext {

    private final YopSigner signer;

    private final SignOptions signOptions;

    private boolean encryptSupported;

    private final YopEncryptor encryptor;

    private final YopCredentials<?> yopCredentials;

    private final Future<EncryptOptions> encryptOptions;

    private ExecutionContext(YopSigner signer, SignOptions signOptions, YopCredentials<?> yopCredentials,
                             YopEncryptor encryptor, Future<EncryptOptions> encryptOptions) {
        this.signer = signer;
        this.signOptions = signOptions;
        this.encryptor = encryptor;
        this.yopCredentials = yopCredentials;
        this.encryptOptions = encryptOptions;
        this.encryptSupported = null != encryptor && null != encryptOptions;
    }

    public YopSigner getSigner() {
        return signer;
    }

    public SignOptions getSignOptions() {
        return signOptions;
    }

    public boolean isEncryptSupported() {
        return encryptSupported;
    }

    public YopEncryptor getEncryptor() {
        return encryptor;
    }

    public YopCredentials<?> getYopCredentials() {
        return yopCredentials;
    }

    public Future<EncryptOptions> getEncryptOptions() {
        return encryptOptions;
    }

    public static final class Builder {
        private YopSigner signer;
        private SignOptions signOptions;
        private YopCredentials<?> yopCredentials;
        private YopEncryptor encryptor;
        private Future<EncryptOptions> encryptOptions;

        private Builder() {
        }

        public static Builder anExecutionContext() {
            return new Builder();
        }

        public Builder withSigner(YopSigner signer) {
            this.signer = signer;
            return this;
        }

        public Builder withSignOptions(SignOptions signOptions) {
            this.signOptions = signOptions;
            return this;
        }

        public Builder withYopCredentials(YopCredentials<?> yopCredentials) {
            this.yopCredentials = yopCredentials;
            return this;
        }

        public Builder withEncryptor(YopEncryptor encryptor) {
            this.encryptor = encryptor;
            return this;
        }

        public Builder withEncryptOptions(Future<EncryptOptions> encryptOptions) {
            this.encryptOptions = encryptOptions;
            return this;
        }

        public ExecutionContext build() {
            return new ExecutionContext(signer, signOptions, yopCredentials, encryptor, encryptOptions);
        }
    }
}
