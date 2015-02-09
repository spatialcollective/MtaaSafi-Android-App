package com.sc.mtaa_safi.newReport;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sc.mtaa_safi.Community;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.ComplexPreferences;
import com.sc.mtaa_safi.SystemUtils.PrefUtils;
import com.sc.mtaa_safi.database.Contract;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NewReportFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    SimpleCursorAdapter mAdapter;
    public static final int REQUEST_IMAGE_CAPTURE = 1, MAX_PIC_COUNT = 3;
    public String detailsText = "", selectedAdmin = "", adminText = "";
    public long selectedAdminId;
    private ComplexPreferences cp;
    public ArrayList<String> picPaths = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setRetainInstance(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_new_report, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedState) {
        NewReportActivity act = (NewReportActivity) getActivity();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.new_toolbar);
        act.setSupportActionBar(toolbar);
        act.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        act.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_remove);

        cp = PrefUtils.getPrefs(getActivity());
        updateDetailsView();
        updatePicPreviews();
        
        setUpVillages();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         return new CursorLoader(getActivity(), Contract.Admin.ADMIN_URI,
                 Community.ADMIN_PROJECTION, null, null, null);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) { mAdapter.swapCursor(cursor); }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { mAdapter.swapCursor(null); }

    private void setUpVillages() {
        mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.select_dialog_item, null,
                Community.ADMIN_FROM, Community.ADMIN_TO, 0);
        mAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override public CharSequence convertToString(Cursor cursor) {
                return cursor.getString(cursor.getColumnIndexOrThrow(Contract.Admin.COLUMN_NAME));
            }
        });

        AutoCompleteTextView autoComplete = (AutoCompleteTextView) getView().findViewById(R.id.enterWard);
        autoComplete.setAdapter(mAdapter);
        autoComplete.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) { }
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) mAdapter.getItem(position);
                selectedAdminId = id;
                selectedAdmin = c.getString(c.getColumnIndex(Contract.Admin.COLUMN_NAME));
                c.close();
            }
        });
        autoComplete.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) { }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adminText = s.toString().trim();
                if (!adminText.isEmpty())
                    attemptEnableSendSave();
            }
        });
        getLoaderManager().initLoader(0, null, this);
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
        if (detailsText.isEmpty() || picPaths == null || picPaths.isEmpty() || adminText == null || adminText.isEmpty()) {
            view.findViewById(R.id.sendButton).setEnabled(false);
            view.findViewById(R.id.saveButton).setEnabled(false);
        } else {
            view.findViewById(R.id.sendButton).setEnabled(true);
            view.findViewById(R.id.saveButton).setEnabled(true);
        }
    }
}
