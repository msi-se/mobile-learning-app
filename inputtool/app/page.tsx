"use client"

import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label, Separator } from "@radix-ui/react-dropdown-menu";
import { useRouter } from 'next/navigation'
import { useState, useEffect } from "react";
import { Loader2 } from 'lucide-react';
import { hasValidJwtToken, login } from "@/lib/utils";
import HtwgPattern from "@/public/htwg-pattern";
import HtwgConnectLogo from "@/public/htwg-connect-logo";


export default function Home() {

  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const handleEnter = (e: KeyboardEvent) => {
      if (e.key === "Enter") {
        handleLogin();
      }
    };
    document.addEventListener("keydown", handleEnter);
    return () => {
      document.removeEventListener("keydown", handleEnter);
    };
  });

  hasValidJwtToken().then((isValid) => {
    if (isValid) router.push("/courses");
  });

  const handleLogin = async () => {
    setLoading(true);
    let success = await login(username, password);
    setLoading(false);
    if (success) router.push("/courses");
  };

  return (
    <>
      <HtwgPattern className="absolute top-0 left-0 w-full h-full z-[-1]" />
      <div className="flex flex-col items-center justify-center h-screen">
        <Card className="w-[350px]">
          <CardHeader>
            <div className="flex items-center">
              <HtwgConnectLogo className="m-5" style={{ height: '100px', width: '100px' }} />
              <div className="row">
                <span className="font-extrabold text-primary text-xl block" >HTWG Connect</span>
                <span className="font-bold text-lg block" >Input Tool</span>
              </div>
            </div>
            <Separator className="my-5"/>
            <CardTitle className="text-center font-bold">Login</CardTitle>
          </CardHeader>
          <CardContent>
            <Label>HTWG-Username</Label>
            <Input autoFocus type="text" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="ma871mus" />
            <Label className="mt-2">Password</Label>
            <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} onSubmit={handleLogin} />
            <Button
              className="mt-4 w-full"
              onClick={handleLogin}
              disabled={!username}
            >{loading ? <Loader2 className="w-6 h-6 animate-spin" /> : "Login"}</Button>
            <Separator className="my-5"/>
            <p className="text-sm">
            This web interface allows lecturers at HTWG Konstanz to create courses and content for the “HTWG-Connect” app.
            The HTWG-Connect app is designed for more digital interaction during lectures.
            Teachers can create feedback surveys and quizzes for students and conduct them live in the lecture.
            This tool is exactly for this preparation process.
            </p>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
