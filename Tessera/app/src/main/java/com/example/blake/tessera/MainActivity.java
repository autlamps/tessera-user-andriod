package com.example.blake.tessera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ToolbarWidgetWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Timer;
import java.util.TimerTask;

import static com.example.blake.tessera.Login.BASE_URL;

public class MainActivity extends AppCompatActivity {

    //initialising variables
    public static final String TOKEN_KEY = "token_key";
    private static final String defaultToken = null;
    private String Token = null;
    private String qr;
    private Integer balance;
    private Timer autoUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    //Generates the QRcode and display it to the page
    public void generateQR(String qr, Integer balancenum)
    {
        Integer balancenumber = balancenum;
        ImageView image;
        TextView balance;
        String qrCode = this.qr;
        image = (ImageView) findViewById(R.id.image);
        balance = (TextView) findViewById(R.id.Balance);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try{
            BitMatrix bitMatrix = multiFormatWriter.encode(qrCode, BarcodeFormat.QR_CODE,200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            image.setImageBitmap(bitmap);
        }
        catch (WriterException e)
        {
            e.printStackTrace();
        }

        balance.setText("$"+ balancenumber);
    }

    //Timer to refresh the page every 40 secconds
    @Override
    public void onResume() {
        super.onResume();
        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        updateHTML();
                    }
                });
            }
        }, 0, 20000); // updates each 40 secs
    }

    //what will be updated and the main body of the page.
    private void updateHTML(){
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);


        android.support.v7.widget.Toolbar hamburger = (android.support.v7.widget.Toolbar) findViewById(R.id.hamburger);
        hamburger.setBackground(getResources().getDrawable(R.color.colorAccent));

        setSupportActionBar(hamburger);


        //items for the hamburger menu
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Home");
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName("Topup");
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName("Settings");
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(4).withName("Logout");

        //adding the hamburger menu
        final Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(hamburger)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        new DividerDrawerItem(),
                        item3,
                        new DividerDrawerItem(),
                        item4
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                                   @Override
                                                   public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                if (drawerItem.getIdentifier() == 1) {

                }
                else if (drawerItem.getIdentifier() == 2) {
                    Intent intent = new Intent(MainActivity.this, Topup.class);
                    startActivity(intent);
                    finish();
                }
                else if (drawerItem.getIdentifier() == 3) {
                    Intent intent = new Intent(MainActivity.this, settings.class);
                    startActivity(intent);
                    finish();
                }
                else if (drawerItem.getIdentifier() == 4) {
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(TOKEN_KEY, null);
                    editor.commit();

                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                    finish();
                }

                return true;
                }

            }
                ).build();


        //all of the required things for backend scripts to be run
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssz")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Api apiService = retrofit.create(Api.class);

        Call<QRToken> call = apiService.GetQR("Token " + sharedPref.getString(TOKEN_KEY, defaultToken));
        call.enqueue(new Callback<QRToken>() {
            @Override
            public void onResponse(Call<QRToken> call, Response<QRToken> response) {

                if (response.isSuccessful()) {

                    // Toast.makeText(MainActivity.this, response.body().getQrCode(), Toast.LENGTH_SHORT).show();
                    qr = response.body().getQrCode().toString();
                    balance = response.body().getCurrentValue();
                    generateQR(qr, balance);

                } else {
                    Toast.makeText(MainActivity.this, "Invalid Credentials, Please try again.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<QRToken> call, Throwable t) {

                Toast.makeText(MainActivity.this, "Invalid Credentials, Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPause() {
        autoUpdate.cancel();
        super.onPause();
    }


}

