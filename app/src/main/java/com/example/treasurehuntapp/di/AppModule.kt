package com.example.treasurehuntapp.di

import android.content.Context
import com.example.treasurehuntapp.data.local.CurrentHuntStorage
import com.example.treasurehuntapp.data.repository.TreasureHuntRepository
import com.example.treasurehuntapp.data.source.remote.api.TreasureHuntApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCurrentHuntStorage(
        @ApplicationContext context: Context
    ): CurrentHuntStorage = CurrentHuntStorage(context)

    @Provides
    @Singleton
    fun provideTreasureHuntRepository(
        api: TreasureHuntApi
    ): TreasureHuntRepository = TreasureHuntRepository(api)
}
