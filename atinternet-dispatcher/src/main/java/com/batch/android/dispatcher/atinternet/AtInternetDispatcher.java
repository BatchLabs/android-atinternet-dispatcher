package com.batch.android.dispatcher.atinternet;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.atinternet.tracker.ATInternet;
import com.atinternet.tracker.CustomVar;
import com.atinternet.tracker.Publisher;
import com.atinternet.tracker.Screen;
import com.atinternet.tracker.Tracker;
import com.batch.android.Batch;
import com.batch.android.BatchEventDispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * AtInternet Event Dispatcher
 * The dispatcher should generate an On-site ads and a campaigns from a XTOR tag and send it to the AtInternet SDK
 * See : https://marketplace.atinternet-solutions.com/ATInternetCampaignCreator/xtor/
 * And : https://developers.atinternet-solutions.com/javascript-en/campaigns-javascript-en/marketing-campaigns-v2/
 */
public class AtInternetDispatcher implements BatchEventDispatcher
{

    /**
     * Batch internal dispatcher information used for analytics
     */
    private static final String DISPATCHER_NAME = "at_internet";
    private static final int DISPATCHER_VERSION = 1;

    private static final String XTOR = "xtor";
    private static final String BATCH_DEFAULT_CAMPAIGN = "[batch-default-campaign]";
    public static final String BATCH_CAMPAIGN_TRACKER = "batch-campaign-tracker";
    public static final String BATCH_PUBLISHER_TRACKER = "batch-publisher-tracker";

    /**
     * Event name used when logging on AT Internet
     */
    private static final String NOTIFICATION_DISPLAY_NAME = "DisplayedBatchPushNotification";
    private static final String NOTIFICATION_OPEN_NAME = "OpenedBatchPushNotification";
    private static final String NOTIFICATION_DISMISS_NAME = "DismissedBatchPushNotification";
    private static final String MESSAGING_SHOW_NAME = "ShowedBatchInAppMessage";
    private static final String MESSAGING_CLOSE_NAME = "ClosedBatchInAppMessage";
    private static final String MESSAGING_CLOSE_ERROR_NAME = "ClosedErrorBatchInAppMessage";
    private static final String MESSAGING_AUTO_CLOSE_NAME = "AutoClosedBatchInAppMessage";
    private static final String MESSAGING_CLICK_NAME = "ClickedBatchInAppMessage";
    private static final String MESSAGING_WEBVIEW_CLICK_NAME = "WebViewClickedBatchInAppMessage";
    private static final String UNKNOWN_EVENT_NAME = "UnknownBatchMessage";

    private final Map<String, Tracker> trackerCache;

    private Tracker trackerOverride;

    AtInternetDispatcher()
    {
        trackerCache = new HashMap<>();
    }

    /**
     * Callback when a new event just happened in the Batch SDK.
     *
     * @param type The type of the event
     * @param payload The payload associated with the event
     */
    @Override
    public void dispatchEvent(@NonNull Batch.EventDispatcher.Type type,
                              @NonNull Batch.EventDispatcher.Payload payload)
    {
        String xtorTag = getXtorTag(payload);

        if (shouldBeDispatchedAsOnSiteAd(type)) {
            dispatchAsOnSiteAd(type, payload, xtorTag);
        }

        Screen screen = getTracker(BATCH_CAMPAIGN_TRACKER).Screens().add(getATEventName(type));
        if (xtorTag != null) {
            screen.Campaign(xtorTag);
        }

        String webViewButtonId = payload.getWebViewAnalyticsID();
        if (webViewButtonId != null) {
            screen.CustomVars().add(1, webViewButtonId, CustomVar.CustomVarType.Screen);
        }
        screen.sendView();
    }

    /**
     Specify the ATInternet {@link Tracker} instance that the dispatcher should use.
     If set to null (its default value), Batch will automatically instantiate its own trackers
     using the default configuration.
     This method is useful if you set your {@link Tracker} instances in your code.

     @param trackerOverride Tracker instance to use. null if Batch should use its default one.
     */
    public void setTrackerOverride(@Nullable Tracker trackerOverride) {
        this.trackerOverride = trackerOverride;
        if (trackerOverride == null) {
            clearTrackerCache();
        }
    }

    private Tracker getTracker(@NonNull String trackerName) {
        if (trackerOverride != null) {
            return trackerOverride;
        }

        Tracker tracker = trackerCache.get(trackerName);
        if (tracker == null) {
            synchronized (trackerCache) {
                tracker = trackerCache.get(trackerName);
                if (tracker == null) {
                    tracker = ATInternet.getInstance().getTracker(trackerName);
                    trackerCache.put(trackerName, tracker);
                }
            }
        }
        return tracker;
    }

    private void clearTrackerCache() {
        synchronized (trackerCache) {
            trackerCache.clear();
        }
    }

    private void dispatchAsOnSiteAd(Batch.EventDispatcher.Type type, Batch.EventDispatcher.Payload payload, String xtorTag) {

        Publisher publisher;
        String campaign = null;

        if (xtorTag != null) {
            Xtor xtor = Xtor.parse(xtorTag);
            if (xtor.isValidXtor()) {
                campaign = xtor.getPart(1);
            }
        }

        Tracker publisherTracker = getTracker(BATCH_PUBLISHER_TRACKER);
        if (campaign != null && !campaign.isEmpty()) {
            publisher = publisherTracker.Publishers().add(campaign);
        } else {
            publisher = publisherTracker.Publishers().add(BATCH_DEFAULT_CAMPAIGN);
        }

        if (type.isNotificationEvent()) {
            publisher.setFormat("[push]");
        } else if (type.isMessagingEvent()) {
            publisher.setFormat("[in-app]");
        }
        publisher.setAdvertiserId("[batch]");

        String webViewButtonId = payload.getWebViewAnalyticsID();
        if (webViewButtonId != null) {
            publisher.setVariant("[" + webViewButtonId + "]");
        }

        if (isImpression(type)) {
            publisher.sendImpression();
        } else if (isClick(type) &&
                (payload.isPositiveAction() || type == Batch.EventDispatcher.Type.MESSAGING_WEBVIEW_CLICK)) {
            // We send the click if it's a positive action or if it's a click inside a WebView In-App
            publisher.sendTouch();
        }
    }

    private static String getXtorTag(Batch.EventDispatcher.Payload payload) {
        String xtorTag = payload.getTrackingId();
        if (xtorTag != null && !xtorTag.isEmpty()) {
            return xtorTag;
        }
        return getTagFromPayload(payload, XTOR);
    }

    private static String getTagFromPayload(Batch.EventDispatcher.Payload payload, String tagName) {
        String tag = getTagFromDeeplink(payload, tagName);

        String tagTmp = payload.getCustomValue(tagName);
        if (tagTmp != null) {
            tag = tagTmp;
        }
        return tag;
    }

    private static String getTagFromDeeplink(Batch.EventDispatcher.Payload payload, String tagName) {
        String tag = null;
        String deeplink = payload.getDeeplink();
        if (deeplink != null) {
            try {
                deeplink = deeplink.trim();
                tagName = tagName.toLowerCase();
                Uri uri = Uri.parse(deeplink);

                String fragment = uri.getFragment();
                if (uri.isHierarchical()) {
                    if (fragment != null && !fragment.isEmpty()) {
                        Map<String, String> fragments = getFragmentMap(fragment);
                        String tagTmp = fragments.get(tagName);
                        if (tagTmp != null) {
                            tag =  tagTmp;
                        }
                    }

                    Set<String> keys = uri.getQueryParameterNames();
                    for (String key : keys) {
                        if (tagName.equalsIgnoreCase(key)) {
                            return uri.getQueryParameter(key);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("Batch", "Something went wrong parsing deeplink: " + e.getLocalizedMessage());
            }
        }
        return tag;
    }

    private static boolean shouldBeDispatchedAsOnSiteAd(Batch.EventDispatcher.Type type) {
        return isImpression(type) || isClick(type);
    }

    private static boolean isImpression(Batch.EventDispatcher.Type type) {
        return type.equals(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY) ||
                type.equals(Batch.EventDispatcher.Type.MESSAGING_SHOW);
    }

    private static boolean isClick(Batch.EventDispatcher.Type type) {
        return type.equals(Batch.EventDispatcher.Type.NOTIFICATION_OPEN) ||
                type.equals(Batch.EventDispatcher.Type.MESSAGING_CLICK) ||
                type.equals(Batch.EventDispatcher.Type.MESSAGING_WEBVIEW_CLICK);
    }

    private static Map<String, String> getFragmentMap(String fragment)
    {
        String[] params = fragment.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String[] parts = param.split("=");
            if (parts.length >= 2) {
                map.put(parts[0].toLowerCase(), parts[1]);
            }
        }
        return map;
    }

    private static String getATEventName(Batch.EventDispatcher.Type type) {
        switch (type) {
            case NOTIFICATION_DISPLAY:
                return NOTIFICATION_DISPLAY_NAME;
            case NOTIFICATION_OPEN:
                return NOTIFICATION_OPEN_NAME;
            case NOTIFICATION_DISMISS:
                return NOTIFICATION_DISMISS_NAME;
            case MESSAGING_SHOW:
                return MESSAGING_SHOW_NAME;
            case MESSAGING_CLOSE:
                return MESSAGING_CLOSE_NAME;
            case MESSAGING_AUTO_CLOSE:
                return MESSAGING_AUTO_CLOSE_NAME;
            case MESSAGING_CLOSE_ERROR:
                return MESSAGING_CLOSE_ERROR_NAME;
            case MESSAGING_CLICK:
                return MESSAGING_CLICK_NAME;
            case MESSAGING_WEBVIEW_CLICK:
                return MESSAGING_WEBVIEW_CLICK_NAME;
        }
        return UNKNOWN_EVENT_NAME;
    }

    @Nullable
    @Override
    public String getName() {
        return DISPATCHER_NAME;
    }

    @Override
    public int getVersion() {
        return DISPATCHER_VERSION;
    }
}
