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
import { DataGrid, GridColDef, GridRenderCellParams } from "@mui/x-data-grid";
import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import {
  getAirlines,
  deleteAirline,
  AirlineDTO,
} from "@/services/airlineService";
import { mapBackendMessageToUserFriendly } from "@/utils/errorMessageMapper";
import { getUserRole, isAuthenticated } from "@/services/authService";

export default function AirlinesPage() {
  const router = useRouter();
  const [airlines, setAirlines] = useState<AirlineDTO[]>([]);
  const [role, setRole] = useState<string | null>(null);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });
  const [loading, setLoading] = useState(true);

  const [openDialog, setOpenDialog] = useState(false);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [selectedName, setSelectedName] = useState<string>("");

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push("/login");
      return;
    }

    const r = getUserRole();
    setRole(r);

    if (r !== "ADMIN" && r !== "USER") {
      router.push("/");
      return;
    }

    const fetchData = async () => {
      try {
        const data = await getAirlines();
        setAirlines(data.content || data);
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

  const handleOpenDialog = (id: number, name: string) => {
    setSelectedId(id);
    setSelectedName(name);
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedId(null);
    setSelectedName("");
  };

  const handleConfirmDelete = async () => {
    if (!selectedId) return;

    try {
      await deleteAirline(selectedId);
      setSnackbar({
        open: true,
        message: `Airline "${selectedName}" deleted successfully 🗑️`,
        severity: "success",
      });
      const updated = await getAirlines();
      setAirlines(updated.content || updated);
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

  const columns: GridColDef<AirlineDTO>[] = [
    { field: "codeIATA", headerName: "IATA Code", flex: 1 },
    { field: "codeICAO", headerName: "ICAO Code", flex: 1 },
    { field: "name", headerName: "Name", flex: 2 },
    { field: "country", headerName: "Country", flex: 1.5 },
    { field: "fleetSize", headerName: "Fleet Size", flex: 1 },
    ...(role === "ADMIN"
      ? [
          {
            field: "actions",
            headerName: "Actions",
            flex: 1,
            sortable: false,
            renderCell: (params: GridRenderCellParams<AirlineDTO>) => (
              <>
                <IconButton
                  color="primary"
                  onClick={() => router.push(`/airlines/${params.row.id}`)}
                >
                  <EditIcon />
                </IconButton>
                <IconButton
                  color="error"
                  onClick={() =>
                    handleOpenDialog(params.row.id, params.row.name)
                  }
                >
                  <DeleteIcon />
                </IconButton>
              </>
            ),
          },
        ]
      : []),
  ];
  if (loading)
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        height="80vh"
      >
        <Typography variant="h6" color="text.secondary">
          Loading Airlines...
        </Typography>
      </Box>
    );

  return (
    <Box>
      <Typography
        variant="h4"
        sx={{ mb: 3, color: "#0D47A1", fontWeight: 600 }}
      >
        {role === "ADMIN" ? "Airlines Management" : "Available Airlines"}
      </Typography>

      <Paper sx={{ p: 2 }}>
        <Box display="flex" justifyContent="flex-end" mb={2}>
          {role === "ADMIN" && (
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => router.push("/airlines/new")}
            >
              Add AIRLINE
            </Button>
          )}
        </Box>

        <div style={{ height: 500, width: "100%" }}>
          <DataGrid
            rows={airlines}
            columns={columns}
            getRowId={(row) => row.id}
            pageSizeOptions={[5, 10]}
            initialState={{
              pagination: { paginationModel: { pageSize: 10, page: 0 } },
            }}
          />
        </div>
      </Paper>

      {/* 🧾 Silme diyaloğu sadece Admin için */}
      {role === "ADMIN" && (
        <Dialog open={openDialog} onClose={handleCloseDialog}>
          <DialogTitle>Confirm Deletion</DialogTitle>
          <DialogContent>
            <DialogContentText>
              Are you sure you want to delete{" "}
              <strong>{selectedName || "this airline"}</strong>? <br />
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
      )}

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
