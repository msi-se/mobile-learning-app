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
import { hasValidJwtToken, login } from "@/lib/utils";


export default function Home() {

  const [mounted, setMounted] = useState(false); // used for detecting renders

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

  useEffect(() => {
    setMounted(true);
  }, []);

  if (!mounted) {
    return <> </>
  }

  if (typeof window === "undefined") {
    return null;
  }

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
    <div className="flex flex-col items-center justify-center h-screen">
      <Card className="w-[350px]">
        <CardHeader>
          <CardTitle>Login</CardTitle>
        </CardHeader>
        <CardContent>
          <Label>HTWG-Username</Label>
          <Input type="text" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="jo871bra" />
          <Label className="mt-2">Password</Label>
          <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} onSubmit={handleLogin} />
          <Button
            className="mt-4"
            onClick={handleLogin}
            disabled={!username || !password}
          >{loading ? <Loader2 className="w-6 h-6 animate-spin" /> : "Login"}</Button>
          {/* TODO: add event listener for enter key */}
        </CardContent>
      </Card>
    </div>
  );
}
