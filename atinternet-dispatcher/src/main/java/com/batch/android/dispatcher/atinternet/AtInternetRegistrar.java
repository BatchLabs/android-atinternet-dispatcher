package com.batch.android.dispatcher.atinternet;

import android.content.Context;

import com.batch.android.BatchEventDispatcher;
import com.batch.android.eventdispatcher.DispatcherRegistrar;

/**
 * AtInternet Registrar
 * The class will be instantiated from the SDK using reflection
 * See the library {@link android.Manifest} for more information
 */
public class AtInternetRegistrar implements DispatcherRegistrar
{
    /**
     * Singleton instance
     */
    private static AtInternetDispatcher instance = null;

    /**
     * Singleton accessor
     * @param context
     * @return
     */
    @Override
    public BatchEventDispatcher getDispatcher(Context context)
    {
        if (instance == null) {
            instance = new AtInternetDispatcher();
        }
        return instance;
    }
}

