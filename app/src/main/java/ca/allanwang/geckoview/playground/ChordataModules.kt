package ca.allanwang.geckoview.playground

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.mozilla.geckoview.GeckoRuntime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChordataModules {

    @Provides
    @Singleton
    fun runtime(@ApplicationContext appContext: Context): GeckoRuntime =
        GeckoRuntime.create(appContext)

}