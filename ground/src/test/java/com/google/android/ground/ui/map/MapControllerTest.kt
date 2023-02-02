/*
 * Copyright 2022 Google LLC
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
package com.google.android.ground.ui.map

import android.location.Location
import com.google.android.ground.BaseHiltTest
import com.google.android.ground.model.geometry.Coordinate
import com.google.android.ground.repository.MapStateRepository
import com.google.android.ground.repository.SurveyRepository
import com.sharedtest.FakeData
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Flowable
import java8.util.Optional
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class MapControllerTest : BaseHiltTest() {
  @Mock lateinit var locationController: LocationController
  @Mock lateinit var surveyRepository: SurveyRepository
  @Mock lateinit var mapStateRepository: MapStateRepository

  private lateinit var mapController: MapController

  @Before
  override fun setUp() {
    super.setUp()
    mapController = MapController(locationController, surveyRepository, mapStateRepository)
  }

  @Test
  fun testGetCameraUpdates_returnsNothing() {
    `when`(locationController.getLocationUpdates()).thenReturn(Flowable.empty())
    `when`(surveyRepository.activeSurvey).thenReturn(Flowable.empty())

    mapController.getCameraUpdates().test().assertValueCount(0)
  }

  @Test
  fun testGetCameraUpdates_whenPanAndZoomCamera() {
    `when`(locationController.getLocationUpdates()).thenReturn(Flowable.empty())
    `when`(surveyRepository.activeSurvey).thenReturn(Flowable.empty())

    val cameraUpdatesSubscriber = mapController.getCameraUpdates().test()

    mapController.panAndZoomCamera(TEST_COORDINATE)

    cameraUpdatesSubscriber.assertValues(CameraPosition(TEST_COORDINATE, 18.0f))
  }

  @Test
  fun testGetCameraUpdates_whenLocationUpdates() {
    `when`(surveyRepository.activeSurvey).thenReturn(Flowable.empty())
    `when`(locationController.getLocationUpdates())
      .thenReturn(
        Flowable.just(
          Location("test provider").apply {
            latitude = TEST_COORDINATE.x
            longitude = TEST_COORDINATE.y
          }
        )
      )

    mapController.getCameraUpdates().test().assertValues(CameraPosition(TEST_COORDINATE, 18.0f))
  }

  @Test
  fun testGetCameraUpdates_whenSurveyChanges_whenLastLocationNotAvailable_returnsEmpty() {
    `when`(locationController.getLocationUpdates()).thenReturn(Flowable.empty())
    `when`(surveyRepository.activeSurvey).thenReturn(Flowable.just(TEST_SURVEY))
    `when`(mapStateRepository.getCameraPosition(any())).thenReturn(null)

    mapController.getCameraUpdates().test().assertEmpty()
  }

  @Test
  fun testGetCameraUpdates_whenSurveyChanges_whenLastLocationAvailable() {
    `when`(locationController.getLocationUpdates()).thenReturn(Flowable.empty())
    `when`(surveyRepository.activeSurvey).thenReturn(Flowable.just(TEST_SURVEY))
    `when`(mapStateRepository.getCameraPosition(any())).thenReturn(TEST_POSITION)

    mapController.getCameraUpdates().test().assertValues(TEST_POSITION.copy(isAllowZoomOut = true))
  }

  companion object {
    private val TEST_COORDINATE = Coordinate(20.0, 30.0)
    private val TEST_POSITION = CameraPosition(TEST_COORDINATE)
    private val TEST_SURVEY = Optional.of(FakeData.SURVEY)
  }
}
