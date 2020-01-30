package com.MrSoftIt.class9_10allbook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ChaptersActivity extends AppCompatActivity {

    String pdf;
    String id;
    String BookName;

    private AdView mAdView;

    private ChapterAdapter Chapteradapter;

    FirebaseFirestore db = FirebaseFirestore.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapters);

        Bundle bundle = getIntent().getExtras();

        // displayFromUri();
        pdf = bundle.getString("pdf");
        id = bundle.getString("id");
        BookName = bundle.getString("BookName");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_support);
        toolbar.setTitle("নবম ও দশম শ্রেণির পাঠ্যপুস্তক");


        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        // TODO: Add adView to your view hierarchy.

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        if (MainActivity.InternetConnection.checkConnection(ChaptersActivity.this)) {

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

        recyclear ();

    }
    private void recyclear () {


        CollectionReference collectionReference = db.collection("users").document(id).collection("Chapters");


        Query query = collectionReference.orderBy("pageNumber");


        FirestoreRecyclerOptions<ChapterNote> options1 = new FirestoreRecyclerOptions.Builder<ChapterNote>()
                .setQuery(query, ChapterNote.class)
                .build();
        Chapteradapter = new ChapterAdapter(options1);

        RecyclerView recyclerView = findViewById(R.id.ChapterRecyclearView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(Chapteradapter);




        Chapteradapter.setOnItemClickListener(new ChapterAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                ChapterNote noteClass = documentSnapshot.toObject(ChapterNote.class);
                String id = documentSnapshot.getId();
                int pageName= noteClass.getPageNumber();
                String path = documentSnapshot.getReference().getPath();

                Intent pdfIntent = new Intent(ChaptersActivity.this,PdfActivity.class);

                pdfIntent.putExtra("PDF",pdf);
                pdfIntent.putExtra("PageNumber",pageName);
                pdfIntent.putExtra("BookName",BookName);

                startActivity(pdfIntent);
            }
        });

    }


    @Override
    protected void onStart () {
        super.onStart();
        Chapteradapter.startListening();
    }


    @Override
    protected void onStop () {
        super.onStop();
        Chapteradapter.stopListening();
    }

}