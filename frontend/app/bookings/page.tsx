"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  Box,
  Button,
  Typography,
  Snackbar,
  Alert,
  Paper,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  CircularProgress,
} from "@mui/material";
import { DataGrid, GridColDef } from "@mui/x-data-grid";
import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";
import VisibilityIcon from "@mui/icons-material/Visibility";
import {
  getAdminBookings,
  getMyBookings,
  cancelBooking,
  type BookingAdminDTO,
  type PassengerBookingDTO,
} from "@/services/bookingService";
import { getUserRole, isAuthenticated } from "@/services/authService";
import { mapBackendMessageToUserFriendly } from "@/utils/errorMessageMapper";

export default function BookingsPage() {
  const router = useRouter();
  const [role, setRole] = useState<string | null>(null);
  const [bookings, setBookings] = useState<
    (BookingAdminDTO | PassengerBookingDTO)[]
  >([]);
  const [loading, setLoading] = useState(true);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });
  const [openDialog, setOpenDialog] = useState(false);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [selectedSeat, setSelectedSeat] = useState<string>("");

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push("/login");
      return;
    }

    const r = getUserRole();
    setRole(r);

    const fetchData = async () => {
      try {
        if (r === "ADMIN") {
          const res = await getAdminBookings();
          setBookings("content" in res ? res.content : res);
        } else if (r === "USER") {
          const res = await getMyBookings();
          setBookings(res);
        }
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

    void fetchData();
  }, [router]);

  const handleOpenDialog = (id: number, seat: string) => {
    setSelectedId(id);
    setSelectedSeat(seat);
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedId(null);
    setSelectedSeat("");
  };

  const handleConfirmCancel = async () => {
    if (!selectedId) return;
    try {
      await cancelBooking(selectedId);
      setSnackbar({
        open: true,
        message: `Booking for seat "${selectedSeat}" cancelled successfully 🛑✈️`,
        severity: "success",
      });
      if (role === "ADMIN") {
        const res = await getAdminBookings();
        setBookings("content" in res ? res.content : res);
      } else {
        const res = await getMyBookings();
        setBookings(res);
      }
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      const msg = mapBackendMessageToUserFriendly(
        axiosErr.response?.data?.message
      );
      setSnackbar({ open: true, message: msg, severity: "error" });
    } finally {
      handleCloseDialog();
    }
  };

  const adminColumns: GridColDef<BookingAdminDTO>[] = [
    { field: "flightNumber", headerName: "Flight No", flex: 1 },
    { field: "origin", headerName: "Origin", flex: 1 },
    { field: "destination", headerName: "Destination", flex: 1 },
    { field: "passengerName", headerName: "Passenger", flex: 1 },
    { field: "seatNumber", headerName: "Seat", flex: 0.8 },
    { field: "bookingStatus", headerName: "Status", flex: 1 },
    {
      field: "price",
      headerName: "Price (₺)",
      flex: 1,
      valueGetter: (_v, row) => `${row.price.toLocaleString()} ₺`,
    },
    {
      field: "actions",
      headerName: "Actions",
      flex: 1,
      sortable: false,
      renderCell: (params) => (
        <>
          <IconButton
            color="primary"
            onClick={() => router.push(`/bookings/${params.row.id}`)}
          >
            <VisibilityIcon />
          </IconButton>
          {params.row.bookingStatus === "CONFIRMED" && (
            <IconButton
              color="error"
              onClick={() =>
                handleOpenDialog(params.row.id, params.row.seatNumber)
              }
            >
              <DeleteIcon />
            </IconButton>
          )}
        </>
      ),
    },
  ];

  const userColumns: GridColDef<PassengerBookingDTO>[] = [
    { field: "flightNumber", headerName: "Flight No", flex: 1 },
    { field: "origin", headerName: "Origin", flex: 1 },
    { field: "destination", headerName: "Destination", flex: 1 },
    { field: "seatNumber", headerName: "Seat", flex: 0.8 },
    { field: "bookingStatus", headerName: "Status", flex: 1 },
    {
      field: "price",
      headerName: "Price (₺)",
      flex: 1,
      valueGetter: (_v, row) => `${row.price.toLocaleString()} ₺`,
    },
    {
      field: "actions",
      headerName: "Actions",
      flex: 1,
      sortable: false,
      renderCell: (params) => (
        <>
          <IconButton
            color="primary"
            onClick={() => router.push(`/bookings/${params.row.bookingId}`)}
          >
            <VisibilityIcon />
          </IconButton>
          {params.row.bookingStatus === "CONFIRMED" && (
            <IconButton
              color="error"
              onClick={() =>
                handleOpenDialog(params.row.bookingId, params.row.seatNumber)
              }
            >
              <DeleteIcon />
            </IconButton>
          )}
        </>
      ),
    },
  ];

  if (loading)
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        height="80vh"
      >
        <CircularProgress />
        <Typography variant="h6" color="text.secondary" sx={{ ml: 2 }}>
          Loading Bookings...
        </Typography>
      </Box>
    );

  return (
    <Box>
      <Typography
        variant="h4"
        sx={{ mb: 3, color: "#0D47A1", fontWeight: 600 }}
      >
        {role === "ADMIN" ? "All Bookings" : "My Bookings"}
      </Typography>

      <Paper sx={{ p: 2 }}>
        <Box display="flex" justifyContent="flex-end" mb={2}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => router.push("/bookings/new")}
          >
            New BOOKING
          </Button>
        </Box>

        <div style={{ height: 600, width: "100%" }}>
          <DataGrid
            rows={bookings}
            columns={
              (role === "ADMIN" ? adminColumns : userColumns) as GridColDef<
                BookingAdminDTO | PassengerBookingDTO
              >[]
            }
            getRowId={(row) => ("id" in row ? row.id : row.bookingId)}
            pageSizeOptions={[5, 10]}
            initialState={{
              pagination: { paginationModel: { pageSize: 10, page: 0 } },
            }}
          />
        </div>
      </Paper>

      <Dialog open={openDialog} onClose={handleCloseDialog}>
        <DialogTitle>Confirm Cancellation</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to cancel the booking for seat{" "}
            <strong>{selectedSeat || "this booking"}</strong>? <br />
            This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Keep Bookıng</Button>
          <Button
            onClick={handleConfirmCancel}
            color="error"
            variant="contained"
          >
            Cancel Bookıng
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: "top", horizontal: "center" }}
        sx={{ mt: 3 }}
      >
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
}
