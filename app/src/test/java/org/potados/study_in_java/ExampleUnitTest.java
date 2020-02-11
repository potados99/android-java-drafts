package org.potados.study_in_java;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void workerTest() {
        // 전지적 시점

        Blog blog = new Blog();

        new Home(blog);

        new Company(blog);

        new Worker(blog).start();
    }
}