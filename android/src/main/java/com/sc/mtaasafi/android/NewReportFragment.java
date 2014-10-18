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
    NewReportActivity mActivity;
    PictureTakenListener mCallback;
    Report pendingReport;
    View mView;

    DescriptionEditText detailsView;
    Button reportBtn;
    RelativeLayout addPicButton, uploadingScreen;
    LinearLayout picPreviewContainer;
    int nextPendingPiece;
    ProgressBar reportTextProgress;

    private ArrayList<String> picPaths;
    private String detailsText;

    public final int REQUIRED_PIC_COUNT = 3;

    public final String DEETS_KEY = "details",
            PENDING_PIECE_KEY ="next_field",
            PENDING_REPORT_ID = "report_to_send_id",
            PIC_PATHS_KEY = "picPaths",
            IS_REPORT_PENDING_KEY = "report_pending",
            HAS_PENDING_REPORT_KEY = "has_pending_key";
            
    static final int REQUEST_IMAGE_CAPTURE = 1;

    AQuery aq;

    public interface PictureTakenListener {
        public void takePicture(int lastPreviewClicked);
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mActivity = (NewReportActivity) getActivity();
        aq = new AQuery(mActivity);
        picPaths = new ArrayList<String>();
        setRetainInstance(true);

        Log.e(LogTags.NEWREPORT, "OnCreate " + this.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        mView = inflater.inflate(R.layout.fragment_new_report, container, false);

        uploadingScreen = (RelativeLayout) mView.findViewById(R.id.uploading_screen);
        reportTextProgress = (ProgressBar) mView.findViewById(R.id.progressBarReportText);

        detailsView = (DescriptionEditText) mView.findViewById(R.id.newReportDetails);
        detailsText = "";
        picPreviewContainer = (LinearLayout) mView.findViewById(R.id.picPreviewContainer);
        addPicButton = (RelativeLayout) mView.findViewById(R.id.addPicsButtonLayout);
        reportBtn = (Button) mView.findViewById(R.id.reportButton);

        setListeners();
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
//        File file = new File(mCurrentPhotoPath);
//        if (file.length() != 0)
//            picPaths.add(mCurrentPhotoPath);

        Log.e("Retrieving State", "...");
        if (savedState != null) {
            Log.e("Retrieving State", "bundle exists");
            detailsText = savedState.getString(DEETS_KEY);
            Log.e("Retrieving State", "got: " + savedState.getString(DEETS_KEY));
            if (detailsText != null)
                detailsView.setText(detailsText);
            updatePicPreviews();
            attemptEnableSend();
            if (savedState.getBoolean(HAS_PENDING_REPORT_KEY)) {
                nextPendingPiece = savedState.getInt(PENDING_PIECE_KEY);
                pendingReport = new Report(PENDING_REPORT_ID, savedState);
                beamUpReport(pendingReport);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e("Saving State", "...");
        if (detailsText != null) {
            Log.e("Saving State", "details");
            outState.putString(DEETS_KEY, detailsText);
            Log.e("Saving State", "saved: " + outState.getString(DEETS_KEY));
        }
        outState.putInt(PENDING_PIECE_KEY, nextPendingPiece);
        if (pendingReport != null) {
            outState.putBoolean(HAS_PENDING_REPORT_KEY, true);
            pendingReport.saveState(PENDING_REPORT_ID, outState);
        }
    }
    public void setData(ArrayList<String> pics, String text) {
        this.picPaths = pics;
        this.detailsText = text;
    }

    public ArrayList<String> getPics() {
        return picPaths;
    }

    public String getDetails() {
        return detailsText;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.e(LogTags.NEWREPORT, "onResume");
        updatePicPreviews();
        attemptEnableSend();
    }

    // @Override
    //     public void onAttach(Activity activity) {
    //     super.onAttach(activity);
    //     try { // This makes sure that the container activity has implemented the callback interface.
    //         mCallback = (PictureTakenListener) activity;
    //     } catch (ClassCastException e) {
    //         throw new ClassCastException(activity.toString() + " must implement PictureTakenListener");
    //     }
    // }

    private void updatePicPreviews() {
        int i = 0, emptyPics = 0;
        while (i < REQUIRED_PIC_COUNT) {
            ImageView picPreview = (ImageView) mView.findViewWithTag(String.valueOf(i));
            if (picPaths.size() > i)
                aq.id(picPreview).image(getThumbnail(picPaths.get(i)));
            else {
                aq.id(picPreview).image(R.drawable.pic_placeholder);
                emptyPics++;
            }
            i++;
        }

        if (emptyPics == 0) {
            addPicButton.setVisibility(View.INVISIBLE);
        } else {
            TextView attachPicsTV = (TextView) mView.findViewById(R.id.attachMorePicturesText);
            attachPicsTV.setText("Need " + emptyPics + " more");
            addPicButton.setClickable(true);
        }
    }

    // Returns dynamically sized thumbnail to populate picPreviews.
    private Bitmap getThumbnail(String picPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picPath, options);
        int picWidth = options.outWidth;
        int screenWidth = 400;
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
        detailsView.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                detailsText = detailsView.getText().toString();
                attemptEnableSend();
            }
        });

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                beamUpNewReport();
            }
        });

        View.OnClickListener picLauncher = new View.OnClickListener() {
            @Override public void onClick(View view) {
                takePicture();
            }
        };
        addPicButton.setOnClickListener(picLauncher);
//        picPreviewContainer.setOnClickListener(picLauncher);
//        for (int i = 0; i < REQUIRED_PIC_COUNT - 1; i++)
//            picPreviewContainer.getChildAt(i).setOnClickListener(picLauncher);
    }

    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null){
            File photoFile = null;
            try {
                photoFile = createImageFile();
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
        Log.e("Activity Result", "Caught it!");
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            Log.e(LogTags.PHOTO, "onActivityResult");
            updatePicPreviews();
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_" + picPaths.size();
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        picPaths.add(image.getAbsolutePath());
        return image;
    }

    public void beamUpNewReport() {
        pendingReport = createNewReport();
        beamUpReport(pendingReport);
    }

    public void beamUpReport(Report report) {
        Log.e(LogTags.BACKEND_W, "Beam it up");
        new NewReportUploader(this).execute(pendingReport);
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(
                mActivity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(detailsView.getWindowToken(), 0);
        detailsView.setFocusable(false);
        uploadingScreen.setVisibility(View.VISIBLE);
        reportTextProgress.setVisibility(View.VISIBLE);
        addPicButton.setClickable(false);
    }

    public Report createNewReport() {
        Log.e(LogTags.NEWREPORT, "createNewReport");
        return new Report(detailsText, "", mActivity.getLocation(), // mActivity.mUsername
                picPaths);
    }

    public void updatePostProgress(int progress){
        nextPendingPiece = progress;
        switch (progress) {
            case 0:
                updateProgressView(0, 0, R.id.progressBarReportText, 0);
                break;
            case 1:
                updateProgressView(R.id.progressBarReportText, R.id.reportUploadingIcon, R.id.progressBarPic1, R.drawable.report_uploaded);
                break;
            case 2:
                updateProgressView(R.id.progressBarPic1, R.id.pic1UploadingIcon, R.id.progressBarPic2, R.drawable.pic1_uploaded);
                break;
            case 3:
                updateProgressView(R.id.progressBarPic2, R.id.pic2UploadingIcon, R.id.progressBarPic3, R.drawable.pic2_uploaded);
                break;
            case -1:
                updateProgressView(R.id.progressBarPic3, R.id.pic3UploadingIcon, 0, R.drawable.pic3_uploaded);
                break;
        }
    }

    private void updateProgressView(int doneProgressId, int doneViewId, int workingId, int drawable) {
        if (doneProgressId != 0) {
            ProgressBar done = (ProgressBar) mView.findViewById(doneProgressId);
            done.setVisibility(View.GONE);
        }
        if (doneViewId != 0 && drawable != 0) {
            ImageView doneView = (ImageView) mView.findViewById(doneViewId);
            doneView.setImageResource(drawable);
        }
        if (workingId != 0) {
            ProgressBar working = (ProgressBar) mView.findViewById(workingId);
            working.setVisibility(View.VISIBLE);
        }
    }

    public void uploadSuccess() {
        Toast.makeText(mActivity, "Thank you for your report!", Toast.LENGTH_SHORT).show();
        clearData();
        clearView();
//        mActivity.clearNewReportData();
    }

    public void uploadFailure(final String failMessage){
//        final Toast toast = Toast.makeText(this, "Failed to upload post!", Toast.LENGTH_SHORT);
//        toast.show();
        retryUpload();
//        mActivity.clearNewReportData();
    }

    public void clearView() {
        uploadingScreen.setVisibility(View.INVISIBLE);
        updatePicPreviews();
        detailsText = "";
        detailsView.setText("");
        detailsView.setFocusable(true);
    }

    public void clearData() {
        pendingReport = null;
        picPaths.clear();
    }

    public void retryUpload(){
    //     beamItUp(reportId, nextPieceKey, pendingReport);
    }

    public void attemptEnableSend() {
        boolean hasDetails = !detailsText.isEmpty();
        if (!hasDetails || picPaths == null || picPaths.isEmpty() || picPaths.size() < REQUIRED_PIC_COUNT)
            disableSend();
        else if (hasDetails && picPaths.size() == REQUIRED_PIC_COUNT)
            enableSend();
    }

    private void enableSend() {
        reportBtn.setClickable(true);
        reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_clickable));
    }

    private void disableSend() {
        reportBtn.setClickable(false);
        reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_unclickable));
    }
}