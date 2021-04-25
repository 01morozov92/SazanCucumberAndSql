package vars;

public class LocalThead {

    private static final ThreadLocal<TestVars> threadLocalScope = new ThreadLocal<>();

    private LocalThead() {
    }

    public static void setTestVars(TestVars var) {
        threadLocalScope.set(var);
    }

    public static TestVars getTestVars() {
        return threadLocalScope.get();
    }
}
