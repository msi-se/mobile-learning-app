"use client"

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@radix-ui/react-dropdown-menu";
import { useRouter } from 'next/navigation'
import { useState, useEffect } from "react";
import { toast } from "sonner";
import { CircleArrowLeft, Loader2 } from 'lucide-react';
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
import { addFeedbackQuestion, deleteFeedbackForm, fetchFeedbackForm, updateFeedbackForm } from "@/lib/requests";

export default function FeedbackFormPage({ params }: { params: { courseId: string, formId: string } }) {

  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);

  const [feedbackformName, setFeedbackFormName] = useState("");
  const [feedbackformDescription, setFeedbackFormDescription] = useState("");
  const [somethingHasChanged, setSomethingHasChanged] = useState(false);

  const [feedbackform, setFeedbackForm] = useState<FeedbackForm>();
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
      setLoading(false);
    };
    loadFeedbackForm();
  }, [params.courseId, params.formId, router]);

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
          <Button
            className="mb-4 self-start text-sm"
            onClick={() => router.push(`/courses/${params.courseId}`)}
          ><CircleArrowLeft /></Button>
          <h1 className="text-2xl mb-4 font-bold">
            Feedback-Form: {feedbackformName}
          </h1>
          <Card className="w-full">
            <CardContent>
              <Label className="mt-6">Name</Label>
              <Input
                autoFocus
                value={feedbackformName}
                onChange={(e) => {
                  setFeedbackFormName(e.target.value);
                  setSomethingHasChanged(true);
                }}
                placeholder="FeedbackForm name"
                className="font-bold bor"
              />
              <Label className="mt-2">Description</Label>
              <Input
                value={feedbackformDescription}
                onChange={(e) => {
                  setFeedbackFormDescription(e.target.value);
                  setSomethingHasChanged(true);
                }}
                placeholder="FeedbackForm description"
              />
              {/* first button at the left second at the right */}
              <div className="flex justify-between">
                <Button
                  disabled={!somethingHasChanged}
                  className="mt-4"
                  onClick={async () => {
                    const result = await updateFeedbackForm(params.courseId, params.formId, feedbackformName, feedbackformDescription);
                    if (result) {
                      setSomethingHasChanged(false);
                      toast.success("FeedbackForm updated.");
                      setFeedbackFormName(feedbackformName);
                      setFeedbackFormDescription(feedbackformDescription);
                    } else {
                      toast.error("FeedbackForm could not be updated.");
                    }

                  }}
                >Update feedbackform</Button>
                <DeleteButton
                  className="mt-4"
                  onDelete={async () => {
                    const result = await deleteFeedbackForm(params.courseId, params.formId);
                    if (result) {
                      router.push(`/courses/${params.courseId}`);
                      toast.success("FeedbackForm deleted.");
                    }
                  }}
                />
              </div>
            </CardContent>
          </Card>
          <h2 className="text-2xl mt-4">Questions</h2>
          <Table>
            <TableHeader>
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
                <TableRow key={question.id} className="hover:bg-green-50 hover:cursor-pointer"
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
            </TableBody>
          </Table>
          <div className="flex flex-col items-stretch justify-center">
            <Button
              className="mt-4"
              onClick={async () => {
                const question = await addFeedbackQuestion(params.courseId, params.formId, "New question", "", "SLIDER", [], "", "");
                if (question) {
                  router.push(`/courses/${params.courseId}/feedbackform/${params.formId}/question/${question.id}`);
                  // toast.success("Question created."); (crashes the app)
                }
              }}
            >Add new question</Button>
          </div>
        </>
      )}
    </div>
  );
}