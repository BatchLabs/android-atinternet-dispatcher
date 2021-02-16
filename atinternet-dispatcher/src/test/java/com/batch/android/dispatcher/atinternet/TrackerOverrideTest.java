package com.batch.android.dispatcher.atinternet;

import android.os.Build;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.atinternet.tracker.ATInternet;
import com.atinternet.tracker.Publisher;
import com.atinternet.tracker.Publishers;
import com.atinternet.tracker.Screen;
import com.atinternet.tracker.Screens;
import com.atinternet.tracker.Tracker;
import com.batch.android.Batch;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = Build.VERSION_CODES.O_MR1)
@PowerMockIgnore({"org.powermock.*", "org.mockito.*", "org.robolectric.*", "android.*", "androidx.*"})
@PrepareForTest(ATInternet.class)
public class TrackerOverrideTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();
    private Screens screens;
    private Screen screen;
    private Publishers publishers;
    private Publisher publisher;
    private Tracker tracker;

    @Before
    public void setUp() {
        publishers = PowerMockito.mock(Publishers.class);
        screens = PowerMockito.mock(Screens.class);

        tracker = PowerMockito.mock(Tracker.class);
        Mockito.when(tracker.Publishers()).thenReturn(publishers);
        Mockito.when(tracker.Screens()).thenReturn(screens);

        publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add("[batch-default-campaign]")).thenReturn(publisher);

        screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("ShowedBatchInAppMessage")).thenReturn(screen);
    }

    @Test
    public void testTrackerOverride() {
        // Setup a mock that crashes when any ATInternet tracker getter is called
        PowerMockito.mockStatic(ATInternet.class);
        ATInternet atInternet = PowerMockito.mock(ATInternet.class);
        Mockito.when(ATInternet.getInstance()).thenReturn(atInternet);
        Mockito.when(atInternet.getDefaultTracker()).thenThrow(new RuntimeException("ATInternet's getDefaultTracker should not be called"));
        Mockito.when(atInternet.getTracker(Mockito.any())).thenThrow(new RuntimeException("ATInternet's getTracker should not be called"));

        TestEventPayload payload = new TestEventPayload(null,
                null,
                new Bundle());

        AtInternetDispatcher atInternetDispatcher = new AtInternetDispatcher();
        atInternetDispatcher.setTrackerOverride(tracker);
        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_SHOW, payload);

        verifyTrackedEvents();
    }

    @Test
    public void testDefaultTracker() {
        // Setup a mock that crashes when any ATInternet tracker getter is called
        PowerMockito.mockStatic(ATInternet.class);
        ATInternet atInternet = PowerMockito.mock(ATInternet.class);
        Mockito.when(ATInternet.getInstance()).thenReturn(atInternet);
        Mockito.when(atInternet.getDefaultTracker()).thenThrow(new RuntimeException("ATInternet's getDefaultTracker should not be called"));
        Mockito.when(atInternet.getTracker(AtInternetDispatcher.BATCH_PUBLISHER_TRACKER)).thenReturn(tracker);
        Mockito.when(atInternet.getTracker(AtInternetDispatcher.BATCH_CAMPAIGN_TRACKER)).thenReturn(tracker);

        TestEventPayload payload = new TestEventPayload(null,
                null,
                new Bundle());

        AtInternetDispatcher atInternetDispatcher = new AtInternetDispatcher();
        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_SHOW, payload);

        Mockito.verify(atInternet).getTracker(AtInternetDispatcher.BATCH_PUBLISHER_TRACKER);
        Mockito.verify(atInternet).getTracker(AtInternetDispatcher.BATCH_CAMPAIGN_TRACKER);
        verifyTrackedEvents();
    }

    private void verifyTrackedEvents() {
        Mockito.verify(publishers).add(Mockito.eq("[batch-default-campaign]"));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[in-app]"));
        Mockito.verify(publisher).sendImpression();
        Mockito.verify(publisher, Mockito.never()).sendTouch();

        Mockito.verify(screens).add(Mockito.eq("ShowedBatchInAppMessage"));
        Mockito.verify(screen, Mockito.never()).Campaign(Mockito.anyString());
        Mockito.verify(screen).sendView();
    }
}
