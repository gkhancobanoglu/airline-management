"use client";
import { useEffect, useState, useMemo } from "react";
import { useParams, useRouter } from "next/navigation";
import {
  Box,
  Button,
  TextField,
  Typography,
  MenuItem,
  Paper,
  Snackbar,
  Alert,
  CircularProgress,
} from "@mui/material";
import {
  getFlightById,
  updateFlight,
  FlightDTO,
} from "@/services/flightService";
import { getAirlines, AirlineDTO } from "@/services/airlineService";
import { mapBackendMessageToUserFriendly } from "@/utils/errorMessageMapper";
import { getUserRole, isAuthenticated } from "@/services/authService";

export default function EditFlightPage() {
  const router = useRouter();
  const params = useParams();

  const id = useMemo(() => {
    const raw = params?.id;
    if (!raw) return null;
    if (Array.isArray(raw)) return Number(raw[0]);
    const n = Number(raw);
    return Number.isFinite(n) ? n : null;
  }, [params]);

  const [form, setForm] = useState<FlightDTO | null>(null);
  const [originalData, setOriginalData] = useState<FlightDTO | null>(null);
  const [airlines, setAirlines] = useState<AirlineDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [role, setRole] = useState<string | null>(null);
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

    const userRole = getUserRole();
    setRole(userRole);

    if (id === null) return;

    void (async () => {
      try {
        const flightRes = await getFlightById(id);

        let airlineList: AirlineDTO[] = [];
        if (userRole === "ADMIN") {
          const airlineRes = await getAirlines();
          const maybePaged = airlineRes as unknown;
          airlineList = Array.isArray(maybePaged)
            ? maybePaged
            : "content" in (maybePaged as object)
            ? (maybePaged as { content: AirlineDTO[] }).content
            : [];
        }

        const data =
          typeof flightRes === "object" &&
          flightRes !== null &&
          "data" in flightRes
            ? (flightRes as { data: FlightDTO }).data
            : (flightRes as FlightDTO);

        setForm(data);
        setOriginalData(data);
        setAirlines(airlineList);
      } catch (err: unknown) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        const msg =
          axiosErr.response?.data?.message ||
          mapBackendMessageToUserFriendly(axiosErr.response?.data?.message) ||
          "Error loading flight.";
        setSnackbar({ open: true, message: msg, severity: "error" });
      } finally {
        setLoading(false);
      }
    })();
  }, [id, router]);

  const handleUpdate = async () => {
    if (!form || !originalData || role !== "ADMIN" || id === null) return;

    const noChanges = JSON.stringify(form) === JSON.stringify(originalData);
    if (noChanges) {
      setSnackbar({
        open: true,
        message: "No changes detected",
        severity: "info",
      });
      return;
    }

    try {
      await updateFlight(id, {
        ...form,
        flightNumber: form.flightNumber.toUpperCase(),
      });
      setSnackbar({
        open: true,
        message: "Flight updated successfully ✅",
        severity: "success",
      });
      setTimeout(() => router.push("/flights"), 1000);
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } };
      const msg =
        e.response?.data?.message ||
        mapBackendMessageToUserFriendly(e.response?.data?.message) ||
        "Error updating flight.";
      setSnackbar({ open: true, message: msg, severity: "error" });
    }
  };

  if (id === null)
    return (
      <Box display="flex" justifyContent="center" mt={10}>
        <Typography>Invalid flight ID.</Typography>
      </Box>
    );

  if (loading)
    return (
      <Box display="flex" justifyContent="center" mt={10}>
        <CircularProgress />
      </Box>
    );

  if (!form)
    return (
      <Typography textAlign="center" mt={4}>
        Flight not found.
      </Typography>
    );

  const isAdmin = role === "ADMIN";

  return (
    <Paper sx={{ p: 4, maxWidth: 700, mx: "auto", mt: 4 }}>
      <Typography
        variant="h5"
        sx={{ mb: 3, fontWeight: 600, color: "#0D47A1" }}
      >
        {isAdmin ? "Edit Flight " : "Flight Details"}
      </Typography>

      <Box display="grid" gridTemplateColumns="repeat(2, 1fr)" gap={2}>
        <TextField
          label="Flight Number"
          value={form.flightNumber}
          fullWidth
          disabled={!isAdmin}
          onChange={(e) =>
            isAdmin &&
            setForm({ ...form, flightNumber: e.target.value.toUpperCase() })
          }
        />
        <TextField
          label="Origin"
          value={form.origin}
          fullWidth
          disabled={!isAdmin}
          onChange={(e) =>
            isAdmin && setForm({ ...form, origin: e.target.value })
          }
        />
        <TextField
          label="Destination"
          value={form.destination}
          fullWidth
          disabled={!isAdmin}
          onChange={(e) =>
            isAdmin && setForm({ ...form, destination: e.target.value })
          }
        />
        <TextField
          label="Base Price (₺)"
          type="number"
          value={form.basePrice}
          fullWidth
          disabled={!isAdmin}
          onChange={(e) =>
            isAdmin &&
            setForm({ ...form, basePrice: parseFloat(e.target.value) })
          }
        />
        <TextField
          label="Capacity"
          type="number"
          value={form.capacity}
          fullWidth
          disabled={!isAdmin}
          onChange={(e) =>
            isAdmin && setForm({ ...form, capacity: parseInt(e.target.value) })
          }
        />
        {isAdmin ? (
          <TextField
            select
            label="Airline"
            value={form.airlineId || ""}
            onChange={(e) =>
              setForm({ ...form, airlineId: Number(e.target.value) })
            }
            fullWidth
          >
            {airlines.map((a) => (
              <MenuItem key={a.id} value={a.id}>
                {a.name}
              </MenuItem>
            ))}
          </TextField>
        ) : (
          <TextField
            label="Airline"
            value={form.airlineName || "Unknown"}
            fullWidth
            disabled
          />
        )}
        <TextField
          label="Departure Time"
          type="datetime-local"
          value={form.departureTime.slice(0, 16)}
          fullWidth
          InputLabelProps={{ shrink: true }}
          disabled={!isAdmin}
          onChange={(e) =>
            isAdmin && setForm({ ...form, departureTime: e.target.value })
          }
        />
        <TextField
          label="Arrival Time"
          type="datetime-local"
          value={form.arrivalTime.slice(0, 16)}
          fullWidth
          InputLabelProps={{ shrink: true }}
          disabled={!isAdmin}
          onChange={(e) =>
            isAdmin && setForm({ ...form, arrivalTime: e.target.value })
          }
        />
      </Box>

      <Box display="flex" justifyContent="space-between" mt={4}>
        <Button variant="outlined" onClick={() => router.push("/flights")}>
          {isAdmin ? "Cancel" : "Back"}
        </Button>
        {isAdmin && (
          <Button variant="contained" onClick={handleUpdate}>
            Save Changes
          </Button>
        )}
      </Box>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: "top", horizontal: "center" }}
      >
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Paper>
  );
}
