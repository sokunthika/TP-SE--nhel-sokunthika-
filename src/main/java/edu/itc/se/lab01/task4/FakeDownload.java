
package edu.itc.se.lab01.task4;

import java.io.IOException;
import java.util.concurrent.Callable;

public class FakeDownload implements Callable<String> {
    private final String url;
    private final boolean shouldFail;
    private final boolean shouldBeSlow;

    public FakeDownload(
            String url,
            boolean shouldFail,
            boolean shouldBeSlow) {
        this.url = url;
        this.shouldFail = shouldFail;
        this.shouldBeSlow = shouldBeSlow;
    }

    @Override
    public String call() throws Exception {
        if (shouldFail) {
            throw new IOException("boom");
        }

        Thread.sleep(shouldBeSlow ? 5_000 : 200);

        return url + " -> " + url.length();
    }
}
