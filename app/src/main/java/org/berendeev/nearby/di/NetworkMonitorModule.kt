package org.berendeev.nearby.di

import org.berendeev.nearby.data.ConnectivityManagerNetworkMonitor
import org.berendeev.nearby.data.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkMonitorModule {
    @Binds
    @Singleton
    abstract fun binds(impl: ConnectivityManagerNetworkMonitor): NetworkMonitor
}
