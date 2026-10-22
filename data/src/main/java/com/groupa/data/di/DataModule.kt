package com.groupa.data.di

import com.groupa.data.repository.MockGroupRepository
import com.groupa.domain.repository.GroupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindGroupRepository(repository: MockGroupRepository): GroupRepository
}
