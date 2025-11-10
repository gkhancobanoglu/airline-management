"use client";
import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import {
  Box,
  Button,
  TextField,
  Typography,
  Paper,
  Snackbar,
  Alert,
  MenuItem,
  CircularProgress,
} from "@mui/material";
import {
  createBooking,
  type BookingCreateRequest,
} from "@/services/bookingService";
import { getFlights, type FlightDTO } from "@/services/flightService";
import { getPassengers, type PassengerDTO } from "@/services/passengerService";
import { getUserRole, isAuthenticated } from "@/services/authService";

type FieldErrors = Record<string, string>;
type BackendErrorBody = {
  message?: string;
  errors?: Record<string, string>;
  fieldErrors?: Record<string, string>;
};

export default function NewBookingPage() {
  const router = useRouter();
  const [flights, setFlights] = useState<FlightDTO[]>([]);
  const [passengers, setPassengers] = useState<PassengerDTO[]>([]);
  const [form, setForm] = useState<BookingCreateRequest>({
    flightId: 0,
    seatNumber: "",
  });
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [role, setRole] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });

  const isAdmin = role === "ADMIN";

  useEffect(() => {
    const init = async () => {
      try {
        if (!isAuthenticated()) {
          router.push("/login");
          return;
        }

        const userRole = getUserRole();
        setRole(userRole);

        if (userRole !== "ADMIN" && userRole !== "USER") {
          router.push("/bookings");
          return;
        }

        const flightRes = await getFlights(0, 1000);
        const flightList =
          typeof flightRes === "object" &&
          flightRes !== null &&
          "content" in flightRes
            ? (flightRes as { content: FlightDTO[] }).content
            : (flightRes as unknown as FlightDTO[]);

        const now = new Date();
        const validFlights = (flightList ?? []).filter(
          (f) => new Date(f.departureTime) > now
        );

        setFlights(validFlights);

        if (userRole === "ADMIN") {
          const passengerRes = await getPassengers(0, 1000);
          const passengerList =
            typeof passengerRes === "object" &&
            passengerRes !== null &&
            "content" in passengerRes
              ? (passengerRes as { content: PassengerDTO[] }).content
              : (passengerRes as unknown as PassengerDTO[]);
          setPassengers(Array.isArray(passengerList) ? passengerList : []);
        }
      } catch {
        setSnackbar({
          open: true,
          message: "Failed to load booking form data.",
          severity: "error",
        });
      } finally {
        setLoading(false);
      }
    };

    void init();
  }, [router]);

  const selectedFlight = useMemo(
    () => flights.find((f) => f.id === form.flightId),
    [flights, form.flightId]
  );

  const isOverbookedFE =
    selectedFlight &&
    typeof selectedFlight.bookedSeats === "number" &&
    typeof selectedFlight.capacity === "number" &&
    selectedFlight.bookedSeats >= Math.round(selectedFlight.capacity * 1.1);

  const handleSubmit = async () => {
    setFieldErrors({});

    try {
      if (!form.flightId || !form.seatNumber.trim()) {
        setSnackbar({
          open: true,
          message: "Please fill in all required fields.",
          severity: "error",
        });
        return;
      }

      if (isAdmin && !form.passengerId) {
        setSnackbar({
          open: true,
          message: "Please select a passenger.",
          severity: "error",
        });
        return;
      }

      if (isOverbookedFE) {
        setSnackbar({
          open: true,
          message:
            "This flight cannot accept more bookings — overbooking limit (110%) reached.",
          severity: "error",
        });
        return;
      }

      const payload: BookingCreateRequest = isAdmin
        ? form
        : { flightId: form.flightId, seatNumber: form.seatNumber };

      await createBooking(payload);

      setSnackbar({
        open: true,
        message: "Booking created successfully ✈️",
        severity: "success",
      });

      setTimeout(() => router.push("/bookings"), 1200);
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

      let msg = data?.message || "Operation failed.";

      const low = msg.toLowerCase();
      if (low.includes("seat")) msg = "This seat is already taken.";
      else if (low.includes("overbooking"))
        msg =
          "This flight cannot accept more bookings — overbooking limit (110%) reached.";
      else if (low.includes("full"))
        msg = "This flight has reached full capacity.";
      else if (low.includes("passenger already has"))
        msg = "Passenger already has a booking for this flight.";
      else if (low.includes("departed"))
        msg = "Flight not available or already departed.";

      setSnackbar({ open: true, message: msg, severity: "error" });
    }
  };

  if (loading)
    return (
      <Box display="flex" justifyContent="center" alignItems="center" mt={10}>
        <CircularProgress />
        <Typography sx={{ ml: 2, color: "text.secondary" }}>
          Loading Booking Form...
        </Typography>
      </Box>
    );

  return (
    <Paper sx={{ p: 4, maxWidth: 700, mx: "auto", mt: 4 }}>
      <Typography
        variant="h5"
        sx={{ mb: 3, fontWeight: 600, color: "#0D47A1" }}
      >
        {isAdmin ? "Create Booking (Admin)" : "Book a Flight"}
      </Typography>

      <Box display="grid" gridTemplateColumns="repeat(2, 1fr)" gap={2}>
        <TextField
          select
          label="Flight"
          value={form.flightId || ""}
          onChange={(e) =>
            setForm({ ...form, flightId: Number(e.target.value) })
          }
          error={!!fieldErrors.flightId}
          helperText={
            fieldErrors.flightId ||
            (isOverbookedFE ? "Overbooking limit reached for this flight." : "")
          }
          fullWidth
        >
          {flights.map((f) => {
            const booked = f.bookedSeats ?? 0;
            const cap = f.capacity ?? 0;
            const full = booked >= cap;
            const over = booked >= Math.round(cap * 1.1);
            return (
              <MenuItem key={f.id} value={f.id} disabled={over}>
                {f.flightNumber} – {f.origin} → {f.destination}
                {over
                  ? "  (Overbooking 110% reached)"
                  : full
                  ? "  (Full / waitlist)"
                  : ""}
              </MenuItem>
            );
          })}
        </TextField>

        {isAdmin && (
          <TextField
            select
            label="Passenger"
            value={form.passengerId || ""}
            onChange={(e) =>
              setForm({ ...form, passengerId: Number(e.target.value) })
            }
            error={!!fieldErrors.passengerId}
            helperText={fieldErrors.passengerId}
            fullWidth
          >
            {passengers.map((p) => (
              <MenuItem key={p.id} value={p.id}>
                {p.name} {p.surname} — {p.email}
              </MenuItem>
            ))}
          </TextField>
        )}

        <TextField
          label="Seat Number"
          value={form.seatNumber}
          onChange={(e) =>
            setForm({ ...form, seatNumber: e.target.value.toUpperCase() })
          }
          error={!!fieldErrors.seatNumber}
          helperText={fieldErrors.seatNumber}
          fullWidth
          inputProps={{ maxLength: 5 }}
        />
      </Box>

      <Box display="flex" justifyContent="space-between" mt={4}>
        <Button variant="outlined" onClick={() => router.push("/bookings")}>
          Cancel
        </Button>
        <Button variant="contained" onClick={handleSubmit}>
          Save BOOKING
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
