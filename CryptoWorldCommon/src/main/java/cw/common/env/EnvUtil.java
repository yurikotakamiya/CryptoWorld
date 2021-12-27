package cw.common.env;

public class EnvUtil {
    private static final String MAC = "Mac";
    public static final Env ENV = System.getProperty("os.name").contains(MAC) ? Env.DEV : Env.PROD;
}
