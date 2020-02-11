package org.potados.study_in_java;

public class Home {

    public Home(Blog blog) {

        // 집에 있는 독자 1
        blog.addNewPostListener(new Blog.OnNewPostListener() {
            @Override
            public void onNewPost(Blog blog) {
                System.out.println("이야! 새 글이 올라왔다!");
                for (String str : blog.getPosts()) {
                    System.out.println(str);
                }
            }
        });
    }
}
