/*
 * The MIT License (MIT)
 * Copyright © 2018 NBCO Yandex.Money LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package ru.yandex.money.android.sdk.impl

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import ru.yandex.money.android.sdk.BuildConfig
import ru.yandex.money.android.sdk.impl.extensions.androidId
import ru.yandex.money.android.sdk.impl.extensions.isTablet

internal class UserAgentInterceptor(androidId: String, isTablet: Boolean) : Interceptor {
    private val userAgent =
        "Yandex.Checkout/Android/${BuildConfig.VERSION_CODE}/$androidId/${getDeviceType(isTablet)}"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(request)
    }

    private fun getDeviceType(isTablet: Boolean) = if (isTablet) {
        "tablet"
    } else {
        "smartphone"
    }
}

internal fun OkHttpClient.Builder.addUserAgent(context: Context): OkHttpClient.Builder =
    addInterceptor(UserAgentInterceptor(context.androidId, context.isTablet))
