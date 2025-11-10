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
import { login } from "@/services/authService";
import { getUserRole } from "@/utils/auth";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });

  const handleLogin = async () => {
    if (!email || !password) {
      setSnackbar({
        open: true,
        message: "Please fill in all fields.",
        severity: "error",
      });
      return;
    }

    try {
      setLoading(true);
      await login(email, password);

      const role = getUserRole();
      setSnackbar({
        open: true,
        message: "Login successful ✅",
        severity: "success",
      });

      setTimeout(() => {
        if (role === "ADMIN") router.push("/airlines");
        else if (role === "USER") router.push("/flights");
        else router.push("/");
      }, 800);
    } catch (err: unknown) {
      const axiosErr = err as {
        response?: { data?: { message?: string }; status?: number };
        message?: string;
      };

      let msg =
        axiosErr.response?.data?.message ||
        axiosErr.message ||
        "Login failed. Please check your credentials and try again.";

      if (
        msg.toLowerCase().includes("invalid email") ||
        msg.toLowerCase().includes("invalid password") ||
        axiosErr.response?.status === 401
      ) {
        msg = "❌ Invalid email or password. Please try again.";
      }

      if (
        msg.toLowerCase().includes("user not found") ||
        msg.toLowerCase().includes("not found")
      ) {
        msg = "❌ No account found with this email address.";
      }

      if (
        msg.toLowerCase().includes("forbidden") ||
        axiosErr.response?.status === 403
      ) {
        msg = "🚫 Access denied. You don’t have permission to log in.";
      }

      setSnackbar({
        open: true,
        message: msg,
        severity: "error",
      });
    } finally {
      setLoading(false);
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
          Airline System Login
        </Typography>

        <TextField
          label="Email"
          fullWidth
          sx={{ mb: 2 }}
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <TextField
          label="Password"
          type="password"
          fullWidth
          sx={{ mb: 3 }}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <Button
          fullWidth
          variant="contained"
          size="large"
          disabled={loading}
          onClick={handleLogin}
          sx={{
            backgroundColor: "#0D47A1",
            "&:hover": { backgroundColor: "#08306B" },
            borderRadius: 2,
            py: 1.2,
          }}
        >
          {loading ? "Logging in..." : "LOGIN"}
        </Button>

        <Typography
          sx={{
            mt: 2,
            fontSize: 14,
            cursor: "pointer",
            color: "#1565C0",
            textDecoration: "underline",
          }}
          onClick={() => router.push("/register")}
        >
          Don’t have an account? Register
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
