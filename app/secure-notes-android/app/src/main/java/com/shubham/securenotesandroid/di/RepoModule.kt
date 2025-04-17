package com.shubham.securenotesandroid.di

import com.shubham.securenotesandroid.core.domain.repositories.AuthRepository
import com.shubham.securenotesandroid.core.domain.repositories.NoteRepository
import com.shubham.securenotesandroid.core.repositories.AuthRepositoryImpl
import com.shubham.securenotesandroid.core.repositories.NoteRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepoModule {

    @Binds
    fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    fun bindNoteRepository(
        noteRepositoryImpl: NoteRepositoryImpl
    ): NoteRepository
}