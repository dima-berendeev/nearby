package org.berendeev.nearby.di

import org.berendeev.nearby.data.PlacesNearbyRepository
import org.berendeev.nearby.data.PlacesNearbyRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlacesNearbyRepositoryModule {
    @Binds
    @Singleton
    abstract fun binds(impl: PlacesNearbyRepositoryImpl): PlacesNearbyRepository
}
