"use server"
export default async function getBackendUrl() {
    return process.env.BACKEND_URL;
}