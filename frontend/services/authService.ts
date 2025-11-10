import api from "@/app/api/axios";

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export const register = async (data: RegisterRequest): Promise<string> => {
  try {
    const res = await api.post<string>("/auth/register", data);
    return res.data;
  } catch (err: unknown) {
    const msg = extractAxiosMessage(err, "Registration failed");
    throw new Error(msg);
  }
};

export const login = async (email: string, password: string): Promise<void> => {
  try {
    const res = await api.post<string>(
      `/auth/login?email=${encodeURIComponent(
        email
      )}&password=${encodeURIComponent(password)}`
    );

    const token = res.data;
    if (!token || typeof token !== "string") {
      throw new Error("No token received from server");
    }

    if (typeof window !== "undefined") {
      localStorage.setItem("token", token);
    }
  } catch (err: unknown) {
    const msg = extractAxiosMessage(err, "Invalid email or password");
    throw new Error(msg);
  }
};

export const getToken = (): string | null => {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("token");
};

export const getUserRole = (): string | null => {
  const token = getToken();
  if (!token) return null;

  try {
    const base64Payload = token.split(".")[1];
    const decodedPayload = JSON.parse(atob(base64Payload));
    let role = decodedPayload.role || null;

    if (typeof role === "string" && role.startsWith("ROLE_")) {
      role = role.replace("ROLE_", "");
    }
    return role;
  } catch {
    return null;
  }
};

export const isAuthenticated = (): boolean => {
  const token = getToken();
  if (!token) return false;

  try {
    const base64Payload = token.split(".")[1];
    const decodedPayload = JSON.parse(atob(base64Payload));
    const exp = decodedPayload.exp ? decodedPayload.exp * 1000 : 0;
    return Date.now() < exp;
  } catch {
    return false;
  }
};

export const logout = (): void => {
  if (typeof window !== "undefined") {
    localStorage.removeItem("token");
  }
};

function extractAxiosMessage(err: unknown, fallback: string): string {
  if (isAxiosErrorLike(err)) {
    const responseData = err.response?.data as
      | { message?: string; error?: string }
      | undefined;
    return responseData?.message || responseData?.error || fallback;
  }

  if (err instanceof Error && err.message) return err.message;

  return fallback;
}

function isAxiosErrorLike(
  err: unknown
): err is { isAxiosError: boolean; response?: { data?: unknown } } {
  return (
    typeof err === "object" &&
    err !== null &&
    "isAxiosError" in err &&
    (err as Record<string, unknown>).isAxiosError === true
  );
}
