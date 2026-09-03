package com.minimechanicserviceapp.di

import com.minimechanicserviceapp.BuildConfig
import com.minimechanicserviceapp.data.remote.MechanicApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson() : Json = Json {
            ignoreUnknownKeys  = true
            coerceInputValues =  true
    }
    @Provides
    @Singleton
    fun provideOkHttpClient() : OkHttpClient{
        val logging = HttpLoggingInterceptor().apply {
            level  = if(BuildConfig.DEBUG){
                HttpLoggingInterceptor.Level.BODY
            }else{
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20 , TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()


    @Provides
    @Singleton
    fun provideMechanicApi(retrofit: Retrofit): MechanicApi =
        retrofit.create(MechanicApi::class.java)


}