package org.potados.study_in_java;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 이 액티비티 안에서 일어나는 일:
 *
 * - 사용자가 파일 선택을 요청했을 때, 파일을 선택하는 다이얼로그를 띄움.
 * - 파일 선택 다이얼로그에서 사진 선택이 완료되면, 그 결과가 콜백으로 전달됨.
 * - 결과가 콜백으로 전달되면 서버로 파일 업로드가 시작됨.
 */
public class UploadImageActivity extends AppCompatActivity {

    private static final String TAG = "UploadImageActivity";

    // 권한 요청 시에 사용됨.
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // 사진 다이얼로그 요청 시에 사용됨.
    private static int PICK_FROM_FILE = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        // 권한내놔!
        giveMePermissions();

        // 버튼 클릭 리스너 설정 부분
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFile();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FROM_FILE && resultCode == RESULT_OK) {
            uploadFile(data.getData());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // good
            } else {
                Snackbar.make(UploadImageActivity.this.findViewById(R.id.root), "권한내놔!!!!!!!!", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 외장 저장소 읽기/쓰기 권한을 요청함.
     */
    private void giveMePermissions() {
        int readPermission = ActivityCompat.checkSelfPermission(UploadImageActivity.this, PERMISSIONS_STORAGE[0]);
        int writePermission = ActivityCompat.checkSelfPermission(UploadImageActivity.this, PERMISSIONS_STORAGE[1]);
        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UploadImageActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    /**
     * 이미지 선택 창을 띄움. 그 결과는 선택 후에 onActivityResult 메소드로 전달될 것.
     */
    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(intent, PICK_FROM_FILE);
    }

    /**
     * 파일의 서버에 업로드함.
     * @param uri 업로드할 파일의 uri.
     */
    private void uploadFile(Uri uri) {
        RetrofitService service = RetrofitSingleton.get().create(RetrofitService.class);
        MediaType type = MediaType.parse("multipart/form-data");

        // POST의 description 부분 생성
        RequestBody description = RequestBody.create(type, "메시지이이이이이이");

        // POST의 file 부분 생성
        String path = UriHelper.getPath(this, uri);
        Log.d(TAG, "Path secured: " + path);
        File imageFile = new File(path);
        RequestBody reqFile = RequestBody.create(type, imageFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("userFile", imageFile.getName(), reqFile);

        // 이제 올리기
        service.uploadImage(description, filePart).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Snackbar.make(UploadImageActivity.this.findViewById(R.id.root), "성공이라우", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Snackbar.make(UploadImageActivity.this.findViewById(R.id.root), "실패했다우!!!", Snackbar.LENGTH_SHORT).show();
                Toast.makeText(UploadImageActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
}
