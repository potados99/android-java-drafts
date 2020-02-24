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


        TokenManager tm = TokenManager.getInstance();

        String token = tm.getToken(this);
        if (token != null) {
            // 자동로그인 ㄱ
            // 토큰을 가지고 서버에 바로 로그인 요청 전송
        } else{
            // 자동로그인 불가.
            // 로그인 화면 띄워야 함.
            // 로그인 후에는 tm.putToken(context, token) 해줌.
        }




        binding.countUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vm.onButtonClick();
            }
        });

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
        // Toast.makeText(v.getContext(), "Click!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MainActivity.this, UploadImageActivity.class));
    }
}
