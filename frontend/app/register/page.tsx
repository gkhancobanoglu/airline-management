"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import {
  Box,
  Button,
  TextField,
  Paper,
  Typography,
  Snackbar,
  Alert,
} from "@mui/material";
import { register } from "@/services/authService";

export default function RegisterPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
  });

  const [errors, setErrors] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
  });

  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });

  const validate = () => {
    const newErrors = { firstName: "", lastName: "", email: "", password: "" };
    let valid = true;

    if (!form.firstName.trim()) {
      newErrors.firstName = "First name is required.";
      valid = false;
    } else if (form.firstName.trim().length < 2) {
      newErrors.firstName = "First name must be at least 2 characters.";
      valid = false;
    }

    if (!form.lastName.trim()) {
      newErrors.lastName = "Last name is required.";
      valid = false;
    } else if (form.lastName.trim().length < 2) {
      newErrors.lastName = "Last name must be at least 2 characters.";
      valid = false;
    }

    if (!form.email.trim()) {
      newErrors.email = "Email is required.";
      valid = false;
    } else if (!/\S+@\S+\.\S+/.test(form.email)) {
      newErrors.email = "Please enter a valid email address.";
      valid = false;
    }

    if (!form.password.trim()) {
      newErrors.password = "Password is required.";
      valid = false;
    } else if (form.password.length < 8) {
      newErrors.password = "Password must be at least 8 characters.";
      valid = false;
    }

    setErrors(newErrors);
    return valid;
  };

  const handleRegister = async () => {
    if (!validate()) return;

    try {
      await register(form);
      setSnackbar({
        open: true,
        message: "Registration successful ✅ Redirecting to login...",
        severity: "success",
      });
      setTimeout(() => router.push("/login"), 1500);
    } catch (err: unknown) {
      console.log("FULL AXIOS ERROR:", err);

      const axiosErr = err as {
        response?: {
          data?: {
            message?: string;
            [key: string]: unknown;
          };
          status?: number;
          statusText?: string;
        };
        message?: string;
        toString?: () => string;
      };

      let rawMsg: string =
        (typeof axiosErr.response?.data?.message === "string"
          ? axiosErr.response?.data?.message
          : undefined) ||
        (typeof axiosErr.response?.data === "string"
          ? axiosErr.response?.data
          : undefined) ||
        axiosErr.message ||
        axiosErr.toString?.() ||
        "Unknown error";

      if (typeof rawMsg === "object") {
        rawMsg = JSON.stringify(rawMsg);
      }

      console.log("BACKEND MESSAGE:", rawMsg);

      if (rawMsg.toLowerCase().includes("email is already registered")) {
        rawMsg =
          "This email address is already registered. Please use another email.";
      }

      setSnackbar({
        open: true,
        message: `❌ ${rawMsg}`,
        severity: "error",
      });
    }
  };

  return (
    <Box
      display="flex"
      justifyContent="center"
      alignItems="center"
      height="100vh"
      sx={{ backgroundColor: "#f5f5f5" }}
    >
      <Paper
        sx={{
          p: 4,
          width: 400,
          textAlign: "center",
          borderRadius: 3,
          boxShadow: 4,
        }}
      >
        <Typography
          variant="h5"
          sx={{ mb: 3, color: "#0D47A1", fontWeight: 600 }}
        >
          Register New Account
        </Typography>

        <TextField
          label="First Name"
          fullWidth
          sx={{ mb: 2 }}
          value={form.firstName}
          onChange={(e) => setForm({ ...form, firstName: e.target.value })}
          error={!!errors.firstName}
          helperText={errors.firstName}
        />
        <TextField
          label="Last Name"
          fullWidth
          sx={{ mb: 2 }}
          value={form.lastName}
          onChange={(e) => setForm({ ...form, lastName: e.target.value })}
          error={!!errors.lastName}
          helperText={errors.lastName}
        />
        <TextField
          label="Email"
          fullWidth
          sx={{ mb: 2 }}
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          error={!!errors.email}
          helperText={errors.email}
        />
        <TextField
          label="Password"
          type="password"
          fullWidth
          sx={{ mb: 3 }}
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          error={!!errors.password}
          helperText={errors.password}
        />

        <Button
          fullWidth
          variant="contained"
          size="large"
          sx={{
            backgroundColor: "#0D47A1",
            "&:hover": { backgroundColor: "#08306B" },
            borderRadius: 2,
            py: 1.2,
          }}
          onClick={handleRegister}
        >
          REGISTER
        </Button>

        <Typography
          sx={{
            mt: 2,
            fontSize: 14,
            cursor: "pointer",
            color: "#1565C0",
            textDecoration: "underline",
          }}
          onClick={() => router.push("/login")}
        >
          Already have an account? Login
        </Typography>

        <Snackbar
          open={snackbar.open}
          autoHideDuration={4000}
          onClose={() => setSnackbar({ ...snackbar, open: false })}
          anchorOrigin={{ vertical: "top", horizontal: "center" }}
        >
          <Alert
            severity={snackbar.severity}
            variant="filled"
            sx={{ fontSize: "0.95rem", fontWeight: 500 }}
          >
            {snackbar.message}
          </Alert>
        </Snackbar>
      </Paper>
    </Box>
  );
}
