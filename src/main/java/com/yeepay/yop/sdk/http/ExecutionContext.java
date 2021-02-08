package com.yeepay.yop.sdk.http;


import com.yeepay.yop.sdk.auth.Encryptor;
import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.signer.YopSigner;

public class ExecutionContext {

    private final YopSigner signer;

    private final SignOptions signOptions;

    private final Encryptor encryptor;

    private final YopCredentials yopCredentials;

    private ExecutionContext(YopSigner signer, SignOptions signOptions, Encryptor encryptor, YopCredentials yopCredentials) {
        this.signer = signer;
        this.signOptions = signOptions;
        this.encryptor = encryptor;
        this.yopCredentials = yopCredentials;
    }

    public YopSigner getSigner() {
        return signer;
    }

    public SignOptions getSignOptions() {
        return signOptions;
    }

    public Encryptor getEncryptor() {
        return encryptor;
    }

    public YopCredentials getYopCredentials() {
        return yopCredentials;
    }

    public static final class Builder {
        private YopSigner signer;
        private SignOptions signOptions;
        private Encryptor encryptor;
        private YopCredentials yopCredentials;

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

        public Builder withEncryptor(Encryptor encryptor) {
            this.encryptor = encryptor;
            return this;
        }

        public Builder withYopCredentials(YopCredentials yopCredentials) {
            this.yopCredentials = yopCredentials;
            return this;
        }

        public ExecutionContext build() {
            return new ExecutionContext(signer, signOptions, encryptor, yopCredentials);
        }
    }
}
