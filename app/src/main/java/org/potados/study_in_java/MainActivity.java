package org.potados.study_in_java;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.potados.study_in_java.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
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


        binding.newActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UploadImageActivity.class));
            }
        });
    }
}
