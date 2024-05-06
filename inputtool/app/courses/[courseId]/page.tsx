"use client"

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@radix-ui/react-dropdown-menu";
import { useRouter, useSearchParams } from 'next/navigation'
import { useState, useEffect, useCallback } from "react";
import { toast } from "sonner";
import { Check, Circle, CircleArrowLeft, CircleCheck, CircleDashed, Loader2, Save } from 'lucide-react';
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
  const [isNew, setIsNew] = useState(false);
  const [userChangedSomething, setUserChangedSomething] = useState(false);
  const searchParams = useSearchParams()

  useEffect(() => {
    let isNew = searchParams.get("is-new") === "true"
    setIsNew(isNew);
  }, [searchParams]);

  const [course, setCourse] = useState<Course>();

  const save = useCallback(async (logSuccess: boolean = true) => {
    const result = await updateCourse(params.courseId, courseName, courseDescription, courseMoodleCourseId);
    if (result) {
      setCourseName(result.name);
      setCourseDescription(result.description);
      setCourseMoodleCourseId(result.moodleCourseId);
      setUserChangedSomething(false);

      if (logSuccess) {
        toast.success("Saved.");
      }

    } else {
      toast.error("Failed to save.");
    }
  }, [courseDescription, courseMoodleCourseId, courseName, params.courseId]);

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


  // setup autosave
  let autosaveTimeout = React.useRef<NodeJS.Timeout | null>(null);
  useEffect(() => {
    if (autosaveTimeout && autosaveTimeout.current) clearTimeout(autosaveTimeout.current);
    if (!userChangedSomething) return;

    autosaveTimeout.current = setTimeout(async () => {
      console.log("Autosaving...");
      await save(false);
    }, 2000);

    return () => {
      if (autosaveTimeout && autosaveTimeout.current) clearTimeout(autosaveTimeout.current);
    };
  }, [courseName, courseDescription, courseMoodleCourseId, save, userChangedSomething]);


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
          <div className="flex justify-between w-full">

            <Button
              variant="secondary"
              className="mb-4 self-start text-sm"
              onClick={() => {
                save();
                router.push(`/courses`)
              }}
            >
              <CircleArrowLeft />
            </Button>
            <div className="flex items-center">
              <DeleteButton
                className="mb-4 self-end"
                onDelete={async () => {
                  const result = await deleteCourse(params.courseId);
                  if (result) {
                    toast.success("Course deleted.");
                    router.push("/courses");
                  }
                }}
              />
              <Button
                variant="secondary"
                id="saveIndicator"
                aria-label="Save"
                title="Save"
                onClick={async () => {

                  if (!userChangedSomething) {
                    toast.success("Already saved.");
                  } else {
                    await save();
                  }
                }}
                className="mb-4 self-end text-sm ml-4"
              >

                {userChangedSomething && (
                  <CircleDashed className="w-6 h-6" />
                )}

                {loading && !userChangedSomething && (
                  <Loader2 className="w-6 h-6 animate-spin" />
                )}

                {!userChangedSomething && (
                  <CircleCheck className="w-6 h-6" />
                )}

              </Button>

            </div>

          </div>
          <h1 className="text-2xl mb-4 font-bold">
            Course: {courseName}
          </h1>
          <Card className="w-full">
            <CardContent>
              <div className="flex flex-wrap gap-4 mt-6 w-full justify-stretch">
                <div className="flex flex-col flex-grow">
                  <Label className="">Name</Label>
                  <Input
                    autoFocus={isNew}
                    onFocus={(e) => {
                      if (isNew) e.target.select();
                    }}
                    value={courseName}
                    onChange={(e) => {
                      setCourseName(e.target.value);
                      setUserChangedSomething(true);
                    }}
                    placeholder="Course name"
                    className="font-bold"
                  />
                </div>
                <div className="flex flex-col flex-grow">
                  <Label>Moodle Course ID</Label>
                  <Input
                    value={courseMoodleCourseId}
                    onChange={(e) => {
                      setCourseMoodleCourseId(e.target.value);
                      setUserChangedSomething(true);
                    }}
                    placeholder="Moodle Course ID"
                  />
                </div>
              </div>
              <div>
                <Label className="mt-2">Description</Label>
                <Input
                  value={courseDescription}
                  onChange={(e) => {
                    setCourseDescription(e.target.value);
                    setUserChangedSomething(true);
                  }}
                  placeholder="Course Description"
                />
              </div>

            </CardContent>
          </Card>
          <div className="flex justify-between w-full mb-4 mt-8 flex-grow flex-wrap gap-4">
            <h2 className="text-2xl">Feedbacks and Quizzes</h2>
            <div className="flex gap-4 justify-end">
              <Button
                className="flex flex-col self-end"
                onClick={async () => {
                  const form = await addFeedbackForm(course?.id || "", "New feedback form", "");
                  if (form) {
                    toast.success("Feedback form created.");
                    router.push(`/courses/${course?.id}/feedbackform/${form.id}?is-new=true`);
                  }
                }}
              >Create Feedback</Button>
              <Button
                className="self-end flex flex-col"
                disabled={true}
              >Create Quiz</Button>
            </div>
          </div>
          <Table>
            <TableHeader className="bg-gray-100">
              <TableRow>
                <TableHead>Type</TableHead>
                <TableHead>Name</TableHead>
                <TableHead>Description</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody className="overflow-y-auto">
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
        </>
      )
      }
    </div >
  );
}