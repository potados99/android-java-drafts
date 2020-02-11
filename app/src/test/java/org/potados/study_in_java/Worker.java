package org.potados.study_in_java;

public class Worker {
    Thread t;

    public Worker(final Blog component) {

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    component.writeNewPost();

                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void start() {
        t.start();

        try {
            t.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
