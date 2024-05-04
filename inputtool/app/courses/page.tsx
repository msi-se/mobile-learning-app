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
  TableCaption,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Course } from "@/lib/models";
import { addCourse, listCourses } from "@/lib/requests";
import { toast } from "sonner";

export default function Courses() {

  const router = useRouter();
  const [mounted, setMounted] = useState(false);
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
    setMounted(true);
    hasValidJwtToken().then((isValid) => {
      if (!isValid) router.push("/");
    });
  }, [router]);

  if (!mounted) {
    return <> </>
  }

  return (
    <div className="flex flex-col items-center justify-center h-max m-4">

      {loading && (
        <Loader2 className="w-6 h-6 animate-spin" />
      )}

      {!loading && (
        <>
          <h1 className="text-2xl font-bold">Courses</h1>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Description</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {courses.map((course) => (
                <TableRow key={course.id} className="hover:bg-green-50 hover:cursor-pointer" onClick={() => router.push(`/courses/${course.id}`)}>
                  <TableCell className="font-medium">{course.name}</TableCell>
                  <TableCell>{course.description}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <div className="flex flex-col items-stretch justify-center">
            <Button
              className="mt-4"
              onClick={async () => {
                const course = await addCourse("New course", "", "");
                if (course) {
                  setCourses([...courses, course]);
                  toast.success("Course created.");
                  router.push(`/courses/${course.id}`);
                }
              }}
            >Create new course</Button>
          </div>
        </>
      )}
    </div>
  );
}