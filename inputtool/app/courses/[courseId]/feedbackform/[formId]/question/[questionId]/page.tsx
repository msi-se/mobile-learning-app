"use client"

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@radix-ui/react-dropdown-menu";
import { useRouter, useSearchParams } from 'next/navigation'
import { useState, useEffect } from "react";
import { toast } from "sonner";
import { CircleArrowLeft, Loader2, Save, SlidersHorizontal, SquareCheckBig, Star, TextCursorInput } from 'lucide-react';
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
  const [somethingHasChanged, setSomethingHasChanged] = useState(false);
  const [isNew, setIsNew] = useState(false);
  const searchParams = useSearchParams()

  useEffect(() => {
    let isNew = searchParams.get("is-new") === "true"
    setIsNew(isNew);
  }, [searchParams]);

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
          <Button
            variant="secondary"
            className="mb-4 self-start text-sm"
            onClick={() => router.push(`/courses/${params.courseId}/feedbackform/${params.formId}`)}
          ><CircleArrowLeft /></Button>
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
                  setSomethingHasChanged(true);
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
                  setSomethingHasChanged(true);
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
                setSomethingHasChanged(true);
              }}>
                <SelectTrigger>
                  <SelectValue>{feedbackQuestion?.type}</SelectValue>
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="SLIDER">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <SlidersHorizontal />
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
                      setSomethingHasChanged(true);
                    }}
                    placeholder={"Very Bad"}
                  />
                  <Label className="mt-2">Range-High</Label>
                  <Input
                    value={feedbackQuestion?.rangeHigh}
                    onChange={(e) => {
                      setFeedbackQuestion({ ...feedbackQuestion, rangeHigh: e.target.value });
                      setSomethingHasChanged(true);
                    }}
                    placeholder={"Very Good"}
                  />
                </>
              )}

              {/* first button at the left second at the right */}
              <div className="flex justify-between">
                <Button
                  disabled={!somethingHasChanged}
                  className="mt-4"
                  onClick={async () => {
                    const result = await updateFeedbackQuestion(params.courseId, params.formId, params.questionId, feedbackQuestion?.name || "", feedbackQuestion?.description || "", feedbackQuestion?.type || "", feedbackQuestion?.options || [], feedbackQuestion?.rangeLow || "", feedbackQuestion?.rangeHigh || "");
                    if (result) {
                      setSomethingHasChanged(false);
                      setFeedbackQuestion(result);
                      toast.success("FeedbackQuestion updated.");
                    } else {
                      toast.error("FeedbackQuestion could not be updated.");
                    }
                  }}
                ><Save /></Button>
                <DeleteButton
                  className="mt-4"
                  onDelete={async () => {
                    const result = await deleteFeedbackQuestion(params.courseId, params.formId, params.questionId);
                    if (result) {
                      router.push(`/courses/${params.courseId}/feedbackform/${params.formId}`);
                      toast.success("FeedbackQuestion deleted.");
                    }
                  }}
                />
              </div>
            </CardContent>
          </Card>
          { /* Options */}
          {feedbackQuestion?.type === "SINGLE_CHOICE" && (
            <>
              <h2 className="text-2xl mt-4">Options</h2>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Option</TableHead>
                    <TableHead>Remove</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {feedbackQuestion?.options?.map((question, index) => (
                    <TableRow key={index}>
                      <TableCell>
                        <Input
                          value={question}
                          onChange={(e) => {
                            setFeedbackQuestion({ ...feedbackQuestion, options: feedbackQuestion.options?.map((q) => q === question ? e.target.value : q) });
                            setSomethingHasChanged(true);
                          }}
                          placeholder="Option"
                        />
                      </TableCell>
                      <TableCell>
                        <Button
                          onClick={() => {
                            setFeedbackQuestion({ ...feedbackQuestion, options: feedbackQuestion.options?.filter((q) => q !== question) });
                            setSomethingHasChanged(true);
                          }}
                        >Remove</Button>
                      </TableCell>
                    </TableRow>
                  ))}

                  {feedbackQuestion?.options?.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={2}>
                        No Options
                      </TableCell>
                    </TableRow>
                  )}
                  <TableRow>
                    <TableCell colSpan={2}>
                      <Button
                        onClick={() => {
                          setFeedbackQuestion({ ...feedbackQuestion, options: [...feedbackQuestion.options || [], ""] });
                          setSomethingHasChanged(true);
                        }}
                      >Add Option</Button>
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </>
          )}
        </>
      )}
    </div>
  );
}