package com.example.spygame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView tvPlayerNum;
    private SeekBar sbPlayerNum;
    private TextView tvSpyNum;
    private Button btnSpyMinus;
    private Button btnSpyPlus;
    private TextView tvCivilianNum;
    private Switch switchAddWB;
    private Switch switchCustomName;
    private Switch switchCustomPuzzle;
    private Button btnStartGame;
    private int playerNum;
    private int spyNum;
    private int civilianNum;
    private int wbNum;
    private CustomAdapter adapter;
    private AlertDialog cNameDialog;
    private ArrayList<String> playerNames;
    public static JSONArray jsonArrayPuzzle;
    private int playerSum;
    private AlertDialog loadingDialog;
    private Button btnCPSave;
    private Button btnCPCancel;
    private EditText etCPCPuzzle;
    private EditText etCPSPuzzle;
    public static String [] customPuzzles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        loadSharedPreferences();
        setSbPlayerNum();
        setSpyNum();
        setAddWB();
        setBtnStartGame();
        cNameDialog = setSwitchCustomName();
        setSwitchCustomPuzzle();
        getPuzzles();
        showLoadingDialog();
    }

    private void showLoadingDialog() {
        if(jsonArrayPuzzle!=null){
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_loading, null);
        loadingDialog = builder
                .setView(view)
                .setCancelable(false)
                .show();

    }

    private void init() {
        tvPlayerNum = findViewById(R.id.main_tv_playerNum);
        sbPlayerNum = findViewById(R.id.main_sb_playerNum);
        tvSpyNum = findViewById(R.id.main_tv_spyNum);
        tvCivilianNum = findViewById(R.id.main_tv_civilianNum);
        btnSpyMinus = findViewById(R.id.main_btn_spyMinus);
        btnSpyPlus = findViewById(R.id.main_btn_spyPlus);
        switchAddWB = findViewById(R.id.main_switch_addWB);
        switchCustomName = findViewById(R.id.main_switch_customNames);
        switchCustomPuzzle = findViewById(R.id.main_switch_customPuzzle);
        btnStartGame = findViewById(R.id.main_btn_startGame);
        customPuzzles = new String [2];
    }

    private void loadSharedPreferences() {

        SharedPreferences sp = getSharedPreferences(Global.SP_NAMES, MODE_PRIVATE);
        if (sp.getInt("civilianNum", -1) == 0 ||
                sp.getInt("civilianNum", -1) == -1) {
            genDefaltData();
            return;
        }
        civilianNum = sp.getInt("civilianNum", 0);
        spyNum = sp.getInt("spyNum", 0);
        wbNum = sp.getInt("wbNum", 0);
        playerNum = civilianNum + spyNum + wbNum;
        playerNames = new ArrayList<>();
        for (int i = 0; i < playerNum; i++) {
            playerNames.add(sp.getString("player" + i, ""));
        }

    }

    private void saveSharedPreferences() {
        SharedPreferences sp = getSharedPreferences(Global.SP_NAMES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("civilianNum", civilianNum)
                .putInt("spyNum", spyNum)
                .putInt("wbNum", wbNum);
    for (int i = 0; i < playerNum; i++) {
            Log.d(TAG, playerNames.get(i));
            editor.putString("player" + i, playerNames.get(i));
        }
        editor.commit();
    }

    private void genDefaltData() {
        playerNum = 3;
        wbNum = 0;
        civilianNum = 2;
        spyNum = 1;
        playerNames = new ArrayList<>();
        for (int i = 0; i < playerNum; i++) {
            playerNames.add(getString(R.string.main_player) + " " + (i + 1));
        }
        setCivilianNum();
    }

    private void setSbPlayerNum() {
        playerSum = playerNum;
        tvPlayerNum.setText(playerNum + "");
        sbPlayerNum.setProgress(playerNum - 3);
        sbPlayerNum.setMax(10);
        sbPlayerNum.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                playerNum = progress + 3;
                ifWBnotEnough();
                if (spyNum >= playerNum / 3){
                    spyNum = playerNum / 3;
                    tvSpyNum.setText(spyNum+"");
                }
                tvPlayerNum.setText(playerNum + "");
                int minus = Math.abs(playerSum - playerNum);
                setCivilianNum();
                if (playerNum > playerSum) {
                    for (int i = 0; i < minus; i++) {
                        playerNames.add(getString(R.string.main_player) + " " + (playerSum + 1));
                        playerSum++;

                    }
                } else if (playerNum < playerSum) {
                    for (int i = 0; i < minus; i++) {
                        playerNames.remove(playerSum - 1);
                        playerSum--;
                    }
                }

                Log.d(TAG, "wb num " + wbNum);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setSpyNum() {
        tvSpyNum.setText(spyNum + "");
        btnSpyMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spyNum == 1) {
                    return;
                }
                tvSpyNum.setText(--spyNum + "");
                setCivilianNum();
            }
        });
        btnSpyPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spyNum == playerNum / 3)
                    return;
                tvSpyNum.setText(++spyNum + "");
                setCivilianNum();
            }
        });
    }

    private boolean ifWBnotEnough(){
        if(playerNum<4 && wbNum==1){
            Toast.makeText(MainActivity.this,R.string.main_toast_not_enough_player, Toast.LENGTH_SHORT).show();
            switchAddWB.setChecked(false);
            wbNum =0;
            return true;
        }
        return false;
    }

    private void setAddWB() {

        if (wbNum == 1) {
            switchAddWB.setChecked(true);
        } else {
            switchAddWB.setChecked(false);
        }
        switchAddWB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ifWBnotEnough()){
                    return;
                }
                if (switchAddWB.isChecked()) {
                    wbNum = 1;
                } else {
                    wbNum = 0;
                }
                setCivilianNum();
            }
        });
        setCivilianNum();
    }

    private void setCivilianNum() {
        civilianNum = playerNum - spyNum - wbNum;
        tvCivilianNum.setText(civilianNum + "");
    }

    private void setBtnStartGame() {
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSharedPreferences();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("playerNames", playerNames);
                bundle.putInt("civilianNum", civilianNum);
                bundle.putInt("spyNum", spyNum);
                bundle.putInt("wbNum", wbNum);
                Intent intent = new Intent(MainActivity.this, SeePuzzleActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });
    }

    private AlertDialog setSwitchCustomName() {

        switchCustomName.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    View view = inflater.inflate(R.layout.dialog_custom_name, null);


                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                    RecyclerView recyclerView = view.findViewById(R.id.dialog_rv);
                    recyclerView.setLayoutManager(layoutManager);
                    adapter = new CustomAdapter();
                    recyclerView.setAdapter(adapter);

                    cNameDialog = builder
                            .setView(view)
                            .setCancelable(false)
                            .create();
                    cNameDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            //if ()// 如果有名字沒填的話會跳出dialog
                        }
                    });
                    cNameDialog.show();
                }
            }
        });
        return cNameDialog;
    }

    private void setSwitchCustomPuzzle() {
        switchCustomPuzzle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    View view = inflater.inflate(R.layout.dialog_custom_puzzles, null);
                    final AlertDialog cPuzzleDialog = builder
                            .setView(view)
                            .setCancelable(false)
                            .show();
                    btnCPSave = view.findViewById(R.id.custom_puzzle_btn_save);
                    btnCPCancel = view.findViewById(R.id.custom_puzzle_btn_cancel);

                    etCPCPuzzle = view.findViewById(R.id.main_custom_puzzle_et_civilian);
                    etCPCPuzzle.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            customPuzzles[0] = etCPCPuzzle.getText().toString();
                        }
                    });
                    etCPSPuzzle = view.findViewById(R.id.main_custom_puzzle_et_spy);
                    etCPSPuzzle.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            customPuzzles[1] = etCPSPuzzle.getText().toString();
                        }
                    });

                    btnCPCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switchCustomPuzzle.setChecked(false);
                            customPuzzles[0]="";
                            customPuzzles[1]="";
                            cPuzzleDialog.dismiss();
                        }
                    });

                    btnCPSave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(customPuzzles[0]==null || customPuzzles[1]==null){
                                Toast.makeText(MainActivity.this,R.string.main_toast_fill_in_blanks, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(MainActivity.this, R.string.main_custom_name_changed, Toast.LENGTH_SHORT).show();
                            cPuzzleDialog.dismiss();
                            switchCustomPuzzle.setChecked(true);
                        }
                    });

                }
            }
        });
    }


    public class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        public CustomAdapter() {

        }

        private class ItemViewHolder extends RecyclerView.ViewHolder {

            public TextView tvCNPlayerNumber;
            public EditText etCNName;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCNPlayerNumber = itemView.findViewById(R.id.custom_name_section_tv_player_number);
                etCNName = itemView.findViewById(R.id.custom_name_section_et_name);
                etCNName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        cNameDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

                    }
                });

                etCNName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(etCNName.length()==14){
                            Toast.makeText(MainActivity.this, R.string.main_toast_max_text_size,Toast.LENGTH_SHORT).show();
                        }
                        playerNames.set(getLayoutPosition(), etCNName.getText().toString());
                    }
                });
            }
        }

        private class AddItemViewHolder extends RecyclerView.ViewHolder {

            private Button btnCNAdd;
            private Button btnCNCancel;

            public AddItemViewHolder(@NonNull View itemView) {
                super(itemView);
                btnCNAdd = itemView.findViewById(R.id.custom_add_btn_save);
                btnCNCancel = itemView.findViewById(R.id.custom_add_btn_cancel);
                btnCNAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cNameDialog.dismiss();
                        saveSharedPreferences();
                        Toast.makeText(MainActivity.this, R.string.main_custom_name_changed, Toast.LENGTH_SHORT).show();
                        switchCustomName.setChecked(true);
                    }
                });
                btnCNCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cNameDialog.dismiss();
                        switchCustomName.setChecked(false);
                    }
                });
            }
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            if (viewType == 1) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.custom_add_btn, viewGroup, false);
                return new AddItemViewHolder(view);
            }
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.custom_name_section, viewGroup, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
            int viewType = getItemViewType(position);
            if (viewType == 0) {
                ItemViewHolder ivh = (ItemViewHolder) viewHolder;
                ivh.tvCNPlayerNumber.setText(position + 1 + "");
                ivh.etCNName.setText(playerNames.get(position));
            } else if (viewType == 1) {

            }
        }

        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return 1;
            }
            return 0;
        }

        @Override
        public int getItemCount() {
            return playerNum + 1;
        }

    }

    private void getPuzzles() {
        if(jsonArrayPuzzle!=null){
            return;
        }
        NetworkController.getInstance().getPuzzles(new NetworkController.CCallback() {
            @Override
            public void onFailure(String errorMsg) {
                Log.d(TAG, "onFailure: " + errorMsg);
            }

            @Override
            public void onResponse(JSONObject data) {
                Log.d(TAG, "onResponse");
                try {
                    jsonArrayPuzzle = data.getJSONArray("puzzles");
                    loadingDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCompleted() {
                Log.d(TAG, "onCompleted");

            }
        });
    }


}
