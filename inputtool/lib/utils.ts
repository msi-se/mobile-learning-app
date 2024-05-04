import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"
import { toast } from "sonner";
import Router from "next/router";
import { Course, FeedbackForm, FeedbackQuestion } from "./models";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function getBackendUrl() {
  const currentUrl = window.location.href;
  return currentUrl.includes("localhost") ? "http://localhost:8080" : `${currentUrl}/api`;
}

export async function hasValidJwtToken(): Promise<boolean> {
  const jwtToken = localStorage.getItem("jwtToken");
  if (!jwtToken) {
    return false;
  }

  // check if the token is still valid
  const BACKEND_URL = getBackendUrl();

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
  // if (!username || !password) {
  //   console.error("Username and password are required.");
  //   toast.error("Username and password are required.");
  //   return false;
  // }
  // DEBUG: password is not required
  if (!username) {
    console.error("Username is required.");
    toast.error("Username is required.");
    return false;
  }

  const BACKEND_URL = getBackendUrl();

  let loginResponse = await fetch(`${BACKEND_URL}/user/login`, {
    method: "POST",
    headers: {
      "AUTHORIZATION": "Basic " + btoa(`${username}:${password}`)
    }
  });
  let jwt = await loginResponse.text();
  if (loginResponse.status !== 200) {
    // console.error(`Failed to login. Status: ${loginResponse.status}`);
    // console.error(loginResponse);
    toast.error("Failed to login. Please check your credentials.");
    return false;
  }
  localStorage.setItem("jwtToken", jwt);
  return true;
}
