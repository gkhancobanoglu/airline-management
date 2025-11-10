"use client";
import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import {
  Box,
  Button,
  TextField,
  Typography,
  Paper,
  Snackbar,
  Alert,
  CircularProgress,
} from "@mui/material";
import {
  getPassengerById,
  updatePassenger,
  PassengerDTO,
  checkEmailUnique,
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

export default function EditPassengerPage() {
  const router = useRouter();
  const params = useParams();
  const id = Number(params.id);

  const [form, setForm] = useState<PassengerDTO | null>(null);
  const [originalData, setOriginalData] = useState<PassengerDTO | null>(null);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [loading, setLoading] = useState(true);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error" | "info",
  });

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push("/login");
      return;
    }

    const role = getUserRole();
    if (role !== "ADMIN") {
      router.push("/flights");
      return;
    }

    const fetchData = async () => {
      try {
        const data = await getPassengerById(id);
        setForm(data);
        setOriginalData(data);
      } catch (err: unknown) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        const msg =
          mapBackendMessageToUserFriendly(axiosErr.response?.data?.message) ||
          "Failed to load passenger.";
        setSnackbar({ open: true, message: msg, severity: "error" });
      } finally {
        setLoading(false);
      }
    };

    void fetchData();
  }, [id, router]);

  const handleUpdate = async () => {
    if (!form) return;
    setFieldErrors({});

    if (JSON.stringify(form) === JSON.stringify(originalData)) {
      setSnackbar({
        open: true,
        message: "No changes detected.",
        severity: "info",
      });
      return;
    }

    if (form.email !== originalData?.email) {
      const unique = await checkEmailUnique(form.email);
      if (!unique) {
        setFieldErrors({ email: "This email is already registered." });
        return;
      }
    }

    try {
      await updatePassenger(id, form);
      setSnackbar({
        open: true,
        message: "Passenger updated successfully ✅",
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
        "Operation failed. Please check the data and try again.";
      setSnackbar({ open: true, message: backendMsg, severity: "error" });
    }
  };

  if (loading)
    return (
      <Box display="flex" justifyContent="center" mt={10}>
        <CircularProgress />
      </Box>
    );

  if (!form)
    return (
      <Typography textAlign="center" mt={4}>
        Passenger not found.
      </Typography>
    );

  return (
    <Paper sx={{ p: 4, maxWidth: 700, mx: "auto", mt: 4 }}>
      <Typography
        variant="h5"
        sx={{ mb: 3, fontWeight: 600, color: "#0D47A1" }}
      >
        Edit Passenger
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
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          error={!!fieldErrors.email}
          helperText={fieldErrors.email}
          fullWidth
        />
        <TextField
          label="Loyalty Points"
          value={form.loyaltyPoints ?? 0}
          onChange={(e) =>
            setForm({
              ...form,
              loyaltyPoints: Number(e.target.value) || 0,
            })
          }
          type="number"
          fullWidth
        />
      </Box>

      <Box display="flex" justifyContent="space-between" mt={4}>
        <Button variant="outlined" onClick={() => router.push("/passengers")}>
          Cancel
        </Button>
        <Button variant="contained" onClick={handleUpdate}>
          Save Changes
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
