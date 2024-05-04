"use client"

import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@radix-ui/react-dropdown-menu";
import Image from "next/image";
import { useRouter } from 'next/navigation'
import { useState, useEffect } from "react";
import { toast } from "sonner";
import { CircleArrowLeft, Loader2 } from 'lucide-react';
import { hasValidJwtToken } from "@/lib/utils";
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
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);

  const [feedbackQuestion, setFeedbackQuestion] = useState<FeedbackQuestion | null>(null);
  const [somethingHasChanged, setSomethingHasChanged] = useState(false);

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
    <div className="flex flex-col items-center justify-center h-screen m-4">

      {loading && (
        <Loader2 className="w-6 h-6 animate-spin" />
      )}

      {!loading && (
        <>
          <Button
            className="mb-4 self-start text-sm"
            onClick={() => router.push(`/courses/${params.courseId}/feedbackform/${params.formId}`)}
          ><CircleArrowLeft /></Button>
          <h1 className="text-2xl mb-4 font-bold">
            Feedback-Question: {feedbackQuestion?.name}
          </h1>
          <Card className="w-full">
            <CardHeader>
              <CardTitle>
                <Input
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
              </CardTitle>
            </CardHeader>
            <CardContent>
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
              <Label className="mt-2">Type</Label> {/* "SLIDER"| "STARS"| "SINGLE_CHOICE"| "FULLTEXT"| "YES_NO" */}
              <Select defaultValue="SLIDER" onValueChange={(value) => {
                setFeedbackQuestion({
                  ...feedbackQuestion,
                  type: value as "SLIDER"| "STARS"| "SINGLE_CHOICE"| "FULLTEXT"| "YES_NO",
                  id: feedbackQuestion?.id || "",
                  key: feedbackQuestion?.key || "",
                  name: feedbackQuestion?.name || "",
                  description: feedbackQuestion?.description || ""
                });
                setSomethingHasChanged(true);
              }}
              >
                <SelectTrigger>
                  <SelectValue>{feedbackQuestion?.type}</SelectValue>
                </SelectTrigger>
                {/* TODO: default selection not working if you click in it */}
                <SelectContent defaultValue={feedbackQuestion?.type}>
                  <SelectItem defaultChecked={feedbackQuestion?.type === "SLIDER"}
                    value="SLIDER">Slider</SelectItem>
                  <SelectItem defaultChecked={feedbackQuestion?.type === "STARS"}
                    value="STARS">Stars</SelectItem>
                  <SelectItem defaultChecked={feedbackQuestion?.type === "SINGLE_CHOICE"}
                    value="SINGLE_CHOICE">Single Choice</SelectItem>
                  <SelectItem defaultChecked={feedbackQuestion?.type === "FULLTEXT"}
                    value="FULLTEXT">Fulltext</SelectItem>
                  <SelectItem defaultChecked={feedbackQuestion?.type === "YES_NO"}
                    value="YES_NO">Yes/No</SelectItem>
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
                >Update feedbackquestion</Button>
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
                  <TableRow>
                    <TableCell>
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