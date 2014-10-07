package com.sc.mtaasafi.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.List;

public class NewReportFragment extends Fragment {
    MainActivity mActivity;
    EditText details;
    TextView attachPicsTV;
    Button reportBtn;
    ImageView[] picPreviews;
    ArrayList<byte[]> pics;
    String detailsString;
    int lastPreviewClicked;
    private final int   PIC1 = 0,
                        PIC2 = PIC1+1,
                        PIC3 = PIC2+1,
                        TOTAL_PICS = PIC3+1;

    private final String DEETS_KEY = "details",
                         picsKey = "pics",
                         LASTPREVIEW_KEY = "last_preview";

    AQuery aq;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        aq = new AQuery(mActivity);
        pics = new ArrayList<byte[]>();
        picPreviews = new ImageView[TOTAL_PICS];
        if(savedInstanceState != null){
            detailsString = savedInstanceState.getString(DEETS_KEY);
            lastPreviewClicked = savedInstanceState.getInt(LASTPREVIEW_KEY);
            List<String> encodedPics = savedInstanceState.getStringArrayList(picsKey);
            for(int i = 0; i < TOTAL_PICS; i++){
                if(!encodedPics.get(i).equals("null"))
                    pics.add(Base64.decode(encodedPics.get(i), Base64.DEFAULT));
                else
                    pics.add(null);
            }
        }
        else{
            for(int i = 0; i < TOTAL_PICS; i++){
                pics.add(null);
            }
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
        picPreviews[0] = (ImageView) view.findViewById(R.id.pic1);
        picPreviews[1] = (ImageView) view.findViewById(R.id.pic2);
        picPreviews[2] = (ImageView) view.findViewById(R.id.pic3);

        for(int i = 0; i < TOTAL_PICS; i++){
            if(pics.get(i) != null){
                aq.id(picPreviews[i])
                        .image(BitmapFactory.decodeByteArray(pics.get(i), 0, pics.get(i).length));
            }
        }

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

        picPreviews[0].setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                lastPreviewClicked = PIC1;
                mActivity.takePicture();
            }
        });
        picPreviews[1].setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                lastPreviewClicked = PIC2;
                mActivity.takePicture();
            }
        });
        picPreviews[2].setOnClickListener(new View.OnClickListener(){

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
            pics);
    }

    // called when the edit texts' listeners detect a change in their texts
    public void attemptEnableReport() {
       boolean hasEmptyPics = false;
       for(byte[] pic : pics){
           if(pic == null){
               hasEmptyPics = true;
               break;
           }
       }
       if (!details.getText().toString().isEmpty() && !hasEmptyPics){
           reportBtn.setClickable(true);
           reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_clickable));
       } else{
           reportBtn.setClickable(false);
           reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_unclickable));
       }
    }

    public void onPhotoTaken(String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytearrayoutputstream);
        Bitmap smallBmp = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
        Log.e(LogTags.PHOTO, "Got to onPHOTOTAKEN in fragment! Bitmap: " + bitmap.toString() +
                " lastPreviewClicked: "+ lastPreviewClicked);
        switch(lastPreviewClicked){
            case PIC1: {
                pics.add(PIC1, bytearrayoutputstream.toByteArray());
                aq.id(picPreviews[PIC1]).image(smallBmp);
                break;
            }
            case PIC2: {
                pics.add(PIC2, bytearrayoutputstream.toByteArray());
                aq.id(picPreviews[PIC2]).image(smallBmp);
                break;
            }
            case PIC3: {
                pics.add(PIC3, bytearrayoutputstream.toByteArray());
                aq.id(picPreviews[PIC3]).image(smallBmp);
                break;
            }
        }
        determineEmptyPicsText();
        lastPreviewClicked = 0;
    }

    private void determineEmptyPicsText(){
        int emptyPics = 0;
        for(int i = 0; i < pics.size(); i++){
            if(pics.get(i) == null)
                emptyPics++;
        }

        if(emptyPics > 1)
            attachPicsTV.setText("Attach " + emptyPics + " more pictures:");
        else if (emptyPics == 1)
            attachPicsTV.setText("Attach " + emptyPics + " more picture:");
        else
            attachPicsTV.setVisibility(View.INVISIBLE);
        attemptEnableReport();
    }

    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString(DEETS_KEY, details.getText().toString());
        outState.putInt(LASTPREVIEW_KEY, lastPreviewClicked);
        List<String> encodedPics = new ArrayList<String>();
        for(byte[] pic : pics){
            if(pic != null)
                encodedPics.add(Base64.encodeToString(pic, Base64.DEFAULT));
            else{
                encodedPics.add("null");
            }
        }
        outState.putStringArrayList(picsKey, (ArrayList<String>) encodedPics);
    }
}
