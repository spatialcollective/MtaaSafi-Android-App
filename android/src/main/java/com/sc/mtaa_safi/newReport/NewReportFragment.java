package com.sc.mtaa_safi.newReport;

import android.content.Intent;
import android.database.Cursor;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.ComplexPreferences;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.PrefUtils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.location.LocationData;
import com.sc.mtaa_safi.location.SyncLocationData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class NewReportFragment extends Fragment {
    public static final int REQUEST_IMAGE_CAPTURE = 1, MAX_PIC_COUNT = 3;
    public String detailsText = "";
    public JSONObject locationJSON;
    private ComplexPreferences cp;
    public ArrayList<String> picPaths = new ArrayList<String>();

    public List<String> villages;
    public HashMap<String, Integer> villageIdMap;
    public HashMap<String, ArrayList<String>> landmarkMap;
    public HashMap<String, Integer> landmarkIdMap;
    private String villageSelected;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setRetainInstance(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        if (NetworkUtils.isOnline(getActivity()))
            new SyncLocationData(getActivity()).execute();
        return inflater.inflate(R.layout.fragment_new_report, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedState) {
        cp = PrefUtils.getPrefs(getActivity());
        locationJSON = new JSONObject();
        updateDetailsView();
        updatePicPreviews();
        addVillages();
        setUpVillages();
    }

    private void setUpVillages(){
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) getView().findViewById(R.id.enterWard);
        autoComplete.setAdapter(new ArrayAdapter<String>
                (getActivity(), android.R.layout.select_dialog_item, villages));
        autoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String trimText = s.toString().trim();
                if (!trimText.isEmpty()) {
                    villageSelected = trimText;
                    try {

                        locationJSON.put("admin", villageSelected);
                        if (villageIdMap.containsKey(villageSelected)){
                            locationJSON.put("adminId", villageIdMap.get(villageSelected));
                        } else {
                            if (locationJSON.has("adminId"))
                                locationJSON.remove("adminId");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    revealSpinner();
                    attemptEnableSendSave();
                }
            }
            @Override public void afterTextChanged(Editable s) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });
    }

    private void revealSpinner() {
        if (landmarkMap.containsKey(villageSelected)) {
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_item, landmarkMap.get(villageSelected));
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            final Spinner landmarkSpinner = (Spinner) getView().findViewById(R.id.landmarkSpinner);
            landmarkSpinner.setAdapter(dataAdapter);
            landmarkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        locationJSON.put("landmark", landmarkSpinner.getSelectedItem().toString());
                        if (landmarkIdMap.containsKey(landmarkSpinner.getSelectedItem().toString()))
                            locationJSON.put("landmarkId", landmarkIdMap.get(landmarkSpinner.getSelectedItem().toString()));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            getView().findViewById(R.id.landmarkLayout).setVisibility(View.VISIBLE);
        }
    }

    private void addVillages(){
        villages = new ArrayList<>();
        landmarkMap = new HashMap<>();
        villageIdMap = new HashMap<>();
        landmarkIdMap = new HashMap<>();

                Cursor villageCursor = getActivity().getContentResolver().query(Contract.Admin.ADMIN_URI, LocationData.ADMIN_PROJECTION,
                null, null, null);
        while(villageCursor.moveToNext()){
            villages.add(villageCursor.getString(villageCursor.getColumnIndexOrThrow(Contract.Admin.COLUMN_NAME)));
            villageIdMap.put(villageCursor.getString(villageCursor.getColumnIndexOrThrow(Contract.Admin.COLUMN_NAME)),
                    villageCursor.getInt(villageCursor.getColumnIndexOrThrow(Contract.Admin._ID)));
            Cursor landmarksCursor = getActivity().getContentResolver().query(Contract.Landmark.LANDMARK_URI, LocationData.LANDMARK_PROJECTION,
                    Contract.Landmark.COLUMN_FK_ADMIN + " = " + villageCursor.getInt(villageCursor.getColumnIndexOrThrow(Contract.Admin._ID)),
                    null, null);
            while(landmarksCursor.moveToNext()){
                addLandmark(landmarksCursor.getString(landmarksCursor.getColumnIndexOrThrow(Contract.Landmark.COLUMN_NAME)),
                        villageCursor.getString(villageCursor.getColumnIndexOrThrow(Contract.Admin.COLUMN_NAME)));
                landmarkIdMap.put(landmarksCursor.getString(landmarksCursor.getColumnIndexOrThrow(Contract.Landmark.COLUMN_NAME)),
                        landmarksCursor.getInt(landmarksCursor.getColumnIndexOrThrow(Contract.Landmark._ID)));
            }

        }
    }

    private void addLandmark(String landmarkName, String villageName) {
        ArrayList<String> list;
        if (landmarkMap.containsKey(villageName))
            list = landmarkMap.get(villageName);
        else
            list = new ArrayList<>();
        list.add(landmarkName);
        landmarkMap.put(villageName, list);
    }


    @Override
    public void onResume(){
        super.onResume();
        attemptEnableSendSave();
    }

    private void updatePicPreviews() {
        Log.i("new report frag", "Pic paths was size " + picPaths.size());
        for (int i = 0; i < picPaths.size(); i++)
            if (picPaths.get(i) != null) {
                ImageView thumb = (ImageView) ((LinearLayout) getView().findViewById(R.id.pic_previews)).getChildAt(i);
                thumb.setVisibility(View.VISIBLE);
                thumb.setImageBitmap(getThumbnail(picPaths.get(i)));
            }
        if (picPaths.size() >= 3)
            getView().findViewById(R.id.take_pic).setVisibility(View.GONE);
        else
            getView().findViewById(R.id.take_pic).setVisibility(View.VISIBLE);
    }

    private Bitmap getThumbnail(String picPath) {
        int thumbWidth = cp.getObject(PrefUtils.SCREEN_WIDTH, Integer.class)/3;
        Bitmap bmp = BitmapFactory.decodeFile(picPath);

        int origWidth = bmp.getWidth();
        int origHeight = bmp.getHeight();
        if (origWidth > origHeight)
            return Bitmap.createScaledBitmap(bmp, thumbWidth, (origHeight * thumbWidth) / origWidth, false);
        else
            return Bitmap.createScaledBitmap(bmp, (origWidth * thumbWidth) / origHeight, thumbWidth, false);
    }

    private void updateDetailsView() {
        SafiEditText detailsView = (SafiEditText) getView().findViewById(R.id.newReportDetails);
        if (detailsText != null && !detailsText.equals(""))
            detailsView.setText(detailsText);
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

    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null){
            try {
                File photoFile = createImageFile();
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
        File file = new File(picPaths.get(picPaths.size() - 1));
        if (file.length() == 0) {
            picPaths.remove(picPaths.size() - 1);
            file.delete();
        }
        updatePicPreviews();
        attemptEnableSendSave();
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_" + picPaths.size();
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.i("FILE PATH", image.getAbsolutePath());
        picPaths.add(image.getAbsolutePath());
        return image;
    }

    public void attemptEnableSendSave() {
        View view = getView();
        if (view == null)
            return;
        if (detailsText.isEmpty() || picPaths == null || picPaths.isEmpty() || villageSelected == null || villageSelected.isEmpty()) {
            view.findViewById(R.id.sendButton).setEnabled(false);
            view.findViewById(R.id.saveButton).setEnabled(false);
        } else {
            view.findViewById(R.id.sendButton).setEnabled(true);
            view.findViewById(R.id.saveButton).setEnabled(true);
        }
    }
}
