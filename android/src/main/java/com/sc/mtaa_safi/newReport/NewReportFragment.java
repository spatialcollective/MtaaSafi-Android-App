package com.sc.mtaa_safi.newReport;

import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sc.mtaa_safi.Community;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.feed.tags.Tag;
import com.sc.mtaa_safi.feed.tags.TagsCompletionView;
import com.sc.mtaa_safi.imageCapture.ImageCaptureActivity;
import com.squareup.picasso.Picasso;
import com.tokenautocomplete.TokenCompleteTextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NewReportFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    SimpleCursorAdapter mAdapter;
    public static final int REQUEST_IMAGE_CAPTURE = 1, MAX_PIC_COUNT = 3;
    public String detailsText = "", selectedAdmin = "", adminText = "";
    public int status = 0;
    public long selectedAdminId;
    public Tag[] tags;
    public ArrayList<String> picPaths = new ArrayList<String>();
    public ArrayAdapter<Tag> mTagAdapter;
    public TagsCompletionView tagsCompletionView;
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
        createToolbar(view);
        updateDetailsView();
        updatePicPreviews();
        setUpVillages();

        tags = new Tag[]{
                new Tag("water"),
                new Tag("sewage"),
                new Tag("cholera")
        };

        mTagAdapter = new ArrayAdapter<Tag>(getActivity(), android.R.layout.simple_list_item_1, tags);
        tagsCompletionView = (TagsCompletionView)getActivity().findViewById(R.id.enterTag);
        tagsCompletionView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Delete);
        tagsCompletionView.setSplitChar(' ');
        tagsCompletionView.setAdapter(mTagAdapter);
    }

    private void createToolbar(View view) {
        NewReportActivity act = (NewReportActivity) getActivity();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.new_toolbar);
        act.setSupportActionBar(toolbar);
        act.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        act.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
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
            addThumb(i);
        setPicButtonState((Button) getView().findViewById(R.id.take_pic));
    }

    private void addThumb(int i) {
        if (picPaths.get(i) != null) {
            ImageView thumb = (ImageView) ((LinearLayout) getView().findViewById(R.id.pic_previews)).getChildAt(i);
            thumb.setVisibility(View.VISIBLE);
            Bitmap bitmap = getThumbnail(picPaths.get(i));
            if (bitmap != null)
                thumb.setImageBitmap(bitmap);
        }
    }

    private void setPicButtonState(Button takePic) {
        if (picPaths.size() >= 3)
            takePic.setVisibility(View.GONE);
        else
            takePic.setVisibility(View.VISIBLE);
        if (picPaths.size() >= 1)
            takePic.setText(R.string.take_another_pic);
        else
            takePic.setText(R.string.take_pic);
    }

    private Bitmap getThumbnail(String picPath) {
        int thumbWidth = Utils.getScreenWidth(getActivity())/3;
        Bitmap bmp = BitmapFactory.decodeFile(picPath);
        if (bmp != null) {
            int origWidth = bmp.getWidth();
            int origHeight = bmp.getHeight();
            Log.i("NewReportFragment", "Width: "+origWidth+" Height: "+origHeight);
            if (origWidth > origHeight)
                return Bitmap.createScaledBitmap(bmp, thumbWidth, (origHeight * thumbWidth) / origWidth, false);
            else
                return Bitmap.createScaledBitmap(bmp, (origWidth * thumbWidth) / origHeight, thumbWidth, false);
        }
        return null;
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
        Intent intent = new Intent();
        intent.setClass(getActivity(),ImageCaptureActivity.class);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_IMAGE_CAPTURE)
            return;
        if (resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            Uri fileUri = Uri.parse(bundle.getString("IMAGE_FILE_NAME"));
//            Toast.makeText(getActivity(), "Image saved to:"+ fileUri.getPath(), Toast.LENGTH_LONG).show();

            picPaths.add(fileUri.getPath());

            updatePicPreviews();
            attemptEnableSendSave();
        }
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
