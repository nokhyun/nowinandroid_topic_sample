/*
 * Copyright 2022 The Android Open Source Project
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

package com.google.samples.apps.nowinandroid.feature.foryou

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells.Fixed
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.samples.apps.nowinandroid.core.model.data.FollowableTopic
import com.google.samples.apps.nowinandroid.core.model.data.NewsResource
import com.google.samples.apps.nowinandroid.core.model.data.NewsResourceType.Video
import com.google.samples.apps.nowinandroid.core.model.data.SaveableNewsResource
import com.google.samples.apps.nowinandroid.core.model.data.Topic
import com.google.samples.apps.nowinandroid.core.ui.NewsResourceCardExpanded
import com.google.samples.apps.nowinandroid.core.ui.NiaLoadingIndicator
import com.google.samples.apps.nowinandroid.core.ui.component.NiaToggleButton
import com.google.samples.apps.nowinandroid.core.ui.icon.NiaIcons
import com.google.samples.apps.nowinandroid.core.ui.theme.NiaTypography
import com.google.samples.apps.nowinandroid.feature.foryou.ForYouFeedUiState.PopulatedFeed
import com.google.samples.apps.nowinandroid.feature.foryou.ForYouFeedUiState.PopulatedFeed.FeedWithTopicSelection
import com.google.samples.apps.nowinandroid.feature.foryou.ForYouFeedUiState.PopulatedFeed.FeedWithoutTopicSelection
import kotlinx.datetime.Instant

@Composable
fun ForYouRoute(
    modifier: Modifier = Modifier,
    viewModel: ForYouViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    ForYouScreen(
        modifier = modifier,
        uiState = uiState,
        onTopicCheckedChanged = viewModel::updateTopicSelection,
        saveFollowedTopics = viewModel::saveFollowedTopics,
        onNewsResourcesCheckedChanged = viewModel::updateNewsResourceSaved
    )
}

@Composable
fun ForYouScreen(
    uiState: ForYouFeedUiState,
    onTopicCheckedChanged: (Int, Boolean) -> Unit,
    saveFollowedTopics: () -> Unit,
    onNewsResourcesCheckedChanged: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        when (uiState) {
            is ForYouFeedUiState.Loading -> {
                item {
                    NiaLoadingIndicator(
                        modifier = modifier,
                        contentDesc = stringResource(id = R.string.for_you_loading),
                    )
                }
            }
            is PopulatedFeed -> {
                when (uiState) {
                    is FeedWithTopicSelection -> {
                        item {
                            TopicSelection(uiState, onTopicCheckedChanged)
                        }
                        item {
                            // Done button
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = saveFollowedTopics,
                                    enabled = uiState.canSaveSelectedTopics,
                                    modifier = Modifier
                                        .padding(horizontal = 40.dp)
                                        .width(364.dp)
                                ) {
                                    Text(text = stringResource(R.string.done))
                                }
                            }
                        }
                    }
                    is FeedWithoutTopicSelection -> Unit
                }

                items(uiState.feed) { (newsResource: NewsResource, isBookmarked: Boolean) ->
                    val launchResourceIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(newsResource.url))
                    val context = LocalContext.current

                    NewsResourceCardExpanded(
                        newsResource = newsResource,
                        isBookmarked = isBookmarked,
                        onClick = { startActivity(context, launchResourceIntent, null) },
                        onToggleBookmark = {
                            onNewsResourcesCheckedChanged(newsResource.id, !isBookmarked)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicSelection(
    uiState: ForYouFeedUiState,
    onTopicCheckedChanged: (Int, Boolean) -> Unit
) {
    Column(Modifier.padding(top = 24.dp)) {

        Text(
            text = stringResource(R.string.onboarding_guidance_title),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            style = NiaTypography.titleMedium
        )

        Text(
            text = stringResource(R.string.onboarding_guidance_subtitle),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp),
            textAlign = TextAlign.Center,
            style = NiaTypography.bodyMedium
        )

        LazyHorizontalGrid(
            rows = Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .height(192.dp)
                .padding(top = 24.dp, bottom = 24.dp)
                .fillMaxWidth()
        ) {
            val state: FeedWithTopicSelection = uiState as FeedWithTopicSelection
            items(state.topics) {
                SingleTopicButton(
                    name = it.topic.name,
                    topicId = it.topic.id,
                    isSelected = it.isFollowed,
                    onClick = onTopicCheckedChanged
                )
            }
        }
    }
}

@Composable
private fun SingleTopicButton(
    name: String,
    topicId: Int,
    isSelected: Boolean,
    onClick: (Int, Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .width(264.dp)
            .height(56.dp)
            .padding(start = 12.dp, end = 8.dp)
            .background(
                MaterialTheme.colors.surface,
                shape = RoundedCornerShape(corner = CornerSize(8.dp))
            )
            .clickable(onClick = { onClick(topicId, !isSelected) }),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = name,
            style = NiaTypography.titleSmall,
            modifier = Modifier.padding(12.dp),
            color = MaterialTheme.colors.onSurface

        )
        NiaToggleButton(
            checked = isSelected,
            modifier = Modifier.align(alignment = Alignment.CenterEnd),
            onCheckedChange = { checked -> onClick(topicId, !isSelected) },
            icon = {
                Icon(imageVector = NiaIcons.Add, contentDescription = name)
            },
            checkedIcon = {
                Icon(imageVector = NiaIcons.Check, contentDescription = name)
            }
        )
    }
}

@Preview
@Composable
fun ForYouScreenLoading() {
    ForYouScreen(
        uiState = ForYouFeedUiState.Loading,
        onTopicCheckedChanged = { _, _ -> },
        saveFollowedTopics = {},
        onNewsResourcesCheckedChanged = { _, _ -> }
    )
}

@Preview
@Composable
fun ForYouScreenTopicSelection() {
    ForYouScreen(
        uiState = FeedWithTopicSelection(
            topics = listOf(
                FollowableTopic(
                    topic = Topic(
                        id = 0,
                        name = "Headlines",
                        shortDescription = "",
                        longDescription = "",
                        url = "",
                        imageUrl = ""
                    ),
                    isFollowed = false
                ),
                FollowableTopic(
                    topic = Topic(
                        id = 1,
                        name = "UI",
                        shortDescription = "",
                        longDescription = "",
                        url = "",
                        imageUrl = ""
                    ),
                    isFollowed = false
                ),
                FollowableTopic(
                    topic = Topic(
                        id = 2,
                        name = "Tools",
                        shortDescription = "",
                        longDescription = "",
                        url = "",
                        imageUrl = ""
                    ),
                    isFollowed = false
                ),
            ),
            feed = listOf(
                SaveableNewsResource(
                    newsResource = NewsResource(
                        id = 1,
                        episodeId = 52,
                        title = "Thanks for helping us reach 1M YouTube Subscribers",
                        content = "Thank you everyone for following the Now in Android series " +
                            "and everything the Android Developers YouTube channel has to offer. " +
                            "During the Android Developer Summit, our YouTube channel reached 1 " +
                            "million subscribers! Here’s a small video to thank you all.",
                        url = "https://youtu.be/-fJ6poHQrjM",
                        headerImageUrl = "https://i.ytimg.com/vi/-fJ6poHQrjM/maxresdefault.jpg",
                        publishDate = Instant.parse("2021-11-09T00:00:00.000Z"),
                        type = Video,
                        topics = listOf(
                            Topic(
                                id = 0,
                                name = "Headlines",
                                shortDescription = "",
                                longDescription = "",
                                url = "",
                                imageUrl = ""
                            )
                        ),
                        authors = emptyList()
                    ),
                    isSaved = false
                ),
                SaveableNewsResource(
                    newsResource = NewsResource(
                        id = 2,
                        episodeId = 52,
                        title = "Transformations and customisations in the Paging Library",
                        content = "A demonstration of different operations that can be performed " +
                            "with Paging. Transformations like inserting separators, when to " +
                            "create a new pager, and customisation options for consuming " +
                            "PagingData.",
                        url = "https://youtu.be/ZARz0pjm5YM",
                        headerImageUrl = "https://i.ytimg.com/vi/ZARz0pjm5YM/maxresdefault.jpg",
                        publishDate = Instant.parse("2021-11-01T00:00:00.000Z"),
                        type = Video,
                        topics = listOf(
                            Topic(
                                id = 1,
                                name = "UI",
                                shortDescription = "",
                                longDescription = "",
                                url = "",
                                imageUrl = ""
                            ),
                        ),
                        authors = emptyList()
                    ),
                    isSaved = false
                ),
                SaveableNewsResource(
                    newsResource = NewsResource(
                        id = 3,
                        episodeId = 52,
                        title = "Community tip on Paging",
                        content = "Tips for using the Paging library from the developer community",
                        url = "https://youtu.be/r5JgIyS3t3s",
                        headerImageUrl = "https://i.ytimg.com/vi/r5JgIyS3t3s/maxresdefault.jpg",
                        publishDate = Instant.parse("2021-11-08T00:00:00.000Z"),
                        type = Video,
                        topics = listOf(
                            Topic(
                                id = 1,
                                name = "UI",
                                shortDescription = "",
                                longDescription = "",
                                url = "",
                                imageUrl = ""
                            ),
                        ),
                        authors = emptyList()
                    ),
                    isSaved = false
                ),
            )
        ),
        onTopicCheckedChanged = { _, _ -> },
        saveFollowedTopics = {},
        onNewsResourcesCheckedChanged = { _, _ -> }
    )
}

@Preview
@Composable
fun PopulatedFeed() {
    ForYouScreen(
        uiState = FeedWithoutTopicSelection(
            feed = emptyList()
        ),
        onTopicCheckedChanged = { _, _ -> },
        saveFollowedTopics = {},
        onNewsResourcesCheckedChanged = { _, _ -> }
    )
}
