package com.sc.mtaasafi.android.newReport;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.sc.mtaasafi.android.SystemUtils.LogTags;
import com.sc.mtaasafi.android.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NewReportFragment extends Fragment {
    ImageView[] picPreviews;
    EditText detailsView;

    public String detailsText;
    public ArrayList<String> picPaths;
    private int previewClicked;

    public static final int REQUEST_IMAGE_CAPTURE = 1,
        MAX_PIC_COUT = 3,
        PIC1 = 0, PIC2 = 1, PIC3 = 2;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        detailsText = "";
        picPaths = new ArrayList<String>();
        for(int i= 0; i < MAX_PIC_COUT; i++)
            picPaths.add(null);
        Log.e(LogTags.NEWREPORT, "OnCreate " + this.toString());
        picPreviews = new ImageView[MAX_PIC_COUT];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_new_report, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedState) {
	    picPreviews[PIC1] = (ImageButton) view.findViewById(R.id.pic1);
        picPreviews[PIC2] = (ImageButton) view.findViewById(R.id.pic2);
        picPreviews[PIC3] = (ImageButton) view.findViewById(R.id.pic3);
        detailsView = (SafiEditText) view.findViewById(R.id.newReportDetails);
        if (detailsText != null && detailsText != "")
            detailsView.setText(detailsText);
        attemptEnableSendSave();
        updatePicPreviews();
        setListeners();
    }

    @Override
    public void onResume(){
        super.onResume();
    }
    @Override
    public void onStop(){
        super.onStop();
        for(ImageView picPreview : picPreviews)
            picPreview = null;
        if(detailsView != null)
            detailsView = null;
    }

    private void updatePicPreviews() {
        int emptyPics = 0;
        for(int i = 0; i < picPaths.size(); i++){
            if(picPaths.get(i) != null)
               picPreviews[i].setImageBitmap(getThumbnail(picPaths.get(i)));
            else
                emptyPics++;
        }
        switch (emptyPics){
            case 0: picPreviews[PIC3].setVisibility(View.VISIBLE);
            case 1: picPreviews[PIC3].setVisibility(View.VISIBLE);
            case 2: picPreviews[PIC2].setVisibility(View.VISIBLE);
            case 3: picPreviews[PIC1].setVisibility(View.VISIBLE);
        }
        Log.e("PicPreviews", "Pic paths was size " + picPaths.size());
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

    private void setListeners() {
        detailsView.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) { }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                detailsText = s.toString();
                attemptEnableSendSave();
            }
        });
    }

    public void takePicture(int clicked) {
        previewClicked = clicked;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null){
            try {
                File photoFile = createImageFile(previewClicked);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException ex){
                Toast.makeText(getActivity(), "Couldn't create file", Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(getActivity(), "Couldn't resolve activity", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_IMAGE_CAPTURE)
            return;
        File file = new File(picPaths.get(previewClicked));
        if (file.length() == 0){
            picPaths.remove(previewClicked);
            file.delete();
        }
        updatePicPreviews();
        attemptEnableSendSave();
    }

    private File createImageFile(int previewClicked) throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_" + picPaths.size();
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.e("FILE PATH", image.getAbsolutePath());
        picPaths.set(previewClicked, image.getAbsolutePath());
        Log.e("PIC PATHS", "Pic path: "+ previewClicked + ". " + picPaths.get(previewClicked));
        return image;
    }

    public void attemptEnableSendSave() {
        View view = getView();
        if (view == null)
            return;
        if (detailsText.isEmpty() || picPaths == null || picPaths.isEmpty()) {
            view.findViewById(R.id.sendButton).setEnabled(false);
            view.findViewById(R.id.saveButton).setEnabled(false);
        } else {
            view.findViewById(R.id.sendButton).setEnabled(true);
            view.findViewById(R.id.saveButton).setEnabled(true);
        }
    }
}
