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
import { fetchFeedbackQuestion, hasValidJwtToken } from "@/lib/utils";
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
    <div className="flex flex-col items-center justify-center h-screen mx-4">

      {loading && (
        <Loader2 className="w-6 h-6 animate-spin" />
      )}

      {!loading && (
        <>
          <h1 className="text-2xl mb-4 font-bold">
            Feedback-Question: {feedbackQuestion?.name}
          </h1>
          <Card className="w-full">
            <CardHeader>
              <CardTitle>
                <Input
                  value={feedbackQuestion?.name}
                  onChange={(e) => {
                    setFeedbackQuestion({ ...feedbackQuestion, name: e.target.value });
                    setSomethingHasChanged(true);
                  }}
                  placeholder="FeedbackQuestion name"
                  className="font-bold bor"
                />
              </CardTitle>
            </CardHeader>
            <CardContent>
              <Label>Description</Label>
              <Input
                value={feedbackQuestion?.description}
                onChange={(e) => {
                  setFeedbackQuestion({ ...feedbackQuestion, description: e.target.value });
                  setSomethingHasChanged(true);
                }}
                placeholder="FeedbackQuestion description"
              />
              <Label>Type</Label> {/* "SLIDER"| "STARS"| "SINGLE_CHOICE"| "FULLTEXT"| "YES_NO" */}
              <Select>
                <SelectTrigger>
                  <SelectValue>{feedbackQuestion?.type}</SelectValue>
                </SelectTrigger>
                <SelectContent>
                  <SelectItem
                    value="SLIDER"
                    onSelect={() => {
                      setFeedbackQuestion({ ...feedbackQuestion, type: "SLIDER" });
                      setSomethingHasChanged(true);
                    }}
                  >SLIDER</SelectItem>
                  <SelectItem
                    value="STARS"
                    onSelect={() => {
                      setFeedbackQuestion({ ...feedbackQuestion, type: "STARS" });
                      setSomethingHasChanged(true);
                    }}
                  >STARS</SelectItem>
                  <SelectItem
                    value="SINGLE_CHOICE"
                    onSelect={() => {
                      setFeedbackQuestion({ ...feedbackQuestion, type: "SINGLE_CHOICE" });
                      setSomethingHasChanged(true);
                    }}
                  >SINGLE_CHOICE</SelectItem>
                  <SelectItem
                    value="FULLTEXT"
                    onSelect={() => {
                      setFeedbackQuestion({ ...feedbackQuestion, type: "FULLTEXT" });
                      setSomethingHasChanged(true);
                    }}
                  >FULLTEXT</SelectItem>
                  <SelectItem
                    value="YES_NO"
                    onSelect={() => {
                      setFeedbackQuestion({ ...feedbackQuestion, type: "YES_NO" });
                      setSomethingHasChanged(true);
                    }}
                  >YES_NO</SelectItem>
                </SelectContent>
              </Select>

              <Label>Range-Low</Label>
              <Input
                value={feedbackQuestion?.rangeLow}
                onChange={(e) => {
                  setFeedbackQuestion({ ...feedbackQuestion, rangeLow: e.target.value });
                  setSomethingHasChanged(true);
                }}
                placeholder="FeedbackQuestion rangeLow"
              />
              <Label>Range-High</Label>
              <Input
                value={feedbackQuestion?.rangeHigh}
                onChange={(e) => {
                  setFeedbackQuestion({ ...feedbackQuestion, rangeHigh: e.target.value });
                  setSomethingHasChanged(true);
                }}
                placeholder="FeedbackQuestion rangeHigh"
              />
              

              {/* first button at the left second at the right */}
              <div className="flex justify-between">
                <Button
                  disabled={!somethingHasChanged}
                  className="mt-4"
                  onClick={() => {
                    updateFeedbackQuestion(feedbackquestion?.id, feedbackquestionName, feedbackquestionDescription, feedbackquestionType, feedbackquestionRangeLow, feedbackquestionRangeHigh);
                  }}
                >Update feedbackquestion</Button>
                <DeleteButton
                  className="mt-4"
                  onDelete={() => {
                    deleteFeedbackQuestion(feedbackquestion?.id);
                  }}
                />
              </div>
            </CardContent>
          </Card>
          <h2 className="text-2xl mt-4">Questions</h2>
          {/* <Table>
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
              {feedbackquestion?.questions.map((question) => (
                <TableRow key={question.id} className="hover:bg-green-50 hover:cursor-pointer"
                  onClick={() => {
                    router.push(`/feedbackquestions/${feedbackquestion?.id}/quizform/${feedbackquestion.id}/question/${question.id}`)
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
          </Table> */}
          {/* <div className="flex flex-col items-stretch justify-center">
            <Button
              className="mt-4"
              onClick={() => router.push(`/feedbackquestions/${feedbackquestion?.id}/question/new`)}
            >Add new question</Button>
          </div> */}
        </>
      )}
    </div>
  );
}