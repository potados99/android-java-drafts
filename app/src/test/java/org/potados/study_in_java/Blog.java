package org.potados.study_in_java;

import java.util.ArrayList;
import java.util.List;

public class Blog {

    private ArrayList<OnNewPostListener> listeners = new ArrayList<>();

    public Blog() {
        super();
    }

    public List<String> getPosts() {
        ArrayList<String> l = new ArrayList<>();
        l.add("BLAH BLAH");
        l.add("LOREM IPSUM DOLOR AMIT");
        return l;
    }

    public void addNewPostListener(OnNewPostListener listener) {
        this.listeners.add(listener);
    }
















    public void writeNewPost() {
        // Write some posts....

        // Tell the subscribers that new post(s) published.
        for (OnNewPostListener l : listeners) {
            l.onNewPost(this);
        }
    }

    public interface OnNewPostListener {
        void onNewPost(Blog blog);
    }
}
