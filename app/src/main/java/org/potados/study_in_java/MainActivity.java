package org.potados.study_in_java;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.potados.study_in_java.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    MainViewModel vm = new MainViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLifecycleOwner(this);
        binding.setVm(vm);

        binding.countUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vm.onButtonClick();
            }
        });

        // startActivity(new Intent(MainActivity.this, UploadImageActivity.class));

        // 첫 번째 방법.
        binding.newActivityButton.setOnClickListener(new TestClickListener());

        // 두 번째 방법.
        binding.newActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Click!", Toast.LENGTH_SHORT).show();
            }
        });

        // 2.5번째 방법.
        // 구현해야 할 메소드가 하나일 때!!!!!!!!!!!! 람다식 사용 삽가능!!
        binding.newActivityButton.setOnClickListener(v -> Toast.makeText(v.getContext(), "Click!", Toast.LENGTH_SHORT).show());

        // 세 번째 방법.
        binding.newActivityButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(v.getContext(), "Click!", Toast.LENGTH_SHORT).show();
    }
}
