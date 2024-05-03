"use client"

import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@radix-ui/react-dropdown-menu";
import Image from "next/image";
import { useRouter } from 'next/navigation'
import { useState, useEffect } from "react";
import { toast } from "sonner";
import { Loader2 } from 'lucide-react';
import { fetchCourse, hasValidJwtToken, login } from "@/lib/utils";
import * as React from "react"
import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Course } from "@/lib/models";

export default function CoursePage({ params }: { params: { id: string } }) {

  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);

  const [courseName, setCourseName] = useState("");
  const [courseDescription, setCourseDescription] = useState("");
  const [somethingHasChanged, setSomethingHasChanged] = useState(false);

  const [course, setCourse] = useState<Course>();
  useEffect(() => {
    const loadCourse = async () => {
      setLoading(true);
      let course = await fetchCourse(params.id);
      if (!course) {
        toast.error("Course not found.");
        router.push("/courses");
        return;
      }
      setCourse(course);
      setCourseName(course.name);
      setCourseDescription(course.description);
      setLoading(false);
    };
    loadCourse();
  }, []);

  useEffect(() => {
    setMounted(true);
    hasValidJwtToken().then((isValid) => {
      if (!isValid) router.push("/");
    });
  }, [router]);

  if (!mounted) {
    return <> </>
  }

  return (
    <div className="flex flex-col items-center justify-center h-screen mx-4">

      {loading && (
        <Loader2 className="w-6 h-6 animate-spin" />
      )}

      {!loading && (
        <>
          <Card className="w-full">
            <CardHeader>
              <CardTitle>
                <Input
                  value={courseName}
                  onChange={(e) => {
                    setCourseName(e.target.value);
                    setSomethingHasChanged(true);
                  }}
                  placeholder="Course name"
                  className="font-bold bor"
                />
              </CardTitle>
            </CardHeader>
            <CardContent>
              <Label>Description</Label>
              <Input
                value={courseDescription}
                onChange={(e) => {
                  setCourseDescription(e.target.value);
                  setSomethingHasChanged(true);
                }}
                placeholder="Course description"
              />
              <Button
                disabled={!somethingHasChanged}
                className="mt-4"
                onClick={() => {
                  updateCourse(course?.id, courseName, courseDescription);
                }}
              >Update course</Button>
            </CardContent>
          </Card>
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
                <TableRow key={form.id} className="hover:bg-green-50 hover:cursor-pointer"
                  onClick={() => {
                    if (form.type === "Quiz") router.push(`/courses/${course?.id}/quizform/${form.id}`)
                    else router.push(`/courses/${course?.id}/feedbackform/${form.id}`)
                  }}
                >
                  <TableCell>{form.type}</TableCell>
                  <TableCell className="font-medium">{form.name}</TableCell>
                  <TableCell>{form.description}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <div className="flex flex-col items-stretch justify-center">
            <Button
              className="mt-4"
              onClick={() => router.push(`/courses/${course?.id}/newfeedbackform`)}
            >Create new feedback form</Button>
            <Button
              className="mt-4"
              onClick={() => router.push(`/courses/${course?.id}/newquizform`)}
            >Create new quiz form</Button>
          </div>
        </>
      )}
    </div>
  );
}