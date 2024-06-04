"use client"

import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@radix-ui/react-dropdown-menu";
import { useRouter, useSearchParams } from 'next/navigation'
import { useState, useEffect, useCallback } from "react";
import { toast } from "sonner";
import { CircleArrowLeft, Loader2, Save, SlidersHorizontal, SquareCheckBig, Star, TextCursorInput, CircleCheck, CircleDashed, ToggleLeft, ListTodo } from 'lucide-react';
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
import { FormResult, QuizQuestion } from "@/lib/models";
import { DeleteButton } from "@/components/delete-button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { deleteQuizQuestion, fetchQuizQuestion, fetchQuizQuestionResults, updateQuizQuestion } from "@/lib/requests";
import { Checkbox } from "@/components/ui/checkbox";


export default function QuizQuestionPage({ params }: { params: { courseId: string, formId: string, questionId: string } }) {

  const OPTION_LIMIT = 5;

  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [quizQuestion, setQuizQuestion] = useState<QuizQuestion>({
    id: "",
    key: "",
    name: "",
    description: "",
    type: "YES_NO",
    options: [],
    hasCorrectAnswers: true,
    correctAnswers: ["yes"]
  });
  const [results, setResults] = useState<FormResult[]>([]);
  const [userChangedSomething, setUserChangedSomething] = useState(false);
  const [isNew, setIsNew] = useState(false);
  const searchParams = useSearchParams()
  const [hoversOnCreateRow, setHoversOnCreateRow] = useState(false);
  const iconSize = 16;
  const [focusLastRow, setFocusLastRow] = useState(false);

  useEffect(() => {
    let isNew = searchParams.get("is-new") === "true"
    setIsNew(isNew);
  }, [searchParams]);

  const save = useCallback(async (logSuccess: boolean = true) => {
    const result = await updateQuizQuestion(params.courseId, params.formId, params.questionId, quizQuestion?.name, quizQuestion?.description, quizQuestion?.type, quizQuestion?.options || [], quizQuestion?.hasCorrectAnswers, quizQuestion?.correctAnswers || []);
    if (result) {
      setQuizQuestion(result);
      setUserChangedSomething(false);

      if (logSuccess) {
        toast.success("Saved.");
      }

    } else {
      toast.error("Failed to save.");
    }
  }, [quizQuestion, params.courseId, params.formId, params.questionId]);

  useEffect(() => {
    const loadQuizQuestion = async () => {
      setLoading(true);
      let quizquestion = await fetchQuizQuestion(params.courseId, params.formId, params.questionId);
      if (!quizquestion) {
        toast.error("QuizQuestion not found.");
        router.back();
        return;
      }

      let results = await fetchQuizQuestionResults(params.courseId, params.formId, params.questionId);
      setResults(results || []);
      setQuizQuestion(quizquestion);
      setLoading(false);
    };
    loadQuizQuestion();
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

  // save on page leave
  useEffect(() => {
    const handleBeforeUnload = async (e: BeforeUnloadEvent) => {
      if (userChangedSomething) {
        e.preventDefault();
        await save();
        toast.info("Saved. (You should give the app a few seconds to save your changes next time.)");
      }
    };
    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
    };
  }, [userChangedSomething, save]);

  // direct save
  useEffect(() => {
    const handleSave = async (e: KeyboardEvent) => {
      if (e.key === "s" && (e.ctrlKey || e.metaKey)) {
        e.preventDefault();
        save();
      }
    };
    document.addEventListener("keydown", handleSave);
    return () => {
      document.removeEventListener("keydown", handleSave);
    };
  }, [save]);

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
                router.push(`/courses/${params.courseId}/quizform/${params.formId}`)
              }}
            >
              <CircleArrowLeft />
            </Button>
            <div className="flex items-center">
              <DeleteButton
                className="mb-4 self-end"
                onDelete={async () => {
                  const result = await deleteQuizQuestion(params.courseId, params.formId, params.questionId);
                  if (result) {
                    router.push(`/courses/${params.courseId}/quizform/${params.formId}`);
                    toast.success("QuizQuestion deleted.");
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
            Quiz-Question: {quizQuestion?.name}
          </h1>
          <Card className="w-full">
            <CardContent>
              <Label className="mt-6">Name</Label>
              <Input
                autoFocus={isNew}
                onFocus={(e) => {
                  if (isNew) e.target.select();
                }}
                value={quizQuestion?.name}
                onChange={(e) => {
                  setQuizQuestion({
                    ...quizQuestion,
                    id: quizQuestion?.id || "",
                    key: quizQuestion?.key || "",
                    description: quizQuestion?.description || "",
                    type: quizQuestion?.type || "YES_NO",
                    name: e.target.value || ""
                  });
                  setUserChangedSomething(true);
                }}
                placeholder="QuizQuestion name"
                className="font-bold bor"
                onKeyDown={(e) => handleEnterPress(e, save)}
              />
              <Label className="mt-2">Description</Label>
              <Input
                value={quizQuestion?.description}
                onChange={(e) => {
                  setQuizQuestion({
                    ...quizQuestion,
                    id: quizQuestion?.id || "",
                    key: quizQuestion?.key || "",
                    name: quizQuestion?.name || "",
                    type: quizQuestion?.type || "SLIDER",
                    description: e.target.value
                  });
                  setUserChangedSomething(true);
                }}
                placeholder="QuizQuestion description"
                onKeyDown={(e) => handleEnterPress(e, save)}
              />
              <Label className="mt-2">Type</Label>
              <Select defaultValue={quizQuestion?.type} onValueChange={(value) => {
                console.log("value", value);
                setQuizQuestion({
                  ...quizQuestion,
                  type: value as "SINGLE_CHOICE" | "YES_NO" | "MULTIPLE_CHOICE" | "FULLTEXT"
                });
                setUserChangedSomething(true);
              }}>
                <SelectTrigger>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    {quizQuestion?.type === "YES_NO" && <ToggleLeft size={iconSize} />}
                    {quizQuestion?.type === "MULTIPLE_CHOICE" && <ListTodo size={iconSize} />}
                    {quizQuestion?.type === "SINGLE_CHOICE" && <SquareCheckBig size={iconSize} />}
                    {quizQuestion?.type === "FULLTEXT" && <TextCursorInput size={iconSize} />}
                    <SelectValue>{quizQuestion?.type}</SelectValue>
                  </div>
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="YES_NO">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <ToggleLeft size={iconSize} />
                      <span>Yes/No</span>
                    </div>
                  </SelectItem>
                  {/* NOT YET SUPPORTED */}
                  {/* <SelectItem value="MULTIPLE_CHOICE">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <ListTodo size={iconSize} />
                      <span>Multiple Choice</span>
                    </div>
                  </SelectItem> */}
                  <SelectItem value="SINGLE_CHOICE">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <SquareCheckBig size={iconSize} />
                      <span>Single Choice</span>
                    </div>
                  </SelectItem>
                  {/* NOT YET SUPPORTED */}
                  {/* <SelectItem value="FULLTEXT">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <TextCursorInput size={iconSize} />
                      <span>Fulltext</span>
                    </div>
                  </SelectItem> */}
                </SelectContent>
              </Select>
              <div className="mt-2 flex gap-4 items-center">
                <Label>Has Correct Answers</Label>
                <Checkbox
                  checked={quizQuestion?.hasCorrectAnswers}
                  onCheckedChange={(checked) => {
                    checked = checked as boolean || false;
                    setQuizQuestion({
                      ...quizQuestion,
                      hasCorrectAnswers: checked
                    });
                    setUserChangedSomething(true);
                  }}
                />
              </div>

              {/* if yes/no -> display checkboxes for yes and no */}
              {quizQuestion?.type === "YES_NO" && quizQuestion?.hasCorrectAnswers && (
                <div className="flex gap-4 items-center bg-gray-100 p-4 rounded-md">
                  <div className="flex gap-4 items-center">
                    <Checkbox
                      checked={quizQuestion?.correctAnswers?.includes("yes")}
                      onCheckedChange={(checked) => {
                        checked = checked as boolean || false;
                        let correctAnswers = quizQuestion?.correctAnswers || [];
                        if (checked) {
                          correctAnswers.push("yes");
                        } else {
                          correctAnswers = correctAnswers.filter(a => a !== "yes");
                        }
                        setQuizQuestion({
                          ...quizQuestion,
                          correctAnswers: correctAnswers
                        });
                        setUserChangedSomething(true);
                      }}
                    />
                    <Label>Yes</Label>
                  </div>
                  <div className="flex gap-4 items-center">

                    <Checkbox
                      checked={quizQuestion?.correctAnswers?.includes("no")}
                      onCheckedChange={(checked) => {
                        checked = checked as boolean || false;
                        let correctAnswers = quizQuestion?.correctAnswers || [];
                        if (checked) {
                          correctAnswers.push("no");
                        } else {
                          correctAnswers = correctAnswers.filter(a => a !== "no");
                        }
                        setQuizQuestion({
                          ...quizQuestion,
                          correctAnswers: correctAnswers
                        });
                        setUserChangedSomething(true);
                      }}
                    />
                    <Label>No</Label>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          {(quizQuestion?.type === "FULLTEXT" && quizQuestion?.hasCorrectAnswers) && (
            <>
              <div className="flex justify-between w-full mb-4 mt-8 flex-grow flex-wrap gap-4">
                <h2 className="text-2xl">Correct Answers</h2>
                <div className="flex gap-4 justify-end">
                  <Button
                    className="flex flex-col self-end"
                    onClick={() => {
                      setQuizQuestion({ ...quizQuestion, correctAnswers: [...quizQuestion.correctAnswers || [], ""] });
                      setUserChangedSomething(true);
                      setFocusLastRow(true);
                    }}
                  >Add Correct Answer</Button>
                </div>
              </div>
              <Table>
                <TableBody>
                  {quizQuestion?.correctAnswers?.map((answer, index) => (
                    <TableRow key={index}>
                      <TableCell>
                        <Input
                          value={answer}
                          onChange={(e) => {
                            setQuizQuestion({
                              ...quizQuestion,
                              correctAnswers: quizQuestion.correctAnswers?.map((a, i) => i === index ? e.target.value : a)
                            });
                            setUserChangedSomething(true);
                          }}
                          placeholder="Answer"
                          onBlur={() => {
                            if ((quizQuestion?.correctAnswers?.filter(q => q === "").length || 0) > 0) {
                              setQuizQuestion({
                                ...quizQuestion,
                                correctAnswers: quizQuestion.correctAnswers?.filter(a => a !== "")
                              });
                              setUserChangedSomething(true);
                            }
                          }}
                          autoFocus={focusLastRow && index === (quizQuestion?.correctAnswers?.length || 0) - 1}
                          onKeyDown={(e) => handleEnterPress(e, save)}
                        />

                      </TableCell>

                      <TableCell>
                        <Button
                          variant="outline"
                          className="hover:bg-red-500 hover:text-white"
                          onClick={() => {
                            setQuizQuestion({
                              ...quizQuestion,
                              correctAnswers: quizQuestion.correctAnswers?.filter((a, i) => i !== index)
                            });
                            setUserChangedSomething(true);
                          }}
                        >Remove</Button>
                      </TableCell>
                    </TableRow>
                  ))}

                  {quizQuestion?.correctAnswers?.length === 0 && (

                    <TableRow
                      className="hover:cursor-pointer hover:text-blue-500"
                      onClick={() => {
                        setQuizQuestion({ ...quizQuestion, correctAnswers: [...quizQuestion.correctAnswers || [], ""] });
                        setUserChangedSomething(true);
                        setFocusLastRow(true);
                      }}
                      onMouseEnter={() => setHoversOnCreateRow(true)}
                      onMouseLeave={() => setHoversOnCreateRow(false)}
                    >
                      <TableCell colSpan={6} className="text-center">
                        {hoversOnCreateRow ? "Create new correct answer" : "No correct answers found."}
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </>
          )}


          { /* Options */}
          {(quizQuestion?.type === "SINGLE_CHOICE" || quizQuestion?.type === "MULTIPLE_CHOICE") && (
            <>
              <div className="flex justify-between w-full mb-4 mt-8 flex-grow flex-wrap gap-4">
                <h2 className="text-2xl">Options ({quizQuestion?.options?.length || 0}/{OPTION_LIMIT})</h2>
                <div className="flex gap-4 justify-end">
                  <Button
                    title={ (quizQuestion?.options?.length || 0) >= OPTION_LIMIT ? "Option limit reached" : "Add Option"}
                    disabled={(quizQuestion?.options?.length || 0) >= OPTION_LIMIT}
                    className="flex flex-col self-end"
                    onClick={() => {
                      setQuizQuestion({ ...quizQuestion, options: [...quizQuestion.options || [], ""] });
                      setUserChangedSomething(true);
                      setFocusLastRow(true);
                    }}
                  >Add Option</Button>
                </div>
              </div>
              <Table>
                <TableHeader className="bg-gray-100">
                  <TableRow>
                    <TableHead>Option</TableHead>

                    {quizQuestion?.hasCorrectAnswers && (
                      <TableHead>Correct</TableHead>
                    )}

                    <TableHead></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {quizQuestion?.options?.map((question, index) => (
                    <TableRow key={index}>
                      <TableCell>
                        <Input
                          value={question}
                          onChange={(e) => {
                            setQuizQuestion({
                              ...quizQuestion,
                              options: quizQuestion.options?.map((q, i) => i === index ? e.target.value : q)
                            });
                            setUserChangedSomething(true);
                          }}
                          placeholder="Option"
                          onBlur={() => {
                            if ((quizQuestion?.options?.filter(q => q === "").length || 0) > 0) {
                              setQuizQuestion({
                                ...quizQuestion,
                                options: quizQuestion.options?.filter(q => q !== "")
                              });
                              setUserChangedSomething(true);
                            }
                          }}
                          autoFocus={focusLastRow && index === (quizQuestion?.options?.length || 0) - 1}
                          onKeyDown={(e) => handleEnterPress(e, save)}
                        />

                      </TableCell>

                      {quizQuestion?.hasCorrectAnswers && (
                        <TableCell>
                          <Checkbox
                            checked={quizQuestion?.correctAnswers?.includes(index.toString())}
                            onCheckedChange={(checked) => {
                              checked = checked as boolean || false;
                              let correctAnswers = quizQuestion?.correctAnswers || [];

                              // if single choice, remove all other correct answers
                              if (quizQuestion?.type === "SINGLE_CHOICE") {
                                correctAnswers = [];
                              }

                              if (checked) {
                                correctAnswers.push(index.toString());
                              } else {
                                correctAnswers = correctAnswers.filter(a => a !== index.toString());
                              }
                              setQuizQuestion({
                                ...quizQuestion,
                                correctAnswers: correctAnswers
                              });
                              setUserChangedSomething(true);
                            }}
                          />
                        </TableCell>
                      )}

                      <TableCell>
                        <Button
                          variant="outline"
                          className="hover:bg-red-500 hover:text-white"
                          onClick={() => {
                            setQuizQuestion({
                              ...quizQuestion,
                              options: quizQuestion.options?.filter((q, i) => i !== index)
                            });
                            setUserChangedSomething(true);
                          }}
                        >Remove</Button>
                      </TableCell>
                    </TableRow>
                  ))}

                  {quizQuestion?.options?.length === 0 && (

                    <TableRow
                      className="hover:cursor-pointer hover:text-blue-500"
                      onClick={() => {
                        setQuizQuestion({ ...quizQuestion, options: [...quizQuestion.options || [], ""] });
                        setUserChangedSomething(true);
                        setFocusLastRow(true);
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
          {/* RESULTS */}
          {results.length > 0 && (
            <>
              <div className="flex justify-between w-full mb-4 mt-8 flex-grow flex-wrap gap-4">
                <h2 className="text-2xl">Results</h2>
              </div>
              <Table>
                <TableHeader className="bg-gray-100">
                  <TableRow>
                    <TableHead>User</TableHead>
                    <TableHead>Value</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {results.map((result, index) => (
                    <TableRow key={index}>
                      <TableCell>{index}</TableCell>
                      { quizQuestion?.type === "SINGLE_CHOICE" && (
                        <TableCell>{quizQuestion.options?.filter((o, i) => result.values.includes(i.toString())).join(", ")}</TableCell>
                      ) || (
                        <TableCell>{result.values.join(", ")}</TableCell>
                      )}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </>
          )}
        </>
      )}
    </div>
  );
}