package com.meikenn.tama.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.meikenn.tama.data.remote.ApiService
import com.meikenn.tama.data.remote.CookieJarImpl
import com.meikenn.tama.data.remote.ExternalApiService
import com.meikenn.tama.data.remote.HeaderInterceptor
import com.meikenn.tama.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    @Singleton
    @Named("cookie_prefs")
    fun provideCookiePrefs(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("cookies", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideCookieJar(@Named("cookie_prefs") prefs: SharedPreferences): CookieJarImpl {
        return CookieJarImpl(prefs)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cookieJar: CookieJarImpl,
        headerInterceptor: HeaderInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(headerInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(okHttpClient: OkHttpClient, gson: Gson): ApiService {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideExternalApiService(okHttpClient: OkHttpClient, gson: Gson): ExternalApiService {
        return Retrofit.Builder()
            .baseUrl(Constants.EXTERNAL_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ExternalApiService::class.java)
    }
}
