"use client"

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@radix-ui/react-dropdown-menu";
import { useRouter, useSearchParams } from 'next/navigation'
import { useState, useEffect } from "react";
import { toast } from "sonner";
import { CircleArrowLeft, Loader2, Save } from 'lucide-react';
import { hasValidJwtToken } from "@/lib/utils";
import * as React from "react"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Course } from "@/lib/models";
import { DeleteButton } from "@/components/delete-button";
import { addFeedbackForm, deleteCourse, fetchCourse, updateCourse } from "@/lib/requests";

export default function CoursePage({ params }: { params: { courseId: string } }) {

  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [courseName, setCourseName] = useState("");
  const [courseDescription, setCourseDescription] = useState("");
  const [courseMoodleCourseId, setCourseMoodleCourseId] = useState("");
  const [somethingHasChanged, setSomethingHasChanged] = useState(false);
  const [isNew, setIsNew] = useState(false);
  const searchParams = useSearchParams()

  useEffect(() => {
    let isNew = searchParams.get("is-new") === "true"
    setIsNew(isNew);
  }, [searchParams]);

  const [course, setCourse] = useState<Course>();
  useEffect(() => {
    const loadCourse = async () => {
      setLoading(true);
      let course = await fetchCourse(params.courseId);
      if (!course) {
        toast.error("Course not found.");
        router.push("/courses");
        return;
      }
      setCourse(course);
      setCourseName(course.name);
      setCourseDescription(course.description);
      setCourseMoodleCourseId(course.moodleCourseId);
      setLoading(false);
    };
    loadCourse();
  }, [params.courseId, router]);

  useEffect(() => {
    hasValidJwtToken().then((isValid) => {
      if (!isValid) router.push("/");
    });
  }, [router]);

  return (
    <div className="flex flex-col items-center justify-center h-max m-4">

      {loading && (
        <Loader2 className="w-6 h-6 animate-spin" />
      )}

      {!loading && (
        <>
          <Button
            variant="secondary"
            className="mb-4 self-start text-sm"
            onClick={() => router.push(`/courses`)}
          ><CircleArrowLeft /></Button>

          <h1 className="text-2xl mb-4 font-bold">
            Course: {courseName}
          </h1>
          <Card className="w-full">
            <CardContent>
              <Label className="mt-6">Name</Label>
                <Input
                  autoFocus={isNew}
                  onFocus={(e) => {
                    if (isNew) e.target.select();
                  }}
                  value={courseName}
                  onChange={(e) => {
                    setCourseName(e.target.value);
                    setSomethingHasChanged(true);
                  }}
                  placeholder="Course name"
                  className="font-bold bor"
                />
              <Label className="mt-2">Description</Label>
              <Input
                value={courseDescription}
                onChange={(e) => {
                  setCourseDescription(e.target.value);
                  setSomethingHasChanged(true);
                }}
                placeholder="Course description"
              />
              <Label className="mt-2">Moodle Course ID</Label>
              <Input
                value={courseMoodleCourseId}
                onChange={(e) => {
                  setCourseMoodleCourseId(e.target.value);
                  setSomethingHasChanged(true);
                }}
                placeholder="Moodle course ID"
              />
              {/* first button at the left second at the right */}
              <div className="flex justify-between">
                <Button
                  disabled={!somethingHasChanged}
                  className="mt-4"
                  onClick={async () => {
                    const result = await updateCourse(params.courseId, courseName, courseDescription, courseMoodleCourseId);
                    if (result) {
                      setSomethingHasChanged(false);
                      toast.success("Course updated.");
                      setCourseName(result.name);
                      setCourseDescription(result.description);
                      setCourseMoodleCourseId(result.moodleCourseId);
                    } else {
                      toast.error("Course update failed.");
                    }
                  }}
                ><Save /></Button>
                <DeleteButton
                  className="mt-4"
                  onDelete={async () => {
                    const result = await deleteCourse(params.courseId);
                    if (result) {
                      toast.success("Course deleted.");
                      router.push("/courses");
                    }
                  }}
                />
              </div>
            </CardContent>
          </Card>
          <h2 className="text-2xl mt-4">Feedback and Quiz Forms</h2>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Type</TableHead>
                <TableHead>Name</TableHead>
                <TableHead>Description</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {[...course?.feedbackForms || [], ...course?.quizForms || []].map((form) => (
                <TableRow key={form.id} className="hover:cursor-pointer"
                  onClick={() => {
                    toast.dismiss();
                    if (form.type === "Quiz") router.push(`/courses/${course?.id}/quizform/${form.id}`)
                    else router.push(`/courses/${course?.id}/feedbackform/${form.id}`)
                  }}
                >
                  <TableCell>{form.type}</TableCell>
                  <TableCell className="font-medium">{form.name}</TableCell>
                  <TableCell>{form.description}</TableCell>
                </TableRow>
              ))}
              {course?.feedbackForms.length === 0 && course?.quizForms.length === 0 && (
                <TableRow>
                  <TableCell colSpan={3} className="text-center">No forms found.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
          <div className="flex flex-col items-stretch justify-center">
            <Button
              className="mt-4"
              onClick={async () => {
                const form = await addFeedbackForm(course?.id || "", "New feedback form", "");
                if (form) {
                  toast.success("Feedback form created.");
                  router.push(`/courses/${course?.id}/feedbackform/${form.id}?is-new=true`);
                }}
              }
            >Create new Feedback Form</Button>
            <Button
              className="mt-4"
              disabled={true}
            >Create new Quiz Form</Button>
          </div>
        </>
      )}
    </div>
  );
}