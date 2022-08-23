/*
 * Copyright 2018 Google LLC
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

package com.google.android.ground.model.submission;

import com.google.android.ground.model.AuditInfo;
import com.google.android.ground.model.job.Job;
import com.google.android.ground.model.locationofinterest.LocationOfInterest;
import com.google.auto.value.AutoValue;

/** Represents a single instance of data collected about a specific {@link LocationOfInterest}. */
@AutoValue
public abstract class Submission {

  public abstract String getId();

  public abstract String getSurveyId();

  public abstract LocationOfInterest getLocationOfInterest();

  public abstract Job getJob();

  /** Returns the user and time audit info pertaining to the creation of this submission. */
  public abstract AuditInfo getCreated();

  /**
   * Returns the user and time audit info pertaining to the last modification of this submission.
   */
  public abstract AuditInfo getLastModified();

  public abstract ResponseMap getResponses();

  public static Builder newBuilder() {
    return new AutoValue_Submission.Builder().setResponses(ResponseMap.builder().build());
  }

  public abstract Submission.Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String newId);

    public abstract Builder setSurveyId(String surveyId);

    public abstract Builder setLocationOfInterest(LocationOfInterest locationOfInterest);

    public abstract Builder setJob(Job job);

    public abstract Builder setCreated(AuditInfo newCreated);

    public abstract Builder setLastModified(AuditInfo newLastModified);

    public abstract Builder setResponses(ResponseMap responses);

    public abstract Submission build();
  }
}