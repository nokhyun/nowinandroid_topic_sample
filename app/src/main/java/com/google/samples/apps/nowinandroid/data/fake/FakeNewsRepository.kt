/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.data.fake

import com.google.samples.apps.nowinandroid.data.model.NewsResource
import com.google.samples.apps.nowinandroid.data.repository.NewsRepository
import com.google.samples.apps.nowinandroid.di.NiaDispatchers
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json

/**
 * [NewsRepository] implementation that provides static news resources to aid development
 */

class FakeNewsRepository @Inject constructor(
    private val dispatchers: NiaDispatchers,
    private val networkJson: Json
) : NewsRepository {

    override fun getNewsResourcesStream(): Flow<List<NewsResource>> =
        flowOf(emptyList())

    override fun getNewsResourcesStream(filterTopicIds: Set<Int>): Flow<List<NewsResource>> =
        flowOf(emptyList())
}