package org.potados.study_in_java;

import android.view.View;
import android.widget.Toast;

public class TestClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        Toast.makeText(v.getContext(), "Click!", Toast.LENGTH_SHORT).show();
    }
}
