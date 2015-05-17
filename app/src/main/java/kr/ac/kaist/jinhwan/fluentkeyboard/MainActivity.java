package kr.ac.kaist.jinhwan.fluentkeyboard;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements MessageListener{
    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences settings;

    TextView outputView;
    TextView rawView;
    //TextView convertedView;
    EditText convertedET;
    RingUIView ringUIView;
    InputFieldView inputFieldView;


    private final static String HISTORY_SAVE_PATH = "/HISTORY";
    private final static String  MAIN_SETTING_HEIGHT_KEY = "mainSettingHeight";
    private final static String LAST_S_PATH_KEY = "last_s_path_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);



        String last_s_path = settings.getString(LAST_S_PATH_KEY,null);
        if(last_s_path != null){
            boolean pass = S.load(last_s_path);
            if(pass){
                Toast.makeText(MainActivity.this, "LOAD: " +
                        last_s_path, Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(MainActivity.this, "FAIL TO LOAD: " +
                        last_s_path, Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(LAST_S_PATH_KEY, null);
                editor.commit();
            }
        }

        S.getInstance().setDisplayMetrics(getResources().getDisplayMetrics());


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final InputFieldView inputFieldView = (InputFieldView)findViewById(R.id.inputFieldView);
        this.inputFieldView=inputFieldView;
        inputFieldView.setMessageListener(this);
        outputView = (TextView)findViewById(R.id.outputView);



        rawView = (TextView)findViewById(R.id.rawTV);
        rawView.setMovementMethod(new ScrollingMovementMethod());

        rawView.setText("");
        rawView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                final Layout layout = rawView.getLayout();
                if(layout != null){
                    int scrollDelta = layout.getLineBottom(rawView.getLineCount() - 1)
                            - rawView.getScrollY() - rawView.getHeight();
                    if(scrollDelta > 0)
                        rawView.scrollBy(0, scrollDelta);
                }
            }
        });

        //convertedView = (TextView)findViewById(R.id.convertedTV);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        ringUIView = (RingUIView)findViewById(R.id.ringUIView);
        inputFieldView.ringUIView = ringUIView;

/*
        convertedView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("convertedTV", "text changed");

            }
        });
*/

        final SelectionWatchEditView editText = (SelectionWatchEditView)findViewById(R.id.selectionWatchET);
        convertedET = editText;
        editText.setFocusable(false);
        editText.addSelectionWatcher(new SelectionWatchEditView.SelectionWatcher() {
            @Override
            public void onSelectionChanged(int selStart, int selEnd) {
                refreshRingUIPosition();

            }
        });
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        editText.setText("setText at runtime", TextView.BufferType.EDITABLE);
        editText.setSelection(editText.getText().length());

        //deal editText position Changed by layout chagne
        final View activityRootView = findViewById(R.id.mainLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @SuppressLint("NewApi")
                    @Override
                    public void onGlobalLayout() {
                        refreshRingUIPosition();

                    }
                });

        onCreateOption();
        Button saveSettingB = (Button)findViewById(R.id.saveSettingB);
        saveSettingB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleFileDialog FileSaveDialog =  new SimpleFileDialog(MainActivity.this, "FileSave",
                        new SimpleFileDialog.SimpleFileDialogListener()
                        {
                            String m_chosen;
                            @Override
                            public void onChosenDir(String chosenDir)
                            {
                                // The code in this function will be executed when the dialog OK button is pushed
                                m_chosen = chosenDir;
                                S.getInstance().save(chosenDir);
                                Toast.makeText(MainActivity.this, "SAVE: " +
                                m_chosen, Toast.LENGTH_LONG).show();

                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString(LAST_S_PATH_KEY, m_chosen);
                                editor.commit();
                            }
                        });

                //You can change the default filename using the public variable "Default_File_Name"
                S.makeDefaultSaveDirectory();
                FileSaveDialog.Default_File_Name = "FKB_Setting";
                FileSaveDialog.chooseFile_or_Dir(S.getDefaultSaveDirPath());
            }
        });
        Button loadSettingB = (Button)findViewById(R.id.loadSettingB);
        loadSettingB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SimpleFileDialog FileOpenDialog =  new SimpleFileDialog(MainActivity.this, "FileOpen",
                        new SimpleFileDialog.SimpleFileDialogListener()
                        {
                            String m_chosen;
                            @Override
                            public void onChosenDir(String chosenDir)
                            {
                                // The code in this function will be executed when the dialog OK button is pushed
                                m_chosen = chosenDir;
                                S.load(chosenDir);
                                Toast.makeText(MainActivity.this, "LOAD: " +
                                        m_chosen, Toast.LENGTH_LONG).show();
                                MainActivity.this.recreate();

                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString(LAST_S_PATH_KEY,  m_chosen);
                                editor.commit();
                            }
                        });

                FileOpenDialog.Default_File_Name = "";
                S.makeDefaultSaveDirectory();
                FileOpenDialog.chooseFile_or_Dir(S.getDefaultSaveDirPath());
            }
        });
        Button resetB = (Button)findViewById(R.id.resetB);
        resetB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.recreate();
            }
        });

        Button showInputGestureB = (Button)findViewById(R.id.showInputGestureHistoryB);
        showInputGestureB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputFieldView.showGestureHistory();
            }
        });

        Button showVowelPositionB = (Button)findViewById(R.id.showVowelInputPositionHistoryB);
        showVowelPositionB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputFieldView.showVIPosHistorey();
            }
        });

        final LinearLayout mainContainer = (LinearLayout) findViewById(R.id.mainSettingContainer);
        if(settings.getInt(MAIN_SETTING_HEIGHT_KEY ,400) > 400){
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(MAIN_SETTING_HEIGHT_KEY,  400);
            editor.commit();
        }
        mainContainer.getLayoutParams().height = settings.getInt(MAIN_SETTING_HEIGHT_KEY , 400);


        mainContainer.setVisibility(View.GONE);
        final View mainSettingHeightBar = (View)findViewById(R.id.mainSettingHeightBar);

        mainSettingHeightBar.setOnTouchListener(new View.OnTouchListener() {

            float mLastX = 0;
            float mLastY = 0;
            int originalHeight = 0;

            @Override
                public boolean onTouch(View v, MotionEvent ev) {
                LinearLayout mainContainer = (LinearLayout) findViewById(R.id.mainSettingContainer);

                int mActivePointerId;

                float x = ev.getX();
                float y = ev.getY();

                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                        // Remember where we started (for dragging)
                        mLastX = x;
                        mLastY = y;
                        originalHeight = mainContainer.getLayoutParams().height;
                        mainSettingHeightBar.setBackgroundColor(Color.RED);
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        // Calculate the distance moved
                        final float dy = y - mLastY;
                        int newHeight = (int) (originalHeight + dy);
                        newHeight = newHeight > 100 ? newHeight : 100;

                        mainContainer.getLayoutParams().height = newHeight;
                        mainContainer.invalidate();
                        mainContainer.requestLayout();
                 /*       mLastX = x;
                        mLastY = y;*/

                        break;

                    }
                    case MotionEvent.ACTION_UP: {
                        if(mainContainer.getVisibility() == View.VISIBLE){
                            mainSettingHeightBar.setBackgroundColor(Color.BLACK);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putInt(MAIN_SETTING_HEIGHT_KEY,  mainContainer.getLayoutParams().height);
                            editor.commit();
                        }

                        if(Math.sqrt(Math.pow(y-mLastY,2.0) + Math.pow(x-mLastX,2.0)) < 20){
                            if(mainContainer.getVisibility() != View.GONE) {
                                mainContainer.setVisibility(View.GONE);
                            }else{
                                mainContainer.setVisibility(View.VISIBLE);
                                mainContainer.getLayoutParams().height = settings.getInt(MAIN_SETTING_HEIGHT_KEY , 400);
                            }
                        }

                        break;
                    }
                }
                return true;
            }
        });

        final String defHisDir =  S.getDefaultSaveDirPath() + HISTORY_SAVE_PATH;
        Button saveHisB = (Button)findViewById(R.id.saveHistoryB);
        saveHisB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleFileDialog FileSaveDialog =  new SimpleFileDialog(MainActivity.this, "FileSave",
                        new SimpleFileDialog.SimpleFileDialogListener()
                        {
                            String m_chosen;
                            @Override
                            public void onChosenDir(String chosenDir)
                            {
                                // The code in this function will be executed when the dialog OK button is pushed
                                m_chosen = chosenDir;
                                inputFieldView.saveInputHistory(m_chosen);
                                Toast.makeText(MainActivity.this, "SAVE: " +
                                        m_chosen, Toast.LENGTH_LONG).show();
                            }
                        });

                //You can change the default filename using the public variable "Default_File_Name"
                S.makeDirectory(defHisDir);
                FileSaveDialog.Default_File_Name = "FKB_History";
                FileSaveDialog.chooseFile_or_Dir(defHisDir);

            }
        });
        Button loadHisB = (Button)findViewById(R.id.loadHistoryB);
        loadHisB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleFileDialog FileOpenDialog =  new SimpleFileDialog(MainActivity.this, "FileOpen",
                        new SimpleFileDialog.SimpleFileDialogListener()
                        {
                            String m_chosen;
                            @Override
                            public void onChosenDir(String chosenDir)
                            {
                                // The code in this function will be executed when the dialog OK button is pushed
                                m_chosen = chosenDir;
                                inputFieldView.loadInputHistory(m_chosen);
                                Toast.makeText(MainActivity.this, "LOAD: " +
                                        m_chosen, Toast.LENGTH_LONG).show();
                            }
                        });

                FileOpenDialog.Default_File_Name = "";
                S.makeDirectory(defHisDir);
                FileOpenDialog.chooseFile_or_Dir(defHisDir);
            }
        });
    }



    private void onCreateOption(){
        SeekBar sb1 = (SeekBar)findViewById(R.id.lastInputRadSB);
        sb1.setProgress((int) S.getInstance().getLastInputRadius() -50);
        sb1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                S.getInstance().setLastInputRadius(progress + 50);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar sb2 = (SeekBar)findViewById(R.id.minFlickRadSB);
        sb2.setProgress((int) S.getInstance().getMinFlickRadius() -20);
        sb2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                S.getInstance().setMinFlickRadius(progress+20);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


/*
        Log.d("Setting", "fixLInput constructing : " + S.getInstance().fixLastInput);
        Switch fixLPositionSW = (Switch)(findViewById(R.id.fixLPosSW));
        fixLPositionSW.setChecked(S.getInstance().fixLastInput);
        fixLPositionSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("Setting", "fixLInput changed : " + isChecked);
                S.getInstance().fixLastInput = isChecked;
            }
        });
        Log.d("Setting", "fixLInput constructed : " + fixLPositionSW.isChecked());
*/

        final SeekBar ringScaleSB = (SeekBar)findViewById(R.id.ringScaleSB);
        ringScaleSB.setProgress((int)(100*S.getInstance().ringScale));
        ringUIView.setScaleX((float)ringScaleSB.getProgress()/100);
        ringUIView.setScaleY((float)ringScaleSB.getProgress()/100);
        ringScaleSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ringUIView.setScaleX((float)progress/100);
                ringUIView.setScaleY((float)progress/100);
                S.getInstance().ringScale=(float)progress/100;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        ringUIView.setScaleX(S.getInstance().ringScale);
        ringUIView.setScaleY(S.getInstance().ringScale);

        NumberPicker ringOffXNB = (NumberPicker)findViewById(R.id.ringOffSetX);
        final int minValue = -20;
        final int maxValue = 20;
        final int ringNBscale = 10;
        NumberPicker.Formatter npf= new NumberPicker.Formatter() {
            @Override
            public String format(int index) {
                return Integer.toString((index + minValue)*ringNBscale);
            }
        };
        ringOffXNB.setMinValue(0);
        ringOffXNB.setMaxValue(maxValue - minValue);
        ringOffXNB.setValue(S.getInstance().ringOffX / ringNBscale - minValue);
        ringOffXNB.setWrapSelectorWheel(false);
        ringOffXNB.setFormatter(npf);
        ringOffXNB.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                S.getInstance().ringOffX = (newVal+minValue)*ringNBscale;
                refreshRingUIPosition();
            }
        });

        NumberPicker ringOffYNB = (NumberPicker)findViewById(R.id.ringOffSetY);
        ringOffYNB.setMinValue(0);
        ringOffYNB.setMaxValue(maxValue - minValue);
        ringOffYNB.setValue(S.getInstance().ringOffY/ringNBscale-minValue);
        ringOffYNB.setWrapSelectorWheel(false);
        ringOffYNB.setFormatter(npf);
        ringOffYNB.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                S.getInstance().ringOffY =(newVal+minValue)*ringNBscale;
                refreshRingUIPosition();
            }
        });

        RadioGroup inputOptionRG = (RadioGroup)findViewById(R.id.inputOptionRG);


        int a = R.id.inputFieldView;
        inputOptionRG.check(S.getInstance().inputOption.getId());
        inputOptionRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                S.getInstance().inputOption = S.InputOption.getFromId(checkedId);
            }
        });

        NumberPicker adaptHistorySizeNP = (NumberPicker)findViewById(R.id.adaptSizeNP);
        adaptHistorySizeNP.setMaxValue(30);
        adaptHistorySizeNP.setMinValue(1);
        adaptHistorySizeNP.setValue(S.getInstance().adaptHistorySize);
        adaptHistorySizeNP.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                S.getInstance().adaptHistorySize = newVal;
            }
        });

        Switch inDirFromStartPos = (Switch)findViewById(R.id.inDirFromStartPosSW);
        inDirFromStartPos.setChecked(S.getInstance().inDirFromStartPos);
        inDirFromStartPos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                S.getInstance().inDirFromStartPos = isChecked;
            }
        });

        SeekBar bent2MinFlickRadiusSB = (SeekBar)findViewById(R.id.bent2MinFlickRadius);
        bent2MinFlickRadiusSB.setProgress((int) S.getInstance().bent2MinFlickRadius);
        bent2MinFlickRadiusSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                S.getInstance().bent2MinFlickRadius = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    private void refreshRingUIPosition(){
        int pos = convertedET.getSelectionStart();

        Log.d("editText", "new selStart %d, cur pos = %d");
        Layout layout = convertedET.getLayout();
        if(layout == null){
            return;
        }
        int line = layout.getLineForOffset(pos);
        int baseline = layout.getLineBaseline(line);
        int ascent = layout.getLineAscent(line);
        float x = layout.getPrimaryHorizontal(pos);
        float y = baseline + ascent/2;

        Log.v("editText", String.format("baseline %d, ascent = %d", baseline, ascent));
        Log.d("editText", String.format("cursor position = %f, %f", x, y));
        int newX = (int)(convertedET.getX() + x - ringUIView.getWidth()/2);
        int newY = (int)(convertedET.getY() + y - ringUIView.getHeight()/2);
        newX += S.getInstance().ringOffX;
        newY += S.getInstance().ringOffY;

        Log.d("editText", String.format("new Ring position = %d, %d", newX, newY));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ringUIView.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        ringUIView.animate().translationX(newX).translationY(newY).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200);
        //params.setMargins((int)newX, (int)newY, 0, 0);

        Switch onTrackSW = (Switch)findViewById(R.id.hoverTrackSW);
        onTrackSW.setChecked(S.getInstance().hoverTrack);
        onTrackSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                S.getInstance().hoverTrack = isChecked;
            }
        });

        SeekBar sb1 = (SeekBar)findViewById(R.id.lastInputRadSB);
        sb1.setProgress((int) S.getInstance().getLastInputRadius() -50);
        sb1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                S.getInstance().setLastInputRadius(progress + 50);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //super.onRestoreInstanceState(savedInstanceState);

        Log.d("Setting", "On Restore");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    public void setConvertedText(String converted){
        //convertedView.setText(converted);
        convertedET.setText(converted);
        convertedET.setSelection(convertedET.getText().length());
    }

    @Override
    public void listenMessage(Type type, String m) {
        if (type == Type.direction) {
            outputView.setText(Html.fromHtml(m), TextView.BufferType.SPANNABLE);
        }else if(type == Type.text){
            rawView.append(m);
            Log.d("Activity", "raw : " + rawView.getText().toString());
            String converted = TextConverter.convertRaw(rawView.getText().toString());
            Log.d("Activity", "con : " + converted);
            setConvertedText(converted);

        }else if(type == Type.special){
            if(m.equals("bs")){
                Log.d("Activity", "special :bs" );
                String rawString = rawView.getText().toString();
                if(rawString.length() >1 ) {
                    rawString = rawString.substring(0, rawString.length() - 1);
                    rawView.setText(rawString);
                    String converted = TextConverter.convertRaw(rawString);
                    setConvertedText(converted);
                }else {
                    rawView.setText("");
                    setConvertedText("");
                }
            }
        }
    }
}
