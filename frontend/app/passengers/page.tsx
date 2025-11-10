"use client";
import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import {
  Box,
  Button,
  Typography,
  Snackbar,
  Alert,
  Paper,
  IconButton,
  Tooltip,
  CircularProgress,
} from "@mui/material";
import { DataGrid, GridColDef } from "@mui/x-data-grid";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import VisibilityIcon from "@mui/icons-material/Visibility";
import RefreshIcon from "@mui/icons-material/Refresh";
import { getPassengers, PassengerDTO } from "@/services/passengerService";
import { getUserRole, isAuthenticated, getToken } from "@/services/authService";
import { mapBackendMessageToUserFriendly } from "@/utils/errorMessageMapper";

export default function PassengersPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [passengers, setPassengers] = useState<PassengerDTO[]>([]);
  const [role, setRole] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });

  const fetchPassengers = async () => {
    setLoading(true);
    try {
      const passengersData = await getPassengers();
      setPassengers(passengersData);
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

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push("/login");
      return;
    }

    const userRole = getUserRole();
    setRole(userRole);

    if (userRole !== "ADMIN") {
      router.push("/flights");
      return;
    }

    void fetchPassengers();
  }, [router]);

  useEffect(() => {
    if (searchParams.get("refresh") === "1") {
      void fetchPassengers();
    }
  }, [searchParams]);

  if (loading)
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        height="80vh"
      >
        <CircularProgress />
        <Typography sx={{ ml: 2, color: "text.secondary" }}>
          Loading Passengers...
        </Typography>
      </Box>
    );

  if (role !== "ADMIN") return null;

  const columns: GridColDef<PassengerDTO>[] = [
    { field: "name", headerName: "First Name", flex: 1 },
    { field: "surname", headerName: "Last Name", flex: 1 },
    { field: "email", headerName: "Email", flex: 2 },
    {
      field: "loyaltyPoints",
      headerName: "Loyalty Points",
      flex: 1,
      renderCell: (params) => {
        const points = params.row.loyaltyPoints ?? 0;
        return (
          <Typography
            sx={{
              fontWeight: 600,
              color: points > 1000 ? "green" : "text.primary",
            }}
          >
            {points} pts
          </Typography>
        );
      },
    },
    {
      field: "actions",
      headerName: "Actions",
      flex: 1.5,
      sortable: false,
      renderCell: (params) => (
        <>
          <Tooltip title="Edit Passenger">
            <IconButton
              color="primary"
              onClick={() => router.push(`/passengers/${params.row.id}`)}
            >
              <EditIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title="View Flight History">
            <IconButton
              color="info"
              onClick={() =>
                router.push(`/passengers/${params.row.id}/bookings`)
              }
            >
              <VisibilityIcon />
            </IconButton>
          </Tooltip>
        </>
      ),
    },
  ];

  return (
    <Box>
      <Typography
        variant="h4"
        sx={{ mb: 3, color: "#0D47A1", fontWeight: 600 }}
      >
        Passengers Management
      </Typography>

      <Paper sx={{ p: 2 }}>
        <Box
          display="flex"
          justifyContent="space-between"
          alignItems="center"
          mb={2}
        >
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => router.push("/passengers/new")}
          >
            Add Passenger
          </Button>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={() => fetchPassengers()}
          >
            Refresh
          </Button>
        </Box>

        <div style={{ height: 600, width: "100%" }}>
          <DataGrid
            rows={passengers}
            columns={columns}
            getRowId={(row) => row.id}
            pageSizeOptions={[5, 10]}
            initialState={{
              pagination: { paginationModel: { pageSize: 10, page: 0 } },
            }}
          />
        </div>
      </Paper>

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
