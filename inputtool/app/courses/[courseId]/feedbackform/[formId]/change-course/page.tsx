"use client"

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Label } from "@radix-ui/react-dropdown-menu";
import { useRouter } from 'next/navigation'
import { useState, useEffect } from "react";
import { toast } from "sonner";
import { CircleArrowLeft, Loader2 } from 'lucide-react';
import { hasValidJwtToken } from "@/lib/utils";
import * as React from "react"
import { Course, FeedbackForm, QuizForm } from "@/lib/models";
import { changeCourseOfForm, fetchFeedbackForm, listCourses } from "@/lib/requests";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"


export default function ChangeCoursePage({ params }: { params: { courseId: string, formId: string } }) {

  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState<FeedbackForm>({
    id: "",
    name: "",
    key: "",
    type: "Feedback",
    description: "",
    questions: [],
    lastModified: new Date(),
  });
  const [courses, setCourses] = useState<Course[]>([]);
  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      let courses = await listCourses();
      let form = await fetchFeedbackForm(params.courseId, params.formId);
      console.log(form);
      setCourses(courses);
      form && setForm(form);
      setLoading(false);
    };
    loadData();
  }, [params.courseId, params.formId]);

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
                  router.push(`/courses/${params.courseId}/feedbackform/${params.formId}`);
              }}
            ><CircleArrowLeft /></Button>
            <div className="flex items-center">
              
            </div>

          </div>
          <h1 className="text-2xl mb-4 font-bold">
            Form: {form.name}
          </h1>
          <Card className="w-full">
            <CardContent>
              <Label
                className="mt-4 mb-2"
              >Select the course in which this form should be used:</Label>
              <Select
                defaultValue={params.courseId}
                onValueChange={async (value) => {
                  let result = await changeCourseOfForm(params.courseId, params.formId, value);
                  if (result) {
                    toast.success("Course changed successfully");
                    router.push(`/courses/${value}/feedbackform/${params.formId}/change-course`);
                  }
                }}
              >
                <SelectTrigger>
                  <SelectValue>{courses.find(c => c.id === params.courseId)?.name}</SelectValue>
                </SelectTrigger>
                <SelectContent>
                  {courses.map((course) => (
                    <SelectItem key={course.id} value={course.id}>
                      {course.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
}