package com.sc.mtaasafi.android.newReport;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.sc.mtaasafi.android.SystemUtils.LogTags;
import com.sc.mtaasafi.android.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NewReportFragment extends Fragment {
    ImageView[] picPreviews;
    EditText detailsView;

    public String detailsText;
    public ArrayList<String> picPaths;
    public ArrayList<String> villages;
    public ArrayList<ArrayList<String>> landMarkLists;
    public HashMap<String, Location> landmarkMap;
    private int previewClicked;
    private String villageSelected;
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
        updatePicPreviews();
        setListeners();
        setUpLandMarks();
    }
    private void setUpVillageText(){
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) getView().findViewById(R.id.enterWard);
        autoComplete.setThreshold(1);
        autoComplete.setAdapter(new ArrayAdapter<String>
                (getActivity(), android.R.layout.select_dialog_item, villages));
        autoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                attemptVillageSelect(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });
    }
    private void setUpLandMarks(){
        String[] villagesArray = {"Mabatini","Mashimoni","Mathare 3A", "Mathare 3B","Mathare 3C",
                                    "Mathare No10", "Mathare Village 1","Mathare Village 2","Thayu"};
        villages = new ArrayList<String>();
        landMarkLists = new ArrayList<ArrayList<String>>();
        for(int i =0; i < villagesArray.length; i++){
            villages.add(villagesArray[i]);
            ArrayList<String> landMarkList = new ArrayList<String>();
            landMarkList.add("What's nearby?");
            landMarkLists.add(landMarkList);
        }
        landmarkMap = new HashMap<String, Location>();
        Spinner landmarks = (Spinner) getView().findViewById(R.id.landMarkSpinner);
        landmarks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
                    String landMark = getLandMarkList(villageSelected).get(position);
                    ((NewReportActivity) getActivity()).setLocation(landmarkMap.get(landMark));
                    attemptEnableSendSave();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        addLandMarks();
        setUpVillageText();
    }
    private void addLandMarks(){
        addLandMark("Good Samaritan",36.85194318395180346,-1.26457937342121363,"Mathare 3B");
        addLandMark("Redeemed Gospel Church",36.85005637859914174,-1.26454956166788746,"Mathare Village 2");
        addLandMark("Youth Foundation",36.84638180025920917,-1.26348753307809458,"Mathare Village 1");
        addLandMark("Assemblies of God",36.84481007694373034,-1.26414277649919193,"Mathare Village 1");
        addLandMark("Pequininos",36.84397241181576277,-1.26491394066149865,"Mathare Village 1");
        addLandMark("St. Theresas Girls",36.84728749565758932,-1.26527911200816279,"Mathare Village 1");
        addLandMark("Karambee Petrol Station",36.84431675221043179,-1.26696390531771019,"Mathare Village 1");
        addLandMark("Snack Bar",36.84387710666648985,-1.26707060961897744,"Mathare Village 1");
        addLandMark("Oil Libya",36.85162397726742256,-1.26607107216148784,"Mathare Village 2");
        addLandMark("White Castle",36.85134882014409641,-1.26517660523235498,"Mathare 3B");
        addLandMark("B. H",36.85312085640122604,-1.26545576667801307,"Mathare 3B");
        addLandMark("Juja Road Self Help Group",36.85381388711459039,-1.26429030372386531,"Mathare 3B");
        addLandMark("Valley View School",36.85293214623843738,-1.2646354987753452,"Mathare 3B");
        addLandMark("Austin Ground",36.85484624072717708,-1.26560428268131386,"Mathare 3B");
        addLandMark("Kiboro Primary School",36.85412161661028563,-1.26538247269166759,"Mathare 3B");
        addLandMark("KENWA Bondeni",36.85408287525691406,-1.26415815667248399,"Mathare 3B");
        addLandMark("Airforce",36.85916936929667997,-1.26560258410458548,"Mathare 3C");
        addLandMark("Community Outreach Church",36.86018571494818019,-1.26363374161378728,"Mathare No10");
        addLandMark("Community Transformers",36.86086849685216293,-1.26356194470018823,"Mathare No10");
        addLandMark("No. 10 Stage",36.86125183997971533,-1.26368933913811854,"Mathare No10");
        addLandMark("Posta",36.85749767374939267,-1.26555412902389786,"Mathare 3A");
        addLandMark("Daraja Mbili",36.85829848914461593,-1.26410518243599457,"Mathare 3C");
        addLandMark("Gumba Market",36.86153198657642349,-1.26134865402846086,"Mashimoni");
        addLandMark("Why Not",36.86233654073090804,-1.26104221736640465,"Mashimoni");
        addLandMark("Kwa Kariuki",36.86275971310861621,-1.25963648604040812,"Mabatini");
        addLandMark("Twaweza Community Project",36.8621164748234591,-1.26139149643420323,"Mashimoni");
        addLandMark("St. Micheal",36.86271157597064985,-1.26132256700885548,"Mashimoni");
        addLandMark("St. Paul and John Catholic Church",36.86338930035581285,-1.26171847747460486,"Thayu");
        addLandMark("Kwa Kanji",36.86362977134245966,-1.25928362471007782,"Mabatini");
        addLandMark("Thayu Petrol Station",36.86323861828417847,-1.26299676932670657,"Thayu");
        addLandMark("Chiefs Camp",36.86471034086649468,-1.26169897825792732,"Mabatini");
        addLandMark("Mabatini Pipeline",36.86478374470465269,-1.26056711265685495,"Mabatini");
        addLandMark("AIC Zion",36.86217474504422142,-1.26304530723085162,"Thayu");
        addLandMark("Undugu Polytechnic",36.8638574563161896,-1.26106053567306975,"Mabatini");
        addLandMark("Ruwawa Plaza Magorofani",36.86416540342514025,-1.25902755953817769,"Mabatini");
        addLandMark("Mathare Hope Family School",36.86293814096197963,-1.26160455891760748,"Mashimoni");
        addLandMark("Mathare Special School",36.8651829040230723,-1.26137108646487373, "Mabatini");
        addLandMark("Sayun",36.86423859666042091,-1.25913729345556935, "Mabatini");
        addLandMark("Mabatini Kwa KANU",36.8655824539915784,-1.26219459157149849, "Mabatini");
        addLandMark("Total Petrol Station",36.84528920656834572,-1.26670492680081814, "Mathare Village 1");
        addLandMark("Al Badir Petrol Station",36.84891561271554394,-1.26587956196086493,"Mathare Village 2");
    }
    private void addLandMark(String landMarkName, double lon, double lat, String villageName){
        Location landMarkLocation = new Location(landMarkName);
        landMarkLocation.setLatitude(lat);
        landMarkLocation.setLongitude(lon);
        landmarkMap.put(landMarkName, landMarkLocation);
        getLandMarkList(villageName).add(landMarkName);
    }
    private ArrayList<String> getLandMarkList(String villageName){
        int i = villages.indexOf(villageName);
        return landMarkLists.get(i);
    }
    @Override
    public void onResume(){
        super.onResume();
        attemptEnableSendSave();
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
    public void attemptVillageSelect(String textEnterred){
        String trimmedText = textEnterred.trim();
        if(villages.indexOf(trimmedText) != -1){
            villageSelected = trimmedText;
            revealSpinner();
        }
    }
    private void revealSpinner(){
        Spinner landmarks = (Spinner) getView().findViewById(R.id.landMarkSpinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, getLandMarkList(villageSelected));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        landmarks.setAdapter(dataAdapter);
        landmarks.setVisibility(View.VISIBLE);
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
        if (detailsText.isEmpty() || picPaths == null || picPaths.isEmpty() || picPaths.get(0) == null)
            ((NewReportActivity) getActivity()).sendSaveDisabled();
        else
            ((NewReportActivity) getActivity()).sendSaveEnabled();
    }
}
