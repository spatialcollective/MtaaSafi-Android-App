package com.sc.mtaasafi.android.NewReport;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.sc.mtaasafi.android.DescriptionEditText;
import com.sc.mtaasafi.android.LogTags;
import com.sc.mtaasafi.android.NewReport.NewReportActivity;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NewReportFragment extends Fragment {
    int nextPendingPiece;

    ImageView[] picPreviews;
    DescriptionEditText detailsView;

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
            TOTAL_PICS = 3,
            SAVED_REPORT_BUTTON_ID = 100;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        picPaths = new ArrayList<String>();
        for(int i = 0; i < TOTAL_PICS; i++)
            picPaths.add(null);
        setRetainInstance(true);
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
        attemptEnableSendSave(view);
        updatePicPreviews(view);
        setListeners();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        if (savedState != null) {
            detailsText = savedState.getString(DEETS_KEY);
            if (detailsText != null)
                detailsView.setText(detailsText);
//            if (savedState.getBoolean(HAS_PENDING_REPORT_KEY)) {
//                nextPendingPiece = savedState.getInt(PENDING_PIECE_KEY);
//                pendingReport = new Report(PENDING_REPORT_ID, savedState);
//                beamUpReport(pendingReport);
//            }
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (detailsText != null)
            outState.putString(DEETS_KEY, detailsText);
        outState.putInt(PENDING_PIECE_KEY, nextPendingPiece);
//        if (pendingReport != null) {
//            outState.putBoolean(HAS_PENDING_REPORT_KEY, true);
//            pendingReport.saveState(PENDING_REPORT_ID, outState);
//        }
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onResume(){
        super.onResume();
        Log.e(LogTags.NEWREPORT, "onResume");
        NewReportActivity mActivity = (NewReportActivity) getActivity();
        if(mActivity.getSavedReportCount() > 0
                && getView().findViewById(SAVED_REPORT_BUTTON_ID) == null)
            addSendSavedReportButton(mActivity.getSavedReportCount());
    }
    @Override
    public void onStop(){
        super.onStop();
        for(ImageView picPreview : picPreviews)
            picPreview = null;
        detailsView = null;
    }

    @SuppressWarnings("ResourceType")
    private void addSendSavedReportButton(int savedReportCount){
        RelativeLayout mLayout = (RelativeLayout) getView().findViewById(R.id.new_report);
        Button sendSavedReport = new Button(getActivity());
        sendSavedReport.setId(SAVED_REPORT_BUTTON_ID);
        sendSavedReport.setBackgroundColor(getResources().getColor(R.color.Coral));
        String buttonText = "Send " + savedReportCount + " saved report";
        if(savedReportCount > 1)
            buttonText +="s";
        sendSavedReport.setText(buttonText);
        sendSavedReport.setTextColor(getResources().getColor(R.color.White));
        sendSavedReport.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(canSend())
                    uploadSavedReports();
            }
        });
        RelativeLayout.LayoutParams buttonParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        sendSavedReport.setLayoutParams(buttonParams);
        LinearLayout ll = (LinearLayout) mLayout.findViewById(R.id.top_layout);
        ll.addView(sendSavedReport, 0);
    }
    private void uploadSavedReports(){
        NewReportActivity mActivity = (NewReportActivity) getActivity();
        mActivity.uploadSavedReports();
    }

    private void updatePicPreviews(View view) {
        AQuery aq = new AQuery(getActivity());
        for(int i = 0; i < REQUIRED_PIC_COUNT; i++){
            if(picPaths.get(i) != null)
                aq.id(picPreviews[i]).image(getThumbnail(picPaths.get(i)));
            else
                aq.id(picPreviews[i]).image(R.drawable.pic_placeholder);
        }
        int emptyPics = getEmptyPics();
        if (emptyPics == 0) {
            view.findViewById(R.id.attachPicsTV).setVisibility(View.INVISIBLE);
        } else {
            TextView attachPicsTV = (TextView) view.findViewById(R.id.attachPicsTV);
            String needMoreString = "Need " + emptyPics + " more picture";
            if(emptyPics > 1)
                needMoreString +="s";
            attachPicsTV.setText(needMoreString);
        }
    }

    // Returns dynamically sized thumbnail to populate picPreviews.
    private Bitmap getThumbnail(String picPath) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(picPath, options);
//        int picWidth = options.outWidth;
//        int picHeight = options.outHeight;
//        NewReportActivity activity = (NewReportActivity) getActivity();
//        int screenWidth = activity.getScreenWidth();
//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        int pixels_per_dp = (int)(metrics.density + 0.5f);
//        int padding_dp = 15;
//        int reqWidth = (screenWidth)/4 - padding_dp * pixels_per_dp;
//        int reqHeight = activity.getScreenHeight()/4;
//
//        int inSampleSize = 1;
//
//        if(picWidth > reqWidth || picHeight > reqHeight){
//            final int halfWidth = picWidth / 2;
//            final int halfHeight = picHeight / 2;
//            while ((halfWidth / inSampleSize) > reqWidth && (halfHeight / inSampleSize) > reqHeight) {
//                inSampleSize *= 2;
//            }
//        }
//        options.inSampleSize = inSampleSize;
//        options.inJustDecodeBounds = false;
        Bitmap bmp = BitmapFactory.decodeFile(picPath);
        return Bitmap.createScaledBitmap(bmp, 120, 120, false);
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
            @Override
            public void afterTextChanged(Editable s) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                detailsText = detailsView.getText().toString();
                attemptEnableSendSave(null);
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
            updatePicPreviews(getView());
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

//    public void beamUpReport(Report report) {
//        Log.e(LogTags.BACKEND_W, "Beam it up");
//        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
//                getActivity().INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(detailsView.getWindowToken(), 0);
//    }

    public Report createNewReport(String userName, Location location) {
        Log.e(LogTags.NEWREPORT, "createNewReport");
        return new Report(detailsText, userName, location, picPaths);
    }

    public void attemptEnableSendSave(View view) {
        if (view == null)
            view = getView();
        if (detailsText.isEmpty() || picPaths == null || picPaths.isEmpty() || getEmptyPics() > 0) {
            disableButton((Button) view.findViewById(R.id.sendButton));
            disableButton((Button) view.findViewById(R.id.saveButton));
        } else {
            enableButton((Button) view.findViewById(R.id.sendButton));
            enableButton((Button) view.findViewById(R.id.saveButton));
        }
    }

    private boolean canSend(){
        String error ="";
        NewReportActivity mActivity = (NewReportActivity) getActivity();
        boolean canSend = false;
        if (!mActivity.isOnline()){
            error = "Connect to a network to send your report";
        } else if(mActivity.getLocation() == null){
            error = "Cannot access location, make sure location services enabled";
        } else{
            canSend = true;
        }
        if(!canSend)
            Toast.makeText(mActivity, error, Toast.LENGTH_SHORT).show();
        return canSend;
    }

    private void enableButton(Button btn) {
        btn.setClickable(true);
        btn.setBackgroundColor(getResources().getColor(R.color.report_button_clickable));
    }
    private void disableButton(Button btn) {
        btn.setClickable(false);
        btn.setBackgroundColor(getResources().getColor(R.color.report_button_unclickable));
    }
}
