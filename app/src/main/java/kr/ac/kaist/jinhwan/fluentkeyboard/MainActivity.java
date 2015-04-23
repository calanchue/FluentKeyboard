package kr.ac.kaist.jinhwan.fluentkeyboard;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements MessageListener{
    TextView outputView;
    TextView rawView;
    TextView convertedView;
    RingUIView ringUIView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final InputFieldView inputFieldView = (InputFieldView)findViewById(R.id.inputFieldView);
        inputFieldView.setMessageListener(this);
        outputView = (TextView)findViewById(R.id.outputView);

        final TextView lastInputTV= (TextView)findViewById(R.id.lastInputTV);
        final TextView minFlickTV = (TextView)findViewById(R.id.minFlickTV);

        rawView = (TextView)findViewById(R.id.rawTV);
        rawView.setText("");
        convertedView = (TextView)findViewById(R.id.convertedTV);



        SeekBar sb1 = (SeekBar)findViewById(R.id.seekBar1);
        sb1.setProgress(40);
        sb1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                inputFieldView.setLastInputRadius(progress + 20);
                lastInputTV.setText(String.format("LastInputR:%d", progress + 20));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                lastInputTV.invalidate();
            }
        });
        inputFieldView.setLastInputRadius(sb1.getProgress() + 20);

        SeekBar sb2 = (SeekBar)findViewById(R.id.seekBar2);
        sb2.setProgress(40);
        sb2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                inputFieldView.setMinFlickRadius(progress+20);
                minFlickTV.setText(String.format("minFlickR:%d", progress+20));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                minFlickTV.invalidate();
            }
        });
        inputFieldView.setMinFlickRadius(sb1.getProgress()+20);


        ringUIView = (RingUIView)findViewById(R.id.ringUIView);
        inputFieldView.ringUIView = ringUIView;


        Switch fixLPositionSW = (Switch)(findViewById(R.id.fixLPosSW));
        fixLPositionSW.setChecked(S.getInstance().fixLastInput);
        fixLPositionSW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                S.getInstance().fixLastInput = isChecked;
            }
        });

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

        EditText editText = (EditText)(findViewById(R.id.editText));
        editText.setSelection(editText.getText().length());




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





    @Override
    public void listenMessage(Type type, String m) {
        if (type == Type.direction) {
            outputView.setText(Html.fromHtml(m), TextView.BufferType.SPANNABLE);
        }else if(type == Type.text){
            rawView.append(m);
            Log.d("Activity", "raw : " + rawView.getText().toString());
            String converted = TextConverter.convertRaw(rawView.getText().toString());
            Log.d("Activity", "con : " + converted);
            convertedView.setText(converted);
        }else if(type == Type.special){
            if(m.equals("bs")){
                Log.d("Activity", "special :bs" );
                String rawString = rawView.getText().toString();
                if(rawString.length() >1 ) {
                    rawString = rawString.substring(0, rawString.length() - 1);
                    rawView.setText(rawString);
                    convertedView.setText(TextConverter.convertRaw(rawString));
                }else {
                    rawView.setText("");
                    convertedView.setText("");
                }
            }
        }
    }
}
