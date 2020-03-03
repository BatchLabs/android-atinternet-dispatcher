package com.batch.android.dispatcher.atinternet;

import android.os.Build;
import android.os.Bundle;

import com.atinternet.tracker.ATInternet;
import com.atinternet.tracker.Publisher;
import com.atinternet.tracker.Publishers;
import com.atinternet.tracker.Screen;
import com.atinternet.tracker.Screens;
import com.atinternet.tracker.Tracker;
import com.batch.android.Batch;
import com.batch.android.BatchMessage;
import com.batch.android.BatchPushPayload;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;

/**
 * Test the AT Internet Event Dispatcher implementation
 * The dispatcher should respect the AT campaign protocol from Google tools
 * See : https://marketplace.atinternet-solutions.com/ATInternetCampaignCreator/xtor/
 * And : https://developers.atinternet-solutions.com/javascript-en/campaigns-javascript-en/marketing-campaigns-v2/
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = Build.VERSION_CODES.O_MR1)
@PowerMockIgnore({"org.powermock.*", "org.mockito.*", "org.robolectric.*", "android.*", "androidx.*"})
@PrepareForTest(ATInternet.class)
public class AtInternetDispatcherTest
{
    @Rule
    public PowerMockRule rule = new PowerMockRule();
    private Screens screens;
    private Publishers publishers;
    private AtInternetDispatcher atInternetDispatcher;

    @Before
    public void setUp() {
        ATInternet atInternet = PowerMockito.mock(ATInternet.class);
        Tracker tracker = PowerMockito.mock(Tracker.class);
        publishers = PowerMockito.mock(Publishers.class);
        screens = PowerMockito.mock(Screens.class);

        PowerMockito.mockStatic(ATInternet.class);
        Mockito.when(ATInternet.getInstance()).thenReturn(atInternet);
        Mockito.when(atInternet.getDefaultTracker()).thenReturn(tracker);
        Mockito.when(atInternet.getTracker(AtInternetDispatcher.BATCH_PUBLISHER_TRACKER)).thenReturn(tracker);
        Mockito.when(atInternet.getTracker(AtInternetDispatcher.BATCH_CAMPAIGN_TRACKER)).thenReturn(tracker);
        Mockito.when(tracker.Publishers()).thenReturn(publishers);
        Mockito.when(tracker.Screens()).thenReturn(screens);

        atInternetDispatcher = new AtInternetDispatcher();
    }

    @Test
    public void testNotificationDisplay() {

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add("[batch-default-campaign]")).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("DisplayedBatchPushNotification")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                null,
                new Bundle());

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);

        Mockito.verify(publishers).add(Mockito.eq("[batch-default-campaign]"));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[push]"));
        Mockito.verify(publisher).sendImpression();
        Mockito.verify(publisher, Mockito.never()).sendTouch();

        Mockito.verify(screens).add(Mockito.eq("DisplayedBatchPushNotification"));
        Mockito.verify(screen, Mockito.never()).Campaign(Mockito.anyString());
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testNotificationDisplayCampaignLabelFragment() {

        String xtor = "CS1-[mylabeltest]-test-15[sef]";
        String campaignExpected = "[mylabeltest]";

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add(campaignExpected)).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("DisplayedBatchPushNotification")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com.com/test#xtor=" + xtor,
                new Bundle());

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);

        Mockito.verify(publishers).add(Mockito.eq(campaignExpected));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[push]"));
        Mockito.verify(publisher).sendImpression();
        Mockito.verify(publisher, Mockito.never()).sendTouch();

        Mockito.verify(screens).add(Mockito.eq("DisplayedBatchPushNotification"));
        Mockito.verify(screen).Campaign(xtor);
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testNotificationDisplayCampaignLabelFragmentEncode() {

        String xtor = "CS1-%5Bmylabeltest%5D-test-15%5Bsef%5D";
        String decodedXtor = "CS1-[mylabeltest]-test-15[sef]";
        String campaignExpected = "[mylabeltest]";

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add(campaignExpected)).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("DisplayedBatchPushNotification")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com/test#xtor=" + xtor,
                new Bundle());

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);

        Mockito.verify(publishers).add(Mockito.eq(campaignExpected));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[push]"));
        Mockito.verify(publisher).sendImpression();
        Mockito.verify(publisher, Mockito.never()).sendTouch();

        Mockito.verify(screens).add(Mockito.eq("DisplayedBatchPushNotification"));
        Mockito.verify(screen).Campaign(decodedXtor);
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testNotificationDisplayHostLessDeeplinkQuery() {

        String xtor = "CS1-[mylabeltest]-test-15[sef]";
        String campaignExpected = "[mylabeltest]";

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add(campaignExpected)).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("DisplayedBatchPushNotification")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                "batch://?xtor=" + xtor,
                new Bundle());

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);

        Mockito.verify(publishers).add(Mockito.eq(campaignExpected));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[push]"));
        Mockito.verify(publisher).sendImpression();
        Mockito.verify(publisher, Mockito.never()).sendTouch();

        Mockito.verify(screens).add(Mockito.eq("DisplayedBatchPushNotification"));
        Mockito.verify(screen).Campaign(xtor);
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testNotificationDisplayHostLessDeeplinkFragment() {

        String xtor = "CS1-[mylabeltest]-test-15[sef]";
        String campaignExpected = "[mylabeltest]";

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add(campaignExpected)).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("DisplayedBatchPushNotification")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                "batch://#xtor=" + xtor,
                new Bundle());

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);

        Mockito.verify(publishers).add(Mockito.eq(campaignExpected));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[push]"));
        Mockito.verify(publisher).sendImpression();
        Mockito.verify(publisher, Mockito.never()).sendTouch();

        Mockito.verify(screens).add(Mockito.eq("DisplayedBatchPushNotification"));
        Mockito.verify(screen).Campaign(xtor);
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testNotificationOpenCampaignLabelQuery() {

        String xtor = "CS2-[mylabeltesttoto]-test-15[sef]";
        String campaignExpected = "[mylabeltesttoto]";

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add(campaignExpected)).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("OpenedBatchPushNotification")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com/test?xtor=" + xtor,
                new Bundle(), true);

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_OPEN, payload);

        Mockito.verify(publishers).add(Mockito.eq(campaignExpected));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[push]"));
        Mockito.verify(publisher).sendTouch();
        Mockito.verify(publisher, Mockito.never()).sendImpression();

        Mockito.verify(screens).add(Mockito.eq("OpenedBatchPushNotification"));
        Mockito.verify(screen).Campaign(xtor);
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testNotificationOpenCampaignLabelQueryEncode() {

        String xtor = "CS2-%5Bmylabeltesttoto%5D-test-15%5Bsef%5D";
        String decodedXtor = "CS2-[mylabeltesttoto]-test-15[sef]";
        String campaignExpected = "[mylabeltesttoto]";

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add(campaignExpected)).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("OpenedBatchPushNotification")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com/test?xtor=" + xtor,
                new Bundle(), true);

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_OPEN, payload);

        Mockito.verify(publishers).add(Mockito.eq(campaignExpected));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[push]"));
        Mockito.verify(publisher).sendTouch();
        Mockito.verify(publisher, Mockito.never()).sendImpression();

        Mockito.verify(screens).add(Mockito.eq("OpenedBatchPushNotification"));
        Mockito.verify(screen).Campaign(decodedXtor);
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testNotificationOpenCampaignLabelCustomPayload() {

        String xtor = "CS3-[mytoto]-test-15[sef]";
        String campaignExpected = "[mytoto]";

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add(campaignExpected)).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("OpenedBatchPushNotification")).thenReturn(screen);

        Bundle customPayload = new Bundle();
        customPayload.putString("xtor", xtor);
        TestEventPayload payload = new TestEventPayload(null,
                null,
                customPayload, true);

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_OPEN, payload);

        Mockito.verify(publishers).add(Mockito.eq(campaignExpected));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[push]"));
        Mockito.verify(publisher).sendTouch();
        Mockito.verify(publisher, Mockito.never()).sendImpression();

        Mockito.verify(screens).add(Mockito.eq("OpenedBatchPushNotification"));
        Mockito.verify(screen).Campaign(xtor);
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testNotificationOpenCampaignLabelTrackingID() {

        String xtor = "CS3-[mytoto]-test-15[sef]";
        String campaignExpected = "[mytoto]";

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add(campaignExpected)).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("OpenedBatchPushNotification")).thenReturn(screen);

        Bundle customPayload = new Bundle();
        TestEventPayload payload = new TestEventPayload(xtor,
                null,
                customPayload, true);

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_OPEN, payload);

        Mockito.verify(publishers).add(Mockito.eq(campaignExpected));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[push]"));
        Mockito.verify(publisher).sendTouch();
        Mockito.verify(publisher, Mockito.never()).sendImpression();

        Mockito.verify(screens).add(Mockito.eq("OpenedBatchPushNotification"));
        Mockito.verify(screen).Campaign(xtor);
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testNotificationOpenNonPositive() {

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add("[batch-default-campaign]")).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("OpenedBatchPushNotification")).thenReturn(screen);

        Bundle customPayload = new Bundle();
        TestEventPayload payload = new TestEventPayload(null,
                null,
                customPayload, false);

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_OPEN, payload);
        Mockito.verify(publisher, Mockito.never()).sendTouch();
        Mockito.verify(publisher, Mockito.never()).sendImpression();

        Mockito.verify(screens).add(Mockito.eq("OpenedBatchPushNotification"));
        Mockito.verify(screen, Mockito.never()).Campaign(Mockito.anyString());
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testNotificationOpenCampaignLabelPriority() {

        String xtor = "CS3-[mytoto]-test-15[sef]";
        String campaignExpected = "[mytoto]";

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add(campaignExpected)).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("OpenedBatchPushNotification")).thenReturn(screen);

        Bundle customPayload = new Bundle();
        customPayload.putString("xtor", xtor);
        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com/test?xtor=AD-[fake]#xtor=CS8-[fake2]",
                customPayload, true);

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_OPEN, payload);

        Mockito.verify(publishers).add(Mockito.eq(campaignExpected));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[push]"));
        Mockito.verify(publisher).sendTouch();
        Mockito.verify(publisher, Mockito.never()).sendImpression();

        Mockito.verify(screens).add(Mockito.eq("OpenedBatchPushNotification"));
        Mockito.verify(screen).Campaign(xtor);
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testNotificationOpenCampaignLabelNonTrimmed() {

        String campaignExpected = "[fake]";

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add(campaignExpected)).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("OpenedBatchPushNotification")).thenReturn(screen);

        Bundle customPayload = new Bundle();
        TestEventPayload payload = new TestEventPayload(null,
                " \n               https://batch.com/test?xtor=AD-[fake]          \n ",
                customPayload, true);

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_OPEN, payload);

        Mockito.verify(publishers).add(Mockito.eq(campaignExpected));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[push]"));
        Mockito.verify(publisher).sendTouch();
        Mockito.verify(publisher, Mockito.never()).sendImpression();

        Mockito.verify(screens).add(Mockito.eq("OpenedBatchPushNotification"));
        Mockito.verify(screen).Campaign("AD-[fake]");
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testNotificationOpen() {

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add("[batch-default-campaign]")).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("OpenedBatchPushNotification")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                null,
                new Bundle(), true);

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_OPEN, payload);

        Mockito.verify(publishers).add(Mockito.eq("[batch-default-campaign]"));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[push]"));
        Mockito.verify(publisher).sendTouch();
        Mockito.verify(publisher, Mockito.never()).sendImpression();

        Mockito.verify(screens).add(Mockito.eq("OpenedBatchPushNotification"));
        Mockito.verify(screen, Mockito.never()).Campaign(Mockito.anyString());
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testNotificationDismiss() {

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add("[batch-default-campaign]")).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("DismissedBatchPushNotification")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                null,
                new Bundle(), true);

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISMISS, payload);
        Mockito.verify(publisher, Mockito.never()).sendImpression();
        Mockito.verify(publisher, Mockito.never()).sendTouch();

        Mockito.verify(screens).add(Mockito.eq("DismissedBatchPushNotification"));
        Mockito.verify(screen, Mockito.never()).Campaign(Mockito.anyString());
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testInAppShow() {

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add("[batch-default-campaign]")).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("ShowedBatchInAppMessage")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                null,
                new Bundle());

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_SHOW, payload);

        Mockito.verify(publishers).add(Mockito.eq("[batch-default-campaign]"));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[in-app]"));
        Mockito.verify(publisher).sendImpression();
        Mockito.verify(publisher, Mockito.never()).sendTouch();

        Mockito.verify(screens).add(Mockito.eq("ShowedBatchInAppMessage"));
        Mockito.verify(screen, Mockito.never()).Campaign(Mockito.anyString());
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testInAppShowCampaignId() {
        String xtor = "AD-4242-yolo-swag";

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add("4242")).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("ShowedBatchInAppMessage")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(xtor,
                null,
                new Bundle());

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_SHOW, payload);

        Mockito.verify(publishers).add(Mockito.eq("4242"));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[in-app]"));
        Mockito.verify(publisher).sendImpression();
        Mockito.verify(publisher, Mockito.never()).sendTouch();

        Mockito.verify(screens).add(Mockito.eq("ShowedBatchInAppMessage"));
        Mockito.verify(screen).Campaign(xtor);
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testInAppShowCampaignLabelFragmentUppercase() {

        String xtor = "CS1-[mylabeltest]-test-15[sef]";
        String campaignExpected = "[mylabeltest]";

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add(campaignExpected)).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("ShowedBatchInAppMessage")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com/test#XtOr=" + xtor,
                new Bundle());

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_SHOW, payload);

        Mockito.verify(publishers).add(Mockito.eq(campaignExpected));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[in-app]"));
        Mockito.verify(publisher).sendImpression();
        Mockito.verify(publisher, Mockito.never()).sendTouch();

        Mockito.verify(screens).add(Mockito.eq("ShowedBatchInAppMessage"));
        Mockito.verify(screen).Campaign(xtor);
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testInAppShowCampaignLabelQueryUppercase() {

        String xtor = "CS1-[mylabeltest]-test-15[sef]";
        String campaignExpected = "[mylabeltest]";

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add(campaignExpected)).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("ShowedBatchInAppMessage")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com/test?XTor=" + xtor,
                new Bundle());

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_SHOW, payload);

        Mockito.verify(publishers).add(Mockito.eq(campaignExpected));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[in-app]"));
        Mockito.verify(publisher).sendImpression();
        Mockito.verify(publisher, Mockito.never()).sendTouch();

        Mockito.verify(screens).add(Mockito.eq("ShowedBatchInAppMessage"));
        Mockito.verify(screen).Campaign(xtor);
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testInAppClickCampaignLabel() {
        String xtor = "EPR-[mylabel]-totot-titi";
        String campaignExpected = "[mylabel]";

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add(campaignExpected)).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("ClickedBatchInAppMessage")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(xtor,
                null,
                new Bundle(), true);

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_CLICK, payload);

        Mockito.verify(publishers).add(Mockito.eq(campaignExpected));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[in-app]"));
        Mockito.verify(publisher).sendTouch();
        Mockito.verify(publisher, Mockito.never()).sendImpression();

        Mockito.verify(screens).add(Mockito.eq("ClickedBatchInAppMessage"));
        Mockito.verify(screen).Campaign(xtor);
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testInAppClickNonPositive() {

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add("[batch-default-campaign]")).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("ClickedBatchInAppMessage")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                null,
                new Bundle(), false);

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_CLICK, payload);

        Mockito.verify(publisher, Mockito.never()).sendTouch();
        Mockito.verify(publisher, Mockito.never()).sendImpression();

        Mockito.verify(screens).add(Mockito.eq("ClickedBatchInAppMessage"));
        Mockito.verify(screen, Mockito.never()).Campaign(Mockito.anyString());
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testInAppGlobalTap() {

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add("[batch-default-campaign]")).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("ClickedBatchInAppMessage")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                null,
                new Bundle(), true);

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_CLICK, payload);

        Mockito.verify(publishers).add(Mockito.eq("[batch-default-campaign]"));
        Mockito.verify(publisher).setAdvertiserId(Mockito.eq("[batch]"));
        Mockito.verify(publisher).setFormat(Mockito.eq("[in-app]"));
        Mockito.verify(publisher).sendTouch();
        Mockito.verify(publisher, Mockito.never()).sendImpression();

        Mockito.verify(screens).add(Mockito.eq("ClickedBatchInAppMessage"));
        Mockito.verify(screen, Mockito.never()).Campaign(Mockito.anyString());
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testInAppClose() {

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add("[batch-default-campaign]")).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("ClosedBatchInAppMessage")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                null,
                new Bundle());

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_CLOSE, payload);
        Mockito.verify(publisher, Mockito.never()).sendImpression();
        Mockito.verify(publisher, Mockito.never()).sendTouch();

        Mockito.verify(screens).add(Mockito.eq("ClosedBatchInAppMessage"));
        Mockito.verify(screen, Mockito.never()).Campaign(Mockito.anyString());
        Mockito.verify(screen).sendView();
    }

    @Test
    public void testInAppAutoClose() {

        Publisher publisher = PowerMockito.mock(Publisher.class);
        Mockito.when(publishers.add("[batch-default-campaign]")).thenReturn(publisher);

        Screen screen = PowerMockito.mock(Screen.class);
        Mockito.when(screens.add("AutoClosedBatchInAppMessage")).thenReturn(screen);

        TestEventPayload payload = new TestEventPayload(null,
                null,
                new Bundle());

        atInternetDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_AUTO_CLOSE, payload);

        Mockito.verify(publisher, Mockito.never()).sendImpression();
        Mockito.verify(publisher, Mockito.never()).sendTouch();

        Mockito.verify(screens).add(Mockito.eq("AutoClosedBatchInAppMessage"));
        Mockito.verify(screen, Mockito.never()).Campaign(Mockito.anyString());
        Mockito.verify(screen).sendView();
    }

    private static class TestEventPayload implements Batch.EventDispatcher.Payload {

        private String trackingId;
        private String deeplink;
        private Bundle customPayload;
        private boolean isPositive;

        TestEventPayload(String trackingId,
                                String deeplink,
                                Bundle customPayload)
        {
           this(trackingId, deeplink, customPayload, false);
        }

        TestEventPayload(String trackingId,
                                String deeplink,
                                Bundle customPayload,
                                boolean isPositive)
        {
            this.trackingId = trackingId;
            this.deeplink = deeplink;
            this.customPayload = customPayload;
            this.isPositive = isPositive;
        }

        @Nullable
        @Override
        public String getTrackingId()
        {
            return trackingId;
        }

        @Nullable
        @Override
        public String getDeeplink()
        {
            return deeplink;
        }

        @Override
        public boolean isPositiveAction()
        {
            return isPositive;
        }

        @Nullable
        @Override
        public String getCustomValue(@NonNull String key)
        {
            if (customPayload == null) {
                return null;
            }
            return customPayload.getString(key);
        }

        @Nullable
        @Override
        public BatchMessage getMessagingPayload()
        {
            return null;
        }

        @Nullable
        @Override
        public BatchPushPayload getPushPayload()
        {
            return null;
        }
    }
}
