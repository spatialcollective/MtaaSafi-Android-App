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
    private ArrayList<String> picPaths;
    private String detailsText;

    public final int REQUIRED_PIC_COUNT = 3;

    public final String DEETS_KEY = "details",
            PENDING_PIECE_KEY ="next_field",
            PENDING_REPORT_ID = "report_to_send_id",
            HAS_PENDING_REPORT_KEY = "has_pending_key";
            
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        picPaths = new ArrayList<String>();
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_new_report, container, false);
        detailsText = "";
        setListeners(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        // if (savedState != null && savedState.getBoolean(HAS_PENDING_REPORT_KEY))
        //     beamUpReport(pendingReport);
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
        attemptEnableSend();
    }

    private void updatePicPreviews() {
        AQuery aq = new AQuery(getActivity());
        int i = 0, emptyPics = 0;
        while (i < REQUIRED_PIC_COUNT) {
            ImageView picPreview = (ImageView) getView().findViewWithTag(String.valueOf(i));
            if (picPaths.size() > i)
                aq.id(picPreview).image(getThumbnail(picPaths.get(i)));
            else {
                aq.id(picPreview).image(R.drawable.pic_placeholder);
                emptyPics++;
            }
            i++;
        }

        RelativeLayout addPicButton = (RelativeLayout) getView().findViewById(R.id.addPicsButtonLayout);
        if (emptyPics == 0) {
            addPicButton.setVisibility(View.INVISIBLE);
        } else {
            TextView attachPicsTV = (TextView) getView().findViewById(R.id.attachMorePicturesText);
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

    private void setListeners(View view) {
        final DescriptionEditText detailsView = (DescriptionEditText) view.findViewById(R.id.newReportDetails);

        detailsView.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                detailsText = detailsView.getText().toString();
                attemptEnableSend();
            }
        });

        view.findViewById(R.id.reportButton)
            .setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    beamUpNewReport();
                }
        });

        View.OnClickListener picLauncher = new View.OnClickListener() {
            @Override public void onClick(View view) {
                takePicture();
            }
        };
        view.findViewById(R.id.addPicsButtonLayout).setOnClickListener(picLauncher);
        // (LinearLayout) picPreviewContainer = view.findViewById(R.id.picPreviewContainer);
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
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            File file = new File(picPaths.get(picPaths.size() - 1));
            if (file.length() == 0)
                picPaths.remove(picPaths.size() - 1);
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
        pendingReport = new Report(detailsText, "", "", //mActivity.mUsername, mActivity.getLocation(),
                picPaths);
        beamUpReport(pendingReport);
    }

    public void beamUpReport(Report report) {
        Log.e(LogTags.BACKEND_W, "Beam it up");
        new NewReportUploader(this).execute(pendingReport);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().findViewById(R.id.newReportDetails).getWindowToken(), 0);
        getView().findViewById(R.id.uploading_screen).setVisibility(View.VISIBLE);
    }

    public void updatePostProgress(int progress){
        View view = getView();
        nextPendingPiece = progress;
        switch (progress) {
            case 0:
                updateProgressView(view, 0, 0, R.id.progressBarReportText, 0);
                break;
            case 1:
                updateProgressView(view, R.id.progressBarReportText, R.id.reportUploadingIcon, R.id.progressBarPic1, R.drawable.report_uploaded);
                break;
            case 2:
                updateProgressView(view, R.id.progressBarPic1, R.id.pic1UploadingIcon, R.id.progressBarPic2, R.drawable.pic1_uploaded);
                break;
            case 3:
                updateProgressView(view, R.id.progressBarPic2, R.id.pic2UploadingIcon, R.id.progressBarPic3, R.drawable.pic2_uploaded);
                break;
            case -1:
                updateProgressView(view, R.id.progressBarPic3, R.id.pic3UploadingIcon, 0, R.drawable.pic3_uploaded);
                break;
        }
    }

    private void updateProgressView(View view, int doneProgressId, int doneViewId, int workingId, int drawable) {
        if (doneProgressId != 0) {
            ProgressBar done = (ProgressBar) view.findViewById(doneProgressId);
            done.setVisibility(View.GONE);
        }
        if (doneViewId != 0 && drawable != 0) {
            ImageView doneView = (ImageView) view.findViewById(doneViewId);
            doneView.setImageResource(drawable);
        }
        if (workingId != 0) {
            ProgressBar working = (ProgressBar) view.findViewById(workingId);
            working.setVisibility(View.VISIBLE);
        }
    }

    public void uploadSuccess() {
        Toast.makeText(getActivity(), "Thank you for your report!", Toast.LENGTH_SHORT).show();
        clearData();
        clearView();
        getActivity().finish();
    }

    public void uploadFailure(final String failMessage){
        Toast.makeText(getActivity(), "Failed to upload post!", Toast.LENGTH_SHORT).show();
        retryUpload();
    }

    public void clearView() {
        DescriptionEditText detailsView = (DescriptionEditText) getView().findViewById(R.id.newReportDetails);
        getView().findViewById(R.id.uploading_screen).setVisibility(View.INVISIBLE);
        updatePicPreviews();
        detailsText = "";
        detailsView.setText("");
        detailsView.setFocusable(true);
    }

    public void clearData() {
        pendingReport = null;
        picPaths.clear();
    }

    public void retryUpload() {
    //     beamItUp(reportId, nextPieceKey, pendingReport);
    }

    public void attemptEnableSend() {
        Button reportBtn = (Button) getView().findViewById(R.id.reportButton);
        boolean hasDetails = !detailsText.isEmpty();
        if (!hasDetails || picPaths == null || picPaths.isEmpty() || picPaths.size() < REQUIRED_PIC_COUNT)
            disableSend(reportBtn);
        else if (hasDetails && picPaths.size() == REQUIRED_PIC_COUNT)
            enableSend(reportBtn);
    }

    private void enableSend(Button reportBtn) {
        reportBtn.setClickable(true);
        reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_clickable));
    }

    private void disableSend(Button reportBtn) {
        reportBtn.setClickable(false);
        reportBtn.setBackgroundColor(getResources().getColor(R.color.report_button_unclickable));
    }
}