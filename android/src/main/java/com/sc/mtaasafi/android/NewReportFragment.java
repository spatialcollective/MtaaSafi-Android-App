package com.sc.mtaasafi.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NewReportFragment extends Fragment {

    Report pendingReport;
    int nextPendingPiece;

    Button reportBtn, saveButton;
    ImageView[] picPreviews;

    DescriptionEditText detailsView;
    NewReportActivity mActivity;

    private ArrayList<String> picPaths;
    private String detailsText;
    private int previewClicked;
    public final int REQUIRED_PIC_COUNT = 3;

    public final String DEETS_KEY = "details",
            PENDING_PIECE_KEY ="next_field",
            PENDING_REPORT_ID = "report_to_send_id",
            HAS_PENDING_REPORT_KEY = "has_pending_key";
            
    static final int    REQUEST_IMAGE_CAPTURE = 1,
                        PIC1 = 0,
                        PIC2 = 1,
                        PIC3 = 2,
                        TOTAL_PICS = 3;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        picPaths = new ArrayList<String>();
        for(int i = 0; i < TOTAL_PICS; i++)
            picPaths.add(null);
        setRetainInstance(true);
        mActivity = (NewReportActivity) getActivity();
        Log.e(LogTags.NEWREPORT, "OnCreate " + this.toString());
        picPreviews = new ImageView[TOTAL_PICS];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_new_report, container, false);
        picPreviews[PIC1] = (ImageView) view.findViewById(R.id.pic1);
        picPreviews[PIC2] = (ImageView) view.findViewById(R.id.pic2);
        picPreviews[PIC3] = (ImageView) view.findViewById(R.id.pic3);
        detailsView = (DescriptionEditText) view.findViewById(R.id.newReportDetails);
        detailsText = "";
        reportBtn = (Button) view.findViewById(R.id.reportButton);
        saveButton = (Button) view.findViewById(R.id.saveButton);
        setListeners();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        detailsView.setText(detailsText);
        if (savedState != null) {
            updatePicPreviews();
            attemptEnableSendSave();
            if (savedState.getBoolean(HAS_PENDING_REPORT_KEY)) {
                nextPendingPiece = savedState.getInt(PENDING_PIECE_KEY);
                pendingReport = new Report(savedState);
                beamUpReport(pendingReport);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (detailsText != null)
            outState.putString(DEETS_KEY, detailsText);
        outState.putInt(PENDING_PIECE_KEY, nextPendingPiece);
        if (pendingReport != null) {
            outState.putBoolean(HAS_PENDING_REPORT_KEY, true);
            pendingReport.saveState(outState);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.e(LogTags.NEWREPORT, "onResume");
        updatePicPreviews();
        attemptEnableSendSave();
    }

    private void updatePicPreviews() {
        AQuery aq = new AQuery(getActivity());
        for(int i = 0; i < REQUIRED_PIC_COUNT; i++){
            if(picPaths.get(i) != null)
                aq.id(picPreviews[i]).image(getThumbnail(picPaths.get(i)));
            else{
                aq.id(picPreviews[i]).image(R.drawable.pic_placeholder);
            }
        }
        int emptyPics = getEmptyPics();
        if (emptyPics == 0) {
            getView().findViewById(R.id.attachPicsTV).setVisibility(View.INVISIBLE);
        } else {
            TextView attachPicsTV = (TextView) getView().findViewById(R.id.attachPicsTV);
            attachPicsTV.setText("Need " + emptyPics + " more");
        }
    }

    // Returns dynamically sized thumbnail to populate picPreviews.
    private Bitmap getThumbnail(String picPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picPath, options);
        int picWidth = options.outWidth;
        NewReportActivity activity = (NewReportActivity) getActivity();
        int screenWidth = activity.getScreenWidth();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int pixels_per_dp = (int)(metrics.density + 0.5f);
        int padding_dp = 15;
        int reqWidth = (screenWidth)/3 - padding_dp * pixels_per_dp;
        int inSampleSize = 1;

        if(picWidth > reqWidth){
            final int halfWidth = picWidth / 2;
            while ((halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(picPath, options);
    }
    private int getEmptyPics(){
        int emptyPics = 0;
        for(int i = 0; i < TOTAL_PICS; i++){
            if(picPaths.get(i) == null)
                emptyPics++;
        }
        return emptyPics;
    }
    private void setListeners() {
        detailsView.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                detailsText = detailsView.getText().toString();
                attemptEnableSendSave();
            }
        });

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                attemptSend();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                attemptSave();
            }
        });
        picPreviews[PIC1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previewClicked = PIC1;
                takePicture(PIC1);
            }
        });
        picPreviews[PIC2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previewClicked = PIC2;
                takePicture(PIC2);
            }
        });
        picPreviews[PIC3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previewClicked = PIC3;
                takePicture(PIC3);
            }
        });
    }

    public void takePicture(int previewClicked) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null){
            File photoFile = null;
            try {
                photoFile = createImageFile(previewClicked);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException ex){
                Toast.makeText(getActivity(), "Couldn't create file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Couldn't resolve activity", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            File file = new File(picPaths.get(previewClicked));
            if (file.length() == 0)
                picPaths.set(previewClicked, null);
            updatePicPreviews();
        }
    }

    private File createImageFile(int previewClicked) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_" + picPaths.size();
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        picPaths.set(previewClicked, image.getAbsolutePath());
        return image;
    }

    public void beamUpNewReport() {
        pendingReport = createNewReport();
        mActivity.beamUpReport(pendingReport);
    }

    public void beamUpReport(Report report) {
        Log.e(LogTags.BACKEND_W, "Beam it up");
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(detailsView.getWindowToken(), 0);
    }

    private void saveReport(){
        if(createNewReport() != null){
            mActivity.saveReport(createNewReport());
            Toast.makeText(getActivity(), "Report saved!", Toast.LENGTH_SHORT).show();
        }
        mActivity.finish();
    }

    public void clearView() {
        updatePicPreviews();
        detailsText = "";
        detailsView.setText("");
        detailsView.setFocusable(true);
    }

    public Report createNewReport() {
        Log.e(LogTags.NEWREPORT, "createNewReport");
        return new Report(detailsText, mActivity.userName, mActivity.getLocation(),
                picPaths);
    }

    public void attemptEnableSendSave() {
        if(detailsText.isEmpty() || picPaths == null || picPaths.isEmpty() || getEmptyPics() > 0){
            disableSend();
            disableSave();
        } else{
            enableSave();
            enableSend();
        }
    }

    private void attemptSend(){
        String error;
        if (!mActivity.isOnline()){
            error = "Connect to a network to send your report";
        } else if(mActivity.getLocation() == null){
            error = "Cannot access location, make sure location services enabled";
        } else{
            mActivity.beamUpReport(createNewReport());
            return;
        }
        Toast.makeText(mActivity, error, Toast.LENGTH_SHORT).show();
    }

    private void attemptSave(){
        String error;
        if(mActivity.getLocation() == null){
            error = "Cannot access location, make sure location services enabled";
        } else{
            mActivity.saveReport(createNewReport());
            mActivity.finish();
            return;
        }
        Toast.makeText(mActivity, error, Toast.LENGTH_SHORT).show();

    }
    private void enableSend() {
        reportBtn.setClickable(true);
        reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_clickable));
    }
    private void enableSave(){
        saveButton.setClickable(true);
        saveButton.setBackgroundColor(getResources().getColor(R.color.report_button_clickable));
    }
    private void disableSend() {
        reportBtn.setClickable(false);
        reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_unclickable));
    }
    private void disableSave(){
        saveButton.setClickable(false);
        saveButton.setBackgroundColor(getResources().getColor(R.color.report_button_unclickable));
    }
}
