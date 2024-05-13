import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"
import { toast } from "sonner";
import Router from "next/router";
import { Course, FeedbackForm, FeedbackQuestion } from "./models";
import getBackendUrl from "@/lib/get-backend-url";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export async function hasValidJwtToken(): Promise<boolean> {

  if (typeof window === 'undefined') {
    return false;
  }

  const jwtToken = localStorage ? localStorage.getItem("jwtToken") : null;
  if (!jwtToken) {
    return false;
  }

  // check if the token is still valid (client-side)
  const isTokenExpired = (jwtToken: String) => Date.now() >= (JSON.parse(atob(jwtToken.split('.')[1]))).exp * 1000;
  if (isTokenExpired(jwtToken)) {
    // console.error(`Token is expired.`);
    // console.error(`JWT: ${jwtToken}`);
    toast.error("Token is expired. Please login again.");
    localStorage.removeItem("jwtToken");
    return false;
  }

  // if the token is not expired, check if it is still valid but only every 10 minutes
  const lastCheck = localStorage.getItem("lastCheck");
  if (lastCheck) {
    const lastCheckTime = parseInt(lastCheck);
    const currentTime = Date.now();
    if (currentTime - lastCheckTime < 10 * 60 * 1000) {
      // console.log(`Last check was less than 10 minutes ago.`);
      return true;
    }
  }

  localStorage.setItem("lastCheck", Date.now().toString());

  // check if the token is still valid
  const BACKEND_URL = await getBackendUrl();

  let checkResponse = await fetch(`${BACKEND_URL}/user/verify`, {
    method: "GET",
    headers: {
      "AUTHORIZATION": "Bearer " + jwtToken
    }
  });
  if (checkResponse.status !== 200) {
    // console.error(`Token is invalid. Status: ${checkResponse.status}`);
    // console.error(checkResponse);
    toast.error("Token is invalid. Please login again.");
    localStorage.removeItem("jwtToken");
    return false;
  }
  // console.log(`Token is still valid.`);
  // console.log(`JWT: ${jwtToken}`);
  return true;
}

export async function login(username: string, password: string): Promise<boolean> {

  if (!username) {
    // console.error("Username is required.");
    toast.error("Username is required.");
    return false;
  }

  const BACKEND_URL = await getBackendUrl();
  const abortController = new AbortController();
  setTimeout(() => abortController.abort(), 3000);
  let loginResponse = null
  try {
    loginResponse = await fetch(`${BACKEND_URL}/user/login`, {
      method: "POST",
      headers: {
        "AUTHORIZATION": "Basic " + btoa(`${username}:${password}`)
      },
      signal: abortController.signal
    });
  } catch (error) {
    // console.error(`Failed to login. Error: ${error}`);
    toast.error("Failed to login. Please check your credentials and your network connection.");
    return false;
  }
  let jwt = await loginResponse?.text();
  if (loginResponse.status !== 200) {
    // console.error(`Failed to login. Status: ${loginResponse.status}`);
    // console.error(loginResponse);
    toast.error("Failed to login. Please check your credentials.");
    return false;
  } else {
    localStorage.setItem("jwtToken", jwt);
    return true;
  }
}


export function handleEnterPress(event: React.KeyboardEvent<HTMLInputElement>, callback: () => void) {
  if (event.key === "Enter" && !event.shiftKey && !event.ctrlKey && !event.altKey && !event.metaKey) {
    event.preventDefault();
    event.stopPropagation();
    event.currentTarget.blur();
    callback();
  }
}