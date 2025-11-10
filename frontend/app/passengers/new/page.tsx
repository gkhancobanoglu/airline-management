"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  Box,
  Button,
  TextField,
  Typography,
  Paper,
  Snackbar,
  Alert,
} from "@mui/material";
import {
  createPassenger,
  checkEmailUnique,
  PassengerDTO,
} from "@/services/passengerService";
import { getUserRole, isAuthenticated } from "@/services/authService";
import { mapBackendMessageToUserFriendly } from "@/utils/errorMessageMapper";

interface FieldErrors {
  [key: string]: string;
}
interface BackendErrorBody {
  message?: string;
  errors?: Record<string, string>;
  fieldErrors?: Record<string, string>;
}

export default function NewPassengerPage() {
  const router = useRouter();

  const [form, setForm] = useState<Partial<PassengerDTO>>({
    name: "",
    surname: "",
    email: "",
    loyaltyPoints: 0,
  });

  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [emailError, setEmailError] = useState<string | null>(null);
  const [role, setRole] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push("/login");
      return;
    }
    Promise.resolve().then(() => {
      const userRole = getUserRole();
      setRole(userRole);
      if (userRole !== "ADMIN") {
        router.push("/flights");
        return;
      }
      setLoading(false);
    });
  }, [router]);

  const handleEmailBlur = async () => {
    if (!form.email) return;
    try {
      const unique = await checkEmailUnique(form.email);
      setEmailError(unique ? null : "This email is already registered.");
    } catch {
      setEmailError("Failed to check email uniqueness.");
    }
  };

  const handleSubmit = async () => {
    setFieldErrors({});
    try {
      if (emailError) {
        setSnackbar({
          open: true,
          message: "Please fix the email error before saving.",
          severity: "error",
        });
        return;
      }

      if (!form.name || !form.surname || !form.email) {
        setSnackbar({
          open: true,
          message: "Please fill in all required fields.",
          severity: "error",
        });
        return;
      }
      await createPassenger(form as PassengerDTO);
      setSnackbar({
        open: true,
        message: "Passenger created successfully 👤",
        severity: "success",
      });
      setTimeout(() => router.push("/passengers"), 1000);
    } catch (err: unknown) {
      let data: BackendErrorBody | undefined;

      if (
        typeof err === "object" &&
        err !== null &&
        "response" in err &&
        typeof (err as Record<string, unknown>).response === "object" &&
        (err as Record<string, unknown>).response !== null &&
        "data" in (err as { response: Record<string, unknown> }).response
      ) {
        const resp = (err as { response: { data?: unknown } }).response;
        if (resp.data && typeof resp.data === "object") {
          data = resp.data as BackendErrorBody;
        }
      }

      const possible = data?.errors ?? data?.fieldErrors;
      if (possible && typeof possible === "object") {
        const formatted: FieldErrors = {};
        Object.entries(possible).forEach(([key, value]) => {
          if (typeof value === "string") formatted[key] = value;
        });
        setFieldErrors(formatted);
        return;
      }

      const backendMsg =
        data?.message ||
        mapBackendMessageToUserFriendly(data?.message) ||
        "Failed to create passenger. Please try again.";
      setSnackbar({ open: true, message: backendMsg, severity: "error" });
    }
  };

  if (loading)
    return (
      <Typography align="center" sx={{ mt: 6, color: "text.secondary" }}>
        Loading...
      </Typography>
    );

  if (role !== "ADMIN") return null;

  return (
    <Paper sx={{ p: 4, maxWidth: 700, mx: "auto", mt: 4 }}>
      <Typography
        variant="h5"
        sx={{ mb: 3, fontWeight: 600, color: "#0D47A1" }}
      >
        Add New Passenger
      </Typography>

      <Box display="grid" gridTemplateColumns="repeat(2, 1fr)" gap={2}>
        <TextField
          label="First Name"
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
          error={!!fieldErrors.name}
          helperText={fieldErrors.name}
          fullWidth
        />
        <TextField
          label="Last Name"
          value={form.surname}
          onChange={(e) => setForm({ ...form, surname: e.target.value })}
          error={!!fieldErrors.surname}
          helperText={fieldErrors.surname}
          fullWidth
        />
        <TextField
          label="Email"
          value={form.email}
          onChange={(e) => {
            setForm({ ...form, email: e.target.value });
            setEmailError(null);
          }}
          onBlur={handleEmailBlur}
          error={!!emailError || !!fieldErrors.email}
          helperText={emailError || fieldErrors.email || ""}
          fullWidth
        />
        <TextField
          label="Loyalty Points"
          value={form.loyaltyPoints ?? 0}
          disabled
          fullWidth
        />
      </Box>

      <Box display="flex" justifyContent="space-between" mt={4}>
        <Button variant="outlined" onClick={() => router.push("/passengers")}>
          Cancel
        </Button>
        <Button variant="contained" onClick={handleSubmit}>
          Save Passenger
        </Button>
      </Box>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: "top", horizontal: "center" }}
        sx={{ mt: 3 }}
      >
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Paper>
  );
}
