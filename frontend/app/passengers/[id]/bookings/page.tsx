"use client";
import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import {
  Box,
  Button,
  Typography,
  Paper,
  CircularProgress,
  Snackbar,
  Alert,
} from "@mui/material";
import { ArrowBack } from "@mui/icons-material";
import { DataGrid, GridColDef } from "@mui/x-data-grid";
import {
  getPassengerBookings,
  PassengerBookingDTO,
} from "@/services/bookingService";
import { isAuthenticated, getUserRole } from "@/services/authService";
import { mapBackendMessageToUserFriendly } from "@/utils/errorMessageMapper";

export default function PassengerBookingsPage() {
  const router = useRouter();
  const params = useParams();
  const id = Number(params.id);
  const [bookings, setBookings] = useState<PassengerBookingDTO[]>([]);
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

    const role = getUserRole();
    if (role !== "ADMIN") {
      router.push("/flights");
      return;
    }

    const fetchData = async () => {
      try {
        const res = await getPassengerBookings(id);
        setBookings(res);
      } catch (err: unknown) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        const msg =
          mapBackendMessageToUserFriendly(axiosErr.response?.data?.message) ||
          "Error loading bookings";
        setSnackbar({ open: true, message: msg, severity: "error" });
      } finally {
        setLoading(false);
      }
    };

    void fetchData();
  }, [id, router]);

  const columns: GridColDef<PassengerBookingDTO>[] = [
    { field: "flightNumber", headerName: "Flight No", flex: 1 },
    { field: "origin", headerName: "Origin", flex: 1 },
    { field: "destination", headerName: "Destination", flex: 1 },
    {
      field: "departureTime",
      headerName: "Departure",
      flex: 1.5,
      renderCell: (params) =>
        new Date(params.row.departureTime).toLocaleString(),
    },
    {
      field: "arrivalTime",
      headerName: "Arrival",
      flex: 1.5,
      renderCell: (params) => new Date(params.row.arrivalTime).toLocaleString(),
    },
    {
      field: "price",
      headerName: "Price (₺)",
      flex: 1,
      renderCell: (params) => `${params.row.price.toLocaleString()} ₺`,
    },
    { field: "seatNumber", headerName: "Seat", flex: 0.7 },
    { field: "bookingStatus", headerName: "Status", flex: 1 },
    {
      field: "loyaltyEarned",
      headerName: "Loyalty +",
      flex: 1,
      renderCell: (params) => `+${params.row.loyaltyEarned} pts`,
    },
  ];

  if (loading)
    return (
      <Box display="flex" justifyContent="center" alignItems="center" mt={10}>
        <CircularProgress />
        <Typography sx={{ ml: 2, color: "text.secondary" }}>
          Loading flight history...
        </Typography>
      </Box>
    );

  return (
    <Paper sx={{ p: 4, mt: 4 }}>
      <Box
        display="flex"
        justifyContent="space-between"
        alignItems="center"
        mb={3}
      >
        <Typography variant="h5" sx={{ fontWeight: 600, color: "#0D47A1" }}>
          Flight History
        </Typography>
        <Button
          variant="outlined"
          startIcon={<ArrowBack />}
          onClick={() => router.push("/passengers")}
        >
          Back to Passengers
        </Button>
      </Box>

      <div style={{ height: 500, width: "100%" }}>
        <DataGrid
          rows={bookings.map((b, i) => ({ ...b, id: b.bookingId ?? i + 1 }))}
          columns={columns}
          pageSizeOptions={[5, 10]}
          initialState={{
            pagination: { paginationModel: { pageSize: 10, page: 0 } },
          }}
        />
      </div>

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
