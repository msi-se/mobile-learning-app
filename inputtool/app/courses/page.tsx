"use client"

import { Button } from "@/components/ui/button";
import { useRouter } from 'next/navigation'
import { useState, useEffect } from "react";
import { Loader2 } from 'lucide-react';
import { hasValidJwtToken, login } from "@/lib/utils";
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
import { addCourse, listCourses } from "@/lib/requests";
import { toast } from "sonner";

export default function Courses() {

  const router = useRouter();
  const [loading, setLoading] = useState(true);

  const [courses, setCourses] = useState<Course[]>([]);
  useEffect(() => {
    const loadCourses = async () => {
      setLoading(true);
      let courses = await listCourses();
      setCourses(courses);
      setLoading(false);
    };
    loadCourses();
  }, []);

  useEffect(() => {
    hasValidJwtToken().then((isValid) => {
      if (!isValid) router.push("/");
    });
  }, [router]);

  return (
    <div className="flex flex-col items-center justify-center h-max m-4">

      <Button
        variant="secondary"
        className="mb-4 self-start text-sm"
        onClick={async () => {
          // hacky way to logout (next cache issue)
          localStorage.removeItem("jwtToken");
          let currentUrl = window.location.href;
          currentUrl = currentUrl.replace("courses", "");
          window.location.href = currentUrl;
        }}
      >Logout</Button>

      {loading && (
        <Loader2 className="w-6 h-6 animate-spin" />
      )}

      {!loading && (
        <>
          <h1 className="text-2xl font-bold mb-4">Courses</h1>
          <Table>
            <TableHeader className="bg-gray-100">
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Description</TableHead>
                <TableHead>Last Modified</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {courses.map((course) => (
                <TableRow key={course.id} className="hover:cursor-pointer" onClick={() => router.push(`/courses/${course.id}`)}>
                  <TableCell className="font-medium">{course.name}</TableCell>
                  <TableCell>{course.description}</TableCell>
                  <TableCell>{new Date(course.lastModified).toLocaleString()}</TableCell>
                </TableRow>
              ))}
              {courses.length === 0 && (
                <TableRow>
                  <TableCell colSpan={2}>No courses found.</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
          <div className="flex flex-col items-stretch justify-center">
            <Button
              className="mt-4"
              onClick={async () => {
                const course = await addCourse("New course", "", "");
                if (course) {
                  toast.success("Course created.");
                  router.push(`/courses/${course.id}?is-new=true`);
                }
              }}
            >Create new Course</Button>
          </div>
        </>
      )}
    </div>
  );
}