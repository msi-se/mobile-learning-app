"use client"

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@radix-ui/react-dropdown-menu";
import { useRouter, useSearchParams } from 'next/navigation'
import { useState, useEffect, useCallback } from "react";
import { toast } from "sonner";
import { Check, Circle, CircleArrowLeft, CircleCheck, CircleDashed, Loader2, Save } from 'lucide-react';
import { handleEnterPress, hasValidJwtToken } from "@/lib/utils";
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
import { addFeedbackForm, addQuizForm, deleteCourse, fetchCourse, updateCourse } from "@/lib/requests";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";

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
      await save(false);
    }, 2000);

    return () => {
      if (autosaveTimeout && autosaveTimeout.current) clearTimeout(autosaveTimeout.current);
    };
  }, [save, userChangedSomething]);


  // save on page leave
  useEffect(() => {
    const handleBeforeUnload = async (e: BeforeUnloadEvent) => {
      if (userChangedSomething) {
        e.preventDefault();
        await save();
        toast.info("Saved. (You should give the app a few seconds to save your changes next time.)");
      }
    };
    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [userChangedSomething, save]);

  // direct save
  useEffect(() => {
    const handleSave = async (e: KeyboardEvent) => {
      if (e.key === "s" && (e.ctrlKey || e.metaKey)) {
        e.preventDefault();
        save();
      }
    };
    document.addEventListener("keydown", handleSave);
    return () => {
      document.removeEventListener("keydown", handleSave);
    };
  }, [save]);

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
                if (userChangedSomething) save();
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
                    onKeyDown={(e) => handleEnterPress(e, save)}
                  />
                </div>
                <div className="flex flex-col flex-grow">
                  <div className="flex gap-2">
                    <Label>Moodle Course ID</Label>
                    <Dialog>
                      <DialogTrigger>
                        <div
                          className="text-gray-400 cursor-pointer"
                          title="Moodle Course ID"
                        >?</div>
                      </DialogTrigger>
                      <DialogContent>
                        <DialogHeader>
                          <DialogTitle>Moodle Course ID</DialogTitle>
                          <DialogDescription>
                            {/* https://moodle.htwg-konstanz.de/moodle/course/view.php?id=793 */}
                            By entering a Moodle Course ID, the students enrolled in the course will be added directly to your course on this platform.
                            <br />
                            You can find the ID in the Moodle URL:
                            <br />
                            <span className="text-blue-500">https://moodle.htwg-konstanz.de/moodle/course/view.php?id=</span>
                            <span className="text-blue-500 font-bold">793</span>
                          </DialogDescription>
                        </DialogHeader>
                      </DialogContent>
                    </Dialog>
                  </div>
                  <Input
                    value={courseMoodleCourseId}
                    onChange={(e) => {
                      setCourseMoodleCourseId(e.target.value);
                      setUserChangedSomething(true);
                    }}
                    placeholder="Moodle Course ID"
                    onKeyDown={(e) => handleEnterPress(e, save)}
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
                  onKeyDown={(e) => handleEnterPress(e, save)}
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
                onClick={async () => {
                  const form = await addQuizForm(course?.id || "", "New quiz", "");
                  if (form) {
                    toast.success("Quiz created.");
                    router.push(`/courses/${course?.id}/quizform/${form.id}?is-new=true`);
                  }
                }}
              >Create Quiz</Button>
            </div>
          </div>
          <Table>
            <TableHeader className="bg-gray-100">
              <TableRow>
                <TableHead>Type</TableHead>
                <TableHead>Name</TableHead>
                <TableHead>Description</TableHead>
                <TableHead>Last Modified</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody className="overflow-y-auto">
              {[...course?.feedbackForms || [], ...course?.quizForms || []].sort((a, b) => a.lastModified > b.lastModified ? -1 : 1).map(form => (
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
                  <TableCell>{new Date(form.lastModified).toLocaleString()}</TableCell>
                </TableRow>
              ))}
              {course?.feedbackForms.length === 0 && course?.quizForms.length === 0 && (
                <TableRow>
                  <TableCell colSpan={4} className="text-center">No forms found.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </>
      )}
    </div >
  );
}