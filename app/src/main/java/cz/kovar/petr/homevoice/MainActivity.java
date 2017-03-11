package cz.kovar.petr.homevoice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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

import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.bus.MainThreadBus;
import cz.kovar.petr.homevoice.bus.events.AuthEvent;
import cz.kovar.petr.homevoice.bus.events.IntentEvent;
import cz.kovar.petr.homevoice.bus.events.SettingsEvent;
import cz.kovar.petr.homevoice.bus.events.ShowEvent;
import cz.kovar.petr.homevoice.frontend.PagerAdapter;
import cz.kovar.petr.homevoice.frontend.adapters.OutputFieldAdapter;
import cz.kovar.petr.homevoice.modules.AboutModule;
import cz.kovar.petr.homevoice.modules.CloseModule;
import cz.kovar.petr.homevoice.modules.LightModule;
import cz.kovar.petr.homevoice.modules.Module;
import cz.kovar.petr.homevoice.modules.RoomModule;
import cz.kovar.petr.homevoice.modules.TimeModule;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.nlu.NLUInterface;
import cz.kovar.petr.homevoice.nlu.WitHandler;
import cz.kovar.petr.homevoice.sr.SpeechRecognition;
import cz.kovar.petr.homevoice.tts.SpeechSynthesizer;
import cz.kovar.petr.homevoice.utils.SentenceHelper;
import cz.kovar.petr.homevoice.zwave.ApiClient;
import cz.kovar.petr.homevoice.zwave.DataContext;
import cz.kovar.petr.homevoice.zwave.services.AuthService;
import cz.kovar.petr.homevoice.zwave.services.DataUpdateService;

public class MainActivity extends AppCompatActivity  {

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

    private Module m_activeModule = null;

    private SpeechSynthesizer m_synthesizer;
    private CloseModule m_closeModule;
    private AboutModule m_aboutModule;
    private TimeModule m_timeModule;
    private RoomModule m_roomModule;
    private LightModule m_lightModule;

    private ServiceConnection m_serviceConnection;

    @Inject
    ApiClient apiClient;
    @Inject
    DataContext dataContext;
    @Inject
    MainThreadBus bus;
    @Inject
    OutputFieldAdapter output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(AppConfig.DEBUG) Log.d(LOG_TAG, "Creation of main activity started.");

        m_synthesizer = new SpeechSynthesizer(this);

        m_closeModule = new CloseModule(this);
        m_aboutModule = new AboutModule(this);
        m_timeModule = new TimeModule(this);
        m_roomModule = new RoomModule(this);
        m_lightModule = new LightModule(this);

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

        output.init((TextView) findViewById(R.id.textResult), m_synthesizer);

        m_recognition = new SpeechRecognition(this);
        m_recognition.setOnSpeechRecognitionListener(new SpeechRecognition.OnSpeechRecognitionListener() {
            @Override
            public void onPartial(String aText) {
                output.printOutput(aText);
            }

            @Override
            public void onRecognized(String aText) {
                output.printOutput(aText);
                WitHandler nlu = new WitHandler();
                nlu.getIntent(new NLUInterface.OnNLUListener() {
                    @Override
                    public void onMsgObjReceived(UserIntent aMsg) {
                        if(aMsg != null && m_activeModule != null) {
                            m_activeModule.handleIntent(aMsg);
                        } else if(aMsg != null && aMsg.getIntent() != null && aMsg.getIntent().getValue() != null) {
                            m_closeModule.handleIntent(aMsg);
                            m_aboutModule.handleIntent(aMsg);
                            m_timeModule.handleIntent(aMsg);
                            m_roomModule.handleIntent(aMsg);
                            m_lightModule.handleIntent(aMsg);
                        } else {
                            output.addOutput(SentenceHelper.randomResponse(MainActivity.this, R.array.unknown), new OutputFieldAdapter.OnProgressListener() {
                                @Override
                                public void onDone() {
                                    startKeywordSpotting();
                                }
                            });
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

        Log.d(LOG_TAG, "onStart");

        final Intent intent = new Intent(this, DataUpdateService.class);
        m_serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {}

            @Override
            public void onServiceDisconnected(ComponentName componentName) {}
        };
        bindService(intent,
                m_serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(LOG_TAG, "onDestroy");
        bus.unregister(this);

        unbindService(m_serviceConnection);
        m_synthesizer.shutdown();

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
        output.printOutput("Trying to connect...");
        AuthService.login(this, event.profile);
        m_pagedAdapter.clear();
    }

    @Subscribe
    public void onAuthSuccess(AuthEvent.Success event) {
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "Successfully connected to smart home.");
        m_pagedAdapter.addLocationIDs(dataContext.getLocationsNames());
    }

    @Subscribe
    public void onAuthFail(AuthEvent.Fail event) {
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "Connection to smart home failed.");
        m_pagedAdapter.clear();
    }

    @Subscribe
    public void onShowLocation(ShowEvent.Location event) {
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "Show location: " + event.id);
        m_pager.setCurrentItem(dataContext.getLocationsNames().indexOf(event.id) + 1);
    }

    @Subscribe
    public void onIntentHandled(IntentEvent.Handled event) {
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "Intent handled by module.");
        m_activeModule = null;
        output.addOutput(event.response, new OutputFieldAdapter.OnProgressListener() {
            @Override
            public void onDone() {
                startKeywordSpotting();
            }
        });
    }

    @Subscribe
    public void onContextIncomplete(final IntentEvent.ContextIncomplete event) {
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "Unable to handle intent, context incomplete.");
        m_activeModule = event.module;
        output.addOutput(event.query, new OutputFieldAdapter.OnProgressListener() {
            @Override
            public void onDone() {
                startRecognition();
            }
        });
    }

    private void startKeywordSpotting() {
        // run on main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(m_micButton != null) m_micButton.setVisibility(View.VISIBLE);
                m_keywordSpotting = new Intent(MainActivity.this, KeywordSpotting.class);
                m_keywordSpotting.putExtra(KeywordSpotting.COMMAND, KeywordSpotting.CMD_START);
                startService(m_keywordSpotting);
            }
        });
    }

    private void startRecognition() {
        // run on main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(m_micButton != null) m_micButton.setVisibility(View.INVISIBLE);
                if(m_recognition != null) m_recognition.startRecognition();
            }
        });
    }

    private void wakeupScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

}
