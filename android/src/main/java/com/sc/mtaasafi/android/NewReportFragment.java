package com.sc.mtaasafi.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.io.ByteArrayOutputStream;

public class NewReportFragment extends Fragment {
    MainActivity mActivity;
    EditText details;
    TextView attachPicsTV;
    Button reportBtn;
    ImageView picPreview1, picPreview2, picPreview3;
    byte[] pic1, pic2, pic3;
    String detailsString;
    boolean isDetailEmpty;
    int lastPreviewClicked;
    private final int PIC1 = 1;
    private final int PIC2 = 2;
    private final int PIC3 = 3;
    private final String DEETS_KEY = "details";
    private final String PIC1_KEY = "pic1";
    private final String PIC2_KEY = "pic2";
    private final String PIC3_KEY = "pic3";

    AQuery aq;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        aq = new AQuery(mActivity);
        if(savedInstanceState != null){
            detailsString = savedInstanceState.getString(DEETS_KEY);
            pic1 = savedInstanceState.getByteArray(PIC1_KEY);
            pic2 = savedInstanceState.getByteArray(PIC2_KEY);
            pic3 = savedInstanceState.getByteArray(PIC3_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_report, container, false);
        details = (EditText) view.findViewById(R.id.newReportDetails);
        if(detailsString != null){
            details.setText(detailsString);
        }
        picPreview1 = (ImageView) view.findViewById(R.id.pic1);
        if(pic1 != null)
            aq.id(picPreview1).image(BitmapFactory.decodeByteArray(pic1, 0, pic1.length));
        picPreview2 = (ImageView) view.findViewById(R.id.pic2);
        if(pic2 != null)
            aq.id(picPreview2).image(BitmapFactory.decodeByteArray(pic2, 0, pic2.length));
        if(pic3 != null)
            aq.id(picPreview3).image(BitmapFactory.decodeByteArray(pic3, 0, pic3.length));

        picPreview3 = (ImageView) view.findViewById(R.id.pic3);

        reportBtn = (Button) view.findViewById(R.id.reportButton);
        reportBtn.setClickable(false);

        attachPicsTV = (TextView) view.findViewById(R.id.attachMorePicturesText);
        setListeners();
        return view;
    }

    private void setListeners(){
        details.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                attemptEnableReport();
            }
        });
        picPreview1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                lastPreviewClicked = PIC1;
                mActivity.takePicture();
            }
        });
        picPreview2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                lastPreviewClicked = PIC2;
                mActivity.takePicture();
            }
        });
        picPreview3.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                lastPreviewClicked = PIC3;
                mActivity.takePicture();
            }
        });

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { sendReport(); }
        });
    }

    public void sendReport() {
        mActivity.beamItUp(createNewReport());
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(
                mActivity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(details.getWindowToken(), 0);
        mActivity.goToFeed();
    }

    public Report createNewReport() {
        return new Report(details.getText().toString(), mActivity.mUsername, mActivity.getLocation(),
            pic1, pic2, pic3);
    }

    // called when the edit texts' listeners detect a change in their texts
    public void attemptEnableReport() {
       if (!details.getText().toString().isEmpty() && pic1 !=null && pic2!=null && pic3!=null){
           reportBtn.setClickable(true);
           reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_clickable));
       }
       else{
           reportBtn.setClickable(false);
           reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_unclickable));
       }
    }

    public void onPhotoTaken(String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytearrayoutputstream);
        Bitmap smallBmp = Bitmap.createScaledBitmap(bitmap, 100, 100, true);

        switch(lastPreviewClicked){
            case PIC1: {
                pic1 = bytearrayoutputstream.toByteArray();
                aq.id(picPreview1).image(smallBmp);
                break;
            }
            case PIC2: {
                pic2 = bytearrayoutputstream.toByteArray();
                aq.id(picPreview2).image(smallBmp);
                break;
            }
            case PIC3: {
                pic3 = bytearrayoutputstream.toByteArray();
                aq.id(picPreview3).image(smallBmp);
                break;
            }
        }
        determineEmptyPicsText();
        lastPreviewClicked = 0;
    }

    private void determineEmptyPicsText(){
        int emptyPics = 0;
        if(pic1 == null){
            emptyPics++;
        }
        if(pic2 == null){
            emptyPics++;
        }
        if(pic3 == null){
            emptyPics++;
        }
        if(emptyPics > 0)
            attachPicsTV.setText("Attach " + emptyPics + " more pictures:");
        else
            attachPicsTV.setVisibility(View.INVISIBLE);
        attemptEnableReport();
    }
    public void onSaveInstanceState(Bundle outState){
        outState.putString(DEETS_KEY, details.getText().toString());
        outState.putByteArray(PIC1_KEY, pic1);
        outState.putByteArray(PIC2_KEY, pic2);
        outState.putByteArray(PIC3_KEY, pic3);
    }
}
