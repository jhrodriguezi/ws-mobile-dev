package lab.jhrodriguezi.tictactoe.di

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import lab.jhrodriguezi.tictactoe.data.network.FirebaseService

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Singleton
    @Provides
    fun providesDatabaseReference() = Firebase.database.reference

    @Singleton
    @Provides
    fun providesFirebaseService(databaseReference: DatabaseReference) =
        FirebaseService(databaseReference)
}