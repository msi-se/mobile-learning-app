package de.htwg_konstanz.mobilelearning.repositories.projectionclasses;

import java.io.Serializable;
import java.util.List;

import org.bson.types.ObjectId;

import de.htwg_konstanz.mobilelearning.models.Course;
import io.quarkus.mongodb.panache.common.ProjectionFor;

@ProjectionFor(Course.class)
public record CourseWithoutForms(
        ObjectId id,
        String name,
        String description,
        List<ObjectId> owners,
        List<ObjectId> students,
        String key,
        String moodleCourseId) implements Serializable {
}