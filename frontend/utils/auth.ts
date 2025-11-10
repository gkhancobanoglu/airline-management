import { jwtDecode } from "jwt-decode";

export type AppRole = "ADMIN" | "USER" | null;

export const getUserRole = (): AppRole => {
  const token =
    typeof window !== "undefined" ? localStorage.getItem("access_token") : null;
  if (!token) return null;
  try {
    const decoded = jwtDecode<{ role?: string }>(token);
    if (decoded.role?.includes("ADMIN")) return "ADMIN";
    if (decoded.role?.includes("USER")) return "USER";
    return null;
  } catch {
    return null;
  }
};

export const getUsername = (): string | null => {
  const token =
    typeof window !== "undefined" ? localStorage.getItem("access_token") : null;
  if (!token) return null;
  try {
    const decoded = jwtDecode<{ sub: string }>(token);
    return decoded.sub;
  } catch {
    return null;
  }
};
