"use client"

import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@radix-ui/react-dropdown-menu";
import Image from "next/image";
import { useRouter } from 'next/navigation'
import { useState, useEffect } from "react";
import { toast } from "sonner";


export default function Home() {

  const [mounted, setMounted] = useState(false); // used for detecting renders

  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  useEffect(() => {
    setMounted(true);
  }, []);

  if (!mounted) {
    return <> </>
  }

  if (typeof window === "undefined") {
    return null;
  }

  // check if there is already a jwt token in the local storage
  const jwtToken = localStorage.getItem("jwtToken");

  if (jwtToken) {
    router.push("/courses");
  }

  const handleLogin = async () => {

    console.log("Logging in...");

    if (!username || !password) {
      console.error("Username and password are required.");
      return;
    }

    const currentUrl = window.location.href;
    const BACKEND_URL = currentUrl.includes("localhost") ? "http://localhost:8080" : `${currentUrl}/api`;

    let loginResponse = await fetch(`${BACKEND_URL}/user/login`, {
      method: "POST",
      headers: {
        "AUTHORIZATION": "Basic " + btoa(`${username}:${password}`)
      }
    });
    let jwt = await loginResponse.text();
    if (loginResponse.status !== 200) {
        console.error(`Failed to login. Status: ${loginResponse.status}`);
        console.error(loginResponse);
        toast.error("Failed to login. Please check your credentials.");
        return;
    }
    console.log(`Successfully logged in.`);
    toast.success("Successfully logged in.");
    console.log(`JWT: ${jwt}`);
    localStorage.setItem("jwtToken", jwt);
    router.push("/courses");
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
          >Login</Button>
        </CardContent>
      </Card>
    </div>
  );
}
