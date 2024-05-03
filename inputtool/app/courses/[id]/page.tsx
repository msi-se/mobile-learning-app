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
          <Card className="w-[350px]">
            <CardHeader>
              <CardTitle>{course?.name}</CardTitle>
            </CardHeader>
            <CardContent>
              <p>{course?.description}</p>
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
                <TableRow key={form.id} className="hover:bg-green-50 hover:cursor-pointer" onClick={() => router.push(`/courses/${course?.id}/feedbackform/${form.id}`)}>
                  <TableCell>{form.type}</TableCell>
                  <TableCell className="font-medium">{form.name}</TableCell>
                  <TableCell>{form.description}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </>
      )}
    </div>
  );
}