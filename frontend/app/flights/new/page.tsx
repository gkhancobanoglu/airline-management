"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  Box,
  Button,
  TextField,
  Typography,
  MenuItem,
  Paper,
  Snackbar,
  Alert,
} from "@mui/material";
import { createFlight, type FlightDTO } from "@/services/flightService";
import { getAirlines, type AirlineDTO } from "@/services/airlineService";
import { mapBackendMessageToUserFriendly } from "@/utils/errorMessageMapper";
import { getUserRole, isAuthenticated } from "@/services/authService";

interface FieldErrors {
  [key: string]: string;
}

interface BackendErrorBody {
  message?: string;
  errors?: Record<string, string>;
  fieldErrors?: Record<string, string>;
}

export default function NewFlightPage() {
  const router = useRouter();
  const [airlines, setAirlines] = useState<AirlineDTO[]>([]);
  const [form, setForm] = useState<Partial<FlightDTO>>({
    flightNumber: "",
    origin: "",
    destination: "",
    departureTime: "",
    arrivalTime: "",
    basePrice: undefined,
    capacity: 100,
    airlineId: undefined,
  });
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [loading, setLoading] = useState(true);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });

  useEffect(() => {
    const checkAccess = async () => {
      if (!isAuthenticated()) {
        router.push("/login");
        return;
      }
      const role = getUserRole();
      if (role !== "ADMIN") {
        router.push("/flights");
        return;
      }

      try {
        const data = await getAirlines();
        if (Array.isArray(data)) {
          setAirlines(data);
        } else if (
          typeof data === "object" &&
          data !== null &&
          "content" in data
        ) {
          setAirlines((data as { content: AirlineDTO[] }).content);
        } else {
          setAirlines([]);
        }
      } catch (err: unknown) {
        const msg =
          err instanceof Error ? err.message : "Error fetching airlines.";
        setSnackbar({ open: true, message: msg, severity: "error" });
      } finally {
        setLoading(false);
      }
    };

    void checkAccess();
  }, [router]);

  const handleSubmit = async (): Promise<void> => {
    setFieldErrors({});

    try {
      if (!form.airlineId) {
        setSnackbar({
          open: true,
          message: "Please select an airline.",
          severity: "error",
        });
        return;
      }

      await createFlight({
        ...form,
        flightNumber: form.flightNumber?.toUpperCase() || "",
      } as FlightDTO);

      setSnackbar({
        open: true,
        message: "Flight created successfully ✈️",
        severity: "success",
      });
      setTimeout(() => router.push("/flights"), 1000);
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

      if (data?.message) {
        setSnackbar({
          open: true,
          message: data.message,
          severity: "error",
        });
        return;
      }

      const msg =
        mapBackendMessageToUserFriendly(data?.message) ||
        "Error creating flight.";
      setSnackbar({ open: true, message: msg, severity: "error" });
    }
  };

  if (loading) {
    return (
      <Typography
        align="center"
        sx={{ mt: 6 }}
        color="text.secondary"
        variant="h6"
      >
        Loading Airlines...
      </Typography>
    );
  }

  return (
    <Paper sx={{ p: 4, maxWidth: 700, mx: "auto", mt: 4 }}>
      <Typography
        variant="h5"
        sx={{ mb: 3, fontWeight: 600, color: "#0D47A1" }}
      >
        Add New Flight
      </Typography>

      <Box display="grid" gridTemplateColumns="repeat(2, 1fr)" gap={2}>
        <TextField
          label="Flight Number"
          value={form.flightNumber}
          onChange={(e) =>
            setForm({ ...form, flightNumber: e.target.value.toUpperCase() })
          }
          error={!!fieldErrors.flightNumber}
          helperText={fieldErrors.flightNumber}
          fullWidth
        />
        <TextField
          label="Origin"
          value={form.origin}
          onChange={(e) => setForm({ ...form, origin: e.target.value })}
          error={!!fieldErrors.origin}
          helperText={fieldErrors.origin}
          fullWidth
        />
        <TextField
          label="Destination"
          value={form.destination}
          onChange={(e) => setForm({ ...form, destination: e.target.value })}
          error={!!fieldErrors.destination}
          helperText={fieldErrors.destination}
          fullWidth
        />
        <TextField
          label="Base Price (₺)"
          type="number"
          value={form.basePrice ?? ""}
          onChange={(e) =>
            setForm({ ...form, basePrice: parseFloat(e.target.value) })
          }
          error={!!fieldErrors.basePrice}
          helperText={fieldErrors.basePrice}
          fullWidth
        />
        <TextField
          label="Capacity"
          type="number"
          value={form.capacity}
          onChange={(e) =>
            setForm({ ...form, capacity: parseInt(e.target.value, 10) })
          }
          error={!!fieldErrors.capacity}
          helperText={fieldErrors.capacity}
          fullWidth
        />
        <TextField
          select
          label="Airline"
          value={form.airlineId || ""}
          onChange={(e) =>
            setForm({ ...form, airlineId: Number(e.target.value) })
          }
          error={!!fieldErrors.airlineId}
          helperText={fieldErrors.airlineId}
          fullWidth
        >
          {airlines.map((a) => (
            <MenuItem key={a.id} value={a.id}>
              {a.name}
            </MenuItem>
          ))}
        </TextField>

        <TextField
          label="Departure Time"
          type="datetime-local"
          value={form.departureTime || ""}
          onChange={(e) => setForm({ ...form, departureTime: e.target.value })}
          error={!!fieldErrors.departureTime}
          helperText={fieldErrors.departureTime}
          fullWidth
          InputLabelProps={{ shrink: true }}
        />
        <TextField
          label="Arrival Time"
          type="datetime-local"
          value={form.arrivalTime || ""}
          onChange={(e) => setForm({ ...form, arrivalTime: e.target.value })}
          error={!!fieldErrors.arrivalTime}
          helperText={fieldErrors.arrivalTime}
          fullWidth
          InputLabelProps={{ shrink: true }}
        />
      </Box>

      <Box display="flex" justifyContent="space-between" mt={4}>
        <Button variant="outlined" onClick={() => router.push("/flights")}>
          Cancel
        </Button>
        <Button variant="contained" onClick={handleSubmit}>
          Save FLIGHT
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
