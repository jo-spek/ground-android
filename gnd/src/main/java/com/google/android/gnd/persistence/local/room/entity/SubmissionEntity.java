/*
 * Copyright 2019 Google LLC
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

package com.google.android.gnd.persistence.local.room.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.google.android.gnd.model.AuditInfo;
import com.google.android.gnd.model.feature.Feature;
import com.google.android.gnd.model.form.Form;
import com.google.android.gnd.model.mutation.SubmissionMutation;
import com.google.android.gnd.model.submission.ResponseMap;
import com.google.android.gnd.model.submission.Submission;
import com.google.android.gnd.persistence.local.LocalDataConsistencyException;
import com.google.android.gnd.persistence.local.room.converter.ResponseMapConverter;
import com.google.android.gnd.persistence.local.room.models.EntityState;
import com.google.auto.value.AutoValue;
import com.google.auto.value.AutoValue.CopyAnnotations;

/**
 * Representation of a {@link Submission} in local db.
 */
@AutoValue
@Entity(
    foreignKeys =
    @ForeignKey(
        entity = FeatureEntity.class,
        parentColumns = "id",
        childColumns = "feature_id",
        onDelete = CASCADE),
    tableName = "submission",
    // Additional index not required for FK constraint since first field in composite index can be
    // used independently.
    indices = {@Index({"feature_id", "form_id", "state"})})
public abstract class SubmissionEntity {

  @CopyAnnotations
  @PrimaryKey
  @ColumnInfo(name = "id")
  @NonNull
  public abstract String getId();

  /**
   * Returns the id of the feature to which this submission applies.
   */
  @CopyAnnotations
  @ColumnInfo(name = "feature_id")
  @NonNull
  public abstract String getFeatureId();

  /**
   * Returns the id of the form to which this submission's responses apply.
   */
  @CopyAnnotations
  @ColumnInfo(name = "form_id")
  @NonNull
  public abstract String getFormId();

  @CopyAnnotations
  @ColumnInfo(name = "state")
  @NonNull
  public abstract EntityState getState();

  /**
   * Returns a JSON object containing user responses keyed by their respective elementId in the form
   * identified by formId. Returns null if no responses have been provided.
   */
  @CopyAnnotations
  @ColumnInfo(name = "responses")
  @Nullable
  public abstract String getResponses();

  @CopyAnnotations
  @NonNull
  @Embedded(prefix = "created_")
  public abstract AuditInfoEntity getCreated();

  @CopyAnnotations
  @NonNull
  @Embedded(prefix = "modified_")
  public abstract AuditInfoEntity getLastModified();

  public static SubmissionEntity fromSubmission(Submission submission) {
    return SubmissionEntity.builder()
        .setId(submission.getId())
        .setFormId(submission.getForm().getId())
        .setFeatureId(submission.getFeature().getId())
        .setState(EntityState.DEFAULT)
        .setResponses(ResponseMapConverter.toString(submission.getResponses()))
        .setCreated(AuditInfoEntity.fromObject(submission.getCreated()))
        .setLastModified(AuditInfoEntity.fromObject(submission.getLastModified()))
        .build();
  }

  public static SubmissionEntity fromMutation(SubmissionMutation mutation, AuditInfo created) {
    AuditInfoEntity authInfo = AuditInfoEntity.fromObject(created);
    return SubmissionEntity.builder()
        .setId(mutation.getSubmissionId())
        .setFormId(mutation.getForm().getId())
        .setFeatureId(mutation.getFeatureId())
        .setState(EntityState.DEFAULT)
        .setResponses(
            ResponseMapConverter.toString(
                ResponseMap.builder().applyDeltas(mutation.getResponseDeltas()).build()))
        .setCreated(authInfo)
        .setLastModified(authInfo)
        .build();
  }

  public static Submission toSubmission(Feature feature, SubmissionEntity submission) {
    String id = submission.getId();
    String formId = submission.getFormId();
    Form form =
        feature
            .getJob()
            .getForm(formId)
            .orElseThrow(
                () ->
                    new LocalDataConsistencyException(
                        "Unknown formId " + formId + " in submission " + id));
    return Submission.newBuilder()
        .setId(id)
        .setForm(form)
        .setSurvey(feature.getSurvey())
        .setFeature(feature)
        .setResponses(ResponseMapConverter.fromString(form, submission.getResponses()))
        .setCreated(AuditInfoEntity.toObject(submission.getCreated()))
        .setLastModified(AuditInfoEntity.toObject(submission.getLastModified()))
        .build();
  }

  // Boilerplate generated using Android Studio AutoValue plugin:

  public static SubmissionEntity create(
      String id,
      String featureId,
      String formId,
      EntityState state,
      String responses,
      AuditInfoEntity created,
      AuditInfoEntity lastModified) {
    return builder()
        .setId(id)
        .setFeatureId(featureId)
        .setFormId(formId)
        .setState(state)
        .setResponses(responses)
        .setCreated(created)
        .setLastModified(lastModified)
        .build();
  }

  public static Builder builder() {
    return new AutoValue_SubmissionEntity.Builder();
  }

  public abstract SubmissionEntity.Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String newId);

    public abstract Builder setFeatureId(String newFeatureId);

    public abstract Builder setFormId(String newFormId);

    public abstract Builder setState(EntityState newState);

    public abstract Builder setResponses(@Nullable String newResponses);

    public abstract Builder setCreated(AuditInfoEntity newCreated);

    public abstract Builder setLastModified(AuditInfoEntity newLastModified);

    public abstract SubmissionEntity build();
  }
}