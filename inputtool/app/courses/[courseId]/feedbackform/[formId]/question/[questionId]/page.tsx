"use client"

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@radix-ui/react-dropdown-menu";
import { useRouter, useSearchParams } from 'next/navigation'
import { useState, useEffect, useCallback } from "react";
import { toast } from "sonner";
import { CircleArrowLeft, Loader2, Save, SlidersHorizontal, SquareCheckBig, Star, TextCursorInput, CircleCheck, CircleDashed } from 'lucide-react';
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
import { FeedbackQuestion } from "@/lib/models";
import { DeleteButton } from "@/components/delete-button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { deleteFeedbackQuestion, fetchFeedbackQuestion, updateFeedbackQuestion } from "@/lib/requests";


export default function FeedbackQuestionPage({ params }: { params: { courseId: string, formId: string, questionId: string } }) {

  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [feedbackQuestion, setFeedbackQuestion] = useState<FeedbackQuestion | null>(null);
  const [userChangedSomething, setUserChangedSomething] = useState(false);
  const [isNew, setIsNew] = useState(false);
  const searchParams = useSearchParams()
  const [hoversOnCreateRow, setHoversOnCreateRow] = useState(false);

  useEffect(() => {
    let isNew = searchParams.get("is-new") === "true"
    setIsNew(isNew);
  }, [searchParams]);

  const save = useCallback(async (logSuccess: boolean = true) => {
    const result = await updateFeedbackQuestion(params.courseId, params.formId, params.questionId, feedbackQuestion?.name || "", feedbackQuestion?.description || "", feedbackQuestion?.type || "", feedbackQuestion?.options || [], feedbackQuestion?.rangeLow || "", feedbackQuestion?.rangeHigh || "");
    if (result) {
      setFeedbackQuestion(result);
      setUserChangedSomething(false);

      if (logSuccess) {
        toast.success("Saved.");
      }

    } else {
      toast.error("Failed to save.");
    }
  }, [feedbackQuestion, params.courseId, params.formId, params.questionId]);

  useEffect(() => {
    const loadFeedbackQuestion = async () => {
      setLoading(true);
      let feedbackquestion = await fetchFeedbackQuestion(params.courseId, params.formId, params.questionId);
      if (!feedbackquestion) {
        toast.error("FeedbackQuestion not found.");
        router.back();
        return;
      }
      setFeedbackQuestion(feedbackquestion);
      setLoading(false);
    };
    loadFeedbackQuestion();
  }, [params.courseId, params.formId, params.questionId, router]);


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
                router.push(`/courses/${params.courseId}/feedbackform/${params.formId}`)
              }}
            >
              <CircleArrowLeft />
            </Button>
            <div className="flex items-center">
              <DeleteButton
                className="mb-4 self-end"
                onDelete={async () => {
                  const result = await deleteFeedbackQuestion(params.courseId, params.formId, params.questionId);
                  if (result) {
                    router.push(`/courses/${params.courseId}/feedbackform/${params.formId}`);
                    toast.success("FeedbackQuestion deleted.");
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
            Feedback-Question: {feedbackQuestion?.name}
          </h1>
          <Card className="w-full">
            <CardContent>
              <Label className="mt-6">Name</Label>
              <Input
                autoFocus={isNew}
                onFocus={(e) => {
                  if (isNew) e.target.select();
                }}
                value={feedbackQuestion?.name}
                onChange={(e) => {
                  setFeedbackQuestion({
                    ...feedbackQuestion,
                    id: feedbackQuestion?.id || "",
                    key: feedbackQuestion?.key || "",
                    description: feedbackQuestion?.description || "",
                    type: feedbackQuestion?.type || "SLIDER",
                    name: e.target.value || ""
                  });
                  setUserChangedSomething(true);
                }}
                placeholder="FeedbackQuestion name"
                className="font-bold bor"
              />
              <Label className="mt-2">Description</Label>
              <Input
                value={feedbackQuestion?.description}
                onChange={(e) => {
                  setFeedbackQuestion({
                    ...feedbackQuestion,
                    id: feedbackQuestion?.id || "",
                    key: feedbackQuestion?.key || "",
                    name: feedbackQuestion?.name || "",
                    type: feedbackQuestion?.type || "SLIDER",
                    description: e.target.value
                  });
                  setUserChangedSomething(true);
                }}
                placeholder="FeedbackQuestion description"
              />
              <Label className="mt-2">Type</Label> {/* "SLIDER"| "STARS"| "SINGLE_CHOICE"| "FULLTEXT" */}
              <Select defaultValue={feedbackQuestion?.type} onValueChange={(value) => {
                console.log("value", value);
                setFeedbackQuestion({
                  ...feedbackQuestion,
                  type: value as "SLIDER" | "STARS" | "SINGLE_CHOICE" | "FULLTEXT",
                  id: feedbackQuestion?.id || "",
                  key: feedbackQuestion?.key || "",
                  name: feedbackQuestion?.name || "",
                  description: feedbackQuestion?.description || ""
                });
                setUserChangedSomething(true);
              }}>
                <SelectTrigger>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    {feedbackQuestion?.type === "SLIDER" && <SlidersHorizontal />}
                    {feedbackQuestion?.type === "STARS" && <Star />}
                    {feedbackQuestion?.type === "SINGLE_CHOICE" && <SquareCheckBig />}
                    {feedbackQuestion?.type === "FULLTEXT" && <TextCursorInput />}
                    <SelectValue>{feedbackQuestion?.type}</SelectValue>
                  </div>
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="SLIDER">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <SlidersHorizontal size={16} />
                      <span>Slider</span>
                    </div>
                  </SelectItem>
                  <SelectItem value="STARS">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <Star />
                      <span>Stars</span>
                    </div>
                  </SelectItem>
                  <SelectItem value="SINGLE_CHOICE">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <SquareCheckBig />
                      <span>Single Choice</span>
                    </div>
                  </SelectItem>
                  <SelectItem value="FULLTEXT">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <TextCursorInput />
                      <span>Fulltext</span>
                    </div>
                  </SelectItem>
                </SelectContent>
              </Select>

              {feedbackQuestion?.type === "SLIDER" && (
                <>
                  <Label className="mt-2">Range-Low</Label>
                  <Input
                    value={feedbackQuestion?.rangeLow}
                    onChange={(e) => {
                      setFeedbackQuestion({ ...feedbackQuestion, rangeLow: e.target.value });
                      setUserChangedSomething(true);
                    }}
                    placeholder={"Very Bad"}
                  />
                  <Label className="mt-2">Range-High</Label>
                  <Input
                    value={feedbackQuestion?.rangeHigh}
                    onChange={(e) => {
                      setFeedbackQuestion({ ...feedbackQuestion, rangeHigh: e.target.value });
                      setUserChangedSomething(true);
                    }}
                    placeholder={"Very Good"}
                  />
                </>
              )}
            </CardContent>
          </Card>
          { /* Options */}
          {feedbackQuestion?.type === "SINGLE_CHOICE" && (
            <>
              <div className="flex justify-between w-full mb-4 mt-8 flex-grow flex-wrap gap-4">
                <h2 className="text-2xl">Options</h2>
                <div className="flex gap-4 justify-end">
                  <Button
                    className="flex flex-col self-end"
                    onClick={() => {
                      setFeedbackQuestion({ ...feedbackQuestion, options: [...feedbackQuestion.options || [], ""] });
                      setUserChangedSomething(true);
                    }}
                  >Add Option</Button>
                </div>
              </div>
              <Table>
                <TableHeader className="bg-gray-100">
                  <TableRow>
                    <TableHead>Option</TableHead>
                    <TableHead></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {feedbackQuestion?.options?.map((question, index) => (
                    <TableRow key={index}>
                      <TableCell>
                        <Input
                          value={question}
                          onChange={(e) => {
                            setFeedbackQuestion({
                              ...feedbackQuestion,
                              options: feedbackQuestion.options?.map((q, i) => i === index ? e.target.value : q)
                            });
                            setUserChangedSomething(true);
                          }}
                          placeholder="Option"
                        />
                      </TableCell>
                      <TableCell>
                        <Button
                          variant="outline"
                          className="hover:bg-red-500 hover:text-white"
                          onClick={() => {
                            setFeedbackQuestion({
                              ...feedbackQuestion,
                              options: feedbackQuestion.options?.filter((q, i) => i !== index)
                            });
                            setUserChangedSomething(true);
                          }}
                        >Remove</Button>
                      </TableCell>
                    </TableRow>
                  ))}

                  {feedbackQuestion?.options?.length === 0 && (

                    <TableRow
                      className="hover:cursor-pointer hover:text-blue-500"
                      onClick={() => {
                        setFeedbackQuestion({ ...feedbackQuestion, options: [...feedbackQuestion.options || [], ""] });
                        setUserChangedSomething(true);
                      }}
                      onMouseEnter={() => setHoversOnCreateRow(true)}
                      onMouseLeave={() => setHoversOnCreateRow(false)}
                    >
                      <TableCell colSpan={6} className="text-center">
                        {hoversOnCreateRow ? "Create new option" : "No options found."}
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </>
          )}
        </>
      )}
    </div>
  );
}