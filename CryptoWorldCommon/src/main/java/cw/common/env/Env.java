package cw.common.env;

public enum Env {
    NONE("none"),
    DEV("dev"),
    PROD("prod");

    private final String envName;

    Env(String envName) {
        this.envName = envName;
    }

    public String getEnvName() {
        return this.envName;
    }
}
