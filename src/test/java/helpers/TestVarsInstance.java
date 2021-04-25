package helpers;

import vars.TestVars;

public class TestVarsInstance {

    private static volatile TestVarsInstance testVarsInstance;
    private static final TestVars testVars = new TestVars();

    public TestVars getTestVars() {
        return testVars;
    }

    public static TestVarsInstance getTestVarsInstance() {
        if (testVarsInstance == null)
            synchronized (TestVarsInstance.class) {
                testVarsInstance = new TestVarsInstance();
            }
        return testVarsInstance;
    }
}
