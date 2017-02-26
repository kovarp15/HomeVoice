package cz.kovar.petr.homevoice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.bus.MainThreadBus;
import cz.kovar.petr.homevoice.bus.events.AuthEvent;
import cz.kovar.petr.homevoice.bus.events.SettingsEvent;
import cz.kovar.petr.homevoice.bus.events.ShowEvent;
import cz.kovar.petr.homevoice.frontend.PagerAdapter;
import cz.kovar.petr.homevoice.modules.AboutModule;
import cz.kovar.petr.homevoice.modules.TimeModule;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.nlu.NLUInterface;
import cz.kovar.petr.homevoice.nlu.WitHandler;
import cz.kovar.petr.homevoice.sr.SpeechRecognition;
import cz.kovar.petr.homevoice.tts.SpeechSynthesizer;
import cz.kovar.petr.homevoice.zwave.ApiClient;
import cz.kovar.petr.homevoice.zwave.DataContext;
import cz.kovar.petr.homevoice.zwave.services.AuthService;
import cz.kovar.petr.homevoice.zwave.services.DataUpdateService;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";
    private static final int NUM_OF_VIEWS = 10;

    public static final String TAG_COMMAND = "cmd";
    public static final int CMD_NONE = -1;
    public static final int CMD_SPEECH_RECOGNITION = 0;

    private PagerAdapter m_pagedAdapter;
    private ViewPager m_pager;

    private Intent            m_keywordSpotting;
    private SpeechRecognition m_recognition;

    private ImageButton m_micButton = null;
    private TextView m_result = null;

    private SpeechSynthesizer m_synthetizer;
    private AboutModule m_aboutModule;
    private TimeModule m_timeModule;

    @Inject
    ApiClient apiClient;
    @Inject
    DataContext dataContext;
    @Inject
    MainThreadBus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_synthetizer = new SpeechSynthesizer(this);
        m_aboutModule = new AboutModule(this, m_synthetizer);
        m_timeModule = new TimeModule(this, m_synthetizer);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        wakeupScreen();
        setContentView(R.layout.activity_main);

        ((ZWayApplication) getApplication()).getComponent().inject(this);
        bus.register(this);

        m_pager = (ViewPager) findViewById(R.id.pager);
        m_pager.setOffscreenPageLimit(NUM_OF_VIEWS);
        m_pagedAdapter = new PagerAdapter(getSupportFragmentManager());
        m_pager.setAdapter(m_pagedAdapter);

        ImageButton homeButton = (ImageButton) findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_pager.setCurrentItem(0);
            }
        });

        ImageButton settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_pager.setCurrentItem(m_pagedAdapter.getCount() - 1);
            }
        });

        m_micButton = (ImageButton) findViewById(R.id.listen_button);
        m_micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(m_keywordSpotting != null) {
                    m_keywordSpotting.putExtra(KeywordSpotting.COMMAND, KeywordSpotting.CMD_STOP);
                    startService(m_keywordSpotting);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startRecognition();
            }
        });

        m_result = (TextView) findViewById(R.id.textResult);

        m_recognition = new SpeechRecognition(this);
        m_recognition.setOnSpeechRecognitionListener(new SpeechRecognition.OnSpeechRecognitionListener() {
            @Override
            public void onPartial(String aText) {
                m_result.setText(aText);
            }

            @Override
            public void onRecognized(String aText) {
                m_result.setText(aText);
                startKeywordSpotting();
                WitHandler nlu = new WitHandler();
                nlu.getIntent(new NLUInterface.OnNLUListener() {
                    @Override
                    public void onMsgObjReceived(UserIntent aMsg) {
                        if(aMsg != null && aMsg.getIntent() != null && aMsg.getIntent().getValue() != null) {
                            m_result.setText(aMsg.getIntent().getValue().toString());
                            if(aMsg.getIntent().getValue().equals("EXIT_APP")) closeActivity(1000);
                            m_aboutModule.handleIntent(aMsg);
                            m_timeModule.handleIntent(aMsg);
                        } else {
                            m_result.setText("UNKNOWN");
                        }
                    }
                }, aText);
            }

            @Override
            public void onError() {
                startKeywordSpotting();
            }
        });

        handleIntent(getIntent());

        AuthService.login(this, UserData.loadZWayProfile(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.e(LOG_TAG, "onStart");

        final Intent intent = new Intent(this, DataUpdateService.class);
        bindService(intent,
                new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {}

                    @Override
                    public void onServiceDisconnected(ComponentName componentName) {}
                },
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (m_recognition != null) {
            m_recognition.destroy();
        }
    }

    public void onNewIntent(Intent aIntent) {
        Log.d(LOG_TAG, "onNewIntent");
        handleIntent(aIntent);
    }

    private void handleIntent(Intent aIntent) {
        Log.d(LOG_TAG, "handleIntent");
        if(aIntent != null) {
            int cmd = aIntent.getIntExtra(TAG_COMMAND, CMD_NONE);
            switch (cmd) {
                case CMD_SPEECH_RECOGNITION:
                    startRecognition();
                    break;
                default:
                    startKeywordSpotting();
            }
        }
    }

    @Subscribe
    public void onSettingsChanged(SettingsEvent.ZWayChanged event) {
        UserData.saveZWayProfile(this, event.profile);
        m_result.setText("Trying to connect...");
        AuthService.login(this, event.profile);
        m_pagedAdapter.clear();
    }

    @Subscribe
    public void onAuthSuccess(AuthEvent.Success event) {
        Log.v(LOG_TAG, "Auth Success!");
        m_result.setText("I am successfully connected to your smart home.");
        m_pagedAdapter.addLocationIDs(dataContext.getLocationsNames());
    }

    @Subscribe
    public void onAuthFail(AuthEvent.Fail event) {
        Log.v(LOG_TAG, "Auth Failed!");
        m_result.setText("I am not able to manage the connection.");
        m_pagedAdapter.clear();
    }

    @Subscribe
    public void onShowLocation(ShowEvent.Location event) {
        Log.v(LOG_TAG, "Show location: " + event.id);
        m_pager.setCurrentItem(dataContext.getLocationsNames().indexOf(event.id) + 1);
    }

    private void startKeywordSpotting() {
        if(m_micButton != null) m_micButton.setVisibility(View.VISIBLE);
        m_keywordSpotting = new Intent(MainActivity.this, KeywordSpotting.class);
        m_keywordSpotting.putExtra(KeywordSpotting.COMMAND, KeywordSpotting.CMD_START);
        startService(m_keywordSpotting);
    }

    private void startRecognition() {
        if(m_micButton != null) m_micButton.setVisibility(View.INVISIBLE);
        if(m_recognition != null) m_recognition.startRecognition();
    }

    private void closeActivity(int delay) {
        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.finish();
            }
        }, delay);
    }

    private void wakeupScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

}
