"use client"

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@radix-ui/react-dropdown-menu";
import { useRouter, useSearchParams } from 'next/navigation'
import { useState, useEffect, useCallback } from "react";
import { toast } from "sonner";
import { CircleArrowLeft, CircleCheck, CircleDashed, Loader2, Save } from 'lucide-react';
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
import { FeedbackForm } from "@/lib/models";
import { DeleteButton } from "@/components/delete-button";
import { addFeedbackQuestion, copyFeedbackForm, deleteFeedbackForm, fetchFeedbackForm, updateFeedbackForm } from "@/lib/requests";
import getBackendUrl from "@/lib/get-backend-url";
import Link from "next/link";

export default function FeedbackFormPage({ params }: { params: { courseId: string, formId: string } }) {

  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [feedbackformName, setFeedbackFormName] = useState("");
  const [feedbackformDescription, setFeedbackFormDescription] = useState("");
  const [userChangedSomething, setUserChangedSomething] = useState(false);
  const [backendUrl, setBackendUrl] = useState("");
  const [isNew, setIsNew] = useState(false);
  const searchParams = useSearchParams()

  useEffect(() => {
    let isNew = searchParams.get("is-new") === "true"
    setIsNew(isNew);
  }, [searchParams]);

  const [feedbackform, setFeedbackForm] = useState<FeedbackForm>();


  const save = useCallback(async (logSuccess: boolean = true) => {
    const result = await updateFeedbackForm(params.courseId, params.formId, feedbackformName, feedbackformDescription);
    if (result) {
      setFeedbackFormName(result.name);
      setFeedbackFormDescription(result.description);
      setUserChangedSomething(false);

      if (logSuccess) {
        toast.success("Saved.");
      }

    } else {
      toast.error("Failed to save.");
    }
  }, [feedbackformDescription, feedbackformName, params.courseId, params.formId]);

  useEffect(() => {
    const loadFeedbackForm = async () => {
      setLoading(true);
      let feedbackform = await fetchFeedbackForm(params.courseId, params.formId);
      if (!feedbackform) {
        toast.error("FeedbackForm not found.");
        router.back();
        return;
      }
      setFeedbackForm(feedbackform);
      setFeedbackFormName(feedbackform.name);
      setFeedbackFormDescription(feedbackform.description);

      let backendUrl = await getBackendUrl();
      setBackendUrl(backendUrl || "");
      console.log(backendUrl);

      setLoading(false);
    };
    loadFeedbackForm();
  }, [params.courseId, params.formId, router]);

  // setup autosave
  let autosaveTimeout = React.useRef<NodeJS.Timeout | null>(null);
  useEffect(() => {
    if (autosaveTimeout && autosaveTimeout.current) clearTimeout(autosaveTimeout.current);
    if (!userChangedSomething) return;

    autosaveTimeout.current = setTimeout(() => {
      save(false);
    }, 2000);
  }, [save, userChangedSomething]);

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
                router.push(`/courses/${params.courseId}`);
              }}
            ><CircleArrowLeft /></Button>
            <div className="flex items-center">
              <Button
                className="mb-4 self-end ml-4"
                variant="outline"
                onClick={async () => {
                  const result = await copyFeedbackForm(params.courseId, params.formId);
                  if (result) {
                    toast.success("Feedback Form Copied.");
                    router.push(`/courses/${params.courseId}/feedbackform/${result.id}?is-new=true`);
                  }
                }}
              >Copy Form</Button>
              <DeleteButton
                className="mb-4 self-end ml-4"
                onDelete={async () => {
                  const result = await deleteFeedbackForm(params.courseId, params.formId);
                  if (result) {
                    toast.success("FeedbackForm deleted.");
                    router.push(`/courses/${params.courseId}`);
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
            Feedback-Form: {feedbackformName}
          </h1>
          <Card className="w-full">
            <CardContent>
              <Label className="mt-6">Name</Label>
              <Input
                autoFocus={isNew}
                onFocus={(e) => {
                  if (isNew) e.target.select();
                }}
                value={feedbackformName}
                onChange={(e) => {
                  setFeedbackFormName(e.target.value);
                  setUserChangedSomething(true);
                }}
                placeholder="FeedbackForm name"
                className="font-bold bor"
              />
              <Label className="mt-2">Description</Label>
              <Input
                value={feedbackformDescription}
                onChange={(e) => {
                  setFeedbackFormDescription(e.target.value);
                  setUserChangedSomething(true);
                }}
                placeholder="FeedbackForm description"
              />
            </CardContent>
          </Card>
          <div className="flex justify-between w-full mb-4 mt-8 flex-grow flex-wrap gap-4">
            <h2 className="text-2xl">Questions</h2>
            <div className="flex gap-4 justify-end">
              <Button
                className="flex flex-col self-end"
                variant="outline"
              >
                <Link href={`${backendUrl}/course/${params.courseId}/feedback/form/${params.formId}/downloadresults?token=${localStorage.getItem("jwtToken")}`}>
                  Download Results
                </Link>
              </Button>
              <Button
                className="flex flex-col self-end"
                onClick={async () => {
                  const question = await addFeedbackQuestion(params.courseId, params.formId, "New question", "", "SLIDER", [], "", "");
                  if (question) {
                    toast.success("Question created.");
                    router.push(`/courses/${params.courseId}/feedbackform/${params.formId}/question/${question.id}?is-new=true`);
                  }
                }}
              >Add New Question</Button>
            </div>
          </div>
          <Table>
            <TableHeader className="bg-gray-100">
              <TableRow>
                <TableHead>Type</TableHead>
                <TableHead>Name</TableHead>
                <TableHead>Description</TableHead>
                <TableHead>Options</TableHead>
                <TableHead>RangeLow</TableHead>
                <TableHead>RangeHigh</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {feedbackform?.questions.map((question) => (
                <TableRow key={question.id} className="hover:cursor-pointer"
                  onClick={() => {
                    router.push(`/courses/${params.courseId}/feedbackform/${params.formId}/question/${question.id}`)
                  }}
                >
                  <TableCell>{question.type}</TableCell>
                  <TableCell className="font-medium">{question.name}</TableCell>
                  <TableCell>{question.description}</TableCell>
                  <TableCell>{question.options?.join(", ")}</TableCell>
                  <TableCell>{question.rangeLow}</TableCell>
                  <TableCell>{question.rangeHigh}</TableCell>
                </TableRow>
              ))}
              {feedbackform?.questions.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} className="text-center">No questions found</TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </>
      )}
    </div>
  );
}