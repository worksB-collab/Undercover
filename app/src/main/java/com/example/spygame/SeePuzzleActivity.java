package com.example.spygame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class SeePuzzleActivity extends AppCompatActivity {

    private static final String TAG = "SeePuzzleActicity";
    private TextView tvSeeUrPuzzle;
    private TextView tvPuzzle;
    private TextView tvPlayerNumber; // 目前數到哪個玩家
    private TextView tvDoubleTapAction;
    private boolean isClicked;
    private int stage;
    private int playerNumber; // the player for now
    private ArrayList<String> playerNames;
    private ArrayList<Integer> playerStatus; // 0>civilian 1>spy 2>wb
    private LinearLayout layout;
    private int playerNum;
    private int spyNum;
    private int civilianNum;
    private int wbNum;
    private String[] currentPuzzle;
    private JSONArray jsonArrayPuzzle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_puzzle);
        init();
        loadBundle();
        setCharacter();
        setPuzzle();
        Log.d(TAG, "currentPuzzle" + currentPuzzle[0] + "," + "currentPuzzle" + currentPuzzle[1]);
        setStage();
        setScreenTouchListener();
    }

    private void init() {
        tvPuzzle = findViewById(R.id.see_puzzle_tv_puzzle);
        tvSeeUrPuzzle = findViewById(R.id.see_puzzle_tv_see_ur_puzzle);
        tvPlayerNumber = findViewById(R.id.see_puzzle_tv_player_name);
        tvDoubleTapAction = findViewById(R.id.see_puzzle_tv_double_tap);
        isClicked = false;
        stage = 0;
        playerNumber = 0;
        layout = findViewById(R.id.see_puzzle_linear_layout);
        playerStatus = new ArrayList<>();
        playerNames = new ArrayList<>();
        currentPuzzle = new String[2];
        jsonArrayPuzzle = MainActivity.jsonArrayPuzzle;
    }

    private void loadBundle() {

        Bundle bundle = this.getIntent().getExtras();
        civilianNum = bundle.getInt("civilianNum");
        spyNum = bundle.getInt("spyNum");
        wbNum = bundle.getInt("wbNum");
        playerNum = civilianNum + spyNum + wbNum;
        playerNames = bundle.getStringArrayList("playerNames");
    }

    private void setCharacter() {
        for (int i = 0; i < playerNames.size(); i++) {
            playerStatus.add(-1);
        }

        if (wbNum == 0) {
            for (int i = 0; i < spyNum; i++) {
                int random = (int) (Math.random() * playerStatus.size());
                if (playerStatus.get(random) == -1) {
                    playerStatus.set(random, 1);
                } else {
                    i--;
                }
            }
            for (int i = 0; i < playerNames.size(); i++) {
                if (playerStatus.get(i) == -1) {
                    playerStatus.set(i, 0);
                }
            }
            return;
        }
        for (int i = 0; i < spyNum; i++) {
            int random = (int) (Math.random() * playerStatus.size());
            if (playerStatus.get(random) == -1) {
                playerStatus.set(random, 1);
            } else {
                i--;
            }
        }
        int random;
        do {
            random = (int) (Math.random() * (playerNames.size()));
        } while (playerStatus.get(random) != -1);
        playerStatus.set(random, 2);
        for (int i = 0; i < playerNames.size(); i++) {
            if (playerStatus.get(i) == -1) {
                playerStatus.set(i, 0);
            }
        }
    }

    private void setCharacterBySuffle() {

    }

    private void setPuzzle() {
        try {
            String[] s = jsonArrayPuzzle.getString((int) (Math.random() * jsonArrayPuzzle.length())).split(",");
            currentPuzzle = s;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (MainActivity.customPuzzles == null || MainActivity.customPuzzles[0] == null && MainActivity.customPuzzles[1] == null ||
                MainActivity.customPuzzles[0].equals("") || MainActivity.customPuzzles[1].equals("")) {
            return;
        }
        currentPuzzle = MainActivity.customPuzzles;
    }

    private void setStage() {
        switch (stage) {
            case 0:
                tvSeeUrPuzzle.setText("");
                tvPuzzle.setText("");
                tvDoubleTapAction.setText(R.string.see_puzzle_double_tap_to_see_your_puzzle);
                tvPlayerNumber.setText(playerNames.get(playerNumber) + "");
                break;
            case 1:
                tvSeeUrPuzzle.setText(R.string.see_puzzle_your_puzzle_is);
                switch (playerStatus.get(playerNumber)) {
                    case 0:
                        tvPuzzle.setText(currentPuzzle[0]);
                        break;
                    case 1:
                        tvPuzzle.setText(currentPuzzle[1]);
                        break;
                    case 2:
                        tvPuzzle.setText(R.string.see_puzzle_you_are_white_board);
                        break;
                }
                tvDoubleTapAction.setText(R.string.see_puzzle_double_tap_and_pass_to_next);
                playerNumber++;
                break;
            case 2:
                tvSeeUrPuzzle.setText(R.string.see_puzzle_your_puzzle_is);
                switch (playerStatus.get(playerNumber)) {
                    case 0:
                        tvPuzzle.setText(currentPuzzle[0]);
                        break;
                    case 1:
                        tvPuzzle.setText(currentPuzzle[1]);
                        break;
                    case 2:
                        tvPuzzle.setText(R.string.see_puzzle_you_are_white_board);
                        break;
                }
                tvDoubleTapAction.setText(R.string.see_puzzle_double_tap_to_next_stage);
//                Log.d("SeePuzzleActicity", "case 2");
                break;
        }
    }


    private void setScreenTouchListener() {
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isClicked) {
                    // move to the next one
                    if (playerNumber == playerNames.size() - 1 && stage == 0) {
                        stage = 2;
                    } else if (stage == 1) {
                        stage = 0;
                    } else if (stage == 2) {
                        jumpToVote();
                    } else {
                        stage++;
                    }
                    setStage();
                    return false;
                }

                isClicked = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isClicked = false;
                    }
                }, 200);
                return false;
            }
        });
    }

    public void jumpToVote() {
        Intent intent = new Intent(SeePuzzleActivity.this, VoteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("playerNames", playerNames);
        bundle.putIntegerArrayList("playerStatus", playerStatus);
        bundle.putInt("civilianNum", civilianNum);
        bundle.putInt("spyNum", spyNum);
        bundle.putInt("wbNum", wbNum);
        bundle.putStringArray("currentPuzzle", currentPuzzle);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }
}
