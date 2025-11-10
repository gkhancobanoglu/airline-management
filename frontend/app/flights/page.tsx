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
} from "@mui/material";
import {
  DataGrid,
  type GridColDef,
  type GridRenderCellParams,
} from "@mui/x-data-grid";
import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import VisibilityIcon from "@mui/icons-material/Visibility";
import {
  getFlights,
  deleteFlight,
  type FlightDTO,
  type PagedResponse,
} from "@/services/flightService";
import { getAirlines, type AirlineDTO } from "@/services/airlineService";
import { mapBackendMessageToUserFriendly } from "@/utils/errorMessageMapper";
import { getUserRole, isAuthenticated } from "@/services/authService";
import { minify } from "next/dist/build/swc/generated-native";

type FlightRow = FlightDTO & {
  airlineName?: string;
};

export default function FlightsPage() {
  const router = useRouter();
  const [flights, setFlights] = useState<FlightRow[]>([]);
  const [role, setRole] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });

  const [openDialog, setOpenDialog] = useState(false);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [selectedName, setSelectedName] = useState<string>("");

  useEffect(() => {
    const initialize = async (): Promise<void> => {
      if (!isAuthenticated()) {
        router.push("/login");
        return;
      }

      const detectedRole = getUserRole();
      if (!["ADMIN", "USER"].includes(detectedRole ?? "")) {
        router.push("/");
        return;
      }

      setRole(detectedRole);
    };

    void initialize();
  }, [router]);

  useEffect(() => {
    if (!role) return;
    void fetchFlights();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [role]);

  const fetchFlights = async (): Promise<void> => {
    try {
      const flightRes = await getFlights(0, 20);
      const flightsArray =
        "content" in flightRes ? flightRes.content ?? [] : [];

      if (role === "USER") {
        setFlights(flightsArray);
        return;
      }

      const airlineRes = await getAirlines();
      const airlineList = ((airlineRes as unknown as PagedResponse<AirlineDTO>)
        ?.content ?? []) as AirlineDTO[];

      const airlineMap = new Map<number, string>(
        airlineList.map((a) => [a.id, a.name])
      );

      const rows: FlightRow[] = flightsArray.map((f) => ({
        ...f,
        airlineName: f.airlineName ?? airlineMap.get(f.airlineId) ?? "Unknown",
      }));

      setFlights(rows);
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } };
      const msg = e.response?.data?.message || "An unexpected error occurred.";
      setSnackbar({ open: true, message: msg, severity: "error" });
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = (id: number, name: string): void => {
    setSelectedId(id);
    setSelectedName(name);
    setOpenDialog(true);
  };

  const handleCloseDialog = (): void => {
    setOpenDialog(false);
    setSelectedId(null);
    setSelectedName("");
  };

  const handleConfirmDelete = async (): Promise<void> => {
    if (!selectedId) return;
    try {
      await deleteFlight(selectedId);
      setSnackbar({
        open: true,
        message: `Flight "${selectedName}" deleted successfully 🗑️`,
        severity: "success",
      });
      await fetchFlights();
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } };
      const msg =
        mapBackendMessageToUserFriendly(e.response?.data?.message) ||
        "Unexpected error";
      setSnackbar({ open: true, message: msg, severity: "error" });
    } finally {
      handleCloseDialog();
    }
  };

  const columns: GridColDef<FlightRow>[] = [
    { field: "flightNumber", headerName: "Flight No", flex: 1 },
    { field: "origin", headerName: "Origin", flex: 1.5 },
    { field: "destination", headerName: "Destination", flex: 1.5 },
    {
      field: "departureTime",
      headerName: "Departure",
      flex: 1.5,
      minWidth: 200,
      renderCell: (params: GridRenderCellParams<FlightRow>) =>
        new Date(params.row.departureTime).toLocaleString(),
    },
    {
      field: "arrivalTime",
      headerName: "Arrival",
      flex: 1.5,
      minWidth: 200,
      renderCell: (params: GridRenderCellParams<FlightRow>) =>
        new Date(params.row.arrivalTime).toLocaleString(),
    },
    {
      field: "basePrice",
      headerName: "Base Price",
      flex: 1,
      renderCell: (params: GridRenderCellParams<FlightRow>) =>
        `${params.row.basePrice} ₺`,
    },
    { field: "capacity", headerName: "Capacity", flex: 1 },
    { field: "airlineName", headerName: "Airline", flex: 1.5 },
    ...(role === "ADMIN"
      ? [
          {
            field: "actions",
            headerName: "Actions",
            flex: 1,
            minWidth: 150,
            sortable: false,
            renderCell: (params: GridRenderCellParams<FlightRow>) => (
              <>
                <IconButton
                  color="primary"
                  onClick={() => router.push(`/flights/${params.row.id}`)}
                >
                  <EditIcon />
                </IconButton>
                <IconButton
                  color="error"
                  onClick={() =>
                    handleOpenDialog(params.row.id, params.row.flightNumber)
                  }
                >
                  <DeleteIcon />
                </IconButton>
              </>
            ),
          },
        ]
      : [
          {
            field: "view",
            headerName: "View",
            flex: 0.7,
            minWidth: 50,
            sortable: false,
            renderCell: (params: GridRenderCellParams<FlightRow>) => (
              <IconButton
                color="primary"
                onClick={() => router.push(`/flights/${params.row.id}`)}
              >
                <VisibilityIcon />
              </IconButton>
            ),
          },
        ]),
  ];

  if (loading || !role)
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        height="80vh"
      >
        <Typography variant="h6" color="text.secondary">
          Loading Flights...
        </Typography>
      </Box>
    );

  return (
    <Box>
      <Typography
        variant="h4"
        sx={{ mb: 3, color: "#0D47A1", fontWeight: 600 }}
      >
        {role === "ADMIN" ? "Flights Management" : "Available Flights"}
      </Typography>

      <Paper sx={{ p: 2 }}>
        <Box display="flex" justifyContent="flex-end" mb={2}>
          {role === "ADMIN" && (
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => router.push("/flights/new")}
            >
              Add FLIGHT
            </Button>
          )}
        </Box>

        <div style={{ height: 600, width: "100%" }}>
          <DataGrid
            rows={flights}
            columns={columns}
            getRowId={(row) => row.id}
            pageSizeOptions={[5, 10]}
          />
        </div>
      </Paper>

      <Dialog open={openDialog} onClose={handleCloseDialog}>
        <DialogTitle>Confirm Deletion</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete{" "}
            <strong>{selectedName || "this flight"}</strong>? <br />
            This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button
            onClick={handleConfirmDelete}
            color="error"
            variant="contained"
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: "top", horizontal: "center" }}
      >
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
}
