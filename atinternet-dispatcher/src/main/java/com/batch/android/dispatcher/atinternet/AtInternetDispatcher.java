package com.batch.android.dispatcher.atinternet;

import android.net.Uri;

import com.atinternet.tracker.ATInternet;
import com.atinternet.tracker.Tracker;
import com.batch.android.Batch;
import com.batch.android.BatchEventDispatcher;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * AtInternet Event Dispatcher
 * The dispatcher should generate the XTOR tag from a Batch payload and send it to the AtInternet SDK
 * See : https://marketplace.atinternet-solutions.com/ATInternetCampaignCreator/xtor/
 */
public class AtInternetDispatcher implements BatchEventDispatcher
{
    private static final String XTOR_KEY = "xtor";

    private Tracker tracker;

    AtInternetDispatcher()
    {
        tracker = ATInternet.getInstance().getDefaultTracker();
    }

    @Override
    public void dispatchEvent(@NonNull Batch.EventDispatcher.Type type,
                              @NonNull Batch.EventDispatcher.Payload payload)
    {
        String xtor;
        if (type.isNotification()) {
            xtor = getNotificationXtor(payload);
        } else {
            xtor = payload.getTrackingId();
        }

        if (xtor != null) {
            tracker.Campaigns().add(xtor);
        }

        // Send event views
        tracker.Screens().add(type.getEventName()).sendView();
    }

    private static String getNotificationXtor(Batch.EventDispatcher.Payload payload)
    {

        String xtor = null;
        String deeplink = payload.getDeeplink();
        if (deeplink != null) {
            Uri uri = Uri.parse(deeplink);

            String fragment = uri.getFragment();
            if (fragment != null && !fragment.isEmpty()) {
                Map<String, String> fragments = getFragmentMap(fragment);
                String mapXtor = fragments.get(XTOR_KEY);
                if (mapXtor != null) {
                    xtor = mapXtor;
                }
            }

            String queryXtor = uri.getQueryParameter(XTOR_KEY);
            if (queryXtor != null) {
                xtor = queryXtor;
            }
        }

        String payloadXtor = payload.getCustomValue(XTOR_KEY);
        if (payloadXtor != null) {
            xtor = payloadXtor;
        }
        return xtor;
    }

    private static Map<String, String> getFragmentMap(String fragment)
    {
        String[] params = fragment.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String[] parts = param.split("=");
            if (parts.length >= 2) {
                map.put(parts[0], parts[1]);
            }
        }
        return map;
    }
}

