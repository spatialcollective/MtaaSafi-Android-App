package com.sc.mtaasafi.android;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NewReportFragment extends Fragment {
    MainActivity mActivity;
    DescriptionEditText details;
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
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mActivity = (MainActivity) getActivity();
        aq = new AQuery(mActivity);
        pics = new ArrayList<byte[]>();
        picPreviews = new ImageView[TOTAL_PICS];
        Log.e(LogTags.NEWREPORT, "On Create called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_report, container, false);
        details = (DescriptionEditText) view.findViewById(R.id.newReportDetails);
        picPreviews[0] = (ImageView) view.findViewById(R.id.pic1);
        picPreviews[1] = (ImageView) view.findViewById(R.id.pic2);
        picPreviews[2] = (ImageView) view.findViewById(R.id.pic3);

        reportBtn = (Button) view.findViewById(R.id.reportButton);
        reportBtn.setClickable(false);

        attachPicsTV = (TextView) view.findViewById(R.id.attachMorePicturesText);
        setListeners();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedState){
        super.onActivityCreated(savedState);
        if(savedState != null){
            detailsString = savedState.getString(DEETS_KEY);
            if(detailsString != null){
                details.setText(detailsString);
            }
            lastPreviewClicked = savedState.getInt(LASTPREVIEW_KEY);
            restorePics(savedState.getStringArrayList(picsKey));
        } else{
            for(int i = 0; i < TOTAL_PICS; i++){
                pics.add(null);
            }
        }
    }

    private void restorePics(List<String> encodedPics){
        for(int i = 0; i < TOTAL_PICS; i++){
            if(!encodedPics.get(i).equals("null")){
                // decode byte[] from string, add to pics list, create a thumb from the byte[],
                // add it to the preview.
                pics.add(Base64.decode(encodedPics.get(i), Base64.DEFAULT));
                aq.id(picPreviews[i]).image(getThumbnail(pics.get(i)));
            } else{
                pics.add(null);
            }
        }
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

        picPreviews[PIC1].setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onPicPreviewClicked(PIC1);
            }
        });
        picPreviews[PIC2].setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onPicPreviewClicked(PIC2);
            }
        });
        picPreviews[PIC3].setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onPicPreviewClicked(PIC3);
            }
        });

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { sendReport(); }
        });
    }
    public void onPicPreviewClicked(int previewClicked){
        lastPreviewClicked = previewClicked;
        setRetainInstance(true);
        mActivity.takePicture((NewReportFragment) getParentFragment());
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
        ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutStream);
        Bitmap thumbnail = getThumbnail(byteArrayOutStream.toByteArray());
        switch(lastPreviewClicked){
            case PIC1: {
                pics.add(PIC1, byteArrayOutStream.toByteArray());
                aq.id(picPreviews[PIC1]).image(thumbnail);
                break;
            }
            case PIC2: {
                pics.add(PIC2, byteArrayOutStream.toByteArray());
                aq.id(picPreviews[PIC2]).image(thumbnail);
                break;
            }
            case PIC3: {
                pics.add(PIC3, byteArrayOutStream.toByteArray());
                aq.id(picPreviews[PIC3]).image(thumbnail);
                break;
            }
        }
        determineEmptyPicsText();
        setRetainInstance(false);
    }

    // Returns 100x100px thumbnail to populate picPreviews.
    private Bitmap getThumbnail(byte[] pic){
        Bitmap bitmap = BitmapFactory.decodeByteArray(pic, 0, pic.length);
        return Bitmap.createScaledBitmap(bitmap, 100, 100, true);
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

    @Override
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
