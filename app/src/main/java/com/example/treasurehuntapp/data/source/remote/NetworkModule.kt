package com.example.treasurehuntapp.di

import com.example.treasurehuntapp.BuildConfig
import com.example.treasurehuntapp.data.source.remote.api.AuthApi
import com.example.treasurehuntapp.data.source.remote.auth.AuthInterceptor
import com.example.treasurehuntapp.data.source.remote.auth.TokenAuthenticator
import com.example.treasurehuntapp.data.source.remote.auth.UnauthorizedInterceptor
import com.example.treasurehuntapp.data.source.remote.api.NotificationsApi
import com.example.treasurehuntapp.data.source.remote.api.TreasureHuntApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import android.content.Context
import com.example.treasurehuntapp.data.source.remote.api.LocationsApi
import com.example.treasurehuntapp.data.source.remote.api.UsersApi
import com.example.treasurehuntapp.data.source.remote.api.UserAnswerApi
import com.example.treasurehuntapp.data.source.remote.api.UserProgressApi
import com.example.treasurehuntapp.data.source.remote.api.UploadsApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import com.example.treasurehuntapp.data.source.remote.auth.TokenStorage

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("noAuthOkHttp")
    fun provideNoAuthOkHttp(): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("Authorization")
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .build()
    }

    @Provides
    @Singleton
    @Named("noAuthRetrofit")
    fun provideNoAuthRetrofit(
        @Named("noAuthOkHttp") client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("authApiNoAuth")
    fun provideAuthApiNoAuth(
        @Named("noAuthRetrofit") retrofit: Retrofit
    ): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideTokenStorage(
        @ApplicationContext context: Context,
        @ApplicationScope appScope: CoroutineScope
    ): TokenStorage = TokenStorage(context, appScope)

    @Provides
    @Singleton
    fun provideOkHttp(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        unauthorizedInterceptor: UnauthorizedInterceptor
    ): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("Authorization")
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(unauthorizedInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(logger)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideTreasureHuntApi(retrofit: Retrofit): TreasureHuntApi =
        retrofit.create(TreasureHuntApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationsApi(retrofit: Retrofit): NotificationsApi =
        retrofit.create(NotificationsApi::class.java)

    @Provides
    @Singleton
    fun provideUsersApi(retrofit: Retrofit): UsersApi =
        retrofit.create(UsersApi::class.java)

    @Provides
    fun provideLocationApi(retrofit: Retrofit): LocationsApi =
        retrofit.create(LocationsApi::class.java)

    @Provides
    fun provideUserAnswerApi(retrofit: Retrofit): UserAnswerApi =
        retrofit.create(UserAnswerApi::class.java)

    @Provides
    fun provideUserProgressApi(retrofit: Retrofit): UserProgressApi =
        retrofit.create(UserProgressApi::class.java)

    @Provides
    fun provideUploadsApi(retrofit: Retrofit): UploadsApi =
        retrofit.create(UploadsApi::class.java)

}

