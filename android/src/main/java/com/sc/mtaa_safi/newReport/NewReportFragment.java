package com.sc.mtaa_safi.newReport;

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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.sc.mtaa_safi.Landmark;
import com.sc.mtaa_safi.SystemUtils.ComplexPreferences;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.PrefUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class NewReportFragment extends Fragment {
    public static final int REQUEST_IMAGE_CAPTURE = 1, MAX_PIC_COUNT = 3;
    public String detailsText = "";
    private ComplexPreferences cp;
    public ArrayList<String> picPaths = new ArrayList<String>();

    public List<String> villages = Arrays.asList("Mabatini", "Mashimoni", "Mathare 3A",
                                                "Mathare 3B", "Mathare 3C", "Mathare No10",
                                                "Mathare Village 1", "Mathare Village 2", "Thayu");
    public HashMap<String, ArrayList<String>> landmarkMap;
    private String villageSelected;

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
        cp = PrefUtils.getPrefs(getActivity());
        updateDetailsView();
        updatePicPreviews();
        setUpVillages();
        addLandmarks();
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
            ((Spinner) getView().findViewById(R.id.landmarkSpinner)).setAdapter(dataAdapter);
            getView().findViewById(R.id.landmarkLayout).setVisibility(View.VISIBLE);
        }
    }

    private void addLandmark(String landmarkName, double lon, double lat, String villageName) {
        ArrayList<String> list;
//        Landmark landmark = new Landmark(landmarkName, lon, lat, villageName);
        if (landmarkMap.containsKey(villageName))
            list = landmarkMap.get(villageName);
        else
            list = new ArrayList<String>();
        list.add(landmarkName);
        landmarkMap.put(villageName, list);
    }
    private void addLandmarks() {
        landmarkMap = new HashMap<String, ArrayList<String>>();
        addLandmark("Good Samaritan", 36.85194318395180346, -1.26457937342121363, "Mathare 3B");
        addLandmark("Redeemed Gospel Church", 36.85005637859914174, -1.26454956166788746, "Mathare Village 2");
        addLandmark("Youth Foundation", 36.84638180025920917, -1.26348753307809458, "Mathare Village 1");
        addLandmark("Assemblies of God", 36.84481007694373034, -1.26414277649919193, "Mathare Village 1");
        addLandmark("Pequininos", 36.84397241181576277, -1.26491394066149865, "Mathare Village 1");
        addLandmark("St. Theresas Girls", 36.84728749565758932, -1.26527911200816279, "Mathare Village 1");
        addLandmark("Karambee Petrol Station", 36.84431675221043179, -1.26696390531771019, "Mathare Village 1");
        addLandmark("Snack Bar", 36.84387710666648985, -1.26707060961897744, "Mathare Village 1");
        addLandmark("Oil Libya", 36.85162397726742256, -1.26607107216148784, "Mathare Village 2");
        addLandmark("White Castle", 36.85134882014409641, -1.26517660523235498, "Mathare 3B");
        addLandmark("B. H", 36.85312085640122604, -1.26545576667801307, "Mathare 3B");
        addLandmark("Juja Road Self Help Group", 36.85381388711459039, -1.26429030372386531, "Mathare 3B");
        addLandmark("Valley View School", 36.85293214623843738, -1.2646354987753452, "Mathare 3B");
        addLandmark("Austin Ground", 36.85484624072717708, -1.26560428268131386, "Mathare 3B");
        addLandmark("Kiboro Primary School", 36.85412161661028563, -1.26538247269166759, "Mathare 3B");
        addLandmark("KENWA Bondeni", 36.85408287525691406, -1.26415815667248399, "Mathare 3B");
        addLandmark("Airforce", 36.85916936929667997, -1.26560258410458548, "Mathare 3C");
        addLandmark("Community Outreach Church", 36.86018571494818019, -1.26363374161378728, "Mathare No10");
        addLandmark("Community Transformers", 36.86086849685216293, -1.26356194470018823, "Mathare No10");
        addLandmark("No. 10 Stage", 36.86125183997971533, -1.26368933913811854, "Mathare No10");
        addLandmark("Posta", 36.85749767374939267, -1.26555412902389786, "Mathare 3A");
        addLandmark("Daraja Mbili", 36.85829848914461593, -1.26410518243599457, "Mathare 3C");
        addLandmark("Gumba Market", 36.86153198657642349, -1.26134865402846086, "Mashimoni");
        addLandmark("Why Not", 36.86233654073090804, -1.26104221736640465, "Mashimoni");
        addLandmark("Kwa Kariuki", 36.86275971310861621, -1.25963648604040812, "Mabatini");
        addLandmark("Twaweza Community Project", 36.8621164748234591, -1.26139149643420323, "Mashimoni");
        addLandmark("St. Micheal", 36.86271157597064985, -1.26132256700885548, "Mashimoni");
        addLandmark("St. Paul and John Catholic Church", 36.86338930035581285, -1.26171847747460486, "Thayu");
        addLandmark("Kwa Kanji", 36.86362977134245966, -1.25928362471007782, "Mabatini");
        addLandmark("Thayu Petrol Station", 36.86323861828417847, -1.26299676932670657, "Thayu");
        addLandmark("Chiefs Camp", 36.86471034086649468, -1.26169897825792732, "Mabatini");
        addLandmark("Mabatini Pipeline", 36.86478374470465269, -1.26056711265685495, "Mabatini");
        addLandmark("AIC Zion", 36.86217474504422142, -1.26304530723085162, "Thayu");
        addLandmark("Undugu Polytechnic", 36.8638574563161896, -1.26106053567306975, "Mabatini");
        addLandmark("Ruwawa Plaza Magorofani", 36.86416540342514025, -1.25902755953817769, "Mabatini");
        addLandmark("Mathare Hope Family School", 36.86293814096197963, -1.26160455891760748, "Mashimoni");
        addLandmark("Mathare Special School", 36.8651829040230723, -1.26137108646487373, "Mabatini");
        addLandmark("Sayun", 36.86423859666042091, -1.25913729345556935, "Mabatini");
        addLandmark("Mabatini Kwa KANU", 36.8655824539915784, -1.26219459157149849, "Mabatini");
        addLandmark("Total Petrol Station", 36.84528920656834572, -1.26670492680081814, "Mathare Village 1");
        addLandmark("Al Badir Petrol Station", 36.84891561271554394, -1.26587956196086493, "Mathare Village 2");
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
        if (detailsText != null && detailsText != "")
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
