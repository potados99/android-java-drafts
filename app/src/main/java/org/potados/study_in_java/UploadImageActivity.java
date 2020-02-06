package org.potados.study_in_java;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 파일 선택 및 업로드하는 액티비티.
 *
 * 호출 흐름:
 *
 * (시간)
 *  |   (대기상태)
 *  |
 *  |   사용자의 버튼 클릭 이벤트
 *  |      -> onClick()
 *  |          -> pickFile()
 *  |               -> startActivityForResult()
 *  |
 *  v   ...(인텐트 처리)...
 *  |
 *  |   인텐트 처리 완료 이벤트
 *  |       -> onActivityResult
 *  |           -> pickiT.getPath() //URI를 통해 실제 경로를 알려줌. 쭉 비동기로 진행됨.
 *  |
 *  v   ...(PickiT의 파일 처리)...
 *  |
 *  |   PickiT 파일 처리 완료 이벤트
 *  |       -> PickiTonCompleteListener()
 *  |           -> uploadFile()
 *  |               -> service.uploadImage()
 *  |
 *  v   ...(Retrofit의 네트워크 작업)...
 *  |
 *  |   Retrofit의 네트워크 작업 완료 이벤트
 *  |       -> onResponse()
 *  |           -> Snackbar.make().show()
 *  |
 *  |   (대기상태)
 *  v
 *
 * 주요 삽질과 이슈:
 *
 *  1. PickiT 라이브러리 선정 배경
 *
 *  안드로이드 시스템은 파일 선택시 해당 파일의 절대경로를 넘겨주지 않고 uri를 넘겨줌.
 *
 *      URI란:
 *      통합 자원 식별자(Uniform Resource Identifier, URI)는 인터넷에 있는 자원을 나타내는 유일한 주소이다.
 *      URI의 존재는 인터넷에서 요구되는 기본조건으로서 인터넷 프로토콜에 항상 붙어 다닌다.
 *      - 위키백과(https://ko.wikipedia.org/wiki/통합_자원_식별자)
 *
 *  uri는 파일의 식별자로써, 해당 파일을 다루는 대부분의 경우에 문제될 것이 없으나,
 *  retrofit2 라이브러리를 사용하여 파일을 업로드하기 위해서는 해당 파일의 절대경로가 필요함.
 *
 *  안드로이드에서 uri를 통해 절대경로를 알아내는 것은 매우 복잡함. 안드로이드 버전에 따라(킷캣 전/후, 오레오 전/후),
 *  로컬파일이 아닌 경우 파일을 복사해서 다른 경로에다 놔야한다. 다른 경로는 안드로이드 시스템이 권장하는 캐시 디렉토리.
 *  URI가 가리키는 곳에 파일이 있다. 바로 가져올 수 없다.. 자바에 inputstream이 있는데 URI를 가지고 URI에 해당하는 파일의 inputstream은 열 수 있다.
 *  inputstream?->꺼내서 읽는 흐름.. 입력용 파일로부터 읽는거. 인풋스트림을 열라면 파일을 지정해줘야한다. 파일 대신 URI로 지정해줄 수 있따.
 *  파일의 실제 경로를 몰라도 URI를 알고있으면 URI를 가리키는 파일의 인풋스트림을 열 수 있다. 인풋스트림을 열면 파일의 내용이 있는데 하나씩 빼와서 캐시 디렉토리에 복사한다.
 *  복사가 끝나면 캐시디렉토리에 있는 파일은 절대 경로를 바로 가져올 수 있다. 이걸 레트로핏에 넘겨준다.
 *
 *  URIHELPER가 안됐던 이유..?
 *
 *  그리고 파일의 위치에 따라, 파일 제공자의 종류에 따라 uri의 형식이 매우 달라짐.
 *  이에 대응하기 위해 가능한 한 모든 경우를 커버하는 UriHelper 클래스를 작성하였으나, 결국 포기함.
 *
 *  따라서 이를 처리해주는 라이브러리를 사용하기로 함.
 *  선정된 라이브러리는 PickiT으로, 주어진 uri 로부터 절대경로를 추출해주는 역할을 수행함.
 *  (링크: https://github.com/HBiSoft/PickiT)
 *
 *  2. 안드로이드 8(또는 이상)에서의 파일 다루기
 *
 *  안드로이드 8 이상에서, 어떤 파일은 uri를 통해 절대경로를 바로 추출하지 못함.
 *  예를 들어, uri가 content://com.android.providers.downloads.documents/document/37 와 같이 주어질 때, (파일명 안보여)
 *  이를 변형한 content://downloads/public_downloads/37 형태의 uri를 사용해 contentProvider에 조회를 요청하여
 *  _data 컬럼을 가져오는 방법으로 절대경로를 알아내야 하는데, 안드로이드 8에서는 이 방법을 사용할 수 없음
 *  (유효하지 않은 uri로 식별됨).
 *
 *
 *  결국 해당 uri가 가리키는 파일의 복사본을 캐시 디렉토리에 새로 만들어 해당 캐시 파일의 경로를 사용해야 함.
 *  이 접근은 안드로이드 8 이상에서 파일 경로를 확보하기 위한 유일한 방법인 것으로 보임.
 *  이 방법에는 단점이 존재함:
 *
 *      1. 파일의 복사본을 생성해야 하므로 오버헤드 발생.
 *      2. 생성된 파일을 지워주어야 할 책임이 있음.
 *
 *  PickiT 라이브러리는 1번 단점을 상쇄하기 위해 비동기 콜백을 제공하고 있으며, 2번을 위해 액티비티 종료시에 실행할 수 있는
 *  deleteTemporaryFile와 같은 캐시 파일 삭제 메소드 또한 제공하고 있음.
 *
 *  PickiT 짱 ><
 */
public class UploadImageActivity extends AppCompatActivity implements PickiTCallbacks {

    ////////////////////////////////////////////////////////////////
    // 필드
    ////////////////////////////////////////////////////////////////

    // 로그 출력할 때에 사용.
    private static final String TAG = "UploadImageActivity";

    // URI로부터 실제 path를 알아내는 데에 사용.
    PickiT pickiT;

    // 권한 요청 시에 사용.
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // 사진 다이얼로그 요청 시에 사용.
    private static int PICK_FROM_FILE = 9999;


    ////////////////////////////////////////////////////////////////
    // AppCompatActivity 콜백
    ////////////////////////////////////////////////////////////////

    /**
     * 액티비티가 초기화될 때에 호출됨.
     * @param savedInstanceState 저장된 이전 상태(화면 방향 전환되었을 때 등에 필요).
     */
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

        // PickiT 초기화
        pickiT = new PickiT(this, this);
        //리스너랑 콜백이랑 같다.
    }

    /**
     * 파일 선택하는 인텐트 처리 후에 호출됨. 이곳에서 pickiT.getPath를 호출하며, 결과는
     * PickiTonCompleteListener 메소드로 전달될 것.
     * @param requestCode 요청 코드
     * @param resultCode 결과 코드
     * @param data 결과가 담긴 객체
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FROM_FILE && resultCode == RESULT_OK) {
            // 파일을 확보하여 path를 구한 뒤에 PickiTonCompleteListener 메소드가 호출됨.
            pickiT.getPath(data.getData(), Build.VERSION.SDK_INT);
        }
    }
    //onActivityResult가 메인쓰레드에서 실행된다.

    /**
     * 권한 요청 처리 후에 호출됨
     * @param requestCode 요청 코드
     * @param permissions 요청한 권한(들)
     * @param grantResults 처리 결과(들)
     */
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
     * 뒤로 가기 버튼이 눌렸을 때에 호출됨.
     */
    @Override
    public void onBackPressed() {
        pickiT.deleteTemporaryFile();
        super.onBackPressed();
    }

    /**
     * 액티비티가 소멸될 때에 호출됨.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            pickiT.deleteTemporaryFile();
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
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, PICK_FROM_FILE);
    }

    /**
     * 주어진 경로의 파일을 서버에 업로드.
     * @param path 올릴 파일의 경로
     * @param desc 파일 설명
     */
    private void uploadFile(String path, String desc) {
        RetrofitService service = RetrofitSingleton.get().create(RetrofitService.class);
        MediaType type = MediaType.parse("multipart/form-data");

        // POST의 description 부분 생성
        RequestBody description = RequestBody.create(type, desc);

        // POST의 file 부분 생성
        File imageFile = new File(path);
        RequestBody reqFile = RequestBody.create(type, imageFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("userFile", imageFile.getName(), reqFile);

        // 이제 올리기
        service.uploadImage(description, filePart).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Snackbar.make(UploadImageActivity.this.findViewById(R.id.root), "성공이라우", Snackbar.LENGTH_SHORT).show();
                }
                else {
                    Snackbar.make(UploadImageActivity.this.findViewById(R.id.root), "실패했다우!!!", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Snackbar.make(UploadImageActivity.this.findViewById(R.id.root), "실패했다우!!!", Snackbar.LENGTH_SHORT).show();
                Toast.makeText(UploadImageActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }


    ////////////////////////////////////////////////////////////////
    // PickiT 콜백
    ////////////////////////////////////////////////////////////////

    /**
     * PickiT의 파일 복사가 시작되었을 때에 호출됨.
     */
    @Override
    public void PickiTonStartListener() {
        Log.d(TAG, "Cache file generation started!");
    }

    /**
     * PickiT의 파일 복사가 진행중일 때에 호출됨.
     * @param progress 진행률 (0-100).
     */
    @Override
    public void PickiTonProgressUpdate(int progress) {
        Log.d(TAG, "Cache file generation in progress: " + progress);
    }

    /**
     * PickiT의 파일 복사가 끝났을 때, 혹은 복사가 필요 없을 때에는 파일 선택 직후에 호출됨.
     * @param path 파일 경로
     * @param wasDriveFile 드라이브에 있는(로컬이 아닌) 파일이었는가?
     * @param wasUnknownProvider 파일 제공자를 알 수 없었는가?
     * @param wasSuccessful 성공적이었는가?
     * @param Reason 만약 성공적이지 않았을 때, 그 이유.
     */
    @Override
    public void PickiTonCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String Reason) {
        Log.d(TAG, "Cache file generation completed!");

        if (wasSuccessful) {
            Log.d(TAG, "The path: " + path);
            uploadFile(path, "success ><");
        }
        else {
            Log.e(TAG, "Path resolutuon failed: " + Reason);
        }
    }
}
