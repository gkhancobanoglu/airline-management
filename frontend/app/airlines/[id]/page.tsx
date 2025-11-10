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
  getAirlineById,
  updateAirline,
  AirlineDTO,
} from "@/services/airlineService";
import { mapBackendMessageToUserFriendly } from "@/utils/errorMessageMapper";
import { isAuthenticated, getUserRole } from "@/services/authService";

export default function AirlineDetailPage() {
  const router = useRouter();
  const params = useParams();
  const id = Number(params.id);

  const [form, setForm] = useState<AirlineDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });

  const validateForm = (data: AirlineDTO): boolean => {
    const newErrors: Record<string, string> = {};

    if (!data.codeIATA?.trim()) {
      newErrors.codeIATA = "IATA code is required";
    } else if (!/^[A-Z0-9]{2}$/.test(data.codeIATA)) {
      newErrors.codeIATA =
        "IATA code must be exactly 2 characters (A-Z or digits)";
    }

    if (!data.codeICAO?.trim()) {
      newErrors.codeICAO = "ICAO code is required";
    } else if (!/^[A-Z0-9]{3}$/.test(data.codeICAO)) {
      newErrors.codeICAO =
        "ICAO code must be exactly 3 characters (A-Z or digits)";
    }

    if (!data.name?.trim()) {
      newErrors.name = "Airline name is required";
    } else if (data.name.length < 2 || data.name.length > 100) {
      newErrors.name = "Name must be between 2 and 100 characters";
    }

    if (!data.country?.trim()) {
      newErrors.country = "Country is required";
    } else if (!/^[A-Za-zğüşöçıİĞÜŞÖÇ\s'.-]{2,60}$/.test(data.country)) {
      newErrors.country = "Country must contain only letters (2–60 characters)";
    }

    if (!data.fleetSize?.trim()) {
      newErrors.fleetSize = "Fleet size is required";
    } else if (!/^[0-9]+$/.test(data.fleetSize)) {
      newErrors.fleetSize = "Fleet size must be a numeric value";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

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

    const fetchAirline = async () => {
      try {
        const data = await getAirlineById(id);
        setForm(data);
      } catch (err: unknown) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        const msg = mapBackendMessageToUserFriendly(
          axiosErr.response?.data?.message
        );
        setSnackbar({ open: true, message: msg, severity: "error" });
      } finally {
        setLoading(false);
      }
    };

    void fetchAirline();
  }, [id, router]);

  const handleUpdate = async () => {
    if (!form) return;
    if (!validateForm(form)) return;

    try {
      await updateAirline(id, form);
      setSnackbar({
        open: true,
        message: "Changes saved successfully ✅",
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

  if (loading)
    return (
      <Box display="flex" justifyContent="center" mt={10}>
        <CircularProgress />
      </Box>
    );

  if (!form)
    return (
      <Typography mt={4} textAlign="center">
        Airline not found.
      </Typography>
    );

  return (
    <Paper sx={{ p: 4, maxWidth: 600, mx: "auto", mt: 4 }}>
      <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
        Edit Airline
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
          onChange={(e) => setForm({ ...form, name: e.target.value })}
        />
        <TextField
          label="Country"
          value={form.country}
          error={!!errors.country}
          helperText={errors.country}
          onChange={(e) => setForm({ ...form, country: e.target.value })}
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
