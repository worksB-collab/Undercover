package com.example.spygame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class VoteActivity extends AppCompatActivity {

    private static final String TAG = "VoteActivity";
    private ArrayList<String> playerNames;
    private ArrayList<Integer> playerStatus;
    private int playerNum;
    private int spyNum;
    private int civilianNum;
    private int wbNum;
    private Button btnExit;
    private Button btnResult;
    private CustomAdapter adapter;
    private TextView tvCResult;
    private TextView tvSResult;
    private String[] currentPuzzle;
    private TextView tvRound;
    private TextView tvResult;
    private ArrayList<Integer> killed; // 1>>alive 0>>dead
    private int[] kills;
    private boolean gameOver;
    private TextView tvSureToKillsb;
    private Button btnConfirmKill;
    private Button btnCancelKill;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(VoteActivity.this, 2);
        RecyclerView recyclerView = findViewById(R.id.vote_rv);
        recyclerView.setLayoutManager(layoutManager);
        loadBundle();
        adapter = new CustomAdapter();
        recyclerView.setAdapter(adapter);
        setTopButtons();
        tvRound = findViewById(R.id.vote_tv_round);
        tvResult = findViewById(R.id.vote_tv_result);
        tvRound.setText(1 + "");
        tvResult.setText(R.string.vote_instruction_choose_a_player);
        kills = new int[3];
        gameOver = false;
    }

    private void loadBundle() {
        Bundle bundle = this.getIntent().getExtras();
        civilianNum = bundle.getInt("civilianNum");
        spyNum = bundle.getInt("spyNum");
        wbNum = bundle.getInt("wbNum");
        playerNum = civilianNum + spyNum + wbNum;
        playerNames = bundle.getStringArrayList("playerNames");
        playerStatus = bundle.getIntegerArrayList("playerStatus");
        currentPuzzle = bundle.getStringArray("currentPuzzle");
        killed = new ArrayList<>();
        for (int i = 0; i < playerNames.size(); i++) {
            killed.add(new Integer(1));
        }
    }

    private String checkPlayerStatus(int index) {
        String status = "";
        switch (playerStatus.get(index)) {
            case 0:
                status = getString(R.string.vote_civilian);
                kills[0] += 1;
                break;
            case 1:
                status = getString(R.string.vote_spy);
                kills[1] += 1;
                break;
            case 2:
                status = getString(R.string.vote_wb);
                kills[2] += 1;
                break;
        }
        return status;
    }

    private void setTopButtons() {

        btnExit = findViewById(R.id.vote_btn_exit);
        btnResult = findViewById(R.id.vote_btn_see_puzzle);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VoteActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog resultDialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(VoteActivity.this);
                LayoutInflater inflater = LayoutInflater.from(VoteActivity.this);
                View view = inflater.inflate(R.layout.dialog_result, null);
                resultDialog = builder
                        .setView(view)
                        .show();
                tvCResult = view.findViewById(R.id.result_dialog_tv_civilian);
                tvSResult = view.findViewById(R.id.result_dialog_tv_spy);
                tvCResult.setText(currentPuzzle[0]);
                tvSResult.setText(currentPuzzle[1]);
            }
        });
    }

    private class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate((R.layout.vote_player_grid), parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            final ItemViewHolder ivh = (ItemViewHolder) holder;
            ivh.tvPlayerName.setText(playerNames.get(position));
            ivh.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!ivh.tvRole.getText().toString().equals("")){
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(VoteActivity.this);
                    LayoutInflater inflater = LayoutInflater.from(VoteActivity.this);
                    View view = inflater.inflate(R.layout.dialog_are_you_sure_kill, null);
                    tvSureToKillsb = view.findViewById(R.id.vote_tv_sure_to_kill_somebody);
                    btnCancelKill = view.findViewById(R.id.vote_btn_cancel_kill);
                    btnConfirmKill = view.findViewById(R.id.vote_btn_confirm_kill);
                    final AlertDialog areYouSureDialog = builder
                            .setView(view)
                            .setCancelable(false)
                            .create();
                    if (gameOver == true) {
                        ivh.tvRole.setText(checkPlayerStatus(position));
                        return;
                    }
                    areYouSureDialog.show();
                    String sureKillText = getString(R.string.vote_dialog_are_you_sure) + playerNames.get(position)+"?";
                    tvSureToKillsb.setText(sureKillText);
                    btnCancelKill.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            areYouSureDialog.dismiss();
                        }
                    });
                    btnConfirmKill.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ivh.tvRole.setText(checkPlayerStatus(position));
                            killed.set(position, 0);
                            showResult();
                            areYouSureDialog.dismiss();
                        }
                    });

                }
            });
        }

        @Override
        public int getItemCount() {
            return playerNames.size();
        }
    }


    private void showResult(){
        if (wbNum > 0 && kills[2] != 1) {
            if (civilianNum == 1 && civilianNum == kills[0]) {
                tvResult.setText(R.string.vote_wb_win);
                gameOver = true;
                return;

            }
            if (spyNum == kills[1] && totalKills() < spyNum +wbNum+ 1) {

                tvResult.setText(R.string.vote_wb_win);
                gameOver = true;
                return;
            }
            if (spyNum - kills[1] > 0 && totalKills() == spyNum +wbNum+1) {
                tvResult.setText(R.string.vote_wb_win);
                gameOver = true;
                return;
            }

        }

        if (spyNum == kills[1] && totalKills() <= spyNum +wbNum+ 1) {
            tvResult.setText(R.string.vote_civilian_win);
            gameOver = true;
            return;
        }
        if (spyNum - kills[1] > 0 && totalKills() == spyNum +wbNum+ 1) {
            tvResult.setText(R.string.vote_spy_win);
            gameOver = true;
            return;
        }
        tvRound.setText(Integer.parseInt(tvRound.getText().toString()) + 1 + "");

    }


    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView tvPlayerName;
        private TextView tvRole;
        private LinearLayout layout;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlayerName = itemView.findViewById(R.id.vote_tv_player_name);
            tvRole = itemView.findViewById(R.id.vote_tv_role);
            layout = itemView.findViewById(R.id.layout_tap_to_kill);
        }
    }

    private int totalKills() {
        return kills[0] + kills[1] + kills[2];
    }
}

