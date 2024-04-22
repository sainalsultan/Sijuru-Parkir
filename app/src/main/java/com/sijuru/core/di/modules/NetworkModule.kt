package com.sijuru.core.di.modules

import android.content.Context
import com.sijuru.BuildConfig
import com.sijuru.core.data.network.ApiInterface
import com.sijuru.core.data.repository.MainRepository
import com.sijuru.core.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideRetrofit(sessionManager: SessionManager, @ApplicationContext context: Context): Retrofit {
        val headerInterceptor = Interceptor { chain ->
            val original: Request = chain.request()
            val request: Request = original.newBuilder()
                .header("X-Sijuru-Token", sessionManager.getTokenAccess())
                .header("Accept", "application/json")
                .method(original.method(), original.body())
                .build()

            chain.proceed(request)
        }

        val loggingInterceptor = if (BuildConfig.BUILD_TYPE == "debug") {
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        } else {
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        }


        val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()

        httpClient.readTimeout(500, TimeUnit.SECONDS)
        httpClient.connectTimeout(500, TimeUnit.SECONDS)
        httpClient.addInterceptor(headerInterceptor)
        httpClient.addInterceptor(loggingInterceptor)
        //httpClient.addInterceptor(ConnectivityInterceptor(context,logUtils))

        return Retrofit.Builder()
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.Base_Url)
            .build()
    }

    @Provides
    fun provideApiInterface(retrofit: Retrofit): ApiInterface {
        return retrofit.create(ApiInterface::class.java)
    }

    @Provides
    fun provideRepository(service: ApiInterface): MainRepository {
        return MainRepository(service)
    }
}