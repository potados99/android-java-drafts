package org.potados.study_in_java;

public class Company {

    public Company(Blog blog) {

        // 회사에 있는 독자 2
        blog.addNewPostListener(new Blog.OnNewPostListener() {
            @Override
            public void onNewPost(Blog blog) {
                System.out.println("\n!! 퇴근하면 읽어보러 가야지 !!\n");
            }
        });
    }
}
