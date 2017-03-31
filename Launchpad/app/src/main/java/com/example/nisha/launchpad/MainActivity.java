package com.example.nisha.launchpad;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Constants
    public static final int NUMBER_OF_BUTTONS = 16;
    private final String USER_AGENT = "Mozilla/5.0";

    // Grid button objects
    Button button00, button01, button02, button03;
    Button button10, button11, button12, button13;
    Button button20, button21, button22, button23;
    Button button30, button31, button32, button33;
    Button launchButton;

    Map<Integer, Integer> buttonPressedColorMap;
    Map<Integer, Integer> buttonReleasedColorMap;

    // toggle button object
    ToggleButton toggleButton;
    ToggleButton helpButton;

    // layout object
    RelativeLayout layout;

    // media player object
    MediaPlayer player;

    // 16 media players one for each button
    MediaPlayer player00;
    MediaPlayer player01;
    MediaPlayer player02;
    MediaPlayer player03;
    MediaPlayer player10;
    MediaPlayer player11;
    MediaPlayer player12;
    MediaPlayer player13;
    MediaPlayer player20;
    MediaPlayer player21;
    MediaPlayer player22;
    MediaPlayer player23;
    MediaPlayer player30;
    MediaPlayer player31;
    MediaPlayer player32;
    MediaPlayer player33;

    // 16 thread objects one for each button
    PlaySound playerThread00;
    PlaySound playerThread01;
    PlaySound playerThread02;
    PlaySound playerThread03;
    PlaySound playerThread10;
    PlaySound playerThread11;
    PlaySound playerThread12;
    PlaySound playerThread13;
    PlaySound playerThread20;
    PlaySound playerThread21;
    PlaySound playerThread22;
    PlaySound playerThread23;
    PlaySound playerThread30;
    PlaySound playerThread31;
    PlaySound playerThread32;
    PlaySound playerThread33;

    // play sound flag
    boolean playSoundFlag = false;

    // map of buttons -> button sounds
    Map<String, String> buttonSoundsMap;

    // senses clicked button (used for uploading sounds)
    String clickedButton;

    // used to open and close side menu
    DrawerLayout drawer;

    // toast used to tell a user no sound is assigned to a button
    Toast toast;

    HelpFragment helpFragment;
    android.app.FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // initialize toast
        Context context = getApplicationContext();
        CharSequence text = "No sound assigned.";
        int duration = Toast.LENGTH_SHORT;
        toast = Toast.makeText(context, text, duration);

        helpFragment = new HelpFragment();
        fragmentManager = getFragmentManager();

        /*********************** INITIALIZE OBJECTS WITH THEIR IDS **************************/

        // connect buttons to their corresponding ids
        button00 = (Button) findViewById(R.id.button00);
        button01 = (Button) findViewById(R.id.button01);
        button02 = (Button) findViewById(R.id.button02);
        button03 = (Button) findViewById(R.id.button03);
        button10 = (Button) findViewById(R.id.button10);
        button11 = (Button) findViewById(R.id.button11);
        button12 = (Button) findViewById(R.id.button12);
        button13 = (Button) findViewById(R.id.button13);
        button20 = (Button) findViewById(R.id.button20);
        button21 = (Button) findViewById(R.id.button21);
        button22 = (Button) findViewById(R.id.button22);
        button23 = (Button) findViewById(R.id.button23);
        button30 = (Button) findViewById(R.id.button30);
        button31 = (Button) findViewById(R.id.button31);
        button32 = (Button) findViewById(R.id.button32);
        button33 = (Button) findViewById(R.id.button33);
        launchButton = (Button) findViewById(R.id.launchButton);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        helpButton = (ToggleButton) findViewById(R.id.helpButton);

        // layout object connects to the main layout where buttons are
        layout = (RelativeLayout) findViewById(R.id.content_main);

        // create a media player object and set stream type
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // create a new tree map and initialize it
        buttonSoundsMap = new TreeMap<>();
        initButtonSounds();

        buttonPressedColorMap = new HashMap<>();
        //buttonReleasedColorMap = new HashMap<>();
        initButtonPressedColorMap();
        //initButtonReleasedColorMap();

        /******************************** END OF INITIALIZATION **************************/


        /******************** BUTTON LISTENERS FOR LAUNCH PAD BUTTONS *******************/

            /*
                EACH BUTTON OBJECT HAS THE SAME TYPE OF FUNCTION:

                    - IF not play sound mode, then open the navigation drawer to allow the user
                      to upload new sounds


                    - ELSE if play sound mode, check for button press and release

                        - on BUTTON PRESS: 1) reset the media player corresponding to the button
                          to play the sound from the start, 2) set sound looping to true as long
                          as button is pressed, 3) create a new thread by calling the PlaySound
                          class and pass in the media player as a parameter, 4) execute the thread

                        - on BUTTON RELEASE: 1) stop looping, 2) cancel the thread so it doesn't
                          take up memory
             */

        button00.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button00";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player00 = new MediaPlayer();
                                    player00.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player00.setLooping(true);
                                    button00.getBackground().setColorFilter(buttonPressedColorMap.get(0), PorterDuff.Mode.MULTIPLY);
                                    if (buttonSoundsMap.get("button00").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread00 = new PlaySound(player00);
                                    playerThread00.execute(buttonSoundsMap.get("button00"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    //button00.getBackground().setColorFilter(buttonReleasedColorMap.get(0), PorterDuff.Mode.MULTIPLY);
                                    button00.getBackground().clearColorFilter();
                                    player00.setLooping(false);
                                    playerThread00.cancel(true);
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button01.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button01";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player01 = new MediaPlayer();
                                    player01.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player01.setLooping(true);
                                    button01.getBackground().setColorFilter(buttonPressedColorMap.get(1), PorterDuff.Mode.MULTIPLY);
                                    if (buttonSoundsMap.get("button01").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread01 = new PlaySound(player01);
                                    playerThread01.execute(buttonSoundsMap.get("button01"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player01.setLooping(false);
                                    playerThread01.cancel(true);
                                    //button01.getBackground().setColorFilter(buttonReleasedColorMap.get(1), PorterDuff.Mode.MULTIPLY);
                                    button01.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button02.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button02";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player02 = new MediaPlayer();
                                    player02.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player02.setLooping(true);
                                    button02.getBackground().setColorFilter(buttonPressedColorMap.get(2), PorterDuff.Mode.MULTIPLY);
                                    if(buttonSoundsMap.get("button02").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread02 = new PlaySound(player02);
                                    playerThread02.execute(buttonSoundsMap.get("button02"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player02.setLooping(false);
                                    playerThread02.cancel(true);
                                    //button02.getBackground().setColorFilter(buttonReleasedColorMap.get(2), PorterDuff.Mode.MULTIPLY);
                                    button02.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button03.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button03";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player03 = new MediaPlayer();
                                    player03.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player03.setLooping(true);
                                    button03.getBackground().setColorFilter(buttonPressedColorMap.get(3), PorterDuff.Mode.MULTIPLY);
                                    if(buttonSoundsMap.get("button03").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread03 = new PlaySound(player03);
                                    playerThread03.execute(buttonSoundsMap.get("button03"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player03.setLooping(false);
                                    playerThread03.cancel(true);
                                    //button03.getBackground().setColorFilter(buttonReleasedColorMap.get(3), PorterDuff.Mode.MULTIPLY);
                                    button03.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button10.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button10";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player10 = new MediaPlayer();
                                    player10.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player10.setLooping(true);
                                    button10.getBackground().setColorFilter(buttonPressedColorMap.get(4), PorterDuff.Mode.MULTIPLY);
                                    if(buttonSoundsMap.get("button10").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread10 = new PlaySound(player10);
                                    playerThread10.execute(buttonSoundsMap.get("button10"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player10.setLooping(false);
                                    playerThread10.cancel(true);
                                    //button10.getBackground().setColorFilter(buttonReleasedColorMap.get(4), PorterDuff.Mode.MULTIPLY);
                                    button10.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button11.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button11";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player11 = new MediaPlayer();
                                    player11.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player11.setLooping(true);
                                    button11.getBackground().setColorFilter(buttonPressedColorMap.get(5), PorterDuff.Mode.MULTIPLY);
                                    if(buttonSoundsMap.get("button11").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread11 = new PlaySound(player11);
                                    playerThread11.execute(buttonSoundsMap.get("button11"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player11.setLooping(false);
                                    playerThread11.cancel(true);
                                    //button11.getBackground().setColorFilter(buttonReleasedColorMap.get(5), PorterDuff.Mode.MULTIPLY);
                                    button11.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button12.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button12";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player12 = new MediaPlayer();
                                    player12.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player12.setLooping(true);
                                    button12.getBackground().setColorFilter(buttonPressedColorMap.get(6), PorterDuff.Mode.MULTIPLY);
                                    if(buttonSoundsMap.get("button12").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread12 = new PlaySound(player12);
                                    playerThread12.execute(buttonSoundsMap.get("button12"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player12.setLooping(false);
                                    playerThread12.cancel(true);
                                    //button12.getBackground().setColorFilter(buttonReleasedColorMap.get(6), PorterDuff.Mode.MULTIPLY);
                                    button12.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button13.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button13";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player13 = new MediaPlayer();
                                    player13.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player13.setLooping(true);
                                    button13.getBackground().setColorFilter(buttonPressedColorMap.get(7), PorterDuff.Mode.MULTIPLY);
                                    if(buttonSoundsMap.get("button13").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread13 = new PlaySound(player13);
                                    playerThread13.execute(buttonSoundsMap.get("button13"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player13.setLooping(false);
                                    playerThread13.cancel(true);
                                    //button13.getBackground().setColorFilter(buttonReleasedColorMap.get(7), PorterDuff.Mode.MULTIPLY);
                                    button13.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button20.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button20";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player20 = new MediaPlayer();
                                    player20.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player20.setLooping(true);
                                    button20.getBackground().setColorFilter(buttonPressedColorMap.get(8), PorterDuff.Mode.MULTIPLY);
                                    if(buttonSoundsMap.get("button20").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread20 = new PlaySound(player20);
                                    playerThread20.execute(buttonSoundsMap.get("button20"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player20.setLooping(false);
                                    playerThread20.cancel(true);
                                    //button20.getBackground().setColorFilter(buttonReleasedColorMap.get(8), PorterDuff.Mode.MULTIPLY);
                                    button20.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button21.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button21";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player21 = new MediaPlayer();
                                    player21.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player21.setLooping(true);
                                    button21.getBackground().setColorFilter(buttonPressedColorMap.get(9), PorterDuff.Mode.MULTIPLY);
                                    if(buttonSoundsMap.get("button21").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread21 = new PlaySound(player21);
                                    playerThread21.execute(buttonSoundsMap.get("button21"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player21.setLooping(false);
                                    playerThread21.cancel(true);
                                    //button21.getBackground().setColorFilter(buttonReleasedColorMap.get(9), PorterDuff.Mode.MULTIPLY);
                                    button21.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button22.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button22";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player22 = new MediaPlayer();
                                    player22.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player22.setLooping(true);
                                    button22.getBackground().setColorFilter(buttonPressedColorMap.get(10), PorterDuff.Mode.MULTIPLY);
                                    if(buttonSoundsMap.get("button22").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread22 = new PlaySound(player22);
                                    playerThread22.execute(buttonSoundsMap.get("button22"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player22.setLooping(false);
                                    playerThread22.cancel(true);
                                    //button22.getBackground().setColorFilter(buttonReleasedColorMap.get(10), PorterDuff.Mode.MULTIPLY);
                                    button22.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button23.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button23";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player23 = new MediaPlayer();
                                    player23.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player23.setLooping(true);
                                    button23.getBackground().setColorFilter(buttonPressedColorMap.get(11), PorterDuff.Mode.MULTIPLY);
                                    if(buttonSoundsMap.get("button23").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread23 = new PlaySound(player23);
                                    playerThread23.execute(buttonSoundsMap.get("button23"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player23.setLooping(false);
                                    playerThread23.cancel(true);
                                    //button23.getBackground().setColorFilter(buttonReleasedColorMap.get(11), PorterDuff.Mode.MULTIPLY);
                                    button23.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button30.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button30";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player30 = new MediaPlayer();
                                    player30.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player30.setLooping(true);
                                    button30.getBackground().setColorFilter(buttonPressedColorMap.get(12), PorterDuff.Mode.MULTIPLY);
                                    if(buttonSoundsMap.get("button30").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread30 = new PlaySound(player30);
                                    playerThread30.execute(buttonSoundsMap.get("button30"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player30.setLooping(false);
                                    playerThread30.cancel(true);
                                    //button30.getBackground().setColorFilter(buttonReleasedColorMap.get(12), PorterDuff.Mode.MULTIPLY);
                                    button30.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button31.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button31";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player31 = new MediaPlayer();
                                    player31.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player31.setLooping(true);
                                    button31.getBackground().setColorFilter(buttonPressedColorMap.get(13), PorterDuff.Mode.MULTIPLY);
                                    if(buttonSoundsMap.get("button31").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread31 = new PlaySound(player31);
                                    playerThread31.execute(buttonSoundsMap.get("button31"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player31.setLooping(false);
                                    playerThread31.cancel(true);
                                    //button31.getBackground().setColorFilter(buttonReleasedColorMap.get(13), PorterDuff.Mode.MULTIPLY);
                                    button31.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button32.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button32";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player32 = new MediaPlayer();
                                    player32.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player32.setLooping(true);
                                    button32.getBackground().setColorFilter(buttonPressedColorMap.get(14), PorterDuff.Mode.MULTIPLY);
                                    if(buttonSoundsMap.get("button32").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread32 = new PlaySound(player32);
                                    playerThread32.execute(buttonSoundsMap.get("button32"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player32.setLooping(false);
                                    playerThread32.cancel(true);
                                    //button32.getBackground().setColorFilter(buttonReleasedColorMap.get(14), PorterDuff.Mode.MULTIPLY);
                                    button32.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        button33.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!playSoundFlag) {
                            clickedButton = "button33";
                            drawer.openDrawer(GravityCompat.START);
                        } else {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    player33 = new MediaPlayer();
                                    player33.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    player33.setLooping(true);
                                    button33.getBackground().setColorFilter(buttonPressedColorMap.get(15), PorterDuff.Mode.MULTIPLY);
                                    if(buttonSoundsMap.get("button33").equals("0")) {
                                        toast.show();
                                    }
                                    playerThread33 = new PlaySound(player33);
                                    playerThread33.execute(buttonSoundsMap.get("button33"));
                                    return true;
                                case MotionEvent.ACTION_UP:
                                    player33.setLooping(false);
                                    playerThread33.cancel(true);
                                    //button33.getBackground().setColorFilter(buttonReleasedColorMap.get(15), PorterDuff.Mode.MULTIPLY);
                                    button33.getBackground().clearColorFilter();
                                    return true;
                            }

                        }
                        return false;
                    }
                }
        );

        /************************ END OF LAUNCH PAD BUTTON LISTENERS ********************/

        /************************ LISTENER FOR LAUNCH BUTTON ***************************/

        // execute the HttpPostRequest class
        launchButton.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                // call http post request class with the url
                                if(playSoundFlag) {
                                    launchButton.getBackground().setColorFilter(buttonPressedColorMap.get(16), PorterDuff.Mode.MULTIPLY);
                                }
                                new HttpPostRequest().execute("http://38.88.74.83/cpen291-project2/appPhp.php");
                                return true;
                            case MotionEvent.ACTION_UP:
                                if(playSoundFlag) {
                                    //launchButton.getBackground().setColorFilter(buttonReleasedColorMap.get(16), PorterDuff.Mode.MULTIPLY);
                                    launchButton.getBackground().clearColorFilter();
                                }
                        }
                        return false;
                    }
                }
        );

        /******************** END OF LISTENER FOR LAUNCH BUTTON ***********************/

        /************ TOGGLE BUTTON LISTENER THAT SETS PLAY SOUND MODE ****************/

        /*
            On click of the toggle button:
                if IS CHECKED, change background color of app, change color of the
                toggle button, and set playSoundFlag to true

                else, reset background color and toggle button color and set flag to false
         */

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layout.setBackgroundColor(0xFF1a1a1a);
                    toggleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_play_button));
                    playSoundFlag = true;

                    // change button colors
//                    button00.getBackground().setColorFilter(buttonReleasedColorMap.get(0), PorterDuff.Mode.MULTIPLY);
//                    button01.getBackground().setColorFilter(buttonReleasedColorMap.get(1), PorterDuff.Mode.MULTIPLY);
//                    button02.getBackground().setColorFilter(buttonReleasedColorMap.get(2), PorterDuff.Mode.MULTIPLY);
//                    button03.getBackground().setColorFilter(buttonReleasedColorMap.get(3), PorterDuff.Mode.MULTIPLY);
//                    button10.getBackground().setColorFilter(buttonReleasedColorMap.get(4), PorterDuff.Mode.MULTIPLY);
//                    button11.getBackground().setColorFilter(buttonReleasedColorMap.get(5), PorterDuff.Mode.MULTIPLY);
//                    button12.getBackground().setColorFilter(buttonReleasedColorMap.get(6), PorterDuff.Mode.MULTIPLY);
//                    button13.getBackground().setColorFilter(buttonReleasedColorMap.get(7), PorterDuff.Mode.MULTIPLY);
//                    button20.getBackground().setColorFilter(buttonReleasedColorMap.get(8), PorterDuff.Mode.MULTIPLY);
//                    button21.getBackground().setColorFilter(buttonReleasedColorMap.get(9), PorterDuff.Mode.MULTIPLY);
//                    button22.getBackground().setColorFilter(buttonReleasedColorMap.get(10), PorterDuff.Mode.MULTIPLY);
//                    button23.getBackground().setColorFilter(buttonReleasedColorMap.get(11), PorterDuff.Mode.MULTIPLY);
//                    button30.getBackground().setColorFilter(buttonReleasedColorMap.get(12), PorterDuff.Mode.MULTIPLY);
//                    button31.getBackground().setColorFilter(buttonReleasedColorMap.get(13), PorterDuff.Mode.MULTIPLY);
//                    button32.getBackground().setColorFilter(buttonReleasedColorMap.get(14), PorterDuff.Mode.MULTIPLY);
//                    button33.getBackground().setColorFilter(buttonReleasedColorMap.get(15), PorterDuff.Mode.MULTIPLY);
//                    launchButton.getBackground().setColorFilter(buttonReleasedColorMap.get(16), PorterDuff.Mode.MULTIPLY);

                } else {
                    layout.setBackgroundColor(Color.WHITE);
                    toggleButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.white_play_button));
                    playSoundFlag = false;

                    // clear button colors
                    button00.getBackground().clearColorFilter();
                    button01.getBackground().clearColorFilter();
                    button02.getBackground().clearColorFilter();
                    button03.getBackground().clearColorFilter();
                    button10.getBackground().clearColorFilter();
                    button11.getBackground().clearColorFilter();
                    button12.getBackground().clearColorFilter();
                    button13.getBackground().clearColorFilter();
                    button20.getBackground().clearColorFilter();
                    button21.getBackground().clearColorFilter();
                    button22.getBackground().clearColorFilter();
                    button23.getBackground().clearColorFilter();
                    button30.getBackground().clearColorFilter();
                    button31.getBackground().clearColorFilter();
                    button32.getBackground().clearColorFilter();
                    button33.getBackground().clearColorFilter();
                    launchButton.getBackground().clearColorFilter();
                }
            }
        });


        helpButton.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            helpButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_help_icon));
                            if(!playSoundFlag) {
                                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                ft.replace(R.id.content_main, helpFragment);
                                ft.show(helpFragment).commit();
                            }
                        } else {
                            helpButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.white_help_icon));
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.hide(helpFragment).commit();
                        }
                    }
                }
        );


        /******************** END OF TOGGLE BUTTON LISTENER *************************/

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /********************** NAVIGATION DRAWER ITEM FUNCTIONS **************************/

            /*
                TWO MODES:

                    MODE 1: Play sound mode -- reset sound play and play the selected sound
                    MODE 2: Upload sound mode -- assign sound to selected button by updating
                    the button sound map
             */

        // play sounds on menu item click of sound flag is true
        if (playSoundFlag || clickedButton == null) {               // MODE 1
            if (id == R.id.nav_sound1) {
                player.reset();
                playSound("1");
            } else if (id == R.id.nav_sound2) { // local mp3
                player.reset();
                playSound("2");
            } else if (id == R.id.nav_sound3) { // local mp3
                player.reset();
                playSound("3");
            } else if (id == R.id.nav_sound4) { // local mp3
                player.reset();
                playSound("4");
            } else if (id == R.id.nav_sound5) {
                player.reset();
                playSound("5");
            } else if (id == R.id.nav_sound6) {
                player.reset();
                playSound("6");
            } else if (id == R.id.nav_sound7) {
                player.reset();
                playSound("7");
            } else if (id == R.id.nav_sound8) {
                player.reset();
                playSound("8");
            } else if (id == R.id.nav_sound9) {
                player.reset();
                playSound("9");
            } else if (id == R.id.nav_sound10) {
                player.reset();
                playSound("10");
            } else if (id == R.id.nav_sound11) {
                player.reset();
                playSound("11");
            } else if (id == R.id.nav_sound12) {
                player.reset();
                playSound("12");
            } else if (id == R.id.nav_sound13) {
                player.reset();
                playSound("13");
            } else if (id == R.id.nav_sound14) {
                player.reset();
                playSound("14");
            } else if (id == R.id.nav_sound15) {
                player.reset();
                playSound("15");
            } else if (id == R.id.nav_sound16) {
                player.reset();
                playSound("16");
            } else if (id == R.id.nav_sound17) {
                player.reset();
                playSound("17");
            } else if (id == R.id.nav_sound18) {
                player.reset();
                playSound("18");
            } else if (id == R.id.nav_sound19) {
                player.reset();
                playSound("19");
            } else if (id == R.id.nav_sound20) {
                player.reset();
                playSound("20");
            } else if (id == R.id.nav_sound21) {
                player.reset();
                playSound("21");
            } else if (id == R.id.nav_sound22) {
                player.reset();
                playSound("22");
            } else if (id == R.id.nav_sound23) {
                player.reset();
                playSound("23");
            } else if (id == R.id.nav_sound24) {
                player.reset();
                playSound("24");
            }
            // else update button sounds if sound flag is false
        } else if (!playSoundFlag) {                                // MODE 2
            if (id == R.id.nav_sound1) {
                buttonSoundsMap.put(clickedButton, "1");
            } else if (id == R.id.nav_sound2) {
                buttonSoundsMap.put(clickedButton, "2");
            } else if (id == R.id.nav_sound3) {
                buttonSoundsMap.put(clickedButton, "3");
            } else if (id == R.id.nav_sound4) {
                buttonSoundsMap.put(clickedButton, "4");
            } else if (id == R.id.nav_sound5) {
                buttonSoundsMap.put(clickedButton, "5");
            } else if (id == R.id.nav_sound6) {
                buttonSoundsMap.put(clickedButton, "6");
            } else if (id == R.id.nav_sound7) {
                buttonSoundsMap.put(clickedButton, "7");
            } else if (id == R.id.nav_sound8) {
                buttonSoundsMap.put(clickedButton, "8");
            } else if (id == R.id.nav_sound9) {
                buttonSoundsMap.put(clickedButton, "9");
            } else if (id == R.id.nav_sound10) {
                buttonSoundsMap.put(clickedButton, "10");
            } else if (id == R.id.nav_sound11) {
                buttonSoundsMap.put(clickedButton, "11");
            } else if (id == R.id.nav_sound12) {
                buttonSoundsMap.put(clickedButton, "12");
            } else if (id == R.id.nav_sound13) {
                buttonSoundsMap.put(clickedButton, "13");
            } else if (id == R.id.nav_sound14) {
                buttonSoundsMap.put(clickedButton, "14");
            } else if (id == R.id.nav_sound15) {
                buttonSoundsMap.put(clickedButton, "15");
            } else if (id == R.id.nav_sound16) {
                buttonSoundsMap.put(clickedButton, "16");
            } else if (id == R.id.nav_sound17) {
                buttonSoundsMap.put(clickedButton, "17");
            } else if (id == R.id.nav_sound18) {
                buttonSoundsMap.put(clickedButton, "18");
            } else if (id == R.id.nav_sound19) {
                buttonSoundsMap.put(clickedButton, "19");
            } else if (id == R.id.nav_sound20) {
                buttonSoundsMap.put(clickedButton, "20");
            } else if (id == R.id.nav_sound21) {
                buttonSoundsMap.put(clickedButton, "21");
            } else if (id == R.id.nav_sound22) {
                buttonSoundsMap.put(clickedButton, "22");
            } else if (id == R.id.nav_sound23) {
                buttonSoundsMap.put(clickedButton, "23");
            } else if (id == R.id.nav_sound24) {
                buttonSoundsMap.put(clickedButton, "24");
            }
            // close drawer after menu item click
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    /**
     * Initialize buttonSoundsMap with all keys (buttons) to value 0 for no sound
     */
    private void initButtonSounds() {
        buttonSoundsMap.put("button00", "0");
        buttonSoundsMap.put("button01", "0");
        buttonSoundsMap.put("button02", "0");
        buttonSoundsMap.put("button03", "0");
        buttonSoundsMap.put("button10", "0");
        buttonSoundsMap.put("button11", "0");
        buttonSoundsMap.put("button12", "0");
        buttonSoundsMap.put("button13", "0");
        buttonSoundsMap.put("button20", "0");
        buttonSoundsMap.put("button21", "0");
        buttonSoundsMap.put("button22", "0");
        buttonSoundsMap.put("button23", "0");
        buttonSoundsMap.put("button30", "0");
        buttonSoundsMap.put("button31", "0");
        buttonSoundsMap.put("button32", "0");
        buttonSoundsMap.put("button33", "0");
    }

    private void initButtonPressedColorMap() {
        // initialize pressed colors for buttons 0-15 and 16 represents launch button
        buttonPressedColorMap.put(0, 0xFFff1a1a);
        buttonPressedColorMap.put(1, 0xFF00ff00);
        buttonPressedColorMap.put(2, 0xFF00ffff);
        buttonPressedColorMap.put(3, 0xFFffff00);
        buttonPressedColorMap.put(4, 0xFFff1a1a);
        buttonPressedColorMap.put(5, 0xFF00ff00);
        buttonPressedColorMap.put(6, 0xFF00ffff);
        buttonPressedColorMap.put(7, 0xFFffff00);
        buttonPressedColorMap.put(8, 0xFFff1a1a);
        buttonPressedColorMap.put(9, 0xFF00ff00);
        buttonPressedColorMap.put(10, 0xFF00ffff);
        buttonPressedColorMap.put(11, 0xFFffff00);
        buttonPressedColorMap.put(12, 0xFFff1a1a);
        buttonPressedColorMap.put(13, 0xFF00ff00);
        buttonPressedColorMap.put(14, 0xFF00ffff);
        buttonPressedColorMap.put(15, 0xFFffff00);
        buttonPressedColorMap.put(16, 0xFFff9933);
    }

//    private void initButtonReleasedColorMap() {
//        buttonReleasedColorMap.put(0, 0xFF00FFFF);
//        buttonReleasedColorMap.put(1, 0xFF00FFFF);
//        buttonReleasedColorMap.put(2, 0xFF00FFFF);
//        buttonReleasedColorMap.put(3, 0xFF00FFFF);
//        buttonReleasedColorMap.put(4, 0xFF00FFCC);
//        buttonReleasedColorMap.put(5, 0xFF00FFCC);
//        buttonReleasedColorMap.put(6, 0xFF00FFCC);
//        buttonReleasedColorMap.put(7, 0xFF00FFCC);
//        buttonReleasedColorMap.put(8, 0xFF00FF99);
//        buttonReleasedColorMap.put(9, 0xFF00FF99);
//        buttonReleasedColorMap.put(10, 0xFF00FF99);
//        buttonReleasedColorMap.put(11, 0xFF00FF99);
//        buttonReleasedColorMap.put(12, 0xFF00FF66);
//        buttonReleasedColorMap.put(13, 0xFF00FF66);
//        buttonReleasedColorMap.put(14, 0xFF00FF66);
//        buttonReleasedColorMap.put(15, 0xFF00FF66);
//        buttonReleasedColorMap.put(16, 0xFF00FF00);
//    }

    /**
     * Convert the map of button sounds to a string for Http Post in
     * the following format: key0=value0&key1=value1
     *
     * @return button sound map encoded in Http Post parameter string
     */
    public String arrayToUrlParams() {
        StringBuilder urlParams = new StringBuilder();

        for (Map.Entry<String, String> entry : buttonSoundsMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!key.equals("button00")) {
                urlParams.append("&");
            }
            urlParams.append(key);
            urlParams.append("=");
            urlParams.append(value);

        }
        return urlParams.toString();
    }

    /**
     * Retrieve the URL of the sound corresponding to the passed value.
     *
     * @param value - sound id
     * @return url of sound file
     */
    private String getSoundUrl(String value) {
        switch (value) {
            case "1":
                return "http://38.88.74.83/cpen291-project2/sounds/1.mp3";
            case "2":
                return "http://38.88.74.83/cpen291-project2/sounds/2.mp3";
            case "3":
                return "http://38.88.74.83/cpen291-project2/sounds/3.mp3";
            case "4":
                return "http://38.88.74.83/cpen291-project2/sounds/4.mp3";
            case "5":
                return "http://38.88.74.83/cpen291-project2/sounds/5.mp3";
            case "6":
                return "http://38.88.74.83/cpen291-project2/sounds/6.mp3";
            case "7":
                return "http://38.88.74.83/cpen291-project2/sounds/7.mp3";
            case "8":
                return "http://38.88.74.83/cpen291-project2/sounds/8.mp3";
            case "9":
                return "http://38.88.74.83/cpen291-project2/sounds/9.mp3";
            case "10":
                return "http://38.88.74.83/cpen291-project2/sounds/10.mp3";
            case "11":
                return "http://38.88.74.83/cpen291-project2/sounds/11.mp3";
            case "12":
                return "http://38.88.74.83/cpen291-project2/sounds/12.mp3";
            case "13":
                return "http://38.88.74.83/cpen291-project2/sounds/13.mp3";
            case "14":
                return "http://38.88.74.83/cpen291-project2/sounds/14.mp3";
            case "15":
                return "http://38.88.74.83/cpen291-project2/sounds/15.mp3";
            case "16":
                return "http://38.88.74.83/cpen291-project2/sounds/16.mp3";
            case "17":
                return "http://38.88.74.83/cpen291-project2/sounds/17.mp3";
            case "18":
                return "http://38.88.74.83/cpen291-project2/sounds/18.mp3";
            case "19":
                return "http://38.88.74.83/cpen291-project2/sounds/19.mp3";
            case "20":
                return "http://38.88.74.83/cpen291-project2/sounds/20.mp3";
            case "21":
                return "http://38.88.74.83/cpen291-project2/sounds/21.mp3";
            case "22":
                return "http://38.88.74.83/cpen291-project2/sounds/22.mp3";
            case "23":
                return "http://38.88.74.83/cpen291-project2/sounds/23.mp3";
            case "24":
                return "http://38.88.74.83/cpen291-project2/sounds/24.mp3";
            default:
                return "http://38.88.74.83/cpen291-project2/sounds/0.mp3";
        }
    }

    /**
     * Try playing the sound of the corresponding value.
     *
     * @param value - sound id
     */
    public void playSound(String value) {
        try {
            // set the data source for the player
            player.setDataSource(getSoundUrl(value));

            // prepare the player
            player.prepare();

            // start the player
            player.start();
        } catch (Exception e) {
        }
    }

    /**
     * Send a Http Post request to the web server using a background thread
     */
    private class HttpPostRequest extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            // url object
            URL url = null;

            // http url connection object that deals with the url connections
            HttpURLConnection client = null;

            try {
                // initialize url object to the url parameter
                url = new URL(params[0]);

                // try opening the connection
                client = (HttpURLConnection) url.openConnection();

                // set the request method to Post requests
                client.setRequestMethod("POST");

                // set request properties
                client.setRequestProperty("User-Agent", USER_AGENT);
                client.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                // set output to true
                client.setDoOutput(true);

                // get the data output stream
                DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());

                // write to the output stream
                outputStream.writeBytes(arrayToUrlParams());

                // flush and close output stream
                outputStream.flush();
                outputStream.close();

                // read the input stream to ensure the output stream was received correctly
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream())
                );
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // return the input stream string
                return response.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast launchToast = Toast.makeText(context, s, duration);

            launchToast.show();
        }
    }

    /**
     * Play a sound using MediaPlayer on a background thread.
     * This class allows multiple sounds to be played simultaneously.
     */
    private class PlaySound extends AsyncTask<String, String, String> {

        private MediaPlayer mediaPlayer;

        // constructor initializes the media player to be used
        public PlaySound(MediaPlayer player) {
            this.mediaPlayer = player;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // set data source for media player
                mediaPlayer.setDataSource(getSoundUrl(params[0]));

                // prepare the player
                mediaPlayer.prepare();

                // start the player
                mediaPlayer.start();

                mediaPlayer.setOnCompletionListener(
                        new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mp.release();
                                mp = null;
                            }
                        }
                );

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
