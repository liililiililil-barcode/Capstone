package kr.ac.uc.home;

import static kr.ac.uc.home.MainActivity.ACTION_SEND_DATA;
import static kr.ac.uc.home.MainActivity.SENSOR_DATA;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.lights.Light;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;//---------------------------

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class home extends AppCompatActivity implements ButtonFragment.OnButtonClickListener {

    private final String TAG = home.class.getSimpleName();

    long readDay = System.currentTimeMillis();// 현재시간
    Date date = new Date(readDay);// 현재시간 data지정
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 날짜 포맷 지정
    String getTime = sdf.format(date);// 날짜 포맷 지정

    int waterLevel = 0; // 물수위 변수

    final int[] arrwaterlevel = {R.drawable.waterlvel1, R.drawable.waterlvel2, R.drawable.waterlvel3, R.drawable.waterlvel4}; // 수정 완료!

    ImageView ivPlant, ivwaterLevel;
    TextView tvPlantedPlantName, tvPlantedDay, tvgrowDay, tvCondition, tvwaterLevelLabel, tvdiary;
    EditText etWaterLevelInput;

    Button btnWaterLeveltest;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_SEND_DATA)) {
                String receivedData = intent.getStringExtra(SENSOR_DATA);
                Log.d(TAG, "리시브 데이터: " + receivedData);

                waterLevel = Integer.parseInt(receivedData);
                waterlevelChange(waterLevel);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // View 초기화
        final TextView title = findViewById(R.id.title); // 제목

        tvwaterLevelLabel = findViewById(R.id.tvwaterLevelLabel); // 물수위 텍스트
        ivwaterLevel = findViewById(R.id.ivwaterLevel); // 물 수위 이미지

        ivPlant = findViewById(R.id.ivPlant); // 심은 식물 이미지
        tvPlantedPlantName = findViewById(R.id.tvPlantedPlantName); // 심은 식물 이름
        tvPlantedDay = findViewById(R.id.tvPlantedDay); // 심은 날짜
        tvgrowDay = findViewById(R.id.tvgrowDay); // 키운 날짜
        tvCondition = findViewById(R.id.tvCondition); // 식물 상태
        tvdiary = findViewById(R.id.tvdiary); // 일기장 작성 여부

        //뷰 초기화 및 기타 코드
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.btnFragment, new ButtonFragment()) // Use fragment_container instead of main
                    .commit();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });




        checkTodayDiary();
        //growDay(); // 그로우데이 수정해줘슈바앍데ㅑㅙ핟ㅈ
        waterlevelChange(waterLevel);
        checkTodayDiary();// 오늘 일기 확인
        growName();// 심은 식물 이름
        //growDay();// 키운 날짜
        //condition();// 식물 상태
        plantImage();// 심은 식물 이미지

    }

    @Override
    public void onButtonClicked(int buttonId) { // 인터페이스 메소드 구현
        // 버튼 ID에 따라 액티비티에 텍스트 출력
        if (buttonId == R.id.btnDiary) {
            Intent intent = new Intent(home.this, Diary.class);
            startActivity(intent);
            finish();
        } else if (buttonId == R.id.btnPlant) {
            Intent intent = new Intent(home.this, Plant.class);
            startActivity(intent);
            finish();
        } else if (buttonId == R.id.btnGame) {
            Intent intent = new Intent(home.this, Game.class);
            startActivity(intent);
            finish();
        }
    }


    private void waterlevelChange(int waterLevel) {// 물수위 센서변화
        this.waterLevel = waterLevel;

        if (waterLevel == 0) {
            tvwaterLevelLabel.setText("물을 채워주세요");
            ivwaterLevel.setImageResource(arrwaterlevel[3]);
        } else if (waterLevel <= 30) {
            tvwaterLevelLabel.setText("부족해요");
            ivwaterLevel.setImageResource(arrwaterlevel[2]);
        } else if (waterLevel <= 60) {
            tvwaterLevelLabel.setText("충분해요");
            ivwaterLevel.setImageResource(arrwaterlevel[1]);
        } else {
            tvwaterLevelLabel.setText("다 채웠어요");
            ivwaterLevel.setImageResource(arrwaterlevel[0]);
        }
    }

    public void checkTodayDiary() {// 오늘 일기 확인
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // 0부터 시작하므로 1 더함
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);//날짜

        String todayFileName = String.format("%d-%02d-%02d.txt", year, month, dayOfMonth); // 오늘 날짜 파일 이름 설정
        File file = new File(getFilesDir(), todayFileName); // 파일 객체 생성

        if (file.exists()) { // 파일이 존재하는지 확인
            tvdiary.setVisibility(View.VISIBLE); // 텍스트뷰 표시
            tvdiary.setText("오늘 일기가 있습니다."); // 일기가 있음을 표시
        } else {
            tvdiary.setVisibility(View.VISIBLE); // 텍스트뷰 표시
            tvdiary.setText("오늘 일기가 없습니다."); // 일기가 없음을 표시
        }
    }
    protected void onResume() { // 브로드캐스트 실행
        super.onResume();
        IntentFilter filter = new IntentFilter(ACTION_SEND_DATA);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() { // 브로드캐스트 중지
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private void updateUI(String data) {
        // 데이터를 사용하여 UI 업데이트
        // 예: TextView textView = findViewById(R.id.dataTextView);
        //     textView.setText(data);

    }


//   public void growDay() {// 키운 날짜
//
//        Intent intent = getIntent();//인텐트 받아오기
//
//        String plantName = intent.getStringExtra("plantName");
//
//        tvPlantedPlantName.setText(plantName);//심은 식물 이름
//
//        String dateData = intent.getStringExtra("dateKey");//심은 날짜 받아오기
//        //날짜 데이터는 YYYY-MM-dd 형식으로 받아옴
//
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); //날짜 포맷 지정
//        //LocalDate startDate = LocalDate.parse(dateData, formatter);//심은 날짜
//
//        LocalDate startDate = LocalDate.parse(dateData, formatter);
//        LocalDate endDate = LocalDate.now();//현재 날짜
//        tvPlantedDay.setText("심은 날짜 : "+startDate);//심은 날짜
//
//        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);//날짜 차이 계산
//
//        tvgrowDay.setText("키운 날짜 : "+daysBetween+" day");//키운 날짜
//
//        tvPlantedDay.setText("심은 날짜 : "+startDate);//심은 날짜
//
//
//
//        // 식물 상태
//        if (daysBetween > 10) {
//            tvCondition.setText("수확 시기 입니다.");
//        } else {
//            tvCondition.setText("잘 자라고 있어요.");
//        }
//
//
//
//    }

    public void growName(){
        Intent intent = getIntent();//인텐트 받아오기

        String plantName = intent.getStringExtra("plantName");

        tvPlantedPlantName.setText(plantName);//심은 식물 이름

    }

   /*public void growDay() {// 키운 날짜

        Intent intent = getIntent();//인텐트 받아오기

        String dateData = intent.getStringExtra("dateKey");//심은 날짜 받아오기
        //날짜 데이터는 YYYY-MM-dd 형식으로 받아옴


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); //날짜 포맷 지정
        LocalDate startDate = LocalDate.parse(dateData, formatter);//심은 날짜

        LocalDate endDate = LocalDate.now();//현재 날짜
        tvPlantedDay.setText("심은 날짜 : "+startDate);//심은 날짜

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);//날짜 차이 계산

        tvgrowDay.setText("키운 날짜 : "+daysBetween+" day");//키운 날짜

        tvPlantedDay.setText("심은 날짜 : "+startDate);//심은 날짜
       }*/
/*
public void condition() {// 식물 상태

    //키운 날짜 받아오기
    int growDay = Integer.parseInt(tvgrowDay.getText().toString());

    // 식물 상태
    if (growDay > 10) {
        tvCondition.setText("수확 시기 입니다.");
    } else {
        tvCondition.setText("잘 자라고 있어요.");
    }

}*/

    public void plantImage() {// 심은 식물 이미지

        String str1 = tvPlantedPlantName.getText().toString() ;


        if (str1.equals("상추")) {
            ivPlant.setImageResource(R.drawable.sangchu);
        }
        if (str1.equals("깻잎")) {
            ivPlant.setImageResource(R.drawable.ggatnip);
        }
        if (str1.equals("스위트 바질")) {
            ivPlant.setImageResource(R.drawable.sweet_basil);
        }
        if (str1.equals("애플 민트")) {
            ivPlant.setImageResource(R.drawable.apple_mint);
        }
        if (str1.isEmpty()) {// 심은 식물이 없을 때

        }
    }


}