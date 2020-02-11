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
        SomeComponent component = new SomeComponent();

        component.setOnEventListener(new SomeComponent.OnEventListener() {
            @Override
            public void onEvent(SomeComponent component) {
                System.out.println("HEY!!! EVENT!!!!");
            }
        });

        new Worker(component).start();
    }
}