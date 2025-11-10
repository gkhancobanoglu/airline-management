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
  Divider,
} from "@mui/material";
import {
  getBookingById,
  cancelBooking,
  type BookingDTO,
} from "@/services/bookingService";
import { getFlightById, type FlightDTO } from "@/services/flightService";
import {
  getPassengerById,
  updatePassenger,
  type PassengerDTO,
} from "@/services/passengerService";
import { getUserRole, isAuthenticated } from "@/services/authService";
import { mapBackendMessageToUserFriendly } from "@/utils/errorMessageMapper";

type FieldErrors = Record<string, string>;

export default function BookingDetailPage() {
  const router = useRouter();
  const params = useParams();
  const id = Number(params.id);

  const [booking, setBooking] = useState<BookingDTO | null>(null);
  const [flight, setFlight] = useState<FlightDTO | null>(null);
  const [passenger, setPassenger] = useState<PassengerDTO | null>(null);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [role, setRole] = useState<string | null>(null);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });

  const isAdmin = role === "ADMIN";
  const isUser = role === "USER";

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push("/login");
      return;
    }

    const detectedRole = getUserRole();
    setRole(detectedRole);
  }, [router]);

  useEffect(() => {
    if (!role) return; // 🟢 Role belli olmadan fetchData çalışmasın

    const fetchData = async () => {
      try {
        const bookingData = await getBookingById(id);
        if (!bookingData) throw new Error("Booking not found");
        setBooking(bookingData);

        const flightData = await getFlightById(bookingData.flightId);
        setFlight(flightData ?? null);

        if (role === "ADMIN" && bookingData.passengerId) {
          const passengerData = await getPassengerById(bookingData.passengerId);
          setPassenger(passengerData ?? null);
        } else if (role === "USER") {
          setPassenger({
            id: bookingData.passengerId ?? 0,
            name: "Private Passenger",
            surname: "",
            email: "",
            loyaltyPoints: 0,
          } as PassengerDTO);
        }
      } catch (err: unknown) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        const msg =
          mapBackendMessageToUserFriendly(axiosErr.response?.data?.message) ||
          "Error loading booking details";
        setSnackbar({ open: true, message: msg, severity: "error" });
      } finally {
        setLoading(false);
      }
    };

    void fetchData();
  }, [id, role]);

  const handleCancel = async () => {
    if (!booking?.id) {
      setSnackbar({
        open: true,
        message: "Booking ID not found",
        severity: "error",
      });
      return;
    }

    if (!confirm("Are you sure you want to cancel this booking?")) return;

    try {
      await cancelBooking(booking.id);
      setSnackbar({
        open: true,
        message: "Booking cancelled successfully ❌",
        severity: "success",
      });
      setTimeout(() => router.push("/bookings"), 1200);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      const msg = mapBackendMessageToUserFriendly(
        axiosErr.response?.data?.message
      );
      setSnackbar({ open: true, message: msg, severity: "error" });
    }
  };

  const handlePassengerChange = (field: keyof PassengerDTO, value: string) => {
    if (!passenger) return;
    setPassenger({ ...passenger, [field]: value });
  };

  const handlePassengerSave = async () => {
    if (!passenger) return;
    const errors: Record<string, string> = {};

    if (!passenger.name.trim()) errors.name = "First name cannot be empty";
    if (!/^[A-Za-zğüşöçıİĞÜŞÖÇ\s]+$/.test(passenger.name))
      errors.name = "First name must contain only letters";
    if (!passenger.surname.trim()) errors.surname = "Last name cannot be empty";
    if (!/^[A-Za-zğüşöçıİĞÜŞÖÇ\s]+$/.test(passenger.surname))
      errors.surname = "Last name must contain only letters";
    if (!passenger.email.trim()) errors.email = "Email cannot be empty";
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(passenger.email))
      errors.email = "Invalid email format";

    if (Object.keys(errors).length > 0) {
      setSnackbar({
        open: true,
        message: "Please fix the validation errors before saving.",
        severity: "error",
      });
      setFieldErrors(errors);
      return;
    }

    try {
      setSaving(true);
      await updatePassenger(passenger.id, passenger);
      setSnackbar({
        open: true,
        message: "Passenger details updated successfully ✅",
        severity: "success",
      });
      setFieldErrors({});
    } catch (err: unknown) {
      const axiosErr = err as {
        response?: {
          data?: { message?: string; errors?: Record<string, string> };
        };
      };
      const msg =
        axiosErr.response?.data?.message || "Failed to update passenger.";

      const validationErrors = axiosErr.response?.data?.errors;
      if (validationErrors) {
        setFieldErrors(validationErrors);
        setSnackbar({
          open: true,
          message: "Please correct highlighted fields.",
          severity: "error",
        });
      } else {
        setSnackbar({ open: true, message: msg, severity: "error" });
      }
    } finally {
      setSaving(false);
    }
  };

  if (loading)
    return (
      <Box display="flex" justifyContent="center" mt={10}>
        <CircularProgress />
      </Box>
    );

  if (!booking)
    return (
      <Typography textAlign="center" mt={4}>
        Booking not found.
      </Typography>
    );

  const isCancelled = booking.bookingStatus === "CANCELLED";

  return (
    <Paper sx={{ p: 4, maxWidth: 700, mx: "auto", mt: 4 }}>
      <Typography
        variant="h5"
        sx={{ mb: 3, fontWeight: 600, color: "#0D47A1" }}
      >
        {isAdmin ? "Booking Details " : "My Booking Details"}
      </Typography>

      {/* Booking Info */}
      <Box display="grid" gridTemplateColumns="repeat(2, 1fr)" gap={2}>
        <TextField
          label="Booking ID"
          value={booking.id ?? "-"}
          fullWidth
          disabled
        />
        <TextField
          label="Flight"
          value={
            flight
              ? `${flight.flightNumber} (${flight.origin} → ${flight.destination})`
              : booking.flightId ?? "-"
          }
          fullWidth
          disabled
        />
        <TextField
          label="Seat Number"
          value={booking.seatNumber ?? "-"}
          fullWidth
          disabled
        />
        <TextField
          label="Price (₺)"
          value={booking.price ? booking.price.toLocaleString() : "-"}
          fullWidth
          disabled
        />
        <TextField
          label="Status"
          value={booking.bookingStatus ?? "-"}
          fullWidth
          disabled
        />
      </Box>

      {/* Passenger Info */}
      {/* Passenger Info (only visible to admin) */}
      {isAdmin && passenger && (
        <>
          <Divider sx={{ my: 3 }} />
          <Typography
            variant="h6"
            sx={{ mb: 2, fontWeight: 500, color: "#0D47A1" }}
          >
            Passenger Information
          </Typography>

          <Box display="grid" gridTemplateColumns="repeat(2, 1fr)" gap={2}>
            <TextField
              label="Name"
              value={passenger.name ?? ""}
              onChange={(e) => handlePassengerChange("name", e.target.value)}
              fullWidth
              error={!!fieldErrors.name}
              helperText={fieldErrors.name}
            />

            <TextField
              label="Surname"
              value={passenger.surname ?? ""}
              onChange={(e) => handlePassengerChange("surname", e.target.value)}
              fullWidth
              error={!!fieldErrors.surname}
              helperText={fieldErrors.surname}
            />

            <TextField
              label="Email"
              value={passenger.email ?? ""}
              onChange={(e) => handlePassengerChange("email", e.target.value)}
              fullWidth
              error={!!fieldErrors.email}
              helperText={fieldErrors.email}
            />

            <TextField
              label="Loyalty Points"
              type="number"
              value={passenger.loyaltyPoints ?? 0}
              onChange={(e) =>
                handlePassengerChange("loyaltyPoints", e.target.value)
              }
              fullWidth
              error={!!fieldErrors.loyaltyPoints}
              helperText={fieldErrors.loyaltyPoints}
            />
          </Box>

          <Box display="flex" justifyContent="flex-end" mt={3}>
            <Button
              variant="contained"
              color="primary"
              onClick={handlePassengerSave}
              disabled={saving}
            >
              {saving ? "Saving..." : "Save Changes"}
            </Button>
          </Box>
        </>
      )}

      <Divider sx={{ my: 3 }} />

      <Box display="flex" justifyContent="space-between" mt={2}>
        <Button variant="outlined" onClick={() => router.push("/bookings")}>
          BACK
        </Button>

        {(isAdmin || isUser) && (
          <Button
            variant="contained"
            color="error"
            onClick={handleCancel}
            disabled={isCancelled}
          >
            CANCEL BOOKING
          </Button>
        )}
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
