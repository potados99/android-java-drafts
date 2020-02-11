package org.potados.study_in_java;

public class SomeComponent {

    private OnEventListener listener = null;

    public SomeComponent() {
        super();
    }

    public void setOnEventListener(OnEventListener listener) {
        this.listener = listener;
    }

    public void handleYourEvent() {
        if (listener != null) {
            listener.onEvent(this);
        }
    }

    public interface OnEventListener {
        void onEvent(SomeComponent component);
    }
}
