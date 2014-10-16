package com.sc.mtaasafi.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

import java.util.ArrayList;

public class NewReportFragment extends Fragment {
    MainActivity mActivity;
    Report pendingReport;
    DescriptionEditText details;
    TextView attachPicsTV, uploadingTV;
    Button reportBtn;
    ImageView[] picPreviews;
    ImageView reportTextUploading, pic1Uploading, pic2Uploading, pic3Uploading;
    ArrayList<String> picPaths;
    RelativeLayout mLayout;
    RelativeLayout uploadingScreen;
    int lastPreviewClicked, nextPendingPieceKey;
    ProgressBar reportTextProgress, pic1Progress, pic2Progress, pic3Progress;

    private final int PIC1 = 0,
            PIC2 = PIC1 + 1,
            PIC3 = PIC2 + 1,
            TOTAL_PICS = PIC3 + 1;

    private final String    DEETS_KEY = "details",
                            PIC_PATHS_KEY = "picPaths",
                            LASTPREVIEW_KEY = "last_preview",
                            PENDING_PIECE_KEY ="next_field",
                            PENDING_REPORT_TYPE_KEY = "report_to_send_id";

    AQuery aq;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mActivity = (MainActivity) getActivity();
        aq = new AQuery(mActivity);
        picPaths = new ArrayList<String>();
        for(int i = 0; i < TOTAL_PICS; i++)
            picPaths.add(null);
        picPreviews = new ImageView[TOTAL_PICS];
        Log.e(LogTags.NEWREPORT, "OnCreate " + this.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedState) {
        // Inflate the layout for this fragment
        Log.e(LogTags.NEWREPORT, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_new_report, container, false);
        mLayout = (RelativeLayout) view.findViewById(R.id.new_report);
        uploadingScreen = (RelativeLayout) view.findViewById(R.id.uploading_screen);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int pixels_per_dp = (int)(metrics.density + 0.5f);
        int padding_dp = 4;
        mLayout.setPadding(0, pixels_per_dp * padding_dp + mActivity.getActionBarHeight(), 0, 0);
        details = (DescriptionEditText) view.findViewById(R.id.newReportDetails);
        picPreviews[0] = (ImageView) view.findViewById(R.id.pic1);
        picPreviews[1] = (ImageView) view.findViewById(R.id.pic2);
        picPreviews[2] = (ImageView) view.findViewById(R.id.pic3);

        reportTextUploading = (ImageView) view.findViewById(R.id.reportUploadingIcon);
        pic1Uploading = (ImageView) view.findViewById(R.id.pic1UploadingIcon);
        pic2Uploading = (ImageView) view.findViewById(R.id.pic2UploadingIcon);
        pic3Uploading = (ImageView) view.findViewById(R.id.pic3UploadingIcon);
        reportTextProgress = (ProgressBar) view.findViewById(R.id.progressBarReportText);
        pic1Progress = (ProgressBar) view.findViewById(R.id.progressBarPic1);
        pic2Progress = (ProgressBar) view.findViewById(R.id.progressBarPic2);
        pic3Progress = (ProgressBar) view.findViewById(R.id.progressBarPic3);
        uploadingTV = (TextView) view.findViewById(R.id.uploadingText);
        reportBtn = (Button) view.findViewById(R.id.reportButton);
        reportBtn.setClickable(false);

        attachPicsTV = (TextView) view.findViewById(R.id.attachMorePicturesText);
        setListeners();
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.e(LogTags.NEWREPORT, "onResume");
        restorePics();
        mActivity.setNewReportFragmentListener(this);
    }

    private void restorePics() {
        int emptyPics = TOTAL_PICS;
        Log.e(LogTags.NEWREPORT, "restorePics size: " + picPaths.size());
        // if picPaths is empty, that means you're in a new newReportFragment
        if(picPaths.size() != 0){
            for (int i = 0; i < TOTAL_PICS; i++) {
                if (picPaths.get(i)!= null) {
                    aq.id(picPreviews[i]).image(getThumbnail(picPaths.get(i)));
                    emptyPics--;
                } else
                    aq.id(picPreviews[i]).image(R.drawable.pic_placeholder);
            }
        } else { 
            for (int i = 0; i < TOTAL_PICS; i++) {
                aq.id(picPreviews[i]).image(R.drawable.pic_placeholder);
            }
        }
        if (emptyPics > 1)
            attachPicsTV.setText("Attach " + emptyPics + " more pictures:");
        else if (emptyPics == 1)
            attachPicsTV.setText("Attach " + emptyPics + " more picture:");
        else
            attachPicsTV.setVisibility(View.INVISIBLE);
        attemptEnableSend();
    }

    // Returns dynamically sized thumbnail to populate picPreviews.
    private Bitmap getThumbnail(String picPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picPath, options);
        int picWidth = options.outWidth;
        int screenWidth = mActivity.getScreenWidth();
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

    private void setListeners() {
        details.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                attemptEnableSend();
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
            public void onClick(View view) {
                beamUpNewReport();
            }
        });
    }

    public void onPicPreviewClicked(int previewClicked) {
        lastPreviewClicked = previewClicked;
        mActivity.takePicture((NewReportFragment) getParentFragment(), lastPreviewClicked);
    }

    public void beamUpNewReport() {
        pendingReport = createNewReport();
        Log.e(LogTags.BACKEND_W, "Beam it up");
        new NewReportUploader(this).execute(pendingReport);
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(
                mActivity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(details.getWindowToken(), 0);
        details.setFocusable(false);
        uploadingScreen.setVisibility(View.VISIBLE);
        reportTextProgress.setVisibility(View.VISIBLE);
        for(int i = 0; i < TOTAL_PICS; i++)
            picPreviews[i].setClickable(false);
    }

    public void updatePostProgress(int progress){
        nextPendingPieceKey = progress;
        switch(progress){
            case 0:{
                reportTextProgress.setVisibility(View.VISIBLE);
                uploadingTV.setText("Uploading report text...");
                break;
            }
            case 1:{
                reportTextProgress.setVisibility(View.GONE);
                uploadingTV.setText("Uploading first picture...");
                pic1Progress.setVisibility(View.VISIBLE);
                reportTextUploading.setImageResource(R.drawable.report_uploaded);
                break;
            }
            case 2:{
                pic1Progress.setVisibility(View.GONE);
                uploadingTV.setText("Uploading second picture...");
                pic2Progress.setVisibility(View.VISIBLE);
                pic1Uploading.setImageResource(R.drawable.pic1_uploaded);
                break;
            }
            case 3:{
                pic2Progress.setVisibility(View.GONE);
                uploadingTV.setText("Uploading third picture...");
                pic3Progress.setVisibility(View.VISIBLE);
                pic2Uploading.setImageResource(R.drawable.pic2_uploaded);
                break;
            }
            case -1:{
                pic3Progress.setVisibility(View.GONE);
                uploadingTV.setText("Report uploaded!");
                pic3Uploading.setImageResource(R.drawable.pic3_uploaded);
                break;
            }
        }
    }

    public void uploadSuccess() {
        mActivity.clearNewReportData();
        clearData();
        clearView();
        Toast.makeText(mActivity, "Thank you for your report!", Toast.LENGTH_SHORT).show();
    }

    public void uploadFailure(final String failMessage){
        mActivity.clearNewReportData();
//        final Toast toast = Toast.makeText(this, "Failed to upload post!", Toast.LENGTH_SHORT);
//        toast.show();
        retryUpload();
    }

    private void clearData() {
        pendingReport = null;
        picPaths.clear();
        for (int i = 0; i < TOTAL_PICS; i++)
            picPaths.add(null);
    }

    private void clearView() {
        uploadingScreen.setVisibility(View.INVISIBLE);
        details.setText("");
        restorePics();
        details.setFocusable(true);
        for (int i = 0; i < TOTAL_PICS; i++)
            picPreviews[i].setClickable(true);
    }

    public void retryUpload() {
    //     beamItUp(reportId, nextPieceKey, pendingReport);
    }

    public Report createNewReport() {
        Log.e(LogTags.NEWREPORT, "createNewReport");
        return new Report(details.getText().toString(), mActivity.mUsername, mActivity.getLocation(),
                picPaths);
    }

    public void attemptEnableSend() {
        int i = 0;
        if (picPaths != null && !picPaths.isEmpty()) {
            while (i < TOTAL_PICS) {
                if (picPaths.get(i) == null) {
                    disableSend();
                    return;
                }
                i++;
            }
            if (!details.getText().toString().isEmpty())
                enableSend();
        }
    }

    private void enableSend() {
        reportBtn.setClickable(true);
        reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_clickable));
    }

    private void disableSend() {
        reportBtn.setClickable(false);
        reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_unclickable));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        for (int i = 0; i < TOTAL_PICS; i++)
//            picPaths.add(null);
        if (savedInstanceState != null) {
            if (savedInstanceState.getString(DEETS_KEY) != null)
                details.setText(savedInstanceState.getString(DEETS_KEY));
            lastPreviewClicked = savedInstanceState.getInt(LASTPREVIEW_KEY);
            restorePics();
            if (savedInstanceState.getStringArrayList(PIC_PATHS_KEY) != null)
                picPaths = new ArrayList(savedInstanceState.getStringArrayList(PIC_PATHS_KEY).subList(0, TOTAL_PICS));
            if (savedInstanceState.getInt(PENDING_REPORT_TYPE_KEY) != -1) { // NO_REPORT_TO_SEND
                nextPendingPieceKey = savedInstanceState.getInt(PENDING_PIECE_KEY);
                pendingReport = new Report(PENDING_REPORT_TYPE_KEY, savedInstanceState);
                // beamUpNewReport();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        if (details != null)
            bundle.putString(DEETS_KEY, details.getText().toString());
        bundle.putStringArrayList(PIC_PATHS_KEY, picPaths);
        bundle.putInt(LASTPREVIEW_KEY, lastPreviewClicked);
        if (pendingReport != null) {
            pendingReport.saveState(PENDING_REPORT_TYPE_KEY, bundle);
            bundle.putInt(PENDING_PIECE_KEY, nextPendingPieceKey);
        }
    }

}
