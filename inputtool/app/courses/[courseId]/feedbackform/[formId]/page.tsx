"use client"

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@radix-ui/react-dropdown-menu";
import { useRouter, useSearchParams } from 'next/navigation'
import { useState, useEffect, useCallback } from "react";
import { toast } from "sonner";
import { CircleArrowLeft, CircleCheck, CircleDashed, Loader2, Save, SlidersHorizontal, SquareArrowDown, SquareArrowUp, SquareCheckBig, Star, TextCursorInput } from 'lucide-react';
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
import { FeedbackForm } from "@/lib/models";
import { DeleteButton } from "@/components/delete-button";
import { addFeedbackQuestion, copyFeedbackForm, deleteFeedbackForm, fetchFeedbackForm, reorderFeedbackFormQuestions, updateFeedbackForm } from "@/lib/requests";
import getBackendUrl from "@/lib/get-backend-url";
import Link from "next/link";

export default function FeedbackFormPage({ params }: { params: { courseId: string, formId: string } }) {

  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [userChangedSomething, setUserChangedSomething] = useState(false);
  const [backendUrl, setBackendUrl] = useState("");
  const [isNew, setIsNew] = useState(false);
  const [hoversOnCreateRow, setHoversOnCreateRow] = useState(false);
  const searchParams = useSearchParams()
  const [userChangedOrder, setUserChangedOrder] = useState(false);

  useEffect(() => {
    let isNew = searchParams.get("is-new") === "true"
    setIsNew(isNew);
  }, [searchParams]);

  const [feedbackform, setFeedbackForm] = useState<FeedbackForm>({
    id: "",
    name: "",
    key: "",
    type: "Feedback",
    description: "",
    questions: []
  });

  const save = useCallback(async (logSuccess: boolean = true, orderChanged: boolean = false) => {
    if (orderChanged) {
      const result = await reorderFeedbackFormQuestions(params.courseId, params.formId, feedbackform.questions.map(q => q.id));
      if (result) {
        setFeedbackForm({ ...feedbackform, questions: result.questions });
        setUserChangedOrder(false);
        setUserChangedSomething(false);
        if (logSuccess) {
          toast.success("Saved.");
        }
      } else {
        toast.error("Failed to save.");
      }
    } 

    const result = await updateFeedbackForm(params.courseId, params.formId, feedbackform.name, feedbackform.description);
    if (result) {

      setFeedbackForm({ ...feedbackform, name: result.name, description: result.description });
      setUserChangedSomething(false);

      if (logSuccess) {
        toast.success("Saved.");
      }

    } else {
      toast.error("Failed to save.");
    }
  }, [feedbackform, params.courseId, params.formId]);

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
      save(false, userChangedOrder);
    }, 2000);
  }, [save, userChangedOrder, userChangedSomething]);

  const moveQuestionUp = async (questionId: string) => {
    let question = feedbackform?.questions.find(q => q.id === questionId);
    if (!question) return;

    let index = feedbackform?.questions.indexOf(question);
    if (index === undefined || index === null) return;

    if (index === 0) return;

    let newQuestions = [...feedbackform?.questions || []];
    newQuestions.splice(index, 1);
    newQuestions.splice(index - 1, 0, question);

    setFeedbackForm({ ...feedbackform, questions: newQuestions });
    setUserChangedSomething(true);
    setUserChangedOrder(true);
  }

  const moveQuestionDown = async (questionId: string) => {
    let question = feedbackform?.questions.find(q => q.id === questionId);
    if (!question) return;

    let index = feedbackform?.questions.indexOf(question);
    if (index === undefined || index === null) return;

    if (index === feedbackform?.questions.length - 1) return;

    let newQuestions = [...feedbackform?.questions || []];
    newQuestions.splice(index, 1);
    newQuestions.splice(index + 1, 0, question);

    setFeedbackForm({ ...feedbackform, questions: newQuestions });
    setUserChangedSomething(true);
    setUserChangedOrder(true);
  }

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
            Feedback-Form: {feedbackform.name}
          </h1>
          <Card className="w-full">
            <CardContent>
              <Label className="mt-6">Name</Label>
              <Input
                autoFocus={isNew}
                onFocus={(e) => {
                  if (isNew) e.target.select();
                }}
                value={feedbackform.name}
                onChange={(e) => {
                  setFeedbackForm({ ...feedbackform, name: e.target.value });
                  setUserChangedSomething(true);
                }}
                placeholder="Feedback Form Name"
                className="font-bold bor"
                onKeyDown={(e) => handleEnterPress(e, save)}
              />
              <Label className="mt-2">Description</Label>
              <Input
                value={feedbackform.description}
                onChange={(e) => {
                  setFeedbackForm({ ...feedbackform, description: e.target.value });
                  setUserChangedSomething(true);
                }}
                placeholder="Feedback Form Description"
                onKeyDown={(e) => handleEnterPress(e, save)}
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
                <TableHead>Order</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {feedbackform?.questions.map((question) => (
                <TableRow key={question.id} className="hover:cursor-pointer"
                  onClick={() => {
                    router.push(`/courses/${params.courseId}/feedbackform/${params.formId}/question/${question.id}`)
                  }}
                >
                  <TableCell>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      {question?.type === "SLIDER" && <SlidersHorizontal size={16}/>}
                      {question?.type === "STARS" && <Star size={16}/>}
                      {question?.type === "SINGLE_CHOICE" && <SquareCheckBig size={16}/>}
                      {question?.type === "FULLTEXT" && <TextCursorInput size={16}/>}
                      {question?.type}
                    </div>
                  </TableCell>
                  <TableCell className="font-medium">{question.name}</TableCell>
                  <TableCell>{question.description}</TableCell>
                  <TableCell>{question.options?.join(", ")}</TableCell>
                  <TableCell>{question.rangeLow}</TableCell>
                  <TableCell>{question.rangeHigh}</TableCell>
                  <TableCell className="flex gap-2 flex-col">
                    <Button
                      className="flex flex-col h-8 w-8"
                      variant="outline"
                      onClick={async (e) => {
                        e.stopPropagation();
                        moveQuestionUp(question.id);
                      }}
                    ><SquareArrowUp /></Button>
                    <Button
                      className="flex flex-col h-8 w-8"
                      variant="outline"
                      onClick={async (e) => {
                        e.stopPropagation();
                        moveQuestionDown(question.id);
                      }}
                    ><SquareArrowDown /></Button>
                  </TableCell>
                </TableRow>
              ))}
              {feedbackform?.questions.length === 0 && (
                <TableRow
                  className="hover:cursor-pointer hover:text-blue-500"
                  onClick={async () => {
                    const question = await addFeedbackQuestion(params.courseId, params.formId, "New question", "", "SLIDER", [], "", "");
                    if (question) {
                      toast.success("Question created.");
                      router.push(`/courses/${params.courseId}/feedbackform/${params.formId}/question/${question.id}?is-new=true`);
                    }
                  }}
                  onMouseEnter={() => setHoversOnCreateRow(true)}
                  onMouseLeave={() => setHoversOnCreateRow(false)}
                >
                  <TableCell colSpan={6} className="text-center">
                    {hoversOnCreateRow ? "Create new question" : "No questions found."}
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </>
      )}
    </div>
  );
}