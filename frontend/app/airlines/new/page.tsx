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
import { createAirline, AirlineDTO } from "@/services/airlineService";
import { mapBackendMessageToUserFriendly } from "@/utils/errorMessageMapper";
import { isAuthenticated, getUserRole } from "@/services/authService";

export default function NewAirlinePage() {
  const router = useRouter();
  const [form, setForm] = useState<AirlineDTO>({
    id: 0,
    codeIATA: "",
    codeICAO: "",
    name: "",
    country: "",
    fleetSize: "",
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push("/login");
      return;
    }

    const role = getUserRole();
    if (role !== "ADMIN") {
      router.push("/");
      return;
    }

    queueMicrotask(() => setLoading(false));
  }, [router]);

  const validateForm = (data: AirlineDTO): boolean => {
    const newErrors: Record<string, string> = {};

    if (!data.codeIATA.trim()) {
      newErrors.codeIATA = "IATA code is required";
    } else if (!/^[A-Z0-9]{2}$/.test(data.codeIATA)) {
      newErrors.codeIATA =
        "IATA code must be exactly 2 uppercase letters or digits";
    }

    if (!data.codeICAO.trim()) {
      newErrors.codeICAO = "ICAO code is required";
    } else if (!/^[A-Z0-9]{3}$/.test(data.codeICAO)) {
      newErrors.codeICAO =
        "ICAO code must be exactly 3 uppercase letters or digits";
    }

    if (!data.name.trim()) {
      newErrors.name = "Airline name is required";
    } else if (data.name.length < 2 || data.name.length > 100) {
      newErrors.name = "Airline name must be between 2 and 100 characters";
    }

    if (!data.country.trim()) {
      newErrors.country = "Country is required";
    } else if (data.country.length < 2 || data.country.length > 60) {
      newErrors.country = "Country name must be between 2 and 60 characters";
    } else if (!/^[A-Za-zğüşöçıİĞÜŞÖÇ\s().'-]+$/.test(data.country)) {
      newErrors.country = "Country name must contain only letters";
    }

    if (!data.fleetSize.trim()) {
      newErrors.fleetSize = "Fleet size is required";
    } else if (!/^[0-9]+$/.test(data.fleetSize)) {
      newErrors.fleetSize = "Fleet size must be a numeric value";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async () => {
    if (!validateForm(form)) return;

    try {
      await createAirline(form);
      setSnackbar({
        open: true,
        message: "New airline added successfully ✈️",
        severity: "success",
      });
      setTimeout(() => router.push("/airlines"), 1000);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      const msg = mapBackendMessageToUserFriendly(
        axiosErr.response?.data?.message
      );
      setSnackbar({ open: true, message: msg, severity: "error" });
    }
  };

  if (loading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        height="80vh"
      >
        <Typography variant="h6" color="text.secondary">
          Loading page...
        </Typography>
      </Box>
    );
  }

  return (
    <Paper sx={{ p: 4, maxWidth: 600, mx: "auto", mt: 4 }}>
      <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
        Add New Airline
      </Typography>

      <Box display="grid" gridTemplateColumns="repeat(2, 1fr)" gap={2}>
        <TextField
          label="IATA Code"
          value={form.codeIATA}
          error={!!errors.codeIATA}
          helperText={errors.codeIATA}
          inputProps={{ maxLength: 2 }}
          onChange={(e) =>
            setForm({
              ...form,
              codeIATA: e.target.value.toUpperCase().slice(0, 2),
            })
          }
        />
        <TextField
          label="ICAO Code"
          value={form.codeICAO}
          error={!!errors.codeICAO}
          helperText={errors.codeICAO}
          inputProps={{ maxLength: 3 }}
          onChange={(e) =>
            setForm({
              ...form,
              codeICAO: e.target.value.toUpperCase().slice(0, 3),
            })
          }
        />
        <TextField
          label="Name"
          value={form.name}
          error={!!errors.name}
          helperText={errors.name}
          inputProps={{ maxLength: 100 }}
          onChange={(e) =>
            setForm({ ...form, name: e.target.value.slice(0, 100) })
          }
        />
        <TextField
          label="Country"
          value={form.country}
          error={!!errors.country}
          helperText={errors.country}
          inputProps={{ maxLength: 60 }}
          onChange={(e) =>
            setForm({ ...form, country: e.target.value.slice(0, 60) })
          }
        />
        <TextField
          label="Fleet Size"
          value={form.fleetSize}
          error={!!errors.fleetSize}
          helperText={errors.fleetSize}
          inputProps={{ maxLength: 6 }}
          onChange={(e) =>
            setForm({
              ...form,
              fleetSize: e.target.value.replace(/[^0-9]/g, "").slice(0, 6),
            })
          }
        />
      </Box>

      <Box display="flex" justifyContent="space-between" mt={4}>
        <Button variant="outlined" onClick={() => router.push("/airlines")}>
          Cancel
        </Button>
        <Button variant="contained" onClick={handleSubmit}>
          Save AIRLINE
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
