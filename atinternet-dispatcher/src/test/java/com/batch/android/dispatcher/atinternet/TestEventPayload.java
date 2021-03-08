package com.batch.android.dispatcher.atinternet;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.batch.android.Batch;
import com.batch.android.BatchMessage;
import com.batch.android.BatchPushPayload;

public class TestEventPayload implements Batch.EventDispatcher.Payload {

    private String trackingId;
    private String deeplink;
    private String webViewAnalyticsID;
    private Bundle customPayload;
    private boolean isPositive;

    TestEventPayload(String trackingId,
                     String deeplink,
                     Bundle customPayload)
    {
        this(trackingId, null, deeplink, customPayload, false);
    }

    TestEventPayload(String trackingId,
                     String webViewAnalyticsID,
                     String deeplink,
                     Bundle customPayload)
    {
        this(trackingId, webViewAnalyticsID, deeplink, customPayload, false);
    }

    TestEventPayload(String trackingId,
                     String webViewAnalyticsID,
                     String deeplink,
                     Bundle customPayload,
                     boolean isPositive)
    {
        this.trackingId = trackingId;
        this.webViewAnalyticsID = webViewAnalyticsID;
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

    @Nullable
    public String getWebViewAnalyticsID() {
        return webViewAnalyticsID;
    }
}