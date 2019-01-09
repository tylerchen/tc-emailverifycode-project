/*******************************************************************************
 * Copyright (c) 2019-01-08 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.iff.app.emailverifycode;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.iff.infra.util.CacheHelper;
import org.iff.infra.util.NumberHelper;
import org.iff.util.SystemHelper;

import java.util.concurrent.TimeUnit;

/**
 * GuavaCache
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-01-08
 * auto generate by qdp.
 */
public class GuavaCache implements CacheHelper.Cacheable {
    private static final Cache<String, String> cache;

    static {
        int expireAfterWriteSeconds = NumberHelper.getInt(SystemHelper.getProps().getProperty("cache.expireAfterWriteSeconds"), 300);
        int expireAfterAccessSeconds = NumberHelper.getInt(SystemHelper.getProps().getProperty("cache.expireAfterAccessSeconds"), 30);
        int initialCapacity = NumberHelper.getInt(SystemHelper.getProps().getProperty("cache.initialCapacity"), 1000);
        int maximumSize = NumberHelper.getInt(SystemHelper.getProps().getProperty("cache.maximumSize"), 100000);
        cache = CacheBuilder
                .newBuilder()
                .expireAfterWrite(expireAfterWriteSeconds, TimeUnit.SECONDS)
                .expireAfterAccess(expireAfterAccessSeconds, TimeUnit.SECONDS)
                .initialCapacity(initialCapacity)
                .maximumSize(maximumSize)
                .build();
    }

    public String get(String key) {
        return cache.getIfPresent(key);
    }

    public void set(String key, Object value) {
        cache.put(key, (String) value);
    }

    public void set(String key, Object value, int seconds) {
        cache.put(key, (String) value);
    }

    public void set(String key, Object value, int timeToIdle, int timeToLive) {
        cache.put(key, (String) value);
    }

    public void del(String key) {
        cache.invalidate(key);
    }
}
