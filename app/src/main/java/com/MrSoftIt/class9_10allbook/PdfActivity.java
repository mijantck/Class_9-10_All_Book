package com.MrSoftIt.class9_10allbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class PdfActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {


    private PDFView pdfView;

    private View view;

    ProgressBar progressBar;
    private InterstitialAd mInterstitialAd;

    private AdView mAdView;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();


    String PDF;
    String BookName;
    int PageNumber;

    TextView offline;
    String path = Environment.getExternalStorageDirectory() + "/Class9-10";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_support);


        pdfView = findViewById(R.id.pdfView);

        offline = findViewById(R.id.ofline_id);
        progressBar = findViewById(R.id.progress_circular);

        Bundle bundle = getIntent().getExtras();

        // displayFromUri();

        PDF = bundle.getString("PDF");

        PageNumber = bundle.getInt("PageNumber");
       // PageNumber1 = Integer.parseInt(PageNumber1);

        BookName = bundle.getString("BookName");

        toolbar.setTitle("নবম ও দশম শ্রেণির "+BookName);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        // TODO: Add adView to your view hierarchy.

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }
        });



        getPath();

        displayFromUri();

        if (MainActivity.InternetConnection.checkConnection(PdfActivity.this)) {

            AdView adView = new AdView(this);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(getString(R.string.Banner_id));

            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });
        } else {
            // Not Available...
            Toast.makeText(this, " No Internet ", Toast.LENGTH_LONG).show();

        }

        offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openStoreg();
            }
        });



    }



    private String getPath() {

        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
            Toast.makeText(this, "Folder Create" +
                    file.getPath(), Toast.LENGTH_SHORT).show();
        }
        return path;
    }

    public void dwnld(Context context, String pdfUrl1, String name) {


        File direct = new File("/Class9-10/pdf");

        if (!direct.exists()) {
            direct.mkdirs();
        }


        // Create request for android download manager
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(pdfUrl1);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);

        // set title and description

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir("/Class9-10/pdf/", name+".pdf");
        
        request.setMimeType("*/*");
        downloadManager.enqueue(request);
    }


    private void displayFromUri() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + "/Class9-10/pdf/"+BookName+".pdf");

        if (file.exists() && pdfView != null) {
            offline.setVisibility(View.GONE);

            pdfView.fromFile(file)
                    .defaultPage(PageNumber)
                    .enableSwipe(true)
                    .load();
            progressBar.setVisibility(View.GONE);
        }
        else {
            offline.setVisibility(View.VISIBLE);

            Toast.makeText(this, "Online View ", Toast.LENGTH_SHORT).show();

            if(PDF==null){
                Toast.makeText(this, " not Pdf Link", Toast.LENGTH_SHORT).show();
            }
            storage.getReferenceFromUrl(PDF)
                    .getBytes(900000000)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {

                            pdfView.fromBytes(bytes)

                                    .defaultPage(PageNumber)
                                    .enableSwipe(true) // allows to block changing pages using swipe
                                    .swipeHorizontal(false)
                                    .enableDoubletap(true)
                                    .load();
                                   progressBar.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PdfActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
    @AfterPermissionGranted(123)
    private void openStoreg() {
        String[] perms = {android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            if (MainActivity.InternetConnection.checkConnection(PdfActivity.this)) {
                // Its Available...

                  dwnld(PdfActivity.this,PDF,BookName);

            } else {
                // Not Available...
                Toast.makeText(this, " No Internet ", Toast.LENGTH_LONG).show();

            }

             
        } else {
            EasyPermissions.requestPermissions(this, "We need storage permissions ",
                    123, perms);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

          dwnld(PdfActivity.this,PDF,BookName);

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {

        }
    }


    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

}
